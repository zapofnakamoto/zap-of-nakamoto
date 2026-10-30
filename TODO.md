# To do

Ideas and follow-ups noted for later. Not a plan: each item still needs its own design.

1. **The count on a mint's balance.** With `notes.strategies=single` every mint holds one note, so `mint_balance` reads
   "₿2,100 in 1 zap" ("in 1 Zap"). Open: drop the count (an override of both plural forms with only `%1$s`), or
   keep it. Found in the notes-as-zaps preview.
