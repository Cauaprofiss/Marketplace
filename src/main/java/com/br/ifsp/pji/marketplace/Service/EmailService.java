package com.br.ifsp.pji.marketplace.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Encapsula o envio de e-mails transacionais do sistema.
 * Usa o JavaMailSender configurado via application.properties (spring.mail.*).
 */
@Service
// Encapsula o envio de e-mails transacionais, como os codigos de recuperacao de senha.
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String remetente;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void enviarEmailCodigoRecuperacao(String destinatario, String codigo, int validadeMinutos) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(destinatario);
        mensagem.setSubject("Campus Marketplace - Codigo de recuperacao de senha");
        mensagem.setText(
                "Ola,\n\n" +
                "Recebemos uma solicitacao para redefinir sua senha no Campus Marketplace.\n" +
                "Use o codigo abaixo na tela de redefinicao (valido por " + validadeMinutos + " minutos):\n\n" +
                codigo + "\n\n" +
                "Se voce nao solicitou essa alteracao, ignore este e-mail; sua senha atual continua valida.\n\n" +
                "Equipe Campus Marketplace"
        );
        mailSender.send(mensagem);
    }
}