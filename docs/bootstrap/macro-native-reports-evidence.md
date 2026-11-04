# Native Macro proof and ordered refusal extraction

## Governing contracts and ownership

D1/D2/D3/D8/D9, L4/L12/L16, C1/C4/C15 and TEST2 govern this increment.
The source parent is unmerged Syntax PR1184 at
f40b25494c45994f5f6cc4598a9458520096bbc9, tree
2e04f87e8ffb18aaa17afa312c8390e68c367c2d. Observed main is
3c97ad75341c5ef6506cf1bba050b9017301695b. No inherited acceptance or CI
establishes this source's integration eligibility.

Independent ownership review identifies two remaining native macro-stage rule
bodies: c4-macro-capability-proof in component 00028 and c4-macro-validate! in
00029. Move their nine predicates and ordered refusal mapping to gravity.macro.
The large earlier constructor in 00029 is shadowed by the active 00869 adapter;
it is not the extraction target. Existing Gravity macro code remains the
semantic expansion owner, while gravity.macro-expansion and c4-macro-evidence
remain explicit hosted compatibility/projection layers.

## API, artifacts and dependency direction

Add native-capability-proof [artifact diagnostic-ids-var] and validate-native!
[source-path artifact proof-var failure-var]. Keep the original Bootstrap names
and arities as explicit adapters. The native artifact and diagnostic catalog
produce the same nine report predicates and status field; validation computes
the proof once and returns :complete or invokes the existing C4 failure owner.
These are supplied native records, not authenticated compiler evidence.

Macro owns predicate evaluation and ordered rule selection. Bootstrap retains
source reading, metadata override admission, artifact construction, authentication,
cache/storage, dispatch, failure construction and publication. Dependencies flow
from supplied native artifacts/catalog through Macro report rules to existing
consumers. Macro does not discover or import Bootstrap or diagnostics owners.
Explicit Var ports preserve current owner resolution at the original use site.

Preserve C4-RETURN, C4-TRACE, C4-HYGIENE, C4-BUILD-EFFECT and
C4-GENERATED-UNSAFE order, actual paths/spans, owner exception and argument
identity, native truthiness and vacuous every? behavior. Incoming source, cache,
replay, identity and readiness fields are consumed unchanged. No replacement
identity, authorization guard, proof certificate or project quota is added.
C4-SIZE remains historical catalog data; comparison-ready is a readiness label.

## Acceptance and evidence

The accepted compiler-c4-macro-engine fixtures and rejected compiler-c4 corpus
exist in paired .gravity/.qst forms. This increment must emit real native report
and diagnostic records on actual paired paths and retain full before/after data,
source text/hash and original identity fields. Native unit inputs do not claim
that source compilation or authentication took place. Historical committed proof
records remain historical transport data and are not renewed by this extraction.

Tests must cover each predicate and ordered refusal, all-invalid first refusal,
truthy values, vacuous native fields, exact diagnostics, late catalog/proof/failure
owners, returning failures, exceptions, source provenance and facade interposition.
Compare complete native artifacts and diagnostics, not counts or hashes alone.
Keep unrelated Macro/source/assembly/admission/pin/manifests unchanged. Add the
focused tests to the existing Macro CI cohort and update only the API inventory
count needed by that public interface change.

Run focused tests, appropriate composition, boundary/ownership, contextual lint,
formatting and structural validators, independent semantic/adversarial review,
exact-source/base hosted CI and immediate clean integration preflight. Keep this
early draft PR current. The initial scope commit has no implementation credit.

## Explicit unsupported boundaries

This does not complete Macro migration or canonical C4. It does not authenticate
syntax/source, execute macro source, validate build grants or cache replay, verify
safety/proofs, admit compiler products, add release/self-hosting/seed-retirement
authority, or promote legacy report labels into certificates. Existing weak native
predicates, malformed-native host failures, source dispatch and unsupported forms
remain honest boundaries. Held compiler/authentication routes and retained dirty
patches are not exercised or adopted.

