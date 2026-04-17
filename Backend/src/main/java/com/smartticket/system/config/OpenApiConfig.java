package com.smartticket.system.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI smartTicketOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Smart Ticket Management API")
                .description("APIs for auto assignment, SLA tracking, dashboard and administration")
                .version("v1")
                .contact(new Contact().name("Smart Ticket Team"))
                .license(new License().name("Demo License")));
    }
}
