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
            // 如果添加失败，再判断这个任务是否已经取消
            if (!timerTaskEntry.cancelled()) {
                // 如果没有取消，说明这个任务已经到期了，可以直接执行。
                taskExecutor.submit(timerTaskEntry.timerTask);
            }
        }
    }

    @Override
    public boolean advanceClock(Long timeoutMs) {
        try {
            // 使用优先级队列推动时间轮前进，这样的时间复杂度是O(1) {从最大堆中获得最大值的时间复杂度是O(1)}
            // update. 上述描述并不准确。从堆中获取最大值的时间复杂度虽然是O(1),但是get完之后还需要调整堆的顺序，这个时间复杂度是O(lgN)
            // in my view, 使用优先级队列的作用是不需要每毫秒都推进一次时间轮。试想，假如不使用优先级队列，则需要没毫秒都推进一格时间轮，
            // however，使用优先级队列后，只需要指定一次poll的过期时间，例如200ms，则会拉取200毫秒内过期的任务来执行，缺点是降低了精度
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
