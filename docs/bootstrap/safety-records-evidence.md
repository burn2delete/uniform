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
receive no waiver. The initial source was prepared before optimization's
separate integration; the actual current-main composition is recorded below.
No denied source/native action, Maven operation, failed CI job or authority pin
is retried, renewed or bypassed for this slice.

## Current-main composition and checks

Source 43c7b502ed9ef07a8989ef33d2c2b8c73ed5afaa, tree
f80d42977ecf47d086122226eed133353d1348c1, passes focused 5 / 129,
registration 24 / 871, language 6 / 11 and C10 leaf 4 / 87. Catalog count 53
and hash c29a49033c0514f4e84d5de019939a519b456134a7623b4b25404ec8274fada7
remain unchanged. All seven actual changed Clojure/EDN paths pass contextual
lint against base 6d1ed9e5: 3492 unchanged baseline warnings, zero blockers.
Docs validate 240 / 19; roadmap and governance pass. The initial draft is
merge-conflicting after main advances; no initial candidate CI is observed.

Main advances to the normally integrated optimization merge
41ab1c357e4d96a510fae4cf5a46e903fe340510, tree
aa81df24465dc67e9d6d7863bc50bdeb886a6742. A normal merge composes that actual
revision. Three genuine shared-registration conflicts are resolved by retaining
both stages, preserving optimization helper index 9 and appending safety at
index 10. Both complete adversarial association tests are retained from their
actual parents. No CI configuration, timeout, cache, authority pin or diagnostic
policy changes. Safety stage/component/test source stays identical to 43c;
optimization stage/component/test/evidence stays identical to main 41ab.

Fresh working-composition tests pass both suites together, 10 / 239.
Registration passes 25 / 888 and language 6 / 11. The exact immutable-base
conservation check also passes on this composition. Original paired source and
record evidence keeps its earlier production context. Final composition still
requires contextual lint against actual main, independent exact-head acceptance,
its own successful CI and a clean current-main integration preflight.

## Package-main composition and fresh evidence

The prior composition b14b954f0bad1d8dbe07e02f9e8261ca04b1f41e, tree
798a9bda2a390225e91b6b767c1ef8153deaa26f, remains nonpassing. Its own CI
41224910156 recorded a Macro watchdog exit 124 at 600 seconds; the other nine
enabled jobs passed. Cause, active test and cleanup are not established. That
head is not rerun, and no later main or CI observation repairs or supplies
old-head or old-bound credit.

A normal merge composes actual Package main
6dcba8de85797f4f19b865db89e44ef4bd0c5dba, tree
cb42bf633ac8db611758abda10f4e2e55a7d13da. Four genuine shared require and
registration conflicts are reconstructed from complete current-main files with
the original Safety association and adversarial inventory test appended. The
namespace map retains main and the existing Safety row. All incoming Package,
Effects, Diagnostics, Lowering, backend, compiler and CI code remains intact.
The stage, both native components and focused Safety test remain identical to
the prior source; all nine bodies, ports and unmoved forms conserve against
actual main. CI configuration, authority pins and count formulas are unchanged.

At clean main 6dc, the ordinary producer compiles both complete accepted and
rejected fixtures and executes the normalized Gravity C10 kernel. The same
producer runs on the working composition. Both spellings yield proven-safe,
runtime-checked, unsafe-island and rejected outcomes; zero division rejects
C10-NUMERIC. Forty-two complete and serialized record pairs match exactly.
All actual source texts and hashes, declared fixture plan IDs, native data
proof IDs and check IDs recompute. Altered basis and child links still fail the
limited native predicates. The retained SH-12 view, capability callback and
historical proof/status labels remain explicitly unauthenticated test data.

Fresh Safety/Package/Runtime/Optimization/Lowering and normalized C10 tests pass
32 tests / 1227 assertions. Registration passes 34 / 1030 and the language
boundary passes 6 / 11. The complete C10 source plan is captured separately:
157 functions, source SHA
786b98b8c307bb96271126a3ab876bcb8c0d54037a26afe31020e79832826a11,
plan ID sha256:d0a6a977812cd7aff11a7c5c6af65653342fb860f51e15e634d259ccae15a6c9.
Compilation uses the existing fixed Stage2 compiler.gravity seed, SHA
57c13e44b46726a136ecd237dde5e85d86cd55b82bd8ac6033afce2fb87bf527,
and execution uses native Clojure gravity.runtime and component 00593. This
does not execute main's changed Gravity emitter/runtime modules or renew
authenticated source, migration, certificate or source-readiness authority.
The provenance record binds actual input hashes to main 6dc and retains the
inherited compiler stamps without fabrication.

