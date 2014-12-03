package org.vs.resourcescheduler;

import org.vs.resourcescheduler.executor.InstructionGenerator;
import org.vs.resourcescheduler.messagegenerator.MessageGeneratorHandler;

public class Main {

  public static void main(String[] args) {
    if (!checkArgs(args)) {
      printHelp();
      return;
    }

    String filename = args[1];
    switch(args[0]) {
      case "prepare":
        MessageGeneratorHandler.loadData(filename);
        System.out.println("Please merge the generated xml file with the template XML provided.");
        break;
      case "run":
        InstructionGenerator generator = new InstructionGenerator(filename);
        System.out.println("program will start in 5 seconds");
        try {
          Thread.sleep(5000);
        } catch (InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        generator.go();
        break;
      default:
        printHelp();
        break;
    }
  }
  
  private static boolean checkArgs(String[] args) {
    if (null == args) {
      return false;
    }

    if (2 > args.length) {
      return false;
    }

    return true;
  }

  private static void printHelp() {
    System.out.println("usage: java -cp $CLASSPATH ResourceScheduler.jar <command> [<args>]");
    System.out.println("commands:");
    System.out.println("\t" + "prepare"
        + "\tgenerate the xml file with instructions in it using the properties file specified.");
    System.out.println("\t" + "run" + "\tstart the execution");
  }

}
