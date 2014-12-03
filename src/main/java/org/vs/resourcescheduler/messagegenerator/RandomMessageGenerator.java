package org.vs.resourcescheduler.messagegenerator;

import java.util.Random;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.message.Message;
import org.vs.resourcescheduler.scheduler.IResourceScheduler;
import org.vs.resourcescheduler.scheduler.exception.TerminatedGroupMessageException;

public class RandomMessageGenerator implements IMessageGenerator {

  private final static Logger logger = Logger.getLogger(RandomMessageGenerator.class);
  private final static String MESSAGE_PREFIX = "MESSAGE";
  private final static String GROUP_ID_PREFIX = "Group_";
  
  private int size;
  private int maxInterval;
  private int groupSize;
  private int workloadUnit;
  private int maxFold;
  private Random random;
  
  private long messageId;
  
  private synchronized long getNextMessageId() {
    return messageId ++;
  }
  
  public void setSize(int size) {
    this.size = size;
  }

  public void setMaxInterval(int maxInterval) {
    this.maxInterval = maxInterval;
  }

  public void setGroupSize(int groupSize) {
    this.groupSize = groupSize;
  }

  public RandomMessageGenerator() {
    random = new Random();
    messageId = 0;
  }


  @Override
  public void run(IResourceScheduler scheduler) throws TerminatedGroupMessageException {

      for (int i = 0; i < size; i++) {
        scheduler.receiveMessage(nextMessage());
//        logger.debug(nextMessage());
//        Thread.sleep(random.nextInt(maxInterval));
      }
//    catch (TerminatedGroupMessageException E) {
//      throw E;
//    }
  }


  @Override
  public IMessage nextMessage() {
    // TODO Auto-generated method stub
    IMessage message = new Message("" + getNextMessageId(),
        "" + random.nextInt(groupSize));
    message.setWorkLoad((random.nextInt(5) + 1) * 200);

    return message;
  }

  public void generateMsgXML(String msg) {
    workloadUnit = 500;
    maxFold = 4;
    int workload;
    for (int i = 0; i < size; i++) {
      workload = workloadUnit * (random.nextInt(maxFold) + 1);
      System.out.println("<instruction subject=\"main\" action=\"sendMessage\">");
      System.out.println("<message content=\"msg\" groupId=\"" + random.nextInt(groupSize)
          + "\" workLoad=\"" + workload + "\" isTermination=\"false\"/>");
      System.out.println("</instruction>");

    }
  }

}
