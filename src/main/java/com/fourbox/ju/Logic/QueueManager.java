package com.fourbox.ju.Logic;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import com.fourbox.ju.Model.Client;
import com.fourbox.ju.Model.Processor;

public class QueueManager implements Runnable {

  private LinkedBlockingQueue<Client> clients = new LinkedBlockingQueue<Client>();
  private ArrayList<Processor> processors = new ArrayList<Processor>();
  private ExecutorService executor = Executors.newCachedThreadPool();
  private int maximumProcessorQueue;
  private final int minimumProcessorQueue = 0;
  private long lastCreationTime;
  private int maximumNumberOfQueues;

  /**
   * If no parameters are specified, use the defaults
   */
  public QueueManager() {
    this(10, 10);
  }

  /**
   * If the maximum number of queues is not specified, then use the default : 10
   * 
   * @param maximumProcessorQueue
   */
  public QueueManager(int maximumProcessorQueue) {
    this(maximumProcessorQueue, 10);
  }

  /**
   * Default constructor for the QueueManager
   * 
   * @param maximumProcessorQueue
   * @param maximumNumberOfQueues
   */
  public QueueManager(int maximumProcessorQueue, int maximumNumberOfQueues) {
    this.maximumProcessorQueue = maximumProcessorQueue;
    this.maximumNumberOfQueues = maximumNumberOfQueues;
  }

  public void run() {
    try {
      while (!Thread.interrupted()) {
        if (processors.size() < maximumNumberOfQueues && isNewProcessorNeeded()) {
          Processor processor = new Processor(this, maximumProcessorQueue);
          processors.add(processor);
          executor.execute(processor);
          System.out.println("created : " + processor);
        }
        else {
          assignClient();
        }
        if (isProcessorOveruse()) {
          try {
            Processor processor = getLeastUsedProcessor();
            System.out.println("overuse : " + processor);
            processor.setClosingFlag();
            processors.remove(processor);
          }
          catch (NullPointerException npe) {
            npe.printStackTrace();
          }
        }
      }
    }
    catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  /**
   * Assigns the first client from the queue to the least used processor
   * 
   * @throws InterruptedException
   */
  private void assignClient() throws InterruptedException {
    Processor processor = getLeastUsedProcessor();
    processor.addClientToProcessingQueue(clients.take());
  }

  /**
   * Checks if there is a need for a new Processor
   * 
   * @return - true if it a need for a new Processor, false otherwise
   */
  private boolean isNewProcessorNeeded() {
    if (processors.isEmpty()) {
      return true;
    }
    if (System.currentTimeMillis() - lastCreationTime <= 2000) { // if there was a processor created soon
      return false;
    }
    for (Processor processor : processors) {
      if (processor.getQueueSize() >= maximumProcessorQueue) {
        lastCreationTime = System.currentTimeMillis();
        return true;
      }
    }
    return false;
  }

  /**
   * 
   * @return - true if it there are too much processors running, false otherwise
   */
  private boolean isProcessorOveruse() {
    if (System.currentTimeMillis() - lastCreationTime <= 2000) { // if there was a processor created soon
      return false;
    }
    if (processors.size() <= 1) {
      return false;
    }
    for (Processor processor : processors) {
      if (processor.getQueueSize() <= minimumProcessorQueue) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the least used processor
   * 
   * @return
   */
  private Processor getLeastUsedProcessor() {
    Processor tmpProcessor = null;
    int tmpQueueSize = maximumProcessorQueue + 1;
    for (Processor processor : processors) {
      if (processor.getQueueSize() < tmpQueueSize) {
        tmpProcessor = processor;
        tmpQueueSize = tmpProcessor.getQueueSize();
      }
    }
    return tmpProcessor;
  }

  /**
   * Adds the given client to the queue. The queue of clients from this class is used to queue up the clients waiting
   * for assignment
   * 
   * @param client
   * @throws InterruptedException
   */
  public void addClientToQueue(Client client) throws InterruptedException {
    if (!clients.contains(client)) {
      clients.put(client);
    }
  }

  /**
   * @return - list of clients
   */
  public LinkedBlockingQueue<Client> getClients() {
    return clients;
  }

  /**
   * Returns the maximum number of queues/processors
   * 
   * @return
   */
  public int getMaximumNumberOfQueues() {
    return maximumNumberOfQueues;
  }

  /**
   * Returns the maximum queue size per processor
   * 
   * @return
   */
  public int getMaximumProcessorQueue() {
    return maximumProcessorQueue;
  }

  /**
   * Sets the maximum number of queues/processors
   * 
   * @param maximumNumberOfQueues
   */
  public void setMaximumNumberOfQueues(int maximumNumberOfQueues) {
    this.maximumNumberOfQueues = maximumNumberOfQueues;
  }

  /**
   * Sets the maximum queue size per processor
   * 
   * @param maximumProcessorQueue
   */
  public void setMaximumProcessorQueue(int maximumProcessorQueue) {
    this.maximumProcessorQueue = maximumProcessorQueue;
  }

  public ArrayList<Processor> getProcessors() {
    return processors;
  }
}
