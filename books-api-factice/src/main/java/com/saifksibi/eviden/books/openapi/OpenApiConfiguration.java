package com.saifksibi.eviden.books.openapi;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI booksApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Books API Factice")
                        .version("1.0.0")
                        .description("""
                                Simple fake Books API used as backend resource behind the authentication gateway.
                                It exposes public, members and admin book resources with hardcoded data.
                                JWT validation is intentionally handled by the gateway, not by this fake API.
                                """));
    }
}