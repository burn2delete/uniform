# Incremental build helper evidence

Base: `8b0b2ed4fb20511b46478189a8c8f554d47d5983`, tree
`b56e5a0d2a2d88f889850f07d6bd4460c371a1cd`. Cloud observations: 2026-10-26.

The pristine `clojure -J-Xmx512m -M:leaf-test --namespace gravity.c16-incremental-test`
passed 6 tests / 80 assertions, with the real 48-entry catalog validated.
The added accepted/rejected validation and proof-projection regressions passed
before the rename: 7 tests / 98 assertions. This is focused leaf execution, not
the full catalog or compiler. The existing proof projection can report a
`:complete` status alongside false observations; the regression preserves that
compatibility behavior and does not claim proof authority.

All four moved helper bodies are byte-identical after their first namespace
line. Exact old-name search found no remaining live references. Candidate
execution, actual before/after planner comparison and independent review remain
pending; historical heavy compiler failures are not replayed or treated as
current results. No public facade/schema/diagnostic ID or historical pin changes.
