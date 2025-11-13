package com.hqh.mall.component;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.service.OmsPortalOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时取消并解锁库存
 */
@Component
public class OrderTimeOutCancelTask {

    private Logger logger = LoggerFactory.getLogger(OrderTimeOutCancelTask.class);
    @Autowired
    private OmsPortalOrderService portalOrderService;

    /**
     * 每10分钟扫描一次，扫描设定超时时间之前下的订单，如果没支付则取消该订单
     */
    @Scheduled(cron = "0 0/10 * ? * ?")
    private void cancelTimeOutOrder(){
        CommonResult result = portalOrderService.cancelTimeOutOrder();
        logger.info("取消订单，并根据sku编号释放锁定库存:{}",result);
    }
}
