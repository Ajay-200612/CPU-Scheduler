#!/bin/bash
# ──────────────────────────────────────────────────────────────
#  compile.sh  —  Compiles all Java source files into ./out/
# ──────────────────────────────────────────────────────────────

SRC_DIR="src"
OUT_DIR="out"

echo "Compiling CPU Scheduling Simulator..."
mkdir -p "$OUT_DIR"

javac -d "$OUT_DIR" "$SRC_DIR"/*.java

if [ $? -eq 0 ]; then
    echo "✔  Compilation successful! Run with:  ./run.sh"
else
    echo "✘  Compilation failed. Check errors above."
    exit 1
fi
