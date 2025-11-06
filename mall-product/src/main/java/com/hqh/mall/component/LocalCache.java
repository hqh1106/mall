package com.hqh.mall.component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hqh.mall.domain.PmsProductParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

/**
 * 缓存管理，LRU
 */
@Component
@Slf4j
public class LocalCache {

    private Cache<String, PmsProductParam> localCache = null;

    @PostConstruct
    private void init() {
        localCache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(500)
                .expireAfterWrite(60, TimeUnit.SECONDS).build();
    }

    public void setLocalCache(String key, PmsProductParam obj) {
        localCache.put(key, obj);
    }

    public PmsProductParam get(String key){
        return localCache.getIfPresent(key);
    }

}
