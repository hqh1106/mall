package com.hqh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class MallUnqidApplication {
    public static void main(String[] args) {
        SpringApplication.run(MallUnqidApplication.class,args);
    }
}
