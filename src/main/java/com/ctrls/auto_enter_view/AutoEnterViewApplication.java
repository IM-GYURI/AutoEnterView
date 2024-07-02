package com.ctrls.auto_enter_view;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AutoEnterViewApplication {

	public static void main(String[] args) {
		SpringApplication.run(AutoEnterViewApplication.class, args);
	}

}
