package com.lpmoon.spring.thread;

import java.lang.management.ThreadInfo;

public interface DeadlockHandler {
    void handleDeadlock(final ThreadInfo[] deadlockedThreads);
}
