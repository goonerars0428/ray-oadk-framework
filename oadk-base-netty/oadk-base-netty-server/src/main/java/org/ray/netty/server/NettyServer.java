package org.ray.netty.server;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.ray.netty.common.ChannelManage;
import org.ray.netty.server.config.ChannelInitializerServer;
import org.ray.netty.server.constant.NettyServerConstant;
import org.ray.oadk.core.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class NettyServer {


    @Autowired
    private ChannelInitializerServer channelInitializer;
    @Autowired
    private NettyServerConstant nettyServerConstant;

    public void initServer() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
//                    .handler(new LoggingHandler(LogLevel.INFO))
                    .option(ChannelOption.SO_BACKLOG, 1024 * 1024 * 10)
                    .childOption(ChannelOption.SO_REUSEADDR, true);

            bootstrap.childHandler(channelInitializer);
            for (int i = 0; i <= (nettyServerConstant.getPort_end() - nettyServerConstant.getPort_begin()); i++) {
                int p = nettyServerConstant.getPort_begin() + i;
                bind(bootstrap, p);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
/*            LogUtil.log("优雅关闭执行");
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();*/
        }
    }

    void bind(ServerBootstrap bootstrap, Integer port) throws InterruptedException {
        ChannelFuture channelFuture = bootstrap.bind(port).sync().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                LogUtil.log("netty server @ port:" + port);
            }
        });
//        channelFuture.channel().closeFuture().sync();
    }

    @PostConstruct
    public void init() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initServer();
            }
        }).start();
        new ChannelManage(nettyServerConstant.getManage_channel_check());
    }

}
