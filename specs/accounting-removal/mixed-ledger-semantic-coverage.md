# Mixed-ledger semantic comparison successor

This separate ACC-01 test successor addresses P2
`PR598-TEST-RETIREMENT-01` from the qualified source review of PR598.
Base: `d58d61c4a83f09ebd3c742a3cc13b9d000244759`, tree
`5b895b67131de6d209b50585551840fd3954011f`. Receipt SHA256:
`f058ec3cfbd19bfb439f6ae3ae8d557c453e99bf4d0377e371b56d092bcdb274`.
The frozen candidate, its finding and PR599's active review remain unchanged.

## Requirement and exact change

[C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
requires preserved lowering semantics and explicit products/diagnostics.
[D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
requires evidence to distinguish source checks from actual conformance.
The current project instruction removes compiler payment requirements while
preserving independently meaningful semantic checks.

The retired tests compared complete row, fact and compiled result maps at a
reported exact allowance. Those six comparison instances included semantic
invariance. The status-only replacements did not preserve that coverage.

Only `mixed-ledger-remains-complete-with-ordinary-entries` changes in each of
the two existing List-to-Set/public Map-key test files. Each keeps its original
complete-status assertion first and adds three semantic product comparisons:

- Row census: accepted status, exact selected row and complete descriptor map.
- Fact census: accepted status and exact selected fact.
- Compile result: complete status and the exact expected builtin-call product,
  including builtin identity and local argument.

Each comparison uses the same mixed source context at two fixed legacy API
arguments, 65536 and 65537. The production API still requires that argument;
neither test derives it from work telemetry or asserts payment, a threshold,
a one-short refusal or a language quota. No production constant changes.
These comparisons restore the nonpayment obligation under a different test
form; full equivalence to the retired payment assertions is not claimed.

## Projection exclusions

Only the top-level `:work-count` key is removed before comparison. In each
row-census accepted return, that key reports accumulated traversal work;
`:status`, `:items` and `:descriptors` remain. In each fact-census accepted
return, it reports traversal work; `:status` and `:items` remain. The successful
compile route adds accumulated work to the compiler result whose semantic
fields are `:status` and `:value`. The census pins those complete producer forms
and the shared compiler-result constructor.

The comparison does not recursively remove fields, select a permissive subset,
or hide diagnostics, reason, provenance, descriptor content or unknown fields.
Complete expected maps and equality of both projected results make an unexpected
remaining field visible. Nested fields, including any remaining accounting in
descriptors, are unchanged and are not endorsed by this test correction.

## Evidence and remaining acceptance

`mixed-ledger-semantic-coverage.edn` records the exact parent/test byte equations,
ordered assertions, producer pins, one-field projection and 24 effective source
corruption refusals. The original positive tests, all negative cases, assertion
order/multiplicity, helper definitions and native-record controls are unchanged.
All production bytes are unchanged. The inert checker reads forms with reader
evaluation disabled; it does not require Bootstrap or execute a target/helper.

Observed local validation: the source checker passed for both families and all
24 corruptions. Changed test files and the evidence EDN lint with zero errors
and warnings. The language-boundary gate passed 6 tests / 11 assertions;
whitespace checks passed. No genuine mixed-ledger test was executed.

Keep draft pending one independently routed affected source/test review.
Actual C7/lowering tests, native/callback behavior and genuine execution are
UNRUN. Neither source validation nor the correction establishes the expected
runtime results. Parent-stack acceptance, PR599 repair/rebase, current-main
composition/integration and whole ACC-01/PR502 closure remain separate gates.
