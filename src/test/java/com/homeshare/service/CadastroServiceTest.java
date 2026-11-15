package com.homeshare.service;

import com.homeshare.config.AppProperties;
import com.homeshare.dto.AuthDto;
import com.homeshare.email.EmailService;
import com.homeshare.entity.Usuario;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Fluxo do Redis: registro guarda o pendente e envia o token; confirmação grava no banco só com token válido. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CadastroServiceTest {

    @Mock StringRedisTemplate redis;
    @Mock ValueOperations<String, String> valueOps;
    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock EmailService emailService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneId.of("UTC"));
    private CadastroService service;

    @BeforeEach
    void setUp() {
        AppProperties props = new AppProperties(
                "America/Sao_Paulo", "http://localhost:3000",
                new AppProperties.Jwt("x".repeat(40), "homeshare-api", 120),
                new AppProperties.Cadastro(15, 60),
                new AppProperties.Convite(48, 720),
                new AppProperties.Cors(List.of("http://localhost:3000")),
                new AppProperties.Mail("log", null, "https://api.resend.com", "x@y.z", null));

        when(redis.opsForValue()).thenReturn(valueOps);
        when(passwordEncoder.encode(anyString())).thenReturn("HASH");
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);

        service = new CadastroService(redis, usuarioRepository, passwordEncoder, emailService, props, clock);
    }

    @Test
    void registroGuardaNoRedisEEnviaTokenSemTocarNoBanco() {
        when(usuarioRepository.existsByEmail("ana@teste.com")).thenReturn(false);

        service.registrar(new AuthDto.RegistroRequest("Ana", "  ANA@teste.com ", "senha-segura-123"));

        verify(valueOps).set(anyString(), anyString(), any(Duration.class));
        ArgumentCaptor<String> token = ArgumentCaptor.forClass(String.class);
        verify(emailService).enviarConfirmacaoCadastro(eq("Ana"), eq("ana@teste.com"), token.capture());
        assertThat(token.getValue()).hasSizeGreaterThanOrEqualTo(40);
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void registroComEmailJaCadastradoRetorna409() {
        when(usuarioRepository.existsByEmail("ana@teste.com")).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(
                new AuthDto.RegistroRequest("Ana", "ana@teste.com", "senha-segura-123")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.CONFLICT));
        verify(emailService, never()).enviarConfirmacaoCadastro(anyString(), anyString(), anyString());
    }

    @Test
    void registroRepetidoDentroDoCooldownRetorna429() {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);

        assertThatThrownBy(() -> service.registrar(
                new AuthDto.RegistroRequest("Ana", "ana@teste.com", "senha-segura-123")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus())
                        .isEqualTo(HttpStatus.TOO_MANY_REQUESTS));
    }

    @Test
    void confirmacaoComTokenDesconhecidoRetorna410() {
        when(valueOps.getAndDelete(anyString())).thenReturn(null);

        assertThatThrownBy(() -> service.confirmar(new AuthDto.ConfirmarRequest("token-invalido")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus()).isEqualTo(HttpStatus.GONE));
        verify(usuarioRepository, never()).saveAndFlush(any());
    }

    @Test
    void confirmacaoValidaGravaUsuarioNoBanco() {
        String json = "{\"nome\":\"Ana\",\"email\":\"ana@teste.com\",\"senhaHash\":\"HASH\"}";
        when(valueOps.getAndDelete(anyString())).thenReturn(json);
        when(usuarioRepository.existsByEmail("ana@teste.com")).thenReturn(false);
        when(usuarioRepository.saveAndFlush(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        var resposta = service.confirmar(new AuthDto.ConfirmarRequest("token-valido"));

        ArgumentCaptor<Usuario> salvo = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).saveAndFlush(salvo.capture());
        assertThat(salvo.getValue().getEmail()).isEqualTo("ana@teste.com");
        assertThat(salvo.getValue().getSenhaHash()).isEqualTo("HASH");
        assertThat(resposta.id()).isEqualTo(1L);
    }
}
