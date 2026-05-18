#!/bin/sh
# Run this BEFORE ./gradlew to auto-set JAVA_HOME to JDK 21

set -e

find_java21() {
  # macOS: use java_home utility
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    /usr/libexec/java_home -v 21 2>/dev/null && return
  fi

  # SDKMAN
  if [ -d "$HOME/.sdkman/candidates/java" ]; then
    find "$HOME/.sdkman/candidates/java" -maxdepth 1 -name "21*" | head -1
    return
  fi

  # Linux common paths
  for p in /usr/lib/jvm/java-21-openjdk-amd64 \
            /usr/lib/jvm/java-21-openjdk \
            /usr/lib/jvm/temurin-21; do
    [ -d "$p" ] && echo "$p" && return
  done

  echo ""
}

CURRENT_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)

if [ "$CURRENT_VER" -ge 21 ] 2>/dev/null; then
  echo "✓ Java $CURRENT_VER detected. Good to go."
  exit 0
fi

echo "⚠ Java $CURRENT_VER detected — need 21+. Searching..."
JDK21=$(find_java21)

if [ -n "$JDK21" ]; then
  export JAVA_HOME="$JDK21"
  echo "✓ Found JDK 21 at: $JAVA_HOME"
  echo "  Run: export JAVA_HOME=\"$JAVA_HOME\" && ./gradlew bootRun"
else
  echo "✗ JDK 21 not found. Install it:"
  echo "  SDKMAN:  sdk install java 21-tem && sdk use java 21-tem"
  echo "  Homebrew: brew install --cask temurin@21"
  echo "  Direct:  https://adoptium.net/releases.html?version=21"
  exit 1
fi
