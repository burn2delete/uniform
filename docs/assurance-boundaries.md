# Assurance boundaries

This is the detailed assurance-separation cleanup backlog and acceptance
contract. [Cleanup navigation](component-roadmaps/README.md) indexes the other
outcomes. This contract applies D1, D8, D9 and
[C18 pass correctness](phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md).
It does not replace the component implementation task lists or introduce a
routine approval gate for development.

## Responsibilities and actual consumers

The source audit used main `176c4ee65bb3ceb545f51edfaa599902030a0cb7`.
Paths below are repository-relative; the compiler component directory is
`bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/`.

| Responsibility | Owner and exact consumer | Evidence required; insufficient substitutes |
| --- | --- | --- |
| Construction | MIR producer: `00736-p15-s23-c11-mir-block-keys.clj` and `00741-p15-s23-c11-mir-map-blocks.clj` | Raw products and construction provenance. Pending modules have `:constructed-unverified` status. Returning a value, a construction token or a successful constructor is not independent acceptance. |
| Independent semantic validation | Native verifier owner: `00740-p15-s23-c11-mir-validate-effect-edge-placement.clj`, `00743-p15-s23-c11-mir-validate-final-artifact.clj`, `00744-opaque-c11-construction-token-definitions.clj` | `validate-constructed!` creates its report. Final-artifact validation recomputes the verifier record from the pending view and authentic checked Core, then checks both equality and strict typed structure against the supplied report. Saved report equality is not itself fresh source replay. Test each actual entry point separately. |
| Publication readiness | Publisher and independent reviewer: `tools/check_worktree_preflight.clj`, `tools/validate_workstream_governance.clj`, [governance](workstream-governance.md) | Exact base/head/tree, clean named branch, affected scope, observed validation and independent review. Preflight checks Git identity; the ledger validator checks structured declarations. Neither authenticates a reviewer merely because its string differs from the author. Mergeability, test totals and a saved ledger are insufficient semantic evidence. |
| Runtime safety | Owning compiler/runtime checks under D8, C7-C11 and target contracts | Real type/index/arity/effect/capability, source/authentication, native class/callback, metadata, memory and runtime checks remain enforced. Construction, publication and project accounting removal cannot legalize unsafe execution. |

Host process observation is a fifth, operational responsibility. The bounded
development runner at
`bootstrap/clojure/test/gravity/self_hosting/sh07_bounded_development_runner.clj`
owns deadline checks, process exit observation and bounded capture. Its child
owns test execution. Both receipts explicitly remain non-authoritative.
An observed exit must agree with the child's declared exit, captured terminal
output must be complete, and closed routes with executable assertions cannot
pass with no tests or no passing assertions. Nonzero counters still do not
prove source authenticity, independent validation or publication readiness.

The runner's `:child-command-fn` is a trusted test injection seam, not a sandbox
for an adversarial same-user executable. Closed result keys reject supplied
authority, mergeability and saved-report additions. This protects the receipt
contract; it does not authenticate the truth of a producer's valid-looking data.

## Finite backlog and acceptance

| ID | Exact work and dependencies | Acceptance evidence and remaining boundary |
| --- | --- | --- |
| AS1 | Audit the four responsibility boundaries above, including actual constructor/report consumers, governance declarations and preflight. Depends on D1/D8/D9/C18 and current owner confirmation. | Source-grounded inventory above; independent review must confirm no stronger authority is inferred. Runtime safety guards remain owned by their implementation components. |
| AS2 | Repair the existing bounded development runner and its adjacent `_test.clj`: no-execution false passes and truncated passing prefixes. No compiler or serializer edits. | Cheap direct parser negatives plus a real saturated shell-output control; failed stream reads remain incomplete. Existing successful-result, timeout, late-result and descendant cleanup controls must be reported separately. No resource-heavy compiler selection is needed. |
| AS3 | Shared DATA transport, watchdog/capture and fresh replay mechanics in the existing Native parameter and MIR paired-process harnesses. Native owns the codec; MIR owns its consumer adaptation. | Exact-class preflight before foreign callbacks; reviewed collection-class policy; scalar/metadata/class roundtrip and negative controls; observed exit/deadline/digest gates; then actual original construction and independent fresh replay on the exact candidate. Controlled shapes and saved reports cannot close unknown actual artifact domains or public admission. |
| AS4 | Publish the completed declared increment with exact immutable evidence, independent review and ordinary integration. Depends on AS1/AS2; whole-outcome closure also depends on AS3. | Draft while incomplete. Record tested head/tree, command, passed/failed/unrun scope, original failed attempts, reviewer and disposition. Independent review and publisher readback own acceptance; this document cannot accept itself. |

