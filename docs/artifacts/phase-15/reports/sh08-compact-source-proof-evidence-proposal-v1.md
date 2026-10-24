# SH-08 Compact Source-Proof Evidence: Draft Design Discussion v1

Status: draft discussion, proposal only, 2026-10-24.

This document proposes a bounded experiment in representation. It implements
nothing, admits no candidate, clears no failed invariant family, and is neither
an accepted RFC nor proof authority. Publication and review of this discussion
do not authorize implementation, GIS execution, a larger allowance, or another
attempt. Every acceptance condition below is prospective unless explicitly
identified as retained evidence.

## Purpose and authority boundary

Explore whether one source-dependent numeric proof can retain complete fresh
input matching while avoiding repeated large values in its serialized output.
The proposed first witness is the held guarded-integer-subtraction (GIS) rule.
GIS is not present on the inspected main revision. This discussion does not
land that rule or turn its development evidence into source admission.

The only deliverable is this Markdown file. No compiler, test, fixture,
interface, catalog, manifest, ledger, resource policy, or evidence record is
changed. The proposal is not recorded as an `architecture_decision`, an
integrated dependency, or prerequisite authority. It cannot satisfy WG008.

[Workstream governance](../../../workstream-governance.md) separates ordinary
development discussion from recorded candidate admission. Its closed
`gravity-architecture-authority-v1` block governs authority-bearing architecture
decisions. None is asserted here. Strict `validate_architecture_authority.clj
--report` without that block would report AA001; no pass or legacy exemption
is claimed. A later governed decision needs real records, the closed block,
normal review, and the applicable lifecycle. This note cannot substitute for
them. The implementation hold remains in force.

## Governing dependencies

The phase indexes and owning documents, rather than a host representation,
govern any later implementation:

- [D6](../../../phase-00-foundation-and-thesis/007-d6-performance-philosophy-and-charter.md),
  [D8](../../../phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md),
  and [D9](../../../phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md):
  preserve safe semantics and evidence; measurements do not prove correctness.
- [C6](../../../phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md)
  and [C7](../../../phase-06-compiler-architecture/086-c7-type-checker-design.md):
  preserve source/generated origins, binding identities, evaluation order,
  type facts, deterministic constraints and diagnostics, and pass verification.
- [SAFE9](../../../phase-02-safety/038-safe9-numeric-safety.md)
  and [SAFE15](../../../phase-02-safety/044-safe15-safety-proof-and-certificate-model.md):
  bind numeric assumptions to the actual operation and invalidate changed proof
  inputs. Runtime guards remain; this proposal grants no check elision.
- [TEST2](../../../phase-14-testing-verification-and-conformance/191-test2-compiler-test-strategy.md),
  [TEST9](../../../phase-14-testing-verification-and-conformance/198-test9-fuzzing-and-property-testing-plan.md),
  and [TEST13](../../../phase-14-testing-verification-and-conformance/202-test13-self-hosting-validation-plan.md):
  accepted/rejected fixtures, preservation and composition checks, reproducible
  hostile cases, and separate self-hosting obligations.
- [BOOT3](../../../phase-15-bootstrap-and-self-hosting/205-boot3-self-hosted-compiler-plan.md),
  [BOOT4](../../../phase-15-bootstrap-and-self-hosting/206-boot4-compiler-in-gravity-coding-standard.md),
  [BOOT7](../../../phase-15-bootstrap-and-self-hosting/209-boot7-self-hosting-validation-and-equivalence-plan.md),
  and [BOOT8](../../../phase-15-bootstrap-and-self-hosting/210-boot8-bootstrap-artifact-provenance-specification.md):
  deterministic Gravity-owned implementation, explicit trust boundaries,
  reviewed comparison modes, and reproducible provenance at migration.

[Development verification](../../../development-verification.md), especially
the C7 index and caller-selected work-allowance sections, supplies the existing
focused workflow. It does not authorize a run in this proposal-only task.
The source language boundary and per-function physical LOC limit remain.

## Inspected revisions and retained evidence

The source baseline is integrated commit
`25cc478b7982c921614c0cdcf40f5f3933d12514`, tree
`ae0108231f36444763802b7acb734fb1f06b9b35`, from PR 291. This discussion starts
on `codex/c7-compact-source-proof-proposal`. The landed work-policy API is a
trusted invocation option, outside canonical schema-3 output and identity.
Its per-module pool is cumulative across rounds; its verifier selects its own
allowance. It grants no whole-graph, CPU, RSS, or arbitrary-carrier bound.

