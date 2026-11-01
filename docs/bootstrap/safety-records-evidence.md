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

## Verified Release main composition

The next normal composition uses verified Release PR741 main
b40ddaaca5aafcd1f955f9b3c73cb6dd965d34bf/tree
8bd22beb1ab157c6751393a4428078565fcab01f. Three registration conflicts are
resolved by reconstructing five complete metadata files from that main and
the existing immutable Safety association/test. Release code, component, tests
and evidence remain byte-identical to main. Shared registry, ownership,
Bootstrap imports and map rows preserve both stages, including unique helper
identities. Safety algorithms, native components, ports and pipeline tests are
unchanged. Primary catalog counts and completion formulas remain unchanged.

Fresh combined tests pass98/3256, including the real Release archive checks.
Current controls compile both C10 spellings and retain all four safety outcomes
and C10-NUMERIC refusal. Scripts and products use `release-main-*`; full/text
comparisons, current provenance, conservation, ownership/language, docs, lint,
independent exact acceptance, own CI and clean preflight remain the integration
gates. Previous CI binds its older base and cannot accept this composition.

## CI harness environment evidence

The current CI runner tests pass20/221 with the installed linter directory on
PATH. The initial invocation lacked that directory and recorded20/192 with two
missing-linter errors; its nonpass log remains at
`/tmp/uniform-leaf-main-ci-runner-missing-linter-nonpass.log`. Correcting the
command environment changes no repository source, guard, policy or watchdog.
Terminal harness receipts are archived outside the source checkout.

## Unsupported boundaries

This is partial temporary Clojure record machinery. Canonical analyze/verify,
general C10 analysis, untrusted native/input/callback admission, genuine
source/Core composition, provider grants, unsafe policy approval, proof-provider
validation, full SAFE15 certificates, actual runtime/native check execution,
safety-check elision, performance certification, migration, release,
self-hosting and seed retirement remain unsupported. Safe language legality
continues to be a compile-time obligation; record construction does not defer
it to a backend or runtime.

## Tooling main composition

The next normal composition uses actual Tooling main
`fd159988ab06ee0397505718e9bd96bbf8df8228`, after source
`52b2a18da32b20243d38e9fbafa8a2a62a4f3599`. Four genuine require/registration
conflicts reconstruct five complete metadata files from that main, appending
only Safety's existing association/test. Safety's native algorithms, both
components, tests and explicit Var ports are unchanged. All incoming Tooling
source, wrapper, tests, three Gravity source-repair files and evidence remain
byte-identical to main. Primary catalog/count formulas and authority pins stay
unchanged.

Fresh coupled Safety, Standard Library, Ownership, Package, Runtime,
Optimization, Lowering, normalized C9/C10, Tooling and complete W5 tests pass
67/2795. Ownership inventory passes 37/1079 and language boundary 6/11. The
42 complete and serialized pairs still match the explicitly historical clean
6dc control; actual source hashes, fixture/native identities and all four C10
outcomes verify. Full-stage/component conservation and exact single Safety
require now bind to fd159. The complete 157-function C10 plan and fixed-seed /
native-Clojure provenance remain exact. Current Gravity emitter/runtime bytes
are captured but not executed.

Evidence uses `/tmp/uniform-safety-records-evidence/tooling-main-*` and
`/tmp/uniform-safety-records-tooling-main-*`. Prior own CI 41260886381 passed
all ten enabled jobs on source52b2/base d84; it supplies no acceptance for this
new composition. Its Macro16/293 and Resolution54/2307 receipts, with inherited
900-second limits, verify all checksum entries; observed times 402.070 and
664.211 seconds do not establish an old 600-second-bound pass or repair.
Current-source review, contextual lint, own CI and current-main integration
preflight remain distinct gates. No semantic, authentication, full safety,
migration, self-hosting, release or completion claim is added.

## Bootstrap literal main composition

