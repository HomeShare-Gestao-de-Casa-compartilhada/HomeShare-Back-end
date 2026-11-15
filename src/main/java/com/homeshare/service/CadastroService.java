package com.homeshare.service;

import com.homeshare.config.AppProperties;
import com.homeshare.dto.AuthDto;
import com.homeshare.dto.UsuarioDto;
import com.homeshare.email.EmailService;
import com.homeshare.entity.Usuario;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

/**
 * Cadastro em duas etapas, com o Redis no meio:
 *
 * 1) register: valida o DTO, faz hash da senha (BCrypt) e guarda o cadastro PENDENTE no Redis
 *    (chave = SHA-256 do token, TTL curto). O token vai por e-mail. Nada vai para o banco ainda.
 * 2) confirm: o usuário devolve o token; o cadastro é lido e apagado do Redis numa operação
 *    atômica (GETDEL, uso único) e só então o usuário é gravado no PostgreSQL.
 *
 * Chaves no Redis:
 *   homeshare:cadastro:token:{sha256(token)}  -> JSON do cadastro pendente
 *   homeshare:cadastro:email:{email}          -> chave do token atual (invalida o anterior)
 *   homeshare:cadastro:cooldown:{email}       -> trava anti-spam de e-mails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CadastroService {

    private static final String PREFIXO_TOKEN = "homeshare:cadastro:token:";
    private static final String PREFIXO_EMAIL = "homeshare:cadastro:email:";
    private static final String PREFIXO_COOLDOWN = "homeshare:cadastro:cooldown:";
    private static final int MAX_BYTES_SENHA_BCRYPT = 72;
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final StringRedisTemplate redis;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AppProperties props;
    private final Clock clock;

    private final SecureRandom secureRandom = new SecureRandom();

    /** Dados que ficam no Redis até a confirmação. A senha já está em hash. */
    public record CadastroPendente(String nome, String email, String senhaHash) {
    }

    public AuthDto.RegistroResponse registrar(AuthDto.RegistroRequest req) {
        String email = normalizarEmail(req.email());
        validarTamanhoSenha(req.senha());

        if (usuarioRepository.existsByEmail(email)) {
            throw BusinessException.conflict("Este e-mail já está cadastrado.");
        }

        // Trava anti-spam: um cadastro por e-mail a cada N segundos.
        String cooldownKey = PREFIXO_COOLDOWN + email;
        Boolean liberado = redis.opsForValue().setIfAbsent(
                cooldownKey, "1", Duration.ofSeconds(props.cadastro().cooldownSeconds()));
        if (!Boolean.TRUE.equals(liberado)) {
            throw BusinessException.tooManyRequests(
                    "Aguarde alguns instantes antes de pedir um novo cadastro para este e-mail.");
        }

        Duration ttl = Duration.ofMinutes(props.cadastro().tokenTtlMinutes());
        String token = gerarToken();
        String tokenKey = PREFIXO_TOKEN + sha256(token);
        String emailKey = PREFIXO_EMAIL + email;

        try {
            // Um novo pedido invalida o token anterior do mesmo e-mail.
            String tokenKeyAnterior = redis.opsForValue().get(emailKey);
            if (tokenKeyAnterior != null) {
                redis.delete(tokenKeyAnterior);
            }

            CadastroPendente pendente = new CadastroPendente(
                    req.nome().trim(), email, passwordEncoder.encode(req.senha()));
            redis.opsForValue().set(tokenKey, JSON.writeValueAsString(pendente), ttl);
            redis.opsForValue().set(emailKey, tokenKey, ttl);

            emailService.enviarConfirmacaoCadastro(pendente.nome(), email, token);
        } catch (RuntimeException e) {
            // Se algo falhar (ex.: e-mail não saiu), desfaz tudo para o usuário poder tentar de novo.
            redis.delete(List.of(tokenKey, emailKey, cooldownKey));
            throw e;
        }

        return new AuthDto.RegistroResponse(
                "Enviamos um e-mail com o código de confirmação.", props.cadastro().tokenTtlMinutes());
    }

    public UsuarioDto.Response confirmar(AuthDto.ConfirmarRequest req) {
        String tokenKey = PREFIXO_TOKEN + sha256(req.token().trim());

        // GETDEL: lê e apaga ao mesmo tempo. Dois pedidos com o mesmo token nunca passam juntos.
        String json = redis.opsForValue().getAndDelete(tokenKey);
        if (json == null) {
            throw BusinessException.gone("Token inválido ou expirado. Faça o cadastro novamente.");
        }
        CadastroPendente pendente = JSON.readValue(json, CadastroPendente.class);

        String emailKey = PREFIXO_EMAIL + pendente.email();
        if (tokenKey.equals(redis.opsForValue().get(emailKey))) {
            redis.delete(emailKey);
        }

        if (usuarioRepository.existsByEmail(pendente.email())) {
            throw BusinessException.conflict("Este e-mail já está cadastrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(pendente.nome());
        usuario.setEmail(pendente.email());
        usuario.setSenhaHash(pendente.senhaHash());
        usuario.setDataCadastro(LocalDateTime.now(clock));

        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            throw BusinessException.conflict("Este e-mail já está cadastrado.");
        }
        log.info("Usuário {} confirmado e gravado no banco.", usuario.getId());
        return UsuarioDto.Response.de(usuario);
    }

    private void validarTamanhoSenha(String senha) {
        // O BCrypt só considera os primeiros 72 BYTES (acentos ocupam mais de 1 byte).
        if (senha.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES_SENHA_BCRYPT) {
            throw BusinessException.badRequest("A senha é muito longa (máximo de 72 bytes).");
        }
    }

    static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    /** 32 bytes aleatórios criptograficamente seguros, em Base64 URL-safe (43 caracteres). */
    private String gerarToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /** O Redis guarda só o hash do token: quem ler o Redis não consegue confirmar cadastros. */
    private static String sha256(String valor) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(valor.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 indisponível", e);
        }
    }
}
