package com.hqh.mall.config;

import com.hqh.mall.component.TradePayProp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceConfig implements WebMvcConfigurer {

    @Autowired
    private TradePayProp tradePayProp;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        /** QrCode图片存储路径 */
        registry.addResourceHandler(tradePayProp.getHttpBasePath()
                        + "/**")
                .addResourceLocations("file:" + tradePayProp.getStorePath() + "/");
    }
}
