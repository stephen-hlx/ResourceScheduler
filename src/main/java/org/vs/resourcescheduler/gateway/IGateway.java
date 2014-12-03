/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.gateway;

import org.vs.resourcescheduler.gateway.exception.InvalidGatewayStateException;
import org.vs.resourcescheduler.gateway.exception.NoResourceAllocatedException;
import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.monitor.IMonitorable;
import org.vs.resourcescheduler.scheduler.IResourceScheduler;
import org.vs.resourcescheduler.scheduler.exception.GatewayUnavailableException;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public interface IGateway extends IMonitorable {

  /**
   * initialise the gateway with a specific size of resource
   * @param size
   */
  void init(int size);
  
  /**
   * This is to have a resource allocated for a scheduler, if there is any.
   * 
   * @param scheduler
   * @return true if resource is allocated, false otherwise
   */
  boolean testAndAcquire(IResourceScheduler scheduler)
      throws InvalidGatewayStateException;
  
  /**
   * This is to request a resource.
   * The method will not return until resource is allocated
   * @param scheduler
   */
  void acquireResource(IResourceScheduler scheduler)
      throws GatewayUnavailableException, InterruptedException, InvalidGatewayStateException;

  /**
   * This is the method to digest / process a message
   * 
   * @param message
   */
  void send(IMessage message)
      throws NoResourceAllocatedException, GatewayUnavailableException, InterruptedException;

  /**
   * If the gateway has any idle resource
   * 
   * @return
   */
  boolean isAvailable();

  /**
   * Set the available resource of the gateway
   * 
   * @param count - Number must be greater or equal to zero
   */
  void setSize(int size);

  /**
   * Shutdown Shutdown the gateway and the resource in it.
   */
  void shutdown();

  boolean isShutdown();
}
