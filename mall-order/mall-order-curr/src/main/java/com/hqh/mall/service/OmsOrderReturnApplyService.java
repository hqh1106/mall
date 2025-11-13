package com.hqh.mall.service;

import com.hqh.mall.dto.OmsOrderReturnApplyResult;
import com.hqh.mall.dto.OmsReturnApplyQueryParam;
import com.hqh.mall.dto.OmsUpdateStatusParam;
import com.hqh.mall.model.OmsOrderReturnApply;

import java.util.List;

/**
 * 退货申请管理
 */
public interface OmsOrderReturnApplyService {
    /**
     * 分页查询申请
     */
    List<OmsOrderReturnApply> list(OmsReturnApplyQueryParam queryParam, Integer pageSize, Integer pageNum);

    /**
     * 批量删除申请
     */
    int delete(List<Long> ids);

    /**
     * 修改申请状态
     */
    int updateStatus(Long id, OmsUpdateStatusParam statusParam);

    /**
     * 获取指定申请详情
     */
    OmsOrderReturnApplyResult getItem(Long id);
}
