package org.ray.datacenter.biz.domain.dto;

import java.io.Serializable;
import java.util.List;

/**
 * @auth congr@inspur.com
 * @date 2019/8/7
 * @description 缓存post请求接收对象
 */
public class CacheDTO implements Serializable {

    /**
     * 缓存名称
     */
    String cacheName;
    /**
     * 缓存指定key列表
     */
    List<String> cacheKeys;

    public String getCacheName() {
        return cacheName;
    }

    public void setCacheName(String cacheName) {
        this.cacheName = cacheName;
    }

    public List<String> getCacheKeys() {
        return cacheKeys;
    }

    public void setCacheKeys(List<String> cacheKeys) {
        this.cacheKeys = cacheKeys;
    }
}
