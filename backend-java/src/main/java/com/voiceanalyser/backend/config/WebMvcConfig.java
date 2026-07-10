package com.voiceanalyser.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        java.io.File distDir = new java.io.File("../frontend/dist");
        String baseLocation = (distDir.exists() && distDir.isDirectory())
                ? "file:../frontend/dist/"
                : "file:../frontend/";

        // Only serve known static asset directories — do NOT use /** which intercepts API routes
        registry.addResourceHandler("/assets/**")
                .addResourceLocations(baseLocation + "assets/");
        registry.addResourceHandler("/static/**")
                .addResourceLocations(baseLocation + "static/");
        // Serve root-level static files (favicon, manifest, etc.)
        registry.addResourceHandler("/favicon.ico", "/manifest.json", "/robots.txt", "/*.js", "/*.css", "/*.svg", "/*.png")
                .addResourceLocations(baseLocation);
    }
}
