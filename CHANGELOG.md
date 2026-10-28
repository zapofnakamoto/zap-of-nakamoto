# Changelog

Every release of Zap of Nakamoto, newest first ([Keep a Changelog](https://keepachangelog.com/),
[Semantic Versioning](https://semver.org/)). zsp shows the section of the released version as its release notes in
Zapstore; the update workflow adds a section for each new White Wallet version.

Zap of Nakamoto is a beta until 1.0.0: any release may change how the app works or break what it did before. Use it
only to try it out, with tiny amounts you could lose.

## [Unreleased]

On White Wallet v0.3.0. From here on the version is the app's own (semver), no longer White Wallet's.

### Added

- Setup and restore let you use another Lightning service: mint.lnurlcash.com stays preselected, "Custom…" takes the
  address of any other.
- Settings → Lightning addresses can add an address on another Lightning service, with the same recovery phrase.
- Settings → Services shows your Lightning service and any other the wallet knows; on Claim, which service a note is
  on and whether you trust it.

### Changed

- History has no filters any more: the "Note maintenance" chip is gone, and so are the entries it showed, which
  never changed your balance.
- The beta notice at setup and in About says what beta means here: until 1.0.0, updates may change how the app
  works, and only tiny amounts belong in it.

## [0.3.0]

On White Wallet v0.3.0.

### Added

- The app says that it is a beta: a badge beside its name at setup and on the wallet, and a sentence on what that
  means at setup and in About.

## [0.2.0]

On White Wallet v0.2.0.

### Added

- A new look: the Spark design, a mark that comes to life, and a new slogan.
- An animation when a payment comes in, and a celebration when one goes out.
- Setup spells out the whole Lightning Address below the name.
- History shows an icon for each kind of entry, marks what the wallet did by itself, and shows fees the Lightning
  service pays back.
- A Lightning service that presents a different signing key can be used again after a warning.

### Fixed

- Lightning Addresses on other services whose invoices carry no or another description hash can be paid.
- Nothing is paid through a Lightning service that presents a different signing key than the one remembered.
- Money in a payment whose answer never came back is out of the balance until the service has settled it.
- A note handed out as a link shows as taken in History once it is claimed.
- A dropped payment to a user on the same service is no longer shown as paid.

## [0.1.0]

First release, on White Wallet v0.1.0.
