# To do

Ideas and follow-ups noted for later. Not a plan: each item still needs its own design.

1. **The count on a mint's balance.** With `notes.strategies=single` every mint holds one note, so `mint_balance` reads
   "₿2,100 in 1 zap" ("in 1 Zap"). Open: drop the count (an override of both plural forms with only `%1$s`), or
   keep it. Found in the notes-as-zaps preview.
2. **Mint keys on the next White Wallet update.** White Wallet after v0.4.0 drops `mints.default` (the first of
   `mints.suggestions` is preselected instead) and adds `mints.custom`. The update pull request's build fails until
   `brand.properties` moves the mint over: `mints.suggestions=mint.lnurlcash.com`, `mints.custom=true`,
   `mints.changeable=true`, with the comments from White Wallet's `brand/brand.properties`. That keeps Zap as it is
   (the LNURLcash mint preselected, "Custom…" offered). Noted on 2026-10-29.
