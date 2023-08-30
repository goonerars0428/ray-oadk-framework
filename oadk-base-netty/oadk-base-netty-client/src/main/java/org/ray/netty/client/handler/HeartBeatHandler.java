package org.ray.netty.client.handler;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.ray.oadk.core.utils.LogUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳执行handler
 */
public class HeartBeatHandler extends ChannelDuplexHandler {

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        IdleStateEvent event = (IdleStateEvent)evt;
        IdleState state = event.state();
        if(state == IdleState.WRITER_IDLE) {
            Channel channel = ctx.channel();
            LogUtil.log("心跳执行",channel.id());
            //触发写空闲事件，发送心跳数据包
            ObjectNode objectNode = new ObjectNode(JsonNodeFactory.instance);
            objectNode.put("hb","ping");
            TextWebSocketFrame tws = new TextWebSocketFrame(objectNode.toString());
            channel.writeAndFlush(tws);
        }
    }
}
