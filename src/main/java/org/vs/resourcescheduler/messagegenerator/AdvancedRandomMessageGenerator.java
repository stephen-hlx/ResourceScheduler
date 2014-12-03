package org.vs.resourcescheduler.messagegenerator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.scheduler.IResourceScheduler;
import org.vs.resourcescheduler.scheduler.exception.TerminatedGroupMessageException;

public class AdvancedRandomMessageGenerator implements IMessageGenerator {

  private Random random;
  private int[] groupSizes;

  // workload unit of measure (e.g. 100 ms)
  private int workloadUOM;
  private int maxWorkloadUnit;
  
  private String outputFilename;

  public void setOutputFilename(String outputFilename) {
    this.outputFilename = outputFilename;
  }

  @Override
  public void run(IResourceScheduler scheduler) throws TerminatedGroupMessageException {
    // TODO Auto-generated method stub

  }

  public AdvancedRandomMessageGenerator() {
    random = new Random();
  }

  @Override
  public IMessage nextMessage() {
    // TODO Auto-generated method stub
    return null;
  }

  public String generateMsgXML() {
    int workload;
    int totalMessageCount = 0;
    StringBuilder xmlStr = new StringBuilder();

    for (int i = 0; i < groupSizes.length; i++) {
      totalMessageCount += groupSizes[i];
    }

    for (int i = 0; i < totalMessageCount; i++) {
      int groupId = random.nextInt(groupSizes.length);
      if (0 == groupSizes[groupId]) {
        // messages for this group have been all generated
        i--;
        continue;
      }
      groupSizes[groupId]--;
      workload = workloadUOM * (random.nextInt(maxWorkloadUnit) + 1);

      StringBuilder sb = new StringBuilder();
      sb.append("<instruction subject=\"main\" schedulerId=\"0\" action=\"sendMessage\" ");
      sb.append("content=\"msg\" groupId=\"" + groupId + "\" ");
      sb.append("workload=\"" + workload + "\" isTermination=\"false\"/>");
      System.out.println(sb.toString());
      xmlStr.append(sb.toString() + "\n");

    }
    return xmlStr.toString();
  }

  public void setGroupSizes(int[] groupSizes) {
    this.groupSizes = groupSizes;
  }

  public void setWorkloadUOM(int workloadUOM) {
    this.workloadUOM = workloadUOM;
  }

  public void setMaxWorkloadUnit(int maxWorkloadUnit) {
    this.maxWorkloadUnit = maxWorkloadUnit;
  }

  public static void main(String[] args) {
    AdvancedRandomMessageGenerator advMG = new AdvancedRandomMessageGenerator();
    int[] groupSizes = new int[5];
    groupSizes[0] = 10;
    groupSizes[1] = 20;
    groupSizes[2] = 30;
    groupSizes[3] = 40;
    groupSizes[4] = 50;
    advMG.setGroupSizes(groupSizes);
    advMG.setWorkloadUOM(200);
    advMG.setMaxWorkloadUnit(5);

    advMG.generateMsgXML();
  }
}
