# Desktop release / distribution (Windows)

This project uses the Gradle `org.beryx.jlink` plugin to build a JavaFX **app-image** via `jpackageImage`.

## What to ship (portable folder)

After building, copy **the whole app-image folder**:

- `Desktop/build/jpackage/ClinicAssistant/`
  - `ClinicAssistant.exe`
  - `app/`
  - `runtime/`

Your app expects your tools folder next to the EXE:

- `ExternalTools/`

Recommended final layout for users:

```
ClinicAssistant/
  ClinicAssistant.exe
  app/
  runtime/
  ExternalTools/
```

## Build a new release

1) (Optional) bump version in `Desktop/build.gradle.kts`:

- `version = "1.0-SNAPSHOT"`  -> e.g. `"1.0.1"`

2) Build the app-image:

```powershell
cd D:\ClinicAssistant
.\gradlew.bat :Desktop:clean :Desktop:jpackageImage
```

Outputs:

- `Desktop/build/jpackage/ClinicAssistant/`

## Create a zip to publish

You can zip the folder yourself, or use the helper task `:Desktop:releaseZip` (added in `Desktop/build.gradle.kts`).

```powershell
cd D:\ClinicAssistant
.\gradlew.bat :Desktop:releaseZip
```

Outputs:

- `Desktop/build/release/ClinicAssistant-<version>-windows.zip`

## Notes

- `jpackage` *installer* (`.msi` / installer `.exe`) requires external tooling on Windows (WiX). This repo currently ships a portable app-image instead.
- Keep the `app/` and `runtime/` folders together with the `.exe` or the launcher won’t work.

