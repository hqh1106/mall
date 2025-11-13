package com.hqh.mall.feignapi.ums;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.model.PortalMemberInfo;
import com.hqh.mall.model.UmsMember;
import com.hqh.mall.model.UmsMemberReceiveAddress;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mall-member",path = "/member")
public interface UmsMemberFeignApi {
    @RequestMapping(value = "/address/{id}", method = RequestMethod.GET)
    @ResponseBody
    CommonResult<UmsMemberReceiveAddress> getItem(@PathVariable(value = "id") Long id);

    @RequestMapping(value = "/center/updateUmsMember",method = RequestMethod.POST)
    CommonResult<String> updateUmsMember(@RequestBody UmsMember umsMember);


    @RequestMapping(value = "/center/getMemberInfo", method = RequestMethod.GET)
    @ResponseBody
    CommonResult<PortalMemberInfo> getMemberById();

    @RequestMapping(value = "/address/list", method = RequestMethod.GET)
    @ResponseBody
    CommonResult<List<UmsMemberReceiveAddress>> list();
}