The next normal composition uses actual Bootstrap literal main
`04de291fbed2bdda7070ecfba665d81207cd6d74`, after Safety source
`f8a360eec403c4d5c3918a10e58bb727efa662ab`. Three registration conflicts
reconstruct five complete metadata files from main, appending the existing
Safety association/test. The stage, both native components, tests and Var ports
remain unchanged. Incoming literal helper, wrapper, tests, original quota
regression and evidence are preserved exactly, as are Tooling's source repairs.

Fresh coupled regressions including the literal helper pass 71/2838. Ownership
inventory passes 38/1093 and language boundary6/11. The full 42-record controls,
source hashes, identities, C10 outcomes and whole-component/require conservation
remain the evidence boundary. Source admission, grants, performed safety checks,
full safety and namespace completion remain unsupported. New-base review,
contextual lint, own CI and exact integration preflight remain required; no
prior-base green is transferred.

## Core repair and external CI main composition

The next normal composition follows Safety80da8a842a67f9a4dc2ff249c045dea5f755835a
and uses actual main167ed15a2c3ce7d76e4eb6b9a761a55186f7a4ad/tree
58ce57515855fd4df4925aec86dab707e1edb2e0. Main includes Core PR730's corrected
operation-root guard and tests, followed by external PR731's workflow and CI
documentation changes. This merge has no conflicts. The Safety stage, both
native components, tests and Var ports remain exact80da. All five incoming
Core/CI files are preserved exactly; this slice makes no CI-policy changes.
The external delta removes a constant-failure job that executed no compiler;
its absence is no source-Core, release or self-hosting evidence. Core's genuine
current-reader controls and Safety's normalized C10/native data remain distinct
evidence boundaries. Fresh exact-base checks, review, own CI and clean
integration preflight remain required.

## Verified Build main composition

Own CI41267130499 passes nine enabled jobs on485349c1/baseb363, with two
experimental skips. The new normal composition uses verified Build main
3b2b7bb267b8a8b1c6a2811b89abba376a0ca2cc/tree
65efa902052c808cb9083b5188a7312cf64ef583. Three real registration conflicts
reconstruct five complete metadata files from that main, appending only the
existing Safety association/test and require. The native stage, both complete
components, tests and Var ports remain unchanged. All Build source, six fixtures,
unique helper/test ids, global-id regression, compiler finalizer and CI changes
are preserved. Primary catalog/count formulas and authority pins are unchanged.

Fresh ownership39/1110 and language6/11 pass. Full-stage, both components and
single-require conservation bind actual3b. Complete accepted/rejected fixture
spellings compile; all four C10 outcomes and C10-NUMERIC refusal remain. The
157-function C10 source/plan and fixed seed/native runtime still match actual
main. The current Gravity emitter hash is
`dad556ad561890e4c107144c1160966fd4440a65293ddbc186d47422ab273564`;
that file is captured but not executed by the native/C10 controls. Earlier
53e92df5 emitter captures remain historical. Source and fixture/native identity
comparisons, coupled checks, contextual lint, exact independent acceptance,
own new CI and clean preflight remain gates. No old-base green transfers.

Logs/evidence use `build-main-*`; scripts are
`/tmp/uniform-safety-records-{verify,conservation,provenance}-build-main.clj`.
The 42 full/text comparisons retain their explicitly historical6dc baseline;
no current-source admission, full safety or release authority is implied.

## Verified CLI main composition

The next normal composition uses actual main
612367e981ec37072cc3d3525f09263bdc91b264/tree
efc971c757313a418a7b40a7b14bcdeb7ef8b45e, the verified CLI PR729 merge.
The incoming CLI implementation, wrapper, tests and evidence remain exact main.
The shared namespace-map file preserves the CLI row and merges the Safety row
without conflict. Safety's stage, both native components, Var ports,
pipeline tests and ownership associations remain exact7a1cec47. No semantic,
policy, authentication or migration-pin changes are introduced.

