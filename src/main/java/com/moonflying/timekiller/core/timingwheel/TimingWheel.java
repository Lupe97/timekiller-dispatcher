package com.moonflying.timekiller.core.timingwheel;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TimingWheel {
    private final long tickMs;

    private final int wheelSize;

    private final long startMs;

    private final AtomicInteger taskCounter;

    private final DelayQueue<TimerTaskList> queue;

    private final long interval;

    private final TimerTaskList[] buckets;

    private long currentTime;

    private volatile TimingWheel overflowWheel;

    private void addOverflowWheel() {
        synchronized (this) {
            if (overflowWheel == null) {
                overflowWheel = new TimingWheel(interval, wheelSize, currentTime, taskCounter, queue);
            }
        }
    }

    TimingWheel(long tickMs, int wheelSize, long startMs, AtomicInteger taskCounter, DelayQueue<TimerTaskList> queue) {
        this.tickMs = tickMs;
        this.wheelSize = wheelSize;
        this.interval = tickMs * wheelSize;
        this.startMs = startMs;
        this.currentTime = this.startMs - (this.startMs % tickMs);
        this.taskCounter = taskCounter;
        this.queue = queue;

        this.buckets = new TimerTaskList[wheelSize];
        for (int i = 0; i < wheelSize; i++) {
            buckets[i] = new TimerTaskList(taskCounter);
        }
    }

    boolean add(TimerTaskEntry timerTaskEntry) {
        long expiration = timerTaskEntry.expirationMs;
        if (timerTaskEntry.cancelled()) {
            return false;
        } else if (expiration < currentTime + tickMs) {
            return false;
        } else if (expiration < currentTime + interval) {
            // 这里算法的含义是什么
            long virtualId = expiration / tickMs;
            TimerTaskList bucket = buckets[(int) (virtualId % tickMs)];
            bucket.add(timerTaskEntry);

            // 这里是什么意思，使用优先级队列的原因
            if (bucket.setExpiration(virtualId * tickMs)) {
                queue.offer(bucket);
            }
            return true;
        } else {
            if (overflowWheel == null) addOverflowWheel();
            return overflowWheel.add(timerTaskEntry);
        }
    }

    void advanceClock(long timeMs) {
        if (timeMs >= currentTime + tickMs) {
            currentTime = timeMs - (timeMs % tickMs);

            if (overflowWheel != null) overflowWheel.advanceClock(currentTime);
        }
    }
}
