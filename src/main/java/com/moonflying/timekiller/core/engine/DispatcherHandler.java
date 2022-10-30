package com.moonflying.timekiller.core.engine;

import com.moonflying.timekiller.core.task.TimeKillerTask;
import com.moonflying.timekiller.core.timingwheel.Timer;
import com.moonflying.timekiller.util.TimingWheelUtils;
import com.moonflying.timekiller.msgproto.ScheduledTaskMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import java.util.List;

public class DispatcherHandler extends SimpleChannelInboundHandler<ScheduledTaskMessage.TaskMessage> {
    private final Timer timer;

    public DispatcherHandler(Timer timer) {
        this.timer = timer;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ScheduledTaskMessage.TaskMessage msg) throws Exception {
        // TODO 根据app name和task name, 如果该app已经存在定时任务, 则立即停止执行并根据最新的信息生成最新的定时任务执行
        // TODO 如果不存在则直接执行生成定时任务执行
        ScheduledTaskMessage.TaskMessage.DataType dataType = msg.getDataType();
        if(dataType == ScheduledTaskMessage.TaskMessage.DataType.RegisterRequest) {
            ScheduledTaskMessage.RegisterScheduledTaskRequest taskRequests = msg.getRegisterRequest();
            List<ScheduledTaskMessage.ScheduledTask> scheduledTasksList = taskRequests.getScheduledTasksList();
            scheduledTasksList.forEach(scheduledTask -> {
                        long expirationMs = TimingWheelUtils.parseCron(scheduledTask.getZone(), scheduledTask.getCorn());
                        if (expirationMs > 0L) {
                            timer.add(new TimeKillerTask(
                                    scheduledTask.getAppName(), scheduledTask.getTaskName(),
                                    scheduledTask.getZone(), scheduledTask.getCorn(), expirationMs, this.timer
                            ));
                            // 将channel与appname进行映射并保存
                            TimeKillerTask.appChannelMapping.put(scheduledTask.getAppName(), ctx.channel());
                        }
                    }
            );
        } else if(dataType == ScheduledTaskMessage.TaskMessage.DataType.ExecuteScheduledTaskResponse) {
            // 根据响应结果对任务进行下一步处理（重试或者丢弃...）
            ScheduledTaskMessage.ExecuteScheduledTaskResponse response = msg.getExecuteResponse();
            System.out.println(response.getCode());
        }
    }

    //数据读取完毕
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(Unpooled.copiedBuffer("hello, 客户端~(>^ω^<)喵1", CharsetUtil.UTF_8));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}