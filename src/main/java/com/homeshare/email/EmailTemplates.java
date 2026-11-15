package com.homeshare.email;

import org.springframework.web.util.HtmlUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

final class EmailTemplates {

    private EmailTemplates() {
    }

    static String linkConfirmacao(String frontendUrl, String token) {
        String base = frontendUrl.endsWith("/") ? frontendUrl.substring(0, frontendUrl.length() - 1) : frontendUrl;
        return base + "/confirmar-cadastro?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
    }

    static String confirmacaoCadastro(String nome, String link, String token, long validadeMinutos) {
        return """
                <div style="font-family:Arial,sans-serif;max-width:480px;margin:auto;color:#222">
                  <h2 style="color:#2f6fed">Bem-vindo(a) ao HomeShare, %s!</h2>
                  <p>Para concluir seu cadastro, clique no botão abaixo:</p>
                  <p><a href="%s" style="background:#2f6fed;color:#fff;padding:12px 20px;border-radius:6px;text-decoration:none;display:inline-block">Confirmar cadastro</a></p>
                  <p>Se preferir, use este código de confirmação:</p>
                  <p style="font-family:monospace;font-size:14px;background:#f3f3f3;padding:10px;border-radius:6px;word-break:break-all">%s</p>
                  <p style="color:#666;font-size:13px">O código expira em %d minutos e só pode ser usado uma vez.
                  Se você não pediu este cadastro, ignore este e-mail.</p>
                </div>
                """.formatted(HtmlUtils.htmlEscape(nome), HtmlUtils.htmlEscape(link),
                HtmlUtils.htmlEscape(token), validadeMinutos);
    }
}
