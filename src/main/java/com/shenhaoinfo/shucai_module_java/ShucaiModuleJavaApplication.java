package com.shenhaoinfo.shucai_module_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
public class ShucaiModuleJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShucaiModuleJavaApplication.class, args);
	}

}
