/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.scheduler.strategy;

import java.util.List;
import java.util.Map;

import org.vs.resourcescheduler.message.IMessage;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public interface IStrategy {
  
  public static enum GroupStatus {
    NOT_RUNNING,
    RUNNING,
    CANCELLED,
    TERMINATED
  }
  
  IMessage getNextMessage(Map<String, Long> groupHistory,
      Map<String, GroupStatus> groupStatus,
      List<IMessage> list);
  
  boolean hasNextMessage(Map<String, Long> groupHistory,
      Map<String, GroupStatus> groupStatus,
      List<IMessage> list);
}
