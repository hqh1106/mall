package com.hqh.mall.promotion.service;

import com.hqh.mall.promotion.domain.FlashPromotionProduct;
import com.hqh.mall.promotion.domain.HomeContentResult;

import java.util.List;

/**
 * 首页内容管理Service
 */
public interface HomePromotionService {

    /* 获取首页推荐品牌和产品*/
    HomeContentResult content(int getType);

    /*秒杀产品*/
    List<FlashPromotionProduct> secKillContent(long secKillId, int status);

    int turnOnSecKill(long secKillId,int status);
}
