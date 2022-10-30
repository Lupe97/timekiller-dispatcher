package com.moonflying.timekiller.core.task;

import com.moonflying.timekiller.core.timingwheel.Timer;
import com.moonflying.timekiller.core.timingwheel.TimerTask;
import com.moonflying.timekiller.msgproto.ScheduledTaskMessage;
import com.moonflying.timekiller.util.TimingWheelUtils;
import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

public class TimeKillerTask extends TimerTask {
    public static ConcurrentHashMap<String, Channel> appChannelMapping = new ConcurrentHashMap<>();

    private final String appName;
    private final String taskName;
    private final String zone;
    private final String cron;
    private final Timer timer;

    public TimeKillerTask(String appName, String taskName, String zone, String cron, long expirationTime, Timer timer) {
        this.appName = appName;
        this.taskName = taskName;
        this.zone = zone;
        this.cron = cron;
        expirationMs = expirationTime;
        this.timer = timer;
    }

    @Override
    public void run() {
        Channel channel = appChannelMapping.get(this.appName);
        if (channel != null) {
            ScheduledTaskMessage.TaskMessage executeMessage = ScheduledTaskMessage.TaskMessage.newBuilder()
                    .setDataType(ScheduledTaskMessage.TaskMessage.DataType.ExecuteScheduledTaskRequest)
                    .setExecuteRequest(
                            ScheduledTaskMessage.ExecuteScheduledTaskRequest.newBuilder()
                                    .setTaskName(this.taskName)
                                    .build()
                    )
                    .build();
            channel.writeAndFlush(executeMessage);
        }

        // 将任务再次加入时间轮，如果还有下次执行计划
        long nextExpirationMs = TimingWheelUtils.parseCron(this.zone, this.cron);
        if (nextExpirationMs > 0L) {
            timer.add(new TimeKillerTask(this.appName, this.appName, this.zone, this.cron, nextExpirationMs, this.timer));
        }
    }
}
