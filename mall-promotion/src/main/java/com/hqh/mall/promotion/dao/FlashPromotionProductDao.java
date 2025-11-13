package com.hqh.mall.promotion.dao;

import com.hqh.mall.promotion.domain.FlashPromotionParam;
import org.apache.ibatis.annotations.Param;

/**
 * 首页秒杀活动首页管理
 */
public interface FlashPromotionProductDao {
    /**
     * 查找所有的秒杀活动商品
     * @return
     */
    FlashPromotionParam getFlashPromotion(@Param("spid") Long spid, @Param("status") Integer status);
}
