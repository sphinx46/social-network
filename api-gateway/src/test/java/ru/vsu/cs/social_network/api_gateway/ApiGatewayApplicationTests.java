package ru.vsu.cs.social_network.api_gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
		// Простой тест для проверки загрузки контекста
		// В тестовом профиле Gateway и Security конфигурации упрощены
	}

}
