package org.vs.resourcescheduler;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.vs.resourcescheduler.messagegenerator.RandomMessageGenerator;
import org.vs.resourcescheduler.processor.IProcessor;
import org.vs.resourcescheduler.processor.MessageProcessor;

public class AppT {

  public static void main(String[] args) {
    RandomMessageGenerator randMG = new RandomMessageGenerator();
    randMG.setGroupSize(5);
    randMG.setMaxInterval(1000);
    randMG.setSize(10);
    f1();

//    try {
//      randMG.run(null);
//    } catch (Exception E) {
//      System.out.println("should never reach here!");
//    }
  }
  
  public static void f1() {
    Set<IProcessor> set = new HashSet<IProcessor>(Arrays.asList(new MessageProcessor[5]));
    IProcessor processor = set.iterator().next();
    if (null == processor) {
      System.out.println("null");
    }
    
    MessageProcessor [] array = new MessageProcessor[5];
    
  }

}
