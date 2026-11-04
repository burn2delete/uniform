# Macro owner-call evaluation order

## Governing contracts and exact defect classification

D1/D2/D3/D8/D9, L4/L12/L16, C1/C4/C15 and TEST2 govern this correction.
The source parent is unmerged native-report PR1185 at
a1c019b2f480c0df43282846e98cd2dc6be07b14, tree
82f4917258b74aa116314688c61e7d5b840a6094. Parent acceptance/CI does not
transfer to this correction. Observed actual main is
3c97ad75341c5ef6506cf1bba050b9017301695b.

Independent isolated Clojure review compares the exact old global operations
in 7f83c764431529e475450302c81e59bb420683de with the extracted Macro
operations. Old component blobs are 00857 d8aa3569ce3450b9f75f639852148cd78e61e7db,
00858 1ecb9faeab79578ba9ae2c732592aba9698f29ef and
00859 c3a8714b966b3edeb17c90ab9d4dabccd913e429. Before extraction, an
original global function call selects its function before evaluating arguments.
A local Var invoked as an IFn resolves its root after arguments, which can
select a different owner after a nested callback changes that root.

All four affected complete bodies are byte-identical across actual main3c97,
native sourcea1 and provider4e55c8a050572a39145466dacb83663f7a53d898.
Main Macro blob eee70be1344c674fb4408dcfd2ee31fc052a22dc itself reproduces
these defects in the independent isolated harness. The native source/provider
Macro blob 0ed0fb7e4df367c101df48ea07e589baa50e2318 adds the separate native
report pair and contract. Thus these are pre-existing Main defects from the
historical extraction, not introduced by the new native-report ports. Components
00028/00029 did change for that pair, including its earlier require; the comparison
claims equality of these four operation bodies, not whole-component equality.
The scoped native-report acceptance remains valid; no full-Macro acceptance is
claimed. Classification and independent old/current controls remain preserved
under /tmp/uniform-macro-owner-calls- and /tmp/uniform-macro-owner-order-review.*.

## Corrective boundary, API and artifacts

Correct only boundary-fail!, execute-plan!, raise-rejection! and
contain-operation!. Capture each formerly global callable Var in operator
position before argument evaluation at each use; preserve ordinary function
values and subsequent-call rebinding. Preserve public names/arities and the
explicit operation maps. Do not snapshot at entry, add callable/type admission,
choose fallback owners or change the input data/configuration port.

Inputs are existing C4 rule facts, diagnostic templates/digests, native arguments
or a supplied unit operation plus trusted owner ports. Outputs are unchanged C4
refusals, native results or contained host diagnostics. Preserve full argument
maps, source/span/version fields, supplied identities, paths, exception identity,
returning failures and original evaluation order. No identity preimage, artifact
admission, source provenance, cache, source IO or plan selection changes.

Macro owns rule and native transport/containment sequencing; Bootstrap retains
source/authentication, configuration, diagnostic construction/publication and
orchestration. Dependencies flow through explicit supplied operations; Macro
imports/discovers no Bootstrap owner. The native-report pair and all other Macro
bodies remain unchanged. The ordinary operation parameter to contain-operation!
keeps its original local-call semantics.

Macro intentionally contains Throwable after handling interruption and known
C4 ExceptionInfo separately. Preserve that broader boundary, all existing
execute-plan! catch classes, direct interruption propagation and exact known
ExceptionInfo rethrow. Do not copy Syntax's narrower error behavior.

## Tests, acceptance and writer coordination

Retain failing regressions for span/input mutation of the C4 owner, nested
execution and stripping mutation, runtime cause-message mutation, rejected-result
span mutation, and Throwable cause-message mutation. Ordinary function-valued
ports, next-call roots, known diagnostic identity, returning failures, interrupt
state and complete native argument/provenance controls must pass. Compare full
native records on actual paired source paths, not hash/count summaries alone;
unit runtime ports do not count as authenticated Gravity execution.

