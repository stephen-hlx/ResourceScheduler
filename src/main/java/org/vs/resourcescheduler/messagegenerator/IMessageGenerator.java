package org.vs.resourcescheduler.messagegenerator;

import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.scheduler.IResourceScheduler;
import org.vs.resourcescheduler.scheduler.exception.TerminatedGroupMessageException;

public interface IMessageGenerator {
  void run(IResourceScheduler scheduler) throws TerminatedGroupMessageException;
  IMessage nextMessage();
}
