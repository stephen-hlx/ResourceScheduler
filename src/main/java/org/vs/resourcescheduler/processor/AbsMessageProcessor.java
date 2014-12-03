/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.processor;

import java.util.Observable;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.message.IMessage;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public abstract class AbsMessageProcessor extends Observable implements IProcessor {
  
  private static final Logger logger = Logger.getLogger("ProcessorLogger");
  
  private static int idCount = 0;
  
  private int id;
  private boolean isShutdown = false;
  protected volatile IMessage message = null;
  
  private static synchronized int getNextId() {
    return idCount ++;
  }
  
  public AbsMessageProcessor() {
    this.id = getNextId();
  }
  
  public void setMessage(IMessage message) {
    this.message = message;
  }
  
  public boolean isIdle() {
    return null == message && !isShutdown;
  }
  
  @Override
  public void shutdown() {
    isShutdown = true;
  }

  public void run() {
    logger.debug(this.toString()
        + " process starts working");
    IMessage messageHolder = message;
    if (null != message) {
      processMessage();
      message.completed();
      setChanged();
      message = null;
    } else {
      logger.fatal(this.toString()
          + " should never reach here");
    }
    logger.debug(this.toString()
        + " process completes");
    notifyObservers(messageHolder);
  }
  
  public String toString() {
    return "[Processor " + id + "] Thread " + Thread.currentThread().getId();
  }
}
