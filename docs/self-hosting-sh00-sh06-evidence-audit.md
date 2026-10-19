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

- **SH-05.1:** The structural census above already identifies the selected
  executable macro positions, separate from quotation and rule DATA. The
  required subset is `defn`; distinct macro, envelope, syntax, resolver and
  reader subject compositions exercise it at their recorded cohorts. General
  user `defmacro`, recursive expansion and syntax-quote
  remain unsupported, but none was required by the selected census. That is
  not general L4 support or execution of every declared function body.
- **SH-05.6:** The recorded compositions compare real expansions with the
  prior-stage reference and preserve hygiene, reused bindings, origins and
  source maps through actual C5 and hosted C6. The later tests below cover
  coherent outer-origin rejection at public C5 and wrong binding-selection
  rejection at hosted C6. The reader's corresponding scoped composition is
  recorded below. Policy-request negatives are not arbitrary effectful macro-code
  execution; current-closure admission still needs its selected stable cohort
  and applicable provenance, equivalence, bootstrap and independent review.
- **SH-06.6:** Real module graph fixtures cover imports/exports, privacy,
  missing/stale origins, cycles, ambiguity, aliases, lexical scope and hosted
  consumption. Later same-JVM profile and capability cases and both hostile
  ingress checks extend that evidence, with the exact failure qualifications
  recorded below. Reader source consumption is now recorded below. Cross-target source
  import remains unreachable under the JVM-only frontdoor, despite normalized
  C5 policy coverage. Stable-cohort freshness and admission evidence remain
  separate requirements; the resolver stays `:development-unadmitted`.

No further small, demonstrated composition gap was identified in this bounded
frontend after those additions. Do not add marginal tests or rerun unchanged
subjects merely to increase a score. The reader run below addresses a genuine
previously unrun subject, not a stamp refresh or migration-only obligation.
The five subject results retain their distinct tested cohorts; they do not
combine into one fresh, complete current-compiler admission package.

Select the next endpoint from the actual build graph below. Name its executing
owners, analyzed population, rule DATA, loaded implementation/toolchain inputs
(including emitter freshness bytes), options, grants, profile/target, dependency
exports and preservation contracts. Ordinary SH-07 implementation must not wait
for this evidence work. If a chosen construction fails within its bounds,
preserve the result before any successor. Use the bounded cost follow-up to
identify retained products or duplicate same-invocation work before choosing a
representation correction; do not raise caps, cache proof, prune required
sources or invent another wrapper/lane. All three groups remain open.

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

### Selected frontend subjects: first development result

The selected macro module now has a passing whole-source frontend check through
ordinary C5 and hosted Stage0 C6: one test / 19 assertions, 46 `defn`
expansions, 1,552 references and 1,678 Core nodes. The check compares original
authenticated C2 records with their C3 descriptor projection, expansion traces
and origins, and independent Stage0 reference expansion. It checks source
reference/binding joins, fresh C5 verification, then authenticated hosted C6
consumption with complete top-level source mapping and no source-form reread.
This analyzes the module's bodies; it does not execute all 46 functions or
establish Gravity C6, typing, effect legality or compiler migration.

The first five-subject attempt remains failed: five assertions passed before
one test projection assertion failed on the first macro subject. It incorrectly
compared C3 descriptors with raw C2 forms. Later assertions, hosted C6 and all
four other subjects were unrun. The corrected harness uses the owning
registered-literal projection and wrapped spans. Its separate non-authoritative
schema probe passed 15 assertions, including malformed source joins.

