#!/bin/bash
# ──────────────────────────────────────────────────────────────
#  run.sh  —  Runs the simulator (compiles first if needed)
# ──────────────────────────────────────────────────────────────

OUT_DIR="out"

if [ ! -d "$OUT_DIR" ] || [ -z "$(ls -A $OUT_DIR 2>/dev/null)" ]; then
    echo "Classes not found — compiling first..."
    bash compile.sh || exit 1
fi

java -cp "$OUT_DIR" Main
