package com.hqh.mall.feignapi.promotion;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.domain.CartPromotionItem;
import com.hqh.mall.domain.SmsCouponHistoryDetail;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(value = "mall-promotion", path = "/coupon")
public interface PromotionFeignApi {
    /*"type", value = "使用可用:0->不可用；1->可用"*/
    @RequestMapping(value = "/listCart", method = RequestMethod.POST)
    @ResponseBody
    public CommonResult<List<SmsCouponHistoryDetail>> listCartCoupons(@RequestParam(value="type") Integer type,
                                                                      @RequestBody List<CartPromotionItem> cartPromotionItemList);
}

