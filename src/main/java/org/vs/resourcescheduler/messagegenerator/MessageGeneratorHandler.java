package org.vs.resourcescheduler.messagegenerator;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class MessageGeneratorHandler {

  public static void main(String[] args) {
    loadData("/home/stephen/workspace/ResourceScheduler/setup.properties");
    
    // loadData(args[1]);
  }

  public static void loadData(String filename) {
    AdvancedRandomMessageGenerator advMG = new AdvancedRandomMessageGenerator();
    Properties prop = new Properties();
    FileInputStream input = null;

    try {
      input = new FileInputStream(filename);
      prop.load(input);
    } catch (IOException e) {
      System.out.println("I/O Exception");
      return;
    } finally {
      if (null != input) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    
    String strGroupSize = prop.getProperty("groupSize");
    int [] groupSizes = string2Array(strGroupSize);
    int workloadUOM = Integer.parseInt(prop.getProperty("workloadUOM"));
    int maxWorkloadUnit = Integer.parseInt(prop.getProperty("maxWorkloadUnit"));

    System.out.println("length: " + groupSizes.length);
    for (int i : groupSizes) {
      System.out.println(i);
    }

    advMG.setGroupSizes(groupSizes);
    advMG.setWorkloadUOM(workloadUOM);
    advMG.setMaxWorkloadUnit(maxWorkloadUnit);
    
    String outputFilename = prop.getProperty("output");
    advMG.setOutputFilename(outputFilename);
    
    writeTxtFile(outputFilename, advMG.generateMsgXML());
    
    return ;
  }

  public static int[] string2Array(String str) {
    String[] parts = str.split(";");
    int length = parts.length;
    int[] retVal = new int[length];

    for (int i = 0; i < length; i++) {
      retVal[i] = Integer.parseInt(parts[i]);
    }

    return retVal;
  }
  
  private static void writeTxtFile(String filename, String str) {
    try {
      BufferedWriter out = new BufferedWriter(new FileWriter(filename));
      out.write(str);
      out.close();
    } catch (IOException e) {
      System.out.println("I/O Exception");
    }
  }


}
