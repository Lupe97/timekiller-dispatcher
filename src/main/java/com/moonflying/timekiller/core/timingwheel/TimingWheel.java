package com.moonflying.timekiller.core.timingwheel;

import com.moonflying.timekiller.core.timingwheel.task.TimerTaskList;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TimingWheel {
    private long tickMs;

    private int wheelSize;

    private long interval;

    private long startMs;

    private long currentTime;

    private AtomicInteger taskCounter;

    private DelayQueue<TimerTaskList> queue;

    private TimerTaskList[] buckets;

    private volatile TimingWheel overflowWheel;

    public TimingWheel(long tickMs, int wheelSize, long startMs, AtomicInteger taskCounter, DelayQueue<TimerTaskList> queue) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.startMs = startMs;
        this.currentTime = startMs - (startMs % tickMs);
        this.taskCounter = taskCounter;
        this.queue = queue;

        this.buckets = new TimerTaskList[wheelSize];
        for (TimerTaskList timerTaskList : buckets) {
            timerTaskList = new TimerTaskList(taskCounter);
        }
    }
}
