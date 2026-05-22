$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$tools = Join-Path $projectRoot '.tools'
$androidSdk = Join-Path $tools 'android-sdk'
$adb = Join-Path $androidSdk 'platform-tools\adb.exe'
$apk = Join-Path $projectRoot 'androidApp\build\outputs\apk\debug\androidApp-debug.apk'

if (-not (Test-Path $adb)) {
    throw 'adb was not found. Run .\scripts\install-android-tools.ps1 first.'
}
if (-not (Test-Path $apk)) {
    throw 'APK was not found. Run .\scripts\build-apk.ps1 first.'
}

& $adb install -r $apk
