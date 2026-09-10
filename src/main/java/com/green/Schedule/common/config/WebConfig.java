package com.green.Schedule.common.config;

import com.green.Schedule.common.interceptor.LoginCheckInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginCheckInterceptor())
                .addPathPatterns("/**")
                // 로그인 없이 볼 수 있는 화면 / 정적 자원은 검사에서 제외
                .excludePathPatterns(
                        "/",
                        "/service-intro",
                        "/features",
                        "/member/**",
                        "/*.css",
                        "/*.js",
                        "/*.ico",
                        "/common/**",
                        "/css/**",
                        "/js/**"
                );
    }
}
