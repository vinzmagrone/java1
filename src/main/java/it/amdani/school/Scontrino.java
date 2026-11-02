package it.amdani.school;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Vector;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.AddressException;

/** Documento salvabile con la serializzazione mostrata dal docente. */
public class Scontrino implements Serializable {
    private static final long serialVersionUID = 1L;
    private final int numero;
    private final LocalDateTime data;
    private final String negozio;
    private final String partitaIva;
    private final String email;
    private final Vector<RigaAcquisto> righe;
    private boolean inviato;

    /** Copia le righe per evitare che il carrello modifichi documenti già emessi. */
    public Scontrino(int numero, String negozio, String partitaIva, String email, Vector<RigaAcquisto> righe) {
        if (numero <= 0 || negozio == null || negozio.isBlank() || negozio.contains("\n") || negozio.contains("\r")) {
            throw new IllegalArgumentException("Numero e nome negozio non validi.");
        }
        if (partitaIva == null || !partitaIva.matches("[0-9]{11}")) {
            throw new IllegalArgumentException("La partita IVA deve contenere 11 cifre.");
        }
        validaEmail(email);
        if (righe == null || righe.isEmpty() || righe.contains(null)) {
            throw new IllegalArgumentException("Lo scontrino deve contenere almeno una merce.");
        }
        this.numero = numero;
        this.negozio = negozio;
        this.partitaIva = partitaIva;
        this.email = email;
        this.righe = new Vector<>(righe);
        this.data = LocalDateTime.now();
    }

    /** JavaMail controlla la sintassi; non garantisce che la casella esista. */
    public static void validaEmail(String email) {
        try {
            if (email == null || email.isBlank() || email.contains("\r") || email.contains("\n")) {
                throw new IllegalArgumentException("Email non valida.");
            }
            InternetAddress indirizzo = new InternetAddress(email, true);
            indirizzo.validate();
            if (!indirizzo.getAddress().contains("@") || !indirizzo.getAddress().equals(email)) {
                throw new IllegalArgumentException("Inserire solo l'indirizzo email del cliente.");
            }
        } catch (AddressException errore) {
            throw new IllegalArgumentException("Email non valida.");
        }
    }

    /** Somma degli importi lordi delle merci. */
    public BigDecimal totale() {
        BigDecimal somma = new BigDecimal("0.00");
        for (RigaAcquisto riga : righe) {
            somma = somma.add(riga.totale());
        }
        return somma;
    }

    /** Somma dell'IVA scorporata e arrotondata per ciascuna riga. */
    public BigDecimal iva() {
        BigDecimal somma = new BigDecimal("0.00");
        for (RigaAcquisto riga : righe) {
            somma = somma.add(riga.iva());
        }
        return somma;
    }

    /** Testo comune alla visualizzazione, al file e al corpo della mail. */
    public String testo() {
        StringBuilder testo = new StringBuilder();
        testo.append("SIMULAZIONE DIDATTICA - NON VALIDO AI FINI FISCALI\n");
        testo.append(negozio).append("\nP. IVA: ").append(partitaIva);
        testo.append("\nScontrino n. ").append(numero);
        testo.append("\nData: ").append(data.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        testo.append("\nCliente: ").append(email).append("\n\n");
        for (RigaAcquisto riga : righe) {
            testo.append(riga.testo()).append("\n");
        }
        testo.append("\nImponibile: ").append(totale().subtract(iva()).toPlainString()).append(" EUR");
        testo.append("\nIVA inclusa: ").append(iva().toPlainString()).append(" EUR");
        testo.append("\nTOTALE: ").append(totale().toPlainString()).append(" EUR\n");
        testo.append("Grazie per il tuo acquisto!\n");
        return testo.toString();
    }

    /** Dati necessari al menu e all'invio. */
    public int getNumero() { return numero; }
    public String getEmail() { return email; }
    public boolean isInviato() { return inviato; }
    /** Si aggiorna solo dopo che il server SMTP ha accettato il messaggio. */
    public void setInviato(boolean inviato) { this.inviato = inviato; }
}
