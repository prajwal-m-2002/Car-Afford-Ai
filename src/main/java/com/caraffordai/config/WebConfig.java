package com.caraffordai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig – Global CORS + Static Resource Configuration
 *
 * Enables frontend (HTML/JS) running on the same server or a different port
 * to call Spring Boot REST APIs without cross-origin blocking.
 *
 * Interview Tip: CORS is enforced by the browser, not the server.
 * Spring's @CrossOrigin or CorsRegistry tells browsers this server
 * accepts cross-origin requests from the listed origins.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Allow all origins during development.
     * In production, replace "*" with specific frontend domain.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    /**
     * Serve static frontend files from classpath:/static/
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }
}
