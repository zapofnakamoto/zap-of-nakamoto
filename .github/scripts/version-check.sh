#!/usr/bin/env bash
# The version in brand.properties decides what a push to main is. app.version.name released already (its tag
# v<app.version.name> exists): not a release, and app.version.code must still be that release's. A new
# app.version.name: a release, so app.version.code must be above the latest release's, app.version.name above its tag,
# and CHANGELOG.md must have a section for it. Changes can wait on main until a commit raises the version. Needs the
# tags (actions/checkout with fetch-depth: 0).
#
# main (and a pull request into it) must pin a White Wallet release tag; only white-wallet-next pins a branch, so
# merging it into main without moving the pin to a release fails here instead of shipping an unreleased White Wallet.
set -euo pipefail
cd "$(dirname "$0")/../.."

pin="$(tr -d '[:space:]' < white-wallet.version)"
target="${GITHUB_BASE_REF:-${GITHUB_REF_NAME:-$(git branch --show-current)}}"
if [[ "$target" == main && ! "$pin" =~ ^v[0-9] ]]; then
  echo "white-wallet.version is '$pin', but main must pin a White Wallet release tag (v…)." >&2
  exit 1
fi
prop() { grep -E "^$1=" "${2:-brand.properties}" | head -1 | cut -d= -f2- | tr -d ' '; }
# Releases before White Wallet grouped its keys called it versionCode.
code_at() { git show "$1:brand.properties" | grep -E '^(app\.version\.code|versionCode)=' | head -1 | cut -d= -f2- | tr -d ' '; }

name="$(prop app.version.name)"
code="$(prop app.version.code)"

if git rev-parse -q --verify "refs/tags/v$name" >/dev/null; then
  released="$(code_at "v$name")"
  if [[ "$code" != "$released" ]]; then
    echo "app.version.name $name is released already, with app.version.code $released, not $code: raise app.version.name too, or put app.version.code back." >&2
    exit 1
  fi
  echo "v$name is released already; this is not a release."
  exit 0
fi

grep -qF "## [$name]" CHANGELOG.md || { echo "CHANGELOG.md has no section '## [$name]'." >&2; exit 1; }
last="$(git tag --list 'v*' --sort=-version:refname | head -1)"
if [[ -z "$last" ]]; then
  echo "No release yet; $name ($code) will be the first."
  exit 0
fi
if [[ "$(printf '%s\n%s\n' "${last#v}" "$name" | sort -V | tail -1)" != "$name" ]]; then
  echo "versionName $name isn't above the latest release, $last." >&2
  exit 1
fi
last_code="$(code_at "$last")"
if (( code <= last_code )); then
  echo "versionCode $code isn't above $last's $last_code." >&2
  exit 1
fi
echo "$last ($last_code) -> v$name ($code): a release."
