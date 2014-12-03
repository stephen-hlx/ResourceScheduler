package org.vs.resourcescheduler.monitor;

import org.apache.log4j.Logger;

public class DaemonMonitor extends Thread implements IMonitor {

  private Logger logger;

  protected IMonitorable subject;
  protected int interval;
  protected boolean isStrong;

  public DaemonMonitor(IMonitorable subject, int interval) {
    this(subject, interval, false);
  }

  public DaemonMonitor(IMonitorable subject, int interval, boolean isStrong) {
    this.setDaemon(true);
    this.subject = subject;
    this.interval = interval;
    this.isStrong = isStrong;
  }

  @Override
  public void run() {
    while (!isInterrupted()) {
      if (isStrong) {
        logger.debug(getStrongSnapshot());
      } else {
        logger.debug(getWeakSnapshot());
      }
      try {
        Thread.sleep(interval);
      } catch (InterruptedException e) {
        logger.debug("monitor " + this.toString() + " was interrupted");
      }
    }

  }

  @Override
  public void setInterval(int interval) {
    this.interval = interval;
  }

  @Override
  public String getWeakSnapshot() {
    return subject.getSnapShot();
  }

  @Override
  public String getStrongSnapshot() {
    synchronized (subject) {
      return subject.getSnapShot();
    }
  }

  public String toString() {
    return "Monitor [" + subject.toString() + "]";
  }

  @Override
  public void setLogger(String loggerName) {
    this.logger = Logger.getLogger(loggerName);    
  }

}
