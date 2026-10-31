# Expected Core construction: pinned source trace after session 53455

## Result and basis

No source-proven semantic mismatch or corrective compiler hunk was found.
The next missing observable is the caught root ExceptionInfo's known structured
code (`:id` or `:rule`), followed, where present, by a known top-level literal
`:reason`. The existing class-only contract does not expose those fields.
This result proposes a narrow extension for review; it implements no new
exception-data observation and grants no diagnostic execution authority.

The exact input is source `f2b5080bc84b343eea1951bb223b47e536feb90d`, tree
`fd1147e505146b06db224608ac5a2a87dbe74062`, over base
`3a4d5ea11c946bd374dfdfa09feaa1d78aa6c582`. The retained stop receipt SHA256
is `8ca67a43cec99d46c2ea0b5f590c8ae6e9f9563eec57647fe8b4f1d04cb901ca`.
It records the third selector's single session 53455, exit 1, and the closed
ExceptionInfo-class result. That is evidence of an exception, not its rule,
originating operation, failed prerequisite or root cause. Session 56177 remains
unknown independently; the fourth selector remains unrun.

The accompanying `expected-core-failure-source-trace.edn` pins 13 directly
inventoried Core/projection files and 17 additional call/contract/fixture files.
Its 41 syntactic ex-info sites include one explicitly excluded neighboring API
site. It is a bounded source cut, not an exhaustive scan of every transitive
compiler, runtime, digest or envelope operation. Each recorded site has its
own physical source line, owner, literal id/rule/reason, data-key list and
whether the ex-info call retains a cause. Dynamic diagnostic maps are marked
rather than guessed. No actual exception message, ex-data, path, cause or stack
was read to build the inventory.

C6 governs the authenticated Core input and provenance boundary; L9 separates
absence, recoverable exceptions and fatal failures; C15 requires stable,
redacted diagnostic codes; BOOT7 requires exact provenance for comparison.
These contracts justify tracing and an explicit diagnostic proposal. They do
not turn missing observations into a semantic failure diagnosis.

## Exact selected branch and ABI

The selector at `sh01_stage2_runtime_iteration_test.clj:29016` creates the exact
two-key invocation with `:reader-source-path` and
`:authenticated-envelope-options {:source :development-current}`. It passes
the same object to explicit C5 source construction (five arguments), then the
two-argument development session. Its first positive source is
`fixtures/self-hosting/sh-08/accepted/string-set-carrier-equality.gravity`.
The source contains a String argument, Set insertions/comparisons, typed
function parameters, recursion and quoted Set literals; these facts alone do
not establish any failing projection or semantic rule.

The accepted 00899 session uses `some?`, preserving its explicit two-argument
Core port and three-argument resolver-verification port with `::legacy`.
The invocation is a nonempty map, so 00897's existing truthy decisions select
its explicit branch too. No false/nil discrepancy is proved for this fixture.
Omitted/nil behavior remains an independent accepted legacy contract.

Construction proceeds in this source order:

| Step | Owning entry and source | Potential failure family |
| --- | --- | --- |
| 1 | 00898 `sh07-core-from-resolution-artifact` -> 00897 `sh07-core-with-fresh-construction:347` | Constructor wrapper; build closure is local, not caller authority |
| 2 | 00897 `sh07-core-construction-carrier!:16` | Aggregate or semantic component carrier refusal |
| 3 | 00813 `sh03-reader-authenticated-envelope-options:3` -> 00808 `sh03-reader-invocation-source!:199` | Exact invocation shape/options and selected-source binding; C2-HASH boundary |
| 4 | 00897 `sh07-core-construction-dependencies:51` | Core/resolver source keys, pinned macro and syntax sources, SH02 selected envelope operation |
| 5 | 00897 `sh07-core-construction-bindings:63` | Resolver current binding and freshness, macro/syntax current bindings, Core cached-binding dereference |
| 6 | 00897 roots/phase -> explicit SH06 verification | Root/phase reads and the actual upstream verifier; class alone does not reveal its outcome |
| 7 | Passed report: Reader check and construction bookends, then `request-from-verified-upstream` | Carrier, catalog, source, thread, phase, root, Reader, envelope and binding joins |
| 8 | Nonpassed report: same local build closure, fallback request from that report | checked_core request admission rejects missing fresh verified SH06 input; this branch does not establish C5 success |
| 9 | 00898 `sh07-core-build-verified-artifact:20` | Source-key/captured-binding join; execution binding; run-with-construction membership check |
| 10 | checked_core `run-structural-request-for-test:3723` | Request preflight; template build/verify; digest resolution; resolved verify; invalid status or revision drift |
| 11 | 00898 artifact assembly, construction check, Core verification/proof, source-key check; 00897 final bookend | Existing evidence and identity joins; no report/catalog bypass |

