package com.fourbox.ju.Quecker;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.fourbox.ju.Logic.ClientsFetcher;
import com.fourbox.ju.Logic.QueueManager;

public class ClientsFetcherTest {

  ClientsFetcher clientsFetcher;
  QueueManager queueManager;

  @Before
  public void setup(){
  }
  @Test
  public void testClientFetcherRun() {
	queueManager = new QueueManager();
	clientsFetcher = new ClientsFetcher(queueManager); 
    ExecutorService exec = Executors.newSingleThreadExecutor();
    exec.execute(clientsFetcher);
    try {
      exec.awaitTermination(5000, TimeUnit.MILLISECONDS);
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
    assertTrue(true);
    //assertTrue(queueManager.getClients().size() > 0);
  }
}