Run focused tests, static conservation, native before/after equality, affected
native compatibility checks, boundary/ownership, contextual lint, validators,
independent semantic/adversarial acceptance, own hosted exact-source/base CI and
immediate clean integration preflight. Finish clean commits and maintain the
initial draft. No implementation credit attaches to this scope-only record.

A fresh actual three-path source writer owns Macro, owner_calls_test.clj and
this evidence. It does not own tools/ci/run.clj or the namespace index. The
original Root C6 actor holds the actual root-c6-ci-handoff scope; preserve it.
CI wiring/index updates remain pending until the applicable writer ends normally
and a fresh legitimate claim succeeds. Continue disjoint source work without
forcing, transferring or reusing leases, interrupting valid checks or imposing
candidate quotas. No Main/controller operation is authorized by this source claim.

## Unsupported boundaries

This does not verify every other operation port, complete Macro or canonical C4,
authenticate compiler/source products, execute source or held private routes,
renew pins/migration evidence, prove safety or establish release, self-hosting or
seed retirement. Native report/readiness data remains non-authoritative. No
unmerged authentication code or retained dirty patch is adopted.

## Corrective implementation and focused regression

The implementation corrects 16 formerly global callable uses across the four
scoped operations through one private owner-function helper. Each Var is
resolved before that call's arguments; other callable values are retained.
The input/configuration port and supplied operation parameter are unchanged.
All 52 public APIs, the newly extracted native-report pair, other Macro bodies,
Bootstrap files, authentication, source pins, manifests and CI remain unchanged.

The prepatch regression runs 11 tests / 42 assertions with seven failures and
zero errors: two span-dependent diagnostic routes, input-dependent boundary,
outer cleaning, inner runtime selection, runtime cause message and contained
Throwable cause message. After correction all 42 assertions pass. Controls
retain next-call roots, ordinary function-valued ports, native diagnostic facts,
exact exception identity, Macro's broader containment, existing execute classes,
fatal propagation from execute, and interruption flags captured before reporting.
The initial test draft had excess closing delimiters; that preparation nonpass
is retained separately and has no production-failure credit. Direct lint is
zero warnings/errors. No source compilation or authentication was executed.

A clean prepatch capture retains 40 full records on two actual paired fixture
paths across Var/function ports: eight native returns, 28 constructed C4
diagnostics and four exact known-diagnostic propagations. Source text/hash,
paths and full native result/diagnostic data are preserved; all runtime ports
are marked explicit unit controls. Metadata-aware EDN SHA256:
3583b32416da3feb5e0af7faf47d6ed08468bba5ca5639147f2c9b6793ffd259.
Clean candidate 411e6bf396b4630b12dba870c0ab516fb86f068c, tree
49a20d0f37755eb686fd63b4c8e891a2a1b35fb9, reproduces all 40 records byte
for byte and as parsed data with that same SHA256. Constructed diagnostics and
known-diagnostic propagation remain separately counted.

The combined native/report/API-inventory/runtime-control regression passes
20 tests / 197 assertions. Boundary/ownership passes 48 / 1198 with no skipped
namespaces. Contextual lint against source parent a1 covers the two changed
Clojure paths with zero baseline warnings and zero blockers; this is local
correction validation, not newer-main composition. Document, roadmap and
governance validators pass. Cold load retains 52 public APIs without Bootstrap.

The conservation helper reverses exactly the 16 operator changes and removes
only the new private helper and scoped contract, reproducing the entire parent
Macro file byte for byte. It proves the native-report pair, all other Macro
functions, input/local-operation calls, all Bootstrap/authentication/pin files,
CI and namespace index unchanged; exactly three source-slice paths differ.
Independent corrective review and outstanding CI/index handoff/wiring, hosted
exact source/base CI and current-base clean integration remain pending.

## Reviewed native-fix composition and CI wiring

