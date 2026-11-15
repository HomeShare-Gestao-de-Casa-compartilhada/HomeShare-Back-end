package com.homeshare.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

/** Tudo que está sob "app:" no application.yaml. A aplicação não sobe se algo obrigatório faltar. */
@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(
        @NotBlank String timezone,
        @NotBlank String frontendUrl,
        @NotNull @Valid Jwt jwt,
        @NotNull @Valid Cadastro cadastro,
        @NotNull @Valid Convite convite,
        @NotNull @Valid Cors cors,
        @NotNull @Valid Mail mail) {

    public record Jwt(
            @NotBlank @Size(min = 32, message = "JWT_SECRET precisa ter pelo menos 32 caracteres") String secret,
            @NotBlank String issuer,
            @Positive long expirationMinutes) {
    }

    public record Cadastro(@Positive long tokenTtlMinutes, @Positive long cooldownSeconds) {
    }

    public record Convite(@Positive int validadePadraoHoras, @Positive int validadeMaximaHoras) {
    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {
    }

    public record Mail(
            @NotBlank String provider,
            String apiKey,
            @NotBlank String apiUrl,
            @NotBlank String from,
            String overrideTo) {
    }
}
