package org.ray.netty.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.ray.oadk.core.utils.LogUtil;

//@ChannelHandler.Sharable

/**
 * 接收消息handler
 */
public class ServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {


    @Override
    public void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
//            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 判断是不是文本消息，不是文本抛异常
        if (!(frame instanceof TextWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
        String request = ((TextWebSocketFrame) frame).text();
        LogUtil.log("客户端IP:" + ctx.channel().remoteAddress(), "服务端收到消息:" + request);

        //处理客户端消息
        doHandleClientMsg(request);
        ctx.fireChannelRead(frame.retain());
    }

    private void doHandleClientMsg(String msg) {
    }


}
