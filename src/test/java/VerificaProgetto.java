import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.Properties;
import java.util.Vector;
import javax.mail.*;
import javax.mail.internet.MimeMessage;

/** Verifiche eseguibili senza aggiungere JUnit o altre librerie al POM. */
public class VerificaProgetto {
    private static int verifiche;

    /** Un controllo falso deve interrompere l'esecuzione con codice diverso da zero. */
    private static void controlla(boolean condizione, String messaggio) {
        verifiche++;
        if (!condizione) {
            throw new AssertionError(messaggio);
        }
    }

    /** Esegue calcoli, validazioni, persistenza e composizione MIME senza inviare email. */
    public static void main(String[] args) throws Exception {
        Path cartella = Files.createTempDirectory("scontrini-test-");
        try {
            Repository repository = new Repository(cartella);
            controlla(repository.load() == null, "Primo avvio senza archivio");
            Vector<RigaAcquisto> righe = new Vector<>();
            righe.add(new RigaAcquisto("Caffè", 2, new BigDecimal("1.22"), new BigDecimal("22")));
            righe.add(new RigaAcquisto("Pane", 1, new BigDecimal("1.04"), new BigDecimal("4")));
            Scontrino documento = new Scontrino(1, "Negozio prova", "12345678901", "cliente@example.com", righe);
            controlla(documento.totale().equals(new BigDecimal("3.48")), "Totale con quantità e IVA diverse");
            controlla(documento.iva().equals(new BigDecimal("0.48")), "Scorporo IVA");
            righe.clear();
            controlla(documento.totale().equals(new BigDecimal("3.48")), "Copia indipendente del carrello");
            Vector<Scontrino> archivio = new Vector<>();
            archivio.add(documento);
            repository.save(archivio);
            Vector<?> riletti = (Vector<?>) repository.load();
            Scontrino riletto = (Scontrino) riletti.firstElement();
            controlla(riletto.testo().equals(documento.testo()), "Persistenza completa e caratteri accentati");
            controlla(!riletto.isInviato(), "Nuovo documento da inviare");
            riletto.setInviato(true);
            repository.save(riletti);
            controlla(((Scontrino) ((Vector<?>) repository.load()).firstElement()).isInviato(), "Persistenza stato invio");
            Path file = repository.saveTesto(documento);
            controlla(Files.readString(file).equals(documento.testo()), "Copia testuale UTF-8");
            MimeMessage mail = SendMail.prepara(Session.getInstance(new Properties()), "negozio@example.com", documento, file);
            controlla(mail.getAllRecipients()[0].toString().equals("cliente@example.com"), "Destinatario MIME");
            controlla(mail.getSubject().contains("1"), "Oggetto con progressivo");
            Multipart parti = (Multipart) mail.getContent();
            controlla(parti.getCount() == 2, "Corpo e allegato");
            controlla(parti.getBodyPart(0).getContent().equals(documento.testo()), "Corpo della mail");
            controlla(parti.getBodyPart(1).getFileName().equals("scontrino-1.txt"), "Nome allegato");
            try (java.io.InputStream input = parti.getBodyPart(1).getInputStream()) {
                controlla(new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8).equals(documento.testo()), "Contenuto allegato");
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            mail.writeTo(output);
            controlla(output.size() > 0, "Serializzazione messaggio MIME");
            boolean errore = false;
            try { Scontrino.validaEmail("non-una-email"); }
            catch (IllegalArgumentException atteso) { errore = true; }
            controlla(errore, "Email invalida rifiutata");
            errore = false;
            try { new RigaAcquisto("Merce", 0, BigDecimal.ONE, BigDecimal.ZERO); }
            catch (IllegalArgumentException atteso) { errore = true; }
            controlla(errore, "Quantità nulla rifiutata");
            errore = false;
            try { new RigaAcquisto("Merce", 1, new BigDecimal("1.001"), BigDecimal.ZERO); }
            catch (IllegalArgumentException atteso) { errore = true; }
            controlla(errore, "Prezzo con frazioni di centesimo rifiutato");
            errore = false;
            try { new Scontrino(2, "Negozio", "12345678901", "cliente@example.com", new Vector<>()); }
            catch (IllegalArgumentException atteso) { errore = true; }
            controlla(errore, "Carrello vuoto rifiutato");
            Files.writeString(cartella.resolve("scontrini.dat"), "file corrotto");
            errore = false;
            try { repository.load(); }
            catch (java.io.IOException atteso) { errore = true; }
            controlla(errore, "Archivio corrotto segnalato senza azzerarlo");
            System.out.println("SUPERATE " + verifiche + " verifiche.");
        } finally {
            // Pulisce solo la cartella temporanea creata da questa prova.
            try (java.util.stream.Stream<Path> file = Files.list(cartella)) {
                for (Path percorso : file.toList()) {
                    Files.deleteIfExists(percorso);
                }
            }
            Files.deleteIfExists(cartella);
        }
    }
}
