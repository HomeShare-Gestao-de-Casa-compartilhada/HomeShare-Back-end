package com.homeshare.security;

import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Escreve um ProblemDetail simples direto na resposta (usado fora dos controllers, nos filtros). */
final class ProblemJson {

    private ProblemJson() {
    }

    static void escrever(HttpServletResponse response, int status, String titulo, String detalhe)
            throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/problem+json");
        // Os textos são constantes (sem aspas), então não precisa escapar.
        response.getWriter().write(
                "{\"title\":\"" + titulo + "\",\"status\":" + status + ",\"detail\":\"" + detalhe + "\"}");
    }
}
