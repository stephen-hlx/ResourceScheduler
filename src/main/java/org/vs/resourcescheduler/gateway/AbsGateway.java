package org.vs.resourcescheduler.gateway;

import java.util.Observer;


public abstract class AbsGateway implements IGateway, Observer {

  protected boolean isShutdown = false;
  
  @Override
  public boolean isShutdown() {
    // TODO Auto-generated method stub
    return isShutdown;
  }

}
