//package com.hqh.mall.config;
//
//import org.apache.shardingsphere.transaction.ShardingTransactionManagerEngine;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Configuration;
//
//import javax.annotation.PostConstruct;
//import javax.sql.DataSource;
//import java.util.Collections;
//
//@Configuration
//public class ShardingXaTransactionConfig {
//
//    @Autowired
//    private DataSource dataSource;
//
//    @PostConstruct
//    public void init() {
//        ShardingTransactionManagerEngine engine = new ShardingTransactionManagerEngine();
//        engine.init(DatabaseTypeFactory.getInstance("MySQL"),
//                Collections.singletonMap("ds", dataSource));
//    }
//}
