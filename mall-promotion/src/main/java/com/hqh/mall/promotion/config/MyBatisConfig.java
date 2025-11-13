package com.hqh.mall.promotion.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@MapperScan({"com.hqh.mall.promotion.mapper","com.hqh.mall.promotion.dao"})
public class MyBatisConfig {
}
