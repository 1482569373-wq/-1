$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$tools = Join-Path $projectRoot '.tools'
$jdkZip = Join-Path $tools 'temurin-jdk21.zip'
$jdkExtract = Join-Path $tools 'jdk-21'
$mavenZip = Join-Path $tools 'apache-maven-3.9.11-bin.zip'
$mavenExtract = Join-Path $tools 'maven'

New-Item -ItemType Directory -Force -Path $tools | Out-Null

if (-not (Test-Path $jdkExtract)) {
    Write-Host 'Downloading Eclipse Temurin JDK 21...'
    Invoke-WebRequest `
        -Uri 'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse' `
        -OutFile $jdkZip
    New-Item -ItemType Directory -Force -Path $jdkExtract | Out-Null
    Expand-Archive -LiteralPath $jdkZip -DestinationPath $jdkExtract -Force
}

if (-not (Test-Path $mavenExtract)) {
    Write-Host 'Downloading Apache Maven 3.9.11...'
    Invoke-WebRequest `
        -Uri 'https://archive.apache.org/dist/maven/maven-3/3.9.11/binaries/apache-maven-3.9.11-bin.zip' `
        -OutFile $mavenZip
    New-Item -ItemType Directory -Force -Path $mavenExtract | Out-Null
    Expand-Archive -LiteralPath $mavenZip -DestinationPath $mavenExtract -Force
}

$javaExe = Get-ChildItem -Path $jdkExtract -Recurse -Filter java.exe |
    Where-Object { $_.FullName -like '*\bin\java.exe' } |
    Select-Object -First 1
$mvnCmd = Get-ChildItem -Path $mavenExtract -Recurse -Filter mvn.cmd |
    Where-Object { $_.FullName -like '*\bin\mvn.cmd' } |
    Select-Object -First 1

if (-not $javaExe) {
    throw 'java.exe was not found after extracting JDK.'
}
if (-not $mvnCmd) {
    throw 'mvn.cmd was not found after extracting Maven.'
}

$jdkHome = Split-Path -Parent (Split-Path -Parent $javaExe.FullName)
$mavenHome = Split-Path -Parent (Split-Path -Parent $mvnCmd.FullName)

[Environment]::SetEnvironmentVariable('JAVA_HOME', $jdkHome, 'User')
[Environment]::SetEnvironmentVariable('MAVEN_HOME', $mavenHome, 'User')
$env:JAVA_HOME = $jdkHome
$env:MAVEN_HOME = $mavenHome

$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
$pathParts = @()
if ($userPath) {
    $pathParts = $userPath -split ';' | Where-Object { $_ }
}

foreach ($entry in @((Join-Path $jdkHome 'bin'), (Join-Path $mavenHome 'bin'))) {
    if ($pathParts -notcontains $entry) {
        $pathParts += $entry
    }
}

[Environment]::SetEnvironmentVariable('Path', ($pathParts -join ';'), 'User')
$env:Path = (Join-Path $jdkHome 'bin') + ';' + (Join-Path $mavenHome 'bin') + ';' + $env:Path

Write-Host ''
Write-Host 'Installed:'
& $javaExe.FullName -version
& $mvnCmd.FullName -version
Write-Host ''
Write-Host 'Open a new PowerShell window, then run:'
Write-Host '  mvn test'
Write-Host '  mvn javafx:run'
