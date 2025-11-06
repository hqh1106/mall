package com.hqh.mall.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hqh.mall.utils.RedisOpsExtUtil;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisExtConfig {

    @Value("${spring.redis.cluster.nodes:127.0.0.1:6379}")
    String redisNodes;
    @Value("${spring.redis.password:nopwd}")
    String redisPass;
    @Value("${spring.redis.host:127.0.0.1}")
    String ip;
    @Value("${spring.redis.port:nopwd}")
    String port;
    @Autowired
    private RedisConnectionFactory connectionFactory;

    @Primary
    @Bean("redisCluster")
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate();
        template.setConnectionFactory(connectionFactory);
        // 序列化工具
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer
                = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jackson2JsonRedisSerializer);

        template.setHashKeySerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public RedisOpsExtUtil redisOpsExtUtil() {
        return new RedisOpsExtUtil();
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
            //先使用单机
//        ClusterServersConfig clusterServersConfig = config.useClusterServers();
//        for (String node : redisNodes.split(",")) {
//            clusterServersConfig.addNodeAddress("redis://" + node);
//        }
        SingleServerConfig clusterServersConfig = config.useSingleServer();
        clusterServersConfig.setAddress("redis://"+ip+":"+port);
        if (!"nopwd".equals(redisPass)) {
            clusterServersConfig.setPassword(redisPass);
        }
        return Redisson.create(config);
    }
}
