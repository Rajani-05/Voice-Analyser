package com.voiceanalyser.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        java.io.File distDir = new java.io.File("../frontend/dist");
        if (distDir.exists() && distDir.isDirectory()) {
            registry.addResourceHandler("/**")
                    .addResourceLocations("file:../frontend/dist/");
        } else {
            registry.addResourceHandler("/**")
                    .addResourceLocations("file:../frontend/");
        }
    }
}
