package com.homeshare.service;

import com.homeshare.dto.UsuarioDto;
import com.homeshare.exception.BusinessException;
import com.homeshare.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public UsuarioDto.Response perfil(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(UsuarioDto.Response::de)
                .orElseThrow(() -> BusinessException.unauthorized("Usuário não encontrado."));
    }
}
