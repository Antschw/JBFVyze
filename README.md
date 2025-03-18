# BFVyze - Real-Time Battlefield V Overlay

## Project Overview
BFVyze is a Java 21-based real-time overlay application for Battlefield V. It displays server statistics and highlights suspicious players. The application uses OCR (Tess4J) to extract server data from screenshots, communicates with external APIs (GameTools, BFVHackers), and provides an intuitive JavaFX GUI. The application adheres to SOLID principles, uses Hexagonal Architecture, supports i18n, and is distributed as a standalone Windows executable.

---

## Features

- Real-time JavaFX overlay with modern UI.
- OCR screenshot analysis using Tess4J.
- API integration with GameTools & BFVHackers.
- Global hotkey detection via JNativeHook.
- Multilingual support.
- Packaged as a standalone `.exe` via jpackage.
- Clean, modular codebase following SOLID and Hexagonal principles.

---

## Project Structure
```
fr.antschw.bfv
├── adapters
│   ├── input (JavaFX, JNativeHook)
│   └── output (HttpClient, Tess4J)
├── application (Service orchestration)
├── config (Constants, i18n)
├── domain (Business logic)
├── ports
│   ├── input (OCR, keyboard interfaces)
│   └── output (API interfaces)
└── Main.java (Launcher)

resources
└── i18n (messages_en.properties, messages_fr.properties)

scripts
└── package-windows.bat
```

---

## Setup Instructions

1. Install Java 21 SDK and JavaFX SDK 21.
2. Clone the repository.
3. Configure JavaFX SDK path if running via IntelliJ (VM Options):
   ```
   --module-path "C:\path\to\javafx-sdk-21\lib" --add-modules javafx.controls,javafx.fxml
   ```
4. Or, build and run via Maven:
   ```bash
   mvn clean javafx:run
   ```
5. Package the application:
   ```bash
   cd scripts
   package-windows.bat
   ```

---

## Development TODO List

### Phase 1: Base Application
- [ ] Implement JavaFX UI with navigation bar (Server, Stats, Settings).
- [ ] Style the UI (semi-opaque background, rounded corners, modern font).
- [ ] Integrate i18n support using ResourceBundle.
- [ ] Centralize constants (window dimensions, colors, etc.).

### Phase 2: User Interaction
- [ ] Integrate JNativeHook to detect global hotkey (e.g., F9).
- [ ] Add dynamic hotkey configuration in Settings tab.

### Phase 3: OCR Capture
- [ ] Implement screenshot capture via java.awt.Robot.
- [ ] Crop & convert to grayscale.
- [ ] Integrate Tess4J OCR adapter.
- [ ] Display extracted server number in the Server tab.

### Phase 4: API Communication
- [ ] Implement HttpClient adapter for GameTools API.
- [ ] Implement HttpClient adapter for BFVHackers API.
- [ ] Parse JSON responses to Java POJOs.
- [ ] Dynamically display API results in Server & Stats tabs.

### Phase 5: Business Logic & Orchestration
- [ ] Implement orchestration service connecting OCR, APIs, and UI.
- [ ] Handle OCR/API errors gracefully in the UI.

### Phase 6: Final Touches
- [ ] Add local persistence of user settings (player name, hotkey).
- [ ] Integrate logging via SLF4J.
- [ ] Generate standalone `.exe` via jpackage.
- [ ] Write complete JavaDoc documentation.

---

## Requirements

- Java 21
- JavaFX 21
- Maven
- Tess4J 5.x
- JNativeHook 2.x
- SLF4J 2.x
- Windows OS
