package com.szschoolmanager.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.*;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.beans.factory.annotation.Value;

@Configuration
public class RedisConfig {

  @Value("${spring.redis.host:localhost}")
  private String redisHost;

  @Value("${spring.redis.port:6379}")
  private int redisPort;

  @Bean
  public LettuceConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(new RedisStandaloneConfiguration(redisHost, redisPort));
  }

  @Bean
  public StringRedisTemplate stringRedisTemplate(LettuceConnectionFactory connectionFactory) {
    StringRedisTemplate template = new StringRedisTemplate();
    template.setConnectionFactory(connectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    return template;
  }
}