100% means every responsibility inventoried here has independently testable
positive and negative boundaries, exact immutable evidence and an ordinary
publication disposition. It requires AS1-AS4; finishing the small runner fix
does not close the shared transport/fresh replay dependency or complete the
language. New findings must be added to the owning row with exact affected
contracts and acceptance evidence, rather than silently excluded.

## Historical dependency evidence

The initial MIR evidence used PR421 head
`8b9dd46ea38eafba1d3d7a49d8c6ca6b6488563f`, tree
`ed9d118e18f5f1a3f04138603e282ade9fb70701`, C11 source SHA256
`c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e`.
Its genuine selector timed out at 120 seconds, exit 124, without completed
counts. Helper 36/327, harness 10/52 and codec shape 10/92 results are reported
separate scopes, not acceptance of that source pair. Newer main receipts do not
accept this older tuple.

The shared codec version SHA256
`df10864240951a7f84c4b22bcdc67ace7d0076144110207ea1c0b1d9c6b7af31`
preserves Integer and metadata but its controlled probe showed HashMap becoming
ArrayMap, SubVector becoming Vector, and foreign metadata invoked before class
rejection. MIR requires exact concrete classes; its actual raw/Core/MIR runtime
domain is unproven. Native remains the sole codec writer. Shared policy and
preflight are open dependencies, not permission to weaken class/callback guards
or create a second serializer. At that historical point, heavy A/B replay and
public admission were unrun. The accepted successor evidence below supersedes
that transport/replay hold, without replacing these failed receipts. Preserve existing specific
C12 execution and original C7 publication restrictions.

## Accepted dependency evidence and remaining criteria

PR421 is merged as `06a256fdb97f49b5fedea36a6803483302631bbc`, tree
`6ed68a337f85d9a044d7eb3d08aa1ae8eebccea2`, exactly the reviewed final
candidate `d67406b305d93e052f54c9439da8142efbb663c0`. It is no longer a
draft or timeout-only dependency. Native remained the sole shared codec writer;
MIR adapted its consumer to the reviewed V3 codec SHA256
`0f91ef22f305785e8e8d730beff1a31ec9aecce8e9adfd768f0636e3a565daf9`.
Strict single-form framing, exact native classes before callbacks, metadata and
SubVector backing/extent conservation, failed-producer nonconsumption, deadline,
external exit and digest gates received cheap positive and negative controls
(24 tests / 162 assertions). These controls do not grant source authority.

On original producer head `8b9dd46ea38eafba1d3d7a49d8c6ca6b6488563f`
and tree `ed9d118e18f5f1a3f04138603e282ade9fb70701`, genuine original A
and B each completed naturally with observed 2 GiB heaps. A completed 13 and B
19 harness checkpoints, collectively covering the original 24 predicates;
these are checkpoints, not Clojure test totals. Each performed actual original
construction and independent fresh C6 replay, real Gravity/Native positives,
provenance and immutability checks. B additionally checked both substitution
directions and exact type and retired-quota-field rejection. Both actual domains contained 12
full-pair and 11 carried classes; neither observed metadata or SubVector.
Controlled coverage of those shapes remains distinct. A's handoff was retained
and independently consumed; B's artifact was not retained, so its domain is
supported by the in-process inventory and terminal evidence, not a later B
artifact rewalk. The original timeout remains a nonpass.

