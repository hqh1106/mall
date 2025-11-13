package com.hqh.mall.service;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.common.exception.BusinessException;
import com.hqh.mall.domain.PmsProductParam;

public interface SecKillOrderService {

    /**
     * 秒杀订单确认
     * @param productId
     * @param memberId
     * @return
     */
    CommonResult generateConfirmMiaoShaOrder(Long productId
            , Long memberId, String token) throws BusinessException;

//    /**
//     * 秒杀订单下单
//     * @param orderParam
//     * @param memberId
//     * @return
//     */
//    CommonResult generateSecKillOrder(OrderParam orderParam, Long memberId,String token) throws BusinessException;

    /**
     * 还原redis库存,每次加1
     * @param productId
     */
    void incrRedisStock(Long productId);

    /**
     * 判断是否应该pub消息清除集群服务本地的售罄标识
     * @param productId
     * @return
     */
    boolean shouldPublishCleanMsg(Long productId);
//
//    /**
//     * 异步下单
//     * @param order
//     * @param orderItem
//     * @param flashPromotionRelationId
//     * @return
//     */
//    Long asyncCreateOrder(OmsOrder order, OmsOrderItem orderItem, Long flashPromotionRelationId);

    /**
     * 获取产品信息
     * @param productId
     * @return
     */
    PmsProductParam getProductInfo(Long productId);

}
