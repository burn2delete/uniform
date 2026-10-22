# SH-07 Completion Tasks

Status: finite implementation and acceptance checklist; SH-07 remains partial.
Assessment date: 2026-10-21.
Planning baseline: main `192dafcc86867ec6d067ee31d6b46fea4d1b0b92`,
tree `d4a01026dfe10d147c01e9f4bcd6bef8443c7165`.

Current desk reconciliation: 2026-10-22, committed main
`2a94fb7156f9e85ad085bc88f4d6f0b05b973e4c`, tree
`cd2380533645332e1b9473eab4e064bf686c2c20`. The historical planning observations
below retain their original binding. See the
[C01 working support map](#c01-working-support-map) and
[current input/evidence reconciliation](self-hosting-current-migration-cohort.md#c01c16-reconciliation-at-2a94fb71).
No task is closed by this documentation update.

## Purpose and authority

This document decomposes the existing six
[SH-07 requirements](self-hosting-milestone-completion-plan.md#sh-07---core-form-semantics-and-lowering)
into exactly 18 completion tasks, `SH07-C01` through `SH07-C18`. It is the
task-level expansion of that plan, not another language contract or evidence
system. The [backlog](self-hosting-slice-backlog.md) retains milestone status;
the [current cohort assessment](self-hosting-current-migration-cohort.md)
retains its proposed-module scope and historical evidence classifications.

The deliverable is verified canonical Core for a declared compiler subset,
constructed by Gravity's owning lowerer from authentic resolved input, with
accepted/rejected behavior, preserved facts, and independently accepted
stable-module migration evidence. Lowering constructs and verifies control
semantics; executing compiled application control through MIR is SH-13 work.

The task count is fixed. The exact compiler-wide semantic population is still
an open input to C01, because the actual complete compiler build recipe is not
yet implemented. This plan does not invent that recipe or equate a directory
inventory with its transitive closure. A bounded frontend module migration can
finish before the complete compiler exists, under BOOT5's explicit support
set; it must not be reported as complete compiler coverage.

## Governing contracts and dependencies

D1/D2/D3 define architecture, ordering and terminology; D6/D8/D9 define
performance, safety and evidence. L2 defines the 13 core forms; L7 and L9
define pattern and error semantics. C3/C5 supply source, origin and binding
facts; C6 owns lowering, verification and diagnostics. BOOT3/BOOT5/BOOT7/BOOT8
and TEST13 govern migration and stage comparison. Resolve IDs through the
[document index](document-sequence.md).

The only milestone dependency is SH-06. Current-cohort reconciliation of
SH-05.1, SH-05.6 and SH-06.6 is still open, and the current resolver remains
development-unadmitted in the existing
[evidence audit](self-hosting-sh00-sh06-evidence-audit.md). C16 carries this
dependency; it does not prevent ordinary development in C02-C15.

The primary semantic owner is
`bootstrap/gravity/src/gravity/checked_core.gravity`. Transport defects belong
to its existing Clojure adapter, particularly `00887`/`00890`; hosted
`gravity.c6-core-lowering` is a separate constructor. Existing ownership and
workstream reservations continue to apply. This checklist assigns task
responsibility, not permission to take another writer's files or resume a
held candidate.

## Candidate scope and change control

The first concrete candidate is the existing proposed `:meta/:jvm`
source-to-Core module endpoint. Its six executed owner sources are the
envelope, reader, syntax, macro, resolver and checked-Core sources in the
cohort assessment. Rule DATA in `p15_s23/compiler.gravity`, seed compilation,
generic plan execution, primitives, codecs/digests, source reads and artifact
binding are declared dependencies. The emitter is a selected freshness input;
it is not executed target lowering in this endpoint.

At the planning baseline, the first seven input rows of that assessment still
have identical bytes and SHA-256. The emitter has changed: it is 1,328,259
bytes with SHA-256
`1bb3568b48b26e994da2cc140ff5597812709786a1f9836ae7ff4af7044e63ed`.
These are desk observations, not renewed source/plan/proof bindings. C01 must
capture actual selected seed/dependency identities as well as owner bytes.

The first candidate is a useful module endpoint, not the entire eventual
compiler closure. For milestone-wide credit, C01 must state the supported
compiler subset and why it meets the backlog exit gate. If the evidence only
supports the six-source module endpoint, C18 records that bounded migration
and leaves SH-07 partial. A missing required form cannot be made optional to
obtain completion. SH-25 later checks that the actual compiler consumes the
accepted revisions or accepted successors.

Once C01 is recorded, fixes stay within these 18 tasks. A newly discovered
required form, primitive or import is assigned to its existing owning task;
optional features go to the full-language gap map. Changing the supported
population requires an explicit scope amendment naming the source/use, owning
contract, affected task, acceptance case and invalidated evidence. It does
not silently create C19 or redefine SH-07 as all of Gravity.

## Evidence status at the planning baseline

This is a desk reconciliation, not a fresh semantic test pass. "Located"
means implementation and checks exist; it does not mean the whole task is
complete. No completion checkboxes are checked by this documentation change.

| Population or obligation | Located support | Remaining closure |
| --- | --- | --- |
| All 13 L2 form names | `sh07-core-contract` lists `quote`, `if`, `do`, `let`, `fn`, `loop`, `recur`, `def`, `var`, `set!`, `try`, `throw`, `match`. | Required variants and composition must pass; listing a form is insufficient. C02-C11. |
| Values, control, bindings, vars, mutation, errors | B1-B9 tests and the current Gravity builder/verifiers. | Reconcile exact compiler uses, unsupported variants, real source composition and rejection coverage. C02-C09. |
| Calls and recursion | B47 function/call/recursion products; separate B48 direct local fixed-arity legality. | General callable/closure/cross-module and dispatch requirements need explicit disposition. B48 has pending nonlocal calls and is not complete call semantics. C05-C07. |
| Patterns | Scalar/quoted-symbol, irrefutable, recursive fixed-vector, keyword-map and root-map guard checks. | Required constructor/rest/nested-guard variants and duplicate binding rejection must be classified; later type/effect/ownership facts remain pending. C10. |
| Canonical products | Existing template/resolved/map verifiers, source mapping, origin, fragment and mutation checks. | Complete task-to-check matrix, declared pending boundaries and affected authentic joins. C11-C15. |
| Source analysis of current checked-Core | Existing audit records an interrupted exit-143 run reaching `:hosted-c6-before`. | INCOMPLETE; original cause unresolved. Current Gravity source-analysis/replay/provenance needs its own finite decision, not a mandatory legacy hosted-C6 redesign. C12. |
| Current upstream migration | Retained historical SH03-SH06 credit and later bounded development results. | Current SH05/SH06 groups and resolver admission remain open. C16. |
| Full compiler graph | Cohort assessment classifies current sources, targets, held work and required broader roles. | Actual executable recipe and semantic closure are not established by the 69+5 inventory. C01; full build ownership remains SH23-SH25. |

The lowerer's header still names some families as pending even where later
direct checks exist, including qualified references/mutation and map guards.
C11 reconciles these declarations from actual behavior. Neither the header
nor an old narrative is an independent completion oracle.

## Ordered task list

All 18 tasks start OPEN in this checklist. Existing accepted evidence should
close matching obligations without unnecessary reruns. "Reconcile" means
inspect the original implementation, cases, raw result and applicability;
implement or test only the demonstrated gap. It does not mean rewrite a
working form.

| Task | Requirement | Responsibility | Dependencies | Concrete output and exit gate |
| --- | --- | --- | --- | --- |
| SH07-C01 | All six | Coordinator with sh-core | none | Record the exact candidate source/compiler/dependency population, supported profile/target and required variant matrix. Separate the bounded six-owner migration from milestone-wide coverage; name each transitive primitive/import/generated-form dependency and residual seed boundary. Every required use has a C02-C15 owner and accepted/rejected case. No "current compiler" or directory count stands in for this selection. |
| SH07-C02 | SH-07.1 | sh-core | C01 | Close literals, vector/map/set constructors and quoted DATA: preserve literal kind/value, source occurrences and constructor evaluation order; quote never executes contents. Cover nil/Bool/integer/decimal/ratio/character/string/keyword, quoted symbols and nested data as selected by C01. Reject malformed quote, unsupported executable form and malformed constructor, with source diagnostic. |
| SH07-C03 | SH-07.1 | sh-core, sh-resolution for producer defects | C01 | Close lexical/top-level and qualified expression references, `def` and `var`: exact binding/namespace identity, shadowing, definition/value distinction and source map. Select alias/cross-module uses from C01, including real callee artifacts where required. Reject unresolved, private, invalid or substituted binding/var targets at the owning stage. |
| SH07-C04 | SH-07.2 | sh-core | C01 | Close `if`/`do`/`let`: false/nil truthiness, one conditional branch, ordered effects/children, sequential initializer scope, nested shadowing and legal body/arity policy. Destructuring required by C01 must lower with preserved origins or be implemented here; reject malformed bindings and reorder/duplicate/drop mutations. |
| SH07-C05 | SH-07.3 | sh-core | C03, C04 | Close `fn`, function values, captures and callable operators used by C01. Record parameter/body/capture bindings, latent body order and operator-before-arguments-once order. Cover lexical shadowing and nested functions. Reject invalid operator/function/capture structure; preserve later capture ownership and call-type obligations explicitly. |
| SH07-C06 | SH-07.3 | sh-core | C05 | Close arity and call graph: local/nonlocal direct calls, indirect/higher-order calls, cross-module edges and recursive components required by C01. Specify fixed/variadic/multi-arity dispatch separately; required dispatch cannot remain silently pending. Reject wrong arity and malformed/mismatched call evidence. Reconcile B48's separate result with the actual selected consumer rather than treating its source proof as integrated legality. |
| SH07-C07 | SH-07.3 | sh-core | C04, C05 | Close `loop`/`recur`: nearest compatible target, function boundary, tail placement, arity and simultaneous ordered next-binding values; retain binding IDs and recursion edges. Reject absent/wrong targets, nontail transfer, mismatched argument count and forged target/order. Carry type compatibility to SH08. |
| SH07-C08 | SH-07.4 | sh-core | C03, C04 | Close explicit `set!` location/RHS/result representation for C01's locations, including selected qualified vars. Preserve once-only RHS and nested/conditional write order, mutable target identity and declared obligations. Reject immutable/invalid target shape and substituted location facts at their owners; leave type/effect/capability/ownership/safety legality unasserted until checked. |
| SH07-C09 | SH-07.4 | sh-core | C03, C04, C07 | Close `throw`/`try`: error value once, protected expression order, typed handler scope/source order, normal/error exits and nested transfers. Resolve protected/handler sequencing and `finally`/cleanup only where required by C01; otherwise retain explicit unsupported rejection. Reject malformed handlers, invalid placement and lost/forged transfer/origin. Runtime selection and stack unwinding remain downstream. |
| SH07-C10 | SH-07.5 | sh-core | C03, C04, C07, C09 | Close selected scalar/quoted-symbol, wildcard/binding/default, recursive vector/map, record/union/constructor and guard patterns. Every named family receives required support or an explicit outside-subset rejection decision in C01; required cases cannot be excluded. Verify scrutinee once, branch-local unique bindings, clause/guard/body order and structural-failure skipping. Reject duplicate bindings and malformed constructor/guard. Preserve nonreturning guard transfer; SH08 establishes normally returning guard Bool and applicable match exhaustiveness/totality. |
| SH07-C11 | SH-07.6 | sh-core | C02-C10 | Reconcile the canonical Core schema and truthful supported/pending declarations. For every required source occurrence, verify L2/primitive/domain disposition, exact child/binding links, evaluation records, origin/source maps, desugaring traces and retained metadata/declarations. Reject residual unowned surface forms and forged/dropped facts. All six requirement groups map to exact selected checks. |
| SH07-C12 | SH-07.6 | Coordinator with sh-core/sh-resolution | C01; C11 before final selection | Close full-source analysis and actual Gravity-lowerer execution as distinct claims. Select the owning authenticated Gravity-Core route and exact subject/resource fit; legacy hosted-C6 representation redesign is not a prerequisite. A hosted-C6 result cannot substitute for Gravity builder execution, and a normalized kernel cannot substitute for source analysis. Required subjects and unsupported bounds stay explicit. |
| SH07-C13 | SH-07.6 and uniform gate | Coordinator with sh-core | C11 | Verify existing source/file/public `sh07-core` entrypoints on accepted and rejected paired sources: deterministic identity, actual-path provenance, extension/UTF-8/read policy and no target reread. Record which spelling actually traversed each stage; parser parity alone is not paired composition evidence. Use existing routes; complete public compiler repointing is a later driver obligation. |
| SH07-C14 | SH-07.6 | Coordinator with affected consumers | C11 | Pass actual SH06 -> Gravity Core -> C7 boundary checks on selected products; handwritten/macro-generated equivalents preserve behavior, source/origin/binding/order and explicit pending facts. Exercise affected C8/emitter/runtime consumers only for changed interfaces or concrete regressions. Do not require all later milestone implementations to finish. |
| SH07-C15 | All six and uniform gate | sh-core with boundary owners | C11-C14 | Complete diagnostics and negative composition matrix: C6 lowering/shape/order/origin/effect-drop/unsafe-drop/domain/verify families as applicable, plus upstream-owned rejection. Stale/substituted/coherently forged inputs and dropped/reordered graph products fail closed; graph limits accept at-bound and reject above-bound. Preserve available source/generated provenance and prevent raw host exceptions. |
| SH07-C16 | SH-06 prerequisite | Coordinator with sh-macro/sh-resolution | C01; semantic work can proceed independently | Reconcile current SH05.1/SH05.6/SH06.6 and resolver support for the exact selected candidate. Retain historical credit; obtain affected source/expansion/resolution/comparison and independent acceptance where missing or invalidated. This task closes the upstream current-cohort dependency, not every optional macro or namespace feature. |
| SH07-C17 | Migration and uniform gate | Coordinator and independent reviewer | C12-C16 | Select the stable revision and produce the applicable fresh module conformance, provenance, actual compiler identities, stage/IR/diagnostic comparisons, reviewed deltas, BOOT5 support/TCB records and independent acceptance. Reuse valid unchanged expensive evidence; refresh affected evidence. Required skipped/failed/interrupted results block their claims. No new authority wrapper or receipt schema is needed. |
| SH07-C18 | Completion decision | Coordinator | C17; C01-C16 closed for claimed scope | Review the complete six-requirement matrix and independently accepted clean revision. Update existing status/evidence records with exact support and residuals. Mark SH-07 complete only if its full declared compiler-subset exit gate and applicable uniform gates pass. Otherwise record bounded module completion and the exact still-open task(s); seed retirement and later-stage completion remain separate. |

## Required cases and evidence per task

C01's matrix is a section/table in the existing planning/support records, not
a new executable manifest. Each required variant row must contain:

1. C-task ID and one of SH-07.1 through SH-07.6, with the owning contract rule.
2. Exact source revision, path, function/form use or transitive dependency
   showing why the behavior is required; generated forms identify their macro.
3. Owning entrypoint and expected Core product/order/control/source invariant.
4. Accepted `.gravity`/`.qst` case and each applicable rejection-family case,
   expected stable diagnostic and provenance; spellings actually run are named.
5. Exact existing/new owning selector, real composition boundary and evidence
   reference, with tested revision and implementation/integration/migration class.
6. Status: located-only, implementation-gap, verification-open, invalidated,
   accepted-for-scope, or outside-subset with justification and rejection case.

A missing required variant, unchecked proof obligation or unsupported required
primitive is an open row. A valid structured rejection closes a negative case;
it does not close required positive support. An outside-subset row is lawful
only when no selected compiler dependency uses that behavior.

The 13 core forms are covered by C02 (`quote`), C03 (`def`, `var`), C04
(`if`, `do`, `let`), C05 (`fn`), C07 (`loop`, `recur`), C08 (`set!`), C09
(`try`, `throw`) and C10 (`match`). C06 covers ordinary calls/dispatch rather
than inventing a fourteenth special form. C11-C15 cover cross-cutting C6
artifacts, preservation, authentic composition and rejection obligations.

## C01 working support map

The selected *source subjects* are the six complete owner files in the
[current cohort table](self-hosting-current-migration-cohort.md#c01c16-reconciliation-at-2a94fb71),
not six snippets, every directory entry, or every later typed/native owner.
The output is ordinary verified canonical Core under `:meta/:jvm`. Source
analysis, execution of the owning Gravity lowerer, and typed execution of a
compiled compiler are three different claims. Only the first two belong to
this bounded proposed module decision; the third still needs its real later
producer/consumer/build graph.

The retained source censuses identify `defn` expansion into original `def`/`fn`
and reused bodies, with quote/catalog DATA kept separate. They do not provide
the complete source-occurrence-to-Core/primitive matrix required to close C01.
The table below populates concrete starting rows without inventing the missing
variant population. Each test name is a locator in the existing owning test
file, not a newly executed or automatically applicable result. Status is
**located-only** unless the original scoped result is explicitly retained by
the audit; candidate-wide acceptance remains **verification-open**.

The [Core test owner](../bootstrap/clojure/test/gravity/self_hosting/sh07_checked_core_test.clj),
[B47 composition owner](../bootstrap/clojure/test/gravity/self_hosting/sh07_b47_function_call_recursion_test.clj)
and [fragment owner](../bootstrap/clojure/test/gravity/self_hosting/sh07_module_fragment_test.clj)
contain the named checks below; the remaining owner paths are in
[Existing validation starting points](#existing-validation-starting-points).

| Required use or unresolved variant decision | Tasks and owning rule/product | Existing accepted/rejected checks and evidence boundary | Missing closure / invalidation |
| --- | --- | --- | --- |
| Six owner `def` DATA and `defn` bodies; quoted type/catalog maps, for example `sh07-atomic-type-id` | C02/C04/C05/C11; L1/L2/C4/C6 preserve kinds, inert quote, constructor order and generated `def`/`fn`. | `sh07_checked_core_test.clj` carrier-shaped DATA and decimal kernels; mutation tests for character occurrences, map source order and quoted duplicates; retained SH05 subject/reference comparisons. | Join every required literal/constructor/quote occurrence, including rejected shape/value/origin cases. A `defn` count is not execution of every body or completeness of primitive support. |
| Named owner calls and exact source binding identities; `sh07-lower-reference`, `sh07-lower-def`, `sh07-lower-var` | C03/C05/C06; C5/C6 retain lexical/namespace/definition/value and call-edge distinctions. | `sh07_references_calls_test.clj`, alias-qualified and var-reference owners; `sh07-b5-qualified-var-development-kernel`; SH06 genuine imported/private/unresolved and stale binding compositions in the audit. | Select actual qualified/import/var uses, if any, from executable positions, not quoted handlers. Preserve upstream C5 rejection versus Core rejection. No ambient host lookup or inferred callee artifacts. |
| Ordinary owner `if`/`let`/`loop`/`recur` algorithms; for example `sh07-vector-contains?` and `sh07-b13-flatten-fragment-field` | C04/C07/C11; L2 control/order/lexical/nearest-target records. | `sh07-b1-if-and-do-preserve-order-and-exclusive-branches`, malformed-control and replay-mutation selectors; `sh07-b3-let-products-preserve-sequential-scope`; owning loop/recur tests. | Exact body/empty/terminal/nested/transfer variants still need occurrence joins. A lowered recur is not a C7 type-compatible transfer or executable runtime loop. |
| Ordinary functions/calls, including real two-function products | C05/C06/C12-C14; original function signatures, parameter IDs, ordered arguments, captures and recursion components. | `sh07-b47-two-i64-call-current-gravity-composition`: retained real current-reader G composition, public fresh verification and session mutations at its original revision; Q source selector was not run. B48 is separate direct-call legality. | Do not turn paired byte/seed-parser checks into Q composition, raw Core into checked C7-C10, or B48 into public ordinary-call routing. Required closure/cross-module/indirect/variadic variants need an explicit use and consumer decision. |
| Mutation, typed errors/handlers and structural/guard match variants | C08-C10; L2/L7/L9/C6 location/RHS order, control exits, clause scopes and structural-failure skipping. | Qualified mutation, throw/try/multiple-catch, match/vector-pattern owners; quoted-symbol, keyword-map and root-map-guard kernels plus their source/forgery negatives. | Implementation exists for scoped variants, but source spelling in the lowerer's quoted dispatch DATA is not a required executable use. Do not exclude a variant as optional or close its task without the complete selected use matrix. Later guard Bool/totality, effects and ownership remain their owners' obligations. |
| Complete Core graph, fragments, maps, retained metadata and declarations | C11/C13-C15; C6 canonical products and authentic source/input joins. | `sh07-b13-fragment-manifest-is-exact-root-aligned-and-exhaustive`, `sh07-b13-fragment-coverage-is-exact-ordered-and-complete`, `sh07-b13-fragment-request-alterations-fail-closed`; owning template/resolved/origin/source-map checks. | Inspect actual tested inputs/results and constructor route. Public default-pinned tests are not current-reader evidence. Graph/identity/provenance/source mutations require the real affected boundary, not a copied passed flag. |
| Full checked-Core source as an analyzed subject versus the Gravity builder as executor | C12/C16, distinct from C02-C11 kernels. | Audit records the incomplete exit-143 source attempt and bounded smaller subject results. Existing `b-run-frontend-subject` ends in hosted `gravity.c6-core-lowering`, not `gravity.checked-core`. | Current Gravity source-analysis/replay/provenance remains OPEN with its own finite subject/resource decision. The legacy attempt remains INCOMPLETE, cause unresolved; no unchanged large retry or mandatory hosted-product redesign follows. |
| Required raw seed primitives behind all six executed owner plans | C01/C11-C12; D1/L2/C6 and declared temporary TCB. | `compiler.gravity` rule DATA, `00575`/`00576`/`00580` seed construction and `00588`/`00591`/`00593` execution; repository closure bound in the cohort document. | Exact used primitive/instruction manifest and actual compiled plan identities are MISSING. The allowed builtin list is not a used-call census. A required unsupported primitive remains open at its implementation owner. |
| Typed downstream primitives used by the actual Core source: `sh07-exact-keys?` uses `map?`, both `count`s, `keys`, `set` and equality; Vector traversal uses `first`/`rest` | C14 affected integration; SH08/SH09/SH13/SH14/SH22 implementation ownership, not new Core forms. | In `sh08_function_call_type_test.clj`: `sh08-vector-rest-current-source-composition`, `sh08-map-keys-current-source-composition`, `sh08-list-set-current-source-composition`, `sh08-bare-keyword-anchor-current-source-composition`. Merged [PR265](https://github.com/burn2delete/uniform/pull/265) and [PR269](https://github.com/burn2delete/uniform/pull/269) add accepted scoped G composition: explicit private `BareMap keys -> HList Keyword -> List set -> bare Set -> nonempty literal Keyword anchor -> Bool`, 61 assertions. | This does not type or execute the whole unannotated helper or establish a variable expected Set, C8, runtime or migration. `map?` supplies no key-domain facts; call shapes do not automatically generalize parameters. Owning SH09/SH01 results remain separate. Do not narrow the helper's non-Map-to-false behavior by silently adding a Map annotation. |

### C01 and C16 stopping point

The six full subjects, ordinary current-reader intake, repository load boundary
and concrete evidence locators are recorded. Still MISSING are the complete
occurrence/primitive/import variant matrix, actual selected plans and external
compiler/environment identities, and candidate-bound acceptance/rejection
results for every required row. This is useful reconciliation, not completion
of C01. No source or fixture was excluded to obtain a passing count.

C16 retains the audit's historical 39-S/three-P traceability classification,
not a current 39/42 execution score. SH-05.1, SH-05.6 and SH-06.6 remain OPEN;
the resolver stays development-unadmitted. Earlier expansion/resolution and
hosted-consumer results may retain their original scope where dependency
comparison justifies it. The changed reader, seed/adapters and explicit emitter
freshness input prevent treating them as one fresh selected cohort. C16's
remaining action is that exact upstream/cohort reconciliation, not an invented
new macro feature, marginal resolution fixture or historical corpus repin.

Before C17, the coordinator selects one genuinely stable, independently
reviewed revision and comparison method in the existing BOOT5/BOOT7/BOOT8
records. Source-unchanged evidence is reused only with its actual dependency
assumptions. Any remaining required semantic or interface row receives its
existing C02-C15 owner and proportionate accepted/rejected checks. If only this
bounded module migration succeeds, C18 records it and leaves milestone-wide
SH-07 partial; it does not assert SH25 compiler rebuild, seed retirement,
release, or support for all later targets.

## Existing validation starting points

Test stems below live under
`bootstrap/clojure/test/gravity/self_hosting/`. They are evidence locators,
not a batch command or a claim that every task already has exhaustive checks.
Add missing behavior tests to those owners and select exact vars.

| Tasks | Existing test files and representative selectors |
| --- | --- |
| C02 | `sh07_checked_core_test.clj`: `sh07-carrier-shaped-language-data-remains-ordinary-data`, `canonical-decimal-presentation-development-kernel`; `sh07_mutation_test.clj`: character occurrence, map source-order and quoted-duplicate development kernels. |
| C03 | `sh07_var_reference_test.clj`: `sh07-b5-qualified-var-development-kernel` and qualified-current-namespace/authentication checks; `sh07_references_calls_test.clj`; `sh07_alias_qualified_reference_test.clj`. |
| C04 | `sh07_control_flow_test.clj`: `sh07-b1-if-and-do-preserve-order-and-exclusive-branches`, malformed-control and order/origin mutations; `sh07_lexical_binding_test.clj`: `sh07-b3-let-products-preserve-sequential-scope`. |
| C05-C07 | `sh07_b47_function_call_recursion_test.clj`: atomic-signature and List-parameter development kernels, exact function/call products and recursion/shadowing/order; `sh07_b48_call_arity_test.clj`; `sh07_loop_recur_test.clj`: target/tail and transfer/order mutation checks. |
| C08-C09 | `sh07_mutation_test.clj`: `sh07-b6-qualified-set-development-kernel` and target/RHS/order checks; `sh07_throw_test.clj`, `sh07_try_catch_test.clj`, `sh07_multiple_catch_test.clj`. |
| C10 | `sh07_match_lowering_test.clj`: quoted-symbol, irrefutable-clause and root-map-guard development kernels, authentic composition and forged/source rejection checks; `sh07_vector_pattern_test.clj`: keyword-map development kernel and vector/branch/binding mutations. |
| C11-C15 | `sh07_checked_core_test.clj`: genuine upstream/supported Core, source/file equality, exact origins, projection mutation, malformed carrier, identity/provenance and template/resolved verification; `sh07_module_fragment_test.clj`: ordered complete coverage and module assembly; `sh07_public_core_routing_test.clj`; source-coverage tests for the selected actual subjects. |
| C12, C16-C17 | The cohort assessment's per-input evidence map, the existing SH06 frontend-subject helper and owning SH05/SH06 tests; current cohort audit and stable-module comparison/support records. Historical counts alone do not close these tasks. |

For example, inspect then run the existing direct guard kernel:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --list
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-match-lowering-test --exact sh07-b10-root-map-guard-development-kernel --fail-fast
```

This example is not full guard acceptance or a command to run for this plan.
Use the [development workflow](development-verification.md) for focused tests,
affected composition, resource bounds and stable-module migration. No heavy
proof, source-analysis retry or native execution is authorized by a task table.

## Execution order and stopping rules

The first next action is C01: adopt and populate the exact candidate support
matrix from the existing cohort/source-use evidence. Independently, C12 selects
the owning Gravity-Core source-analysis/replay transaction with exact resource fit.
Neither action requires legacy hosted-product redesign, old whole-corpus replay or a new
proof architecture. Ordinary demonstrated semantic gaps can be fixed meanwhile.

After C01, work in dependency order: C02-C04, C05-C07, C08-C10, then C11.
C12/C13/C14 follow the affected interfaces; C15 closes the negative matrix.
C16 closes the upstream migration dependency before C17. C17 and C18 are the
single stable-candidate acceptance/completion decision. These are ordering
constraints, not instructions to run every test sequentially in one JVM.

Report implementation and evidence separately. For each C-task, retain the
exact required rows still open and the next concrete source/test action. A
new feature or merged PR does not count as closing a task unless its acceptance
rows are satisfied. Administrative task counts are not a percentage of compiler
functionality or elapsed effort.

Required primitive implementations belong to their real owners: types to SH08,
effects/profile/capability checks to SH09, ownership/safety to SH10/SH11,
MIR/runtime control to SH12/SH13, collections/layout/allocation to SH14,
runtime providers to SH19 and library APIs to SH22. A primitive closes SH-07
work only where it changes required Core lowering or enables its selected
builder execution; downstream development must name its downstream milestone.

SH-07 completion does not require the entire type/effect/ownership/safety
pipeline, compiled application execution, all L7 variants in every profile,
Linux/native toolchain support, repointing every public compiler command,
package refresh, full compiler rebuild, seed retirement or release. It does
require honest pending facts and lawfully supported selected source inputs.
The current staged Linux LLVM release target remains unchanged.

Existing held B51/source-publication, exception architecture and other governed
work keeps its recorded disposition. This planning document does not adopt a
held route as a prerequisite, waive its invariant or change its lifecycle.
If a required task actually depends on such work, C01 records the concrete
edge and the coordinator resolves it through the existing owner and policy.

## Outputs and conformance criteria

The required outputs are one populated support/task matrix, the owning Core
implementation and fixture changes for demonstrated gaps, selected focused
and affected-composition results, and the existing stable-module acceptance
bundle. This document supplies the finite checklist and baseline locators;
it supplies no new test results, migration authority or milestone promotion.

Completion requires all 18 tasks closed for the declared claim, every required
variant row accepted, no required input silently excluded, a verified canonical
artifact and stable negative diagnostics, completed applicable dependencies,
and independently accepted exact revision/evidence. C18 distinguishes module
migration from the full SH-07 gate explicitly. Missing evidence cannot be
reported as a pass, and later compiler-closure validation cannot inherit
unsupported semantics from a bounded earlier claim.
