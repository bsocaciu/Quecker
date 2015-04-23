package com.fourbox.ju.Model;

import java.util.concurrent.LinkedBlockingQueue;

import com.fourbox.ju.Logic.QueueManager;

/**
 * Processes tasks given by the Clients
 * 
 * @author SCBbestof
 *
 */
public class Processor implements Runnable {
  private static int counter = 0;
  private final int id = ++counter;

  private LinkedBlockingQueue<Client> clients = new LinkedBlockingQueue<Client>();
  private boolean closingFlag = false;

  /**
   * If there is only one param, initialize the rest with default values
   * 
   * @param manager
   */
  public Processor(QueueManager manager) {
    this(manager, 10, new LinkedBlockingQueue<Client>());
  }

  /**
   * If the list of clients is absent from the params list, then initialize it with an empty list
   * 
   * @param manager
   * @param maximumProcessingQueue
   */
  public Processor(QueueManager manager, int maximumProcessingQueue) {
    this(manager, maximumProcessingQueue, new LinkedBlockingQueue<Client>(maximumProcessingQueue));
  }

  /**
   * Default constructor for the Processor class
   * 
   * @param manager
   * @param maximumProcessingQueue
   * @param clients
   */
  public Processor(QueueManager manager, int maximumProcessingQueue, LinkedBlockingQueue<Client> clients) {
    this.clients = clients;
  }

  public void run() {
    System.out.println(this);
    try {
      while (!Thread.interrupted()) {
        if (closingFlag) {
          if (clients.isEmpty()) {
            System.out.println(this + " return");
            return;
          }
        }
        if (!clients.isEmpty()) {
          System.out.println(this + " " + clients);
          processGivenTask();
        }
      }
    }
    catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  /**
   * 
   * @return - clients queue size
   */
  public int getQueueSize() {
    return clients.size();
  }

  /**
   * Processes the task of the first client in queue
   * 
   * @throws InterruptedException
   */
  private synchronized void processGivenTask() throws InterruptedException {
    Client client = clients.take();
    System.out.println(this + " is processing " + client);
    Thread.sleep(client.getProcessingTime());
  }

  /**
   * Sets the closing flag to true, closing the queue after the last client has been serviced
   */
  public void setClosingFlag() {
    this.closingFlag = true;
  }

  public synchronized void addClientToProcessingQueue(Client client) throws InterruptedException {
    clients.offer(client);
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(id);
    return buffer.toString();
  }
}
