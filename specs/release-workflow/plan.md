# Self-hosting and release implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks), preserving
the historical transfer plan, governance interfaces and source-family work
below. Read [spec.md](spec.md), the complete applicable BOOT/TEST13/package
contracts and the selected SH/Phase 18 outcome. Use the existing SH crosswalk
and dependency representation rather than creating another task or stage graph.

Name the exact complete source/component closure and owners, actual prior and
next compiler, target/ABI/runtime/build/lock/environment, required real process
invocations, outputs and comparison mode. Distinguish local implementation,
stable migration, component construction, actual next-stage rebuild, clean
equivalence, trust and public delivery. Plans or supplied records are inputs to
execution machinery, not evidence that a compiler process ran.

Plan real accepted and rejected compiler/application runs plus missing output,
substituted builder/component, stale/incorrect comparison, seed-boundary and
required trust/metadata failures relevant to the selected increment. Preserve
actual process exits, logs, artifacts, diagnostics, exact lineage, conformance
and residual TCB. Apply focused checks during implementation and the complete
required stable-candidate suite at admission/release.

Refine only the selected existing authority. Preserve current operation holds,
signing custody and writer reservations; record failed/unrun work explicitly.
Independent review and coordinator integration remain separate from author
validation, and final product acceptance does not close wider language scope.

## Compile-time evaluation dependency interface

