# Native Safety report ownership

## Contract boundary

D1/D2/D3/D8/D9, SAFE1/SAFE15, C1/C9/C10/C11/C15, BOOT8 and TEST2 govern
this extraction. Move the three existing C10 native report bodies from
c10_safety_analysis/verification.clj into gravity.safety. Preserve predicates,
legacy truthy/sequence values, evaluation order, callbacks, diagnostic ids,
source forwarding and return values. Keep the compatibility entry points and
interposable C10 facade.

The proposed public APIs are native-verifier-report, native-capability-proof
and validate-native-report!. Inputs are trusted native inventory, outcome,
runtime-check, obligation, certificate-reference, unsafe, taint/capability,
generated-origin, optimization and diagnostic data, or explicit projection and
failure operations. Outputs are the existing report, legacy projection,
complete marker or ordered C10 missing-field refusal. The stage owns these
native predicates and refusal selection. Existing callers own source spans,
diagnostic construction, transport and all input admission.

Dependency direction is core to gravity.safety to the native C10 compatibility
leaf and facade. The stage must not import Bootstrap or C10. Existing Gravity
classification and checked-core verification remain the semantic owners.

## Evidence obligations

The clean before source is 3e668d81205aa66f991f232e3ea7a2f7ae2f07e0,
tree f4a73f8a6fa5887044ffdaa7ada5684a3f925db1. It includes pending PR880,
which is inherited work and receives no duplicate acceptance or credit here.
Capture the original native artifact, projection, successful validation and
all eleven actual ordered missing-field refusals without Bootstrap loaded.
Compare complete serialized and parsed records and recompute hashes after
extraction; record clean exact source identity on both sides.

Retain paired Gravity/qst numeric-safety plans, real accepted outcomes and
C10-NUMERIC rejection in the existing Safety pipeline tests. These programs
exercise the existing normalized-request classifier, not genuine source/Core
admission. Add focused native predicate, ordering, truthiness, provenance,
callback return/exception and compatibility tests. Prove exact moved-body and
wrapper conservation; run affected C10 composition, language/ownership,
proportionate lint and contract checks. Independent exact-source review, own
CI/artifact verification and current-main preflight remain integration gates.

## Implemented extraction and local checks

The three original whole bodies now live in gravity.safety under the declared
names. Exact text checks reconstruct the original bodies and compatibility
wrappers; no native predicate is strengthened. The stage contract declares
all twelve APIs, trusted input/output data, the seven retained refusal ids,
diagnostic ownership and dependency direction. The C10 facade, policy,
catalogs, assembly and Gravity classifier remain unchanged.

Focused C10/native tests pass 7 tests/224 assertions. The retained paired
Gravity/qst Safety pipeline passes 5/147, including actual normalized-request
classification, deterministic plan identities and C10-NUMERIC rejection.
Language/ownership checks pass 48/1197 with no skipped namespace. Documents,
roadmap and governance validators pass. Fresh clean candidate capture,
committed lint, independent acceptance and CI remain pending.

The initial focused report test had one failed expectation: it expected the
original overall status to fail when inventory status is missing. The
corrected test preserves the observed report inconsistency and verifies that
the subsequent validator still refuses. The first extraction helper had an
extra parenthesis and failed compilation before source writes; its original
file and error record remain. A duplicate-target patch was rejected before
application. No production guard, pin, identity or report field was changed
to force a passing result. Helpers/logs use uniform-safety-native-reports-*
under /tmp. The PR attachment attempt failed at the existing 100-identity
limit and is not retried.

## Complete backend and Safety composition

The integration candidate normally composes reviewed Safety source
23a146e412412c25f5242b5b1fad874c8a478a3e with reviewed backend source
7b38fffbaab2d3da55f72bda816c04d5da6ce5da. The latter includes actual main
c28077686b5bd641c8cc0717bbd23641a701987a, tree
67eb8f7ab968c29fc3b58ce7a4ac980ac41e4d21, and its selected Syntax owner
authentication. All 25 backend paths match the reviewed backend source. All
six Safety paths match the reviewed Safety source before this composition
paragraph. All other tracked paths match actual main. The complete union is
31 paths, preserving both original draft histories without duplicate credit.

Fresh clean native captures, paired Safety/backend controls, exact complete
composition review, own CI/artifact checks and immediate current-main
preflight remain required. No provider source is adopted, and earlier CI
results do not confer integration eligibility on the combined source.

## Complete stage cohort composition

Normal source merges compose actual main
f0f427b96a11c1959ccfcb6a7bcf953a6710a1fa, tree
01e1dca28949e55c402e755432e0921d3789b757, with the reviewed backend/Safety
source d0664498f993d3ebc3ce68e0f4d221658d886298, reviewed Optimization
source bd50bb2ed3fd3c75d225fb4c438253a6a5816039 and reviewed Lowering
source eb34d250d2f524ec75094b3437f06f49597a0d5d. Initial complete merge
f83558298cec99ab519bbd7e32eb15b16e7c5712, tree
934080b23910e2e62ebbb39ad05f4e52beadcfa6, contains their disjoint 31+8+8
path union. The final documentation delta is this composition note and the
Optimization/Lowering rows of the existing namespace map. All implementation
and test paths remain byte-identical to their reviewed sources; all other
tracked paths remain actual main.