Use exact selectors in `gravity.self-hosting.sh06-resolution-adapter-test`:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh06-resolution-adapter-test --exact sh06-b-frontend-source-schema-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh06-resolution-adapter-test --exact sh06-b-frontend-macro-development-coverage
```

At that checkpoint, separate envelope, syntax, resolver and reader selectors
existed but had not run. The later envelope and syntax sections record their
actual results and supersede that unrun status for those subjects. Resolver and
reader remain unrun. Select one subject at a time when its coverage decision is
needed; do not repeat the unchanged macro product merely to run another
subject. All three partial groups remain open and the resolver remains
development-unadmitted.

The failed attempt took 221.827 seconds after 7.066 seconds of loading. The
corrected macro check took 330.949 seconds after 6.899 seconds of loading at
2 GiB; the cheap schema body took 12.141 milliseconds after 6.936 seconds of
loading at 512 MiB. These are distinct test intervals, not a speed comparison.
The source interval includes product construction, verification and consumer
checks, not just scheduling. Their internal cost breakdown remains unmeasured;
the bounded representation follow-up above is unchanged. No limits or
historical corpus/migration pins were increased or repinned.

### Envelope frontend composition: failed development check

The next selected subject, `compiler/authenticated_envelope.gravity`, ran once
at accepted development revision `512ecc51`. The existing direct selector
`sh06-b-frontend-envelope-development-coverage` completed with exit 1:
one test, 13 passing assertions, zero failures and one error. The independent
auditor confirmed the complete raw log and timing receipt. This is a failed
composition check, not a passing frontend subject or renewed admission.

The twelve pre-C6 guards and Stage0 expanded-form comparison passed. They cover
72 source-derived `defn` expansions, authenticated C2/C3 source joins,
trace/hygiene/origin links, C4 parent and reference/binding joins, and the first
fresh C5 verification. The observed C5 summary contains 74 definitions and
2,146 references; those counts are not function-execution coverage.

Hosted C6 then rejected at its fresh C5 verification gate, before lowering,
with `C6-VERIFY` and `[:fresh-authenticated-sh06-resolution]`. The six later
Core identity, verifier, source-map, no-reread and stability assertions did not
run. No Core summary was returned. The generic diagnostic does not identify
the failed verifier predicate or establish physically missing artifact data.
The first verification passed outside the test's source-reread spy; the failing
repeat occurred inside it. Its underlying verifier report was not retained,
so that interaction is a static diagnosis target, not an established cause.

Selected work took 328.198 seconds after 6.870 seconds of namespace loading
with a 2 GiB heap. The interval does not separate construction, verification
and scheduling costs. No bound was increased, no subject was retried and no
next subject or unchanged macro proof was run. SH-05.1, SH-05.6 and SH-06.6
remain partial; the resolver remains development-unadmitted.

That failed checkpoint required tracing the repeated C5 verifier and reread-spy
interaction before a corrected composition. The test correction and actual
result below supersede that pending action; both fresh verification gates and
authenticated consumption remain intact.

### Envelope read-spy correction and completed composition

Static tracing identified a concrete collision: the selected subject is also
the authenticated-envelope compiler owner. Fresh upstream verification can
legitimately compile that owner from the same file. The old path-only spy
could block that construction before C6 consumed its supplied artifact.
The original failed log does not recover its contained inner report.

The test-only correction in `sh06_resolution_adapter_test.clj` observes the
existing plan constructor and scopes its actual path/text pair to the current
thread during delegation. Matching owner reads still invoke the original
reader and are counted separately; direct subject parsing outside that scope
or with different text is rejected. Original constructors and verifiers are
unchanged. Nested and exceptional exits restore the observation scope. Failed
verification reports are returned unchanged with bounded scalar diagnostics.
This is specific test instrumentation, not a new production wrapper, cache,
filename exemption, authority flag or verification lane.

The direct normalized regression
`sh06-b-subject-read-audit-distinguishes-owner-construction` passed one test and
13 assertions at 512 MiB. It checks delegation and both constructor arities,
nested/exception restoration, direct and mismatched-read rejection, unrelated
reads, and preservation of a failed verifier report.

The corrected envelope selector at development commit `96aa2973` then passed
one test and 19 assertions. It covers 72 source-derived expansions, 74 C5
definitions, 2,146 references and a 2,530-node hosted Core result. All nineteen
preservation checks completed, including fresh verification and authenticated
hosted-C6 consumption. The observer recorded 30 owner-compilation reads and
zero forbidden subject-read attempts: this is not zero I/O from that file.
The selected work took 384.109 seconds after 6.701 seconds of loading at 2 GiB.
The normalized and source checks are different workloads, not a speed comparison.

No production compiler semantics changed in this correction. It does not
execute every envelope function body, establish Gravity C6 execution, close
the remaining syntax/resolver/reader subjects or negative criteria, or renew
authoritative migration. SH-05.1, SH-05.6 and SH-06.6 remain partial and the
resolver remains development-unadmitted. Failed V1 remains failed; no other
subject or unchanged macro check was replayed.

Construction cost remains separate from scheduling overhead. Before a future
necessary changed composition, inspect repeated owner-plan construction and
the fresh verifier calls in existing adapters `00042` and `00041`. Preserve
their public-boundary obligations; the present timings do not establish which
call dominates or justify an accepted-proof cache. Do not run an unchanged
composition merely to collect that breakdown.

The bounded read-only follow-up identifies two concrete repeated paths, not
their time dominance. `00042-sh06-c6-lowering-from-resolution-artifact.clj`
freshly verifies its supplied C5 artifact before delegating to
`00041-compiler-c6-lowering-source-artifact.clj`, which verifies that same
artifact again. The bounded C5 verifier in `00879` verifies SH05, reconstructs
the module request, reruns resolution and checks the expected envelope.
The SH02 binding path in `00750` compiles its envelope-owner plan when no
plan-emission context exists. Existing scoped binding reuse means repeated
calls do not necessarily imply repeated complete construction. In particular,
30 observed reader delegations are not 30 measured distinct plans.

During the next genuinely necessary changed composition, collect bounded call
counts and inclusive/exclusive intervals at those existing boundaries. Then
decide whether `00042` can delegate the mandatory fresh verification solely to
`00041` while preserving its kind rejection and the standalone public
consumer's verification behavior. That is a bounded proposed follow-up, not a
current change, verification bypass or accepted-proof cache. Any such change
needs malformed/stale input and affected composition checks. It must preserve
source, parent/dependency, profile/target/grant, verifier catalog, reader,
macro/resolver, rule DATA, emitter-freshness and seed/executor invalidation.
No wrapper, lane or unchanged proof run is needed for this assessment.

The frontend development helper now omits its additional standalone C5
verification before that public C6 call. Both production gates in `00042` and
`00041` remain unchanged and freshly verify the exact supplied input; either
failure prevents a Core result. The helper still checks returned C5 identity,
the Core verifier, complete source mapping, subject-read auditing and source
stability. This removes one test-only verifier invocation, not either public
gate or artifact construction. It introduces no cached acceptance or new flag.

The tradeoff is explicit: the earlier separately labelled freshness assertion
is gone. A bad artifact may reach the reference-form comparison before the
public consumer rejects, and that rejection may be a test error rather than
the old assertion failure. A future successful helper run has 18 assertions,
not 19; historical 19-assertion results are unchanged and are not relabelled.
The removed call does not establish a measured speedup or identify dominant
representation/construction cost. The large unchanged Core subject is not
replayed to validate this deletion.

The existing small regression batch passed three tests/74 assertions: 46 for
genuine two-function public-C6 consumption plus bad-ID and coherently changed
binding rejection, 15 source-schema checks and 13 subject-read-audit checks.
The original command exited 0 at 2 GiB; selected work took 56.777 seconds after
7.110 seconds of namespace loading. This checks the preserved public gates,
schema and auditing, not a fresh execution of the shortened large-subject
helper or a renewal of any earlier corpus result. No additional test lane,
wrapper or skip mode was added. All three partial groups remain open.

### Genuine source aliases: behavior and corrected span expectations

The new direct selector `sh06-b-source-duplicate-alias-composition` uses two
genuine library artifacts and a caller, not caller-supplied binding candidates.
Distinct `left` and `right` qualifiers resolve to the corresponding exported
definitions. Fresh C5 verification, authenticated hosted-C6 consumption,
surface-to-Core mapping and zero forbidden subject-read attempts passed.
The otherwise equivalent caller with two `duplicate` qualifiers rejects with
`C5-ALIAS` / `duplicate-namespace-alias`, both actual library artifact IDs and
namespace import records. Neither an accepted caller nor a Core result is
returned for the negative. Paired QST fixtures receive byte/parser parity only;
the three accepted artifact constructions and the rejected caller use Gravity.

The actual development run at `c4bf0039` completed with exit 1: one test,
42 passing assertions, three failures and no errors. All 45 assertions ran.
The failures were test expectations, not missed alias rejection: the effective
module constructor supplies authenticated header ranges under `:all` and
`:primary`, not a `:form-index`; public diagnostic transport adds a convenience
`:source` only to the outer span. Import candidate spans therefore equal the
public span's `:all`/`:primary` projection, not its complete map.

The future test now checks the actual header range against this fixture's
source and header byte extent, requires the primary range in the nonempty
range vector, and compares import ranges without the public convenience key.
It retains the separate source-path assertion, both actual dependency IDs,
nil body syntax ID and empty generated-origin chain. This header diagnostic
locates the complete namespace form, not each individual alias clause; no
body-reference or macro-origin locator is fabricated. The original aggregate
remains failed and the added assertion is not retroactively executed evidence.
The corrected future selector has 46 assertions. A separate read-only check of
the complete retained diagnostic passed one test/four assertions for the
outer source path, primary header extent, primary/all membership and both
candidate ranges. It neither rebuilt source products nor changed the original
42-pass/three-failure outcome.

Independent review accepted the qualified positive composition and negative
behavior, and identified the same oracle correction. Selected work took
166.573 seconds after 7.040 seconds of namespace loading at 2 GiB. These
intervals do not separate construction, verification and scheduling. No
production compiler change or unchanged source replay is indicated. The
remaining syntax/resolver/reader subjects and other explicit negative criteria
still need bounded coverage decisions. SH-05.1, SH-05.6 and SH-06.6 remain open;
the resolver remains development-unadmitted. No migration or release claim
follows from this composition.

### Syntax frontend composition

The existing `sh06-b-frontend-syntax-development-coverage` selector ran once
at clean development revision `e61b4fd9` and passed one test/19 assertions.
It constructed the distinct `bootstrap/syntax.gravity` subject: 102
source-derived `defn` expansions, 105 C5 definitions, 2,846 references and a
2,943-node hosted Core result. These are analyzed/expanded population counts,
not a count of executed function bodies.

All selected source-byte/C2/C3 joins, expansion trace/hygiene/origin checks,
Stage0 expansion parity, reference/binding joins, fresh C5 verification and
authenticated hosted-C6 identity/source-map checks completed. The observer
reported zero owner-compilation reads and zero forbidden subject-read attempts;
that is not a claim of zero filesystem I/O across construction. Independent
review accepted the complete scoped result.

Selected work took 585.898 seconds after 6.875 seconds of namespace loading
with a 2 GiB heap. Construction and repeated verification remain inside that
interval; no per-call cost dominance or speedup was measured. No macro,
envelope or unchanged proof product was replayed for this subject. At that
checkpoint, the resolver and reader subjects were unrun; the resolver result
below supersedes that status for its distinct positive composition only.
The selected closure's other explicit
negative criteria are not replaced by this positive composition. SH-05.1,
SH-05.6 and SH-06.6 remain open; the resolver remains development-unadmitted.
This does not execute the Gravity C6 implementation, admit the current compiler
closure, or establish self-hosting, seedless or release support.

### Resolver frontend composition

The existing `sh06-b-frontend-resolver-development-coverage` selector ran once
at clean development revision `4db0dc57` and passed one test/19 assertions.
It constructed the distinct `resolution.gravity` subject: 111 source-derived
`defn` expansions, 114 C5 definitions, 4,349 references and a 4,823-node hosted
Core result. These are analyzed/expanded population counts, not execution of
all 111 function bodies. The selected resolver source and test harness remained
unchanged through the run; the subsequent documentation integration does not
relabel the tested revision as a newer compiler snapshot.

All selected source-byte/C2/C3 joins, expansion trace/hygiene/origin checks,
Stage0 expansion parity, reference/binding joins, fresh C5 verification and
authenticated hosted-C6 identity/source-map checks completed. The observer
reported zero owner-compilation reads and zero forbidden subject-read attempts;
this is not zero filesystem I/O across construction. Independent review
accepted the full scoped result.

Selected work took 763.572 seconds after 6.771 seconds of namespace loading
with a 2 GiB heap. This separates the selected-test and loading intervals,
but does not isolate individual product-construction or verification costs,
identify a dominant operation, or establish a speedup. No completed macro,
envelope or syntax subject was replayed for this distinct composition.

This result supersedes earlier resolver-unrun statements only for the selected
positive frontend composition. The reader subject remains unrun, and the
selected closure's other explicit negative criteria remain outstanding.
SH-05.1, SH-05.6 and SH-06.6 remain open; the resolver remains
development-unadmitted. No Gravity-C6 body execution, current-closure migration,
self-hosting, seedless or release support follows from these checks.

### Same-target profile-edge source rejection

The distinct `sh06-b-source-profile-incompatible-composition` check ran at
development revision `8179c8e3` plus its exact test/fixture changes and passed
one test/27 assertions. It constructed a genuine accepted/verified meta/JVM
library artifact, then supplied it through the ordinary module-input API to a
hosted/JVM caller without an accepted boundary. C5 rejected the caller with
`C5-CROSS-PROFILE` and reason `:unsupported-or-impure-module-boundary`.

The checks cover the actual library's profile/target/capabilities, the caller's
active profile/target, dependency namespace/alias, authenticated namespace-header
syntax locator, wrapped header source span, structured remediation and empty
candidate list. A header locator is not a generated function-body macro origin.
The diagnostic's dependency record precedes enrichment with the actual callee's
metadata/identity; the test does not mistake that header record for the verified
library contract. G compiled through the source frontdoor; paired Q received
byte/parser parity only. No accepted caller or downstream C6 result is claimed.

Selected work took 53.028 seconds after 6.802 seconds of namespace loading with
a 2 GiB heap. This is one new negative composition, not the cross-target attempt
below rerun under a new label, and not complete profile/capability or closure
coverage. SH-05.1, SH-05.6 and SH-06.6 remain open and the resolver remains
development-unadmitted.

### Same-target capability-edge source composition

The distinct `sh06-b-source-capability-boundary-composition` check ran at
development revision `df73bc8d` plus its exact test/fixture changes and passed
one test/40 assertions. It constructed a genuine accepted/verified hosted/JVM
`gravity.io` library and a hosted/JVM caller declaring the required
`:io/write` effect and `:io/stdout` capability. The accepted caller's imported
binding, reference, origin and enriched dependency metadata join the actual
verified library artifact through the ordinary module-input API.

A second caller retained the effect declaration but omitted the capability.
C5 rejected it with `C5-CAPABILITY` and `:missing-import-capability`, with the
authenticated namespace-header syntax locator, header source span, alias,
structured remediation and empty candidate list. Its diagnostic dependency
record precedes enrichment: its empty capability/effect vectors and header
artifact identity are not the verified callee contract. The test checks the
actual required capability against the separately verified library instead.

Selected work took 89.058 seconds after 7.058 seconds of namespace loading with
a 2 GiB heap. All three G sources used fresh construction; the library's Q
fixture and two new Q caller fixtures received byte/parser parity only. These are import
metadata and resolution checks, not execution of IO, proof of provider or
deployment grants, downstream C6 execution, or current-closure admission.
SH-05.1, SH-05.6 and SH-06.6 remain open and the resolver remains
development-unadmitted.

### Target-edge source coverage limit

A bounded attempt at development revision `8179c8e3` tried to exercise a real
target-incompatible import: a fresh accepted/verified JVM library artifact
supplied to a WASM caller through the ordinary source constructor. The selected
Stage0 module parser rejected the caller first with `B1-TARGET-UNSUPPORTED`,
target `:wasm`, supported set `#{:jvm}`, and the caller's source/form-0 locator.
It did not produce a caller C5 compatibility diagnostic or dependency edge.

