package com.homeshare.email;

public interface EmailService {

    /** Envia o e-mail com o link/token para confirmar o cadastro. */
    void enviarConfirmacaoCadastro(String nome, String emailDestino, String token);
}
