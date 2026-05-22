$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot
$env:JAVA_HOME = Join-Path $projectRoot '.tools\jdk-21\jdk-21.0.11+10'
$env:MAVEN_HOME = Join-Path $projectRoot '.tools\maven\apache-maven-3.9.11'
$env:Path = (Join-Path $env:JAVA_HOME 'bin') + ';' + (Join-Path $env:MAVEN_HOME 'bin') + ';' + $env:Path

Set-Location $projectRoot
mvn test
