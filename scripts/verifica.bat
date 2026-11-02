@echo off
rem Compila ed esegue le verifiche senza aggiungere librerie di test.
setlocal
cd /d "%~dp0.."
if errorlevel 1 exit /b 1
chcp 65001 >nul
call mvn -B package dependency:copy-dependencies
if errorlevel 1 exit /b 1
rem Mantiene il codice di uscita: una verifica fallita segnala errore.
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp "target/test-classes;target/classes;target/dependency/*" it.amdani.school.VerificaProgetto
exit /b %errorlevel%
