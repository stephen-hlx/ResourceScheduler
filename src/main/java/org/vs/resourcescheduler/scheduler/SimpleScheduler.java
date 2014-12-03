/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.scheduler;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.gateway.IGateway;
import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.scheduler.exception.TerminatedGroupMessageException;
import org.vs.resourcescheduler.scheduler.strategy.IStrategy;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public class SimpleScheduler extends AbsResourceScheduler {

  private final static Logger logger = Logger.getLogger(SimpleScheduler.class);

  private boolean isShutdown = false;

  public SimpleScheduler(IGateway gateway) {
    super();
  }

  public void shutdown() {
    isShutdown = true;
  }

  @Override
  public void run() {
//    while (!isShutdown) {
//      if (getGateway().isAvailable()) {
//        logger.debug("gateway " + getGateway() + " is available");
//        if (getMessageQ().size() > 0) {
//          IMessage message = getNextMessage();
//          logger.debug("sending message: " + message);
//          getGateway().send(message);
//        }
//      }
//    }
  }

  @Override
  public void cancelGroup(String GroupID) {
    return;
  }

  @Override
  public void completed(IMessage message) {
    logger.debug("message process completed: " + message);
    run();
  }

  @Override
  public void receiveMessage(IMessage message) throws TerminatedGroupMessageException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void setStrategy(IStrategy strategy) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getSnapShot() {
    // TODO Auto-generated method stub
    return null;
  }

}
