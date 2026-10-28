#!/usr/bin/env bash
# The version in brand.properties decides what a push to main is. versionName released already (its tag v<versionName>
# exists): not a release, and versionCode must still be that release's. A new versionName: a release, so versionCode
# must be above the latest release's, versionName above its tag, and CHANGELOG.md must have a section for it. Changes
# can wait on main until a commit raises the version. Needs the tags (actions/checkout with fetch-depth: 0).
set -euo pipefail
cd "$(dirname "$0")/../.."
prop() { grep -E "^$1=" "${2:-brand.properties}" | head -1 | cut -d= -f2- | tr -d ' '; }
code_at() { git show "$1:brand.properties" | grep -E '^versionCode=' | cut -d= -f2- | tr -d ' '; }

name="$(prop versionName)"
code="$(prop versionCode)"

if git rev-parse -q --verify "refs/tags/v$name" >/dev/null; then
  released="$(code_at "v$name")"
  if [[ "$code" != "$released" ]]; then
    echo "versionName $name is released already, with versionCode $released, not $code: raise versionName too, or put versionCode back." >&2
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
