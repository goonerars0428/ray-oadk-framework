package org.ray.data.redis.properties;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;

public class RedisExtProperties extends RedisProperties {

    /**
     * 自定义redis数据源标识
     */
    private String redisKey;
    /**
     * 自定义数据源类型
     */
    private RedisType redisType;

    public String getRedisKey() {
        return redisKey;
    }

    public void setRedisKey(String redisKey) {
        this.redisKey = redisKey;
    }

    public RedisType getRedisType() {
        return redisType;
    }

    public void setRedisType(RedisType redisType) {
        this.redisType = redisType;
    }

    public enum RedisType {

        /**
         * Use the Cluster redis type.
         */
        CLUSTER,

        /**
         * Use the Standalone redis type.
         */
        STANDALONE

    }
}
