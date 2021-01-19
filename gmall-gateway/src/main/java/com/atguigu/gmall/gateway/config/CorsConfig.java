package com.atguigu.gmall.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    /**
     * Spring提供了两个cors跨域的过滤器
     * 1. CorsFilter：针对底层基于servlet的网关组件，如zuul
     * 2. CorsWebFilter：针对底层基于webflux的网关组件，如gateway
     */
    @Bean
    public CorsWebFilter corsWebFilter(){
        //构造器要传入的对象准备
        UrlBasedCorsConfigurationSource configurationSource = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        //设置过滤逻辑
        corsConfiguration.addAllowedOrigin("http://manager.gmall.com");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.setAllowCredentials(true);//允许携带cookies
        //拦截所有域名
        configurationSource.registerCorsConfiguration("/**",corsConfiguration);
        return new CorsWebFilter(configurationSource);
    }

}
