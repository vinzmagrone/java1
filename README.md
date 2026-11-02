# Gestione degli scontrini e invio ai clienti

Progetto didattico Java con menu testuale, realizzato a partire dagli esempi del docente su file, serializzazione e SMTP. La sintassi usa classi, metodi, cicli, `Vector` e gestione delle eccezioni. I commenti nel codice spiegano responsabilità e operazioni.

**I documenti prodotti sono simulazioni e non hanno validità fiscale.** L'emissione fiscale reale richiede dispositivi o servizi autorizzati e gli adempimenti previsti dalla normativa. Questo programma non comunica con l'Agenzia delle Entrate.

## Vincoli rispettati

Il POM conserva `it.amdani.school:smtp-example:0.0.1-SNAPSHOT` e l'unica dipendenza dichiarata nell'allegato, `com.sun.mail:javax.mail:1.6.2`. JavaMail usa anche la propria dipendenza transitiva Activation: non è stata aggiunta una libreria applicativa. Nessun framework, database, generatore PDF o libreria di test è stato introdotto. Si usa il JDK 21 già preparato nell'ambiente e Maven 3.9.9, senza sostituire versioni indicate dal docente.

## Avvio rapido

Servono JDK 21 e Maven. Su Linux/macOS, dalla cartella del progetto:

```bash
bash scripts/avvia.sh
```

Per utilizzare un archivio diverso:

```bash
bash scripts/avvia.sh /tmp/prova-scontrini
```

Nell'ambiente cloud preparato per questo progetto, attivare prima gli strumenti e la configurazione di rete:

```bash
export JAVA_HOME=/workspace/.tools/jdk-21.0.13+9
export PATH="$JAVA_HOME/bin:/workspace/.tools/apache-maven-3.9.9/bin:$PATH"
export MAVEN_SETTINGS=/workspace/.maven-setup/settings.xml
export MAVEN_OPTS='-Djavax.net.ssl.trustStore=/etc/ssl/certs/java/cacerts'
cd /workspace/java1
bash scripts/avvia.sh
```

Gli ultimi due parametri riguardano il proxy cloud e i suoi certificati, non vanno copiati su un computer privo di quei file. In questo ambiente i comandi di rete richiedono inoltre l'accesso consentito dal runner.

Comandi equivalenti senza script:

```bash
mvn package dependency:copy-dependencies
java -cp 'target/classes:target/dependency/*' it.amdani.school.Main
```

Su Windows il separatore del classpath è `;`: `java -cp "target/classes;target/dependency/*" it.amdani.school.Main`.

## Guida al menu

1. **Nuovo scontrino:** inserire negozio, partita IVA di 11 cifre ed email cliente; poi descrizione, quantità intera positiva, prezzo unitario lordo e aliquota di ogni merce. Una descrizione vuota termina il carrello. Confermare con `s` dopo aver letto il riepilogo.
2. **Elenco:** mostra numero, cliente, totale e stato di invio.
3. **Visualizza:** richiede il numero e mostra il documento completo.
4. **Invia email:** richiede il numero e invia il testo come corpo e allegato `.txt`. Per un documento già inviato chiede conferma prima di ripetere l'invio.
0. **Esci:** termina il programma. La fine dell'input termina anche una sessione non interattiva.

Punto e virgola sono accettati come separatore decimale. Il prezzo può avere al massimo due cifre decimali. Un errore nei dati annulla l'operazione corrente e riporta al menu. Un carrello vuoto non può essere emesso. Non è necessario configurare SMTP per creare o consultare gli scontrini.

### Esempio

Acquistando due caffè a 1,22 EUR con IVA 22% e un pane a 1,04 EUR con IVA 4%, si ottengono totale 3,48 EUR, IVA inclusa 0,48 EUR e imponibile 3,00 EUR. Il documento riporta la dicitura di simulazione, il negozio, la partita IVA, la data, il numero, il cliente e le righe acquistate.

## Configurazione dell'email

Il server è Gmail: `smtp.gmail.com`, porta predefinita `587`, con STARTTLS. Configurare nel proprio ambiente le seguenti variabili, senza inserire credenziali nel codice o nel repository:

| Variabile | Significato |
| --- | --- |
| `SMTP_PORT` | Porta STARTTLS, predefinita `587` |
| `SMTP_USER` | Indirizzo completo dell'account Gmail, usato anche come mittente |
| `SMTP_PASSWORD` | Password per le app generata dall'account Google |

Nell'account Google attivare la verifica in due passaggi e generare una password per le app, se disponibile per l'account. Usare questa password al posto della password ordinaria di Gmail. Alcuni account scolastici o aziendali non consentono password per le app: in quel caso occorre verificare le impostazioni con l'amministratore. Non inviare la password in chat. `SMTP_HOST` e `SMTP_FROM` non servono: server e mittente sono già determinati dal programma.

L'invio usa autenticazione e STARTTLS obbligatorio, con controllo del certificato e del nome del server. Non supporta la porta 465 con TLS implicito. Il provider deve consentire questo tipo di autenticazione. L'esempio originale con password nel sorgente è stato adattato per leggere le variabili d'ambiente.

Un proxy HTTPS non abilita automaticamente SMTP: nell'ambiente cloud occorre un percorso TCP autorizzato verso il server e la porta. Eventuali credenziali sostituite soltanto da un proxy HTTPS non sono automaticamente utilizzabili da JavaMail via SMTP. Nessuna email viene inviata all'avvio o durante le prove automatiche.

La conferma indica che il server SMTP ha accettato il messaggio, non che il cliente lo abbia ricevuto o letto. Se l'invio fallisce, il documento resta nell'archivio e si può riprovare. Se il server accetta la mail ma la scrittura dello stato fallisce, compare un avviso: ripetere l'invio potrebbe duplicarla. Anche un timeout dopo la trasmissione può lasciare incerto l'esito: controllare il provider prima di ritentare.

## Verifiche

```bash
bash scripts/verifica.sh
```

Lo script compila con Maven ed esegue esplicitamente `VerificaProgetto`: un controllo fallito produce un codice di uscita diverso da zero. Le 20 verifiche riguardano calcoli, copia del carrello, validazione, archivio assente e corrotto, salvataggio e rilettura, stato invio, testo UTF-8 e struttura/contenuto del messaggio MIME. I file di prova sono temporanei e vengono rimossi.

**`mvn test` da solo non esegue queste verifiche**, perché non si è aggiunta una libreria di test. Per validare il progetto usare lo script indicato. L'invio SMTP reale richiede credenziali e rete: non viene simulato come consegna riuscita dai test offline.

La relazione tecnica e una traccia per la presentazione in classe sono in [docs/DOCUMENTAZIONE.md](docs/DOCUMENTAZIONE.md).
