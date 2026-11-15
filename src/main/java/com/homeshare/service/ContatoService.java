package com.homeshare.service;

import com.homeshare.dto.ContatoDto;
import com.homeshare.entity.Contato;
import com.homeshare.entity.Membro;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.ContatoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Contatos importantes (dono da casa, encanador, eletricista, emergência...). */
@Service
@RequiredArgsConstructor
public class ContatoService {

    private final ContatoRepository contatoRepository;
    private final CasaAcessoService acesso;

    @Transactional
    public ContatoDto.Response criar(Long casaId, Long usuarioId, ContatoDto.Request req) {
        Membro lider = acesso.exigirLider(casaId, usuarioId);
        Contato contato = new Contato();
        contato.setCasa(lider.getCasa());
        preencher(contato, req);
        contatoRepository.save(contato);
        return ContatoDto.Response.de(contato);
    }

    @Transactional(readOnly = true)
    public List<ContatoDto.Response> listar(Long casaId, Long usuarioId) {
        acesso.exigirMembroAtivo(casaId, usuarioId);
        return contatoRepository.findByCasaIdOrderByNomeAsc(casaId).stream()
                .map(ContatoDto.Response::de)
                .toList();
    }

    @Transactional
    public ContatoDto.Response atualizar(Long casaId, Long usuarioId, Long contatoId, ContatoDto.Request req) {
        acesso.exigirLider(casaId, usuarioId);
        Contato contato = buscar(contatoId, casaId);
        preencher(contato, req);
        return ContatoDto.Response.de(contato);
    }

    @Transactional
    public void excluir(Long casaId, Long usuarioId, Long contatoId) {
        acesso.exigirLider(casaId, usuarioId);
        contatoRepository.delete(buscar(contatoId, casaId));
    }

    private Contato buscar(Long contatoId, Long casaId) {
        return contatoRepository.findByIdAndCasaId(contatoId, casaId)
                .orElseThrow(() -> BusinessException.notFound("Contato não encontrado."));
    }

    private void preencher(Contato contato, ContatoDto.Request req) {
        contato.setNome(req.nome().trim());
        contato.setFuncao(req.funcao().trim());
        contato.setTelefone(req.telefone().trim());
    }
}
