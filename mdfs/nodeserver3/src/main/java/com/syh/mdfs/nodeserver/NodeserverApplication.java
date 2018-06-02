package com.syh.mdfs.nodeserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication
public class NodeserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(NodeserverApplication.class, args);
	}
}
