package org.ray.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.ray.netty.client.config.ChannelInitializerClient;
import org.ray.netty.client.constant.NettyClientConstant;
import org.ray.netty.common.ChannelManage;
import org.ray.oadk.core.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.net.URI;

@Component
public class NettyClient {

    public static ThreadLocal<String> SERVER_URL_HOLDER = new ThreadLocal<>();
    @Autowired
    private NettyClientConstant nettyClientConstant;
    @Autowired
    private ChannelInitializerClient channelInitializerClient;

    public void initClient(String nettyServerUrl) {
        URI websocketURI = URI.create(nettyServerUrl);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        try {
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 60000)
                    .option(ChannelOption.SO_REUSEADDR, true);
            //todo 此处存在线程安全问题
            channelInitializerClient.setServerUrl(nettyServerUrl);
            //添加handler
            bootstrap.handler(channelInitializerClient);
            //建立连接
            for (int i = 0; i < nettyClientConstant.getChannel_count(); i++) {
                Channel channel = connect(bootstrap, websocketURI);
                ChannelManage.addChannel(channel);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
/*            LogUtil.log("优雅关闭执行");
            workerGroup.shutdownGracefully();*/
        }
    }

    public Channel connect(Bootstrap bootstrap, URI websocketURI) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.connect(websocketURI.getHost(), websocketURI.getPort()).sync().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    LogUtil.log(LogUtil.LogLevel.ERROR, "创建连接失败");
                }
            }
        });
        return channelFuture.channel();
    }

    /**
     * 初始化netty客户端
     */
    @PostConstruct
    public void init() {
        for (int i = 0; i <= (nettyClientConstant.getServer_port_end() - nettyClientConstant.getServer_port_begin()); i++) {
            String nettyServerUrl = new StringBuilder("ws://").append(nettyClientConstant.getServer_host()).append(":").append(nettyClientConstant.getServer_port_begin() + i)
                    .append(nettyClientConstant.getServer_path()).append("/").append(nettyClientConstant.getToken()).toString();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    initClient(nettyServerUrl);
                }
            }).start();
        }
    }
}
