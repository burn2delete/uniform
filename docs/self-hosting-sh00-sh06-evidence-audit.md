# SH-00 through SH-06 Evidence Reconciliation

## Purpose and scope

This report maps the first 42 requirements in the
[finite milestone plan](self-hosting-milestone-completion-plan.md) to existing
checks and recovered historical run/review records. It reconciles current
module-owner projections without promoting a milestone or renewing a proof.

Audit input: committed tree `299af0717e5fee244af267a9e4406036c7190869`.
Unrelated active compiler, fixture and document edits were neither imported nor
modified. The report and ownership repair were prepared in a separate worktree
of that revision. No expensive historical proof was replayed, and no archived
agent was contacted or restarted. Archived instructions are evidence, not new
execution permission. Old Python validator results below are historical facts,
not instructions to restore retired tooling.

## Evidence classification

- **S:** implementation/check support located; recovered runs can corroborate
  the original bounded revision. This is not a new semantic or migration pass.
- **P:** support exists, but present corpus/scope assertions need reconciliation.
- **U:** required execution/review records not located or verified.

The first desk audit had 34 S, six P and two U. Recovery found both missing
SH-00 records; the ownership repair addresses three projection gaps. After
the focused repair check passes, traceability is 39 S and three P: 92.9% and
7.1% of 42 requirement groups, not a functional completion percentage.

Seven historical milestone credits remain recorded. This audit does not
freshly admit the current compiler revision. A matching source content pin
does not establish plan/dependency identity, semantic correctness or provenance.

## Historical evidence recovered

The retained local coordinator transcript is:

`/Users/matt/.codex/sessions/2026/07/09/rollout-2026-07-09T19-10-29-019f4993-78b9-7573-b154-139d87a127b5.jsonl`

Locators below are one-based in the inspected transcript. Timestamp, author
and payload ID permit relocation if physical line positions change. These
are original retained statements, not new reviewer decisions. The full
transcript is not copied into the repository because it contains unrelated
work and private conversation history.

