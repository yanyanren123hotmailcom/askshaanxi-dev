package com.ryy.app.gateway.filter;

import com.ryy.app.gateway.utils.AppJwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Order(-1)   //@Order 注解的值是一个整数，数值越小，优先级越高，即越先加载。在 Spring MVC 中，如果有多个 @Component 注解的处理器拦截器，@Order 注解可以用来控制它们的拦截顺序。
@Slf4j
public class AuthFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request= exchange.getRequest();
        ServerHttpResponse response= exchange.getResponse();
        String path = request.getURI().getPath();
        if(path.contains("login")){
            //登录请求放行
            return chain.filter(exchange);
        }

        String token = request.getHeaders().getFirst("token");
        if (StringUtils.isEmpty(token)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //5.判断token是否有效
        try {
            log.info("判断token是否有效-------------------");
            Claims claimsBody = AppJwtUtil.getClaimsBody(token);
            //是否是过期
            int result = AppJwtUtil.verifyToken(claimsBody);
            if(result == 1 || result  == 2){
                log.info("token过期------------");
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
            //获取用户id
            String useId = (String)claimsBody.get("id");
            //添加userId到header
            ServerHttpRequest serverHttpRequest = request.mutate().headers(httpHeaders -> {
                httpHeaders.add("userId", useId + "");
            }).build();
            //重置requestServlet
            exchange.mutate().request(serverHttpRequest).build();

        }catch (Exception e){
            e.printStackTrace();
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        //6.放行
        log.info("放行-------------------");
        return chain.filter(exchange);
    }
}
