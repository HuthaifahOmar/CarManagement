$ErrorActionPreference = "Stop"

$base = Split-Path -Parent $MyInvocation.MyCommand.Path
$javaFxBase = "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-base\21\javafx-base-21-win.jar"
$javaFxGraphics = "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-graphics\21\javafx-graphics-21-win.jar"
$javaFxControls = "$env:USERPROFILE\.m2\repository\org\openjfx\javafx-controls\21\javafx-controls-21-win.jar"
$modulePath = "$javaFxBase;$javaFxGraphics;$javaFxControls"
$out = Join-Path $base "out"

New-Item -ItemType Directory -Force -Path $out | Out-Null
$sources = Get-ChildItem -LiteralPath $base -Filter *.java | ForEach-Object { $_.FullName }
$compileArgs = @("--module-path", $modulePath, "--add-modules", "javafx.controls", "-d", $out) + $sources
& javac @compileArgs
$runArgs = @("--module-path", $modulePath, "--add-modules", "javafx.controls", "-cp", $out, "CarProjDS2.MainApp")
& java @runArgs
