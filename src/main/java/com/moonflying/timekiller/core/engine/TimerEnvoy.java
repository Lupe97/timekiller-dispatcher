package com.moonflying.timekiller.core.engine;

import com.moonflying.timekiller.core.timingwheel.SystemTimer;
import com.moonflying.timekiller.core.timingwheel.Timer;
import com.moonflying.timekiller.core.timingwheel.TimerTask;

public class TimerEnvoy {
    public volatile boolean isRunning = true;

    private final Timer timer;

    public TimerEnvoy() {
        timer = new SystemTimer("Scheduled");
        this.work();
    }

    public void addTask(TimerTask task) {
        timer.add(task);
    }

    private void work() {
        while (isRunning) {
            timer.advanceClock(200L);
        }
    }
}
