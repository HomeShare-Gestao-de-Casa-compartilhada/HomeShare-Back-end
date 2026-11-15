package com.homeshare.email;

import com.homeshare.config.AppProperties;
import com.homeshare.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Envia e-mails pela API REST do Resend (POST /emails).
 * Ativo quando app.mail.provider = resend (padrão).
 *
 * Enquanto você não tem domínio verificado no Resend, ele só entrega para o e-mail
 * dono da conta. Por isso existe app.mail.override-to: se preenchido, TODO e-mail
 * é redirecionado para esse endereço (o destinatário real aparece no assunto).
 */
@Service
@ConditionalOnProperty(prefix = "app.mail", name = "provider", havingValue = "resend")
@Slf4j
public class ResendEmailService implements EmailService {

    private final AppProperties props;
    private final RestClient restClient;

    public ResendEmailService(AppProperties props) {
        this.props = props;

        String apiKey = props.mail().apiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "RESEND_API_KEY não configurada. Defina no .env ou use MAIL_PROVIDER=log para testar sem enviar.");
        }

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(10));

        this.restClient = RestClient.builder()
                .baseUrl(props.mail().apiUrl())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.USER_AGENT, "homeshare-api/1.0")
                .build();
    }

    @Override
    public void enviarConfirmacaoCadastro(String nome, String emailDestino, String token) {
        String destino = destinoEfetivo(emailDestino);
        boolean redirecionado = !destino.equalsIgnoreCase(emailDestino);

        String assunto = redirecionado
                ? "[DEV - para: " + emailDestino + "] Confirme seu cadastro no HomeShare"
                : "Confirme seu cadastro no HomeShare";

        String link = EmailTemplates.linkConfirmacao(props.frontendUrl(), token);
        String html = EmailTemplates.confirmacaoCadastro(nome, link, token, props.cadastro().tokenTtlMinutes());

        Map<String, Object> corpo = Map.of(
                "from", props.mail().from(),
                "to", List.of(destino),
                "subject", assunto,
                "html", html);

        try {
            restClient.post()
                    .uri("/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(corpo)
                    .retrieve()
                    .toBodilessEntity();
            log.info("E-mail de confirmação enviado (redirecionado={}).", redirecionado);
        } catch (RestClientResponseException e) {
            log.error("Resend recusou o envio. status={} corpo={}",
                    e.getStatusCode().value(), e.getResponseBodyAsString());
            throw BusinessException.badGateway("Não foi possível enviar o e-mail de confirmação. Tente novamente.");
        } catch (RestClientException e) {
            log.error("Falha de comunicação com o Resend: {}", e.getMessage());
            throw BusinessException.badGateway("Não foi possível enviar o e-mail de confirmação. Tente novamente.");
        }
    }

    private String destinoEfetivo(String emailOriginal) {
        String override = props.mail().overrideTo();
        return (override != null && !override.isBlank()) ? override.trim() : emailOriginal;
    }
}
