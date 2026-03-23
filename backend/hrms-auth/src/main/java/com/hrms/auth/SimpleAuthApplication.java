package com.hrms.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 简化认证服务启动类
 *
 * @author HRMS
 */
@SpringBootApplication(exclude = {
    // 排除Spring Security自动配置
    // org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
    // org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class
})
public class SimpleAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(SimpleAuthApplication.class, args);
    }
}