The separate held reference is
`a7f2af95b5f0f20994af536d3ef08bd927892e79`, tree
`7b901e31e2decd06a40ce677873d4433e562cb31`, on the retained GIS branch. Its C7
source SHA-256 is
`ab4464d5dab37bc7be790638762a95c4ac6a21352289b45571b21ede757b4232`;
its SH08 function-test SHA-256 is
`3c4bff2625b1aa812e92d5b3f585ee55dd9303ae75b67c1c91b879dc54fff3f5`.
These are historical comparison inputs, not dependencies conferring authority.
No held branch content was applied, parsed, compiled, or executed for this note.

Two retained a7 logs bound the motivating claim:

| Retained attempt | Actual result | Evidence identity |
| --- | --- | --- |
| Explicit-policy unseeded normalized program | One failed assertion; 68 planned assertions unrun. At N=1048576, round one charged 16860; round two entered at 1031716 and reported 1053329, leaving -21613/resource-limit. | `/tmp/sh08-gis-a7f2af95-policy-unseeded-20261024.log`, SHA-256 `24ea9c5c27325c2d8dcc392532c77b4d8eb17492a4b94323974c6884a00d267c`; original command `exec-a4d07205-23a3-4840-be1d-882e6e36e414`. |
| Original observed unseeded normalized program | One failed assertion; 44 planned assertions unrun. Same refusal, with a bounded incomplete trace. | `/tmp/sh08-gis-a7f2af95-policy-observed-unseeded-20261024.log`, SHA-256 `a02cef752b6db6d754b6cd8413e960d1a048d26a4fd7ac9280ce9c6ccb677d78`; original command `exec-04408557-945d-4108-bf70-01429373b0c5`. |

The observed trace retained 114 rows and dropped 114; `incomplete` was true.
Inline equality and recur work were unobserved. New allowance-aware workers
were not all separate observation targets. This is not a complete call census,
global first refusal, total-work decomposition, or minimum successful allowance.

In retained row 228, one `sh08-gis-data-work` under source work entered with
67410 units. Validation completed at 33232, and a three-comparison reservation
required 55791 more: required total 89023, with comparisons and storage not
performed. Thus 67410 - 33232 = 34178 available versus 55791 required gives a
local shortfall of 21613. Its enclosing source-work row 132 reported 314461
required against 292848 incoming, with 258670 completed. Readset ordinal 5,
`[sources 3]`, identifies that selected source-prefix vector by static inspection;
the old comparison bound there is W=18597. An earlier retained row 116 accepted
that leaf at 798312. Neither row proves the global minimum or the next frontier.

The original deadlines and failures remain failures or unverified coverage.
The policy-only scalar, imported-module, neighbour-fuel and minimal charged-map
passes establish their exact tested scopes. They do not establish GIS support,
GIS fit at N=1048576, compact-proof performance, or a retroactive pass of the
larger charged-map or six-variable source batches. No attempt is rerun here.
The local `/tmp` logs are retained observations, not a newly published durable
evidence bundle; later executable claims need their own complete retained inputs.

## Actual shape and consumer inventory