| Ref | UTC time / line | Evidence and limits |
| --- | --- | --- |
| E00-R | 2026-07-18 01:56:36 / 105273 | `sh00_final_audit`, PASS-for-SH-00: genuine signed-i32 C11-C13-C14-B1-B4 path, pinned Node parsing/execution, 24 rejection mutations, separated identity/provenance and explicit nonclaims. ID `amsg_019f72f0-937f-76d3-8b7e-e7dde8d788ff`. |
| E00-T | 2026-07-18 02:36:48 / 105798 | Actual tool output, call `call_heczS0yyvTqyxElVmXMQSyYK`, completed chunk `280d1b`, exit 0: `{:test 14, :pass 879, :fail 0, :error 0}`. |
| E01 | 2026-07-18 03:04:47 / 106210 | `sh01_candidate_audit`, PASS-for-SH-01: 4 tests/57 assertions, zero failures/errors, five intended staged files, ownership of the then-current 34 modules and dedicated discovery. ID `amsg_019f732e-ffa2-7440-94ad-e11f566e75f0`. |
| E02 | 2026-07-18 05:30:13 / 107594 | `sh02_final_audit`, PASS: 13/177, zero failures/errors, 59,369-byte/72-function module, genuine C13/B1 reuse, fresh reconstruction, path-neutral identity, graph bounds and explicit host TCB. ID `amsg_019f73b4-22e7-7a73-9728-a65abe5aaa43`. |
| E03-T | 2026-07-18 23:54:59 / 116605 | `sh03_regression_gate`: dedicated 19/989, isolated self-source 1/8, historical regression 25/1,817, stage1 1/115, SH-02 13/177; reported zero failures/errors and validators passed. ID `amsg_019f77a7-9720-7fa1-8f10-5b050f8a334f`. |
| E03-R | 2026-07-19 00:02:02 / 116696 | Post-commit definitive PASS: exact `eeb9253d`, parent, 108 paths, frozen hashes, 49 fixture pairs, empty index and unrelated edits excluded. ID `amsg_019f77ae-0956-73a2-8f96-a1309f711ce7`. |
| E04-R | 2026-07-19 03:40:18 / 120257 | Final SH-04 definitive PASS after earlier findings were addressed. ID `amsg_019f7875-df30-77f2-aa90-5117525da4f8`. |
| E04-T | 2026-07-19 03:56:36 / 120420 | Central C2/C3/P15 9/379, zero failures/errors; dedicated 18/825 confirmed reported green with preserved hash; validators/diff passed. ID `amsg_019f7884-cc8d-7710-ac84-c9756adbb259`. |
| E05-C1 | 2026-07-22 01:42:11 / 126988 | Final shard 1/3: 12 modules, 265 definitions, 1/4, zero failures/errors, exit 0. ID `amsg_019f877c-ce6f-7192-849c-62ae6585ee64`. |
| E05-C2 | 2026-07-22 02:33:27 / 127174 | Final shard 2/3: 12 modules, 600 definitions, 1/4, zero failures/errors. ID `amsg_019f87ab-bd5a-7a81-9edb-27485d97b88e`. |
| E05-C3 | 2026-07-22 01:52:33 / 127030 | Final shard 3/3: 12 modules, 411 definitions, 1/4, zero failures/errors, exit 0. ID `amsg_019f8786-4d52-7541-956c-ad18c16f6dd9`. |
| E05-T | 2026-07-22 04:48:51 / 127868 | Dedicated 18/698, zero failures/errors, exit 0; corpus explicitly skipped. The separate final shards above supply corpus coverage. ID `amsg_019f8827-b6e9-7332-9c5e-60d084ba1800`. |
| E05-R | 2026-07-22 04:52:37 / 127911 | Definitive pre-commit PASS for scope, diagnostics, bounds, fixtures and corpus accounting; unrelated files excluded. ID `amsg_019f882b-2683-7983-948c-c0f3e2956d90`. |
| E06-R | 2026-07-23 06:16:20 / 134879 | Static PASS conditional on finishing running gates: 37 modules, genuine SH-05 input/replay, ten C5 families, C6 consumption and distinct resource bounds. ID `amsg_019f8d9e-2724-73e0-a792-c0e79ebb5c32`. |
| E06-C | 2026-07-24 00:13:02 / 138949 | Final 19-module and 18-module shards, each 2/16, zero failures/errors, both exit 0; pre/post guards passed; 37-path inventory hash `dd422643f5aa91848b5093284db10aad9c501b63dabab74efbe32d5dcb3ab2b1`. ID `amsg_019f9177-ea95-7093-aa63-5d0727a7babb`. |
| E39 | 2026-08-05 19:32:08 / 253432 | Actual tool output `fco_019fd369-6a4e-7da0-bfdf-17751346a4b0`, completed chunk `6dec34`, exit 0: SH-07 source-proof run over resolver source, 77,209 bytes, source hash `001ef59741f17b98b37ee5bdb21e698cb1e6e56ce76c5f5fdd5f1fc9a4caeb56`, artifact `f58d88d1103ea2c72bf549ffdae0fac42272efc8dd5f7305ea85d269f68537fb`, all contract checks true. This joins the backlog's 39-of-41 source-coverage step, not current resolver migration. |
| E41 | 2026-08-06 15:21:57 / 263542 | Actual tool output `fco_019fd7aa-b70a-7fa2-b722-4b150d5d68af`, completed chunk `1a2268`, exit 0: checked-Core source proof at 407,721 bytes, source hash `c020a0344d42569577e8baa6e28a2ce210f3eb76f774ff25490ddb18390d93e0`, artifact `fceeeac37185c5eebec3a6de6b7979c3ec86e0616da46bb274d8caaec35d3189`, all contract checks true. The embedded resolver revision is the same frozen 77,209-byte source; this is a later SH-07 source-coverage endpoint, not a rerun of the original whole SH-06 corpus. |

The corresponding original commits are SH-00 `dbd7764c`, SH-01 `cbc55cd1`,
SH-02 `914215e3`, SH-03 `eeb9253d`, SH-04 `64f7b968`, SH-05 `bd8384b5`,
and SH-06 `8f4b91d3`. Resolve their full identities through retained Git history.

Not every recovered record is raw stdout: E00-T contains tool counters, while
others include original independent reports of runs/reviews. This audit has
not cryptographically reauthenticated every historical input/output occurrence
or proved a continuous acceptance chain to HEAD. Failed/interrupted/superseded
attempts remain such; their counts are not combined with final passing runs.
The older June B4 document report explicitly lacks external tool execution
and is not substituted for E00-T.

## All 42 requirement mappings

Test stems below are under
`bootstrap/clojure/test/gravity/self_hosting/`, except explicitly central
checks in `bootstrap/clojure/test/gravity/bootstrap_test.clj`. These are
entrypoints for evidence inspection, not instructions to rerun every namespace.
S does not mean a current full-suite or migration pass.

