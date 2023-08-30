package org.ray.netty.server.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;
import org.ray.netty.common.ChannelManage;
import org.ray.oadk.core.utils.LogUtil;

/**
 * 连接管理handler
 */
public class ChannelManageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {


    /**
     * 处理服务端webSocket握手
     */
    private WebSocketServerHandshaker handshaker;


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        //添加连接
        ChannelManage.addChannel(ctx.channel());
        ctx.fireChannelActive();
        LogUtil.log("客户端加入连接", ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        //断开连接
        ChannelManage.removeChannel(ctx.channel());
        ctx.fireChannelInactive();
        LogUtil.log("客户端断开连接", ctx.channel());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    /**
     * 唯一的一次http请求，用于创建websocket
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //解码http失败返回
        if (!request.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.BAD_REQUEST, ctx.alloc().buffer()));
            return;
        }

        if (!"websocket".equals(request.headers().get("Upgrade"))) {
            sendHttpResponse(ctx, request, new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.FORBIDDEN, ctx.alloc().buffer()));
            return;
        }

        //websocket握手，参数分别是ws地址，子协议，是否扩展，最大frame长度
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, true, 5 * 1024 * 1024);
        handshaker = factory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            handshaker.handshake(ctx.channel(), request);
        }
    }

    private String getWebSocketLocation(FullHttpRequest request) {
        String location = request.headers().get(HttpHeaderNames.HOST) + "/webSocket";
        return "ws://" + location;
    }

    /**
     * 拒绝不合法的请求，并返回错误信息
     */
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LogUtil.log(LogUtil.LogLevel.ERROR, "连接异常", cause.getMessage());
    }
}
