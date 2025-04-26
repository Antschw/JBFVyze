# BFVyze — Real-Time Battlefield V Overlay

## Project Overview

BFVyze is a Java 21 desktop application that overlays live Battlefield V server and player statistics on your screen. It captures a screenshot of the game window, extracts a server ID via Tess4J OCR, queries external APIs for server info and player stats, and displays everything in a lightweight JavaFX UI. The codebase follows SOLID principles and a Hexagonal (Ports & Adapters) architecture, with full English/French i18n support.

---

## Features

- **Server Tab**
    - Press a global hotkey (default F12) to grab a screenshot and run OCR.
    - Fetch server metadata from GameTools and BFVHackers (cheater count, player list).
    - Display server name, short/long IDs and number of detected cheaters.

- **Stats Tab**
    - Enter a player name in Settings; view overall and session stats (K/D, KPM, accuracy).
    - Automatic refresh every 2 minutes.
    - In-memory + disk cache (JSON, TTL 5 days) to minimize API calls.

- **Settings Tab**
    - Configure the monitored player name and the scan hotkey at runtime.
    - Changes persist across sessions in a user‐home config file.
    - UI labels update immediately when you change the hotkey.

- **Resource & Thread Management**
    - Clean shutdown of the JNativeHook keyboard listener to avoid hanging native threads.
    - Dedicated thread pool for concurrent player queries, with graceful shutdown on exit.
    - OCR resources closed via `BFVOcrFactory.shutdown()`, and forced `System.exit(0)` to kill any remaining non-daemon threads.

- **Internationalization (i18n)**
    - English/French support via `ResourceBundle` (`messages_en.properties`, `messages_fr.properties`).

- **Build & Packaging**
    - Maven build using the JavaFX Maven Plugin for development.
    - `scripts/package-windows.bat` produces a standalone Windows EXE with jpackage.

---

## Project Structure

```
fr.antschw.bfv
├── application
│   ├── orchestrator        # high-level workflows: OCR → API → UI
│   │   ├── PlayerStatsCoordinator.java
│   │   ├── PlayerStatsFilter.java
│   │   └── ServerScanCoordinator.java
│   │
│   └── util                # app-wide helpers & constants
│       ├── AppConstants.java
│       └── I18nUtils.java
│
├── domain                  # core business interfaces & models
│   ├── exception           # application-level exceptions
│   │   ├── ApiRequestException.java
│   │   ├── HotkeyListenerException.java
│   │   └── ScreenshotCaptureException.java
│   │
│   ├── model               # data classes
│   │   ├── HotkeyConfiguration.java
│   │   ├── InterestingPlayer.java
│   │   ├── ServerInfo.java
│   │   ├── ServerPlayer.java
│   │   ├── ServerPlayers.java
│   │   └── UserStats.java
│   │
│   └── service             # domain-level interfaces (ports)
│       ├── HotkeyConfigurationService.java
│       ├── HotkeyListenerService.java
│       ├── ScreenshotService.java
│       ├── ServerInfoService.java
│       ├── ServerPlayersService.java
│       ├── UserStatsCacheService.java
│       └── UserStatsService.java
│
├── infrastructure          # concrete adapters & bindings
│   ├── api
│   │   ├── client          # HTTP clients implementing domain services
│   │   │   ├── BfvHackersClient.java
│   │   │   ├── GameToolsClient.java
│   │   │   └── PlayerClient.java
│   │   ├── type            # enums and API metadata
│   │   │   └── ApiType.java
│   │   └── util            # request URI builders
│   │       └── ApiUriBuilder.java
│   │
│   ├── binding             # Guice module binding interfaces to adapters
│   │   └── AppModule.java
│   │
│   ├── cache               # disk-backed stats cache
│   │   └── UserStatsCacheAdapter.java
│   │
│   ├── hotkey              # hotkey configuration & listener adapters
│   │   ├── HotkeyConfigurationAdapter.java
│   │   └── HotkeyListenerAdapter.java
│   │
│   └── screenshot          # AWT Robot screenshot adapter
│       └── ScreenshotAdapter.java
│
├── ui                      # JavaFX application & views
│   ├── BFVyzeApplication.java
│   ├── MainController.java
│   ├── control             # reusable UI controls
│   │   ├── table
│   │   │   └── PlayerTableRow.java
│   │   ├── ResizeController.java
│   │   ├── TitleBarController.java
│   │   └── WindowController.java
│   │
│   ├── panel               # composite panels used in views
│   │   ├── PlayersPanel.java
│   │   ├── ScanControlPanel.java
│   │   └── StatusPanel.java
│   │
│   └── view                # one class per tab
│       ├── ServerView.java
│       ├── SettingsView.java
│       └── StatsView.java
│
├── resources               # non-code assets
│   ├── i18n                # messages_en.properties, messages_fr.properties
│   └── styles              # style.css
│
└── scripts
    └── package-windows.bat
```

---

## Setup & Development

### Prerequisites

- Java 21 SDK
- JavaFX 21 SDK (`javafx.controls`, `javafx.fxml`)
- Maven 3.6+
- Windows 10/11 (for jpackage) or adapt `package-windows.bat` on your OS

### Build & Run

```bash
git clone https://github.com/your-org/bfvyze.git
cd bfvyze
mvn clean package
mvn javafx:run
```

If JavaFX modules aren’t on the path:

```text
--module-path /path/to/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml
```

### Package Standalone Executable

```bash
cd scripts
package-windows.bat
```

Result: a self-contained `bfvyze-setup.exe` in `target/`.

---

## Usage

1. Launch the app.
2. In **Settings**, enter your player name and (optionally) change the scan hotkey.
3. Switch to **Server** tab, press the hotkey → server scan runs.
4. Switch to **Stats** tab to view live stats for your chosen player.
5. Cache file location:
   ```
   %USERPROFILE%\.bfvyze\statsCache.json
   ```

---

## Testing

- **Unit tests**:  `mvn test`
- **Integration tests**: add API-client mocks under `src/test/java`.

---

## Completed Beyond Initial Scope

- JSON disk cache with shutdown-hook persistence.
- Dedicated thread-pool in `ServerScanCoordinator` with graceful shutdown.
- Forced JVM exit to reclaim native resources.
- Coordinators in `application.orchestrator` encapsulate workflow logic.
- Full refactoring to consistent package-class naming.

---

## Future Work

- Improve in-UI error messages and retry logic.
- Add screenshots and UI mockups to this README.
- Extend i18n beyond EN/FR.
- Automate CI/CD pipeline for build, test and packaging.

