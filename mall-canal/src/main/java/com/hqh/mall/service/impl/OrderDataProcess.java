package com.hqh.mall.service.impl;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.hqh.mall.service.IProcessCanalData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class OrderDataProcess implements IProcessCanalData {
    @Autowired
    @Qualifier("orderConnector")
    private CanalConnector connector;
    @Value("${canal.order.batchSize:1000}")
    private int batchSize;
    @Value("${canal.order.subscribe:server}")
    private String subscribe;

    @Override
    @PostConstruct
    public void connect() {
        connector.connect();
        if ("server".equals(subscribe))
            connector.subscribe();
        else
            connector.subscribe(subscribe);
        connector.rollback();
    }

    @Override
    @PreDestroy
    public void disConnect() {
        connector.disconnect();
    }

    @Override
    @Async
    @Scheduled(initialDelayString = "${canal.order.initialDelay:5000}", fixedDelayString = "${canal.order.fixedDelay:5000}")
    public void processData() {
        try {
            if (!connector.checkValid()) {
                log.warn("与Canal服务器的连接失效！！！重连，下个周期再检查数据变更");
                this.connect();
            } else {
                Message message = connector.getWithoutAck(batchSize);
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    log.info("本次[{}]没有检测到数据更新。", batchId);
                } else {
                    log.info("本次[{}]数据本次共有[{}]次更新需要处理", batchId, size);
                    Set<String> factKeys = new HashSet<>();
                    for (CanalEntry.Entry entry : message.getEntries()) {
                        if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN
                                || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                            continue;
                        }
                        CanalEntry.RowChange rowChange = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
                        String tableName = entry.getHeader().getTableName();
                        if (log.isDebugEnabled()) {
                            CanalEntry.EventType eventType = rowChange.getEventType();
                            log.debug("数据变更详情：来自binglog[{}.{}]，数据源{}.{}，变更类型{}",
                                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                                    entry.getHeader().getSchemaName(), tableName, eventType);
                        }
                    }
                    for (String key : factKeys) {
                        if (StringUtils.isNotEmpty(key)) {
                            log.info("数据产出");
                        }
                    }
                    connector.ack(batchId); // 提交确认
                    log.info("本次[{}]处理Canal同步数据完成", batchId);
                }
            }
        } catch (Exception e) {
            log.error("Canal同步数据失效，请检查", e);
        }
    }
}
