package com.hqh.mall.feignapi.ums;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.domain.CartPromotionItem;
import com.hqh.mall.domain.SmsCouponHistoryDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "mall-promotion", path = "/coupon")
public interface UmsCouponFeignApi {
    @RequestMapping(value = "/listCart", method = RequestMethod.POST)
    @ResponseBody
    CommonResult<List<SmsCouponHistoryDetail>> listCartCoupons(@RequestParam("type") Integer type,
                                                               @RequestBody List<CartPromotionItem> cartPromotionItemList);
}
