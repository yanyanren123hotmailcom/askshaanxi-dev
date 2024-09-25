package com.ryy.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

//@EnableDiscoveryClient注解：能够让注册中心能够发现，扫描到该服务。
//@MapperScan
//作用：指定要变成实现类的接口所在的包，然后包下面的所有接口在编译之后都会生成相应的实现类
//添加位置：是在Springboot启动类上面添加，
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.ryy.user.mapper")
@ComponentScan(basePackages = "com.ryy")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class,args);
    }
}
