package com.homeshare.controller;

import com.homeshare.dto.AuthDto;
import com.homeshare.dto.UsuarioDto;
import com.homeshare.service.AuthService;
import com.homeshare.service.CadastroService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CadastroService cadastroService;
    private final AuthService authService;

    /** Etapa 1: guarda o cadastro pendente no Redis e envia o token por e-mail. */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public AuthDto.RegistroResponse register(@Valid @RequestBody AuthDto.RegistroRequest req) {
        return cadastroService.registrar(req);
    }

    /** Etapa 2: com o token correto, o usuário é gravado no banco. */
    @PostMapping("/confirm")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto.Response confirm(@Valid @RequestBody AuthDto.ConfirmarRequest req) {
        return cadastroService.confirmar(req);
    }

    @PostMapping("/login")
    public AuthDto.TokenResponse login(@Valid @RequestBody AuthDto.LoginRequest req) {
        return authService.login(req);
    }
}
