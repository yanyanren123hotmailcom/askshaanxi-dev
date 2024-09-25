网关的作用：
1、路由
2、限流
3、鉴权
4、监控

网关的配置
spring:
    cloud:
        gateway:
            globalcors:  --->跨域配置
                add-to-simple-url-handler-mapping: true
                corsConfigurations:
                    '[/**]':  --->允许所有域名
                        allowedHeaders: "*"
                        allowedOrigins: "*"
                        allowedMethods:
                            - GET
                            - POST
                            - DELETE
                            - PUT
                            - OPTION
            routes:  --->路由规则
                - id: user   --->自定义id
                    uri: lb://askshaanxi-user   --->到哪个微服务去
                    predicates:   --->断言
                        - Path=/user/**   --->若访问路径匹配/user/**，则路由到askshaanxi-user服务上去
                    filters:
                        - StripPrefix= 1   --->过滤路径前缀第一个，即输入路径的user
//例如一个请求：http://192.168.22.223:56001/user/api/v1/login/login_auth,则会路由到lb://askshaanxi-user/api/v1/login/login_auth
                - id: article
                    uri: lb://askshaanxi-article
                    predicates:
                        - Path=/article/**
                    filters:
                        - StripPrefix= 1
                - id: askshaanxi-search
                    uri: lb://askshaanxi-search
                    predicates:
                        - Path=/search/**
                    filters:
                        - StripPrefix= 1
                - id: askshaanxi-behavior
                    uri: lb://askshaanxi-behavior
                    predicates:
                        - Path=/behavior/**
                    filters:
                        - StripPrefix= 1
                - id: askshaanxi-comment
                    uri: lb://askshaanxi-comment
                    predicates:
                        - Path=/comment/**
                    filters:
                        - StripPrefix= 1
