package com.project.fitness.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.addViewController("/home").setViewName("forward:/index.html");
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/register").setViewName("forward:/index.html");
        registry.addViewController("/forgot-password").setViewName("forward:/index.html");
        registry.addViewController("/reset-password").setViewName("forward:/index.html");
        registry.addViewController("/verify-otp").setViewName("forward:/index.html");
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/activities").setViewName("forward:/index.html");
        registry.addViewController("/recommendations").setViewName("forward:/index.html");
    }

    @org.springframework.context.annotation.Bean
    public org.springframework.web.filter.ForwardedHeaderFilter forwardedHeaderFilter() {
        return new org.springframework.web.filter.ForwardedHeaderFilter();
    }
}
