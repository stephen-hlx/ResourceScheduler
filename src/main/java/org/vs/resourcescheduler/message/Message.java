/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.message;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public class Message extends AbsMessage {

  private String groupID;
  
  private boolean isTermination = false;
  
  public Message(String message, String groupID) {
    this(message, groupID, false);
  }
  
  public Message(String message, String groupID, boolean isTermination) {
    super();
    this.message = message;
    this.groupID = groupID;
    this.isTermination = isTermination;
    setWorkLoad(1);
  }
  
  @Override
  public boolean isTermination() {
    return isTermination;
  }


  @Override
  public String getGroupID() {
    // TODO Auto-generated method stub
    return groupID;
  }


}
