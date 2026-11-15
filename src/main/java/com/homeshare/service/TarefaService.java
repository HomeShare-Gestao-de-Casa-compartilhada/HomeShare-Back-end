package com.homeshare.service;

import com.homeshare.dto.PageResponse;
import com.homeshare.dto.TarefaDto;
import com.homeshare.entity.Membro;
import com.homeshare.entity.Tarefa;
import com.homeshare.enums.Prioridade;
import com.homeshare.enums.StatusTarefa;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.TarefaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final CasaAcessoService acesso;
    private final Clock clock;

    /** Qualquer morador cria tarefas para si; atribuir a OUTRO morador é papel do líder. */
    @Transactional
    public TarefaDto.Response criar(Long casaId, Long usuarioId, TarefaDto.Request req) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        LocalDateTime agora = LocalDateTime.now(clock);

        Membro responsavel = ator;
        if (req.responsavelId() != null && !req.responsavelId().equals(ator.getId())) {
            if (!acesso.ehLider(ator)) {
                throw BusinessException.forbidden("Apenas o líder pode atribuir tarefas a outros moradores.");
            }
            responsavel = acesso.buscarMembroAtivoDaCasa(req.responsavelId(), casaId);
        }
        validarPrazoFuturo(req.prazo(), agora);

        Tarefa tarefa = new Tarefa();
        tarefa.setTitulo(req.titulo().trim());
        tarefa.setDescricao(textoOuVazio(req.descricao()));
        tarefa.setPrazo(req.prazo());
        tarefa.setPrioridade(req.prioridade() != null ? req.prioridade() : Prioridade.NORMAL);
        tarefa.setStatus(StatusTarefa.PENDENTE);
        tarefa.setDataCriacao(agora);
        tarefa.setResponsavel(responsavel);
        tarefa.setCasa(ator.getCasa());
        tarefaRepository.save(tarefa);

        return TarefaDto.Response.de(tarefa, agora);
    }

    @Transactional(readOnly = true)
    public List<TarefaDto.Response> listar(Long casaId, Long usuarioId, StatusTarefa status,
                                           Long responsavelId, Prioridade prioridade) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        LocalDateTime agora = LocalDateTime.now(clock);
        return tarefaRepository.filtrar(casaId, status, responsavelId, prioridade).stream()
                .map(t -> TarefaDto.Response.de(t, agora))
                .toList();
    }

    /** A "tela só dele": tarefas do morador logado (por padrão, as pendentes). */
    @Transactional(readOnly = true)
    public List<TarefaDto.Response> minhas(Long casaId, Long usuarioId, StatusTarefa status) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        StatusTarefa filtro = status != null ? status : StatusTarefa.PENDENTE;
        LocalDateTime agora = LocalDateTime.now(clock);
        return tarefaRepository.filtrar(casaId, filtro, ator.getId(), null).stream()
                .map(t -> TarefaDto.Response.de(t, agora))
                .toList();
    }

    /** Histórico: quem fez o quê e quando (tarefas concluídas, mais recentes primeiro). */
    @Transactional(readOnly = true)
    public PageResponse<TarefaDto.Response> historico(Long casaId, Long usuarioId, int pagina, int tamanho) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        PageRequest pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), 100),
                Sort.by(Sort.Direction.DESC, "dataConclusao"));
        Page<Tarefa> page = tarefaRepository.findByCasaIdAndStatus(casaId, StatusTarefa.CONCLUIDA, pageable);
        LocalDateTime agora = LocalDateTime.now(clock);
        return PageResponse.de(page, t -> TarefaDto.Response.de(t, agora));
    }

    @Transactional
    public TarefaDto.Response atualizar(Long casaId, Long usuarioId, Long tarefaId, TarefaDto.Request req) {
        acesso.exigirLider(casaId, usuarioId);
        LocalDateTime agora = LocalDateTime.now(clock);
        Tarefa tarefa = buscar(tarefaId, casaId);

        if (tarefa.getStatus() != StatusTarefa.PENDENTE) {
            throw BusinessException.unprocessable("Apenas tarefas pendentes podem ser editadas.");
        }
        if (!req.prazo().equals(tarefa.getPrazo())) {
            validarPrazoFuturo(req.prazo(), agora);
        }
        if (req.responsavelId() != null && !req.responsavelId().equals(tarefa.getResponsavel().getId())) {
            tarefa.setResponsavel(acesso.buscarMembroAtivoDaCasa(req.responsavelId(), casaId));
        }
        tarefa.setTitulo(req.titulo().trim());
        tarefa.setDescricao(textoOuVazio(req.descricao()));
        tarefa.setPrazo(req.prazo());
        if (req.prioridade() != null) {
            tarefa.setPrioridade(req.prioridade());
        }
        return TarefaDto.Response.de(tarefa, agora);
    }

    /** O morador só conclui a própria tarefa; o líder pode concluir a de qualquer um. */
    @Transactional
    public TarefaDto.Response concluir(Long casaId, Long usuarioId, Long tarefaId) {
        Membro ator = acesso.exigirMembroAtivo(casaId, usuarioId);
        Tarefa tarefa = buscar(tarefaId, casaId);

        if (tarefa.getStatus() != StatusTarefa.PENDENTE) {
            throw BusinessException.unprocessable("Só é possível concluir tarefas pendentes.");
        }
        boolean dono = tarefa.getResponsavel().getId().equals(ator.getId());
        if (!dono && !acesso.ehLider(ator)) {
            throw BusinessException.forbidden("Você só pode marcar como feitas as suas próprias tarefas.");
        }
        LocalDateTime agora = LocalDateTime.now(clock);
        tarefa.setStatus(StatusTarefa.CONCLUIDA);
        tarefa.setDataConclusao(agora);
        tarefa.setConcluidoPor(ator);
        return TarefaDto.Response.de(tarefa, agora);
    }

    /** "Apagar" = cancelar (exclusão lógica). Tarefas concluídas ficam no histórico. */
    @Transactional
    public void cancelar(Long casaId, Long usuarioId, Long tarefaId) {
        acesso.exigirLider(casaId, usuarioId);
        Tarefa tarefa = buscar(tarefaId, casaId);
        if (tarefa.getStatus() == StatusTarefa.CONCLUIDA) {
            throw BusinessException.unprocessable(
                    "Tarefas concluídas fazem parte do histórico e não podem ser apagadas.");
        }
        tarefa.setStatus(StatusTarefa.CANCELADA);
    }

    private Tarefa buscar(Long tarefaId, Long casaId) {
        return tarefaRepository.findByIdAndCasaId(tarefaId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Tarefa não encontrada."));
    }

    private void validarPrazoFuturo(LocalDateTime prazo, LocalDateTime agora) {
        if (!prazo.isAfter(agora)) {
            throw BusinessException.badRequest("O prazo precisa ser uma data futura.");
        }
    }

    private String textoOuVazio(String texto) {
        return texto == null ? "" : texto.trim();
    }
}
