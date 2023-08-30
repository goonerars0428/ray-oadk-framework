package org.ray.netty.server.constant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "constant.netty.server")
public class NettyServerConstant {

    /**
     * netty server 起始端口
     */
    Integer port_begin;

    /**
     * netty server 终止端口
     */
    Integer port_end;

    /**
     * 连接管理检查时间
     */
    Integer manage_channel_check;
    /**
     * 心跳检查时间
     */
    Integer hb_read_idle_time;
    /**
     * 心跳超时阈值
     */
    Integer hb_threshold;

    public Integer getPort_begin() {
        return port_begin;
    }

    public void setPort_begin(Integer port_begin) {
        this.port_begin = port_begin;
    }

    public Integer getPort_end() {
        return port_end;
    }

    public void setPort_end(Integer port_end) {
        this.port_end = port_end;
    }

    public Integer getManage_channel_check() {
        return manage_channel_check;
    }

    public void setManage_channel_check(Integer manage_channel_check) {
        this.manage_channel_check = manage_channel_check;
    }

    public Integer getHb_read_idle_time() {
        return hb_read_idle_time;
    }

    public void setHb_read_idle_time(Integer hb_read_idle_time) {
        this.hb_read_idle_time = hb_read_idle_time;
    }

    public Integer getHb_threshold() {
        return hb_threshold;
    }

    public void setHb_threshold(Integer hb_threshold) {
        this.hb_threshold = hb_threshold;
    }
}
