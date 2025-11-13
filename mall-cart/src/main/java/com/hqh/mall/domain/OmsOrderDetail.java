package com.hqh.mall.domain;


import com.hqh.mall.model.OmsOrder;
import com.hqh.mall.model.OmsOrderItem;

import java.util.List;

public class OmsOrderDetail extends OmsOrder {
    private List<OmsOrderItem> orderItemList;

    public List<OmsOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OmsOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
