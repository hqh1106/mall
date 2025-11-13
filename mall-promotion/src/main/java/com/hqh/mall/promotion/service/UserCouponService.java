package com.hqh.mall.promotion.service;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.promotion.domain.CartPromotionItem;
import com.hqh.mall.promotion.domain.SmsCouponHistoryDetail;
import com.hqh.mall.promotion.model.SmsCouponHistory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户优惠券管理Service
 */
public interface UserCouponService {
    /**
     * 会员添加优惠券
     */
    @Transactional
    CommonResult activelyGet(Long couponId, Long memberId, String nickName);

    /**
     * 获取优惠券列表
     *
     * @param useStatus 优惠券的使用状态
     */
    List<SmsCouponHistory> listCoupons(Integer useStatus, Long memberId);

    /**
     * 根据购物车信息获取可用优惠券
     */
    List<SmsCouponHistoryDetail> listCart(List<CartPromotionItem> cartItemList, Integer type, Long memberId);
}
