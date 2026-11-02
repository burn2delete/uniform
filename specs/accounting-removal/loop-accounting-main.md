# Fresh-main unused loop helper retirement

Planning: ACC-01/07/12. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. Assessed main:
`40ecbb97ede5e93c3202c894c4a7ec5e64cdd5bd`, tree
`d343460804f202f08c9751c1c82eb634f6c38cf4`. This candidate extracts only the
finite two-helper deletion independently reviewed on PR616; main acceptance
is a separate pending decision.

## Consumed exact child review

PR616 candidate `2f2f8f701f19660dad89e98f351693669a97980b`, tree
`e21649ac124613802ad024edce7df203176f32e8`, parent PR615
`bb3a9acfbb5dd683d6df0403867a179a5fdc51bc`, tree
`4c78e092a77a7dac53732d5bf0439d5fdf964493`, received independent
`pass-exact-child-source-slice` from reviewer
`01a18529-dad4-7de1-b68d-3e567c8976a3`. Receipt SHA256:
`c91616d8e3279d871583f174398f00b649173384c1343d199038df8cd38e6bee`.
Its three source controls passed 89 assertions with zero failures/errors and no
findings. The receipt and reviewer output were read through supported local
access, and the receipt hash, candidate/parent/tree, author/reviewer distinction,
source hashes and reviewed census hash were verified against immutable Git.
The raw receipt and private paths remain local.

The review covers only the two-helper child deletion and census. It explicitly
accepts no PR615 dependency, main integration, whole call graph, live loop
accounting, compiler/runtime/performance, release or self-hosting result. It
is distinct from the stopped PR569 review and does not retry or replace it.

## Exact main extraction

Delete only `sh08-lsi-row-index` and `sh08-lsi-target-rows` from current main's
C7 source. The complete 29-line removed interval is byte-identical to the
reviewed child's parent interval. Both complete helper forms also match that
reviewed parent. Their absence matches historical PR502
`6a4f87f013d0076b81aba9e0667650e70d28e635`.

Every other main byte and definition is unchanged: 1087 remaining definitions,
including all 30 loop-family functions. The namespace/export form matches main
and the reviewed child. The remaining loop-family forms match all three inputs.
The child stack's other source changes are not copied into this main candidate.

[The census](loop-accounting-main-census.edn) pins main/child/review identities,
complete source hashes, the exact removed interval, current namespace/exports,
all 30 loop-family forms and the reference scope. The tracked bootstrap, tools,
workflow, agent/Spec Kit and dependency scan finds only the two definitions and
internal call before deletion and zero references after it. Neither helper is
exported or named in tests within that scope. Arbitrary external/untracked or
dynamically constructed calls are not ruled out as a whole-callgraph theorem.

The current [accounting removal rule](../../docs/development-verification.md#remove-project-cost-and-budget-mechanisms)
requires deleting unused accounting mechanisms. [C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
and [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
retain active semantic validation and exact evidence identities. All live loop
validation and accounting remain in unchanged code with their existing open
acceptance obligations. No test, public API, runtime or Native implementation
changes here.

## Observed validation and remaining gates

The pinned local Clojure checker passed receipt identity/hash and source/census
verification, byte-identical main extraction, exact definition-set subtraction,
current-main byte conservation, namespace/export conservation, all 30 unchanged
loop-family forms, historical absences and fresh reference scans. It reads forms
with evaluation disabled and Bootstrap unloaded. Complete pins and generator
digest are in the census; exact scripts, invocation and raw output remain local.

The direct boundary gate passes 6 tests/11 assertions. Canonical docs,
full-language roadmap and whitespace pass. No Clojure test/tooling code changes;
new semantic tests and changed-Clojure lint are unnecessary for the exact
unused-helper deletion. No compiler, runtime, Native/provider, authenticated
source, Effects/emitter, broker or held selector ran. Child CI results retain
their own scope; this candidate's automatic CI is separate.

Keep draft pending coordinator-routed main-candidate disposition. Required gates:

1. Independently assess this exact main-based candidate, including changed base,
   byte extraction, current callers/exports and live-body conservation. The child
   review is consumed evidence and does not confer main acceptance.
2. Consume applicable current-head CI and determine required semantic/source
   coverage gates without launching a held or denied operation. Preserve every
   existing loop positive/negative and genuine execution obligation.
3. Recheck fresh main and candidate identities, ancestry/tree equivalence and the
   governing lifecycle. The coordinator alone routes acceptance and integration;
   integration preflight remains UNRUN.

The existing generated lint cache is still local/untracked. Its prohibited
cleanup is not retried or routed around, and no clean-worktree claim is made.
No integration, dependency-stack acceptance, live loop accounting removal,
whole-callgraph completeness, performance, authentication, release, self-hosting,
PR502 closure or expanded PR532 acceptance is claimed.
