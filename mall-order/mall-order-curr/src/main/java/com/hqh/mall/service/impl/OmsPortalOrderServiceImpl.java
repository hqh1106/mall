package com.hqh.mall.service.impl;

import com.github.pagehelper.PageHelper;
import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.common.api.ResultCode;
import com.hqh.mall.component.CancelOrderSender;
import com.hqh.mall.component.rocketmq.ReduceStockMsgSender;
import com.hqh.mall.dao.PortalOrderDao;
import com.hqh.mall.dao.PortalOrderItemDao;
import com.hqh.mall.domain.*;
import com.hqh.mall.feignapi.cart.CartFeignApi;
import com.hqh.mall.feignapi.pms.PmsProductStockFeignApi;
import com.hqh.mall.feignapi.promotion.PromotionFeignApi;
import com.hqh.mall.feignapi.ums.UmsMemberFeignApi;
import com.hqh.mall.feignapi.unqid.UnqidFeignApi;
import com.hqh.mall.mapper.OmsOrderItemMapper;
import com.hqh.mall.mapper.OmsOrderMapper;
import com.hqh.mall.mapper.OmsOrderSettingMapper;
import com.hqh.mall.model.*;
import com.hqh.mall.service.OmsPortalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.transaction.annotation.ShardingTransactionType;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 前台订单管理
 */
@Service
@Slf4j
public class OmsPortalOrderServiceImpl implements OmsPortalOrderService {
    @Autowired
    private UnqidFeignApi unqidFeignApi;

    @Autowired
    private UmsMemberFeignApi umsMemberFeignApi;

    @Autowired
    private ReduceStockMsgSender reduceStockMsgSender;
    @Autowired
    private PromotionFeignApi promotionFeignApi;

    @Autowired
    private PmsProductStockFeignApi pmsProductStockFeignApi;

    @Autowired
    private PortalOrderDao portalOrderDao;
    @Autowired
    private OmsOrderSettingMapper orderSettingMapper;
    @Autowired
    private OmsOrderItemMapper orderItemMapper;
    @Autowired
    private CancelOrderSender cancelOrderSender;
    @Autowired
    private CartFeignApi cartFeignApi;

    @Autowired
    private OmsOrderMapper omsOrderMapper;
    @Autowired
    private PortalOrderItemDao orderItemDao;

    public Long generateOrderId(Long memberId) {
        String leafOrderId = unqidFeignApi.getSegmentId(OrderConstant.LEAF_ORDER_ID_KEY);
        String strMemberId = memberId.toString();
        String OrderIdTail = memberId < 10 ? "0" + strMemberId
                : strMemberId.substring(strMemberId.length() - 2);
        log.debug("生成订单的orderId，组成元素为：{},{}", leafOrderId, OrderIdTail);
        return Long.valueOf(leafOrderId + OrderIdTail);
    }

