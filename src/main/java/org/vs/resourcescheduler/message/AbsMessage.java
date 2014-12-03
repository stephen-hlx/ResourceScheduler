package org.vs.resourcescheduler.message;

import org.vs.resourcescheduler.scheduler.IResourceScheduler;

public abstract class AbsMessage implements IMessage {
  
  private static int nextID = 0;

  private final int id;
  
  // payload
  protected String message;
  // unit of measure if 100ms
  protected long workLoad;
  
  protected IResourceScheduler scheduler;
  
  private static synchronized int getNextID() {
    return nextID ++;
  }
  
  protected AbsMessage() {
    id = getNextID();
  }
  
  public int getID() {
    return id;
  }
  
  @Override
  public void setResourceScheduler(IResourceScheduler scheduler) {
    this.scheduler = scheduler;
  }

  @Override
  public IResourceScheduler getResourceScheduler() {
    return scheduler;
  }

  @Override
  public void completed() {
    scheduler.completed(this);
  }

  @Override
  public String getMessage() {
    return message;
  }
  
  // UOM is 100 ms
  public long getWorkLoad() {
    return workLoad;
  }

  @Override
  public void setWorkLoad(long workLoad) {
    this.workLoad = workLoad;
  }
  

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    sb.append("message_" + getID() + " ");
    sb.append("group_" + getGroupID() + " ");
    sb.append("workload=" + getWorkLoad() + " ");
    sb.append("isTermination: " + isTermination());
    sb.append("]");
    return sb.toString();
  }
}
