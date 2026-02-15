#!/bin/bash

# Build script for REST API Tester
# This script compiles and packages the application

echo "Building REST API Tester..."

# Navigate to source directory
cd src/main/java

# Compile the Java source
javac com/apitester/RestApiTester.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    
    # Create JAR file
    jar cfe RestApiTester.jar com.apitester.RestApiTester com/apitester/RestApiTester.class
    
    if [ $? -eq 0 ]; then
        echo "JAR file created: RestApiTester.jar"
        echo ""
        echo "To run the application:"
        echo "  java -jar RestApiTester.jar"
        echo ""
        echo "Or run directly:"
        echo "  java com.apitester.RestApiTester"
    else
        echo "Failed to create JAR file"
        exit 1
    fi
else
    echo "Compilation failed"
    exit 1
fi
