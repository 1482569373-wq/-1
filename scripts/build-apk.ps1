$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$tools = Join-Path $projectRoot '.tools'
$jdkHome = Join-Path $tools 'jdk-21\jdk-21.0.11+10'
$gradleHome = Join-Path $tools 'gradle-8.9'
$androidSdk = Join-Path $tools 'android-sdk'
$cmdlineLatest = Join-Path $androidSdk 'cmdline-tools\latest'

if (-not (Test-Path (Join-Path $jdkHome 'bin\java.exe'))) {
    throw 'JDK 21 was not found. Run .\scripts\install-dev-tools.ps1 first.'
}
if (-not (Test-Path (Join-Path $gradleHome 'bin\gradle.bat'))) {
    throw 'Gradle was not found. Run .\scripts\install-android-tools.ps1 first.'
}
if (-not (Test-Path (Join-Path $androidSdk 'platforms\android-35'))) {
    throw 'Android SDK platform android-35 was not found. Run .\scripts\install-android-tools.ps1 first.'
}

$env:JAVA_HOME = $jdkHome
$env:GRADLE_HOME = $gradleHome
$env:ANDROID_HOME = $androidSdk
$env:ANDROID_SDK_ROOT = $androidSdk
$env:Path = (Join-Path $jdkHome 'bin') + ';' +
    (Join-Path $gradleHome 'bin') + ';' +
    (Join-Path $androidSdk 'platform-tools') + ';' +
    (Join-Path $cmdlineLatest 'bin') + ';' +
    $env:Path

Set-Location $projectRoot
gradle :androidApp:assembleDebug --no-daemon

$apk = Join-Path $projectRoot 'androidApp\build\outputs\apk\debug\androidApp-debug.apk'
if (Test-Path $apk) {
    Write-Host ''
    Write-Host "APK created: $apk"
}
