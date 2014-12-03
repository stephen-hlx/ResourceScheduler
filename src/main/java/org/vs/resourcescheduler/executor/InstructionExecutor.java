package org.vs.resourcescheduler.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.SimpleThreadFactory;
import org.vs.resourcescheduler.gateway.Gateway;
import org.vs.resourcescheduler.gateway.IGateway;
import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.monitor.DaemonMonitor;
import org.vs.resourcescheduler.monitor.IMonitorable;
import org.vs.resourcescheduler.scheduler.IResourceScheduler;
import org.vs.resourcescheduler.scheduler.ResourceScheduler;
import org.vs.resourcescheduler.scheduler.exception.TerminatedGroupMessageException;
import org.vs.resourcescheduler.scheduler.strategy.GroupPrioritisedStrategy;
import org.vs.resourcescheduler.scheduler.strategy.IStrategy;
import org.vs.resourcescheduler.scheduler.strategy.SimpleStrategy;

public class InstructionExecutor {
  private final static Logger logger = Logger.getLogger(InstructionExecutor.class);
  
  Map<String, IResourceScheduler> schedulers;
  Map<String, IStrategy> strategies;
  IGateway gateway = null;
  ExecutorService executor;
  
  public InstructionExecutor() {
    schedulers = new HashMap<String, IResourceScheduler>();
    strategies = new HashMap<String, IStrategy>();
    executor = Executors.newCachedThreadPool(new SimpleThreadFactory());
  }
  
  public void gatewayInit(int size) {
    logger.info("initialising gateway with size " + size);
    gateway = Gateway.getInstance();
    gateway.init(size);
    DaemonMonitor dt = new DaemonMonitor(gateway, 1000, true);
    dt.setLogger("GatewayMonitorLogger");
    dt.start();
  }
  
  public void gatewaySetSize(int size) {
    logger.info("setting gateway size to " + size);
    gateway.setSize(size);
  }
  
  public void gatewayShutdown() {
    gateway.shutdown();
  }
  
  public void createStrategy(String id, String type) throws InvalidInstructionException {
    logger.info("creating strategy " + type);
    IStrategy strategy;
    
    switch(type) {
      case "SimpleStrategy":
        strategy = new SimpleStrategy();
        break;
      case "GroupPrioritisedStrategy":
        strategy = new GroupPrioritisedStrategy();
        break;
      default:
        logger.fatal("unknow strategy type: " + type);
        throw new InvalidInstructionException("unknow strategy type: " + type);
    }
    
    strategies.put(id, strategy);
  }
  
  public void schedulerInit(String schedulerId, String type, String strategyId) throws InvalidInstructionException {
    logger.info("initialising scheduler with id " + schedulerId + " with strategy id " + strategyId);
    
    if (null == gateway) {
      logger.fatal("gateway not found!");
      throw new InvalidInstructionException("gateway not found!");
    }
    
    IStrategy strategy = strategies.get(strategyId);
    if (null == strategy) {
      logger.fatal("strategy " + schedulerId + " not found!");
      throw new InvalidInstructionException("strategy " + schedulerId + " not found!");
    }
    
    IResourceScheduler scheduler = schedulers.get(schedulerId);
    if (null != scheduler) {
      logger.fatal("scheulder " + schedulerId + " already exists");
      throw new InvalidInstructionException("scheulder " + schedulerId + " already exists");
    }
    
    switch(type) {
      case "ResourceScheduler":
        scheduler = new ResourceScheduler(gateway, strategy);
        DaemonMonitor dt = new DaemonMonitor(scheduler, 1000, true);
        dt.setLogger("SchedulerMonitorLogger");
        dt.start();
        schedulers.put(schedulerId, scheduler);
        break;
      default:
        logger.fatal("unknow scheduler type: " + type);
        throw new InvalidInstructionException("unknow scheduler type: " + type);
    }
    
    executor.execute(schedulers.get(schedulerId));
  }
  
  public void schedulerSetStrategy(String schedulerId, String strategyId) throws InvalidInstructionException {
    IStrategy strategy = strategies.get(strategyId);
    if (null == strategy) {
      logger.fatal("strategy " + strategyId + " not found!");
      throw new InvalidInstructionException("strategy " + strategyId + " not found!");
    }
    
    IResourceScheduler scheduler = schedulers.get(schedulerId);
    if (null == scheduler) {
      logger.fatal("scheduler " + schedulerId + " not found!");
      throw new InvalidInstructionException("scheduler " + schedulerId + " not found!");
    }
    
    scheduler.setStrategy(strategy);
  }

  public void schedulerCancelGroup(String schedulerId, String groupId)
      throws InvalidInstructionException {

    IResourceScheduler scheduler = schedulers.get(schedulerId);
    if (null == scheduler) {
      logger.fatal("scheduler " + schedulerId + " not found!");
      throw new InvalidInstructionException("scheduler " + schedulerId + " not found!");
    }

    scheduler.cancelGroup(groupId);
  }
  
  public void schedulerShutdown(String schedulerId) throws InvalidInstructionException {
    IResourceScheduler scheduler = schedulers.get(schedulerId);
    if (null == scheduler) {
      logger.fatal("scheduler " + schedulerId + " not found!");
      throw new InvalidInstructionException("scheduler " + schedulerId + " not found!");
    }
    
    scheduler.shutdown();
  }
  
  public void mainWait(long length) {
    logger.info("wait");
    try {
      Thread.sleep(length);
    } catch (InterruptedException e) {
      logger.fatal("main() should never be interrupted");
    }
  }
  
  public void mainSendMessage(String schedulerId, IMessage message) {
    logger.info(message);
    try {
      schedulers.get(schedulerId).receiveMessage(message);
    } catch (TerminatedGroupMessageException e) {
      logger.fatal("message received from a terminated group");
    }
  }
}
