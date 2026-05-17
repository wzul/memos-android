#!/bin/bash
set -euo pipefail

REPO="wzul/memos-android"
ARTIFACT_NAME="app-debug"

echo "Fetching latest successful build..."
RUN_ID=$(gh run list --repo "$REPO" --workflow "Build APK" --status completed --json databaseId --jq '.[0].databaseId')

echo "Downloading APK artifact from run $RUN_ID..."
TMPDIR=$(mktemp -d)
gh run download "$RUN_ID" --repo "$REPO" --name "$ARTIFACT_NAME" --dir "$TMPDIR"

APK=$(find "$TMPDIR" -name "*.apk" | head -n 1)
if [ -z "$APK" ]; then
    echo "No APK found in artifact"
    exit 1
fi

echo "Installing to connected device via ADB..."
adb install -r "$APK"

echo "Installed successfully: $APK"
rm -rf "$TMPDIR"
