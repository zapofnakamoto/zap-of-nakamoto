# Zap of Nakamoto

The simplest bitcoin wallet built on [White Wallet](https://github.com/zapofnakamoto/white-wallet), by configuration and a
little design code: this repository is the whole app. It holds White Wallet's brand files (`brand.properties`, `res/`),
the brand's look in code (`code/`: the living mark and its components, nothing about money), the Zapstore listing and
the workflows that keep it current and publish it. The design it follows is [`design/DESIGN.md`](design/DESIGN.md).

**Who it's for:** absolute beginners who want to send and receive bitcoin, and don't care how LNURLcash works. (An
app for experts and power users would be a different brand.)

> **Beta until 1.0.0.** Zap of Nakamoto is not ready for everyday money yet. Until version 1.0.0, any update may
> change how the app works or break what it did before, and bugs can cost you what's in the wallet. Use it only to
> try it out, with tiny amounts you could lose without minding.

## Product rules

| Rule | How (brand.properties) |
|---|---|
| One service, chosen for the user, another if they want | `defaultServiceHost=mint.lnurlcash.com` is preselected at setup and restore; `allowServiceChange=true` lets users pick a custom one there instead (no suggestions) |
| One trusted service | the home mint, whichever it is, is trusted by default; Settings → Services (`mints.visible=true`) shows it, and others can be trusted by hand |
| One account | `accounts.max=1`: no switcher, no "Add account" |
| One Lightning address, asked for at setup | `addresses.lightning=optional`, `addresses.max=1` |
| One note per service, kept automatically | `notes.strategies=single`, `notes.strategy.default=single` (it also renews the address's notes) |
| Notes from elsewhere come home by themselves | `notes.collect.atHomeMint=always` (fee cap 2 %) |
| Nothing to manage by hand | `notes.view=hidden` (no Notes tab or page); Note settings disappear by themselves, as there is nothing to choose. Services are there to look at and change (`mints.visible=true`), never needed |
| One unit, no choice | `amounts.unit.default=bitcoin` (₿1,000; texts say "bitcoin", never "BTC" or "sats"), `amounts.unit.changeable=false` (no Amounts page) |
| No developer tools in sight | `developer.options=hidden`: never in release builds (debug builds unlock them by tapping the version in About seven times) |
| Plain words | the custodian is "a Lightning service" (`res/values*/strings.xml`), slogan "Just zap it." |

What users see: setup (pick a name), the wallet (balance, Send, Receive), History, and Settings with their recovery
phrase, address, their service (and other services), security and About.

## Files

| File | What |
|---|---|
| `brand.properties` | the rules above, name, application ID (`io.github.zapofnakamoto`, final) and version |
| `res/drawable*/ic_brand_logo.xml` | the mark (a bolt with a cut-out face), ink on light and spark on dark; written by `dev/mark.py` |
| `res/drawable/ic_launcher_foreground.xml` | the mark for the launcher in one colour, so themed icons work; written by `dev/mark.py` |
| `res/values/zap_colors.xml` | the Spark palette: spark, ink, paper, gold |
| `res/values*/brand_colors.xml` | the palette mapped onto White Wallet's colour roles: paper and ink on light, ink and spark on dark |
| `res/font/`, `res/values/zap_type.xml` | Unbounded (balance, headlines) and Figtree (everything else), with their licences in `fonts/` |
| `res/values*/strings.xml` | slogan, first-run texts, the brand term and Zap's voice, in English and German |
| `code/` | `ZapDesign`: the living mark (it reacts to the wallet's status and payments), the celebrations on the success screens, the wordmark, spark pill buttons |
| `dev/mark.py` | writes the mark's drawables above; run it after changing the mark |
| `design/` | the brand design: `DESIGN.md` (palette, type, the mark, its states and motion, voice), the reference SVGs and their generator (`assets/`), the interactive prototype (`prototype/zap-states.html`, open it in a browser) and the earlier rounds (`explorations/`) |
| `white-wallet.version` | the White Wallet release this app is built from (a tag, e.g. `v0.1.0`) |
| `zapstore.yaml`, `zapstore/` | the store listing and its screenshots |
| `CHANGELOG.md` | release notes, one section per version; Zapstore shows the released one |

## Run it

The builds need a White Wallet checkout next to this one (`../white-wallet`, or `WHITE_WALLET=<dir>`) at the pinned
tag: `just checkout` switches it there.

```sh
just phone      # build the debug APK, install it on the connected phone and open it
just emulator   # the same on the running emulator
just run        # the same on a chosen device (ANDROID_SERIAL=<id>)
just build      # only the debug APK
just release    # the release APK (signed with keystore.properties or WW_KEYSTORE*, see below)
just check      # brand check and release build, as CI does
```

In Android Studio (or IntelliJ), the shared run configurations in `.run/` do the same: "Run on device"
(`just phone`) and "Run on emulator" (`just emulator`).

The debug build installs as `io.github.zapofnakamoto.debug`, next to a release build and other White Wallet brands.

## Updates and releases

Everything that ships is decided here; CI only carries it out.

- **A new White Wallet release** (a `v*` tag there): `update.yml` checks daily and opens a pull request that moves
  `white-wallet.version` to it (`chore(deps): white-wallet v…`) and then, in a commit of its own
  (`chore(release): v…`), raises `app.version.name` (minor for a new White Wallet minor or major, patch for a White Wallet
  patch), raises `app.version.code` by one and adds a `CHANGELOG.md` section with its release notes. Keys White Wallet adds
  come with defaults, so a brand rarely needs more than that; a renamed or removed key makes the pull request's build
  fail instead of the app.
- **A change to the brand itself** (texts, colors, a rule): its commits add to `## [Unreleased]` in `CHANGELOG.md`;
  a commit of its own (`chore(release): v…`) then raises `app.version.code` and `app.version.name` (patch for fixes and
  wording, minor for anything new) and names that section.
- **Versions** are the app's own [semver](https://semver.org/), independent of White Wallet's (each `CHANGELOG.md`
  section says which White Wallet it's on). Until 1.0.0 the app is a beta and any release may break things; 1.0.0
  is a decision made here, never a White Wallet update's.
- **CI** (`ci.yml`) builds and checks every pull request against the pinned White Wallet. Changes can wait on main
  unreleased: while `app.version.name` is a released one, a push is no release (and `app.version.code` must stay that
  release's). A new `app.version.name` needs a higher `app.version.code` and its `CHANGELOG.md` section.
- **Release** (`release.yml`): every push to main whose `v<versionName>` tag doesn't exist yet is released: the signed
  APK is built, its certificate checked, and a GitHub release `v<versionName>` created with the APK and its checksum.
  Zapstore picks the release up from there. A push that doesn't raise `app.version.name` releases nothing. If the
  release job fails, re-run that job in Actions; a new push would skip the version, as its tag exists by then.

### Set up once

1. **The signing key**, only for this app. Losing it ends updates under `io.github.zapofnakamoto`: keep two offline
   backups.
   ```sh
   keytool -genkeypair -v -keystore zap-release.jks -alias zap -keyalg RSA -keysize 4096 -validity 10000
   keytool -list -v -keystore zap-release.jks -alias zap | grep SHA256   # the fingerprint for ZAP_CERT_SHA256
   ```
2. **The publisher's Nostr identity**: a key only for publishing, held by a NIP-46 bunker (nsec.app or a self-hosted
   nsecbunkerd), here npub1k98clftxkp4tmce4wp7evmczymhl0cqly2qgjwshdv3uxj73d8ysv04zap. Zapstore's relay must admit a
   new key before it can publish; ask Zapstore if `zsp` says the key isn't allowed.
3. **GitHub**, in the environment `zapstore`:
   - secrets `ZAP_KEYSTORE_BASE64` (`base64 -w0 zap-release.jks`), `ZAP_KEYSTORE_PASSWORD`, `ZAP_KEY_ALIAS`,
     `ZAP_KEY_PASSWORD`;
   - variable `ZAP_CERT_SHA256` (the fingerprint, colons optional).

   Repository variable `UPDATE_APP_ID`, with the secret
   `UPDATE_APP_KEY`: a GitHub App allowed to write contents and pull requests here, so the update pull requests run
   CI (pull requests opened with the workflow's own token don't).

   Until then nothing fails: `update.yml` skips without `UPDATE_APP_ID`.

### First release

1. Take the screenshots into `zapstore/` and list them under `images:` in `zapstore.yaml`.
2. `just check`, then push to main: `release.yml` creates the GitHub release with the APK.
3. On your machine, run `zsp` here with the publisher's bunker and choose "Publish now". It publishes the listing and
   the release, and the proof (NIP-C1) that ties the signing certificate to the publisher, so Zapstore takes later
   releases from GitHub as the publisher's. Saving `.env` is optional: it is git-ignored, but holds the bunker
   secret in plain text.
4. After each later release, `zsp check` shows whether Zapstore lists it, and until when the proof is valid; run `zsp`
   again if a release isn't listed or before the proof expires.

`release.yml` can also publish to Zapstore itself, with the repository variable `ZAPSTORE_PUBLISH=true` and the
secret `ZAPSTORE_BUNKER` (the bunker's `bunker://…` URL, its CI connection allowed to sign without asking). Its zsp
step is written for zsp 0.5 and needs moving to zsp v2 first.
