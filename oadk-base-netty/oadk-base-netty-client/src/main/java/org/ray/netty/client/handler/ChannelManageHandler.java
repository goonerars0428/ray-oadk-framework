package org.ray.netty.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.util.CharsetUtil;
import org.ray.netty.common.ChannelManage;
import org.ray.oadk.core.utils.LogUtil;

import java.net.URI;

/**
 * 处理连接handler
 */
public class ChannelManageHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    /**
     * 握手的状态信息
     */
    WebSocketClientHandshaker handshaker;
    /**
     * netty自带的异步处理
     */
    ChannelPromise handshakeFuture;

    public ChannelManageHandler(String websocketUrl) {
        handshaker = WebSocketClientHandshakerFactory.newHandshaker(URI.create(websocketUrl), WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse response) throws Exception {
        Channel channel = channelHandlerContext.channel();
        //进行握手操作
        if (!this.handshaker.isHandshakeComplete()) {
            try {
                //握手协议返回，设置结束握手
                this.handshaker.finishHandshake(channel, response);
                //设置成功
                this.handshakeFuture.setSuccess();
//                log.info("服务端发送消息:" + response.headers());
            } catch (WebSocketHandshakeException var7) {
                String errorMsg = String.format("握手失败,status:%s,reason:%s", response.status(), response.content().toString(CharsetUtil.UTF_8));
                this.handshakeFuture.setFailure(new Exception(errorMsg));
            }
        } else {
            throw new IllegalStateException("Unexpected FullHttpResponse (getStatus=" + response.status() + ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
        ChannelManage.addChannel(ctx.channel());
        LogUtil.log("服务端建立连接");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ChannelManage.removeChannel(ctx.channel());
        LogUtil.log("服务端断开连接");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        LogUtil.log(LogUtil.LogLevel.ERROR, "连接异常断开", cause.getMessage());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        this.handshakeFuture = ctx.newPromise();
    }

}
