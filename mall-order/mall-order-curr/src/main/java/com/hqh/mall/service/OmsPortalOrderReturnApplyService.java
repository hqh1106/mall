package com.hqh.mall.service;

import com.hqh.mall.domain.OmsOrderReturnApplyParam;

/**
 * 订单退货管理
 */
public interface OmsPortalOrderReturnApplyService {
    /**
     * 提交申请
     */
    int create(OmsOrderReturnApplyParam returnApply);
}
