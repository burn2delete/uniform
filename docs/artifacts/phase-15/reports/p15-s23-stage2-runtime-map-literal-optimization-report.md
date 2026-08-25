# P15-S23 stage2 runtime map-literal optimization report

This report records a bounded, non-authoritative performance observation for a
stage2 interpreter hot path. It does not change the fresh integration gate and
does not grant result-cache authority to an authoritative or release run.

## Change

`p15-s23-stage2-runtime-execute-instruction` now routes `:map-literal` through
an internal helper that evaluates vector entries with an indexed loop and one
transient map. It avoids the temporary two-element vector allocated by the
previous `into {}`/`map` reducer. A sequence fallback retains direct callers
that provide list entries. Key and value evaluation order, `recur` rejection,
duplicate-key last-write behavior, and the returned persistent map are
unchanged.

## Bounded evidence

The focused baseline and candidate observations used the same JVM, Clojure
runtime, 32-entry instruction, 1,000 warmup iterations, 5,000 measurement
iterations, and five rounds. The baseline operation was the former `into {}` /
`map` body with the same stage2 instruction evaluator and non-tail checks. The
candidate operation invoked the optimized `:map-literal` instruction.

| observation | median time / 5,000 operations | allocated bytes / operation |
| --- | ---: | ---: |
| former reducer | 21.290458 ms | 5,632.0352 |
| transient indexed helper | 16.540916 ms | 3,064.0352 |

This one bounded observation is approximately 22% faster and 46% lower in
thread allocation for this 32-entry workload. It is not a whole-suite or
generalized speed claim.

A 60-second JFR profile of the unmodified whole-language proof slice showed
`RT.count`, `PersistentArrayMap.indexOf`, persistent map association, sequence
advancement, and recursive stage2 instruction execution among the hottest
methods. Its allocation view was dominated by `Object[]`, `ArraySeq`, map
entries, chunked sequences, and persistent map nodes. A 40-second candidate
profile reached the same recursive stage2 path while showing the new helper on
stack; the fresh test was intentionally stopped before completion. The JFR
profiles are diagnostic evidence only and are not authoritative proof results.

## Semantic checks

The focused stage2 iteration suite passed 30 tests and 398 assertions. The
stage2 benchmark contract passed 2 tests and 229 assertions. Added fixtures
cover vector and list entry carriers, duplicate keys, key-before-value
evaluation, and `recur` rejection. No full P15-S23 verifier was run for this
candidate.

## Nonclaims and residual boundary

The optimization preserves Clojure-seed stage2 execution and does not claim
Gravity self-hosting, seed retirement, release authority, fresh-proof
completion, or cache authority. Artifact identities and diagnostics were not
rewritten by this change; a fresh authoritative integration run must still
verify the complete artifact graph and exact identities.
