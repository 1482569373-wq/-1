# JavaFX Snake

A simple Snake game built with Java 21, JavaFX, Maven, and an Android APK module.

## Run Desktop

Install JDK 21 and Maven, then run:

```powershell
mvn javafx:run
```

You can also run through the project-local helper script:

```powershell
.\scripts\run-game.ps1
```

If Java and Maven are not installed, run the bundled installer script in PowerShell:

```powershell
.\scripts\install-dev-tools.ps1
```

Open a new PowerShell window after the script finishes so `JAVA_HOME`, `MAVEN_HOME`, and `Path` are refreshed.

## Test

```powershell
mvn test
```

Or:

```powershell
.\scripts\run-tests.ps1
```

## Android APK

Install the Android command line build tools:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install-android-tools.ps1
```

Build the debug APK:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-apk.ps1
```

The APK is created at:

```text
androidApp\build\outputs\apk\debug\androidApp-debug.apk
```

To install it on a connected Android phone with USB debugging enabled:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\install-apk.ps1
```

## Difficulty

- Simple: 180ms per move
- Normal: 120ms per move
- Hard: 80ms per move

## Desktop Controls

- `1`, `2`, `3`: choose difficulty from menu or game-over screen
- `Enter` or `Space`: start/retry current difficulty
- Arrow keys or `WASD`: move
- `Space`: pause or resume while playing
- `R`: restart current difficulty
- `M` or `Esc`: return to menu from pause/game-over screen

## Android Controls

- Tap a difficulty button to start
- Swipe on the screen or use the direction buttons to move
- Tap Pause/Continue to pause or resume
- Tap Restart to restart current difficulty
- After game over, tap any difficulty button to start again