Line anchors for GIS below refer to the held revision, not main. Main C7 source
is `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.

| Boundary | Actual inspected behavior | Representation pressure or obligation |
| --- | --- | --- |
| Public C7 ingress | `sh08-ft-authenticated-core` checks the request/Core/B47/coordinator relation. `sh08-im-derive-with-allowance` obtains the current binding table and original authenticated forms. Imported producers use invocation-local request matching. | Neither a proof id nor an imported accepted result replaces these checks. No new cross-invocation memo is proposed. |
| GIS source selection, held lines 20750-20879 and 21039-21195 | Scalar-ID scans find unique control/function/call rows. Three eight-row packets contain node, call, edge, operator, binding, reference, literal and caller projection. Other readsets contain control rows, parameter/function records, selected source prefixes, heads, module/type facts and ancestry joins. | Full packet canonical validation includes unread payloads such as binding-context. Source prefixes retain source-span, origin-chain and generated-origin, plus values where read. |
| GIS canonical/payment path, held lines 20895-20962 and 21196-21360 | `data-tallies` separates actual validation/bookkeeping B=V+T from comparison/storage bound W. Packet work validates whole values, reserves projection construction and then pays field comparisons. Subtraction construction reserves 173 units; each ordering packet has separate domain and 228-unit projection reservations. | Paying validation does not pay comparisons, hashing/rendering or output storage. A narrow readset is not permission to omit the whole-packet refusal boundary. |
| GIS construction, held lines 21371-21516 | `reconstruct` carries full parameter records/forms, outer and inner if nodes, false node, two ordering descriptors, three literal proofs and ancestry in `:range-proof`. The application descriptor also carries source, operator-source, call, edge and binding. Producer validates and pays `[descriptor constraint]` storage. | Several nested paths repeat the same source-derived values. Their physical/canonical output size has not been measured by this note. |
| GIS consumer, held lines 21527-21567 | Consumer validates the supplied descriptor, freshly derives expected output from current inputs, reserves full output comparison, then compares. Constraint dependencies and call facts embed the descriptor; constraint identity and solution also retain proof data. | Compactness must cover these output copies, not only a temporary construction view. Independent consumption must still reconstruct. |
| Public result and verifier, main `sh08-ft-finish-function-type-core` and `sh08-verify-function-type-result-with-work-policy` | Operator types, constraint ledger and call facts are repeated in canonical result, identity-input and typed-core projections. Fresh verification rebuilds the whole expected result with its own options. | Host sharing does not shrink printed/canonical output. An in-memory alias, new envelope or small diagnostic printout is not output compaction. |
| C8/body effects and development lowering | `effects.gravity` owns body numeric descriptors and shared function identity projections; C8 checks exact schema-3 projections. `emitter.gravity` independently reconstructs supported numeric descriptors. Existing quotient/unit-step families carry similar source-rich proofs. | These are compatibility consumers, not existing GIS implementations. Generic call-fact shape/effects acceptance does not validate an unknown numeric proof version. |

Current `effects.gravity`, `c8_effect_checker_engine.gravity`, `emitter.gravity`
and `runtime.gravity` contain no GIS rule. C9/C10/MIR must preserve or invalidate
evidence according to their contracts; that is not evidence they understand a
new GIS proof. The detached C8 function adapter currently receives typed output
and a verification value, not the original C7 source request. That missing
input context is a real compatibility boundary, not permission to trust ids.

## Diagnosis: three different questions

| Question | What the retained evidence supports | What remains unknown |
| --- | --- | --- |
| Construction and output size | Static code builds projection packets and embeds large records in nested proof/output positions. This is a concrete place to try less materialization and smaller serialized output. | Actual allocation, unique live objects, serialized byte totals, peak heap and CPU share. |
| Logical inference work | The normalized cold program exhausted an explicit cumulative pool. One retained source-prefix comparison reservation failed after complete local validation. | Complete total by stage, first actual refusal, output-storage contribution in the failed invocation, successful N or benefit of the proposed form. |
| Scheduling and execution time | Existing processes and deadlines bound their own attempts. | A proof that a scheduling change, runtime optimization or larger deadline fixes the logical refusal. A deadline without stage evidence does not identify a compiler hotspot. |

The experiment must address input matching and output size together. Merely
renaming W, removing charges, increasing N, resetting rounds, printing fewer
fields, or moving the same expanded output behind a wrapper answers neither.

## Proposed smallest representation

Use one rule-specific compact range witness, not a general proof arena,
certificate service or new pass. Keep the existing authenticated source/Core
request as the source of actual records. Suggest
`:sh08-guarded-integer-subtraction-v2` as the application/proof rule version;
the name is a design suggestion, not a reserved or implemented interface.
Keep schema-3 top-level keys and unrelated proof families unchanged if the
consumer review confirms that nested versioning is sufficient.

Retain the current small semantic header: operation and result type, numeric
mode, safety outcome, argument types, profile/target, effects/capabilities,
evaluation order, source/origin diagnostics, invalidations, retained runtime
guards and `:check-elision :not-authorized`. Initially retain the outer
descriptor's existing source/call/edge/binding fields. Compact only the nested
range witness; removing those outer fields is a separate compatibility choice.

Replace its expanded records with the following closed, typed roles:

| Role | Proposed retained data | Reconstructed from current authenticated input |
| --- | --- | --- |
| Input binding | Current Core artifact id and owner function syntax/core ids | Full Core, B47/coordinator binding, actual forms/bindings and module. The id is a locator and mismatch check, never evidence by itself. |
| Operation | Subtraction node/operator/binding ids and ordered operand ids | Exact current call/edge/operator records and source occurrence joins. |
| Parameter | Parameter binding id and index | Unique named function record, unannotated parameter declaration, immutable binding, defining syntax and reference uses. |
| Control | Outer-if, condition-if, lower/upper-call and false-node ids, with the required then-arm coordinate | Exact unique control topology, ownership, evaluation metadata and all current source/form links. |
| Numeric claim | Lower/upper/subtrahend values and input/result intervals | Actual Integer literal occurrences and independent ordering operations; subtrahend equals lower bound, lower <= upper <= 127, result interval [0, upper-lower]. |
| Ancestry | Ordered form/syntax-id pairs up to the existing ancestry-depth bound | Every current parent step, kind, owning function and full origin evidence used by the rule. No detached ancestry assertion is trusted. |

Ids and ordered coordinates distinguish occurrences with equal values. Equal
content is not equal source occurrence. No full fn/if/call raw `:value` tree is
added to the compact witness; such values remain in the original input wherever
the existing canonical refusal boundary requires validation. Source spans,
origin chains, generated origins and arbitrary metadata do not become hashes.

All copies in operator type, constraint dependencies/identity, solution,
call-fact application, result identity and typed-core use this same smaller
closed value. No path silently retains the old expanded nested witness. No
top-level proof table is needed for this first experiment. No serializer or
host object identity is relied on for sharing. A consumer without the original
authenticated input cannot resolve this witness and must reject it until its
own source-bearing contract is explicitly supported.

For a fixed number R of rule roles and ancestry length A, the proposed nested
witness contains O(R+A) scalar references plus a constant number of numeric and
policy fields, rather than copies of the referenced records. This is a
structural estimate, not a byte, allocation or time measurement. The retained
outer source/call/binding payload S still contributes to every descriptor, and
the public result still has multiple descriptor positions. Measure all copies;
a smaller nested witness with no useful whole-result reduction fails the
output-size part of the experiment.

## Complete fresh input matching

The proposed reduction is fewer repeated large comparisons, not fewer facts
checked. A future implementation must account for each existing comparison site
and retained packet field in an independently reviewed readset-to-obligation
table. It must demonstrate these steps on every independent invocation:

1. Preserve public option, request, B47/Core/coordinator and import checks.
   Keep active/deferred behavior: unresolved operands do not gain a proof or
   trigger unrelated source interpretation. Validate native domains and bounds
   before hashing, rendering, equality, indexing or constructing derived values.
2. Validate the complete original packet values required by the old refusal
   boundary, including unread binding-context and nested metadata. Preserve
   current scalar/container domains, depth/width and pending-work bounds.
   Compact projections cannot sanitize, drop or replace hostile original data.
3. Pay for complete physical-row scans before selecting unique ids. Check all
   rows relevant to ambiguity, mutations, recursion/reentry and recur transfers;
   do not stop at the first matching row. Private invocation-local indexes may
   select records only after duplicate and domain checks. Any once-only
   validation factoring must be dominated by validation of those exact immutable
   current inputs and remain private to that invocation; no supplied flag,
   stored acceptance token, host identity or cross-invocation cache is evidence.
   Only immutable source selection may be factored this way. Round-varying type
   facts, inferred descriptors, binding shapes and their matches are recomputed
   at the current step/consumer; a prior round's successful match is not reused.
4. Join the selected current records field by field with the compact roles.
   Recheck complete call and edge shape; operator spelling/package/binding;
   owner/parameter/definition relations; all ordered children and source parents;
   full source/origin fields where compared; profile/target/safety and declared
   effects/capabilities; literal values and types; ancestry and unique then-arm
   control. Retain every currently read `:value`, including operator, reference,
   literal and Boolean-false values. A narrower witness cannot narrow this set.
5. Reconstruct the numeric proof from these matches, not from the supplied
   interval, a preseeded type, declared call result or existing descriptor tag.
   Validate and pay all compact output construction/storage, then independently
   compare supplied and reconstructed witnesses. Reject stale, extra, missing,
   inconsistent or unsupported fields/versions.
6. The public producer and fresh public verifier independently perform steps
   1-5. Every standalone consumer also obtains its own current source context and
   performs complete matching. A successful producer receipt, matching digest,
   `:passed` field or proof-pool membership cannot replace that work.

Full validation still traverses large original values. Compaction may therefore
fail to improve a particular input. Retaining that refusal is preferable to
turning an expensive boundary into acceptance by omission.

The boundary remains standard canonical first-order carriers. Hostile-leaf tests
inside those carriers do not establish callback freedom or hard preemption for
arbitrary host collection/scalar implementations. No new host trust claim is
part of v2.

## Work, construction, diagnostics and invalidation

Keep N and all unrelated depth, width, round, import, throw and host ceilings
unchanged. The versioned operation must debit one cumulative pool across cold
rounds and the existing call-consumer path. Fresh consumers use their own
options. No automatic search for N, retry, growth or scheduling change belongs
to this experiment.

Publish a v2 fee derivation before implementing it. Distinguish complete
original-data visits/bookkeeping, scalar field matching, scans/index creation,
projection/materialization, comparisons, hashing/rendering when required, and
compact output storage. Prepay each operation before performing it. Do not
substitute B for W or infer a comparison/storage payment from a validation
payment. A deduplicated source-row visit needs an exact unique-row justification;
physical duplicate candidates and original nested occurrences still count.

The legacy fee schedule and boundary tests remain historical reference data.
Smaller v2 outputs and different field comparisons can have different exact
fees; those are a reviewed versioned algorithm change, not v1 fee equivalence.
No improved successful allowance or unchanged resource frontier is promised.
At every v2 reservation, exact payment must accept the otherwise-valid local
operation and one unit less must refuse before the unpaid operation. Test
whole-inference cumulative exhaustion separately from local exact-budget cases.

Preserve the old diagnostic phases: malformed option/request/authentication;
bounded canonical/domain checks and work reservations; operand/deferred checks;
source/control matching; semantic range proof; output validation and supplied
proof matching. Preserve existing priority within each active old route.
Unknown-version and compact-reference failures need explicit reviewed goldens,
not accidental generic-shape acceptance. Multi-fault cases must show which
error wins at sufficient budget and which reservation wins at insufficient
budget. Compare rule/reason, source node/syntax/span, generated-origin chain,
profile/target and remediation, not only rejected status.

Invalidate on all held GIS dependencies: calls/edges, binding table and package
identity, source forms/origins, type facts, function records/parameters,
mutations, recursion/reentry, recur targets/transfers, control-flow and operand
position, profile/target/safety/numeric mode, and evaluation order. Compiler or
proof-version changes invalidate the corresponding derived output/evidence.
Provenance path fields retain their current path-neutral identity rules;
compaction must neither erase diagnostic origins nor introduce path-sensitive
identity. Equal digest syntax or self-consistent recomputed ids cannot rescue
changed inputs.

## Version transition and downstream compatibility

No existing implementation accepts the proposed v2 proof. The held v1 is not
an admitted compatibility baseline. Existing supported numeric families on main
remain unchanged, including their versions, descriptors and exact interfaces.

| Producer/consumer combination | Required future behavior |
| --- | --- |
| Existing supported main family through unchanged consumers | Preserve existing accepted/rejected behavior, identities and diagnostics. |
| Held GIS v1 fixture as comparison oracle | Development reference only; never describe it as a main/release capability. No silent v1-to-v2 relabeling. |
| Implemented v2 producer plus v2-aware C7 fresh verifier | Fresh matching and expected-result reconstruction must pass; changed source or candidate must reject. |
| v2 candidate plus old or unaware consumer | Reject explicitly before treating generic call-fact shape or empty effects as numeric legality. |
| v2 C8/body-effect or lowering consumer with its own actual source context | Independently reconstruct accepted facts and compare; preserve effects, evaluation, guards, origins and identity projections. |
| Detached C8 adapter without current source context | Reject v2, even with a self-consistent typed result and supplied verification map. Extending its original request contract needs separate review; this note does not add a wrapper. |
| Missing input, wrong module/occurrence, mixed v1/v2, dangling or duplicate references, unknown/extra fields | Reject deterministically; do not expand, downgrade, ignore or treat as opaque proved data. |

Review rule version and constraint/call-fact identity domains together. All
application copies must agree; artifact identity must bind the compact content
and version. Reusing a v1 identity for different v2 content is forbidden. Whether
nested versions suffice for the public schema-3 consumer contract is an open
compatibility decision; if not, stop for a separately authorized schema change.
Do not add an output table, option key or public entrypoint speculatively.

## Prospective owners, paths and acceptance matrix

These are future implementation responsibilities, not current path grants.
No person or workstream is assigned by this note.

| Owner boundary | Exact prospective source/test paths | Required work before a claim |
| --- | --- | --- |
| C7 rule and fresh verifier | `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`; `bootstrap/clojure/test/gravity/self_hosting/sh08_function_call_type_test.clj` | Reconcile a separately authorized minimal rule onto then-current main; close readsets, fee derivation, output copies, versions, diagnostics and full producer/consumer matching. Never merge the held branch wholesale. |
| C8 body analysis and adapter | `bootstrap/gravity/src/gravity/effects.gravity`; `bootstrap/gravity/src/gravity/compiler/c8_effect_checker_engine.gravity`; `bootstrap/clojure/test/gravity/self_hosting/sh09_body_effect_test.clj`; `bootstrap/clojure/test/gravity/self_hosting/sh09_c7_effect_adapter_test.clj` | Add explicit unsupported-version fences first; support only with independently checkable current source context and reviewed identity projection. |
| Development lowering/runtime | `bootstrap/gravity/p15_s23/emitter.gravity`; `bootstrap/gravity/src/gravity/runtime.gravity`; `bootstrap/clojure/test/gravity/self_hosting/sh01_stage2_runtime_iteration_test.clj` | Lowering rederives proof premises, records retained checks and rejects substitutions; original runtime path only after lowering acceptance. Runtime edits are justified only by an actual affected contract. |
| Source fixtures | `bootstrap/clojure/fixtures/self-hosting/sh-08/accepted/compact-source-proof-interval.gravity` and `.qst`; matching `rejected/compact-source-proof-interval-*` pairs | Proposed new paths only. Real inline guarded source with an unannotated parameter called with an Integer; no supplied type/proof annotations or fabricated B47 facts. Canonical files must survive the full reader/C5/Core/request lifetime. |
| C6/provenance boundary | `bootstrap/gravity/src/gravity/checked_core.gravity`; `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00887-sh07-core-build-form-tree.clj`; `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00890-sh07-b13-module-assembly-manifest.clj` remain unchanged for the proposed first experiment | Reopen ownership only if real source-occurrence transport fails. A proof-format change is not permission to modify input provenance or the reader. |

| Future case | Producer | Fresh consumer/verifier | Required evidence |
| --- | --- | --- | --- |
| Valid minimal packet and complete cold normalized program | Derive v2 without preseeded parameters or declared result borrowing | Independently accept reconstructed output | Exact input/output/version and complete counts; normalized results remain development-only. |
| Real accepted inline interval, unannotated parameter, actual Integer caller | Original reader -> C3/SH05/SH06 -> Core/B47 -> C7 succeeds | New independent public verification succeeds at its own fixed N | Function parameter/return/call types, range and source facts; exact executed spelling. Paired bytes/parser parity alone is not a second typed pass. |
| Guard missing, wrong arm/owner/binding, wrong subtrahend, reversed interval, upper >127, mutation/reentry | Reject at the original semantic/source boundary | Reject the corresponding supplied proof independently | Stable reason and full source diagnostic; wrong range never becomes accepted Dynamic. |
| Valid proof transplanted into changed source with retained or coherently rebuilt ids | Reject or derive a different expected proof | Reject against actual current input | Change source span/origin, source parent/child, package/operator binding, function/parameter, operand type, call edge and control relation independently. Digest-only self-consistency is insufficient. |
| Hostile packet or compact carrier | No proof; reject safely | No unpaid or untyped leaf callbacks | Opaque values at active metadata and untyped keys/ids, nonfinite/fractional values in Integer/id/coordinate fields, extra/absent fields, duplicate/dangling ids, cycles, depth/width excess and oversized tails. Preserve existing general scalar domains and defined handling of inactive/quoted data. |
| Tiny, exact, exact-minus-one, accumulated rounds and consumer allowance mismatch | Refuse before unpaid work or accept at exact local payment | Fresh consumer pays its full separate work; cannot inherit producer N or prior validation | Separate assertions for completed versus required work; zero forbidden equality/hash/render after reservation failure; unchanged neighbouring-family controls. |
| Compact result/application/constraint/identity substitution | Never publish mismatched copies | Reject every single-copy and coherent multi-copy tamper | Whole-result fresh reconstruction, not a count check, positive receipt cache or hash comparison. |
| Downstream supported versus unaware versions | Emit only the explicitly tested surface | Effects/lowering accept rederived v2 or explicitly reject unsupported v2 | Original consumer calls and negative transfer fences. C9/C10/MIR preservation does not imply GIS proof interpretation or check elision. |

Future output artifacts should include a complete readset/preservation table,
independent fee worksheet, positive and hostile fixture manifest, stable
diagnostic results, original command/cleanup records, and a comparison report.
The report must measure expanded canonical bytes and scalar/container/depth
counts of the nested witness, descriptor-plus-constraint and whole public
result including identity/typed-core copies. Count original physical source
input validation separately. Report construction work, input matching, output
storage/comparison and total cumulative inference independently. An incomplete
observer must mark missing coverage and cannot establish a total.

The first size comparison must show a strict reduction for both the nested
witness and the complete canonical public result on the same accepted case,
with bytes and structural counts reported. The input comparison must retain
full original-data validation and include all new construction/index fees.
Reducing repeated comparisons while increasing total work is reported as such;
it does not establish a cheaper complete invocation or a successful fixed N.

## First falsification question and stopping rules

After separate authorization, ask one bounded question first: for the same
minimal valid and hostile canonical source packets, can this one rule's compact
witness reduce both retained output size and repeated input-comparison work
while preserving complete fresh validation, all required matches and rejection
priority? Use an independently reviewed old expanded reconstruction as the
development oracle. A fixture containing a proof is not implementation, and a
shape-only compact validator is not the fresh consumer.

Start with the standalone constructor and independent consumer, exact/minus-one
and hostile cases, at one predeclared allowance and existing host limits. Do not
begin with a whole-inference replay. Only after that gate passes and a separate
run is authorized should the cold normalized producer and then one minimal real
source/public-verifier composition execute. Downstream execution is another
affected boundary, not an automatic follow-up. No current log is permission to
run these future steps.

Stop if compact ids cannot bind all current inputs, complete canonical
validation is omitted, output expansion recreates the same copies, unsupported
consumers accept by generic shape, diagnostics lose provenance/priority, costs
are unaccounted, or one fixed bounded gate fails. Report the failure and missing
coverage. Do not raise limits or rewrite failed evidence to manufacture fit.

## Alternatives, tradeoffs and unresolved choices

- Keep full evidence and existing limits: simplest compatibility story, with
  the known refusal retained. This is the fallback if compact matching cannot
  preserve the boundary.
- Only factor construction or prepared indexes: may reduce interpreter work,
  but unchanged logical charges and repeated canonical output remain. Useful
  independently, insufficient for this two-part question.
- One module-wide proof/source arena: could remove more output repetition, but
  adds table ownership, canonical ordering, liveness, cross-reference, standalone
  transport and schema migration obligations. Defer until the rule-specific
  experiment establishes a need. Never deduplicate by unvalidated digest or
  host equality.
- Compact ids plus a shared acceptance cache or hash-only certificate: reject;
  it cannot establish fresh binding to current source/metadata and independently
  reconstructed output.
- Higher N or a different scheduling lane: may alter a separate resource
  question, but supplies neither compact output nor current input matching.
  Neither is part of this proposal.

Open decisions for a later implementation review are the exact closed v2 field
set and fee worksheet; whether nested versioning suffices for every public
consumer; how each source-bearing downstream consumer obtains original context;
and whether retaining the outer source/call/binding fields still gives useful
output reduction. No size target, successful N, wall-time bound, allocation
ratio, CPU/RSS guarantee, or general proof-family benefit is asserted before
measurement. Any broader arena or context API requires a new scope decision.

## Discussion completion and explicit nonclaims

This proposal is ready for document review when its input/output inventory,
retained evidence limits, fresh-validation obligations, version/rejection rules,
owners and acceptance matrix are independently assessed. Static ASCII/link/diff
checks and coordinator-allocated documentation validators validate the document
only. They do not validate the proposed algorithms. No JVM, parser, source probe,
inference replay, runtime benchmark or new method was run to write this note.

There is no new GIS capability, compact certificate implementation, safety or
performance result, full compiler support, bootstrap stage advancement,
self-hosting, seed retirement or release authority. Normal document integration
publishes a discussion. Implementation and any later governed admission remain
separate decisions under the existing hold and governance rules.
