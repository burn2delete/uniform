# Current-source function verifier fixtures

This bounded C7 fixture increment preserves the useful source and result
controls from PR599 while adapting them to the current main APIs.  The working
base is main `97445f364d938004618f2ac13d4d4c6e1ca32946` (tree
`bde2ef38e3ff53c9a61a88b9ea2ff5d2be1a7bc2`).  The original PR head is
`b786b464a67e33af2b75bfcf90a87619b71774c3` and its declared base is
`d58d61c4a83f09ebd3c742a3cc13b9d000244759`; those identities are retained for
history and are not treated as an integration base.  Production C7 logic is
not changed by this fixture increment.

## Contract and scope

The controls exercise L5 type facts, C7 function return and Map-get facts,
complete type and constraint products, source provenance, and fresh result
verification.  They do not claim whole-constructor equivalence, native
callback or cycle safety, companion runtime semantics, release readiness,
self-hosting, or PR502 closure.  The four preserved fixture files are source
inputs, not admission evidence by themselves.

The current unmetered importer is
`sh08-ft-function-type-core-artifact-with-imports` with its four arguments
`[core bindings forms imports]`.  Retired allowance/work-policy arguments are
not restored or renamed.  The development tests explicitly select the current
Reader, Syntax, and Macro sources with
`:gravity/sh04-development-syntax-selection-v1`; ordinary pinned/default
selection remains unchanged and fail-closed.  No admission hash or canonical
pin is refreshed by this work.

## Preserved source and tests

The following `.gravity`/`.qst` pairs are byte-identical and retain their
original spelling:

- `function-verifier-work-ledger` defines `tone` with an ordinary Map lookup.
- `function-verifier-qualified-map-get` defines `shade` with
  `gravity.core/get`.

The construction controls assert Keyword return facts, direct Map-get facts,
type-table and constraint-ledger equality, and the absence of retired
accounting fields.  They also assert deterministic rejection of nil,
malformed artifact shape, and duplicate-node inputs with `C7-VERIFY`
diagnostics.  The public reconstruction control checks each byte-identical
companion, reconstructs fresh C7 products through the current source path,
and verifies expected/candidate equality.  It then applies eight mutations per
fixture (schema, status, typed types, constraints, return type, namespace,
identity ledger, and unknown field); every mutation is rejected.

The explicit current-source handoff used by these tests is a prerequisite for
ordinary development only.  Its source/operation-context checks preserve
authenticity, provenance, tamper detection, native-class and callback rules;
they do not alter the pinned admission route or imply migration authority.

## Focused evidence

On the clean current-source candidate, the following focused controls passed:

- `ordinary-function-construction-preserves-semantic-results`: 23 assertions;
- `qualified-map-get-construction-preserves-semantic-results`: 23 assertions;
- `function-verifier-source-reconstruction-and-tampering`: 32 assertions.

These runs used the Clojure development runner with the current source
selection above.  Native C, C12, broker, and restricted/heavy pipeline routes
were not run.  The candidate remains subject to independent review, exact
base/head/tree preflight, and composition checks before integration.
