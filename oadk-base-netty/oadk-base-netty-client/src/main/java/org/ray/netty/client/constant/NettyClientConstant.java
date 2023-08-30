package org.ray.netty.client.constant;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "constant.netty.client")
public class NettyClientConstant {

    /**
     * netty服务端IP
     */
    private String server_host;

    /**
     * netty服务端起始端口号
     */
    private Integer server_port_begin;
    /**
     * netty服务端截止端口号
     */
    private Integer server_port_end;
    /**
     * webservice服务路径
     */
    private String server_path;

    /**
     * 客户端与服务端创建连接的数量
     */
    private Integer channel_count;
    /**
     * 心跳检测-写空闲时间
     */
    private Integer hb_write_idle_time;
    /**
     * 服务端连接密钥
     */
    private String token;

    public String getServer_host() {
        return server_host;
    }

    public void setServer_host(String server_host) {
        this.server_host = server_host;
    }

    public Integer getServer_port_begin() {
        return server_port_begin;
    }

    public void setServer_port_begin(Integer server_port_begin) {
        this.server_port_begin = server_port_begin;
    }

    public Integer getServer_port_end() {
        return server_port_end;
    }

    public void setServer_port_end(Integer server_port_end) {
        this.server_port_end = server_port_end;
    }

    public String getServer_path() {
        return server_path;
    }

    public void setServer_path(String server_path) {
        this.server_path = server_path;
    }

    public Integer getChannel_count() {
        return channel_count;
    }

    public void setChannel_count(Integer channel_count) {
        this.channel_count = channel_count;
    }

    public Integer getHb_write_idle_time() {
        return hb_write_idle_time;
    }

    public void setHb_write_idle_time(Integer hb_write_idle_time) {
        this.hb_write_idle_time = hb_write_idle_time;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