| Requirement | Implementation/check evidence | Historical corroboration | Score |
| --- | --- | --- | --- |
| SH-00.1 | Central `authenticated-binary-integer-comparisons-reach-gravity-b4-wasm`, typed MIR/op joins | E00-R/T | S |
| SH-00.2 | Same check, Gravity B4 emission and actual byte map | E00-R/T | S |
| SH-00.3 | Central `authenticated-binary-integer-comparisons-run-real-wasm` | E00-T/R | S |
| SH-00.4 | Central `authenticated-binary-integer-comparisons-fail-closed-at-b4` | E00-R/T | S |
| SH-00.5 | Exact independent review/tool locators above | E00-R/T | S |
| SH-00.6 | Exact committed six-file change and scoped integration record | E00-R, Git | S |
| SH-01.1 | Module-owner map and `ownership-record-is-total-and-unambiguous` | E01 | S after repair |
| SH-01.2 | Shared reservations, fixture conventions and owner projection | E01 | S after repair |
| SH-01.3 | `dedicated-test-routing-does-not-use-the-monolithic-test-file` | E01 | S |
| SH-01.4 | Stage owners and compatibility wrapper tests | E01 | S |
| SH-01.5 | Strict namespace/selector/order/unknown-selection tests | E01 | S |
| SH-01.6 | Exact ownership set/existence/paired-source-test assertions | E01 | S after focused repair check |
| SH-02.1 | `genuine-c13-and-b1-packets-use-two-reusable-stage-envelopes` | E02 | S |
| SH-02.2 | `every-authority-bearing-family-changes-the-semantic-root` | E02 | S |
| SH-02.3 | `semantic-and-provenance-roots-have-separate-dependencies` | E02 | S |
| SH-02.4 | Changed-product/stale-carrier/contextual rejection checks | E02 | S |
| SH-02.5 | `recursive-leaf-bounds-accept-the-maximum-and-reject-the-next-value` | E02 | S |
| SH-02.6 | Real C13/B1 construction, replay and contextual verification | E02 | S |
| SH-03.1 | Unicode/scalar/newline/literal fixtures and dedicated reader tests | E03-T/R | S |
| SH-03.2 | Complete surface, delimiter and exact depth-bound checks | E03-T/R | S |
| SH-03.3 | Registered extension, abbreviation and metadata cases | E03-T/R | S |
| SH-03.4 | `sh03-co-canonical-fixtures-have-accepted-and-rejected-parity` | E03-T/R | S |
| SH-03.5 | Public C2/C3/P15 form and source-occurrence routing tests | E03-T/R | S |
| SH-03.6 | `sh03-public-reader-accepts-its-own-gravity-source`, bridge regressions | E03-T/R | S for original subset |
| SH-04.1 | `sh04-public-c3-preserves-exact-spans-metadata-and-pair-identity` | E04-T/R | S |
| SH-04.2 | `sh04-generated-syntax-preserves-hygiene-capture-and-origins` | E04-T/R | S |
| SH-04.3 | Generated-origin graph and paired-lineage mutation checks | E04-T/R | S |
| SH-04.4 | `sh04-complete-stream-products-roundtrip-and-mutations-fail-closed` | E04-T/R | S |
| SH-04.5 | `sh04-graph-validation-is-bounded-and-fails-closed` | E04-T/R | S |
| SH-04.6 | Public C3/P15 freshness checks and later SH-05 consumption | E04-T/R, E05 | S for declared revisions |
| SH-05.1 | Gravity `defn` expansion and complete-corpus selector | E05-C1/C2/C3 | P for current cohort |
| SH-05.2 | `sh05-basic-defn-expansion-is-stable-co-canonical-and-origin-linked` | E05-T/R | S |
| SH-05.3 | Phase/profile/generated-output rejection families | E05-T/R | S |
| SH-05.4 | Build-effect/capture authority rejection fixtures and facts | E05-T/R | S |
| SH-05.5 | Depth/size/return/hygiene/trace malformed cases | E05-T/R | S |
| SH-05.6 | Stage0 body comparison and real corpus construction | E05-C1/C2/C3 | P for current cohort |
| SH-06.1 | Generic module/foreign/compiler-case resolution checks | E06-R/C | S |
| SH-06.2 | `sh06-resolution-order-is-real-stable-and-c4-bound` | E06-R/C | S |
| SH-06.3 | Sequential environments, qualified names and scoped bindings | E06-R/C | S |
| SH-06.4 | Ten C5 rejection families and oracle-exclusion tests | E06-R/C | S |
| SH-06.5 | Serialization/replay/identity checks and authenticated C6 consumer | E06-R/C | S for accepted revisions |
| SH-06.6 | Whole-corpus shards and exact inventory checks | E06-C | P for current cohort |

The SH-03 through SH-06 classifications retain their explicit bootstrap
subsets, host TCB and unsupported boundaries. They are not full C2-C5 language
completion or seed retirement. Current SH-06 changes remain development work,
not silently accepted successors to E06-R/C.

