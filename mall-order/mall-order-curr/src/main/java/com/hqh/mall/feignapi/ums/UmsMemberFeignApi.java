package com.hqh.mall.feignapi.ums;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.model.UmsMemberReceiveAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 远程调用 会员中心获取具体收获地址
 */
@FeignClient(name = "mall-member",path = "/member")
public interface UmsMemberFeignApi {
    @RequestMapping(value = "/address/{id}", method = RequestMethod.GET)
    @ResponseBody
    CommonResult<UmsMemberReceiveAddress> getItem(@PathVariable(value = "id") Long id);
}
