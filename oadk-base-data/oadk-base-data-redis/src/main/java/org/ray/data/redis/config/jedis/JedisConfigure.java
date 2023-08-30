package org.ray.data.redis.config.jedis;

import org.ray.data.redis.config.RedisMutiConfig;
import org.ray.data.redis.properties.RedisExtProperties;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import redis.clients.jedis.JedisPoolConfig;

/**
 * jedis配置后续从RedisMutiConfig抽出至此
 */
public class JedisConfigure {

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

    public JedisConfigure(RedisProperties defaultConfig, RedisExtProperties customConfig) {
        this.defaultConfig = defaultConfig;
        this.customConfig = customConfig;
    }


    /**
     * 获取jedis连接配置
     *
     * @return
     */
    public JedisClientConfiguration getJedisClientConfiguration() {
        JedisClientConfiguration.JedisClientConfigurationBuilder builder = applyJedisProperties(JedisClientConfiguration.builder());
        RedisProperties.Pool pool = defaultConfig.getJedis().getPool();
        if (RedisMutiConfig.isPoolEnabled(pool)) {
            applyPooling(pool, builder);
        }
        if (org.springframework.util.StringUtils.hasText(customConfig.getUrl())) {
            customizeJedisConfigurationFromUrl(builder);
        }
        return builder.build();
    }

    /**
     * jedis-通过url参数自定义配置
     * @param builder
     */
    private void customizeJedisConfigurationFromUrl(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        RedisMutiConfig.ConnectionInfo connectionInfo = RedisMutiConfig.parseUrl(customConfig.getUrl());
        if (connectionInfo.isUseSsl()) {
            builder.useSsl();
        }
    }

    /**
     * jedis-封装连接池配置
     * @param pool
     * @return
     */
    private JedisPoolConfig jedisPoolConfig(RedisProperties.Pool pool) {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(pool.getMaxActive());
        config.setMaxIdle(pool.getMaxIdle());
        config.setMinIdle(pool.getMinIdle());
        if (pool.getTimeBetweenEvictionRuns() != null) {
            config.setTimeBetweenEvictionRuns(pool.getTimeBetweenEvictionRuns());
        }
        if (pool.getMaxWait() != null) {
            config.setMaxWait(pool.getMaxWait());
        }
        return config;
    }

    /**
     * jedis-应用连接池配置
     * @param pool
     * @param builder
     */
    private void applyPooling(RedisProperties.Pool pool,
                              JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        builder.usePooling().poolConfig(jedisPoolConfig(pool));
    }

    /**
     * jedis-应用其他配置
     *
     * @param builder
     * @return
     */
    private JedisClientConfiguration.JedisClientConfigurationBuilder applyJedisProperties(JedisClientConfiguration.JedisClientConfigurationBuilder builder) {
        PropertyMapper map = PropertyMapper.get().alwaysApplyingWhenNonNull();
        map.from(defaultConfig.isSsl()).whenTrue().toCall(builder::useSsl);
        map.from(defaultConfig.getTimeout()).to(builder::readTimeout);
        map.from(defaultConfig.getConnectTimeout()).to(builder::connectTimeout);
        map.from(defaultConfig.getClientName()).whenHasText().to(builder::clientName);
        return builder;
    }

}
