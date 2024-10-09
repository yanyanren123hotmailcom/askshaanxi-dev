package com.ryy.wemedia.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


/*
***因为启动类的EnableFeignClients扫不到降级代码fallback，因此加此配置，扫描指定包
 */
@ComponentScan(basePackages = {"com.ryy.apis.article.fallback","com.ryy.apis.user.fallback"})
@Configuration
public class InitConfig {
}
