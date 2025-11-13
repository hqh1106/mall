package com.hqh.mall.promotion.clientApi.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * Feign调用添加请求头
 */
@Slf4j
public class HeaderInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(null != attributes){
            HttpServletRequest request = attributes.getRequest();
            log.info("从Request中解析请求头");
            requestTemplate.header("memberId",request.getHeader("memberId"));
        }
    }
}
