package ru.vsu.cs.OOP.mordvinovil.task2.social_network;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.vsu.cs.OOP.mordvinovil.task2.social_network.service.messaging.MessageCacheService;

@ActiveProfiles("test")
@SpringBootTest
class CommunicationNetworkApplicationTests {

	@MockitoBean
	private RedisConnectionFactory redisConnectionFactory;

	@MockitoBean
	private RedisTemplate<String, Object> redisTemplate;

	@MockitoBean
	private MessageCacheService messageCacheService;

	@Test
	void contextLoads() {
	}
}