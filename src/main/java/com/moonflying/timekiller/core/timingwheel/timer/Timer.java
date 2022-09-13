package com.moonflying.timekiller.core.timingwheel.timer;

import com.moonflying.timekiller.core.timingwheel.task.TimerTask;

public interface Timer {
    void add(TimerTask timerTask);

    boolean advanceClock(Long timeoutMs);

    int size();

    void shutDown();
}
