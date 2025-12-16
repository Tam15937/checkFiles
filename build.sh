#!/bin/bash

echo "1. Building JAR..."
mvn clean package

echo "2. Preparing files..."
cp target/file-damage-analyzer.jar .
cp rpm/file-damage-analyzer.service .

echo "3. Creating source archive..."
mkdir -p file-damage-analyzer-1.0.0
cp -r src pom.xml application.properties README.md rpm LICENSE file-damage-analyzer-1.0.0/
tar czf file-damage-analyzer-1.0.0.tar.gz file-damage-analyzer-1.0.0/
rm -rf file-damage-analyzer-1.0.0

echo "4. Creating SRPM..."
gear-rpm

echo "5. Building RPM with hasher..."
hasher-make

echo "Done! RPM packages:"
ls -la x86_64/*.rpm 2>/dev/null || ls -la noarch/*.rpm