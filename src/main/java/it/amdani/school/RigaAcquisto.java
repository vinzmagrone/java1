package it.amdani.school;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Una merce acquistata con prezzo unitario comprensivo di IVA. */
public class RigaAcquisto implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String descrizione;
    private final int quantita;
    private final BigDecimal prezzo;
    private final BigDecimal aliquota;

    /** Convalida i dati anche quando la classe viene usata senza il menu. */
    public RigaAcquisto(String descrizione, int quantita, BigDecimal prezzo, BigDecimal aliquota) {
        if (descrizione == null || descrizione.isBlank() || descrizione.contains("\n") || descrizione.contains("\r")) {
            throw new IllegalArgumentException("Inserire una descrizione su una sola riga.");
        }
        if (quantita <= 0 || prezzo == null || prezzo.signum() < 0 || prezzo.scale() > 2) {
            throw new IllegalArgumentException("Quantità positiva e prezzo con massimo due decimali richiesti.");
        }
        if (aliquota == null || aliquota.signum() < 0 || aliquota.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("L'IVA deve essere compresa tra 0 e 100.");
        }
        this.descrizione = descrizione.trim();
        this.quantita = quantita;
        this.prezzo = prezzo.setScale(2);
        this.aliquota = aliquota;
    }

    /** BigDecimal evita gli errori di arrotondamento dei numeri double. */
    public BigDecimal totale() {
        return prezzo.multiply(BigDecimal.valueOf(quantita));
    }

    /** Scorpora l'IVA dal prezzo già lordo, arrotondando per riga ai centesimi. */
    public BigDecimal iva() {
        return totale().multiply(aliquota).divide(new BigDecimal("100").add(aliquota), 2, RoundingMode.HALF_UP);
    }

    /** Rappresentazione testuale della riga nello scontrino. */
    public String testo() {
        return descrizione + " | " + quantita + " x " + prezzo.toPlainString()
                + " EUR | IVA " + aliquota.toPlainString() + "% | " + totale().toPlainString() + " EUR";
    }
}
