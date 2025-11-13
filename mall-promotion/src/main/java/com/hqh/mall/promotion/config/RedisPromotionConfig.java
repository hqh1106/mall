package com.hqh.mall.promotion.config;

import com.hqh.mall.promotion.utils.RedisDistrLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisPromotionConfig {
    @Bean
    public RedisDistrLock redisDistrLock(){
        return new RedisDistrLock();
    }
}