## Current integrity checks

Read-only byte comparisons found 97/97 identical accepted/rejected source
pairs: SH-02 1+1, SH-03 10+39, SH-04 2+12, SH-05 4+14, SH-06 4+10. This is
byte parity, not 97 semantic passes. SH-04/05 include executable scenario DATA;
not every rejected file is a standalone rejected program.

SH-03 reader, SH-04 syntax and SH-05 macro source byte counts/SHA-256 match
their current literal pins. SH-06 source is 97,032 bytes versus its frozen
77,209-byte pin and a different hash. Its ordinary binding explicitly labels
current code `:development-unadmitted`. That is not automatically a defect or
permission to change pins. Plan/tool/dependency freshness remains unaudited.

## Ownership projection repair

The baseline had 69 tracked `.gravity` files under `bootstrap/gravity/src/`,
but only 60 in the module-owner projection. This change adds exactly:

- `gravity/effects.gravity` to existing stage owner `:sh-effects`.
- `gravity/runtime.gravity` to existing stage owner `:sh-runtime`.
- `gravity/sh07_b51/{producer_a,producer_b,producer_c,producer_d,root6_a,root6_b,root8}.gravity`
  to existing stage owner `:sh-core`.

These stage-level projections do not transfer governed candidate ownership,
create a writer/reservation, or accept implementation. Shared-path coordination
remains unchanged. Exact source count is 69 and module-owner count is 202.

The initial focused repair check then exposed one existing coordinator helper
missing from the central reservation:
`bootstrap/clojure/src/gravity/authenticated_artifact_pipeline.clj`.
Its source explicitly describes shared hosted orchestration and non-authority.
It is now reserved as coordinator support, not misclassified as an unpaired
Stage0 leaf. The support count changes from 14 to 15; the paired Stage0 path
count remains 108. Exact set equality, file existence, paired ownership and
reserved-path checks are retained, with an explicit assertion for this helper.

Initial check result: one test, 552 assertions, three failures, zero errors.
That failed result is not a pass. The final corrected focused result and the
language/document validator outcomes are recorded at delivery; no expensive
compiler proof is part of this repair.

The corrected four-var selection passed four tests/624 assertions with zero
failures/errors. It selected `ownership-record-is-total-and-unambiguous`,
`coordinator-reservations-and-leaf-conventions-are-explicit`,
`dedicated-test-routing-does-not-use-the-monolithic-test-file` and
`repeatable-namespace-selection-is-strict-and-deterministic` through the existing
development runner at Xmx512m. The existing language-boundary namespace passed
six tests/11 assertions with zero failures/errors at Xmx512m. These are local
development checks of this repair, not independent acceptance or migration.

## Corpus reconciliation

| Population | Paths | Evidence meaning |
| --- | ---: | --- |
| SH-05 commit `bd8384b5`, `bootstrap/gravity/src/` | 36 | Original macro corpus; three passing shards, 1,276 definitions |
| SH-06 commit `8f4b91d3`, same root | 37 | Original resolver corpus; two passing shards and exact inventory hash |
| Later backlog SH-06 coverage statement | 41 | Later SH-07 source-coverage population; E39/E41 corroborate selected source-proof endpoints, not a new whole-corpus SH-06 run |
| Backlog's additive inventory statement | 42 | Another later inventory, not a fresh execution result |
| Audited HEAD, same directory enumeration | 69 | Current selector population, not 69 admitted modules |
| Additional `bootstrap/gravity/p15_s23/` sources | 5 | Important compiler/emitter/runtime/native inputs outside those selectors |

Git confirms the original SH-05 cohort is contained in original SH-06; the
added path is `gravity/resolution.gravity`. All 37 original SH-06 paths remain
present today, with 32 additions. Presence does not prove unchanged bytes.

Exact Git blob comparison from `8f4b91d3` to the audited HEAD found 19 unchanged
source blobs, 18 changed source blobs, 32 additions and zero removals. The 18
changed paths, relative to `bootstrap/gravity/src/gravity/`, are:

- `backend/b1_backend_interface_specification.gravity`,
  `backend/b2_c_backend_design.gravity`, `backend/b3_llvm_backend_design.gravity`,
  `backend/b4_wasm_backend_design.gravity`.
