package com.hqh.mall.feignapi.unqid;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 调用分布式ID生成服务
 */
@FeignClient(name = "mall-unqid")
public interface UnqidFeignApi {
    @RequestMapping(value = "/api/segment/get/{key}")
    public String getSegmentId(@PathVariable("key") String key) ;

    @RequestMapping(value = "/api/segment/getlist/{key}")
    public List<String> getSegmentIdList(@PathVariable("key") String key, @RequestParam("keyNumber") int keyNumber) ;

}
