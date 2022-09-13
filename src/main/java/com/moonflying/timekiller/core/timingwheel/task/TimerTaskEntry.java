package com.moonflying.timekiller.core.timingwheel.task;

public class TimerTaskEntry implements Comparable<TimerTaskEntry> {
    volatile TimerTaskList list;
    protected TimerTaskEntry next;
    protected TimerTaskEntry prev;

    protected TimerTask timerTask;
    private final Long expirationMs;

    public TimerTaskEntry(TimerTask timerTask, Long expirationMs) {
        this.timerTask = timerTask;
        this.expirationMs = expirationMs;

        if (timerTask != null) {
            timerTask.setTimerTaskEntry(this);
        }
    }

    public boolean cancelled() {
        return timerTask.getTimerTaskEntry() != this;
    }

    public void remove() {
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
