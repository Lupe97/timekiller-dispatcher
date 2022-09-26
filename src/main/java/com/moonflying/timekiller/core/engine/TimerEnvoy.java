package com.moonflying.timekiller.core.engine;

import com.moonflying.timekiller.core.timingwheel.SystemTimer;
import com.moonflying.timekiller.core.timingwheel.Timer;
import com.moonflying.timekiller.core.timingwheel.TimerTask;

public class TimerEnvoy {
    private Timer timer;

    public TimerEnvoy() {
        timer = new SystemTimer("Scheduled");
    }

    public void addTask(TimerTask task) {
        timer.add(task);
    }
}