## Implementation and initial local evidence

The two original rule bodies now live in gravity.macro, with only name/arglist
changes and explicit diagnostic-catalog, proof and failure Var ports. Catalog
lookup stays at its original expression position; proof/failure Vars resolve
their current functions in operator position. The two original Bootstrap names
and arities delegate to those APIs. Component 00028 explicitly requires the
stage immediately before its early native facade: the first focused run caught
the previous later-only load point, and that nonpass is retained.

The source/test/CI contract exposes 52 Macro APIs, adds only the new focused
namespace to the existing Macro cohort, and changes only the existing API count
in its inventory test. Focused native predicates/refusals plus inventory pass
eight tests / 148 assertions. A separate cold load exposes 52 APIs without
Bootstrap. No other existing Macro operation body is modified.

Before extraction, 28 native capture records on the two actual accepted fixture
paths retain full input/proof/diagnostic data and actual source text/hash.
Proof projection yields 26 records and two original malformed-cache host errors;
validation yields four returns, 22 C4 refusals and the same two host errors.
The metadata-aware EDN SHA256 is
d76b80092e45ef9dfec5b9189649a5d49772646b8284d4085c758eb245767577.
These are explicitly native unit artifacts, not products compiled from that
source text. Baseline output completed before source edits; file modification
times corroborate that ordering. Clean candidate
db0b82ec6347e1e22381df231ab719315fc294b3, tree
e50e5c36e7a08c1581445142b6ab398e27dc5b3c, reproduces all 28 complete
records byte for byte and as parsed data with the same SHA256. The native host
errors remain host errors and receive no diagnostic-refusal credit.

Boundary/ownership checks pass 48 tests / 1198 assertions with no skipped
namespaces. CI formatting and document/roadmap/governance validators pass.
An initial combined legacy regression invocation incorrectly mixed cold-leaf
tests with a Bootstrap-dependent namespace, producing two cold-load assertions.
That harness nonpass is retained. Fresh separate JVMs pass the two cold-leaf
namespaces (18 tests / 198 assertions) and the Bootstrap compatibility namespace
(five tests / 44 assertions). No cold-load assertion or host failure is weakened.

Static conservation proves both moved bodies exactly modulo the declared
names/arglists and explicit ports, both facades and their early require, the
unchanged shadowed constructor and all surrounding Bootstrap code, every
existing Macro function body, the sole API count correction and the one CI
namespace insertion. At db0, exactly eight slice paths differ. Source authentication,
pins and manifests remain byte-identical. Contextual lint for the six changed
Clojure paths against source parent f40 passes with zero baseline warnings and
zero blockers. This is local extraction validation, not a composition check
against newer main 3c97. Evidence files use /tmp/uniform-macro-native-reports-
locally. Independent acceptance, hosted CI and integration remain pending.

Independent review also confirmed an existing Var-call timing defect in four
other Macro operations against exact pre-extraction bodies. Those operations
are byte-identical in this increment; the separate corrective work remains open.
This extraction does not claim that those other ports are fully verified.

## Hosted static nonpass and load-point correction

Own hosted run 41928851212 attempt 1 uses actual base
3c97ad75341c5ef6506cf1bba050b9017301695b, source
a1c019b2f480c0df43282846e98cd2dc6be07b14 and provider
4e55c8a050572a39145466dacb83663f7a53d898. Provider tree
bae43fa23fa67f98fb802e188c8db11c7bc9b7aa is the reviewed composition of
that source and four exact incoming Main paths; it differs from source tree
82f4917258b74aa116314688c61e7d5b840a6094. Neither source review nor
local extraction lint establishes that combined branch's CI acceptance.

Static job 126921175037 failed two actual checks. The CI harness still requires
exactly one --namespace in every Macro child, while the existing cohort now
correctly includes the native-report namespace as well. Its update must preserve
explicit complete namespace selection and the existing disjoint receipt/source
coverage controls; replacing the assertion with a generic lower bound would
not establish that coverage. At discovery, Root's actual active C6/CI writer
owned both tools/ci/run.clj and tools/ci/run_test.clj, so the shared-file
correction awaited its normal handoff without interrupting valid checks.

