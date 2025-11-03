package com.hqh.mall.config;

import org.springframework.context.annotation.Configuration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
@Configuration
@EnableTransactionManagement
@MapperScan({"com.hqh.mall.mapper"})
public class MybatisConfig {
}
