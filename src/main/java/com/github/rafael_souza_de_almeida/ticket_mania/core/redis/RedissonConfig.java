package com.github.rafael_souza_de_almeida.ticket_mania.core.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private String redisPort;

    @Value("${spring.data.redis.password}")
    private String redisPassword;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();

        String redisUrl = String.format("rediss://default:%s@%s:%s", redisPassword, redisHost, redisPort);

        config.useSingleServer()
              .setAddress(redisUrl);

        return Redisson.create(config);
    }
}
