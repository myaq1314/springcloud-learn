package com.zz.scservice2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Created by Francis.zz on 2018/2/27.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ScService2Application {
    public static void main(String[] args) {
        SpringApplication.run(ScService2Application.class, args);
    }
}
