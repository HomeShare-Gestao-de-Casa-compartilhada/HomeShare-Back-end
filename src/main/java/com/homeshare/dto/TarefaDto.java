package com.homeshare.dto;

import com.homeshare.entity.Tarefa;
import com.homeshare.enums.Prioridade;
import com.homeshare.enums.StatusTarefa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public final class TarefaDto {

    private TarefaDto() {
    }

    /**
     * Criar e editar usam o mesmo corpo.
     * responsavelId: se omitido, vale o próprio usuário (criar) ou mantém o atual (editar).
     * prioridade: se omitida, vale NORMAL (criar) ou mantém a atual (editar).
     */
    public record Request(
            @NotBlank(message = "O título é obrigatório")
            @Size(max = 100, message = "O título deve ter no máximo 100 caracteres")
            String titulo,

            @Size(max = 500, message = "A descrição deve ter no máximo 500 caracteres")
            String descricao,

            @NotNull(message = "O prazo é obrigatório")
            LocalDateTime prazo,

            Prioridade prioridade,

            Long responsavelId) {
    }

    public record Response(
            Long id,
            String titulo,
            String descricao,
            LocalDateTime prazo,
            Prioridade prioridade,
            StatusTarefa status,
            boolean atrasada,
            LocalDateTime dataCriacao,
            LocalDateTime dataConclusao,
            MembroDto.Resumo responsavel,
            MembroDto.Resumo concluidoPor) {

        public static Response de(Tarefa t, LocalDateTime agora) {
            boolean atrasada = t.getStatus() == StatusTarefa.PENDENTE && t.getPrazo().isBefore(agora);
            return new Response(t.getId(), t.getTitulo(), t.getDescricao(), t.getPrazo(), t.getPrioridade(),
                    t.getStatus(), atrasada, t.getDataCriacao(), t.getDataConclusao(),
                    MembroDto.Resumo.de(t.getResponsavel()), MembroDto.Resumo.de(t.getConcluidoPor()));
        }
    }
}
