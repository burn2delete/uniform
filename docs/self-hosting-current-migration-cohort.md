# Proposed Current Compiler Migration Cohort

Status: read-only build-graph assessment and proposed first module cohort.
Not a stable-revision selection, admission, execution authorization, milestone
completion, target change, or release decision.

Assessment revision: `e1c05842181c1b3002ccbd013cc8b1148472badf`.
This document does not include subsequent working-tree compiler changes.

The [C01/C16 reconciliation below](#c01c16-reconciliation-at-2a94fb71) records
a later committed input snapshot. It does not replace this historical
assessment's identities, select an authoritative revision, or include the
concurrent private-Map `keys` implementation.

Concurrent work known during assessment is not part of this revision: C5
internal projection sharing in `00877`, `00880-03` and `00879-bounded-02` plus
its owning SH06 test; seed module-local `let` work in `00575` and the `00580`
module-source component, using the unchanged `00576` interface; and binary
atomic `not=` work in C7/effects/P15 emitter and their tests, with runtime and
seed unchanged by that increment.
These are pending development changes, not accepted future evidence. If they
merge, reconcile the actual merged diff and results: C5 changes affect the
verification/projection equations; seed changes affect owner plan construction;
emitter changes affect selected freshness even when its lowering is not used.
The exact repository identities below must not be relabelled as their successor.

## Purpose and governing contracts

The smallest coherent next **module** migration to consider is the ordinary
source-to-canonical-Core compiler slice under `:meta/:jvm`: the five existing
frontend owners plus `gravity.checked-core`, their real seed/toolchain inputs,
and the actual producer-to-consumer contracts. It emits verified canonical
Core, not typed/effected/safety-checked MIR or an executable compiler binary.
It is a useful first decision within the complete compiler migration, not a
replacement definition of that complete compiler.

The complete compiler/release closure still requires types, effects,
capabilities, profiles, ownership, safety, MIR, required passes, diagnostics,
selected backend, runtime/providers, library, build and driver work. Those
requirements do not become optional because this first module endpoint stops
at Core. The existing staged Linux LLVM release candidate is unchanged.

[D0](phase-00-foundation-and-thesis/001-d0-gravity-vision-and-design-thesis.md),
[D1:28](phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md),
[D2:297](phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md),
[D3](phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md),
[D6](phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md),
[D8](phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md)
and [D9:75](phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
govern semantics, ordering, safety and evidence. In particular, D1 permits
bounded/fused implementations only with equivalent boundary facts; it does not
permit an untyped Core artifact to acquire later legality facts by naming.
[BOOT3:21](phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md),
[BOOT5](phase-15-bootstrap-and-self-hosting/207-boot5-stage-compatibility-matrix.md),
[BOOT7](phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md)
and [BOOT8](phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md)
govern an eventual explicit migration. The
[completion plan:42](self-hosting-milestone-completion-plan.md) requires the
actual transitive compiler population, not whichever files make a test green.

The three boundaries in [development verification:9](development-verification.md)
remain separate: ordinary semantic development, affected integration, and
explicit stable-revision migration with fresh provenance/equivalence/bootstrap
and independent review. Nothing here makes the third a prerequisite for the
first two. The master goal and all existing milestone states are unchanged.
SH-05.1, SH-05.6 and SH-06.6 remain OPEN; the current resolver remains
`:development-unadmitted`.

## Actual construction graph and proposed endpoint

The ordinary artifact pipeline's closed stage list and fact ledger are in
[pipeline schema:5](../bootstrap/clojure/src/gravity/authenticated_artifact_pipeline/01-pipeline-schema-version.clj).
Its [build-payload:86](../bootstrap/clojure/src/gravity/authenticated_artifact_pipeline/04-envelope-valid.clj)
delegates to existing owners:

```text
source bytes / declared source context
  -> C2 / selected SH03 reader execution
  -> C3 / Gravity syntax construction
  -> SH05 / Gravity defn expansion
  -> SH06 / Gravity resolution + real module artifacts when imported
  -> SH07 / Gravity checked-core template + verification + source map
  -> canonical Core (C7-C10 facts still pending)

seed construction underneath these owners:
compiler.gravity rule DATA -> Clojure seed compilation -> function plans
                           -> Clojure generic plan execution / primitives
envelope owner <-> digest requests / canonical codecs / host artifact binding
emitter.gravity bytes -> selected resolver/Core toolchain-freshness keys
```

This diagram separates executed algorithms, rules, and freshness dependencies;
an arrow does not assert that the preceding module compiles its successor
outside the seed. The six owner headers have no Gravity `:requires` clause.
That does not mean they are dependency-free: their plans use seed primitives,
source/header projections, canonical encoders, hashes, coordinator verifiers,
and the envelope owner. C5 fixture imports additionally require the actual
callee artifacts; names or `:refer` declarations cannot substitute for them.

Two different Core constructors must not be conflated:

| Route | Actual constructor and use contract | Evidence implication |
| --- | --- | --- |
| Frontend subject analysis | `b-run-frontend-subject` at [SH06 test:2331](../bootstrap/clojure/test/gravity/self_hosting/sh06_resolution_adapter_test.clj) calls `sh06-c6-lowering-from-resolution-artifact`; [00042:2](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00042-sh06-c6-lowering-from-resolution-artifact.clj) and [00041:3](../bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00041-compiler-c6-lowering-source-artifact.clj) freshly verify C5 and call [gravity.c6-core-lowering:1](../bootstrap/clojure/src/gravity/c6_core_lowering.clj). | Real C2/C3/C4/C5 and hosted Stage0 C6 source analysis; not execution of `gravity.checked-core`. |
| Proposed Gravity-Core module endpoint | [00898:160](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00898-sh07-core-from-authenticated-request.clj) authenticates the resolution-derived request; [00894:140](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00894-sh07-core-template-carrier-census.clj) executes `sh07-build-core-template` and verifiers from the plan bound by [00885:69](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00885-sh07-core-source-relative-path.clj). | Actual owning Gravity Core execution is required on accepted/rejected requests. Its result still records C7-C10 as pending and `:self-hosted? false` ([00898:117](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00898-sh07-core-from-authenticated-request.clj)). |

The first route is useful analysis of the second route's source. It is not a
substitute for exercising that source's Core-building behavior. Conversely,
running the Core builder on normalized DATA does not establish that the full
builder source passes real frontend analysis. The proposed module migration
needs both claims with their distinct inputs and comparison modes.
In `00894`, the authenticated runner at line 140 verifies C5 and exact request
membership before delegating to the structural runner at line 67. Calling the
structural runner alone does not authenticate that membership. The incomplete
large-source attempt neither undoes already executed bounded Gravity-owner
checks nor supplies a completed successor-cohort result.

The selected endpoint is `sh07-core-from-resolution-artifact` -> authenticated
SH06 projection -> Gravity `sh07-build-core-template` -> template verification
-> digest resolution -> resolved verification/publication. Its rows already use
child IDs and source-occurrence links; it does not call hosted `c6-lowering-artifact`.
The old helper chose that legacy constructor: exit 143 remains INCOMPLETE, cause
unresolved. [PR270](https://github.com/burn2delete/uniform/pull/270)'s 82-assertion
fixed-DATA lossless feasibility result has the combined view 984 bytes larger
than child-only, not a production benefit or redesign prerequisite. Current
Gravity source-analysis/replay/provenance remains OPEN for its own finite decision.

## Exact proposed repository inputs

These are content observations at the assessment revision, not new migration
pins. Paths in this table are relative to `bootstrap/gravity/`; each digest is
SHA-256 of the actual file bytes. Do not edit existing count/hash assertions
to match this table.

| Role | Required input | Bytes | SHA-256 |
| --- | --- | ---: | --- |
| Executed envelope owner | `src/gravity/compiler/authenticated_envelope.gravity` | 59495 | `04470b93d923611108df2c5167d72b27b5c444fe00052fa1c69bfec9e44f9c71` |
| Executed reader subset; full source subject | `src/gravity/bootstrap/reader.gravity` | 257161 | `bd262c746ad88d9213e4b3160943fecde948c5ceee12a65dfe67ffb29d4e5b77` |
| Executed syntax owner | `src/gravity/bootstrap/syntax.gravity` | 81241 | `afcab42f39743e1609657e389ee478a79f8e98cabcc6fe331c2168106e584553` |
| Executed macro owner | `src/gravity/macro.gravity` | 56488 | `19fe589efb27228b8788347439381b61c907a7b6a562a2a3ac3f7256ae77e549` |
| Executed current resolver | `src/gravity/resolution.gravity` | 97032 | `32be5f6759d3841df30d3c296cff4ceb549bce3b9d6b117f6b51517a75201d08` |
| Executed current Core owner; required source subject | `src/gravity/checked_core.gravity` | 537749 | `511e715e66f12cc3945e177ef667d073ec6294166e1140d548f3152d933e7337` |
| Required interpreted rules, not compiler execution | `p15_s23/compiler.gravity` | 128447 | `be4ba46088d413322ba5cf86156bb22bb79aeb7bec1fcf3899d8b081019ef854` |
| Required freshness input; not executed lowering in this endpoint | `p15_s23/emitter.gravity` | 840028 | `e9c88db61bb9fb497142b7cdfed6572a95d87504aaeb8ece425f4cc0b33254de` |

The proposed source-analysis population is the six complete owner sources;
rule DATA remains separately labelled. Analysis of every function declaration
does not mean execution of every body. SH03 deliberately compiles only its
231-function `sh03-*` closure, not the full reader's 275 `defn` declarations:
[00808:3](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00808-sh03-reader-expected-source-byte-count.clj),
[00809:48](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00809-sh03-reader-plan-instruction-summary.clj).
The six full sources also contain quoted/catalog DATA and ordinary `def`
initializers; the existing five-owner and checked-Core censuses separate
those from executable macro positions
([audit:337](self-hosting-sh00-sh06-evidence-audit.md),
[audit:1031](self-hosting-sh00-sh06-evidence-audit.md)). They justify the
observed `defn`-only macro subset for these subjects, not general recursive
user-macro support or closure-wide semantic execution.

### Complete seed/toolchain dependency boundary for this proposal

The smallest semantic migration unit is not the smallest physically loaded
host tree. [gravity.bootstrap:1](../bootstrap/clojure/src/gravity/bootstrap.clj)
requires hosted namespaces and then loads every entry in
`gravity/bootstrap_impl/load_order.edn` through
[loader:58](../bootstrap/clojure/src/gravity/bootstrap_impl/loader.clj).
Those resources include shared semantic modules, lexical/data shards and
runtime guards even when their Gravity counterpart is outside the selected
endpoint. Do not claim an invented pruned host build or omit loaded guards.

For a proposed same-revision reconstruction, bind the complete tracked
`bootstrap/clojure/src/` resource tree at this revision as the seed dependency,
not only the numerically named entrypoints below. This is a conservative,
exact existing tree selection, not migration credit for all its algorithms.
Other loaded host namespaces do not imply execution/admission of all Gravity
source files named by their constants.

| Required dependency | Exact repository revision/identity and use | Existing evidence, invalidation and remaining check |
| --- | --- | --- |
| Clojure compiler/adapter/primitive resource closure | Assessment commit; Git subtree `bootstrap/clojure/src` = `219b62bb9979365a03fd42a70d4e7ee79b79aeee`. Includes bootstrap namespace, loader/order, all referenced host resources, SH02-SH07 adapters/verifiers, reader/source projections, digest/codecs and hosted C6. | Historical stage reports and later compositions exercise bounded uses, not every loaded function. Compare actual executing files, classpath and options before reuse. No current whole-toolchain equivalence report was produced here. |
| Plan construction and execution | Existing [00575](../bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00575-p15-s23-stage2-seed-validate-recur.clj), [00576:1](../bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00576-p15-s23-stage2-seed-compile-expr.clj), [00580:102](../bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00580-p15-s23-stage2-compiler-artifact-expected-artifact-hash.clj), [00593:35](../bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00593-p15-s23-stage2-runtime-execute-function.clj), all in that tree. Three-argument `p15-s23-stage2-compiler-artifact-plan` delegates to the host raw seed compiler. | Seed expansion, source headers, function tables, recur validation, plan construction and generic execution remain trusted. Required primitives include the rule's builtin set plus the exact additions at [00576:213](../bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00576-p15-s23-stage2-seed-compile-expr.clj); this is not a new Gravity stdlib implementation. Primitive/compiler/executor changes invalidate affected plans/results. Freeze actual instruction/builtin usage and observed plans at migration; do not infer semantics from names. |
| Rule source | Exact `compiler.gravity` bytes above; [00644:3](../bootstrap/clojure/src/gravity/bootstrap_impl/components/backends/00644-c-backend-stage2-plan-emitter-source-rule.clj) reads and validates `p15-s23-stage2-plan-emitter` DATA. | Existing rule checks establish their own shape/coverage. Rule changes invalidate plan construction and consumers. Source inventory and declared pipeline DATA do not prove execution of that compiler pipeline. Compare rule identity and actual produced plans, not only the whole-file digest. |
| Emitter freshness | Exact `emitter.gravity` bytes above enter [00874:50](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00874-sh06-resolution-build-binding.clj) and [00885:118](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00885-sh07-core-source-relative-path.clj) keys. | Lowering-body execution is outside this first endpoint. A changed emitter still changes this selected toolchain context. No fresh frontend result may be relabelled as current merely because the raw seed does not execute its body. Determine unchanged semantic use versus required fresh context binding at an actual decision. |
| Tests and real fixture/module inputs | Assessment commit; test subtree = `4d5d19c7b548a058e336040f2305903d24b87239`; fixture subtree = `c3f804cd30be326cbcfde9cd69fcf6838df088d3`. Select existing SH02-SH07 exact tests/paired fixtures and actual callee artifacts used by those tests. | Test tree is a location/revision boundary, not authority for every test. Module inputs carry source, exports/visibility, imports, profile/target, grants and parent identity; changed callees invalidate C5 and Core consumers even when caller bytes match. Record the exact selected artifacts and which extension really ran. |
| Repository dependency/options entry | `deps.edn` Git blob = `f05e98ad6df21dca09d9183a82cb8d0ec30c23de`; `:gravity`, `:test` and `:dev-test` use the existing bootstrap/runners. | The file does not pin external Clojure/JDK/CLI artifact versions by itself. Resolved classpath, CLI and JDK versions/hashes, launcher configuration, user/system dependency inputs, JVM options and environment remain to be explicitly captured for the chosen migration. No version is guessed from an older run. |
| Policy/contracts | Assessment commit; `contracts/` subtree = `af05a81f544b3a2d78429dad79f047c69376e748`; governing docs linked here, standard reader options, existing source/carrier/work bounds, deny-by-default macro/build policy and ordinary module-input interface. | Contract/tree identity locates inputs, not proof of all policy. Changed grants, catalogs, schema versions, reader options, target policy or preserved-fact contract invalidate affected results. Required filesystem access is declared seed/coordinator IO, not guest ambient authority. No network/shell/provider grant is created here. |

External toolchain identities are deliberately unfilled **migration
requirements**, not fabricated evidence. Thus this document proposes an exact
repository-input cohort but does not assert a closed, executed stable compiler
artifact or complete external environment. A future migration must bind those
actual dependencies and compiler artifacts in existing BOOT5/BOOT8 records.

## C01/C16 reconciliation at 2a94fb71

This is a committed-input desk reconciliation on 2026-10-22, at
`2a94fb7156f9e85ad085bc88f4d6f0b05b973e4c`, tree
`cd2380533645332e1b9473eab4e064bf686c2c20`. The proposed ordinary
`:meta/:jvm` endpoint still has exactly the six complete owner sources above,
plus their actual seed, rule, freshness and producer/consumer dependencies.
It constructs canonical Core with downstream legality pending. It is not the
69-plus-five directory inventory, a compiled compiler build recipe, or a
replacement for the milestone-wide SH-07 exit gate.

These are actual file observations at that commit, not renewed pins:

| Input relative to `bootstrap/gravity/` | Bytes | SHA-256 |
| --- | ---: | --- |
| `src/gravity/compiler/authenticated_envelope.gravity` | 59495 | `04470b93d923611108df2c5167d72b27b5c444fe00052fa1c69bfec9e44f9c71` |
| `src/gravity/bootstrap/reader.gravity` | 257183 | `e3f840456ac3f2df71bb5ddf894f9524d5a8df74297252bad49925d9060a9f71` |
| `src/gravity/bootstrap/syntax.gravity` | 81241 | `afcab42f39743e1609657e389ee478a79f8e98cabcc6fe331c2168106e584553` |
| `src/gravity/macro.gravity` | 56488 | `19fe589efb27228b8788347439381b61c907a7b6a562a2a3ac3f7256ae77e549` |
| `src/gravity/resolution.gravity` | 97032 | `32be5f6759d3841df30d3c296cff4ceb549bce3b9d6b117f6b51517a75201d08` |
| `src/gravity/checked_core.gravity` | 537749 | `511e715e66f12cc3945e177ef667d073ec6294166e1140d548f3152d933e7337` |
| `p15_s23/compiler.gravity` (interpreted rules) | 128447 | `be4ba46088d413322ba5cf86156bb22bb79aeb7bec1fcf3899d8b081019ef854` |
| `p15_s23/emitter.gravity` (freshness input) | 1599698 | `0911e08ec4e7323f348b592b40db76b4a95ff5087a749c8007ab48d7839ded15` |

Five owner sources and the rule source match the older table; the reader and
emitter do not. An unchanged owner hash alone cannot retain a result through a
changed reader, adapter, seed or explicit freshness dependency. SOURCE's dirty
private-Map `keys` changes in C7/effects/emitter, L5/STD3 and their tests are
not a frozen input to this snapshot. Their eventual merge requires another
affected-input comparison; this table must not silently absorb those bytes.

### Loaded dependencies and identities still needed

The existing loader is the actual load graph: `gravity.bootstrap` requires its
host namespaces, then [loader](../bootstrap/clojure/src/gravity/bootstrap_impl/loader.clj)
loads every component named by
[load_order.edn](../bootstrap/clojure/src/gravity/bootstrap_impl/load_order.edn),
including the referenced data shards. The complete tracked host-resource tree
below conservatively includes that closure; it is not a claim that every
loaded function executes. There is no new pruned seed or build recipe here.

| Repository input at this snapshot | Git object | Remaining selection/binding |
| --- | --- | --- |
| `bootstrap/clojure/src/` | tree `f4204197ce5cdc099ab55d3468b8a22dc8855111` | Actual loaded resource URLs/classpath order and generated function-plan/digest graph identities are not captured by this document. |
| `bootstrap/clojure/test/` | tree `6b248d9f5854916e51a170dde3c5058ea8008293` | Select exact vars and their actual fixture/module artifacts; the tree is not a passing test population. |
| `bootstrap/clojure/fixtures/` | tree `20cd1c4c64d0d184a5da5f808ec2700bfd0f0e56` | Bind actual imported callee artifacts, grants, exports and paths for each selected composition. |
| `contracts/` | tree `af05a81f544b3a2d78429dad79f047c69376e748` | Applicable schemas, grants, bounds and compatibility rows remain claim-specific. |
| `deps.edn` | blob `f05e98ad6df21dca09d9183a82cb8d0ec30c23de` | The unchanged aliases select runners, not external compiler/runtime identities. |

The following remain **missing for stable migration**, rather than guessed
from a previous run: resolved Clojure/CLI/JDK artifacts and hashes; effective
classpath and user/system dependency configuration; launcher/JVM options and
environment; actual plan/function manifest and used primitive/instruction
population; both compared compiler artifacts; output/digest graph identities;
and the applicable BOOT5 support, BOOT7 comparison and BOOT8 provenance records.
The seed's allowed builtin table is not the exact set of builtins used by this
candidate. Source-header/global-literal construction and generic function,
call, loop/recur and primitive execution remain trusted seed behavior.

The current development intake is explicit, not a historical pin refresh:
[source4](../bootstrap/clojure/src/gravity/bootstrap_impl/components/compiler/00884-compiler-c5-resolution-source-artifact.clj)
takes `[source-path source-text module-inputs reader-source-path]` and retains
the original C3 -> SH05 -> SH06 parents. The corresponding from-parent route
authenticates the retained reader revision and brackets construction/final
verification with actual selected-source checks. Default arities still enforce
historical reader pins. The existing SH06 frontend-subject helper uses that
default file route; it is not already a current-reader six-subject invocation.
The current-intake tests and limits are recorded in the
[development workflow](development-verification.md#default-sh-07-workflow-three-boundaries).

### Evidence reuse and C16 disposition

The audit's **39 S / three P out of 42 requirement groups** is traceability at
its stated historical revision, not 39 current passing source modules, a
functional percentage, or a coherent current toolchain. The three partial
groups remain SH-05.1, SH-05.6 and SH-06.6. The separate historical 39-of-41 and
later 41/42 source-proof steps are not that denominator. Resolver status stays
`:development-unadmitted`.

| Retained evidence | What remains useful | Current reconciliation still open |
| --- | --- | --- |
| E02-E06 and the five frontend subject results in the audit | Original accepted/rejected semantics, source/origin/binding preservation and reviewed failures at their exact cohorts. | Reader/host/plan/context and emitter-freshness changes require an actual dependency comparison before reuse for a selected stable revision. Do not replay unchanged expensive subjects by count or relabel them current. |
| SH05 `defn` census and original Stage0 expansion comparisons | The retained five-owner and checked-Core censuses distinguish executable macro positions from quote/catalog DATA; no new required macro was demonstrated by the bounded review. | They do not execute every body, establish general user macros, or close a new complete current cohort. C16 remains open, not a demand for marginal macro fixtures. |
| SH06 genuine imports/private/unresolved, cycles, referred ambiguity, alias, lexical, profile/capability and authenticated-consumer checks | Their exact selectors and original outcomes are mapped in the audit; current source4/from-parent transport has separate development evidence. | Default-pinned historical selectors are not explicit current-reader compositions. Cross-target source imports remain fenced before C5 by the JVM frontdoor. No newly required small resolution behavior was demonstrated. |
| Interrupted checked-Core source analysis | Preserve the exit-143 log and the observed `:c5-after` / `:hosted-c6-before` boundaries. | No completed hosted-C6 result, final counters or receipt exists. C12 must separate construction and verification cost before another bounded question; no unchanged hour-long retry is selected. |
| Actual Gravity Core kernels and later ordinary source compositions | Existing owning tests exercise canonical construction and rejection, separately from hosted C6 source analysis. | Select the exact required variants and actual candidate-bound results through C11-C15; normalized DATA is not a full-source analysis pass. |

C01 therefore remains OPEN: the six source subjects and repository dependency
boundary are explicit, but the source-occurrence/primitive-use matrix and
actual plan/external-toolchain identities are incomplete. C16 also remains
OPEN. The [working task map](self-hosting-sh07-completion-plan.md#c01-working-support-map)
names concrete uses and existing checks without converting located support
into accepted coverage. The next migration decision must name which missing
rows are closed by reusable evidence, which changed dependencies need affected
checks, and which required semantics still need implementation. C17 cannot be
inferred from this desk reconciliation; C18 must distinguish a bounded module
migration from completion of the milestone's whole declared compiler subset.

## C16 applicability after PR294-PR300

This desk comparison binds main
`0067e9418de992900a95cd47d016029af62ff99c`, tree
`fd1eb88b8d72da3681f7393d982568b91ab602d1`. It preserves the historical
assessment and reconciliation tables above. No compiler, source proof or
composition was executed for this comparison. Source identity, a retained
semantic result, loaded seed identity and stable migration acceptance are
separate observations.

All six selected owner sources, `p15_s23/compiler.gravity`,
`p15_s23/emitter.gravity`, and both Gravity runtime files have identical blobs
between `64aa890a` and this main. Five owner sources and rule DATA also match
`2a94fb71`; the reader matches that later snapshot, not the original
257161-byte historical reader pin. Checked-Core remains 538443 bytes with
SHA-256 `6cb8631a0fb826868624bc8ca6f84370bccbb9206f302f53d523ded4266518fe`,
including `44709ea9`'s duplicate-match-binding rejection, versus the
537749-byte source in that snapshot. The emitter remains 1670330 bytes with
SHA-256 `23d49699bf187d56714257f88056bc0805ed158c4a76e7ea85af660c41d39d9c`;
its snapshot value was 1599698 bytes. These actual deltas remain relevant
before PR294 and are not erased by unchanged bytes in PR294-PR300.

| Selected dependency at `0067e941` | Identity and actual use | Applicability consequence |
| --- | --- | --- |
| Raw seed plan construction and generic execution | `00580-p15-s23-stage2-compiler-artifact-expected-artifact-hash.clj:165-169` sends the three-argument plan constructor to `plan-raw`; its function construction invokes `p15-s23-stage2-seed-compile-function`. `00874:3-13` and `00885:69-77` use this route for resolver/Core plans. The selected seed compiler and `00593` executor are unchanged since `64aa890a`. | PR298 does not alter this executed edge. Actual selected plan/function identities and primitive uses remain required inputs; an unchanged constructor is not a recorded plan result. |
| Current reader and authentic parent/consumer adapters | `compiler/00884:8-15` retains the explicit reader through C3, SH05 and SH06. `00898:160-164` derives the authenticated Core request; `00894:140-155` verifies C5 and exact membership before invoking the Gravity builder and verifiers. These adapters and six owner blobs are unchanged since `64aa890a`. | Retain their located scoped behavior and original source/binding/origin checks. Historical default-pinned file intake does not become explicit current-reader intake; full-subject and required-variant evidence remains open. |
| Compiler rule and emitter freshness | `backends/00644` derives the plan-emitter rule DATA from `compiler.gravity`. `00874:50-57` and `00885:118-128` separately hash current compiler/emitter bytes into resolver/Core freshness keys. | The rule variable named `emitter` is not execution of the full emitter lowering source. PR298 changes neither rule nor freshness bytes. Earlier emitter changes still prevent relabelling old context-bound results as a current cohort. |
| PR298 typed lowering interface | Only `00581-p15-s23-stage2-compiler-artifact-binding.clj` and `00581-p15-s23-stage2-typed-lowering-operation.clj` change host source after `64aa890a`: blobs `cc96442e411f05d8978c3b0709c485afa1d79ff1` and `f8539db9b7233bc06f3c743f8307756d6555af90`. Typed four-argument calls retain the legacy fresh verifier; explicit five-argument calls select the policy-aware fresh verifier and unwrap its verification report. | These changed typed entrypoints are outside the preceding raw seed-plan edge. Their typed/source joins need their own affected evidence; they do not demonstrate semantic invalidation of every earlier frontend result. |
| Complete loaded host-resource tree | `bootstrap/clojure/src/` changes from `5c9aac1b3a98e4ad1b1c2242a1769f73a2443352` at `64aa890a` to `6d09b4788df56c2a732f1bbbcdf867560cdabad6`. Both changed `00581` files are loaded by `load_order.edn`, even when this endpoint does not call their typed branches. | Complete seed/TCB identity is changed. Preserve the actual complete loaded closure for provenance; do not prune it to executed function names or reuse an old whole-tree authority identity. Semantic applicability can still be narrower than that loaded closure. |
| Contracts, dependency options, tests and actual fixtures | `contracts/` remains tree `ad57372a977b12712e8600151f4ce6264643b3ab`; `deps.edn` remains blob `f05e98ad6df21dca09d9183a82cb8d0ec30c23de`. Test tree is `1adee0d8b602d38389105492637eef1764cc8f2c`; fixture tree is `d17a99740fe7d902489728bbace13d7f2a3fb105`. PR294 adds the loop-helper pair/test; PR295-PR297 change only the three held B51 sources and their dedicated test outside this selected endpoint. PR299-PR300 are design documents. | Bind exact selected fixture/callee/grant/test dependencies, not a passing directory count. The B51 repairs and design documents neither renew the six-owner cohort nor create an additional required ordinary source. Actual external compiler/classpath/environment identities remain missing. |

The following applicability rows retain each evidence family's actual input
and output boundary. Audit references mean
`self-hosting-sh00-sh06-evidence-audit.md`; they locate recovered historical
records, not new acceptance. The PR298 branch analysis above supplies a reason
to retain unaffected semantic scope, not a replacement execution receipt.

| Existing evidence family | Retained claim and input assumptions | Current missing or affected obligation |
| --- | --- | --- |
| E02 envelope; E03 reader; E04 syntax | Original descriptor/reader-policy/syntax-origin schemas, original compiled plans, fixtures, compiler and host dependencies; audit E02-E04 and its requirement map. The unchanged envelope/syntax source bytes and unchanged PR298 raw seed route give no reason to discard those original semantic results. | Current reader differs from the historical pin; selected current reader/plan/options, envelope/digest context and full seed provenance must match the actual claim. Source sameness alone supplies none of those joins. |
| E05 macro corpus, `defn` census and Stage0 body comparison | Original 36-source/1276-definition final shards and their exact expansion/body/origin results; the five-owner and checked-Core source censuses separate executable macro positions from quoted DATA. Macro owner bytes are unchanged. | SH-05.1 and SH-05.6 remain partial OPEN. Changed subject bodies, reader/seed assumptions and generated-body/Core consumers require their actual selected occurrence and comparison rows. No new unsupported macro was established by this comparison. |
| E06 resolution and import/privacy/alias/profile/capability negatives | Original 37-source shards and actual module-artifact/catalog/grant inputs; later scoped source4/from-parent checks retain their declared current-reader route. PR298 leaves the resolver and these adapters unchanged. | The current resolver remains development-unadmitted, distinct from its historical 77209-byte revision. SH-06.6 remains partial OPEN. Select actual imported callee artifacts where required; historical normalized or default-reader checks do not establish current full-source resolution. |
| Five whole-source frontend subject results and E39/E41 | The audit locates the five separate subject runs at macro `baf3f246`, envelope `96aa2973`, syntax `e61b4fd9`, resolver `4db0dc57` and reader `2a4ea770`; each recorded 1/19 through hosted Stage0 C6. E39/E41 are older frozen source-proof endpoints. | Current hosted-C6 differs from the original cohorts through `00036`/`00038` and `gravity/c6_core_lowering{,/lowering}.clj`; current reader and earlier seed/freshness changes also need affected reasoning. No hosted result is execution of the Gravity Core builder or a continuous present acceptance chain. |
| Checked-Core full-source question | Audit `Checked-Core composition: incomplete bounded attempt` retains `b99989e3` exit 143 and the last observed hosted-C6 boundary. | Still INCOMPLETE, not a semantic rejection or pass. The selected source changed through duplicate-binding handling; no missing counters, raw log or timing receipt is supplied by this comparison. C12 needs its own bounded current source-analysis/replay decision. |
| PR266 two-definition hex composition | Candidate `020e5b59`, merge `ddef9c1a`, current-reader source4 -> genuine parents -> actual Gravity Core and public replay; 1/342 for its separate at-most-2-KiB G subject and coherent operand-order rejection. Retained `/tmp/sh03-hex-source-composition-v3-020e5b59-20261022.{log,edn}` corroborates that result. | Later Core/emitter/host context differs from that run. Preserve the helper semantic result and its own provenance; do not promote it to a full-reader, six-subject or current migration result. Failed V1/V2 stay failed. |
| PR294 original loop-helper composition | Base `ae6acbe9`; `sh07_current_loop_helper_test.clj` copies the unchanged original helper to its own paired fixture, uses actual source4/C5/Gravity Core and rejects coherent target substitution with fresh template replay. Retained `/tmp/sh07-current-loop-helper-ae6acbe9-20261024-attempt1.{log,edn}` records 1/133. Test, pair, owner body, raw seed route and freshness inputs remain unchanged through `0067e941`. | Scoped behavior can be reused across PR298's unrelated typed branch delta. Its original loaded-host context stays explicit. G traversed those stages; Q had text parity only. Unchanged extension handling does not itself justify a duplicate Q run under the development guide's proportionality rule. This is not full-subject or migration evidence. |
| PR298 caller-policy bridge | Frozen `1af5fd81`/tree `39ad9017`: actual rejection groups 1/19 and 1/17, plus conservation 15/135, in three separate runs. Raw files under `/Users/matt/.codex/artifacts/sh01-typed-lowering-work-policy-20261024/` include `public-policy-rejections-v1`, `public-join-rejections-v1`, `final-conservation-v1` logs and `conservation.edn`. | Bridge mocks prove data flow only. Positive V1/V2 aggregates remain failed; unchanged production and dependency comparison retains only their actually completed operations and assertions. The corrected positive selector remains unrun in this evidence. Neither this bridge nor its earlier input-construction probe establishes whole C8/runtime fit or C16 migration. |

C16's actual upstream gap remains complete current-cohort applicability for
SH-05.1, SH-05.6 and SH-06.6, including the selected full checked-Core subject
and the actual Gravity Core consumer. The returned matrix is complete for
this desk question; those requirements are not complete. No new small macro,
resolver or extension defect is demonstrated here. The existing current-reader
module-source/from-parent check in `sh06_resolution_adapter_test.clj` already
tests genuine callee membership and stale/duplicate/missing artifacts at C5;
inventing another import fixture without a selected required use would not
close C16.

As of `0067e941`, there is no additional ready C16 implementation leaf from
this comparison. Reopen at ROOT's candidate-bound C01 occurrence/use/plan map
and C12 full-source/consumer/resource selection, or at a concrete changed
reader/adapter/primitive/import/policy edge. Then select the corresponding
existing producer and consumer plus a stale/coherently substituted rejection;
allocate only that demonstrated missing case and its proportionate resource
scope. Do not repeat the old corpus or interrupted source method by count.
Ordinary C02-C15 semantic development continues independently. C17 separately
requires stable compiler artifacts, dependency/environment/provenance and
BOOT5/BOOT7/BOOT8 comparison/TCB evidence with independent acceptance.
The historical 39-S/three-P traceability and all failed/interrupted outcomes
retain their original meanings; no C01-C18 task or migration status is closed.

## Per-owner evidence, use contracts and missing work

Evidence references below are retained results, not tests run for this
document. The [audit](self-hosting-sh00-sh06-evidence-audit.md) contains raw-log
locators, review qualifications and failed/interrupted predecessors. E02-E06
refer to that audit's historical evidence table. Current source bytes alone do
not extend any historical acceptance to this toolchain.

| Required owner | Actual input -> output / consumer | Existing evidence that can be retained | Invalidation and genuinely missing checks for this proposed cohort |
| --- | --- | --- | --- |
| `compiler/authenticated_envelope.gravity` | Private bounded descriptor -> semantic/provenance template and verifier; SH05/SH06 and later owners consume bound envelopes. [Owner:50](../bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity); [00750 source binding:2](../bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-003-p15-s23-sh02-compile-source-binding-through-p15-s23-c13-c14-b1-wasm-source-bindings.clj). | E02 retains genuine two-stage C13/B1 reuse, path-neutral semantic identity, separate physical provenance and bounded/rejected graphs. Full-source frontend composition at `96aa2973`: 1 test/19 assertions, 72 expansions, 2146 references, hosted C6; [audit:617](self-hosting-sh00-sh06-evidence-audit.md). | Reuse only within original descriptor/schema/checker/compiler/primitive assumptions. Changed digest/codec, reference closure, policy or stage context affects consumers. Full source analysis is not execution of all 72 bodies. Current selected Core consumer must join its own descriptor and actual output; fresh stable-stage comparison/provenance/review remains missing. |
| `bootstrap/reader.gravity` | Selected `sh03-read-source-unit`/verifier closure consumes explicit bytes/options -> forms, spans, occurrences and diagnostics; C3 consumes authenticated products. | E03 retains original paired accepted/rejected reader subset and review. Whole reader source frontend composition at `2a4ea770fa97892ae7ba7620655f2129a428d039`: 1/19, 275 expansions, 7868 references, hosted C6; [audit:955](self-hosting-sh00-sh06-evidence-audit.md). | Encoding/options/reader primitive or plan changes invalidate affected forms and every downstream source/syntax join. Full reader source analysis does not execute every reader path. Preserve its 231-function execution boundary, co-canonical provenance and applicable unchanged E03 results; bind the selected current toolchain and complete required Core-source composition before broader migration credit. |
| `bootstrap/syntax.gravity` | Authenticated reader occurrences -> syntax/stream templates, scopes/marks, origins, serialization and validation; macro owner consumes them. [Owner:54](../bootstrap/gravity/src/gravity/bootstrap/syntax.gravity); [00851:3](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00851-sh04-syntax-build-binding.clj). | E04 retains original hygiene/origin/roundtrip/bounds cases. Full-source composition at `e61b4fd9`: 1/19, 102 expansions, 2846 references, hosted C6; [audit:757](self-hosting-sh00-sh06-evidence-audit.md). | Reader occurrence/literal projection, scopes/marks, origin schema or canonical identity changes invalidate syntax and macro/binding/source maps. Retain existing malformed/cyclic/bounds cases; current Core composition must preserve these facts, not reconstruct them in a host oracle. Stable-revision stage/context evidence remains missing. |
| `macro.gravity` | Authenticated C3 syntax plus version/environment/build policy -> `def`/`fn` expanded syntax, hygiene/origin trace and diagnostics; C5 then Core consume output. [Owner:48](../bootstrap/gravity/src/gravity/macro.gravity); [00857:153](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00857-sh05-macro-source-relative-path.clj). | E05 retains original 36-source/1276-definition `defn` corpus and separate final shards. Later macro-source composition associated with `baf3f2467616e480ee700afd0383d7ee535bf8da`: 1/19, 46 expansions, 1552 references, hosted C6; [audit:539](self-hosting-sh00-sh06-evidence-audit.md). Coherent outer-origin ingress rejection is retained at [audit:893](self-hosting-sh00-sh06-evidence-audit.md). | Recheck required macro positions when subject population changes; do not manufacture support from historical `defn` success. Recipe, environment, policy, literal or origin changes invalidate expansion and downstream bindings/Core. Current checked-Core subject's complete expansion/downstream result is missing. Prior-stage form parity is not full generated-Core semantic equivalence or general `defmacro` execution. SH-05.1/.6 stay OPEN. |
| `resolution.gravity` | Authenticated expanded syntax + real module artifacts/catalog -> binding/alias/import/export/scope graph, exact occurrences and diagnostics -> authenticated Core request. [Owner:49](../bootstrap/gravity/src/gravity/resolution.gravity); [00874:3](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00874-sh06-resolution-build-binding.clj). | E06's original 37-source shards retain their original scope. Later resolver-source composition at `4db0dc57`: 1/19, 111 expansions, 4349 references, hosted C6; [audit:785](self-hosting-sh00-sh06-evidence-audit.md). Actual import/privacy/cycle/alias/ambiguity/profile/capability and hostile-binding consumer checks are retained in that audit. | The current 97032-byte resolver differs from the historically accepted 77209-byte revision; current source compilation is expressly development-unadmitted. Catalog/import/export/source/profile/grant/phase changes invalidate bindings and consumers. Finish the selected full Core-source composition and real Gravity-Core downstream join. JVM source target fence prevents a cross-target C5 source claim; normalized policy tests do not remove that limitation. SH-06.6 stays OPEN. |
| `checked_core.gravity` | Verified C5 carrier -> authenticated Core request -> Gravity template construction/replay/resolved verification/source map; source spans, origins, selected bindings and evaluation order survive. [Owner:1](../bootstrap/gravity/src/gravity/checked_core.gravity), [00894:140](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00894-sh07-core-template-carrier-census.clj), [00898:3](../bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00898-sh07-core-from-authenticated-request.clj). | Existing SH07 accepted/rejected/mutation and current-development checks are relevant bounded behavior evidence. E39/E41 are older SH07 source-proof endpoints with frozen upstream identities, not present migration. Current full source census is 401 `defn` plus three `def`; [audit:1031](self-hosting-sh00-sh06-evidence-audit.md). | The current source differs from the 498219-byte historical pin in 00885; its current binding also says development-unadmitted. Required whole-source frontend composition at `b99989e3` was interrupted before hosted C6 returned, exit 143: [audit:1056](self-hosting-sh00-sh06-evidence-audit.md). No completed result may be credited. Missing work is that specific source question plus complete required Core-form variants and actual Gravity-Core accepted/rejected composition at the eventual selected revision, followed by fresh migration evidence. A host-produced Core result or normalized-only kernel cannot close both. |

All these rows depend on the seed, rule DATA, primitive/runtime, policy,
fixture/dependency and environment rows above. None assumes that a same-spelled
host primitive has Gravity semantics by default.

### Concrete reuse assessment at this revision

Read-only Git comparisons found that the six owner sources,
`p15_s23/compiler.gravity`, `bootstrap/clojure/src/` and `deps.edn` are
unchanged between the later macro/envelope/syntax/resolver subject revisions
listed above and this assessment, except for `p15_s23/emitter.gravity`.
Relative to the reader result `2a4ea770`, the same exception applies; its test
helper also changed, as documented in the audit. This is stronger than a
directory-count comparison but still not a current migration pass.

The emitter is an explicit resolver/Core freshness input. Its changed bytes
mean the earlier results retain their tested context; they are not one newly
passing eight-input cohort. Preserve those positive semantic/preservation
results and independently reviewed historical reports. Determine the exact
affected context/plan/comparison obligations at stable selection; do not
reflexively replay every unchanged reader/macro subject or erase a real
freshness obligation because lowering was not executed in that route.

The shortened frontend helper removes one redundant test-only C5 verification;
both production hosted-C6 gates remain. The historical source results each
have 19 assertions; a future successful shortened-helper run would have 18,
not a rewritten historical count. The merged small regression batch is 3
tests/74 assertions, not a large-source rerun
([audit:688](self-hosting-sh00-sh06-evidence-audit.md)). Failed initial
five-subject/read-spy attempts, failed cross-target attempt, and interrupted
checked-Core attempt remain separately failed/incomplete.

## Required broader compiler inputs, optional features and held work

This classification is functional, not a count-based admission population.
Paths below are relative to `bootstrap/gravity/src/gravity/` unless a P15
prefix is given. All named source inputs are at the assessment commit; the
entire Gravity subtree is Git tree `fbdc48122b0333ebbdf13d102d42a347e2ee4773`.
That identity locates sources; it is not a claim of executing all of them.

| Population | Disposition and reason | Existing boundary and missing broader evidence |
| --- | --- | --- |
| Six owners plus P15 rule/freshness inputs above | Required for the proposed first module endpoint. | Explicit per-input map above; neither admission nor completed current cohort. |
| `compiler/l1_c2_surface_syntax_reader.gravity`, `c3_syntax_object_model.gravity`, `c4_macro_expansion_engine.gravity`, `c5_name_resolution_namespace_analyzer.gravity`, `l2_core_language_semantics.gravity`, `c6_core_lowering_engine.gravity` | Compatibility/contract source subjects when those routes are selected, not replacements for executed reader/syntax/macro/resolver/checked-Core owners. Keep required compatibility reads in provenance. | Existing source-module catalog in [compiler.gravity:185](../bootstrap/gravity/p15_s23/compiler.gravity) is not a call graph. Hosted C6 has its own semantic owner. A record or facade alone does not establish full C2-C6 execution. |
| `compiler/c6_call_arity_legality.gravity` | Separate B48 direct-call legality subject; required if that legality result is selected by the broader callable/compiler closure, not imported by the first ordinary Core root. | [Header:1](../bootstrap/gravity/src/gravity/compiler/c6_call_arity_legality.gravity) consumes authenticated B47 vectors and explicitly does not claim public routing, closures, typing or execution. Source/call-arity evidence cannot stand in for complete calls. |
| `compiler/c7_type_checker_engine.gravity`, `effects.gravity`, `compiler/c8_effect_checker_engine.gravity` | Required for typed/effect/capability/profile facts in the broader compiler; outside the first proposed module migration, not optional language legality. | C7/C8 exact current-source development and affected composition are mapped in [development verification:260](development-verification.md). C8 explicitly imports `gravity.effects` ([header:5](../bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity)). Required variants, lawful compiler clients and stable-stage joins remain beyond those individual kernels. |
| `compiler/c9_ownership_checker_engine.gravity`, `c10_safety_analysis_pipeline.gravity`, `c6_c10_checked_core_pipeline.gravity` | Required ownership/safety obligations and actual selected C6-C10 route for the existing MIR/backend slice. Not implicit facts on SH07 Core. | [00701:7](../bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00701-p15-s23-c6c10-source-relative-path.clj) selects the Gravity bridge. Its [scope:80](../bootstrap/gravity/src/gravity/compiler/c6_c10_checked_core_pipeline.gravity) rejects calls, collections, dependencies, effects and other operations needed by a full compiler. Bounded pure construction evidence is retained but does not close the full compiler. |
| `compiler/c11_mir_specification.gravity`, `c12_domain_ir_architecture.gravity`, `c13_mir_optimization_passes.gravity`, `c14_target_lowering_architecture.gravity`, `backend/b1_backend_interface_specification.gravity`, `backend/b3_llvm_backend_design.gravity` | Required verified IR, required optimization, anchored domain boundaries where used, lowering and selected Linux backend for the broader compiler/release path. | Actual C11 source selection is in [00726:149](../bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00726-p15-s23-c6c10-gravity-candidate-verification.clj); [00742:3](../bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/00742-p15-s23-c11-mir-expected-ingress-semantic.clj) distinguishes accepted ingress kinds. C13/C14/B1 and B3 have independent source bindings; a new ordinary SH07/C7 product cannot be silently spliced into them. Complete required compiler operations and reviewed ingress compatibility remain open. |
| `bootstrap/diagnostics.gravity`, `compiler/c15_compiler_diagnostics.gravity`, `backend/w5_b13_artifact_emitter.gravity`, `w5_b14_backend_conformance_verifier.gravity` | Required diagnostic and emission/conformance obligations at their real broader boundaries; not all are executed by the ordinary frontend. | SH15/SH20 and B14 own stable diagnostics/maps, actual bytes/provenance and independent validation. Record construction/verifier evidence does not establish actual artifact writing or all rejection families; [completion plan:590](self-hosting-milestone-completion-plan.md). |
| `runtime.gravity`, P15 `emitter.gravity`, P15 `runtime.gravity` | Required real lowering/runtime components for the hosted typed executable path and required semantic/runtime work for the final compiler; only emitter freshness is selected above. | P15 runtime explicitly imports `gravity.runtime` ([header:8](../bootstrap/gravity/p15_s23/runtime.gravity)). Its declared JVM allocator/transcript handler is a reference boundary, not deployable stdout or Linux runtime closure. Current C7/effects/emitter/runtime development must continue without waiting for migration. |
| `self_hosting/meta_compiler_legality.gravity`, `hermetic_package_build.gravity`, `stdlib/self_hosting_core.gravity`, driver records in P15 `compiler.gravity` | Required meta/library/build/driver capabilities for the broader executable compiler, outside the first module endpoint. The present library module is explicitly experimental. | [Meta header:11](../bootstrap/gravity/src/gravity/self_hosting/meta_compiler_legality.gravity) says applying legality to all modules/executing compiler is integration work. [Build header:11](../bootstrap/gravity/src/gravity/self_hosting/hermetic_package_build.gravity) plans/validates, not executes actions. [Library:12](../bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity) retains provider/authenticated integration gaps. Driver DATA is not a command implementation. SH21-SH25 require actual lawful clients/builds/execution. |
| P15 `native_plan_c_emitter.gravity`, `native_runtime_driver.gravity` | Distinct bounded native helpers; not substitutes for the selected Linux LLVM compiler/runtime. Include if their actual route is selected; do not call them the Linux closure by filename. | [Native emitter:1](../bootstrap/gravity/p15_s23/native_plan_c_emitter.gravity) owns bounded C text translation, not a provider/compiler/runner. [Runtime driver:13](../bootstrap/gravity/p15_s23/native_runtime_driver.gravity) records an `:arm64-macos` provider and residual seed boundary. Linux/provider/ABI compatibility and compiler-required services are not supplied by that Darwin harness. |
| `compiler/c16_incremental_compilation_design.gravity`, `c17_compiler_plugin_pass_api.gravity`, `c18_compiler_verification_pass_correctness.gravity`; corresponding `w5_c16_incremental_executor.gravity`, `w5_c17_plugin_executor.gravity`, `w5_c18_pass_verifier.gravity`, `w5_compiler_pipeline_verifier.gravity` | Incremental/plugin features are optional only if not required by the selected build; pass verification remains required where D9/C18 demand it. These standalone helpers are not imports into the inspected first root. | Select actual executable passes/verification rather than treating every catalog entry as a build dependency. If build recipe uses incremental/plugin behavior, its sources/options/verifiers and negative invalidation cases become required; no optional label can prune such an edge. |
| `self_hosting/w5_reader_module_executor.gravity`, `w5_typed_effect_safety_executor.gravity`, `w5_ir_lowering_executor.gravity`, `w5_performance_math_executor.gravity`, `w5_domain_schema_ai_executor.gravity`, `w5_tooling_conformance_verifier.gravity`, `w5_full_language_evidence_verifier.gravity`, `w5_subsystem_closure_verifier.gravity` | Conditional standalone executor/evidence modules, not demonstrated first-root dependencies. Nonrequired domains/math/tooling features can remain outside a one-target compiler subset; actual required transitive helper uses cannot. | Existing input-record validation or selected helper execution does not prove the entire subsystem/client or a compiled compiler. Declare which functions/clients the actual build invokes before deciding their inclusion; retain their own unsupported boundaries. |
| `self_hosting/w5_compiler_identity_verifier.gravity`, `w5_stage_rebuild_orchestrator.gravity`, `w5_stage_equivalence_verifier.gravity`, `w5_trust_provenance_verifier.gravity` | Broader bootstrap evidence obligations are required. These existing implementations validate their selected supplied records; their filenames do not establish a rebuild/comparison/process invocation. | SH26-SH28 require actual N/N+1 builds, artifacts, comparisons and trust accounting; [completion plan:856](self-hosting-milestone-completion-plan.md). Use an existing helper only for the claim its real execution supports. |
| `backend/b2_c_backend_design.gravity`, `b4_wasm_backend_design.gravity`, `b5_jvm_backend_design.gravity`, `b6_javascript_typescript_backend_design.gravity`, `b7_mlir_backend_design.gravity`, `b8_gpu_backend_design.gravity`, `b9_hdl_backend_design.gravity`, `b10_workflow_graph_backend_design.gravity`, `b11_query_relational_backend_design.gravity`, `b12_mobile_backend_design.gravity` | Optional target features relative to one selected Linux LLVM compiler endpoint, not removed from the master goal. B4's accepted historical comparison remains a bounded reference baseline. | A seed running on JVM does not execute the Gravity B5 backend. A C implementation tool/provider does not automatically select the Gravity B2 backend. Required target/tool dependencies must still be included if the real recipe invokes them. |
| Seven files in `sh07_b51/`: `producer_a.gravity`, `producer_b.gravity`, `producer_c.gravity`, `producer_d.gravity`, `root6_a.gravity`, `root6_b.gravity`, `root8.gravity` | Held outside this ordinary module endpoint under the existing restrictions. No admission/routing authority is granted by the proposal. | Preserve existing held/attempt records and failed prerequisites. Do not borrow their sources/products to replace missing ordinary compiler semantics, silently add them as admitted modules or route around their invariant. |

This accounts for the current 69 `src/` plus five P15 files as distinct
inventory roles, not 74 admitted modules. Historical SH05 36, SH06 37, later
SH07 41/42 and current 69+5 populations remain distinct
([audit:186](self-hosting-sh00-sh06-evidence-audit.md)). No new authoritative
module count, inventory hash or coverage percentage is assigned here.

### Preserved Linux LLVM closure and the genuine remaining graph decision

The existing Linux slice explicitly selects `:llvm-x86_64-linux`, triple
`x86_64-unknown-linux-gnu`, ELF/SysV AMD64, i64 carrier and i32 process result;
source declaration remains JVM with the recorded explicit seed target
override. Its declared platform dependencies include Linux startup/ELF loader
and glibc 2.36; build tools are LLVM 20.1.8. See
[frozen expectations:1](../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm/018-frozen-contract-expectations.clj).
The existing `linux/amd64` container is pinned as
`silkeh/clang@sha256:ae2f3deffd84470fbb2904cfb990db208a5f9880b4bcf9d3eae080a50a8900b4`;
the [Linux policy:33](../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm/032-linux-policy-and-execution.clj)
forbids fallback to host Darwin/Mach-O. Its actual external-tool observations
belong to [toolchain execution](../bootstrap/clojure/src/gravity/bootstrap_impl/semantic_llvm/050-linux-toolchain-execution.clj),
not this desk assessment. No Docker/native command was run here.

The [existing public-native contract:353](../contracts/p15-public-native-admission-v1.edn)
separates W1 backend carrier, W2 runtime/provider and W3 contained execution
authority; development candidate observations are not final handoffs. A
passing JVM frontend cannot close those interfaces or change release target.
The narrow Linux slice's no-Gravity-helper runtime is not a complete compiler
runtime: required compiler collections, text, allocation, IO and error services
still need actual native/provider closure and evidence.

The unresolved build-graph decision is not whether to count 69 or 74 files.
It is how the eventual **actual compiler build recipe** consumes current
canonical SH07/Core and later type/effect facts at the existing verified MIR
ingress, then builds/runs that compiler using the selected Linux backend,
required runtime/library/providers and real build/driver implementation.
The inspected legacy C6-C10 bridge is deliberately too narrow for a compiler
client; the current development executable path is not already that LLVM
rebuild. Neither a documented pipeline nor a list of W5 validators supplies
the missing executable recipe.

Keep the current staged Linux contracts unchanged. Before proposing a complete
compiler migration, identify its exact existing/implemented producer at each
ingress, required function/primitive/library closure, generated source and
build actions; test compatible fact-preserving joins and reject substitution.
If the current canonical route requires new MIR integration, implement that
ordinary semantic/integration work at its owner. Do not assert a made-up
complete transitive source set or create a second admission system to bridge it.

## Bounded next decision and acceptance checks

The proposed next decision is: **after a concrete stable revision is explicitly
chosen, may the existing current resolver plus Gravity-owned canonical-Core
module boundary become the recorded accepted successor for the six-source
module cohort above, with the exact seed/toolchain residuals retained?**
It is not a decision to run every corpus, admit all present modules, complete
SH07, change the Linux release target, or retire the seed. This document alone
does not choose that stable revision or authorize executing the checks.

The bounded work needed to make that decision real is:

1. Keep ordinary C7/effects/emitter/runtime and other semantic development
   moving. Its current focused cases and affected composition are development
   evidence, not authority refresh. At explicit stable selection, capture the
   actual source/compiler/dependency/fixture/toolchain/environment identities,
   exact supported operations, input/output schemas, reader/macro policy,
   grants and resource bounds. Compare against the per-input evidence map;
   retain compatible historical evidence instead of rerunning it by count.
2. Select the current Gravity **source-analysis/replay/provenance** transaction
   through its existing authenticated owner, with exact subject/resource fit.
   Preserve the legacy exit-143 attempt as INCOMPLETE; its printed `:c5-after`
   is not a completed result. No legacy hosted-product redesign is prerequisite.
   Keep the original verifiers; no unchanged large retry, cache, verifier skip, required
   subject pruning or new lane is selected here.
3. Independently select the required **actual Gravity-Core execution** cases
   from the existing SH07 owners/tests: accepted/rejected values/quote,
   references/definitions, bindings/control, functions/calls/recur, errors,
   mutation and match variants used by this compiler source closure. Existing
   starts include `sh07-b1-if-and-do-preserve-order-and-exclusive-branches`,
   `sh07-b1-malformed-control-flow-fails-closed`,
   `sh07-b1-evaluation-child-and-origin-mutations-fail-replay` in
   [control-flow tests:301](../bootstrap/clojure/test/gravity/self_hosting/sh07_control_flow_test.clj),
   plus the owning SH07 checks in the completion plan. A list of names is not
   an exhaustive/passing selection; any required unsupported variant remains
   an owning semantic gap. Check real C5 -> Gravity-Core composition, exact
   occurrence/binding/order/origin joins and coherent stale/substituted outputs.
4. Reuse the already located macro-origin-at-C5 and binding-at-hosted-C6
   negative results for their actual gates. Add/select the corresponding real
   Gravity-Core consumer check only where missing or invalidated; hosted C6
   evidence is not renamed as Gravity-Core evidence. Compare handwritten and
   generated forms through the selected consumer, not merely `defn` surface
   expansion. Preserve C5 diagnostics and generated-origin attribution;
   [C5:176](phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md)
   and [C6:147](phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md)
   own the contracts. Cross-target source import remains unsupported under the
   selected JVM frontdoor; do not weaken its earlier target fence to obtain
   a C5 diagnostic.
5. Only for the explicitly selected stable module migration, produce the
   applicable fresh conformance, source/compiler/dependency provenance,
   diagnostic/IR comparison, bootstrap stage comparison, reviewed deltas,
   BOOT5 compatibility and TCB records, with independent acceptance. Name
   both actual compiler artifacts and comparison modes; an inventory, source
   pin, normalized test or historical body-expansion result is insufficient.
   Use existing backlog/matrix/evidence records after acceptance, not a new
   receipt/manifest authority scheme. Until then the three partial groups and
   development-unadmitted resolver stay unchanged.

The acceptance population must include all behavior actually required to
build/run this selected endpoint through its real seed dependency. It cannot
hide an unsupported primitive/macro/import merely because a selected fixture
does not reach it. Conversely, compiler-required semantics belonging to the
broader LLVM executable closure remain required there without expanding this
bounded module decision into every language/backend feature.

## Outputs, checks performed and explicit nonclaims

The output is this proposed cohort/evidence/invalidation map only. Assessment
used Git status/history/tree/blob comparisons, direct source/contract reading
and file hashing. No source parser, macro producer, JVM, compiler proof,
native provider, Docker toolchain or test namespace was executed. Static
path/line and diff/ASCII checks are appropriate for this document; the
coordinator owns serialized structural validation and independent review.

No source, fixture, compiler pin, ownership projection, milestone checkbox,
governance/admission record, manifest or shared index changes here. Retained
failed, skipped and incomplete outcomes stay so. This document supplies no
fresh current-compiler admission, self-hosting, seedless/release, all-target,
performance or safety-completion claim.

## PR266: bounded current hex-source composition

[PR266](https://github.com/burn2delete/uniform/pull/266) merged on 2026-10-22 at
`ddef9c1a4df10bb66d240362bf25e0cd7caae848`, tree
`82cbb1946cd8d76a8704d2ece7f251a9cfb714aa`, identical to tested candidate
`020e5b59ba33267ee4e7d5fd6900a1203e80daa9`. Scoped development evidence only;
no cohort renewal or authoritative migration follows.
Later PR265/main `6aba074a` changes emitter freshness inputs: these artifacts
are not renewed current-cohort provenance. Reassess affected provenance at a
concrete stable-cohort admission decision, not by replaying now or blocking
ordinary development; the reader/helper semantic result retains its tested scope.

The exact selector `sh03-current-hex-definitions-compose-through-authenticated-core`
in [the SH03 adapter tests](../bootstrap/clojure/test/gravity/self_hosting/sh03_reader_adapter_test.clj)
uses the two actual current `sh03-lowercase-hex?`/`sh03-sha256-id?` definitions
as a new, at-most-2-KiB `.gravity` subject with authentic new-path provenance,
under `:meta/:jvm/:safe` with no imports. Explicit current-reader source4 builds
genuine C3/SH05/C5 parents, then the actual Gravity Core owner and fresh public
verifier run. This is neither hosted-C6 source analysis nor a default-pin update.

V3 naturally exited 0 at 512 MiB: one test/342 passed assertions, no failures or
errors. Both exact definition/body/origin pairs, selected bindings,
operator/argument order and loop/recur joins passed. Fresh original replay
passed; a coherent reordered-`subvec` operand carrier, retaining the original
authenticated request, C5 parent and source, was rejected by six public replay
checks. Raw output and the non-authoritative timing EDN share the prefix
`/tmp/sh03-hex-source-composition-v3-020e5b59-20261022` (`.log`/`.edn`).
See the PR for detailed change/evidence context and scoped independent acceptance.

Preserved V1 (`0e73ef48`) exited 2 during namespace reading: no EDN or selector
execution. After the one-parenthesis fix `08e108cc`, V2 remains FAILED:
332 passed/two failed assertions/no errors. The test incorrectly applied
`rest` to the already body-only expanded forms, mispairing and truncating the
comparison. `020e5b59` removes that extra `rest` and guards all five streams at
exactly two entries, retaining both name/body checks. V3 is a new aligned-pair
run, not a relabelled V2 result.

Only this two-definition composition is resolved; full reader/checked-Core
source comparisons, Core variants and dependency/toolchain/bootstrap evidence
remain open. SH-05.1, SH-05.6, SH-06.6 and full-cohort C16 remain OPEN;
the resolver remains
`:development-unadmitted`. No historical row, pin, score or status is promoted.
No QST, typed-bounds/C7, C8/effect, runtime, full-reader or migration claim is
made; C7-C10 facts remain pending.

## PR318: conserved selected plans, changed seed TCB

This applicability check binds merged main
`c4faaccb1467115b362c0f6ea9100e63a990a4f5`, tree
`cf84af39ac3239bd18bc587ba2b03d8c6a8eb93b`. It does not refresh the historical
cohorts above. [PR318](https://github.com/burn2delete/uniform/pull/318) changes
two loaded seed resources: `00576-p15-s23-stage2-seed-compile-expr.clj` adds
`bootstrap-symbol-utf16-unit-count` to compiler-artifact builtin augmentation;
`00588-p15-s23-stage2-runtime-invoke-builtin.clj` adds its exact native Symbol,
compiler-artifact `:meta/:jvm` runtime branch. BOOT2's new ABI declaration is
not a public Core/library export or authenticated compiler authority. The
ordinary catalog, authenticated `00944` route, pins and migration fingerprints
are unchanged.

The six selected owner sources, compiler rule DATA, emitter freshness bytes,
and `deps.edn` are unchanged from `0067e941`. `00580` applies the augmented
builtin set before function compilation, so a same-named executable call could
change classification. SH03's allowed-builtin population also grows through
the union in `00808:82-84`; it is not an unchanged admission predicate. The
actual selected plans contain no call to the new ABI. Its own producer,
arity/type/context and once-only operand checks belong to
`sh01_bootstrap_symbol_utf16_unit_count_test.clj`; this comparison neither
duplicates them nor claims completed C7/Effects consumers.

An actual normal-broker 2GiB batch with a 120-second whole-JVM watchdog passed
the current C01 tests and language boundary: 9 tests/103 assertions, zero
failures/errors. Its subsequent whole-row comparison FAILED because physical
definition paths retained the original worktree. The run exited 1; passing
tests do not turn that comparison into a pass. The corrected comparison ran
separately under the same limits and exited 0. It checked every historical
definition path against its original root and every new path against its
actual root, then compared rows with only that physical `:source` field
removed. All source coordinates, source hashes, six plan IDs, definition and
function-plan hashes, instruction summaries, call occurrences and outside-plan
reader rows matched. Selected input fingerprints and the used-builtin
population also matched. This is actual seed-plan construction, not invocation
of every mapped function, full-source Gravity-Core composition or migration.

SH03's structural auditor can now admit this builtin name; it is not source
authentication. Historical admission additionally checks fixed plan/function
identities, while current-reader admission constructs its plan internally from
the selected source and checks source snapshots. No caller-supplied plan
substitution boundary was found there. The existing unknown-builtin negative
uses `unreviewed-current-builtin-test`, still outside the allowed set; changing
it to the new permitted ABI and expecting structural rejection would test the
wrong contract. The raw plan execution seam is not source admission.

The complete 2146-resource seed identity did not match. The report's canonical
content fingerprint changes from
`sha256:e60449db42dcc6bdefb2009a05aded442b2b34c2d35e3fc47b492d4c4834e86b`
to `sha256:97dc9781c7ea2e822350997cb2a752ed4be76c5a7764acde3dd6bab9c0b0f96c`;
the Git source-tree identity changes from `6d09b4788df56c2a732f1bbbcdf867560cdabad6`
to `b8c9b113a0e4138270d76cc18c0dc068d7c0df3f`. Unchanged plan IDs are not a
complete-host-TCB identity. Existing `00874`/`00885` binding keys cover owner,
compiler and emitter files, not every loaded seed byte; this check does not
claim that they reject this host change. A new stale-seed rejection contract
would require owning production work, not a fabricated C16 test validator.

Retain the old report and semantic results with their original seed context.
The actual comparison justifies narrower selected-plan conservation across
PR318, not reuse of its old complete-seed fingerprint as current authority.
SH-05.1, SH-05.6, SH-06.6 and C16 remain OPEN; the resolver remains
development-unadmitted. No extra stale/coherent consumer case was demonstrated
on this selected edge. Future reader policy/source changes and actual new ABI
consumers need their affected checks; the failed full replay remains failed.

Raw scripts/logs are `/private/tmp/gravity-c16-pr318-u2N7il-run.{clj,log}` and
`/private/tmp/gravity-c16-pr318-u2N7il-compare-v2.{clj,log}`. The failed first
log SHA256 is `405a212203637f59c6d04f63df44db6d6a84f989909c77e624e0ca2eba00e1b2`;
the successful comparison log SHA256 is
`64bd8ba3f09c471899bccb9bfceab430289e398fc94353e2c33262e504e90724`.
