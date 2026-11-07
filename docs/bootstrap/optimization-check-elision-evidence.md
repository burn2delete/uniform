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
Its seven-path writer was released normally; this isolated eight-path writer
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
Add no reverse Bootstrap, facade, Safety, Diagnostics or backend import.
Existing native C13 assembly/reconstruction APIs remain intact. Add the narrow
metadata support boundary while retaining `:safety-check-elision` and
`:proof-provider-validation` as unsupported.

## Tests and acceptance

Extend the existing Bootstrap-free `gravity.c13-optimization-test` leaf with
direct Optimization native controls. It is already in the fresh portable
leaf catalog. Add its actual direct Optimization test dependency to
`contracts/stage0-clojure-components.json`; no CI command or workflow change
is needed. Keep the facade's authority tests. Update the existing Optimization
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

This is the initial scope record before implementation. Captures, source
changes, focused checks, source acceptance, final publication and own hosted
CI audit remain pending. The eight owned paths are Optimization, component
00523, the native leaf test, the pipeline inventory test, plan-assembly source
tests, the component inventory, this record and the namespace map.

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
