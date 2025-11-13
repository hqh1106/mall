package com.hqh.mall.service.impl;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.dao.FlashPromotionProductDao;
import com.hqh.mall.domain.CartPromotionItem;
import com.hqh.mall.mapper.PmsSkuStockMapper;
import com.hqh.mall.mapper.SmsFlashPromotionProductRelationMapper;
import com.hqh.mall.model.PmsSkuStock;
import com.hqh.mall.model.SmsFlashPromotionProductRelation;
import com.hqh.mall.service.PmsProductService;
import com.hqh.mall.service.StockManageService;
import com.hqh.mall.utils.RedisOpsExtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.List;

@Service
@Slf4j
public class StockManageServiceImpl implements StockManageService {

    @Autowired
    private PmsSkuStockMapper skuStockMapper;

    @Autowired
    private SmsFlashPromotionProductRelationMapper flashPromotionProductRelationMapper;

    @Autowired
    private FlashPromotionProductDao flashPromotionProductDao;

    @Autowired
    private PmsProductService productService;

    @Autowired
    private RedisOpsExtUtil redisOpsUtil;

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
        if (ObjectUtils.isEmpty(miaoshaStock)) {
            return CommonResult.failed("不存在该秒杀商品！");
        }

        return CommonResult.success(miaoshaStock.getFlashPromotionCount());
    }

    @Override
    public CommonResult lockStock(List<CartPromotionItem> cartPromotionItemList) {
        try {

            for (CartPromotionItem cartPromotionItem : cartPromotionItemList) {
                PmsSkuStock skuStock = skuStockMapper.selectByPrimaryKey(cartPromotionItem.getProductSkuId());
                skuStock.setLockStock(skuStock.getLockStock() + cartPromotionItem.getQuantity());
                skuStockMapper.updateByPrimaryKeySelective(skuStock);
            }
            return CommonResult.success(true);
        } catch (Exception e) {
            log.error("锁定库存失败...");
            return CommonResult.failed();
        }
    }
}
