package com.moonflying.timekiller.core.timingwheel;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class TimerTaskList implements Delayed {
    private final AtomicInteger taskCounter;
    private final TimerTaskEntry root;
    private final AtomicLong expiration;

    public TimerTaskList(AtomicInteger taskCounter) {
        this.taskCounter = taskCounter;
        root = new TimerTaskEntry(null, -1L);
        root.next = root;
        root.prev = root;
        expiration = new AtomicLong(-1L);
    }

    // Set the bucket's expiration time
    // Returns true if the expiration time is changed
    boolean setExpiration(long expirationMs) {
        return expiration.getAndSet(expirationMs) != expirationMs;
    }

    long getExpiration() {
        return expiration.get();
    }

    void foreach(Consumer<TimerTask> f) {
        synchronized (this) {
            TimerTaskEntry entry = root.next;
            while (entry != root) {
                TimerTaskEntry nextEntry = entry.next;

                if (!entry.cancelled()) f.accept(entry.timerTask);

                entry = nextEntry;
            }
        }
    }

    // Add a timer task entry to this list
    void add(TimerTaskEntry timerTaskEntry) {
        boolean done = false;

        while (!done) {
            // Remove the timer task entry if it is already in any other list
            // We do this outside of the sync block below to avoid deadlocking.
            // We may retry until timerTaskEntry.list becomes null.
            timerTaskEntry.remove();

            synchronized (this) {
                synchronized (timerTaskEntry) {
                    if (timerTaskEntry.list == null) {
                        // put the timer task entry to the end of the list. (root.prev points to the tail entry)
                        TimerTaskEntry tail = root.prev;
                        timerTaskEntry.next = root;
                        timerTaskEntry.prev = tail;
                        tail.next = timerTaskEntry;
                        root.prev = timerTaskEntry;

                        timerTaskEntry.list = this;
                        taskCounter.incrementAndGet();
                        done = true;
                    }
                }
            }
        }
    }

    void remove(TimerTaskEntry timerTaskEntry) {
        synchronized (this) {
            synchronized (timerTaskEntry) {
                if (timerTaskEntry.list == this) {
                    timerTaskEntry.next.prev = timerTaskEntry.prev;
                    timerTaskEntry.prev.next = timerTaskEntry.next;
                    timerTaskEntry.next = null;
                    timerTaskEntry.prev = null;
                    timerTaskEntry.list = null;
                    taskCounter.decrementAndGet();
                }
            }
        }
    }

    // Remove all task entries and apply the supplied function to each of them
    void flush(Consumer<TimerTaskEntry> f) {
        synchronized (this) {
            TimerTaskEntry head = root.next;
            while (head != root) {
                // 首先从当前bucket中删除该taskEntry
                remove(head);
                // 再通过执行SystemTimer::addTimerTaskEntry方法来决定是否将当前taskEntry降级或执行(到期任务通过SystemTimer::addTimerTaskEntry方法执行)
                f.accept(head);
                head = root.next;
            }
            expiration.set(-1L);
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(Long.max(getExpiration() - TimeUnit.NANOSECONDS.toMillis(System.nanoTime()), 0), TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        if (other instanceof TimerTaskList) {
            return Long.compare(getExpiration(), ((TimerTaskList) other).getExpiration());
        }
        return 0;
    }
}
