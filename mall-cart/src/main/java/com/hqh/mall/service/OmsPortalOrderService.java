package com.hqh.mall.service;


import com.hqh.mall.common.exception.BusinessException;
import com.hqh.mall.domain.ConfirmOrderResult;

import java.util.List;

/**
 * 前台订单管理Service
 */
public interface OmsPortalOrderService {

    ConfirmOrderResult generateConfirmOrder(List<Long> itemIds, Long memberId) throws BusinessException;

}

