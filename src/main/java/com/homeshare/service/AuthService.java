package com.homeshare.service;

import com.homeshare.dto.AuthDto;
import com.homeshare.dto.UsuarioDto;
import com.homeshare.entity.Usuario;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.UsuarioRepository;
import com.homeshare.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** Hash de uma senha qualquer: usado para gastar o mesmo tempo quando o e-mail não existe. */
    private final String hashFalso;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.hashFalso = passwordEncoder.encode("senha-falsa-para-igualar-o-tempo");
    }

    @Transactional(readOnly = true)
    public AuthDto.TokenResponse login(AuthDto.LoginRequest req) {
        String email = CadastroService.normalizarEmail(req.email());
        Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);

        boolean senhaValida = req.senha().getBytes(StandardCharsets.UTF_8).length <= 72
                && passwordEncoder.matches(req.senha(), usuario != null ? usuario.getSenhaHash() : hashFalso);

        // Mesma mensagem para "e-mail não existe" e "senha errada": não revela quais e-mails estão cadastrados.
        if (usuario == null || !senhaValida) {
            throw BusinessException.unauthorized("E-mail ou senha inválidos.");
        }

        return new AuthDto.TokenResponse(
                jwtService.gerarToken(usuario),
                "Bearer",
                jwtService.expiracaoEmSegundos(),
                UsuarioDto.Response.de(usuario));
    }
}
