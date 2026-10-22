package com.zingly.events.config;

import static com.zingly.events.common.constants.ApiConstants.*;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configures the title, ownership, and server metadata shown in Swagger UI. */
@Configuration(proxyBeanMethods = false)
public class OpenApiConfig {

    /** Builds the OpenAPI description exposed by Springdoc. */
    @Bean
    OpenAPI engagementEventsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title(OPEN_API_TITLE)
                        .version(OPEN_API_VERSION)
                        .description(OPEN_API_DESCRIPTION)
                        .contact(new Contact().name(OPEN_API_CONTACT_NAME))
                        .license(new License().name(OPEN_API_CONTACT_URL)))
                .addServersItem(new Server()
                        .url(LOCAL_SERVER_URL)
                        .description(LOCAL_SERVER_DESCRIPTION));
    }
}
