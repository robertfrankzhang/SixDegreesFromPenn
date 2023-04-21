#!/bin/bash
export CLASSPATH=lib/jsoup-1.15.4.jar:.

rm -rf bin/*
javac src/*.java
mv src/*.class bin/

if [ "$1" == "TxtToCsv" ]; then
  java -cp bin:lib/jsoup-1.15.4.jar TxtToCsv
elif [ "$1" == "App" ]; then
  java -cp bin:lib/jsoup-1.15.4.jar App
else
  echo "Usage: ./run.sh [TxtToCsv|App]"
fi