Main's net Macro test repair (blob
2670c12834328613a0f72ba025ec9fc42c383181) and two-file Resolution envelope
patch are inherited intact. The old held Macro patch and provider commits
are not adopted. Main's dormant Simplify tooling is conserved without
launching it, creating a Project, changing ignored state or activating workers.
Its separate integration timing exception grants this cohort no CI waiver.

Optimization's preceding own CI run 41708584754 retains a 900-second Macro
timeout (900116ms), while its unchanged Resolution children pass all 43 tests
and 2203 assertions in 351452ms and 321812ms. These results remain bound to
old source bd50 and provider 10e3d3b09e716774477c2ff01b316cec7fa25f78;
they are not new-source acceptance or a causal performance-fix claim. Other
predecessor failures/cancellations and source-specific PRs remain history.

Require fresh clean captures for all four native artifact/refusal groups,
bounded stage and affected integration checks, full exact independent
composition review, committed lint, this cohort's own CI/artifact audit and
immediate exact clean current-main preflight. PR892 carries the complete
cohort; predecessor drafts remain until actual verified integration and gain
no separate or duplicate credit.

## Current-main Diagnostics composition

Normal merge 80f88568dc1148d2b3bdb8e6becba23aa53b73e2, tree
e577c5e65922a07ce9115f449c83f710d51b5b35, preserves actual main
05df36d8535afef03d3f3ebea2b7c80eaab26a76, tree
947423b0d9cb270d719446cdcb35fcc1b12bbc5b. Normal merge
0809510a89ec571403f3c234380bde523aafcde4, tree
44728241ecba5c0c1aba869df5c196fbdfe6ec09, adds the reviewed seven-path
Diagnostics source 173e366101ea41d6e8ae78e93242c6b3e6264f12. The union is
54 paths: all implementation, tests, policies and registries remain exact
reviewed source; only the two existing composition evidence notes change.
Incoming Runtime, Effects/emitter, Core construction/session and CI context
paths remain exact main. Inherited source and fixture labels grant no new
authentication, execution or migration authority.

The preceding 47-path source da024bc1 retained a Macro timeout in own run
41713342032 (900071ms/124), with 26 entries and 25 returns, last entered
real-paired-macro-expansion-retains-identities-provenance-and-output. All
eleven bundles and 41 command/watchdog vectors are audited; ten other
applicable jobs passed. Diagnostics source 173e passed own run41717160196,
whose actual provider parents are main5040 and 173e; its eleven bundles and
41 vectors are audited. Neither observation transfers to this composition.
Fresh five-group captures, affected current-source checks, complete exact
review, own CI and immediate clean current-main preflight remain required.
The 54-path checkpoint above predates the Macro orchestration repair PR909.

## Complete CI orchestration composition

Normal merge 96a918f19caff7e90b7963ffa18ace06c9838011, tree
259460b93c8c6cbf621d79449a166846295091b3, composes the reviewed four-path
CI repair 1c31441ec7781babecbd1a6beb53f3c34aa47d01 with the reviewed
54-path stage source ee4bcfbd281c6f8d604528742bb1038149d648b6. The complete
union is 58 paths against main05df. The CI paths remain exact reviewed source;
all stage implementation, test, policy and registry paths remain unchanged.
Only this evidence note records the later composition. Full CI selection is
required because the orchestration paths cross stage boundaries.

Macro retains all 23 pipeline and five compatibility tests in three fresh
processes: twenty ordinary tests, three exact receipt/callback controls and the
whole compatibility namespace. Each child retains 900 seconds; the aggregate
is 2700 seconds and the hosted Macro envelope is 50 minutes. No compiler or
test body changes, causal performance fix, previous failure reclassification,
new execution authority or migration/release claim accompanies this repair.
The local three-child run belongs to source1c; final composition captures,
runner checks, lint, independent exact review, own CI/artifact audit and an
immediate clean exact-current-main preflight remain separate requirements.

## Residual boundaries

No new general SAFE classification, source/Core authentication, proof-provider
trust, certificate verification, profile legality, capability grants, unsafe
approval, runtime check execution, optimization/check erasure, MIR/backend
admission, performance, release, migration, self-hosting or seed retirement.
Existing report completeness and proof labels remain native data. In
particular, the original set comparison does not establish uniqueness of
duplicate outcome records, and vacuous predicates remain compatibility
behavior. Overall report status also omits the inventory-status predicate;
the subsequent validator still rejects its false completeness field.
This extraction must not strengthen those predicates or silently
claim canonical C10 conformance. Historical held routes, guards, source pins
and denied attempts remain unchanged.
