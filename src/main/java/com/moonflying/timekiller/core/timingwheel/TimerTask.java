package com.moonflying.timekiller.core.timingwheel;

public abstract class TimerTask implements Runnable{
    protected long expirationMs;

    private TimerTaskEntry timerTaskEntry;

    void cancel() {
        synchronized (this) {
            if (timerTaskEntry != null) timerTaskEntry.remove();
            timerTaskEntry = null;
        }
    }

    void setTimerTaskEntry(TimerTaskEntry entry) {
        synchronized (this) {
            // if this timerTask is already held by an existing timer task entry,
            // we will remove such an entry first.
            if (timerTaskEntry != null && timerTaskEntry != entry)
                timerTaskEntry.remove();
            timerTaskEntry = entry;
        }
    }

    TimerTaskEntry getTimerTaskEntry() {
        return timerTaskEntry;
    }
}
