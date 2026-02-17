#!/bin/bash

# Build script for REST API Tester
# This script compiles and packages the application as a standalone macOS app

echo "Building REST API Tester..."

# Cleanup and setup
rm -rf build dist
mkdir -p build/com/apitester dist

# Compile the Java source
javac -d build RestApiTester.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Create JAR file
    jar cfe dist/RestApiTester.jar com.apitester.RestApiTester -C build .
    
    if [ $? -eq 0 ]; then
        echo "JAR file created: dist/RestApiTester.jar"
        
        # Build macOS .app bundle using jpackage
        echo "Creating macOS .app bundle..."
        
        # Try to find jpackage in the path first
        JPACKAGE_PATH=$(which jpackage)
        
        # Fallback to a hardcoded path if not found in PATH
        if [ -z "$JPACKAGE_PATH" ]; then
            JPACKAGE_PATH="/opt/homebrew/Cellar/openjdk/25.0.2/libexec/openjdk.jdk/Contents/Home/bin/jpackage"
        fi
        
        echo "Using jpackage from: $JPACKAGE_PATH"
        
        $JPACKAGE_PATH \
            --type app-image \
            --input dist \
            --dest dist \
            --name RestApiTester \
            --main-jar RestApiTester.jar \
            --main-class com.apitester.RestApiTester \
            --icon RestApiTester.icns \
            --verbose
            
        if [ $? -eq 0 ]; then
            echo "macOS Application created: dist/RestApiTester.app"
            echo ""
            echo "To run the application:"
            echo "  open dist/RestApiTester.app"
            echo ""
            echo "Or run the JAR directly:"
            echo "  java -jar dist/RestApiTester.jar"
        else
            echo "Failed to create macOS bundle"
            exit 1
        fi
    else
        echo "Failed to create JAR file"
        exit 1
    fi
else
    echo "Compilation failed"
    exit 1
fi