Fresh checks include CLI and the existing coupled Safety/Build/compiler/runtime
subjects, language/ownership gates, all42 complete/text controls, whole-stage
and component conservation, and current compiler provenance. Logs, evidence and
scripts use `cli-main-*`. Exact independent review, contextual lint, own new-head
CI and clean current-base integration preflight remain required. Older green
remains historical, with no acceptance transferred across bases.

The coupled tests pass82/2995, ownership39/1110 and language6/11. All42
complete/text comparisons, current provenance and whole-component conservation
pass. Docs240/19, roadmap/governance185 and contextual lint7 paths/3486 unchanged
baseline warnings/0 blockers pass. The two first control/provenance launches
omitted test sources from the script classpath and stopped before subject
execution. Their `cli-main-*-classpath-nonpass.log` files remain; corrected
explicit classpaths pass without changing subjects, guards, pins or semantics.

## Verified Reader main composition

The next normal composition uses actual main
ec15f95051e6e9e674974d43f5a14e6555cf176a/tree
118c6a279533a1e796d34287dc2fba526a92991f, the verified Reader PR739 merge.
Reader implementation, compatibility component, tests and evidence remain
byte-identical to main. The shared map preserves the Reader row and merges the
Safety row without conflict. Safety algorithms, ports, native components,
pipeline tests and ownership metadata remain exact7b41af13. No semantic or
authority changes are introduced by this composition.

Fresh coupled tests include the Reader decoder and CLI. Controls, provenance
and conservation scripts use `reader-main-*`, retaining the original42-pair
historical baseline explicitly. Prior-base CI and review remain historical;
new exact-source acceptance, contextual lint, own CI and clean current-base
integration preflight are required. No release, source authentication, safety
completion or self-hosting credit transfers.

## Leaf CI dependency main composition

Actual main09e48072fbb69131dbe3a7826b4dbbc4c9bb45f0/tree
c63848d2d311935d3b0764c64a2bb0e6675f70e6 changes exactly four CI workflow,
runner/test and documentation paths. The normal composition preserves all four
byte-for-byte. Safety and all compiler/runtime/fixture inputs are unchanged
from d67bfaef.
The new leaf selector retains full fallback for unknown, cross-cutting and
closure-discovery changes. It does not supply source-Core or release evidence.
Current-base controls, conservation/provenance, affected checks, exact independent
review, own CI and clean integration preflight remain gates; previous CI is
historical and cannot accept this new tuple.

## Loader and snapshot CI main composition

Actual main625b42aa8da25eb897df4b9807372e21108bd8e4/tree
54ac1c4bbcb47e492d2457e068e1765b2c675615 includes shared test support and
direct bootstrap data loading. The conflict-free composition preserves all123
incoming loader paths exactly. Fresh coupled tests pass71/2838, ownership
38/1093, language6/11, docs240/19, roadmap and governance. All42 complete and
serialized pairs still match the explicitly historical clean6dc control;
source hashes, fixture/native identities and all four C10 outcomes recompute.
Whole stage, both components and the single require delta conserve against625.
The actual157-function C10 source/plan and fixed-seed/native-Clojure provenance
remain exact; captured current Gravity emitter/runtime files are not executed.
Contextual lint7 paths/3486 unchanged baseline warnings/0 blockers passes;
terminal receipts are archived outside the clean source checkout.

The next actual main is b363c32b5569e4ea76df852abd142be7c067292e/tree
49b5e7fa4d82a8d41fb3a8ffec876d39b8b8e495. Its only delta from625 is CI
workflow/docs and snapshot tooling/tests, all four preserved exactly. Safety
algorithms, ports, controls and compiler/runtime execution inputs are unchanged.
Actual b363 is the integration base. Updated-base conservation/provenance,
independent acceptance, own CI and clean preflight remain gates. Prior green
or cancelled runs supply no new-tuple acceptance. Logs/evidence use
`loader-main-*`. One script passed to the development test runner stopped at
selection parsing; the correct direct script invocation passes. No guard,
pin, runner or semantic comparison was weakened.
