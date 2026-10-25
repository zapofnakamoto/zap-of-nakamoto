#!/usr/bin/env bash
# Fails if what ships (brand.properties, res/, white-wallet.version) differs from the latest release but the version
# doesn't say so: versionCode must be above the release's, the tag v<versionName> unused, and CHANGELOG.md must have
# a section for versionName. Needs the tags (actions/checkout with fetch-depth: 0).
set -euo pipefail
cd "$(dirname "$0")/../.."
prop() { grep -E "^$1=" "${2:-brand.properties}" | head -1 | cut -d= -f2- | tr -d ' '; }

name="$(prop versionName)"
code="$(prop versionCode)"
grep -qF "## [$name]" CHANGELOG.md || { echo "CHANGELOG.md has no section '## [$name]'." >&2; exit 1; }

last="$(git tag --list 'v*' --sort=-version:refname | head -1)"
if [[ -z "$last" ]]; then
  echo "No release yet; $name ($code) will be the first."
  exit 0
fi
if git diff --quiet "$last" -- brand.properties res white-wallet.version; then
  echo "Nothing that ships changed since $last."
  exit 0
fi
last_code="$(git show "$last:brand.properties" | grep -E '^versionCode=' | cut -d= -f2- | tr -d ' ')"
if git rev-parse -q --verify "refs/tags/v$name" >/dev/null; then
  echo "The app changed since $last, but v$name is already released: set a new versionName (README.md, \"Updates and releases\")." >&2
  exit 1
fi
if (( code <= last_code )); then
  echo "The app changed since $last, but versionCode $code isn't above its $last_code." >&2
  exit 1
fi
echo "$last ($last_code) -> v$name ($code)"
