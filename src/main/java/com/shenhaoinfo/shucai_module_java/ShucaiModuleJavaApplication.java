package com.shenhaoinfo.shucai_module_java;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@MapperScan("com.shenhaoinfo.shucai_module_java.mapper")
public class ShucaiModuleJavaApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShucaiModuleJavaApplication.class, args);
	}

}
