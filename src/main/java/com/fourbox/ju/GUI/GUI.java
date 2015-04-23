package com.fourbox.ju.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SpringLayout;
import javax.swing.Timer;

import com.fourbox.ju.Logic.ClientsFetcher;
import com.fourbox.ju.Logic.QueueManager;
import com.fourbox.ju.Model.Processor;
import com.fourbox.ju.Utility.SpringUtilities;

/**
 * Creates and formats the graphical user interface of this application
 * 
 * @author SCBbestof
 *
 */
public class GUI extends JFrame implements Runnable {

  private static final long serialVersionUID = 4063550242586268123L;

  private JButton buttonRun = new JButton("Initialize"); // change to "Adapt" after the first click
  private JButton buttonStop = new JButton("Stop");

  private SpinnerModel spinnerModelMaxQueues = new SpinnerNumberModel(10, 1, 100, 1);
  private SpinnerModel spinnerModelMaxProcessorQueues = new SpinnerNumberModel(10, 1, 100, 1);
  private SpinnerModel spinnerModelMaxProcessingTime = new SpinnerNumberModel(1, 0.5, 100, 0.1);
  private SpinnerModel spinnerModelWaitingTime = new SpinnerNumberModel(1, 0.5, 100, 0.1);
  // All measured in seconds!!!
  private JSpinner spinnerMaximumNumberOfQueues = new JSpinner(spinnerModelMaxQueues);
  private JSpinner spinnerMaximumProcessorQueue = new JSpinner(spinnerModelMaxProcessorQueues);
  private JSpinner spinnerMaximumClientProcessingTime = new JSpinner(spinnerModelMaxProcessingTime);
  private JSpinner spinnerMaximumFetcherWaitingTime = new JSpinner(spinnerModelWaitingTime);

  private JLabel labelNoteForUsage = new JLabel("NOTE: Times are given in seconds!");
  private JLabel labelMaximumNumberOfQueues = new JLabel("Maximum number of queues: ");
  private JLabel labelMaximumProcessorQueue = new JLabel("Maximum processor queue size: ");
  private JLabel labelMaximumClientProcessingTime = new JLabel("Maximum client processing time: ");
  private JLabel labelMaximumFetcherWaitingTime = new JLabel("Maximum generating interval: ");

  private Timer timer;
  private JLabel labelTimer = new JLabel("00000000");
  private int count = 0;
  private int timerDelay = 1000; // miliseconds
  // progress bars
  private List<JProgressBar> progressBars = new ArrayList<JProgressBar>();

  // variables
  private QueueManager queueManager;
  private ClientsFetcher clientsFetcher;
  private ExecutorService executor = Executors.newCachedThreadPool();
  private static boolean running = false;
  private GUI gui;
  private JFrame resultFrame;
  private JPanel panel;
  private JPanel resultPanel;
  private int peakhour = 0;
  private JLabel labelPeakHour = new JLabel("0");

  public GUI(String TITLE) {
    super(TITLE);
    SpringLayout layout = new SpringLayout();
    panel = new JPanel(layout);
    initGUI();
    gui = this;
  }

  private void initGUI() {
    labelMaximumClientProcessingTime.setLabelFor(spinnerMaximumClientProcessingTime);
    labelMaximumFetcherWaitingTime.setLabelFor(spinnerMaximumFetcherWaitingTime);
    labelMaximumNumberOfQueues.setLabelFor(spinnerMaximumNumberOfQueues);
    labelMaximumProcessorQueue.setLabelFor(spinnerMaximumProcessorQueue);

    setActionListeners();

    // Add UI elements
    panel.add(labelNoteForUsage);
    panel.add(labelTimer);

    panel.add(labelMaximumClientProcessingTime);
    panel.add(spinnerMaximumClientProcessingTime);
    panel.add(labelMaximumNumberOfQueues);
    panel.add(spinnerMaximumNumberOfQueues);
    panel.add(labelMaximumProcessorQueue);
    panel.add(spinnerMaximumProcessorQueue);
    panel.add(labelMaximumFetcherWaitingTime);
    panel.add(spinnerMaximumFetcherWaitingTime);

    panel.add(buttonRun);
    panel.add(buttonStop);

    // Format the JFrame
    SpringUtilities.makeCompactGrid(panel, 6, 2, 10, 10, 10, 20);
    this.add(panel);
    this.pack();
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setVisible(true);
  }

