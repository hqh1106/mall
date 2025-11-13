package com.hqh.mall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.ConfigurableApplicationContext;

@EnableDiscoveryClient
@SpringBootApplication
public class ProductApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ProductApplication.class, args);
        System.out.println(">>> 当前主数据源: " + context.getEnvironment().getProperty("spring.datasource.dynamic.primary"));
    }
}
