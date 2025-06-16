package learning.spring.binarytea;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.hibernate5.Hibernate5Module;
import com.fasterxml.jackson.datatype.jodamoney.JodaMoneyModule;
import learning.spring.binarytea.model.MenuItem;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

import java.time.Duration;

@SpringBootApplication
@EnableCaching
public class BinaryTeaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BinaryTeaApplication.class, args);
    }

    @Bean
    public JodaMoneyModule jodaMoneyModule() {
        return new JodaMoneyModule();
    }

    @Bean
    public Hibernate5Module hibernate5Module() {
        return new Hibernate5Module();
    }

    // 配置 Spring Cache 抽象使用的 Redis 缓存配置
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(ObjectMapper objectMapper) {
        // 创建一个新的 ObjectMapper 副本，避免影响全局配置
        ObjectMapper cacheObjectMapper = objectMapper.copy();
        // 启用默认类型信息，这样 Jackson 会在 JSON 中包含类型信息
        cacheObjectMapper.activateDefaultTyping(
            cacheObjectMapper.getPolymorphicTypeValidator(),
            ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        serializer.setObjectMapper(cacheObjectMapper);
        
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofSeconds(60)) // 设置缓存过期时间
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
                .disableCachingNullValues(); // 不缓存 null 值
    }

    // 添加 Redis 配置，支持 JSON 序列化
    // 保留原有的 RedisTemplate 配置（用于直接操作 Redis）
    @Bean
    public RedisTemplate<String, MenuItem> redisTemplate(RedisConnectionFactory connectionFactory,
                                                         ObjectMapper objectMapper) {
        Jackson2JsonRedisSerializer<MenuItem> serializer = new Jackson2JsonRedisSerializer<>(MenuItem.class);
        serializer.setObjectMapper(objectMapper);

        RedisTemplate<String, MenuItem> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);
        redisTemplate.setKeySerializer(RedisSerializer.string());
        redisTemplate.setValueSerializer(serializer);
        return redisTemplate;
    }
}