Current-main composition was separately checked at
`38f8f6e00e4ca97c07d3b36ecb499ea93b5c5b29`, tree
`5b2f86c3843ad0309c951661bc1c8a9b5c1bf1a7`: retained-A current-owner
validation and affected helper/forwarding controls passed 42 tests / 426
assertions. Genuine Bool-parameter original and fresh replay passed separately
with 1/7 and 1/48 tests/assertions on that same tuple and C11 c41 source.
Final reconciliation at d67406b3 conserved that reviewed evidence with explicit
SH04 applicability review and a fresh 21/94 leaf/boundary selection, docs
240/19 and exact integration preflight. Earlier results retain their original
identities; they are not represented as fresh executions on d67406b3.
Independent source and retained-record review preceded ordinary integration.

| Original ID | Accepted disposition | Exact remaining acceptance boundary |
| --- | --- | --- |
| AS1 | Closed for the inventoried construction, semantic validation, publication and runtime-safety responsibilities; PR431 integrated the reviewed inventory and runner contract. | Preserve those distinct responsibilities when consuming subsequent evidence; this is not completion of every compiler component. |
| AS2 | Closed by the independently reviewed, validated runner repair in PR431. EOF/capture, no-execution, exit agreement and observation-failure controls remain enforced. | No additional compiler or language feature is required to close this runner increment. |
| AS3 | Shared V3 transport and development replay were accepted through PR438/PR421. The later frozen v2 candidate has independently reviewed genuine public construction, fresh report/replay and actual report/tuple rejection evidence, detailed below. | Preserve the exact tested producer/consumer tuples. Current-main composition and publication remain open; historical candidate success is not current-source acceptance. |
| AS4 | PR431 and the accepted PR421 dependency increments have ordinary reviewed publication and integration dispositions. PR457 integrated this canonical reconciliation at `d7451d7546cbc02895d2b5cb43449d9252b919d6`, tree `adb182d7ce6fe73bf0e80e64193ae701c8d3725c`. | Publish the remaining final-admission increment through normal independent review and integration. Whole-outcome closure remains open until AS3's final entry-point boundary is evidenced. |

The selected-source v2 successor supplied the actual public route and rejection
evidence described below. It preserves historical pins, Core authentication and
fresh replay. Source identity observations and cheap dispatch controls alone
still cannot establish that result. The remaining integration work does not
expand assurance cleanup into completing another language family, W4 release,
other backends or C12 execution. No measured percentage follows from these
milestones.

## Current assessment

Assessment date: 2026-10-28, America/Edmonton. Source base:
`cbf5c210544a0033e9c755d7ef8358f01f1ad57c`. Original criteria are unchanged:
AS1 closed, AS2 closed, AS3 partial, AS4 partial. The previous assessment at
2026-10-26 22:31 MDT remains historical; its statements that public Core and
C11 verification were unrun no longer describe the accepted local candidates.
This assessment records completed evidence without claiming current-main
integration or closing the whole outcome.

The inherited dashboard value of 70%, dated Oct26 04:11, is a provisional
estimate, not measured coverage. No weighting of the original responsibilities
was defined. This assessment does not recalculate that value from PRs, tests or
closed-row counts. The collector can display these current milestones without
presenting the inherited number as a new measurement.

| Original ID | Accepted advance since the merged baseline | Remaining gate |
| --- | --- | --- |
| AS1 | The responsibility inventory remains closed. Successor reviews explicitly distinguish selected-source authentication, relative structural/report validation, publication and runtime authority. | Preserve the distinctions in actual final entry-point evidence; no new language family is an AS1 requirement. |
| AS2 | The bounded runner repair remains closed. Private controllers additionally preserve external timeout/nonzero nonconsumption, immutable transport, capture failure and monotonic sampling boundaries. | These controls neither establish a current semantic result nor resolve an older process's unknown cleanup. No additional compiler family is required for AS2. |
| AS3 | Genuine public Core construction and fresh contextual verification passed at `cb3701556e9082ae8387c18fa1f6396a9d284617`. Genuine C11 construction, fresh public report/replay and the actual rejection boundaries below passed with that source/CWD and consumer `9c27c3e8856b9659482a3e48238896e1548fce02`. | These are exact candidate results. Reconcile the affected current-main consumer/source closure, run the public route where required by that change, and independently accept it. Do not relabel old source bytes as the latest source. |
| AS4 | The accepted v2 results and reviewed integration candidate are concrete publication inputs. Candidate `dced564a20c175b95dcb919459c3e673a4ca7ade` is not an ancestor of this assessment's main. Its retained CI run failed projections after the other six jobs passed. | The integration owner must repair the applicable source-derived snapshot/catalog input through its owner, pass required current-head checks, obtain independent review and verify normal main integration. This assessment cannot supply that disposition. |

