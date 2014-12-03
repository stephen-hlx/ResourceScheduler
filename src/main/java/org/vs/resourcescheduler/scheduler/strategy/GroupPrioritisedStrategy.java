/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.scheduler.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.message.IMessage;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public class GroupPrioritisedStrategy implements IStrategy {

  private final static Logger logger = Logger.getLogger(GroupPrioritisedStrategy.class);

  private class GroupPrioritisedComparator implements
      Comparator<Map.Entry<String, List<IMessage>>> {

    @Override
    public int compare(Entry<String, List<IMessage>> o1, Entry<String, List<IMessage>> o2) {
      long l1 = groupHistory.get(o1.getKey()).longValue();
      long l2 = groupHistory.get(o2.getKey()).longValue();

      if (o1 == o2 || l1 == l2) {
        return 0;
      } else if (l1 < l2) {
        return -1;
      }
      return 1;
    }

  }

  // Group delivery history <GroupID, deliveryTimeStamp>
  private Map<String, Long> groupHistory;

  private Map<String, GroupStatus> groupStatus;

  public GroupPrioritisedStrategy() {}

  @Override
  public IMessage getNextMessage(Map<String, Long> groupHistory,
      Map<String, GroupStatus> groupStatus, List<IMessage> list) {
    return _getNextMessage(groupHistory, groupStatus, list);
  }

  private IMessage _getNextMessage(Map<String, Long> groupHistory,
      Map<String, GroupStatus> groupStatus, List<IMessage> list) {
    this.groupHistory = groupHistory;
    this.groupStatus = groupStatus;

    Map<String, List<IMessage>> groupedMessage = new HashMap<String, List<IMessage>>();
    String groupID = null;
    for (int i = 0; i < list.size(); i++) {
      groupID = list.get(i).getGroupID();
      if (null == groupedMessage.get(groupID)) {
        groupedMessage.put(groupID, new LinkedList<IMessage>());
      }
      groupedMessage.get(groupID).add(list.get(i));
    }

    List<Map.Entry<String, List<IMessage>>> orderedGroupedMessage =
        new ArrayList<Map.Entry<String, List<IMessage>>>();

    orderedGroupedMessage.addAll(groupedMessage.entrySet());

    Collections.sort(orderedGroupedMessage, this.new GroupPrioritisedComparator());

    for (int i = 0; i < orderedGroupedMessage.size(); i++) {
      groupID = orderedGroupedMessage.get(i).getKey();
      if (groupStatus.get(groupID) == GroupStatus.NOT_RUNNING) {
        IMessage message = orderedGroupedMessage.get(i).getValue().get(0);
        if (null == message) {
          continue;
        }
        return message;
      }
    }
    return null;
  }

  public static void main(String[] args) {
    GroupPrioritisedStrategy s = new GroupPrioritisedStrategy();
    Map<String, Long> groupHistory = new HashMap<String, Long>();
    groupHistory.put("group1", new Long(1));
    groupHistory.put("group2", new Long(3));
    groupHistory.put("group3", new Long(2));

    s.getNextMessage(groupHistory, null, null);
  }

  @Override
  public boolean hasNextMessage(Map<String, Long> groupHistory,
      Map<String, GroupStatus> groupStatus, List<IMessage> list) {
    IMessage message = _getNextMessage(groupHistory, groupStatus, list);
    if (null == message) {
      return false;
    }
    
    return true;
  }
}
