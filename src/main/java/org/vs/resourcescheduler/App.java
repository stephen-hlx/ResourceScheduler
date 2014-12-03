package org.vs.resourcescheduler;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.gateway.Gateway;
import org.vs.resourcescheduler.gateway.IGateway;
import org.vs.resourcescheduler.messagegenerator.AdvancedRandomMessageGenerator;
import org.vs.resourcescheduler.messagegenerator.RandomMessageGenerator;
import org.vs.resourcescheduler.monitor.DaemonMonitor;
import org.vs.resourcescheduler.scheduler.IResourceScheduler;
import org.vs.resourcescheduler.scheduler.ResourceScheduler;
import org.vs.resourcescheduler.scheduler.exception.TerminatedGroupMessageException;
import org.vs.resourcescheduler.scheduler.strategy.GroupPrioritisedStrategy;

/**
 * Hello world!
 * 
 */
public class App {

  private final static Logger logger = Logger.getLogger(App.class);

  public static void main(String[] args) {
    logger.debug("starting ...");
    //generateXMLMessage();
    f1();
    logger.debug("exiting ...");
  }

  public static void f1() {
    IGateway gateway = Gateway.getInstance();
    gateway.init(6);
    logger.debug(gateway);
    DaemonMonitor dtGateway = new DaemonMonitor(gateway, 1000, true);
    dtGateway.setLogger("GatewayMonitorLogger");
    dtGateway.start();
    
    IResourceScheduler scheduler = new ResourceScheduler(gateway);
    scheduler.setStrategy(new GroupPrioritisedStrategy());

    SimpleThreadFactory threadFactory = new SimpleThreadFactory();
    Thread schedulerThread = threadFactory.newThread(((Runnable) scheduler));
    
    DaemonMonitor dtScheduler = new DaemonMonitor(scheduler, 1000, true);
    dtScheduler.setLogger("SchedulerMonitorLogger");
    dtScheduler.start();
    schedulerThread.start();

    RandomMessageGenerator randMG = new RandomMessageGenerator();
    randMG.setGroupSize(7);
    randMG.setMaxInterval(1000);
    randMG.setSize(500);
    
    try {
//      scheduler.receiveMessage(randMG.nextMessage());
      randMG.run(scheduler);
    } catch (TerminatedGroupMessageException E) {
      logger.fatal("should never reach here!");
    }
  }
  
  public static void generateXMLMessage() {
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
