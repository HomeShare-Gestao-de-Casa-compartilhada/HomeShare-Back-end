package com.homeshare.service;

import com.homeshare.dto.CasaDto;
import com.homeshare.entity.Casa;
import com.homeshare.entity.Membro;
import com.homeshare.entity.Usuario;
import com.homeshare.enums.StatusCasa;
import com.homeshare.enums.StatusMembro;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.CasaRepository;
import com.homeshare.repository.MembroRepository;
import com.homeshare.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/** Regras de liderança do escopo: líder sai -> mais antigo assume; sem ninguém -> casa encerrada. */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CasaServiceTest {

    private static final Long CASA_ID = 1L;

    @Mock CasaRepository casaRepository;
    @Mock MembroRepository membroRepository;
    @Mock UsuarioRepository usuarioRepository;
    @Mock CasaAcessoService acesso;

    private final Clock clock = Clock.fixed(Instant.parse("2026-11-15T12:00:00Z"), ZoneId.of("UTC"));
    private CasaService service;

    private Usuario lider;
    private Usuario outro;
    private Casa casa;
    private Membro membroLider;
    private Membro membroOutro;

    @BeforeEach
    void setUp() {
        service = new CasaService(casaRepository, membroRepository, usuarioRepository, acesso, clock);

        lider = usuario(10L, "Lider");
        outro = usuario(20L, "Outro");

        casa = new Casa();
        casa.setId(CASA_ID);
        casa.setNome("Republica");
        casa.setCriador(lider);
        casa.setLider(lider);
        casa.setStatus(StatusCasa.ATIVA);
        casa.setDataCriacao(LocalDateTime.of(2026, 1, 1, 0, 0));

        membroLider = membro(100L, lider);
        membroOutro = membro(200L, outro);

        when(casaRepository.findByIdParaAtualizar(CASA_ID)).thenReturn(Optional.of(casa));
    }

    @Test
    void liderQueSaiPassaLiderancaParaMoradorMaisAntigo() {
        when(acesso.exigirMembroAtivo(CASA_ID, 10L)).thenReturn(membroLider);
        when(membroRepository.findFirstByCasaIdAndStatusAndUsuarioIdNotOrderByDataEntradaAscIdAsc(
                CASA_ID, StatusMembro.ATIVO, 10L)).thenReturn(Optional.of(membroOutro));

        service.sair(CASA_ID, 10L);

        assertThat(membroLider.getStatus()).isEqualTo(StatusMembro.SAIU);
        assertThat(membroLider.getDataSaida()).isNotNull();
        assertThat(casa.getLider()).isSameAs(outro);
        assertThat(casa.getStatus()).isEqualTo(StatusCasa.ATIVA);
    }

    @Test
    void ultimoMoradorQueSaiEncerraACasa() {
        when(acesso.exigirMembroAtivo(CASA_ID, 10L)).thenReturn(membroLider);
        when(membroRepository.findFirstByCasaIdAndStatusAndUsuarioIdNotOrderByDataEntradaAscIdAsc(
                anyLong(), any(), anyLong())).thenReturn(Optional.empty());

        service.sair(CASA_ID, 10L);

        assertThat(casa.getStatus()).isEqualTo(StatusCasa.ENCERRADA);
    }

    @Test
    void moradorComumQueSaiNaoMudaOLider() {
        when(acesso.exigirMembroAtivo(CASA_ID, 20L)).thenReturn(membroOutro);

        service.sair(CASA_ID, 20L);

        assertThat(membroOutro.getStatus()).isEqualTo(StatusMembro.SAIU);
        assertThat(casa.getLider()).isSameAs(lider);
        assertThat(casa.getStatus()).isEqualTo(StatusCasa.ATIVA);
    }

    @Test
    void liderNaoPodeExpulsarASiMesmo() {
        when(acesso.exigirLider(CASA_ID, 10L)).thenReturn(membroLider);
        when(acesso.buscarMembroDaCasa(100L, CASA_ID)).thenReturn(membroLider);

        assertThatThrownBy(() -> service.expulsar(CASA_ID, 10L, 100L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getStatus())
                        .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY));
        assertThat(membroLider.getStatus()).isEqualTo(StatusMembro.ATIVO);
    }

    @Test
    void expulsaoMarcaStatusEMantemOHistorico() {
        when(acesso.exigirLider(CASA_ID, 10L)).thenReturn(membroLider);
        when(acesso.buscarMembroDaCasa(200L, CASA_ID)).thenReturn(membroOutro);

        service.expulsar(CASA_ID, 10L, 200L);

        assertThat(membroOutro.getStatus()).isEqualTo(StatusMembro.EXPULSO);
        assertThat(membroOutro.getDataSaida()).isNotNull();
    }

    @Test
    void transferirLiderancaTroca() {
        when(acesso.exigirLider(CASA_ID, 10L)).thenReturn(membroLider);
        when(acesso.buscarMembroDaCasa(200L, CASA_ID)).thenReturn(membroOutro);

        CasaDto.Response resposta = service.transferirLideranca(CASA_ID, 10L, 200L);

        assertThat(casa.getLider()).isSameAs(outro);
        assertThat(resposta.souLider()).isFalse();
    }

    @Test
    void naoTransfereParaQuemNaoEstaAtivo() {
        membroOutro.setStatus(StatusMembro.SAIU);
        when(acesso.exigirLider(CASA_ID, 10L)).thenReturn(membroLider);
        when(acesso.buscarMembroDaCasa(200L, CASA_ID)).thenReturn(membroOutro);

        assertThatThrownBy(() -> service.transferirLideranca(CASA_ID, 10L, 200L))
                .isInstanceOf(BusinessException.class);
        assertThat(casa.getLider()).isSameAs(lider);
    }

    private Usuario usuario(Long id, String nome) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setNome(nome);
        u.setEmail(nome.toLowerCase() + "@teste.com");
        return u;
    }

    private Membro membro(Long id, Usuario usuario) {
        Membro m = new Membro();
        m.setId(id);
        m.setUsuario(usuario);
        m.setCasa(casa);
        m.setStatus(StatusMembro.ATIVO);
        m.setDataEntrada(LocalDateTime.of(2026, 1, 1, 0, 0));
        return m;
    }
}
