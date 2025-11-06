package com.hqh.mall.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@Configuration
@MapperScan({"com.hqh.mall.mapper","com.hqh.mall.dao"})
public class MybatisConfig {
}