The attempted test failed: one test, 15 passing and 13 failing assertions,
zero errors. Selected work took 43.162 seconds after 6.826 seconds of namespace
loading with a 2 GiB heap. Passing checks on absent C5 fields do not establish
C5 behavior. Independent review confirmed the earlier reachability assumption
was wrong: tracing the C5 policy alone had missed the earlier target fence.
The exact attempted test/paired-fixture patch and full raw log/timing receipt
were retained separately before removing these root-owned uncommitted additions.
The failed attempt remains failed; no corrected source run was made.

The existing `parse-module-profile-and-target-checks` regression already covers
the Stage0 unsupported-target fence, so no duplicate test or target override
was added. The normalized C5 module-policy test still covers its own target
compatibility rule. Genuine cross-target source composition remains an explicit
gap under the selected JVM-only source frontdoor, not a demonstrated resolver
defect or permission to fabricate a module artifact, weaken the fence or expand
backend support. Revisit it when an actual target-support or stable migration
decision makes that composition reachable. All three partial groups stay open.

### Coherent macro-origin rejection at the C5 ingress

The existing
`sh05-expanded-syntax-and-coherent-outer-trace-substitutions-reject` test was
extended at development revision `88f9eb18` to exercise the public C5 consumer,
not only the macro owner's verifier. One genuine tiny `defn-multiple` Gravity
parent is reused for the original five verifier mutations and an accepted,
freshly verified C5/hosted-Stage0-C6 control. No parent is reconstructed for
each mutation and no compiler implementation changed.

