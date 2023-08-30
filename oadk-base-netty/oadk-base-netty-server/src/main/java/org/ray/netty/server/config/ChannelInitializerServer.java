package org.ray.netty.server.config;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.ray.netty.server.constant.NettyServerConstant;
import org.ray.netty.server.handler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@ConditionalOnMissingBean(name = "extChannelInitializerServer")
public class ChannelInitializerServer extends ChannelInitializer<SocketChannel> {

    @Autowired
    private NettyServerConstant nettyServerConstant;


    public LinkedHashMap<String,ChannelHandler> createHandler() {
        //key:操作-当前handler名称-相关handler名称;value:handler对象
        //操作标识:0:addFirst;1:addLast;2:addBefore;3:addAfter
        LinkedHashMap<String,ChannelHandler> map = new LinkedHashMap<>();

        map.put(createHandlerKey(1,"HttpServerCodec"),new HttpServerCodec());
        map.put(createHandlerKey(1,"ChunkedWriteHandler"),new ChunkedWriteHandler());
        map.put(createHandlerKey(1,"HttpObjectAggregator"),new HttpObjectAggregator(65535));
        map.put(createHandlerKey(1,"FixedLengthFrameDecoder"),new FixedLengthFrameDecoder(Long.BYTES));
        //添加空闲检测
        map.put(createHandlerKey(1,"IdleStateHandler"),new IdleStateHandler(nettyServerConstant.getHb_read_idle_time(), 0, 0, TimeUnit.SECONDS));
        //添加心跳处理
        map.put(createHandlerKey(3,"HeartBeatHandler","IdleStateHandler"),new HeartBeatHandler(nettyServerConstant.getHb_threshold()));
        //添加连接管理
        map.put(createHandlerKey(1,"ChannelManageHandler"),new ChannelManageHandler());
        //添加获取消息
        map.put(createHandlerKey(1,"ServerHandler"),new ServerHandler());
        //测试
        map.put(createHandlerKey(1,"test1handler"),new Test1Handler());
        map.put(createHandlerKey(1,"test2handler"),new Test2Handler());
        return map;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        Map<String,ChannelHandler> handler = createHandler();
        for (String handlerKey : handler.keySet()) {
            ChannelHandler channelHandler = handler.get(handlerKey);
            String[] split = handlerKey.split("-");
            switch (split[0]) {
                case "0":
                    pipeline.addFirst(split[1],channelHandler);
                    break;
                case "1":
                    pipeline.addLast(split[1],channelHandler);
                    break;
                case "2":
                    pipeline.addBefore(split[2],split[1],channelHandler);
                    break;
                case "3":
                    pipeline.addAfter(split[2],split[1],channelHandler);
                    break;
            }
        }
    }

    protected String createHandlerKey(Integer operate,String handlerName,String handlerName2) {
        return new StringBuilder().append(operate).append("-").append(handlerName).append("-").append(handlerName2).toString();
    }
    protected String createHandlerKey(Integer operate,String handlerName) {
        return createHandlerKey(operate,handlerName,"");
    }
}
