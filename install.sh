#!/usr/bin/env bash
#
# Low Cortisol Mods - Linux/macOS installer.
# Installs the Fabric Loader profile for Minecraft 26.3 (if missing),
# downloads Fabric API, and copies the mod into your mods folder.
#
# Usage:
#   ./install.sh
#   ./install.sh "/path/to/.minecraft"
#
set -euo pipefail

MC_VERSION="26.3"
LOADER_VERSION="0.19.5"
FABRIC_API_FILE="fabric-api-0.161.0+26.3.jar"
FABRIC_API_URL="https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0%2B26.3/fabric-api-0.161.0%2B26.3.jar"
INSTALLER_URL="https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.1.2/fabric-installer-1.1.2.jar"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Default Minecraft directory per OS (first argument overrides).
if [ "${1:-}" != "" ]; then
  MC_DIR="$1"
elif [ "$(uname)" = "Darwin" ]; then
  MC_DIR="$HOME/Library/Application Support/minecraft"
else
  MC_DIR="$HOME/.minecraft"
fi

echo "[lcmods] Minecraft directory: $MC_DIR"
[ -d "$MC_DIR" ] || { echo "[lcmods] Minecraft directory not found: $MC_DIR (pass it as the first argument)"; exit 1; }

MOD_JAR="$(ls "$SCRIPT_DIR"/dist/lcmods-*.jar 2>/dev/null | head -n1 || true)"
[ -n "$MOD_JAR" ] || { echo "[lcmods] Could not find dist/lcmods-*.jar next to this script."; exit 1; }

dl() { if command -v curl >/dev/null 2>&1; then curl -sL -o "$2" "$1"; else wget -qO "$2" "$1"; fi; }

# 1) Fabric Loader profile for 26.3
if ls "$MC_DIR"/versions/fabric-loader-*-"$MC_VERSION" >/dev/null 2>&1; then
  echo "[lcmods] Fabric Loader for $MC_VERSION already installed."
elif command -v java >/dev/null 2>&1; then
  echo "[lcmods] Installing Fabric Loader $LOADER_VERSION for $MC_VERSION ..."
  TMP_INST="$(mktemp).jar"
  dl "$INSTALLER_URL" "$TMP_INST"
  java -jar "$TMP_INST" client -dir "$MC_DIR" -mcversion "$MC_VERSION" -loader "$LOADER_VERSION"
  rm -f "$TMP_INST"
  echo "[lcmods] Fabric Loader installed."
else
  echo "[lcmods] No Java found - install Fabric manually: https://fabricmc.net/use/installer/ (Minecraft $MC_VERSION), then re-run."
fi

# 2) mods folder
mkdir -p "$MC_DIR/mods"

# 3) Fabric API
if [ -f "$MC_DIR/mods/$FABRIC_API_FILE" ]; then
  echo "[lcmods] Fabric API already present."
else
  echo "[lcmods] Downloading Fabric API ..."
  dl "$FABRIC_API_URL" "$MC_DIR/mods/$FABRIC_API_FILE"
fi

# 4) the mod
cp -f "$MOD_JAR" "$MC_DIR/mods/"
echo "[lcmods] Installed $(basename "$MOD_JAR")."
echo "[lcmods] Done! Launch the 'fabric-loader-$LOADER_VERSION-$MC_VERSION' profile."
