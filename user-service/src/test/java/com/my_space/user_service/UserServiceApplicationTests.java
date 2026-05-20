package com.my_space.user_service;

import com.my_space.user_service.entity.User;
import com.my_space.user_service.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
class UserServiceApplicationTests {

	public static final int USERS = 10;
	@Autowired
	private UserRepository userRepository;

	@Test
	void contextLoads() {
	}

	@Disabled
	@Test
	void addUsersToDB() {
		for (int i = 1; i<= USERS; i++) {
			User user = User.builder()
					.name("name" + i)
					.surname("surname" + i)
					.email("email" + i + "@test.com")
					.alerting(i%2 == 0)
					.energyAlertingThreshold(1000.0 + i)
					.address("address" + i)
					.build();
			userRepository.save(user);
		}
		log.info("Users created");
	}

}
