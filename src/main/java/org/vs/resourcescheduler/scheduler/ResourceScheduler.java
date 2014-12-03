/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.scheduler;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.gateway.IGateway;
import org.vs.resourcescheduler.gateway.exception.InvalidGatewayStateException;
import org.vs.resourcescheduler.gateway.exception.NoResourceAllocatedException;
import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.scheduler.exception.GatewayUnavailableException;
import org.vs.resourcescheduler.scheduler.exception.TerminatedGroupMessageException;
import org.vs.resourcescheduler.scheduler.strategy.IStrategy;
import org.vs.resourcescheduler.scheduler.strategy.IStrategy.GroupStatus;
import org.vs.resourcescheduler.scheduler.strategy.SimpleStrategy;
import org.vs.resourcescheduler.timestampgenerator.ITimestampGenerator;
import org.vs.resourcescheduler.timestampgenerator.SimpleTimestampGenerator;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public class ResourceScheduler extends AbsResourceScheduler {

  private final static Logger logger = Logger.getLogger("SchedulerLogger");

  private ITimestampGenerator timestampGenerator = new SimpleTimestampGenerator();

  private boolean shutdown = false;
  private List<IMessage> messageQ;

  private IStrategy strategy;
  private IGateway gateway;

  // Group delivery history <GroupID, deliveryTimeStamp>
  private Map<String, Long> groupHistory;

  private Map<String, GroupStatus> groupStatus;

  // Group message processed
  private Map<String, Long> groupMessageCount;

  public void setStrategy(IStrategy strategy) {
    this.strategy = strategy;
  }

  public ResourceScheduler(IGateway gateway) {
    this(gateway, new SimpleStrategy());
  }

  private synchronized IMessage getNextMessage() {
    IMessage message = strategy.getNextMessage(groupHistory, groupStatus, messageQ);
    return message;
  }

  public ResourceScheduler(IGateway gateway, IStrategy strategy) {
    logger.debug("initialising scheduler");
    this.gateway = gateway;
    this.strategy = strategy;

    messageQ = new LinkedList<IMessage>();
    groupHistory = new LinkedHashMap<String, Long>();
    groupStatus = new LinkedHashMap<String, GroupStatus>();
    groupMessageCount = new HashMap<String, Long>();
    logger.debug("scheduler initialised");
  }

  @Override
  public synchronized void receiveMessage(IMessage message) throws TerminatedGroupMessageException {
    long timestamp = timestampGenerator.getTimestamp();
    logger.debug("message received at timestamp: " + timestamp);
    logger.debug("message: " + message);

    if (GroupStatus.TERMINATED == groupStatus.get(message.getGroupID())) {
      logger.fatal("message received from terminated group " + message.getGroupID());
      throw new TerminatedGroupMessageException();
    }

    if (GroupStatus.CANCELLED == groupStatus.get(message.getGroupID())) {
      // disgard messages from cancelled groups
      logger.debug("message from cancelled group " + message.getGroupID() + " disgarded");
      return;
    }

    // record the delivery time of the first message from a new group
    // and set the group status as not running
    if (!groupHistory.containsKey(message.getGroupID())) {
      groupHistory.put(message.getGroupID(), timestamp);
      groupStatus.put(message.getGroupID(), GroupStatus.NOT_RUNNING);
    }

    message.setResourceScheduler(this);
    messageQ.add(message);
    notify();
    logger.debug("message receive completed");
  }

  /**
   * notify? if it is a terminating message ...
   */
  @Override
  public synchronized void completed(IMessage message) {
    if (message.isTermination()) {
      groupStatus.put(message.getGroupID(), GroupStatus.TERMINATED);
      groupHistory.remove(message.getGroupID());
    } else {
      String groupId = message.getGroupID();
      if (groupStatus.get(groupId) != GroupStatus.CANCELLED) {
        groupStatus.put(groupId, GroupStatus.NOT_RUNNING);
      }
      Long count = groupMessageCount.get(message.getGroupID());
      if (null == count) {
        groupMessageCount.put(groupId, new Long(1));
      } else {
        groupMessageCount.put(groupId, new Long(count.longValue() + 1));
      }

    }
    notifyAll();
  }

  @Override
  public synchronized void cancelGroup(String GroupID) {

    logger.debug("Thread " + Thread.currentThread().getId() + " cancelling message group "
        + GroupID);

    // removing messages from the queue
    List<IMessage> cancelledGroup = new LinkedList<IMessage>();
    for (IMessage message : messageQ) {
      if (message.getGroupID().equalsIgnoreCase(GroupID)) {
        cancelledGroup.add(message);
      }
    }
    messageQ.removeAll(cancelledGroup);
    groupStatus.put(GroupID, GroupStatus.CANCELLED);
    logger.debug("message group " + GroupID + " cancelled");

    // if a group is cancelled, other groups may be eligible for process
    notifyAll();
  }

  @Override
  public void shutdown() {
    logger.debug("shutting down");
    shutdown = true;
  }

  @Override
  public void run() {
    logger.info("scheduler started");
    while (!shutdown) {
      synchronized (this) {
        while (!hasNextMessage()) {
          try {
            logger.debug("waiting for new incoming messages");
            wait();
          } catch (InterruptedException e) {
            logger.info("scheduler interrupted, and now is shutting down");
            shutdown();
            return;
          }
        }
      }
      try {
        logger.debug(this.toString() + " requesting resource");
        gateway.acquireResource(this);
        logger.debug(this.toString() + " resource acquired");
        sendMessage();
      } catch (GatewayUnavailableException E) {
        logger.fatal("Gateway is unavailable");
        shutdown();
      } catch (NoResourceAllocatedException E) {
        logger.fatal("No Resource Allocated for scheduler " + toString());
        shutdown();
      } catch (TerminatedGroupMessageException E) {
        logger.fatal("message from terminated group received, scheduler is now shutting down");
        shutdown();
      } catch (InterruptedException e) {
        logger.info("scheduler interrupted, and now is shutting down");
        shutdown();
      } catch (InvalidGatewayStateException e) {
        logger.fatal("Gateway is at an invalid state.");
        shutdown();
      }
    }
  }

  private synchronized boolean hasNextMessage() {
    return strategy.hasNextMessage(groupHistory, groupStatus, messageQ);
  }

  private synchronized void sendMessage() throws NoResourceAllocatedException,
      GatewayUnavailableException, InterruptedException, TerminatedGroupMessageException {

    IMessage message = getNextMessage();
    if (null == message) {
      logger.debug("no message is to be sent, probably due to a change of strategy");
      return;
    }
    logger.debug(this.toString() + " is about to send " + message);
    messageQ.remove(message);
    gateway.send(message);
    logger.debug(message + " sent by " + this.toString());
    groupStatus.put(message.getGroupID(), GroupStatus.RUNNING);
  }

  @Override
  public String getSnapShot() {
    int i = 0;
    for (GroupStatus status : groupStatus.values()) {
      if (GroupStatus.RUNNING == status) {
        i++;
      }
    }
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append("################\n");
    sb.append("scheduler id: " + getId() + "\n");
    sb.append("Gateway Status: " + gateway.isAvailable() + "\n");
    sb.append("messageQ size: " + messageQ.size() + "\n");
    sb.append("strategy: " + strategy.getClass().getName() + "\n");
    sb.append("gateway: " + gateway.toString() + "\n");
    sb.append("number of running groups: " + i + "\n");
    sb.append("groupStatus: (sorted by timestampe)\n");
    for (Map.Entry<String, GroupStatus> kvPair : groupStatus.entrySet()) {
      sb.append("Group_" + kvPair.getKey() + " : " + "status " + kvPair.getValue());
      sb.append("\t" + "message processed:\t" + groupMessageCount.get(kvPair.getKey()) + "\n");
    }
    sb.append("groupHistory: (sorted by timestampe)\n");
    for (Map.Entry<String, Long> kvPair : groupHistory.entrySet()) {
      sb.append("Group_" + kvPair.getKey() + " : " + "timestamp " + kvPair.getValue() + "\n");
    }
    sb.append("################\n");
    sb.append("\n");
    return sb.toString();
  }

}
