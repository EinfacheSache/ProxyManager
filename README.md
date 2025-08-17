# ProxyManager

[![Java 21](https://img.shields.io/badge/Java-21-red?logo=openjdk)](https://openjdk.org/)
[![Velocity 3.4.0‑SNAPSHOT](https://img.shields.io/badge/Velocity-3.4.0--SNAPSHOT-blue?logo=apache&logoColor=white)](https://velocitypowered.com/)
[![Build with Maven](https://img.shields.io/badge/Build-Maven-orange?logo=apachemaven)](https://maven.apache.org/)
[![License: Apache‑2.0](https://img.shields.io/github/license/EinfacheSache/ProxyManager)](LICENSE.md)
[![CI](https://github.com/EinfacheSache/ProxyManager/actions/workflows/build.yml/badge.svg)](https://github.com/EinfacheSache/ProxyManager/actions/workflows/build.yml)

🌍 **Verfügbare Sprachen:** [English](README.md) | [Deutsch](README.de.md)

---

# English Version

**ProxyManager** is a **Velocity** proxy plugin with optional **Discord bot** integration, **Redis/MySQL modules**, a small **TCP server** (port **6060**) for Discord communication, and optional **Protocolize** UI support.  
It helps you manage maintenance/global mute, show a settings UI, and bridge Minecraft ↔ Discord events.

> Tested/declared in project files: **Java 21**, **Maven**, **Velocity 3.4.0‑SNAPSHOT** (provided), **Protocolize API** (optional, provided).  
> Plugin metadata: `src/main/resources/velocity-plugin.json` → **id:** `proxymanager`, **main:** `de.einfachesache.proxymanager.velocity.VProxyManager`.

---

## Table of Contents
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Configuration](#configuration)
    - [`config.yml`](#configyml)
    - [`modules/minecraft.yml`](#modulesminecraftyml)
    - [`modules/discord.yml`](#modulesdiscordyml)
    - [`modules/redis.yml`](#modulesredisyml)
    - [`modules/mysql.yml`](#modulesmysqlyml)
    - [`application.properties` (Discord token)](#applicationproperties-discord-token)
- [Commands](#commands)
- [Developer Notes](#developer-notes)
- [License](#license)

---

## Features

- **Velocity plugin** with metadata in `velocity-plugin.json`
    - `id: proxymanager`, `main: de.einfachesache.proxymanager.velocity.VProxyManager`
    - Optional dependency: **Protocolize** (for GUI features)
- **Discord module (JDA)** with global commands/listeners (tickets, giveaways, info, lookup, ping, etc.)
- **Maintenance mode** & **Global mute** controls
- **Settings/Proxy GUI** (requires Protocolize to be installed on the proxy)
- **Connection management**: server icon from player heads, domain whitelist & verification controls
- **Redis connector** (caching & pub/sub patterns), **MySQL data source** scaffolding
- **TCP server** (port **6060**) to accept connections when `discord.tcp-server: true`

---

## Requirements

| Component        | Version / Note                      |
|------------------|-------------------------------------|
| Java             | 21                                  |
| Velocity         | 3.4.0‑SNAPSHOT (provided)           |
| Maven            | 3.9+                                 |
| Protocolize API  | Optional (provided)                  |
| Discord Bot      | Token required (see `application.properties`) |
| Redis            | Optional (default host `127.0.0.1`, port **1337**) |
| MySQL            | Optional (disabled by default)      |

> All versions & IDs above come from the repo’s `pom.xml` and `velocity-plugin.json`.

---

## Installation

1. **Build** (or download a release):
   ```bash
   git clone https://github.com/EinfacheSache/ProxyManager.git
   cd ProxyManager
   mvn clean package
   ```
   The shaded JAR will be in `target/`.

2. **Deploy** to your **Velocity** proxy: copy the JAR to `plugins/`.

3. **Start** the proxy once → default configs are created in `plugins/ProxyManager/`:
    - `config.yml`
    - `modules/discord.yml`, `modules/minecraft.yml`, `modules/redis.yml`, `modules/mysql.yml`
    - `data.yml`

4. **Configure** the modules (see below) and add **`application.properties`** containing your Discord bot token.

5. *(Optional)* Install **Protocolize** on the proxy to enable in‑game GUI/menus used by commands like `/proxy` and `/settings`.

---

## Configuration

All files live under `plugins/ProxyManager/` after the first start.

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
  verify-server-domain: ''   # e.g. verify.yourdomain.com
  verify-server: ''          # e.g. "Verify"
  allowed-domains:
    - 'YourServerName.de'
```

### `modules/discord.yml`

```yml
discord:
  enabled: true
  activity-type: PLAYING
  activity: 'Flareon Events 🔥'
  tcp-server: true   # starts a small TCP server on port 6060

servers:
  '1006339615550091284':
    name: Development
    # Role IDs
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

> Runtime data such as counting/giveaway state is stored in `data.yml` under `servers.<guildId>.*`.

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

### `application.properties` (Discord token)

The code reads your bot token from a **classpath resource** named `application.properties` (see `Config#getToken`).  
Create `src/main/resources/application.properties` **before building** (or otherwise ensure the file is on the classpath at runtime).

```properties
TOKEN=YOUR_DISCORD_BOT_TOKEN
```

> If the file is missing, the log will contain: `./application.properties file can't be found`.

---

## Commands

Registered in `VProxyManager#register()`:

| Command       | Aliases               | Purpose (short)                         |
|---------------|-----------------------|-----------------------------------------|
| `/proxy`      | `pr`, `proxygui`      | Opens proxy GUI (Protocolize required)  |
| `/settings`   | —                     | Opens settings GUI (Protocolize)        |
| `/maintenance`| —                     | Toggle/handle maintenance mode          |
| `/commands`   | —                     | Lists available commands/help           |
| `/gmute`      | —                     | Global mute toggle via Redis broadcast  |

> Discord-side commands (e.g., `/core`, `/ping`, `/info`, `/ticket`, `/giveaway`, `/lookup`, context menus) are registered by the bot and configured via `modules/discord.yml`.

---

## Developer Notes

- **Maven Coordinates:** `de.cubeattack:proxymanager:v1.0.0`
- **Shade Plugin:** produces a single, deployable JAR.
- **Velocity metadata:** `src/main/resources/velocity-plugin.json`
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

### Build locally
```bash
mvn clean package
# Output: target/ProxyManager-v1.0.0.jar (shaded)
```

### Run on Velocity
- Drop the JAR into `plugins/`
- Configure the module YAMLs and `application.properties` (token)
- *(Optional)* Install **Protocolize** on the proxy for GUI features

---

## License

This project is licensed under the **Apache‑2.0 License**.  
See [LICENSE.md](LICENSE.md) for details.