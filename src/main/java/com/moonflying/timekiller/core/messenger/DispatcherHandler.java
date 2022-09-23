package com.moonflying.timekiller.core.messenger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DispatcherHandler extends SimpleChannelInboundHandler {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        // 根据app name, 如果该app已经存在定时任务, 则立即停止执行并根据最新的信息生成最新的定时任务执行
        // 如果不存在则直接执行生成定时任务执行
        // 如何解析corn表达式
    }
}
