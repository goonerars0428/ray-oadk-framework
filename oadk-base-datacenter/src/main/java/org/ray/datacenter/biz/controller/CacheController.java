package org.ray.datacenter.biz.controller;

import org.apache.commons.lang3.StringUtils;
import org.ray.datacenter.biz.domain.dto.CacheDTO;
import org.ray.datacenter.biz.domain.dto.CommonDTO;
import org.ray.datacenter.biz.domain.dto.ResponseDTO;
import org.ray.oadk.core.utils.Pagination;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * 缓存控制controller
 * 存在cacheManager时才加载，避免项目未使用缓存时启动报错
 */
@RestController("oadk-cacheController")
@RequestMapping("oadk/cache")
public class CacheController {

    @Autowired
    private CacheManager cacheManager;

    @GetMapping(value = "name/list", name = "获取jvm中缓存名称")
    public ResponseDTO cacheNameList() {
        Collection<String> cacheNames = cacheManager.getCacheNames();
        return new ResponseDTO(new CommonDTO(cacheNames));
    }

    @GetMapping(value = "key/list", name = "获取jvm中指定缓存的key")
    public ResponseDTO cacheKeyList(String cacheName, Integer pageIndex, Integer pageSize) {
        Collection<Object> keys = new ArrayList<>();
        Cache cache = cacheManager.getCache(cacheName);
        Object nativeCache = cache.getNativeCache();
        if (nativeCache instanceof javax.cache.Cache) {
            javax.cache.Cache javaCache = (javax.cache.Cache) nativeCache;
            if (javaCache == null) {
                return new ResponseDTO(new CommonDTO());
            }
            Iterator<javax.cache.Cache.Entry> iterator = javaCache.iterator();
            Pagination pagination = new Pagination(pageIndex, pageSize);
            int count = 1;
            while (iterator.hasNext()) {
                int i = count++;
                if (i > (pagination.getStartRow() + pagination.getPageSize())) {
                    break;
                }
                Object key = iterator.next().getKey();
                if (i > pagination.getStartRow()) {
                    keys.add(key);
                }
            }
            return new ResponseDTO(new CommonDTO(keys));
        }
        return new ResponseDTO(new CommonDTO());
    }

    @GetMapping(value = "info", name = "获取jvm中指定缓存指定key的内容")
    public ResponseDTO cacheInfo(String cacheName, String cacheKey) {
        Cache cache = cacheManager.getCache(cacheName);
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
        Object cacheValue = null;
        if (valueWrapper != null) {
            cacheValue = valueWrapper.get();
        }
        return new ResponseDTO(new CommonDTO(cacheValue));
    }

    @PostMapping(value = "clean", name = "清理jvm中指定缓存指定key的内容")
    public ResponseDTO jvmCacheClean(@RequestBody Collection<CacheDTO> caches) {

        if (caches != null && !caches.isEmpty()) {
            for (CacheDTO cacheDTO : caches) {
                String cacheName = cacheDTO.getCacheName();
                List<String> cacheKeys = cacheDTO.getCacheKeys();
                if (StringUtils.isNotBlank(cacheName) && cacheManager.getCache(cacheName) != null) {
                    Cache cache = cacheManager.getCache(cacheName);
                    if (cacheKeys == null || cacheKeys.isEmpty()) {
                        cache.clear();
                    } else {
                        for (String key : cacheKeys) {
                            cache.evict(key);
                        }
                    }
                }
            }
        } else {
            Collection<String> cacheNames = cacheManager.getCacheNames();
            for (String cacheName : cacheNames) {
                cacheManager.getCache(cacheName).clear();
            }
        }
        return new ResponseDTO();
    }
}