- `bootstrap/reader.gravity`, `resolution.gravity`.
- `compiler/c10_safety_analysis_pipeline.gravity`,
  `compiler/c11_mir_specification.gravity`, `compiler/c12_domain_ir_architecture.gravity`,
  `compiler/c13_mir_optimization_passes.gravity`,
  `compiler/c14_target_lowering_architecture.gravity`,
  `compiler/c15_compiler_diagnostics.gravity`, `compiler/c6_core_lowering_engine.gravity`,
  `compiler/c7_type_checker_engine.gravity`, `compiler/c8_effect_checker_engine.gravity`,
  `compiler/c9_ownership_checker_engine.gravity`,
  `compiler/l1_c2_surface_syntax_reader.gravity`,
  `compiler/l2_core_language_semantics.gravity`.

Thus 50 of the 69 directory inputs have changed or were added since the
original resolver cohort. These are delta-review candidates, not a directive
to run 50 expensive proofs. The 19 byte-identical source files are reuse
candidates only: changed compiler, dependency, runtime or policy inputs can
still invalidate their old evidence.

The 32 additions, relative to `bootstrap/gravity/src/gravity/`, are:

- Backend: `backend/w5_b13_artifact_emitter.gravity`,
  `backend/w5_b14_backend_conformance_verifier.gravity`.
- Core: `checked_core.gravity`, `compiler/c6_call_arity_legality.gravity`.
- Compiler helpers: `compiler/w5_c16_incremental_executor.gravity`,
  `compiler/w5_c17_plugin_executor.gravity`, `compiler/w5_c18_pass_verifier.gravity`,
  `compiler/w5_compiler_pipeline_verifier.gravity`.
- Stage-owned effects/runtime: `effects.gravity`, `runtime.gravity`.
- Build/meta: `self_hosting/hermetic_package_build.gravity`,
  `self_hosting/meta_compiler_legality.gravity`.
- W5 helpers under `self_hosting/`: `w5_compiler_identity_verifier.gravity`,
  `w5_domain_schema_ai_executor.gravity`, `w5_full_language_evidence_verifier.gravity`,
  `w5_ir_lowering_executor.gravity`, `w5_performance_math_executor.gravity`,
  `w5_reader_module_executor.gravity`, `w5_stage_equivalence_verifier.gravity`,
  `w5_stage_rebuild_orchestrator.gravity`, `w5_subsystem_closure_verifier.gravity`,
  `w5_tooling_conformance_verifier.gravity`, `w5_trust_provenance_verifier.gravity`,
  `w5_typed_effect_safety_executor.gravity`.
- Seven `sh07_b51/` source files listed in the ownership repair above.
- Library: `stdlib/self_hosting_core.gravity`.

The five additional P15 files are `compiler.gravity`, `emitter.gravity`,
`native_plan_c_emitter.gravity`, `native_runtime_driver.gravity`, and
`runtime.gravity`. These 74 tracked Gravity inputs are a concrete current
candidate inventory, not an accepted complete transitive bootstrap closure.
Generated code, providers, tools and package inputs still require SH-21-SH-25
closure accounting. Experimental/held modules must not be silently excluded
to obtain a green count or silently admitted because they exist on disk.

The existing corpus selectors expose drift directly:

- `sh05-expands-the-complete-authoritative-compiler-defn-subset` enumerates the
  current directory but asserts 36 files and 1,276 definitions.
- `sh06-corpus-shards-cover-the-sorted-inventory-exactly-once` enumerates it but
  asserts 41 paths and the old inventory hash.
- Full-corpus tests have explicit skip controls; an enabled skip is not corpus
  evidence, even when the surrounding namespace reports green.

Those count/hash assertions and all migration pins are deliberately unchanged.
Replacing them with 69 without reviewing and executing the changed acceptance
population would manufacture coverage. Do not run an old expensive corpus
merely to rediscover these known static mismatches.

The later counts refer to a different gate. E39 authenticates the resolver
source as an SH-07 input; E41 authenticates the checked-Core source and embeds
the accepted SH-06 resolver revision. That is meaningful producer/consumer
evidence, but not interchangeable with whole-source-set SH-06 resolution or
full required semantics. The current backlog also explicitly marks final B47
replay of its subsequent optimized source revision as pending. Its later
additive B48 proof is bounded source/call-arity evidence, not SH-07 completion
or a fresh 42-module resolver migration. No current 69-file coverage claim is
derived from any of these records.

## Specific next actions and remaining gates

1. Review the 18 changed sources, 32 additions and five P15 inputs against the actual compiler
   build graph: required behavior, dependencies, owner, development status and
   accepted producer/consumer boundary. Required inputs remain in scope;
   genuinely optional modules need explicit support exclusions.
2. Complete the individual evidence joins for the remaining later 41/42-module
   source-proof steps if those are needed for the selected migration. E39/E41
   already identify the differing gate and frozen resolver revision. Do not
   infer current coverage from either these endpoints or the original 37-module
   evidence, and do not silently treat B47's pending successor replay as passed.