The original `.gravity` and later `.qst` development replay lanes remain
separate from public admission. The local `.qst` tuple
`66f46c53aaa24bfecffca782eb8f2128b85ac1e1` passed original1/7 and fresh1/48;
it is not current-main public verification. The expression-owner predecessor
`77019e4948fed440a3dc4aee5552825d7c9d605e` remains NONPASS (37 passed,
1 failed); the corrected result does not overwrite or sum it.

The actual public C11 route performed fresh C6 authentication and returned a
passed public report with semantic replay parity. The actual contained-report
validator rejected semantic-report and vector/list kind substitutions. A
coherently regenerated policy passed that relative validator and was then
rejected by the actual fresh public replay at semantic parity. Actual public
entry-point cases also rejected source-rule, type-fact, Core identity and
requested-target substitutions, plus malformed/mixed/legacy headers and a
context source-hash inconsistency. These results distinguish relative DATA
coherence from fresh source authority; neither a saved report nor a shape
predicate supplied the latter.

The complete constructor artifact was retained with an exact domain walk.
The later full public report and raw owner metadata were not retained: compact
report/policy evidence and in-process domain checks support their stated scope.
Independent full-report/raw-metadata/policy-hash recomputation is not claimed.
The actual admission state remained development-unadmitted and host-seeded;
frozen Reader, release and seed-retirement acceptance do not follow.

Consumer composition `1f51ee30c2e9c93db409f2e871af71b58a5540bc` separately
passed an actual fresh public report using the original physical source/CWD.
A separate normal-new-CWD check exercised the affected Reader/C2 host checks
and rejected the unsupported expression source at the existing source guard.
Later source preservation reviews do not turn that execution into a run on
this assessment's main. The selected v2 dependency remains the historical
expression revision; newer expression bytes require their own supported
contract before positive admission. The larger flat-Bool v3 family is separate
unfinished work, not a new definition of the original assurance finish line.

The old-pin rejection, missing-provenance-layer rejection, sorted-owner-metadata
rejection, load-order hold and failed CI predecessors remain failed history.
Later v3 watchdog and host-capacity failures also remain NONPASS. They neither
invalidate the accepted v2 tuple nor establish present execution safety.

## Admission evidence and historical cleanup

Historical semantic acceptance, historical process cleanup, live broker
admission and permission to execute are separate facts. A later worker's
successful admission/release cannot close an earlier worker's unknown terminal
or cleanup. An empty process/holder snapshot, peer reports of no owned jobs,
or a broker timeout cannot prove global capacity. An access denial does not
supply the missing process observation and must not be retried by another route.

The inspected controller enters semantic work only inside the Git-common and
host-wide exclusive broker scopes and after acquiring the canonical file lock.
The broker acquires the actual slot locks under its queue/admission protocol;
timeout or acquisition failure does not call the protected thunk. These live
locks, fixed roots/order, deadlines, source/runtime freeze and owned-process
lifecycle remain required. They do not authenticate source semantics or prove
the absence of noncooperating work outside the broker's model.

The broker has no historical-receipt input. Requiring an unrelated successful
workload before allowing the next normal broker acquisition can create a
circular project prerequisite, and still cannot prove future availability.
Do not manufacture such a proof with another validation. Conversely, an
explicit hold for an unresolved original process remains a separate instruction;
identifying this circularity does not waive that hold or authorize execution.
The original recovery owner retains that investigation. No shared-lock probe,
reset, release, service change or compiler launch is implied by this assessment.

