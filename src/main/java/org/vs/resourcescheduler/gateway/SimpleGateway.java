package org.vs.resourcescheduler.gateway;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.processor.IProcessor;
import org.vs.resourcescheduler.processor.MessageProcessor;
import org.vs.resourcescheduler.scheduler.IResourceScheduler;
import org.vs.resourcescheduler.scheduler.exception.GatewayUnavailableException;
import org.vs.resourcescheduler.timestampgenerator.SimpleTimestampGenerator;

public class SimpleGateway extends AbsGateway {

  private final static Logger logger = Logger.getLogger(SimpleGateway.class);

  // release a source if it is not used within 500 ms after allocation
  private final static long RETENTION_PERIOD = 500;

  private SimpleTimestampGenerator timestampGenerator = new SimpleTimestampGenerator();
  private ExecutorService exec;
  private int size;
  private Set<IProcessor> processorPool;
  private Queue<IProcessor> idleProcessors;

  private List<IProcessor> runningProcessors;

  public SimpleGateway(int resourceCount) {
    size = resourceCount;
    init();
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("Gateway Status:\n");
    sb.append("Gateway is " + (!isShutdown ? "available" : "inavailable") + "\n");
    sb.append("Gateway Resource size: " + size + "\n");
    sb.append("processorPool size: " + processorPool.size() + "\n");
    sb.append("idleProcessors size: " + idleProcessors.size() + "\n");
    return sb.toString();
  }

  private void init() {
    logger.info("initialising Gateway");
    exec = Executors.newCachedThreadPool();
    processorPool = new HashSet<IProcessor>();

    for (int i = 0; i < size; i++) {
      MessageProcessor processor = new MessageProcessor();
      processor.addObserver(this);
      processorPool.add(processor);
    }

    idleProcessors = new LinkedList<IProcessor>(processorPool);
    runningProcessors = new LinkedList<IProcessor>();
  }

  /**
   * Process the message 1. setup the message processor 2. move it from idle to running 3. kick it
   * off
   * 
   * @throws InterruptedException
   */
  @Override
  public synchronized void send(IMessage message) throws GatewayUnavailableException,
      InterruptedException {
    logger.debug("send message \t" + "scheduler " + message.getResourceScheduler().hashCode()
        + "\tmessage " + message);

    while (idleProcessors.size() < 1) {
      wait();
      if (isShutdown) {
        throw new GatewayUnavailableException();
      }
    }

    IProcessor processor = idleProcessors.poll();
    processor.setMessage(message);
    runningProcessors.add(processor);
    exec.execute(processor);
  }

  @Override
  public void setSize(int count) {
    // TODO Auto-generated method stub

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

  @Override
  public synchronized void update(Observable o, Object arg) {
    runningProcessors.remove((IProcessor) o);
    if (((IProcessor) o).isIdle()) {
      idleProcessors.add((IProcessor) o);
    }
    notifyAll();
  }

  @Override
  public boolean testAndAcquire(IResourceScheduler scheduler) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isAvailable() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void acquireResource(IResourceScheduler scheduler)
      throws GatewayUnavailableException, InterruptedException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public void init(int size) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public String getSnapShot() {
    // TODO Auto-generated method stub
    return null;
  }
}
