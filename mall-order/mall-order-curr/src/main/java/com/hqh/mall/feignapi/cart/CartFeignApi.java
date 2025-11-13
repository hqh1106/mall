package com.hqh.mall.feignapi.cart;

import com.hqh.mall.domain.CartPromotionItem;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

/**
 * 购物车远程调用
 */
@FeignClient(name = "mall-cart",path = "cart")
public interface CartFeignApi {
    @RequestMapping(value = "/list/selectedpromotion", method = RequestMethod.POST)
    public List<CartPromotionItem> listSelectedPromotion(@RequestBody List<Long> itemIds);
}
