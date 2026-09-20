<#
  Low Cortisol Mods - Windows installer.
  Installs the Fabric Loader profile for Minecraft 26.3 (if missing),
  downloads Fabric API, and copies the mod into your mods folder.

  Usage:
    powershell -ExecutionPolicy Bypass -File install.ps1
    powershell -ExecutionPolicy Bypass -File install.ps1 -MinecraftDir "D:\games\.minecraft"
#>
param(
    [string]$MinecraftDir = "$env:APPDATA\.minecraft"
)
$ErrorActionPreference = "Stop"

$McVersion     = "26.3"
$LoaderVersion = "0.19.5"
$FabricApiFile = "fabric-api-0.161.0+26.3.jar"
$FabricApiUrl  = "https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/0.161.0%2B26.3/fabric-api-0.161.0%2B26.3.jar"
$InstallerUrl  = "https://maven.fabricmc.net/net/fabricmc/fabric-installer/1.1.2/fabric-installer-1.1.2.jar"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

function Info($m) { Write-Host "[lcmods] $m" -ForegroundColor Cyan }
function Ok($m)   { Write-Host "[lcmods] $m" -ForegroundColor Green }
function Warn($m) { Write-Host "[lcmods] $m" -ForegroundColor Yellow }

Info "Minecraft directory: $MinecraftDir"
if (-not (Test-Path $MinecraftDir)) {
    throw "Minecraft directory not found: $MinecraftDir  (pass -MinecraftDir <path>)"
}

$modJar = Get-ChildItem -Path (Join-Path $scriptDir "dist") -Filter "lcmods-*.jar" -ErrorAction SilentlyContinue |
          Select-Object -First 1
if (-not $modJar) {
    throw "Could not find dist\lcmods-*.jar next to this script."
}

# 1) Fabric Loader profile for 26.3
if (Test-Path (Join-Path $MinecraftDir "versions\fabric-loader-*-$McVersion")) {
    Ok "Fabric Loader for $McVersion already installed."
} else {
    Info "Installing Fabric Loader $LoaderVersion for $McVersion ..."
    $java = $null
    if (Get-Command java -ErrorAction SilentlyContinue) { $java = "java" }
    elseif ($env:JAVA_HOME -and (Test-Path "$env:JAVA_HOME\bin\java.exe")) { $java = "$env:JAVA_HOME\bin\java.exe" }

    if ($java) {
        $installer = Join-Path $env:TEMP "lcmods-fabric-installer.jar"
        Invoke-WebRequest -Uri $InstallerUrl -OutFile $installer
        & $java -jar $installer client -dir $MinecraftDir -mcversion $McVersion -loader $LoaderVersion
        if ($LASTEXITCODE -ne 0) { throw "Fabric installer failed (exit $LASTEXITCODE)." }
        Remove-Item $installer -ErrorAction SilentlyContinue
        Ok "Fabric Loader installed."
    } else {
        Warn "No Java found - cannot auto-install Fabric Loader."
        Warn "Install it manually: https://fabricmc.net/use/installer/ (Minecraft $McVersion), then re-run this script."
    }
}

# 2) mods folder
$modsDir = Join-Path $MinecraftDir "mods"
New-Item -ItemType Directory -Force -Path $modsDir | Out-Null

# 3) Fabric API
$apiTarget = Join-Path $modsDir $FabricApiFile
if (Test-Path $apiTarget) {
    Ok "Fabric API already present."
} else {
    Info "Downloading Fabric API ..."
    Invoke-WebRequest -Uri $FabricApiUrl -OutFile $apiTarget
    Ok "Fabric API installed."
}

# 4) the mod
Copy-Item $modJar.FullName (Join-Path $modsDir $modJar.Name) -Force
Ok "Installed $($modJar.Name)."

Ok "Done! Launch Minecraft with the 'fabric-loader-$LoaderVersion-$McVersion' profile."
