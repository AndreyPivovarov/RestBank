package com.example.bankcards.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "RestBank API",
                description = "REST API для управления банковскими картами. " +
                        "Система поддерживает создание карт, переводы между картами, " +
                        "управление пользователями и запросы на блокировку карт.",
                version = "1.0.0",
                contact = @Contact(
                        name = "Andrey Pivovarov",
                        email = "support@restbank.com",
                        url = "https://github.com/AndreyPivovarov/RestBank"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development Server",
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Production Server",
                        url = "https://api.restbank.com"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT токен для аутентификации. Получите токен через /api/auth/login",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
