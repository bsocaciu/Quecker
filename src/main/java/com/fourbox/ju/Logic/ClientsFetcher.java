package com.fourbox.ju.Logic;

import java.util.Random;

import com.fourbox.ju.Model.Client;

/**
 * Used to create random clients
 * 
 * @author SCBbestof
 *
 */
public class ClientsFetcher implements Runnable {

  private QueueManager manager;
  private int maximumClientProcessingTime;
  private int maximumFetcherWaitingTime;
  private Random random = new Random();

  /**
   * If only the manager is specified, use defaults for the other two
   * 
   * @param manager
   */
  public ClientsFetcher(QueueManager manager) {
    this(manager, 5000, 10000);
  }

  /**
   * Initializes the ClientFetcher
   * 
   * @param manager
   * @param maximumClientProcessingTime
   * @param maximumFetcherWaitingTime
   */
  public ClientsFetcher(QueueManager manager, double maximumClientProcessingTime, double maximumFetcherWaitingTime) {
    this.manager = manager;
    this.maximumClientProcessingTime = (int) (maximumClientProcessingTime * 1000);
    this.maximumFetcherWaitingTime = (int) (maximumFetcherWaitingTime * 1000);
  }

  public void run() {
    try {
      while (!Thread.interrupted()) {
        synchronized (manager) {
          waitForRandomTime();
          Client client = createRandomClient();
          manager.addClientToQueue(client);
        }
      }
    }
    catch (InterruptedException ie) {
      ie.printStackTrace();
    }
  }

  /**
   * Makes the instance of the ClientsFetcher to sleep for a random time
   * 
   * @throws InterruptedException
   */
  private void waitForRandomTime() throws InterruptedException {
    Thread.sleep(random.nextInt(maximumFetcherWaitingTime));
  }

  /**
   * @return - new Client with a random processing time
   */
  private Client createRandomClient() {
    Client client = new Client(random.nextInt(maximumClientProcessingTime));

    return client;
  }

  /**
   * 
   * @return - maximumFetcherWaitingTime
   */
  public int getMaximumFetcherWaitingTime() {
    return maximumFetcherWaitingTime;
  }

  /**
   * 
   * @return - maximumClientProcessingTime
   */
  public int getMaximumClientProcessingTime() {
    return maximumClientProcessingTime;
  }

  /**
   * 
   * @param maximumClientProcessingTime
   */
  public void setMaximumClientProcessingTime(int maximumClientProcessingTime) {
    this.maximumClientProcessingTime = maximumClientProcessingTime * 1000;
  }

  /**
   * 
   * @param maximumFetcherWaitingTime
   */
  public void setMaximumFetcherWaitingTime(int maximumFetcherWaitingTime) {
    this.maximumFetcherWaitingTime = maximumFetcherWaitingTime * 1000;
  }

}