  /**
   * Sets the action listeners from the buttons
   */
  private void setActionListeners() {
    buttonRun.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (!isRunning()) {
          System.out.println("----------------------------------");
          queueManager = new QueueManager((Integer) spinnerMaximumProcessorQueue.getValue(),
              (Integer) spinnerMaximumNumberOfQueues.getValue());
          clientsFetcher = new ClientsFetcher(queueManager, (Double) spinnerMaximumClientProcessingTime.getValue(),
              (Double) spinnerMaximumFetcherWaitingTime.getValue());
          executor.execute(clientsFetcher);
          executor.execute(queueManager);

          createResultPanel();
          // buttonRun.setText("Adapt");
          synchronized (gui) {
            System.out.println("GUInotify");
            gui.notify();
          }

          startTimer();
          running = true;
        }
        synchronized (gui) {
          System.out.println("GUInotify");
          gui.notify();
        }
        /*
         * bar.setBounds(200,130, 100, 10); gui.add(bar); gui.revalidate(); gui.repaint();
         */
      }
    });

    buttonStop.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        executor.shutdownNow();
        running = false;
        stopTimer();
        // TODO:
        // executor.awaitTermination(, unit);
      }
    });

  }

  public void run() {
    try {
      while (!Thread.interrupted()) {
        synchronized (this) {
          if (!isRunning()) {
            System.out.println("GUIwait");
            this.wait();
          }
          if (!sameMaxNumberOfQueues(queueManager)) {
            queueManager.setMaximumNumberOfQueues((Integer) spinnerMaximumNumberOfQueues.getValue());
          }
          if (!sameMaxProcessingQueues(queueManager)) {
            queueManager.setMaximumProcessorQueue((Integer) spinnerMaximumProcessorQueue.getValue());
          }
          if (!sameMaximumFetcherWaitingTime(clientsFetcher)) {
            clientsFetcher.setMaximumFetcherWaitingTime(((Double) spinnerMaximumFetcherWaitingTime.getValue()).intValue());
          }
          if (!sameMaximumClientProcessingTime(clientsFetcher)) {
            clientsFetcher.setMaximumClientProcessingTime(((Double) spinnerMaximumClientProcessingTime.getValue()).intValue());
          }
          updateProgressBars();
          updatePeakTime();
        }
      }
    }
    catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  /**
   * Creates the result panel consisting of progressbars
   */
  private void createResultPanel() {
    resultFrame = new JFrame();
    resultPanel = new JPanel(new SpringLayout());

    int maximumNumberOfQueues = (Integer) spinnerMaximumNumberOfQueues.getValue();
    createProgressBars(maximumNumberOfQueues);
    for (JProgressBar bar : progressBars) {
      resultPanel.add(bar);
    }
    JLabel labelTimerDescription = new JLabel("Time elapsed : ");
    JLabel labelPeakHourDescription = new JLabel("Peak hour: ");
    resultPanel.add(labelTimerDescription);
    resultPanel.add(labelTimer);
    resultPanel.add(labelPeakHourDescription);
    resultPanel.add(labelPeakHour);
    SpringUtilities.makeGrid(resultPanel, 2 + maximumNumberOfQueues / 2, 2, 10, 10, 20, 20);
    resultFrame.add(resultPanel);
    resultFrame.pack();
    resultFrame.setSize(1100, 300);
    resultFrame.setTitle("Simulation");
    resultFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    resultFrame.setVisible(true);
  }

  /**
   * Creates the progressbars used in the simulation
   * 
   * @param numberOfProgressBars
   */
  private void createProgressBars(int numberOfProgressBars) {
    for (int i = 0; i < numberOfProgressBars; ++i) {
      JProgressBar bar = new JProgressBar();
      bar.setMaximum(100);
      bar.setMinimum(0);
      bar.setSize(100, 20);
      progressBars.add(bar);
    }
  }

  /**
   * Updates the peak time
   */
  private void updatePeakTime() {
    int tmpMaxQueue = 0;
    for (Processor processor : queueManager.getProcessors()) {
      tmpMaxQueue += processor.getQueueSize();
    }
    tmpMaxQueue += queueManager.getClients().size();
    if (tmpMaxQueue > peakhour) {
      peakhour = tmpMaxQueue;
      labelPeakHour.setText(labelTimer.getText());
    }
  }

  /**
   * Updates the progress bars
   */
  private void updateProgressBars() {
    List<Processor> processors = queueManager.getProcessors();
    for (int i = 0; i < processors.size(); i++) {
      if (processors.get(i).getQueueSize() > 0) {
    	  if(progressBars.get(i) == null){
    		  return;
    	  }
        progressBars.get(i).setValue(
            processors.get(i).getQueueSize() * (100 / (Integer) spinnerMaximumProcessorQueue.getValue()));
      }
      else {
        progressBars.get(i).setValue(0);
      }
    }
  }

  /**
   * Starts the timer
   */
  private void startTimer() {
    count = 0;
    timer = new Timer(timerDelay, new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        count++;
        labelTimer.setText(String.format("%08d", count));
      }
    });
    timer.start();
  }

  /**
   * Stops the timer
   */
  private void stopTimer() {
    if (timer == null) {
      return;
    }
    timer.stop();
  }

  /**
   * Checks if the MaximumProcessingQueues from the GUI is the same as the one currently used in the Queue Manager
   * 
   * @return
   */
  private boolean sameMaxProcessingQueues(QueueManager queueManager) {
    if (queueManager.getMaximumProcessorQueue() == (Integer) spinnerMaximumProcessorQueue.getValue()) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the MaximumNumberOfQueues from the GUI is the same as the one currently used in the Queue Manager
   * 
   * @return
   */
  private boolean sameMaxNumberOfQueues(QueueManager queueManager) {
    if (queueManager.getMaximumNumberOfQueues() == (Integer) spinnerMaximumNumberOfQueues.getValue()) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the MaximumFetcherWaitingTime from the GUI is the same as the one currently used in the ClientsFetcher
   * 
   * @param clientsFetcher
   * @return
   */
  private boolean sameMaximumFetcherWaitingTime(ClientsFetcher clientsFetcher) {
    if (clientsFetcher.getMaximumFetcherWaitingTime() == ((Double) spinnerMaximumFetcherWaitingTime.getValue()).intValue()) {
      return true;
    }
    return false;
  }

  /**
   * Checks if the MaximumClientProcessingTime from the GUI is the same as the one currently used in the ClientsFetcher
   * 
   * @param clientsFetcher
   * @return
   */
  private boolean sameMaximumClientProcessingTime(ClientsFetcher clientsFetcher) {
    if (clientsFetcher.getMaximumClientProcessingTime() == ((Double) spinnerMaximumClientProcessingTime.getValue()).intValue()) {
      return true;
    }
    return false;
  }

  public boolean isRunning() {
    return running;
  }
}
