package com.hqh.mall.component;

import com.hqh.mall.domain.MqCancelOrder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CancelOrderSender implements InitializingBean {

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    public void sendMessage(MqCancelOrder mqCancelOrder, final long delayTimes) {
        rocketMQTemplate.syncSend("mall.order.cancel.ttl",
                MessageBuilder.withPayload(mqCancelOrder).build(), 5000, 16);
        log.info("send orderId:{}", mqCancelOrder.getOrderId());
    }

    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
