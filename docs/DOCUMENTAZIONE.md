# Relazione di progetto

## Problema e obiettivo

Un'attività commerciale deve registrare le merci acquistate da un cliente, produrre un riepilogo e inviarlo via email. Il progetto realizza il flusso didattico di emissione, conservazione e invio. La fiscalizzazione reale resta fuori dal prototipo.

L'operatore è l'unico attore che usa il programma. Il cliente riceve la mail. Il servizio SMTP è un sistema esterno. Non sono previsti accesso multiutente, pagamenti, magazzino o modifica di documenti già emessi.

## Requisiti

| Codice | Requisito | Realizzazione |
| --- | --- | --- |
| R1 | Inserire i dati del negozio e del cliente | Menu nuovo scontrino |
| R2 | Registrare più merci e quantità | `Vector<RigaAcquisto>` |
| R3 | Calcolare totale, IVA e imponibile | `BigDecimal`, prezzi IVA inclusa |
| R4 | Assegnare numero e data | Massimo progressivo salvato + 1, data locale |
| R5 | Conservare gli acquisti tra due esecuzioni | Serializzazione su `scontrini.dat` |
| R6 | Consultare elenco e dettaglio | Menu elenco e visualizza |
| R7 | Inviare il documento al cliente | JavaMail e allegato di testo |
| R8 | Distinguere emissione e invio | Stato di accettazione SMTP salvato |
| R9 | Segnalare gli errori senza perdere l'archivio esistente | Eccezioni, scrittura temporanea e sostituzione |

La partita IVA è controllata solo per lunghezza e cifre; non si verifica il codice di controllo o l'esistenza dell'impresa. Anche la verifica email riguarda la sintassi, non l'esistenza della casella. L'aliquota è inserita dall'operatore tra 0 e 100: il programma non decide quale trattamento fiscale applicare.

## Struttura e origine del codice

| Classe | Responsabilità | Collegamento con l'allegato |
| --- | --- | --- |
| `Main` | Input da tastiera, menu e coordinamento | Nuova interfaccia testuale |
| `RigaAcquisto` | Merce, quantità, prezzo lordo e IVA | Nuovo modello dei dati |
| `Scontrino` | Dati documento, calcoli, testo e stato invio | Oggetto serializzabile conservato dal Repository |
| `Repository` | Lettura/scrittura oggetti e copia testuale | Riunisce i due esempi omonimi del docente |
| `SendMail` | Messaggio MIME, allegato e trasporto SMTP | Adatta `Session`, `Authenticator`, `MimeMultipart` e `Transport.send` dell'esempio |
| `VerificaProgetto` | Controlli ripetibili senza librerie nuove | Classe di verifica con metodo `main` |

Le due classi `Repository` dell'allegato avevano lo stesso nome e non potevano convivere nello stesso package. Sono state unificate. I percorsi Windows fissi sono stati sostituiti da una cartella relativa o configurabile. I flussi sono chiusi automaticamente con `try` e le eccezioni raggiungono il chiamante invece di nascondere un errore come risultato vuoto.

Il codice SMTP mantiene la libreria e la versione originali. Il server è Gmail (`smtp.gmail.com:587`, STARTTLS); il mittente coincide con `SMTP_USER` e `SMTP_PASSWORD` contiene la password per le app Google. L'allegato PDF dimostrativo è sostituito dal file di testo realmente generato: produrre PDF richiederebbe codice ulteriore o nuove librerie non consentite. Il testo semplice evita anche di dover interpretare come HTML i dati inseriti dal cliente.

## Flusso di emissione

1. L'operatore inserisce intestazione e cliente.
2. Aggiunge una o più merci, specificando quantità, prezzo lordo e aliquota.
3. Il programma calcola e mostra il riepilogo.
4. L'operatore conferma oppure annulla.
5. La conferma salva il documento nell'archivio; poi genera la copia testuale.
6. L'invio avviene con una scelta separata del menu, così l'emissione funziona anche senza rete.
7. Dopo l'accettazione SMTP viene salvato lo stato inviato.

Se la scrittura dell'archivio fallisce, il nuovo documento viene rimosso anche dalla memoria. Se fallisce soltanto la copia `.txt`, il documento resta emesso e la copia viene rigenerata al successivo invio. Nessun documento viene eliminato perché la mail non è partita.

## Calcoli

Per ogni riga:

```text
lordo riga = prezzo unitario × quantità
IVA riga = lordo riga × aliquota / (100 + aliquota)
totale documento = somma dei lordi
IVA documento = somma delle IVA arrotondate per riga
imponibile documento = totale documento − IVA documento
```

