# SH-06 B51 Vector-Leaf Checkpoint Fixtures

Every source has a byte-identical co-canonical `.gravity` / `.qst` peer.
These fixtures exercise checkpoint A only: schema-1 lexical scopes and the
schema-15 SH-07 projection of `let` and `loop` vector binding slots.

Accepted fixtures cover nested leaves, wildcards, empty vector slots,
sequential visibility, and unchanged simple-symbol `_` / `&` slots. Recovery
fixtures retain only the first preorder occurrence of a duplicate vector leaf
while preserving the raw pattern for the later B51 diagnostic boundary.
