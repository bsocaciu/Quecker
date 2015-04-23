package com.fourbox.ju.Model;

/**
 * Represents the Client sending tasks to the Processor/Server
 * 
 * @author SCBbestof
 *
 */
public class Client {
  private static int counter = 0;
  private final int id = ++counter;
  private int processingTime;

  public Client(int processingTime) {
    this.processingTime = processingTime;
  }

  /**
   * 
   * @return - the ID of this client
   */
  public int getId() {
    return id;
  }

  /**
   * 
   * @return - the time it takes for the processor to serve this client
   */
  public int getProcessingTime() {
    return processingTime;
  }

  @Override
  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append(id);
    buffer.append(" ");
    buffer.append(processingTime);
    buffer.append("ms");
    return buffer.toString();
    //return super.toString();
  }
}
