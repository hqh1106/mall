package com.hqh.mall.component.rocketmq;


import com.hqh.mall.mapper.OmsOrderMapper;
import com.hqh.mall.service.OmsPortalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.support.RocketMQHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;

@Slf4j
@RocketMQTransactionListener(rocketMQTemplateBeanName="extRocketMQTemplate") //一个事物监听器对应一个事物流程
public class ReduceStockMsgListener implements RocketMQLocalTransactionListener {

    @Autowired
    private OmsOrderMapper omsOrderMapper;

    @Autowired
    private OmsPortalOrderService portalOrderService;

    /**
     * 事务消息发送后的回调方法，当消息发送给mq成功，此方法被回调
     */
    @Override
    public RocketMQLocalTransactionState executeLocalTransaction(Message message, Object arg) {
        
        try {
            //解析message
            Long orderId = Long.parseLong(String.valueOf(arg));
            String transactionId = (String) message.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);
            Integer payType = Integer.valueOf((String)message.getHeaders().get("payType"));

            //修改订单状态
            portalOrderService.updateOrderStatus(orderId,payType,transactionId);

            //当返回RocketMQLocalTransactionState.COMMIT，自动向mq发送commit消息，mq将消息的状态改为可消费
            return RocketMQLocalTransactionState.COMMIT;
        } catch (Exception e) {
            e.printStackTrace();
            return RocketMQLocalTransactionState.ROLLBACK;
        }
        
        
    }
    
    /**
     * 事务状态回查
     * @param message
     * @return
     */
    @Override
    public RocketMQLocalTransactionState checkLocalTransaction(Message message) {

        String transactionId = (String) message.getHeaders().get(RocketMQHeaders.TRANSACTION_ID);
        int existTx = omsOrderMapper.isExistTx(transactionId);
        if (existTx > 0) {
            return RocketMQLocalTransactionState.COMMIT;
        } else {
            return RocketMQLocalTransactionState.UNKNOWN;
        }
    }
}