The first conservation launch used the old verifier's 6d1 Bootstrap baseline.
It accepted the complete stage but failed its stale whole-Bootstrap expectation
because main now includes the other integrated stage requires. That failed log
is retained. The current-base verifier rebuilds the same extraction from actual
6dc without writes and passes the entire stage, both whole components, all
unmoved forms and the exact single Safety require delta; no comparison is
removed or weakened.

Records use /tmp/uniform-safety-records-evidence/package-main-*; command logs
use /tmp/uniform-safety-records-package-main-*. Fresh contextual lint,
docs/roadmap/governance, exact independent acceptance, this complete head's own
CI and clean current-main integration preflight remain required. Earlier
acceptance remains bound to its earlier tuple. This increment does not complete
the namespace or any general safety contract.

## Ownership-main composition

Ownership PR705 advances actual main to
a1174ebcaac1a8795f3a9f6f9a139f6fd4e46dda, tree
856e8b6a5677b0cbfad8b38998e7cbbd08faf192. A normal merge composes that main
with source d3386f60e0eea1c2eae7c5c83fb8ee7f0ca449df. Five genuine shared
require, registration and namespace-map conflicts are reconstructed from
complete main files and the existing Safety association/test. Both native
components merge without conflict. The current-base conservation verifier
rebuilds the Safety extraction from actual a117 and confirms the entire stage,
both complete components, all unchanged Ownership wrappers/unmoved forms, and
the single Safety require delta. Stage code and focused tests remain unchanged.

Fresh Safety/Ownership/Package/Runtime/Optimization/Lowering and normalized
C9/C10 tests pass 43 / 1925. Registration passes 35 / 1047 and the language
boundary passes 6 / 11. The fresh producer's 42 full/text records match the
explicitly historical clean-6dc control, including recomputed actual source
hashes, fixture plan IDs and native data proof/check IDs. The complete C10 plan
and actual fixed-seed/native provenance are captured again and bound to a117;
the two Gravity emitter/runtime modules remain non-executed inputs.

Both prior reviewers accepted d338 conditionally. Its CI 41256196592 remains
separate evidence on base 6dc. This new complete tuple requires its own exact
reviews, contextual lint, CI and clean current-main preflight. No earlier
acceptance, cancelled run, failed watchdog or later success is transferred to
another tuple or used as completion authority.

## Standard-Library-main composition

Standard Library PR726 advances actual main to
d84f2837fe4747ece773a47176f499c27e62f0b7, tree
5a32776ff5d0eb6d07f4350f9442c52874a5bc16. Both reviewers accepted prior
Safety source 9291f6af60a84a384a1861989c13086970511945 conditionally; its own
CI 41258076535 completed successfully on base a117. That green remains
historical and supplies no current-base or former-watchdog pass credit.

A normal merge composes actual main. Four genuine require/registration
conflicts are reconstructed from complete main files with the unchanged Safety
association/test. All Standard Library code, native component/facet, tests,
contracts and evidence remain byte-identical to main. Safety stage/tests and
nine algorithm bodies are unchanged. The current-base extraction check passes
the entire stage, both whole components including existing Ownership wrappers
and unmoved forms, and the single Safety require delta.

Fresh Safety/Standard Library/Ownership/Package/Runtime/Optimization/Lowering
and normalized C9/C10 regressions pass 49 tests / 2036 assertions. Registration
passes 36 / 1063 and language 6 / 11. All 42 full/text records still match the
explicitly historical clean-6dc control, with actual source hashes and fixture/
native data IDs recomputed. Complete C10 plan and actual fixed-seed/native
provenance bind to main d84; the two Gravity modules remain non-executed inputs.

The new complete tuple requires its own exact reviews, contextual lint, CI and
clean current-main integration preflight. No authority, release, self-hosting,
seed-retirement, full safety or namespace-completion claim is added.

## Unsupported boundaries

This is partial temporary Clojure record machinery. Canonical analyze/verify,
general C10 analysis, untrusted native/input/callback admission, genuine
source/Core composition, provider grants, unsafe policy approval, proof-provider
validation, full SAFE15 certificates, actual runtime/native check execution,
safety-check elision, performance certification, migration, release,
self-hosting and seed retirement remain unsupported. Safe language legality
continues to be a compile-time obligation; record construction does not defer
it to a backend or runtime.
