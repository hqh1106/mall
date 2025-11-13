package com.hqh.mall.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan({"com.hqh.mall.dao","com.hqh.mall.mapper"})
public class MyBatisConfig {
}