Gli importi usano `BigDecimal`. L'IVA è arrotondata ai centesimi con `HALF_UP` per ogni riga. A parità di acquisto, raggruppare le righe in modo diverso può cambiare un centesimo: è una scelta dichiarata del prototipo. Non si usa `double`, che può rappresentare in modo approssimato gli importi decimali.

## File e gestione degli errori

La cartella predefinita `dati/` contiene `scontrini.dat` e `scontrino-N.txt`. Il file oggetti è la fonte dei dati; i testi sono copie rigenerabili. Numero, data, intestazione, cliente, righe e stato vengono conservati insieme. I dati locali e `target/` sono esclusi da Git.

Il salvataggio crea un file temporaneo nella stessa cartella e sostituisce l'archivio; quando supportato dal filesystem la sostituzione è atomica. Un archivio corrotto interrompe l'avvio: non viene trattato come vuoto e sovrascritto. La serializzazione legge solo file locali fidati: non importare archivi ricevuti da terzi. Conservare copie di sicurezza e proteggere i dati personali con i permessi del sistema operativo.

Il prototipo prevede **una sola istanza per cartella dati**: non implementa blocchi o transazioni tra processi concorrenti. Il numero è progressivo soltanto all'interno di quell'archivio e non viene azzerato ad ogni nuovo anno. Non c'è cifratura dell'archivio, autenticazione degli operatori o registro delle modifiche. Cambi futuri alle classi serializzate richiedono attenzione alla compatibilità dei vecchi file.

## Piano di verifica e dimostrazione

Su Windows eseguire `.\scripts\verifica.bat`; su Linux/macOS eseguire `bash scripts/verifica.sh` per i controlli automatici descritti nel README. Gli script `.bat` usano il classpath Windows e attivano UTF-8 per gli accenti; richiedono JDK 21 e Maven nel Path. Non sono stati eseguiti su Windows nell'ambiente di sviluppo Linux. Per la dimostrazione del menu:

1. Creare un archivio temporaneo e inserire l'acquisto dell'esempio: due caffè da 1,22 EUR al 22%, un pane da 1,04 EUR al 4%.
2. Verificare il totale di 3,48 EUR e l'IVA di 0,48 EUR.
3. Uscire e riavviare con la stessa cartella: il documento deve essere ancora presente.
4. Inserire quantità zero, email errata o carrello vuoto: l'operazione deve essere annullata.
5. Richiedere un invio senza configurazione: deve comparire l'indicazione della variabile mancante e il documento deve restare disponibile.
6. Solo con una casella di prova autorizzata e SMTP configurato, inviare a un destinatario di prova e controllare corpo e allegato. Quest'ultima verifica richiede un servizio esterno e non fa parte dei controlli offline.

## Esiti verificati nell'ambiente di sviluppo

La compilazione Maven è terminata con `BUILD SUCCESS`. Il comando `bash scripts/verifica.sh` ha eseguito e superato tutte le 20 verifiche. È stato inoltre provato il menu con input automatico: creazione dell'acquisto da 3,48 EUR, chiusura e riapertura del processo, rilettura del documento, assegnazione del numero 2 al secondo acquisto, rifiuto di carrello vuoto e quantità zero. Il tentativo senza configurazione SMTP ha conservato il documento da inviare. Dopo l'impostazione del server Gmail, la prima variabile richiesta è `SMTP_USER`.

Composizione MIME e contenuto dell'allegato sono verificati offline. Non è stato eseguito un invio SMTP reale: non sono state fornite credenziali o una destinazione di prova autorizzata. Per questa parte resta la verifica manuale descritta sopra.

## Organizzazione proposta del gruppo

Questa è una suddivisione da assegnare ai componenti reali del gruppo, non un elenco di attività attribuite a persone che non hanno partecipato:

| Ruolo | Attività | Materiale da presentare |
| --- | --- | --- |
| Analisi e modello | Requisiti, dati e calcoli | Esempio di acquisto e formula IVA |
| Persistenza | Repository e gestione degli errori | File salvati e riavvio |
| Interfaccia e invio | Menu, SMTP e MIME | Dimostrazione guidata e configurazione |
| Verifica e relazione | Prove, limiti e documentazione | Esiti dei test e relazione finale |

## Traccia per l'esposizione in classe

- **1 minuto:** problema, attori e obiettivi.
- **2 minuti:** classi e riuso degli esempi del docente.
- **3 minuti:** creazione, visualizzazione e riapertura di uno scontrino.
- **1 minuto:** mail MIME e allegato, distinguendo composizione offline e invio reale.
- **2 minuti:** prove automatiche, errori gestiti e limiti del prototipo.

Ogni componente spiega la propria parte mostrando il codice e un risultato osservabile. Terminare chiarendo che il progetto è una simulazione didattica: l'integrazione con un sistema fiscale autorizzato è un lavoro distinto.
