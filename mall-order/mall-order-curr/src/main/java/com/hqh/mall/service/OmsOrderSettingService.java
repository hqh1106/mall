package com.hqh.mall.service;

import com.hqh.mall.model.OmsOrderSetting;

/**
 * 订单设置
 */
public interface OmsOrderSettingService {
    /**
     * 获取指定订单设置
     */
    OmsOrderSetting getItem(Long id);

    /**
     * 修改指定订单设置
     */
    int update(Long id, OmsOrderSetting orderSetting);
}
