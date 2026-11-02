#!/usr/bin/env bash
# Compila con Maven e avvia il menu dalla radice del progetto.
set -euo pipefail
cd "$(dirname "$0")/.."
# MAVEN_SETTINGS è facoltativo: serve negli ambienti con proxy configurato.
opzioni=()
if [ -n "${MAVEN_SETTINGS:-}" ]; then
    opzioni=(-s "$MAVEN_SETTINGS")
fi
mvn "${opzioni[@]}" -B package dependency:copy-dependencies
java -cp 'target/classes:target/dependency/*' it.amdani.school.Main "$@"
