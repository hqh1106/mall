package com.hqh.mall.promotion.clientApi;

import com.hqh.mall.model.PmsBrand;
import com.hqh.mall.model.PmsProduct;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * Feign远程调用商品服务接口
 */
@FeignClient(value = "mall-product",path = "pms")
public interface PmsProductClientApi {
    @RequestMapping(value = "/getRecommandBrandList", method = RequestMethod.POST)
    @ResponseBody
    List<PmsBrand> getRecommandBrandList(@RequestParam(value="brandIdList") List<Long> brandIdList);

    @RequestMapping(value = "/getProductBatch", method = RequestMethod.POST)
    @ResponseBody
    List<PmsProduct> getProductBatch(@RequestParam(value="productIdList") List<Long> productIdList);
}