The hostile candidate consistently changes the physical call-site source in
three outer views: expansion trace, generated-origin map and expanded syntax
origins. Those views agree with one another but disagree with the independent
retained owning run. This is not an all-field rehashed artifact. The public C5
consumer rejects it at its first authenticated-parent gate with
`C5-UNRESOLVED` and top-level missing field
`:authenticated-sh05-macro-parent`. Observations retain failed macro-owner
verification and a valid unchanged C3 parent. The source/form-0 locator is an
upstream authentication diagnostic, not a generated-body binding failure.
No accepted C5 or Core output is credited to the hostile candidate.

The selected test passed one test/32 assertions in 63.057 seconds after
6.714 seconds of namespace loading with a 2 GiB heap. The control consumes
genuine generated code through hosted C6; it does not execute the Gravity C6
implementation or a user function body. Only the Gravity fixture was
constructed in this selection; no QST construction is claimed. The reader
subject remains unrun, all three partial groups remain open, and the resolver
remains development-unadmitted. No current-closure migration, self-hosting,
seedless or release claim follows.

### Coherent binding-selection rejection at the hosted C6 ingress

The existing `sh06-authenticated-resolution-is-the-c6-consumer-boundary` test
was extended at development revision `c410fcb7`. It reuses one genuine tiny
`compiler-subset` C5 artifact and its clean hosted C6 control. The new hostile
candidate selects a different, genuine parameter binding in the same lexical
scope for one reference, consistently across three outer resolution views.
The binding population, reference identity/symbol/span and independently
retained request, run, upstream parent and proof/envelope records are unchanged.

