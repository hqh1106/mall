package com.hqh.mall.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashSet;

@Data
@ConfigurationProperties("hqh.gateway")
public class NotAuthUrlProperties {
    private LinkedHashSet<String> shouldSkipUrls;
}
