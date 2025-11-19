package com.iiil.tutoring;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TutoringPlatformApplicationTests {

	@Test
	void contextLoads() {
		// This test will verify that the Spring context can load properly
		// which validates our configuration and entity mappings
	}

}
