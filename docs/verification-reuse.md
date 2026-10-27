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

Current assessment: **2026-10-27 04:32:34 UTC**, on merged main
`15a5499e97798621d270c36dd2546ce98ddd62a9`, tree
`aa5dd1d419594b31f9f9837ba6d09e39894b3562`. The original inventory is unchanged.
The statuses below describe this cleanup's acceptance criteria, not compiler or
language completion. Earlier increment receipts had accumulated without a dated
reconciliation for the dashboard collector; this assessment corrects that gap.

| ID | Exact boundary and files | Dependency and acceptance | Current disposition |
| --- | --- | --- | --- |
| RV1 | `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/resolution-construction-inputs.clj`; later Resolve construction proof and public verifier | Any additional Macro check elision requires transitive live-plan, dynamic-dispatch and AE identity stability, plus mutation observation across the whole proposed reuse seam. Same logical parent is insufficient. Preserve error order, fallback, invocation/transaction isolation and fresh public/tamper checks. | **Partial.** PR396's construction reuse is present at `d00cc2d5`; PR469's callback and PR477's embedded-proof necessity decisions cover only their named operations. PR480 reduces structural calculation work; PR483 repairs its load regression. None proves whole upstream-report equivalence or every retained repeat necessary. The full identity/mutation boundary, independent negative controls, review and integration remain required. |
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
tested scopes and independent reviews. No implementation PR for this outcome
remains draft at this assessment time.

Unpublished work remains separate: the reserved
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
message operations are preserved. This reconciliation launches no test or
compiler replay and does not write the dashboard.

## Exact Reader leaf closure

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
