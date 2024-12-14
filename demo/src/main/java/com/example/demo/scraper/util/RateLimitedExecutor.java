package com.example.demo.scraper.util;

import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RateLimitedExecutor extends ThreadPoolExecutor {
  ArrayList<Thread> awaitingThreads = new ArrayList<>();
  int resetPeriod = 1000;
  AtomicBoolean done = new AtomicBoolean(false);
  Thread th;
  int maxExecutions;
  AtomicInteger remainingExecutions;
  public RateLimitedExecutor(int resetPeriod, int maxExecutions, int poolSize) {
    super(poolSize, poolSize, 500, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>() {});
    this.resetPeriod = resetPeriod;
    this.maxExecutions = maxExecutions;
    this.remainingExecutions = new AtomicInteger(maxExecutions);
    Runnable reset = () -> {
      while (!done.get()) {
        remainingExecutions.set(this.maxExecutions);
        awaitingThreads.forEach(Thread::notify);
        try {
          TimeUnit.MILLISECONDS.sleep(this.resetPeriod);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    };
    th = new Thread(reset);
    th.start();
  }

  @Override
  protected void beforeExecute(Thread t, Runnable r) {
    try {
      awaitingThreads.add(t);
      t.wait();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