The candidate's ordinary artifact ID is recomputed through the existing owner
function. Fresh identity and source-derived request-replay checks pass, while
the namespace-analysis, binding-table and resolution-table projections fail.
The embedded capability proof and SH02 envelope also disagree after the ID
change: this is not an all-field re-sealed artifact or a claim that only a
semantic check failed. It specifically exercises more than a bogus-ID case.

The original public hosted C6 consumer rejects the failed fresh owner report
with `C6-VERIFY`, stage `:core-lowering`, and top-level missing field
`:fresh-authenticated-sh06-resolution`. Its source/form-0 locator is an
upstream boundary failure, not an invented core-node or generated-body origin.
No Core output is credited to the hostile candidate. The existing delegated
read observer still reports no target-source parser reread during C6 calls;
this is not zero filesystem I/O across construction or verification.

The selected test passed one test/46 assertions in 61.008 seconds after
6.787 seconds of namespace loading with a 2 GiB heap. No fixture, compiler,
wrapper or verifier override was added. This establishes bounded hosted-C6
consumer rejection, not Gravity-C6 implementation execution, current-closure
migration or milestone completion. The reader subject remains unrun, all three
partial groups remain open, and the resolver remains development-unadmitted.

### Reader frontend composition

The genuinely unrun reader subject was selected at clean
`2a4ea770fa97892ae7ba7620655f2129a428d039`, tree
`550d82b1040172bde0f37be75709b9991c72373f`, for this development question:
does the selected defn-only frontend compose the complete reader source through
macro expansion, real C5 resolution and hosted C6? No earlier subject was
replayed and no test, fixture, inventory pin or compiler code changed.

