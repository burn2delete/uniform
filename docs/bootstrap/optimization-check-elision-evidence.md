# Native Stage0 check-elision metadata

## Scope and governing contracts

Move the two complete bodies in foundation component 00523 into existing
`gravity.optimization`: the five-field proof-marker presence predicate and
the erased-without-marker refusal. Preserve the original Bootstrap signatures
as adapters. Keep `stage0-compiled-performance-manifest`, PERF1 claim
validation, `validate-stage0-compiled-performance!`, its iteration/order and
plan/report orchestration unchanged.

D0, D1, D2, D3, D6, D8, D9, C13, C15, SAFE15, PERF1, PERF2, PERF10,
BOOT8 and TEST2 govern the increment. Root read the owning performance and
compiler contracts and existing stage contract. Independent placement review
SHA256 is
`8b2072ac41447c1c718fa98c19e36ed409e1aad0e407b1c1982c729b33c7e9cf`.
It recommends the existing Optimization owner, separate from Safety, MIR and
the C13 compatibility facade, with no new implementation namespace or generic
proof-presence abstraction.

The frozen source parent is
`2b5bcbb089166599086e5ac0603435f8329ae328`, tree
`851d99c658c614a7eb44229b051be6656d99bacd`. Source freeze SHA256 is
`8e6329d93670476be564151b1597f8f34ebbd6d1c144d16c5039901ffcec482a`.
Its seven-path writer was released normally; this isolated writer
does not inherit CI, source admission or integration acceptance. Parent own CI
is under independent audit. Main
`21f42b7d92d163be9ede20376c32808ec3b90efd` is already an ancestor; no Main
files are imported. The nine publication SDK records match Source and Main.

## Public API and artifacts

Add only these native APIs to the existing fourteen-API stage:

```clojure
(native-stage0-check-elision-proof-present? perf-present-var record)
(validate-native-stage0-check-elision! failure-var proof-presence-var module record)
```

The first API consumes caller-owned metadata and the late presence Var. It
checks `:proof-id`, `:proof`, `:certificate`, `:certificate-id` and
`:dominating-proof` with the original short circuits and returns the original
presence result. The second consumes the late failure and legacy presence
helper Vars, module and record. It returns nil or the original owner's
result/exception. Both are native data-only, non-admitting, non-authenticating
and non-certificate-issuing boundaries.

Preserve source-path lookup, `true?` versus truthiness, sequence behavior for
erased checks, the unconditional presence-helper call even on non-erased
records, callable selection before argument reads, late Var interposition,
original field/record identity and exact exceptions. Do not infer evaluation
order from Clojure map spelling. Retain the complete old body text and change
only the named bindings/call references needed for the stage ports.

The surrounding permitted public macro/module/Stage0-plan route must emit and
execute a real instruction plan for a nonempty marker-bearing performance
record, and reject the existing real missing-marker source. Compare complete
paired `.gravity`/`.qst` observations, plans, reports, execution results,
source provenance and deterministic identities. Markers are metadata; Stage0
must keep `:optimization-mode :none`, with no actual check erasure or
performance claim inferred from them.

## Ownership, diagnostics and dependencies

Optimization owns the five-field presence algorithm, erased-without-presence
decision and `PERF10-PROOF-MISSING` facts. The supplied existing generic
failure owner retains diagnostic construction, publication, identity and
exception transport. Bootstrap retains PERF1 composition, source admission
and outer plan/report identity. Keep the C13 facade's explicit absence of
check-elision authority.

Dependencies point Bootstrap adapters to Optimization and then Clojure core.
Bootstrap explicitly requires Optimization. The first full-Main comparison
found that the adapters otherwise relied on a transitive C13 load and reported
an unresolved-namespace warning. That failed check is preserved; an additional
one-path writer claim owns the direct Bootstrap require correction.
Add no reverse Bootstrap, facade, Safety, Diagnostics or backend import.
Existing native C13 assembly/reconstruction APIs remain intact. Add the narrow
metadata support boundary while retaining `:safety-check-elision` and
`:proof-provider-validation` as unsupported.

## Tests and acceptance

Extend the existing Bootstrap-free `gravity.c13-optimization-test` leaf with
direct Optimization native controls. It is already in the fresh portable
leaf catalog. The initial scope proposed a component-inventory dependency
edit, but independent review SHA256
`a42319d65e0114830bc310dbc6b86a69ba10b676dcfe9fc459808f2f1e92e0f3`
confirmed that v1 dependency arrays reference only primary component IDs and
cannot encode a test import of a production helper. Leave
`contracts/stage0-clojure-components.json` unchanged as an explicit no-op
owned context path. The real direct require and this record declare the helper
dependency without inventing a schema or overloading an ID. No CI command or
workflow change is needed. Keep the facade's authority tests. Update the existing Optimization
pipeline API inventory for the two added APIs. Local selection of its pure
inventory must not force the existing private source-factory delays or run
the broader authenticated/compiler-carrier tests.

