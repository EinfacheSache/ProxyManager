# ProxyManager

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/)
[![Velocity 3.4.0‑SNAPSHOT](https://img.shields.io/badge/Velocity-3.4.0--SNAPSHOT-blue?logo=apache&logoColor=white)](https://velocitypowered.com/)
[![Build with Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven)](https://maven.apache.org/)
[![License: Apache‑2.0](https://img.shields.io/github/license/EinfacheSache/ProxyManager)](LICENSE.md)
[![CI](https://github.com/EinfacheSache/ProxyManager/actions/workflows/build.yml/badge.svg)](https://github.com/EinfacheSache/ProxyManager/actions/workflows/build.yml)

🌍 **Verfügbare Sprachen:** [English](README.md) | [Deutsch](README.de.md)

---

## Kurzbeschreibung

**ProxyManager** ist ein **Velocity**‑Proxy‑Plugin mit optionaler **Discord‑Bot**‑Integration, **Redis/MySQL‑Modulen**, einem kleinen **TCP‑Server** (Port **6060**) für die Discord‑Kommunikation und optionaler **Protocolize**‑Unterstützung für UI/Inventar‑Menüs.  
Es hilft dir beim Verwalten von **Wartungsmodus**/**Global‑Mute**, bietet eine **Settings/Proxy‑GUI** und baut eine Brücke zwischen Minecraft ↔ Discord‑Ereignissen.

> In den Projektdateien deklariert/getestet: **Java 21**, **Maven**, **Velocity 3.4.0‑SNAPSHOT** (provided), **Protocolize API** (optional, provided).  
> Plugin‑Metadaten: `src/main/resources/velocity-plugin.json` → **id:** `proxymanager`, **main:** `de.einfachesache.proxymanager.velocity.VProxyManager`.

---

## Inhaltsverzeichnis
- [Funktionen](#funktionen)
- [Voraussetzungen](#voraussetzungen)
- [Installation](#installation)
- [Konfiguration](#konfiguration)
    - [`config.yml`](#configyml)
    - [`modules/minecraft.yml`](#modulesminecraftyml)
    - [`modules/discord.yml`](#modulesdiscordyml)
    - [`modules/redis.yml`](#modulesredisyml)
    - [`modules/mysql.yml`](#modulesmysqlyml)
    - [`application.properties` (Discord‑Token)](#applicationproperties-discord-token)
- [Befehle](#befehle)
- [Hinweise für Entwickler](#hinweise-für-entwickler)
- [Lizenz](#lizenz)

---

## Funktionen

- **Velocity‑Plugin** mit Metadaten in `velocity-plugin.json`
    - `id: proxymanager`, `main: de.einfachesache.proxymanager.velocity.VProxyManager`
    - Optionale Abhängigkeit: **Protocolize** (für GUI‑Features)
- **Discord‑Modul (JDA)** mit globalen Commands/Listenern (Tickets, Giveaways, Info, Lookup, Ping, …)
- **Wartungsmodus** & **Global‑Mute**‑Steuerung
- **Settings/Proxy‑GUI** (benötigt Protocolize auf dem Proxy)
- **Connection‑Management**: Server‑Icon aus Spieler‑Köpfen, Domain‑Whitelist & Verifikations‑Kontrollen
- **Redis‑Connector** (Caching & Pub/Sub), **MySQL**‑Datenquelle
- **TCP‑Server** (Port **6060**) für Verbindungen, wenn `discord.tcp-server: true`

---

## Voraussetzungen

| Komponente       | Version / Hinweis                                |
|------------------|---------------------------------------------------|
| Java             | 21                                                |
| Velocity         | 3.4.0‑SNAPSHOT (provided)                         |
| Maven            | ≥ 3.9                                             |
| Protocolize API  | Optional (provided)                               |
| Discord‑Bot      | Token erforderlich (siehe `application.properties`)|
| Redis            | Optional (Standard‑Host `127.0.0.1`, Port **1337**) |
| MySQL            | Optional (standardmäßig deaktiviert)              |

> Alle Versionen & IDs stammen aus `pom.xml` und `src/main/resources/velocity-plugin.json`.

---

## Installation

1. **Build** (oder Release herunterladen):
   ```bash
   git clone https://github.com/EinfacheSache/ProxyManager.git
   cd ProxyManager
   mvn clean package
   ```
   Das **shaded JAR** liegt anschließend unter `target/`.

2. **Deployment** auf deinen **Velocity**‑Proxy: JAR nach `plugins/` kopieren.

3. **Starten** → Standard‑Configs werden unter `plugins/ProxyManager/` erstellt:
    - `config.yml`
    - `modules/discord.yml`, `modules/minecraft.yml`, `modules/redis.yml`, `modules/mysql.yml`
    - `data.yml`

4. **Module konfigurieren** (siehe unten) und **`application.properties`** mit deinem Discord‑Bot‑Token hinzufügen.

5. *(Optional)* **Protocolize** auf dem Proxy installieren, um die In‑Game‑GUIs (z. B. `/proxy` und `/settings`) zu nutzen.

---

## Konfiguration

Nach dem ersten Start liegen alle Dateien unter `plugins/ProxyManager/`.

### `config.yml`

```yml
server-name: 'YourServerName'
```

### `modules/minecraft.yml`

```yml
server-domain-name: 'YourServerName.de'
maintenance-mode: false

manage-connections:
  enabled: true
  player-head-as-server-icon: false
  verify-server-domain: ''   # z. B. verify.yourdomain.com
  verify-server: ''          # z. B. "Verify"
  allowed-domains:
    - 'YourServerName.de'
```

### `modules/discord.yml`

```yml
discord:
  enabled: true
  activity-type: PLAYING
  activity: 'Flareon Events 🔥'
  tcp-server: true   # startet einen kleinen TCP‑Server auf Port 6060

servers:
  '1006339615550091284':
    name: Development
    # Rollen‑IDs
    user-role-id: 1007349225383788634
    staff-role-id: 1402702044695105548
    beta-tester-role-id: 1398068570201522257
    # Channel/Category IDs
    log-channel-id: 1396658613405356072
    tickets-category-id: 1120488702511161447
    counting-channel-id: 1389001094109331627
    giveaway-channel-id: 1110668721296511086
    invite-log-channel-id: 1396658613405356072
```

> Laufzeitdaten (z. B. Counting/Giveaway) werden in `data.yml` unter `servers.<guildId>.*` gespeichert.

### `modules/redis.yml`

```yml
redis:
  host: '127.0.0.1'
  port: 1337
  user: ''
  password: ''
```

### `modules/mysql.yml`

```yml
mysql:
  connect: false
  host: ''
  port: 3306
  user: root
  password: ''
  database: ''
```

### `application.properties` (Discord-Token)

Der Bot‑Token wird aus einer **Classpath‑Ressource** `application.properties` gelesen (siehe `Config#getToken`).  
Lege **vor dem Build** `src/main/resources/application.properties` an (oder stelle sicher, dass die Datei zur Laufzeit auf dem Classpath liegt).

```properties
TOKEN=DEIN_DISCORD_BOT_TOKEN
```

> Falls die Datei fehlt, erscheint im Log: `./application.properties file can't be found`.

---

## Befehle

In `VProxyManager#register()` registriert:

| Befehl         | Aliases            | Zweck (kurz)                               |
|----------------|--------------------|--------------------------------------------|
| `/proxy`       | `pr`, `proxygui`   | Öffnet Proxy‑GUI (Protocolize erforderlich)|
| `/settings`    | —                  | Öffnet Settings‑GUI (Protocolize)          |
| `/maintenance` | —                  | Wartungsmodus umschalten/verwalten         |
| `/commands`    | —                  | Listet verfügbare Befehle/Hilfe            |
| `/gmute`       | —                  | Global‑Mute via Redis‑Broadcast            |

> Discord‑seitige Commands (z. B. `/core`, `/ping`, `/info`, `/ticket`, `/giveaway`, `/lookup`, Context‑Menüs) werden vom Bot registriert und über `modules/discord.yml` konfiguriert.

---

## Hinweise für Entwickler

- **Maven‑Koordinaten:** `de.cubeattack:proxymanager:v1.0.0`
- **Shade‑Plugin:** erzeugt ein einzelnes, deploybares JAR.
- **Velocity‑Metadaten:** `src/main/resources/velocity-plugin.json`
  ```json
  {
    "id": "proxymanager",
    "name": "ProxyManager",
    "version": "${project.version}:${buildNumber}",
    "authors": ["EinfacheSache"],
    "url": "https://einfachesache.de/discord/",
    "main": "de.einfachesache.proxymanager.velocity.VProxyManager",
    "description": "A Plugin for manage your ProxyServer",
    "dependencies": [{ "id": "protocolize", "optional": true }]
  }
  ```

### Lokal bauen
```bash
mvn clean package
# Ergebnis: target/ProxyManager-v1.0.0.jar (shaded)
```

### Auf Velocity starten
- JAR nach `plugins/` kopieren
- YAML‑Module & `application.properties` (Token) konfigurieren
- *(Optional)* **Protocolize** auf dem Proxy installieren

---

## Lizenz

Dieses Projekt steht unter der **Apache‑2.0‑Lizenz**.  
Siehe [LICENSE.md](LICENSE.md) für Details.