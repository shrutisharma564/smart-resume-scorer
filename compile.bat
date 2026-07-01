@echo off
REM Compiles all source files. Make sure lib\mysql-connector-j-*.jar
REM and lib\pdfbox-app-3.0.7.jar are both present in the lib folder before running this.

if not exist bin mkdir bin
if exist sources.txt del sources.txt

for /r src %%f in (*.java) do echo %%f >> sources.txt

javac -d bin -cp "lib/*" @sources.txt
del sources.txt

echo.
echo Done. Run "run.bat" to start the app.
pause
