#!/bin/bash
# Compile and run Legends of Sword and Wand
# Usage: ./run.sh  (from the project root folder)

set -e
cd "$(dirname "$0")"

echo "Compiling..."
mkdir -p out
find src/main/java -name "*.java" > sources.txt
javac -cp "lib/mysql-connector-j-8.3.0.jar" -d out @sources.txt
rm sources.txt

echo "Running..."
java -cp "out:lib/mysql-connector-j-8.3.0.jar" com.legends.Main
