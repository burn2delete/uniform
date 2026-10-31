# Safety: native proof and runtime-check records

## Boundary and contracts

D1, D2, D3, D8, D9, SAFE1, SAFE15, C1, C6-C11, C15, PERF10, BOOT8
and TEST2 govern this increment. The source base is main
6d1ed9e58fdcf4c8668c49fc43cbdf26b20184cb. Nine existing seed algorithms
move from Bootstrap components 00684 and 00685 into gravity.safety:
managed-allocation-check, transcript-delivery-check, safety-basis,
pure-safety-proof, pure-safety-proof-valid?, structural-safety-proof,
structural-safety-proof-valid?, child-obligation-refs and
rederive-proven-safety.

Each public Var declares exact arguments, inputs/outputs, diagnostic ownership,
dependency direction and admission:none. The stage imports no namespace.
Explicit digest, capability-record, proof-builder and child-reference Var
operations retain original arities, evaluation order and late interposition.
Bootstrap adds its require and retains the wrappers. All other component forms
remain unchanged, including the full owner verification and authority checks.

These are native record constructors, digest/structural predicates and ordered
proof-data rederivation. Outcome classification remains with the existing
Gravity C10 engine and checked-core verifier. Existing callers retain
C10-NO-OUTCOME, C10-PROOF, source enrichment and diagnostic construction.
The stage does not grant providers/capabilities, execute checks, authenticate
input or validate proof providers. A self-consistent digest is not admission.
The owner still reconstructs records from verified source/node data and checks
the complete expected result before authoritative use.

The extraction preserves rule versions, source hash/path/origin/span, profile,
source target, type/effects/capabilities, ownership digest, child references,
runtime-check conditions/providers/handler metadata, status and invalidation
records. Existing authenticated/proved labels remain data, not authority issued
by this namespace. Transcript metadata retains live-external-io?:false.
Runtime-checked, rejected and unsafe-island nodes pass through rederivation
without losing check, audit, diagnostic or ai/human-review fields. No fifth
outcome or new safe-code admission path is introduced.

## Actual development evidence

The before producer runs at clean main 6d1ed9e5 in the original analysis
checkout. The after producer runs with working extraction before final
registration/tests/docs. Both actual accepted and rejected SH-11 source
spellings compile. The current Gravity C10 source executes normalized numeric
requests with proven-safe, runtime-checked and unsafe-island outcomes; a zero
divisor without valid evidence is rejected with C10-NUMERIC and rejected
outcome. These are original classification kernels, not execution of dangerous
native operations or genuine source/Core admission.

The native record exercise uses a separately identified retained five-node
SH-12 fixture record, explicit numeric-fixture content hash and supplied
effect/child-reference data. This intentionally assembled test view is not a
source-correlated proof or an authenticated Core artifact. Its inherited
fixture identities and passed/proved labels carry no authority. The
capability-record operation in rederivation is explicitly mocked to return no
records; its arguments/order are captured. No real authority validator or
provider holder is invoked for that native exercise.

Forty-two complete and forty-two captured-text pairs match before/after:
source text/hash records, compiled fixture plans, actual requests/results and
rejection, retained test input, native proof/check records, child obligations,
predicate refusals, rederived nodes and callback traces. Source hashes,
declared fixture plan IDs and native proof/check IDs recompute through the
existing protocols. No identity encoding changes. Altered native proof basis
or child links fail the existing digest/structural predicates; the full real
C10 rejection remains independently recorded.

The immutable-base conservation checker rebuilds the exact port-only extraction
without executing project writes. It compares the complete stage, both
components and the single Bootstrap require delta. All nine bodies and every
unmoved source form are conserved. These are development observations, not
execution at a later committed head, migration bookends or proof certificates.

## Tests and association

gravity.safety.pipeline-test covers actual paired four-outcome classification,
fixture plan identity, record/check identity preimages, source fields,
invalidation records, digest/child-link tampering, capability callback order,
prior-node child references, late Var interposition and preservation of
non-proven safety metadata. It explicitly demonstrates that a self-consistent
but semantically unverified record can satisfy the limited digest predicate;
the predicate is not advertised as safety admission. Mock capability records
exercise joins only and grant no authority.

Focused tests pass 5 / 129. EDN/JSON registration joins
gravity.safety/sh-safety/SH-11 with its actual Bootstrap consumer and standalone
test. Association tests reject missing/forged source, namespace, owner,
consumer, test and registration data. Existing primary counts and the leaf
catalog remain unchanged. The original C10 semantic engine remains the owner.

Registration/language, affected ordinary C10 regressions, contextual lint,
docs/roadmap/governance, independent exact-head review, own successful CI and
clean current-main integration preflight remain required before integration.
Unmerged failed effects, diagnostics and ownership candidates are excluded and
receive no waiver. Optimization is a separate reviewed draft until integrated.
No denied source/native action, Maven operation, failed CI job or authority pin
is retried, renewed or bypassed for this slice.

## Unsupported boundaries

This is partial temporary Clojure record machinery. Canonical analyze/verify,
general C10 analysis, untrusted native/input/callback admission, genuine
source/Core composition, provider grants, unsafe policy approval, proof-provider
validation, full SAFE15 certificates, actual runtime/native check execution,
safety-check elision, performance certification, migration, release,
self-hosting and seed retirement remain unsupported. Safe language legality
continues to be a compile-time obligation; record construction does not defer
it to a backend or runtime.
