package org.vs.resourcescheduler.scheduler;

public abstract class AbsResourceScheduler implements IResourceScheduler {
  
  private static int idCount = 0;
  
  private int id;
  
  protected static synchronized int getNextId() {
    return idCount ++;
  }
  
  public AbsResourceScheduler() {
    this.id = getNextId();
  }
  
  public int getId() {
    return id;
  }
  
  public String toString() {
    return "[scheduler " + id + "] Thread " + Thread.currentThread().getId();
  }

}
