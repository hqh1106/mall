package com.hqh.mall.component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hqh.mall.utils.RedisOpsExtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LocalCache<T> {
    private RedisOpsExtUtil redisOpsExtUtil;
    private Cache<String, T> localCache = null;

    @PostConstruct
    private void init() {
        localCache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(500)
                .expireAfterWrite(60, TimeUnit.SECONDS)
                .build();
    }

    public void setLocalCache(String key, T value) {
        localCache.put(key, value);
    }

    public <T> T getCache(String key) {
        return (T) localCache.getIfPresent(key);
    }

    public void remove(String key) {
        localCache.invalidate(key);
    }
}