# Zap of Nakamoto, built from a White Wallet checkout next to this one (or WHITE_WALLET=<dir>) at the tag in
# white-wallet.version. `just` lists the tasks; README.md explains updates and releases.

set shell := ["bash", "-euo", "pipefail", "-c"]

ww := env("WHITE_WALLET", justfile_directory() / ".." / "white-wallet")
brand := justfile_directory()
pinned := trim(read("white-wallet.version"))

# List the tasks
default:
    @just --list --unsorted

# Switch the White Wallet checkout to the pinned tag, or to the pinned branch on white-wallet-next (no local changes)
[group('white wallet')]
checkout:
    git -C {{ ww }} fetch --tags --quiet
    if [[ "{{ pinned }}" == v* ]]; then git -C {{ ww }} switch --detach {{ pinned }}; else git -C {{ ww }} switch {{ pinned }}; fi

# Build the debug APK
[group('build')]
build: pinned-check
    just -f {{ ww }}/justfile build {{ brand }}

# Build the release APK (signed with keystore.properties in the White Wallet checkout or WW_KEYSTORE*)
[group('build')]
release: pinned-check
    just -f {{ ww }}/justfile release {{ brand }}

# Install the debug build on the emulator or chosen device (ANDROID_SERIAL=<id>) and open it
[group('build')]
run: pinned-check
    just -f {{ ww }}/justfile run {{ brand }}

# Install the debug build on the connected phone and open it
[group('build')]
phone: pinned-check
    just -f {{ ww }}/justfile phone {{ brand }}

# Install the debug build on the running emulator and open it
[group('build')]
emulator: pinned-check
    #!/usr/bin/env bash
    set -euo pipefail
    adb="${ADB:-$(command -v adb || echo "$HOME/Android/Sdk/platform-tools/adb")}"
    mapfile -t emulators < <("$adb" devices | awk 'NR > 1 && $2 == "device" && $1 ~ /^emulator-/ { print $1 }')
    case ${#emulators[@]} in
        0) echo "No emulator running. Start one from Android Studio's Device Manager." >&2; exit 1 ;;
        1) echo "Installing on ${emulators[0]}" ;;
        *) echo "Several emulators running (${emulators[*]}); pick one: ANDROID_SERIAL=<id> just run" >&2; exit 1 ;;
    esac
    ANDROID_SERIAL="${emulators[0]}" just -f {{ ww }}/justfile run {{ brand }}

# What CI checks: no brand values in White Wallet's code, and the release build with lint
[group('checks')]
check: pinned-check
    {{ ww }}/dev/check-brand.sh {{ brand }}
    {{ ww }}/gradlew -p {{ ww }} :app:assembleRelease :app:lintDebug -PbrandDir={{ brand }}

# Delete build outputs (they live in the White Wallet checkout)
[group('housekeeping')]
clean: pinned-check
    just -f {{ ww }}/justfile clean

# Warn when the White Wallet checkout isn't at the pinned tag or branch (another version is fine for trying out)
[private]
pinned-check:
    @[[ -d "{{ ww }}" ]] || { echo "No White Wallet checkout at {{ ww }}; clone it there or set WHITE_WALLET." >&2; exit 1; }
    @at="$(git -C {{ ww }} describe --tags --exact-match 2>/dev/null || git -C {{ ww }} branch --show-current)"; [[ "$at" == "{{ pinned }}" ]] || echo "note: {{ ww }} is not at {{ pinned }} (just checkout)" >&2
