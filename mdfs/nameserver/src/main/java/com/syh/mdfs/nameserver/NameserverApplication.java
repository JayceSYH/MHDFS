package com.syh.mdfs.nameserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class NameserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(NameserverApplication.class, args);
	}
}
