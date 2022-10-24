package com.moonflying.timekiller.core.engine;

import com.moonflying.timekiller.core.timingwheel.SystemTimer;
import com.moonflying.timekiller.core.timingwheel.Timer;
import com.moonflying.timekiller.msgproto.ScheduledTaskMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DispatcherStarter implements ApplicationRunner {
    public static volatile boolean isRunning = true;

    @Override
    public void run(ApplicationArguments args) throws InterruptedException {
        Timer timer = new SystemTimer("TimeKiller");
        startCommunicator(timer);
        work(timer);
    }

    private void startCommunicator(Timer timer) throws InterruptedException {
        //创建两个线程组
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup(); //8个NioEventLoop

        try {
            ServerBootstrap b = new ServerBootstrap();

            b.group(bossGroup, workerGroup)
                    // 使用NioSocketChannel 作为服务器的通道实现
                    .channel(NioServerSocketChannel.class)
                    // 设置线程队列得到连接个数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    // 设置保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            //获取到pipeline
                            ChannelPipeline pipeline = ch.pipeline();
                            //向pipeline加入解码器
                            pipeline.addLast("decoder", new ProtobufDecoder(ScheduledTaskMessage.TaskMessage.getDefaultInstance()));
                            //向pipeline加入编码器
                            pipeline.addLast("encoder", new ProtobufEncoder());
                            //加入自己的业务处理handler
                            pipeline.addLast(new DispatcherHandler(timer));

                        }
                    });

            System.out.println("netty 服务器启动");
            ChannelFuture channelFuture = b.bind(9999).sync();

            //监听关闭
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private void work(Timer timer) {
        while (isRunning) {
            timer.advanceClock(200L);
        }
    }

    public void stopRunning() {
        isRunning = false;
    }
}
