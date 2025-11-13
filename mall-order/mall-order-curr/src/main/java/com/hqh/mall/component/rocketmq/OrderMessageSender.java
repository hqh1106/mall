package com.hqh.mall.component.rocketmq;


import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderMessageSender {

    @Value("${rocketmq.mall.scheduleTopic}")
    private String scheduleTopic;

    @Value("${rocketmq.mall.transGroup}")
    private String transGroup;

    @Value("${rocketmq.mall.transTopic}")
    private String transTopic;

    @Value("${rocketmq.mall.asyncOrderTopic}")
    private String asyncOrderTopic;

    private String TAG = "cancelOrder";
    private String TXTAG = "trans";
    private String ORDERTAG = "create-order";

    @Autowired
    private RocketMQTemplate rocketMQTemplate;

    /**
     * 使用事务消息机制发送订单
     * @return
     */
    public boolean sendCreateOrderMsg(Long orderId, Long memberId){
        String destination = asyncOrderTopic+":"+ORDERTAG;
        Message<String> message = MessageBuilder.withPayload(orderId+":"+memberId)
                .build();
        TransactionSendResult sendResult = rocketMQTemplate.sendMessageInTransaction(destination,message,orderId);
        return SendStatus.SEND_OK == sendResult.getSendStatus();
    }
}
