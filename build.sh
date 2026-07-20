#!/bin/bash
# ═══════════════════════════════════════════════════════════════
#  build.sh — Compile and run Gym Management System
#  Usage: bash build.sh
# ═══════════════════════════════════════════════════════════════

set -e   # exit on error

# ── Paths ────────────────────────────────────────────────────
SRC_DIR="src"
OUT_DIR="out"
LIB_DIR="lib"
MAIN_CLASS="com.gym.server.GymServer"
JAR_NAME="gym-server.jar"

echo ""
echo "  ╔══════════════════════════════════════╗"
echo "  ║   IronPulse Gym — Build Script       ║"
echo "  ╚══════════════════════════════════════╝"
echo ""

# ── Verify Java ───────────────────────────────────────────────
if ! command -v javac &> /dev/null; then
  echo "  ✗ javac not found. Install JDK 17+ and add it to PATH."
  exit 1
fi
JAVA_VER=$(javac -version 2>&1 | awk '{print $2}' | cut -d. -f1)
echo "  ✓ Java $JAVA_VER detected"

# ── Verify MySQL driver ───────────────────────────────────────
if [ ! -f "$LIB_DIR/mysql-connector-j.jar" ]; then
  echo ""
  echo "  ✗ MySQL connector JAR not found."
  echo "    Download: https://dev.mysql.com/downloads/connector/j/"
  echo "    Place the JAR as: lib/mysql-connector-j.jar"
  echo ""
  exit 1
fi
echo "  ✓ MySQL connector found"

# ── Compile ───────────────────────────────────────────────────
echo ""
echo "  ▶ Compiling Java sources..."
mkdir -p "$OUT_DIR"

find "$SRC_DIR" -name "*.java" > sources.txt

javac -cp "$LIB_DIR/mysql-connector-j.jar" \
      -d "$OUT_DIR" \
      --source-path "$SRC_DIR" \
      @sources.txt

rm sources.txt
echo "  ✓ Compilation successful"

# ── Package into executable JAR ───────────────────────────────
echo "  ▶ Packaging JAR..."
cat > "$OUT_DIR/manifest.txt" << EOF
Main-Class: $MAIN_CLASS
Class-Path: lib/mysql-connector-j.jar
EOF

jar cfm "$JAR_NAME" "$OUT_DIR/manifest.txt" -C "$OUT_DIR" .
echo "  ✓ JAR created: $JAR_NAME"

# ── Launch ────────────────────────────────────────────────────
echo ""
echo "  ▶ Starting Gym Server..."
echo "    Open browser: http://localhost:8080"
echo "    Press Ctrl+C to stop"
echo ""

java -cp "$JAR_NAME:$LIB_DIR/mysql-connector-j.jar" "$MAIN_CLASS"
