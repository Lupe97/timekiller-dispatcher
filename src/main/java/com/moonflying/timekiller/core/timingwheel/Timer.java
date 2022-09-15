package com.moonflying.timekiller.core.timingwheel;

public interface Timer {
    void add(TimerTask timerTask);

    boolean advanceClock(Long timeoutMs);

    int size();

    void shutDown();
}
