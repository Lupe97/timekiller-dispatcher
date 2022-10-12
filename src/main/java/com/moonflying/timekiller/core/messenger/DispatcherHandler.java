package com.moonflying.timekiller.core.messenger;

import com.moonflying.timekiller.msgproto.ScheduledTaskMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import java.util.List;

public class DispatcherHandler extends SimpleChannelInboundHandler<ScheduledTaskMessage.TaskMessage> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ScheduledTaskMessage.TaskMessage msg) throws Exception {
        // 根据app name, 如果该app已经存在定时任务, 则立即停止执行并根据最新的信息生成最新的定时任务执行
        // 如果不存在则直接执行生成定时任务执行
        // 如何解析corn表达式
        ScheduledTaskMessage.TaskMessage.DataType dataType = msg.getDataType();
        if(dataType == ScheduledTaskMessage.TaskMessage.DataType.RegisterRequest) {
            ScheduledTaskMessage.RegisterScheduledTaskRequest taskRequests = msg.getRegisterRequest();
            List<ScheduledTaskMessage.ScheduledTask> scheduledTasksList = taskRequests.getScheduledTasksList();
            System.out.println(scheduledTasksList);
        } else if(dataType == ScheduledTaskMessage.TaskMessage.DataType.ExecuteScheduledTaskResponse) {
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