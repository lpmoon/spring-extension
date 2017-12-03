package com.lpmoon.spring.thread;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import sun.jvm.hotspot.runtime.DeadlockDetector;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DeadlockChecker implements ApplicationListener<ContextRefreshedEvent> {
    private String handlerId;
    private DeadlockHandler deadlockHandler;
    private long period;
    private TimeUnit unit;

    private final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
    private final ScheduledExecutorService scheduler =
            Executors.newScheduledThreadPool(1);

    final Runnable deadlockCheck = new Runnable() {
        @Override
        public void run() {
            long[] deadlockedThreadIds = DeadlockChecker.this.mbean.findDeadlockedThreads();

            if (deadlockedThreadIds != null) {
                ThreadInfo[] threadInfos =
                        DeadlockChecker.this.mbean.getThreadInfo(deadlockedThreadIds);

                DeadlockChecker.this.deadlockHandler.handleDeadlock(threadInfos);
            }
        }
    };

    public DeadlockChecker(String handlerId,
                            long period, TimeUnit unit) {
        this.handlerId = handlerId;
        this.period = period;
        this.unit = unit;
    }

    public void start() {
        this.scheduler.scheduleAtFixedRate(
                this.deadlockCheck, this.period, this.period, this.unit);
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if(contextRefreshedEvent.getApplicationContext().getParent() == null) {
            if (this.handlerId == null) {
                this.deadlockHandler = new PrintDeadlockHandler();
            } else {
                this.deadlockHandler = (DeadlockHandler) contextRefreshedEvent.getApplicationContext().getBean(handlerId);
            }

            start();
        }
    }
}
