package com.hqh.mall.config;

import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RibbonConfig {
    /**
     * 手动注入loadBalancerInterceptor拦截器，实现负载均衡功能
     * @param loadBalancerInterceptor
     * @return
     */
    @Bean
    public RestTemplate restTemplate(LoadBalancerInterceptor loadBalancerInterceptor){
        RestTemplate restTemplate = new RestTemplate();
        List<ClientHttpRequestInterceptor> list = new ArrayList();
        list.add(loadBalancerInterceptor);
        restTemplate.setInterceptors(list);
        return restTemplate;
    }
}
