package org.ray.data.redis.config.lettuce;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.cluster.ClusterClientOptions;
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.ray.data.redis.config.RedisMutiConfig;
import org.ray.data.redis.properties.RedisExtProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;

import java.time.Duration;

/**
 * lettuce配置后续从RedisMutiConfig抽出至此
 */
public class LettuceConfigure {

    private RedisProperties defaultConfig;

    private RedisExtProperties customConfig;

    public RedisProperties getDefaultConfig() {
        return defaultConfig;
    }

    public void setDefaultConfig(RedisProperties defaultConfig) {
        this.defaultConfig = defaultConfig;
    }

    public RedisExtProperties getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(RedisExtProperties customConfig) {
        this.customConfig = customConfig;
    }

    public LettuceConfigure(RedisProperties defaultConfig, RedisExtProperties customConfig) {
        this.defaultConfig = defaultConfig;
        this.customConfig = customConfig;
    }


    /**
     * 获取lettuce连接配置
     *
     * @return
     */
    public LettuceClientConfiguration getLettuceClientConfiguration() {
        RedisProperties.Pool pool = defaultConfig.getLettuce().getPool();
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = createBuilder(pool);
        applyLettuceProperties(builder);
        if (org.springframework.util.StringUtils.hasText(customConfig.getUrl())) {
            customizeLettuceConfigurationFromUrl(builder);
        }
        builder.clientOptions(createClientOptions());
        return builder.build();
    }

    private ClientOptions createClientOptions() {
        ClientOptions.Builder builder = initializeClientOptionsBuilder();
        Duration connectTimeout = defaultConfig.getConnectTimeout();
        if (connectTimeout != null) {
            builder.socketOptions(SocketOptions.builder().connectTimeout(connectTimeout).build());
        }
        return builder.timeoutOptions(TimeoutOptions.enabled()).build();
    }

    /**
     * lettuce-处理集群刷新参数
     *
     * @return
     */
    private ClientOptions.Builder initializeClientOptionsBuilder() {
        RedisProperties.Cluster cluster = defaultConfig.getCluster();
        if (cluster != null) {
            ClusterClientOptions.Builder builder = ClusterClientOptions.builder();
            RedisProperties.Lettuce.Cluster.Refresh refreshProperties = defaultConfig.getLettuce().getCluster().getRefresh();
            ClusterTopologyRefreshOptions.Builder refreshBuilder = ClusterTopologyRefreshOptions.builder()
                    .dynamicRefreshSources(refreshProperties.isDynamicRefreshSources());
            if (refreshProperties.getPeriod() != null) {
                refreshBuilder.enablePeriodicRefresh(refreshProperties.getPeriod());
            }
            if (refreshProperties.isAdaptive()) {
                refreshBuilder.enableAllAdaptiveRefreshTriggers();
            }
            return builder.topologyRefreshOptions(refreshBuilder.build());
        }
        return ClientOptions.builder();
    }

    /**
     * lettuce-处理配置参数url
     *
     * @param builder
     */
    private void customizeLettuceConfigurationFromUrl(LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        RedisMutiConfig.ConnectionInfo connectionInfo = RedisMutiConfig.parseUrl(customConfig.getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }
    }

    /**
     * lettuce-应用属性
     *
     * @param builder
     * @return
     */
    private LettuceClientConfiguration.LettuceClientConfigurationBuilder applyLettuceProperties(
            LettuceClientConfiguration.LettuceClientConfigurationBuilder builder) {
        if (defaultConfig.isSsl()) {
            builder.useSsl();
        }
        Duration timeout = defaultConfig.getTimeout();
        if (timeout != null) {
            builder.commandTimeout(timeout);
        }
        RedisProperties.Lettuce lettuce = defaultConfig.getLettuce();
        if (lettuce != null) {
            if (lettuce.getShutdownTimeout() != null && !lettuce.getShutdownTimeout().isZero()) {
                builder.shutdownTimeout(lettuce.getShutdownTimeout());
            }
        }
        String clientName = defaultConfig.getClientName();
        if (org.springframework.util.StringUtils.hasText(clientName)) {
            builder.clientName(clientName);
        }
        return builder;
    }

    /**
     * lettuce-创建builder
     *
     * @param pool
     * @return
     */
    private LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool pool) {
        if (RedisMutiConfig.isPoolEnabled(pool)) {
            return new PoolBuilderFactory().createBuilder(pool);
        }
        return LettuceClientConfiguration.builder();
    }

    /**
     * lettuce-内部类
     */
    static class PoolBuilderFactory {

        LettuceClientConfiguration.LettuceClientConfigurationBuilder createBuilder(RedisProperties.Pool properties) {
            return LettucePoolingClientConfiguration.builder().poolConfig(getPoolConfig(properties));
        }

        private GenericObjectPoolConfig<?> getPoolConfig(RedisProperties.Pool properties) {
            GenericObjectPoolConfig<?> config = new GenericObjectPoolConfig<>();
            config.setMaxTotal(properties.getMaxActive());
            config.setMaxIdle(properties.getMaxIdle());
            config.setMinIdle(properties.getMinIdle());
            if (properties.getTimeBetweenEvictionRuns() != null) {
                config.setTimeBetweenEvictionRuns(properties.getTimeBetweenEvictionRuns());
            }
            if (properties.getMaxWait() != null) {
                config.setMaxWait(properties.getMaxWait());
            }
            return config;
        }

    }
}
