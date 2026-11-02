# Preserved branch-join test port

PR595's original source is
`99893151b3d0b7c3c435d5c2e6e90c03255b5951`, tree
`67c0a515f30c26b61fc6f0bc06d37f2ed28bd107`. Its source and direct caller
migrations are already covered by main's accounting-free C7 implementation.
The older PR591/593 source stack remains separate open work and is not replayed.

This port starts from main
`c8a67ad6f6eda914a60f883a45d2de2a77ce00b3`, tree
`7cf36b5ebaef9125ab62776dbfa2ac8b6f26c681`, and retains three useful controls
that were absent there:

- A non-vector `:child-form-ids` value rejects with the existing source mismatch.
- A missing conditional input remains unknown without a spurious diagnostic.
- A 4,097-member List join checks every member and rejects an invalid member
  at index 4,096.

The existing 1,025-member, idempotence, independent-branch, source and diagnostic
controls stay intact. The malformed-child control initially exposed an eager
`count` of a keyword inside the current source-check vector. The count is now
guarded by `vector?`, restoring the intended C7 source-mismatch refusal. This
one expression is the only production change. No allowance API or work counter
is introduced. L5, C7 and D9 retain their source/type/semantic boundaries.

The original `branch-joins-census.edn` and `branch-joins.md` are retained
byte-for-byte as historical records of the original source above, not of this
main composition. PR569 separately preserves the source/adaptation review and
its qualifications; its integration remains a separate gate. This test port
does not relabel historical evidence current.
These normalized C7 helper controls do not establish authenticated Core,
C8/lowering/runtime behavior, whole PR502 closure, release or self-hosting.

The three selected List-join kernels passed 3 tests / 884 assertions after the
guard repair. The initial 3-test / 877-assertion run had one host builtin error
on the non-vector case and remains a nonpass. Independent current-composition
review, configured CI and exact integration preflight remain required.
