package org.ray.netty.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.ray.oadk.core.utils.LogUtil;

public class Test1Handler extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        LogUtil.log("test1 read");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.log("test1 active");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtil.log("test1 inactive");
    }
}