The library `authenticated-request:2899` is not the entry for this constructor:
00897 directly calls `request-from-verified-upstream:2638`. In particular, the
neighboring missing-lexical-context throw at checked_core:2949 must not be
reported as a demonstrated origin of this exact constructor branch.

## Reachable structured-code sites

00897's central throw at line 12 uses id C6-VERIFY and a reason supplied by six
literal call sites: trusted-artifact-carrier, trusted-semantic-components,
envelope-source-location, construction-selected-reader-drift,
construction-dependency-drift and construction-check-catalog-drift. A source
walk does not tell which, if any, was taken.

checked_core binding construction has C6-VERIFY sites at 974, 995, 1075, 1088
and 1098. They cover module contract, source drift and toolchain drift. 00898
adds two operation-revision-drift sites at 34 and 71. Source keys and cached
bindings are actual runtime comparisons, not facts recoverable from the old
class-only receipt. No constant or current hash is adopted here.

Request conversion can throw at checked_core:2710 before semantic projection.
Decimal/origin work can throw at 1294, 1629, 1699 and 2142 (C6-ORIGIN).
Fragment alias work can throw at 2540 (C6-VERIFY). The installed projection is
not the unrelated stage0 `c6_core_lowering/projection.clj`: the selected
`154235-sh07-core-resolution-install.clj` installs `sh07-core-resolution-projection!`.
Its scope/reference joins throw at lines 109 and 155; its projection helper
throws at 107 and 121; its sequence helper throws at 40; upstream-parity throws
at 24. Their literal reasons are recorded in the EDN. These joins are reachable
through `request-from-verified-upstream`, but none is observed failing.

checked_core's request-preflight emits structured request diagnostics through
`request-diagnostic!:3571` (throw at 3618). It can reject map/vector shapes,
projection binding, alias lineage, schema 15 or scope. Its diagnostic uses
`:rule "C6-VERIFY"` and puts its reason under `:facts`; the proposed minimal
top-level-reason view below intentionally does not traverse that nested data.
`raise-diagnostic!:3549` (throw at 3567) instead emits the first Gravity
rejection diagnostic. The Gravity source catalog at checked_core.gravity:193
names stable Core codes. Returning an ExceptionInfo does not prove a template
was even reached or rejected.

Execution/digest paths add C6-VERIFY sites at checked_core:3005, 3374, 3391,
3408, 3417, 3451 and 3490. Structural execution adds 3766, 3805, 3831, 3845 and
3854. Several have no literal top-level reason. Consequently C6-VERIFY alone
cannot distinguish module-contract rejection, unresolved digest reference,
malformed digest request, template replay, resolved replay or invalid status.

There is an additional information-loss boundary at checked_core `execute!`:
lines 1154/1164/1174/1184 normalize synthetic stack/memory/assertion/linkage
Errors into ExceptionInfo without a cause; line 1195 wraps ordinary Exception
with a cause; ExceptionInfo itself is rethrown. Thus class-only evidence also
cannot exclude earlier host-error normalization. c28's correct causal scan can
only preserve causes still present; it cannot reconstruct a cause erased by a
lower layer. This is a source limitation, not a claim that a host Error occurred.
No change to that upstream policy is included.

Delegated edges remain explicit: macro pinned-source guards use C4-TRACE
(macro.clj:473), syntax pinned-source guards use C3-ID (syntax.clj:1258), and
Reader source validation uses C2-HASH (00808:77). Resolver binding, SH02
construction/verification, plan compilation, generic execution, canonical
hashing and proof-transaction operations can themselves refuse. Their runtime
inputs/results were not inspected; this bounded inventory does not claim all
their transitive throw sites or codes are closed by the five-code proposal.

## First missing observable and narrow contract alternatives

