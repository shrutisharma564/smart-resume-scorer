#!/bin/bash
# Compiles all source files. Make sure lib/mysql-connector-j-*.jar
# and lib/pdfbox-app-3.0.7.jar are both present in the lib folder before running this.

mkdir -p bin
javac -d bin -cp "lib/*" $(find src -name "*.java")
echo "Done. Run ./run.sh to start the app."
