package org.ray.netty.server.handler;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleStateEvent;
import org.ray.oadk.core.utils.LogUtil;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 心跳处理handler
 */
public class HeartBeatHandler extends ChannelDuplexHandler {

    private AtomicInteger readIdleTimes = new AtomicInteger();
    private AtomicInteger writeIdleTimes = new AtomicInteger();
    private AtomicInteger allIdleTimes = new AtomicInteger();

    private Integer threshold;

    public HeartBeatHandler(Integer threshold) {
        this.threshold = threshold;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent) evt;
        String eventType = null;
        switch (event.state()) {
            case READER_IDLE:
                eventType = "读空闲";
                readIdleTimes.incrementAndGet();
                break;
            case WRITER_IDLE:
                eventType = "写空闲";
                writeIdleTimes.incrementAndGet();
                break;
            case ALL_IDLE:
                eventType = "读写空闲";
                allIdleTimes.incrementAndGet();
                break;
        }
        LogUtil.log(ctx.channel().remoteAddress() + "-超时事件", eventType);
        if (readIdleTimes.get() >= threshold || writeIdleTimes.get() >= threshold || allIdleTimes.get() >= threshold) {
            LogUtil.log("空闲超过阈值，连接关闭", ctx.channel().remoteAddress());
            TextWebSocketFrame tws = new TextWebSocketFrame("idle close");
            ctx.channel().writeAndFlush(tws).sync().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    channelFuture.channel().close();
                }
            });
        }
    }
}
