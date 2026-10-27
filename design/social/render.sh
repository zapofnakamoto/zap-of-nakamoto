#!/usr/bin/env sh
# Renders social-preview.html to PNGs (1280×640) with headless Firefox.
set -eu
cd "$(dirname "$0")"
profile=$(mktemp -d)
for v in spark ink paper; do
  firefox --headless --no-remote --profile "$profile" --window-size=1280,640 \
    --screenshot "$PWD/social-preview-$v.png" "file://$PWD/social-preview.html#$v" >/dev/null 2>&1
done
rm -rf "$profile"