`sh06-b-frontend-reader-development-coverage` passed one test/19 assertions,
zero failures/errors, at 2 GiB. The original command completed exit 0
(`6fcac2`, session `50112`, terminal `5f863b`). Raw output and the standard
non-authoritative timing record are
`/tmp/b-reader-2a4ea770-source-v1-20261019.log` and `.edn`.
Selected work took 1,647.946 seconds after 6.950 seconds loading the namespace.
The brief String-predicate focused batch ran concurrently in its separate
worktree; these observations are not isolated benchmarks or a speedup claim.

The actual source has 275 expanded `defn` forms and 23 `def` forms, yielding
298 definition records and 7,868 resolved references. The owner artifact is
`sha256:159b2cb1e2d4fc50e5cc0097df889c35701c18469b7ac74d56c7b718512341ec`.
Checks passed for complete source streams, original syntax/source joins,
introduced/reused origins, macro-parent identity, reference/binding bijection,
fresh C5 verification, prior-stage expansion parity and authenticated hosted
C6 consumption. The hosted result has 8,506 Core nodes, a passed verifier and
complete source mapping. The scoped parser observer recorded zero owner reads
and zero forbidden subject-read attempts during consumption; it is not an
all-I/O census. Source/test/toolchain pins and the clean worktree remained
unchanged; no Java survivor was observed after completion.

This completes the selected reader-subject development composition, not
execution of 275 function bodies or every path of its 231-function SH03 plan.
P15 compiler rules remain DATA, emitter bytes are a freshness input rather than
executed lowering, and hosted Stage0 C6 plus the declared seed/coordinator TCB
remain in use. Earlier unrun-reader descriptions are superseded only for this
recorded subject result. The three partial groups remain open for the complete
declared compiler population and applicable evidence; the resolver remains
development-unadmitted. No 69-file migration, full language, target, self-hosting
or seedless completion follows.

### Next compiler input: actual build graph, not directory counts

