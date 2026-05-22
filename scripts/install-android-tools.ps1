$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$tools = Join-Path $projectRoot '.tools'
$gradleVersion = '8.9'
$gradleHome = Join-Path $tools "gradle-$gradleVersion"
$androidSdk = Join-Path $tools 'android-sdk'
$cmdlineLatest = Join-Path $androidSdk 'cmdline-tools\latest'
$gradleZip = Join-Path $tools "gradle-$gradleVersion-bin.zip"
$cmdlineZip = Join-Path $tools 'android-commandline-tools.zip'
$cmdlineTemp = Join-Path $tools 'android-commandline-tools-temp'

New-Item -ItemType Directory -Force -Path $tools | Out-Null

# Reuse the local JDK installed for the desktop JavaFX build.
$jdkHome = Join-Path $tools 'jdk-21\jdk-21.0.11+10'
if (-not (Test-Path (Join-Path $jdkHome 'bin\java.exe'))) {
    throw 'JDK 21 was not found. Run .\scripts\install-dev-tools.ps1 first.'
}

if (-not (Test-Path (Join-Path $gradleHome 'bin\gradle.bat'))) {
    Write-Host "Downloading Gradle $gradleVersion..."
    Invoke-WebRequest `
        -Uri "https://services.gradle.org/distributions/gradle-$gradleVersion-bin.zip" `
        -OutFile $gradleZip
    Expand-Archive -LiteralPath $gradleZip -DestinationPath $tools -Force
}

if (-not (Test-Path (Join-Path $cmdlineLatest 'bin\sdkmanager.bat'))) {
    Write-Host 'Finding latest Android command line tools from the official Android Studio page...'
    $cmdlineUrl = 'https://dl.google.com/android/repository/commandlinetools-win-14742923_latest.zip'
    try {
        $studioPage = Invoke-WebRequest -Uri 'https://developer.android.com/studio' -UseBasicParsing
        $match = [regex]::Match($studioPage.Content, '(https://dl\.google\.com/android/repository/commandlinetools-win-[0-9]+_latest\.zip|commandlinetools-win-[0-9]+_latest\.zip)')
        if ($match.Success) {
            if ($match.Value.StartsWith('https://')) {
                $cmdlineUrl = $match.Value
            } else {
                $cmdlineUrl = 'https://dl.google.com/android/repository/' + $match.Value
            }
        }
    } catch {
        Write-Host 'Could not parse developer.android.com/studio; using the current official command line tools URL fallback.'
    }

    Write-Host 'Downloading Android command line tools...'
    Invoke-WebRequest -Uri $cmdlineUrl -OutFile $cmdlineZip

    if (Test-Path $cmdlineTemp) {
        Remove-Item -LiteralPath $cmdlineTemp -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $cmdlineTemp | Out-Null
    Expand-Archive -LiteralPath $cmdlineZip -DestinationPath $cmdlineTemp -Force

    New-Item -ItemType Directory -Force -Path $cmdlineLatest | Out-Null
    $inner = Join-Path $cmdlineTemp 'cmdline-tools'
    Get-ChildItem -LiteralPath $inner -Force | ForEach-Object {
        Move-Item -LiteralPath $_.FullName -Destination $cmdlineLatest -Force
    }
    Remove-Item -LiteralPath $cmdlineTemp -Recurse -Force
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

[Environment]::SetEnvironmentVariable('ANDROID_HOME', $androidSdk, 'User')
[Environment]::SetEnvironmentVariable('ANDROID_SDK_ROOT', $androidSdk, 'User')
[Environment]::SetEnvironmentVariable('GRADLE_HOME', $gradleHome, 'User')

$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
$pathParts = @()
if ($userPath) {
    $pathParts = $userPath -split ';' | Where-Object { $_ }
}
foreach ($entry in @(
        (Join-Path $gradleHome 'bin'),
        (Join-Path $androidSdk 'platform-tools'),
        (Join-Path $cmdlineLatest 'bin'))) {
    if ($pathParts -notcontains $entry) {
        $pathParts += $entry
    }
}
[Environment]::SetEnvironmentVariable('Path', ($pathParts -join ';'), 'User')

$sdkManager = Join-Path $cmdlineLatest 'bin\sdkmanager.bat'
Write-Host 'Accepting Android SDK licenses...'
1..100 | ForEach-Object { 'y' } | & $sdkManager "--sdk_root=$androidSdk" --licenses

Write-Host 'Installing Android SDK packages...'
& $sdkManager "--sdk_root=$androidSdk" 'platform-tools' 'platforms;android-35' 'build-tools;35.0.0'

Write-Host ''
Write-Host 'Installed Android build tools:'
& (Join-Path $gradleHome 'bin\gradle.bat') --version
$adb = Join-Path $androidSdk 'platform-tools\adb.exe'
if (Test-Path $adb) {
    & $adb version
} else {
    Write-Host 'adb was not installed. Re-run this script after SDK license acceptance finishes.'
}
Write-Host ''
Write-Host 'Build the APK with:'
Write-Host '  .\scripts\build-apk.ps1'
