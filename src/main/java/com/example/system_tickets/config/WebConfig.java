package com.example.system_tickets.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Permite ver las fotos de la carpeta "uploads" usando la URL "/uploads/..."
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}