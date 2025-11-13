package com.hqh.mall.mapper;

import com.baomidou.dynamic.datasource.annotation.DS;
import com.hqh.mall.domain.StockChanges;
import com.hqh.mall.model.PmsSkuStock;
import com.hqh.mall.model.PmsSkuStockExample;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
@DS("goods")
public interface PmsSkuStockMapper {
    long countByExample(PmsSkuStockExample example);

    int deleteByExample(PmsSkuStockExample example);

    int deleteByPrimaryKey(Long id);

    int insert(PmsSkuStock record);

    int insertSelective(PmsSkuStock record);

    List<PmsSkuStock> selectByExample(PmsSkuStockExample example);

    PmsSkuStock selectByPrimaryKey(Long id);

    int updateByExampleSelective(@Param("record") PmsSkuStock record, @Param("example") PmsSkuStockExample example);
    @DS("goods")
    int lockStockByExample(@Param("lockQuantity") Integer lockQuantity, @Param("example") PmsSkuStockExample example);
    @Select("select count(1) from local_transaction_log where tx_no = #{txNo}")
    int isExistTx(String txNo);
    @Insert("insert into local_transaction_log values(#{txNo},now());")
    int addTx(String txNo);
    int reduceStockByExample(@Param("reduceQuantity") Integer reduceQuantity, @Param("example") PmsSkuStockExample example);
    @DS("goods")
    int updateSkuStock(@Param("itemList") List<StockChanges> orderItemList);

    int recoverStockByExample(@Param("recoverQuantity") Integer recoverQuantity, @Param("example") PmsSkuStockExample example);

    int updateByExample(@Param("record") PmsSkuStock record, @Param("example") PmsSkuStockExample example);

    int updateByPrimaryKeySelective(PmsSkuStock record);

    int updateByPrimaryKey(PmsSkuStock record);
}