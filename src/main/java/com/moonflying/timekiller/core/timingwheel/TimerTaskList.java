package com.moonflying.timekiller.core.timingwheel;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TimerTaskList implements Delayed {
    private final AtomicInteger taskCounter;

    public TimerTaskList(AtomicInteger taskCounter) {
        this.taskCounter = taskCounter;
    }



    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }
}
