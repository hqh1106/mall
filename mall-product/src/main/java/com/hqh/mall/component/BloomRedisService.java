package com.hqh.mall.component;


import com.hqh.mall.utils.BloomFilterHelper;
import org.apache.curator.shaded.com.google.common.base.Preconditions;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * 布隆过滤器，根据值进行多次hash，判断对应位置是否为true
 */
public class BloomRedisService {
    private RedisTemplate<String, Object> redisTemplate;

    private BloomFilterHelper bloomFilterHelper;

    public void setBloomFilterHelper(BloomFilterHelper bloomFilterHelper) {
        this.bloomFilterHelper = bloomFilterHelper;
    }

    public void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public <T> void addByBloomFilter(String key, T value) {
        Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper不能为空");
        int[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (int i = 0; i < offset.length; i++) {
            redisTemplate.opsForValue().setBit(key, offset[i], true);
        }
    }

    public <T> boolean includeByBloomFilter(String key, T value) {
        com.google.common.base.Preconditions.checkArgument(bloomFilterHelper != null, "bloomFilterHelper不能为空");
        int[] offset = bloomFilterHelper.murmurHashOffset(value);
        for (int i : offset) {
            if (!redisTemplate.opsForValue().getBit(key, i)) {
                return false;
            }
        }
        return true;
    }
}
