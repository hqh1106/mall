package com.hqh.mall.promotion.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class RedisDistrLock {

    private final static String UNLOCK_LUA_NAME = "Redis分布式锁解锁脚本";

    private final static String UNLOCK_LUA = "local result = redis.call('get',KEYS[1]);" +
            "if result == ARGV[1] then redis.call('del',KEYS[1]) " +
            "return 1 else return nil end";
    //当前线程的锁集合，处理锁的可重入
    private ThreadLocal<Map<String, Integer>> lockers = new ThreadLocal<>();
    //当前线程锁的key和value集合
    private ThreadLocal<Map<String, String>> values = new ThreadLocal<>();

    @Autowired
    private RedisTemplate redisTemplate;

    private AtomicBoolean isLoadScript = new AtomicBoolean(false);
    private DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();

    @PostConstruct
    private void loadScript() {
        if (isLoadScript.get()) return;
        redisScript.setScriptText(UNLOCK_LUA);
        redisScript.setResultType(Long.class);
        loadRedisScript(redisScript, UNLOCK_LUA_NAME);
        isLoadScript.set(true);
    }

    /**
     * 加载Lua脚本到redis服务器
     */
    private void loadRedisScript(DefaultRedisScript<Long> redisScript, String luaName) {
        try {
            List<Boolean> results = redisTemplate.getConnectionFactory().getConnection().scriptExists(redisScript.getSha1());
            if (Boolean.FALSE.equals(results.get(0))) {
                String sha = redisTemplate.getConnectionFactory().getConnection()
                        .scriptLoad(redisScript.getScriptAsString().getBytes(StandardCharsets.UTF_8));
                log.info("预加载lua脚本成功：{}, sha=[{}]", luaName, sha);
            }

        } catch (Exception e) {
            log.error("预加载lua脚本异常：{}", luaName, e);
        }
    }

    /**
     * 尝试获取锁
     */
    private Boolean tryLock(String key, long timeout) {
        if (timeout <= 0) timeout = 5000;
        String value = getValueByKey(key);
        if (value == null) {
            value = UUID.randomUUID().toString();
            values.get().put(key, value);
        }
        if (redisTemplate.opsForValue().setIfAbsent(key, value, Duration.ofSeconds(timeout))){
            return true;
        }
        boolean isLock;
        long endTime = System.currentTimeMillis() + timeout;
        while (true){
            try{
                Thread.sleep(5);
                isLock = this.tryLock(key,timeout);
                if (isLock){
                    return true;
                }
                if (endTime < System.currentTimeMillis()){
                    return false;
                }
            }catch (InterruptedException e){
                log.debug("拿锁的休眠等待被中断！");
            }
        }
    }

    /**
     * 尝试释放锁
     * @param key
     * @return
     */
    private boolean tryRelease(String key){
        String[] keys = new String[]{key};
        String[] args = new String[]{getValueByKey(key)};

        Long result = (Long) redisTemplate.execute(redisScript, Collections.singletonList(key),
                getValueByKey(key));
        return result !=null;
    }

    /**
     * 获取当前线程的锁
     * @param key
     * @return
     */
    private Integer getLockerCnt(String key){
        Map<String, Integer> map = lockers.get();
        if (map!=null){
            return map.get(key);
        }
        lockers.set(new HashMap<>(4));
        return null;
    }

    private String getValueByKey(String key) {
        // 获取当前线程的锁和对应值的键值对集合
        Map<String, String> map = values.get();
        // 如果集合不为空，返回key对应的值
        if (map != null) {
            return map.get(key);
        }
        values.set(new HashMap<>(4));
        return null;
    }
    public boolean lock(String key, long timeout){
        Integer refCnt = getLockerCnt(key);
        if (refCnt != null){
            lockers.get().put(key,refCnt+1);
            return true;
        }

        boolean ok = this.tryLock(key, timeout);

        if (!ok){
            return false;
        }
        lockers.get().put(key,1);
        return true;
    }

    /**
     * 释放可重入锁
     * @param key
     * @return
     */
    public boolean unlock(String key){
        Integer refCnt = getLockerCnt(key);
        if (refCnt ==null){
            return false;
        }
        refCnt--;
        if (refCnt> 0){
            lockers.get().put(key,refCnt);
        }else {
            lockers.get().remove(key);
            return this.tryRelease(key);
        }
        return true;
    }
}
