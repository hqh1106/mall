package com.hqh.mall.dao;

import com.hqh.mall.model.OmsOrderItem;
import org.apache.ibatis.annotations.Param;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 订单商品信息
 */
@Mapper
public interface PortalOrderItemDao {
    int insertList(@Param("list") List<OmsOrderItem> list);
}
