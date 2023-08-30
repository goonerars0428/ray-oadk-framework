package org.ray.netty.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;
import org.ray.oadk.core.utils.LogUtil;

/**
 * 获取消息handler
 */
public class ClientHandler extends SimpleChannelInboundHandler {


    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
//        log.info("当前握手状态:" + this.handshaker.isHandshakeComplete());
        Channel channel = ctx.channel();
        //接收服务端的消息
        WebSocketFrame frame = (WebSocketFrame) msg;
        //文本信息
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
            LogUtil.log("WebSocket Client received TextWebSocketFrame", textFrame.text());
        }
        //二进制信息
        if (frame instanceof BinaryWebSocketFrame) {
            BinaryWebSocketFrame binFrame = (BinaryWebSocketFrame) frame;
            LogUtil.log("BinaryWebSocketFrame");
        }
        //ping信息
        if (frame instanceof PongWebSocketFrame) {
            LogUtil.log("WebSocket Client received pong");
        }
        //关闭消息
        if (frame instanceof CloseWebSocketFrame) {
            LogUtil.log("WebSocket Client receive close frame");
            channel.close();
        }
    }

}
