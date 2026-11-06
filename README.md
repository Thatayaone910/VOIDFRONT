# VOIDFRONT

VOIDFRONT is a personal refactoring of a collaborative educational Robot World project into a multiplayer, 2D space-combat simulation. It keeps the original client/server design and implemented mechanics while updating domain names, protocol terminology, and presentation. It does not add combat or environmental mechanics that were not present in the source project.

## Implemented gameplay

- TCP server supporting concurrent client connections and one shared sector per server.
- JSON request/response messages over Java data streams.
- Starship deployment with unique callsigns, supported ship classes, and unoccupied spawn positions.
- Cardinal heading, rotation, thrust, and reverse movement.
- Sector boundaries, collision handling, and asteroid, nebula, and void-rift hazards.
- Scans for aligned starships, hazards, and sector edges.
- Firing, shields, ammunition rearming, and shield repair.
- Command-line client and JavaFX client.

The only currently configured ship class is `sniper`. Coordinates use **north = +Y, south = -Y, east = +X, west = -X**.

## Requirements

- JDK 25
- Apache Maven

Check the versions available in PowerShell:

```powershell
java -version
mvn -version
```

The Maven compiler uses `--release 25`.

## Build and test

From the repository root:

```powershell
mvn clean verify
```

## Run the game

Open two PowerShell terminals in the repository root.

In the first terminal, start the server:

```powershell
mvn exec:java '-Dexec.mainClass=io.github.thatayaone910.voidfront.server.ServerMain'
```

The server listens on port `5000`. Its console accepts `dump`, `starships`, and `quit`.

In the second terminal, start **one** of the clients.

JavaFX client:

```powershell
mvn exec:java '-Dexec.mainClass=io.github.thatayaone910.voidfront.client.AppLauncher' '-Dexec.args=--gui localhost 5000'
```

Command-line client:

```powershell
mvn exec:java '-Dexec.mainClass=io.github.thatayaone910.voidfront.client.ClientMain' '-Dexec.args=localhost 5000'
```

The JavaFX client asks for a callsign and ship class. The CLI client starts with `deploy sniper <callsign>`. Once deployed, both clients support `state`, `scan`, `heading`, `thrust`, `reverse`, `rotate`, `fire`, `rearm`, `repair`, and `quit`.

The server loads `sector.properties` from the classpath. `SectorConfig` also accepts an external `.properties` file path and falls back to default settings if the file cannot be loaded.

## Protocol

Each request is a JSON object with `callsign`, `command`, and an `arguments` array. Commands include `deploy`, `state`, `scan`, `heading`, `thrust`, `reverse`, `rotate`, `fire`, `rearm`, `repair`, and `quit`. Scan contacts use the types `STARSHIP` and `HAZARD`. Client and server exchange each JSON message in a length-prefixed UTF frame.

## Project layout

```text
src/main/java/io/github/thatayaone910/voidfront/
├── client/       # CLI, JavaFX UI, and socket client
├── protocol/     # JSON command request models
├── sector/       # Sector configuration, starship and position models
│   └── hazard/   # Asteroids, nebulae, and void rifts
├── server/       # TCP listener and per-client request handling
└── service/      # Deployment, navigation, query, combat, and quit behavior
src/main/resources/
├── sector.properties
├── Voidfront.fxml
└── icons/        # UI and in-game sprites; source-art/ preserves the original artwork
```

New starship, hazard, and blast sprites are included for VOIDFRONT. The original sprites are preserved under `icons/source-art/` to retain the collaborative source project's artwork.

## Project provenance

VOIDFRONT is a personal refactoring project based on a collaborative educational codebase. Applicable upstream licensing and attribution requirements must be preserved and verified. This project does not claim individual authorship of the original work.
