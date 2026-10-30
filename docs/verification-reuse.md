# Repeated verification cleanup

This is the detailed backlog and acceptance record for the verification-reuse
outcome. Stable IDs below belong to this cleanup, not language completion.
The shared cleanup navigation lives in the component roadmap index.

## Governing contracts and inventory

[D6](phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md),
[D9](phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md),
[C18](phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md)
and [TEST2](phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md)
require surviving evidence, invalidation and independent verification.
[Development verification](development-verification.md#explicit-stage2-runtime-execution-attribution)
already distinguishes original producer evidence from its later application.
Saved reports and equality of logical parents do not confer authority.

Inventory base: `176c4ee65bb3ceb545f51edfaa599902030a0cb7`.
Inspected interfaces: Resolve construction inputs and its public verifier;
Core construction verification tests; the incremental dependency contract,
planner, child cache wiring and explicit input revalidation; SH15 diagnostic
case construction; current Native/MIR source/composition receipts.

Current PR494 assessment: **2026-10-27 18:47:24 UTC**, on tested source
`8204a761c85aa3d04c8772f73b865698e30a3143`, tree
`8e5160f9e1770647b9a24877d304c8196ba89def`, based on merged main
`bffefedfada9e3312e68e0a28a69d71db459d0e2`. The original inventory is unchanged.
RV1 closure remains withdrawn and its separate correction is held; this
facade-batching result neither resumes nor accepts that work.
The statuses below describe this cleanup's acceptance criteria, not compiler or
language completion. Earlier increment receipts had accumulated without a dated
reconciliation for the dashboard collector; this assessment corrects that gap.

| ID | Exact boundary and files | Dependency and acceptance | Current disposition |
| --- | --- | --- | --- |
| RV1 | `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/resolution-construction-inputs.clj`; later Resolve construction proof and public verifier | Any additional Macro check elision requires transitive live-plan, dynamic-dispatch and AE identity stability, plus mutation observation across the whole proposed reuse seam. Same logical parent is insufficient. Preserve error order, fallback, invocation/transaction isolation and fresh public/tamper checks. | **Partial.** PR396's construction reuse is present at `d00cc2d5`; PR469/477 cover their named necessity decisions and PR480/483 the structural calculation and load repair. PR493 at `e5d0bacd` closes the concrete after-Resolve parent-mutation control with genuine positive/public verification and rejection evidence. It does not prove whole-report equivalence or every retained repeat necessary. Transitive live-plan, Reader invocation, AE/dispatch and closed-over-state equivalence, or a complete necessity decision, remain open. |
| RV1-LDR | `sh01_development_loop_wiring.clj` and its test under `bootstrap/clojure/test/gravity/self_hosting/` | Establish a conservative resource-dispatch boundary for context and compiler-bound ClassLoaders before startup lookup or cache admission. Resource mutation, URL addition and unknown-delegation controls must preserve fresh fallback. This is a runtime prerequisite, not Macro check-elision authority. | **Closed for this resource-dispatch prerequisite.** Reviewed fallback and controls integrated through PR452 at `7ae18ee6`. This supplies no in-memory class identity or whole-RV1 proof and is not an additional original outcome counted toward a percentage. |
| RV2 | `contracts/stage0-incremental-test-dependencies.edn`; `sh01_component_test_dependencies.clj`, `sh01_impact_test_planner.clj`, `sh01_development_loop_wiring.clj` and the latter two test files under `bootstrap/clojure/test/gravity/self_hosting/` | Re-audit the pure Reader artifact-identity facade, five nested files and test; bind existing runners/contracts/classpath/inventories/shadows/runtime. Demonstrate a real test producer, unrelated docs/test-body reuse with original provenance, consumed-input invalidation, and preserved fresh policies. | **Partial.** Corrected Reader closure integrated through PR432 at `6e49d6f8`; PR461 at `e93322ef` supplies the actual supported-leaf miss/hit/invalidation proof. Both bootstrap-dependent compatibility selections still need complete runtime read/effect closure, a reviewed representation and separately attributed producer/reuse/invalidation evidence, or proof that their repeats are necessary. Full-repository fallback and audit inventories do not close that obligation. |
| RV2a | Same dependency contract; `docs/self-hosting-slice-ownership.edn`; planner test | Four existing non-slice namespaces block catalog construction on the inventory base. Add exact source-grounded owners and fresh policies for parameter Reader invocation, keyword scalar controls, staged Reader closure and token spelling. Select their existing bodies without running them as cache controls. | **Closed for the four-namespace catalog correction.** Source-grounded owners, freshness and conservative execution dispositions integrated through PR432 at `6e49d6f8`. No residual catalog gate for those four namespaces; their component/source authentication acceptance remains separate. |
| RV3 | `bootstrap/clojure/test/gravity/self_hosting/sh15_diagnostic_boundary_test.clj` | Reuse one genuine immutable upstream case for the same diagnostic reference tests; preserve independently constructed changed-source/ordinal cases and resolved verification. | **Closed for the scoped reuse change**, integrated at `c3f35fef51a7f729c65689e4d68e5a3adb0c7f44`. Current source retains the representative delay, delayed references and explicit build counter; later accounting-test edits did not change that region. This assessment ran no C12 work and grants no current C12/product admission. |
| RV4 | RV1-RV3 records, exact candidate diff, execution receipts and independent review | Close each inventory row by integrated regression evidence or a source-grounded necessary-check decision. Preserve failed, unrun and stale boundaries. | **Partial.** PR461's supported-leaf three-phase evidence step is closed on its original `9db7e9f3` tuple; finite merged increments have independent reviews and attributed receipts below. Final whole-outcome closure still requires RV1 and RV2's remaining acceptance, regression evidence, independent review and integration. No saved PASS or counts replace those gates. |

Numerical reporting: **50% remains the inherited provisional estimate from
2026-10-26 04:11, not a current coverage measurement.** This review supplies no
new measured percentage. The original criteria have no agreed weighting, and
PR totals, assertion totals or the number of closed rows are not a defensible
conversion. Recent RV1 increments advance bounded subcases but do not close its
whole-report criterion; bootstrap compatibility remains unfinished. The current
assessment is the dated disposition and residual acceptance above.

Accepted merged advances are retained with their original scope: PR432's
corrected Reader closure/catalog, PR440's removal of false compatibility
declarations, PR441/443's explicitly incomplete audits, PR448/451/452's safe
fallbacks, PR461's actual supported-leaf evidence, and PR469/477/480's bounded
RV1 work. PR483 at `c0c7f9c363c709827e95f166e922fe9d1c07caf2` fixes PR480's
Bootstrap regression; it is a repair, not another closed original criterion.
The detailed sections preserve exact source/merge identities, failed attempts,
tested scopes and independent reviews. PR493's mutation control is merged;
the compatibility initialization candidate below is unfinished and must remain
draft until its real C11 acceptance blocker is resolved.

At the earlier **2026-10-27 04:32:34 UTC** assessment, unpublished work remained
separate: the reserved
`resolution_parent_mutation_test.clj` candidate and SH06 fresh/unsupported
execution metadata are in progress. Independent review accepted the proposed
whole-boundary test scope only; the exact harness/code/result are not accepted.
Two cheap metadata/admission controls passed 72 assertions on the unpublished
working candidate based on `c0c7f9c363c709827e95f166e922fe9d1c07caf2`; they
are not current-main semantic coverage. No genuine current Reader/C5 mutation
process has launched. The acceptance gate is a genuine
current Reader-to-C5 positive and independent public verification, then rejection
of the same aliased parent after actual Resolve execution, followed by review
and integration. Even that concrete negative does not alone prove every repeat
necessary or close whole RV1. Existing reservations and uncertain Reader
message operations are preserved. The subsequent genuine result is recorded
below; this dated reconciliation itself launched no test or
compiler replay and does not write the dashboard.

## Exact Reader leaf closure

The current Reader identity catalog policy requires fresh execution while the
expanded lexical closure lacks a real producer evidence bundle. Its explicit
input declaration is retained for audit and regression checks, but the planner
does not authorize narrowed cache reuse. The earlier producer and reuse results
below retain only their original source/input scope.

The opt-in is only `gravity.c2-artifact-identity-test`. Its leaf production closure is
`bootstrap/clojure/src/gravity/c2_artifact_identity.clj` and
`c2_artifact_identity/{artifacts,canonical,incremental,operations,projections}.clj`.
These files use core collection operations and supplied operation callbacks.
The test supplies deterministic hash/graph/span callbacks and literal carriers;
it does not load `gravity.bootstrap`, read Gravity source or authenticate C2
products. Its existing assertions explicitly reject those authority claims.

The consumed runner dependency `bootstrap/clojure/src/gravity/digest.clj` is
also explicit: both the wiring and cache load it to compute identity hashes.
Changing its implementation must invalidate reuse even if its hash outputs
are preserved. All test code, runner code, ownership/backlog/dependency contracts and
`deps.edn` remain consumed inputs. Existing classpath ordering, `.cljc` and
compiled shadows, data readers, class inventories, runtime executable and
effective child configuration checks remain. The actual test JVM still starts;
only an unchanged complete selection may reuse a non-authoritative result.
The original producer revision and applying revision remain distinct.

Unrelated documentation or an unrelated existing test body is outside this
closure. A governing contract or any consumed test change invalidates it.
Adding/removing a dedicated test path changes the catalog inventory and also
invalidates it. This is no blanket exemption for docs or test changes.

## Necessary fresh work and remaining scope

The standalone current-source Reader, Core, MIR and Resolve controls remain
fresh. The compatibility test closures retain the complete-repository fallback:
their bootstrap/component loader dependencies have not been re-audited for
narrow reuse. Undeclared selections also execute fresh. This is conservative
invalidation, not a proof that every such repeat is necessary; further
source-grounded duplication discoveries remain work for this outcome.

Native/MIR owners retain their serializer, harness and source validation. The
failed PR421 source timeout is a nonpass on its original tuple. Changed Reader
and canonical dependencies justified that attempt; unchanged docs do not turn
it into accepted evidence. Public final-report/source-pin admission remains
open. C12 execution and the unresolved C7 publication operation remain outside
the authorized actions here.

## Completion and evidence

100% requires every inventoried redundant computation to be eliminated with
integration/regression evidence or proved necessary and classified outside
redundancy. It does not mean zero verification, a closed compiler pipeline or
a complete language. Unproved reuse prerequisites, unexplored conservative
fallbacks, independent review and integration are open boundaries, not credit.

The accepted original finite advances are the Reader leaf closure and fresh
namespace mappings in PR432, removal of obsolete compatibility declarations
in PR440, and the SH15 immutable-case reuse already integrated at `c3f35fef`.
PR448, PR451 and PR452 close specific unsafe startup/resource reuse routes.
Those safety corrections are prerequisites, not additional compiler or language
completion. These milestones do not support a measured percentage.

The remaining original criteria are finite and separate:

1. RV1 needs the transitive live-plan, dynamic-dispatch and AE stability plus
   whole-seam mutation proof required for another construction-check elision,
   or a source-grounded decision that each retained check is necessary under
   the current interface. The recursive callback event schedule and public
   embedded-proof comparison below are explicit necessity decisions; the
   remaining Macro checks stay active.
2. RV2 still needs the read/effect and representation closure for the two
   inventoried bootstrap-dependent compatibility selections, or evidence
   proving their retained repeats necessary. A full-repository key is the
   conservative fallback, not proof that every documentation edit is semantic.
3. RV4 now has independently reviewed current supported-leaf real
   miss/hit/consumed-runner invalidation evidence on `9db7e9f3`, recorded below.
   Closing the whole outcome still requires reviewed, integrated acceptance or
   necessary-check decisions for the remaining RV1/RV2 rows. Earlier real
   results retain their original tuples; none is relabeled as a newer execution.

The three-phase pure Reader leaf observation completes this RV4 evidence step.
The private harness's missing-value and owned-child cleanup findings were
corrected and independently reviewed before launch. Its first report-path
nonpass and the corrected actual results remain separate below. This increment
consumes an original acceptance criterion; it adds no new hardening family.

The finite Reader closure/mapping increment is published in
[PR #432](https://github.com/burn2delete/uniform/pull/432). Independent code
review and separate actual-result readback found no blocking finding on
`6159eb721840e33fdda465f0d5dcd8a921415598`, tree
`123d1fc50b787cf51b0a08c79a4db53830d4b96b`.

| Observed scope | Result |
| --- | --- |
| Planner on the tested source tree | 59 tests / 518 assertions, zero failures/errors |
| Wiring, including all five nested-input invalidations and provenance controls | 43 / 398, zero failures/errors |
| Documents; language boundary and LOC gates | 240 documents / 19 indexes; 11 / 23, passed |
| Actual child producer, 256 MiB under the existing broker | 18 / 930, natural exit 0, non-authoritative |
| Unrelated documentation and existing test-body changes | Two child launches, both hits, `tests-executed? false`; original producer provenance retained and applying snapshots distinct |
| Changed consumed contract | Cache miss and real fresh 18 / 930, natural exit 0 |

Temporary control edits were restored and clean exact head/tree bookends
checked. Hit assertion totals describe the original producer, not newly run
assertions. The failed first harness report path, preliminary baseline command
error and initial catalog failures remain failed/unrun history. An unchanged
base one-test control reproduced the four orphan namespace catalog error;
their bodies were not executed for this mapping repair.

Normal reconciliation with main `48f81c4bf2d7e7ecb01905949586253b797aaf98`
produced `101d3bb4b08b528d326e2b4d6d8c67ca51194fbf`, tree
`21c8e05d286499f2a2190568deb762739b4fd25b`. Every consumed bootstrap,
contract, classpath declaration, backlog and ownership blob, and the dedicated
test path inventory, is unchanged from the tested source tree. The 13 incoming
documentation/tool paths exactly match main. Fresh scoped composition checks
passed 70 tests / 541 assertions plus documents 240 / 19. Independent review
accepted that composition and retained the original child evidence without
claiming a new child execution. This usage/evidence-only successor changes no
consumed input; it requires documentation validation and review, not another
compiler replay.

The original branch publication was denied and did not execute. After new
direct user publication/integration intent, one supported reassessment of the
same action succeeded; no actor or route substitution was used. This does not
waive any remaining restriction or grant compiler authority. No speedup, saved
JVM launch, proof, migration, release or self-hosting completion is claimed.

### Independent review correction: consumed digest implementation

Subsequent parent review found a blocking omission in `2982760e`: the Reader
closure alone omitted `gravity/digest.clj`, although both included runner
namespaces require it. The earlier peer reviews and actual child results above
remain historical evidence on the incomplete closure; they do not establish
that a digest implementation edit would invalidate those old results.

The narrow successor adds that exact production input, updates the expected
closure regression and adds a copied-repository control that changes only a
comment in the consumed digest file. That preserves its behavior and hash
outputs, but must reject the old hit, change the request key and execute the
new request. Corrected planner/wiring validation passed 103 tests / 922
assertions with zero failures/errors. Independent correction review accepted
`67a1f7c78e84d21abf1825cb186a129cf81c5f0d`; later scoped integration is recorded
below. No full compiler or heavy source replay is needed
for this runner-input correction.

### Current-main Runtime catalog integration

Main subsequently added `runtime_module_reads_test.clj` without a standalone
catalog mapping. A one-selector planner control reproduced that catalog error
without loading or running the Runtime provider. The existing source ownership
maps `runtime.gravity` to SH-19 / `:sh-runtime`; its owner confirmed that mapping
and required freshness. The namespace is now discoverable under that owner.

Its established source execution needs the common host broker, host-exclusive
and canonical-heavy guards. No committed route currently represents all three.
Plans therefore retain the namespace and explicitly mark execution unsupported.
The existing parallel runner recomputes that policy at plan normalization and
both namespace-oriented process entrypoints (`run-namespace-process` and
`run-normal-batch-process`). It rejects before scheduling, broker acquisition
or launch, even if a caller omits or forges the shard marker, supplies a string
namespace or relabels the job exclusive. The established owner-local wrapper
uses the separate explicit-command process API with all three guards already
held; compatibility with this successor has not been executed. This mapping
does not claim Runtime acceptance or authorize a generic source run. No new
runner or resource-limit substitution is introduced.

Planner validation passed 60 tests / 537 assertions. The exact injected runner
negative passed 1 / 13 with zero worker/launcher calls. The initial design that
rejected plan construction also blocked six legitimate discovery tests; it was
replaced by preserved plan discovery and rejection at actual execution admission.
That nonpassing intermediate result remains history, not acceptance evidence.
An independent source review then found that the direct single/batch process
APIs could bypass the initial plan-only check. The shared policy check now
covers those entrypoints too. Three selected injected runner tests passed
3 tests / 55 assertions: plan rejection, direct entrypoint zero-launch
controls and an ordinary single-process positive control. No real Runtime
provider or subprocess cleanup test was executed for these injected controls.

### Integrated Reader and catalog increment

PR432 merged as `6e49d6f880104cb8a51fd5981ebafa29dfd5995e`, with the exact
reviewed candidate `086ada645ab2fa961701afff898880dd07a16c3d` and identical tree
`9827866ced4e361089ffea642b41cb055361d2f7`. Final scoped validation passed
115 tests / 964 assertions, plus the three injected runner tests / 55 assertions
and document validation (240 documents / 19 indexes). The first final gate
timed out waiting for host capacity before tests; it remains a nonpass.
Independent code and terminal-result review accepted the finite increment.
No remote CI result or final-candidate compiler execution is inferred.

The later runner guard changed a consumed input. Earlier actual leaf producer
18 / 930 and hit observations therefore remain on their original source tuples;
they are not final-candidate executions. Historical incomplete-closure evidence
above remains historical. Broader RV1, narrower compatibility reuse and RV4
completion stay open.

## Compatibility loader closure audit

Audit base: `da2d62217aa1e630f69f3bb03e4238fe92be2600`. The declarations for
`gravity.bootstrap-compatibility.c11-test` and
`gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test` each named
66 inputs, but omitted `bootstrap_impl/loader.clj`, `load_order.edn` and all
resources loaded by that manifest. Reading the manifest and literal data-resource
references without evaluating compiler code found 1,576 distinct component
resources and 118 referenced data resources. Including loader and manifest,
all 1,696 concrete loader inputs were absent from both old declarations.
This is a loader-input lower bound, not a complete transitive or runtime-I/O
closure proof.

Production already authorized narrowed reuse only for the three audited pure
leaves. Both compatibility selections therefore used the whole-repository key;
the audit demonstrates obsolete metadata, not an observed production stale hit.
This successor removes their misleading narrowed declarations and model tests
that treated them as complete. Planner controls require no reviewed closure or
narrowing authorization for either selection. Existing deterministic development
policy is unchanged; freshness-required and authoritative work stay fresh.

A replacement control creates a small Git fixture and exercises actual repository
snapshot, request, probe and cache wiring. For each compatibility selection it
changes loader, manifest, component, data and both accepted fixture paths, with
equal-length bytes and restored modification time. Each change must reject the
old snapshot, change the key and execute the renewed test-only model request;
restoring the exact inputs restores the original hit. It never loads or executes
the compatibility compiler. Existing mixed/fresh fallback, symlink, deletion and
read-race controls now use audited pure leaves. Generic byte, mode, shadow,
classpath and singleflight controls remain.

Planner, wiring and language-boundary validation passed 108 tests / 1,011
assertions with zero failures/errors. A preceding attempt timed out awaiting
host capacity before tests; the first executed gate rejected the fixture's
noncanonical macOS path (108 / 936, one error). Both remain nonpasses. The
fixture now resolves its real Git root; no guard was weakened. Independent
review and integration of this successor remain pending. Narrower
bootstrap reuse remains open until the ordered loader, nested namespace, data,
runtime-I/O and shadow dependencies are fully bound and independently reviewed.
The conservative fallback does not prove unrelated documentation intrinsically
requires a compiler rerun, and this audit claims no runtime speedup or fresh
compiler acceptance.

### Compiler-pass compatibility seam observations

The next RV2 unit starts at integrated source
`688da420ea4e41572a0e7548e1ef7cd19e5b2317`. It targets only
`gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test`.
The test loads the real `gravity.bootstrap` facade. Its interposition of
`compiler-pass-source-artifact` does not avoid the fixture read:
`foundation/00790-compiler-pass-file-artifact.clj` evaluates `(slurp path)`
before calling that function. The accepted `compiler-passes.gravity` fixture
is therefore a consumed input, rather than path-valued metadata.

`tools/compatibility_closure_audit.clj` records read-only source observations:
the selected test's recursive literal namespace requirements, recursive literal
absolute `load` candidates, the explicitly loaded bootstrap loader, ordered
component manifest, literal data-resource
candidates and the accepted fixture. It hashes observed bytes and executable
mode and preserves manifest order. It rejects missing, escaping, symlinked or
ambiguous namespace inputs. It reads namespace declarations with reader
evaluation disabled and never loads the observed namespaces or compiler.
Built-in `inst` and `uuid` tags and custom tags reject in the namespace and
manifest forms read by the audit. Source bodies are hashed and scanned, not
evaluated. A supplied audit root that is itself a symlink rejects; ancestor
aliases such as macOS `/var` resolve to a canonical root before child checks.
All arguments of flat literal absolute `load` forms are observed. Relative,
computed or otherwise unparsed load forms remain explicitly unresolved.
The tool can be run with:

```sh
clojure -M tools/compatibility_closure_audit.clj --audit
```

Its source read-site list is syntactic evidence for further review, not a call
graph or an execution trace. Comments and metadata can overapproximate literal
resource candidates. Computed paths, arbitrary initialization effects, dynamic
body calls, atomic read consistency and runtime/classpath identity remain
unresolved. Neither input counts nor an unchanged observation fingerprint
prove complete dependencies. Every report explicitly retains `:authority :none`,
`:closure-complete? false` and `:cache-narrowing-authorized? false`.

The current `valid-cache-closure?` representation permits only 1-96 flat explicit
inputs with fixed roles and inventories. This audit neither widens that gate
nor adds a manifest exception. A manifest-backed representation requires its
own reviewed contract, including ordered resource resolution and invalidation.
Existing conservative fallback remains active. A producer run is a separate
step after a complete closure and supported execution route are established;
this observation tool does not launch or authorize one.

Cheap synthetic controls cover nested requirements, ordered manifest changes,
component/data/fixture mutations, namespace shadows, missing and escaping
resources, symlinks and reader-evaluation attempts. Computed resources stay
explicitly unresolved. Initial controls passed 5 tests / 28 assertions; language
boundary passed 6 / 11 and documents passed 240 / 19. The preceding broker wait
timed out before revised controls or the real-source audit ran; it remains a
nonpass. The actual source observation recorded 1,982 inputs, 1,578 ordered
components, 118 literal data candidates and 186 syntactic read candidates.
That initial observation digest is
`9df558db90d4022c6b37661dd778699744ca8ebc6e5a4bd502e4cc27d53110ee`;
it identifies those observations, not a complete or accepted closure. Subsequent
read-only form-context analysis found fifteen literal initialization loads in
`c2_pass_cache.clj` that namespace requirements alone did not follow. The
successor includes those loaded files recursively and tests their mutation,
missing-file and shadow behavior. It also preserves multiline `append-data!`
read candidates that the initial line matcher missed. Neither correction
enables reuse or makes a complete-closure claim.
Final successor controls passed 7 tests / 34 assertions and language boundary
6 / 11. Its observation includes 1,998 inputs and sixteen literal loaded files:
the fifteen direct loads plus the nested `capacity_handoff.clj` load from
`adapter_request.clj`. There are 306 syntactic read candidates after the multiline
correction and loaded-file traversal. The new observation digest is
`e893375079f95a22f95b12d2394430403ea6c6ed3ff7844d919088a82e44e0a0`.
Independent review then required explicit tag-reader and audit-root policies
and multi-argument load handling. Their controls now pass 10 tests / 45
assertions, plus language boundary 6 / 11. An intermediate 10 / 45 run had two
fixture-stage failures because the previous tagged manifest was not restored
before probing the next source tag; those failures remain nonpasses. The
fixture reset was corrected without relaxing the reader policy. These policy
changes preserve the observed input set and observation digest above; they
do not establish initialization-effect or cache authority.
Independent closure review remains pending; the next eligible source review is
the reported initialization
and dynamic read boundary, followed by representation design only if that
boundary can be closed. RV1 and whole-outcome RV4 remain open.

### Initialization calls, effects and representation boundary

This unit starts at `b45bdeebcac7caafce8e67ee6738ec3cf85dbc06`, tree
`ab7138e11e09e27876727604d26211f7fe923af2`. The dependency observation is still
`e893375079f95a22f95b12d2394430403ea6c6ed3ff7844d919088a82e44e0a0`.
Reading all 1,878 observed Clojure files produced 9,642 top-level forms without
evaluating source. This is an index of observed source, not measured execution
or acceptance coverage.

| Source boundary | Established dependency or behavior | Remaining condition |
| --- | --- | --- |
| `bootstrap_impl/loader.clj` | `resource-text` resolves a classpath resource and reads it; `load-component!` passes its text to `Compiler/load`. The ordered manifest and component bytes are initialization inputs. | Physical repository observations do not prove the runtime classloader selected those exact resources. |
| Loader `begin-data!`, `append-data!`, `finish-data!` | Begin/finish change loader-local assembly state; append reads the named EDN resource. Incomplete assembly rejects. | Preserve state/error behavior and resource resolution; a data count is not an effect proof. |
| The sixteen `c2_pass_cache` loads | Fifteen facade loads plus `adapter_request.clj`'s `capacity_handoff` load introduce additional source during namespace initialization. | The literal load order and owning namespace must survive a future representation. |
| `00811-sh03-reader-plan-validate.clj` and `00885-sh07-core-source-relative-path.clj` | The first cached binding wraps its builder in `delay`; the second uses `reify` methods that invoke binding/source-key functions when called. Their bodies must not be counted as already executed merely because they appear under `def`. | Macro resolution, method invocation and callers remain separate obligations. |
| `00945-p15-s23-sh07-b51-plan-linker-02.clj` | The byte-array class initializer calls `Class/forName` with `[B`. This is a runtime class dependency, not an accepted-source byte allowance. | Runtime/classpath identity remains required. |
| `00826-standard-reader-options.clj` | Initialization computes the policy hash through `reader-canonical-hash`, `c2-artifact-identity-call` and `c2-artifact-identity-ops`. That map contains live function values passed through `with-operations` and the compatibility trampoline. | `fn?` validates callable shape, not purity. The default digest/canonical bodies are observed source, but the actual operation bindings and transitive calls require a supported runtime contract. |
| `00438-p15-s23-artifact-context-contract-version.clj` | `p15-s23-required-evidence-keys` builds a set from the declared required-evidence vector and adds the seed-retirement key; its rejected-candidate initializer is data construction. | Preserve the referenced data and function binding; this is no seed-retirement acceptance evidence. |
| Local interposition macros and the selected `deftest` | Inspected macro definitions construct expansion data. Function/method/test bodies and macro expansion are different stages. | Generated code, macro bindings and later calls cannot be dismissed by syntactic containment. |

The installed Clojure source inspected for `fn`, `defn`, `delay`, `reify`,
`deftype` and `deftest` was version 1.12.5. Core/test source hashes are retained
with the private source observations. This readback establishes which library
source was inspected; it is not a runtime admission receipt for a future child.

`tools/compatibility_initialization_audit.clj` records per-file form order,
source locations, definition kinds and call candidates separated into current
syntax and body syntax. Those labels are conditional on source spellings:
locally shadowed `delay`, an immediately invoked function, higher-order calls
or macro-generated code can invalidate a naive eager/deferred interpretation.
The report retains the body candidates, spelling assumptions and unresolved
call/effect status. It never expands a macro or resolves an observed runtime
Var. The shared reader policy is reused from the dependency audit; its old
first-form behavior remains intact.

The concrete dynamic seam is the policy initializer's callback map in
`compiler/00799-c2-artifact-identity-ops.clj` and invocation bridge in
`compiler/00801-c2-artifact-identity-call.clj`. The leaf's operation validator
and `call-entrypoint-body` check callable shape and establish invocation state;
they do not certify an I/O-free function body. The observed default hash path
ends in the canonical leaf and `gravity.digest`, but a static function name or
unchanged map is not evidence that the live bindings cannot differ. No callback,
authentication, class or invocation guard is removed to make this audit close.

The distinct `:gravity/compatibility-initialization-observations-v1` format is
an audit representation only. It carries no cache declaration or acceptance
token. `:authority :none`, `:closure-complete? false` and
`:cache-narrowing-authorized? false` cannot be upgraded by caller fields.
Controls demonstrate that the existing `valid-cache-closure?` consumer rejects
the report, including forged completion flags or the old cache schema name.
The old flat-input bound, roles and inventories are unchanged. Wrapping this
larger observation in one alleged flat input would not establish a valid cache
closure and is not an admitted representation.

Current unresolved work is actual namespace load order, lexical/dynamic call
and Var binding, macro expansion and generated-code effects, transitive
initializer effects, runtime classpath/resource resolution, reader conditionals
and atomic input consistency. A manifest-backed cache representation requires
separate review after that read/effect boundary is established. A separately
attributed producer remains unrun until the complete closure and a supported
execution route are established. Conservative fallback and fresh public
verification remain active.

The reused reader controls passed 10 tests / 45 assertions; new initialization
controls passed 7 / 37. The first attempt passed the reader controls but could
not read the new tool because of an unmatched delimiter; no new controls or
source audit ran in that attempt. That nonpass is retained. Independent source
and representation review, documentation/language gates and integration
completed for the finite audit-only unit through PR443, recorded below.
Runtime effect closure and whole RV1/RV4 closure remain open.

### Startup resources before the child cache decision

The initialization observations and audit-only format were integrated through
PR443 as `a1b991be499d50579a8b4366976c519ae8949224`, with full tree identity to
reviewed `5ccc754b`, tree `5b62819e511bfc7e6b5eba0527d8c9b5883c5572`.
The small documentation successor retained all three tested tool blobs; only
document validation was repeated. This does not close the runtime effect proof.

A subsequent startup audit at `f51a43ae267736a81587d81ffe52839c86c02b2d`
found a consumed resource outside the old narrowed requests. Installed Clojure
1.12.5 `RT.doInit` calls `maybeLoadResourceScript("user.clj")` before entering
the runner. The child cache wraps `run-namespaces`; the selected namespace's
`require` occurs inside that operation. A cache hit therefore does not freshly
initialize that namespace or observe its live operation bindings.

A copied-closure control added and changed comment-only `user.clj` fixture
bytes outside the actual classpath. Its absent, present and changed request
keys were equal on the old code. No startup code was executed and no production
stale hit was observed. The source/runtime trace establishes the omitted input;
the older producer observations retain their original scope and do not prove
startup closure completeness.

The correction keeps tests fresh for an unreviewed startup domain. Single and
batch command construction omit the optional cache argument when project
classpath roots contain `user.clj` or a `clojure/` override location, before the
child JVM starts. Standard-library overrides are conservatively outside the
reviewed domain; this is not proof that every file below that prefix is used.
The child independently checks project locations and the currently resolved
`user.clj` resource, including external resources. Unknown observations also
stay fresh, and effective-runtime revalidation repeats that check across the
operation and hit boundary. These presence observations are not atomic
attestation or proof of prior nonexecution.

The existing closed cache-option validator is shared by the command builders
and child parser. Only a valid optional cache argument may be omitted. Malformed
arguments stay on the child's original `SH01-TEST-USAGE` refusal path; they
cannot become a successful fresh run by having their invalid option dropped.
The fields, CLI data validation and target selection are unchanged. Existing
cache refusal/error handling remains in place for eligible cache attempts;
unsupported startup configurations run fresh without probing that state.
No flat closure bound, roles, inventories or public-verification
authority is changed, and no bootstrap-dependent selection gains narrowed reuse.

New controls cover both project roots, startup source locations, standard-library
override locations, dangling links, both command builders, malformed-option
refusal, resolved external startup resources and observation errors. Injected
child controls require fresh execution with zero cache lookups and no runtime
measurement when startup resources are unreviewed. Wiring passed 45 tests /
507 assertions. The first attempt could not compile the new test because its
`clojure.java.io` alias was missing; no controls ran, and that nonpass is retained.
Three selected injected runner controls passed 3 tests / 104 assertions;
language boundary passed 6 / 11 and documents passed 240 / 19. Composition
with main `06a256fd` repeated wiring and boundary controls (51 / 518) and
documents (240 / 19); the selected injected runner evidence retains its
original candidate attribution.

Independent review found that the enclosing runtime observer swallowed a
startup observation's interruption through its broad exception fallback.
The correction restores and rethrows interruption before that fallback.
Injected controls verify zero cache lookups and zero test executions on this
interruption, while an ordinary observation error still permits fresh tests.
The corrected wiring controls passed 46 / 518. Independent successor review
accepted the finite increment, integrated through PR448 as
`3bf69cbcd16be840fac8e687adfcf8e52b65696e`, tree
`5d074683014ae871e1d2a9cf71e43e0ef33f2cd8`, identical to the reviewed
candidate. The initial publication denial remains recorded; one supported
reassessment using explicit user publication intent succeeded. Complete
bootstrap initialization/resource closure, a separately reviewed cache
representation, producer evidence and RV1/RV4 remain open.

### External archive manifest dependency boundary

RV2's next finite family starts at integrated PR448. The installed JDK's
`URLClassPath` adds a JAR loader's manifest `Class-Path` URLs to its search
order. Those URLs can name other archives or directories outside the listed
`java.class.path`. Hashing only the listed archive does not bind those bytes.
This is a source-backed omission, not a measured production stale hit.

The narrowed classpath admission and actual child runtime observer now require
each external classpath file to be an inspectable archive without a main
manifest `Class-Path` attribute. Any such attribute, including an empty value,
stays outside the reviewed domain. Malformed, absent, directory and symlink
inputs cannot establish that condition. Unknown archives disable optional reuse;
the existing child fallback executes tests fresh. No referenced URL is opened
by this inspection, and no recursive manifest representation is admitted.

A cheap fixture uses a real `URLClassLoader` to read a marker from an archive
referenced only through the listed archive's manifest. Changing the referenced
marker changes the observed resource while preserving the listed archive's
identity. Both observations reject narrowed admission. Separate controls verify
that a closed archive is accepted for further checks, adding a manifest edge
rejects it, malformed and missing archives remain unknown, and the actual
runtime observer applies the same condition. Fixtures load resources only;
they execute no classes or source. Wiring passed 48 tests / 543 assertions.
Independent code/result/composition review accepted the finite fallback;
composed wiring and boundary passed 54 / 554, documents passed 240 / 19, and
exact clean preflight passed. PR451 integration is recorded below. No provider
or compiler replay was performed for those gates.

This closes only the manifest dependency fallback rule. It does not establish
complete JVM image, custom ClassLoader, native library or bootstrap runtime
effect closure, admit narrower bootstrap reuse, or refresh older producer
evidence after a consumed runner change. RV1/RV4 remain open.

### RV1-LDR: resource-dispatch fallback

The manifest fallback increment above was integrated through PR451 as
`662f516f1c476664fc0204eb424c9565ac127962`, tree
`e6fc4e860be03f5cd2a1e4ad4a684dcf28f5cdc6`, identical to its independently
reviewed candidate. Its actual resource-mutation evidence stays on that tuple.

The next source audit separates two resource-dispatch interfaces. Installed
Clojure 1.12.5 `clojure.java.io/resource` uses the current thread's context
ClassLoader by default. `RT.baseLoader` separately prefers `Compiler.LOADER`
when bound, then the context loader when configured, otherwise the defining
loader. These loaders can supply additional resource bytes absent from
`java.class.path`, even when all declared classpath file hashes are unchanged.
`DynamicClassLoader` inherits URL resource lookup and exposes `addURL`.

The finite supported domain requires the bootstrap-defined JDK application
system loader, also defining `Compiler`. Both the current context loader and
actual `RT.baseLoader` must be that loader or a chain of exact
`DynamicClassLoader` instances with empty URL lists delegating to it.
Subclasses, other loader implementations, additional URLs and nil/unknown
delegation stay outside the domain. Exact class checks precede calls that
could dispatch to a custom loader implementation. An observation failure
disables reuse; interruption propagates. The condition applies before child
startup-resource lookup and basis measurement, at narrowed classpath admission,
and during existing runtime revalidation. Unknown loaders execute the existing
fresh operation with reason `:unreviewed-resource-loaders` and no cache lookup.

Resource-only controls mutate an external marker observed through a real
context loader while the declared classpath stays unchanged. They require two
fresh operation calls and zero startup, basis or cache lookups. A compiler-bound
empty dynamic loader is initially inside the resource domain; adding a URL
exposes a marker through `RT.baseLoader` and rejects the configuration. Custom
subclass, nil-context and nil-compiler-loader controls also reject, without
invoking custom URL callbacks. Wiring passed 50 tests / 566 assertions. Initial
observation attempts failed before observation because the test path was absent
and then because an inline form was malformed; both nonpasses remain recorded.
The corrected development-JVM observation and controls completed separately.
Independent code/result review accepted `b293edb2db8e0109c5323d340bdc54924c6ec198`,
tree `85b01e6b161390d4fb579cad428a202e36de4587`. Boundary passed 6 / 11,
final documents passed 240 / 19 and exact clean preflight passed. PR452 merged
as `7ae18ee6c388db55748b0b2427f2964f60953768`, with full tree identity to
that reviewed candidate. The source observation failures remain historical;
no compiler/provider replay or remote CI result is inferred.

This is a current-thread resource-domain observation, not atomic attestation,
proof of prior nonexecution, or proof of in-memory class identity. A known empty
dynamic loader can already contain defined classes. Other threads, JVM image,
native libraries, bootstrap effects, live operation/AE identity and unsampled
mutation remain separate unresolved prerequisites. No additional Macro check
is removed, and no whole-RV1 completion or new bootstrap reuse is admitted.

### RV4: current supported-leaf miss, hit and invalidation

The frozen clean source was `9db7e9f30ddf26b1c42138e7abbde04aa2214eb1`, tree
`f9a573eaf36ff964fac4843273c73f580cea93d1`. Its consumed runner includes the
integrated startup, manifest and resource-loader guards. The selection was only
`gravity.c2-artifact-identity-test`, a pure artifact-identity leaf; it does not
construct or authenticate a current-source Reader product.

The reviewed private supervisor had SHA-256
`3ab235dce37c3c12318826d786bd75d61505d4e9ad2baa4bd74ba516a2911dba`.
Before test-child execution, 14 report/data cases, two owned-process cleanup
cases and two existing report-path cases passed. Missing keys/provenance,
trailing EDN, wrong selection, zero counts, saturated output, interruption and
report exceptions reject rather than becoming acceptance evidence. The existing
child writer's path contract is checked before launch; it was not relaxed.

The corrected supervisor and all three child commands completed with natural
exit zero. Each child ran under the existing normal broker with a configured
256 MiB heap and 60-second watchdog. Full reports, process waits/exits, cleanup
facts and source bookends were retained before compact extraction.

| Phase | Actual result | Attribution |
| --- | --- | --- |
| Cold-cache producer | 18 tests / 930 assertions; stored miss | Fresh non-authoritative result on the frozen clean source |
| Unchanged request | Hit; `tests-executed? false` | Same nonempty key and full original producer provenance; displayed 18 / 930 belongs to that producer |
| Consumed runner comment changed | Different key; fresh 18 / 930 and stored miss | Same Git HEAD plus the explicit working-tree delta below |

Only `sh01_development_loop_wiring.clj` was temporarily changed, by appending a
comment. Its original SHA-256 was
`668b0501e46631430671e20a26967dd356897fb0b4bdecbf815bcab9598e137d`;
the changed bytes had SHA-256
`fa32d2c71858f0de74c032382e43e2b7a47b007995861fe45737f4068c65d264`.
The changed phase is not claimed as a clean-tree execution. Its prelaunch
snapshot, applying snapshot and producer snapshot record that delta. Final
restoration returned the exact original bytes and identical clean source
bookend. Independent result readback accepted all three full reports, natural
exits, provenance equality and restoration without replay.

The first attempted producer remains a nonpass: its child naturally exited one
because the harness chose a phase-named report file instead of `result.edn`.
No passing report or accepted counts were obtained, and its later phases were
not launched. A possible internal cache result from that failed command was
not acceptance authority and was not used by the corrected cold-cache attempt.
That correction received separate review and positive/negative path controls.

This closes the current supported-leaf real-evidence criterion in RV4 and
renews the Reader leaf evidence in RV2. It does not close the remaining RV1
identity/mutation prerequisites or the bootstrap compatibility read/effect and
representation gap. Public/fresh verification, compiler/provider acceptance,
benchmark, release and whole-outcome completion do not follow from these
development receipts. The hit still launched a child JVM; no startup avoidance
or measured speedup is claimed.

### RV1: required recursive canonical callback dispatch

This records a bounded necessity decision within RV1's transitive dispatch
prerequisite. Within one canonicalization call, the current partial operation override
interface requires the callback event for each child, including equal children.
Equality of input values or callback function identity cannot justify suppressing
one of those events. This classifies that recursive dispatch outside redundancy;
it does not determine how many whole Macro owner-verification calls are needed.

The source chain is explicit. `c2_artifact_identity.clj` owns `with-operations`,
`call-entrypoint-body` and recursive interposition. Its `operations.clj` validator
accepts supplied functions without certifying purity or immutable closure state.
`c2_artifact_identity/canonical.clj` applies the callback to every vector/list
child and preserves map/set printer ordering. Bootstrap components
`00799-c2-artifact-identity-ops.clj`, `00801-c2-artifact-identity-call.clj` and
`00802-reader-canonical-value.clj` connect the compatibility route to that API.
Observing outer binding data and unchanged Var roots does not establish the
absence of mutable state inside a supplied function.

The Reader owner's retained compiler-free leaf probe executed at
`f40942fe903b25ad6fbaf626aeae246afc815aee`, tree
`48d8d0118c2e38086d56f78882d27808aebddc8f`. Four existing contract tests passed
4 tests / 28 assertions, zero failures/errors. An additional actual control
canonicalized `[7 7]`: the same supplied callback observed both children and
threw its sentinel on the second call. The identical Throwable object propagated.
Suppressing the second event would suppress that observed exception, changing
the current contract rather than removing redundant work.

Those tests also preserve recursive Var interposition, stable printed-sort ties,
printer call order and exact thrown-object precedence. The driver SHA-256 is
`7faf723b3a223876e158308e6a66d4b2df360006d6dec263bb17fc24d8aedec1`;
the original complete receipt SHA-256 is
`7ed7ab7a91d9ec553913dc85b6a3453c1cb511b4f8e16a241f79ce2d3a5f5dd8`.
Its source/control bookends match, measured maximum heap is 268435456 bytes,
and the original process completed with exit zero in 983.636125 ms. The probe
did not load `gravity.bootstrap` or authenticate a Reader product. All eleven
recorded source/test inputs remain byte-identical in this successor; the receipt
retains its original source attribution and was not rerun for this decision.

Any further RV1 proposal must conserve these callback/printer events and error
identity/order, or separately establish an eligible closed route where the
proposed reuse preserves its contract. Live-plan and AE identity, invocation
isolation and whole-seam unsampled mutation remain unresolved for another Macro
check removal. No new runtime implementation is justified by this leaf result.
Allocation changes that preserve every event require their own evidence; no
full-source cost share, speedup or whole-RV1 completion follows here. RV2's
bootstrap compatibility closure and the existing fresh public verifiers remain
separate, and the accepted RV4 supported-leaf proof remains unchanged.

### RV1: construction and public verification have different proof inputs

The retained computation considered here is the Resolve verification body
entered for construction-proof generation and for caller-requested public
verification. These calls have different semantic modes. The public
`:embedded-capability-proof-current?` comparison is necessary; a construction
PASS is not an interchangeable public-verification report.
The upstream `sh06-resolution-verify-sh05` call is a separate reuse candidate;
this local comparison does not establish stability of that call's live inputs.

The current `00881-sh06-resolution-finalize-candidate.clj` already avoids an
immediate duplicate Gravity replay after installing its freshly generated proof.
That site is not a remaining duplicate to remove. The construction helper in
`00880-sh06-resolution-artifact-verification.clj` passes `construction? true`;
the public contained verifier passes false. In
`00879-sh06-resolution-artifact-verification-bounded-02.clj`, the embedded-proof
predicate permits a missing proof during construction, while public mode
requires equality to the expected proof derived from current base checks.
The identity-input projection in `00877-sh06-resolution-product-projections.clj`
does not include the embedded proof, so an artifact identity alone does not
establish this comparison.

A bounded source-expression probe used frozen commit
`bca9ea29ac5afa077313611c6fa1e2672e36cb41`, tree
`4933dea927bb19434cd0b94482ea21239b99d108`. It read the actual predicate with
reader evaluation disabled, selected its exact lexical binding and allowed
only the required core Boolean/equality operations. It evaluated that expression
alone using native synthetic maps. No admitted artifact or compiler report was
supplied or produced; no Bootstrap, Reader, Macro or public-verifier entrypoint
was loaded/called. These are predicate cases, not C5 acceptance tests.

| Controlled predicate input | Observed Boolean |
| --- | --- |
| Construction mode, missing supplied proof | true |
| Public mode, missing supplied proof | false |
| Public mode, exact expected proof fixture | true |
| Public mode, failed proof status | false |
| Public mode, extra proof field | false |
| Construction mode, stale supplied proof | false |
| Same supplied proof, changed current expected-check fixture | false |

All seven cases matched, with the same synthetic parent fixture throughout.
The process exited zero under the existing normal broker with measured maximum
heap 268435456 bytes; source/control bookends were identical and clean.
Target source SHA-256:
`a355968131742b4875d573371860eb08f1f4617e1700fc56d0117be24b78444c`.
Driver SHA-256:
`5af4c77af2472621a922d49d63cf69fb3444fa9a0f406ed0564f1fad6bd2bf7b`.
Complete receipt SHA-256:
`3644d1d6836e5718e1b7ed395631579b84701598625b82c7c87e4946770d6569`.

This closes only the necessity decision for the public proof comparison and
rules out substituting the whole construction report for public verification.
It does not prove that every upstream Macro recheck must be recomputed.
Sharing that fragment still requires an owner guarantee for the exact live
plan, Reader invocation, AE/dispatch identities and mutation boundary; a saved
PASS, common parent ID or compiled binding DATA does not supply it. That predicate
result alone justifies no runtime change. The original RV1/RV2 remaining
criteria, fresh public verification and immutable RV4 evidence remain intact.

### RV1: fresh structural checks without temporary key sets

The whole-boundary review confirmed that both repeated upstream Macro reports
use SH05 `:final` mode. C5's construction/public proof difference cannot justify
that entire repeat. Report equivalence across intervening Resolve execution
still needs the actual Reader invocation/source, Macro plan/runtime, AE/lineage
and dispatch/closure state, preserving observable errors and callbacks. The
short construction guard does not provide that guarantee. An atomic execution
domain is one possible design; factoring a pure fragment can be smaller.

The implementation starts with the existing `run-storage-exact?` calculation
in `semantic_late/150856-sh05-verification-expansion-replay.clj`. Its new helper,
`gravity/macro_run_storage.clj`, checks the same four key sets without constructing
temporary sets for native `PersistentArrayMap` values with keyword keys.
Distinct key bits preserve set equality, including duplicate keys. It visits
all keys before deciding even after an extra keyword, preserving later errors.
Other map classes and foreign keys retain the original set computation.
Values and metadata outside the key-shape calculation are not inspected.

Every shape is read fresh. An initial construction-local memo design failed a
cheap negative: `PersistentArrayMap` can retain a caller-supplied array, whose
mutation changed the shape without changing the vector identity. That design
was discarded before publication. Its earlier eight-test pass did not cover
aliasing; the corrected selector recorded one test, two passing assertions and
one failure. The first selector accidentally selected no test and earns no
credit. No cached Boolean, verification report or token survives in the
implementation, and no construction or public verification call is removed.

Controls compare the original expression across consumed-key changes, other
schema keywords, foreign-key callbacks and identical thrown errors, lazy
short-circuiting, malformed and odd backing arrays, duplicate keys, and actual
aliased-array mutation. A source-based adapter model executes the actual
expansion-stage function twice and checks the unchanged collaborator call
order. Its collaborators are explicitly modeled; no admitted artifact or fresh
Gravity/Reader/AE execution is claimed. The new namespace remains fresh in the
development catalog under SH05 ownership and receives no cache opt-in.

A separate native fixture allocation control used 32 measured calls with 65
runs each, after 32 warmup calls per operation, under the existing normal broker
at 256 MiB. The retained baseline allocated 6,542,664 bytes; the fresh helper
allocated 3,830,272 bytes on that fixture. An earlier helper allocated more
than its baseline and was replaced. These observations establish the bounded
allocation result only; they are not compiler speedup or verification-elision
evidence. Bootstrap was not loaded. The accepted supported-leaf and callback
evidence is unchanged; whole RV1, RV2 compatibility closure and whole RV4 remain
open pending their original acceptance requirements.

On exact implementation `7e7da6deaf810a85412d372dddd1578c3fd2d83c`, tree
`880eb72fdb4fadce3a8bec047f8eaff20727a65d`, the combined fresh helper/adapter,
planner and language-boundary controls passed 78 tests / 718 assertions; docs
passed 240 / 19. The helper contributed 12 / 151, the planner 60 / 556 and the
boundary 6 / 11. The brokered 256 MiB process exited zero with identical clean
source/hash bookends and no Bootstrap load. No compiler/provider acceptance
or public artifact replay follows from that scope.

### PR480 Bootstrap load-order regression

Accounting's subsequent composition on main
`8cebb9336fa5546090d6b5ac6a4d8dc1930b70fa`, tree
`d0e0d650e67c13de5f71c8a12dd356a2f1fda30f`, failed Bootstrap loading before
semantic tests: the `macro-run-storage` alias was unavailable. The original
bounded process exited 1 after 7,472 ms with no semantic observations. Its
complete retained receipt SHA-256 is
`d1b1cded91173a3924269e244e643edd5932f3dcf801090cb8cfbd7bc5f5e1de`.
That NONPASS is not replaced by the earlier modeled-adapter results.

The ordered manifest loads the semantic expansion stage before the public
SH05 facade where PR480 placed the helper require. The modeled test primed the
alias itself and therefore missed this actual entry-order failure. The repair
moves the require to the first consumer, removes the late declaration, and
loads all consumer forms into a fresh test namespace without priming its helper
alias. A negative omitting that declaration must reproduce the missing-alias
compile error. The resulting helper/consumer controls pass 13 tests / 157
assertions; they remain Bootstrap-free.

A complete cold Bootstrap namespace load is a separate acceptance requirement
for this repair. It validates actual entry/load order without requesting compiler,
Reader, AE, provider or C12 entrypoints; it does not prove namespace initialization
effect closure. The structural calculation, fallback and
verification calls are unchanged. The original RV1 mutation candidate is
preserved separately; neither this repair nor PR480 closes whole RV1/RV2/RV4.

The cold load completed on repair `537664462a25e9affb26a7810b104b36b4799c36`,
tree `dd2b5fd0b78e53b4b0dd891821c4dbcb5d8faaa2`: natural exit 0, actual
2 GiB, 8,123 ms bounded-process elapsed, complete capture and identical clean
source bookends. Bootstrap and the helper namespace were both absent before
loading; the actual public/private Macro verifier definitions, helper alias and
public arglists were present afterward. The supervisor receipt SHA-256 is
`9fa047f5493867e4ac410c03d3442761619e2bce572b461c7a00a30ef36b93f5`.
Helper/load-order and language-boundary controls passed 19 / 168, docs passed
240 / 19, and exact integration preflight passed. These remain load-order
observations on the named repair, not semantic acceptance of Accounting's
separate candidate or proof of initialization-effect closure.

### RV1: real supplied-parent mutation after Resolve

The bounded mutation control completed on
`b930cdf64b5aadf697b09337c4a9e86001d6a054`, tree
`7272ad6d08d4c1c95435b1dbad269ced7fe64687`, based on merged assessment
`c637326dcf026cfd1697420a3b9b383439cb804a`. This closes the concrete
same-parent mutation observation for this fixture; RV1 remains partial.

`resolution_parent_mutation_test.clj` constructs the actual accepted SH06
compiler-subset fixture through the direct current Reader route, with an
explicit Reader source and invocation and no precomputed C2 product or
candidate. It constructs a genuine Macro parent and C5 result, then runs the
independent public C5 verifier, which passes. No SH07 proof transaction is
borrowed. Every observed Macro verifier and Resolve operation calls its
original implementation; no report or exception is substituted.

The changed case keeps the same parent object and invocation. Immediately
after the actual Resolve operation returns, it changes the caller-owned
array-map key `:resolved-verification`, leaving expansion and digest operands
intact. The subsequent real Macro report fails
`:macro-run-storage-exact?`; C5 raises `C5-UNRESOLVED`, with missing field
`:final-authenticated-resolution-artifact` and failed check
`:fresh-upstream-sh05-verification?`. The alias and both observer roots are
restored. The existing short-seam root catalog stays stable, but this is not
a complete transitive identity or closed-over-state proof.

Actual result: 1 test / 20 assertions, zero failures/errors, natural exit 0,
2 GiB heap, 104,975 ms supervisor elapsed. Both source bookends are identical
and clean; stdout is complete at 835 bytes, stderr is empty, and cleanup
completes with the process root dead. Supervisor receipt SHA-256:
`8b4f95d0db3dc1cdea09cb5fbae9d16ea2499f297f099f28044b6b134443b284`.
Full child receipt SHA-256:
`82c3c677d7bfee8743d54f4d49b818f3f24c1948a5ea1d4f63bac1ee1300d091`.
The exact code and harness were independently approved before this single
genuine attempt. Independent retained-result review accepted the actual
positive, mutation, rejection and restoration evidence without replay.
That exact result and its code were subsequently integrated through PR493 at
`e5d0bacdc59611ec9219e37d659532bf04f084a2`, tree
`7f739ed52784386df7cd63edca42d1679b65f87d`. The merge tree equals the independently
reviewed composition. The genuine result remains attributed to `b930cdf6`;
incoming source changes receive no semantic acceptance from it.

Cheap source-form observer controls passed 4 / 20 without entering Bootstrap;
planner and language-boundary controls passed 67 / 590, injected generic-launch
refusal controls passed 3 / 148, and docs passed 240 / 19. The SH06 catalog entry
requires fresh execution and rejects generic execution until a committed route
represents the common host broker, host-exclusive lease and canonical heavy
lock. Discovery does not authorize execution. CI's separately owned leaf
catalog correction is unchanged here.

This observation rules out reusing an earlier successful whole Macro report
solely from parent identity across this Resolve seam. It does not prove that
every computation within that report must repeat. Full live-plan, Reader
invocation, AE/dispatch and closed-over-state equivalence, or a complete
necessity decision for the original row, remains required. No additional
verification call is removed, no compatibility cache is enabled, and RV2/RV4
whole-outcome acceptance remains open. The earlier failed aliased-array memo
experiment and original PR480 load failure retain their NONPASS dispositions.

### RV2: shared compatibility initialization

This finite implementation targets repeated Bootstrap initialization for the
two original compatibility selections, not narrower result-cache authority.
`sh01_parallel_test_runner.clj` gives the two named normal jobs a common batch
affinity when their policies match and neither supplies a narrowed closure.
In that original candidate, the actual test bodies and separate namespace
results remained unchanged. The explicit test-only successor below changes
the C11 facade test's construction-dependent assertion, not either production
facade or its authentication contract.
Different policies, narrowed closures, foreign namespaces, other resource
classes, batch-size limits, standalone selection and fail-fast skips retain
their existing boundaries. A heterogeneous batch does not inherit one
component's identity. Full-repository cache fallback remains active.

Independent code/harness review accepted source
`8c4026b7147149a0828b63b5f5f68e55de6f4153`, tree
`b3c2802673a32c1c4e36ea4d6468ee4b6c94850d`, before the genuine attempt.
Its immediate base is Reader PR492, `4325a9f46292472a7f984c5adfef81209874bb5c`;
the initial handoff incorrectly named PR493 as that immediate base and is
corrected here. Eleven injected controls passed 66 assertions, language
boundary passed 6 / 11, and the actual receipt-predicate negatives passed
1 / 13 without loading Bootstrap.

The single genuine combined child returned NONPASS: natural exit 1, 2 GiB,
10,315 ms supervisor elapsed, identical clean bookends, complete outer capture
and completed cleanup with the root dead. It observed exactly one actual
`gravity/bootstrap_impl/load_order.edn` initialization. All observed Bootstrap,
C11 and loader-observer roots were restored. Manifest compatibility passed
1 test / 48 assertions. C11 compatibility passed seven assertions, then raised
`C2-HASH` with missing field `:exact-regular-pinned-sh03-reader-source`:
expected Reader bytes 231576, observed 231666. The C11 artifact assertions were
not reached. Total: 2 tests, 55 passing assertions, zero failures and one error.
The C11 namespace's inner output was truncated at 8192 of 25720 bytes; the
failure and available diagnostic facts are retained, not promoted to a pass.

The exact guard is
`components/self-hosting-sh03-sh07/00808-sh03-reader-expected-source-byte-count.clj`
under `bootstrap/clojure/src/gravity/bootstrap_impl/`. It binds an authenticated
Reader source identity, not a work allowance to increase for this test. The
current accepted Reader source and the legacy C11 facade route disagree.
The existing MIR owner is asked to identify the intended current invocation
or required source-owner handoff. That original attempt changed no pin, test
body or compiler source; the later handoff below is distinct. No unchanged
retry is authorized by the original result.

Supervisor receipt SHA-256:
`1829423660404107fed5e394c2f8dd4faf73d91add813d049183ba57271981ba`.
Full child receipt SHA-256:
`2458ea4f04a1ce9221d7570df265934df7c70974bbaccf582bc483d532767ce3`.
Independent retained-result review confirms the NONPASS and held disposition.
The initialization observation does not close this implementation's acceptance:
both real compatibility namespaces must pass with independent result review
before normal integration. RV2 runtime read/effect closure and the original
RV1/RV4 residual criteria remain open. CI's separately owned catalog and helper
registration files are untouched.

#### PR494 finite completion criteria

PR494 owns only sharing Bootstrap initialization between the two named normal
compatibility jobs, their runner controls, the handed-off C11 facade test and
this acceptance record. It does not own a new Reader mode, C11 product
construction, cache narrowing or the separately held RV1 correction.

| ID | Required completion evidence | Current disposition and dependency |
| --- | --- | --- |
| PR494-1 | Preserve both namespace selections and standalone execution; share one bounded JVM only for matching normal-job policies without narrowed closures. Preserve resource limits and independent namespace results. | Accepted on `8204a761` after exact source/scope/harness review. The tested combined schedule selects one child; each standalone schedule still selects one. Runner and test bodies are unchanged by the subsequent receipt-only documentation. |
| PR494-2 | Reject sharing for mismatched or unknown policies, narrowed closures, foreign affinity keys and non-normal resources; honor batch-size one. Preserve fail-fast skips and retain an error alongside the other namespace's passing result when fail-fast is off. Preserve String job identities and freshness policy. | Bookended controls on `8fc4b6a3` passed 13 / 78, boundary 6 / 11 and docs 240 / 19, natural exit 0 at 256 MiB with Bootstrap absent. Those control bodies are unchanged at `8204a761`. The initial compile error and incorrect Symbol-normalization expectation remain nonpasses. |
| PR494-3 | Fresh standalone and combined executions of the actual facade-only C11 namespace and manifest namespace pass on one reviewed tuple. Compare per-namespace status, counters and complete stdout/stderr; the combined child initializes the real Bootstrap once. All interposed roots, including the final artifact factory, must be restored. | Accepted actual evidence on `8204a761`: standalone C11 facade 1 / 14, standalone manifest 1 / 48, combined 2 / 62. Complete per-namespace outcomes match exactly; all declared roots are restored. This is the explicitly reviewed oracle scope below, not product construction. |
| PR494-4 | Retain full reports, actual natural process exits, heap/watchdog/capture/cleanup results and identical clean source bookends. Use existing common, host-exclusive and canonical guards. No cached report substitutes for any execution or authentication. | All three fresh phases exited naturally 0 at actual 2 GiB, with complete captures, stderr empty, cleanup complete/root dead and identical clean source/private-harness bookends. Independent retained-result review accepted them without replay. Original `8c4026b7` product NONPASS remains separate. |
| PR494-5 | Reconcile current main normally, conserve incoming blobs and reviewed source/test scope, pass relevant cheap checks and actual configured CI, and obtain independent current-head code/result/integration acceptance before expected-head merge. | Closed by PR494, merged as `49e7c8f2b86ba5e28ed054310ccac0765cc24c0c`, with full tree identity to independently accepted `d311644f`, tree `7e9d0d987ed34b13553e56f2d0049fb88f1afba3`. Current-head CI run 40566281239 passed seven applicable jobs; source-Core-readiness and experimental-prerelease were skipped. Docs 240 / 19 and exact clean integration preflight passed. Source-provider and whole-RV2 acceptance remain separate. |

The legacy entrypoints have only source `[path text]` and file `[path]`
arities. The newer public checked-Core/C11 interface is a different API; it
cannot be substituted silently. The MIR owner released only
`bootstrap/clojure/test/gravity/bootstrap_compatibility/c11_test.clj` for the
following explicit facade-only correction, with all production, Reader,
provider and codec paths retained by their existing owners.

The test keeps the real Bootstrap wrapper, `c11-call`, `c11/with-operations`
and configuration assembly. Only `gravity.c11-mir.artifact/file-artifact` is
replaced by a recording oracle. The oracle captures the actual configuration
and path, and returns a unique Object sentinel; it fabricates no C11 artifact
fields or verifier report. Assertions cover call count, unchanged path,
sentinel identity and actual interposed governing-document, diagnostic-ID and
rejected-design configuration. Existing operation, arity and public-API checks
remain. The path is an opaque facade argument here, not evidence that the
fixture bytes were read or authenticated.

This amendment tests the namespace's stated wrapper/interposition contract.
It deliberately replaces the previous accidental hosted-product construction
with that explicit oracle. The old `C2-HASH` failure remains a real failure of
the hosted chain; current-source/Reader/C11 product acceptance stays separately
owned and unproven by PR494. The original test was introduced as
`c11-mir-compatibility-wrappers-preserve-interposition`; its operation, public
arity and interposed-configuration assertions define that forwarding property.
The engine contract also explicitly declares partial operation overrides,
compatibility-only status and no source-authentication or canonical-verifier
authority. Those facts support this facade-only oracle, not removal of a
product-conformance requirement.

Actual verifier/product obligations remain open in the canonical
[MIR tasks](../specs/mir-workflow/tasks.md), particularly `mir/3` and `mir/5`;
none of their task states is changed here. The specific legacy hosted file
constructor's `C2-HASH` failure above also remains unresolved with the MIR and
upstream owners. New checked-Core/C11 public-v2 evidence does not repair or
accept that different legacy route. A supported source-policy interface or
properly reviewed authenticated source revision is a separate owner decision.
No upstream authenticated input or report is stubbed to obtain source
acceptance, and no pin is changed.

The source-owner review accepted the single C11 test amendment at `8fc4b6a3`;
independent review separately accepted its original facade/interposition intent,
the complete runner/checklist scope and the three-phase harness. A docs-only
successor corrected historical unchanged-test wording; the driver and comparator
then changed only their expected head/tree literals to the reviewed `8204a761`
tuple. Actual receipt-predicate negatives passed 1 / 15 and natural-process
predicate negatives passed 1 / 12 without loading Bootstrap.

| Fresh phase | Test result | Supervisor elapsed | Initialization/restoration |
| --- | --- | --- | --- |
| C11 facade standalone | 1 test / 14 assertions, zero failures/errors | 8,637 ms | One real Bootstrap manifest; declared roots restored |
| Manifest standalone | 1 / 48, zero failures/errors | 8,028 ms | One manifest; declared roots restored |
| Combined | 2 / 62, zero failures/errors | 9,034 ms | One manifest for both suites; declared roots restored |

These are independent fresh processes at the same source tuple. The comparison
first validates each actual supervisor exit/capture/cleanup/source record, then
compares per-namespace statuses, attempt flags, summaries, counters and complete
stdout/stderr. Only elapsed time is excluded. Equality holds. The unmodified
production batch-report validator also accepts all three serialized reports;
that consumption check loads no Bootstrap or provider. No timing speedup or
whole runtime read/effect closure is inferred.

The equivalence receipt SHA-256 is
`21279e022b004c9615a8213f19a0aafa628c0c30312ee6b4f0293dffde064b6f`.
The three full child receipt hashes, in table order, are
`676e40fdfdf3535b4db89ce348374aec7fb436046646634ab5a070345a0f4020`,
`5071acecf616bd32d7265e56b490b2da2a78e017888c3488d69fb38c33fa5c1a`,
and `57ab406be14a65689bd69cb0763347c0d1bf0ff4239cbb9d66c260193120224c`.
PR494-5's final integration gate is closed by the independently reviewed,
validated merge recorded in its criteria row above.
The earlier failed product attempt is never relabeled as this facade-only pass.

### RV2: compatibility execution-effect eligibility

At integrated PR494, the three-leaf whitelist in `run-child-unit!` limits
narrowed cache closures. It does not limit whole-repository result reuse:
the two compatibility selections' deterministic policy flags also pass the
separate `reviewed?` condition. A hit can therefore skip their Bootstrap
initialization. The existing source/resource inventories do not establish its
complete runtime read/effect closure.

The child runtime guards already constrain startup resources, loaders,
classpath, basis, environment and properties. Those constraints are meaningful;
the callback shape check alone also does not demonstrate impurity in the
default fresh child. This is an unproved eligibility boundary, not a measured
stale hit or proof that every repeat is necessary.

The correction keeps exactly these two inventoried compatibility selections
fresh, including mixed and combined selections, before startup-resource,
runtime-basis, snapshot or result-cache observations. It leaves policy metadata,
the three reviewed pure-leaf paths, PR494's bounded initialization sharing and
operation errors unchanged. Its receipt reason is
`:unreviewed-bootstrap-initialization-effects`. No new effects certificate,
global JVM audit or broad namespace whitelist is introduced.

The finite acceptance boundary is the actual child entrypoint: both named
selections, either order and mixed with a supported leaf execute the supplied
operation with zero lookup/basis/snapshot observations; success and error
outcomes and thrown errors survive; the three supported leaves still reach
their existing runtime observer. These injected controls do not construct a
Bootstrap product.

The new control passed 1 test / 67 assertions at exact source
`c6a94ef46acb39fab135729a3dfb1735ce0d2aca`, tree
`015d41040b90166aa23e2cc7da9c7926342ddc42`. The complete wiring regression
namespace passed 51 / 633. Both processes exited naturally 0 at 256 MiB,
with identical clean source bookends and Bootstrap absent. Boundary controls
passed 6 / 11 and documents passed 240 / 19. The first invocation used the
repository runner alias with a script argument and failed before tests; that
nonpass is retained. Source, retained-result and integration review remain
required for this successor; no compiler/provider acceptance follows.

The first configured CI attempt on `6a85dc57` failed its static job on the
existing temporary-root fixture macro's unresolved `base` binding. Baseline
and current lint reported the same error; the all-errors gate was preserved.
The test-only correction expresses the fixture as a function with explicit
callback bindings and preserves its permissions, return value and `finally`
cleanup. It changes no production guard or lint policy. Local clj-kondo
2025.10.23 reports zero errors/warnings for that test source; renewed wiring
validation at `f4e8624ad8cd79010aaab6ed0cc16388404511c2`, tree
`5b85853014fa9bbdcf855c4c6b08617aa05f0eac`, passed 51 / 633, natural exit 0
at 256 MiB with identical clean bookends and Bootstrap absent. A non-evaluating
source comparison confirms all 43 callback bodies and other forms are unchanged
after removing only the explicit wrapper; reader-generated argument names and
regex objects are compared as source data. Its initial object-identity
comparison was invalid and is not acceptance evidence. Exact successor review,
current-head configured CI and integration remain required.

Closing unsupported admission does not close original RV2's redundancy
criterion. Complete compatibility read/effect closure and a reviewed reusable
representation with real producer/hit/invalidation evidence, or a complete
source-grounded necessity decision, remain open. RV4 still depends on that
criterion and the separately held RV1 correction. The uncertain RV1 operation
and candidate are untouched.

### RV2: literal namespace dependency regression fence

The three existing explicit leaf closures now have a non-evaluating regression
control over the literal `:require` and `:use` declarations in their declared
Clojure inputs. Every repository namespace must resolve to exactly one source
or test path already present in that closure. A newly imported helper, missing
helper, source/test shadow or unsupported dependency spelling fails the control.
Synthetic mutation controls distinguish an unchanged declaration from an added
undeclared dependency and its explicitly declared successor; reader evaluation
is disabled. This control grants no cache admission and does not resolve
dynamic loads, callbacks, initialization effects or transitive host behavior.

Its finite acceptance is the three current declarations plus the mutation,
unknown/shadow and non-evaluation negatives, independent exact review and
integration. Validation and review are pending on this candidate. The held
compatibility cohort and ACC13 Syntax seam are untouched; original RV1/RV2/RV4
residual criteria remain open.

The first actual control run on `c2da65a2`, tree `b7925f8a`, failed: two
tests, 66 passes, one failure, zero errors, natural exit 1 at 256 MiB with
Bootstrap absent and clean source bookends. It identified a concrete missing
dependency: the current Reader identity test requires and calls
`gravity.c2-lexical-validation`. Its facade and three implementation namespaces
were absent from the explicit closure. The correction binds those four files
and permits only the three exact nested lexical paths for this existing test.
No production implementation or other namespace admission changes.

Additional injected cache controls require miss/hit conservation followed by
comment-only mutation invalidation, a changed renewed key, and missing-file
rejection for each lexical input, without executing any compiler or leaf body.
These controls test closure mechanics, not a new real producer proof. Earlier
producer evidence retains its original consumed-source attribution; it is not
retroactively evidence for this corrected closure. Renewed controls and
independent current-source/result review remain required.

The omitted import entered the current test through PR523's depth regression;
the production facade itself did not acquire that dependency. The corrected
control run on `baa6319d`, tree `a1bd36d5`, passed 3 tests / 103 assertions,
natural exit 0 at 256 MiB, Bootstrap absent and clean bookends. These are
injected cache-mechanics controls. The planner successor also preserves the
lexical facade's existing paired owner when admitting its three exact nested
paths; missing ownership and an unknown nested path remain refusals. Its
renewed planner and control results are pending.

The next exact-source run on `fdab1acd`, tree `1145c8a3`, retained three
passing controls (103 assertions) but the planner composition test errored on
main's existing unknown `private-collection-chain-test` catalog entry. Overall:
4 tests, 103 passes, 0 failures, 1 error, natural exit 1, Bootstrap absent.
That separate owner's registration remains an integration dependency; the
candidate neither whitelists the namespace nor changes fail-closed catalog
behavior. A direct owner-record control is added without weakening the full
planner test, which must pass after the catalog dependency is integrated.

The direct owner-record successor on `0863b599`, tree `4d0c1bc8`, passed
4 tests / 118 assertions, natural exit 0 at 256 MiB, Bootstrap absent with clean
source bookends. Docs passed 240 / 19. Static lint found zero errors and two
existing planner warnings, which remain visible. The source reader successor
also rejects caller-provided tagged readers without invoking their callbacks.
Independent review, the catalog-dependent planner test, a separately reviewed
real leaf producer proof for the expanded closure, and current-main integration
remain open. This finite correction does not close whole RV2 or RV4.

Final focused controls passed on `05af79b50728003d2d83d7b94caf329125a5a685`,
tree `71b903903dc4e0ec894821f4bb67625b21be3032`: 4 tests / 120 assertions,
natural exit 0, actual 268435456-byte heap, Bootstrap absent, no compiler
launched and identical clean bookends. This includes tagged-reader callback
nonexecution. The final log SHA-256 is
`76c423cfccf416fde13efa79876f7168c56b30b266f524094dc6a9f283b529c1`.
The two lint warnings match baseline `cbf5c210`; no suppression was added.
This documentation-only successor retains that source attribution. The full
catalog-dependent planner test, independent review and real leaf producer
evidence remain pending; no historical failure is replaced by these passes.

Current-main composition at `ef9e14dc0846c160a599704ae513a2cde14bfe3d`,
tree `bece619a8a39c8a4ad59e4bca1a841f86da259dc`, normally merges the
preserved `8406358d` Reader correction with main `2855b6cc`. PR537 supplies
the already integrated PC registration and independently adopted SH ownership
source-version metadata. The full affected planner suite passed 63 tests /
619 assertions, including the catalog-dependent Reader identity closure check;
the three literal-import and injected lexical invalidation controls passed
3 tests / 105 assertions. Both results had zero failures/errors, natural exit
0 at 268435456-byte heap, Bootstrap absent and identical clean source bookends.
Guarded semantic launcher call counts were zero. The planner's existing
namespace-listing child was metadata-only; cache/classpath operations remained
injected and isolated test fixtures did not acquire the shared host broker.
The new control log SHA-256 is
`d91d6e8eaad031e02c89466879e43dc9a1d6e65e5892e49fedd735356b5532a9`.
An initial harness invocation lacked the test classpath and exited before
loading the controls; its separate failed receipt is retained.

This closes the affected catalog/planner control gap on that composition only.
The original missing-input/catalog failures, `05af79b5` control attribution,
`8406358d` static review and separate unpublished `e912852e` variant remain
history. Independent exact composition review and current-head CI/integration
remain required. Real expanded-closure producer miss/hit/per-input invalidation
with actual runtime/classpath/source identities remains OPEN and subject to its
separate review and runtime admission. No semantic job, runtime admission or
whole RV1/RV2/RV4 acceptance follows from this compiler-free result.

### Reader identity fresh-only correction

The independent final integration assessment of `baa6bc48` is HOLD: its active
expanded cache declaration requires real producer miss/hit/per-input
invalidation evidence with actual runtime, classpath and source identities.
Planner controls, injected operations and successful CI do not discharge that
gate, and earlier smaller-closure evidence cannot be promoted.

The separable correction sets `:freshness-required? true` for only
`gravity.c2-artifact-identity-test` and marks its planner cache closure
unauthorized. Its four lexical inputs, literal dependency fence, paired owners,
unknown-path refusals and declared-input validation remain. Parent request
policy preserves the catalog's fresh flag despite false caller options. Child
selection reads that actual catalog policy and executes fresh without preparing
cache or effective-runtime state. Other namespaces retain their policies.

The new compiler-free controls must demonstrate repeated non-cacheable parent
misses without storage, repeated fresh child operations without cache/runtime
setup, and rejection of each deleted lexical input before execution. The
existing hypothetical admitted-policy cache-mechanics controls remain tests of
the retained declaration, not production cache admission or real producer proof.
This fresh-only successor requires its own exact independent review and CI;
the original HOLD remains valid on its original candidate. Future cache
admission and whole RV1/RV2/RV4 acceptance remain OPEN.

### PR529 supersession and remaining RV2 evidence

[PR529](https://github.com/burn2delete/uniform/pull/529), source
`8406358dd2b353964299b5f4ad5543b067efbf4c`, tree
`a64144d9499c516d9cc7b126e0930623e294913f`, is superseded by the integrated
fresh-only correction in [PR538](https://github.com/burn2delete/uniform/pull/538).
The actual merge is `9b74ed2cb07465a2f47d2a87f618033fab4f7302`, tree
`2a70190954586219858ba384adb750bab02ac86a`, identical to independently
accepted source `db1026230c926515975d31e3f8cfc214fda53954`. This is semantic
supersession of the finite correction; PR529 itself was not merged. The
original branch, static review, `05af79b5` controls, missing-input/catalog
failures, `baa6bc48` active-cache HOLD and separate `e912852e` variant remain
preserved at their original identities.

| Original PR529 outcome | Disposition in merged PR538 |
| --- | --- |
| Bind the consumed lexical facade and its three implementation files to the Reader identity declaration. | Incorporated. The complete C2 input declaration is retained unchanged; it is audit metadata while fresh execution is required. See the [merged dependency contract](https://github.com/burn2delete/uniform/blob/9b74ed2cb07465a2f47d2a87f618033fab4f7302/contracts/stage0-incremental-test-dependencies.edn). |
| Admit only the three named nested lexical paths under the existing paired owner; reject missing/wrong owners, wrong roles and unknown paths. | Incorporated. The ownership validator and paired-owner regressions remain. The planner retains the closure but marks narrowed reuse unauthorized under the fresh policy. |
| Check literal imports for the three existing declarations and reject undeclared/missing/shadowed helpers or unsupported forms without evaluation or tagged-reader callbacks. | Incorporated. The literal-import parser and its positive/negative controls are retained. This fence still grants no cache admission or dynamic/effect closure proof. |
| Preserve injected miss/hit, comment-mutation/key invalidation and deletion controls for all four lexical inputs. | Incorporated as hypothetical admitted-policy mechanics, not real producer evidence. The original control bodies and source attribution remain; the fresh successor also checks deleted inputs through input revalidation without a broker or execution. |
| Pass the actual catalog-dependent Reader planner check without bypassing unknown-catalog refusal. | Incorporated. PR537 supplies the separately integrated PC registration; the retained full planner suite passes 63 tests / 620 assertions. No unknown namespace is whitelisted. |
| Obtain exact independent source/result/composition review, current-head CI and integration. | Completed for the fresh-only successor only. Exact changed-head review, committed readback and separate final integration acceptance bind `db1026230` and its tree. [CI40771247212](https://github.com/burn2delete/uniform/actions/runs/40771247212) passed seven routine jobs with two optional skips. Focused fresh/dependency/compatibility controls passed 6 / 171 and the language boundary passed 6 / 11, with zero broker or semantic launches. |
| Make the expanded declaration available for narrowed cache reuse. | Intentionally disabled by the safer successor: catalog `:freshness-required? true`, planner authorization false, parent non-cacheable/no storage, and child fresh selection before cache/runtime preparation. This does not satisfy the real reuse outcome. |
| Demonstrate a real expanded-closure producer and independently accept reuse/invalidation with actual runtime/classpath/source identities. | Unimplemented and OPEN under original RV2 and RV4 below. Earlier smaller-closure results and injected controls cannot discharge this obligation. |

The stable task truth remains **RV2** and **RV4** in the governing table at the
top of this document; no new task population or completion percentage is
created. The current residual for the expanded Reader declaration is:

- Independently review an exact real-producer method for the four retained
  lexical inputs and the complete consumed runner/contract/runtime closure.
- Subject to separate runtime admission, observe a real cold miss/producer,
  unchanged-selection hit with original producer provenance, and unrelated
  documentation/existing-test-body reuse only when every consumed input is
  unchanged. Record the applying revision separately from the producer.
- For each lexical facade/operations/primitives/product input, prove mutation
  invalidates the old key/result and requires a fresh real producer; retain
  missing-input, ownership, shadow, inventory and fresh-policy rejection.
- Bind actual runtime, ordered classpath, source/contract identities, clean
  head/tree and complete result bookends, then obtain independent result and
  exact integration/admission review before clearing the fresh gate. No
  historical tuple is relabeled as this expanded-closure execution.

These are the original RV2 producer/reuse obligations and RV4 evidence
obligations, not an optional follow-up. The [fresh-only correction](#reader-identity-fresh-only-correction)
and [merged controls](https://github.com/burn2delete/uniform/blob/9b74ed2cb07465a2f47d2a87f618033fab4f7302/bootstrap/clojure/test/gravity/self_hosting/sh01_development_loop_wiring_test.clj)
preserve a non-admitting boundary while they remain open. The broader RV2
compatibility read/effect/representation or necessity decision, RV1's
transitive mutation/equivalence or necessity decision, and whole RV4 remain
open. Superseding PR529 neither executes those cases nor transfers restricted
warm evidence or uncertain operations.

### RV2/RV4: offline Reader producer evidence driver

`gravity.build.producer-evidence`, under `bootstrap/clojure/test/gravity/build/`,
checks retained public Reader producer captures without starting a producer,
loading Bootstrap, acquiring a broker, probing processes, or reading cache
state. This is the next bounded implementation of the existing RV2/RV4 evidence
criterion. The current catalog freshness policy and production runners are
unchanged. No real expanded-closure producer or reuse result is supplied here.

The driver reads exactly three explicitly supplied local files: a capture-specific
method manifest, the dependency contract, and the capture envelope. The caller
supplies the method's SHA-256 from independent exact-artifact review. The method
pins the other two files by their raw byte digests. Replacing a capture and
recomputing its own hash cannot satisfy the independently pinned method. The
capture-specific manifest is instantiated after capture; it is not the separate
pre-execution method review or runtime admission required above.

The closed `:gravity/reader-producer-method-v1` map has `:contract-sha256`,
`:capture-sha256` and `:binding`. The binding contains:

- `:source`: exact Git `:commit`, `:tree`, complete `:repository-identity` and
  `:clean? true`;
- `:inputs`: every declared Reader input, including all four lexical files,
  consumed runners, tests and contracts, in declaration order, retaining
  `:path`, `:role` and the runner's framed `:sha256` identity;
- `:inventories` and `:shadow-inputs`: the pinned runner's complete material,
  including absent-path identities, used to recompute the existing explicit
  `:consumed-input-identity` without changing its encoding;
- `:classpath-identities`: the actual ordered child classpath identities;
  `:runtime-tool-inputs`: identified runtime/tool bytes; and
  `:child-runtime-identity`: the actual-child runtime fingerprints, including
  basis, basis file, configuration, JVM arguments, environment and properties.

The `:gravity/reader-producer-capture-v1` envelope repeats the binding in
`:binding-before` and `:binding-after`, retains `:producer-provenance` separately
from `:applying-source`, and holds the complete emitted batch report as
`:report-edn`. Its `:terminal` requires natural exit zero, no timeout, complete
stdout/stderr, completed cleanup and a dead process root. The report must select
exactly the Reader identity namespace, attempt it successfully, reconcile its
nonzero test/assertion totals and retain untruncated namespace output. Missing,
changed, mismatched, tagged, trailing or incomplete evidence is rejected with
`PRODUCER-EVIDENCE-INVALID` and a stable `:field`; malformed EDN/UTF-8 also fails.

Invoke only on explicitly supplied unrestricted artifacts:

```bash
clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.build.producer-evidence \
  REVIEWED_METHOD_SHA256 METHOD_EDN CONTRACT_EDN CAPTURE_EDN
```

A successful result says `:status :consistent`, `:cache-admission? false`,
`:real-evidence-accepted? false` and `:producer-executed-by-driver? false`.
The checker compares supplied observations; it does not independently measure
or authenticate a child, discover effects/resources, establish that the supplied
shadow inventory is complete, or prove that terminal observations occurred.
Independent source/method and retained-result review must establish those facts.
The controls use explicitly synthetic captures and do not count as real evidence.
Real cold miss/producer, unchanged-selection hit with original provenance,
unrelated documentation/test-body reuse, each lexical input's real invalidation,
actual runtime attribution and final independent admission remain OPEN under
RV2/RV4. Broader RV1 and compatibility closure obligations are unchanged.

#### Complete captured output and frozen operational requirements

The offline checker follows the existing batch runner's captured-output format:
`:bytes`, `:observed-bytes` and `:limit-bytes` must be nonnegative integers,
retained bytes cannot exceed the recorded capture limit, and complete output
requires equal retained/observed/UTF-8 byte counts with `:truncated? false`.
The initial driver omitted the capture-limit checks and could accept a missing
or negative limit on an otherwise complete-looking record. The correction
rejects those records through `PRODUCER-EVIDENCE-INVALID` / `:complete-output`,
even when a synthetic method is repinned to the malformed capture. This is
validation of the actual host capture record, not a compiler work allowance.
Focused stdout/stderr controls retain valid exact-limit multibyte UTF-8 output,
reject missing/mismatched metadata, and round-trip the public result with
unchanged input byte hashes and a recorded serialized output hash.

The existing RV2/RV4 operational evidence requirements remain fixed at this
seam; the capture-specific method pins the source, declaration, runtime,
ordered classpath, producer/applying provenance and terminal observations
already specified above. A successful offline check never substitutes for the
following observations:

| Existing required observation | Exact evidence still required |
| --- | --- |
| Real cold miss and producer | Separately reviewed/admitted operational method; actual child execution and its original producer provenance, input/key identities, complete report and natural terminal bookends. The current driver accepts only fresh non-cacheable reports and does not validate an admitted miss. |
| Unchanged-selection hit | Same consumed input, runtime and ordered classpath identities; no newly executed tests; original producer provenance retained and applying source recorded separately. The current driver rejects a hit report. |
| Unrelated documentation/existing-test-body reuse | Exact applying source delta; unchanged consumed declarations, runner/contracts, inventories, shadows and runtime/classpath; original result/provenance retained. A declared test or contract mutation is not unrelated. |
| Each lexical input's invalidation | Separate facade, operations, primitives and product mutations; changed identity/key and refusal of the old result, followed by a separately observed real fresh producer. Hypothetical repinning or synthetic failure checks are not these observations. |
| Failure, missing input and tampering | Nonpassing terminal/report outcomes, absent or changed bytes, wrong owners/shadows/inventories, and mismatched provenance must refuse admission. No successful report or hash hides a failed or incomplete process. |

The next operational gate is independent review of the exact public expanded-
closure capture method and its observer/serializer, including how it obtains
actual runtime and terminal facts and represents the later miss/hit cases,
followed by separate runtime admission. No producer is authorized by this pure
correction. Fresh-only policy stays active; real execution/result review and
any later cache admission remain OPEN. The original 8/86 controls retain their
source attribution; the changed output guard receives focused new controls
rather than a repeated broad suite. Merged-commit CI40783760547 is terminal
cancelled and supplies no successful result; it was not rerun or cancelled by
this correction.

#### Source-defined producer snapshot serialization

The existing development wiring attaches its complete `:snapshot` as
`:test-producer-provenance :repository-snapshot`. The offline driver's initial
exact-map comparison instead required only `:repository-identity`, rejecting
those source-defined records unless their metadata was removed. The correction
accepts the existing closed snapshot shapes without changing their encoding:

- `:gravity/sh01-complete-repository-snapshot-v1` / `:full-content-v1`;
- `:gravity/sh01-incremental-repository-snapshot-v2` /
  `:git-index-full-content-v2`; and
- `:gravity/sh01-incremental-repository-snapshot-v2` /
  `:git-index-overlay-v2`, including tracked and overlay path counts.

These shapes come directly from `full-repository-snapshot`,
`full-index-repository-snapshot` and `incremental-repository-snapshot` in
`sh01_development_loop_wiring.clj`. Their required counters are nonnegative
integers, their schema/engine pairing is exact, and their repository identity
must match the pinned binding. Missing/extra fields, unknown schema/engine
pairs and wrong identities fail with `:producer-snapshot`. The legacy
hash-only v1 record remains a supplied declaration. Source revision, consumed
identity, ordered classpath, runtime and terminal checks remain intact.

The original snapshot metadata remains in the raw capture bytes pinned by the
method; no observer fields are dropped or reconstructed. An injected metadata
change changes the capture/method pin, and the old pin rejects it. Schema and
engine names identify serialization shapes, not proof that an observer ran.
All new controls supply these fields and terminal facts explicitly; none are
captured runtime observations. The consistency result retains
`:real-evidence-accepted? false` and `:cache-admission? false`.

This does not add a runtime observer or a new miss/hit format. The current
fresh-only wiring returns before producing the cache-enabled runtime/provenance
bundle; that operational observation gap remains. Miss/hit claims, failed
terminal records, and mismatched source/runtime/classpath provenance still
refuse. Independent review of the actual observer/serializer method, separately
admitted execution, real producer/reuse/invalidation evidence and result review
remain the original RV2/RV4 gates. No schema-shaped fixture discharges them.

#### Pure adapter for fresh returns and supplied observations

`serialize-fresh-capture` prepares the existing capture-v1 bytes from a fresh
return plus an explicit supplied map containing `:binding-before`,
`:binding-after`, `:producer-provenance`, `:applying-source` and `:terminal`.
It validates the declared dependency contract and the same binding, report,
output, snapshot, provenance and terminal rules as `verify-capture!`. It calls
no observation function or operation and returns no method, trusted review pin,
authentication result or admission. New serialized bytes need their own pinned
method and independent review; existing pinned artifacts are never rewritten.

The source-backed fresh-only return can omit runtime/provenance metadata.
This adapter requires those inputs explicitly instead of deriving observations
from a report or silently replacing original producer metadata with applying
metadata. If a return already has `:test-producer-provenance`,
`:test-applying-revision` or `:test-applying-repository-snapshot`, each must
match the supplied fresh capture. Both preparation and offline verification
reject contradictions with `:report-provenance`. This closes an injected
counterexample where a valid-looking outer envelope concealed a conflicting
producer inside `:report-edn`. Missing supplied fields, bad types or identities,
changed bookends, incomplete output/terminals and miss/hit claims still refuse.

All new tests supply their snapshot, runtime and terminal values. They do not
measure or authenticate them. Complete snapshot metadata and ordered identities
survive the round-trip and raw byte pins remain explicit. The normal wiring is
unchanged: its fresh-only branch still does not acquire the missing observation
bundle. Connecting a real observer/serializer requires exact method review and
separate runtime admission. Real producer, miss/hit/reuse/per-input invalidation
and result/admission evidence remain OPEN; fresh-only policy is unchanged.

#### Opt-in child observation connected to fresh serialization

The existing child development-cache request can explicitly include
`:observe-producer? true` for exactly `gravity.c2-artifact-identity-test`.
`run-child-unit!` then observes the actual supported child runtime, declared
input bytes, inventories, shadows, ordered classpath, runtime tools, source
revision and complete repository snapshot before and after its existing fresh
execution. It attaches those observations as `:test-producer-observation`.
Unknown runtime, missing declaration, a non-fresh policy, unsupported selection,
dirty source or changed bookends rejects the requested capture. The observation
does not mark the closure cache-authorized or alter the fresh-only decision.
Without the explicit option, existing child behavior is unchanged.

`serialize-observed-fresh-capture` consumes that attached child bundle and
requires separate terminal observations. It preserves the report and producer
snapshot and applies the existing fresh-capture validation; missing child
observations or incomplete terminal facts refuse serialization. The child
cannot observe its own eventual natural exit, output completion or descendant
containment. Neither this serializer nor a schema-shaped report proves that
those facts were observed by a parent. The actual parent observer/finalizer,
exact method review and separately admitted operational evidence are still
required. A root-exit flag alone does not establish owned-descendant cleanup.

Focused controls inject observations into the real child-to-serializer path
and check operation ordering, unchanged fresh policy, drift and unsupported
runtime/selection refusal, complete metadata preservation and terminal refusal.
These are source/injected controls, not a real producer or cache-reuse result.
Real miss/hit, unrelated reuse, each lexical input's invalidation, failed/tamper
controls, independent retained-result acceptance and RV2/RV4 adoption remain
OPEN. No genuine comparison, runtime retry or cache-policy admission is part
of this source increment.

#### Parent ownership adapter with unresolved reachability

`gravity.build.parent-producer-observation/observe!` accepts a newly launched
Process and forwards the caller's exact options to the canonical runner's
three-argument `process-result`. It retains one raw ownership token and passes
that same token exactly once. The runner owns wait, capture and finalization,
including exception cleanup; the adapter neither repeats finalization nor
reconstructs ownership from PIDs. Returned raw token/handles are local parent
state, not serializable evidence. The full original process result survives.

Root exit, tracked-owned-process completion, global cleanup and descendant
coverage remain separate fields. The selected C2 workload's complete
source-created child set has not been established by this increment. Its
reachability is explicitly unresolved even when every retained handle is
terminal. Capture-v1 readiness and cache admission remain false; the adapter
does not call the serializer with tracked completion renamed as cleanup.

Injected controls check exact token/options forwarding, single consumption,
unknown/failed terminal preservation and original exception propagation. No
producer launch, process/holder probe, held-operation retry or runtime admission
is included. An executable method still needs exact source/loader, bounds and
output destinations frozen and reviewed, plus source-grounded reachability
resolution before capture-v1 serialization. All original RV2 real-producer,
miss/hit, unrelated reuse, per-lexical-input invalidation, provenance and
fresh-policy acceptance obligations remain OPEN.

#### Fresh workload reachability and startup attribution

The source trace below starts from the `:test` main entrypoint in `deps.edn`,
with exactly `gravity.c2-artifact-identity-test`, a contained `result.edn`, and
`:observe-producer? true` in the existing development-cache options. It covers
the observer surrounding execution, rather than treating the test body as the
whole workload. This is a source assessment, not an observed execution.

| Reachable source boundary | Source result |
| --- | --- |
| `self_hosting_test_runner.clj` namespace initialization | Requires EDN, I/O, string, test and component-dependency namespaces. Its selectable namespace set calls `dependency-index`, which reads and validates the dependency contract; it does not require all selectable tests. Dedicated test discovery inventories paths. |
| `-main` optional wiring load | `requiring-resolve` loads `sh01_development_loop_wiring.clj`, including basis, digest, component dependencies, cache and broker namespaces. Cache/broker initialization defines constants, functions and process-local atoms; loading them does not itself acquire a broker lease or run a producer. |
| `observe-fresh-producer!` bookends | Each `observe-fresh-producer-binding!` calls `effective-child-runtime`, `prepare-context`, declared-file/inventory reads and `run-git-bytes!` for clean status. `prepare-context` also creates/checks private coordination directories; fresh-only does not mean no observation I/O. |
| Repository identity subprocesses | `git-repository-root`/`git-common-directory`, `incremental-observation`, full-snapshot fallback and revision collection call the same `run-git-bytes!` port. Commands include `rev-parse`, `ls-files`, `config`, `check-attr` and `status`. This port constructs `["git" ...]`, starts a ProcessBuilder, writes stdin on a daemon thread, drains merged output, joins the writer and waits for the root. It does not retain a canonical ownership token or descendant finalization record. |
| Selected test loading and callbacks | `run-one-namespace` requires the selected namespace immediately before `clojure.test/run-tests`. The test requires both C2 facades; identity requires its five extracted modules, lexical validation its three modules, with `clojure.string` in lexical product. These bodies perform collection projection, hashing, span and graph checks. The test's operation callbacks and printer interpositions exercise those bodies, local atoms and explicit exceptions; the callback APIs are not general effect restrictions. |
| Report and parent boundary | The child atomically publishes its validated report, prints its summary and calls `shutdown-agents`. The merged parent adapter receives an already launched Process, retains one token and delegates wait/capture/finalization to the canonical runner. The serializer consumes a report and separately supplied terminal facts; it neither launches nor discovers descendants. |

The identity facade, its five modules, lexical facade, its three modules and
selected test have no direct subprocess launch in their reviewed bodies. That
finite finding does not close initialization, Git's own execution or arbitrary
callback/host behavior. In particular, the Git helper inherits its executable
search and configuration environment. `base-runtime-identities` identifies
Clojure, Java and the test command, not the executable launched as bare `git`.
Child environment/property fingerprints and repository byte identity do not
establish Git's effective executable/configuration or its descendant set.
There is no complete source-created child-set proof for the whole workload.
Root death and tracked-handle completion therefore still leave global cleanup
and selected-workload descendant reachability unresolved.

The trace also found a concrete attribution gap: the fresh-only branch returns
before the ordinary reusable-child startup snapshot check. Equal fresh
observer bookends could therefore describe a new clean snapshot after runner
code had loaded from an older prelaunch snapshot. The fresh observer now
compares its context snapshot with the parent's supplied repository identity
before collecting the binding. Missing or changed identity rejects with
`SH01-DEVELOPMENT-LOOP-SNAPSHOT-RACE` and `:input-kind :child-startup`.
Injected controls cover both refusals and the matching-identity path; they do
not launch Git or the selected producer.

An executable capture method remains unprepared: its exact resolved launcher,
Java/basis/ordered classpath and startup inputs, Git execution/configuration
and lifecycle scope, parent/child bounds, and unique contained report/capture
destinations must be bound together. The child report writer uses atomic
replacement of contained `result.edn`; it is not a one-shot capture publisher.
The parent adapter does not yet read that report or publish capture bytes.
Resolving those concrete gaps requires affected method review before a
separately authorized execution. This source correction does not establish
descendant coverage, set capture readiness, enable cache admission or satisfy
real miss/hit, unrelated reuse or per-lexical-input acceptance.

#### Selected observer Git ownership without protocol replacement

`gravity.build.producer-git-observation/run-selected!` is a direct source seam
for the exact fresh C2 selection. It supplies lifecycle hooks only while each
`observe-fresh-producer-binding!` bookend executes. The selected test operation
runs outside that thread binding, and normal callers bind no hooks. The new
module is an explicit runner input in the C2 declaration, with a path allowance
restricted to that selection; freshness-required remains true.

The Git port here is the existing ProcessBuilder implementation, not
`clojure.java.shell/sh`. Its command vector/order, inherited environment,
working directory, merged stderr, daemon stdin writer/close, complete byte
drain, writer join, root wait and existing nonzero/input-error diagnostics are
preserved. The ordinary runner's bounded text capture is not substituted:
that would change complete byte output, merged-stream behavior and decoding.
Only Git refusal output is decoded as before, using Java's UTF-8 replacement
behavior. A source-form oracle retained from `40ecbb97` replaces only process
construction with a fake and compares malformed UTF-8, NUL, output beyond
1 MiB, stdin and nonzero-exit behavior. No subprocess is launched by it.

Each launched Process is recorded before canonical retention is attempted.
The same raw token is retained through root wait and one canonical finalizer
call, including drain/writer/wait/error paths. Retention failure leaves the
actual Process recorded with an unproven terminal; it does not invent a token
or infer cleanup. Original errors survive ordinary cleanup failures as primary
objects with suppressed details. Interruption is restored, and VM, linkage or
ThreadDeath failures from cleanup are not demoted to ordinary Git refusals.
Root-wait exit, raw handles and the finalizer's tracked/global-unknown facts
remain separate local records. No PID lookup, global process registry, inferred
release permission or descendant-complete claim is added.

`freeze-binding!` and `verify-binding!` read an explicit method-supplied Git
executable, dispatch files, selection links and configuration/absence witnesses
before and after each command. PATH selection is rechecked; inherited
environment values are represented by a hash. File reads compare metadata
around hashing. Symbolic-link targets are retained, while linked directory
contents are explicitly unbound. These are filesystem observations only; no
Git discovery command is introduced. The explicit file set does not prove
complete Git configuration search/includes, native dispatch/helper creation,
or protection against change-and-restore between observations.

Injected controls cover launch, retain, stream, wait, cleanup, interruption,
identity drift and bookend scope restoration. The ACC13 implementation was a
source reference only; its request, admission, roots, profile and review are
not RV2 authority. The source seam now has the opt-in supported entrypoint
described below; its exact operational launch remains unfrozen. Git's
inherited root wait/full-stream protocol still needs the
exact outer watchdog binding; this patch does not claim a bounded producer
method or termination of unobserved helpers. Exact executable configuration,
native/helper reachability, loader, watchdog and report/capture publication
remain method gates. Capture readiness, cache admission and the original real
producer/reuse/invalidation acceptance criteria remain unchanged and open.

#### Supported opt-in child entrypoint and retained Git observations

The existing `--development-cache` request now also accepts
`:producer-git-binding` with schema `:gravity/rv2-git-binding-v1`, together
with `:observe-producer? true`. This route requires exactly
`gravity.c2-artifact-identity-test`; another or mixed selection, a missing
opt-in, malformed root, unknown schema, or extra top-level lifecycle-hook data
is rejected before dispatch. The actual binding is still revalidated against its explicit
filesystem inputs before each Git command. Parsing this data is not method
approval or executable authentication.

The existing `self-hosting-test-runner` main entrypoint resolves
`producer-git-observation/run-selected!` only for that explicit request.
Requests without the binding use the existing wiring route. Thus this source
route retains the normal main/basis/argument checks instead of introducing an
alternate `-e` launch that the supported-runtime observer would reject.
The parent-prelaunch identity and all fresh-only checks remain required.

The supplied Git binding is included as
`:selected-observer-git-binding` in the observer's runtime-tool identities.
The report also retains `:test-producer-git-observation`: the frozen binding,
per-command argv/CWD, root-wait result and original finalizer observations.
Raw Process, ownership token and handles remain in local child records;
Throwable values are represented only by class names in the serializable
view. Root exit and tracked completion do not change unknown descendant
coverage. The observation explicitly keeps capture-v1 readiness and cache
admission false. Existing report serialization retains the extra observations
without turning them into evidence of a real run.

Injected controls cover the actual main dispatch, closed options and exact
selection, binding identity inclusion, preservation through EDN round-trip,
raw-token exclusion and unchanged default route. No selected producer or Git
bookend workload is launched by these controls. A supported source entrypoint
is now present; exact launch/configuration, native/helper reachability, outer
watchdog, output/report/capture publication, independent method review and
separate runtime admission remain open. Real miss/hit, unrelated reuse,
per-lexical-input invalidation and independent result acceptance remain the
original RV2 obligations.

#### Retained parent observations connected to capture serialization

`parent-producer-observation/serialize-capture` consumes the child report,
dependency-contract bytes and the retained parent observation returned by
`observe!`. It maps the normal runner's exit/wait, root and per-stream
capture-status/error/truncation/forced-close fields to the existing terminal
shape, then calls `serialize-observed-fresh-capture`. It copies
`:cleanup-complete?` from the original process result unchanged. It never
substitutes `:tracked-owned-processes-terminal?` or a root-exit flag for global
cleanup, and never waits or finalizes the token again.

Current canonical parent results have global cleanup nil on natural completion
and false on containment paths. They therefore refuse capture-v1 serialization
even when root exit and tracked-handle completion are both true. Unknown
descendant coverage and readiness/cache-admission flags remain unchanged in the
retained observation. This source connection removes manual terminal-map
construction; it does not supply the missing workload/containment proof.

The injected positive supplies a synthetic global-cleanup=true result solely
to check byte/report preservation through the existing serializer. It is not a
fact produced by the current runner or evidence that the method is ready.
Negative controls retain the current nil-global/true-tracked case, failed or
missing wait/root/stream facts, timeout, forced close, truncation and stream
errors. Raw ownership stays local. Report-file reading, one-shot output
publication, exact launch/loader/watchdog binding, native/helper reachability,
independent method review, separate runtime admission and all original real
producer/reuse/invalidation evidence remain open.

### RV1: retained checks at the live Resolve/Reader/AE boundary

Source assessment: `dfa4c5b653d8049ac78dd30962172e0d91c768e2`.
This is a necessity disposition for the current interfaces, not new runtime
acceptance. No verification call is removed. A check's semantic input can be
necessary while its repeated calculation remains an unresolved elision
candidate. The short construction root catalog establishes neither purity nor
complete transitive dispatch identity.

#### Actual producer and consumer closure

Paths below are relative to `bootstrap/clojure/src/gravity/`.
`components/` means `bootstrap_impl/components/`; `late/` means
`bootstrap_impl/semantic_late/`. Unqualified numbered `008xx` filenames below
are inside `components/self-hosting-sh03-sh07/`.

1. `components/self-hosting-sh03-sh07/resolution-construction-inputs.clj`
   checks the native carrier, captures thread, transaction, Reader invocation
   and Resolve operation binding, observes dependencies, then executes
   `sh06-authenticated-sh05-parent!`. Its successful short-seam route supplies
   the base request without a second immediate parent gate. Unavailable
   optional observation takes the existing full base-request route; it is not
   a successful stability observation. The observations perform file reads
   and can initialize plan caches. Their exceptions are contained as
   `::unavailable`, except interruption, which propagates with its flag set.
   The two post-gate checks bracket base-request construction, which parses
   forms and constructs imports, definitions and analysis inputs. The latter
   check detects drift during that intervening work; deleting it loses that
   observation. Neither endpoint comparison proves absence of an intervening
   change-and-restore or state change below the catalog.
2. `00874-sh06-resolution-build-binding.clj` routes
   `sh06-resolution-verify-sh05` to the actual Macro public verifier, passing
   the current invocation when present. `00880-...-verification-02.clj`
   additionally checks Macro identity/proof/provenance and the C3 owner.
   The later `00879-sh06-resolution-artifact-verification-bounded-02.clj`
   executes that Macro verifier again before Resolve replay. It requires
   `:fresh-upstream-sh05-verification?`; a failed parent prevents that replay.
   `00880-sh06-resolution-artifact-verification.clj` uses this body for
   construction proof and caller-requested public verification, with different
   missing-proof modes. `00881-sh06-resolution-finalize-candidate.clj` already
   omits the immediate replay after installing the freshly generated proof.
   `00876-sh06-resolution-require-carrier.clj` also distinguishes Resolve
   template verification from resolved verification: digest/analysis
   resolution occurs between them and their arguments differ. Reader's
   entrypoint/verifier, Macro's template/resolved verifier and AE's
   builder/verifier are likewise distinct operations, not duplicate calls
   merely because they share an owner or plan.
3. The Macro public wrapper is `00866-sh05-macro-artifact-verification.clj`.
   Its implementation is installed by
   `late/150856-sh05-verification-install.clj`, which captures the **values**
   of four helpers: carrier, expansion-replay, provenance and proof. The
   matching four `150856-sh05-verification-*.clj` files are consumed code,
   even after their temporary Vars are removed by the cleanup file. Checking
   just the public wrapper's Var cannot enumerate those captured functions'
   transitive dependencies or mutable state.
4. The carrier helper checks explicit Reader options first, executes
   `sh05-macro-gravity-verifier-report`, reconstructs/verifies the Macro AE,
   obtains the current Macro binding and descriptor, then checks Reader
   lineage and C3 proof. `00861-sh05-macro-trace-identity-input.clj` executes
   both `sh05-verify-macro-template` and `sh05-verify-macro-resolved` for every
   stored run. `00858-sh05-macro-execute.clj` dispatches through
   `p15-s23-stage2-runtime-execute-function`, strips host metadata and owns
   diagnostic containment. The latter runtime entry is in
   `components/bootstrap-p15/00593-p15-s23-stage2-runtime-execute-function.clj`;
   it selects module-bundle or scoped execution and reaches further runtime
   instruction, lookup and primitive dispatch. None of that closure is
   certified pure by equality of Macro plan IDs.
5. Reader lineage reaches
   `components/compiler/00842-c3-syntax-stream-reader-products-authentic.clj`
   and `00847-c3-reader-artifact-view.clj`. The latter joins the retained
   Reader revision and actual AE descriptor to
   `sh03-reader-verification-invocation!`'s current binding. That function in
   `00813-sh03-reader-canonical-identity.clj` validates the closed invocation
   map and selected path. `00808-sh03-reader-expected-source-byte-count.clj`
   rereads the selected Reader bytes. The explicit-source arity of
   `00811-sh03-reader-plan-validate.clj` builds a current plan, measures its
   structure and rereads source; it is not just a lookup of an advertised
   hash. The ordinary lineage route also replays C3 stream verification,
   serialization/deserialization and Reader/C3 AEs. Registered literal
   projection and nil-boundary branches have their own conditions; a result
   for one branch does not authorize elision in another.
6. AE verification is the descriptor API in
   `components/bootstrap-p15/00750-p15-s23-c13-c14-b1-authority-token-018-p15-s23-stage2-sh02-authenticated-envelope-verification-report-through-p15-s23-c-backend-diagnostic-stage.clj`.
   `p15-s23-stage2-sh02-descriptor-envelope-verify!` reconstructs an expected
   envelope and requires strict structure and equality. The `-003-` sibling's
   `p15-s23-sh02-source-binding!` requires its lexically captured private
   authority and reopens/authenticates source and emitter inputs on every
   authority-bearing call. Only successful compilation may already be shared
   in the current `*p15-s23-stage2-plan-emission-context*`, keyed by exact
   authenticated inputs, policy and additional targets. The `-009-` sibling
   invokes actual `authenticated-envelope-build-template` and
   `authenticated-envelope-verify-template`; `-004-` dispatches them through
   the generic runtime. Compiled binding reuse is distinct from skipping
   fresh envelope verification or acquiring authority from saved DATA.

#### Necessary facts versus unresolved repeats

| Retained fact/check | Necessity supported by current source/evidence | Disposition of repeated work |
| --- | --- | --- |
| Live Macro/Reader/Resolve/AE source and executable plan agree at use | A retained digest or function object does not establish current source bytes, plan contents, emitter policy or target binding. Macro's cached delay and pinned-plan identity atom, Resolve's binding atom and source key, and Reader's explicit-source plan builder consume different state. | Keep current owner source authentication. No whole-seam proof says every repeated read/plan calculation is required; narrower plan/identity calculation reuse remains unresolved. |
| Reader invocation and branch selection agree | The selected canonical Reader path, exact `{:source :development-current}` options, current source/plan identities and retained C2/C3 revision must join. Nil invocation chooses the pinned route; the explicit route must not inherit its result. `00881` also joins the Reader argument to the invocation path. | Validation at public entry remains necessary. Repeating identical validation inside one invocation may be elidable only with a stable complete source/plan/dispatch closure and preserved error order. Map equality alone is insufficient. |
| Stored Macro run and parent still satisfy the owner | PR493's real after-Resolve mutation changes `:resolved-verification` in aliased array-map storage while preserving the same parent, invocation and expansion/digest operands. The subsequent actual owner rejects `:macro-run-storage-exact?`, and Resolve rejects before publishing a complete proof. | A whole earlier Macro PASS cannot replace the post-Resolve check. Run-storage shape is a proved necessary observation for that fixture; the result does not prove that unchanged replay operands require a second execution. |
| Run count, shortcuts, plan/version, grants, trace/output, graph, generated origins, run/path/Reader provenance, identity and exact artifact shape | These are the remaining Macro base checks in the expansion-replay, provenance and proof helpers. They compare current supplied fields, reconstructed products and lineage, rather than trusting the parent's logical ID. PR493 does not mutate every one of these fields. | Retain all checks. Their semantic obligations are explicit, but necessity of each repeated calculation is conservative/unresolved until its own inputs and mutation seam are established. No blanket necessity credit is inferred. |
| Current descriptor and freshly reconstructed AE agree | AE dispatch consumes actual stage/kind/descriptor/options, private owner authority, source/emitter inputs, binding and builder/verifier execution. An unchanged outer descriptor-verifier Var neither freezes its callees nor substitutes for fresh public verification. | Keep actual fresh public AE verification and source authentication. Possible compilation-only reuse is already scoped by the owner; removing nested reconstruction requires separate equivalence evidence and cannot turn a saved report into authority. |
| Public embedded capability proof matches current checks | PR477's exact Resolve predicate permits a missing construction proof but rejects missing/stale/changed public proof. `00879` still has those exact bytes. | Proved necessary public comparison. Whole construction/public reports are not interchangeable. The already removed immediate post-install replay is not new work. |
| Callback/dispatch events and exceptions remain observable | The C2 operation API accepts functions without a purity or immutable-closure certificate. Equal children still dispatch independently. Runtime calls, canonical/printer callbacks, dynamic bindings and closed-over atoms can change or throw while an outer function identity is unchanged. | PR469 proves its recursive event schedule, not the necessity of every whole Macro call. No new callback suppression is justified. The transitive runtime/AE/closed-over-state seam remains conservative, not proven stable. |
| Invocation/transaction isolation | `00863-sh07-proof-transaction-thread-id.clj` rejects closed/cross-thread contexts. `00864-sh07-proof-transaction-report.clj` separates stage, mode, phase, verifier epoch, invocation, catalog and immutable artifact root; its no-context branch actually verifies. Construction reuse requires no inherited transaction. | Keep those boundaries. A construction observation is not a transaction receipt, and a report from another invocation/thread/mode cannot be imported. |

Evaluation order is observable, not just final report equality. In the Macro
carrier, invalid invocation precedes Macro replay; replay and AE failures are
contained into failed facts, while interruption is rethrown; obtaining the
current Macro binding can still throw before lineage. At the initial Resolve
parent gate, interruption is rethrown; the later `00879` parent-report catch
contains `Throwable` as failure. The later replay runs only after parent PASS.
A proposal must preserve these **actual distinct** exception policies and
first failing operation, rather than imposing a new uniform error policy.
Optional observation failure must keep its full fallback. Do not move a cache
lookup ahead of invocation/carrier/source legality or suppress its diagnostic.

#### Exact remaining elision candidates and next mutation controls

These are candidate computations, not approved removals. Use the existing
`gravity.self-hosting.resolution-parent-mutation-test` positive fixture and
`observe-construction` seam for any genuine next comparison. Its current
`actual-current-parent-mutation-is-rejected-after-resolve` control must remain
intact and fresh-only. Add a distinct exact test var per candidate rather than
weakening that control or running the whole expensive namespace by discovery.
No such producer/observer test is launched by this source assessment.

| Candidate | Exact proposed reuse seam | Required executable comparison before removing a call |
| --- | --- | --- |
| Macro replay fragment only | The two actual Macro template/resolved verifier executions in `00861`, from the initial authenticated-parent gate to the later construction check; do not reuse run-storage/proof/lineage facts wholesale. | Delegate every observation to the original implementation. After the real Resolve return, mutate each actual template, resolved-expansion, request and digest operand in turn, including aliased backing storage; also change a live callee/plan with unchanged advertised hash. Baseline must pass real public C5 verification. Every changed case must reject or invalidate reuse before consumption, with the same failure/exception order as the retained path. Preserve PR493's stored-key-only negative, where operands are unchanged. |
| Resolve run fragment only | The completed `sh06-resolution-run-request!` result produced during candidate construction versus the run replay in `00879` construction-proof verification; keep current parent/source/public verification and every stored-product comparison. | Change the authenticated request, module inputs, selected binding/actual plan and every stored raw template, raw/resolved analysis, request/digest and template/resolved report field after construction. Compare against the original fresh run, including rejected-template precedence and template verification before digest/analysis resolution. Invalidated inputs must execute fresh or reject; an earlier C5 PASS and matching advertised plan hash are insufficient. Public verification still executes its genuine current path. |
| Reader plan/identity calculation only | Repeated `sh03-reader-verification-invocation!`/explicit `sh03-reader-current-binding!` within the same public call. Keep invocation legality and source authentication at the owning boundary. | Mutate selected Reader bytes, selected-path identity, actual plan function/instruction storage and source/emitter dispatch after the first observation; independently change invocation options/path and nil/explicit route. Require rejection or fresh fallback, original diagnostics, and before/after source joins. Equal source hashes with changed plan/dispatch must not hit. |
| AE compilation or strictly internal reconstruction fragment | The owner-scoped exact-input compilation seam, or one named repeated descriptor reconstruction within a single verifier; never the caller's final public AE check. | Exercise stage, kind, descriptor and options mutations, source/emitter replacement, additional-target/dynamic context changes, and same-function closed-over-state changes across that exact seam. Observe actual builder/verifier calls and original throws. Failed or unavailable observations must publish no reusable success; changed inputs must reconstruct/reject. Preserve strict public comparison and the private owner boundary. |
| Pure Macro projection/identity calculation only | A named repeated projection among stream/trace/graph/provenance/identity checks, with fresh stored-field comparison retained. | Freeze its complete raw operands and actual callback/printer operation bindings, then mutate every consumed field and aliased child between uses. A same-object callback with a counter/sentinel on the second event must still observe its event and exact Throwable order. Show a closed eligible route or keep dispatch fresh; PR469 forbids simply memoizing equal values. |

For all five comparisons, keep original and applying source identities separate;
record each actual argument/result and event order without supplying replacement
reports. A same-function callback with mutable captured state and a callee swap
below an unchanged outer Var are distinct required cases. Test invocation B
must not consume invocation A's fragment; changed thread/closed transaction
must retain the existing refusal. Restore aliases, dynamic roots and filesystem
fixtures in `finally`, and verify restoration. Real positives, fresh public
verification, each negative and nonpublication must be separately attributable.
Source-form checks can establish call order or predicate shape; they cannot
stand in for these producer observations. Existing host/execution restrictions
remain prerequisites for actually running these tests.

#### Conserved evidence and acceptance limits

PR477's `00879` source file and PR493's mutation test,
`macro_run_storage.clj` and `resolution-construction-inputs.clj` are byte-identical
from their recorded tested commits to this assessment. Their original predicate
and mutation results are reused only at that scope, without replay. They do
not authenticate the entire current transitive closure. PR469's canonical
implementation is unchanged, but the facade, operations declaration and test
file changed during accounting removal and lexical extraction. Its original
4/28 receipt therefore remains historical; the unchanged recursive contract
is source-grounded here, with no claim that all eleven old input bytes remain
current. The current Macro verifier installation and all downstream Reader/AE
code above are explicitly part of any next dependency proof.

This disposition establishes no further safe call removal. RV1 stays partial:
proved stored-parent/public-proof/callback necessities are separated from the
five finite unresolved fragment candidates above. RV2's fresh-only cache
boundary and remaining real miss/hit/invalidation, and whole RV4 acceptance,
are unchanged. Source review does not grant runtime, source-authentication,
integration, release, self-hosting or performance acceptance.

### RV1: executable Macro fragment mutation preparation

The next source increment adds seven distinct exact test vars to the existing
`gravity.self-hosting.resolution-parent-mutation-test`. Its preparation base is
`149503152d7edab3f7e638c000a553f62c54e679`; this is not a tested runtime tuple.
The entire earlier file prefix, including
`actual-current-parent-mutation-is-rejected-after-resolve` and
`observe-construction`, remains byte-for-byte unchanged. The existing namespace
policy remains fresh-only and generic execution still requires the owner
wrapper. There is no call removal, cached PASS or new public-verification path.

| Exact new selector | After the first actual Resolve return |
| --- | --- |
| `actual-macro-template-operand-mutation-after-resolve` | Replace the actual first run's raw expansion-template artifact reference through aliased run backing storage. |
| `actual-macro-resolved-operand-mutation-after-resolve` | Replace the actual resolved expansion's artifact ID with a distinct SHA256-shaped value through that storage. |
| `actual-macro-request-operand-mutation-after-resolve` | Change the first actual digest request's ordinal through that storage. |
| `actual-macro-digest-operand-mutation-after-resolve` | Change the first actual resolved digest to a distinct SHA256-shaped value through that storage. |
| `actual-macro-live-callee-swap-after-resolve` | Replace the execution Var's observer with one that forwards the template-verifier call to the original execution boundary using the distinct expansion entrypoint, whose arity differs. |
| `actual-macro-live-plan-swap-with-unchanged-advertised-hash-after-resolve` | Forward to the original execution boundary with the actual template function definition replaced by the expansion definition in the passed plan, retaining every advertised binding field outside the plan. |
| `actual-macro-same-function-closed-state-change-after-resolve` | Keep the observer function object identical but change its captured state so it forwards the changed callee to the original execution boundary. This is distinct from the Var-root swap. |

Every selector constructs a new genuine current Reader/C3/Macro parent and a
positive C5 artifact, then requires an actual independent public C5 PASS before
its changed case. No prior artifact or report is supplied as that positive.
The tests reuse `observe-construction` for the exact mutation time. Operand
cases retain the native run keyset and assert both actual Macro verifier calls
and a failed actual verifier result, rather than passing from a shape-only
refusal. Dispatch cases forward to the original implementation; their expected
original arity failure must occur at template execution before any resolved
verifier call. No result or exception is fabricated by an observer.

Each case requires a post-mutation failed Macro report, `C5-UNRESOLVED` at
`:final-authenticated-resolution-artifact`, the failed fresh-upstream check,
no published C5 value, and no subsequent Resolve consumption. It records its
case ID, original parent/binding, invocation, actual positive public report,
operand before/after, dispatch arguments and events, error identity fields and
restoration results. Aliases and dispatch state are restored in `finally`;
`with-redefs-fn` restores the original execution roots even after failed setup.
No filesystem fixture is edited. Execution commit/tree, complete dependencies,
ordered classpath and source bookends must still come from the separately
reviewed existing owner harness; the preparation-base literal is never an
applying/executed revision claim.

At preparation time these were **unrun genuine comparisons**. The retained
operation record below updates that status without replaying any case.
Compiler-free source reading,
compilation of helper definitions against forbidden fake producer ports, and
injected alias/dispatch/Throwable/restoration controls establish only harness
mechanics. They do not load Bootstrap, authenticate input, run Reader/C5 or
establish these tests' expected genuine refusals. The original whole-source
negative controls remain separate and unchanged.

Before an actual run, admit one exact selector and its exact clean commit/tree
through the existing owner wrapper with the existing host guards. Review the
actual test and harness dependency closure, source/producer inputs, observation
and restoration code, bounded capture and completion conditions. Attribute the
fresh positive/public verification and changed case separately, preserve all
errors and nonpasses, and review actual results before any acceptance. Do not
run this namespace generically, borrow a proof transaction, replay an uncertain
operation or infer RV1/RV2/RV4 closure or fragment equivalence from preparation.

#### Retained operand results and stopped live-callee operation

The original operation and independent result-review receipts dated
2026-10-29 retain four finite operand results and one nonpassing live-callee
operation, published with [PR578](https://github.com/burn2delete/uniform/pull/578).
All five bind executed source `c25e4805d95ae36bdbd98664e6389556f3fc7690`,
tree `fa0a8e5aa9c4f74f15265b676f8ac28ae1dfd4e9`, with identical clean
source/runtime/classpath bookends. These are original execution identities,
not an applying-source claim for later source increments.

Each of the four completed operand fixtures records one test, 24 passing
assertions, zero failures/errors and a new genuine positive/public C5 PASS.
Its changed case rejects with `C5-UNRESOLVED` at
`:final-authenticated-resolution-artifact`, preserves exact run storage,
publishes no final C5 value and permits no subsequent Resolve consumption.
Aliases and dispatch state are restored. The first template/resolved verifier
outcomes distinguish the changed operands; the second run's verifiers pass.

| Operand fixture | Changed first template / resolved verifier | Final operation receipt SHA-256 | Independent result-review SHA-256 |
| --- | --- | --- | --- |
| `actual-macro-template-operand-mutation-after-resolve` | FAIL / PASS | `505c8ec17f195167e1fe0cef30afc8c95a4b0c366ff40e8cff4bce999b0f95aa` | `1ce50390f4a1e4c0d0f468575d2d550f1e4a05733627c28be7c5ec2cd2abc8af` |
| `actual-macro-resolved-operand-mutation-after-resolve` | PASS / FAIL | `cf7742be976132aa8769d84cb9968c326a3943ce1a31a3a0203ef61c562d8930` | `070ed275f9c365e87bf6fb8a2500d1f6a194fd20a2be9fe593151fb42d82094e` |
| `actual-macro-request-operand-mutation-after-resolve` | FAIL / PASS | `5fb1dcee432b22b8fd5a4a54d5dde7d48e4df80d5b40b14ac9643a9d0a962fcc` | `2158f7e2dba7e435d72018e6a591bde3f5983a3583049a6f347ee525e7dab00a` |
| `actual-macro-digest-operand-mutation-after-resolve` | PASS / FAIL | `3272ce806f5bde490efb4517308262d514d2ddff149f22d4515ce8d8a8dec5fe` | `35a00d622e346cf1f1bde3ccc15180032364fb78be6fbc04ec6213851bf0042c` |

The request fixture changes the actual first digest-request ordinal from 0 to
1. Its template header checks ordinal/purpose while the resolved verifier
checks a different subset; claiming both verifiers failed would be incorrect.
The digest fixture changes the actual first resolved-digest element through
aliased storage. These results retain only their independently reviewed
mutation/observation scope; no verifier call is elided by them.

The first three operations used historical common-normal then host-exclusive
admission with the canonical heavy lock. Those semantic observations remain
retained; they establish no reusable nesting exception. The digest operation
used the separately reviewed one-common-exclusive plus canonical-lock method,
with no host-root acquisition. Original terminal cleanup/capture/release fields
remain observations under each original method, not proof of current capacity,
global host clearance or permission for another operation.

`actual-macro-live-callee-swap-after-resolve` is terminal NONPASS from original
owner session 6343: child timeout at 120 seconds, exit 124, no child or normal
parent receipt, no observed assertion counts, positive/public C5 or mutation
result. It establishes neither semantic PASS nor semantic FAIL. Its outer
cleanup-complete field is false, cleanup-failed true and timeout containment
unproven. Nested clean/root-dead observations do not override those flags;
reported lock/lease release is not containment or capacity clearance. Its
final operation receipt is
`ce4ba8414e64660c37a6693d999221508d00225d8460be78206a41899a5ca9cf`;
independent NONPASS review is
`78600e07102c23dc5aee1f6fa7791da12be532e1d052e0923a76398a78e002a7`.

The live-plan and same-function closed-state selectors remain UNRUN. No retry,
recovery, selector reroute, bounds increase or process/holder probe follows
from this record. Historical next-action fields retain their original time
scope; they do not supersede the later live-callee stop. PR493 and all four
completed results are retained without replay. Reader/DSE/R4 denials and the
unknown 22232 operation remain untouched. Whole RV1, RV2 and RV4 acceptance,
fragment equivalence, runtime/cache admission and elision remain open.
