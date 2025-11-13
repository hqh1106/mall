package com.hqh.mall.service.impl;

public interface OrderConstant {
     String LEAF_ORDER_ID_KEY = "order_id";
     String LEAF_ORDER_ITEM_ID_KEY = "order_item_id";

    /*订单确认*/
     int CONFIRM_STATUS_NO = 0;
     int CONFIRM_STATUS_YES = 1;

    /*订单删除状态*/
     int DELETE_STATUS_NO = 0;
     int DELETE_STATUS_YES = 1;

    /*订单来源：0->PC订单；1->app订单*/
     int SOURCE_TYPE_PC = 0;
     int SOURCE_TYPE_APP = 1;

    /*订单状态：0->待付款；1->待发货；2->已发货；3->已完成；4->已关闭；5->无效订单*/
     int ORDER_STATUS_UNPAY = 0;
     int ORDER_STATUS_UNDELIVERY = 1;
     int ORDER_STATUS_DELIVERYED = 2;
     int ORDER_STATUS_COMPLETE = 3;
     int ORDER_STATUS_CLOSE = 4;
     int ORDER_STATUS_INVALID = 5;

    /*订单类型：0->正常订单；1->秒杀订单*/
     int ORDER_TYPE_NORMAL = 0;
     int ORDER_TYPE_SECKILL = 1;


    /*支付方式：0->未支付；1->支付宝；2->微信*/
     int ORDER_PAY_TYPE_NO = 0;
     int ORDER_PAY_TYPE_ALIPAY = 1;
     int ORDER_PAY_TYPE_WECHAT = 2;

     String REDIS_CREATE_ORDER = "mall:create_order";
}
