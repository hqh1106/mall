package com.hqh.mall.promotion.clientApi.interceptor.config;

import com.hqh.mall.promotion.clientApi.interceptor.HeaderInterceptor;
import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class FeignConfig {
    @Bean
    public Logger.Level level(){
        return Logger.Level.FULL;
    }
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new HeaderInterceptor();
    }
}