The next eligible assurance work is exact source/evidence reconciliation and
independent review of the publication candidate. Any distinct public validation
must preserve the live admission and lifecycle guards and satisfy the current
execution directive. Whole AS3/AS4 closure still requires actual applicable
public construction/report/replay evidence and ordinary verified integration;
another status receipt cannot replace those outcomes.

## Runner increment evidence

Before the fix, the two new controls produced 2 tests / 17 assertions:
9 passed, 8 failed, exit 1. The real 256 KiB saturated-output control showed a
passing retained prefix hiding a second terminal result. No compiler source
construction, broker acquisition or restricted operation occurred.

The first affected-namespace run after the fix produced 10 tests / 64 assertions:
63 passed, 1 failed, exit 1. The unchanged descendant-cleanup assertion expected
a positive captured descendant count but observed zero; root cleanup completed.
That failure is retained and is not a passing watchdog receipt. A subsequent
minimal process-observation diagnostic failed with `Operation not permitted
(sysctl failed)`. The sampler previously swallowed that exception and could
claim complete cleanup without observing descendants. Sampling failure now
explicitly prevents a complete-cleanup claim. Failed-stream-read and
failed-process-observation controls were added. Final immutable validation and independent
review must record their own exact source tuple and current results.

Run this namespace with a 256 MiB JVM and the existing test classpath; the tests
launch only synthetic shell children. They do not launch a compiler route.
Run the language boundary and structural docs gate under the existing host
resource policy before integration. Preserve fresh independent compiler replay
for the component acceptance claims that require it.

## Immutable runner validation receipt

Tested head `3d98800f906e7a66fdbda26cc6b1be474278811f`, tree
`6ba5a222d2267690de797dd4c5f02b5960f7797e`, base
`176c4ee65bb3ceb545f51edfaa599902030a0cb7`. Git head/tree and empty
tracked/untracked status matched before and after each command. Both existing
normal resource brokers admitted the run. Supported escalation enabled actual
process inspection; the earlier sandbox failure remains above. Every child
exited naturally within its 60-second watchdog.

| Stage | Exact selection | Result |
| --- | --- | --- |
| Runner | `clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e "(require 'clojure.test 'gravity.self-hosting.sh07-bounded-development-runner-test) (let [r (clojure.test/run-tests 'gravity.self-hosting.sh07-bounded-development-runner-test)] (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))"` | Natural exit 0, 3778 ms; 12 tests / 71 assertions, zero failures/errors |
| Docs | `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Natural exit 0, 1391 ms; 240 docs / 19 phase indexes |
| Language | `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | Natural exit 0, 1710 ms; 6 tests / 11 assertions, zero failures/errors; one namespace attempted, none skipped |

The exact runner SHA256 is
`17721a14e5cca596df91185b49b384df5793fef1175fc9b07abc5c162e2716a7`;
test SHA256 is
`b4d0f4805112e72ca82b7813230658be64c29e52c511113de5aac9d4faabe0f4`.
Retained process receipt SHA256 values are runner
`ee264dc27ba1e5ae12e135bc46c767341a91ff7833a15e720407c720f5dc91f1`,
docs `cd16b7050458fb35f58fa182423b7acc604b5d37063ddf399d00ea08b20bd0c5`,
language `c0454599a3dc6daa4ad2e846fa412896781f2e17507ca0678897122592de894c`.
These are operational receipts, not new acceptance authority.

Independent static review of this exact source/tests/document tuple found no
blocking defect for the finite AS1/AS2 runner increment. The reviewer checked
EOF-only capture, saturation and read failure, declared/observed exit agreement,
empty execution, counter representability and sampling-failure cleanup. The
review did not independently execute the tests. A later documentation-only
receipt commit does not change tested runner/test bytes or imply another run.

AS1 source inventory and AS2 runner controls are evidenced for this increment.
The initial runner increment did not close AS3. Its later accepted transport,
domain and replay evidence and remaining public admission boundary are recorded
above. AS4 publication and integration must name each final exact head. No compiler construction,
authoritative migration, runtime safety proof, C12 execution or original C7
publication was performed by these checks. Whole-outcome completion is open.
