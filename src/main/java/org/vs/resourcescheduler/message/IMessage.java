/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.message;

import org.vs.resourcescheduler.scheduler.IResourceScheduler;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public interface IMessage {
  void setResourceScheduler(IResourceScheduler scheduler);
  IResourceScheduler getResourceScheduler();
  void completed();
  String getMessage();
  String getGroupID();
  boolean isTermination();
  void setWorkLoad(long workload);
  long getWorkLoad();
  int getID();
}
