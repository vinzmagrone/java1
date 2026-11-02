package it.amdani.school;

import java.nio.file.Path;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

/** Adatta l'esempio SMTP del docente, senza password scritte nel sorgente. */
public class SendMail {
    /** Legge una configurazione obbligatoria senza mostrare le credenziali. */
    private static String variabile(String nome) {
        String valore = System.getenv(nome);
        if (valore == null || valore.isBlank()) {
            throw new IllegalStateException("Configurare la variabile " + nome + " prima dell'invio.");
        }
        return valore;
    }

    /** Costruisce il messaggio separatamente dal trasporto, così è verificabile offline. */
    public static MimeMessage prepara(Session sessione, String mittente, Scontrino scontrino, Path file) throws Exception {
        Scontrino.validaEmail(mittente);
        MimeMessage messaggio = new MimeMessage(sessione);
        messaggio.setFrom(new InternetAddress(mittente));
        messaggio.setRecipient(Message.RecipientType.TO, new InternetAddress(scontrino.getEmail()));
        messaggio.setSubject("Scontrino n. " + scontrino.getNumero() + " - simulazione didattica", "UTF-8");
        MimeBodyPart corpo = new MimeBodyPart();
        corpo.setText(scontrino.testo(), "UTF-8");
        MimeBodyPart allegato = new MimeBodyPart();
        allegato.attachFile(file.toFile());
        Multipart parti = new MimeMultipart();
        parti.addBodyPart(corpo);
        parti.addBodyPart(allegato);
        messaggio.setContent(parti);
        messaggio.saveChanges();
        return messaggio;
    }

    /** Richiede autenticazione, STARTTLS e verifica dell'identità del server. */
    public void invia(Scontrino scontrino, Path file) throws Exception {
        // Server Gmail richiesto per il progetto: SMTP con STARTTLS sulla porta 587.
        String host = "smtp.gmail.com";
        String utente = variabile("SMTP_USER");
        String password = variabile("SMTP_PASSWORD");
        // Per Gmail il mittente coincide con l'account che si autentica.
        String mittente = utente;
        String porta = System.getenv("SMTP_PORT");
        if (porta == null || porta.isBlank()) {
            porta = "587";
        }
        int numeroPorta = Integer.parseInt(porta);
        if (numeroPorta < 1 || numeroPorta > 65535) {
            throw new IllegalArgumentException("Porta SMTP non valida.");
        }
        Properties proprieta = new Properties();
        proprieta.setProperty("mail.smtp.host", host);
        proprieta.setProperty("mail.smtp.port", porta);
        proprieta.setProperty("mail.smtp.auth", "true");
        proprieta.setProperty("mail.smtp.starttls.enable", "true");
        proprieta.setProperty("mail.smtp.starttls.required", "true");
        proprieta.setProperty("mail.smtp.ssl.checkserveridentity", "true");
        proprieta.setProperty("mail.smtp.connectiontimeout", "10000");
        proprieta.setProperty("mail.smtp.timeout", "10000");
        proprieta.setProperty("mail.smtp.writetimeout", "10000");
        Session sessione = Session.getInstance(proprieta, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(utente, password);
            }
        });
        Transport.send(prepara(sessione, mittente, scontrino, file));
    }
}
