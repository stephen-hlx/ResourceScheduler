package org.vs.resourcescheduler.executor;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.vs.resourcescheduler.message.IMessage;
import org.vs.resourcescheduler.message.Message;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class InstructionGenerator extends DefaultHandler {
  private final static Logger logger = Logger.getLogger(InstructionGenerator.class);
  
  private String filename;
  private InstructionExecutor exec;
  
  public InstructionGenerator(String filename) {
    this.filename = filename;
    this.exec = new InstructionExecutor();
  }
  
  public void go() {
    parseDocument();
  }
  
  private void parseDocument() {
    SAXParserFactory factory = SAXParserFactory.newInstance();
    
    try {
      SAXParser parser = factory.newSAXParser();
      parser.parse(filename, this);
    } catch (ParserConfigurationException e) {
      logger.debug("ParserConfig Error");
    } catch (SAXException e) {
      logger.debug("SAXException: XML not well formed");
    } catch (IOException e) {
      logger.debug("IO error");
    }
  }
  
  @Override
  public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    try {
      if (qName.equals("gateway")) {
        exec.gatewayInit(Integer.parseInt(attributes.getValue("size")));
      }

      if (qName.equals("strategy")) {
        exec.createStrategy(attributes.getValue("id"), attributes.getValue("type"));
      }

      if (qName.equals("scheduler")) {
        exec.schedulerInit(attributes.getValue("id"), attributes.getValue("type"), attributes.getValue("strategyId"));
      }

      if (qName.equals("instruction")) {
        processInstruction(uri, localName, qName, attributes);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new SAXException(e);
    }
  }
  
  private void processInstruction(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    String subject = attributes.getValue("subject");
    switch (subject) {
      case "main":
        processMainInstruction(uri, localName, qName, attributes);
        break;
      case "gateway":
        processGatewayInstruction(uri, localName, qName, attributes);
        break;
      case "scheduler":
        processSchedulerInstruction(uri, localName, qName, attributes);
        break;
      default:
        logger.fatal("unknow subject: " + subject);
        throw new SAXException("unknow subject: " + subject);
    }
  }
  
  private void processMainInstruction(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    String action = attributes.getValue("action");
    switch (action) {
      case "wait":
        exec.mainWait(Long.parseLong(attributes.getValue("length")));
        break;
      case "sendMessage":
        String schedulerId = attributes.getValue("schedulerId");
        String content = attributes.getValue("content");
        String groupId = attributes.getValue("groupId");
        String workload = attributes.getValue("workload");
        boolean isTermination = new Boolean(attributes.getValue("isTermination")).booleanValue();
        IMessage message = new Message(content, groupId, isTermination);
        message.setWorkLoad(Long.parseLong(workload));
        exec.mainSendMessage(schedulerId, message);
        break;
      default:
        logger.fatal("unknow action: " + action);
        throw new SAXException("unknow action: " + action);
    }
  }
  
  private void processGatewayInstruction(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    String action = attributes.getValue("action");
    switch (action) {
      case "setSize":
        exec.gatewaySetSize(Integer.parseInt(attributes.getValue("size")));
        break;
      case "shutdown":
        exec.gatewayShutdown();
        break;
      default:
        logger.fatal("unknow action: " + action);
        throw new SAXException("unknow action: " + action);
    }
  }
  
  private void processSchedulerInstruction(String uri, String localName, String qName, Attributes attributes) throws SAXException {
    String action = attributes.getValue("action");
    try {
      switch (action) {
        case "setStrategy":
          exec.schedulerSetStrategy(attributes.getValue("id"),
              attributes.getValue("strategyId"));
          break;
        case "cancelGroup":
          exec.schedulerCancelGroup(attributes.getValue("id"),
              attributes.getValue("groupId"));
          break;
        case "shutdown":
          exec.schedulerShutdown(attributes.getValue("id"));
          break;
        default:
          logger.fatal("unknow action: " + action);
          throw new SAXException("unknow action: " + action);
      }
    } catch (InvalidInstructionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    InstructionGenerator generator = new InstructionGenerator("/home/stephen/workspace/ResourceScheduler/instructions_2schedulers.xml");
    generator.go();
  }

}
