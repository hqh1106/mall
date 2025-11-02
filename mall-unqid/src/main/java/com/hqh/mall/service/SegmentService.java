package com.hqh.mall.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.hqh.leafcore.IDGen;
import com.hqh.leafcore.common.PropertiesFactory;
import com.hqh.leafcore.common.Result;
import com.hqh.leafcore.common.ZeroIDGen;
import com.hqh.leafcore.segment.SegmentIDGenImpl;
import com.hqh.leafcore.segment.dao.IDAllocDao;
import com.hqh.leafcore.segment.dao.impl.IDAllocDaoImpl;
import com.hqh.mall.Constants;
import com.hqh.mall.exception.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
public class SegmentService {
    private Logger logger = LoggerFactory.getLogger(SegmentService.class);

    private IDGen idGen;

    private DruidDataSource dataSource;
    public SegmentService() throws SQLException, InitException {
        Properties properties = PropertiesFactory.getProperties();
        boolean flag = Boolean.parseBoolean(properties.
                getProperty(Constants.LEAF_SEGMENT_ENABLE, "true"));
        if (flag){
            dataSource = new DruidDataSource();
            dataSource.setUrl(properties.getProperty(Constants.LEAF_JDBC_URL));
            dataSource.setUsername(properties.getProperty(Constants.LEAF_JDBC_USERNAME));
            dataSource.setPassword(properties.getProperty(Constants.LEAF_JDBC_PASSWORD));
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setValidationQuery("select 1");
            dataSource.init();

            logger.info("leaf.properties配置：{}",properties);
            // Config Dao
            IDAllocDao dao = new IDAllocDaoImpl(dataSource);

            // Config ID Gen
            idGen = new SegmentIDGenImpl();
            ((SegmentIDGenImpl) idGen).setDao(dao);
            if (idGen.init()) {
                logger.info("Segment Service Init Successfully");
            } else {
                throw new InitException("Segment Service Init Fail");
            }
        }else {
            idGen = new ZeroIDGen();
            logger.info("Zero ID Gen Service Init Successfully");
        }
    }
    public Result getId(String key){
        return idGen.get(key);
    }
    public List<Result> getIds(String key, int keyNumber){
        List<Result> results = new ArrayList<>();
        for (int i = 0; i < keyNumber; i++) {
            results.add(idGen.get(key));
        }
        return results;
    }
    public SegmentIDGenImpl getIdGen(){
        if (idGen instanceof SegmentIDGenImpl){
            return (SegmentIDGenImpl) idGen;
        }
        return null;
    }

}
