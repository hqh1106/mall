package com.hqh.mall.component;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class LocalCache<T> {
    private Cache<String, T> localCache = null;

    @PostConstruct
    private void init() {
        localCache = CacheBuilder.newBuilder()
                .initialCapacity(10)
                .maximumSize(500)
                .expireAfterAccess(60, TimeUnit.SECONDS).build();
    }
    public void setLocalCache(String key,T object){
        localCache.put(key,object);
    }

    /***
     * 返回值 如果不存在返回null
     * @param key
     * @param <T>
     * @return
     */
    public <T> T getCache(String key){
        return (T) localCache.getIfPresent(key);
    }

    public void remove(String key){
        localCache.invalidate(key);
    }

}
