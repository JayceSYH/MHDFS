package com.syh.mdfs.testclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class TestclientApplication {

	public static void main(String[] args) {
		SpringApplication.run(TestclientApplication.class, args);
	}
}