3. Freeze the exact next stable acceptance cohort and compare content/semantic
   changes, not only path counts. Reuse valid unchanged evidence and identify
   the smallest new/invalidated semantic/composition checks.
4. Run fresh authoritative migration only for that concrete stable-revision
   decision. Do not make this accounting work a prerequisite for ordinary
   SH-07 implementation or replay seven completed milestone proofs.

E06-R was conditional; E06-C closes the original corpus condition, not every
later gate or present revision. Recovery is not a signed, independently
reauthenticated acceptance package for HEAD. This report, focused projection
tests and documentation validation do not change the existing 7/30 count.

## Bounded current frontend closure

The next development coverage endpoint is ordinary C2 -> C3 -> C4 -> C5 ->
hosted Stage0 C6 under `:meta/:jvm`. This is an explicit bounded selection,
not the full compiler, all 69 directory inputs or authoritative migration.
C4/C5, L4/L12, BOOT3 and TEST2 govern the stage and preservation obligations.
The implementation, analyzed subjects and future release closure are separate:

| Role | Selected inputs and limits |
| --- | --- |
| Executed Gravity owners | `compiler/authenticated_envelope.gravity`, `bootstrap/reader.gravity`, `bootstrap/syntax.gravity`, `macro.gravity`, `resolution.gravity`, relative to `bootstrap/gravity/src/gravity/` |
| Reader execution subset | The selected `sh03-*` function closure, not every entry in the full reader source subject |
| Required rule DATA | `bootstrap/gravity/p15_s23/compiler.gravity`, whose interpreted emitter rules support the temporary seed compiler |
| Required freshness input | P15 `emitter.gravity` enters the resolver toolchain key; this is not execution of its Gravity lowering body |
| Temporary implementation/TCB | Clojure source/header projection, expression/function seed compilation, plan construction, generic execution, digest/codecs, SH02-SH06 adapters/verifiers, diagnostics and hosted C6, plus declared Clojure/JDK/filesystem support |
| Initial analyzed subjects | The five selected owner sources, rule DATA separately labelled, and the actual artifact-backed module fixtures used by C5 import tests |
| Compatibility subjects | Gravity C3/C4/C5 facades package records; they do not replace the executed owners or gain authentication credit |
| Optional for this endpoint | Later passes, backends and runtime/emitter execution; these are not optional for their eventual language/target claims |
| Experimental/source inventory | Preserve each module's declared status; declarations and record validators are not execution evidence |
| Held | The seven B51 sources remain outside this ordinary frontend selection with existing restrictions intact |

The actual leaf plan route includes the existing `00575`/`00576` temporary
expression/function compiler, `00580` plan construction and `00593` generic
executor. P15 rule DATA is read by `00644`. Resolver `00874` includes emitter
freshness. `00042-sh06-c6-lowering-from-resolution-artifact.clj` delegates through
`00041` to `gravity.c6-core-lowering`, whose configuration explicitly identifies
hosted Stage0 lowering. It does not execute `gravity.checked-core`. Importing
an authenticated namespace description also does not execute its functions.
No Gravity emitter/runtime, backend, native, target or seedless credit follows.

The subject selection is a next bounded coverage obligation, not a statement
that all five full sources have already passed at this revision. Its exact
macro positions and dependency graph must be inventoried before complete
coverage is claimed. Reader subset execution cannot be relabeled as whole-reader
execution, and optional/held exclusions cannot be extended to a broader claim.

A bounded reader-only structural census at this revision found 606 top-level
`defn` subjects and 34 top-level `def` subjects in the five sources:
envelope 72/2, reader 275/23, syntax 102/3, macro 46/3 and resolver 111/3.
P15 rule DATA separately contains one top-level `defn` and 27 `def` forms.
Each file also has one `ns`. The read used `*read-eval* false`, a 512 MiB heap,
no project loading, no source evaluation and no macro expansion; source bytes
remained unchanged from `3a816435`.

Respecting quotation and catalog DATA, the census found no nested executable
macro position or required unsupported macro. The actual macro recipe introduces
`def`/`fn` and reuses bodies without further required macro calls. In particular,
P15's nested `(defn name params body)` is raw rule DATA, and its sole actual
function returns quoted DATA. This justifies the defn-only behavior selection,
not general recursive/user macro support or a Gravity corpus pass. The selected
leaf plans contain 562 function declarations, including 231 reader functions;
neither declaration count says that every function executed. New subjects,
macro positions or rule consumers invalidate this structural coverage decision.

### Remaining criteria by partial group

