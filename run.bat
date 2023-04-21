@echo off
set CLASSPATH=lib\jsoup-1.15.4.jar;.

del bin\*
javac src\*.java
move src\*.class bin\

if "%1" == "TxtToCsv" (
  java -cp bin;lib\jsoup-1.15.4.jar TxtToCsv
) else if "%1" == "App" (
  java -cp bin;lib\jsoup-1.15.4.jar App
) else (
  echo Usage: run.bat [TxtToCsv^|App]
)