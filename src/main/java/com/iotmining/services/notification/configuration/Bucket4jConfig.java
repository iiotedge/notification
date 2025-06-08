package com.iotmining.services.notification.configuration;


import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Bucket4jConfig {

//    @Bean
//    public ProxyManager<String> proxyManager() {
//        // Create a Redis client to connect to Redis server
//        RedisClient redisClient = RedisClient.create("redis://localhost:6379");
//        StatefulRedisConnection<String, byte[]> connection = redisClient.connect(new ByteArrayCodec());
//
//        // Create a ProxyManager for managing the rate-limiting buckets
//        return Bucket4j.extension(RedisProperties.Lettuce.class)
//                .proxyManagerFor(connection);
//    }
}
