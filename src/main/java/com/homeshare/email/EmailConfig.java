package com.homeshare.email;

import com.homeshare.config.AppProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Escolhe a implementação de e-mail pelo valor de MAIL_PROVIDER:
 * "log" imprime o token no console; qualquer outro valor usa o Resend.
 * Sempre existe um EmailService, então a aplicação nunca falha por falta do bean.
 */
@Configuration
public class EmailConfig {

    @Bean
    public EmailService emailService(AppProperties props) {
        String provider = props.mail().provider().trim();
        if (provider.equalsIgnoreCase("log")) {
            return new LogEmailService(props);
        }
        return new ResendEmailService(props);
    }
}
