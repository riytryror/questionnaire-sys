package com.whu.survey.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射规则：
        // 访问路径: /files/**
        // 物理路径: file:D:/survey_files/ (注意前面要加 file:)
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:D:/A_HomeWork/OSSDEV/lFinal/Questionnaire_sys/src/main/webapp/uploads/");

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:D:/A_HomeWork/OSSDEV/lFinal/Questionnaire_sys/src/main/webapp/uploads/");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 对所有路径生效
        registry.addMapping("/**")
                // 允许所有来源
                .allowedOriginPatterns("*")
                // 允许所有方法 (GET, POST, PUT, DELETE, OPTIONS)
                .allowedMethods("*")
                // 允许携带 Cookie
                .allowCredentials(true)
                // 预检请求缓存时间 (1小时)
                .maxAge(3600);
    }
}