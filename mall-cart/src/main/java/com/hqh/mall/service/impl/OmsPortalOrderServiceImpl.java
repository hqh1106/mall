package com.hqh.mall.service.impl;

import com.hqh.mall.common.exception.BusinessException;
import com.hqh.mall.domain.CartPromotionItem;
import com.hqh.mall.domain.ConfirmOrderResult;
import com.hqh.mall.feignapi.ums.UmsMemberFeignApi;
import com.hqh.mall.model.UmsMemberReceiveAddress;
import com.hqh.mall.service.OmsCartItemService;
import com.hqh.mall.service.OmsPortalOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * 前台订单管理Service
 */
@Service
@Slf4j
public class OmsPortalOrderServiceImpl implements OmsPortalOrderService {

    @Autowired
    private UmsMemberFeignApi umsMemberFeignApi;

    @Autowired
    private OmsCartItemService cartItemService;

    /**
     * 确认选择购买的商品
     * @param itemIds
     * @param memberId
     * @return
     * @throws BusinessException
     */
    @Override
    public ConfirmOrderResult generateConfirmOrder(List<Long> itemIds, Long memberId) throws BusinessException {
        ConfirmOrderResult result = new ConfirmOrderResult();
        List<CartPromotionItem> cartPromotionItemList = cartItemService.listSelectedPromotion(memberId,itemIds);
        result.setCartPromotionItemList(cartPromotionItemList);
        List<UmsMemberReceiveAddress> memberReceiveAddressList = umsMemberFeignApi.list().getData();
        result.setMemberReceiveAddressList(memberReceiveAddressList);

        ConfirmOrderResult.CalcAmount calcAmount = calcCartAmount(cartPromotionItemList);
        result.setCalcAmount(calcAmount);
        return result;
    }

    /**
     * 计算购物车中商品的价格
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
            if (null!=cartPromotionItem.getReduceAmount()) {
                promotionAmount = promotionAmount.add(cartPromotionItem.getReduceAmount().multiply(new BigDecimal(cartPromotionItem.getQuantity())));
            }
        }
        calcAmount.setTotalAmount(totalAmount);
        calcAmount.setPromotionAmount(promotionAmount);
        calcAmount.setPayAmount(totalAmount.subtract(promotionAmount));
        return calcAmount;
    }
}
