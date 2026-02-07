# Documento di Analisi dei Requisiti - Dinner Plan

## 1. Introduzione
**Dinner Plan** è un'applicazione web progettata per facilitare l'organizzazione di cene ed eventi aziendali o di gruppo. Il sistema permette di gestire l'intero ciclo di vita dell'evento: dalla creazione, alla proposta di luoghi e date, alla votazione democratica, fino alla scelta finale e alla comunicazione tra i partecipanti.

## 2. Attori del Sistema
*   **Utente Partecipante**: Può visualizzare eventi, votare proposte, chattare e gestire il proprio profilo.
*   **Organizzatore**: Possiede i privilegi del Partecipante ma può anche creare eventi, gestire i partecipanti, aggiungere proposte, estendere scadenze e decretare la scelta finale.
*   **Amministratore**: Ha accesso completo alla gestione degli utenti e alle funzionalità di manutenzione del sistema (Backup & Restore).

## 3. Requisiti Funzionali

### 3.1 Gestione Utenza e Profilo
*   **Registrazione e Login**: Gli utenti possono registrarsi autonomamente e accedere al sistema.
*   **Gestione Profilo**:
    *   Modifica password ed email.
    *   **Geolocalizzazione**: Inserimento indirizzo con calcolo automatico delle coordinate (Latitudine/Longitudine) per il calcolo delle distanze.
    *   **Preferenze Alimentari**: Selezione del regime alimentare (Onnivoro, Vegetariano, Vegano, Celiaco) per il controllo automatico delle incompatibilità.

### 3.2 Dashboard e Navigazione
*   **Vista Eventi**: Elenco degli eventi a cui l'utente partecipa o che organizza, con indicazione dello stato (APERTO, CHIUSO, DECISO).
*   **Classifica Proposte (Globali)**:
    *   Elenco di tutte le location utilizzate o suggerite.
    *   Visualizzazione dettagliata con Mappa, Contatti (Email, Telefono, Sito Web).
    *   **Calcolo Distanza**: Visualizzazione della distanza in km dalla posizione dell'utente corrente.
    *   **Filtri Dietetici**: Icone che indicano quali esigenze alimentari soddisfa la location.
*   **Calendario**:
    *   Vista mensile interattiva.
    *   Visualizzazione scadenze sondaggi (rosso) ed eventi confermati (verde).
    *   Esportazione calendario completo in formato ICS.
*   **Ottimizzazione Mobile**: Interfaccia adattiva con funzionalità complete anche su smartphone.

### 3.3 Gestione Eventi (Organizzatore)
*   **Creazione Evento**: Definizione di titolo, descrizione e scadenza del sondaggio.
*   **Gestione Partecipanti**: Invito o rimozione degli utenti dall'evento.
*   **Ciclo di Vita**:
    *   *Aperto*: Si possono aggiungere proposte e votare.
    *   *Chiuso*: Scadenza raggiunta, voti bloccati.
    *   *Deciso*: L'organizzatore ha selezionato la data/luogo definitivo.
*   **Estensione Scadenza**: Possibilità di riaprire un evento o posticipare la chiusura.
*   **Cancellazione**: Eliminazione dell'evento con pulizia a cascata dei dati correlati.

### 3.4 Gestione Proposte
*   **Aggiunta Proposte**:
    *   Inserimento manuale (Luogo, Indirizzo, Descrizione, Contatti).
    *   **Smart Suggestion**: Riutilizzo di proposte dalla "Classifica Proposte" globale.
    *   **Proposta Centrale**: Algoritmo che individua automaticamente una location equidistante da tutti i partecipanti, dando priorità a quelle che soddisfano le esigenze alimentari di tutti.
*   **Opzioni Data**: Associazione di una o più date/orari a una location.
*   **Controlli di Compatibilità**:
    *   Il sistema avvisa visivamente ("⚠️ Attenzione") se una proposta non è compatibile con le preferenze alimentari di uno o più partecipanti specifici.
    *   Visualizzazione della distanza per ogni proposta nel dettaglio evento.

### 3.5 Interazione e Voto
*   **Votazione**: I partecipanti possono votare (Sì/No) su ogni combinazione Luogo-Data.
*   **Chat in Tempo Reale**: Sistema di messaggistica integrato nella pagina dell'evento.
*   **Selezione Finale**: L'organizzatore sceglie la proposta vincente (diventa "SELEZIONATA").
*   **Rating Post-Evento**: Dopo la decisione, i partecipanti possono lasciare un feedback (Like/Dislike) sulla scelta effettuata.

### 3.6 Amministrazione
*   **Gestione Utenti**: Reset password, promozione a Organizzatore, eliminazione utenti.
*   **Backup e Ripristino**:
    *   Esportazione dell'intero database in formato ZIP (contenente JSON).
    *   Importazione con ripristino completo dei dati (Utenti, Eventi, Proposte, Voti, Messaggi).
    *   Supporto completo per tutti i campi (incluse coordinate e preferenze alimentari).

## 4. Requisiti Non Funzionali
*   **Internazionalizzazione (i18n)**: Supporto completo per Italiano, Inglese e Svedese.
*   **Geocoding**: Integrazione con servizi esterni (es. Photon/ArcGIS) per la risoluzione degli indirizzi.
*   **Persistenza**: Database relazionale (configurato su Derby Embedded per default).
*   **Architettura**: Architettura a livelli (Controller, Service, Repository) basata su Spring Boot.
*   **Mapping**: Utilizzo di MapStruct per la conversione efficiente tra Entità e DTO.

## 5. Note Tecniche
*   Il sistema gestisce automaticamente la pulizia delle dipendenze circolari (es. Evento <-> Data Proposta) durante la cancellazione.
*   I caratteri speciali nei file di localizzazione sono gestiti tramite escape Unicode per garantire la compatibilità cross-platform.
