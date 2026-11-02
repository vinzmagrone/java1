#!/usr/bin/env bash
# Le verifiche hanno un main dedicato perché non aggiungiamo librerie di test.
set -euo pipefail
cd "$(dirname "$0")/.."
opzioni=()
if [ -n "${MAVEN_SETTINGS:-}" ]; then
    opzioni=(-s "$MAVEN_SETTINGS")
fi
mvn "${opzioni[@]}" -B package dependency:copy-dependencies
java -cp 'target/test-classes:target/classes:target/dependency/*' it.amdani.school.VerificaProgetto