Recommended next owning interface: the ordinary-exception outcome in 00899,
after its accepted complete fatal/interruption scan and before class-only
normalization. A separately reviewed, strictly native projection could emit
only a known literal code and one of the 15 listed literal top-level reasons.
This is a new observation contract, not authority implied by the existing
class-only contract. It would be a fresh source candidate with actual injected
controls, not permission to execute the failed target.

The precise proposed failed-only extension is optional
`:known-diagnostic {:code <known-string> :reason <known-keyword>}` inside the
existing exception variant. `:code` is required; `:reason` is optional, allowed
only with C6-VERIFY. Allowed codes are C2-HASH, C3-ID, C4-TRACE, C6-ORIGIN and
C6-VERIFY. The EDN enumerates all 15 reason literals and their pinned origins.
An unknown code, conflicting id/rule, unsupported native data or unknown reason
must never be copied. Unknown reason is omitted while a valid code may survive;
unknown/conflicting code leaves the original class-only result. Nil and ready
variants get no new fields. Failed status/reason and replay refusal stay exact.

No presently implemented exception-to-session interface guarantees that
arbitrary ExceptionInfo data can be inspected without foreign callbacks.
Any implementation must first require the exact ExceptionInfo class, then use
the existing strict native-carrier validation before map/key/value operations;
subclasses and foreign IExceptionInfo/ILookup/Map/comparator/metadata carriers
must not be called to obtain a code. Return only literal whitelist members,
never a retained value or arbitrary id/reason. Read no message, path, nested
facts, missing-fields, cause data, stack or diagnostic-id hash. A code is a
redacted category, not proof of its claimed emitter or authentication.
Full native validation can reject a carrier because of an unrelated unsupported
payload value; that must leave the old class-only result intact, not imply the
error lacked a known code. This conservative unknown bucket limits coverage.

Schema review must explicitly decide this optional-field amendment. Reusing
version 2 would preserve ready/admission ABI but change closed failed variants;
a new diagnostic sub-version would instead make that change explicit to
consumers. Neither choice is made silently in this report. ACC09 currently
requires the exact three-key exception variant, so even an implemented
extension would be dropped until Accounting reviews its closed projection.
Reader must not edit that fixture.

A code-only alternative is smaller but leaves the C6-VERIFY collisions above.
A unique source-operation tag would distinguish those remaining sites without
serializing arbitrary data, but requires an explicitly designed owner channel.
Wrapping/replacing the exception to smuggle a tag would break the accepted
original-object policy; new callbacks, dynamic authority or mutable exception
annotation are not proposed. Start with known code/reason classification,
retain an unknown bucket, and evaluate remaining ambiguity from actual future
evidence rather than instrumenting every operation speculatively.

## Evidence, required controls and limits

The source-only reader ran with eval disabled and loaded no project namespace.
It located 41 ex-info expressions, verified six construction reason call sites
and derived 15 proposed reasons from pinned literals. Initial evidence-builder
and source-check scripts had delimiter errors; those failed tooling attempts
are preserved separately from corrected successful source-data runs. They
executed no compiler or target. The corrected check verifies all 30 source
SHA256 and Git-blob pins, the unchanged compiler/contracts/tools trees, ASCII
and no loaded project namespaces. Documentation validators and diff hygiene
pass; these structural checks do not establish runtime semantics.

No diagnostic projector is implemented by this result. Required future
injected controls are therefore criteria, not claimed passes: exact known and
unknown code cases; contradictory id/rule; reason whitelist; malicious
getData/ILookup/equality/hash/comparator/metadata callbacks; malformed/native
carriers; data cycles and cause cycles; direct/wrapped fatal and interruption
including fatal-before-interruption; preexisting interrupt state; original
outer/cause identity; nil/ordinary/ready full-result preservation; failed-session
refusal and unchanged invocation, integrity, replay and catalog joins.
The accepted c28 source/injected evidence is reused only for unchanged code.
No repeated c28 controls or independent review dispatch was performed.

Only this report and its machine-readable source inventory are changed. No
source constructor, pin, authentication policy, session schema, fixture or
normalization code is edited. OWN can route one review of this source diagnosis
and contract recommendation. Any implementation and Accounting composition
remain separate concrete source work before any independently authorized
observation. Sessions 53455 and 56177 remain unknown beyond their actual
recorded fields; the original genuine goal remains open.