The existing ordinary pipeline reaches a stage named `:checked-core` through
`gravity/authenticated_artifact_pipeline/01-pipeline-schema-version.clj` and
`04-envelope-valid.clj`. Its original `sh07-core-from-resolution-artifact`
route in component `00898` uses `00885-sh07-core-source-relative-path.clj` to
compile `bootstrap/gravity/src/gravity/checked_core.gravity` with the existing
raw3 seed and authenticate its source, exports and header. Component `00894`
then executes `sh07-build-core-template` and the owning verifiers. That makes
`checked_core.gravity` a genuinely required additional analyzed subject when
extending this endpoint to Gravity-owned Core lowering, not every file in the
69+5 inventory. Analyzing that source is distinct from executing it on accepted
and rejected Core requests. The facade's stage name is not C1's fully checked
MIR input: `00898` still records C7 through C10 as pending.

| Population | Actual endpoint and classification |
| --- | --- |
| `checked_core.gravity` | Required for the selected ordinary Gravity-owned Core endpoint; one next source-composition question, with exact census and bounds determined before execution. |
| `compiler/c6_call_arity_legality.gravity` | Separately executable B48/test and SH25 subject, not imported by that ordinary root; its header makes no public-routing claim. |
| C7 engine, `effects.gravity` and the C8 compatibility module | Required when the endpoint asks for typed/effect-checked facts; actual C8 wrapping requires `gravity.effects`. Not automatically executed by hosted C6. |
| P15 emitter, `gravity/runtime.gravity` and P15 runtime compatibility source | Required for their actual lowering/execution endpoint; frontend emitter freshness alone does not execute them. |
| C6-C10, MIR and later backend owners | Separate explicit routes: `00701` selects the C6-C10 bridge; `00726` contains the C11/MIR source declaration at line 149 as well as earlier verification code; `00733` binds MIR and `00743` consumes it. Later C13/C14/B1 bindings use `00748`/`00750`. Select the real route, not a filename or declared module record. |
| W5, build and meta additions | Conditional standalone executors/record validators, not imports into the inspected ordinary roots. Their own claims require their actual inputs; supplied records do not establish compiler builds, rebuilds or execution. |
| `stdlib/self_hosting_core.gravity` | Explicitly experimental; runtime-provider/full-library integration remains pending. |
| Seven B51 sources | Held outside this endpoint under the existing scoped ownership, not silently excluded to manufacture acceptance. |
| Other targets and P15 native sources | Optional only relative to the present JVM endpoint; required for their own future target/native claims. Native plan emission and runtime-driver DATA/contract paths are separate, not current all-target execution. |

This bounded call trace found no newly required unsupported macro. It does not
declare a complete 74-node execution graph or authorize a larger proof run by
count alone. Choose a concrete next source/semantic question, reuse valid
unchanged evidence where applicable and retain every actual obstruction.
Authoritative migration separately needs its deliberately selected coherent
compiler/source/toolchain/provider/target cohort and required fresh provenance,
equivalence, bootstrap and independent-review evidence. Ordinary semantic work
does not wait for that admission decision.

### Checked-Core source census before another composition

A single reader-only Clojure query at clean commit
`2efd518b4f4482ce3fce2752e2f30d26bfb20f17` inspected the unchanged
537,749-byte `checked_core.gravity` source (SHA256
`511e715e66f12cc3945e177ef667d073ec6294166e1140d548f3152d933e7337`).
It used `*read-eval* false`, empty custom readers and inert tagged-literal
preservation, without loading a Gravity namespace. The actual query exited 0
at 512 MiB and observed 405 top-level forms: one namespace, three `def`s and
401 `defn`s. There is no namespace `:requires` clause. All observed executable
sequence heads were in function bodies; the three initializers contain DATA.
The census separated 64 quote boundaries from executable positions and found
no newly required unsupported macro head. This is host-reader inventory, not
Gravity-reader agreement, accepted C2/C3 products or execution of 401 bodies.

Source bytes fit the existing 1 MiB source gate, but do not establish recursive
form, expansion, reference, carrier or owner-specific bound fit. The next
concrete question is whether this one source composes through the existing
C2/C3/macro/C5/hosted-C6 path with original-source joins and reference
equivalence. That question is separate from executing the Gravity-owned Core
builder on accepted and rejected requests. Select its existing direct helper
and bounded cost before running it; neither this census nor a directory count
authorizes a larger corpus, bound growth or an unchanged proof replay. All
three partial groups remain open and the resolver remains development-unadmitted.

