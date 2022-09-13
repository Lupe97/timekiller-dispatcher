package com.moonflying.timekiller.core.timingwheel;

import java.util.concurrent.TimeUnit;

public class Time {
    public static Long getHiresClockMs() {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime());
    }
}
