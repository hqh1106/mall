package com.hqh.mall.service.impl;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.domain.CartPromotionItem;
import com.hqh.mall.domain.StockChanges;
import com.hqh.mall.mapper.PmsSkuStockMapper;
import com.hqh.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.hqh.mall.model.PmsSkuStockExample;
import com.hqh.mall.model.SmsFlashPromotionProductRelation;
import com.hqh.mall.service.StockManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;
@Slf4j
@Service
public class StockManageServiceImpl implements StockManageService {

    @Autowired
    private PmsSkuStockMapper skuStockMapper;

    @Autowired
    private SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;

    @Override
    public Integer incrStock(Long productId, Long skuId, Integer quanlity, Integer miaosha, Long flashPromotionRelationId) {
        return null;
    }

    @Override
    public Integer descStock(Long productId, Long skuId, Integer quanlity, Integer miaosha, Long flashPromotionRelationId) {
        return null;
    }

    @Override
    public CommonResult<Integer> selectStock(Long productId, Long flashPromotionRelationId) {
        SmsFlashPromotionProductRelation miaoshaStock = flashPromotionProductRelationMapper.selectByPrimaryKey(flashPromotionRelationId);
        if(ObjectUtils.isEmpty(miaoshaStock)){
            return CommonResult.failed("不存在该秒杀商品！");
        }

        return CommonResult.success(miaoshaStock.getFlashPromotionCount());
    }

    @Override
    public CommonResult lockStock(List<CartPromotionItem> cartPromotionItemList) {
        try {
            for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
                PmsSkuStockExample pmsSkuStockExample = new PmsSkuStockExample();
                pmsSkuStockExample.createCriteria()
                        .andIdEqualTo(cartPromotionItem.getProductSkuId())
                        .andStockGreaterThanOrEqualTo(cartPromotionItem.getQuantity());
                skuStockMapper.lockStockByExample(cartPromotionItem.getQuantity(),pmsSkuStockExample);
            }
            //简化操作，认定锁定库存一定成功，实际应该检查是否锁定成功，后期补全
            return CommonResult.success(true);
        }catch (Exception e) {
            log.error("锁定库存失败...{}",e);
            return CommonResult.failed();
        }
    }

    /**
     * 订单支付后实际扣减库存
     * @param stockChangesList
     * @return
     */
    @Override
    public CommonResult reduceStock(List<StockChanges> stockChangesList) {
        try{
            int result = skuStockMapper.updateSkuStock(stockChangesList);
            return CommonResult.success(result);
        }catch (Exception ex){
            log.error("订单支付后扣减库存失败...{}",ex);
            return CommonResult.failed();
        }
    }

    /**
     * 订单取消，恢复库存
     * @param stockChangesList
     * @return
     */
    @Override
    public CommonResult recoverStock(List<StockChanges> stockChangesList) {
        try {
            for (StockChanges changesProduct : stockChangesList) {
                PmsSkuStockExample pmsSkuStockExample = new PmsSkuStockExample();
                pmsSkuStockExample.createCriteria()
                        .andIdEqualTo(changesProduct.getProductSkuId());
                skuStockMapper.recoverStockByExample(changesProduct.getChangesCount(),pmsSkuStockExample);
            }
            return CommonResult.success(true);
        }catch (Exception e) {
            log.error("恢复库存失败...{}",e);
            return CommonResult.failed();
        }
    }
}
