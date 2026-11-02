package it.amdani.school;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.util.Scanner;
import java.util.Vector;

/** Interfaccia testuale: nessun framework e nessuna libreria aggiuntiva. */
public class Main {
    private final Scanner tastiera = new Scanner(System.in);
    private final Repository repository;
    private final Vector<Scontrino> archivio = new Vector<>();

    /** Carica soltanto file locali fidati prodotti dall'applicazione. */
    public Main(Path cartella) throws Exception {
        repository = new Repository(cartella);
        Object dati = repository.load();
        if (dati != null) {
            if (!(dati instanceof Vector<?>)) {
                throw new IllegalStateException("Formato archivio non valido.");
            }
            for (Object elemento : (Vector<?>) dati) {
                if (!(elemento instanceof Scontrino)) {
                    throw new IllegalStateException("Documento in archivio non valido.");
                }
                archivio.add((Scontrino) elemento);
            }
        }
    }

    /** L'argomento facoltativo permette di usare un archivio separato per le prove. */
    public static void main(String[] args) {
        try {
            Path cartella = Path.of(args.length == 0 ? "dati" : args[0]);
            new Main(cartella).menu();
        } catch (Exception errore) {
            System.err.println("Avvio interrotto: impossibile leggere l'archivio. " + errore.getClass().getSimpleName());
            System.exit(1);
        }
    }

    /** Legge una riga; la chiusura dell'input termina il menu senza un ciclo infinito. */
    private String leggi(String domanda) {
        System.out.print(domanda);
        if (!tastiera.hasNextLine()) {
            throw new java.util.NoSuchElementException();
        }
        return tastiera.nextLine().trim();
    }

    /** Accetta sia il punto sia la virgola come separatore decimale. */
    private BigDecimal decimale(String domanda) {
        return new BigDecimal(leggi(domanda).replace(',', '.'));
    }

    /** Ogni operazione fallita torna al menu e mantiene consultabile l'archivio. */
    public void menu() {
        System.out.println("SCONTRINI - SIMULAZIONE DIDATTICA, NON VALIDA AI FINI FISCALI");
        while (true) {
            System.out.println("\n1 Nuovo scontrino\n2 Elenco\n3 Visualizza\n4 Invia email\n0 Esci");
            try {
                String scelta = leggi("Scelta: ");
                switch (scelta) {
                    case "1": crea(); break;
                    case "2": elenco(); break;
                    case "3": System.out.println(cerca().testo()); break;
                    case "4": invia(); break;
                    case "0": return;
                    default: System.out.println("Scelta non valida.");
                }
            } catch (java.util.NoSuchElementException errore) {
                System.out.println("\nInput terminato.");
                return;
            } catch (IllegalArgumentException | IllegalStateException errore) {
                System.out.println("Operazione annullata: " + errore.getMessage());
            } catch (Exception errore) {
                // Evita di stampare dettagli SMTP che possono contenere dati personali.
                System.out.println("Operazione non completata (" + errore.getClass().getSimpleName()
                        + "). Controllare file, rete e configurazione SMTP.");
            }
        }
    }

    /** Raccoglie i dati e salva prima di dichiarare emesso il documento. */
    private void crea() throws Exception {
        String negozio = leggi("Nome negozio: ");
        String partitaIva = leggi("Partita IVA (11 cifre): ");
        String email = leggi("Email cliente: ");
        Scontrino.validaEmail(email);
        Vector<RigaAcquisto> righe = new Vector<>();
        while (true) {
            String descrizione = leggi("Merce (INVIO per terminare): ");
            if (descrizione.isEmpty()) {
                break;
            }
            int quantita = Integer.parseInt(leggi("Quantità intera: "));
            BigDecimal prezzo = decimale("Prezzo unitario IVA inclusa: ");
            BigDecimal aliquota = decimale("Aliquota IVA percentuale (es. 22): ");
            righe.add(new RigaAcquisto(descrizione, quantita, prezzo, aliquota));
        }
        // Il progressivo riparte dal massimo esistente anche dopo un riavvio.
        int numero = 1;
        for (Scontrino documento : archivio) {
            numero = Math.max(numero, Math.addExact(documento.getNumero(), 1));
        }
        Scontrino nuovo = new Scontrino(numero, negozio, partitaIva, email, righe);
        String conferma = leggi(nuovo.testo() + "Confermare emissione? (s/n): ");
        if (!conferma.equalsIgnoreCase("s")) {
            System.out.println("Emissione annullata.");
            return;
        }
        archivio.add(nuovo);
        try {
            repository.save(archivio);
        } catch (Exception errore) {
            archivio.remove(nuovo);
            throw errore;
        }
        System.out.println("Scontrino n. " + numero + " emesso e archiviato.");
        // La copia testuale è rigenerabile: l'archivio oggetti è la fonte dei dati.
        repository.saveTesto(nuovo);
    }

    /** Mostra lo stato di accettazione SMTP, distinto dall'emissione. */
    private void elenco() {
        if (archivio.isEmpty()) {
            System.out.println("Nessuno scontrino presente.");
        }
        for (Scontrino documento : archivio) {
            System.out.println(documento.getNumero() + " | " + documento.getEmail() + " | "
                    + documento.totale().toPlainString() + " EUR | "
                    + (documento.isInviato() ? "Accettato da SMTP" : "Da inviare"));
        }
    }

    /** Cerca un documento senza modificare il suo contenuto. */
    private Scontrino cerca() {
        int numero = Integer.parseInt(leggi("Numero scontrino: "));
        for (Scontrino documento : archivio) {
            if (documento.getNumero() == numero) {
                return documento;
            }
        }
        throw new IllegalArgumentException("Scontrino non trovato.");
    }

    /** L'invio è sempre esplicito; un errore non elimina lo scontrino. */
    private void invia() throws Exception {
        Scontrino documento = cerca();
        if (documento.isInviato()) {
            if (!leggi("Già inviato. Inviare di nuovo? (s/n): ").equalsIgnoreCase("s")) {
                return;
            }
        }
        Path file = repository.saveTesto(documento);
        new SendMail().invia(documento, file);
        documento.setInviato(true);
        try {
            repository.save(archivio);
        } catch (Exception errore) {
            System.out.println("ATTENZIONE: SMTP ha accettato la mail ma lo stato non è stato salvato."
                    + " Un nuovo invio può duplicare la mail.");
            throw errore;
        }
        System.out.println("Messaggio accettato dal server SMTP; consegna finale non verificata.");
    }
}
