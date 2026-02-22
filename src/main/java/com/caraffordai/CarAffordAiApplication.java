package com.caraffordai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CarAfford AI – Smart Car Affordability Advisor
 *
 * Entry point for the Spring Boot application.
 * Architecture: Controller → Service → Repository → Entity
 *
 * Interview Tip: @SpringBootApplication is a meta-annotation that combines:
 * - @Configuration → Beans definition
 * - @EnableAutoConfiguration → Auto-wires Spring components
 * - @ComponentScan → Scans com.caraffordai.* packages
 */
@SpringBootApplication
public class CarAffordAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CarAffordAiApplication.class, args);
    }
}
