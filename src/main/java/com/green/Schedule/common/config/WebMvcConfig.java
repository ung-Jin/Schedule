package com.green.Schedule.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 업로드된 파일(AS 처리 사진 등)을 /uploads/** 경로로 서빙하기 위한 설정.
 * 업로드 폴더는 클래스패스(static) 밖, 프로젝트 루트의 uploads/ 디렉터리를 사용합니다.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
