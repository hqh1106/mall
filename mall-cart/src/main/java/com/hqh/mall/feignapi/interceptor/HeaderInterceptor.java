package com.hqh.mall.feignapi.interceptor;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

@Slf4j
public class HeaderInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes !=null){
            HttpServletRequest request = requestAttributes.getRequest();
            log.info("从Request中解析请求头:{}",request.getHeader("memberId"));
            requestTemplate.header("memberId",request.getHeader("memberId"));
        }
    }
}
