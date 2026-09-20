# Low Cortisol Mods (`lcmods`)

Lightweight, vanilla-friendly quality-of-life features for **single-player Minecraft**, bundled as one small [Fabric](https://fabricmc.net/) mod. Built for **Minecraft 26.3**.

Each feature replaces a per-tick datapack with efficient, event-driven code — no extra entities, no client/server sync, no mixins.

## Features

| Feature | What it does | How to use |
|---|---|---|
| **Coordinates HUD** | XYZ, facing direction and real tool durability, bottom-left | `/coords` to toggle |
| **Waypoint compass** | Arrow, name and distance to your nearest home (current dimension), top-center | `/waypoint` to toggle |
| **Homes / fast-travel** | Save and teleport to named home points | `/sethome [name]`, `/home [name]`, `/homes`, `/delhome <name>` |
| **VeinCapitator** | Sneak + break a log or ore with the matching tool to mine the whole vein at once | Sneak + mine (max 128 blocks, never breaks your tool, respects Fortune/Silk Touch) |
| **Zoom** | OptiFine-style zoom | Hold **C** (rebindable under Options ▸ Controls ▸ *Low Cortisol Mods*) |

`/homes` prints a clickable list with `[TP]` and `[X]` buttons.

## Requirements

- Minecraft **26.3**
- **Fabric Loader** 0.19.5 or newer
- **Fabric API** `0.161.0+26.3` (the installer downloads this for you)
- Java 25 — bundled with the Minecraft launcher; a system Java is only needed for the installer's automatic Fabric-Loader step

## Install (recommended)

Download this repo (**Code ▸ Download ZIP**, then unzip) or clone it, then run the installer for your OS.

### Windows

```powershell
powershell -ExecutionPolicy Bypass -File install.ps1
```

### Linux / macOS

```bash
chmod +x install.sh
./install.sh
```

The installer installs the Fabric-Loader profile for 26.3 (if missing), downloads Fabric API, and copies the mod into your `mods/` folder.
Using a non-default game directory? Pass it along:
`install.ps1 -MinecraftDir "D:\games\.minecraft"` or `./install.sh "/path/to/.minecraft"`.

When it's done, open the Minecraft launcher, select the **`fabric-loader-0.19.5-26.3`** profile and play.

> The launcher shows *"This installation is modified…"* for any Fabric profile. That's a normal, harmless notice — everything works.

## Manual install

1. Install Fabric Loader for Minecraft 26.3: <https://fabricmc.net/use/installer/>
2. Drop these two files into `.minecraft/mods/`:
   - `dist/lcmods-1.1.1.jar` (from this repo)
   - Fabric API for 26.3: <https://modrinth.com/mod/fabric-api>

## Build from source

Requires **JDK 25**.

```bash
./gradlew build        # Windows: .\gradlew.bat build
```

The mod is written to `build/libs/lcmods-<version>.jar`.

## License

[MIT](LICENSE)
