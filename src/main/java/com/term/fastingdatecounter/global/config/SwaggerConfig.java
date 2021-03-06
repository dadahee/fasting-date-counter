package com.term.fastingdatecounter.global.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI fastingServiceAPI() {
        return new OpenAPI()
                .info(new Info().title("fasting-date-counter API")
                        .description("fasting service API documentation")
                        .version("v0.0.1")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("fasting-date-counter github")
                        .url("https://github.com/dadahee/fasting-date-counter"));
    }
}
