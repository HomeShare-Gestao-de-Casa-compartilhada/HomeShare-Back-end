package com.homeshare.dto.Usuario;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioRequestDto(
        @NotBlank(message = "nome é obrigatório")
    String nome,
    @NotBlank(message = "email é obrigatório")
    @Email
    String email,
    @NotBlank(message = "senha é obrigatória")
    @Size(min = 3, max = 50)
    String senha
) {}
