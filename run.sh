#!/bin/bash

# =============================================================================
# Configuration
# =============================================================================

# Instructions:
# 1. Download JavaFX SDK: https://gluonhq.com/products/javafx/
# 2. Set the PATH_TO_FX variable below to point to the 'lib' folder of your SDK.
#    Example: /Users/username/javafx-sdk-21/lib

# Check if environment variable is set, otherwise use a placeholder
if [ -z "$PATH_TO_FX" ]; then
    echo "Error: PATH_TO_FX environment variable is not set."
    echo "Please run: export PATH_TO_FX=/path/to/your/javafx-sdk/lib"
    exit 1
fi

# =============================================================================
# Compilation
# =============================================================================

echo "Cleaning previous build..."
rm -rf bin
mkdir bin

echo "Compiling source code..."
# Compiles all java files in src/application
# -d bin: Outputs class files to 'bin' folder
# --module-path: Links JavaFX libraries
# --add-modules: Adds specifically required modules (Controls for UI, Media for Music)
javac --module-path "$PATH_TO_FX" \
      --add-modules javafx.controls,javafx.media \
      -d bin \
      src/application/*.java

# Check if compilation succeeded
if [ $? -ne 0 ]; then
    echo "Compilation failed! Please check your JavaFX path and code."
    exit 1
fi

# =============================================================================
# Execution
# =============================================================================

echo "Starting Space Shooter..."
# Runs the application.Main class
# -cp bin: Looks for compiled classes in 'bin'
java --module-path "$PATH_TO_FX" \
     --add-modules javafx.controls,javafx.media \
     -cp bin \
     application.Main