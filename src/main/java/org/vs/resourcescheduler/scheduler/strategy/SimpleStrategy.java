/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.scheduler.strategy;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.vs.resourcescheduler.message.IMessage;

/**This strategy simply employs a FIFO strategy
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public class SimpleStrategy implements IStrategy {

  public SimpleStrategy() {
    
  }
  
  @Override
  public IMessage getNextMessage(Map<String, Long> groupHistory, Map<String, GroupStatus> groupStatus, List<IMessage> list) {
    if (null == list) {
      return null;
    }
    
    IMessage message = null;
    Iterator<IMessage> iterator = list.iterator();
    if (iterator.hasNext()) {
      message = iterator.next();
      iterator.remove();
    }
    
    return message;
  }

  @Override
  public boolean hasNextMessage(Map<String, Long> groupHistory,
      Map<String, GroupStatus> groupStatus,
      List<IMessage> list) {
    // TODO Auto-generated method stub
    return list.size() > 0;
  }


}