Genuine source controls belong in existing
`gravity.lowering.plan-assembly-test`, whose whole-namespace CI command already
exercises cross-stage emission/refusal. Cover all five marker fields, absent
or empty values, non-erased records, owner-return and exception identity,
opaque lookup timing, late helper/failure selection and both Bootstrap
adapters. Add a real nonempty accepted source distinct from the absent-suite
case; preserve the existing real `PERF10-PROOF-MISSING` refusal and adjacent
PERF1/C1 composition behavior before instruction compilation.

Run proportionate native and source regressions, the language/ownership
gates, cold core-only loading and sixteen-API metadata checks, inventory
validation, changed-file lint, document validators and full current-Main lint.
Open an early draft before the move; maintain it, freeze a clean named-branch
commit, obtain fresh independent source/adversarial acceptance and audit its
own hosted provider/run. Exact current-base integration remains separate.

## Status and unsupported boundaries

The early draft is [PR #1387](https://github.com/burn2delete/uniform/pull/1387),
opened before the move at clean scope commit
`5ba3aba947c2a3fb3860db39b0d808764d5bafdf`. Both complete bodies now live in
Optimization with their original Bootstrap adapters. The public API grows
from fourteen to sixteen; modern C13 assembly/reconstruction and PERF1
orchestration remain unchanged. Raw body text was retained with only function
names, arguments and operation references changed; map forms were not walked
or reprinted. Original/moved-body receipt SHA256 is
`b05922e2d3cd9a36bbdc93a1ee8494868d444c7ca237c70157dcf6981ee828f3`.

All twenty complete paired observations match: eight emitted plans,
compiler/runtime/performance reports and interpreted/compiled agreements,
plus twelve pre-emission refusals. Nonempty metadata exercises all five marker
fields and both erasure forms. Real PERF10, PERF1 and C1 refusals remain
ordered before instruction compilation. Baseline SHA256 is
`a4920fd8e6cd995bbab0e9cc2978d8cb1aa6012b505a6171f5ee174a8ea418b6`;
after-capture SHA256 is
`487fd5d7983c2c3700407932d51ff8197f808a5972a9434a6016348c892b19af`.
The baseline binds the actual clean scope HEAD. The after-capture binds that
same HEAD and dirty producer hashes, not a future clean commit.

After the explicit Bootstrap require correction, all twenty observations still
match the original baseline. The three-producer capture binds actual HEAD
`98f7137c448abf9029ef3d139b4825ddaf395a0e` with dirty producer hashes;
SHA256 is
`021ee9116cd4b1585a317a37ece7ac8ed37a0bc9bd91ee667f9e0fb6ce506299`.
Source/instruction regressions again pass 42/863 and language/ownership gates
again pass 48/1202. The earlier candidate remains held by its failed Main lint;
its bounded semantic review does not replace corrected-source acceptance.

Two initial harness expectations were wrong and their failed logs are
preserved. First, the emitted plan's module projection omits metadata; the
performance report on that genuine plan therefore reports
`:no-checks-elided`, even when the original module's metadata reached the gate.
The original module/artifact and source hash retain the supplied metadata's
provenance. No metadata is injected into a plan to manufacture report support.
The report's native `:certificate-backed` projection is not certificate
validation and is not reached through these ordinary plan inputs. Second, the
legacy presence operation treats an empty string as present because strings
are not Clojure collections. The final capture preserves that accepted native
case and separately rejects nil/empty-collection-only markers. These are
metadata observations, not proof, trust or actual check removal.

The Bootstrap-free C13 leaf passes 15 tests/235 assertions, including seven
new owner-level native tests. The whole plan-assembly/instruction regressions
pass 42 tests/863 assertions, including real paired sources, all marker fields,
both erasure forms, empty-string presence, real refusals, PERF1-before-PERF10
ordering and both adapter Var/object identities. Language/ownership gates
pass 48 tests/1202 assertions. Cold loading verifies all sixteen API
declarations with no aliases or Bootstrap, Safety, Diagnostics or facade
load. Native/test lint has zero errors or warnings. The existing pipeline
inventory is updated, but its broader private compiler-carrier test suite is
not run locally; native leaf and cold-load checks cover this metadata boundary.

Complete corrected-source verification, current-Main lint, final-source
acceptance, final publication and own hosted CI audit remain pending. Eight
paths will change within the nine-path owned context: Bootstrap's require,
Optimization, component 00523, the native leaf test, the pipeline
inventory test, plan-assembly source tests, this record and the namespace map.
The component inventory remains an explicit no-op; no schema edit is made.

No proof, certificate, dominance, invalidation, residual-check, policy,
backend-preservation or source-map verification is claimed. Matching labels
do not establish PERF10 conformance or SAFE15 admission. No MIR
transformation, actual check removal, performance certification, canonical
C13/C15 completion, backend execution, authenticated source/IR/evidence,
release, migration, self-hosting or seed retirement is supported by this
increment. Held Reader/authentication/private source-factory/pin routes remain
excluded. Clojure remains the temporary seed. Manual Source has no
Main/Ready/controller/service/recovery or GraphQL role; the designated
integrator retains integration authority.
