# Checked-core request and reference boundary

This increment moves the existing authenticated C5-to-Core request producer and
semantic/digest-reference helpers into `gravity.checked-core`. D1, D2, D3, D9,
L1, L2, C1-C6, C15, BOOT8 and TEST2 govern the boundary. The input is a C5
artifact plus its lexical verification context and explicit Reader invocation;
the output is the existing Core request data, identities and reference records.

The public gateway must still call the C5 owner's verification through explicit
operations, select the same context/invocation arity, validate Reader invocation
options and reject missing lexical authority. Its request constructor remains
private and retains the existing artifact-kind, passed-report and complete-proof
requirements. Neither a supplied public proof map nor a stage helper grants
Core artifact admission or publication authority.

Digest ordinals remain nonnegative integers with the existing exact reference
schema and authority marker. Resolution must produce an actual SHA-256 identity
or the existing C6-VERIFY rejection. These are format/index legality checks,
not accounting or input quotas. Semantic reference normalization must preserve
the existing source/projection identity distinctions and deterministic order.

Bootstrap keeps Core owner admission, proof transactions, publication and
historical migration checks. Compatibility names/arities/private visibility,
diagnostic data, explicit Var ports and late construction/receipt epochs stay
intact. Acceptance requires real paired requests and rejection, complete output
parity, arity/context/digest adversarial tests, appropriate regressions,
independent acceptance, exact-head CI and clean final integration preflight.
No historical denied Reader, native, migration or external-capture action is
retried. This draft grants no acceptance and no Core artifact, backend, release,
self-hosting or seed-retirement completion.

The extraction moves nine existing functions. Public APIs are
`authenticated-request` (all three arities), `digest-reference-ordinal`,
`resolve-reference!`, `resolve-reference-vector!`, `semantic-projection-id`,
`semantic-binding`, `semantic-resolution` and `semantic-var-reference`.
`request-from-verified-upstream` stays private. The full namespace now has 38
public functions and four private helpers. Its contract marks the gateway's
upstream-owner verification explicitly and grants no Core admission authority.
Bootstrap keeps original names, arities and visibility. The original legacy
context keyword keeps its Bootstrap namespace identity. The later-installed
resolution projection Var is declared before the operation map and captured
only after its existing loader installation, preserving receipt qualification.

Real paired current-development Reader/C5 products emit the same 66-form Core
request and resolve two genuine digest references to its projection binding,
`sha256:779017dd0c05bb4de564cc2a64de4b0fd73d062f222a1b61d022b54df55e2bb1`.
Passing nil lexical context with the actual parent rejects C6-VERIFY with
`:missing-fields [:sh06-context]`; an unresolved digest index also rejects
C6-VERIFY. Complete before/after parent, request, resolved digest vector and
both rejection payloads compare equal across both spellings (eight comparisons).
These are real request/data products, not admitted Core artifacts.

All nine moved algorithms, the unselected module assembly wrapper and all
unselected runtime/pin definitions are conserved after removing explicit
operation bindings. Strict EOF reads cover complete source files. A fresh
process loads 38 public functions and four private helpers without Bootstrap.
Direct stage/test lint has zero errors or warnings. One test delimiter was
corrected before semantic tests; the forward Var declaration was identified
from the actual loader order before extraction, not from a failed owner run.

Focused checks pass 5 tests / 180 assertions. They cover every existing gateway
arity, context/invocation selection and order, missing lexical authority,
unchanged owner failure propagation, private constructor premises, exact digest
schema/authority/index/hash legality and semantic identity normalization. Large
nonnegative ordinal data remains legal until actual digest lookup; no quota is
introduced. Construction and receipt tests include every new public/private
stage and compatibility root. The actual fresh-process CI mode passes stage
19 tests / 1526 assertions, current-binding/kernel 4 / 57 and projection kernels
3 / 286: 26 tests / 1869 assertions, zero failures and errors. The language gate
passes 6 / 11. Contextual lint, docs validation, independent acceptance, final
main composition, exact-head CI and integration preflight remain required.
