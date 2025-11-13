package com.hqh.mall.sharding;

import org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.hint.HintShardingValue;

import java.util.Collection;

/**
 * 分库分表后兜底路由策略，全库表路由
 */
public class OrderAllRangeHintAlgorithm implements HintShardingAlgorithm {
    @Override
    public Collection<String> doSharding(Collection availableTargetNames, HintShardingValue hintShardingValue) {
        return availableTargetNames;
    }
}
