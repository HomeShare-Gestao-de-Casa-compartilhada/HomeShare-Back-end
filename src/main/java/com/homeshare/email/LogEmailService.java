package com.homeshare.email;

import com.homeshare.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Não envia nada: imprime o token no console. Útil para desenvolver sem Resend.
 * Ative com MAIL_PROVIDER=log. NUNCA use em produção (o token vai para o log).
 */
@Service
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "log")
@RequiredArgsConstructor
@Slf4j
public class LogEmailService implements EmailService {

    private final AppProperties props;

    @Override
    public void enviarConfirmacaoCadastro(String nome, String emailDestino, String token) {
        log.warn("[MAIL_PROVIDER=log] Confirmação para {} <{}>. Link: {} | Token: {}",
                nome, emailDestino, EmailTemplates.linkConfirmacao(props.frontendUrl(), token), token);
    }
}
