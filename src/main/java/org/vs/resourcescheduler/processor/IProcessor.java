/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.processor;

import org.vs.resourcescheduler.message.IMessage;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public interface IProcessor extends Runnable {
  void processMessage();
  boolean isIdle();
  void setMessage(IMessage message);
  void shutdown();
}
