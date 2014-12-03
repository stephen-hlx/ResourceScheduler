/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.processor;

import org.apache.log4j.Logger;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public class MessageProcessor extends AbsMessageProcessor {

  private static final Logger logger = Logger.getLogger("ProcessorLogger");
  
  public MessageProcessor() {
    super();
  }

  @Override
  public void processMessage() {
    logger.info(this.toString()
        + " Processing message " + message);
    try {
      Thread.sleep(message.getWorkLoad());
    } catch (InterruptedException E) {}
    logger.info(this.toString()
        + " message process completed " + message);
  }

}
