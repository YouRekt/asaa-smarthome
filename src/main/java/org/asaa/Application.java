package org.asaa;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		new SpringApplicationBuilder(Application.class)
				.headless(false) // Disable headless mode so JADE GUI can open
				.run(args);
	}

}