    @Override
    @Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
    @ShardingTransactionType(TransactionType.BASE)
    public CommonResult generateOrder(OrderParam orderParam, Long memberId) {
        log.debug("接受参数OrderParam：{} memberId：{}", orderParam, memberId);
        if (null == orderParam || null == memberId) {
            return CommonResult.failed(ResultCode.VALIDATE_FAILED, "参数不能为空！");
        }
        Long orderId = orderParam.getOrderId();
        if (null == orderId) {
            orderId = generateOrderId(memberId);
            log.debug("前端页面未传递orderId，临时生成：{}", orderId);
        } else {
            log.debug("前端页面传递orderId[{}]", orderId);
        }
        /*这里我们对OrderSn简单处理，在实际业务时可以根据情况做变化，比如添加前缀或可逆加密，
        只要保证可以从OrderSn中解析出orderId即可*/
        String orderSn = orderId.toString();

        List<OmsOrderItem> orderItemList = new ArrayList<>();

        List<CartPromotionItem> cartPromotionItemList = cartFeignApi.listSelectedPromotion(orderParam.getItemIds());
        int itemSize = cartPromotionItemList.size();

        /*一次获取多个OrderItem的id，但是可能获取的数量少于订单详情数*/
        List<String> omsOrderItemIDList = unqidFeignApi.getSegmentIdList(OrderConstant.LEAF_ORDER_ITEM_ID_KEY,
                itemSize);
        log.debug("获得订单详情的ID：{}，需求{}个，实际{}个", omsOrderItemIDList, itemSize, omsOrderItemIDList.size());
        int itemListIndex = 0;
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            //生成下单商品信息
            OmsOrderItem orderItem = new OmsOrderItem();
            orderItem.setProductId(cartPromotionItem.getProductId());
            orderItem.setProductName(cartPromotionItem.getProductName());
            orderItem.setProductPic(cartPromotionItem.getProductPic());
            orderItem.setProductAttr(cartPromotionItem.getProductAttr());
            orderItem.setProductBrand(cartPromotionItem.getProductBrand());
            orderItem.setProductSn(cartPromotionItem.getProductSn());
            orderItem.setProductPrice(cartPromotionItem.getPrice());
            orderItem.setProductQuantity(cartPromotionItem.getQuantity());
            orderItem.setProductSkuId(cartPromotionItem.getProductSkuId());
            orderItem.setProductSkuCode(cartPromotionItem.getProductSkuCode());
            orderItem.setProductCategoryId(cartPromotionItem.getProductCategoryId());
            orderItem.setPromotionAmount(cartPromotionItem.getReduceAmount());
            orderItem.setPromotionName(cartPromotionItem.getPromotionMessage());
            orderItem.setGiftIntegration(cartPromotionItem.getIntegration());
            orderItem.setGiftGrowth(cartPromotionItem.getGrowth());
            orderItem.setOrderId(orderId);
            orderItem.setOrderSn(orderSn);
            if (itemListIndex < itemSize) {
                orderItem.setId(Long.valueOf(omsOrderItemIDList.get(itemListIndex)));
            } else {
                log.warn("从分布式ID服务获得的id已经用完，可能订单详情太多或分布式ID服务出错，请检查！" +
                        "正尝试每个订单详情单独获得id");
                orderItem.setId(Long.valueOf(unqidFeignApi.getSegmentId(OrderConstant.LEAF_ORDER_ITEM_ID_KEY)));
            }
            //判断是否使用了优惠券
            if (orderParam.getCouponId() == null) {
                //不用优惠券
                for (OmsOrderItem orderItemTemp : orderItemList) {
                    orderItemTemp.setCouponAmount(new BigDecimal(0));
                }
            } else {
                //使用优惠券
                SmsCouponHistoryDetail couponHistoryDetail = getUseCoupon(cartPromotionItemList, orderParam.getCouponId());
                if (couponHistoryDetail == null) {
                    return CommonResult.failed("该优惠券不可用");
                }
                //对下单商品的优惠券进行处理
                handleCouponAmount(orderItemList, couponHistoryDetail);
            }
            orderItem.setCouponAmount(new BigDecimal(0));
            orderItem.setIntegrationAmount(new BigDecimal(0));
            orderItemList.add(orderItem);
            itemListIndex++;
        }

        //计算order_item的实付金额
        handleRealAmount(orderItemList);
        // 分布式事务 进行库存锁定
        CommonResult lockResult = pmsProductStockFeignApi.lockStock(cartPromotionItemList);
        if (lockResult.getCode() == ResultCode.FAILED.getCode()) {
            log.warn("远程调用锁定库存失败");
            throw new RuntimeException("远程调用锁定库存失败");
        }
        //模拟异常
//        int c = 0;
//        if (c==0){
//            c = 1/0;
//        }
        OmsOrder order = new OmsOrder();
        order.setId(orderId);
        order.setDiscountAmount(new BigDecimal(0));
        order.setTotalAmount(calcTotalAmount(orderItemList));
        order.setFreightAmount(new BigDecimal(0));
        order.setPromotionAmount(new BigDecimal(0));
        order.setPromotionInfo("无优惠");
        order.setCouponAmount(new BigDecimal(0));
        order.setIntegration(0);
        order.setIntegrationAmount(new BigDecimal(0));

