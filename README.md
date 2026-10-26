# Zap of Nakamoto

The simplest bitcoin wallet built on [White Wallet](https://github.com/zapofnakamoto/white-wallet), by configuration and a
little design code: this repository is the whole app. It holds White Wallet's brand files (`brand.properties`, `res/`),
the brand's look in code (`code/`: the living mark and its components, nothing about money), the Zapstore listing and
the workflows that keep it current and publish it. The design is `../zap-app/DESIGN.md`.

**Who it's for:** absolute beginners who want to send and receive bitcoin, and don't care how LNURLcash works. (An
app for experts and power users would be a different brand.)

## Product rules

| Rule | How (brand.properties) |
|---|---|
| One service, chosen for the user | `defaultServiceHost=mint.lnurlcash.com`, `allowServiceChange=false`, no suggestions |
| One trusted service | the home mint is trusted by default; `showMints=false` leaves nothing to trust by hand |
| One account | `maxAccounts=1`: no switcher, no "Add account" |
| One Lightning address, asked for at setup | `requireAddress=true`, `maxAddresses=1` |
| One note per service, kept automatically | `noteStrategies=single`, `defaultNoteStrategy=single` (it also renews the address's notes) |
| Notes from elsewhere come home by themselves | `collectAtHomeMint=always` (fee cap 2 %) |
| Nothing to manage by hand | `notesView=hidden` (no Notes tab or page), `showMints=false`; Note settings disappear by themselves, as there is nothing to choose |
| One unit, no choice | `defaultAmountUnit=bitcoin` (₿1,000; texts say "bitcoin", never "BTC" or "sats"), `allowUnitChange=false` (no Amounts page) |
| No developer tools in sight | `developerOptions=hidden`: never in release builds (debug builds unlock them by tapping the version in About seven times) |
| Plain words | the custodian is "a Lightning service" (`res/values*/strings.xml`), slogan "Bitcoin, as easy as a zap." |

What users see: setup (pick a name), the wallet (balance, Send, Receive), History, and Settings with their recovery
phrase, address, security and About.

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
| `code/` | `ZapDesign`: the living mark (it reacts to the wallet's status and payments), the wordmark, spark pill buttons |
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
  `white-wallet.version` to it, sets `versionName` to its version, raises `versionCode` by one and adds a
  `CHANGELOG.md` section with its release notes. Keys White Wallet adds come with defaults, so a brand rarely needs
  more than that; a renamed or removed key makes the pull request's build fail instead of the app.
- **A change to the brand itself** (texts, colors, a rule): a pull request that also raises `versionCode`, sets
  `versionName` to the White Wallet version plus a revision (`0.1.0.1`) and adds its `CHANGELOG.md` section.
- **CI** (`ci.yml`) builds and checks every pull request against the pinned White Wallet, and refuses a change to
  what ships (`brand.properties`, `res/`, `white-wallet.version`) without a new version.
- **Release** (`release.yml`): every push to main whose `v<versionName>` tag doesn't exist yet is released: the signed
  APK is built, its certificate checked, a GitHub release `v<versionName>` created with the APK and its checksum, and
  the release published to Zapstore. A push that ships nothing new (a README change) does nothing. If the release job fails
  (the bunker offline, say), re-run that job in Actions; a new push would skip the version, as its tag exists by then.

### Set up once

1. **The signing key**, only for this app. Losing it ends updates under `io.github.zapofnakamoto`: keep two offline
   backups.
   ```sh
   keytool -genkeypair -v -keystore zap-release.jks -alias zap -keyalg RSA -keysize 4096 -validity 10000
   keytool -list -v -keystore zap-release.jks -alias zap | grep SHA256   # the fingerprint for ZAP_CERT_SHA256
   ```
2. **The publisher's Nostr identity**: a key only for publishing, held by a NIP-46 bunker (nsec.app or a self-hosted
   nsecbunkerd) that is online whenever main is pushed. Let the CI connection sign zsp's events without asking.
3. **GitHub**, in the environment `zapstore`:
   - secrets `ZAP_KEYSTORE_BASE64` (`base64 -w0 zap-release.jks`), `ZAP_KEYSTORE_PASSWORD`, `ZAP_KEY_ALIAS`,
     `ZAP_KEY_PASSWORD`, `ZAPSTORE_BUNKER` (the `bunker://…` URL);
   - variable `ZAP_CERT_SHA256` (the fingerprint, colons optional).

   Repository variable `UPDATE_APP_ID`, with the secret
   `UPDATE_APP_KEY`: a GitHub App allowed to write contents and pull requests here, so the update pull requests run
   CI (pull requests opened with the workflow's own token don't).

   Until then nothing fails: `update.yml` skips without `UPDATE_APP_ID`, and `release.yml` releases nothing while
   `zapstore.yaml` has no `pubkey`.

### First release

1. Take the screenshots into `zapstore/` and list them under `images:` in `zapstore.yaml`.
2. Link the signing key to the publisher (NIP-C1), on your machine, as it needs the keystore:
   `SIGN_WITH=<bunker://…> zsp identity --link-key zap-release.jks`.
3. Put the publisher's public key under `pubkey:` in `zapstore.yaml`, so the relay can verify this repository as the
   publisher's. Until then `release.yml` skips releasing, with a notice.
4. `just check`, then push to main: `release.yml` publishes 0.1.0, and every later version the same way.
