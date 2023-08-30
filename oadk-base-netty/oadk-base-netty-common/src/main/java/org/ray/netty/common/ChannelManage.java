package org.ray.netty.common;

import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.ray.oadk.core.utils.LogUtil;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * netty核心连接管理器
 */
public class ChannelManage {

    static ChannelGroup GLOBAL_GROUP = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    static ConcurrentHashMap<String, Channel> CHANNEL_MAP = new ConcurrentHashMap<>();

    public ChannelManage(Integer manageChannelCheck) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> LogUtil.log("当前连接数", CHANNEL_MAP.size()), 0, manageChannelCheck, TimeUnit.SECONDS);
    }

    /**
     * 添加通道
     *
     * @param channel
     */
    public static void addChannel(Channel channel) {
        GLOBAL_GROUP.add(channel);
        CHANNEL_MAP.put(channel.id().toString(), channel);
    }

    /**
     * 移除通道
     *
     * @param channel
     */
    public static void removeChannel(Channel channel) {
        GLOBAL_GROUP.remove(channel);
        CHANNEL_MAP.remove(channel.id().toString());
    }

    /**
     * 查找通道
     *
     * @param channelId
     * @return
     */
    public static Channel findChannel(String channelId) {
        return CHANNEL_MAP.get(channelId);
    }

    public static ChannelGroup getGlobalGroup() {
        return GLOBAL_GROUP;
    }

    public static ConcurrentHashMap<String, Channel> getChannelMap() {
        return CHANNEL_MAP;
    }
}
