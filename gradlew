#!/bin/sh
# Gradle wrapper launcher for Workora.
# The pinned distribution is defined in gradle/wrapper/gradle-wrapper.properties.
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
PROPERTIES="$APP_HOME/gradle/wrapper/gradle-wrapper.properties"
DIST_URL=$(sed -n 's/^distributionUrl=//p' "$PROPERTIES" | sed 's/\\:/:/g; s/\\\\/\\/g')
DIST_NAME=$(printf '%s' "$DIST_URL" | sed 's#.*/##')
GRADLE_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_DIR="$GRADLE_HOME/wrapper/dists/$DIST_NAME"
GRADLE_BIN="$DIST_DIR/gradle-9.3.1/bin/gradle"
if [ ! -x "$GRADLE_BIN" ]; then
  mkdir -p "$DIST_DIR"
  ARCHIVE="$DIST_DIR/$DIST_NAME"
  if [ ! -f "$ARCHIVE" ]; then
    if command -v curl >/dev/null 2>&1; then curl --fail --location --retry 3 "$DIST_URL" -o "$ARCHIVE"; else wget -O "$ARCHIVE" "$DIST_URL"; fi
  fi
  TMP="$DIST_DIR/.extract.$$"
  rm -rf "$TMP"
  mkdir "$TMP"
  if command -v unzip >/dev/null 2>&1; then unzip -q "$ARCHIVE" -d "$TMP"; else jar xf "$ARCHIVE"; fi
  mv "$TMP"/gradle-* "$DIST_DIR/gradle-9.3.1"
  rm -rf "$TMP"
fi
exec "$GRADLE_BIN" "$@"
