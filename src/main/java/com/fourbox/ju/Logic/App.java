package com.fourbox.ju.Logic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fourbox.ju.GUI.GUI;

public class App {
  private static final String TITLE = "Quecker";
  private static final ExecutorService executor = Executors.newCachedThreadPool();

  /**
   * Used to launch the application
   * @param args
   */
  public static void main(String[] args) {
    GUI gui = new GUI(TITLE);
    executor.execute(gui);
  }
}
