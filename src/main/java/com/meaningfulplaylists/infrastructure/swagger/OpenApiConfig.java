package com.meaningfulplaylists.infrastructure.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI().info(defaultInfo());
    }

    private Info defaultInfo() {
        return new Info()
                .title("Meaningful-playlists API")
                .description("API for creating playlists in Spotify")
                .version("v1.0.0");
    }
}
