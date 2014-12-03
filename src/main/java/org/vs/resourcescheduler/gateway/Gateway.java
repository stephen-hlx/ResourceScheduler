package org.vs.resourcescheduler.gateway;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.SimpleThreadFactory;
import org.vs.resourcescheduler.gateway.exception.InvalidGatewayStateException;
import org.vs.resourcescheduler.gateway.exception.NoResourceAllocatedException;
import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.processor.IProcessor;
import org.vs.resourcescheduler.processor.MessageProcessor;
import org.vs.resourcescheduler.scheduler.IResourceScheduler;
import org.vs.resourcescheduler.scheduler.exception.GatewayUnavailableException;
import org.vs.resourcescheduler.timestampgenerator.SimpleTimestampGenerator;


public class Gateway extends AbsGateway {

  private static class NeglectedResourceRecoverer implements Runnable {
    private static final Logger logger = Logger.getLogger("GatewayNRR");;

    private Gateway gateway;
    private static volatile NeglectedResourceRecoverer instance = null;

    private NeglectedResourceRecoverer(Gateway gateway) {
      logger.debug("NeglectedResourceRecoverer initialising");
      this.gateway = gateway;
    }

    public static void startRecoverer(Gateway gateway) {
      if (null == instance) {
        synchronized (NeglectedResourceRecoverer.class) {
          if (null == instance) {
            instance = new NeglectedResourceRecoverer(gateway);
            Thread t = new Thread(instance);
            t.setDaemon(true);
            t.start();
          }
        }
      }
    }

    private void check() {
      scanAllocatedResources();
    }

    private void scanAllocatedResources() {
      synchronized (gateway) {
        // TODO what if the gateway is re-initialised?

        long timestamp = TS_GENERATOR.getTimestamp();
        Set<IResourceScheduler> setSchedulers = gateway.allocatedProcessors.keySet();
        IProcessor allocatedProcessor = null;
        for (IResourceScheduler scheduler : setSchedulers) {
          allocatedProcessor = gateway.allocatedProcessors.get(scheduler);
          if (null != allocatedProcessor) {
            // for those scheduler having resources allocated
            if (timestamp - gateway.requestHistory.get(scheduler) > RETENTION_PERIOD) {
              // found neglected resource
              logger.debug("found neglected resource "
              + allocatedProcessor.toString()
              + " allocated for scheduler " + scheduler.getId());

              // move the resource to idle pool
              gateway.idleProcessors.add(allocatedProcessor);

              // revoke the sources allocated
              gateway.allocatedProcessors.remove(scheduler);

              logger.debug("neglected resource recovered");
            }
          }
        }
      }
    }

    @Override
    public void run() {
      logger.debug("NeglectedResourceRecoverer started");
      try {
        while (true) {
          check();
          Thread.sleep(RETENTION_PERIOD);
        }
      } catch (InterruptedException E) {
        logger.fatal("DAEMON NeglectedResourceRecoverer is interrupt, this should never happen!");
      }
      logger.debug("NeglectedResourceRecoverer exiting");
    }

  }
  
  private final static Logger logger = Logger.getLogger("GatewayLogger");
  private static Gateway instance = null;

  // release a source if it is not used within 500 ms after allocation
  private final static long RETENTION_PERIOD = 500;

  private final static SimpleTimestampGenerator TS_GENERATOR = new SimpleTimestampGenerator();
  private ExecutorService exec;
  private int size;
  private Set<IProcessor> processorPool;
  private Queue<IProcessor> idleProcessors;

  private Map<IResourceScheduler, IProcessor> allocatedProcessors;

  private Map<IResourceScheduler, Long> requestHistory;

  private List<IProcessor> runningProcessors;

  public static Gateway getInstance() {
    if (null == instance) {
      synchronized (Gateway.class) {
        if (null == instance) {
          instance = new Gateway();
        }
      }
    }

    return instance;
  }

  private Gateway() {}

  public String toString() {
    return "Gateway";
  }

  public void init(int size) {
    logger.info("initialising Gateway");

    if (null != exec) {
      exec.shutdown();
    }
    exec = Executors.newCachedThreadPool(new SimpleThreadFactory());

    this.size = size;
    processorPool = new HashSet<IProcessor>();
    idleProcessors = new LinkedList<IProcessor>();
    allocatedProcessors = new HashMap<IResourceScheduler, IProcessor>();
    runningProcessors = new LinkedList<IProcessor>();
    requestHistory = new HashMap<IResourceScheduler, Long>();

    adjustResourceSize();

    NeglectedResourceRecoverer.startRecoverer(instance);
  }

  private void heartbeat(IResourceScheduler scheduler) {
    logger.debug("heartbeat of scheduler " + scheduler.getId() + " received");
    requestHistory.put(scheduler, TS_GENERATOR.getTimestamp());
  }

