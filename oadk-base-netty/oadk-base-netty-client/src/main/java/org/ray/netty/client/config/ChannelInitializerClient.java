package org.ray.netty.client.config;


import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.FixedLengthFrameDecoder;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.timeout.IdleStateHandler;
import org.ray.netty.client.NettyClient;
import org.ray.netty.client.constant.NettyClientConstant;
import org.ray.netty.client.handler.ChannelManageHandler;
import org.ray.netty.client.handler.ClientHandler;
import org.ray.netty.client.handler.HeartBeatHandler;
import org.ray.oadk.core.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class ChannelInitializerClient extends ChannelInitializer {

    @Autowired
    private NettyClientConstant nettyClientConstant;

    private String serverUrl;

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception {
        String url = serverUrl;
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(65535));
        pipeline.addLast(new FixedLengthFrameDecoder(Long.BYTES));
        //添加空闲检测
        pipeline.addLast(new IdleStateHandler(0, nettyClientConstant.getHb_write_idle_time(), 0, TimeUnit.SECONDS));
        //添加心跳执行
        pipeline.addLast(new HeartBeatHandler());
        //添加连接管理
        pipeline.addLast(new ChannelManageHandler(url));
        //添加获取消息
        pipeline.addLast(new ClientHandler());
    }
}