- **SH-05.1:** Inventory actual executable macro positions, distinguishing
  quoted data and rule records. `gravity.macro` implements `defn`; general user
  `defmacro`, recursive macro evaluation and general syntax-quote are explicitly
  outside its present subset. If selected executable subjects require them,
  they are real semantic gaps. The existing one-pass top-level-defn adapter and
  legacy external/local macro route do not supply that coverage.
- **SH-05.6:** Compare selected real expansions and rejection behavior with
  the prior-stage reference; exercise generated syntax through actual C5 and
  hosted C6. Preserve hygiene, reused bindings, macro call/definition origins
  and source mapping, with existing coherent trace/origin tamper checks.
  Policy-request negatives do not demonstrate arbitrary effectful macro code.
- **SH-06.6:** Execute the declared subject population and real module graph;
  retain alias/import/export/privacy/source identities, lexical scope and
  authenticated downstream consumption. Existing normalized cycle, ambiguity,
  profile and target kernels remain useful but are not all authentic
  multi-module source compilations. Coherent binding and changed-input
  rejection remain required. The resolver stays `:development-unadmitted`.

### Generated-code composition and diagnostic repair

The direct test `sh06-generated-defn-shadowing-and-diagnostic-composition` in
`sh06_resolution_adapter_test.clj` uses paired sources in the SH-06
`composition/` directory. The initial run used local development inputs based
on `3a816435`, not an admitted successor to the historical audit. The accepted
definition shadows a namespace function
with its parameter and then uses sequential/nested lexical bindings. The
rejected variant substitutes an unresolved body reference, not scenario DATA.

The first run completed FAILED: one test, 38 passes, three failures, zero errors.
It built the accepted Gravity/QST pair and the Gravity negative; negative QST
has byte-parity coverage only. Positive reference expansion equality, three
distinct selected local binding IDs, current C5 verification, generated Core
mapping and authenticated hosted-C6 consumption without source rereading all
passed. It does not establish canonical Gravity C6 execution or type checking.

Two failed expectations incorrectly assumed a direct `:source` field inside
the macro call-site span. The test now compares that span directly with the
retained expanded syntax span; this expectation edit has not replayed the
unchanged positive products and is not a new green result.

The third failure exposed a concrete preservation gap: the unresolved generated body
correctly reports `C5-UNRESOLVED`, symbol, namespace and actual source, with the
C4 artifact ID in facts, but its public diagnostic omits the generated-origin
chain. L12 requires generation provenance on a generated-code failure. The
repair carries authenticated macro origin/call-site data through the existing
source-artifact diagnostic transport, not through a new schema or pin. It joins
the actual reference-check rejection to its exact bounded ordinal, reference
identity/source and unique authenticated expanded owner. Module-level failures
may use the first reference as a display location; those are not causal macro
evidence. Unknown/mismatched/ambiguous/no-chain cases rethrow the same exception.
A valid augmentation retains every original field/message and the original
exception as cause. Only the existing owner's origin vector is attached.

Final focused transport checks passed one test / 90 assertions. Earlier 76/80/87
passing refinements overlap and are not additive coverage. Independent review
caught a missing one-candidate unresolved-core-availability case in the overly
broad zero-candidate guard; the final guard follows actual reason-specific
cardinality and preserves that diagnostic's origin too. Resolver semantics,
raw request/result schemas, migration pins and limits are unchanged.

The changed `sh06-generated-unresolved-origin-composition` and new
`sh06-source-module-cycle-composition` passed two tests / 26 assertions at
2 GiB on the development candidate based on `c7484e92`. Seven checks establish
the actual rejected generated body now carries exactly its authenticated
expanded owner's origin. The 19 cycle checks establish genuine accepted and
verified A0/B artifacts, retained dependency/source/binding identity and the
candidate A1 rejection described below. This exercises four Gravity-source
cases; QST has byte parity only. The earlier accepted shadowing pair was not
rebuilt. Its original 38-pass/three-fail aggregate remains failed; the corrected
span expectations have no new execution credit. Language boundary passed 6/11.

Selected source checking took 137.154 seconds after 7.360 seconds of loading:
26.287 seconds for the changed origin negative and 110.867 for the new cycle.
These are test intervals, not a constructor/verification/allocation breakdown.
This closes the specific diagnostic gap and adds real cycle composition
coverage, not current-cohort migration. All three partial groups remain open.

Use the existing direct runner for this affected composition, not a full corpus:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --exact sh06-generated-unresolved-origin-composition \
  --exact sh06-source-module-cycle-composition --fail-fast
