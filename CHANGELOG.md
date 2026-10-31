# Changelog

Every release of Zap of Nakamoto, newest first ([Keep a Changelog](https://keepachangelog.com/),
[Semantic Versioning](https://semver.org/)). zsp shows the section of the released version as its release notes in
Zapstore; the update workflow adds a section for each new White Wallet version.

Zap of Nakamoto is a beta until 1.0.0: any release may change how the app works or break what it did before. Use it
only to try it out, with tiny amounts you could lose.

## [Unreleased]

## [0.4.0]

On White Wallet v0.6.3. From here on the version is the app's own (semver), no longer White Wallet's.

### Added

- Setup and restore let you use another mint: mint.lnurlcash.com stays preselected, "Custom…" takes the address of
  any other.
- Setup lets you continue without a Lightning address: you receive by invoice and can pick an address later in
  Settings.
- Settings → Lightning addresses can add an address on another mint, with the same recovery phrase.
- Settings → Mints shows your mint and any other the wallet knows; on Claim, which mint a zap is on and whether you
  trust it.
- A Zaps tab next to Wallet and History lists your zaps one by one, each on a small spark tile (dashed while it is
  being paid), the ones you handed out under their own heading, on a turned-over tile, ink with a spark bolt, saying
  they wait to be claimed, and the mark above the total, waiting while a zap is out, curious when there are none. Tap one for its card and what you can do with it. In English and German.
- The wallet says what it did to your zaps by itself, and what that did to your balance ("Combined 2 zaps into one
  of ₿63. You got ₿1 in fees back.").
- While the wallet works on your zaps, the mark takes the place of the balance and says what it does ("Checking zap
  3 of 5…"), instead of figures about to change.
- An empty wallet shows a bigger mark and says how to get your first bitcoin.
- A zap your mint no longer knows (a mint that lost its records, or misbehaves) no longer stops the wallet: it is set
  aside, out of your balance, and the wallet asks the mint again by itself. The wallet shows a warning with "Check
  again", and once the mint knows the zap again, it is back in your balance.

### Changed

- A note you hand out is a zap: "Send as a zap", "Claim a zap", "Sent a zap", in English and German. What you hold
  is bitcoin, so Settings and the mint pages say "your bitcoin" where they said "your notes". The success screens
  say "Sent!" and "Bitcoin arrived!", waiting for a payment says "Waiting for the payment…", so "zap" means one thing.
- A zap is claimed, never redeemed: its page, the Zaps tab and History say "Not claimed yet" and "Claimed", and the
  zap's page says the recipient has the bitcoin, or that the bitcoin is back in your balance.
- A zap is a spark card: the mark, the amount and, on a handed-out zap's page,
  "Scan to claim" above its QR code. The card says where it stands: waiting to be claimed; claimed, a dashed
  outline once the money left, the mark rising out of the card when it happens; back in your wallet, a solid one.
  Claim shows the same card, "Ready to claim". On paper it is an ink outline. The QR code stays covered until you
  tap, behind a gold cover, and is covered again when you leave the app. In English and German.
- While nothing happens, the mark on the wallet floats gently up and down and now and then glances aside, and
  sometimes blinks twice (not with reduced motion).

- History has no filters any more: the "Note maintenance" chip is gone, and so are the entries it showed, which
  never changed your balance.
- The beta notice at setup and in About says what beta means here: until 1.0.0, updates may change how the app
  works, and only tiny amounts belong in it.

### Fixed

- Splitting, renewing or receiving no longer fails with "already in use", as it could after a payment you asked for
  by invoice never came.
- Restoring from your recovery phrase no longer ends with an empty wallet when the mint refuses to look a zap up: it
  says the mint can't be used instead.

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
