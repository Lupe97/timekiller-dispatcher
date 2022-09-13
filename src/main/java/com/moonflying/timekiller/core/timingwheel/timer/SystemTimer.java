package com.moonflying.timekiller.core.timingwheel.timer;

import com.moonflying.timekiller.core.timingwheel.task.TimerTask;
import com.moonflying.timekiller.core.timingwheel.task.TimerTaskList;
import com.moonflying.timekiller.core.timingwheel.TimingWheel;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SystemTimer implements Timer {
    private String executorName;

    private long tickMs = 1L;

    private int wheelSize = 20;

    private final long startMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

    private final TimingWheel timingWheel;

    private final AtomicInteger taskCounter = new AtomicInteger(0);

    private final DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();

    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(1, (runnable) -> new Thread(runnable, "executor-" + executorName));

    public SystemTimer(String executorName) {
        this.executorName = executorName;
        this.timingWheel = new TimingWheel(this.tickMs, this.wheelSize, startMs, taskCounter, delayQueue);
    }

    public SystemTimer(String executorName, long tickMs, int wheelSize) {
        this.executorName = executorName;
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;

        timingWheel = new TimingWheel(tickMs, wheelSize, startMs, taskCounter, delayQueue);
    }

    @Override
    public void add(TimerTask timerTask) {

    }

    @Override
    public boolean advanceClock(Long timeoutMs) {
        return false;
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void shutDown() {

    }
}
