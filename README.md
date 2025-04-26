BFVyze - Real-Time Battlefield V analyzer

## Project Overview

BFVyze is a Java 21 desktop application that provides a real-time analyzer for Battlefield V. It captures screenshots of the game window, extracts a server ID via OCR, retrieves server and player data from external APIs, and displays live statistics in a JavaFX interface. The codebase follows SOLID principles, employs a Hexagonal Architecture (Ports and Adapters), and supports internationalization (i18n) for English and French.

---

## Features

- **Server Tab**
   - Press the global hotkey (default F12) to capture a screenshot and extract the short server ID via Tess4J OCR.
   - Retrieve server information from GameTools API (server name, long ID) and BFVHackers API (cheater count).
   - Display cheater count and list of players with basic stats.

- **Stats Tab**
   - Monitor a specific player's overall and session statistics (K/D ratio, kills per minute, accuracy).
   - Automatic refresh every 2 minutes to update live stats.
   - Cache player statistics in memory and persist to JSON on disk (TTL 5 days) to reduce API calls.

- **Settings Tab**
   - Configure monitored player name and global scan hotkey at runtime.
   - Settings persist between sessions in a user-specific configuration file.
   - Validation of hotkey input and dynamic update of UI labels.

- **Resource Management and Cleanup**
   - Graceful shutdown of JNativeHook global key listener and removal of listeners to prevent JVM hang.
   - Dedicated thread pool for asynchronous player queries with a shutdown hook in the application stop method.
   - OCR resources terminated via `BFVOcrFactory.shutdown()`.
   - Application exit forced with `System.exit(0)` to terminate any remaining non-daemon threads.

- **Internationalization (i18n)**
   - ResourceBundle-based support for English (`messages_en.properties`) and French (`messages_fr.properties`).

- **Packaging and Build**
   - Maven build with JavaFX Maven Plugin for development and jpackage for producing a standalone Windows executable.

---

## Project Structure

```
fr.antschw.bfv
├── adapters
│   ├── input        # JavaFX UI controllers and window components
│   ├── window       # Title bar, resize controller, scan control pane
│   └── infrastructure
│       ├── cache    # JSON cache adapter
│       ├── hotkey   # JNativeHook listener adapter
│       └── screenshot # AWT Robot screenshot adapter
├── application
│   ├── service      # Orchestration (ServerScanService, HotkeyListener)
│   └── usecase      # Business logic (PlayerStatsUseCase)
├── common
│   └── constants    # API and UI constants
├── domain
│   ├── model        # Data classes (ServerInfo, UserStats, etc.)
│   └── service      # Service interfaces (OCR, API clients, hotkey)
├── infrastructure
│   └── api          # HTTP clients for GameTools and BFVHackers APIs
├── utils            # I18n utility class
├── resources
│   ├── i18n         # Language bundles
│   └── styles       # CSS stylesheet
└── scripts          # Packaging scripts (package-windows.bat)
```

---

## Setup Instructions

1. **Prerequisites**
   - Java Development Kit 21 (JDK 21)
   - JavaFX SDK 21 (modules: javafx.controls, javafx.fxml)
   - Maven 3.6 or higher
   - Windows OS (for jpackage) or adjust packaging script for your platform.

2. **Clone and Build**
   ```bash
   git clone https://github.com/your-org/bfvyze.git
   cd bfvyze
   mvn clean package
   ```

3. **Run in Development**
   ```bash
   mvn javafx:run
   ```
   - If JavaFX modules are not on the module path, add VM options:
     ```text
     --module-path /path/to/javafx-sdk-21/lib --add-modules javafx.controls,javafx.fxml
     ```

4. **Package Standalone Executable**
   ```bash
   cd src/main/scripts
   package-windows.bat
   ```
   - Result: `bfvyze-setup.exe` in the `target` directory.

---

## Settings and Usage

- **Global Hotkey**: Default F12. Change in Settings tab; the button labels update immediately.
- **Player Name**: Enter the username in Settings. Statistics in the Stats tab will update for this player.
- **Cache Location**: `%USERHOME%/.bfvyze/statsCache.json` for player stats.
- **Log Files**: SLF4J logs to console; configure your own `logback.xml` or similar if needed.

---

## Completed Milestones (Beyond Initial Scope)

- Implemented JSON-based disk cache with automatic save on shutdown and shutdown hook.
- Introduced dedicated thread pool for player stats queries and proper shutdown logic.
- Added forced JVM exit to prevent orphaned AWT or native hook threads.
- Enhanced Settings tab to persist both player name and hotkey configuration.
- Auto-refresh of the Stats tab every 2 minutes.

---

## Testing

- **Unit Tests**: Run with Maven:
  ```bash
  mvn test
  ```
- **Integration Tests**: Add API client mocks to `src/test/java` as needed.

---

## Future Work

- Improve error handling and user feedback in the UI.
- Add screenshot examples and UI mockups to the README.
- Extend language support beyond English and French.
- Automate CI/CD pipeline for builds and packaging.
