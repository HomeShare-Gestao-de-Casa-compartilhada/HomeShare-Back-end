package com.homeshare.controller;

import com.homeshare.dto.UsuarioDto;
import com.homeshare.security.SecurityUtils;
import com.homeshare.service.UsuarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @GetMapping("/me")
    public UsuarioDto.Response me() {
        return usuarioService.perfil(SecurityUtils.usuarioId());
    }
}
