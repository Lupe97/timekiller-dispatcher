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
        } else if (expiration < currentTime + interval) { // 如果过期时间在当前的时间轮内，则直接在当前时间轮内计算
            // 找到当前时间点对应的bucket坐标
            // 首先除以每个格子的大小，从而求出总共需要经过多少个格子
            long virtualId = expiration / tickMs;
            // 然后再和一个时间轮的格子数取余，求出在当前时间轮的哪个格子上
            TimerTaskList bucket = buckets[(int) (virtualId % wheelSize)];
            bucket.add(timerTaskEntry);

            // 对于计算出的同一个bucket，尝试进行设置新的过期时间。因为同一个bucket轮子中是可以重复使用的。
            // 如果设置成功，说明当前bucket已经被重复使用了，需要重新排队。
            // 否则，虽然不同task的expiration不同，但由于经过除法计算出的bucket相同-->bucket的expiration相同，不会重复排队
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
