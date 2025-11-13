package com.hqh.mall.service;

import com.hqh.mall.common.api.CommonResult;

/**
 * 交易服务
 */
public interface TradeService {
    /**
     * 根据订单生成支付二维码
     * @param orderId
     * @param payType
     * @return
     */
    CommonResult tradeQrCode(Long orderId, Integer payType,Long memberId);

    /**
     * 查询订单支付状态
     * @param orderId
     * @return
     */
    CommonResult tradeStatusQuery(Long orderId, Integer payType);
}
