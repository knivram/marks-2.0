@echo off
REM Überprüfen, ob der Dateiname als erster Parameter übergeben wurde
IF "%~1"=="" (
    echo Dateiname nicht angegeben.
    exit /b
)

REM Überprüfen, ob der Dateiname einen "\" enthält und diesen ersetzen
SET "FILENAME=%~1"
SET "REPLACED_FILENAME=%FILENAME:\=/%"

REM Schritt 1: bash ausführen
bash --version
IF %errorlevel% neq 0 (
    echo bash ist nicht installiert oder nicht im Pfad.
    exit /b
)

REM Schritt 2-4: Befehle an Bash-Konsole übergeben
echo Aktualisiere apt...
bash -c "dos2unix '%REPLACED_FILENAME%' || apt update && apt install -y dos2unix && dos2unix '%REPLACED_FILENAME%'"
IF %errorlevel% neq 0 (
    echo Ein Fehler ist aufgetreten.
    exit /b
)

echo Fiel wurde erfolgreich formatiert.
exit /b
