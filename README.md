# Noten System
Das Noten-System soll die momentane Handhabung der Noten aller Lernenden auf Confluence ablösen.
## Local-Dev-Environment Infrastruktur
![Environment Setup Diagram](https://confluence.ergon.ch/rest/gliffy/1.0/embeddedDiagrams/7fa45f1a-2cfb-43bf-b7d5-1c6d579bd4c1.png)
## Environment Setup
### 1. Projekt Klonen
```bash
git clone https://stash.ergon.ch/scm/lehrlinge/marvin.studentmarks.git
```
oder
```bash
git clone ssh://git@stash.ergon.ch:7999/lehrlinge/marvin.studentmarks.git
```
### 2. DB starten
   Öffne die Konsole und navigiere in den Ordner backend. Führe danach diesen Befehl aus, welcher die Datenbank erstellt und initialisiert.
```bash
cd ./backend
docker compose -f ./docker-compose-db-only.yml up -d
```
Das "-d" steht für "detach" und bewirkt, dass der Befehl im Hintergrund ausgeführt wird. So wird das Terminal nicht blockiert.
Beim Ausführen dieses Befehls kann dieser Fehler auftreten.
```
/bin/bash^M: bad interpreter: No such file or directory
```
Ist dies der Fall, dann navigiere mit deinem Terminal in den Ordner dockerData und führe folgenden Befehl aus.
```bash
cd ./dockerData
.\formateToUnix.bat .\create-multiple-postgresql-databases.sh
```
Wichtig: für das Ausführen dieses Befehls muss WSL installiert sein. [Zur Anleitung](https://learn.microsoft.com/de-de/windows/wsl/install).

### 3. Backend starten
Versuche nun das Backend zu starten. Im Falle, dass Intellij keine Möglichkeit für das Starten der Applikation anzeigt, kannst du in die Klasse `StudentMarksApplication.kt` gehen und dort auf das Start-Icon neben der main-Funktion drücken.

Im Notfall kannst du auf Projekt-Root-Ebene diesen Befehl für das Starten des Backends nutzen.
```bash
./gradlew bootRun
```
### 4. Frontend starten

Kopieren als Erstes die Datei defaultSetupProxy.js aus dem Hauptordner des Projekts.

```bash
cp defaultSetupProxy.js frontend/src/setupProxy.js
```

Bevor du das Frontend starten kannst, muss erst sichergestellt werden, dass alle Dependencies installiert sind. Navigiere dazu in den Ordner Frontend und starte folgenden Befehl.
```bash
cd ./frontend
npm install
```
Danach kannst du das Frontend mit einem weiteren Befehl zum ersten Mal starten.
```bash
cd ./frontend
npm run dev
```
### 5. Frontend-User ändern
Jetzt sollte das Projekt lokal auf deinem Rechner laufen. Es gibt aber noch etwas, das für das Testen in der Entwicklungsumgebung wichtig ist.

Navigiere in den Ordner **src** im **frontend**. In diesem findest du das, im letzten Schritt kopierte, File `setupProxy.js`. Dieses File ist für die Weiterleitung der API-Calls an das Backend zuständig. Ausserdem ist es für die Bereitstellung des momentanen Users in der Test-Umgebung zuständig. Es hat drei vorgegebene User. Zwischen diesen kannst du wechseln, in dem du die doppelten Backslashs löschst und den React-Development-Server neu startest.