        order.setPayAmount(calcPayAmount(order));
        //转化为订单信息并插入数据库
        order.setMemberId(memberId);
        order.setCreateTime(new Date());
        order.setMemberUsername(null);
        //支付方式：0->未支付；1->支付宝；2->微信
        order.setPayType(orderParam.getPayType());
        order.setSourceType(OrderConstant.SOURCE_TYPE_APP);
        order.setStatus(OrderConstant.ORDER_STATUS_UNPAY);
        order.setOrderType(OrderConstant.ORDER_TYPE_NORMAL);
        //收货人信息：姓名、电话、邮编、地址
        /* TODO 通过Feign远程调用 会员服务*/
        UmsMemberReceiveAddress address = umsMemberFeignApi.getItem(orderParam.getMemberReceiveAddressId()).getData();
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverCity(address.getCity());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverDetailAddress(address.getDetailAddress());
        order.setConfirmStatus(OrderConstant.CONFIRM_STATUS_NO);
        order.setDeleteStatus(OrderConstant.DELETE_STATUS_NO);
        //计算赠送积分
        order.setIntegration(0);
        //计算赠送成长值
        order.setGrowth(0);
        order.setOrderSn(orderSn);
        //插入order表和order_item表
        omsOrderMapper.insert(order);
        orderItemDao.insertList(orderItemList);

        //TODO 分布式事务 删除购物车中的下单商品
//        deleteCartItemList(cartPromotionItemList, memberId);
        Map<String, Object> result = new HashMap<>();
        result.put("order", order);
        result.put("orderItemList", orderItemList);
        return CommonResult.success(result, "下单成功");
    }

    @Override
    public CommonResult getDetailOrder(Long orderId) {
        return CommonResult.success(portalOrderDao.getDetail(orderId));
    }

