package com.moonflying.timekiller.core.timingwheel;

public class TimerTaskEntry implements Comparable<TimerTaskEntry> {
    volatile TimerTaskList list;
    TimerTaskEntry next;
    TimerTaskEntry prev;

    TimerTask timerTask;
    final Long expirationMs;

    TimerTaskEntry(TimerTask timerTask, Long expirationMs) {
        this.timerTask = timerTask;
        this.expirationMs = expirationMs;

        if (timerTask != null) {
            timerTask.setTimerTaskEntry(this);
        }
    }

    boolean cancelled() {
        return timerTask.getTimerTaskEntry() != this;
    }

    void remove() {
        TimerTaskList currentList = list;
        while (currentList != null) {
            currentList.remove(this);
            currentList = list;
        }
    }

    @Override
    public int compareTo(TimerTaskEntry that) {
        return Long.compare(expirationMs, that.expirationMs);
    }
}
