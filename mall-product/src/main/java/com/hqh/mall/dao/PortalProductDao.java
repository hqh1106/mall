package com.hqh.mall.dao;


import com.hqh.mall.domain.CartProduct;
import com.hqh.mall.domain.PmsProductParam;
import com.hqh.mall.domain.PromotionProduct;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 前台系统自定义商品Dao
 */
@Repository
public interface PortalProductDao {
    CartProduct getCartProduct(@Param("id") Long id);
    List<PromotionProduct> getPromotionProductList(@Param("ids") List<Long> ids);

    /**
     * add by yangguo
     * 获取商品详情信息
     * @param id 产品ID
     */
    PmsProductParam getProductInfo(@Param("id") Long id);

    /**
     * 查找出所有的产品ID
     * @return
     */
    List<Long> getAllProductId();
}
