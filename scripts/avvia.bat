@echo off
rem Compila con Maven e avvia il menu su Windows.
setlocal
rem Usa la cartella del progetto anche se il percorso contiene spazi.
cd /d "%~dp0.."
if errorlevel 1 exit /b 1
rem UTF-8 permette di visualizzare correttamente le lettere accentate.
chcp 65001 >nul
rem call attende il completamento di mvn.cmd senza interrompere questo script.
call mvn -B package dependency:copy-dependencies
if errorlevel 1 exit /b 1
rem Su Windows le voci del classpath sono separate da punto e virgola.
java -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -cp "target/classes;target/dependency/*" it.amdani.school.Main %*
exit /b %errorlevel%