After the separate native-report correction was independently accepted, normal
merge c41c1f6f0b4aa29e6b1f797a190dfa0bfe23ffc7, tree
b8fc2c0e8974d2e67fe5d6e6eb854deada5dbce6, preserves both parents:
61fbdc7c33cbcbbf0432cb65f1d6f87914eafe9a and
f8ee2eff8951a8337bee616107cda5cb152ff062. The four incoming Native paths
are the retired redundant late require, earlier-load ownership test, corrected
Macro CI coverage controls and its evidence. No Macro operation body or owner
test changes in that merge, and no source/authentication or controller authority
is adopted. The earlier failed Native run41928851212 remains NONPASS; its
fresh corrected run41933922570 remains separately bound to old Main3c97.

The owner-call namespace is now added once to the existing Macro cohort beside
pipeline and native-report tests. The CI harness compares the complete exact
namespace vectors, checks that all eleven owner deftests are selected, and
demonstrates that omitting the namespace loses its selection. Native-report
coverage and the original disjoint receipt partitions/mutation controls remain.
The exact 256MiB real-linter harness passes 49 tests / 522 assertions. Combined
native/report/API-inventory/runtime controls pass 20 / 197; boundary/ownership
passes 48 / 1202 with no skipped namespaces. Direct lint is zero warnings/errors.
Own generated harness outputs are preserved recoverably outside the checkout
after the process ended, with all bytes/hashes retained.

A fresh six-path source-composition/CI/evidence writer owns only those actual
paths. Original source and Native corrective tokens remain normally released;
no token is transferred or reused. The namespace index is genuinely held by
the active root-pr918-after1179 writer, so no index edit is made and its link
update remains pending a normal handoff. The scoped contract already identifies
the new test owner. Source metadata/CI wording from earlier phases records the
state when those phases ended; it is not fresh integration authority.

Current Main29711aa8532157a1d9c38c0b2df02d57957969ee separately integrates
the controller-owned SH21 change. Only actual Main was fetched/read; that
automated candidate, PR, leases, state and credit remain with its owner. This
source branch has not composed those Main changes. At clean wired source
341d6f0e3ff400759dfc31d2c79f347476299a3d, tree
2149ec7bd2414ca61122243813bb1ae04d4fc4a3, all 40 complete native records
still match the original raw/parsed data and SHA2563583b32416da3feb5e0af7faf47d6ed08468bba5ca5639147f2c9b6793ffd259.
The complete 54-path source context against captured Main297 passes with
3459 unchanged baseline warnings and zero blockers, explicitly excluding the
nine known Main-only paths that a provider would retain from Main. It is local
source validation, not hosted provider or current-main integration credit.

Conservation verifies the normal merge's exact ordered parents and four Native
blob/mode additions, all other prior owner source blobs, all 16 operator fixes,
the native-report pair and other Macro bodies, the early-load correction and
ownership tests, and the single CI namespace insertion. The complete owner slice
relative to Native f8 is five paths, with CI harness changes reviewed separately;
the namespace index is byte-identical and remains pending. An initial local
observer delimiter/forward-declaration preparation error is retained separately,
without production-failure credit. Formatting and document validation pass.
Independent composition/wiring acceptance, fresh fully wired exact-source/base
hosted CI and immediate clean integration remain pending.

## Namespace-index handoff

The six-path composition/checking writer ended naturally and its own token was
normally released, with actual row absence recorded at
/tmp/uniform-macro-owner-calls-wired-source-frozen.edn. The former index holder
also ended naturally. Fresh current-registry inspection found no conflicting
writer, followed by an own two-path claim for the index and this evidence only.
No prior token or unmerged PR918 source/index content was adopted.

Only the Macro row now links the corrective evidence and test namespace and
records diagnostic/runtime function selection before arguments, existing known
diagnostic identity, interruption and broader Macro Throwable containment.
All other namespace rows, APIs, dependency directions and authority remain
unchanged. This closes the link update recorded as pending in earlier phase
snapshots; it is documentation of the already tested four-operation boundary.
CI wiring and its original source/code checks remain unchanged. Review of this
documentation-only delta and fresh fully wired hosted CI/current-base clean
integration remain pending.