### Checked-Core composition: incomplete bounded attempt

Commit `b99989e30a64690c69f614839e3fac3af4d16ae6` selected this one
source through the unchanged frontend helper. One actual direct invocation used
2 GiB on 2026-10-19, from 08:50:41 UTC until the coordinator stopped the owned
JVM after the preselected 09:50:14 UTC monitored ceiling. Identity was checked
at 09:50:19 UTC before TERM; the original tool returned exit 143 and the PID
was absent afterward. This was a coordinator interruption, not an observed
semantic rejection, OOM, verifier failure or completed test pass. The ceiling
was monitored, not an independently enforced hard invocation deadline.

The retained log reached `:c5-after` with 404 definitions and 20,209 references,
then `:hosted-c6-before`. It contains no returned hosted-C6 summary or final test
counters, and the requested timing receipt was absent. Reaching those printed
boundaries does not establish that all assertions passed. The original source
and test hashes were unchanged after termination. Raw output remains at
`/tmp/b-checked-core-b99989e3-source-v1-20261019.log` (SHA256
`d83290fad2ed7e6864574cb25c44f6c4b6982e398d40c8310e19efc2f56c1c28`);
the selector and exact tested inputs remain recoverable in the original commit.

The new four-line selector was removed in a subsequent normal commit rather
than adding a newly unvalidated, potentially hour-long default regression.
The existing helper, tests and compiler are unchanged. Checked-Core remains a
required, open source-composition question for the selected Core endpoint;
removing this selector neither excludes it from that closure nor closes any of
the three partial groups. There is no unchanged retry or bound increase here.

One one-second OS sample during construction exited 0, but its mostly unknown
JIT frames were inconclusive. Neither that snapshot nor these overlapping-run
timings establish a dominant owner, allocation/GC cause or speedup. The next
action is the bounded existing-helper cost follow-up described above: separate
artifact construction from redundant test-only verification and public consumer
verification before selecting another concrete experiment. Any representation
redesign remains outside ordinary SH-07 development and requires its own scoped
decision; no cache, verifier skip or admission wrapper is introduced here.

### Bounded invocation-local projection sharing

Development commit `2f6a7fb521d8fd139bac0825b49e1d19858452c9` removes one
concrete duplication in the ordinary C5 adapter. The constructor and bounded
verifier each compute the binding-ID and span-neutral resolution-row chunks
once locally, then use that pair for identity input and envelope summary.
Public one-argument helpers still recompute freshly; both fresh C5 verification
gates on the public C6 path, request replay, complete projections and envelope
verification remain.
No pair crosses invocations or the producer/verifier boundary, and no wire
field, public override, accepted-result cache or new load fragment is added.

The source-derived reduction at each paired site is from
`2 * (ceil(binding-count/16) + ceil(row-count/16))` inner chunk hashes to one
such pair. Chunk domains, ordering, duplicates and canonical identity/summary
equations are unchanged. This is deterministic duplicate-work removal, not a
measured elapsed-time, heap or speedup result or the cause of the interrupted
Core attempt.

The selected bounded check passed seven tests / 302 assertions, including old
equations at 0/1/15/16/17/31/32/33 entries, genuine tiny C5 -> public hosted-C6
acceptance, coherent alternate-binding rejection, fresh candidate-local work,
stale envelope/projection mutations, preflight bounds and source-read auditing.
A separate language-boundary check passed six tests / eleven assertions.
Both original commands exited zero; no test remained unrun and no retry or
bound increase occurred. Independent code and actual-result review accepted
this scoped development change. The interrupted large-Core attempt above was
not rerun or relabelled. Its outstanding source-composition question, the three
partial groups and resolver development-unadmitted status remain unchanged.
The changed adapter revision is a new toolchain input to assess at an explicit
migration decision; equivalent equations do not renew historical provenance.

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

SH-07 development revisions can change the P15 emitter, which is a selected
toolchain-freshness input even when a frontend check does not execute its
lowering body. Each result retains its actual tested source/toolchain cohort;
its authority is not renewed for a later fingerprint.
Ordinary development does not replay those products just to refresh a stamp.
A concrete stable-revision admission decision must select the intended cohort
and satisfy its applicable invalidation and fresh-evidence requirements.

A stable migration decision still requires its fresh provenance, equivalence,
bootstrap and independent-review evidence under BOOT3/D9. None is renewed here.
Keep historical 36/37, later SH-07 41/42 and present 69+5 inventories distinct;
do not rerun an obsolete full corpus to rediscover known count drift.