```

For local transport edits, start with
`sh06-generated-diagnostic-origin-transport-development-kernel` in that same
namespace. Do not repeat unchanged source products for a reporting update.

The initial failed combined composition took 95.307 seconds after 6.941 seconds of
namespace loading, at 2 GiB. No per-stage construction breakdown was measured;
neither that interval nor the number of checks establishes scheduling overhead
or a speedup. Verification-product representation measurement/redesign remains
a separate bounded follow-up, not another wrapper or proof lane.

One concrete follow-up candidate is the existing
`sh04-syntax-strip-host-metadata` traversal in `00853`: it recursively rebuilds
collection structure for both arguments and results at SH04/SH05/SH06 invocation
boundaries, even when metadata is absent. An accepted C5 run invokes builder,
template verifier and resolved verifier, so it has six such crossings before
additional replay or module recursion. This is statically observable work, not
evidence that it dominates CPU time or allocation; nested persistent artifact
references alone do not prove physical copying.

At the next already-needed changed-candidate composition, measure bounded
scalar time/call counts at existing stage boundaries, separating cleaning,
Gravity execution and canonical/digest work. Record successful/rejected paths
separately and allocation only if available without broadening the task. Do not
rerun unchanged products for this measurement. Only if the observations justify
it, investigate preserving unchanged immutable collection structure inside that
one traversal while still removing all nested metadata. Require unchanged
values/families, canonical bytes/IDs, diagnostics, complete traversal/bounds
and hostile-carrier rejection; every builder/verifier must still run freshly.
This would be materialization optimization, not proof/result caching or a
schema/provenance reduction. No representation code has changed here.

The executed source graph check uses distinct immutable `cycle-a0`,
`cycle-b` and `cycle-a1` fixture pairs. B retains its genuine A0 artifact;
candidate A1 supplies only B as its direct dependency. The rejected cycle is
the authenticated logical namespace graph A -> B -> A, with A1 the candidate
revision of namespace A, not a third graph node. This is not mutually accepted
cyclic artifacts or B rebuilt against A1. It also checks that a graph-level
failure receives no unrelated macro-origin chain. The test checks both distinct
directed edge endpoints, not an exact edge-row cardinality. QST byte parity is
not execution; neither cyclic artifact acceptance nor package revision selection
is established.

### Genuine referred-name ambiguity: development coverage

The existing SH06 adapter test now constructs two genuine declared-pure `:meta`/`:jvm`
library artifacts exporting the same non-core name, then resolves a caller
that explicitly refers that name from both. Both libraries are accepted and
freshly verified. The caller rejects with `C5-AMBIGUOUS`,
`:multiple-legal-bindings` and exactly two candidates, rather than an earlier
alias, privacy, profile or missing-module rejection.

`sh06-b-source-referred-ambiguity-composition` passed one test / 30 assertions
on the development candidate based on `96d41843`, with independent scoped
acceptance. It joins each candidate to its distinct actual library artifact,
binding, definition syntax and original definition artifact, and checks the
caller source span. Selected execution took 98.788 seconds after 6.773 seconds
of namespace loading at 2 GiB; these are non-authoritative test intervals, not
stage-construction costs or a performance claim.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --exact sh06-b-source-referred-ambiguity-composition
```

The three `.gravity` subjects were processed through the ordinary C5 source
frontdoor; their function bodies were not guest-executed. Their `.qst` partners
have byte parity only. No downstream artifact is attached to the C5 rejection; this is not an
instrumented C6 execution claim. The check adds source/header/import ambiguity
coverage, not ambiguity-specific macro-origin coverage or full selected-cohort
coverage. SH-05.1, SH-05.6 and SH-06.6 remain partial, and the resolver remains
development-unadmitted. No historical corpus or migration fingerprint changed.

### Invalidation and reuse

Unchanged source bytes are only one reuse condition. Preserve compatible
compiler/toolchain, macro versions/environment, reader options, grants,
profile/target, dependency artifacts/exports and preservation contracts.
Reader changes invalidate affected forms and downstream syntax; syntax/hygiene
changes invalidate affected traces and binding/source maps; macro recipe or
policy changes invalidate expansion consumers; resolver catalog/import/export
changes invalidate binding graphs and their consumers. Seed compiler, rule DATA
or generic executor changes can invalidate execution even with unchanged
Gravity owners. Unrelated target implementation changes outside this selected
closure do not automatically require frontend replay.

A stable migration decision still requires its fresh provenance, equivalence,
bootstrap and independent-review evidence under BOOT3/D9. None is renewed here.
Keep historical 36/37, later SH-07 41/42 and present 69+5 inventories distinct;
do not rerun an obsolete full corpus to rediscover known count drift.
