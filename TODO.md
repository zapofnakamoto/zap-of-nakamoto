# To do

Ideas and follow-ups noted for later. Not a plan: each item still needs its own design.

1. **"Redeemed" on a zap's page.** Zap says "claimed" for a zap everywhere else ("Claim a zap", "This zap has already
   been claimed."), but the zap's page keeps White Wallet's English `note_waiting` ("Not redeemed yet. You can take it
   back until it is.") and `note_redeemed` ("Redeemed. The recipient has the money."). Overriding them as "Not claimed
   yet…" and "Claimed. The recipient has the bitcoin." would make it one word; German already says "eingelöst".
   Found in the notes-as-zaps preview.
2. **The count on a mint's balance.** With `noteStrategies=single` every mint holds one note, so `mint_balance` reads
   "₿2,100 in 1 zap" ("in 1 Zap"). Open: drop the count (an override of both plural forms with only `%1$s`), or
   keep it. Found in the notes-as-zaps preview.
