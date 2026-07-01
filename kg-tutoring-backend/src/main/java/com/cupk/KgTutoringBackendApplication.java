package com.cupk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.cupk.mapper")
public class KgTutoringBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(KgTutoringBackendApplication.class, args);
	}

}
