package com.lkj.lkjapigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;


/**
 * 全局跨域配置
 */
@Configuration
public class CorsConfig{

    private static final String MAX_AGE = "18000L";

    @Bean
    public CorsWebFilter corsWebFilter(){
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //1、配置跨域
        //设置允许的header属性
        corsConfiguration.addAllowedHeader("*");
        //设置允许的方法
        corsConfiguration.addAllowedMethod("*");
        //设置允许跨域请求的域名
        corsConfiguration.addAllowedOriginPattern("*");
        corsConfiguration.setAllowCredentials(true);


        source.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(source);
//        @Bean
//        public WebFilter corsFilter() {
//        return (ServerWebExchange ctx, WebFilterChain chain) -> {
//            ServerHttpRequest request = ctx.getRequest();
//            // 使用SpringMvc自带的跨域检测工具类判断当前请求是否跨域
//            if (!CorsUtils.isCorsRequest(request)) {
//                return chain.filter(ctx);
//            }
//            HttpHeaders requestHeaders = request.getHeaders();                                  // 获取请求头
//            ServerHttpResponse response = ctx.getResponse();                                    // 获取响应对象
//            HttpMethod requestMethod = requestHeaders.getAccessControlRequestMethod();          // 获取请求方式对象
//            HttpHeaders headers = response.getHeaders();                                        // 获取响应头
//            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, requestHeaders.getOrigin());   // 把请求头中的请求源（协议+ip+端口）添加到响应头中（相当于yml中的allowedOrigins）
//            headers.addAll(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, requestHeaders.getAccessControlRequestHeaders());
//            if (requestMethod != null) {
//                headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, requestMethod.name());    // 允许被响应的方法（GET/POST等，相当于yml中的allowedMethods）
//            }
//            headers.add(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");          // 允许在请求中携带cookie（相当于yml中的allowCredentials）
//            headers.add(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "*");                // 允许在请求中携带的头信息（相当于yml中的allowedHeaders）
//            headers.add(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);                           // 本次跨域检测的有效期(单位毫秒，相当于yml中的maxAge)
//            if (request.getMethod() == HttpMethod.OPTIONS) {                                    // 直接给option请求反回结果
//                response.setStatusCode(HttpStatus.OK);
//                return Mono.empty();
//            }
//            return chain.filter(ctx);                                                           // 不是option请求则放行
//        };
    }
}