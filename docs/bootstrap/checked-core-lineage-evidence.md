# Checked-core lineage and fragment records

This increment moves the existing C6 lineage, projection binding, fragment
selection, binding order, coverage and module assembly record helpers into
`gravity.checked-core`. D1, D2, D3, D9, L2, C1, C3-C6, C15, B13, BOOT8 and TEST2
govern the boundary. The B13-derived records are Core request data, not complete
backend artifact manifests or release evidence.

Inputs are existing verified C5 semantic records, projected form trees,
bindings, resolutions, aliases and explicit seed operations. Outputs are
source/semantic lineage identities, projection-binding inputs, ordered fragment
records, coverage summaries and module assembly records. Exact source identity,
semantic projection identity, generated traces, reference order and declared
alias requirements remain intact. An undeclared fragment alias must retain its
existing C6-VERIFY diagnostic.

Bootstrap retains owner verification, admission, authenticated request assembly,
publication, proof transactions and migration checks. The lineage implementation
helper stays private. All public APIs declare admission:none, and late
construction/receipt qualification includes their stage and compatibility roots.
No input quotas, work accounting or replacement migration pins are introduced.

Acceptance requires actual paired source requests and lineage/fragment outputs,
real undeclared-alias rejection, complete before/after equality, deterministic
identity/provenance checks, proportionate regressions, independent adversarial
acceptance, current CI and final exact clean integration preflight. A named
namespace or wrapper alone grants no completion credit. This early draft grants
no acceptance and no full Core, backend, release, performance, self-hosting or
seed-retirement support.

The implementation moves nine existing functions. Public APIs are `lineage`,
`projection-binding-input`, `alias-name`, `fragment-record`, `fragment-manifest`,
`binding-order`, `fragment-coverage` and `module-assembly-manifest`.
`lineage-with-semantic-trace` stays private, alongside the two previously private
projection helpers. All 30 public functions and three private implementations
carry the explicit namespace contract. Bootstrap keeps the original names,
arities and private visibility through callable Var ports. Construction and
receipt epochs include every new stage, helper and compatibility root.

The actual current-development Reader/C5 path emits paired requests from the
existing source-occurrences fixtures: six fragments, 66 forms, seven local
bindings and nine resolutions. Semantic projection identity is
`sha256:ac7f672c35248d8c867e03fef25de66c05c44acac48616f2c38cf83b67c93680`;
projection binding is
`sha256:779017dd0c05bb4de564cc2a64de4b0fd73d062f222a1b61d022b54df55e2bb1`.
An alias-qualified reference inserted into a real fragment with no declared
alias rejects C6-VERIFY, `:fragment-undeclared-alias`. The complete C5 parent,
request, lineage/fragment/coverage/assembly records and rejection compare equal
before and after for both spellings, eight full product comparisons. This is
actual request data and does not admit a published Core or backend artifact.

The nine moved bodies conserve the existing algorithms. The single nested
lineage let is flattened only after confirming disjoint bindings; unused stage
arguments are named with underscores while compatibility arguments stay exact.
All unselected request admission and reference helper bodies are conserved.
Strict full-file EOF reads validate the source. A fresh process loads 30 public
functions and three private helpers without loading Bootstrap. Direct stage and
test lint passes with zero errors or warnings. Initial redundant-do/let lint
findings were corrected; a scratch conservation script's missing clojure.set
require and test delimiters were corrected before their semantic checks.

Focused tests pass 4 tests / 135 assertions. They distinguish the authenticated
owner identity from semantic projection identity, track source/module/binding
changes, preserve fragment and reference order and declared aliases, and retain
the existing projection-binding envelope exclusions. The real fresh-process CI
mode passes stage 15 tests / 1215 assertions, current-binding/kernel 4 / 57, and
projection kernels 3 / 286: 22 tests / 1558 assertions, zero failures and errors.
The language gate passes 6 / 11. Contextual lint, current docs validation,
independent acceptance and exact-head CI remain required before integration.
