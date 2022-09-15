package com.moonflying.timekiller.core.timingwheel;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SystemTimer implements Timer {
    private String executorName;

    private long tickMs = 1L;

    private int wheelSize = 20;

    private final long startMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

    private final ExecutorService taskExecutor = Executors.newFixedThreadPool(1, (runnable) -> new Thread(runnable, "executor-" + executorName));

    private final DelayQueue<TimerTaskList> delayQueue = new DelayQueue<>();

    private final AtomicInteger taskCounter = new AtomicInteger(0);

    private final TimingWheel timingWheel;

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

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writeLock = readWriteLock.writeLock();

    @Override
    public void add(TimerTask timerTask) {
        readLock.lock();
        try {
            addTimerTaskEntry(new TimerTaskEntry(timerTask, timerTask.delayMs + TimingWheelUtils.getHiresClockMs()));
        } finally {
            readLock.unlock();
        }
    }

    private void addTimerTaskEntry(TimerTaskEntry timerTaskEntry) {
        if (!timingWheel.add(timerTaskEntry)) {
            if (!timerTaskEntry.cancelled()) {
                taskExecutor.submit(timerTaskEntry.timerTask);
            }
        }
    }

    @Override
    public boolean advanceClock(Long timeoutMs) {
        try {
            TimerTaskList bucket = delayQueue.poll(timeoutMs, TimeUnit.MILLISECONDS);
            if (bucket != null) {
                writeLock.lock();
                try {
                    while (bucket != null) {
                        timingWheel.advanceClock(bucket.getExpiration());
                        bucket.flush(this::addTimerTaskEntry);
                        bucket = delayQueue.poll();
                    }
                } finally {
                    writeLock.unlock();
                }
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public int size() {
        return taskCounter.get();
    }

    @Override
    public void shutDown() {
        taskExecutor.shutdown();
    }
}
