package com.jfseat.lowcode;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.jfseat.lowcode.integration.PlmIntegrationProperties;

@SpringBootApplication
@EnableConfigurationProperties(PlmIntegrationProperties.class)
public class LowCodeApplication {

    /**
     * 启动PLM低代码编辑器服务
     **
     * @param args 启动参数
     * @author caipan by codex
     * @date 2026/9/3 10:00
     */
    public static void main(String[] args) {
        SpringApplication.run(LowCodeApplication.class, args);
    }
}
