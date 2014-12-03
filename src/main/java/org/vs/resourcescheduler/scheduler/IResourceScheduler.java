/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.scheduler;

import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.monitor.IMonitorable;
import org.vs.resourcescheduler.scheduler.exception.TerminatedGroupMessageException;
import org.vs.resourcescheduler.scheduler.strategy.IStrategy;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public interface IResourceScheduler extends Runnable, IMonitorable {
  void receiveMessage(IMessage message) throws TerminatedGroupMessageException;
  void completed(IMessage message);
  void cancelGroup(String GroupID);
  void shutdown();
  void setStrategy(IStrategy strategy);
  int getId();
}
