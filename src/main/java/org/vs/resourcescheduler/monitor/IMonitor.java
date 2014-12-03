package org.vs.resourcescheduler.monitor;

public interface IMonitor {
  
  void setInterval(int interval);
  
  // no locking will be used in this implementation so the information may not correct
  String getWeakSnapshot();
  
  // this would not work if synchronisation is enforced on a member of the subject
  String getStrongSnapshot();
  
  void setLogger(String loggerName);
}
