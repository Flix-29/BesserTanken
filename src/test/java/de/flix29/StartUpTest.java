package de.flix29;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
public class StartUpTest {

	@Test
	void startUp_WithNoErrors() {

	}

	@Test
	void loadRepositorySecrets() {
		String env = System.getenv("API_KEY");

		assertThat(env)
				.isNotBlank();
	}

}