  /**
   * if nothing has been allocated for the current requesting scheduler, throws an exception.
   * Otherwise, 1. setup the message processor, 2. move it from allocated to running, 3. kick it off
   */
  @Override
  public synchronized void send(IMessage message) throws NoResourceAllocatedException {
    IResourceScheduler scheduler = message.getResourceScheduler();
    IProcessor processor = allocatedProcessors.remove(scheduler);

    logger.debug("send message \t" + "scheduler " + scheduler.getId() + "\tmessage " + message);

    // keep this scheduler alive
    heartbeat(scheduler);

    if (null == processor) {
      throw new NoResourceAllocatedException();
    }

    logger.debug("assigning " + message + " to " + processor);
    processor.setMessage(message);
    logger.debug("assigned " + message + " to " + processor);
    runningProcessors.add(processor);
    exec.execute(processor);
  }

  @Override
  public void setSize(int size) {
    this.size = size;
    adjustResourceSize();
  }

  @Override
  public boolean isAvailable() {
    return !isShutdown && idleProcessors.size() > 0;
  }

  /**
   * 1. shutdown the executor service 2. notify all processors about this 3. set shutdown flag 4.
   * notify all depending schedulers about this
   */
  @Override
  public synchronized void shutdown() {
    exec.shutdown();
    for (IProcessor processor : processorPool) {
      processor.shutdown();
    }
    isShutdown = true;
    notifyAll();
  }

  /**
   * Acquire a resource if there is any available 1. test if there is any resource available, return
   * false if no resource is available 2. allocate a resource for the scheduler
   * @throws InvalidGatewayStateException 
   */
  @Override
  public synchronized boolean testAndAcquire(IResourceScheduler scheduler) throws InvalidGatewayStateException {
    logger.debug("scheduler " + scheduler.getId() + " is trying to acquire resource");
    if (!isAvailable()) {
      return false;
    }
    
    // if resource is previously allocated
    if (null != allocatedProcessors.get(scheduler)) {
      return true;
    }

    allocateResource(scheduler);

    return true;
  }

  @Override
  public synchronized void acquireResource(IResourceScheduler scheduler)
      throws InterruptedException, GatewayUnavailableException, InvalidGatewayStateException {
    logger.debug("scheduler " + scheduler.getId() + " is trying to acquire resource");

    // return if resource has been previously allocated
    if (null != allocatedProcessors.get(scheduler)) {
      return;
    }

    while (idleProcessors.size() < 1) {
      logger.debug("no resource available, start waiting ...");
      wait();
      if (isShutdown) {
        throw new GatewayUnavailableException();
      }
    }

    logger.debug("there is resource now");

    allocateResource(scheduler);
  }

  private synchronized void allocateResource(IResourceScheduler scheduler) throws InvalidGatewayStateException {
    logger.debug("allocating resource for scheduler " + scheduler.getId() + " idle.size = "
        + idleProcessors.size());

    IProcessor processor = idleProcessors.poll();
    if (null == processor) {
      throw new InvalidGatewayStateException();
    }

    allocatedProcessors.put(scheduler, processor);

    logger.debug(processor + " allocated for " + scheduler.getId() + " idle.size = "
        + idleProcessors.size());
    
    // keep this scheduler alive
    heartbeat(scheduler);
  }

  @Override
  public synchronized void update(Observable o, Object arg) {
    releaseResource((IProcessor) o);
  }

  private synchronized void releaseResource(IProcessor processor) {
    logger.debug("removing " + processor + " from running pool");
    runningProcessors.remove(processor);
    logger.debug("removed " + processor + " from running pool");
    logger.debug("adding " + processor + " to idle pool");
    idleProcessors.add(processor);
    logger.debug("added " + processor + " to idle pool");
    adjustResourceSize();
  }

  private synchronized void adjustResourceSize() {
    while (processorPool.size() < size) {
      MessageProcessor processor = new MessageProcessor();
      processor.addObserver(instance);
      processorPool.add(processor);
      idleProcessors.add(processor);
    }

    while (processorPool.size() > size) {
      if (0 == idleProcessors.size()) {
        break;
      }
      processorPool.remove(idleProcessors.poll());
    }
    
    // there MIGHT have been new resource available, notify all
    notifyAll();
  }

  @Override
  public String getSnapShot() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    sb.append("################\n");
    sb.append("Gateway Status:\n");
    sb.append("Gateway is " + (isAvailable() ? "available" : "inavailable") + "\n");
    sb.append("Gateway Resource size: " + size + "\n");
    sb.append("usage: "
        + (double)(runningProcessors.size()) / processorPool.size() * 100 + "%\n");
    sb.append("processorPool size: " + processorPool.size() + "\n");
    sb.append("idleProcessors size: " + idleProcessors.size() + "\n");
    sb.append("allocatedProcessors size: " + allocatedProcessors.size() + "\n");
    sb.append("runningProcessors size: " + runningProcessors.size() + "\n");
    sb.append("################\n");
    sb.append("\n");
    return sb.toString();
  }
}