Use the [compile-time evaluation producer and consumer order](../expand-workflow/plan.md#compile-time-evaluation-producer-and-consumer-order) and existing selected-result table. Each existing harness consumes real evaluator/generated/compiler/cache/target/API/runtime products and executes its own subject/oracle. Owner cases and historical finite receipts neither replace the harness nor clear a held action.

Task bodies/statuses and finite review subjects stay unchanged. Current source/interface entailment and independent acceptance remain pending; preserve all actual holds.

## Complete compile-time evaluation interface

Use the [complete compile-time evaluation source criteria](../expand-workflow/plan.md#complete-compile-time-evaluation-source-criteria). Consume actual evaluator/generated/compiler/cache/hermetic results and execute each applicable existing suite on its own exact subject and oracle. Owner examples, records and finite historical review are not independently run conformance.

Current source/interface entailment and implementation acceptance remain pending; preserve existing task authority, frozen reviews and every actual restriction.

## Provider and capability dependency interface

Use the [provider and capability producer and consumer order](../auth-workflow/plan.md#provider-and-capability-producer-and-consumer-order) and existing selected-result table. Each existing suite consumes actual owner/compiler/provider/startup/action/cache/replacement products and executes its own subject/oracle. Neither source definitions, owner examples nor assigned passed flags establish that harness result or authorize held actions.

Task authorities and frozen review subjects stay unchanged. Selected interfaces remain proposed; current independent acceptance and implementation are unassessed. Preserve all actual restrictions.

## Provider and capability source interface

Use the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria). Consume actual checker/gate/startup/provider/API and owner-example results, then execute the existing language/compiler/runtime/profile/safety/library suites with exact subjects/oracles. Recorded conformance is not a harness run.

Historical finite reviews remain scoped; current whole-source/interface review and implementation acceptance remain pending. Preserve all actual holds and existing task authority.

## Concurrency dependency interface

Use the [concurrency producer and consumer order](../legality-workflow/plan.md#concurrency-producer-and-consumer-order) and existing selected-result table. The seven existing language/compiler/runtime/profile/safety/library/agent suites consume actual owner/API/target results and run their own subjects/oracles. Early definitions and owner examples neither require nor prove final harness acceptance.

Task bodies/statuses, source criteria and qualified reviews stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve every actual hold.

## Source effect dependency interface

Use the [source effect and handler dependency order](../effects-workflow/plan.md#source-effect-and-handler-dependency-order) and the existing selected-result table. Language/runtime/profile suites consume actual selected compiler/handler/fixture products and owner examples, then execute their own exact subject/oracle/matrix. API construction and owner examples do not require final suite acceptance first, and do not prove a harness run or independent admission.

Task bodies/statuses and previous qualified evidence stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve all actual operation holds.

## Current governance consumer composition

The existing first task now carries the proposed consumer block in
[tasks.md](tasks.md#implementation-tasks), linking its release evidence to
[the existing governance producer authority](../evolution-workflow/tasks.md).
The original task body, IDs, Open states and historical receipts remain intact;
the additional criteria are a current reconciliation proposal, not accepted
implementation. The historical four-contract navigation table below keeps its
original no-amendment scope; it does not describe this later consumer addition.

Select the exact applicable release/change/library/target/experiment/package
subject and consume real scoped producer/checker outputs and actual conformance,
including missing/stale/wrong-scope refusals at the real release consumer.
Retain GOV2 comparison dimensions, GOV7 use/expiry metadata, GOV8 tests/schedules
and distinct GOV4/GOV9 reviews. Do not require unrelated governance tasks to
finish before isolated release tooling can develop. Required independent
current-composition review and exact-candidate release acceptance remain
separate; no policy approval or final release action is performed here.

## Testing, seed and aggregate source composition

The [published Release task source](https://github.com/burn2delete/uniform/blob/df11890b21f71d3f246cc47e9f63d866239a0d68/specs/release-workflow/tasks.md)
supplies five detailed aggregate criteria and thirteen additional Open producer
records. Their text is retained in [the sole task file](tasks.md#testing-and-seed-conformance-task-proposals).
The future records are outside the current five-key SH projection until exact
future-task/SH composition is reviewed. Preserve current task IDs, checkboxes,
governance inputs and earlier receipts; resumption does not accept those tasks.

Select one task and one named source predicate in its closing criterion. Read
the complete [Testing contracts](../../docs/phase-14-testing-verification-and-conformance/README.md),
the applicable BOOT/Phase 18 and upstream semantic contracts, and actual owner
interfaces. Reuse the existing
[development runner](../../bootstrap/clojure/test/gravity/development_test_runner.clj),
[self-hosting runner](../../bootstrap/clojure/test/gravity/self_hosting_test_runner.clj)
and [CI entrypoint](../../tools/ci/run.clj) where applicable after inspecting the
selected call path. Existing phase-14 artifact validators and source declarations
are inspection leads, not proof that a suite runner executes. Add cases beside
the owning fixtures/tests; identify exact paths in the selected implementation
change rather than introduce thirteen new frameworks or a duplicate compiler.

All suffixes below use the existing `release/remaining-tasks-and-evidence/`
namespace and refer to the future records, not additional planning checkboxes.

| Producer suffix | Actual inputs, products and decisive negative cases |
| --- | --- |
| `execute-language-conformance-fixtures` | Validate fixture profile/target/expected type/effect/result/diagnostics, run real L1-L19 accepted/rejected source, compare reader/syntax/Core artifacts and origins. Reject malformed fixture metadata, portable/profile mixing, semantic errors disguised as reader errors and undeclared live capability. |
| `execute-compiler-preservation-fixtures` | Run actual stage/pass and affected composition fixtures with preserved/transformed facts, diagnostic goldens, cache mutations and plugin denials. Generated code re-enters the normal pipeline. Reject unsound rewrites, lost facts, stale reuse and ambient authority; ordinary feedback needs no fresh bootstrap admission. |
| `execute-runtime-conformance-fixtures` | Exercise every claimed runtime family, grants, lifetime/cleanup, concurrency, replay, FFI and redacted observability using actual selected interfaces. Reject absent family coverage, repeated replay effects, ABI/callback errors, ambient grants and secret leakage. |
| `execute-profile-compliance-fixtures` | Exercise allowed/checked/delegated/rejected behavior under each claimed profile/target and multi-profile boundary. Reject hidden hosted assumptions, wrong severity, unrecorded delegation and illegal allocation/runtime/grant use. |
| `execute-safety-conformance-fixtures` | Execute positive, negative and audited-unsafe cases under the four safety outcomes; inspect real checks, taint sinks, grant denials and proof-dependent elision. Reject undefined safe behavior, missing audits, bypasses and stale/unrelated proofs. |
| `execute-library-conformance-fixtures` | Join every claimed public API/module to real behavior/property/runnable-example or explicitly permitted proof evidence. Exercise profile availability, safe wrappers, denied effects and compatibility; reject omitted APIs, hosted leakage and evidence-free stability. |
| `execute-agent-and-workflow-evaluations` | Run declared dataset/metric, first-run/replay and review grant/deny/expiry/revocation/payload-change cases. Preserve subject/model/prompt/tool/memory/policy/runtime identities and real external-cost policy. Reject stale subjects, missing probes/review/replay or failed thresholds at the real release consumer. |
| `execute-fuzz-and-property-fixtures` | Execute generated source/schema/MIR/math/package/workflow/runtime cases with seed, generator, shrinker and independent failure oracle; minimize and promote actual failures. Reject missing replay identity, altered failure causes, fabricated passes and undeclared live effects. Actual host failure is not a compiler-quota pass. |
| `execute-differential-conformance-fixtures` | Execute applicable versioned oracles on the same declared observables, modes and artifacts, recording permitted divergence and reproducers. Reject absent trust/applicability, unexplained drift, ignored diagnostic categories, wrong ABI/provenance or fresh nondeterminism where replay is required. |
| `execute-formal-evidence-checks` | Invoke actual declared checkers for exact claim/input/pass/before-after IR/assumption/trust subjects; retain counterexamples and invalidation. Reject unknown/stale checkers, unmet formal obligations, prose-only proofs and uncheckable certificates. The checker remains its own implementation authority. |
| `execute-performance-regression-fixtures` | Measure declared artifact/environment/device/metric with variance/repeat policy and explicit thresholds, after semantic/safety gates. Separate actual AI provider/tool/memory/review/replay costs. Reject wrong-result speedups, proofless elision, unavailable counters and incomparable or opaque samples. |
| `execute-staged-compiler-validation` | Invoke required TEST1-TEST12 suites with each actual claimed stage compiler, inspect real rebuild/comparison/provenance/TCB/audit results and reject missing or failing applicable evidence. Preserve subset limits and signed-or-otherwise-verifiable provenance; signing is mandatory only under the governing policy. |
| `implement-declared-seed-compiler-and-native-observations` | Reuse applicable narrow seed/native implementations and receipts; exercise the declared seed subset and exact-class native header/reference/UTF16 APIs only in their compiler-artifact meta/JVM scope. Preserve unsupported/nil distinctions, no-callback/non-forcing behavior, physical backing rules, sticky denial and ordinary-export exclusion. Do not repeat denied probes, cache DATA validity or claim seed retirement. |

TEST6 remains with `backend-conformance` in
[Backends](../backends-workflow/tasks.md#implementation-tasks); Release consumes
actual backend matrix, artifact/ABI/source-map/diagnostic and execution reports.
Math supplies its domain fixture/oracle/result contract, the independent checker
supplies exact verdicts, and these suite tasks execute their applicable cases.
Runtime, library and provider implementation remains with those owners.

The apparent stage cycle is split by named artifacts and versions. Build/run
the next candidate with the actual declared prior compiler, execute the required
subset suites, compare the two real compiler outputs, then let the intermediate
admission consumer assess that exact candidate's conformance, equivalence,
matrix, provenance, TCB and audit evidence. That decision does not manufacture
its inputs or require the candidate to have been admitted before ordinary
development tests. Final release acceptance separately consumes the complete
claimed closure and public command/rebuild/trust/governance evidence. Comparison,
admission and final release are not aliases for one another.

The five aggregate refinements also retain D2 documentation-only M0 lock gates,
source-bound coverage/status and replacement-parity claims, actual milestone
programs, complete support matrices and scoped trust reduction. Those are claim
obligations, not documentation locks on ordinary development. Public CLI behavior
comes from the existing CLI task; release consumes actual `check`, `run`,
`compile`, full conformance and successful self-host verification through the
required binary, preserving both source extensions and the nontrivial app.
Final rebuilds, four-boundary Clojure exclusion, signed supply-chain bundles and
eligibility require actual artifacts; hosted wrappers and supplied records do
not close them. Signing custody, external live calls and every existing held
Reader/Gravity/Bootstrap/C12/native/provider operation retain their actual policy.
This source reconciliation executes none of those operations.

Record real commands, inputs, exits, artifacts, diagnostics, failures/unrun
cases and residual host/provider boundaries in the existing evidence route.
New temporary tooling remains Clojure and Gravity remains the destination.
Independent review and coordinator integration remain separate from author
validation; no suite result or source-copy check awards itself admission.

## Release dependency projection and first executable boundary

Use the existing
[suite/admission dependency declarations](../../docs/acceptance-dependencies.md#release-suite-comparison-and-admission-boundaries).
All thirteen future producers and five existing aggregates now have explicit
selected-output joins in that representation. The checker preserves their 77
literal source criteria with exact task-relative UTF8 spans, hashes and owners.
Those source IDs and baseline links are not new tasks or canonical entailment
proof. All task bodies, source versions, Open states and narrow receipts remain
unchanged; current source/facet/repartition review is still required.

Implement a selected runner against declared subject/fixture/oracle interfaces
before composing actual outputs from the named stage. Reuse the existing focused
and self-hosting runners. Do not require a completed public self-hosted compiler
or authoritative migration admission merely to develop tests. Test both real
accepted execution and missing/stale/wrong-subject/negative-result refusals; label
partial, mock, recorded, live and actual-target scope accurately. TEST6 retains
its Backends authority; TEST7's permitted proof alternative and actual external
cost/credential/retention policy remain intact.

The candidate-build task produces an actual intermediate compiler and lineage;
the comparison owner computes raw comparison products; TEST13 executes required
subset suites/rebuild/trust checks; the existing comparison aggregate then owns
independent intermediate admission/denial. Final distribution and independent
release acceptance consume the full final artifacts and public command/seedless
proofs separately. Preserve the coarse cycles and these consumer-specific result
boundaries. A supplied success flag, runner report or author review cannot
manufacture its consumed evidence or confer independent acceptance.

The next project implementation preparation remains the existing Reader contract
coverage increment. A Release implementation increment must select one of these
existing source predicates and its exact real call path, report unrun/held work,
and retain compiler/source/fixture/profile/target/backend/runtime/provider/trust
identities. Future SH/full-language finish-line joins and global requirement
conjunctions remain pending. This planning work runs no compiler, native probe,
provider, migration, signing or publication operation and assigns no new person
or permission to an existing role.

## Full-language and terminal release navigation

The existing
[finish-line declarations](../../docs/acceptance-dependencies.md#full-language-and-release-finish-line-joins)
now connect full-language tasks, original SH21-SH29 and the separate Phase18
tasks/gates to selected component results. Use their complete source blocks and
proof rules; neither the high-level roadmap nor this plan is a second editable
component implementation ledger. Existing early audit/wrapper/package receipts
retain their original scope without becoming final public seedless evidence.

Build actual candidate/runner/comparison/provenance products before final proof
decisions. P15-S23 and applicable final Phase18 claims require genuine retirement;
full-language final evidence retains the broader accepted task conjunction and
the first supported seedless boundary. SH29 does not require all 240 language
documents merely to close its declared first-release scope. The full project's
broader language/target support remains required separately. Conditional Platform
Product adoption does not choose an ISA, OS, application inventory or physical
delivery boundary. Structural reachability establishes no executed command,
accepted source conjunction or independent release decision.

## Complete macro source interface

Use the [complete macro source criteria](../expand-workflow/plan.md#complete-macro-source-criteria). Consume actual owner corpus/trace/equivalence products before independent harness execution and compare the declared seed/Gravity subjects without widening finite receipts.

Existing task identities/statuses, historical reviews, actual restrictions and ordinary construction versus composition/acceptance boundaries remain. These are source-backed current criteria proposals; full source/interface entailment and independent current acceptance are pending.

## Macro dependency interface

Use the [macro producer and consumer order](../expand-workflow/plan.md#macro-producer-and-consumer-order) and existing selected-result table. Use actual owner and independent harness products, raw candidate comparisons and declared compatibility subjects before later admission. The nine-task feedback adds conditional governance; raw comparison, stable adoption and final release credit remain distinct.

Task bodies/statuses and frozen review subjects stay unchanged. Declared interfaces permit local construction; actual composition, acceptance and release retain their separate required results. Current independent source/interface acceptance is pending and every actual restriction remains.

## Syntax dependency interface

Use the [syntax producer and consumer order](../syntax-workflow/plan.md#syntax-producer-and-consumer-order) and existing selected-result table. Consume actual complete owner source/generated/fact/codec products before independently executed harnesses and claimed-stage comparison; existing SH-04 receipts and syntax round-trip confer no broader admission.

All task identities/statuses and historical review subjects remain unchanged. Actual selected products, independent acceptance and final release retain their own requirements; no held operation is enabled.

## Complete namespace and resolution interface

Use the [complete namespace and resolution source criteria](../resolve-workflow/plan.md#complete-namespace-and-resolution-source-criteria). Consume actual L3/C5 positive/negative and composition results for the claimed compiler closure. The frozen owner-source-Core subset, module schema or restored planning criteria cannot confer whole resolver, self-hosting or seed-retirement acceptance.

Task identities/statuses, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held execution or task transfer is authorized.

## Resolution dependency interface

Use the [resolution producer and consumer order](../resolve-workflow/plan.md#resolution-producer-and-consumer-order) and the existing selected-result table. Run independent language/compiler/profile harnesses after actual owner cases and selected receiver results. Compare full claimed-stage binding/module/diagnostic products; frozen SH06/owner-to-Core evidence, structural checks and source restoration confer no whole resolver, self-hosting or seed-retirement acceptance.

Existing task bodies/statuses, source predicates, frozen evidence and actual restrictions remain unchanged. Current source/interface entailment, independent acceptance and final release require their own applicable evidence; no held action is enabled.

## Complete Core semantic interface

Use the [complete Core source criteria](../core-workflow/plan.md#complete-core-source-criteria). Run real source/generated primitive, observable-order, profile/error/domain/metadata and malformed/tamper cases against actual owner and receiver products before claimed-stage comparisons. Preserve finite receipts without whole Core, self-hosting or seed-retirement promotion.

Task identities/statuses, prior source/review subjects and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held execution or task transfer is authorized.

## Core dependency interface

Use the [Core producer and consumer order](../core-workflow/plan.md#core-producer-and-consumer-order) and existing selected-result table. Independent harnesses consume actual owner fixtures, source-bound graph/analysis/transform/runtime results and refusal controls. Owner examples precede final harness execution; structural planning checks and frozen finite reviews confer no compiler or seedless release acceptance.

Existing task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current selected-interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete Types source interface

Use the [complete Types source criteria](../types-workflow/plan.md#complete-types-source-criteria). Execute actual owner type/schema/boundary/guard/branch/entry/loop and tamper/refusal fixtures before independently claiming full language/compiler conformance. Frozen source reviews and narrow current-runtime receipts establish no full C7 or seed retirement.

Current task identities/statuses, historical review subjects and original C7 publication/other actual restrictions remain unchanged. This source proposal grants no implementation or current independent acceptance.

## Types dependency interface

Use the [Types producer and consumer order](../types-workflow/plan.md#types-producer-and-consumer-order) and existing selected-result table. Independent language/compiler harnesses consume actual complete owner fixtures and type/verifier/invalidation/MIR/transform/target preservation/refusal products. Preserve full independent-inference premises and real claimed runtime observations; final suite acceptance cannot be an initial checker input.

Existing task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete pattern-matching source interface

Use the [complete pattern-matching source criteria](../core-workflow/plan.md#complete-pattern-matching-source-criteria). Independent conformance consumes actual owner fixtures for every pattern family/condition and representative target-equivalent behavior, including once-only/false/non-returning guards, totality/defaults, unvalidated shapes, ownership and stale/tampered artifacts. Owner preparation precedes final harness execution; narrow receipts do not close L7.

Task IDs/statuses, historical review scopes and actual restrictions remain unchanged. Current complete source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Pattern-matching dependency interface

Use the [pattern-matching producer and consumer order](../core-workflow/plan.md#pattern-matching-producer-and-consumer-order) and existing selected-result table. Independent language/compiler/backend harnesses consume actual owner family/profile/guard/coverage/validation/resource and representative target products. Record availability and nondeterminism honestly; first fixture construction does not wait for final harness or release acceptance.

Task bodies/statuses, source criteria, frozen review scopes and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending; no held operation is enabled.

## Complete protocol and dispatch source interface

Use the [complete protocol and dispatch source criteria](../types-workflow/plan.md#complete-protocol-and-dispatch-source-criteria). Independent conformance consumes actual owner fixtures, selected target/runtime behavior, seven diagnostics and mode/implementation identity across required L8 surfaces. Owner fixture construction precedes final independent harnesses; narrow receipts do not close L8.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Protocol and dispatch dependency interface

Use the [protocol and dispatch producer and consumer order](../types-workflow/plan.md#protocol-and-dispatch-producer-and-consumer-order) and existing selected-result table. Independent language/compiler/backend harnesses consume actual owner method/mode/effect/profile/host/tool/proof/fallback/diagnostic products; complete L8 needs its required specialization fixtures while initial valid compilation remains earlier.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Complete error-handling source interface

Use the [complete error-handling source criteria](../core-workflow/plan.md#complete-error-handling-source-criteria). Independent conformance consumes actual owner Option/Result/try/throw/panic/check/host/FFI/workflow/AI fixtures, constrained-versus-hosted lowering, Result/Option exhaustiveness, normalization, failure/compensation replay and schema/policy records. Held/unsupported/unavailable cases remain work, and owner construction precedes final harness execution.

Task identities/statuses, historical review scopes and actual restrictions remain. Current full source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Error-handling dependency interface

Use the [error-handling producer and consumer order](../core-workflow/plan.md#error-handling-producer-and-consumer-order) and existing selected-result table. Owner real Option/Result/try/throw/panic/check/host/FFI/workflow/AI fixtures precede independent harnesses. Actual host normalization, failure/compensation replay, schema/policy records and representative target failure behavior remain required; unsupported/held/unavailable cases stay open.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Complete library-principles source interface

Use the [complete library-principles source criteria](../stdlib-workflow/plan.md#complete-library-principles-source-criteria). Consume real library owner examples/fixtures/properties/audits and current module/profile results through existing independent conformance routes. Compiler-support closure, wider library modules, source acceptance, seed retirement and final release remain distinct.

Current source/interface entailment and independent acceptance remain pending. Task identities/statuses, finite historical reviews and actual restrictions survive; no held action is enabled.


## Library dependency interface

Use the [library producer and consumer order](../stdlib-workflow/plan.md#library-producer-and-consumer-order) and existing selected-result table. Owner examples, selected boundary/property/availability interfaces and actual APIs precede the dedicated independent library harness. Later exact component/self-hosting/release claims still require their own full evidence; no final acceptance gates the first API fixture.

Task bodies/statuses, source criteria, frozen reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance are pending; no held operation is enabled.

## Complete facet source interface

Use the [complete facet source criteria](../expand-workflow/plan.md#complete-facet-source-criteria). Consume actual owner facet fixtures through the existing independent compiler/language/profile/safety/domain harness tasks as applicable. Real at-least-two-facet composition and zk/schema/security/formal privacy cases, actual examples and eleven diagnostic families remain distinct from artifact inventories and final release/self-hosting acceptance. Held/unavailable required cases stay Open.

Current source/interface entailment and independent acceptance remain pending. Preserve existing task identities/statuses, historical review scopes and every actual restriction. No held execution is enabled.


## Facet dependency interface

Use the [facet producer and consumer order](../expand-workflow/plan.md#facet-producer-and-consumer-order) and existing selected-result table. Early real owner examples/property/checker/runnable fixtures precede independent language/compiler/profile/safety/formal/agent-workflow results. Actual at-least-two-facet and separately required zk/privacy cases remain; source generation grants no live action. Final selfhost/release acceptance is later.

Task bodies/statuses, source criteria, historical reviews and actual restrictions remain unchanged. Current source/interface entailment and independent acceptance remain pending. No held action is enabled.


## Complete alternative type system source interface

Use the [complete alternative type system source criteria](../types-workflow/plan.md#complete-alternative-type-system-source-criteria). Consume actual owner L5 accepted/rejected and provider/common-Core fixtures through existing independent language/compiler/profile/safety/formal harnesses as applicable. Compare the exact declared soundness population; one fixture, family counter or compatibility status cannot establish full provider or profile support.

Current source/interface entailment and independent acceptance remain pending. Preserve existing task identities/statuses, historical review scopes and every actual restriction. No held execution is enabled.


## Alternative type system dependency interface

Use the [alternative type system producer and consumer order](../types-workflow/plan.md#alternative-type-system-producer-and-consumer-order) and sole selected-result table. Complete L17 independent language fixtures consume early actual owner source/checker/oracle/corpus/check schemas, then execute genuine selected L5 comparison and generated-source cases under each distinct declared soundness meaning. Actual gradual boundary/failure/blame and unsafe-cast refusals remain required where claimed. Final Auth report and full selfhost/release are no first fixture; unavailable/held cases stay Open and supplied reports never pass. Complete L17 independent compiler fixtures consume early actual common schema/verifier/property/case interfaces, then execute selected real lowering/consumer/fact lifecycle and erased-check proof-link positive/refusal cases. Initial checker implementation precedes its final corpus result; raw/unoptimized cases retain their earlier route. Owner test flags do not confer independent acceptance or whole C7 publication. Complete L17 independent profile fixtures use early actual supported-feature/evidence/check/representation interfaces and candidate cases, then real supported/refused profile tuples and applicable theorem/proof/test/audit joins. Future complete Auth conformance is not the first oracle; genuine current independent results feed later admission/reporting. Same-profile guarantees and forbidden gradual metadata/allocation/dynamic behavior cannot weaken. Complete L17 independent safety fixtures consume early actual invariant/check/proof/ownership/cast schemas, then genuine selected D8 and unsafe-as-safe/refinement/nullability/fact-loss positives/refusals. Required real checked evidence precedes the affected safety claim. Final whole checker acceptance is not a first fixture; explicit unsafe and permitted runtime-checked outcomes retain their real obligations. Complete L17 independent formal-evidence fixtures use early real selected property/proof-schema/checker/subject interfaces, then actual required exact-subject proof/refinement and erased-check-reference verification. L17 permits the applicable theorem, proof, test-suite or audit method; this route invents no universal stronger method or whole future Formal prerequisite for ordinary checking. Final conformance follows genuine selected results, not accepted flags.

Task bodies/statuses, all source criteria, historical reviews and forty future source bindings remain unchanged. Current source/interface entailment, independent acceptance and integration remain pending. No held action is enabled.


## Complete interoperability and migration source interface

Use the [complete interoperability and migration source criteria](../runtime-workflow/plan.md#complete-interoperability-and-migration-source-criteria). Existing independent language/compiler/profile/runtime/safety/library harnesses execute actual selected owner cases and incumbent oracles for all eight compatibility-evidence families, including performance where sensitive and at least one real incumbent-language/schema parity report. Unavailable/held execution remains Open; projected passed reports and final release checkboxes are not first fixtures.

Current source/interface entailment and independent acceptance remain pending. Preserve existing task identities/statuses, historical review scopes and every actual restriction. No held execution is enabled.


## Interoperability dependency interface

Use the [interoperability producer and consumer order](../runtime-workflow/plan.md#interoperability-producer-and-consumer-order) and sole selected-result table. Complete L19 independent language fixtures consume early actual owner source/incumbent/schema/type-map/wrapper/oracle interfaces, then real complete declarations, conversions, errors, provenance and supported/refused profile cases. At least one actual incumbent-language/schema comparison remains required for parity; final source migration or full Runtime acceptance is not the first fixture, and held/unavailable cases do not pass. Complete L19 independent compiler fixtures consume early actual declaration/generator/verifier/map/drift/checker interfaces, then actual generated Q/common-consumer/source/ABI/schema invalidation and exact refusal results. A final passed parity report cannot supply its own first corpus/oracle; genuine source-bound products remain separate from metadata-only checks and full C7 publication. Complete L19 independent profile fixtures consume early truthful support/representation/service/availability and source cases, then actual allowed/refused eight-profile boundary tuples and host-leak diagnostics. Final backend or runtime family acceptance is no first profile oracle. Workflow remains distributed-facet scoped, unsupported/held targets remain work, and no physical product or universal live service is selected. Complete L19 independent runtime fixtures consume early actual wrapper/mapping/error/ownership/thread/root/service/ABI and incumbent-oracle contracts, then genuine selected values, Option absence, translated failures, transfer/release, blocking/concurrency and replay results. Real execution supports execution/parity claims rather than complete flags; held native/provider/callback/device actions remain unavailable, not successful fixtures. Complete L19 independent safety fixtures use early actual invariant/check/proof/unsafe/pointer/lifetime/allocator/thread schemas and cases, then real wrapper/foreign-boundary positives and raw-call/null/error/ownership/host-leak refusals. Required actual checks/proofs precede safe claims and source reviews do not renew themselves. A future complete safe wrapper cannot be the first invariant fixture. Complete L19 independent library fixtures consume early actual API/source/codec/validator/error/release/incumbent-oracle interfaces, then real selected public value/failure/cleanup/adapter and unsupported-profile results. Final standard-library or shim release is not the first fixture; API names and a projected parity report cannot prove usable implementation. Actual wider domain/backend obligations remain with their owners. Complete L19 performance-sensitive replacement comparisons use early actual selected incumbent/envelope/measurement/runtime/target/lock interfaces, then genuine owner-produced compared measurements before the specific performance/parity claim. This applies where replacement is performance-sensitive, not as a universal benchmark gate on ordinary declaration/schema work. Preserve real D6 evidence and actual host/platform limits while project work/input/cost/attempt accounting remains removed.

Task bodies/statuses, all source criteria, historical reviews and forty future source bindings remain unchanged. Current source/interface entailment, independent acceptance and integration remain pending. No held action is enabled.


## B1-B3 dependency interface

Use the [B1-B3 producer and consumer order](../backends-workflow/plan.md#b1-b3-producer-and-consumer-order) and sole selected-result table. Complete B1-B3 independent compiler checks first use actual owner positive/refusal/target/toolchain/pass/oracle interfaces. Later execute selected C declared-flag compilation and LLVM pre/post verifier, metadata/repair, proofless attribute/poison/UB/unsupported cases and required MIR differential comparisons against real outputs. Optional Gravity Opt and certificate-specific cases run only for their selected claims; shared refusals stay applicable. Whole self-hosting/release is not the first fixture input, and no held target run becomes authorized by this plan. Complete B1-B3 independent profile checks consume early actual target/runtime/helper support and profile oracle contracts, then actual selected eligible and refused compiler artifacts. Exercise no-runtime/firmware/kernel hidden-service failures, incompatible layouts/features, helper selection and preserved compile-time legality. No final native execution or whole family acceptance is required for initial profile fixtures; unavailable target records cannot skip required shared eligibility/refusal checks. Complete B1-B3 independent safety checks begin with real operation/proof/check/audit/numeric/ownership/atomic/oracle interfaces, then actual emitted and refused subjects. Exercise missing/stale/wrong-subject assumptions, pointer/lifetime/provenance, invalid shifts/overflow, strict/relaxed certificate cases and metadata loss independent of equal results. Certificates apply only to affected operations; safe defined behavior and four safety outcomes remain mandatory. Whole target execution is not the first checker contract and no quota-based refusal replaces semantics. Imported certificate use additionally needs actual compatible package/compiler/profile/target/provider/schema/digest and caller-policy trust verification before reliance; a package name or old signature is not sufficient. Complete B1-B3 independent Runtime checks expose actual ABI/helper/value/error/cleanup/FFI/synchronization and comparator interfaces, then actual permitted selected linked programs and execution/refusal records for the claimed C or LLVM runtime path. Compare Gravity-visible numeric/error/resource/order behavior and manifests with exact target/provider/flags/pass identities. Early fixture work does not await whole Runtime/backend acceptance; real execution remains required where claimed and actual holds persist.

Task bodies/statuses, all twenty source criteria, historical review scopes and forty future source bindings remain unchanged. Current source/interface entailment, implementation and independent acceptance remain unassessed. No held operation is enabled.


## Complete B4-B14 source interface

Use the [complete B4-B14 source criteria](../backends-workflow/plan.md#complete-b4-b14-source-criteria). Compose actual B14 shared/per-family matrices and exact diagnostic/metadata/artifact/differential-reference/replay packs, including accepted/refused and partial-output cases. Target availability explains only justified skips; required shared checks remain and a default-target run cannot accept wider support. Preserve TEST6's original conditional scope without weakening B14. Actual policy-required conformance/reproduction and content provenance govern stabilization/release; final self-hosting/seedless work is not the first family fixture gate.

Original task identities/statuses, source predicates and historical review scopes remain; current whole-source/interface entailment and implementation acceptance are unassessed. No held operation is enabled.


## B4-B14 dependency interface

Use the [B4-B14 producer and consumer order](../backends-workflow/plan.md#b4-b14-producer-and-consumer-order) and sole selected-result table. Complete B4-B14 selected interface: Independent compiler fixtures use actual family input/output/negative/verifier/oracle contracts early, then real permitted selected lowering, conversion verification, semantic/reference comparisons and independent metadata/manifest/diagnostic results. Full target execution or final Release is no initial fixture gate; actual required selected execution remains required later and unavailable cases retain shared checks. Complete B4-B14 selected interface: Independent profile fixtures use genuine selected feature/service/availability and rejection contracts early, then actual eligible/refused artifacts and source/helper/grant/metadata checks. No-runtime, hardware, host, GPU, workflow and mobile restrictions remain distinct; an unavailable target cannot skip applicable shared manifest or refusal checks. Complete B4-B14 selected interface: Independent safety fixtures use actual ownership/lifetime/taint/proof/numeric/order/CDC/authority oracle contracts early, then actual selected emitted and refused subjects and current checker results. Preserve semantic numeric error bounds, proof direction and invalidation; target UB, GC, sandboxing or generic unsafe records cannot replace the governing source obligations. Real policy checks are not removed for speed. Complete B4-B14 selected interface: Independent runtime fixtures use real selected ABI/value/error/root/resource/async/order and comparator contracts first, then actual permitted linked programs, execution/refusal records and required independent metadata checks. Explicit availability explains only justified target skips; shared checks continue, and an injected provider/default success record is no runtime acceptance. Complete B4-B14 selected interface: Independent agent/workflow evaluations use real selected model/prompt/message/tool/provider/policy/schema/review/event-log and comparator contracts early, then actual required permitted graph/adapter/evaluation/restart/replay results. A final evaluation pack is no initial model schema or operation grant; fake-provider scope cannot expand into live-provider acceptance.

All task bodies/statuses, 84 source criteria, historical receipt/review scopes and forty future declarations/bindings remain exact. Current source/interface entailment and independent acceptance remain pending. No implementation or held action is enabled.

## Current collections source interface

Use the [complete current collections criteria](../stdlib-workflow/plan.md#complete-current-collections-source-criteria). Independent library conformance consumes actual callable module/profile/representation fixtures, expected STD3001-STD3008 diagnostics, complete-census and invalid tail/value cases, type/flow and lifetime/proof products, property/benchmark/schema-boundary results and real selected target comparisons. Source inventory, p16 summary status flags and an injected fixture do not establish module acceptance. Preserve finite receipt scopes and actual holds.

Existing task identities/statuses, historical evidence and actual restrictions remain. Current whole-source/interface acceptance is pending and no held operation is enabled.

## Library foundation source interface

Use the [complete library foundation criteria](../stdlib-workflow/plan.md#complete-library-foundation-source-criteria). Independent library suites consume actual callable positives, required refusals, per-profile examples, proof/provider/audit and benchmark/compatibility products. Bootstrap/compiler owners retain unsafe implementation boundaries; actual self-hosted closure/trust evidence consumes their audits without treating metadata, plans or seed packaging as execution.

Existing task status, historical evidence, actual restrictions and forty future declarations/bindings remain. Current whole-source/interface acceptance is pending; no held operation is enabled.

## Concurrency and IO library source interface

Use the [complete concurrency and IO library criteria](../stdlib-workflow/plan.md#complete-concurrency-and-io-library-source-criteria). Existing TEST7 and broader language/runtime/profile/AI-workflow suites produce real legal/illegal module, race/lifetime, path/encoding/durability, TLS/schema/redaction and replay results for exact claimed subjects. Library assembles those outputs; Release consumes them with current independent audits/review. Existing future suite predicates/roles remain exact; no test catalog, p16 status, seed package or narrow Linux LLVM receipt closes wider service support.

Existing task identities/statuses, historical evidence, forty future declarations/source bindings and actual restrictions remain. Current whole-source/interface acceptance is pending; no held operation is enabled.

## Library source dependency interface

Use the [Library source producer and consumer order](../stdlib-workflow/plan.md#library-source-producer-and-consumer-order) and the sole selected-result table. Complete STD1-STD9 selected library order: STD1-STD9 self-hosted closure uses early authentic source/export/profile/provider/unsafe-boundary and build/fixture interfaces, then actual selected compiled callable compiler/runtime/library/build products and required current audit/trust/conformance evidence before a real prior-compiler rebuild/run. Compiler-support selection is narrower than all required wider modules; broader full-language/product release retains its separate complete obligations. A p16 summary, seed package, final signed report or supplied next-stage receipt is not the prior compiler or callable closure; no future release result is its own first bootstrap premise. Complete STD1-STD9 selected library order: STD1-STD9 library suites first define real public operation/profile/provider/source/diagnostic and independent oracle/fixture expectations. Actual checked callable subjects and selected genuine runtime/provider results precede executed positive/refusal, compiled example, Unicode/numeric/collection, race/lifetime, path/symlink/encoding/durability, TLS/body/redaction and record/replay results. Those results precede whole module/release claims; the final suite result is not the API or fixture's first construction premise. Required unavailable/held cases stay Open, and mock or p16 summaries prove no live support. Complete STD1-STD9 selected library order: STD6-STD9 runtime suites use independent real provider/lifecycle/scheduler/order/service-gate/event/replay fixture contracts before actual subject execution. Real selected startup, resource/task/channel/actor, file/socket/stream/HTTP and denied/cleanup/recorded operations precede the claimed runtime results. Include no-action-after-denial, deterministic close and redacted host-normalized errors. Neither generic Runtime completion nor a future test result is the first primitive interface, and fakes do not accept a live service. Complete STD1-STD9 selected library order: STD1-STD9 profile suites begin with the genuine declared module/operation/target/provider matrix and accepted/refused example expectations. Actual checked imports/calls and selected compiled/executed products precede evidence of availability or forbidden blocking/ambient access. Pure core/URL/data claims require no live IO; firmware/kernel physical contracts, distributed recorded services, AI tool/review gates, formal hooks and meta compiler capabilities remain distinct. A built availability label or library suite name is not a profile result. Complete STD1-STD9 selected library order: STD1-STD9 safety suites first have actual independent race/capture/borrow/init/linear/wrapper/path/taint/proof and denial fixture expectations. Real checked source and actual selected safe operation/refusal/cleanup products precede conformance, including complete physical collection validation, scoped task/handle lifetime, root escape, invalid TLS/body/secret and current unsafe audit cases. Debug-only detection or an assigned proven flag cannot discharge the source requirement. Exact required proof and target preservation precede check-erasure claims; held operations stay unrun. Complete STD1-STD9 selected library order: STD7-STD9 AI/workflow evaluations start with real typed tool/model/service policy, required review, eval ordering, schema/taint/redaction and recorded-activity expectations. Actual selected allowed/refused source/runtime and permitted recorded results precede review/eval/replay claims; tool schemas or review-request capabilities issue no approval. Project budget clauses stay explicitly superseded while actual external account/billing and service timeout/retry semantics remain. No future evaluation report is its own first policy and no held external action is enabled. Complete STD1-STD9 selected library order: STD1-STD9 performance suites define real module/operation/profile/target/allocator/region/layout/compiler/scheduler/channel/lock/atomic workload and measurement contracts before benchmarking the actual callable subject. Actual correct checked implementations, permitted selected execution and reproducible measurement products precede any performance claim. Host defaults, summary counts or benchmark names establish no result; optional optimization is not a prerequisite for a valid unoptimized baseline and physical resource semantics do not reinstate project quotas.

All current/future task bodies, 101 source criteria, canonical versions and historical receipt/review scopes remain exact. These selected inputs do not prove all old route edges or current global executable order. Whole-source/interface and independent acceptance remain pending; no implementation or held action is enabled.

## Historical workflow-transfer plan

# Implementation plan: Release workflow migration

Spec: [spec.md](spec.md). Owner: backend workflow migration owner.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification workflow](../../docs/development-verification.md) and
[canonical contracts](spec.md#purpose-and-canonical-contracts).
The workflow records work and evidence; it does not replace normative authority.

## Approach and exact files

Keep the four workflow artifacts and direct roadmap task link.

| Canonical truth | Reference | Package use |
| --- | --- | --- |
| Finite outcome | [Release outcome](../../docs/component-roadmaps/release.md#release-outcome) | Declares component scope, not migration completion |
| Implementation tasks | [Release canonical remaining work](../../docs/component-roadmaps/release.md#remaining-tasks-and-evidence) | the five remaining-task rows with selected-compiler prerequisite narrowing; retain their original status and evidence |
| Component acceptance | [Acceptance criteria](../../docs/component-roadmaps/release.md#acceptance-and-integration) | Required semantic evidence stays here |
| Phase tasks | [Phase implementation roadmap](../../docs/phase-15-bootstrap-and-self-hosting/IMPLEMENTATION-ROADMAP.md) | Historical/current phase claims retain original scope |

Component role: bootstrap/release maintainer. Interface owners: [runtime](../../docs/component-roadmaps/runtime.md#release-outcome), [stdlib](../../docs/component-roadmaps/stdlib.md#release-outcome), [build](../../docs/component-roadmaps/build.md#release-outcome), [cli](../../docs/component-roadmaps/cli.md#release-outcome), [auth](../../docs/component-roadmaps/auth.md#release-outcome).
Release admission consumes accepted component outcomes and exact candidate evidence. Ordinary development is not release admission. Runtime compiler-services, Standard Library compiler-support and selected compiler CLI outcomes bound the compiler prerequisite surface; wider platform outcomes remain separate. No seed retirement follows from this migration.

## Validation and evidence

Check ASCII, the exact seven-file set, local Markdown target/anchor resolution,
semantic package names and absence of duplicated component checklists.
Negative acceptance cases are broken/missing links, treating artifact presence
as implementation completion, or narrowing retained contracts without disposition.
Run the existing structural documentation and full-language roadmap validators.
Run focused task-ledger navigation and language-boundary checks; no
compiler/service execution suite is claimed. Record commands/results and exact review identity in evidence.md.

## Delivery and remaining work

Publish partial artifact work in a draft PR. Keep package review and integration
unaccepted until their actual receipts exist. Deus owns final integration;
repository validation cannot confer semantic component or release acceptance.
Do not add a recurring acceptance loop solely because evidence text changes.

## Names, callers and compatibility

No source names, callers, wire/schema identities, load order or historical pins
change. Retain original task identities and phase anchors. Future source work
uses subsystem/behavior names under the semantic naming rule, separately reviewed.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [TEST13](../../docs/phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md#requirements): Stage admission requires declared trusted inputs, conformance, rebuild comparisons, provenance and TCB delta. Gravity source presence or a wrapper does not establish seed retirement.
- [GOV2](../../docs/phase-17-governance-and-evolution/232-gov2-compatibility-policy.md#requirements): Consume baseline/profile/target/artifact compatibility reports and migration records for release claims; Release does not approve semantic breaks.

## Governance evidence consumers

This bounded requirement-reconciliation correction binds four previously
unlinked contracts to the existing task
`release/remaining-tasks-and-evidence/bind-each-claimed-component-to-exact-implementation-and-conformance`
in [tasks.md](tasks.md#implementation-tasks). Its ID, title, Open status and
closing evidence remain byte-identical. This table explains the applicable
governance inputs to its existing positive/negative evidence obligation; it
does not amend task acceptance. The existing exact-candidate independent
acceptance task remains separate. No implementation record is added.

| Contract | Evidence consumed by Release | Required negative evidence |
| --- | --- | --- |
| [GOV1](../../docs/phase-17-governance-and-evolution/231-gov1-language-evolution-process.md#conformance-criteria) | Owned change ID, affected contracts, decision history and trace through implementation, conformance and release notes | Ownerless changes, untraceable accepted changes and omitted user-visible release notes reject admission |
| [GOV6](../../docs/phase-17-governance-and-evolution/236-gov6-rfc-process.md#conformance-criteria) | RFC decision, affected contracts, design/impact/test/migration sections, applicable specialized reviews and implementation/validation links | Missing required sections, review history or implementation/conformance links cannot support release |
| [GOV7](../../docs/phase-17-governance-and-evolution/237-gov7-experimental-feature-policy.md#conformance-criteria) | Explicit opt-in, stable default-off or governed preview-channel decision, profile availability, expiry, rollback, stabilization/removal criteria, package/build dependencies, on/off fixtures and lifecycle notes | Accidental experimental use, missing dependency metadata or expiry without a reviewed decision rejects admission |
| [GOV8](../../docs/phase-17-governance-and-evolution/238-gov8-deprecation-and-stabilization-policy.md#conformance-criteria) | Exact stabilization surfaces and compatibility/conformance/negative-fixture history; or replacement/rationale, diagnostic, schedule and migration records; tests through removal and release lifecycle notes | Evidence-free stabilization, missing migration metadata or removal before the approved window without the security-emergency records rejects admission |

These are consumer interfaces, not assignment of language-policy authority to
Release. The broader evolution workflow proposed in
[contract mapping](../../.specify/contract-mapping.md#evolution-workflow-proposal)
still needs an accountable policy owner and full lifecycle implementation.
[FL-P17-T01](../../docs/full-language-implementation-gap-map.md#phase-17---governance-and-evolution)
remains Open for real command-produced records and release-blocking behavior.
The Phase 17 stage0 history is preserved; it does not accept this wider gate.

PR445's proposed requirement population and missing-interface census are not
recomputed by adding these links. Source/facet-to-task entailment still needs
exact-manifest review; no candidate requirement gains implementation credit.
