package com.homeshare.security;

import com.homeshare.exception.BusinessException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /** Id do usuário logado (o "sub" do JWT). Use nos controllers: SecurityUtils.usuarioId(). */
    public static Long usuarioId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw BusinessException.unauthorized("Não autenticado.");
        }
        try {
            return Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw BusinessException.unauthorized("Token inválido.");
        }
    }
}
