/**
 * Oracle CopyRight
 */
package org.vs.resourcescheduler.timestampgenerator;

import java.util.Date;

/**
 * @todo TODO
 * @author stephen
 * @version 1.0
 */
public class SimpleTimestampGenerator implements ITimestampGenerator {

  @Override
  public synchronized long getTimestamp() {
    // TODO Auto-generated method stub
    return new Date().getTime();
  }

}
