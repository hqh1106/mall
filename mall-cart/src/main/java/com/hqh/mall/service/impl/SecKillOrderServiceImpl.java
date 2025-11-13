package com.hqh.mall.service.impl;

import com.hqh.mall.common.api.CommonResult;
import com.hqh.mall.common.constant.RedisKeyPrefixConst;
import com.hqh.mall.common.exception.BusinessException;
import com.hqh.mall.component.LocalCache;
import com.hqh.mall.component.rocketmq.OrderMessageSender;
import com.hqh.mall.dao.MiaoShaStockDao;
import com.hqh.mall.domain.CartPromotionItem;
import com.hqh.mall.domain.ConfirmOrderResult;
import com.hqh.mall.domain.PmsProductParam;
import com.hqh.mall.feignapi.pms.PmsProductFeignApi;
import com.hqh.mall.feignapi.ums.UmsMemberFeignApi;
import com.hqh.mall.mapper.OmsOrderItemMapper;
import com.hqh.mall.mapper.OmsOrderMapper;
import com.hqh.mall.model.UmsMember;
import com.hqh.mall.model.UmsMemberReceiveAddress;
import com.hqh.mall.service.SecKillOrderService;
import com.hqh.mall.utils.RedisOpsExtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {
    @Autowired
    private UmsMemberFeignApi umsMemberFeignApi;
    @Autowired
    private RedisOpsExtUtil redisOpsUtil;
    @Autowired
    private PmsProductFeignApi pmsProductFeignApi;
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OmsOrderItemMapper orderItemMapper;
    @Autowired
    private LocalCache<Boolean> cache;
    @Autowired
    private MiaoShaStockDao miaoShaStockDao;
    //    @Value("${redis.key.prefix.orderId}")
//    private String REDIS_KEY_PREFIX_ORDER_ID;
    @Autowired
    private OrderMessageSender orderMessageSender;

    /**
     * 秒杀订单确认信息
     * @param productId
     * @param memberId
     * @param token
     * @return
     * @throws BusinessException
     */
    @Override
    public CommonResult generateConfirmMiaoShaOrder(Long productId, Long memberId, String token) throws BusinessException {
        //【1】 进行订单金额确认前的库存与购买权限检查
        CommonResult commonResult = confirmCheck(productId,memberId,token);
        if(commonResult.getCode() == 500){
            return commonResult;
        }
        // 【2】调用会员服务获取会员信息
        UmsMember member = umsMemberFeignApi.getMemberById().getData();

        // 【3】从产品服务获取产品信息
        PmsProductParam product = getProductInfo(productId);

        if(product == null){
            return CommonResult.failed("无效的商品！");
        }

        //【4】 验证秒杀时间是否超时
        if(!volidateMiaoShaTime(product)){
            return CommonResult.failed("秒杀活动未开始或已结束！");
        }

        ConfirmOrderResult result = new ConfirmOrderResult();

        //【5】 获取用户收货地址列表
        List<UmsMemberReceiveAddress> memberReceiveAddressList = umsMemberFeignApi.list().getData();
        result.setMemberReceiveAddressList(memberReceiveAddressList);

        //【6】构建商品信息
        List<CartPromotionItem> cartPromotionItemList = new ArrayList<>();
        CartPromotionItem promotionItem = new CartPromotionItem();
        promotionItem.setProductSubTitle(product.getSubTitle());
        promotionItem.setPrice(product.getPrice());
        promotionItem.setProductId(product.getId());//产品ID
        promotionItem.setProductName(product.getName());//产品名称
        promotionItem.setMemberId(memberId);//会员ID
        promotionItem.setMemberNickname(member.getNickname());//昵称
        promotionItem.setProductPic(product.getPic());//产品主图
        promotionItem.setProductBrand(product.getBrandName());//品牌
        promotionItem.setQuantity(1);//购买数量,一次只能秒杀一件
        Integer stock = redisOpsUtil.get(RedisKeyPrefixConst.MIAOSHA_STOCK_CACHE_PREFIX + productId,Integer.class);
        promotionItem.setRealStock(stock);//库存
        promotionItem.setProductCategoryId(product.getProductCategoryId());//产品类目ID
        promotionItem.setGrowth(product.getGiftGrowth());//送积分
        promotionItem.setIntegration(product.getGiftPoint());//送成长值
        promotionItem.setReduceAmount(product.getPrice().subtract(product.getFlashPromotionPrice()));//计算秒杀优惠价格
        promotionItem.setPromotionMessage("秒杀特惠活动");
        cartPromotionItemList.add(promotionItem);
        result.setCartPromotionItemList(cartPromotionItemList);
        //【7】 计算订单总金额
        ConfirmOrderResult.CalcAmount calcAmount = calcCartAmount(product);
        result.setCalcAmount(calcAmount);
        //【8】 会员积分
        result.setMemberIntegration(member.getIntegration());
        return CommonResult.success(result);
    }

    @Override
    public void incrRedisStock(Long productId) {
        if(redisOpsUtil.hasKey(RedisKeyPrefixConst.MIAOSHA_STOCK_CACHE_PREFIX + productId)){
            redisOpsUtil.incr(RedisKeyPrefixConst.MIAOSHA_STOCK_CACHE_PREFIX + productId);
        }
    }
    private CommonResult confirmCheck(Long productId,Long memberId,String token) throws BusinessException {
        /*1、设置标记，如果售罄了在本地cache中设置为true*/
        Boolean localcache = cache.getCache(RedisKeyPrefixConst.MIAOSHA_STOCK_CACHE_PREFIX + productId);
        if(localcache != null && localcache){
            return CommonResult.failed("商品已经售罄,请购买其它商品!");
        }

        /*
         *2、 校验是否有权限购买token TODO 楼兰
         */
      /*  String redisToken = redisOpsUtil.get(RedisKeyPrefixConst.MIAOSHA_TOKEN_PREFIX + memberId + ":" + productId);
        if(StringUtils.isEmpty(redisToken) || !redisToken.equals(token)){
            return CommonResult.failed("非法请求,token无效!");
        }*/

        //3、从redis缓存当中取出当前要购买的商品库存
        Integer stock = redisOpsUtil.get(RedisKeyPrefixConst.MIAOSHA_STOCK_CACHE_PREFIX + productId,Integer.class);

        if(stock == null || stock <= 0){
            /*设置标记，如果售罄了在本地cache中设置为true*/
            cache.setLocalCache(RedisKeyPrefixConst.MIAOSHA_STOCK_CACHE_PREFIX + productId,true);
            return CommonResult.failed("商品已经售罄,请购买其它商品!");
        }

        String async = redisOpsUtil.get(RedisKeyPrefixConst.MIAOSHA_ASYNC_WAITING_PREFIX + memberId + ":" + productId);
        if(async != null && async.equals("1")){
            Map<String,Object> result = new HashMap<>();
            result.put("orderStatus","1");//下单方式0->同步下单,1->异步下单排队中,-1->秒杀失败,>1->秒杀成功(返回订单号)
            return CommonResult.failed(result,"异步下单排队中");
        }
        return CommonResult.success(null);
    }
    @Override
    public boolean shouldPublishCleanMsg(Long productId){
        Integer stock = redisOpsUtil.get(RedisKeyPrefixConst.MIAOSHA_STOCK_CACHE_PREFIX + productId,Integer.class);
        return (stock == null || stock <= 0);
    }
    /**
     * 获取产品信息,http调用产品服务
     */
    @Override
    public PmsProductParam getProductInfo(Long productId){
        //获取商品信息,判断当前商品是否为秒杀商品
        CommonResult<PmsProductParam> commonResult = pmsProductFeignApi.getProductInfo(productId);
        return commonResult.getData();
    }
    /**
     * 计算总金额
     */
    private ConfirmOrderResult.CalcAmount calcCartAmount(PmsProductParam product) {
        ConfirmOrderResult.CalcAmount calcAmount = new ConfirmOrderResult.CalcAmount();
        calcAmount.setFreightAmount(new BigDecimal(0));
        BigDecimal totalAmount = new BigDecimal("0");
        BigDecimal promotionAmount = new BigDecimal("0");
        totalAmount = totalAmount.add(product.getFlashPromotionPrice()
                .multiply(new BigDecimal(1)));
        calcAmount.setTotalAmount(totalAmount);
        calcAmount.setPromotionAmount(promotionAmount);
        calcAmount.setPayAmount(totalAmount);
        return calcAmount;
    }

    /**
     * 验证秒杀时间
     * @param product
     * @return
     */
    private boolean volidateMiaoShaTime(PmsProductParam product){
        //当前时间
        Date now = new Date();
        if(product.getFlashPromotionStatus() != 1
                || product.getFlashPromotionEndDate() == null
                || product.getFlashPromotionStartDate() == null
                || now.after(product.getFlashPromotionEndDate())
                || now.before(product.getFlashPromotionStartDate())){
            return false;
        }
        return true;
    }
}
