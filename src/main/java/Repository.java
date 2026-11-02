import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/** Riprende i due Repository del docente: oggetti e testo su file. */
public class Repository {
    private final Path cartella;

    /** Ogni archivio conserva i propri file nella cartella indicata. */
    public Repository(Path cartella) throws IOException {
        this.cartella = cartella;
        Files.createDirectories(cartella);
    }

    /** Un archivio assente è normale al primo avvio; uno corrotto genera errore. */
    public Object load() throws IOException, ClassNotFoundException {
        Path file = cartella.resolve("scontrini.dat");
        if (!Files.exists(file)) {
            return null;
        }
        try (ObjectInputStream input = new ObjectInputStream(Files.newInputStream(file))) {
            return input.readObject();
        }
    }

    /** Scrive prima un file temporaneo per proteggere il precedente archivio. */
    public void save(Object dati) throws IOException {
        Path temporaneo = Files.createTempFile(cartella, "archivio-", ".tmp");
        try {
            try (ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(temporaneo))) {
                output.writeObject(dati);
            }
            try {
                Files.move(temporaneo, cartella.resolve("scontrini.dat"),
                        StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException errore) {
                Files.move(temporaneo, cartella.resolve("scontrini.dat"), StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporaneo);
        }
    }

    /** Genera una copia leggibile UTF-8 da stampare o allegare alla mail. */
    public Path saveTesto(Scontrino scontrino) throws IOException {
        Path file = cartella.resolve("scontrino-" + scontrino.getNumero() + ".txt");
        Files.writeString(file, scontrino.testo(), StandardCharsets.UTF_8);
        return file;
    }
}
