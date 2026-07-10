#!/bin/bash
set -e

echo "=== SmartMart POS Installation ==="

echo "Checking Java version..."
java -version

echo "Checking Maven..."
mvn -version

echo "Building project..."
mvn clean package

if [ -f "target/smartmart-pos.jar" ]; then
    echo "BUILD SUCCESS: target/smartmart-pos.jar created."
else
    echo "BUILD FAILED: JAR not found."
    exit 1
fi

echo "Checking MySQL connection..."
mysql -u root -p -e "SELECT 1;" || {
    echo "WARNING: Could not connect to MySQL. Verify credentials in config.properties."
}

echo "Installation complete. Run './scripts/run.sh' to start the application."