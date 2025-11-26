#!/bin/bash
# University ERP - Compilation Script
# This script compiles all Java source files

echo "======================================"
echo "University ERP - Build Script"
echo "======================================"
echo ""

# Check if bin directory exists
if [ ! -d "bin" ]; then
    echo "Creating bin directory..."
    mkdir -p bin
fi

echo "Compiling all Java files..."
echo ""

# Compile all source files
javac -d bin \
    src/edu/univ/erp/Main.java \
    src/edu/univ/erp/util/*.java \
    src/edu/univ/erp/auth/*.java \
    src/edu/univ/erp/data/*.java \
    src/edu/univ/erp/access/*.java \
    src/edu/univ/erp/domain/*.java \
    src/edu/univ/erp/service/*.java \
    src/edu/univ/erp/ui/auth/*.java \
    src/edu/univ/erp/ui/admin/*.java \
    src/edu/univ/erp/ui/instructor/*.java \
    src/edu/univ/erp/ui/student/*.java \
    src/edu/univ/erp/ui/common/*.java 2>&1

# Check compilation result
if [ $? -eq 0 ]; then
    echo ""
    echo "✓ Compilation successful!"
    echo ""
    echo "To run the application:"
    echo "  cd bin"
    echo "  java -cp .:<postgresql-driver>.jar edu.univ.erp.Main"
    echo ""
else
    echo ""
    echo "✗ Compilation failed!"
    echo "Please check errors above."
    exit 1
fi

echo "======================================"
echo "Build Complete"
echo "======================================"
