package com.wtl.novel.Config;

import com.wtl.novel.Interceptor.SignatureInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired // 自动注入拦截器 Bean
    private SignatureInterceptor signatureInterceptor;

    @Bean
    public List<String> limitUrl() {
        List<String> urls = new ArrayList<>();
        urls.add("/api/chapters/update");
        urls.add("/api/feedback/add");
        urls.add("/api/comments/add");
        return urls;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(signatureInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/api/auth/**","/test/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径
                .allowedOriginPatterns("*") // 允许所有来源（Spring Boot 2.4+ 使用 allowedOriginPatterns 替代 allowedOrigins）
                .allowedMethods("*") // 允许所有 HTTP 方法（GET、POST 等）
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(false) // 不允许凭证（若为 true，则 allowedOrigins 不能为 *）
                .maxAge(3600); // 预检请求缓存时间
    }
}