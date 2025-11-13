package com.hqh.mall.config;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.net.InetSocketAddress;

@Configuration
@EnableScheduling
@EnableAsync
public class CanalOrderConfig {
    @Value("${canal.server.ip}")
    private String canalServerIp;

    @Value("${canal.server.port}")
    private int canalServerPort;

    @Value("${canal.server.username:canal}")
    private String userName;

    @Value("${canal.server.password:canal}")
    private String password;

    @Value("${canal.order.destination:order}")
    private String destination;

    @Bean("orderConnector")
    public CanalConnector newSingleConnector() {
        String userNameStr = "blank".equals(userName) ? "" : userName;
        String passwordStr = "blank".equals(password) ? "" : password;
        return CanalConnectors.newSingleConnector(
                new InetSocketAddress(canalServerIp, canalServerPort), destination, userNameStr, passwordStr
        );
    }
}
