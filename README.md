# Gestione degli scontrini e invio ai clienti

Progetto didattico Java con menu testuale, realizzato a partire dagli esempi del docente su file, serializzazione e SMTP. La sintassi usa classi, metodi, cicli, `Vector` e gestione delle eccezioni. I commenti nel codice spiegano responsabilità e operazioni.

**I documenti prodotti sono simulazioni e non hanno validità fiscale.** L'emissione fiscale reale richiede dispositivi o servizi autorizzati e gli adempimenti previsti dalla normativa. Questo programma non comunica con l'Agenzia delle Entrate.

## Vincoli rispettati

Il POM conserva `it.amdani.school:smtp-example:0.0.1-SNAPSHOT` e l'unica dipendenza dichiarata nell'allegato, `com.sun.mail:javax.mail:1.6.2`. JavaMail usa anche la propria dipendenza transitiva Activation: non è stata aggiunta una libreria applicativa. Nessun framework, database, generatore PDF o libreria di test è stato introdotto. Si usa il JDK 21 già preparato nell'ambiente e Maven 3.9.9, senza sostituire versioni indicate dal docente.

## Struttura del progetto

Le sei classi del programma sono nel package predefinito, senza dichiarazioni `package`. Non ci sono cartelle per controller, servizi o altri livelli aggiuntivi.

```text
java1/
├── pom.xml                     Configurazione Maven e libreria del professore
├── README.md                   Come importare e usare il progetto
├── DOCUMENTAZIONE.md           Relazione e presentazione in classe
└── src/
    ├── main/java/
    │   ├── Main.java                 Menu e avvio del programma
    │   ├── RigaAcquisto.java         Una merce acquistata
    │   ├── Scontrino.java            Documento e calcoli
    │   ├── Repository.java           Salvataggio e lettura dei file
    │   ├── SendMail.java             Invio email
    │   └── ConfigurazioneGmail.java  Email e password da inserire
    └── test/java/
        └── VerificaProgetto.java     Verifiche separate dal programma
```

Durante l'uso vengono create `dati/` per gli scontrini e `target/` per i file compilati: non occorre modificarle. Per iniziare, aprire `Main.java`; per configurare l'email, aprire `ConfigurazioneGmail.java`.

## Avvio in Eclipse su Windows, senza terminale

1. Installare **JDK 21** ed **Eclipse IDE for Java Developers** con il supporto Maven (m2e). Eclipse gestisce Maven: non occorre installarlo separatamente.
2. Scaricare il progetto da GitHub con **Code → Download ZIP** ed estrarlo.
3. In Eclipse scegliere **File → Import → Maven → Existing Maven Projects**.
4. Con **Browse** selezionare la cartella che contiene `pom.xml`, selezionare il progetto e premere **Finish**. Attendere il download delle dipendenze: al primo avvio serve Internet.
5. In **Window → Preferences → Java → Installed JREs** selezionare il **JDK 21**. Se manca, aggiungerlo con **Add → Standard VM**, indicando la cartella del JDK. In **Project → Properties → Java Build Path → Libraries** verificare che il progetto utilizzi Java 21.
6. Espandere **src/main/java → (default package)**, aprire `Main.java` e scegliere **Run As → Java Application**.
7. Usare il menu nella scheda **Console** di Eclipse: fare clic nella Console per digitare le risposte.

Non occorrono comandi di terminale. Il menu è testuale nella Console di Eclipse, non una finestra grafica. Se Eclipse segnala dipendenze mancanti, fare clic destro sul progetto e scegliere **Maven → Update Project**.

### Se Eclipse mostra molti errori

Il repository include `.project`, `.classpath` e `.settings` per indicare esplicitamente a Eclipse le due cartelle sorgenti, Java 21 e le dipendenze Maven. Non copiare i file Java dentro un vecchio progetto con package: importare la cartella completa tramite **Maven → Existing Maven Projects**.

Se il progetto era gia aperto prima degli aggiornamenti:

1. Salvare a parte eventuali modifiche locali, soprattutto `ConfigurazioneGmail.java` e la cartella `dati`.
2. Usare la versione aggiornata del progetto. In Eclipse premere **Refresh**, poi **Maven → Update Project**, infine **Project → Clean**.
3. In **Window → Preferences → Java → Installed JREs → Execution Environments → JavaSE-21**, selezionare il JDK 21 installato. Se `JavaSE-21` non compare, serve una versione di Eclipse che supporti Java 21.
4. Avviare la classe `Main` attuale dal package predefinito; un'eventuale vecchia configurazione di avvio con `it.amdani.school.Main` non e piu valida.

Errori sugli import `javax.mail` indicano che controllare **Maven Dependencies** e l'esito del download Maven. Errori su `java.lang.Object` o sulla JRE richiedono di controllare il JDK associato. Il testo preciso nella scheda **Problems** permette di distinguere queste cause: la configurazione salvata non sostituisce l'installazione locale di JDK e supporto Maven.

### Gmail: credenziali direttamente nel codice

Aprire `ConfigurazioneGmail.java` e sostituire i due segnaposto tra virgolette:

```java
public static final String EMAIL = "INSERISCI_LA_TUA_EMAIL";
public static final String PASSWORD_APP = "INSERISCI_LA_PASSWORD_PER_LE_APP";
```

Inserire il proprio indirizzo Gmail e la **password per le app Google**, poi salvare con **Ctrl+S** e riavviare `Main.java`. Server `smtp.gmail.com`, porta `587`, autenticazione e STARTTLS sono già configurati. Il mittente coincide con l'indirizzo inserito. Non servono variabili d'ambiente.

La password per le app si genera dalle impostazioni dell'account Google dopo aver attivato la verifica in due passaggi, se disponibile per quell'account. Alcuni account scolastici o aziendali non la consentono: verificare con l'amministratore. Non usare la password ordinaria Gmail e non pubblicare su GitHub il file dopo aver inserito credenziali reali. I segnaposto pubblicati non contengono credenziali funzionanti.

Il programma funziona subito per creazione e consultazione anche senza compilare questi due campi. Per inviare realmente, email e password per le app sono indispensabili.

### Verifiche da Eclipse

Espandere **src/test/java → (default package)**, aprire `VerificaProgetto.java` e scegliere **Run As → Java Application**. Nella Console deve comparire **SUPERATE 20 verifiche.** Non usare **Run As → JUnit Test**: non è stata aggiunta la libreria JUnit.

### Dove vengono salvati gli scontrini

Nella cartella `dati` del progetto, con la directory di lavoro predefinita di Eclipse. Per vederla, fare clic destro sul progetto e scegliere **Refresh**. Per un archivio diverso: **Run → Run Configurations → Java Application → Main → Arguments → Program arguments**, inserire il percorso tra virgolette, per esempio `"C:\Users\Nome\Documenti\Prova scontrini"`.

Se hai già creato un archivio con la precedente versione dotata di package, conserva quei file: la serializzazione di quella versione non è compatibile con le classi ora prive di package. Per provare questa versione usa una cartella dati nuova, senza cancellare l'archivio precedente.

Eclipse compila direttamente il progetto importato da Maven. L'ambiente di verifica disponibile è Linux: l'importazione e l'esecuzione in Eclipse su Windows non sono state provate qui.

## Guida al menu

1. **Nuovo scontrino:** inserire negozio, partita IVA di 11 cifre ed email cliente; poi descrizione, quantità intera positiva, prezzo unitario lordo e aliquota di ogni merce. Una descrizione vuota termina il carrello. Confermare con `s` dopo aver letto il riepilogo.
2. **Elenco:** mostra numero, cliente, totale e stato di invio.
3. **Visualizza:** richiede il numero e mostra il documento completo.
4. **Invia email:** richiede il numero e invia il testo come corpo e allegato `.txt`. Per un documento già inviato chiede conferma prima di ripetere l'invio.
0. **Esci:** termina il programma. La fine dell'input termina anche una sessione non interattiva.

Punto e virgola sono accettati come separatore decimale. Il prezzo può avere al massimo due cifre decimali. Un errore nei dati annulla l'operazione corrente e riporta al menu. Un carrello vuoto non può essere emesso. Non è necessario configurare SMTP per creare o consultare gli scontrini.

### Esempio

Acquistando due caffè a 1,22 EUR con IVA 22% e un pane a 1,04 EUR con IVA 4%, si ottengono totale 3,48 EUR, IVA inclusa 0,48 EUR e imponibile 3,00 EUR. Il documento riporta la dicitura di simulazione, il negozio, la partita IVA, la data, il numero, il cliente e le righe acquistate.

## Invio email

La configurazione è nella classe `ConfigurazioneGmail`, descritta sopra. L'invio usa autenticazione e STARTTLS obbligatorio, con controllo del certificato e del nome del server. Non supporta TLS implicito sulla porta 465.

La conferma significa che il server SMTP ha accettato il messaggio, non che il cliente lo abbia ricevuto o letto. Un errore di invio non elimina lo scontrino. Se l'accettazione riesce ma il salvataggio dello stato fallisce, il programma avvisa che ripetere l'invio può duplicare la mail. Anche un timeout dopo la trasmissione può lasciare incerto l'esito: controllare prima di ritentare.

## Verifiche

In Eclipse fare clic destro su `VerificaProgetto.java` e scegliere **Run As → Java Application**. Un controllo fallito interrompe il programma e mostra un errore nella Console. Le 20 verifiche riguardano calcoli, copia del carrello, validazione, archivio assente e corrotto, salvataggio e rilettura, stato invio, testo UTF-8 e struttura/contenuto del messaggio MIME. I file di prova sono temporanei e vengono rimossi.

**`mvn test` da solo non esegue queste verifiche**, perché non si è aggiunta una libreria di test. Per validare il progetto eseguire la classe indicata. L'invio SMTP reale richiede credenziali e rete: non viene simulato come consegna riuscita dai test offline.

La relazione tecnica e una traccia per la presentazione in classe sono in [DOCUMENTAZIONE.md](DOCUMENTAZIONE.md).
