package com.homeshare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDto {

    private AuthDto() {
    }

    public record RegistroRequest(
            @NotBlank(message = "O nome é obrigatório")
            @Size(min = 2, max = 100, message = "O nome deve ter entre 2 e 100 caracteres")
            String nome,

            @NotBlank(message = "O e-mail é obrigatório")
            @Email(message = "E-mail inválido")
            @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres")
            String email,

            @NotBlank(message = "A senha é obrigatória")
            @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres")
            String senha) {
    }

    public record RegistroResponse(String mensagem, long expiraEmMinutos) {
    }

    public record ConfirmarRequest(
            @NotBlank(message = "O token é obrigatório")
            @Size(max = 200, message = "Token inválido")
            String token) {
    }

    public record LoginRequest(
            @NotBlank(message = "O e-mail é obrigatório")
            @Email(message = "E-mail inválido")
            String email,

            @NotBlank(message = "A senha é obrigatória")
            String senha) {
    }

    public record TokenResponse(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            UsuarioDto.Response usuario) {
    }
}
