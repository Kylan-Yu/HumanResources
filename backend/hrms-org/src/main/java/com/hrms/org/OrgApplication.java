package com.hrms.org;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 组织服务启动类
 *
 * @author HRMS
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class OrgApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrgApplication.class, args);
    }
}