The full combined-branch lint also finds one introduced duplicate-require warning
at component 00857 line 1. That component was byte-identical base to candidate
in the failed run; the required early native facade import made the old later
require redundant in the combined context. All 3459 baseline warnings remain unchanged.
The six-path local extraction check missed the new warning at that older
context location; its zero result is retained at its narrower scope. The actual
provider static nonpass remains evidence and receives no integration credit.

A fresh two-path source claim owns only component 00857 and this evidence;
the old eight-path token stays released. The correction removes only the old
later require and its following blank line, preserving the earlier native
facade import and every operation body. It does not add suppression, change a
warning policy or move any source/admission rule. Full contextual reproduction,
native parity and independent review of this load-point correction remain
pending, as do the shared CI harness correction and final fully wired CI.
Evidence is retained under /tmp/uniform-macro-native-reports-static-failure/
and /tmp/uniform-macro-native-reports-load-fix-*.

After Root's recorded normal release and actual row absence, a fresh own
three-path CI coverage claim owns run.clj for transaction serialization,
run_test.clj and sh01_ownership_test.clj. It adopts no foreign token and does
not edit run.clj. The cohort guard now compares the exact three namespace
vectors, checks every native-report deftest is selected, explicitly proves
omitting that namespace loses its selection, and retains all existing complete/
disjoint receipt partitions and mutation counterexamples. Its selection model
checks both loaded namespace and qualified/unqualified selectors.

The source ownership guard now checks the actual early component 00028 import
and native facade, forbids a duplicate at the later runtime consumer, verifies
strict load-order precedence, and keeps its existing runtime-consumer and forged
ownership rejections. This updates the real join instead of retaining a redundant
import to satisfy a stale location assertion. The retained first post-removal
ownership nonpass identifies that stale assertion.

At clean load-fix source 739fbcd8ff5fb7fa91696c80b31741367de33bdf, full
provider-context reproduction uses the original 50 paths plus the newly changed
00857: 3459 unchanged baseline warnings and zero blockers. The first observer
incorrectly expected 00857 to be among those 50 direct paths, failed before lint,
and is retained; the corrected 51-path run covers the actual new change.
Native 28-record parity retains d76b80092e45ef9dfec5b9189649a5d49772646b8284d4085c758eb245767577;
focused rules/inventory remain 8 / 148. The updated ownership/boundary run passes
48 / 1202 with no skipped namespaces. The exact 256MiB CI harness passes
49 tests / 519 assertions, including real lint and all original adversarial
controls. The first local harness attempt omitted the pinned linter from PATH;
its three launch errors remain separate preparation evidence, not code failures.
The corrected environment uses the same real pinned linter without skips or
policy changes. Thirteen own generated harness output files were moved
recoverably to /tmp/uniform-macro-native-ci-coverage-target-ci after its process
ended, with full byte/hash preservation. No foreign checkout or source was cleaned.

At corrected source fe6741d7fd5a913553cf41ec2e388a66f9e6ca43, tree
5e628d1f583cd9c0cb78ae01ea892f826e1441e5, the complete 53-path combined
source context against captured Main3c97 passes: 3459 unchanged baseline
warnings, zero blockers. The four known Main-only project paths are excluded
from the source-branch comparison because the provider retains them from Main.
This is local reproduction, not a new hosted provider or integration grant.
Conservation covers eleven total slice paths: the original two rule bodies
remain exact modulo their ports, all old Macro bodies and surrounding source/
authentication rules are unchanged, the late prefix deletion is exact, and
the CI/ownership control changes receive separate review. One initial local
conservation-observer delimiter error was corrected before it could run and
is not production evidence. Independent acceptance of this corrected source,
fresh exact-head hosted CI and current-base clean integration remain required.
The failed a1/provider4e55 run remains NONPASS permanently.
