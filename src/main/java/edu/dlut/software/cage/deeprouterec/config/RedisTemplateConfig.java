package edu.dlut.software.cage.deeprouterec.config;

import edu.dlut.software.cage.deeprouterec.domain.StationDataRedis;
import edu.dlut.software.cage.deeprouterec.domain.TrainDataRedis;
import edu.dlut.software.cage.deeprouterec.domain.TrainPassbyDataRedis;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisTemplateConfig {

    @Bean("stationRedisTemplate")
    RedisTemplate<String, StationDataRedis> stationRedisTemplate(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, StationDataRedis> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(StationDataRedis.class));
        return template;
    }

    @Bean("trainPassbyDataRedis")
    RedisTemplate<String, TrainPassbyDataRedis> trainPassbyDataRedis(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, TrainPassbyDataRedis> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(TrainPassbyDataRedis.class));
        return template;
    }

    @Bean("trainDataRedis")
    RedisTemplate<String, TrainDataRedis> trainDataRedis(JedisConnectionFactory jedisConnectionFactory) {
        RedisTemplate<String, TrainDataRedis> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new Jackson2JsonRedisSerializer<>(TrainDataRedis.class));
        return template;
    }
}