//    @Override
//    public Integer paySuccess(Long orderId, Integer payType) {
//        OmsOrderDetail orderDetail = portalOrderDao.getDetail(orderId);
//        //订单已经超时关闭了，这时再支付就没用了。
//        if (orderDetail.getStatus().equals(5)) {
//            log.warn("订单" + orderDetail.getOrderSn() + "已经关闭，无法正常支付。请发起支付宝订单退款接口。");
//            //TODO 发起支付宝订单退款
//            return -1;
//        }//        //修改订单支付状态
//        OmsOrder order = new OmsOrder();
//        order.setId(orderId);
//        order.setStatus(OrderConstant.ORDER_STATUS_UNDELIVERY);
//        order.setPayType(payType);
//        order.setPaymentTime(new Date());
//        omsOrderMapper.updateByPrimaryKeySelective(order);
//
//        List<StockChanges> stockChangesList = new ArrayList<>();
//        for (OmsOrderItem omsOrderItem : orderDetail.getOrderItemList()) {
//            stockChangesList.add(new StockChanges(omsOrderItem.getProductSkuId(), omsOrderItem.getProductQuantity()));
//        }
//        /*实际进行真实库存的扣减*/
//        // 分布式事务
//        // PO :可以使用MQ进行异步扣减
//        CommonResult lockResult = pmsProductStockFeignApi.reduceStock(stockChangesList);
//        if (lockResult.getCode() == ResultCode.FAILED.getCode()) {
//            log.warn("远程调用真实库存的扣减失败");
//            return -1;
//            //throw new RuntimeException("远程调用真实库存的扣减失败");
//        } else {
//            log.debug("远程调用真实库存的扣减成功");
//            return (Integer) lockResult.getData();
//        }
@Override
public Integer paySuccess(Long orderId,Integer payType) {
    OmsOrderDetail orderDetail = portalOrderDao.getDetail(orderId);
    //订单已经超时关闭了，这时再支付就没用了。
    if(orderDetail.getStatus().equals(5)){
        log.warn("订单"+orderDetail.getOrderSn()+"已经关闭，无法正常支付。请发起支付宝订单退款接口。");
        //TODO 发起支付宝订单退款
        return -1;
    }

    /*实际进行真实库存的扣减*/
    // todo 分布式事务
    // PO :可以使用MQ进行异步扣减
    // 使用事务消息机制发送扣减库存消息
    return reduceStockMsgSender.sendReduceStockMsg(orderId,payType,orderDetail)? 1 : -1;
}

    @Override
    public CommonResult cancelTimeOutOrder() {
        OmsOrderSetting orderSetting = orderSettingMapper.selectByPrimaryKey(1L);
        //查询超时、未支付的订单及订单详情
        List<OmsOrderDetail> timeOutOrders = portalOrderDao.getTimeOutOrders(orderSetting.getNormalOrderOvertime());
        if (CollectionUtils.isEmpty(timeOutOrders)) {
            return CommonResult.failed("暂无超时订单");
        }
        //修改订单状态为交易取消
        List<Long> ids = new ArrayList<>();
        List<StockChanges> stockChangesList = new ArrayList<>();
        for (OmsOrderDetail timeOutOrder : timeOutOrders) {
            ids.add(timeOutOrder.getId());
            if (CollectionUtils.isEmpty(timeOutOrder.getOrderItemList())) {
                log.warn("订单{}没有下没有商品详情，请检查该订单！", timeOutOrder.getId());
            }
            //解除订单商品库存锁定
            for (OmsOrderItem omsOrderItem : timeOutOrder.getOrderItemList()) {
                stockChangesList.add(new StockChanges(omsOrderItem.getProductSkuId(), omsOrderItem.getProductQuantity()));
            }
        }
        portalOrderDao.updateOrderStatus(ids, OrderConstant.ORDER_STATUS_CLOSE);
        if (!CollectionUtils.isEmpty(stockChangesList)) {
            pmsProductStockFeignApi.recoverStock(stockChangesList);
        }

//        for (OmsOrderDetail timeOutOrder : timeOutOrders) {
//            //修改优惠券使用状态
//            //updateCouponStatus(timeOutOrder.getCouponId(), timeOutOrder.getMemberId(), 0);
//        }
        return CommonResult.success(null);
    }

    @Override
    public void cancelOrder(Long orderId, Long memberId) {
        //查询为付款的取消订单
        OmsOrderExample example = new OmsOrderExample();
        example.createCriteria().andIdEqualTo(orderId).andStatusEqualTo(0).andDeleteStatusEqualTo(0);
        List<OmsOrder> cancelOrderList = omsOrderMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(cancelOrderList)) {
            return;
        }
        OmsOrder cancelOrder = cancelOrderList.get(0);
        if (cancelOrder != null) {
            //修改订单状态为取消
            cancelOrder.setStatus(OrderConstant.ORDER_STATUS_CLOSE);
            /*MemberId为分片键，不能修改*/
            cancelOrder.setMemberId(null);
            omsOrderMapper.updateByPrimaryKeySelective(cancelOrder);
            OmsOrderItemExample orderItemExample = new OmsOrderItemExample();
            orderItemExample.createCriteria().andOrderIdEqualTo(orderId);
            List<OmsOrderItem> orderItemList = orderItemMapper.selectByExample(orderItemExample);
            List<StockChanges> stockChangesList = new ArrayList<>();
            for (OmsOrderItem omsOrderItem : orderItemList) {
                stockChangesList.add(new StockChanges(omsOrderItem.getProductSkuId(), omsOrderItem.getProductQuantity()));
            }
            //解除订单商品库存锁定
            if (!CollectionUtils.isEmpty(stockChangesList)) {
                pmsProductStockFeignApi.recoverStock(stockChangesList);
            }
        }
    }

    /**
     * 删除订单[逻辑删除],只能status为：3->已完成；4->已关闭；5->无效订单，才可以删除
     * ，否则只能先取消订单然后删除。
     *
     * @param orderId
     * @return 受影响的行数
     */
    @Override
    public int deleteOrder(Long orderId) {
        return portalOrderDao.deleteOrder(orderId);
    }

    @Override
    public void sendDelayMessageCancelOrder(MqCancelOrder mqCancelOrder) {
        long delayTimes = 5000L;
        //发送延迟消息
        cancelOrderSender.sendMessage(mqCancelOrder, delayTimes);
    }

    /**
     * 查询用户订单
     *
     * @param pageSize
     * @param pageNum
     * @param memberId 会员ID
     * @param status   订单状态
     * @return
     */
    @Override
    public CommonResult<List<OmsOrderDetail>> findMemberOrderList(Integer pageSize, Integer pageNum, Long memberId, Integer status) {
        PageHelper.startPage(pageNum, pageSize);
        return CommonResult.success(portalOrderDao.findMemberOrderList(memberId, status));
    }

    @Override
    public void updateOrderStatus(Long orderId, Integer payType, String transactionId) {
        //本地事务 修改订单支付状态
        OmsOrder order = new OmsOrder();
        order.setId(orderId);
        order.setStatus(OrderConstant.ORDER_STATUS_UNDELIVERY);
        order.setPayType(payType);
        order.setPaymentTime(new Date());
        omsOrderMapper.updateByPrimaryKeySelective(order);

        //添加事务日志
        omsOrderMapper.addTx(transactionId);
    }

    private void handleRealAmount(List<OmsOrderItem> orderItemList) {
        for (OmsOrderItem orderItem : orderItemList) {
            //原价-促销优惠-优惠券抵扣-积分抵扣
            BigDecimal realAmount = orderItem.getProductPrice();
            if (null != orderItem.getPromotionAmount()) {
                realAmount.subtract(orderItem.getPromotionAmount());
            }
            if (null != orderItem.getCouponAmount()) {
                realAmount.subtract(orderItem.getCouponAmount());
            }
            if (null != orderItem.getIntegrationAmount()) {
                realAmount.subtract(orderItem.getIntegrationAmount());
            }
            orderItem.setRealAmount(realAmount);
        }
    }

    /**
     * 计算订单应付金额
     *
     * @param order
     * @return
     */
    private BigDecimal calcPayAmount(OmsOrder order) {
        //总金额+运费-促销优惠-优惠券优惠-积分抵扣
        BigDecimal payAmount = order.getTotalAmount()
                .add(order.getFreightAmount())
                .subtract(order.getPromotionAmount())
                .subtract(order.getCouponAmount())
                .subtract(order.getIntegrationAmount());
        return payAmount;
    }

    /**
     * 对优惠券优惠进行处理
     *
     * @param orderItemList
     * @param couponHistoryDetail
     */
    private void handleCouponAmount(List<OmsOrderItem> orderItemList, SmsCouponHistoryDetail couponHistoryDetail) {
        SmsCoupon coupon = couponHistoryDetail.getCoupon();
        if (coupon.getUseType().equals(0)) {
            //全场通用
            calcPerCouponAmount(orderItemList, coupon);
        } else if (coupon.getUseType().equals(1)) {
            //指定分类
            List<OmsOrderItem> couponOrderItemList = getCouponOrderItemByRelation(couponHistoryDetail, orderItemList, 0);
            calcPerCouponAmount(couponOrderItemList, coupon);
        } else if (coupon.getUseType().equals(2)) {
            //指定商品
            List<OmsOrderItem> couponOrderItemList = getCouponOrderItemByRelation(couponHistoryDetail, orderItemList, 1);
            calcPerCouponAmount(couponOrderItemList, coupon);
        }
    }

    /**
     * 对每个下单商品进行优惠券金额分摊的计算
     *
     * @param orderItemList
     * @param coupon
     */
    private void calcPerCouponAmount(List<OmsOrderItem> orderItemList, SmsCoupon coupon) {
        BigDecimal totalAmount = calcTotalAmount(orderItemList);
        for (OmsOrderItem orderItem : orderItemList) {
            //(商品价格/可用商品总价)*优惠券面额
            BigDecimal couponAmount = orderItem.getProductPrice().divide(totalAmount, 3, RoundingMode.HALF_EVEN).multiply(coupon.getAmount());
            orderItem.setCouponAmount(couponAmount);
        }
    }

    /**
     * 获取与优惠券有关系的下单商品
     *
     * @param couponHistoryDetail
     * @param orderItemList
     * @param type
     * @return
     */
    private List<OmsOrderItem> getCouponOrderItemByRelation(SmsCouponHistoryDetail couponHistoryDetail, List<OmsOrderItem> orderItemList, int type) {
        List<OmsOrderItem> result = new ArrayList<>();
        if (type == 0) {
            List<Long> categoryIdList = new ArrayList<>();
            for (SmsCouponProductCategoryRelation productCategoryRelation : couponHistoryDetail.getCategoryRelationList()) {
                categoryIdList.add(productCategoryRelation.getProductCategoryId());
            }
            for (OmsOrderItem orderItem : orderItemList) {
                if (categoryIdList.contains(orderItem.getProductCategoryId())) {
                    result.add(orderItem);
                } else {
                    orderItem.setCouponAmount(new BigDecimal(0));
                }
            }
        } else if (type == 1) {
            List<Long> productIdList = new ArrayList<>();
            for (SmsCouponProductRelation productRelation : couponHistoryDetail.getProductRelationList()) {
                productIdList.add(productRelation.getProductId());
            }
            for (OmsOrderItem orderItem : orderItemList) {
                if (productIdList.contains(orderItem.getProductId())) {
                    result.add(orderItem);
                } else {
                    orderItem.setCouponAmount(new BigDecimal(0));
                }
            }
        }
        return result;
    }

    /**
     * 获取该用户可以使用的优惠券
     */
    private SmsCouponHistoryDetail getUseCoupon(List<CartPromotionItem> cartPromotionItemList, Long couponId) {
        //远程调用可用优惠卷列表
        CommonResult<List<SmsCouponHistoryDetail>> couponResult = promotionFeignApi.listCartCoupons(1, cartPromotionItemList);
        if (ResultCode.SUCCESS.getCode() == couponResult.getCode()) {
            List<SmsCouponHistoryDetail> couponHistoryDetailList = couponResult.getData();
            for (SmsCouponHistoryDetail couponHistoryDetail : couponHistoryDetailList) {
                if (couponHistoryDetail.getCoupon().getId().equals(couponId)) {
                    return couponHistoryDetail;
                }
            }
        }
        return null;
    }

    /**
     * 计算总金额
     *
     * @param orderItemList
     * @return
     */
    private BigDecimal calcTotalAmount(List<OmsOrderItem> orderItemList) {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsOrderItem item : orderItemList) {
            totalAmount = totalAmount.add(item.getProductPrice().multiply(new BigDecimal(item.getProductQuantity())));
        }
        return totalAmount;
    }

    /**
     * 计算购物车中商品的价格
     *
     * @param cartPromotionItemList
     * @return
     */
    private ConfirmOrderResult.CalcAmount calcCartAmount(List<CartPromotionItem> cartPromotionItemList) {
        ConfirmOrderResult.CalcAmount calcAmount = new ConfirmOrderResult.CalcAmount();
        calcAmount.setFreightAmount(new BigDecimal(0));
        BigDecimal totalAmount = new BigDecimal("0");
        BigDecimal promotionAmount = new BigDecimal("0");
        for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
            totalAmount = totalAmount.add(cartPromotionItem.getPrice().multiply(new BigDecimal(cartPromotionItem.getQuantity())));
            if (null != cartPromotionItem.getReduceAmount()) {
                promotionAmount = promotionAmount.add(cartPromotionItem.getReduceAmount().multiply(new BigDecimal(cartPromotionItem.getQuantity())));
            }
        }
        calcAmount.setTotalAmount(totalAmount);
        calcAmount.setPromotionAmount(promotionAmount);
        calcAmount.setPayAmount(totalAmount.subtract(promotionAmount));
        return calcAmount;
    }
}
