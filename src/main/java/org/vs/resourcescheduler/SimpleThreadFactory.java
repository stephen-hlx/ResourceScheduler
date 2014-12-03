package org.vs.resourcescheduler;

import java.util.concurrent.ThreadFactory;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import org.apache.log4j.Logger;

public class SimpleThreadFactory implements ThreadFactory {

  private static final Logger logger = Logger.getLogger(SimpleThreadFactory.class);
  
  @Override
  public Thread newThread(Runnable r) {
    logger.debug(this + "creating new thread");
    Thread t = new Thread(r);
    logger.debug(this + "thread created: " + t);
    t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
      private final Logger logger = Logger.getLogger("ThreadExceptionHandler");
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        logger.fatal("Exception caught: " + e);
        logger.fatal(pw.toString());
      }
    });
    return t;
  }

}
