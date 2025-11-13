package com.hqh.mall.promotion.service.impl;

public interface ConstantPromotion {
    /*优惠券类型；0->全场赠券；1->会员赠券；2->购物赠券；3->注册赠券*/
     int COUPON_TYPE_GENERAL = 0;
     int COUPON_TYPE_USER = 0;
     int COUPON_TYPE_SHOPPING = 0;
     int COUPON_TYPE_REGISTER = 0;

    /*优惠券使用平台：0->全部；1->移动；2->PC*/
     int COUPON_PLATFORM_ALL = 0;
     int COUPON_PLATFORM_PC = 2;
     int COUPON_PLATFORM_MOBIL = 1;

    /*优惠券使用类型：0->全场通用；1->指定分类；2->指定商品'*/
     int COUPON_USE_TYPE_GENERAL = 0;
     int COUPON_USE_TYPE_SPEC_KIND = 1;
     int COUPON_USE_TYPE_SPEC_PRODUCT = 2;

    /*用户优惠券获取方式：0->后台赠送；1->主动获取'*/
     int USER_COUPON_GET_TYPE_GIFT = 0;
     int USER_COUPON_GET_TYPE_PROACTIVE = 1;

    /*用户优惠券使用状态：0->未使用；1->已使用；2->已过期'*/
     int USER_COUPON_USE_STATE_UNUSE = 0;
     int USER_COUPON_USE_STATE_USED = 1;
     int USER_COUPON_USE_STATE_OVERDUE = 2;

    /*首页推荐状态：0->不推荐;1->推荐*/
     int HOME_PRODUCT_RECOMMEND_NO = 0;
     int HOME_PRODUCT_RECOMMEND_YES = 1;

    /*首页轮播广告轮播位置：0->PC首页轮播；1->app首页轮播*/
     int HOME_ADVERTISE_TYPE_PC = 0;
     int HOME_ADVERTISE_TYPE_APP = 1;

    /*首页轮播广告上下线状态：0->下线；1->上线*/
     int HOME_ADVERTISE_STATUS_OFFLINE = 0;
     int HOME_ADVERTISE_STATUS_ONLINE = 1;

    /*首页推荐分页大小*/
     int HOME_RECOMMEND_PAGESIZE = 4;

    /*获取推荐内容类型:0->全部；1->品牌；2->新品推荐；3->人气推荐;4->轮播广告*/
     int HOME_GET_TYPE_ALL = 0;
     int HOME_GET_TYPE_BARND = 1;
     int HOME_GET_TYPE_NEW = 2;
     int HOME_GET_TYPE_HOT = 3;
     int HOME_GET_TYPE_AD = 4;

    /*秒杀状态：0->下线；1->上线*/
     int SECKILL_CLOSE = 0;
     int SECKILL_OPEN = 1;

    /*秒杀静态页处理结果：0->成功；-1->失败*/
     int STATIC_HTML_SUCCESS = 0;
     int STATIC_HTML_FAILURE = -1;
}
