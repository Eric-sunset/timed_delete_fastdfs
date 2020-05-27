package com.jitu.timeddeleted.config;

import com.jitu.timeddeleted.tool.FastJsonRedisSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * redis的配置类
 * @author wangronglian
 * @date 2020年1月6日13:51:26
 * @version 1.0
 */
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String,Object> redisTemplate(LettuceConnectionFactory factory){
        RedisTemplate<String,Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);

        // 配置key和value的序列化方式
        redisTemplate.setKeySerializer(stringRedisSerializer);
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        // 返回生成的实例
        return  redisTemplate;
    }
}
