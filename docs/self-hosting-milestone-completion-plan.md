# Finite Self-Hosting Milestone Completion Plan: SH-00 through SH-29

Status: implementation and acceptance plan, not an evidence bundle.

The [SH-00 through SH-06 evidence reconciliation](self-hosting-sh00-sh06-evidence-audit.md)
maps the first 42 requirements to retained evidence and records current scope
gaps separately from historical completion credit.
Baseline: completed development revision
`e63a7feec4be13fa6176f9bbafce38bbb3509a54`, reconciled with main
`a682cd50c98847be4b80dbdc1a5b73c38ac25960` for integration.
The completed development commits accompany this plan; unfinished working-tree
changes do not. These Git revisions identify planning inputs, not renewed
migration evidence or independent acceptance.

## Purpose and authority

This is the detailed completion checklist for the 30 milestones in the
[self-hosting backlog](self-hosting-slice-backlog.md#countable-backlog).
It gives each milestone a finite deliverable, six numbered acceptance
requirements, an implementation owner, validation starting points, an affected
composition boundary, and an explicit stopping rule. The 180 requirements are
planning units, not 180 equally sized tasks or claims of passing tests.

This plan is supplemental: it does not add a 241st normative document, change
language semantics, assign file reservations, or replace the backlog's status
and admission records. D2 governs ordering; D3 governs terminology; D8 governs
safety; D9 governs evidence. BOOT3, BOOT5, BOOT7, BOOT8 and TEST13 govern
authoritative migration. Contract IDs below resolve through the
[document index](document-sequence.md). Read the owning documents before
implementing a requirement; a checklist summary does not replace them.

The first finish line is a genuinely seedless compiler for the declared
bootstrap language and one declared release target. SH-29 does not mean every
feature in all 240 documents or every backend is complete. The existing master
goal remains full documented language and target coverage after that first
finish line; use the [full-language gap map](full-language-implementation-gap-map.md)
and the Phase 18 release roadmap for the remaining product gates.

## Dependencies and finite scope

The dependencies in the index below are unchanged from the backlog. They gate
completion credit, not useful implementation against a documented upstream
interface. A pending downstream stage is not an excuse to give an earlier
stage runtime, type, or release responsibilities it does not own.

The bootstrap subset is not whatever the current fixtures happen to cover.
For each stable candidate, enumerate the compiler, runtime, library and build
source closure needed to build and run that compiler, including generated
code, macros, transitive primitives, providers, imported modules and their
required behavior. Each required construct belongs to one of these milestone
requirements. An unimplemented construct needed by that closure blocks the
owning milestone; it cannot be excluded merely to obtain a passing count.

Use the existing compiler inventory, pass manifests and BOOT5 matrix for this
enumeration, rather than another authority system. SH-21 checks legality,
SH-22 closes required library APIs, SH-23 closes the build graph, SH-24 closes
the driver, and SH-25 verifies the complete executable component closure.
Earlier milestones can accept bounded module revisions before SH-25, but the
final compiler closure must consume those revisions or accepted successors.

Freeze the following together for an actual migration or release decision:

- Source/module closure and required semantic variants, not only file names.
- Exact profile, target triple, ABI, backend, runtime and provider set.
- Required package/build inputs, environment controls and comparison modes.
- Acceptance and rejection cases, preservation invariants and evidence class.
- Explicit unsupported features and residual seed/host/tool dependencies.

That is a finite acceptance population for a candidate, not a proof run before
each edit. New optional language features and additional targets go into the
later coverage matrix. Discovering a missing dependency of the frozen
compiler, a semantic contradiction, or a safety defect is a correctness gap,
not optional scope. Amend the affected requirement explicitly; do not silently
weaken the contract or indefinitely add unrelated features to a milestone.

The current staged Linux LLVM candidate and host-specific native harnesses
must not be conflated. An available Darwin toolchain does not establish the
Linux candidate's execution or release support. Changing the selected release
target requires the corresponding ABI, provider, build and conformance changes.

## Three boundaries and one shared completion rule

Follow [development verification](development-verification.md), using existing
compiler interfaces and direct tests:

| Boundary | What is sufficient for that boundary | What it does not establish |
| --- | --- | --- |
| Implementation | Owning implementation changes; real accepted and rejected cases; focused semantic and preservation checks. | Authoritative module migration or milestone completion. |
| Integration | Affected producer-to-consumer composition, interface, diagnostic and regression checks. | Whole-stage equivalence, all targets or seed retirement. |
| Authoritative completion | All six requirements, completed dependencies, applicable uniform backlog gates and fresh stable-revision migration evidence independently accepted. | Features, targets or release claims outside the recorded support set. |

Before marking any SH-03 or later milestone complete:

1. Map every numbered requirement and required variant to its owning contract,
   implementation entrypoint, accepted `.gravity`/`.qst` fixture pair,
   rejection-family fixtures, expected artifact/result or diagnostic, and
   actual selected checks. Missing cases are open work, not implicit passes.
2. Execute the claimed Gravity implementation. Classifying supplied records,
   emitting a report, interpreting an unrelated host implementation, or
   compiling source without exercising the claimed behavior is insufficient.
3. Preserve or explicitly transform the pass's required source, origin,
   binding, type, effect, capability, ownership, safety and artifact facts.
   Test malformed outputs and relevant stale/substituted boundary inputs.
4. Pass focused checks and affected integration/regressions; preserve failed,
   interrupted, skipped and unsupported results accurately. A target emission
   claim additionally needs an independent target tool's validation/execution.
5. At explicit admission, satisfy BOOT3/TEST13 stage comparison, conformance,
   source/compiler/dependency provenance, BOOT5 support and TCB accounting,
   unsafe audits and required independent review for that migration. SH-26
   through SH-29 additionally require their whole-stage/release checks.
6. Record an accepted clean verified revision and evidence references in the
   existing backlog/compatibility records. Keep implementation, integration
   and authoritative status separate. A stale proof stays stale after edits;
   it does not prohibit focused testing of those edits.

SH-00 through SH-02 retain their already accepted baseline/process/envelope
scope. They do not acquire new language obligations from this document.
Recorded completion is not reopened merely to reissue unchanged evidence.
Reuse valid unchanged expensive evidence; refresh affected evidence when
source, compiler, semantic inputs, target, dependencies or policy invalidate it.
Preserve existing resource limits and admission requirements for heavy runs.

### How to use the check references

Unless an explicit different path is given, test file names below are relative to
`bootstrap/clojure/test/gravity/self_hosting/`. Their namespace prefix is
`gravity.self-hosting`; the file stem's underscores become hyphens. They are
existing starting points, not assertions of exhaustive coverage or low cost.
Inspect and select the exact vars for the requirement being changed:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --list
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --exact sh07-b10-quoted-symbol-development-kernel
git diff --check
```

Listing loads the namespace but does not run its tests. For another behavior,
use its actual owning selector, not the quoted-symbol example. Do not run
every listed namespace as a prerequisite for a small edit. Some namespaces
construct expensive authenticated upstream products; select direct semantic
kernels for feedback and affected composition separately. Add missing cases
to the owning tests, not a new wrapper or lane. There is no claim here that a
single existing command validates an entire future SH milestone.

## Milestone index

These are baseline recorded states, not a new audit. The backlog remains the
live state record. Requirements below define completion without promoting any
state: 7 complete, 11 partial, 8 queued and 4 blocked.

| Milestone | Deliverable | Completion dependencies | Baseline state |
| --- | --- | --- | --- |
| [SH-00](#sh-00---accepted-comparison-baseline) | Accepted comparison baseline | none | complete |
| [SH-01](#sh-01---module-and-test-ownership) | Module/test ownership | SH-00 | complete |
| [SH-02](#sh-02---reusable-authenticated-envelopes) | Reusable authenticated envelopes | SH-00 | complete |
| [SH-03](#sh-03---bootstrap-reader) | Bootstrap reader | SH-01, SH-02 | complete |
| [SH-04](#sh-04---syntax-objects) | Syntax, hygiene and origins | SH-03 | complete |
| [SH-05](#sh-05---bootstrap-macroexpansion) | Bootstrap macroexpansion | SH-04 | complete |
| [SH-06](#sh-06---namespace-and-binding-resolution) | Namespace/binding resolution | SH-05 | complete |
| [SH-07](#sh-07---core-form-semantics-and-lowering) | Core semantics/lowering | SH-06 | partial |
| [SH-08](#sh-08---bootstrap-types) | Bootstrap types | SH-07 | partial |
| [SH-09](#sh-09---effects-capabilities-and-profiles) | Effects, capabilities, profiles | SH-08 | partial |
| [SH-10](#sh-10---ownership-lifetimes-and-initialization) | Ownership and memory analysis | SH-08 | partial |
| [SH-11](#sh-11---safety-outcomes) | Safety outcomes | SH-09, SH-10 | partial |
| [SH-12](#sh-12---mir-construction-and-verification) | Verified MIR | SH-11 | partial |
| [SH-13](#sh-13---executable-functions-and-control-flow) | Functions/control through MIR | SH-12 | partial |
| [SH-14](#sh-14---data-layout-and-allocation) | Data/layout/allocation through MIR | SH-12 | queued |
| [SH-15](#sh-15---compiler-diagnostics) | Gravity-owned diagnostics | SH-13, SH-14 | partial |
| [SH-16](#sh-16---required-mir-optimization) | Required MIR passes | SH-15 | partial |
| [SH-17](#sh-17---target-lowering-boundary) | Target-lowering/backend boundary | SH-16 | partial |
| [SH-18](#sh-18---one-native-target) | Independently executed native program | SH-17 | queued |
| [SH-19](#sh-19---self-hosting-runtime-subset) | Runtime outside Clojure | SH-11, SH-17 | queued |
| [SH-20](#sh-20---artifact-emission-and-provenance) | Release-grade emission | SH-17 | partial |
| [SH-21](#sh-21---legal-meta-compiler) | Executable legal meta compiler modules | SH-15, SH-17, SH-19 | queued |
| [SH-22](#sh-22---bootstrap-standard-library) | Required standard library | SH-13, SH-14, SH-19 | queued |
| [SH-23](#sh-23---hermetic-package-and-build-subset) | Hermetic package/build path | SH-20, SH-21, SH-22 | queued |
| [SH-24](#sh-24---compiler-driver-and-cli) | Self-hosted driver/CLI | SH-18, SH-19, SH-20, SH-23 | queued |
| [SH-25](#sh-25---complete-compiler-component-build) | Complete executable component build | SH-21, SH-22, SH-24 | queued |
| [SH-26](#sh-26---actual-next-stage-rebuild) | Actual prior-stage rebuild | SH-25 | blocked |
| [SH-27](#sh-27---equivalence-and-reproducibility) | Equivalent clean rebuilds | SH-26 | blocked |
| [SH-28](#sh-28---trust-provenance-and-seed-boundary) | Trust/provenance/TCB closure | SH-27 | blocked |
| [SH-29](#sh-29---public-seedless-release) | Public seedless release | SH-28 | blocked |

## SH-00 - Accepted comparison baseline

Contracts: C11, C14, B1, B4, B14, D9. Owner: the bounded C11-C14-B1-B4
comparison path and its existing backend fixtures, not every Wasm feature.

1. **SH-00.1:** Preserve the exact claimed typed MIR operation subset and its
   checked lowering request as the comparison input.
2. **SH-00.2:** Produce a genuine Wasm artifact through the declared backend,
   preserving source, legality and artifact metadata.
3. **SH-00.3:** Independently validate/execute the accepted artifact and compare
   its observable result with the declared reference behavior.
4. **SH-00.4:** Reject malformed MIR, unsupported lowering and mismatched or
   substituted comparison inputs without target-dependent legalization.
5. **SH-00.5:** Retain the independent review, tool identities and bounded
   comparison results, including all limitations.
6. **SH-00.6:** Keep that accepted work committed with a clean integration
   baseline and explicit remaining seed boundary.

Checks: the existing exact vars
`authenticated-binary-integer-comparisons-reach-gravity-b4-wasm`,
`authenticated-binary-integer-comparisons-run-real-wasm` and
`authenticated-binary-integer-comparisons-fail-closed-at-b4` in
`bootstrap/clojure/test/gravity/bootstrap_test.clj`. Retain accepted unchanged
evidence rather than replaying these expensive checks for this planning change.
Integration: MIR -> lowering -> backend -> external comparison.
Stop: retain the accepted bounded baseline; do not reopen it for all targets.

## SH-01 - Module and test ownership

Contracts: D1, D2, BOOT4, TEST2. Owner: existing module/test ownership records,
stage-owned implementation files and thin compatibility entrypoints.

1. **SH-01.1:** Name one semantic owner for each compiler stage and public API.
2. **SH-01.2:** Name owners for fixtures, tests, artifact generation, coverage
   and shared routing; no executable shared file has competing writers.
3. **SH-01.3:** Permit a focused test to be added/run without unrelated edits
   to a monolithic bootstrap test namespace.
4. **SH-01.4:** Keep compatibility entrypoints delegated to their stage owner;
   reject duplicate semantic implementations masquerading as adapters.
5. **SH-01.5:** Select affected tests correctly, reject unknown selectors and
   preserve actual failures rather than counting skips as passes.
6. **SH-01.6:** Verify representative leaf and shared changes follow the
   ownership boundary without unrelated compiler modifications.

Checks: `sh01_ownership_test.clj`, `sh01_language_boundary_test.clj`,
`sh01_development_test_runner_test.clj` and affected routing tests.
Integration: shared dispatch/test discovery and stage compatibility facade.
Stop: the accepted ownership boundary; no mass namespace refactor or new
coordination framework is required for each semantic change.

## SH-02 - Reusable authenticated envelopes

Contracts: D9, C18, BOOT8. Owner:
`bootstrap/gravity/src/gravity/compiler/authenticated_envelope.gravity`.

1. **SH-02.1:** Use one reusable envelope contract at two distinct compiler
   stages, with explicit producer, consumer and artifact schema identities.
2. **SH-02.2:** Bind source revision, semantic inputs and required preserved
   facts to the actual output lineage.
3. **SH-02.3:** Compute deterministic path-neutral semantic identity while
   retaining actual source-path provenance separately.
4. **SH-02.4:** Reject stale, substituted and cross-stage inputs even when
   an attacker changes related redundant fields coherently.
5. **SH-02.5:** Reject malformed/cyclic/over-bound graphs before uncontrolled
   traversal; failures are structured and attributable.
6. **SH-02.6:** Exercise valid two-stage composition and invalid envelopes
   through the real shared verifier, with explicit remaining trust.

Checks: `sh02_authenticated_envelope_test.clj` and
`sh02_sh06_authenticated_plan_context_test.clj`.
Integration: the two selected stage producers/consumers.
Stop: accepted reusable verification, not an admission prerequisite for every
local test or a new envelope version for each development increment.

## SH-03 - Bootstrap reader

Contracts: L1, C2, BOOT2, TEST1, TEST2. Owner:
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity`, with the declared
L1/C2 reader module and thin seed adapter.

1. **SH-03.1:** Decode the claimed bootstrap source/literals, Unicode and
   newline policy, retaining exact positions and co-canonical source paths.
2. **SH-03.2:** Read lists, vectors, maps, sets and nesting with complete
   delimiter, token and end-of-input rules for the compiler closure.
3. **SH-03.3:** Read required abbreviations, quoting, metadata and reader
   extension syntax without executing ungranted host behavior.
4. **SH-03.4:** Reject each malformed encoding, literal, delimiter, extension
   and resource-bound family with a stable reader diagnostic and span.
5. **SH-03.5:** Serialize/round-trip source forms and occurrences without
   losing distinctions needed by syntax, resolution and lowering.
6. **SH-03.6:** Execute the Gravity reader over the declared compiler source
   subset and compare accepted forms/rejected diagnostics with the stage
   reference; a Clojure reread cannot supply the claimed behavior.

Checks: `sh03_reader_adapter_test.clj`; reader source-coverage cases in the
existing SH-07 coverage tests when the compiler source cohort changes.
Integration: bytes -> forms/occurrences -> SH-04 syntax.
Stop: accepted bootstrap reader, not every future reader extension.

## SH-04 - Syntax objects

Contracts: C3, L3, L4, TEST2. Owner:
`bootstrap/gravity/src/gravity/bootstrap/syntax.gravity` and the declared C3
module; preserve the existing owner/compatibility split.

1. **SH-04.1:** Construct syntax objects from reader occurrences with value,
   span, namespace, phase, profile and metadata context.
2. **SH-04.2:** Represent lexical scopes and hygiene marks without accidental
   capture or conflating same-spelled bindings.
3. **SH-04.3:** Retain generated-origin chains to both macro producer and
   originating source, including nested generated forms.
4. **SH-04.4:** Serialize/deserialize the full claimed syntax representation
   with deterministic identity and preserved occurrence/source distinctions.
5. **SH-04.5:** Reject broken references, origin cycles, invalid scope/mark
   relations, malformed metadata and over-bound syntax graphs.
6. **SH-04.6:** Supply real SH-03 -> SH-04 -> SH-05 inputs without host-created
   syntax facts being mistaken for Gravity construction evidence.

Checks: `sh04_syntax_adapter_test.clj` and affected literal projection cases.
Integration: reader occurrence transport -> syntax -> macro/resolution.
Stop: syntax/hygiene/origin construction, not macro execution or type checking.

## SH-05 - Bootstrap macroexpansion

Contracts: L4, L12, C4, P3, BOOT3. Owner:
`bootstrap/gravity/src/gravity/macro.gravity` and the declared C4 engine.

1. **SH-05.1:** Expand every macro required by the compiler source closure,
   including recursive expansion to the declared expanded-syntax boundary.
2. **SH-05.2:** Preserve scopes, hygiene, source locations and nested generated
   origins through introduced and reused syntax.
3. **SH-05.3:** Enforce phase separation and caller-profile legality; generated
   code re-enters ordinary resolution/core/type/effect checks.
4. **SH-05.4:** Reject ungranted compile-time filesystem, environment, network,
   shell and other effects rather than inheriting seed authority.
5. **SH-05.5:** Diagnose invalid expansion shape, unsupported macros, cycles or
   exhausted bounds, with the macro call and producer attributable.
6. **SH-05.6:** Compare real compiler macro expansions and rejected diagnostic
   behavior with the accepted prior-stage subset.

Checks: `sh05_macro_adapter_test.clj` and affected C4 source-coverage cases.
Integration: syntax -> expansion -> resolution and generated Core.
Stop: required bootstrap macros, not arbitrary plugin/macro ecosystems.

## SH-06 - Namespace and binding resolution

Contracts: L3, L8, C5, BOOT3. Owner:
`bootstrap/gravity/src/gravity/resolution.gravity` and the declared C5 module.

1. **SH-06.1:** Build the compiler namespace/module graph with imports,
   aliases, exports, visibility and declared dependency-cycle policy.
2. **SH-06.2:** Resolve lexical, top-level, qualified and required core bindings
   with stable identities and correct shadowing/availability.
3. **SH-06.3:** Resolve references, operators, macro-generated names and
   pattern-bound names in their actual scopes and phases.
4. **SH-06.4:** Reject unresolved, private, ambiguous, duplicate and illegal
   cyclic or phase-crossing references with stable binding diagnostics.
5. **SH-06.5:** Preserve exact source occurrences, spelling, selected binding
   identity and resolution order into the Core input contract.
6. **SH-06.6:** Execute resolution over the complete declared compiler source
   set; coherent forged bindings and changed module inputs fail validation.

Checks: `sh06_resolution_adapter_test.clj` and affected SH-06 -> SH-07 tests.
Integration: expanded syntax/module graph -> binding graph -> Core lowering.
Stop: resolved names, not type inference, runtime var permissions or execution.

## SH-07 - Core-form semantics and lowering

Use the [18-task SH-07 completion checklist](self-hosting-sh07-completion-plan.md)
for the ordered implementation, evidence and migration work behind the six
requirements below, including the explicit candidate-scope decision. The
checklist does not change these requirements or confer completion credit.

Contracts: L2, L7, L9, C6; preserve C3/C5 inputs. Primary semantic owner:
`bootstrap/gravity/src/gravity/checked_core.gravity`. The C6 compatibility
facade and request projection do not own independent language semantics.

1. **SH-07.1 - Values, quotation and references:** Lower compiler-required
   literals/constructors, quoted DATA, lexical/top-level references, `def`
   and `var`, preserving binding identity and source mapping. Accept
   quoted-symbol data without execution; reject malformed quotation,
   unrecognized forms and invalid reference/definition targets.
2. **SH-07.2 - Sequencing and bindings:** Lower `if`, `do` and `let` with
   L2 truthiness, exactly one selected branch, sequential initializer scope
   and left-to-right evaluation. Include empty/terminal bodies where legal,
   shadowing and nested control. Reject malformed arity/bindings and lost or
   reordered evaluated source occurrences.
3. **SH-07.3 - Functions and recursion:** Lower `fn`, calls, `loop` and
   `recur`, including compiler-required closures, function values and
   recursive call shapes. Preserve operator/argument evaluation, target,
   tail position and arity. Reject invalid call/Core shapes, absent or
   incompatible recur targets and incorrect recur arity. Type compatibility
   is checked by SH-08, not guessed by C6.
4. **SH-07.4 - Mutation and errors:** Lower `set!`, `try` and `throw` with
   explicit location/RHS order, handler scope/order and normal/error exits;
   preserve required cleanup semantics. Reject invalid mutable-location
   shapes, malformed handlers and illegal control placement. Carry type,
   mutation/error effect, capability and profile obligations to their owners;
   lowering success does not grant runtime mutation or exceptions.
5. **SH-07.5 - Match decision structure:** Lower all pattern variants required
   by the compiler closure, including scalar/quoted-symbol literals,
   wildcard/binding/default, recursive collection/record/union shapes and
   guards when used. Preserve scrutinee-once, clause order, branch scope,
   bindings and structural-failure/guard/body control. Reject duplicate
   pattern bindings, malformed constructors/guards and unsupported patterns.
   L7 requires normally returning guards to be Bool, evaluated once after
   structural success; SH-08 owns that type check and narrowing/totality,
   SH-09 guard effects, SH-10 moves/borrows, and SH-13 runtime control.
6. **SH-07.6 - Verified canonical Core:** Every required executable form
   lowers to L2 Core, a declared primitive or an owned domain boundary.
   Verify child/binding references, source/generated origins, preserved
   declarations and evaluation records; emit maps/traces and C6 diagnostics.
   Exercise handwritten/macro-generated equivalents and real SH-06 -> C6
   composition; missing/forged/reordered products must fail closed.

Checks: existing `sh07_control_flow_test.clj`, `sh07_lexical_binding_test.clj`,
`sh07_var_reference_test.clj`, `sh07_references_calls_test.clj`,
`sh07_loop_recur_test.clj`, `sh07_mutation_test.clj`,
`sh07_try_catch_test.clj`, `sh07_throw_test.clj`,
`sh07_match_lowering_test.clj`, `sh07_vector_pattern_test.clj`, and the owning
conformance/source-coverage tests. Select actual exact kernels per behavior;
use authenticated composition only for the affected boundary. These names
are not an assertion that all six requirements are currently satisfied.

Integration: SH-06 -> C6 -> C7; when changing errors, guards, mutation or
control, exercise affected C8/emitter/runtime consumers separately. Artifact
construction cost is not a new language requirement. A direct kernel result
is useful progress but is not authoritative source-composition proof.

Stop: verified Core for the required compiler forms plus scoped migration
evidence. SH-07 does not require completed C7-C10, all MIR/runtime behavior,
every L7 pattern in all profiles, an optimized backend or seedless release.
Downstream-pending facts must remain explicit, never represented as checked.

## SH-08 - Bootstrap types

Contracts: L5, L7-L10, C7. Owner:
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.

1. **SH-08.1:** Assign types or structured diagnostics to every compiler Core
   node, including primitive literals, quoted/meta values and collections.
2. **SH-08.2:** Check function parameters/results, direct/indirect calls,
   closures, recursive functions and recur parameter compatibility.
3. **SH-08.3:** Check branches/joins, error/handler paths and required
   record/union/constructor/field operations with consistent environments.
4. **SH-08.4:** Produce pattern narrowing and required closed-match coverage;
   reject incompatible patterns, missing required cases and non-Bool or
   unestablished guard results, preserving non-returning paths.
5. **SH-08.5:** Record required generic/protocol instantiations, explicit
   dynamic/cast boundaries, layout/resource facts and latent function effects;
   reject unsafe implicit conversions or missing constrained-profile facts.
6. **SH-08.6:** Verify complete type/constraint coverage on actual compiler
   Core and its ill-typed variants; unresolved constraints cannot become
   silent Dynamic fallback or backend-only errors.

Checks: `sh08_primitive_type_test.clj`, `sh08_function_call_type_test.clj`,
`sh08_loop_recur_type_test.clj`, `sh08_recursive_function_type_test.clj`,
`sh08_records_unions_type_test.clj`, `sh08_record_access_match_typing_test.clj`,
and `sh08_authenticated_type_integration_test.clj`.
Integration: C6 -> typed Core -> C8/C9; changed types reach affected MIR cases.
Stop: compiler-required typing, not every optional type-system extension.

## SH-09 - Effects, capabilities and profiles

Contracts: L6, L12, L15, C8, P1/P3/P13 and release-profile contract. Owner:
`bootstrap/gravity/src/gravity/effects.gravity` and the declared C8 engine.

1. **SH-09.1:** Compute direct, latent, transitive, thrown-error and residual
   effects for all compiler constructs, including guards and mutation.
2. **SH-09.2:** Check function/namespace declarations against the conservative
   inferred effect set and preserve required evaluation/resource ordering.
3. **SH-09.3:** Validate concrete grant scope, principal, phase and provider;
   effect presence alone never supplies authority.
4. **SH-09.4:** Enforce compile-time profile/target/runtime intersection;
   reject forbidden allocation, dispatch or services before backend emission.
5. **SH-09.5:** Separate build and runtime effects; reject ambient filesystem,
   environment, network, shell or registry access and retain required replay
   obligations rather than silently performing effects during compilation.
6. **SH-09.6:** Emit verified effect/capability/profile records consumed by
   ownership, safety and MIR; malformed, missing or stale legality facts and
   illegal real programs receive structured diagnostics.

Checks: `sh09_body_effect_test.clj`, `sh09_error_effect_integration_test.clj`,
`sh09_effect_legality_test.clj`, `sh09_authenticated_effect_integration_test.clj`.
Integration: typed Core -> legality -> C9/C10/MIR and provider requests.
Stop: compiler/release-path legality, not all distributed/AI runtime services.

## SH-10 - Ownership, lifetimes and initialization

Contracts: L10, SAFE2-SAFE5, C9. Owner:
`bootstrap/gravity/src/gravity/compiler/c9_ownership_checker_engine.gravity`.

1. **SH-10.1:** Represent persistent sharing, owned mutable values, copies,
   moves and borrows required by the compiler without hidden copying.
2. **SH-10.2:** Enforce immutable-sharing/exclusive-mutation rules and reject
   overlapping mutable aliases or moving actively borrowed owners.
3. **SH-10.3:** Reject uninitialized reads, use after move/consume and invalid
   partial initialization along branch, loop and error paths.
4. **SH-10.4:** Track closure/return/global/provider escapes and required
   region/arena generations; reject references outliving their owners.
5. **SH-10.5:** Require exactly one valid terminal state or ownership transfer
   for each required linear resource on normal, error and cleanup paths;
   runtime checks are explicit and profile-legal, unsafe paths audited.
6. **SH-10.6:** Verify complete ownership/lifetime facts and carry them to
   safety/MIR; malformed or coherently substituted ownership graphs reject.

Checks: `sh10_ownership_transition_test.clj`,
`sh10_initialization_move_family_test.clj`,
`sh10_runtime_borrow_conflict_family_test.clj`,
`sh10_authenticated_ownership_integration_test.clj`.
Integration: typed/effected Core -> ownership -> safety/MIR cleanup.
Stop: required memory regimes, not universal GC/arena/concurrency support.

## SH-11 - Safety outcomes

Contracts: D8, SAFE1/SAFE6/SAFE15/SAFE16 and applicable specialized safety
contracts, C10. Owner:
`bootstrap/gravity/src/gravity/compiler/c10_safety_analysis_pipeline.gravity`.

1. **SH-11.1:** Inventory every dangerous operation in the compiler subset;
   exactly one outcome is proven-safe, runtime-checked, rejected or unsafe-island.
2. **SH-11.2:** Classify bounds, initialization, lifetime, resource and
   capability operations using actual upstream facts and precise conditions.
3. **SH-11.3:** Check numeric overflow, division, shifts and conversions under
   explicit numeric modes; no target UB or silent wrapping/fast-math fallback.
4. **SH-11.4:** Emit residual checks with source, guard dominance and defined
   profile-legal failure behavior; missing proof retains the check or rejects.
5. **SH-11.5:** Validate unsafe-island owner, invariants, authority, review,
   safe wrapper and generated origins; reject incomplete/forbidden islands.
6. **SH-11.6:** Verify safety fact completeness and proof/check/operation joins;
   altered evidence fails closed and valid outcomes survive into MIR.

Checks: `sh11_numeric_safety_test.clj`, `sh11_division_bounds_safety_test.clj`,
`sh11_overflow_cast_safety_test.clj`, `sh11_runtime_checked_verifier_test.clj`,
`sh11_authenticated_safety_integration_test.clj`.
Integration: C8/C9 -> C10 -> checked Core/MIR -> actual check failure paths.
Stop: safety for the declared subset; no performance claim follows.

## SH-12 - MIR construction and verification

Contracts: C11, C12, C18. Owner:
`bootstrap/gravity/src/gravity/compiler/c11_mir_specification.gravity`.

1. **SH-12.1:** Construct modules, globals, functions, blocks, values and
   operations from actual checked Core for every required operation family.
2. **SH-12.2:** Construct valid terminators, dominance, block arguments/joins
   and data-flow edges; reject missing definitions and invalid CFG structure.
3. **SH-12.3:** Preserve type, effect/order, capability, profile, source and
   generated-origin tables with complete operation references.
4. **SH-12.4:** Attach ownership, safety outcomes, proof/unsafe references and
   residual checks to their exact operations; checks guard the intended work.
5. **SH-12.5:** Represent required domain boundaries with semantic anchors,
   not opaque target-specific instructions in generic MIR.
6. **SH-12.6:** Run the MIR verifier before optimization/lowering, test each
   malformed/fact-loss family and compare Core-to-MIR semantics on fixtures.

Checks: `sh12_c10_mir_adapter_test.clj`, `sh12_branch_join_mir_test.clj`,
`sh12_runtime_checked_mir_test.clj`, `sh12_authenticated_mir_integration_test.clj`.
Integration: checked Core -> MIR -> function/data consumers and optimization.
Stop: structurally/semantically verified construction. SH-13/14 own complete
execution of control/data families; no all-domain/all-target claim is implied.

## SH-13 - Executable functions and control flow

Contracts: L2/L7/L9, C11/C12, C18. Owners: C11 function/CFG lowering,
`bootstrap/gravity/p15_s23/emitter.gravity` and its declared runtime consumer;
do not duplicate Core semantics in a backend.

1. **SH-13.1:** Execute multiple typed functions with correct argument/result
   transfer, direct calls and the compiler's required indirect/closure calls.
2. **SH-13.2:** Execute recursion and tail recur with correct state updates;
   reject invalid arity, target and control transfer.
3. **SH-13.3:** Execute branches, loops and joins with observable required
   order and only the selected branch/body work performed.
4. **SH-13.4:** Execute match decision paths, structural failure and guard
   failure, preserving single evaluation and branch binding scope.
5. **SH-13.5:** Execute return/error/handler/cleanup exits without losing
   effects, ownership, checks or source-attributed failures.
6. **SH-13.6:** Differentially compare Core intent and verified MIR execution;
   altered CFG, operands, call targets and domain anchors reject.

Checks: `sh13_control_flow_test.clj`, `sh13_c11_domain_evidence_adapter_test.clj`
and affected emitter/runtime tests in `sh01_stage2_runtime_iteration_test.clj`.
Integration: C11 -> executable control -> runtime; SH-14 supplies required data.
Stop: required function/control execution, not a full native release backend.

## SH-14 - Data, layout and allocation

Contracts: L2/L5/L10, C11/C14, R5, STD2-STD4/STD6. Owners: C11 data
operations, selected backend layout/emission and runtime data providers.

1. **SH-14.1:** Construct/read strings, symbols, keywords and numeric/scalar
   values with the compiler's required equality, encoding and identity rules.
2. **SH-14.2:** Construct/access persistent lists, vectors, maps and sets;
   preserve required iteration/evaluation semantics, not host container accidents.
3. **SH-14.3:** Construct/access tuples, records and tagged variants with
   explicit tags, field order, widths/alignment and checked projections.
4. **SH-14.4:** Execute bounded mutable storage and allocation/deallocation or
   declared no-allocation alternatives with explicit ownership/lifetime facts.
5. **SH-14.5:** Reject invalid indexes, tags, encodings, layouts, allocations
   and unsupported data regimes with the specified checked failure behavior.
6. **SH-14.6:** Verify data/layout facts survive MIR -> target/runtime;
   representative nested compiler syntax/IR data round-trips without loss.

Checks: `sh14_data_layout_test.clj`, `sh14_authenticated_layout_test.clj`
and affected runtime/collection execution tests.
Integration: MIR data operations -> ABI/layout -> runtime/stdlib.
Stop: all compiler-required data representations, not every library type.

## SH-15 - Compiler diagnostics

Contracts: C15, TEST2, BOOT3. Owners:
`bootstrap/gravity/src/gravity/compiler/c15_compiler_diagnostics.gravity` and
`bootstrap/gravity/src/gravity/bootstrap/diagnostics.gravity` at their declared
schema/construction boundaries; stage passes retain ownership of error meaning.

1. **SH-15.1:** Construct every required bootstrap rejection family as Gravity
   diagnostic DATA, not host exception text or a generic catch-all classifier.
2. **SH-15.2:** Include stable rule/severity/stage, actual primary and related
   spans, syntax/artifact identity and generated-origin chain.
3. **SH-15.3:** Include profile/target, typed relevant facts and actionable
   remediation categories; deterministic ordering/counts match the stream.
4. **SH-15.4:** Render CLI and required bootstrap tool output from the same
   structured records; wording changes do not silently change semantic IDs.
5. **SH-15.5:** Redact secrets/private values without erasing actionable
   provenance, and structure genuine internal errors/minimized reproducers.
6. **SH-15.6:** Compare actual rejection outputs across migrated stages and
   test malformed diagnostics, lost origins and ID/order/redaction changes.

Checks: `sh15_diagnostic_boundary_test.clj` and owning stage diagnostic goldens.
Integration: every migrated stage -> diagnostic DATA -> driver/renderers.
Stop: bootstrap diagnostic families, not every IDE/localization feature.

## SH-16 - Required MIR optimization

Contracts: D6, C13/C18, PERF10. Owner:
`bootstrap/gravity/src/gravity/compiler/c13_mir_optimization_passes.gravity`.

1. **SH-16.1:** Enumerate the finite required bootstrap pass pipeline with
   inputs/outputs, versions, analyses and deterministic order.
2. **SH-16.2:** Execute each required transformation on actual MIR, including
   decision records when a pass makes no change; a registry alone is not a pass.
3. **SH-16.3:** Preserve facts or declare invalidation and regenerate analyses
   before their consumers; relevant stale analysis/cache inputs reject.
4. **SH-16.4:** Preserve effect order, ownership, origins and residual checks;
   erase checks only with surviving/regenerated proof and valid dominance.
5. **SH-16.5:** Verify changed MIR and perform C18-appropriate translation
   validation/differential checks, including unsound-rewrite counterexamples.
6. **SH-16.6:** Feed verified optimized MIR to SH-17 with valid decision,
   invalidation and residual-cost records; no proofless target assumptions.

Checks: `sh16_mir_optimization_test.clj`,
`sh16_c12_domain_evidence_boundary_test.clj` and C13 negative rewrite cases.
Integration: MIR/domain boundary -> passes -> verifier -> lowering.
Stop: explicit required pass set. Optional optimization quality/speedups and
all domain optimizers are later work; do not substitute a no-op for a required pass.

## SH-17 - Target-lowering boundary

Contracts: C14, B1, P13, C18. Owners:
`bootstrap/gravity/src/gravity/compiler/c14_target_lowering_architecture.gravity`
and `bootstrap/gravity/src/gravity/backend/b1_backend_interface_specification.gravity`.

1. **SH-17.1:** Accept only the actual verified MIR/domain artifact and its
   current checked facts; stale or substituted inputs fail before emission.
2. **SH-17.2:** Select explicit profile, target triple/features, backend and
   artifact kinds; unsupported combinations reject, not silently fallback.
3. **SH-17.3:** Validate ABI, layouts, runtime/provider availability and
   capability scope; no hidden services or newly granted authority.
4. **SH-17.4:** Map proof-backed numeric/alias/alignment assumptions to target
   metadata; reject unjustified no-overflow/no-alias/fast-math assumptions.
5. **SH-17.5:** Preserve origins, effects, safety/proof and dependency records
   in requests/results, with stable unsupported-feature diagnostics.
6. **SH-17.6:** Exercise one genuine producer -> C14 -> B1 -> backend handoff
   and coherent carrier/policy mutations through the reusable interface.

Checks: `sh17_target_lowering_hardening_test.clj`,
`sh17_c13_optimized_mir_carrier_test.clj`,
`sh17_c13_evidence_rejection_test.clj` and selected backend continuity tests.
Integration: optimized MIR -> C14/B1 -> selected emitter.
Stop: valid interface and affected composition, not every backend implementation.

## SH-18 - One native target

Contracts: B2 or B3, B14, C14, native profile/runtime contract. Owners: the
selected C/LLVM backend, existing native emitter and toolchain harness.

1. **SH-18.1:** Compile a real source program through checked Core/MIR and
   SH-17, not prewritten target text presented as compiler emission.
2. **SH-18.2:** Cover typed arguments/results, arithmetic, multiple functions,
   required collections and explicit stdout in that executable program set.
3. **SH-18.3:** Emit and independently compile/verify/link for the exact target
   ABI/toolchain, retaining compiler/runtime/library identities and artifacts.
4. **SH-18.4:** Execute the binary and compare expected output, exit status
   and error/check behavior with Gravity semantics.
5. **SH-18.5:** Reject unsupported ops, ABI/provider mismatch, illegal profile
   use and malformed source; preserve tool failures without false success.
6. **SH-18.6:** Retain source-to-binary provenance/maps and independent
   execution evidence, with bounded resource/cleanup results and actual omissions.

Checks: `sh18_native_c_emitter_test.clj`, `sh18_native_toolchain_harness_test.clj`.
Integration: actual source -> backend -> external toolchain -> native execution.
Stop: one exact native target/program subset, not compiler self-hosting or
all-target support. A standalone harness success alone does not satisfy .1.

## SH-19 - Self-hosting runtime subset

Contracts: R1/R3/R5/R11, L9/L10/L15, SAFE6. Owners:
`bootstrap/gravity/src/gravity/runtime.gravity`, native runtime driver and
declared providers; temporary C providers remain explicit trusted components.

1. **SH-19.1:** Start the compiled compiler/runtime with defined argv, inputs,
   initialization order, service manifest and process-exit behavior.
2. **SH-19.2:** Provide required strings/bytes, bounded allocation and resource
   lifetime behavior or explicit no-allocation alternatives outside Clojure.
3. **SH-19.3:** Perform compiler-required file reads and stdout/stderr writes
   under concrete grants, with checked encoding/path/size/error behavior.
4. **SH-19.4:** Report panic, typed errors and safety-check failures with
   defined exits, source context and resource cleanup.
5. **SH-19.5:** Reject ambient authority, unavailable services, wrong provider
   scope, invalid handles and unsupported runtime policies.
6. **SH-19.6:** Execute actual provider operations on the selected native path,
   account residual trusted code/audits and exclude Clojure from that runtime
   boundary; validating provider-action DATA alone is insufficient.

Checks: `sh19_minimal_runtime_test.clj` and existing native runtime provider
contract/packet-binding checks described in development verification.
Integration: generated calls -> provider actions -> real OS/runtime outcomes.
Stop: bootstrap runtime services, not universal managed/distributed/AI runtimes.

## SH-20 - Artifact emission and provenance

Contracts: B13, PKG3/PKG7/PKG10/PKG12, BOOT8. Owner:
`bootstrap/gravity/src/gravity/backend/w5_b13_artifact_emitter.gravity` and
actual artifact-writing integration, not descriptor-only emission.

1. **SH-20.1:** Produce real required objects/executables and associated
   artifacts with kinds, schemas, content identities and compiler identity.
2. **SH-20.2:** Emit source/generated-origin maps, profile/target/ABI/runtime
   manifests, effects/capabilities and safety/proof summaries.
3. **SH-20.3:** Emit a closed dependency graph binding actual source, compiler,
   inputs and tool outputs, with canonical path-neutral semantic identities.
4. **SH-20.4:** Retain actual-path provenance separately and make manifests
   and maps consumable/verifiable by independent tools.
5. **SH-20.5:** Enforce development/release distinctions; reject missing,
   stale, malformed or substituted content and incomplete release evidence.
6. **SH-20.6:** Validate canonical provenance/SBOM/signature inputs under the
   applicable release policy; no signed descriptor substitutes for missing bytes.

Checks: `sh20_artifact_emission_test.clj` plus selected external artifact/map
consumption and actual file-writing checks. Descriptor tests alone cannot close .1.
Integration: backend/runtime outputs -> package/build/release consumers.
Stop: release-capable emission, not an assertion that a release is already approved.

## SH-21 - Legal meta compiler

Contracts: P3, L12/L15, BOOT3/BOOT4, C17/C18. Owner:
`bootstrap/gravity/src/gravity/self_hosting/meta_compiler_legality.gravity`
plus every compiler module's real declarations and executable implementation.

1. **SH-21.1:** Enumerate every compiler module and its input/output schemas,
   effects, capabilities, preserved facts and required generated code.
2. **SH-21.2:** Compile each module under the declared meta/bootstrap contract
   through actual type/effect/profile/ownership/safety checks.
3. **SH-21.3:** Reject ambient filesystem, environment, network, shell,
   registry and undeclared host-service dependencies in compiler execution.
4. **SH-21.4:** Re-enter generated compiler code through the normal pipeline,
   preserving producer origins and rejecting generated illegal behavior.
5. **SH-21.5:** Exercise actual compiler work under explicit grants; test
   missing, narrowed, wrong-phase and wrong-provider authority denials.
6. **SH-21.6:** Verify module diagnostics, pass facts and unsafe audits against
   the complete compiler closure; validating declared policy DATA is not enough.

Checks: `sh21_meta_compiler_legality_test.clj`, applicable compiler module
source-coverage cases and real meta execution/denial cases.
Integration: whole compiler modules -> legality -> build/driver.
Stop: compiler meta legality, not every language profile or arbitrary plugin.

## SH-22 - Bootstrap standard library

Contracts: L13, STD1-STD6/STD8/STD15, TEST7. Owner:
`bootstrap/gravity/src/gravity/stdlib/self_hosting_core.gravity` and its
declared collection/text/numeric/error/IO/meta providers.

1. **SH-22.1:** Enumerate all library APIs transitively required by the
   compiler/runtime/build closure, with signatures and explicit unsupported APIs.
2. **SH-22.2:** Execute required collections, iteration, updates and lookup
   operations with correct equality, bounds and persistent/mutable semantics.
3. **SH-22.3:** Execute text/UTF8, symbols/keywords and checked numeric helpers,
   rejecting malformed data, overflow and invalid conversions.
4. **SH-22.4:** Execute required error, artifact-data, canonical serialization
   and compiler syntax/IR/meta helpers with preserved identity/provenance.
5. **SH-22.5:** Enforce effects, grants, safe wrapper and resource contracts on
   actual IO/provider operations; denied inputs cannot become ambient actions.
6. **SH-22.6:** Compile and run compiler clients against these Gravity APIs and
   paired conformance/rejection cases; a proposed provider request alone does
   not prove the external operation occurred.

Checks: `sh22_standard_library_test.clj`,
`sh22_authenticated_library_integration_test.clj` and required real client tests.
Integration: compiler calls -> library -> native runtime/provider.
Stop: complete bootstrap API closure, not all 20 standard-library documents.

## SH-23 - Hermetic package and build subset

Contracts: PKG1/PKG2/PKG3/PKG5-PKG7/PKG10, L12, BOOT4. Owner:
`bootstrap/gravity/src/gravity/self_hosting/hermetic_package_build.gravity`
and actual build action execution outside Clojure orchestration.

1. **SH-23.1:** Read and validate the fixed compiler project/lock/manifest
   inputs; reject unlocked, missing, inconsistent or substituted dependencies.
2. **SH-23.2:** Resolve the complete required bootstrap graph deterministically,
   enforce dependency order and diagnose cycles/missing edges.
3. **SH-23.3:** Construct and execute explicit compiler/tool/provider build
   actions with controlled inputs, outputs, effects and capability scopes.
4. **SH-23.4:** Reject ambient filesystem/environment/network/shell resolution
   and undeclared fetch/build effects; no host registry fallback.
5. **SH-23.5:** Bind actual output bytes/manifests to source, lock, build recipe
   and tool identities, with conservative invalidation on changed inputs.
6. **SH-23.6:** Build real compiler components through the Gravity-owned build
   path without Clojure orchestration; action plans/record verification alone
   do not close this requirement.

Checks: `sh23_hermetic_package_build_test.clj` and actual controlled build/denial
tests once execution is wired. The existing DATA checks are development evidence.
Integration: meta/library/runtime -> build actions -> SH-20 artifacts/SH-24 driver.
Stop: fixed bootstrap graph, not a public package registry or all build plugins.

## SH-24 - Compiler driver and CLI

Contracts: T1, C1/C15, PKG2, BOOT3, Phase 18 command boundary. Owner: actual
Gravity driver/CLI; existing driver seeds are in
`bootstrap/gravity/p15_s23/compiler.gravity` with emitter/runtime helpers.

1. **SH-24.1:** Accept source/project/target/output requests through a Gravity
   executable with explicit command parsing and supported-option diagnostics.
2. **SH-24.2:** Load only declared source/dependency inputs under grants and
   orchestrate the canonical passes in dependency order.
3. **SH-24.3:** Implement real `check` and `compile`, required bootstrap
   verification commands, target selection and artifact writing.
4. **SH-24.4:** Render structured diagnostics and return defined status codes
   on rejected source, failed passes/tools, invalid requests and output errors.
5. **SH-24.5:** Preserve co-canonical `.gravity`/`.qst` behavior, actual paths,
   compiler identity, provider policy and output provenance end to end.
6. **SH-24.6:** Execute the commands using the selected native/runtime/build
   path; a Clojure wrapper or supplied command-result record is not the driver.

Checks: existing P15 driver and Phase 18 command tests, plus actual seedless
driver command cases required above. There is no dedicated SH-24 test file in
the baseline; add focused driver cases to its owning command tests, not a runner.
Integration: CLI -> source/pass/build orchestration -> diagnostics/artifacts.
Stop: compiler/bootstrap command subset, not a complete REPL/LSP/IDE.

## SH-25 - Complete compiler component build

Contracts: BOOT3-BOOT5, C1-C18, B1/B13, TEST13. Owner: compiler inventory and
actual Gravity component builder; existing catalog seed is
`bootstrap/gravity/p15_s23/compiler.gravity`.

1. **SH-25.1:** Freeze the complete compiler, generated-code, runtime, library
   and package/build component closure with actual source identities and owners.
2. **SH-25.2:** Cover reader, syntax, macros, resolution, Core, types, effects,
   ownership, safety, MIR, required passes/domain anchors and diagnostics.
3. **SH-25.3:** Cover selected lowering/backend, emission, driver, runtime,
   required library and package/build components, including transitive helpers.
4. **SH-25.4:** Actually compile every component from Gravity sources with the
   Gravity compiler, recording inputs/outputs and accepted dependency ordering.
5. **SH-25.5:** Reject omitted/replaced components, stale graphs, illegal meta
   modules, fake output inventories and missing producer-to-consumer links.
6. **SH-25.6:** Run the resulting components on real compiler work and their
   required conformance cases; update BOOT5/TCB with exact remaining seed roles.

Checks: `sh25_component_build_test.clj` and actual complete component build.
Existing inventory/carrier tests do not prove compilation of the entire closure.
Integration: SH-21/22/24 and every upstream component -> executable compiler.
Stop: complete component build; SH-26 owns the actual next-stage rebuild.

## SH-26 - Actual next-stage rebuild

Contracts: BOOT1/BOOT3/BOOT5/BOOT7/BOOT8, TEST13. Owner: the real stage rebuild
executor; `w5_stage_rebuild_orchestrator.gravity` currently validates supplied
records and must not be mistaken for a process-invoking rebuild implementation.

1. **SH-26.1:** Identify the prior Gravity compiler artifact and the complete
   next-stage source/lock/build/environment/runtime/library inputs.
2. **SH-26.2:** Invoke that prior compiler to build every next-stage component
   from the SH-25 closure, retaining actual tool/process outcomes and outputs.
3. **SH-26.3:** Exclude Clojure execution from candidate compilation, runtime
   and package/build orchestration; retained audit/recovery seeds are separate.
4. **SH-26.4:** Link/package and execute the next-stage compiler on accepted
   compiler/application inputs and rejected semantic/legality inputs.
5. **SH-26.5:** Reject missing outputs, substituted builders/components,
   unauthorized actions and failed or incomplete rebuilds without laundering
   process plans or supplied records into execution evidence.
6. **SH-26.6:** Produce actual rebuild logs, stage compiler artifacts,
   conformance and lineage inputs sufficient for SH-27 comparison.

Checks: `sh26_stage_rebuild_test.clj` for record/boundary behavior, plus the
actual complete rebuild and execution required by .2-.4. A test with a rebuild
name that only validates DATA does not close this milestone.
Integration: prior compiler -> full next compiler -> real compiler execution.
Stop: one successful complete next-stage rebuild, not yet fixed-point equivalence.

## SH-27 - Equivalence and reproducibility

Contracts: BOOT6/BOOT7, TEST10/TEST13, PKG7. Owner:
`bootstrap/gravity/src/gravity/self_hosting/w5_stage_equivalence_verifier.gravity`
and the independent actual rebuild/comparison execution.

1. **SH-27.1:** Perform two clean controlled rebuilds of the complete accepted
   compiler closure; identify stage N and N+1 artifacts, not fixture aliases.
2. **SH-27.2:** Compare actual compiler/output artifacts and canonical manifests
   under declared byte/content identity modes.
3. **SH-27.3:** Compare stable diagnostic codes/spans/facts and conformance
   results for accepted and rejected programs at both stages.
4. **SH-27.4:** Compare runtime outputs and required IR modulo nonsemantic IDs;
   declared bootstrap resource/performance bounds remain enforced.
5. **SH-27.5:** Review each permitted delta against its policy; missing output,
   unexplained semantic drift or an overbroad normalization fails comparison.
6. **SH-27.6:** Link reproducible recipes, actual rebuilds, compiler identities,
   comparisons and accepted deltas into provenance consumed by SH-28.

Checks: `sh27_stage_equivalence_test.clj` and actual clean rebuild/differential
conformance commands for the selected stable candidate.
Integration: actual N/N+1 builds -> comparison -> trust/provenance gate.
Stop: equivalent/reproducible declared compiler, not diverse trust or public release.

## SH-28 - Trust, provenance and seed boundary

Contracts: BOOT6/BOOT8, PKG7/PKG10/PKG12, SAFE6, TEST13. Owner:
`bootstrap/gravity/src/gravity/self_hosting/w5_trust_provenance_verifier.gravity`
and the actual independent rebuild/audit/signing inputs.

1. **SH-28.1:** Supply diverse or independently anchored actual rebuild
   evidence with identified tools and a reviewed method/trust limitation.
2. **SH-28.2:** Traverse real compiler lineage to seed inputs; reject gaps,
   undeclared cycles, substituted anchors and self-asserted builder identity.
3. **SH-28.3:** Bind source/dependency/lock/recipe/environment identities to
   SH-27 conformance, equivalence and actual rebuilt content.
4. **SH-28.4:** Canonicalize and verify required provenance, SBOM and signatures;
   revoked builders, compilers, dependencies or signatures block release.
5. **SH-28.5:** Complete unsafe audits and a measured TCB delta, with all
   residual OS/toolchain/runtime/provider trust explicit; seedless is not trustless.
6. **SH-28.6:** Establish the Clojure seed outside the candidate release
   compiler/runtime/build path using actual path evidence, not boolean fields
   or a descriptor-only trust report.

Checks: `sh28_trust_provenance_test.clj`, actual independent rebuild, lineage,
revocation, signature and unsafe-audit checks for the stable candidate.
Integration: actual rebuild/equivalence -> signed/auditable release candidate.
Stop: accepted trust and seed-boundary evidence; SH-29 owns public delivery.

## SH-29 - Public seedless release

Contracts: D2/D9, BOOT1-BOOT8, TEST13, PKG7/PKG10/PKG12, T1, applicable
governance and Phase 18 release gates. Owner: public release binary and its
actual compiler/runtime/build/package closure, separate from `gravity-bootstrap`.

1. **SH-29.1:** Deliver a real public `gravity` executable produced by the
   accepted self-hosted path; bootstrap-hosted packaging/fallback cannot stand in.
2. **SH-29.2:** Through that executable, build/check the complete compiler,
   required runtime/library/package/build components and release executable
   without invoking Clojure inside the claimed release boundary.
3. **SH-29.3:** Pass declared full-bootstrap conformance and Phase 18 public
   command/application cases on both source extensions, including a nontrivial
   application and stable feature-specific rejected diagnostics.
4. **SH-29.4:** Verify actual delivered content against SH-27/28 reproducibility,
   equivalence, provenance, SBOM, signatures, revocation and TCB evidence.
5. **SH-29.5:** Demonstrate fail-closed public verification on missing, changed,
   untrusted or inconsistent release components; publish exact support/limits
   and retain an explicitly separate bootstrap recovery path if distributed.
6. **SH-29.6:** Only after the complete gate passes, record
   `:full-language-compiler-self-hosted? true`, `:clojure-seed-retired? true`,
   and `:clojure-seed-boundary? false` for that precise supported release.
   These artifact flags require real evidence and do not assert 240/240 coverage.

Checks: Phase 18's actual public `check`, `run`, `compile`, emitted-program
execution and `self-host verify` cases, plus the complete selected bootstrap
conformance/release bundle. Existing fail-closed wrappers are not passing
seedless-release evidence; there is no dedicated SH-29 test file in the baseline.
Integration: delivered public executable -> complete rebuild/verification ->
installed application execution and release trust validation.
Stop: the first honest seedless release on the declared target. Track remaining
language/profile/target support separately, without moving this finish line.

## Validation gaps and specific next actions

This is an executable-work roadmap, not a promise that all listed test oracles
already exist. In particular:

- SH-07 through SH-17: continue small owning-pass changes and map each required
  compiler construct to the relevant numbered requirement. Fill actual semantic
  and composition gaps; source-shape, carrier and proof-record tests alone do
  not close executable semantics. Start with the first missing SH-07 case,
  using the existing development guide and conformance fixtures.
- SH-18/19/20: complete source-to-native/provider/artifact-byte execution.
  Separate toolchain harness success and descriptor validation from emitted
  Gravity program behavior and actual provider calls.
- SH-21 through SH-25: establish the exact transitive required API/module/build
  closure and execute it. These existing tests need real client/build/driver
  integrations before they can support whole-component completion.
- SH-26 through SH-29: implement and run the real rebuild/public release path.
  Supplied-record validators remain useful negative/unit checks, but are not
  the missing process executions, independent rebuilds or delivered binary.
- Expensive product construction: before redesign, measure a representative
  cold producer, consumer replay/validation, serialization, namespace loading,
  scheduling and native tool work separately. If representation dominates,
  propose a bounded producer/consumer representation change with equivalence,
  tamper rejection, memory/time bounds and an affected migration decision.
  Do not add another runner/receipt system or waive semantic checks to hide cost.

Unresolved details within a required family must be settled in its owning
contract with accepted/rejected examples before that requirement is called
complete. For example, runtime map-pattern extra-key behavior belongs to L7
and its downstream execution checks, not an inference from Core record shape.
The Bool guard policy is already specified by current L7; do not reopen it as
an unspecified host-language truthiness choice.

## Outputs, progress accounting and conformance

The output of this planning change is one completion plan linked from the
backlog and documentation entrypoint. It adds no runtime lane, wrapper,
authority receipt, manifest schema, execution grant or milestone promotion.

For subsequent implementation, record exact supported behavior and selected
check outcomes against these IDs in existing development records. For actual
completion, link the accepted revision and evidence for all applicable IDs and
the shared gate, then update the existing backlog through its normal review.
Do not mark a six-part group complete because one representative fixture passed.

Report three different quantities: implemented requirements with actual checks,
integrated requirements with affected composition, and authoritatively completed
milestones out of 30. A requirement with partial subcase coverage remains
partial. Any ratio must name its revision and denominator; 180 checklist groups
are not an effort-weighted language percentage. SH-00/01 process completion,
test/review counts and document preparation do not become new executable
language progress. Preserve the backlog's separate TCB and completed-prefix
accounting and the full-language/target/release dimensions.

This document is structurally conforming when all 30 unique IDs occur in the
index and each has six numbered requirements, owners, checks, integration and
stopping boundaries; dependencies match the backlog; references resolve; and
no implementation or evidence state is promoted by this text alone.

After documentation edits run:

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
git diff --check
```

Passing those checks validates documentation structure/accounting, not any of
the 30 milestones' semantic or bootstrap completion gates.
