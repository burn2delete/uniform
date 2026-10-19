# Development Verification

## Purpose

Repository verification uses the Clojure runners directly.

Run commands from the repository root.

## Default SH-07 workflow: three boundaries

The practical unit of progress is an executable semantic change, not another
lane, routing record, receipt, or authority proposal. These are distinct tasks:

| Boundary | Required work | Trigger |
| --- | --- | --- |
| Semantic implementation | Edit the owning pass; add accepted/rejected fixtures; run focused tests of semantics, preserved facts and stable diagnostics. | Every semantic change. |
| Integration validation | Test the affected upstream/downstream composition and proportionate regressions. | Behavior, input/output shape, identity, diagnostics or interfaces change. |
| Authoritative migration | Fresh provenance, equivalence, bootstrap stage comparison, exact identities and independent acceptance. | Explicit admission of a stable module revision, not every edit. |

D2 requires executable increments and honest milestone ordering; TEST2 requires
pass-level accepted/rejected cases and composition regressions. D9 requires
evidence appropriate to each claim, reproducible provenance and conservative
invalidation. BOOT3 requires fresh equivalence and stage comparison at
**authoritative module migration**, not before every local edit. Compile-time
legality, profile, safety, provenance and release obligations are unchanged.

Reservations, candidate tuples, admission reviews, receipt schemas and terminal
publication come from supplemental admission contracts. Generated test catalogs
and historical attempt-specific grants are tooling/coordination policies, not
language semantics or prerequisites for explicit local test selection. The
repository language boundary, source LOC limit and host resource limits remain
implementation constraints, separate from proof authority.

Keep paired source fixtures first-class. For extension-independent behavior
with unchanged reader, canonical identity and source-path projection, one real
source composition plus explicit paired byte/parser checks can be proportionate.
Record which spelling actually ran; parser parity does not establish fresh
paired typed/effect/lowering/runtime equivalence. Renew the affected paired
composition when extension handling, reader transport, canonical identity or
path projection changes, or a concrete cross-extension regression requires it.
A new primitive alone does not require rebuilding every product twice.

For SH-07, change `bootstrap/gravity/src/gravity/checked_core.gravity` (the
owning Gravity pass), not only the hosted C6 compatibility facade. The thin
adapter lives in
`bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00885-sh07-core-source-relative-path.clj`.
Tests are in `bootstrap/clojure/test/gravity/self_hosting/sh07_*_test.clj`;
paired `.gravity`/`.qst` fixtures are under
`bootstrap/clojure/fixtures/self-hosting/`.

Follow the owning boundary when the next change is downstream of SH-07:

| Behavior being changed | Implementation owner | Focused workflow below |
| --- | --- | --- |
| Canonical source/Core shape and legality | `bootstrap/gravity/src/gravity/checked_core.gravity` | Default SH-07 commands here. |
| Namespace/reference binding and core availability | `bootstrap/gravity/src/gravity/resolution.gravity` | SH06 adapter namespace with exact owning resolution selectors; affected source composition when selected bindings or diagnostics change. |
| Reader literal kind in syntax transport | `bootstrap/clojure/src/gravity/c3_literal_projection.clj` and the `00853-sh04-syntax-reader-binding.clj` consumer | Character occurrence transport checks below. |
| Retained source occurrences to the temporary C6 request | `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00887-sh07-core-build-form-tree.clj` and its `00890` caller | `gravity.self-hosting.sh07-mutation-test`, exact `sh07-map-source-order-development-kernel`; affected real source composition before accepting a projection change. |
| Function and expression types | `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` | Loop/recur and the matching type-development section. |
| Body/latent effects and source allowances | `bootstrap/gravity/src/gravity/effects.gravity` | Constructor allocation effects and the matching body-effect kernels. |
| Typed Core to executable development IR | `bootstrap/gravity/p15_s23/emitter.gravity` | Explicit typed input, then the matching lowering section. |
| Development IR evaluation and control | `bootstrap/gravity/src/gravity/runtime.gravity` | Runtime namespace boundary, then the matching execution section. |

Change the owning Gravity implementation, not only a compatibility adapter or
fixture expectation. Select the relevant exact tests; this table is not a
requirement to run every upstream/downstream namespace for each edit.
The temporary request projection is an exception only for transport defects:
it must preserve source occurrences, bindings and evaluation order for the
owning Gravity pass. Fixing that bridge does not move language semantics or
implementation authority into it.

Check `git status --short`, preserve unrelated work, then use existing commands:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-var-reference-test --list
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-var-reference-test --exact sh07-b5-qualified-var-development-kernel
# Affected SH-06 -> SH-07 artifact composition (costlier, not the inner loop):
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh07-var-reference-test --exact sh07-b5-qualified-current-namespace-preserves-resolution-and-mapping --exact sh07-b5-qualified-var-forged-order-spelling-and-id-fail-authentication
git diff --check
```

These commands target the current qualified-var slice: the direct kernel test
executes the owning Gravity lowerer and validators without constructing the
whole upstream proof product. Its small test inputs are not authoritative
SH-06 evidence. The separate artifact tests exercise authenticated composition.
Use the whole B5 namespace only when broader regression coverage is warranted.
Substitute the owning namespace and tests for a different slice. Prefer exact
semantic vars when a namespace also contains costly authenticated construction or source
coverage. `--list` does not execute tests, but namespace loading can cost time.
Explicit `:dev-test` selections use test resources on the classpath without a
manifest authority entry; unknown namespaces and unmatched selectors fail.
Use a fresh JVM after compiler source changes to avoid stale process-local plans.

Ordinary `compiler-c2-reader-file-artifact` construction executes the existing
bounded, authenticated reader directly. It does not require persistent cache
publication or a free cache-store budget. Call the existing explicit
`compiler-c2-reader-file-artifact-cached` API when testing incremental reuse;
its current-input revalidation, secure admission and capacity limits still
apply. A cache policy rejection is not permission to clear shared state,
increase limits or suppress unrelated compiler failures.

When debugging a shared parser or plan-loading error, the existing
`--fail-fast` option stops after the first failed test var, avoiding the same
load-error stack from every selected var. It does not stop within a test var,
and skipped tests are not passes. After fixing the error, run the affected
semantic kernels and proportionate regressions normally.

Selectors apply across all selected namespaces. A global regex can therefore
select more than the exact vars added to a mixed batch; use exact selections
when combining different-cost checks. In the SH08 function-test namespace,
`*-c8-composition` tests construct the public C8 module bundle even though their
inputs are normalized DATA. They are integration checks, not fast
`*-development-kernel` tests. Their bodies and coverage remain available;
renaming them does not require replaying an unchanged expensive proof.

### Recognition of an already declared seed primitive

When ordinary C5 reports an unresolved primitive that the selected seed
already declares and implements, check the source request's declaration
catalog in
`bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00870-sh06-resolution-special-symbols.clj`.
Repair a demonstrated declaration omission there; binding selection remains
owned by the Gravity resolver described below. Do not use ambient host lookup,
bulk-whitelist names or interpret recognition as typed/runtime/target support.

For the `list?` catalog repair, use the direct focused source check:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh06-list-predicate-binding-test --exact sh06-list-predicate-binding-authenticated-source --fail-fast
```

It constructs three tiny source attempts and invokes the original fresh C5
verifier: core and qualified binding/provenance, lexical shadowing and inert
quoted data, with unknown-lookalike and unimported host-namespace rejection.
It does not construct the whole runtime module or prove a typed List predicate.
Catalog records have sorted ordinals, so an insertion can also change later
core binding identities and affected C5 requests. Reassess affected evidence;
unchanged target bytes alone do not establish validity. Do not renew a corpus
or migration stamp merely to validate this development correction.

### Qualified core resolution and availability

Edit `bootstrap/gravity/src/gravity/resolution.gravity` for binding selection
and its C5 diagnostics, not the core catalog or a host namespace resolver.
Use the existing direct owning checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --exact sh06-qualified-core-var-development-kernel \
  --exact sh06-atomic-signature-type-namespace-development-kernel \
  --exact sh06-ordinary-module-policy-development-kernel
```

These execute the current owning Gravity resolver with normalized requests.
They check route ownership, selected identity, privacy/ambiguity precedence,
core profile/target eligibility and diagnostic facts. They are not fresh
source composition or authoritative migration. Preserve existing failure
facts unless the new availability check itself rejects; a private or duplicate
candidate must not disappear through eligibility filtering.

For the emitter's declaration/source-spelling join, use the existing
`gravity.self-hosting.sh01-stage2-runtime-iteration-test` namespace with exact
selector `current-gravity-qualified-type-declaration-development-kernel`.
It checks ordinary and qualified List/String declarations and rejects forged
spellings, bindings and source identities without rebuilding source products.

When resolution or that lowering join changes, run the affected composition
separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-qualified-core-authenticated-composition
```

This checks paired original sources through C5/C6/C7, public lowering, C8 and
execution, with missing-member rejection at C5. It is not an inner-loop
prerequisite, a reason to replay unchanged proof products, or authoritative
module migration. Preserve failed attempts and distinguish their completed
checks from assertions that never ran.

### Map source-order projection

The temporary SH-07 request builder must use retained C2 entry occurrences,
not host-map iteration, for evaluated map children. Its local correction keeps
existing form IDs, bindings, reference paths and decimal coordinates; only the
map parent's ordered child links change. Quoted map DATA retains its existing
materialized value and is not subjected to evaluated-map restrictions.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-mutation-test \
  --exact sh07-map-source-order-development-kernel \
  --exact sh07-map-quoted-duplicate-source-development-kernel
```

These normalized checks cover correspondence and preservation, not actual
source composition or map execution. The affected composition must separately
check original textual order through C2, the request and Core IR, and reject a
reordered request against unchanged ancestry. No new migration proof or
host-projection accounting protocol is implied by this correction.

Run that affected composition separately, not in the edit loop:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-mutation-test \
  --exact sh07-map-source-order-authenticated-composition
```

The paired fixture checks opposite nine-key source orders, quoted duplicate-key
DATA compatibility and a coherently rehashed request-order rejection. Its
accepted result establishes Core IR order, not evaluated-map runtime support.

### Character occurrence transport checks

L1/C2 own Character decoding, including supplementary Unicode scalars. The
temporary C3/SH04 transport must retain the original authenticated occurrence's
kind and decoded value. A supplementary Character's existing four-field
representation is not a license to classify an ordinary source map as Character.
Keep the global host-value classifier unchanged.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-mutation-test \
  --exact sh07-character-c3-occurrence-development-kernel \
  --exact sh07-character-source-occurrence-development-kernel \
  --exact sh07-map-source-order-development-kernel \
  --exact sh07-map-quoted-duplicate-source-development-kernel
```

These normalized kernels check occurrence/value joins, scalar representation,
ordinary-map distinction and malformed or retargeted records. C6 derives kind
before recursion from the original source occurrence, including quoted and
nested values; the map regressions preserve source order and quoted duplicate
materialization. The supplied integrity report is conditional test input, not
proof that C2 authenticated a
source. Actual source composition must separately preserve the same distinction
through C6 and the affected consumers, including quoted and nested occurrences.
Do not clone the Unicode parser, add a caller authority flag, or refresh
migration fingerprints to run this transport check.

### Ordinary module dependencies

L3/C5 module resolution consumes real callee artifacts. Namespace names,
`:refer` entries and `:compiler`/`:sh06` metadata do not establish that a
definition exists or is exported. Change the owning policy in
`bootstrap/gravity/src/gravity/resolution.gravity`; the existing SH-06 adapter
transports and verifies artifacts rather than selecting imports itself.

Use the ordinary constructor's module-input argument, not a new wrapper or
authority flag:

```clojure
(require '[gravity.bootstrap :as bootstrap])

(bootstrap/sh06-resolution-source-artifact caller-path caller-text
  {:artifact :gravity/sh06-module-inputs
   :schema-version 1
   :module-artifacts [callee-artifact]})
```

Build `callee-artifact` with the same ordinary constructor or file constructor.
The two-argument source constructor means an empty module-input set; every
external Gravity `:requires`, including an unused requirement, then needs an
explicit callee artifact. Missing inputs fail with `C5-UNRESOLVED`. There is no
ambient host-namespace lookup or export-all fallback. The existing strict
SOURCE-context interface is separate and is not a development shortcut.

Start with the existing direct policy checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --exact sh06-ordinary-module-policy-development-kernel \
  --exact sh06-ordinary-seed-build-does-not-call-public-frontdoors
```

Run the affected real module composition when import behavior or its artifact
interface changes. Local artifact verification checks the supplied semantic
inputs; it does not admit a module revision, refresh historical migration
proofs, or establish imported-call typing, execution or self-hosting support.

### Loop/recur type development

The existing owning pass is
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.
Its internal `sh08-ft-loop-type-step` consumes canonical C6 nodes, bindings,
targets and transfers plus current type/binding tables and the remaining
record-visit budget. Its DATA result contains bindings, constraints, issues and
work counts. `sh08-ft-normal-return-type` joins ordered child types with a
nominal type. C7 retains input authentication, inference orchestration and
diagnostic rendering; calling an internal kernel does not grant acceptance.
The optional `gravity.types` extraction is deferred, not a prerequisite for
this semantic increment. There is one implementation, in the actual C7 source.

Run those current-source kernels directly for fast feedback:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact normal-return-bottom-development-kernel \
  --exact loop-working-core-retains-authenticated-bindings-development-kernel \
  --exact loop-slot-transfer-development-kernel \
  --exact zero-slot-nested-nearest-development-kernel \
  --exact function-target-reset-is-not-forgery-development-kernel \
  --exact loop-effect-backedge-development-kernel
```

These tests use the existing single-source compiler interface on the exact C7
source, not a stripped source or a raw root projected from a module bundle.
The kernels use normalized records, not authoritative source evidence.
The production adapter joins the separately authenticated bindings into a
private loop-analysis view; it does not add fields to published canonical core.
`Never` means no normal return; unknown evidence is not `Never`. Loop slots
derive from sequential initializers, and recur arguments must match their
individual target slots without coercion. A never-returning argument does not
produce a mismatched transfer value. Ordinary `if` retains L2 truthiness, and
function construction does not execute its body. Function-target recur uses
the separately declared parameter slots described below; unresolved slots are
an annotation requirement, not malformed C6 by themselves.

`gravity.effects` owns the corresponding C8 body-effect equations. A recur
backedge returns to the target's body, not its initializers. Its focused check
joins the retained C7 slot/transfer premises to C6 control records, starts effect
summaries at bottom and checks the final equations within the existing logical
work budget. This is development feedback, not a termination or runtime-cost
proof.

After changing C7 inference or its namespace boundary, validate the affected
real C5/C6/C7/C8 composition and execute the same source bytes through the
existing seed interface:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact loop-recur-authenticated-paired-composition
```

The separate `loop-c6-controls-precede-types-current-source` selector exercises
the original source-level C6 rejection boundary. It is deliberately not named
as a development kernel: it constructs upstream products and is slower than
the normalized leaf checks. Run it when that composition boundary is affected,
not on every leaf edit. Leaf kernels alone do not establish real composition,
program termination, effect legality, migration or self-hosting completion.

### Function-target recur development

Use the same C7, `gravity.effects`, and loop-test owners. C7 takes each function
recur slot from the original parameter declaration, not from a caller's inferred
argument types. It checks original target, binding order, nearest-function reset
and transfer shape before deciding whether a type annotation is missing.
Supported recur transfers have type `Never`; the enclosing normal-return join
still determines the function result. C8 joins the original function signature,
parameter facts and body identity, then solves body backedges with the existing
bounded fixed-point equations. A function-only recur must not enter the ordinary
acyclic solver merely because the module has no `loop` node.

Use these exact selectors for local feedback:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact function-recur-node-inference-development-kernel \
  --exact function-recur-slots-development-kernel \
  --exact function-recur-backedge-development-kernel \
  --exact function-target-reset-is-not-forgery-development-kernel
```

The backedge check calls the same private solver-dispatch helper used by module
analysis. It covers function-only recur, malformed control tables, and parity
with ordinary acyclic, loop and named-self-call paths. This is not a substitute
for the complete module joins. When that composition changes, run
`function-recur-authenticated-paired-composition` in the same namespace. It
checks actual accepted/rejected annotated `.gravity` and `.qst` source products,
fresh C7/C8 verifiers and tampered artifacts. Its result must be recorded, not
inferred from the kernel checks.

The compiler composition does not itself establish runtime execution. The
separate exact-source seed check below supplies that evidence for the bounded
fixture. General recursion, termination, target legality and authoritative
migration remain separate claims.

### Annotated single-source seed execution

The existing three-argument compiler-artifact plan interface lowers fixed-arity
atomic parameter and return annotations without changing the source text.
Definition records retain `:source-declarations` alongside logical parameter
names and executable body forms. Raw `def`/`fn` return prefixes and the existing
macro-produced `typed/return` header are retained in their actual forms; the
latter is not relabeled as literal unexpanded source. Full source hashes and
function records remain part of plan and semantic identity.

The owning seed code is `foundation/00546` (header normalization and validation),
`bootstrap-p15/00580` (existing source records and raw plan construction), and
`bootstrap-p15/00584` (direct-emitter validation and declaration comparison).
The shared raw definition parser remains unchanged. Original headers are
validated before body compilation, including invalid nil/false return tokens
that legacy macro expansion can erase. Plain records and plans retain their
previous shape; shared function-table callers are not assumed to be isolated.

Run the existing focused header and compatibility checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact seed-function-declaration-headers-development-kernel \
  --exact seed-function-declaration-rejections-development-kernel \
  --exact seed-function-declaration-plan-parity-development-kernel \
  --exact seed-function-declaration-macro-route-development-kernel \
  --exact seed-shared-function-table-plain-parity-development-kernel \
  --exact seed-annotated-function-loop-isolation-development-kernel
```

For execution of the exact annotated function-recur fixture:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact function-recur-annotated-seed-execution
```

This seed lowering validates declaration grammar, not type legality. C5/C6/C7/C8
remain the semantic owners; a symbolic type token is not proof that its type
exists or that its use is legal. Composite annotations, variadics, duplicate
logical names, missing/repeated markers and empty bodies reject in this subset.
The four-argument module constructor retains its unannotated-definition policy;
the direct compiled-emitter path retains its hosted-profile requirement. No
annotation stripping, unchecked imported execution or new authority follows.

### Direct typed-throw development

The bounded source path checks a direct `throw` payload after local type
inference. A known payload type is retained as a thrown-error fact; the transfer
expression has type `Never`, so it does not become a normal branch result.
Unknown payloads require an annotation. The owning files are
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` and
`bootstrap/gravity/src/gravity/effects.gravity`; paired examples live under
`bootstrap/clojure/fixtures/self-hosting/sh-08/` as `direct-typed-throw` and the
three `direct-throw-*` rejected cases.

Use the existing direct kernels for edit feedback:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact direct-typed-throw-source-and-type-development-kernel \
  --exact direct-typed-throw-effect-replay-development-kernel \
  --exact direct-typed-throw-nested-and-allowance-development-kernel
```

For an affected source/type/effect interface change, the existing
`direct-typed-throw-authenticated-paired-composition` selector constructs fresh
C5/C6/C7 products and checks the current C8 effect leaf over both extensions.
It checks payload/result typing, source effect allowances and tampered products;
it does not reconstruct the public C8 compiler bundle. Do not rerun this
unchanged composition merely for a later seed-execution observation or commit.

C8 independently joins the original payload-before-transfer record and checks
inferred effects against original source allowances. Missing `:error/throw`
declarations report `C8-UNDECLARED`; this is not the full package/provider/grant
or profile-lowering legality intersection. The normalized legacy C8 registry's
`:error/raise` label is not silently renamed or aliased.

Nested function ownership is separate, quoted DATA is inert, and an operand
that never returns preserves the outer source record without inventing a
`Never`-typed error. Unrelated calls keep their existing behavior. Direct local
call propagation has its own bounded increment below. Imported concrete throws
remain fenced by C8's existing foreign-summary check, not a new C7 import
guarantee. `try`,
handlers, cleanup, host-exception normalization, general runtime execution,
error legality and authoritative migration remain separate work.

### Direct-local thrown-error propagation

Use the same C7, `gravity.effects`, fixture directory and loop-test namespace.
The bounded acyclic component analysis joins original local call edges,
bindings, arguments and callee bodies. Normal return types and possible thrown
types are separate: a String-returning function can also throw String. A pure
leaf gets an empty thrown-type vector only after its supported body is checked;
pending or unknown facts are not treated as empty.

Start with these exact development checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact local-throw-call-closure-development-kernel \
  --exact local-throw-call-effect-equations-development-kernel \
  --exact local-throw-call-existing-type-guards-development-kernel \
  --exact local-throw-final-signature-status-development-kernel \
  --exact local-throw-source-body-index-parity-development-kernel
```

When the call/type/effect composition changes, use
`local-throw-propagation-authenticated-paired-composition` in that namespace.
It checks fresh C5/C6/C7 products, independent C8 body equations, altered
summaries and real rejection fixtures. It also executes the same accepted
source through the existing seed interface, without a second compiler proof.

Error vectors conservatively include source operand error potential in order;
this is not path-sensitive reachability analysis. A `Never` operator or argument
prevents normal invocation, so the callee body's errors are excluded while the
original static call edge and source records remain present. Disconnected
direct-throw owners retain their prior analysis. Throwing recursion, recur in
the affected component, imported or indirect throwing calls, handlers and
cleanup remain unsupported here. These checks do not admit a module revision
or establish Gravity-emitter/runtime ownership, complete legality or self-hosting.

### Atomic typed catch development

The owning compiler files are the existing C7 engine and `gravity.effects`.
Canonical C6 handler records supply the source/type/binder/scope/clause-order
boundary; C7 consumes authenticated resolved core rather than reconstructing C5
resolution through a new API. C8 independently checks its full request and the
typed handler premises. Keep these joins separate from runtime catch execution.

Start with the direct kernels in the existing loop/type test namespace:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact atomic-typed-handler-partition-development-kernel \
  --exact atomic-typed-handler-source-development-kernel \
  --exact atomic-typed-handler-products-development-kernel \
  --exact atomic-typed-handler-body-development-kernel \
  --exact atomic-typed-handler-downstream-boundary-development-kernel \
  --exact atomic-typed-handler-diagnostic-provenance-development-kernel
```

`atomic-typed-handler-source-scope-integration` calls the ordinary public C5
constructor on real rejected fixtures. It is not an inner-loop kernel. Its
initial completed run spent about 169 seconds in that test, compared with about
one second across the five direct handler/downstream checks and seven seconds
of namespace loading. This observation identifies upstream product construction
separately from scheduling overhead; it is not a general C5 benchmark or a
speedup claim. The selector was renamed after that run without replaying its
unchanged body.

Handler candidate-transfer lists are source-potential censuses, not escaping
error summaries. Nested residuals must be derived bottom-up. Global handler
ordinals follow construction order; per-try clause ordinals follow source order.
A handler's own throw escapes that try rather than entering a sibling handler.

Covered and residual source error types do not by themselves establish legal
effect discharge. Preserve performed `:error/throw`, source allowances and
pending profile/provider permission. Actual C7/effects composition is required
when these typed facts or interfaces change; it is not a reason to replay old
unchanged public bundles or refresh admission pins.

For a changed handler boundary, run its affected composition explicitly:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact atomic-typed-catch-authenticated-paired-composition --fail-fast
```

The initial completed composition passed one test/220 assertions in about
829 seconds of test execution, plus about eight seconds of startup/loading.
It constructs fresh accepted `.gravity` and `.qst` products, checks coherent
typed/effect mutations and semantic rejection cases. This cost belongs to the
affected product construction and checking, not a requirement before every
direct-kernel edit. A rejected-diagnostic source-location correction or removal
of optional test output does not require replaying the unchanged positive proof.

### Explicit typed input to current Gravity lowering

The existing `p15-s23-stage2-compile-function` and
`p15-s23-stage2-assemble-plan-products` interfaces have an ordinary four-argument
form: emitter, module, definition(s), and `{:request request :typed typed
:verification report}`. These are actual pass products, not an accepted flag.
The temporary Clojure orchestration component invokes current Gravity C7
verification once per operation. The Gravity emitter requires an accepted typed
result, the exact fresh expected/candidate result and matching supplied report,
then joins the original source snapshot, module, declarations, function/body
identities, parameter bindings and types.

The original source comes from the authenticated request's retained C2 tokens;
ordinary lowering does not require the source file to remain on disk. The
current C7 and emitter implementation sources are still read and compiled.
Raw internal kernel arguments are DATA, not cryptographic authority tokens.
Current-source products expose their actual compiler/source provenance and
pending effect legality. They do not enter the old pinned `00582` envelope.
Existing three-argument calls retain their old behavior and bindings.

Edit `bootstrap/gravity/p15_s23/emitter.gravity` for the typed canonical-core
lowerer. Its initial supported body subset is literals with an implemented
runtime representation, parameter references, `throw`, `if` and `do` in fixed
named top-level single-body functions. Unelaborated numeric descriptors and unsupported operations
reject; raw catch spelling is not type authority. The small `00581` Clojure
component only orchestrates existing compiler interfaces. It does not implement
type inference or a second source grammar.

Use the existing runtime-iteration test namespace:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-lowering-input-development-kernel \
  --exact typed-lowering-public-orchestration-development-kernel
# Affected source -> current C7 -> lowering -> Gravity evaluator composition:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-lowering-authenticated-composition --fail-fast
```

The first command uses normalized DATA to check joins, diagnostics and unchanged
legacy orchestration; it is not authenticated source evidence. The second is
the affected integration boundary. Build its actual source products once per
extension and reuse those immutable inputs across assertions. Neither command
admits a module revision or establishes profile/target legality, runtime catch
support or self-hosting.

The accepted focused batch passed four tests/283 assertions, including existing
emitter/runtime throw regressions. The test-only namespace/body correction then
passed the two typed-input selectors/74 assertions. The sole affected paired
composition passed one test/65 assertions in about 175 seconds of test execution
plus seven seconds of loading. These overlapping results are not additive and
do not compare equivalent workloads. SH05's `:expanded-forms` contains the body,
not the namespace declaration: parse the retained C2 original forms for the
module header, then associate the authenticated expanded body for function
analysis. Do not reconstruct upstream products merely to fix this test setup.

### Canonical atomic catch lowering and execution

The next increment uses the same emitter, runtime and test namespace. Catch
types come from canonical C7 bindings and solved handler premises, not source
spelling or host exception classes. The current subset is Bool, String, Symbol
and Keyword. Quoted DATA uses the solved C7 quote record; see the recursive
quoted-DATA boundary below. General calls are not catch-type evidence.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-catch-lowering-development-kernel \
  --exact current-gravity-typed-catch-runtime-development-kernel
# Run when the catch lowering/runtime composition changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-catch-authenticated-composition --fail-fast
```

The direct checks distinguish normal, thrown and rejected control, preserve
payload identity and evaluate the protected expression once. The first matching
handler runs; its own throw bypasses sibling handlers and may reach an outer
try. A malformed handler descriptor rejects before protected execution. Body
map validation is shallow, not a new whole-IR validity proof. Fatal host
failures are not guest error values.

The affected composition uses both existing atomic-catch fixture extensions
and the small local-runtime pair. It constructs each source product once and
checks fresh current C7 verification per public lowering operation. Runtime
execution at that checkpoint used an exact nine-function runtime projection
with original-source and dependency checks. The current selector now compiles
the whole actual `gravity.runtime` leaf; the historical 122-assertion result
below remains attributed to its original test revision, not a rerun of the
changed loader. Zero seed throw transport must be observed around every actual
execution, not inferred from an uninstrumented call. This boundary does not
admit a revision, refresh old pins, discharge effect/profile legality or prove
Error subtyping, resource cleanup, self-hosting or seed retirement.

The final direct batch passed two tests/128 assertions. The sole affected real
composition passed one test/122 assertions in about 491 seconds of execution
plus seven seconds of namespace loading. Its four upstream inputs were each
constructed once. These checks overlap; their counts are not additive and
their timings do not compare equivalent workloads. The instrumentation-only
correction preceded this run, rather than triggering a second proof build.

### Canonical symbol-let lowering

Use the same emitter and runtime-iteration test namespace for immutable symbol
bindings. The lowerer consumes the original C6 lexical records and C7 local
binding facts, including initializer identity, ordinal, scope, prior-visible
binding IDs and inferred type. Initializers see outer and preceding bindings,
not the binding currently being initialized or later bindings. Nested scopes
do not mutate the outer lowering context.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-let-lowering-development-kernel \
  --exact current-let-container-scope-projection-development-kernel
# Affected source -> typed lowering -> runtime composition:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-let-authenticated-composition --fail-fast
```

Repeated names in one source binding vector create successive lexical scopes
in the existing C5/C6 implementation. Preserve that behavior: nested one-binding
runtime lets avoid changing the flat instruction validator's duplicate-label
restriction. Runtime labels are not binding authority. An empty binding vector
still requires a body. A `Never` initializer prevents later initializers and
body execution; do not infer the let result from the last body type alone.
The binding-vector container itself has no assigned scope in the current
projection. Do not invent an equality between that nil value and the owner's
real scope. The projection helper check exercises this schema without building
an authenticated source product.

Run the affected real-source composition when this consumer changes, reusing
each constructed input across lowering and runtime assertions. Ordinary
self/forward-reference errors belong to C5. The unchanged C6 restrictions on
malformed bindings, missing bodies and unsupported destructuring are not a
reason to replay their historical expensive proof for each lowerer edit.
Other call forms, mutable locals, destructuring, numeric coercion, full legality
and authoritative migration remain separate work.

After correcting the unsupported container-scope premise, the focused batch
passed eight tests/493 assertions. The changed candidate's real composition
passed one test/132 assertions in about 366 seconds of execution plus seven
seconds of loading. It constructs each accepted input once per extension and
reuses it for nine-function assembly, lowering, runtime and mutation checks;
four C5 self/forward-reference rejections exercise the real owning boundary.
The first real composition remains failed (nine passed assertions, one error),
not relabeled by the later pass. Counts overlap and are not additive; timings
measure different workloads, not a speedup.

### Canonical recursive quoted-DATA lowering

Change `bootstrap/gravity/p15_s23/emitter.gravity` for this consumer. The
existing public typed operation first runs the current C7 verifier on the same
request and candidate, then joins that result to the lowering context. C7 owns
recursive quote shape, value, type, child order, source identity and work-bound
validation. Lowering selects the unique solved quote producer and checks its
scalar root, operator, payload, source and origin coordinates. It emits the
producer's exact value as existing `:quote` IR, without evaluating its contents.
It does not repeat the recursive checker or reinterpret quoted syntax.

Supported DATA is nil, Bool, represented exact integer, character, String,
Keyword, Symbol, list, vector, map and set, including empty and nested mixed
containers. Qualified symbols and symbol map keys remain inert; quoted
`(throw unresolved/data)` and `(quote nested)` are lists, not executed forms.
Immutable let forwarding and direct same-module returns preserve the value.
Numeric elaboration, unquote, evaluated set constructors, general match
execution, complete effect/profile legality and migration remain separate.
Evaluated vectors and maps have their own development boundaries below.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-recursive-quote-lowering-development-kernel \
  --exact current-gravity-typed-lowering-input-development-kernel \
  --exact typed-lowering-public-orchestration-development-kernel
# Run separately when this source -> C7 -> lowering -> runtime boundary changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-recursive-quote-authenticated-composition
```

The direct quote kernel consumes normalized records with an explicit verified
context precondition; it is not another public authentication entrypoint. Its
four-unit logical projection charge does not count all VM work. The context
already reserves ledger lookup work, and the projection adds no recursive
comparison, hashing, serialization or DATA reconstruction. Opaque sentinel
controls test that boundary without claiming those sentinels are accepted
source DATA.

The affected composition uses the complete `local-runtime-quoted-data`
`.gravity`/`.qst` pair, not a selected subset of a module containing unsupported
bodies. Each original C5/C6/C7 product is constructed once and reused for a
representative public function compile, whole-module assembly and execution
through the actual runtime leaf. Deep payload, type and source forgeries are
rejected by fresh public C7 verification. Existing owning C5/C6/C7 malformed
quote, unresolved-name, annotation and numeric-representation rejection
evidence is not rebuilt merely because this lowerer changed.

### Canonical evaluated vectors

Edit the same Gravity emitter and `bootstrap/gravity/src/gravity/runtime.gravity`.
C6 already supplies ordered constructor children and source coordinates; C7
supplies positional element types and Never propagation. Lowering compiles
those verified children into existing `:vector-literal` IR. It does not return
the source form's `:source-value` as an evaluated result. Empty and nested
vectors, mixed represented values, typed parameters, quoted DATA elements,
immutable let forwarding and direct local calls use this path.

The runtime checks the closed `{:op :vector-literal :items [...]}` descriptor,
including a vector of at most 65,536 immediate instruction maps, before item
execution. This is shallow descriptor validation, not a whole nested-IR proof.
Items run once from left to right with operand recur targets cleared. Normal
values accumulate into a vector; the first thrown or rejected result keeps its
payload/output and skips later items. Control-looking quoted DATA remains a
normal element. Fatal host failures remain fatal, not guest throw values.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-evaluated-vector-lowering-development-kernel \
  --exact current-gravity-evaluated-vector-runtime-development-kernel \
  --exact current-gravity-evaluated-vector-c8-development-kernel
# Affected source/type/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-evaluated-vector-authenticated-composition
```

The lowerer reserves `12 + 10 * element-count` logical join units before its
per-element checks, then passes the remaining budget through child lowering.
The existing cumulative cap is unchanged. This is not a bound on every VM
operation, total heap use or program execution. Missing/unknown types and
incoherent Never, source, order or count facts reject with `P15S23Q002`.
Malformed runtime descriptors reject with `P15S23X002`. Evaluated sets retain
`:unsupported-typed-lowering-operation`; maps have the separate bounded
development subset below. Unsupported language forms are not described as
malformed vectors.

This selects compiler-internal `:meta/:jvm` value construction. P3 permits
internal compiler allocation, and L10 makes its visibility regime-dependent;
neither empty declarations nor C7 acceptance grants arbitrary allocator or
runtime authority. Independent current C8 body checks retain element effects,
ordered coverage and declaration rejection. The existing internal
`body-analyze-module` consumes the original request and `typed.identity-input`
facts, not the outer typed artifact. These checks do not replace the missing
full C8-to-lowering/MIR legality boundary. The existing public typed input,
fresh C7 verification, source provenance and explicit pending effect/profile
legality status are unchanged. No MIR, deployable-program, hosted allocator,
target, authoritative migration or self-hosting acceptance follows.

### Canonical evaluated maps

Use the same emitter, `gravity.runtime`, and runtime-iteration test namespace.
The collection lowerer consumes G's ordered C6 children and C7's flat ordered
member types, sharing the vector source/type joins and child compiler. The
initial map subset requires distinct literal Keyword keys; computed,
non-Keyword and duplicate evaluated keys remain unsupported, not newly illegal
Gravity. Quoted maps remain inert DATA, including existing duplicate-key
materialization. Neither lowerer reads a source map as the evaluated result.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-evaluated-map-lowering-development-kernel \
  --exact current-gravity-evaluated-map-runtime-development-kernel
# Affected source/type/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-evaluated-map-authenticated-composition
```

The existing `:map-literal` IR contains an ordered vector of closed key/value
instruction pairs. Runtime shape checks precede evaluation. Keys and values
then run once in entry order, preserving the first transfer, payload and output
and skipping later entries. Lowering reserves `12 + 14 * child-count` logical
join units, including both children of each pair, before child compilation;
this is not a total execution or heap bound. Keep vector/conservation checks
when changing their shared helper, and run affected composition separately.
That composition reuses each paired source product for public lowering,
assembly, execution and independent C8 body checks with `typed.identity-input`
facts. It also checks source-order and typed-input forgeries and the new
String-versus-map result rejection, rather than replaying old whole proofs.

The source-order guarantee belongs to canonical four-argument typed lowering.
The runtime executes ordered instruction DATA; it does not certify how legacy
untyped host-map compilation selected that order. That legacy route's key
sorting is not repaired by adding map execution. Full effect/profile/provider
legality, general collection operations, target support and authoritative
migration remain separate. Real-source validation results belong in the
backlog, not inferred from these normalized checks.

### Literal Keyword map lookup

For `(:key map-expression)`, edit the existing C7 engine for inferred member
types, the typed emitter for solved-row projection, `gravity.effects` for
operand effects, and `gravity.runtime` for instruction execution. SH-07's
existing canonical lookup record owns the one-operand shape and evaluation
order; do not add a second dispatch interface.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-map-lookup-lowering-development-kernel \
  --exact current-gravity-keyword-map-lookup-runtime-development-kernel \
  --exact current-gravity-keyword-map-lookup-c8-development-kernel
# Affected paired-source composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-map-lookup-authenticated-composition --fail-fast
```

The initial development subset derives map shape from constructors, verified
quoted DATA and immutable let ancestry, including nested lookup extraction.
A present member retains its type; established absence yields Nil. Missing
shape evidence is an implementation boundary, not implicit Dynamic or a new
language prohibition. A Never operand remains non-returning. General `get`,
defaults, arbitrary key types and call-returned shape propagation are separate
increments.

The producer records source, ordered operands, reconstructed shape, selected
coordinate and presence in a solved constraint. Fresh public C7 verification
owns recursive reconstruction; lowering joins that verified row rather than
adding another recursive checker. Lookup outer joins and extracted-evidence
logical charges share the existing inference counter with loops across rounds
and failures. Those charges are not recursive helper-visit or CPU counts;
existing shape depth, size and alternative caps remain separate.

Runtime validates the closed two-argument instruction, evaluates the literal
key and whole map operand once in order, then performs checked lookup. A
missing key does not skip operand evaluation. Throws, rejection, payload
identity and accumulated output propagate unchanged. Effect analysis must
retain the operand's effects and escaping thrown types. Run the affected
paired-source composition separately once its candidate is stable; the
normalized kernels do not prove source composition, migration or self-hosting.

The older `sh07-b15-rejected-arities-use-the-normative-shape-diagnostic`
selector reconstructs six complete source frontdoors. It is a costlier
upstream regression, not an arity kernel or a prerequisite for every downstream
lookup edit. Select it when changing canonical lookup arity/shape behavior;
do not add it automatically to the focused lookup command above.

### Constructor shapes returned by local calls

Shape propagation belongs to the existing C7 engine, not runtime dispatch or a
caller-provided shape summary. Follow the actual same-module acyclic callee's
body using its established parameter types and authenticated call, edge,
binding, definition and function records. Retain that ancestry in the existing
lookup or structural-match constraint. Equal return-type vectors do not prove
equal map keys. Do not specialize a callee from caller argument values.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-returned-map-shape-development-kernel \
  --exact current-gravity-keyword-map-lookup-lowering-development-kernel
```

Use these normalized checks while editing the shape path. Affected composition
must separately build a real map-returning function, forward its result and
look up the member through current typed lowering and runtime execution. Reuse
each source product within that test; do not rerun the unchanged lookup matrix
as a prerequisite. Structural-match compiler acceptance does not establish
runtime match lowering.

```bash
# Affected source/type/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-returned-map-shape-authenticated-composition --fail-fast
```

Current facts take precedence over prior-round facts within the same fresh
inference invocation, permitting caller-before-callee source order without a
cache. New call-index/join and retained-evidence logical charges use the
existing inference budget; existing seed/reconstruction shape caps are
separate, not an aggregate CPU-visit proof. Avoid constructing unused call-shape
indexes for unrelated type-only modules. Imported and recursive return shapes,
unknown map parameters and unresolved alternative return shapes remain
explicit implementation boundaries.

### Finite Boolean match lowering

Edit the existing Gravity emitter for execution of C7's solved finite Boolean
matches. Root `true`, `false`, wildcard and lexical binding patterns use the
existing `if`, `do` and `let` instructions; no runtime match operation or
generated temporary name is needed. The scrutinee is evaluated once, before
the selected body. Keep a residual binder inside its own branch so it cannot
shadow an outer same-named value used by a sibling branch.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-boolean-match-lowering-development-kernel \
  --exact current-gravity-boolean-match-routes-development-kernel \
  --exact current-gravity-boolean-match-call-errors-development-kernel \
  --exact current-gravity-boolean-match-fixtures-development-kernel
```

These normalized checks exercise the emitter and its evidence joins, not a
complete source pipeline. Select C7 rows by their match identity and clause
ordinal: their `lhs` identifies a branch body, not the match. Preserve source
order, narrowed binder scope, original failure diagnostics and branch tail
targets. Use the existing lowering budget, including binder-use traversal.

Throwing local calls through a match also use C7's existing call-error graph and
the independent verifier in `gravity/effects.gravity`. Their supported-form
checks must agree with the existing conservative child-error union. This is
source-error potential, not a claim that every branch executes at runtime.
When changing interaction with source `let` or its instruction census, also
select `current-gravity-typed-let-lowering-development-kernel`; it checks the
existing let-body `do` wrapper directly. An unchanged lookup matrix is not a
prerequisite for editing match lowering.

Before accepting this increment, separately exercise real source through C7,
lowering and runtime, including both Boolean values, whole-domain binders and
wildcards, shadowing, throwing scrutinees and valid branch-tail recur. A focused
pass alone does not establish that composition. Partial Boolean matching,
structural pattern execution and guards remain separate implementation work;
this finite lowering limit is not a general L7 requirement that every match
be total. Normally returning guards retain L7's Bool-only policy.

```bash
# Affected source/type/effect/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-boolean-match-authenticated-composition --fail-fast
```

### Root Keyword match typing: development implementation

The current C7 extension reuses structural matching's existing scalar domain,
ordered partition, branch-local extraction and type-join machinery. Start with
the owning type kernel:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-keyword-match-development-kernel \
  --exact sh08-keyword-call-match-development-kernel \
  --exact sh08-keyword-match-fallback-and-source-development-kernel \
  --exact sh08-keyword-loop-match-cold-inference-development-kernel
```

An established Keyword parameter has an open domain. Literal clauses remove
their values from that domain; a fallback binder retains those exclusions.
An inner match on the binder or its immutable alias must not forget them.
Known literal/quote and validated alias domains remain exact, rather than
being widened to all Keywords. Open partial Keyword matches are currently
unsupported; this is not a language-wide totality rule or proof of closed-type
exhaustiveness. Existing Bool-only guard semantics are unchanged.

A private root-scalar demand may use an independently established Keyword
result without reconstructing a selected constructor coordinate. It does not
establish that type, authenticate a call, or grant deep selected-shape/callee
ancestry. The ordinary C7 call/source checks and guarded nth proof remain
required. Constructor, lookup, callee-body and literal-nth projection paths
retain their existing shape obligations.

The first kernel covers parameter inference, residual-binder and alias
preservation, exact literal domains, wrong-family patterns and source/binding
mutations. The fallback/source kernel adds wildcard coverage, duplicate-clause
rejection, escaped or wrong-scope binders and exact quoted/aliased domains.
The cold kernel starts with empty type/proof tables, derives the
existing loop and guarded-index facts, and then types the residual Keyword
binder. Binder seeding consumes the established Keyword result instead of
losing it through nominal call re-inference. A normally returning String branch
remains String and cannot acquire the loop's Never premise. No second range
solver or inference-order change is involved.

The call-shape kernel supplies an established Keyword result and checks its
shallow call/source joins and work boundary. It does not independently infer a
real callee's result or recover its constructor ancestry.

The existing emitter lowers scalar Keyword matches to the existing
`structural-match` instruction. Its focused check joins the current scrutinee
type, source, ordered rows and branch-local binder evidence:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-match-lowering-development-kernel
```

These kernels provide focused development feedback, not migration or
self-hosting evidence. The combined regression batch passed 13 tests and 332
assertions; the final lowering/fixture batch passed two tests and 37 assertions.
They overlap and are not summed as unique coverage. An earlier extra closing
delimiter stopped the fallback test's namespace load before assertions; the
corrected fallback kernel separately passed 15 assertions.

Run the affected source composition separately once the candidate is stable:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-match-authenticated-composition --fail-fast
```

Its paired fixture connects parameter and local-call scrutinees, exact literal
aliases, function recur, guarded vector indexing and Keyword throws. Rejected
source fixtures and fresh C7/C8 checks are separate from the normalized kernels.
The source `escape` fixture tests first-class `nth` escape, not a match binder
escaping its clause; binder-use and scope rejection currently have focused
ownership coverage. Parsing paired fixtures is not source-to-execution evidence.

The single frozen composition completed one test with 243 passing assertions,
one failure and no errors. Test-var execution took 1,044.249 seconds plus 8.248
seconds of namespace loading at 2 GiB. All 31 observed compiler, runtime, test
and fixture hashes remained unchanged. The original aggregate remains failed.
Both positive extensions passed actual local-call return typing, five matches
and eleven clause rows, function/loop recur, and seventeen guest outcomes each.
The scan returns three or throws the actual unmatched Keyword value; the catch
returns 99. Ordinary Keyword truthiness in `if` remains distinct from Bool-only
guards.

Seven mandatory fresh public C7 operations cover four positive operations and
three altered candidates. The stale nth source echo still passes its finite
helper but fails full reconstruction. Fictional exclusions and a changed
binder extraction are rejected by public reconstruction; these are not an
isolated partition-causality or comprehensive verifier-correctness proof.
Positive extraction and accepted/nonempty C8 graph guards prevent trivial
missing-value rejection credit. Both whole-effect verifications and the three
coherently resealed effect-graph hostiles passed.

All ten rejected `.gravity` fixtures rejected. The sole failed assertion was
the outside-guard diagnostic set: an unestablished match binder reaches the
earlier `C7-ANNOTATION :typed-throw-payload-type-required` prerequisite, rather
than one of the three anticipated later diagnostics. The expectation now
includes that observed pair; compiler behavior was not weakened. Other allowed
diagnostic memberships do not uniquely identify which alternative matched.
Rejected `.qst` files have byte parity, not separate execution coverage. The
unchanged expensive composition is not replayed merely to make its aggregate
green. Check unknown throw-payload diagnostic ownership directly:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact direct-typed-throw-source-and-type-development-kernel
```

The existing unknown-payload assertion now checks the exact rule and reason.
It passed 23 assertions; together with the unchanged lowering and fixture
checks, the post-correction batch passed three tests/60 assertions. Only the
two test files changed after the composition; its original verdict is intact.

Open partial Keyword matches, other root scalar families, generalized unions
and deep selected-shape propagation remain separate implementation work.
Quoted Keyword domain propagation has focused coverage only; this source
fixture does not exercise quoted-match composition. This is scoped executable
development support, not authoritative migration or a target/release claim.

### Constructor-linked structural match execution

For unguarded structural matches, edit the existing emitter and stage-owned
`gravity/runtime.gravity`. C7 owns shape inference, partitioning, narrowing and
exhaustiveness. Lowering consumes its freshly verified pattern tree and joins
source coordinates, clause order, binding scopes, types and uses; it does not
solve the shape problem again.

Start with these existing direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-structural-match-lowering-development-kernel \
  --exact current-gravity-structural-match-runtime-development-kernel \
  --exact current-gravity-structural-integer-pattern-development-kernel \
  --exact compiler-module-whole-runtime-data-development-kernel
```

The bounded projection emits one closed internal `:structural-match`
instruction. Fixed vectors require exact width. Literal-Keyword map patterns
permit extra keys and distinguish an absent key from a present nil. Recursive
patterns support wildcard, lexical bindings and represented primitive leaves;
primitive-family checks precede equality. Represented Integer literals are not
an implicit coercion to I64 or other numeric types. Keep unsupported numeric
elaboration explicit rather than interpreting host numeric equality as policy.

The scrutinee executes once without an outer recur target. Only the selected
body executes, retaining its original tail target. Failed partial bindings
must not leak into later clauses; throws, rejected control and fatal errors
retain their original payload and output. Validate the whole closed descriptor
before evaluating its scrutinee. Existing descriptor depth/work ceilings are
local shape bounds, not an aggregate evaluator CPU or termination proof.

These normalized checks are development feedback, not source composition or
authoritative migration. A changed projection or runtime instruction also
requires the affected paired source/type/effect/lowering/runtime composition,
including genuine constructor-derived patterns and public C7 rejection of
coherently changed extraction evidence. Reuse each fresh source product within
that composition; do not repeat unchanged Boolean/map/vector proof matrices.
Select the existing Boolean lowering/routes kernels when changing shared
match dispatch. Guard execution has its own checks below; additional L7
pattern families remain separate implementation work, not language prohibitions.

```bash
# Affected real-source composition, outside the edit loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-structural-match-authenticated-composition --fail-fast
```

### Root-map guard completion and execution

L7 guards that return normally require an established Bool result. `true`
selects the body, `false` continues, and non-returning `Never` preserves its
control transfer. There is no truthiness conversion. Change C7 for completion,
narrowing and result facts; `gravity/effects.gravity` for independent effect
joins; and the existing emitter/runtime for execution.

Start with the owning producer checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact sh08-root-map-guard-development-kernel \
  --exact sh08-root-map-guard-literal-development-kernel \
  --exact sh08-root-map-guard-completion-development-kernel \
  --exact sh08-guard-match-forward-inference-development-kernel \
  --exact sh08-quoted-data-guard-development-kernel \
  --exact body-effect-guard-development-kernel

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-root-map-guard-lowering-development-kernel \
  --exact current-gravity-root-map-guard-runtime-development-kernel \
  --exact current-gravity-guard-local-loop-parent-development-kernel \
  --exact current-gravity-guard-recur-diagnostic-envelope-development-kernel
```

Keep every body's original type and legality checks. A body that cannot execute
after a false or Never guard contributes no normal match result; its separate
effective-completion evidence does not relabel that body as Never. An unresolved
potential result remains Unknown until inference resolves it, including when
a later branch never returns. Never guards consume their structurally matched
cases by transferring control; they do not select a body or fall through.

After structural success, evaluate the guard once with its pattern bindings in
non-tail position. Preserve lexical target ancestry for authenticating a local
loop; that ancestry does not permit a direct recur from the guard. False
restores the original bindings for later
clauses while preserving output. Only the selected body receives its original
tail target. Nested guards must preserve authenticated binding-use order, not
assume that the grouped Core child vector is an execution schedule. Conservative
source-error summaries remain separate from selected-path precision.

These are normalized development checks. Changes to guard facts or execution
also need one affected paired real-source composition through C7, lowering,
runtime and actual C8 body/allowance checks before acceptance. Preserve direct
guard-tail-recur and escaped-binding rejection. The unchanged
`sh07-b10-root-map-guard-source-rejections` selector constructs four complete
upstream source products; it is not a cheap prerequisite for every guard edit.
Use affected source negatives in the selected composition rather than replaying
that unchanged matrix. No new authority admission or migration pin is needed
for this development feedback.

```bash
# Affected paired source/type/effect/lowering/runtime composition, separately:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-root-map-guard-authenticated-composition --fail-fast
```

### Constructor allocation effects

Change `bootstrap/gravity/src/gravity/effects.gravity` for evaluated collection
allocation inference, latent/caller propagation and source declarations. L6
already registers `:memory/allocate`; STD3 requires constructor allocation
behavior to be visible. Start with the current-source kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact body-constructor-allocation-development-kernel \
  --exact body-allocation-fixtures-development-kernel \
  --exact body-effect-operation-development-kernel \
  --exact body-effect-call-metadata-development-kernel \
  --exact current-gravity-evaluated-vector-c8-development-kernel \
  --exact current-gravity-keyword-map-lookup-c8-development-kernel
```

The constructor's own allocation is conditional on normal operand completion.
A Never operand prevents construction but does not erase operand effects or
errors. Quote remains inert DATA, not evaluated nested construction. Function
bodies retain latent effects; defining a function does not execute its body.
Existing declaration checking rejects inferred allocation outside allowances
with `C8-UNDECLARED`.

The effect node's allocation record describes static behavior. Extent counts
constructor input positions: map entries or vector/set elements. It is not
heap bytes, observed allocations or a set's runtime cardinality after equality
deduplication.
Placement, allocator-provider selection and ownership remain pending. Effect
inference and a source allowance do not grant allocator capabilities or prove
complete profile/provider legality.

For changes to this rule or its graph representation, run the affected source
composition separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-allocation-authenticated-paired-composition --fail-fast
```

It reuses fresh accepted/rejected source/type products and checks the actual
effect owner plus fresh whole-result reconstruction. It does not rebuild the
unchanged public C8 module bundle. Select that broader module composition only
when its forwarding/module interface changes; do not replay unchanged emitter
or runtime matrices for an effects-only correction.

C8 owns effect reconstruction, not a second implementation of C7's type and
shape solver. Its current public body-input gate checks a host-bound report
envelope; a passed report or matching digest does not itself execute fresh C7
verification. The paired tests above explicitly construct and verify current
C7 products before checking effects. Preserve that distinction: C8 must check
the source, ordered operands, completion facts and dependencies that determine
its effects, but deep type/shape authenticity remains the upstream boundary.
Do not add a fresh-proof wrapper to the local effect-edit loop or describe
effect-result reconstruction as independent certification of C7 correctness.

### Bare Map parameters: focused feedback

Change the existing `gravity.checked-core` signature owner, C7 engine, effect
checker and typed emitter. The new compatibility boundary retains each actual
argument type and its own declared-parameter premise, including mixed Map,
bare List, homogeneous List and scalar calls. It must not invent a structural
Map shape or erase other type obligations. Start with the existing runner:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-bare-map-parameter-development-kernel \
  --exact sh08-mixed-map-list-parameter-development-kernel
```

These are normalized source/compatibility checks, not accepted whole-module
products. Keep existing List row versions unchanged when no Map adaptation
occurs. Independent downstream reconstruction and real paired-source execution
remain separate integration work; no fresh authoritative admission is required
for these focused tests.

For the independent compatibility consumers, select the existing namespaces:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact body-mixed-map-list-parameter-development-kernel \
  --exact current-gravity-mixed-map-list-parameter-development-kernel
```

These checks reconstruct mixed parameter premises and whole compatibility rows,
not a fresh whole-source product. Full typed-call and source integration must
still exercise the real callee declarations, return types and execution.

For family-only removal, use these focused selectors:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-family-removal-development-kernel \
  --exact body-map-family-removal-development-kernel \
  --exact current-gravity-map-family-removal-lowering-development-kernel
```

These require bare-family operation evidence, reject invented member shapes and
check normal allocation versus operand transfer. They do not authenticate a
whole source module. Retain failed controls separately and rerun only selectors
affected by the diagnosed correction, not an unchanged passing aggregate.

Once the changed candidate is stable, run the affected source composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-parameters-authenticated-composition
```

It selects four fresh inputs: the accepted Gravity/QST pair, a non-Map call
rejection and an undeclared-allocation rejection. Accepted products are reused
for compatibility, effect, lowering and runtime checks; other negative fixture
pairs do not acquire fresh source-rejection credit from header checks.
Public C7 reconstructs upstream type facts. C8 independently checks the
compatibility evidence it owns, not a duplicate general quote/type solver.
This composition is development integration, not authoritative migration.

### Persistent Map removal: focused feedback

The current `dissoc` increment starts in the existing C7 type owner. For its
pure known-shape transition, use the direct selector:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-map-removal-transition-development-kernel \
  --exact sh08-map-removal-source-development-kernel \
  --exact sh08-map-removal-inference-development-kernel
```

This checks present/absent/empty removal, dense result coordinates, retained
survivor source parts, false/nil payload types and logical work limits. Its
input is an already established Map shape; acceptance by this private copying
helper does not establish member-type validity or authenticate source input.
The source kernel separately joins the real owning shape reconstruction to
the actual argument type and checks original forms, resolved call/binding
records, allocation policy and source/work-limit rejection. These kernels alone
do not establish a compiler-supported source call. The inference kernel checks cold normal and
Never cases, ordered constraints, descriptor mutations and Unknown deferral
with exact/one-below budgets. Fresh public C7 reconstruction, independent C8
reconstruction, lowering/runtime behavior and affected real
source composition are checked separately below. The
generic STD3 Map rule is broader than the first literal-Keyword/known-shape
implementation frontier. No admission proof is needed to run this kernel.

For the independent effect-consumer increment, use:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-map-removal-development-kernel
```

It reconstructs removal, source/call/fact/constraint joins, allocation/storage
records and work limits for normal present/absent/empty cases. The node-record
Never control checks operand effects and suppression of direct allocation;
it does not establish source-bound or whole-C8 noninvoked-call acceptance.
Fresh genuine upstream inputs, full graph checks and lowering/runtime/source
integration remain distinct obligations. A copied C7 input shape is not
independently authenticated merely because its removal transition matches.

For the existing typed emitter and stage-owned runtime, select:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-removal-lowering-development-kernel \
  --exact current-gravity-map-removal-runtime-development-kernel
```

These check source-bound descriptor reconstruction, per-call rows, rejected
work carry, binary IR, eager operands, first control transfer, Map/Keyword
value guards and inert stored payloads. Raw IR computed-key cases exercise
the evaluator, not wider source-key support. Normalized caller fields are not
authentic function-tuple integration. Keep the failed mixed batch distinct
from its corrected targeted tests; do not rerun unaffected selectors merely
to obtain a green aggregate. Real paired source and whole-artifact checks are
still required before accepting the complete slice.

For the stable changed candidate, the separate source composition is:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-removal-authenticated-composition
```

It builds the accepted Gravity/QST pair and one quoted-Map missing-allocation
rejection, reusing accepted products for all consumers and mutations. Genuine
non-returning calls, surviving-source provenance, whole-C8 budgets and ordered
row census belong here, not to the normalized kernels' claims. Other rejected
fixture pairs are not fresh source-proof results merely because their headers
pass. No W5 execution, general Map parameters, arbitrary source keys, target
support or authoritative migration follows from this selection.

The first actual Map-removal composition stopped after six loader assertions
and one error, before any fresh source product completed: automatic C2 cache
publication exceeded the existing 512 MiB aggregate store limit. This is a
development-path dependency, not a Map-removal result. The narrowly changed
default reader uses its existing fresh producer; the explicit cache API and
strict admission failures remain unchanged. Validate that dispatch and parity
with the existing two hosted C2 cache selectors before a distinct composition
run. Preserve the failed result rather than relabeling it or clearing the store.
The corrected reader tests passed 42 assertions; strict admission passed four
separate assertions. The changed-dependency composition passed 448 assertions
in 421.346 seconds. Its support remains the literal-Keyword/known-Map-shape
frontier, not general Map parameters or authoritative module admission.

### Persistent map association

The current-source development path for `(assoc map :literal-key value)` uses
the same C7 engine, effect owner, typed emitter and `gravity.runtime` evaluator.
Change those owning files rather than adding a new lane or wrapper. Start with:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-association-development-kernel \
  --exact sh08-map-association-premises-development-kernel \
  --exact body-map-association-development-kernel \
  --exact body-map-association-catch-effects-development-kernel \
  --exact current-gravity-map-association-lowering-development-kernel \
  --exact current-gravity-map-association-runtime-development-kernel
```

The bounded operation adds or replaces one literal Keyword key in an
established map without changing its input. Values may have established types,
including Bool parameters and String results; they need not be literals.
Association results retain derived shape ancestry for later association,
lookup, supported local returns and structural matching. They are not
relabeled literal constructors. Operator, map, key and value retain once-only
source order; a control transfer prevents invocation. Allocation remains
conditional on normal operand completion, with operand effects preserved.
Typed catches can clear escaping error types without erasing the performed
`:error/throw` effect. Check those projections separately; effect-label
discharge remains pending profile, handler and capability authority.

General or dynamic keys, variadic association, unknown map parameters,
transients and complete collection-library support remain outside this slice.
Changes to the type/shape carrier, effect projection or executable instruction
require the affected real source/type/effect/lowering/runtime composition,
separately from these normalized kernels. Neither check admits a stable module
revision or establishes authoritative migration, target support or self-hosting.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-association-authenticated-composition
```

This selector shares each fresh paired source/type product across actual
lowering, guest execution and C8 checks. Deep association-ancestry mutations are
checked by the public lowerer's genuine C7 verification; effect-graph mutations
are checked by C8's whole-result reconstruction. The seven rejected `.gravity`
sources exercise type/key/arity/allocation boundaries; the cheap fixture check
establishes byte-identical `.qst` counterparts, not separately executed negative
source coverage.

### Collection count

The same C7 engine, effect owner, typed emitter and runtime own direct
`(count collection)` for currently established Map, Vector and persistent List types. Use:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-collection-count-development-kernel \
  --exact body-collection-count-development-kernel \
  --exact current-gravity-collection-count-lowering-development-kernel \
  --exact current-gravity-collection-count-runtime-development-kernel
```

The query returns `Integer`, not an implicit I64, or Never when its operand
does not return normally. It evaluates the operand once and counts the actual
top-level elements or associations at runtime. It does not substitute a
compile-time member count and erase operand work. Its own query has no
collection-construction effect; operand allocation and error effects remain.

Do not route this query through structural-pattern analysis. A legal typed
quoted map with String keys, or nested values outside the pattern domain, can
still be counted. Count needs the established collection type, not a deep
shape proof or a static-cardinality artifact. Keep authentic call/source/type
joins and fresh verification at their existing boundaries. Unknown operands
still reject; this is not a new generic Map annotation or type-union feature.
String Unicode counting, nil conventions and other collection families remain
separate language/library work.

For changes to this query or its typed instruction, run the affected source
composition separately from the edit loop:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-collection-count-authenticated-composition
```

It reuses the paired current source products for type/effect checks, public
lowering and execution. The source cases include quoted materialized maps,
aliases, local returns, association, guards and control transfer. The separate
rejected `.gravity` cases check operation and type diagnostics; cheap parity
checks cover their `.qst` counterparts. This is affected composition, not
authoritative migration or complete collection/profile/target support.

Persistent List cardinality follows STD3. Use the
same four owners and focused selectors, not a new lane or C5/C6 bridge. Bare,
positional and homogeneous List types establish the collection family; `count`
does not need member typing, nonemptiness or a static-length proof. Validate
closed argument containers and unary arity before collecting operand type
work. Keep existing Map/Vector evidence unchanged where possible and give
List evidence an accurate input-family description.

For the new List behavior, add these existing-namespace selectors to the same
direct command above:

```text
--exact sh08-list-count-development-kernel
--exact body-list-count-development-kernel
--exact current-gravity-list-count-lowering-development-kernel
--exact current-gravity-list-count-runtime-development-kernel
```

Private opaque-member and large-metadata controls check that this query does
not visit member data. They do not admit host objects or enlarge upstream
source/type bounds.

The affected List composition is separate from the earlier Map/Vector source
aggregate; do not rerun that unchanged aggregate for the List extension:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-count-authenticated-composition
```

The accepted Gravity/QST pair covers bare List parameters, homogeneous tails,
an explicitly typed List-returning producer and an aborting operand. Products
are constructed once per source and reused for runtime and hostile checks;
one String source supplies the representative semantic rejection. Qualified
core-operator source resolution remains a separate C5 frontier, so this pair
uses ordinary `count`. The normalized qualified-header control is not source
resolution evidence.

### Persistent vector append

For the fixed-arity `(conj vector value)` slice, edit the existing C7 engine,
`gravity/effects.gravity`, typed emitter and stage-owned `gravity/runtime.gravity`.
STD3 owns persistent collection semantics; the core spelling is a compatibility
resolution, not evidence of a complete standard-library module or provider.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-vector-append-development-kernel \
  --exact sh08-vector-append-projection-development-kernel \
  --exact body-vector-append-development-kernel \
  --exact current-gravity-vector-append-lowering-development-kernel \
  --exact current-gravity-vector-append-runtime-development-kernel
```

An established vector's complete ordered element types are followed by the
appended value type, including heterogeneous values. Normal results retain the
existing 1024-element bound. Operator and operands evaluate once, left-to-right;
the original vector and prefix values are preserved. A Never operand prevents
the append's own allocation without excusing invalid source or operand types.
Operand effects and error transfer remain visible. C8 must retain append work
in both the module's remaining budget and its returned cumulative work total.

`count` consumes the established result type. Literal `nth` projection retains
an explicit append application/source link: an appended coordinate selects the
new value; a prefix coordinate delegates to the original vector. It does not
relabel the append as a constructor or inspect unrelated nested prefix data.
Whole-shape consumers currently require one established vector shape.
Appended-vector S/T iteration and growing-vector recur types are not silently
admitted by this extension; their origin/type frontiers remain separate work.

Focused checks are development feedback. Changes to this slice also require
affected real paired source through current C7/C8 verification, lowering and
execution, including persistent input preservation, append-to-count/nth,
throwing operands and rejected family/arity/extent/allocation cases. This is
affected integration, not an authoritative module migration or a collection,
profile, target or self-hosting completion claim.

For changes to this composition, run the existing source selector separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-append-authenticated-composition --fail-fast
```

The cheap `current-gravity-vector-append-fixtures-development-kernel` checks
paired source parity, parsing and test-only diagnostic handling; it does not
establish semantic acceptance or rerun the oversized source.
The backlog records the original partial-error composition and its separate
remaining-negative result. The oversized literal reaches C6's fragment-form
bound before C7's vector extent check; the latter has normalized kernel coverage.

### Value equality development

STD2 owns value equality; STD4 requires exact text semantics. The current
binary `=` development slice covers Nil, Bool, Character, String, Symbol and
Keyword. Same-family operands compare values; different supported families
compare false. Strings compare the exact scalar sequence without normalization
or locale folding. The scalar path does not introduce numeric or function
equality typing, nor does it change ordinary integer runtime compatibility.

STD3 also defines ordered persistent Vector value equality. Its current
development path accepts independently established finite Vector types with
complete ordered element types, recursively containing Vectors and the
supported scalar or nil-bearing scalar alternatives. This is a value query,
not a requirement for literal-constructor ancestry. Counts, positions and
stored values matter; physical sharing and type identity do not decide the
answer. Numeric members, bare/unknown Vector types and other collection
domains remain unsupported by this implementation, not forbidden by STD3.

The integral path follows STD5 and SAFE9: two independently established
operands of the same integral type compare their original integer values.
It records `:proof-required` and `:proven-safe` because comparison performs
no conversion or arithmetic and cannot overflow or round. The supported type
families are Integer, I8/I16/I32/I64 and U8/U16/U32/U64. Generic Integer does not
select a new machine-width or arbitrary-precision arithmetic policy. Mixed
types, floating/decimal/ratio equality and numeric Vector members remain
separate implementation frontiers.

Use the owning type, effect, lowering and runtime kernels without rebuilding
upstream products:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-atomic-equality-development-kernel \
  --exact sh08-character-literal-and-quote-development-kernel \
  --exact body-atomic-equality-development-kernel \
  --exact current-gravity-atomic-equality-lowering-development-kernel \
  --exact current-gravity-atomic-equality-runtime-development-kernel --fail-fast
```

For a Vector equality change, select its four owning checks instead:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-vector-equality-development-kernel \
  --exact body-vector-equality-development-kernel \
  --exact current-gravity-vector-equality-lowering-development-kernel \
  --exact current-gravity-vector-equality-development-kernel --fail-fast
```

For an integral equality change, use its corresponding owners:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-integral-equality-development-kernel \
  --exact body-integral-equality-development-kernel \
  --exact current-gravity-integral-equality-lowering-development-kernel \
  --exact current-gravity-integral-equality-runtime-development-kernel --fail-fast
```

Its fixed-width type cases are normalized compiler evidence. Runtime checks
exercise the existing represented-Integer primitive, including adjacent values
above `2^53`; a host `integer?` check does not admit arbitrary raw values into
fixed-width source parameters. One Never operand does not excuse the other
operand's type legality.

The Vector checks cover bounded type/value traversal and rejected-work accounting.
Both complete runtime operand domains are validated before a length or earlier
element mismatch can return false. The query adds no guest allocation; operand
constructors still retain their C8 allocation effects. This is not a claim
that the interpreter's internal traversal uses no host allocation.
Add the existing scalar, nil-bearing or module-initialization checks when a
change also affects their shared dispatch or runtime owner; do not rerun every
historical equality composition for an isolated Vector change.

Operands require independently established supported types and exact source,
binding and evaluation-order joins. Normal results are Bool; Never prevents
invocation without excusing the other operand's legality. Equality introduces
no direct allocation, capability or error effect; operand effects remain.
Character transport uses the occurrence checks above, not a map-tag shortcut
or a new Character annotation spelling.

These kernels are development feedback, including distinct equal-content
Strings, NUL and supplementary text, complete Symbol/Keyword values, once-only
operand order and error transfer. Unsupported private-IR payloads reject without
invoking opaque equality or hashing callbacks; this is not source host-object
support. The Character guard recognizes representation, while C2 owns the
codepoint/text decoding theorem.

Affected real source composition is a separate required check before accepting
the complete executable slice:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-atomic-equality-authenticated-composition --fail-fast
```

It exercises paired current source through type/effect verification, lowering
and execution, including supplementary Character occurrences and distinct
ordinary Map DATA. Keep negative mutations tied to actual Character source
identities rather than rewriting every equal host-map payload. The cheap
`current-gravity-atomic-equality-fixtures-development-kernel` checks the fixture
inputs before that run, not the complete semantic composition. No authoritative
migration or standard-library completion follows from these development checks.

For a stable changed Vector integration, use
`current-gravity-vector-value-equality-authenticated-composition` in that same
SH01 namespace instead. It preserves W5's original modes definition/getter,
compares actual stored values, verifies C8 before execution, and reuses accepted
products for hostile mutations. Run this affected composition once after the
owning checks and review; documentation edits do not require reconstructing
unchanged products. The fixture's explicit allocation allowance does not settle
the full W5 module's current empty effect declaration.

The corresponding integral selector is
`current-gravity-integral-value-equality-authenticated-composition`. It checks
real W5-style count and private ordinal comparisons, a typed left-operand
transfer that suppresses the right operand, and mixed I64/U64 source rejection.
The accepted I64/U64 function bodies are compiled and checked, not invoked with
raw host values. Handler type flow does not discharge profile/provider or
capability authority; the effect artifact retains that pending boundary. Keep
those claims separate from the executed generic-Integer results.

### Persistent List prepend: focused feedback

STD3 defines binary `(conj items value)` for persistent Lists as prepend,
leaving the original ordered values after the new head. The development slice
supports reverse-and-join with persistent List accumulators. C7 owns receiver-type dispatch and result typing; C8 owns
independent reconstruction and allocation; the typed emitter and runtime own
execution. Preserve the existing vector-append path rather than treating a
List as a Vector.

Start with the existing direct component selectors:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-list-prepend-development-kernel \
  --exact sh08-vector-append-development-kernel \
  --exact body-list-prepend-development-kernel \
  --exact current-gravity-list-prepend-lowering-development-kernel \
  --exact current-gravity-list-prepend-runtime-development-kernel \
  --exact current-gravity-list-prepend-fixtures-development-kernel
```

The new rule preserves a matching homogeneous member type, prepends to finite
positional types, and leaves a bare List bare. Unknown values do not invent
membership. Operand/result validation, finite width/depth and construction
work remain bounded; an aborted invocation adds no prepend allocation.
These focused checks include normalized downstream and runtime kernels, not
fresh source composition or authoritative migration. When the source-to-typed
interface or affected composition changes, use the existing
`current-gravity-list-prepend-authenticated-composition` selector in the SH01
namespace once on the stable candidate. It checks the real reverse-and-join
source path and semantic rejection cases. Do not rebuild unchanged source
products to repeat a green aggregate; do not update admission fingerprints for
ordinary development.

### Homogeneous List returns: declaration feedback

The explicit `(List T)` return slice is in development. L5 requires the body
type to be established independently and retained alongside return-specific
compatibility evidence. Start declaration and transport changes with these
existing selectors; their names also cover the earlier parameter boundary:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --namespace gravity.self-hosting.sh07-b47-function-call-recursion-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh06-list-parameter-type-projection-development-kernel \
  --exact sh07-b47-list-parameter-reference-projection-development-kernel \
  --exact seed-function-declaration-headers-development-kernel \
  --exact seed-function-declaration-rejections-development-kernel \
  --exact seed-function-declaration-plan-parity-development-kernel
```

Both the C5 walk and retained C6 mirror must visit the constructor and member
in type positions, preserving scope, occurrence identities and body coordinates.
The temporary header normalizer checks shape, not constructor spelling;
authenticated C6 binding resolution owns constructor legality. These checks do
not establish return typing, effect reconstruction, typed lowering or actual
source composition. Those remain separate required checks for the new slice.

For return compatibility itself, edit the C7 engine and use its direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-homogeneous-list-return-development-kernel \
  --exact sh08-homogeneous-list-parameter-development-kernel \
  --exact sh08-bare-list-parameter-development-kernel
```

The return rule retains the declared outward type separately from the actual
body type. It must debit member/type work across functions and inference
rounds, and reject changed source/binding premises. Passing these kernels does
not replace independent C8 reconstruction, typed lowering or source composition.

For C8 or lowering return-context changes, use the matching existing kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact body-homogeneous-list-return-development-kernel \
  --exact body-homogeneous-list-return-function-joins-development-kernel \
  --exact current-gravity-homogeneous-list-return-consumer-development-kernel \
  --exact current-gravity-homogeneous-list-return-definition-development-kernel
```

These compare independently reconstructed return premises and strict ordered
row censuses, including function maps whose iteration order differs from source
order, and validate complete normalized definition joins. They are not
whole-module or fresh source acceptance. For changes to independent body
inference, also select
`sh08-homogeneous-list-return-branch-independence-development-kernel` and
`sh08-homogeneous-list-return-structural-independence-development-kernel` in
the SH08 namespace. Canonical `if` now supports the independently established
List join described below; other control-flow joins are not widened. The
return annotation must not supply a common type. The affected source
composition must also be reviewed before accepting the new return slice.

Run the affected source composition only for a stable changed candidate:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-homogeneous-list-return-authenticated-composition
```

It constructs the accepted Gravity/QST pair and representative C7/C8 negatives
once each, plus one C5-only rejection. Accepted products are reused across the
runtime cases and hostile checks. This is development integration evidence,
not authoritative migration. Preserve a failed run and diagnose its actual
scope: a test-accessor correction alone does not justify repeating unchanged
expensive source construction. Validate such a correction with the affected
cheap projection check and review the retained actual results.

### Homogeneous List canonical-if joins: focused feedback

For L5's independently established List branch join, edit the C7 type owner.
Do not widen the shared normal join used by other control-flow forms or use a
return annotation to invent a missing element type.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-list-if-join-type-development-kernel \
  --exact sh08-list-if-join-source-development-kernel \
  --exact sh08-list-if-join-inference-development-kernel
```

These normalized checks exercise independent element evidence, actual branch
types, source/binding correspondence, compatibility rows and inference work
limits. They cover both branch orders and preserve ordinary `if` truthiness,
distinct from Bool-only match guards. Passing them does not establish public
C7 artifact integration, independent C8 reconstruction, typed lowering or
source execution. Those checks are separate integration coverage; an existing
return declaration check is not a substitute for them.

For changes to the independent C8 context or typed lowering, select their
existing consumer kernels separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact body-list-if-join-development-kernel \
  --exact current-gravity-list-if-join-lowering-development-kernel
```

These check ordered compatibility-row census, source-bound reconstruction,
exact/one-below budgets, failure-work carry and the existing `if` instruction
with type-consistent guest values. They do not replace affected original-source
composition or establish module migration. Only rerun the relevant kernels
when their owning behavior changes.

For the stable changed candidate, run the affected original-source composition
separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-if-join-authenticated-composition
```

It constructs the accepted Gravity/QST pair and one representative C7 rejection,
then reuses accepted products for public lowering, independent C8 checks,
runtime cases and hostile mutations. A failed mixed regression batch remains
failed even when a targeted correction passes; review the retained passing
checks and rerun only affected regressions instead of rebuilding unchanged
source proofs to obtain a green aggregate. This is development integration,
not authoritative migration or target/self-hosting completion.

### Nil-bearing canonical-if joins: focused feedback

Edit the existing C7 type owner, C8 effect checker and typed emitter for L5's
independently established nil-or-atomic branch join. The current frontier is
Bool, Character, String, Symbol and Keyword. Preserve the actual nil/value,
local inferred return type and source-bound join premise; an expected non-nil
return declaration cannot remove nil. Different nil-bearing atomic families
can both contain nil, so equality compares their actual values rather than
folding the result from their non-nil types.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-nil-if-join-type-development-kernel \
  --exact sh08-nil-if-join-inference-development-kernel \
  --exact sh08-nil-atomic-equality-development-kernel \
  --exact body-nil-if-join-development-kernel \
  --exact body-nil-atomic-equality-development-kernel \
  --exact current-gravity-nil-if-and-equality-development-kernel
```

These normalized checks cover closed type validation, source-bound premises,
ordered mixed List/nil join rows, budgets, lowering and runtime value equality.
They are not cold source inference or authoritative migration. The separate
`current-gravity-nil-if-join-fixtures-development-kernel` checks that the paired
fixture preserves W5's actual unannotated `w5-se-canonical-form` helper.
For affected cold source composition, run separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-nil-if-join-authenticated-composition --fail-fast
```

It checks the unannotated helper's local callers and declared-non-nil rejection,
reusing the accepted pair and one negative product for consumer and runtime
observations. It is not a prerequisite for each subsequent unchanged edit.
Ordinary `if` truthiness and Bool-only match guards remain distinct:
nil-or-Bool is not a legal guard type. Numeric/general unions, new annotation syntax and widening
of match, try, loop or recur joins are outside this increment.

### Homogeneous List traversal: focused feedback

The `(List T)` parameter and guarded-head slice supports content-consuming
traversal such as joining a persistent List of Strings. Exercise
type-position transport, parameter headers, C6 declaration/source joins, C7
typing, independent C8 reconstruction and typed lowering directly:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh06-resolution-adapter-test \
  --namespace gravity.self-hosting.sh07-b47-function-call-recursion-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact sh06-list-parameter-type-projection-development-kernel \
  --exact sh07-b47-atomic-signature-lowering-development-kernel \
  --exact sh07-b47-list-parameter-reference-projection-development-kernel \
  --exact seed-function-declaration-headers-development-kernel \
  --exact seed-function-declaration-rejections-development-kernel \
  --exact sh08-homogeneous-list-parameter-development-kernel \
  --exact sh08-homogeneous-list-head-development-kernel \
  --exact body-homogeneous-list-parameter-development-kernel \
  --exact body-homogeneous-list-head-development-kernel \
  --exact current-gravity-homogeneous-list-call-lowering-development-kernel \
  --exact current-gravity-homogeneous-list-head-lowering-development-kernel
```

The temporary adapters preserve compound annotation source and type-position
references; they do not confer constructor or member-type authority. C6 owns
those checks. Parameter-reference transport has two sides:
`semantic_late/151848-sh06-resolution-inputs-traversal.clj` records C5 references
and `semantic_late/154235-sh07-core-resolution-fn.clj` projects them into the C6
request (both under `bootstrap/clojure/src/gravity/bootstrap_impl/`). Preserve
their reference order, type positions and source coordinates; do not relax the
request's bijection checks when adding an annotation shape.
Compound return annotations remain outside this implementation
increment. Empty Lists satisfy the parameter declaration, but `first` can have
the non-Nil member type only under an established nonempty fact. This increment
reconstructs that fact in the else branch of the nearest canonical
`if (empty? same-immutable-binding)`. A different binding, a tail, mutation or
an unrelated nearer condition cannot borrow it. `rest` preserves `(List T)`,
not nonemptiness; function recur retains exact slot types. Supported `T` is an
existing non-Nil atomic declaration, not nested applications or general
generics. Homogeneous-to-bare List adaptation remains unsupported.

Run the affected source-to-runtime composition once for a stable changed
candidate, rather than replaying old source proofs after every component edit:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-homogeneous-list-authenticated-composition --fail-fast
```

The paired accepted source joins empty, singleton and two-element String Lists
to `""`, `"alpha"` and `"alphabeta"`. The changed-candidate composition passed
257 assertions, including actual execution, retained head/throw identity,
specific C6/C7/C8 rejection cases and fresh affected verification. Selected
execution took 1,211.036 seconds plus 8.853 seconds of namespace loading at
2 GiB. This is a workload observation, not a phase profile or speedup claim.
The historical public emitter facade
may reject changed source against stale admission pins; do not refresh those
pins merely to make development feedback green.

### Typed shrinking-List traversal

L5's explicit bare `List` parameter boundary lets a function consume Lists
without retaining a particular positional length. Change the signature path in
`checked_core.gravity`, then C7, C8 and the typed emitter for call compatibility;
the existing runtime owns the List query and recur execution.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh07-b47-function-call-recursion-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh07-b47-atomic-signature-lowering-development-kernel \
  --exact sh08-bare-list-parameter-development-kernel \
  --exact sh08-list-empty-development-kernel \
  --exact body-bare-list-parameter-development-kernel \
  --exact body-list-query-development-kernel \
  --exact current-gravity-bare-list-call-lowering-development-kernel \
  --exact current-gravity-list-empty-traversal-runtime-development-kernel
```

The actual finite argument type stays in the type table and call facts. A
separate, reconstructed compatibility premise binds the information-forgetting
step to the explicit parameter annotation, argument and call source. It is not
type equality, a runtime conversion or an unannotated inference shortcut.
Schema, ownership, linearity, region and profile obligations are not erased.
Imports, higher-order calls and general subtype/coercion rules are not extended
by this ordinary-local-call rule.

Inside the function, `empty?` returns Bool for an established List and `rest`
preserves its bare List type. Existing function-target `recur` still checks its
slot types exactly. These operations permit a real shrinking traversal without
guessing any element type. Exact bare-List return annotations are separate from
finite-to-bare return adaptation, which is not supplied by the parameter rule.
`first` on a bare List still needs head-type evidence; homogeneous element
annotations, general generics and path-sensitive head refinement remain later
work. Operand effects and original transfers remain visible.

Run the changed real-source traversal composition only after these kernels and
proportionate regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-traversal-authenticated-composition --fail-fast
```

The paired `local-runtime-list-traversal` fixture exercises an annotated tail
function, shrinking recur slots, finite-to-bare argument compatibility and
ordinary exact List calls. Manual runtime plans and normalized type premises do
not establish C5-to-C8 source composition or authoritative module migration.

### Persistent List head and suffix queries

STD3 owns unary `first` and `rest` on persistent Lists. Change C7, C8, the
typed emitter and the existing Gravity runtime; do not add a seed operation
or infer executable syntax authority from the stored values.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-list-query-development-kernel \
  --exact body-list-query-development-kernel \
  --exact current-gravity-list-query-fixtures-development-kernel \
  --exact current-gravity-list-query-lowering-development-kernel \
  --exact current-gravity-list-query-runtime-development-kernel
```

Finite positional List types establish the first element's type, or Nil for
an empty List, and the exact ordered suffix. A known bare List is enough for
`rest` to return a bare List; it need not invent element facts. `first` on a
bare List remains a missing-head-type diagnostic, not an implicit Dynamic
operation or a language-wide ban. Queries use established type evidence,
not constructor, quote or local-return ancestry.

The operand evaluates once before querying. A transfer skips the query,
preserving its original payload and output. Stored payloads are not executed
and the original List is unchanged. The selected development runtime guards
actual Lists and returns the existing immutable tail, which may retain input
storage. Empty input gives nil for `first` and an empty List for `rest`.
The query adds no direct allocation in this representation; allocations and
errors from the operand remain visible. This is not a generic sequence,
profile, ownership, provider or backend claim.

After these kernels, exercise the changed current-source composition once
before accepting its integration:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-query-authenticated-composition --fail-fast
```

Do not replay unchanged constructor or
String compositions. Record actual source results separately from these
normalized kernel checks and from authoritative migration.

### Evaluated persistent List construction

STD3's `list` constructor builds immutable DATA, not executable forms or syntax
authority. Change C7, C8, the typed emitter and the existing Gravity runtime.
Use the direct development kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-list-construction-development-kernel \
  --exact body-list-construction-development-kernel \
  --exact current-gravity-list-construction-fixtures-development-kernel \
  --exact current-gravity-list-construction-lowering-development-kernel \
  --exact current-gravity-list-construction-runtime-development-kernel
```

The finite call boundary accepts zero through 1,023 established, lowerable
first-order operands; the existing 1,024-child limit includes the operator.
Known bare collection types need no invented member information. Supplied
positional type records are validated within the existing depth/width/work
limits, including visited work on rejection. The result records operand types
in order, with evaluated-construction ancestry rather than quoted-data IDs.

Operands evaluate once, left-to-right. A transfer prevents construction and
later execution, without excusing unknown or illegal source operands. The
current conservative constructor policy records latent allocation and normal-
invocation direct allocation, even for an empty List; placement, provider and
ownership remain unresolved by that policy. Source allowances still apply.
The guest returns a real List, preserving nesting, duplicates, false and nil.
Its shared argument collector uses a loop rather than host-stack recursion.

Run the affected source composition separately after the kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-list-construction-authenticated-composition --fail-fast
```

The cold composition must infer List returns from real bodies and local calls,
not supplied result types or new annotation syntax. It checks inert form DATA
containing a computed String, genuine control transfers and representative
source rejections. Do not replay the unchanged String composition. List
queries have a separate focused workflow above; generic sequences,
syntax-object generation, arbitrary recursive
traversal, imported List signatures and full profile/provider/backend legality
remain separate frontiers.

### Binary String concatenation

STD4 owns the binary String overload of the compatibility spelling `str`.
Change C7, C8 and the typed emitter for its applied type, allocation and
lowering. The existing runtime's `str` route does not establish typing or
permission to format arbitrary values.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-string-concat-development-kernel \
  --exact body-string-concat-development-kernel \
  --exact current-gravity-string-concat-lowering-development-kernel \
  --exact current-gravity-string-concat-existing-runtime-development-kernel
```

Both normally returning operands must have established String types. They
evaluate once, left-to-right; a transfer prevents invocation and preserves
operand effects. The applied overload retains latent `:memory/allocate`, while
direct allocation occurs only on normal invocation, including empty Strings.
C8 independently reconstructs these facts and checks the source allowance;
the generic compatibility binding's empty effect row is not overload purity.
Concatenation preserves exact scalar sequences without normalization, coercion
or general formatting. Variadic calls, other overloads, library exports and
provider/profile legality remain separate frontiers.

For the affected source composition, run separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-concat-authenticated-composition --fail-fast
```

It assembles each accepted extension once and checks nested calls, eager Map
fallback allocation, actual transfers and representative source rejections.
Public verification remains fresh for every selected operation. The negative
fixture siblings are parity checks, not duplicate source proofs. Do not replay
the unchanged Map lookup or earlier compositions as a prerequisite.

### Map lookup with an explicit default

STD3 defines `(get map key default)` as an eager ordinary call. A present value
is returned even when it is false or nil; only an absent key selects the
default. Map, key and default evaluate once in that order, including the
default on a hit. A transfer in any operand prevents invocation.

Edit the existing C7 engine, effect owner, typed emitter and Gravity runtime:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-get-development-kernel \
  --exact body-map-get-development-kernel \
  --exact current-gravity-map-get-lowering-development-kernel \
  --exact current-gravity-map-get-runtime-development-kernel --fail-fast
```

The finite normal-returning route requires an independently established Map
member-type vector, a dynamic String or Keyword key and an established default
type T. Every value potentially matching that key family must have exactly T;
unrelated known scalar key families may have other value types. Empty Maps use
T. This is a type join, not a literal-constructor or static-presence proof.
Unknown or malformed member keys cannot be discarded as unrelated.

With a Never operand, no lookup returns normally and no value/default join is
invented. A known bare Map is then sufficient, while the other normally
returning operands must still have legal, established domains. Supplied
structured-type evidence retains its bounds and integrity checks. The query
adds no allocation or effect; all operand allocation and errors remain visible,
including the eagerly evaluated default.

These normalized checks do not authenticate source products. Before accepting
the changed composition, separately exercise actual source, local Map returns,
eager defaults, meaningful rejections and fresh public reconstruction. The
affected source selector is:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-get-authenticated-composition --fail-fast
```

It assembles all functions once per accepted extension, retaining fresh public
verification, rather than also compiling the same positive function separately.
Representative real source rejections complement the complete focused type
matrix; this is not a request to replay the preceding membership composition.
The result type does not confer selected deep-shape ancestry. General stored-key
families, first-class calls,
full profile/provider support and authoritative migration remain separate
frontiers rather than new language prohibitions. The distinct proven-present
two-argument route below can select a type from a heterogeneous finite Map.

### Map lookup with established presence

For `(get map :key)`, change the existing C7 Map-lookup owner, effect checker
and typed emitter. Normal invocation needs independently established finite
Map shape, the actual literal Keyword, its selected coordinate and stored type;
a member-type family does not establish key presence. Use the direct kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-present-get-development-kernel \
  --exact body-map-present-get-development-kernel \
  --exact current-gravity-map-present-get-lowering-development-kernel
```

These are normalized development inputs, not fresh source products. Keep the
three-argument lookup's existing descriptor and behavior unchanged. Missing or
unproved presence is a typing rejection, not a fabricated nil/default result.
Stored false and nil remain values. A transferring operand needs no normally
returned value or presence proof, but other returning operands retain their
domain checks. Lower both operands non-tail and once in source order; only a
validated inert literal Keyword may be reordered in the existing lookup IR.
C8 checks its retained compatibility premises, not a duplicate source-shape
solver. Affected source composition remains necessary before accepting the
changed integration; authoritative migration is separate.

For that stable-candidate composition, select:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-present-get-authenticated-composition
```

It constructs the accepted Gravity/QST pair and one real missing-key rejection,
then reuses the accepted products for whole C8, public lowering and runtime
observations. Keep coherent deep source-shape forgeries at the public C7
boundary, and test C8's own selection premises with genuine input shape.
Do not construct another source product for each malformed proof field.

### Private call-entry Map key presence

For an immutable private function parameter, L5 permits entry-specific key
facts when every normally invoking incoming call independently establishes the
key and selected scalar type. Edit the same C7, effect and emitter owners, not a new
admission wrapper. Preserve each caller's distinct source, Map shape and key
coordinate; equal member types do not imply equal keys or a common deep shape.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-private-entry-get-development-kernel \
  --exact sh08-private-entry-get-entry-controls-development-kernel \
  --exact sh08-private-entry-get-nonparameter-fallback-development-kernel \
  --exact sh08-private-entry-get-cold-inference-development-kernel \
  --exact body-private-entry-get-development-kernel \
  --exact current-gravity-private-entry-get-development-kernel
```

These normalized checks are development feedback. The
complete binding-use/call census, visibility, nonvacuity, parameter re-entry
and work bounds are compiler obligations, not a prerequisite authority record
for editing the implementation. Public/escaping helpers, unproved keys,
function-parameter recursion and general deep structural inference remain
outside this increment. Recursive callers can supply independent literal
arguments; ordinary nested loops are not function re-entry.

For a stable changed integration, run the accepted Gravity/QST pair and one
real missing-key rejection through the existing source composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-private-entry-span-authenticated-composition --fail-fast
```

It preserves W5's original unannotated span helper, reuses accepted products
for C8/lowering/runtime checks, and does not migrate W5 or admit a compiler
revision. Do not repeat unchanged upstream construction for every mutant or
subsequent documentation edit.

### Module-local literal initialization and reads

Edit the typed emitter and Gravity runtime for this boundary, not the seed's
literal-data index. A closed execution initializes supported module-local
literal definitions once, in source root order, before entry execution. Reads
use that instance's binding-ID pool through calls and `recur`; direct expression
evaluation does not implicitly initialize a module or accept a caller's pool.
Unquoted collection literals remain evaluated constructors, not quoted DATA.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-module-literal-runtime-development-kernel \
  --exact current-gravity-module-literal-emitter-development-kernel
```

For a stable changed integration, select
`current-gravity-module-literal-modes-authenticated-composition` separately in
the same namespace. It preserves W5's literal definition and getter, checks C8
legality before execution, and reuses accepted products for malformed-input
checks. Initialization allocation belongs to the original constructor; reads
do not add allocation. Initializer calls, references/aliases, mutation,
imported initialization and other arbitrary initializer code remain outside
this literal-only implementation. A derived
constructor-occurrence count is not an allocation-event measurement. The
fixture's explicit allocation allowance does not migrate the full W5 module,
whose current empty effect declaration remains a separate integration issue.

### Map key membership

STD3's direct Map `contains?` query distinguishes key presence from value
truthiness. A present entry is present even when its value is false or nil.
This development slice accepts independently established Map types and dynamic
String or Keyword keys; it does not require a literal constructor, a known key
set or a deep value-shape proof.

Edit the existing C7 engine, effect owner, typed emitter and Gravity runtime.
Use the existing focused checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-map-contains-development-kernel \
  --exact body-map-contains-development-kernel \
  --exact current-gravity-map-contains-lowering-development-kernel \
  --exact current-gravity-map-contains-runtime-development-kernel --fail-fast
```

Map and key evaluate once, left-to-right, in non-tail positions. Never transfers
before the lookup. The query adds no allocation, effect or capability; operand
effects and errors remain visible. String and Keyword keys compare by value,
without coercion or Unicode normalization. Stored values remain uninspected.
Runtime instructions retain the existing closed binary builtin-call shape;
Map and key-domain checks precede lookup.

These normalized kernels provide development feedback, not authenticated source
evidence. Before accepting a changed composition, separately exercise real
paired source through current typing, effects, lowering and execution, with
fresh public reconstruction and meaningful altered-fact rejections:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-contains-authenticated-composition --fail-fast
```

Do not replay unchanged earlier source proofs for this query. Other key domains,
vector/set membership, presence-based type narrowing, first-class invocation,
full collection/profile support and authoritative migration remain separate
work, not language-level prohibitions inferred from this finite slice.

### Unary truthiness complement

STD2's unary `not` complements L2 truthiness: false and nil produce true; other
established normally returning values produce false. This is not equality's
six-family restriction or an implicit conversion in match guards. L7 guards
still require Bool; an explicit `not` supplies a Bool result.

Edit the existing C7 engine, effect owner and typed emitter. The existing
runtime conditional already implements the needed evaluation semantics:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-not-development-kernel \
  --exact body-not-development-kernel \
  --exact current-gravity-not-lowering-development-kernel \
  --exact current-gravity-not-existing-if-runtime-development-kernel --fail-fast
```

The operand is evaluated once, in non-tail position. Lowering uses the existing
conditional with literal false/true branches, not a new runtime instruction or
seed primitive. Never transfers before either branch. Operand allocation and
errors remain visible; `not` adds none. Empty text and collections, zero and
Characters are truthy. Their existing unsupported source or lowering boundaries
remain separate from `not`; they are not new truthiness prohibitions.

Keep the exact binding, unary call/source/order, operand type and Bool/Never
result joins, fresh type/effect reconstruction and cumulative work limits.
These kernels are normalized development feedback. Real source composition,
including Bool match guards and rejected non-tail recur, remains a separate
integration check before accepting the executable slice:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-not-authenticated-composition --fail-fast
```

The paired source uses `(not (= tag :rejected))` in a Bool guard and retains
raw non-Bool guard rejection. For local-call oracles, use the canonical direct
same-module call guidance below. First-class `not`, other predicates, profile/provider
completion and authoritative migration are not established by these checks.

### Proven vector indexing

The same C7 engine, effect owner, typed emitter and runtime own the bounded
two-argument `(nth vector index)` implementation. Start with:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-vector-nth-development-kernel \
  --exact sh08-vector-nth-selected-shape-development-kernel \
  --exact sh08-vector-nth-selected-coordinate-development-kernel \
  --exact body-vector-nth-development-kernel \
  --exact current-gravity-vector-nth-lowering-development-kernel \
  --exact current-gravity-vector-nth-runtime-development-kernel
```

Normal selection requires an established positional vector type and an Integer
literal satisfying `0 <= index < length`. C7 selects the corresponding actual
element type, including from heterogeneous vectors. A Nil element is a valid
value, not missing type evidence or an out-of-bounds fallback. Scalar selection
does not require structural-pattern analysis of unrelated members. A later
lookup or pattern consumer requests the selected value's deeper ancestry only
when it needs that evidence. Structural limits on the selected value do not
justify requiring unrelated vector members to be valid patterns. Preserve the
vector/index/source and binding/call joins while projecting the selected path.

Transferring operands produce Never without an executed bounds proof or a
selected value. Other non-Never operands still require their supported type
and index facts. Operands execute once, left to right, until control transfers;
the query preserves selected value identity and adds no collection allocation.
Operand allocation and error effects remain visible.

The bounds fact is not authorization to erase checks. The development runtime
retains vector, integer and range guards before access. Static invalid bounds
use the owning safety diagnostic; malformed development instructions use the
existing contained rejection. That transport rejection does not define a
valid-source dynamic-index failure API. Dynamic homogeneous indexing still
needs an implemented bounds-failure contract; heterogeneous dynamic indexing
also needs a result-typing rule. Neither host exceptions, nil fallback nor an
unimplemented Result path may silently fill those gaps.

Validate the affected real source-to-type/effect/lowering/runtime composition
when the bounds/type interface or emitted instruction changes. Keep that
costlier integration separate from the normalized edit-loop checks above and
from authoritative migration:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-nth-authenticated-composition \
  --timing-receipt /tmp/sh01-vector-nth-composition.edn
```

This finite operation does not establish full
collection, profile, provider, target or self-hosting support.

### Bounded counter successor

The same C7, effect, emitter and runtime owners implement the finite
proof-required Integer `inc` rule. Use the existing direct tests:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-bounded-successor-development-kernel \
  --exact sh08-bounded-successor-multiple-backedges-development-kernel \
  --exact sh08-bounded-successor-extent-bound-development-kernel \
  --exact sh08-bounded-successor-guard-relations-development-kernel \
  --exact body-bounded-successor-development-kernel \
  --exact current-gravity-bounded-successor-lowering-development-kernel \
  --exact current-gravity-bounded-successor-runtime-development-kernel \
  --exact current-gravity-bounded-successor-fixtures-development-kernel
```

The supported source relation is a zero-initialized immutable Integer counter,
a dominating strict `<` comparison against runtime `count` of the same
immutable vector, and a unit successor on every incoming backedge. C7 checks
the original target, slot, source, guard, count and complete transfer census.
The established vector extent `N` is bounded by the existing 1024-child limit.
The invariant `0 <= i <= N` and true-edge fact `i < N` prove the successor
remains within the finite range. A zero-length vector has an unsatisfiable
increment edge and a normal zero-iteration exit; this does not manufacture a
Never result or an executed increment.

Integer typing remains separate from the numeric-mode and range records. It
does not silently select I64 or arbitrary-precision arithmetic. Count still
executes in the original condition; no folding, hoisting, termination claim or
check elision follows. The runtime retains integer, current-counter and range
guards before addition. Hostile development-IR rejection does not define a
source overflow-error API. The successor adds no allocation or error effect;
operand effects and control transfer remain visible.

Changes to this numeric/control relation or its emitted instruction require
the affected source-to-type/effect/lowering/runtime composition, separately
from these normalized kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-bounded-successor-authenticated-composition \
  --timing-receipt /tmp/sh01-bounded-successor-composition.edn
```

General arithmetic, arbitrary starts or steps,
unproved extents, fixed-width checked errors, BigInt support, target admission
and authoritative migration remain separate work. This rule is a supported
finite analysis domain, not a prohibition on other documented numeric forms.

### Guarded loop vector indexing

The C7 extension to proven vector indexing consumes the existing bounded
counter invariant. It does not run a second loop solver. Start with the
current producer checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-vector-loop-nth-development-kernel \
  --exact sh08-vector-loop-nth-cold-inference-development-kernel \
  --exact sh08-vector-loop-nth-origin-scope-development-kernel \
  --exact sh08-vector-loop-nth-selected-shape-development-kernel
```

These normalized checks cover a nonempty homogeneous established vector,
its exact immutable origin, the original Integer loop slot, and access beneath
the strict count guard's true edge. The element result is its established
homogeneous type. The successor's result may equal the extent; that does not
make `(nth values (inc i))` safe. Access needs the separate `0 <= i < N` fact.

The cold-inference check starts without a completed successor descriptor and
tests both recur/throw branch orders and current facts overriding prior-round
facts. This is owning type-inference feedback, not a real-source composition
or authoritative proof. For effect reconstruction, lowering and paired fixture
syntax, use the affected consumer kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact local-throw-call-effect-equations-development-kernel \
  --exact body-vector-loop-nth-development-kernel \
  --exact current-gravity-vector-loop-nth-lowering-development-kernel \
  --exact current-gravity-vector-loop-nth-fixtures-development-kernel
```

These checks include exact source/call/edge joins, malformed bounds evidence,
operand effects and work-limit boundaries. The local throwing-call kernel also
exercises the actual loop-effect derivation and throwing-call verifier together;
checked loop/recur nodes must not be excluded by a stale operation whitelist.
Fixture parsing alone does not establish rejection or execution.
The existing runtime evaluates index expressions and retains vector, integer
and bounds guards; no runtime change or check elision is implied here.

Changes to the producer/consumer relation need its affected composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-loop-nth-authenticated-composition \
  --timing-receipt /tmp/sh01-vector-loop-nth-composition.edn
```

This is a separate, expensive source-to-runtime integration check, not the
default edit-to-test command above. Reuse its observed result for unchanged
source/test/fixture bytes; do not repeat it just to repair a reporting label or
obtain another green aggregate. Failed and partial results stay accurately
recorded. No authoritative migration or self-hosting claim follows from it.

The retained full composition completed its type/lowering/runtime checks but
failed C8's throwing-call compatibility boundary. After changing that consumer
alone, select its affected single-source test instead of repeating those
unchanged stages:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-vector-loop-nth-throw-control-authenticated-composition \
  --timing-receipt /tmp/sh09-vector-loop-nth-throw-control-composition.edn
```

It constructs one fresh unchanged `.gravity` input through the existing type
helper, then checks the current C8 graph, original loop/effect equations,
String propagation and catch residual, and coherently altered graph rejection.
This affected test passed 24 assertions after the whitelist correction; the
earlier failed aggregate remains failed. Its one-source C8 result complements
the unchanged source/type/lowering/runtime evidence rather than relabeling it.

Empty vectors without an element type, heterogeneous result joins, arbitrary
dynamic indices, deep selected-shape propagation and dynamic-bounds failure
APIs remain separate work. These implementation limits are not language bans.

### Guarded loop Vector get

The same existing C7, C8 and emitter owners have a distinct two-operand `get`
variant for the bounded loop domain above. It needs a nonempty homogeneous
Vector, the original Integer counter slot, the same immutable Vector origin as
the count guard, complete backedge evidence and access below the strict true
edge. It does not infer bounds from a member type or from an equal-shaped but
different Vector. An alias may preserve an origin; a new constructor does not.

Use the direct component checks while editing:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-vector-loop-get-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-vector-loop-get-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-loop-get-lowering-development-kernel --fail-fast
```

The source operation, compatibility binding, descriptor, call fact and ledger
row remain `get`-specific. Only after reconstructing those facts and the bounds
proof does lowering use the existing checked `nth` development instruction,
with Vector-then-index evaluation. Runtime guards remain; there is no check
elision or new runtime path. C8 checks the complete ordered `nth`/Vector-`get`
ledger census, including missing, duplicate and orphan rows. The local binary
lowering path retains its own logical reservation and completed operand prefix
on failure; an outer zero-budget rejection does not enter that helper.

Map lookup, explicit defaults and the prior Never/Unknown routing remain
separate. This subset does not grant general Vector `get`, literal-only access
outside this proof, arbitrary dynamic indices, heterogeneous results or an
out-of-range/absent-result policy. The paired `local-runtime-vector-loop-get`
fixtures and wrong-origin, heterogeneous, nonstrict and outside-loop negatives
belong to the affected source composition, not the edit/test inner loop.
Actual source outcomes are recorded in the backlog; normalized component
checks alone do not establish source execution or authoritative migration.

When this producer/consumer behavior changes, run the affected composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-loop-get-authenticated-composition --fail-fast
```

This is not the default command for every edit. It checks the paired source,
fresh verification, typed lowering, effects and function-table execution.
Do not repeat an unchanged composition to repair a reporting label or refresh
migration status.

### Binary unit steps in bounded loops

For exact `(+ original-counter 1)` inside the bounded strict-loop domain, edit
the existing C7 engine, `gravity/effects.gravity`, P15 emitter and
`gravity/runtime.gravity`. Start with the owning component, not a corpus proof:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-bounded-unit-addition-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-bounded-unit-addition-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-bounded-unit-addition-lowering-development-kernel \
  --exact current-gravity-bounded-unit-addition-runtime-development-kernel --fail-fast
```

Use `sh08-bounded-unit-addition-cold-two-loop-development-kernel` for the cold
mixed-step/get/lexical-type dependency, and
`sh08-bounded-unit-access-current-lexical-development-kernel` for exact immutable
initializer propagation and budget guards. Neither substitutes for source
composition. The cold source-prefix selector is a more expensive diagnostic
tool when real frontend shape matters; it is not the default inner loop.

The complete incoming census reconstructs each actual inc or binary unit step;
all-inc v1 and mixed v2 evidence are not interchangeable. Both binary operands
remain evaluated in order, runtime guards remain, and Never does not establish
a numeric invariant. This is not general addition or ordinary-call induction.
When these producer/consumer behaviors change, the existing affected integration
selector is `current-gravity-bounded-unit-addition-authenticated-composition`.
Its recorded result and limitations are in the backlog. Do not replay it for
unchanged source or to refresh authoritative migration status.

### Direct integral ordering

The C7 engine, `gravity/effects.gravity` and existing emitter own direct binary
`<`, `<=`, `>` and `>=` typing, effect reconstruction and lowering. The runtime
already evaluates these operations. Start with the current-source kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-integral-ordering-development-kernel \
  --exact body-integral-ordering-development-kernel \
  --exact current-gravity-integral-ordering-lowering-development-kernel \
  --exact current-gravity-integral-ordering-runtime-development-kernel \
  --exact current-gravity-integral-ordering-fixtures-development-kernel
```

This finite rule requires exactly two operands with the same established
integral type: Integer, I8/I16/I32/I64 or U8/U16/U32/U64. Integer literals are
not implicitly converted to fixed-width operands. Normally invoked operations
return Bool. Never operands preserve source-order control transfer; other
operands still require legality checks even when invocation cannot occur.

An applied intrinsic descriptor belongs to one authenticated operator
occurrence, not a fabricated function or generally callable binding type.
Keep the existing C6 call/edge shapes and function census. Fresh C7 verification
reconstructs the descriptor, numeric-mode constraint and call fact; effects
and lowering independently join the actual binding, source, order and types.
The proof-required safety rule is finite same-integral total ordering without
conversion, overflow or rounding. Local shadowed functions retain ordinary
dispatch. Numeric semantics belongs to `gravity.math`; the existing core
catalog remains compatibility resolution, not a newly implemented math import.
The actual source composition uses unqualified comparison operators. Current
C5 does not admit `gravity.core/>` as a qualified variable spelling; do not
infer that source support from normalized intrinsic-descriptor tests.

Run one affected paired source composition separately when those facts or
execution interfaces change:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integral-ordering-authenticated-composition --fail-fast
```

It combines ordering, a computed map guard, shadowing and typed catches. Reuse
each fresh product within that test; do not replay unchanged guard or match
proof matrices. Empty direct comparison effects do not establish allocation-
free wrapper execution: the fixture declares allocation, and constructor
allocation inference is a separate outstanding slice. Mixed numeric families,
floating/decimal ordering, conversions, escaped operators and complete
profile/provider, target, standard-library or migration acceptance are not
established by these development checks.

### Canonical direct same-module calls

Edit the existing Gravity emitter and runtime for fixed-arity, acyclic calls
between named functions in one actual module. The lowerer joins canonical C6
call/edge records, resolved binding and definition/function identities, ordered
arguments and C7 call facts/constraints. A qualified current-module spelling
can select the same resolved binding; spelling alone never selects executable
code. A `Never` argument makes the call non-returning without changing the
callee's declared normal return type.

When writing composition oracles, current ordinary C7 local-call facts omit
`:dispatch`. Check the actual canonical caller/callee edge and call-fact joins.
The separate `:direct-local-function` higher-order-call tag is not an ordinary
direct-call classification. This is an artifact-shape distinction, not a
missing executable behavior or a reason to reconstruct upstream proofs.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-direct-call-lowering-development-kernel \
  --exact current-gravity-direct-call-runtime-development-kernel
# Affected source -> current typed call lowering -> Gravity runtime:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-direct-call-authenticated-composition --fail-fast
```

The closed plan supplies its existing function table to the private Gravity
evaluator context. Legacy evaluator/helper entrypoints use an empty table;
public plan execution keeps its existing record shape. Validate the target,
fixed parameter vector and arity before argument effects. Evaluate arguments
once, left-to-right in the caller environment, then bind values in a fresh
callee environment. A thrown or rejected argument prevents later arguments and
invocation. Callee output and control return unchanged to the caller.

An active call path rejects cycles before argument evaluation. The current
256-entry path ceiling is a bounded consumer limitation, not a new language
legality rule or proof of host-stack fit, termination, total runtime cost or
memory use. Shallow target-body shape checks are not whole-plan validation.
Do not add host lookup, ambient callbacks or host exception translation to
implement these guest calls. Existing cross-plan continuation machinery is
separate and unchanged. Imports, callable parameters, closures, variadics and
recursive components remain later slices, as do complete profile/provider
legality, whole-runtime compilation and authoritative migration.
The supported declarations are those accepted by current C7. Qualified
references work, but qualified declaration names currently fail C7's
simple-binding-name versus definition-name equality before this lowerer.
That is an implementation gap to resolve in C7, not a new L2/L3 prohibition;
changing the call consumer alone would not close it.

The focused batch passed nine tests/592 assertions. The sole affected paired
composition passed one test/214 assertions in about 869 seconds of execution
plus eight seconds of loading. It builds each accepted input once per extension
for thirteen-function assembly and execution, then exercises the current C5
unknown-call and C7 arity, type and nonfunction-call boundaries. Current C7
verification runs once per public lowering operation; input tampering rejects
and every guest execution is observed for zero seed throw transport. The
sixteen-function runtime projection is not whole-module compilation. Counts
overlap and timings describe different workloads, not a speedup comparison.

### Canonical loop and function-target execution

Edit the existing Gravity emitter and runtime for canonical `loop`/`recur`
lowering and execution. Reuse C6's target, sequential binding and transfer
records and C7's slot types and constraint rows; do not add an admission input
or refresh historical migration pins for this work.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-loop-lowering-development-kernel \
  --exact current-gravity-loop-runtime-development-kernel \
  --exact current-gravity-function-recur-lowering-development-kernel \
  --exact current-gravity-function-recur-runtime-development-kernel
# Affected source -> current typed lowering -> Gravity runtime composition:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-loop-authenticated-composition --fail-fast
```

Initializers run once, sequentially. Recur arguments run left-to-right in the
old environment; only after they all complete are the slots replaced together.
A separate private control position carries the target ID, so ordinary or
thrown data that resembles a recur instruction stays inert. The matching loop
consumes its backedge. Non-tail positions, `try` regions and call boundaries
retain the existing C6 transfer restrictions; a nested loop establishes its
own target. Function-target recur uses the current function's declared parameter
slots, not types guessed from a caller. Its matching activation replaces all
parameters together in a fresh local environment and retains the same call
path. Function recur is a backedge, not another ordinary call. The separate
declared direct-self path below uses real ordinary activations; it does not
turn those calls into recur or provide tail-call optimization.

The function-target increment passed its six-input affected composition: one
test/276 assertions. Use its existing selector when validating that changed
boundary:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-function-recur-authenticated-composition --fail-fast
```

That selector constructs six source products: the existing annotated function
pair, the new executable function-recur pair, and a representative rejected
slot-type pair. It includes a terminating zero-parameter case, parameter swaps,
shadowing, nested loops, and throws before and after a restart. The selected run
took 660.745 seconds, including product construction and fresh public
verification. That is not a phase-cost or speedup measurement. Do not repeat
this unchanged composition, the old C6/C7 matrix, or migration proofs to debug
these development kernels or correct documentation.

That 276-assertion result belongs to commit `55b7dca9`, before a test-only
construction cleanup. Assembly already lowers every definition, so the revised
selector checks every assembled function and retains one representative
standalone compile per accepted input. It expects 16 fresh public C7
verifications instead of 36; every retained public call still verifies freshly.
The six SH01 type-only callers also omit the unused SH09 binding seal. The C8
helper still adds its original seal to the same fresh products. Production and
fixture bytes did not change. The old composition was not replayed merely to
renew evidence for this harness refactor, and no elapsed-time improvement or
new composition pass is claimed for the revised selector.

When changing that helper or test schedule, use the cheap preservation checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact fresh-type-products-construction-development-kernel \
  --exact fresh-products-seal-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact function-recur-composition-work-development-kernel \
  --exact typed-lowering-public-orchestration-development-kernel
```

The helper checks use mocked control-flow DATA, not accepted compiler evidence.
The schedule check reads the actual test source. Neither is a substitute for
affected composition when production behavior or fixtures change.

One remaining cost candidate is compiler-code plan construction, distinct from
semantic product construction and verification. The existing `00581` typed
lowering operation constructs both current C7 and emitter plans before each
fresh public C7 verification. Thus the Map membership selector's nine public
operations imply eighteen code-plan constructions if that path completes.
`00580` parses and compiles those definitions and canonicalizes the plan for
its identity. This is a static call count, not an observed phase-time share.

At the next already-required changed composition or migration run, bounded
observations around code-plan construction and fresh verification can determine
whether that repetition is material. Do not launch a separate profiling run or
add another test lane to establish this. If measurements justify a change,
consider reuse of immutable executable code plans for identical captured
compiler inputs, with exact source/emitter/toolchain binding and invalidation.
Never reuse semantic products, accepted results or proof authority. Preserve
canonical plan bytes, public results, source-change reconstruction and drift
rejection, and one fresh verifier invocation for every public operation,
including hostile inputs. Product representation redesign remains separate
unless actual measurements identify it as the bottleneck; no speedup is
established by this follow-up proposal.

The focused checks exercise normalized producer-shaped data, including rows
constructed by the current C7 constraint producer. They are not authenticated
source evidence. The separate paired selector constructs each source input
once and reuses its products for assembly, lowering, execution and negative
checks. Run it when this composition changes, not to replay unchanged C6
tail/arity proofs or to repair a test-only assertion. Passing results are scoped
to the actual source and implementation revision tested.

Represented integer values remain generic Integer, not guessed I64 values.
Unelaborated decimal descriptors still reject at this consumer boundary;
that is not a language-level prohibition on decimal loops. Arbitrary recursive
calls, full effect/profile legality, whole-runtime compilation and authoritative
migration remain separate work. The current twenty-two-function runtime
projection and active-target bound do not establish
termination, total cost, memory bounds or host-stack fit.

If changing C7's source-error composition, edit
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` and run
the existing owning kernels before the affected integration check:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact loop-lexical-source-error-development-kernel \
  --exact loop-handler-source-error-development-kernel \
  --exact loop-called-source-error-development-kernel
```

These checks cover lexical error unions, handler-only activation and called
bodies. They do not add control backedges or establish termination. A failure
before lowering must be diagnosed at its owning boundary rather than hidden
by dropping the source case or weakening identity validation.

For the typed lowerer's source-definition join, use:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-raw-root-source-development-kernel
```

Public definitions retain raw expanded source values; canonical C6 values may
instead contain exact occurrence-linked numeric descriptors. The lowerer pairs
all verified raw roots with C6 root IDs in source order, checks root coverage
and definition/header coordinates, and preserves full supplied-definition and
function-table equality. Only typed C6 nodes drive emission. The explicit host
decimal presentation helper is not canonical compiler input. Changing this
common join requires an affected real multi-function composition as well as
the focused malformed-root and numeric-diagnostic controls; it does not require
refreshing authoritative migration pins.

The preceding loop-only source-join candidate passed this bounded composition
(one test/128 assertions):

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-loop-source-join-authenticated-composition --fail-fast
```

It selects one multi-function positive, both decimal boundary cases and three
type-rejection cases. Its 586.919-second selected time includes real product
construction and public verification, not merely scheduling. It is not a full
language, target, runtime-module or authoritative migration proof. Do not rerun
this expensive composition for unchanged code or documentation-only edits.

### Current Gravity emitter throw lowering

Edit `bootstrap/gravity/p15_s23/emitter.gravity` for the Gravity-owned
instruction lowerer. Its one-operand `throw` becomes `{:op :throw :value ...}`;
wrong arity rejects before operand compilation. Instruction summaries traverse
the operand. Products containing throws explicitly leave effect inference
pending and legality unchecked, rather than reporting an empty inferred effect.
Ordinary products and original declaration records retain their existing shape.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-emitter-throw-development-kernel
```

This check compiles and executes the actual current Gravity emitter source
through the existing raw three-argument developer interface. It lowers the
unchanged compiler-accepted paired throw fixture and observes normal return and
escaping payload through the temporary seed executor. Reuse the unchanged
fixture's upstream evidence; rerun affected compiler composition only when its
behavior or interface changes. Do not refresh old admission pins to run this
development check. Gravity-owned runtime error transfer, handlers, target error
lowering and authoritative migration remain separate gates.

### Current Gravity runtime throw execution

Edit `bootstrap/gravity/src/gravity/runtime.gravity` for the local closed
evaluator. The namespace is `gravity.runtime`; the older
`bootstrap/gravity/p15_s23/runtime.gravity` retains formatting, historical
catalog DATA, B51 machine helpers and four direct compatibility forwarders.
Its internal result is `[value output control]`, with `:normal`, `:thrown` and
`:rejected` kept separate from payload DATA. Argument, binding, sequence and
conditional evaluation propagate a transfer before doing later work. Ordinary
top-level success retains its existing seven-key record and output order;
escaping errors and rejected instructions do not publish normal completion.

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-runtime-throw-development-kernel \
  --exact current-gravity-emitter-throw-development-kernel
```

The runtime check freshly compiles the entire current `gravity.runtime` file,
checks its 22 function names/arities and call closure, and retains its actual
source hash separately from the compatibility file's identity. It no longer
selects or concatenates function forms. No function is spliced into an old plan,
and no admission pin is refreshed.

The affected composition lowers the unchanged accepted paired throw fixture
with the current Gravity emitter and evaluates its returned instruction with
the current Gravity evaluator. The seed's private throw transport must not
implement guest error transfer. The generic seed interpreter and primitive
boundary remain. Old admitted packet validation still excludes throw; these
development checks do not widen it or establish a target exception ABI,
handlers, resource cleanup, complete profile legality or authoritative migration.

### Runtime namespace boundary

`gravity.runtime` owns evaluator traversal, guest activation, typed handler
selection and loop/function-target transfer. It has no module imports. The
compatibility module depends on this leaf, never the reverse. L2/L3 control and
module semantics, C5 origins/privacy, C15 diagnostics, P4/R4 hosted boundaries
and BOOT3's development/migration distinction govern this boundary.

The leaf exports only these existing entrypoints; its other 18 functions are
private:

| Entry point | Arguments | Result |
| --- | --- | --- |
| `p15-s23-runtime-execute-closed-plan` | plan | Normal, thrown or rejected execution record. |
| `p15-s23-runtime-evaluate-closed-instruction` | instruction, environment, output | Internal value/output/control tuple. |
| `p15-s23-runtime-evaluate-in-context` | instruction, environment, output, functions, path | Internal tuple with explicit guest function context. |
| `p15-s23-runtime-activate-function` | function, values, output, functions, path | Internal tuple with fresh activation locals. |

Inputs are development instruction/function DATA, not newly admitted typed
programs. Payloads remain DATA; internal `:recur` control additionally carries
its target identity. Normal public completion preserves the seven-key record.
The evaluator retains `P15S23X002` rejection behavior; module-development
reconstruction/routing failures remain `BOOT2006`. Existing diagnostics and
control representations do not become a target error ABI.

The leaf declares managed allocation. Output is accumulated as a string, not
written to a live stdout provider. Source declarations do not grant allocator,
provider or host authority. The generic Clojure interpreter and primitive
boundary remain. Focused checks compile the actual whole leaf directly;
compatibility source/export/import/routing changes require the separate actual
two-file composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-runtime-extraction-authenticated-composition
```

That composition uses real ordinary C5 root/leaf artifacts and an independent
leaf plan through the existing module interface. Do not merge function tables,
rewrite namespaces or inject its bundle into the stale pinned runtime adapter.
It is development integration, not checked imported-user legality, whole-runtime
conformance, authoritative migration or seed retirement.

The present module consumer does more than these foundational contracts
prescribe: `00580` calls full public C5 verification, including SH05 and envelope
verification, during every public bundle reconstruction. That is an existing
tool implementation choice, not a prerequisite imposed by BOOT3 or TEST2 on
every development invocation. Do not rerun it for unrelated semantic edits.
Verifier entry counts are not replay or allocation counts: existing permitted
proof-transaction object reuse may apply within an invocation.
If this cost blocks a needed integration decision, first distinguish initial
C5 construction, bundle construction, public reconstruction and execution;
then locate replay, hashing or equality cost at the owning boundary. Compare
source-derived semantic reconstruction in this same interface with sharing
immutable products within one invocation. Either approach must still reject
source/export/private/profile/origin/plan/route substitutions and preserve
traceable inputs, canonical identities and stale-authority status. Accepting
rehashed maps or `:passed` fields is not a substitute. No redesign, proof cache,
new runner or additional admission lane is selected here.

### Single-source seed throw execution

The temporary seed lowers one-operand `throw` in the existing three-argument
compiler-artifact plan interface. It evaluates the operand once and transfers
out without a normal value. The private seed transport retains the original
payload as DATA, actual plan source hash and module; it does not throw the
payload object itself or classify arbitrary host errors as source throws.
Nested throws escape before an outer transfer can occur. Ordinary control-key
maps remain DATA, and the existing host-error and recur boundaries remain in
force. Malformed source arity and missing operand instructions reject before
operand work.

The owning seed files are `bootstrap-p15/00575`, `00576`, `00590` and `00592`,
with operand summary traversal in `foundation/00555`. The existing `00580` raw
plan builder marks throw-containing plans as Clojure-seed lowering with pending
effect inference and unchecked legality; source declarations and plan identity
remain present. Unchanged no-throw plans retain their previous shape. The
existing module-source and module-bundle checks reject executable throw in the
four-argument module route, while preserving quoted throw DATA.

Run the focused transfer checks without constructing upstream proof products:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact seed-throw-lowering-and-arity-development-kernel \
  --exact seed-throw-transport-and-nesting-development-kernel \
  --exact seed-throw-evaluation-order-development-kernel \
  --exact seed-throw-host-error-and-module-fences-development-kernel
```

Use `direct-typed-throw-annotated-seed-execution` in the existing
`gravity.self-hosting.sh08-loop-recur-type-test` namespace to execute both
unchanged typed-throw fixtures. It reuses the accepted compiler composition for
those exact bytes, then observes both the ordinary branch and a real escaping
throw. It does not rerun the unchanged compiler proof. This is explicit seed
execution, not Gravity-authored emitter/runtime ownership, imported-error
execution, a public JVM exception ABI, deterministic resource cleanup, complete
profile legality or authoritative migration.

### Imported-call type development

The owning C7 implementation is
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.
Use its existing `sh08-function-type-core-artifact` and
`sh08-verify-function-type-result` interfaces. Extend the existing authenticated
caller request with actual callee C7 requests, not copied signatures or stored
verification results:

```clojure
(assoc caller-c7-request
  :module-type-inputs
  {:artifact :gravity/sh08-module-type-inputs
   :schema-version 1
   :module-requests [callee-c7-request]})
```

Each callee request must refer to the same real C5 module artifact already
supplied to the caller's resolution step. C7 freshly derives the selected
exported signature and joins its original binding to the projected C6 function.
Imported parameters are immutable premises, not variables specialized by the
caller. Missing typing inputs and incomplete signatures require a C7 annotation
diagnostic; they do not trigger ambient module discovery or dynamic fallback.
The scalar imported-call boundary accepts complete types inferred within the
producer as well as checked annotations. Any annotation that is present must
still agree with the derived signature. An unresolved producer cannot be
completed from caller arguments. The initial paired fixtures cover annotated
I64, Bool and String direct calls in the same `:meta/:jvm` profile/target; the
producer-inference increment is tracked separately in the slice backlog.
Only the existing literal/atomic scalar type catalogs are supported at this
boundary. Composite or function-valued imported signatures, cross-profile
typing, generic specialization and runtime module linking remain separate work,
not language-level prohibitions on those features.

Start with the existing direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-imported-function-types-development-kernel \
  --exact sh08-imported-inferred-signatures-development-kernel
```

The affected real compositions are
`sh08-imported-function-authenticated-source-composition` for the initial
annotated boundary and
`sh08-imported-inferred-signature-authenticated-source-composition` for
producer-local inference, in the same namespace. Run the one affected by the
change, not both merely to refresh an aggregate. They exercise the ordinary
C5 -> C6 -> C7 path, not authoritative module migration.
The imported result is type-only: declared effects/capabilities are preserved,
body-effect inference and thrown-error analysis remain pending. The legacy C8
type-only adapter rejects it with `:imported-body-effects-pending`; the separate
body-analysis interface below requires the complete original producer requests.
Type checking does not authorize target
lowering, linking, effect execution or runtime behavior.

### Body-effect development

The body-analysis owner is `bootstrap/gravity/src/gravity/effects.gravity`.
The existing `gravity.compiler.c8-effect-checker-engine` module forwards its
`sh09-build-authenticated-body-effect-result [typed verification binding]` and
`sh09-verify-authenticated-body-effect-result [typed verification binding candidate]`
entrypoints to that owner. Its older pure-result and compiler-read interfaces
remain separate. A compiler-read allowance is not evidence that a body is pure.

Use the actual canonical core retained by the existing C7 request and binding,
not a supplied effect summary. The local first-order subset derives effects
from complete node coverage and acyclic local calls. Function creation retains
latent body effects without executing the body. Conditional branches and match
guards contribute conservatively to the effect union while their evaluation
regions remain conditional. Declared effects remain allowances; unused
nonempty declarations do not become inferred actions. Thrown-error facts remain
pending, and this analysis does not establish full effect legality.

Run the direct Gravity kernels first:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-operation-development-kernel \
  --exact body-effect-coverage-development-kernel \
  --exact body-effect-guard-development-kernel \
  --exact body-effect-final-input-fences-development-kernel \
  --exact body-effect-call-metadata-development-kernel \
  --exact body-effect-import-origin-development-kernel \
  --exact body-effect-import-bounds-development-kernel \
  --exact body-effect-foreign-summary-development-kernel
```

These use normalized graph DATA for development feedback, not authenticated
upstream evidence. Changes to input joins or graph semantics also need affected
real C5/C6 -> C7 -> effects composition, using fresh C7 verification and the
complete original request in its binding. Do not reuse an accepted result as
that composition's input authority.

The current local-body composition also exercises the real public C8 bundle:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-authenticated-paired-composition --fail-fast
```

That test derives paired C5/C6 and C7 products, freshly verifies C7, and checks
the leaf before constructing the real C8 module bundle. It exercises the public
constructor and verifier without changing the bundle's fresh validation rules.
Repeat that public-bundle boundary when C8 forwarding/authentication code,
namespace dependencies, exports/arities, qualified callsite placement, or
bundle construction/routing changes. Private leaf semantics alone do not
require rebuilding this expensive boundary if affected real upstream-to-leaf
composition and unchanged forwarding/route checks cover the change. Source and
plan identities must still describe the current bytes: prior route evidence is
not proof of a new bundle identity. Keep post-run reject-only or diagnostic
corrections distinct from the source snapshot exercised by an earlier run.
Imported bodies, higher-order or general recursive propagation, unsupported operations,
throw/try composition, general provider/grant policy and authoritative effect
identities remain outside this local subset. The legacy imported-type-only
input still rejects with `:imported-body-effects-pending`. Record actual test
results separately from migration, target support and self-hosting status.

### Direct imported body analysis

The same `gravity.effects` body interface consumes the existing imported C7
result and fresh verification, with the original four-key request retained in
its binding. Each actual producer request must match the caller's retained C5
module input and C7 producer identity. Analysis follows the original public
export through its projected definition to the function and body; private
producer-local callees are analyzed within their own module, not exported as
new APIs. No declaration-only effect summary or synthetic accepted producer
result replaces the body.

Caller and producer graphs have separate module coordinates. A foreign body
dependency records both producer artifact IDs, the caller's imported binding,
the producer's projected definition binding, function and body. The original
C5 export identity remains in the validated origin join. Whole producer graphs enter the effect
identity; caller node coverage still covers only the caller. The existing
aggregate node/function limits apply across this invocation. The deferred-work
budget counts node-analysis attempts, not all VM operations or a CPU guarantee.
Legality and thrown-error facts remain pending. C7's original type-only facts
and pending labels are preserved as upstream evidence, not rewritten as if C7
performed effect analysis. The older compiler-read request validator continues
requiring exactly three keys.

Run affected real producer/consumer composition separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-imported-authenticated-paired-composition --fail-fast
```

This uses the existing paired inferred module fixtures, actual C5/C6 products,
fresh caller C7 construction/verification and the current effects leaf. It does
not build a new public C8 bundle or claim its current identity. Use
`body-effect-public-forwarding-source-boundary` and affected module-route
kernels for unchanged-forwarder checks; the public-bundle triggers above still
apply. Direct leaf producers and scalar imported signatures are the current
boundary. Transitive imports, callable values, general recursive propagation,
throw/try, provider/grant legality, target execution and authoritative migration
remain separate work.

### Declared ordinary direct-self calls

The current development increment accepts a fully declared, monomorphic,
fixed-arity named function with one direct self-call in a singleton component,
using the supported atomic or homogeneous-List parameter and return types.
Change the existing C7 engine, effects, emitter and runtime owners together
when this boundary changes. C7 uses the complete parameter and return types
as value assumptions, then checks the actual body and every incoming and
self-call argument. A declaration is not an inferred effect or error summary.
C8 reconstructs the source-bound proof and derives effects and escaping error
types from bottom; fresh verification rejects nonleast candidate graphs even
when their equations hold.

Use the existing focused checks, selecting only the changed component:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-recursive-function-type-test \
  --exact current-gravity-declared-self-value-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-declared-self-effects-development-kernel \
  --exact current-gravity-declared-self-prefix-work-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-loop-recur-type-test \
  --exact current-gravity-declared-self-caller-loop-effects-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-declared-self-lowering-development-kernel \
  --exact current-gravity-declared-self-runtime-development-kernel --fail-fast
# Only when changing the paired-source comparison:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-typed-function-path-parity-development-kernel --fail-fast
# Affected source composition, not the ordinary edit/test inner loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-declared-self-authenticated-composition --fail-fast
```

The runtime retains target, arity and activation-path checks before argument
evaluation, once-only left-to-right operands and fresh activation locals. It
allows the current function's contiguous self-activation suffix, not an
indirect cycle through another function. The existing path limit remains;
cheap supplied-path controls do not prove host-stack safety at that depth.
An acyclic caller may use its existing explicit function recur while calling
the self-recursive function. That combination uses one effect/error and loop
summary fixed point under the unchanged cumulative work budget.

The paired source selector exercises String List traversal with transformed
`rest` and `str` arguments, empty/one/two-element results, a deeper String throw,
argument transfer and a Bool caller recur. Rejected sources cover a wrong
self-argument, a wrong body return and missing source effect allowance. Its
actual validation result is recorded in the backlog, not implied by listing
the command here. Do not replay an unchanged expensive composition to edit
this guidance.

The private inferred extension below is separate from this declared path.
This increment excludes mutual or indirect recursion, arbitrary captures and
non-core named helpers inside the self body,
and try/loop/recur inside that body. These are current implementation frontiers,
not language-wide prohibitions. It proves neither termination nor TCO, fatal
host-error normalization, full profile/provider legality, target completeness,
authoritative migration or self-hosting.

### Private ordinary direct-self inference

Work on private recursive value inference in the existing C7 engine, effects
and emitter owners; the ordinary-call runtime does not need another recursion
mechanism. The narrow development case is one private, fixed-positive-arity
function with one direct self-call. Its complete normal incoming-call census
must independently establish one monomorphic parameter vector, and its actual
return positions must provide an independent, consistent return witness.
Supported value domains are the current atomic and homogeneous-List domains,
not generic Integer, positional Vector or an unconstrained recursive type.

Use the existing direct runner and select the changed component:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-recursive-function-type-test \
  --exact current-gravity-private-self-value-projections-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-private-self-effects-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-private-self-lowering-development-kernel --fail-fast
# Affected source composition, not a prerequisite for every ordinary edit:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-private-self-authenticated-composition --fail-fast
```

The source selector uses the existing paired `private-self-values` fixtures
and rejected entry-conflict, return-conflict and missing-effect fixtures.
Listing it is not a claim that its current run passed; actual outcomes belong
in the backlog. Focused normalized kernels provide development feedback, not
source compilation or migration evidence.

Cold inference excludes recursive-result-dependent facts rather than treating
them as independent seeds. A Never callee result is not an invocation guard;
a definitely nonnormal operand prevents invocation. The derived parameter
premise supplies type and immutability, never nonemptiness: `first` still needs
the existing same-binding `empty?` branch proof. Effects and escaping errors
are derived separately from bottom. Calls remain ordinary activations, not
`recur` or TCO. Exported or escaping recursive functions, conflicting entries,
mutual recursion, general captures, imported recursive producers and expanded
recursive value domains remain outside this increment. No authoritative module
migration, full W5, target, self-hosting or release status follows from it.

### Private indexed ordinary recursion

Change the owning C7 pass for type/proof behavior, `effects.gravity` for C8,
the typed emitter for lowering and `runtime.gravity` for activation semantics.
Use paired `local-runtime-indexed-private-self` accepted fixtures and the six
`indexed-private-self-*` rejected pairs. They delimit the private zero-entry,
same-Vector/same-needle unit-step family; they do not make broader recursion
language-illegal. Existing public compiler interfaces remain unchanged.

Start with the affected direct kernel, adding adjacent kernels when its
interface or behavior changes:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh08-recursive-function-type-test \
  --exact current-gravity-indexed-private-rejection-development-kernel --fail-fast
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-indexed-private-source-context-development-kernel --fail-fast
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-indexed-private-lowering-runtime-development-kernel --fail-fast
# Costly affected source composition, not the ordinary edit-to-test loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-indexed-private-authenticated-composition --fail-fast
```

The source selector constructs fresh products and exercises function-table
activations, not `execute-closed-plan`. Its recorded run took about 737 seconds;
do not replay it merely to change labels, collect metrics or restate unchanged
evidence. Runtime, producer or downstream composition changes determine when a
new affected integration run is useful. Focused checks are development feedback;
fresh provenance/equivalence/bootstrap evidence belongs to an explicit stable
revision admission decision. The backlog records actual outcomes and exclusions.

### Vector family classification

STD3 owns unary `vector?` semantics. The development implementation accepts
independently established closed operand types, evaluates the operand once,
preserves effects/transfers, and returns Bool on normal invocation. Never
aborts invocation; Unknown is unsupported, not a false classification. A Bool
result supplies no member, length, index or type-refinement facts.

Edit the owning pass, not an authority-admission adapter:

- C7: `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`.
- C8: `bootstrap/gravity/src/gravity/effects.gravity`.
- Typed lowering: `bootstrap/gravity/p15_s23/emitter.gravity`.
- Runtime: `bootstrap/gravity/src/gravity/runtime.gravity`.

Use the affected existing direct kernel first:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-vector-predicate-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-vector-predicate-source-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-predicate-lowering-runtime-development-kernel --fail-fast
# Affected source composition, separate from the normal inner loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-predicate-authenticated-composition --fail-fast
```

The paired source programs separately exercise the unchanged W5 indexed
helper/wrapper and the existing typed-catch path. The cold-wrapper kernel in
the SH08 recursive-function namespace checks independent caller-derived
Vector7/Keyword facts; it does not obtain them from `vector?`. Rejected fixture
pairs cover unknown operands and zero/two arity. QST byte/parser parity is not
negative source-compilation evidence. Actual results and exclusions belong in
the backlog. No fresh authority proof is a prerequisite for ordinary edits;
authoritative migration remains an explicit separate decision.

### Integer family classification

STD5 owns unary `integer?` semantics. The development frontier accepts
independently established closed operand types, including the existing Integer
and I8/I16/I32/I64/U8/U16/U32/U64 type facts. These normalized typing cases do
not establish native-width arithmetic. Unknown remains unsupported; Never
aborts invocation. Operand evaluation happens once in non-tail position and
preserves effects and transfers. A Bool result supplies no conversion, width,
range, arithmetic-mode, overflow or type-refinement proof.

Use the same C7, C8, emitter and runtime owners listed for `vector?`, and the
existing direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-integer-predicate-development-kernel \
  --exact sh08-integer-predicate-throw-consumer-development-kernel \
  --exact sh08-ordering-pending-call-entry-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-integer-predicate-source-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-predicate-lowering-runtime-development-kernel --fail-fast
# Affected source composition, separate from the normal inner loop:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-predicate-authenticated-composition --fail-fast
```

The throwing-call consumer check exercises the containing pass, not only the
predicate helper: resource reasons and measured work must survive semantic
rejection. Binding, source, descriptor, fact and ordered ledger checks remain
independent at their owning boundaries. Run an affected source composition when
those boundaries change; direct focused feedback does not first require fresh
authority proofs or a migration fingerprint update.
The pending-call entry check covers source-order independence in the existing
bounded inference pass. An unresolved ordering diagnostic remains present in
that round while later real calls contribute independent parameter facts;
final unresolved, incompatible or conflicting facts still reject. No predicate
result is used as an operand-type refinement.
The paired source programs separate the unchanged W5 nonnegative-integer helper
from existing typed-catch transfer behavior. They do not imply general W5,
native-width or authoritative migration support. Rejected QST fixture parity
is not negative source-compilation evidence. Record actual results and limits
in the backlog.

### Set conversion and cardinality

STD3 owns persistent Set conversion and cardinality. The current bounded
development constructor accepts a finite, independently established homogeneous
String or Keyword Vector on the JVM target and returns a bare Set type. It does not invent
cardinality, ordering or member facts. Normal construction declares possible
allocation even for an empty input; operand transfers prevent invocation and
preserve their effects. Set count is a separate outer-family query and is not
restricted to String members or allowed to evaluate stored payloads.

The owning files remain `c7_type_checker_engine.gravity`, `effects.gravity`,
`p15_s23/emitter.gravity` and `runtime.gravity`. Their focused checks use the
existing direct runner:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-set-conversion-development-kernel \
  --exact sh08-set-count-development-kernel \
  --exact sh08-set-conversion-throw-consumer-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-set-conversion-source-development-kernel \
  --exact current-gravity-set-count-source-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-set-conversion-lowering-runtime-development-kernel \
  --exact current-gravity-set-count-lowering-runtime-development-kernel \
  --exact current-gravity-set-target-and-association-eligibility-development-kernel --fail-fast
```

The runtime validates every actual String member before hashing or insertion.
Its existing 65,536-unit component allowance reserves logical content work
`1 + 2*n + n*C + n*(n-1)/2`, with at most 1,024 input members and `C` the total
JVM UTF-16 representation length. Products are checked against the allowance
before multiplication. This is not a public String-count policy, normalization
rule, host-provider CPU bound or native/realtime claim. Supported values can
still exceed the explicit work allowance; that rejection is not a new language
ban on larger Sets.

When changing the public typed/effected/lowered composition, exercise the
affected accepted and rejected source fixtures as well as these kernels.
Run the affected composition separately from the edit loop:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-set-conversion-fixtures-development-kernel \
  --exact current-gravity-set-conversion-authenticated-composition --fail-fast
```

Preserve the distinction between normalized JVM/WASM eligibility checks and
actual target execution. Set equality, iteration, other conversion families,
whole-W5 execution and authoritative migration remain separate work; none is
established by these focused checks.

### Keyword Vector Set conversion

Use the same four conversion owners for the Keyword input extension. String,
empty and Never descriptors retain their existing route. Nonempty homogeneous
Keyword input has a distinct versioned construction policy; output is still
bare Set. Input extent is an allocation upper bound, not post-dedup cardinality
or a finite-member proof for Set equality/membership consumers.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-keyword-vector-set-conversion-development-kernel --exact sh08-keyword-vector-set-conversion-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-keyword-vector-set-conversion-source-development-kernel --exact current-gravity-keyword-vector-set-conversion-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-vector-set-conversion-lowering-development-kernel --exact current-gravity-keyword-vector-set-conversion-id-guards-development-kernel --exact current-gravity-keyword-vector-set-conversion-runtime-development-kernel
```

The complete actual Keyword Vector is validated and frozen before printing,
comparison, hashing or insertion. Reserve `1 + 2n + n(n-1)/2 + (n+1)U` within
the existing 65,536 logical allowance and 1,024-member limit, with U the total
printed JVM UTF-16 units used for cost only. Gravity Keyword equality chooses
representatives; spelling is not equality and provider Set storage is not the
semantic deduplication proof. Preserve duplicate collapse, namespace/name
distinctions, original input, precharged work, dominant source/producer ID
guards, complete C8 ledger and normal/Never allocation. This quota does not
establish provider CPU or realtime bounds.

For the affected composition:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-vector-set-conversion-fixtures-development-kernel --exact current-gravity-keyword-vector-set-conversion-authenticated-composition --fail-fast
```

The fixture preserves the actual C16 semantic-fields declaration and binary
conj/conversion expression. Use the existing AP closed module-instance path
to initialize the declaration, not a nil-pool table wrapper or a replacement
local helper. Twenty-four input positions produce 23 distinct runtime members;
this observation does not refine the output type. One Gravity positive and one
mixed-family C7 negative run; QST is byte/parser only. Do not claim whole C16,
variadic conj, keys conversion, new Set consumer domains or migration support.
The retained source run described in the backlog remains failed at one false
harness expectation; its correction and separate cheap checks do not relabel
it as a green aggregate. Reuse unchanged completed observations where valid;
do not reconstruct expensive products solely to change a historical label.

### Binary nonnumeric atomic inequality

For binary `not=`, edit the C7 engine, `gravity.effects` and the typed emitter.
STD2 owns the complement-of-applicable-equality law. This nonnumeric increment
supports Nil, Bool, Character, String, Symbol, Keyword, their five existing
nil-or-atomic carriers, and Never. Both operand domains are checked even when
one transfers. Numeric, collection, callable, Unknown and malformed domains
remain outside this increment; the separate Integer extension is below. No
first-class or variadic operation is added.

Start with the owning focused checks, retaining adjacent `=`/nil-equality
regressions when changing their shared dispatch:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-atomic-inequality-development-kernel --exact sh08-atomic-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-atomic-inequality-source-development-kernel --exact current-gravity-atomic-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-atomic-inequality-id-guards-development-kernel --exact current-gravity-atomic-inequality-lowering-runtime-development-kernel --exact current-gravity-atomic-inequality-transfer-fence-development-kernel --fail-fast
```

C7/C8/emitter independently validate the source binding, two operands, versioned
descriptor, call/edge, constraints and complete ledger. Lowering owns an existing
equality test with false/true branches; it creates no source `=` facts. Evaluate
both operands once, left-to-right and non-tail, preserving effects and original
transfers. Compare actual nullable values: different nullable families can both
hold nil. Use the existing canonical Character transport, not a manually tagged
BMP record. Meter actual pre-child work separately from success-only suffix work.

For the affected source boundary, select the cheap fixture check before the
bounded completion composition:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-atomic-inequality-completion-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-atomic-inequality-completion-authenticated-composition --fail-fast
```

This executes five Gravity entries, two same-emitted-IR seed controls, genuine
C7/C8 guards and coherent rejection controls on the smaller completion input.
QST is byte/parser coverage only. The seed cannot interpret the existing typed
map-lookup or typed-catch instructions; do not widen it to satisfy a reference
outside its domain. The original sixteen-function source attempt remains a
failed partial observation, not a green aggregate. Preserve its passed and unrun
boundaries as recorded in the backlog; do not reconstruct that unchanged input
merely to change its historical label. Runtime and seed production files are
unchanged. This is development implementation, not migration, whole W5 execution,
other-target support, self-hosting or release evidence.

### Binary represented-Integer inequality

The same C7, effects and emitter owners extend binary `not=` to the existing
represented `Integer` equality domain. Select exactly Integer/Integer,
Integer/Never or Never/Integer; the old Never/Never atomic path is unchanged.
Do not broaden the atomic whitelist or infer fixed-width, cross-numeric,
Float/Decimal/Ratio or nullable-Integer support. Integer/Keyword still rejects.
The old numeric rejection fixture now uses `7.5`, because Integer `7` is no
longer an unsupported operand. This is an implementation frontier change,
not a change to STD2's complement law.

The versioned numeric record preserves existing Integer equality's original
values, no conversion/arithmetic/overflow/rounding, `:proof-required` mode and
`:proven-safe` rule. Numeric semantics remain owned by `gravity.math` and its
Layer0 owner; the actual source operation remains `gravity.core/not=`. All
three passes reconstruct the complete descriptor and constraint solution.
Lowering still owns its equality/false/true IR and creates no source `=` fact.
Runtime, seed, public APIs and instruction schemas are unchanged.

Start with the new owning kernels, alongside affected atomic-inequality and
integral-equality regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-integer-inequality-development-kernel \
  --exact sh08-integer-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-integer-inequality-source-development-kernel \
  --exact current-gravity-integer-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-integer-inequality-transfer-fence-development-kernel --fail-fast
```

Select the affected real composition separately when these producer/consumer
facts or lowering behavior change:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-inequality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-integer-inequality-authenticated-composition --fail-fast
```

That selection uses one changed Gravity positive and one changed Gravity
negative. Count-produced Integers are concrete supported inputs; this is not
execution of a whole compiler consumer. Q sources provide byte/parser parity
only. Type-conforming runtime entries, actual pre-child work, preserved
operand effects and original transfers remain required. The seed reference
uses the same emitted IR and excludes typed catches it cannot interpret.
Focused results and this bounded composition do not establish another target,
authoritative migration, complete numeric support or self-hosting.

### Binary same-type fixed-integral inequality

The same three owners also support binary `not=` for I8, I16, I32, I64,
U8, U16, U32 and U64. Both normally returning operands must have the same
established type; either position may instead be Never. Cross-width,
signed/unsigned and generic-Integer/fixed pairs reject, as do Float, Decimal,
Ratio, nullable fixed types and collections. Existing Integer, nonnumeric and
Never/Never descriptors remain unchanged.

The fixed-family numeric record names the actual type and preserves the
existing equality policy: original values, no coercion or arithmetic,
`:proof-required` mode and `:proven-safe` safety. C7, effects and the emitter
independently reconstruct the complete record and its joins. Runtime, seed,
public APIs and IR schemas are unchanged. Start with these owning checks,
including affected Integer, atomic-inequality and integral-equality regressions
when their shared rules change:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-fixed-integral-inequality-development-kernel \
  --exact sh08-fixed-integral-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-fixed-integral-inequality-source-development-kernel \
  --exact current-gravity-fixed-integral-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-fixed-integral-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-fixed-integral-inequality-transfer-fence-development-kernel --fail-fast
```

Runtime fixtures independently require Integer representation and the declared
signed/unsigned bounds before every normal or same-IR seed entry. This checks
conditional type-conforming execution, not implicit source Integer conversion,
host FFI admission or runtime width validation. Never transfers still preserve
their original identity, non-tail restrictions and left-to-right effects.

When those producer/consumer facts or lowering change, select the new paired
fixtures and affected source composition separately:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-fixed-integral-inequality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-fixed-integral-inequality-authenticated-composition --fail-fast
```

This uses compiled helpers with explicit fixed-type signatures, not implicitly
converted source Integer literals. Paired Q sources provide byte/parser parity
only. Do not reconstruct earlier unchanged Integer or atomic source products
merely to refresh a green label.

### Binary finite persistent Vector inequality

For binary `not=` over established finite positional Vector types, change the
existing C7 engine, effects checker and typed emitter. Reuse their existing
Vector equality domain and owned `if (= left right) false true` IR; runtime,
seed, public interfaces and type grammar do not change. Each pass reconstructs
the complete versioned collections policy and the original `not=` constraint,
call fact and ledger. Do not manufacture a source `=` fact.

Both complete type domains are validated before an early value mismatch.
Supported recursive members are Vector, Nil, Bool, Character, String, Symbol,
Keyword and the five existing closed nullable nonnumeric atomic types. Empty
and differently shaped/length Vectors compare normally; static type equality
is not required. Width 1,024, depth 128 and work 65,536 remain the existing
finite limits. Numeric members, unknown/bare Vector types, other collections,
whole nullable Vectors and nested Never remain unsupported. Top-level Never
preserves non-tail control transfer when the other operand establishes this
Vector domain; Never/Never retains the existing atomic rule.

Start with these focused checks and affected equality/inequality regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-vector-inequality-development-kernel \
  --exact sh08-vector-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-vector-inequality-source-development-kernel \
  --exact current-gravity-vector-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-vector-inequality-transfer-fence-development-kernel --fail-fast
```

Direct domain/derivation checks retain the Vector-specific work-limit reason.
The containing C7 verifier reports its existing atomic-inequality work-limit
reason and charged work. The private inferred-call check retains the
underlying subtype reason and exact work.
Neither boundary may discard failed-child work or assume the scalar ceiling.

For changed producer/consumer facts or lowering, separately select the paired
fixture check and one affected source composition:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-inequality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-vector-inequality-authenticated-composition --fail-fast
```

This uses the W5 seven-Keyword value with a fresh producer and new inequality
expressions, not a copied or unchanged W5 inequality helper. Q fixtures provide
byte/parser parity only. Do not repeat unchanged Vector equality, numeric
inequality or whole-W5 source compositions to refresh a label. Focused results
are development feedback, not authoritative migration or self-hosting proof.

### Binary finite Keyword-Set inequality

For binary `not=` over established finite literal-Keyword Set types, edit the
existing C7 engine, effects checker and typed emitter. Reuse their complete
Keyword-Set equality domain checks and owned `if (= left right) false true`
IR. Each pass reconstructs its own versioned inequality policy, original
source call, constraint solution, call fact and ledger. Do not manufacture a
source `=` fact. Runtime, seed, public interfaces and type grammar do not change.

Both complete domains are checked before cardinality or value comparison.
Empty, differently sized and reordered Sets compare normally; matching static
types are not required. The existing limits are 1,024 members per side and
65,536 work units. Bare/homogeneous/Unknown or malformed Sets, whole nullable
Sets, non-Keyword members, nested collections and nested Never remain outside
this slice. A top-level Never preserves transfer when the other operand
establishes the supported Set domain; Never/Never keeps the atomic rule.

Start with focused semantic checks and affected equality/inequality regressions:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-keyword-set-inequality-development-kernel \
  --exact sh08-keyword-set-inequality-throw-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-keyword-set-inequality-source-development-kernel \
  --exact current-gravity-keyword-set-inequality-consumer-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-inequality-lowering-runtime-development-kernel \
  --exact current-gravity-keyword-set-inequality-transfer-fence-development-kernel --fail-fast
```

Preserve failed-child work and the containing atomic-inequality diagnostic;
the private inferred-call check retains the Set-specific underlying reason.
Existing runtime equality validates both actual domains, member uniqueness
and the complete worst-case comparison reservation before an early result.
This is not authority to rely on host Set storage or claim a provider CPU bound.

For changed producer/consumer facts or lowering, select the paired fixture
check and one affected source composition separately:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-inequality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-inequality-authenticated-composition --fail-fast
```

The source fixture uses C17's capability-Set value with a fresh producer and
new inequality expressions. It does not activate or compile the whole C17
helper. Q fixtures provide byte/parser parity only. Do not replay unchanged
Keyword-Set equality or Vector inequality source compositions to refresh a
label. Development results do not establish migration or self-hosting.

### Nil value classification

For unary `(nil? value)`, edit the existing C7 engine, effects and typed emitter
owners. Runtime, seed, public APIs and IR schemas remain unchanged. Current
validated closed types select existing IR: known Nil/non-Nil evaluates the
operand then returns true/false; the existing nullable Bool, Character, String,
Symbol and Keyword families compare the evaluated operand with Nil. Never
retains the original transfer. Nullable Integer/collections, Unknown/Dynamic,
malformed types and unsupported callables are not added by this increment.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-nil-predicate-development-kernel --exact sh08-nil-predicate-id-guard-development-kernel --exact sh08-nil-predicate-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-nil-predicate-source-development-kernel --exact current-gravity-nil-predicate-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-nil-predicate-lowering-runtime-development-kernel --exact current-gravity-nil-predicate-transfer-fence-development-kernel
```

Keep the classification/source/type joins, independent C8 reconstruction and
complete ledger, exact/short budgets, failed-child work, original throw and
non-tail recur fences. The synthetic nullable equality is typed lowering,
not a fabricated source equality call. No generated local, source rewrite or
raw runtime `nil?` builtin is introduced. This type-directed route is supported
for authenticated typed calls and type-conforming entries, not arbitrary
wrong-typed host/FFI arguments. It is not check-elision or migration evidence.

For affected source composition, run:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-nil-predicate-fixtures-development-kernel --exact current-gravity-nil-predicate-authenticated-composition --fail-fast
```

The fixture compares the copied `w5-se-canonical-form` body with its read-only
owner, then adds new known/unknown-mode queries on that nullable Keyword
producer. It does not claim an existing W5 nil-query helper or whole-W5 build.
One Gravity positive and one C7 arity rejection run; QST is byte/parser only.
An allocation-bearing operand checks source effect preservation; observable
I/O ordering is covered separately by normalized kernels, not this source
fixture. Seed comparisons interpret the same emitted IR, not raw-source parity.

### Boolean family classification

Unary `(boolean? value)` returns true for both `true` and `false`, not for
truthy values generally. Edit the existing C7 engine, effects, typed emitter
and Gravity runtime owners. Existing valid closed first-order operand types
are accepted; Never preserves control transfer. Unknown/Dynamic, malformed
carriers and unsupported callable types remain diagnosed. The type grammar,
unary IR and seed primitive are unchanged.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-boolean-predicate-development-kernel --exact sh08-boolean-predicate-id-guard-development-kernel --exact sh08-boolean-predicate-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-boolean-predicate-source-development-kernel --exact current-gravity-boolean-predicate-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-boolean-predicate-lowering-runtime-development-kernel --exact current-gravity-boolean-predicate-shallow-runtime-development-kernel
```

Keep the once-only function-returned-false control: classification must return
true without dropping or repeating operand effects. String/Keyword spelling,
nil, represented numbers and collections do not become Bool values. A wrapper
Map is not unwrapped. Runtime classification is shallow, with no payload
traversal, hashing, equality, mutation or collection construction. Retain
dominant unary/ID guards, independent complete C8 analyzer/ledger and exact
budget checks, failed-child work, transfers/original failures and proportionate
Keyword/Symbol-predicate regressions. These are component development checks,
not source integration or authoritative migration evidence.

For affected compiler-to-runtime composition, run the paired preflight and
the separate source check:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-boolean-predicate-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-boolean-predicate-authenticated-composition
```

The preflight compares the copied C6 scalar-helper defn AST with its actual
owner. Only false/true callers supply that helper, giving one inferred Bool
parameter domain. Its existing nil comparison is `(= value nil)`, not a new
`nil?` primitive. Other families have separate direct-query functions. This is
not full C6 execution, polymorphic scalar validation or new parameter grammar.
One Gravity positive and one C7 arity rejection run; QST has byte/parser coverage
only. Same-emitted-IR seed comparisons are not independent source compilation.

### Symbol family classification

Unary `(symbol? value)` classifies the normally evaluated value, not source
identifier spelling. Edit the existing C7 engine, effects, typed emitter and
Gravity runtime owners. Valid closed first-order types are accepted; Symbol
values classify true, other families false and Never propagates. Unknown,
Dynamic, malformed carriers and unsupported callable types remain typing
failures. The existing type grammar, unary IR and seed primitive are unchanged.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-symbol-predicate-development-kernel --exact sh08-symbol-predicate-id-guard-development-kernel --exact sh08-symbol-predicate-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-symbol-predicate-source-development-kernel --exact current-gravity-symbol-predicate-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-symbol-predicate-lowering-runtime-development-kernel --exact current-gravity-symbol-predicate-shallow-runtime-development-kernel
```

The query preserves unary evaluation, operand effects, transfers and original
failures. Runtime classification is shallow, with no payload traversal,
hashing, equality, mutation or collection construction. Qualified and
metadata-bearing Symbols remain values. A String or Keyword with similar
spelling stays non-Symbol; a syntax-wrapper Map is not unwrapped. Classification
grants no binding, call, macro or capability authority. Keep complete C8
context/ledger and exact/one-less budget checks, dominating unary shape/ID
guards, and proportionate Keyword/Set-predicate regressions. Focused tests are
component feedback, not source integration, frontend-module or migration proof.

For affected compiler-to-runtime composition, run the paired preflight and
the separate source check:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-symbol-predicate-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-symbol-predicate-authenticated-composition
```

The existing Symbol return annotation is exercised through an ordinary
identity helper with a monomorphically inferred quoted-Symbol input. Other
families use separate functions, not fabricated polymorphic inference or new
parameter annotation syntax. A tagged wrapper is ordinary Map DATA, not proof
of accepted syntax-object schema. One Gravity positive and one C7 arity
rejection run; QST has byte/parser coverage only. Seed interpretation of the
same emitted IR is not independent source compilation or whole frontend
module coverage. Pure source operands do not independently observe I/O order.

### Set family classification

For unary `(set? value)`, edit the existing C7 engine, effects, typed emitter
and Gravity runtime owners. Existing valid closed first-order operand types
are accepted: bare or finite Set families classify true and other families
false. Unknown/Dynamic, malformed carriers and unsupported callable types
remain typing failures, not false classifications. Never propagates normally.
The existing type grammar, unary builtin IR and seed primitive are unchanged.

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-set-predicate-development-kernel --exact sh08-set-predicate-id-guard-development-kernel --exact sh08-set-predicate-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact current-gravity-set-predicate-source-development-kernel --exact current-gravity-set-predicate-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-set-predicate-lowering-runtime-development-kernel --exact current-gravity-set-predicate-shallow-runtime-development-kernel
```

These exercise the owning passes, including the complete C8 analyzer's
context debit, exact/one-less budget and missing/duplicate/orphan ledger rows.
Malformed unary call shape must reject before any ID traversal. Runtime
classification itself is shallow: it does not count, sequence, hash, compare,
mutate or validate Set payloads. A throwing-provider control checks zero such
callbacks. This is not a claim that arbitrary foreign values bypass ordinary
input/evaluation boundaries. Operand effects, once-only evaluation, transfers
and original failures remain; the query adds no collection allocation.
Retain proportionate Map-predicate and Set-conversion regressions. Closed type
carrier validation is distinct from runtime payload inspection. Focused
component checks do not establish source integration or migration evidence.

For affected compiler-to-runtime composition, run the paired preflight and
then the separate source check:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-set-predicate-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-set-predicate-authenticated-composition
```

The preflight checks paired bytes/parser shape and unchanged C9 aggregate-helper
body reuse. That helper is exercised monomorphically on the existing bare Set
result of String-Vector conversion; it is not a full C9 module build or generic
polymorphism test. Other finite Set/non-Set cases use separate functions. The
composition selects one Gravity positive and one Gravity C7 arity rejection,
not QST typed products. Same-emitted-IR interpreter comparisons do not establish
independent raw-source compiler parity. There is no new Set annotation grammar.

### Finite Keyword-Set membership

Edit the existing C7 engine, effects, typed emitter and Gravity runtime for
binary `(contains? items value)`. The development domain is an existing finite
literal-Keyword Set type and an independently established Keyword needle;
the normal result is Bool. This frontier is not a language-wide restriction.
The closed value-type grammar, existing Map behavior and Never transfers stay
unchanged. Querying adds no collection-allocation effect; constructors retain
their own allocation requirements.

Run the owning semantic kernels directly:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-keyword-set-contains-development-kernel --exact sh08-keyword-set-contains-dispatch-guard-development-kernel --exact sh08-keyword-set-contains-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact body-keyword-set-contains-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-set-contains-lowering-development-kernel --exact current-gravity-keyword-set-contains-runtime-development-kernel
```

These check independent source/type/descriptor joins, budget-first rejection,
failed-child work, complete producer rows, once-only operand evaluation,
transfers and original failures. Runtime membership validates all actual Set
members and their semantic uniqueness before either hit or miss. Width remains
1,024 and the logical work allowance 65,536. Keyword representation units
measure prepaid work; membership uses actual Gravity Keyword equality, not
printed identity, host Set lookup or hashing. This is not a provider CPU bound.
The query preserves original values and does not execute stored payloads.

Retain the existing Map-contains kernels and proportionate evaluated-Set and
Set-equality regressions when their shared consumers change. Focused kernels
are development feedback, not fresh source-composition or migration evidence.

When membership descriptors, effects, lowering or runtime composition change,
run the paired fixture preflight, then the affected source composition:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-set-contains-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-keyword-set-contains-authenticated-composition
```

The preflight checks paired bytes and parser shape. The composition selects
one Gravity accepted source and one Gravity C7 rejection; it does not run QST
typed products. Seed interpretation of the same typed-emitted IR is a runtime
comparison, not independent raw-source compiler parity. Renew affected paired
composition when reader/extension/identity handling changes, not every edit.

### Finite Character-Set membership

The same four owning passes support a finite Character-Set value and a
Character needle, including quoted Set values. Evaluated literal construction
has separate owning checks below; Character-Set equality is not selected.
Start with the membership kernels:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh08-function-call-type-test --exact sh08-character-set-contains-development-kernel --exact sh08-character-set-contains-dispatch-guard-development-kernel --exact sh08-character-set-contains-throw-consumer-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh09-body-effect-test --exact body-character-set-contains-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-character-set-contains-lowering-development-kernel --exact current-gravity-character-set-contains-runtime-development-kernel --exact current-gravity-character-set-contains-transfer-fence-development-kernel
```

Check independently reconstructed source/type/descriptor joins, complete
producer rows, Never transfers, malformed carriers, semantic duplicates and
exact/short budgets. The runtime validates every member and the needle,
reserves the complete comparison budget, then checks semantic uniqueness
before returning either hit or miss. Host Set lookup, hashing and printing
are not membership authority. Existing Keyword membership remains unchanged.
The Character representation premise is the existing atomic Character IR;
C2 owns Unicode decoding and source validity. These checks do not establish
stronger validity for arbitrary host-created values or a provider CPU bound.

A quoted copy of C17's 16 hex Characters exercises the membership value, not
the unchanged C17 helper, whose evaluated Set construction is a separate
development slice. Keep that distinction when recording source results.
As with Keyword membership, affected source composition is a separate check,
not a prerequisite authority-admission workflow for ordinary focused edits.

For changes to membership's composed behavior, use the existing paired
fixture preflight and affected Gravity source selection:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-character-set-contains-fixtures-development-kernel
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-character-set-contains-authenticated-composition
```

The source subject has seven functions and six membership queries, including
both caught transfer positions. QST receives byte/parser checks only; normal
seed controls interpret the same emitted IR, not an independent raw-source
compiler. Supplementary and hostile Character carriers remain focused IR
controls rather than claims about this ASCII hex source subject.

### Evaluated literal-Keyword Sets

For the bounded JVM slice, edit the existing typed emitter and runtime owners.
C7 already describes finite literal Set member types, and C8 already accounts
for normal constructor allocation, including the empty Set. The lowerer emits
the existing `:set-literal`/`:items` instruction from authenticated actual
Keyword children, not the parent's cached `:source-value`. Start with:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-literal-keyword-set-c8-development-kernel \
  --exact current-gravity-literal-keyword-set-lowering-development-kernel \
  --exact current-gravity-literal-keyword-set-runtime-development-kernel --fail-fast
```

Check source/child/type joins, malformed IDs before structural equality,
own-plus-prefix-plus-failed-child work, complete Keyword domains and duplicate
rejection before host insertion. Set items must participate in instruction
width and module-read traversal. L1's duplicate-literal rejection remains in
force; this is not Vector-to-Set conversion's deduplication behavior.
The current component limits are width 1,024 and work allowance 65,536;
fitting the width does not imply that comparison/content work fits.

Quoted Sets remain inert, and evaluated Set module initializers remain outside
this slice. Character literals have the separate extension below; other member
families or arbitrary effectful Set children, Keyword List
conversion, `keys` ordering and whole-W5 execution are separate frontiers.
Pure literal children cannot independently observe once-only evaluation order.
When assembly/lowering/runtime composition changes, validate genuine accepted
and representative rejected source through those affected boundaries; ordinary
kernel edits do not require a fresh corpus or authoritative migration first.

Run the paired fixtures/duplicate-reader check before the affected source
composition, as separate existing-runner selections:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-literal-keyword-set-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-literal-keyword-set-authenticated-composition --fail-fast
```

The composition selects one accepted G program and one upstream-accepted,
lowering-rejected String-member program. Q gets byte/parser checks, plus the
separate C2 duplicate diagnostic, not typed-product parity. Reference execution
uses the seed interpreter on the same emitted IR, not independent raw-source
compilation. Listing these commands does not claim that they passed.

### Evaluated literal-Character Sets

Use the same emitter and runtime owners. Existing C7 finite member types and
C8 allocation accounting apply without a new production pass or interface.
The bounded extension accepts homogeneous Character literal children,
including existing supplementary Character carriers; empty construction keeps
the legacy path. Start with:

```bash
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-literal-character-set-lowering-development-kernel --exact current-gravity-literal-character-set-runtime-development-kernel --fail-fast
```

Preserve complete source occurrence, ordered child, parent and type joins;
the cached aggregate value is not authority. Compile each original child once,
non-tail, and retain failed-child work. The instruction remains the existing
literal-only `:set-literal`/`:items` shape. Mixed families, arbitrary expression
children and evaluated Set module initializers remain unsupported here.

Validate all Character values and reserve full logical work before comparing
or inserting any member. Runtime reservation is
`1 + 2*n + n*(n-1)/2 + (n+1)*U`, with `U` the sum of existing Character
representation units (one for boxed Characters, two for supplementary
carriers). Width 1,024 and work 65,536 remain separate bounds. Compare
semantic Character classes when rejecting duplicates: distinct carrier Maps
can have the same codepoint. Do not silently deduplicate a Set literal,
substitute host Map equality or add a new Unicode decoder. Source validity
remains C2-owned under the existing atomic Character IR premise.

Retain existing Keyword construction, Character membership and C7/C8
literal/allocation checks as affected regressions. Construction requires
`:memory/allocate`; membership's pure query does not erase that effect.
Source composition, current-closure migration and release evidence remain
separate from these direct focused checks.

For changes to the composed constructor, select the existing preflight and
source checks separately:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-literal-character-set-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test --exact current-gravity-literal-character-set-authenticated-composition --fail-fast
```

Preflight checks the unchanged C17 helper form, paired bytes/C2 values and
duplicate supplementary-source rejection. The affected Gravity composition
requires actual evaluated Set instructions and allocation, including
supplementary values; the mixed-member negative must pass upstream checks
before typed lowering rejects it. QST is not a typed-product comparison.
Seed controls use the same emitted IR, not an independent raw-source compiler.

### Finite Keyword-Set value equality

STD3 owns Set value equality. The bounded development slice uses binary `=`
for two independently established finite Sets whose complete member domains
are Keywords, including empty Sets. Each operand keeps its own type evidence;
equal type vectors, constructor identity and enumeration order are not equality
premises. Bare Set types and unknown or other member domains remain separate
implementation frontiers, not language-wide prohibitions.

Change the existing C7, C8, typed emitter and runtime owners. Start with the
owning kernels and affected adjacent equality regressions, not fresh upstream
proof construction:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact sh08-keyword-set-equality-development-kernel \
  --exact sh08-keyword-set-equality-dispatch-guard-development-kernel \
  --exact sh08-keyword-set-equality-throw-consumer-development-kernel \
  --exact body-keyword-set-equality-development-kernel \
  --exact current-gravity-keyword-set-equality-lowering-development-kernel \
  --exact current-gravity-keyword-set-equality-runtime-development-kernel --fail-fast
```

The implementation must validate both complete typed and actual domains,
including distinct Keyword value classes within each actual operand, before
returning a cardinality or member mismatch. Runtime member matching uses the
existing guarded Keyword value-equality leaf, not host Set equality, hashing
or printed-spelling identity. Worst-case within-operand and cross-operand
pairwise/content work is prepaid within the existing component allowance;
actual comparisons and logical
reservation are distinct. JVM representation units used for accounting do not
define semantic equality or establish a provider CPU bound.

Check paired fixture bytes/parser interpretation cheaply. When the
typed/effected/lowered composition changes, run the affected source selector
separately from the edit loop:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-equality-fixtures-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-set-equality-authenticated-composition --fail-fast
```

Use genuine quoted Sets and ordinary function returns rather than inventing an
accepted verification result. Ordinary operands retain left-to-right once-only
evaluation, effects and transfers; stored members remain inert and originals
unchanged. These checks do not establish evaluated Set-literal lowering,
Keyword List conversion, `keys` ordering, whole-W5 execution, other-target
support or authoritative migration. Record actual outcomes in the backlog;
listing a command is not evidence that it passed.

### Map family classification

STD3 owns unary `map?`. The bounded JVM implementation classifies an
independently established Map as true and other supported closed value families
as false. Unknown remains unsupported, and Never prevents invocation. Normal
operand evaluation happens once in a non-tail position with effects preserved;
classification does not evaluate stored keys/values, allocate a collection or
grant key/member, cardinality, presence or ordering facts.

Edit the same C7, C8, typed emitter and runtime owners used for the adjacent
collection queries. Start with the existing direct focused checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-map-predicate-development-kernel \
  --exact sh08-map-predicate-throw-consumer-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-map-predicate-source-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-predicate-lowering-runtime-development-kernel --fail-fast
```

These exercise owning Gravity implementations using normalized development
inputs, including source-bound reconstruction; they do not construct a fresh
source composition or admit a module revision. Changes to the public
typed/effected/lowered composition require its affected accepted and rejected
source checks separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-map-predicate-fixtures-development-kernel \
  --exact current-gravity-map-predicate-authenticated-composition --fail-fast
```

`keys`, collection conversion/equality and unchanged
W5 exact-key validation remain separate slices, not predicate-result facts.

### Keyword family classification

STD2's `gravity.core` owns unary `keyword?`. The bounded JVM implementation
accepts independently established closed operand types: actual Keywords return
true and other supported values return false, including the actual nil branch
of a nil-or-Keyword operand. Unknown and Dynamic remain unsupported, not
language-wide bans. Keyword-like String/Symbol spelling creates neither a
Keyword nor a binding, and a qualified Keyword confers no capability.

Edit the owning C7, C8, typed emitter and runtime files. Use the direct focused
checks before affected source composition:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-keyword-predicate-development-kernel \
  --exact sh08-keyword-predicate-throw-consumer-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-keyword-predicate-source-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-predicate-lowering-runtime-development-kernel --fail-fast
```

The query preserves once-only non-tail operand evaluation, effects and control
transfers; Never prevents invocation. It evaluates no stored payloads, adds no
collection allocation or own capability requirement, and grants no type-flow
refinement. Normalized reconstruction and JVM/WASM eligibility checks are not
actual target execution or fresh source products. Use a changed source
composition when the public typed/effected/lowered boundary changes:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-keyword-predicate-fixtures-development-kernel \
  --exact current-gravity-keyword-predicate-authenticated-composition --fail-fast
```

Compiling one predicate/projection subexpression is not execution of an unchanged W5
validator, current-closure migration or self-hosting completion.

### String family classification

STD4's `gravity.text` owns unary `string?` semantics behind the existing core
compatibility spelling. Edit C7, C8, typed lowering and runtime, then run:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh08-function-call-type-test \
  --exact sh08-string-predicate-development-kernel \
  --exact sh08-string-predicate-throw-consumer-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact current-gravity-string-predicate-source-development-kernel --fail-fast

clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-predicate-lowering-runtime-development-kernel --fail-fast
```

Classification distinguishes actual Strings, including empty Strings, from
other supported independently established closed values. Nil-or-String operands
classify their actual value; Unknown and Dynamic remain implementation
frontiers. It preserves once-only non-tail operand evaluation, effects and
transfers without conversion, payload execution or type-flow refinement. It
does not independently validate Unicode or establish normalization, length or
indexing facts; existing String validity obligations remain at their owning
boundaries. It grants no new collection allocation, capability, export or
profile permission.
Text/view/Bytes or host-carrier convertibility does not establish String type.

Use changed source composition when the public typed/effected/lowered boundary
changes, and record its actual result separately:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact current-gravity-string-predicate-fixtures-development-kernel \
  --exact current-gravity-string-predicate-authenticated-composition --fail-fast
```

This selector constructs the two positive Gravity programs and one Gravity
arity rejection. Paired `.qst` fixtures receive byte/parser checks only; no
fresh paired typed/effect/lowering/result equivalence is claimed. Both positive
programs retain current C7/C8 verification and execution guards, and both
forgery checks remain. Compiling the W5 runtime record's String field checks
is not executing its unchanged validator, admitting
a compiler revision or completing self-hosting.

### Legacy bounded self-recursive body analysis

The same body interface also recognizes C7's existing named, capture-free,
positive-fixed-arity positional-call/literal-base proof. Positive arity means a
nonzero parameter count, not positive argument values. Ordinary `if` retains
L2 truthiness; the Bool-only match-guard policy is not an extra `if` restriction.
The original recursive
proof, call facts, component, positional arguments, literal base and external
parameter evidence must agree with the complete consumed core. A direct
imported producer uses the same per-module analysis and retains its own source
and artifact coordinates; it is not replaced by a synthetic local result.

Start with the direct checks:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-recursive-proof-development-kernel \
  --exact body-effect-recursive-fixed-point-development-kernel
```

The solver starts function summaries at the empty effect set, then derives a
least fixed point within the existing shared node-attempt budget. Only the
authenticated self-body dependency is cut internally while a round is solved.
The final graph retains that dependency, including a recursive branch under a
literal false condition, and its equations are checked against the complete
graph. Equation equality alone is insufficient: fresh candidate verification
reconstructs from bottom so an inflated, nonleast fixed point cannot pass.
Classification, iteration, final reconstruction and equation visits share the
invocation's existing work budget across caller and producers.

The affected real-source composition is separate from these normalized kernels:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-recursive-authenticated-paired-composition --fail-fast
```

It uses the existing Integer and String recursive fixture pairs, a paired caller
importing both producers, and the existing real mutual-recursion rejection
fixture. Fresh C7 products and verification precede body analysis. Test results
belong in the slice backlog after the actual run; the command itself is not
evidence of a pass. The unchanged public-bundle triggers above still apply.
This bounded analysis does not prove program termination, recursion depth,
tail-call optimization, mutual recursion, callable recursion, completed thrown
effects, full effect legality, target execution or authoritative migration.

### Capture-free callable body analysis

The body interface recognizes C7's existing named identity/apply-one proof,
not arbitrary higher-order values or the older fixture bridges. The callable
parameter is allowed only at the exact proof-selected apply slot, with the
original nested function type. Other callable parameters, callable returns,
captures, multi-hop dispatch and higher-order recursion remain separate work.

The inner call keeps its original nonlocal C6 edge and nil direct callee. A
separate selected-body dependency joins the outer function-value argument,
callable parameter, inner operator/argument, selected identity function and
actual body. Carrying or constructing a function value does not execute it;
the inner invocation propagates its derived body effects. C7 declaration
allowances do not replace that derivation. Pending throws and legality remain
pending.

Run the direct checks first:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh09-body-effect-test \
  --exact body-effect-callable-proof-development-kernel \
  --exact body-effect-callable-dependency-development-kernel
```

The affected real composition uses
`body-effect-callable-authenticated-paired-composition` in the same namespace.
The separate `body-effect-callable-current-c7-rejections` selector covers the
real nonfunction and capture fixture pairs.
It consumes fresh C7 construction/verification and the original complete
binding. A direct scalar import can select the producer's identity export while
the complete producer graph also contains its validated apply body. The
selected imported function must still have a first-order signature; this is
not an imported callable API. Original producer coordinates and full private
body coverage remain required. The existing aggregate bounds, unchanged public
C8 forwarding checks and public-bundle rerun triggers apply. Actual results
are recorded separately in the slice backlog, not inferred from these commands.
Current C7 diagnostic precedence, rather than historical fixture comments,
defines the rejection oracle. An oracle-only correction does not require
repeating already completed positive composition when production and fixture
bytes are unchanged; retain the failed aggregate and run the affected negative
selector separately.

### Compiler-artifact module execution

The temporary seed interpreter has a separate, explicitly unadmitted module
bundle input for developing compiler code across real namespace boundaries.
This is not the checked C7/C8 user-program path. Keep the existing raw
three-argument plan constructor for a single source; it does not invoke the
checked frontdoors. For an ordinary caller with an explicit dependency, use:

```clojure
(bootstrap/p15-s23-stage2-compiler-artifact-plan
 emitter caller-path caller-text
 {:caller-resolution-artifact caller-c5
  :dependency-plans [independent-callee-plan]})
```

Build `caller-c5` with the ordinary module-input interface above. Build a leaf
plan independently with the raw three-argument constructor and the same
emitter. For an acyclic chain, retain each dependency's genuine ordinary C5
inputs and supply one plan for every nonroot module. A nonleaf plan can come
from that module's own bundle `:plan` entry; the root independently recompiles
and compares every supplied plan, so the projected entry is input DATA, not
accepted authority. Pass the resulting flat bundle to the existing
`p15-s23-stage2-runtime-execute-function` interface. Source reconstruction,
actual C5 public exports/origins, separate plans and exact callsite routes are
checked; a matching name or recomputed hash alone does not establish a link.
The public invocation reconstructs the bundle, and linked-call route and arity
checks precede argument evaluation. Local function names remain module-local.
The two supported module pairs are development construction/execution subsets,
not full profile conformance or host-capability grants. Ordinary C5 still owns
target, capability and cross-profile import diagnostics; this interface does
not turn a meta/hosted edge into an implicit facade.

Transitive calls use this same four-argument interface, not nested bundles or
a separate runner. The root's retained C5 inputs determine the complete
closure; missing, extra or conflicting inputs, and inputs that fail fresh
reconstruction, reject. Full-DATA
deduplication is not accepted-result caching: every unique module still enters
the original fresh source verifier, whose own dependency replay remains.
Per-module routes preserve lexical identity when different modules use the
same function name and callsite path. Existing limits remain 257 modules,
256 nonroot plans, 65536 aggregate routes and the per-plan instruction bound.
Cycles still reject in ordinary C5. This does not add imported DATA reads,
general module initialization or checked user-call effects.

For this explicit development interface, the entry is an unqualified function
name in the freshly reconstructed root module. It may be private or public,
matching the existing single-source interpreter interface; it is not a lookup
through dependency modules. Missing names, qualified names, non-symbol entries
and literal DATA declarations are not callable entries. Function arity and the
lexical module scope still apply. Cross-module calls remain limited to actual
public exports and their validated routes, even when the root entry is private.
This permits focused compiler-kernel checks after extracting a stage-owned
namespace without exporting test-only functions or rewriting source. It does
not grant checked user-program execution or stable private-import access.

The subset is fixed-arity, unannotated `defn` modules in `:meta/:jvm` or
`:hosted/:jvm`, with caller and dependency using the same profile and target.
It supports direct qualified imported calls and source-local literal DATA declarations
of the form `(def name literal-data)`. Scalars, nested vectors/maps/sets and
exact quoted DATA are materialized before body compilation for unqualified
module-local reads; quoted symbols,
lists and call-looking payloads remain inert. Parameters and lexical bindings
take precedence over module DATA. Actual C5 var export descriptors are retained,
but exported DATA is not callable and imported DATA reads remain unsupported.
This is an immutable seed subset, not a language-wide restriction on `def`.
Arbitrary initializer calls, bare-symbol aliases, forward/dependency initializer
references, duplicate definitions, variadics and source/seed operator-shadowing
mismatches reject. Imported calls in symbol-only sequential `let` and `loop`
initializers and bodies are supported, including nested scopes, shadowing and multiple body
forms. Initializers see earlier bindings, not later ones; argument evaluation
remains once, left-to-right. Loop calls also retain their distinct paths through
conditions and `recur` arguments; nearest targets, tail position and arity still
come from the existing recur validator/runtime. Imported calls under nested
functions, maps or sets are not yet supported; function-body vector expressions
are supported. Forwarding nested DATA through a parameter is distinct from those
executable forms. Quoted DATA remains inert. General module initialization and
checked runtime linking are separate work, not language-level prohibitions.

For module-call path or sequential-let changes, start with the dedicated cheap
checks and then the affected real composition:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-lexical-call-test \
  --exact stage2-module-let-fixture-parity \
  --exact stage2-module-let-call-path-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-lexical-call-test \
  --exact stage2-module-let-authenticated-composition --fail-fast
```

The second command constructs genuine Gravity caller/callee C5 products and
executes the development module bundle. The paired `.qst` fixtures establish
byte/parser parity only, not a second authenticated execution. Private imports,
wrong arity and a coherently resealed route substitution reject. This temporary
Clojure-seed composition does not establish checked user-call effects, runtime
ownership, authoritative migration or self-hosting.

For loop-call paths and recur diagnostic changes, use:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-loop-call-test \
  --exact stage2-module-loop-fixture-parity \
  --exact stage2-module-loop-call-path-development-kernel \
  --exact stage2-module-loop-recur-diagnostics-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-loop-call-test \
  --exact stage2-module-loop-authenticated-composition --fail-fast
```

The normalized kernels cover paths, sequential scope and complete diagnostic
parity with the original body validator. On malformed loop bindings, the
walker delegates to that validator over the whole definition so an earlier
recur or throw error is not hidden by a later loop error. Source location comes
from the verified module, not a new authority channel. Valid compilation does
not add another validation pass. The separate composition exercises actual
outer and nested loop backedges, ordered effects and qualified imports; it is
not a termination proof or checked user-call effect analysis.

For closure reconstruction or transitive-route changes, use the separate
focused selection and one affected three-module composition:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-transitive-call-test \
  --exact stage2-module-transitive-fixture-parity \
  --exact stage2-module-transitive-closure-development-kernel \
  --exact stage2-module-transitive-routing-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-transitive-call-test \
  --exact stage2-module-transitive-authenticated-composition --fail-fast
```

The cheap kernels use normalized routing DATA and explicit substitutions;
they do not demonstrate original C5 verification. The composition constructs
genuine leaf, middle and root C5 artifacts, then exercises three-module execution,
source-bound exports, private-import rejection and a coherently resealed
transitive-route mutation before argument effects. Paired Q sources provide
byte/parser parity only. Run this composition when those boundaries change,
not for unrelated semantic edits or to refresh authoritative evidence.
For imported calls inside ordered vector expressions, use:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-vector-call-test \
  --exact stage2-module-vector-fixture-parity \
  --exact stage2-module-vector-call-path-development-kernel \
  --exact stage2-module-vector-recur-diagnostics-development-kernel --fail-fast
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-vector-call-test \
  --exact stage2-module-vector-authenticated-composition --fail-fast
```

Vector elements retain zero-based callsite paths and left-to-right evaluation,
including nested vectors, let initializers and loop bodies. Quotes remain
inert; vectors inside blocked function/set contexts do not escape those
fences. An enclosing Map requires the derived context described below.
This is function-body expression support, not executable top-level
`def` initialization. Declared allocation/IO metadata does not establish
checked effects or provider permissions; the seed boundary remains unadmitted.

Effects extraction still needs its exact remaining call-context coverage and a
concrete source/token-bound fit; transitive, loop and vector routing do not
close that dependency or the retained C9 source failure.

For evaluated Map literal order in the module-development constructor, edit
`00575-p15-s23-stage2-module-map-order.clj` and its existing recur, expression
compiler and `00580` reconstruction consumers. Start with:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-map-order-test \
  --exact stage2-module-map-order-fixture-parity \
  --exact stage2-module-map-order-development-kernel \
  --exact stage2-module-map-order-diagnostic-kernel --fail-fast
# Only when this source-order/reconstruction boundary changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-map-order-test \
  --exact stage2-module-map-order-authenticated-composition --fail-fast
```

L2 expression order is not a Map enumeration guarantee. After fresh original
C5 verification, the constructor joins retained C2 child occurrences to the
actual `defn` expansion and derives Map evaluation/diagnostic order separately
from host-map callsite coordinates. Public reconstruction repeats the join;
there is no new caller-supplied proof input. No-import dependencies are also
reconstructed with the current source-order context: incompatible legacy raw
plans reject.

The module subset supports distinct literal String or Keyword keys, including
mixed Maps, in evaluated function-body Maps. Computed, numeric, Symbol and
collection keys remain unsupported here, not forbidden by L2. This supersedes
the initial Keyword-only module restriction; it does not widen the separate
canonical typed-Gravity Map subset. Quoted Maps remain inert. The traversal's
depth-64 and 65,536-visit guards are local
constructor bounds, not language limits. Raw three-argument/B51 behavior and
public bundle interfaces remain unchanged. This Map-order-only correction did
not enable imports inside Maps; the subsequent Map-value increment is below.
Neither increment establishes checked effects or Effects token-bound fit.

For qualified imported calls in supported Map values, change the same
`00575` Map-order component and existing `00580` module-source/module-bundle
owners. Use the dedicated focused checks first:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-map-call-test \
  --exact stage2-module-map-fixture-parity \
  --exact stage2-module-map-reference-development-kernel \
  --exact stage2-module-map-context-fences-development-kernel \
  --exact stage2-module-map-wrapped-arity-diagnostic-development-kernel --fail-fast
# Affected source resolution -> module construction -> runtime boundary:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-map-call-test \
  --exact stage2-module-map-authenticated-composition --fail-fast
```

Keep three coordinates distinct: original C5 host enumeration, seed-host
callsite paths, and C2 source evaluation order. Repeated callee names cannot be
joined by symbol counts or a source-ordered zip. For Map-bearing functions,
project the complete actual expanded owner syntax and match every original
reference, including unqualified references, by request ordinal, identity,
position and spans. Bridge literal keys to seed-host slots, then check imported
call origins and arity in source order using those original C5 references.
Original binding, scope, export, profile and target facts remain C5-owned.

Map values compose with existing let, loop, recur-argument and Vector paths.
Quote payloads remain inert. Set and nested-function fences remain inherited
both inside and outside Maps; a missing derived context does not enable the
new walker. Public construction derives it after fresh verification, never
from a caller's acceptance flag. Existing recur/throw pass precedence is not
replaced with a global cross-pass source-error ordering rule.

For literal String/Keyword key-domain changes, the owner remains `00575`;
the source/seed layout handoff belongs to existing `00580` module-source.
Use its dedicated paired-fixture, reference and rejection checks first:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-literal-map-key-test \
  --exact stage2-module-literal-map-key-fixture-parity \
  --exact stage2-module-literal-map-key-reference-development-kernel \
  --exact stage2-module-literal-map-key-rejections-development-kernel --fail-fast
# After the authenticated-source/seed layout handoff changes:
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-literal-map-key-test \
  --exact stage2-module-literal-map-key-seed-handoff-development-kernel --fail-fast
# Only for the affected source/composition boundary:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.stage2-module-literal-map-key-test \
  --exact stage2-module-literal-map-key-authenticated-composition --fail-fast
```

Keep C2 scalar kinds and decoded values joined without coercion: String
`":tag"` and Keyword `:tag`
are different keys. Lost duplicate occurrences, including differently escaped
spellings decoding to the same String, reject the occurrence bijection rather
than silently borrowing host materialization. These supplied-record checks do
not replace genuine reader/source composition.

Equal Map values can have different C5 and seed host layouts. Select seed
definitions from the independent macro forms only after the complete equality
check against verified forms; retain original syntax/reference authority.
Derive C2-to-seed slots from that actual seed definition, rather than relaxing
the strict host-key join or assuming value equality fixes entry coordinates.

Full reference projection and traversal share a local 65,536-unit allowance
and depth-64 guard; unsupported expansion/header shapes reject rather than invent C5
references. These are bounded development frontiers, not new language limits
or preservation of every previously accepted module capacity. Raw3/B51,
runtime and public interfaces are unchanged. No new C5 resolver, admission
lane or proof cache is introduced; use one changed composition, not an
unchanged source matrix, when this producer/consumer boundary changes.

Use the existing runtime test namespace, not an admission runner:

```bash
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-root-private-entry-development-kernel \
  --exact compiler-module-route-development-kernel \
  --exact compiler-module-seed-shape-development-kernel \
  --exact compiler-module-b51-dispatch-development-kernel
# After literal declaration or local lookup changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-literal-data-development-kernel \
  --exact compiler-module-literal-data-existing-source-kernel
# Whole current runtime source: DATA conservation and real runtime execution:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-whole-runtime-data-development-kernel \
  --exact compiler-module-literal-data-development-kernel
# Run after module input, compilation or dispatch behavior changes:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-authenticated-source-and-runtime-composition
# Root-entry behavior, including private root functions and public dependencies:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-root-private-authenticated-paired-composition
# Affected literal DATA composition and actual C8 compatibility:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-literal-data-authenticated-composition \
  --exact compiler-module-literal-data-actual-c8-composition
# After the module profile/declaration/origin joins change:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-profile-source-development-kernel \
  --exact compiler-module-profile-origin-development-kernel
# Affected hosted caller/leaf composition, with one meta/JVM regression:
clojure -J-Xmx2g -M:dev-test \
  --namespace gravity.self-hosting.sh01-stage2-runtime-iteration-test \
  --exact compiler-module-hosted-authenticated-paired-composition
```

The direct kernels use normalized inputs and dispatch instrumentation; they
are not authentic source or admission evidence. The separate composition
constructs actual caller/callee C5 artifacts and independent plans. Bundle
records retain the temporary Clojure seed/interpreter boundary and unchecked
effects status. Neither path grants SOURCE/B51 authority, checked imported-call
legality, target support, migration acceptance or seed retirement.
The compile-time literal table is not another raw-plan field: actual source
identity binds unused declarations, while embedded literal instructions bind
values a function reads. Changing an unused catalog therefore changes module
identity without changing an otherwise identical function-semantic projection.
The whole-runtime check parses compatibility catalog DATA and compiles the
entire actual `gravity.runtime` leaf once, not the older selected-function
projection. The separate two-file composition validates the compatibility
module's actual bundle and routes. Explicit catalog quotes preserve symbolic
DATA without evaluating labels as variable references. Conservation checks are
development regression oracles, not refreshed authority pins. After evaluated
vector support intentionally changes one evaluator body, the current check
compares the unchanged 21 leaf/29 reconstructed original function bodies and
all catalog DATA; functional vector checks cover the changed owner. The earlier
whole-body conservation result stays attributed to its original revision. The
historical pinned runtime adapter remains separately stale and fail-closed;
repairing its byte counts, definition set or contract hashes is not a prerequisite
for this current-source check.
Do not refresh authority pins to account for redundant development metadata.
Run only the affected composition rather than replaying unchanged source proofs
to refresh an aggregate result.

Ordinary focused tests do not require fresh authority proofs, frozen migration
fingerprint refreshes, workstream receipts, broker setup or a new routing record.
Current-source execution must retain actual identities; it must not inherit a
stale frozen authority claim. Source, dependency, tool and policy changes still
conservatively invalidate prior authoritative evidence. Record implemented
behavior and focused results separately from migration and release status.

For example, `sh07-b47-c7-stage2-plan-identity-is-exact` in
`sh07_c7_type_source_coverage_test.clj` pins a historical C7 source revision,
247 functions and exact plan/function hashes. It is a migration identity check,
not the current C7 semantic loader. Do not refresh those pins or rebuild that
proof before a local type-rule edit. Changed source conservatively invalidates
its current-authority claim. Independent current-source checks in the same
file, such as control-form arities, remain applicable; this is not a blanket
exclusion of the test namespace. Current C7 semantic tests must execute the
actual dependency bundle and retain private-kernel regression coverage.
Ordinary record/match fixture checks compare that bundle's retained root source
path/text with the current file and inspect its actual function members. They
must not require refreshing a stale source-byte/hash golden merely to run a
semantic regression. Keep fixture parity, current-source fidelity and semantic
negative checks; retain exact historical fingerprints in their migration tests.

The stage2 interpreter's existing pure DATA predicates, collection operations,
integer quotient and vector slicing are ordinary execution facilities, not
plan-admission gates. Inspecting an argument with `map?` or `contains?` does
not authenticate the caller or its result. Both generic and unary dispatch keep
their arity, evaluation-order, normalization and contained-error behavior.
Historical pinned-plan checks, compiler-internal instruction provenance, SOURCE
admission and external-call authority remain separate checks at their owning
boundaries. No module-name spoof, caller authority flag or fresh migration is
needed to execute a current-source C7 development test. This supersedes old
attempt-specific expectations that raw pure-predicate execution itself proves
or requires M3 admission; their immutable evidence remains historical.

This section supersedes historical monitor/attempt instructions insofar as they
treated supplemental admission policies as prerequisites for ordinary semantic
development. Immutable attempt reports, terminal records and old proofs remain
history; they are not rewritten or reactivated. Workstream admission still
governs actual admission. Impact planners, caches, warm workers, profiling and
bounded resource runners below are optional tools, not the default inner loop.
Bound and serialize genuinely heavy work to protect the host, rather than
requiring lightweight tests to maintain heavy-run admission state first.

## Repository and explicit admission checks

### Product construction versus scheduling overhead

The ordinary C5 adapter shares identity chunks only within one constructor or
bounded-verifier invocation. Its existing public identity/summary helpers still
recompute freshly and both fresh C5 gates on the public C6 path remain. When editing these equations,
start with the bounded equation-only kernel, then select the affected real
consumer separately:

```bash
clojure -J-Xmx512m -M:dev-test --namespace gravity.self-hosting.sh06-resolution-adapter-test --exact sh06-product-identity-chunks-preserve-old-equations --fail-fast
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh06-resolution-adapter-test --exact sh06-invocation-local-chunks-preserve-fresh-verification --exact sh06-authenticated-resolution-is-the-c6-consumer-boundary --fail-fast
```

The first compares the old chunk/identity/summary equations at bounded chunk
edges. The second constructs a genuine tiny artifact and exercises fresh
verification, mutations and the public hosted-C6 consumer. It is not the
large checked-Core source subject. Preserve malformed/preflight, stale-envelope,
binding/order/span and source-read regressions when changing their owners.
The [evidence reconciliation](self-hosting-sh00-sh06-evidence-audit.md#bounded-invocation-local-projection-sharing)
records the actual selected results and their limits. This local factoring is
neither acceptance caching nor measured proof that chunk construction dominates
Core composition; do not rerun the unchanged large subject to refresh a stamp.

Some SH-07 tests build large authenticated products before reaching a semantic
assertion. The backlog records a 9,172,831-node reader-source template and a
later 17,681,671-node B47 template. Another lane or lock does not remove that
construction cost. The parked SOURCE draft separately exposed repeated scans
over 2,994,224 blob bytes: seventeen eight-unit scans alone would charge
407,214,464 metadata operations against its unchanged 67,108,864 cap. This is
a deficit in that draft, not a prerequisite for ordinary language development.

The quoted-pattern results documented in `7ccc15c3` separate this further: four
selected tests executed in 314.985 seconds after 6.990 seconds of namespace
loading. The accepted fixture artifact was already shared between test vars;
the current-source compiled plan is also reused. Static tracing identifies
repeated form-value traversal in `sh07-core-build-form-tree`, argument/result
metadata stripping in `sh07-core-execute!`, producer replay in template and
resolved verification, and canonicalization/hashing in digest resolution.
Repeated DATA fields can share objects: these call sites do not establish
allocation counts, runtime dominance or a speedup opportunity by themselves.

The imported-call type slice provides another concrete separation: its five
direct kernel tests executed in 0.893 seconds after 7.131 seconds of namespace
loading, while its one real-source composition test executed in 950.667 seconds
after 6.963 seconds of loading. The latter constructs paired modules and three
source mismatch variants and checks fresh composition and replay. These are
different workloads, not a speedup comparison or a measurement of which
construction phase dominates. Use direct kernels for iteration and this
affected composition for interface changes; adding scheduling machinery would
not remove its product-construction work.

The homogeneous-List composition later spent 1,211.036 seconds in its selected
test; the prepend slice's combined focused batch spent 13.700 seconds in test
execution. These are different workloads, not a speedup comparison. The prepend
source selector deliberately constructs eleven distinct sources, then reuses
the accepted products for runtime cases. `fresh-type-products` constructs and
verifies C5/C6/C7 products; `sh08-verify-function-type-result` reconstructs the
expected typed result. Coordinator and C8 binding preimages embed overlapping
request, artifact and verification DATA. Sharing immutable objects does not
remove repeated canonical serialization occurrences. The existing
`p15-s23-c11-mir-digest` also realizes canonical DATA and a complete `pr-str`
before hashing. These call sites identify candidates, not measured dominance
or permission to omit fresh verification.

Alongside the next necessary changed composition, a bounded cost investigation
can time/count those existing construction, verification and digest boundaries
without printing products. If canonical realization is material, investigate
only its transient representation/lifetime first, preserving exact bytes,
digests, ordering, diagnostics and hostile checks. No extra unchanged proof run,
accepted-result cache, new lane or wrapper is required by this investigation.

The new fixed-DATA case in the existing hosted C6 leaf namespace isolates one
representation mechanism without itself constructing compiler products:

```bash
clojure -J-Xmx512m -M:dev-test \
  --namespace gravity.c6-core-lowering-test --fail-fast
```

Its four fixed, guarded DATA chains use the production flattener. For one,
two, four and eight nodes, nested node occurrences in the flat table are
1, 3, 10 and 36. Observed root-vector / flat-table UTF-8 bytes are 94 / 94,
192 / 285, 388 / 961 and 780 / 3,489. Full rows are checked before traversal
or printing, with conservative pre-print and final 256 KiB bounds. The whole
leaf namespace passed five tests / 114 assertions. This establishes small-case
printed amplification, not unique heap size, timing, actual Checked-Core
size, failure cause or a reason to replay the interrupted large subject.
The returned hosted C6 keeps a selected C5 view, not the full SH05/C3 ancestry;
do not attribute all upstream bytes to its whole-artifact hash. Any future
representation correction must inventory consumers and preserve required
identity, provenance, diagnostics and fresh verification, or explicitly version
and invalidate changed evidence. No representation redesign is selected here.

The typed-catch composition exposed a separate retention limitation. Rendering
its first original request, typed result and verification report as one EDN
string produced 43,392,705 characters. The retention attempt used the existing
16,777,216 scalar-unit bound as its serialized-text cap, so it wrote no file.
This is not a measurement of UTF-8 bytes or unique retained objects, nor proof
that the compiler's aggregate carrier bound was exceeded. The observed accepted
type/effect statuses and passed verifier report do not establish the aggregate
test verdict. Do not enlarge bounds, strip embedded proof DATA or rerun an
unchanged proof merely to recover that optional file. Before redesigning this
representation, inspect expected/candidate duplication, identity preimages,
source copies and the applicability of value versus serialized-output limits
using available data and construction code. Keep that bounded investigation
separate from semantic implementation and scheduling changes.

One such construction issue is already corrected in `7aa24451`: the six SH01
type-only callers use `fresh-type-products` in `sh09_body_effect_test.clj` to
construct genuine C5/C6/C7 inputs without the unused SH09 binding seal. The
existing `fresh-products` helper adds that unchanged seal for consumers that
need it. Focused helper and schedule checks cover the split; the earlier
authentic composition remains evidence for its original test revision, not a
replay of the revised harness. This removes statically unused construction,
not a measured runtime bottleneck or a claimed speedup. No new lane, cache or
authority shortcut is involved. Do not change a helper used by a live frozen
composition.

Bounded follow-up: observe one existing authentic-composition test on one
stable revision. Separate upstream/projection, metadata stripping, Gravity
build/replay/resolve, canonicalization/hashing, and final fresh verification
only as needed for the concrete decision. Phase time, allocated/retained bytes,
DATA occurrences versus unique objects, and canonical byte totals are useful
where observable; collecting every metric is not a prerequisite to fixing a
directly observed construction failure. Use existing interfaces and tests, not
another lane or wrapper. Justify the changed representation or lifetime from
that evidence and preserve identities/equivalence and negative tests.
Do not substitute child hashes for embedded DATA, cache accepted proofs, or
change wire formats in this simplification. Heavy runs should answer a concrete
representation or migration question, not refresh historical receipts merely
because another development commit exists.

For example, the current loop-type composition failed at the 2 GiB limit before
semantic assertions. Narrowing to the ordinary C2 reader and observing the
printer's caller identified `p15-s23-c6c10-canonical-record` materializing its
complete canonical text during SH03 digest resolution. The digest caller only
needs a hash; the identity caller separately discards that text. This is
product-construction work, not scheduling overhead. A bounded correction may
avoid those unused strings while preserving the record API, exact canonical
form, UTF-8 bytes, hashes, limits and diagnostics. Require focused legacy/new
parity and the specifically failing source boundary at the unchanged heap;
do not infer a general allocation bound, speedup or semantic pass from the
identified callsite or a proposed streaming implementation.

```bash
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M tools/validate_workstream_governance.clj
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M:test
git diff --check
```

Run checks appropriate to the changed files. Documentation changes require the
document validator; tooling changes require affected tool tests. The full suite
serves broad integration/release decisions, not every semantic edit, and does
not itself establish release, self-hosting, seed-retirement, performance or safety.

## Explicit full fresh integration candidate check

Focused composition tests provide integration-development feedback. To publish
the full fresh-candidate evidence described here, explicitly run the exact
committed candidate through this existing command. It is not a prerequisite
for local semantic or focused composition tests:

```bash
clojure -M tools/verify_integration_candidate.clj \
  --base-ref BASE_COMMIT \
  --candidate-base BASE_COMMIT \
  --candidate-commit CANDIDATE_COMMIT \
  --candidate-tree CANDIDATE_TREE
```

The runner first applies integration-mode worktree preflight to those exact
identities. It then checks out the exact committed tree through a temporary
Git index into a new directory,
uses `-Srepro -Sforce`, runs the existing full `clojure -M:test` suite without
narrowing it, and runs the document, roadmap, governance, and language-boundary
gates in that tree. It also runs `git diff --check` over the exact base-to-
candidate range in the preflight worktree. Existing repository target output
and incremental runner caches are not visible to the exported tree; the
temporary tree is not resumed.
Candidate `.gitattributes` files are rejected so checkout filters cannot make
the verified bytes depend on local Git configuration.

The receipt is written once to
`target/validation/integration-fresh-verification/CANDIDATE_COMMIT/receipt.edn`.
It records the candidate identities, fixed command list, exit codes, elapsed
times, fresh-tree policy, and explicit nonclaims. A prior receipt is never
overwritten or resumed. `C16-SPECULATIVE` rejects speculative, resumed, or
repository-local-cache evidence at this publishable boundary.

For CI, provide the reviewed base SHA, candidate SHA, and candidate tree SHA
from immutable job inputs and upload the receipt when the lane reaches receipt
publication, including a failed verification receipt. Usage, identity, or
receipt-publication errors can fail before a receipt exists.
CI must also impose its own job timeout; an externally killed verifier cannot
finish or publish a receipt.
Do not derive a publishable candidate identity from a mutable branch after the
job starts. For local admission preparation, commit first, keep the named
branch clean, and pass the same identities intended for the workstream ledger.

This lane supplies candidate-only integration evidence. It does not replace an
independent accepted review or grant release, self-hosting, seed-retirement,
safety, performance, Stage3, or SH-07 authority. When the owning contract
requires a Stage3 or SH-07 proof, run its existing fresh/no-resume command
separately and record that receipt in the governed evidence bundle.
`contracts/workstream-governance.json` still requires owned paths, governing
contracts, accepted and rejected fixtures, stable diagnostics, successful
checks, independent acceptance, residual boundaries, and explicit nonclaims
in addition to exact Git identities; `WG009` through `WG012` fail closed when
that admission evidence or authority boundary is incomplete.

The fresh lane also emits optional bounded observability. During the full suite
it gives the child test runner a temporary progress path, which records the
currently running namespace/test var, and the parent prints sparse heartbeats
with elapsed time, descendant RSS, and RSS high-water. These records are
diagnostic-only. Process sampling inspects at most 256 process identifiers and
records when that view is truncated; the host `ps` invocation is separately
time-bounded. Telemetry does not resume work, read repository caches, select
tests, change exit status, or add integration/release authority. The final
receipt keeps the existing identities, command list, status, and exit-code
semantics; the additive `:observability` field is safe to ignore when telemetry
is unavailable.

## Focused Development Runners

Use the reviewed Clojure aliases:

```bash
clojure -M:incremental-check
clojure -M:incremental-check --base-ref main
clojure -M:sh01-test
clojure -M:leaf-test --group foundation-reader
clojure -M:dev-test --catalog
clojure -M:project-structure-runner-unit
clojure -M:project-structure-test --help
clojure -M:stage3-verification --help
```

`clojure -M:incremental-check` passes the current tracked and untracked changed
paths to the existing SH-01 impact planner. `--base-ref REF` additionally
includes committed branch changes from `merge-base(REF, HEAD)..HEAD`, so a
clean candidate branch still produces a plan. Discovery records the resolved
base and merge-base plus committed, tracked-working, and untracked path sets.

The deterministic explanation keeps SH-01 ownership and downstream slice
closure as governance metadata while using the reviewed
`contracts/stage0-incremental-test-dependencies.edn` map for component-level
development invalidation. A reviewed component source selects its declared
leaf and compatibility tests. A present dedicated test-file change selects its
exact namespace. Deleted dedicated tests retain conservative slice fallback;
malformed dependency records and relevant unowned paths fail closed. JVM
groups in the reviewed map keep bootstrap-free tests isolated from tests that
load the bootstrap facade. The plan and execution report are explicitly
non-authoritative and cannot satisfy authoritative migration, release,
self-hosting, or seed-retirement gates.

The SH-01 impact runner batches only normal namespaces from the same slice in
warm JVMs, with a reviewed maximum batch size of eight. `:memory-heavy` work
retains its capacity-one fresh-process lane, and `:exclusive` work remains
fresh and sequential after the parallel lanes drain. Batch reports preserve
namespace fixture boundaries, deterministic fail-fast skips, bounded output,
and explicit non-authority.

### Retained SH-07 B8 regression wrapper

This older, route-specific wrapper runs the
two affected test vars in one bounded child JVM, allowing the existing
process-local fixture/artifact cache to be shared:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-bounded-development-runner \
  --route b8-regression \
  --coordination-root "$GRAVITY_SH01_COORDINATION_ROOT"
```

The route is memory-heavy and therefore requires the reviewed SH-01 host
resource broker. Its child receipt reports the exact selected-var count and
the number of calls to the SH-07 artifact constructor; both values are
development observations only. Unknown routes, mixed namespace/route
selection, and extra options fail closed before launch. Admission also compares
the route owner with the live `SH-07` leaf owner and binds its path/namespace to
the current test dependency catalog; drifted, missing, malformed, or extra
owner metadata fails closed. These restrictions govern this wrapper, not
ordinary edits or direct `:dev-test` selections. Use the default three-boundary
workflow above for current development: focused checks first, affected
composition when behavior or interfaces change, and fresh authoritative
verification for an explicit stable-module migration. A complete SH-07
namespace run is a broad regression decision, not a prerequisite for every
semantic change. This wrapper and its cached products remain non-authoritative.

## Bounded P15 Profiling Receipt

The bounded pass-execution serialization observation uses its existing accepted
receipt fixture and one widened metadata fixture:

```bash
clojure -J-Xmx512m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M tools/profile_pass_execution_sort.clj
```

It uses the shared normal-resource broker, a 120-second process watchdog,
separate serialization accounting, and seven warm timing rounds per fixture.
Exact source, tree, file, harness, classpath, JVM, and heap identities accompany
its non-authoritative output. See the [comparison report](artifacts/phase-15/reports/pass-execution-canonical-sort-work-v1.md)
for two-process baseline/candidate receipts, variance, profiler limitations,
preservation tests, and the ranked remaining bottlenecks. This observation
does not establish full-suite or SH-07 performance or verification authority.

The fixed P15 stage2 profiling slice is a development-only observation over
`bootstrap/clojure/fixtures/accepted/core-app.gravity`. It returns an EDN
receipt with static stage2 instruction/function-call counts, finite static
function-frame depth, request-scoped proof-DAG hit/build accounting, phase
duration observations, and JVM thread-allocation observations when available:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e '(require (quote [gravity.self-hosting.p15-bounded-profile :as p])) (prn (p/run-profile))'
```

The receipt id covers only stable source/accounting fields; duration and
allocation remain observational. This slice neither changes nor reads the
development result cache or fresh-verification authority. It is not benchmark,
performance, allocation/stack-bound, stage-advancement, self-hosting, or
seed-retirement evidence. Its DAG counters cover only the request-scoped
P15 source-data node, and its frame-depth field is the static direct-call
depth from the fixture entrypoint, not a runtime stack measurement.

## Explicit SH-07 Cold-Build Phase Telemetry

The SH-07 cold-build telemetry slice is an opt-in diagnostic around one
`sh07-core-file-artifact` invocation.  It is intended for a long C6 source
build, not for the ordinary test suite.  Run it only when the requested source
path and its JVM have been reserved for this diagnostic:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh07-cold-build-phase-telemetry --source PATH
```

The receipt aggregates a fixed set of SH-06, SH-07, and Stage2 plan phases and
the three declared SH-07 digest purposes plus a bounded `:other` bucket.  Each
digest row records call count, elapsed time, failed calls, and capped counts
for large preimage collections such as forms, nodes, bindings, resolutions,
and children.  A synchronous `:on-progress` callback can print sparse phase
and digest records while a long phase is running; callback failures are
swallowed and a maximum of 256 progress records is retained by default.
The collection bound is checked before realization, so an accidental infinite
or unexpectedly wide sequence cannot make the observer unbounded.

The profiler uses temporary process-local root-Var wrappers under a private
lock.  It does not alter the observed source, plan, digest, proof, diagnostic,
cache, or authoritative runner, and it does not memoize or reuse an artifact.
The `:cold-plan-binding-realized-before?` field indicates whether the pinned
SH-07 plan delay was already realized in that JVM; use a fresh JVM when a cold
binding observation is required.  Elapsed values and the source path are
host-variable observations.  The phase and digest accounting is diagnostic
only and grants no benchmark, performance, proof, integration, release,
self-hosting, seed-retirement, or cache authority.  No full or multi-hour
SH-07 run is part of the default telemetry tests; those tests exercise the
observer through bounded synthetic seams.

## Explicit Stage2 Emitter Phase Benchmark

The real Stage2 emitter benchmark is an explicit, bounded development
observation; default-suite coverage uses only lightweight seams. Run at most
three fresh emissions of the accepted hosted-core fixture with:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-stage2-plan-emitter-benchmark --iterations 1
```

Each sample retains the existing whole-emission elapsed time and optional
current-thread allocation observation. Deterministic accounting is limited to
the source/phase call counts and semantic receipt. The benchmark reports source
kinds `:authenticated-envelope`, `:syntax`, `:plan-emitter`, and `:other`;
`:other` includes the requested fixture and any unclassified source rather than
silently dropping it. Elapsed time, allocation availability/bytes, and Java and
Clojure runtime versions are host-variable observations. Safely observable
phases are macro parse/expand, function-table construction, function lowering,
instruction summary, canonicalization, and hashing. The emitted plan, semantic
receipt, cache scope, and fresh-emission behavior are unchanged; nested calls
are attributed once to their outermost observable phase.

The temporary `with-redefs` wrappers are protected by a private process-local
lock, so simultaneous benchmark requests in one JVM serialize rather than
overlap. This isolation applies only to explicit benchmark invocations; it does
not make the global Clojure Var replacement an authority boundary.

This is diagnostic profiling only. It supplies no benchmark baseline,
performance improvement/regression, allocation bound, fresh/no-cache
verification, integration, release, self-hosting, seed-retirement, or stage
advancement authority. The Clojure/JVM observer and the remaining Clojure seed
rule runner remain explicit residual boundaries.

## Explicit Stage2 Runtime Execution Attribution

The real runtime-execution profiler is an opt-in, single-iteration observation
over the accepted hosted-core fixture:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -m gravity.self-hosting.sh01-stage2-runtime-execution-profile
```

It attributes the Stage2 runtime calls made during fresh plan emission and one
direct execution of that emitted plan. Deterministic accounting is limited to
the semantic receipt, sparse function/instruction/call-edge rows, row-sum
coverage, source identities, and a bounded plan-ID registry. The hot seams use
preallocated primitive counters and an identity-keyed plan registry: they do
not take clocks, read thread allocation, update atoms or persistent maps, or
bind dynamic function state per instruction. Compiler artifact plans omit
source paths, so the profiler registers at most eight plan identities at the
compiler-plan seam and distinguishes authenticated-envelope, syntax, reader,
plan-emitter, emitted-plan, runtime-artifact, and explicit `:other` execution.

Elapsed time, runtime versions, and sampled inclusive function costs are
host-variable. Allocation and elapsed samples are taken only once per 8,192
calls of a function; they are inclusive, sparse, and not a cost ranking or a
sum of the work. Exact instruction attribution is count-only. The output
permits one sample, 128 function rows per source (including an overflow row),
32 instruction operations, 255 named call-edge rows plus one exact overflow
row, and eight plan identities.
The receipt reports off-owner-thread events separately and excludes them from
the owner-thread counters. Counters and sampled sums saturate instead of
wrapping. `:counter-overflow?` makes row reconciliation explicitly incomplete;
the separately host-variable `:sample-overflow?` marks sampled-cost saturation.

Temporary root-Var wrappers are serialized by a private process-local lock.
The lock prevents two explicit profiler requests from overlapping in one JVM;
it does not make global Var replacement safe for unrelated concurrent work.
Do not run it alongside unrelated same-JVM execution. It grants no fresh/no-
cache, performance, allocation-bound, cost-ranking, integration, release,
self-hosting, or seed-retirement authority.

The profiler also exposes an opt-in targeted-cost view for the two currently
authenticated clusters: `:authenticated-envelope-digest-cluster` and
`:syntax-c3-lowercase-hex?`. Selection is predeclared and bounded (sample
stride at most 4096); primitive counters count every selected call while host
elapsed/allocation values are sampled only around selected spans. Targeted
rows remain inclusive and non-exclusive. A ranking is withheld unless both
clusters have nonzero deterministic counts and no sampled-cost saturation;
unsupported targets and unbounded strides are rejected.

`clojure -M:incremental-check` enables the thin development-loop wiring. In
the parent, before any child JVM can launch, it computes one conservative
SHA-256 identity from Git's content-addressed index plus the bytes and modes of
only dirty, deleted, and non-ignored untracked paths. Clean tracked content is
represented by its index mode, blob identity, stage, and repository-relative
path, so repeated snapshots do not reread unchanged files. Unsupported index
states, including unmerged entries, special tracked modes, assume-unchanged,
skip-worktree flags, and repositories configured to ignore file-mode changes,
fail closed to the complete content-and-index-hashing snapshot. The same
fallback applies when automatic CRLF conversion or any tracked-path Git
attribute could transform clean checkout bytes.
Repository symlinks fail closed because their followed targets are outside this
declared closure. That identity supplies the complete production, transitive,
fixture/contract, runner, and classpath closure in each closed cache request;
the request also
binds the test unit, command, external classpath file bytes, Java and command
executable bytes, runtime identities, policy, and bounded timeout. Ignored
generated state is not an input: a test declared deterministic by this
contract must not depend on it or on undeclared ambient environment state. An
injected development-loop context is a test seam; its repository root and
command identity must match the current execution or
`SH01-DEVELOPMENT-LOOP-CONTEXT` fails closed. The reviewed component dependency contract is
the only current source of deterministic cache eligibility. Slice-closure and
ad hoc dedicated tests have no such declaration and therefore execute
uncached. Authoritative,
freshness-required, performance, proof, and nondeterministic policies always
execute and are never stored.

The contract may additionally opt a reviewed deterministic test into an
explicit cache closure. Such a closure lists every production, transitive,
test, fixture, runner, contract, and internal-classpath input directly; the
runner never infers Clojure `require` edges. The content key sorts and hashes
the declared repository-relative paths, roles, executable modes, and bytes,
and also binds the dependency-contract schema, runner, semantic command,
policy, external classpath, and runtime/tool identities. Internal classpath
order, alternate `.cljc` and compiled-class shadows, data-reader resources,
unexpected internal class files, and the self-hosting test-catalog path set are
also bound; inventories are compared before and after closure reads. Absolute
worktree paths are excluded, so identical declared inputs may reuse a result
across Git worktrees. Missing, malformed, unknown, unowned, or undeclared
closures retain the complete-repository identity. A detected read or inventory
race fails closed rather than falling back. The initial and post-operation
repository snapshot checks remain complete but incrementally observed. Reports
record the snapshot engine and exact numbers of paths and bytes rehashed, in
addition to complete path observations. The reviewed set covers
`gravity.c11-mir-test`, `gravity.compiler-pass-manifest-test`,
`gravity.bootstrap-compatibility.c11-test`, and
`gravity.self-hosting.sh01-compiler-pass-manifest-compatibility-test`. The two
compatibility closures enumerate `gravity.bootstrap`'s complete internal eager
namespace load set and the exact fixture used by each test. Mixed batches and
authoritative, freshness-required, performance, proof, or nondeterministic
work retain the complete-repository identity and remain uncached.

An immutable parent probe removes an existing valid hit before executor
submission, so that hit acquires no broker lease and launches no child JVM. A
miss is rechecked by the cross-process per-key singleflight at execution time;
concurrent identical checks can therefore race at the probe without producing
duplicate child work. Only a successful, deterministic, non-authoritative
result whose repository snapshot still matches after child completion is
reusable. A parent hit is likewise revalidated before it is admitted. Failures,
timeouts, corrupt entries, incomplete requests, and excluded policies run
again. Reports retain deterministic cache receipts,
the original producer's deterministic broker receipts, planned JVM count,
post-cache planned JVM count, and successful launcher-return count. A launcher
that throws before returning is not counted as a launch; its deterministic
broker admission/release receipts and the parent cache-miss receipt remain in
the failed report.

The shared cache and broker roots are private mode-`0700` directories under
the repository's Git common directory at
`gravity-development-loop-v1/{cache,broker}`. Linked worktrees therefore use
the same cooperative same-user state without a daemon. The default incremental
child timeout is one hour. The direct `clojure -M:test` runner and fresh
integration verifier do not enable this wiring or observe its cache. The test
alias itself does not confer authority.

Fail-fast stops queue refill but does not cancel already-submitted workers that
are waiting for a cache key or broker lease; those workers remain bounded by
the same child/broker timeout and drain under the existing runner contract.
The shared roots, repository snapshot reads, and child-published batch report
remain a cooperative same-user boundary. They do not defend against hostile
same-user path replacement while a check is running. No daemon, shared compiler
blob, or authoritative result store is introduced by this slice.

## Host-Wide Development Resource Broker Foundation

The Clojure-only SH-01 host resource broker is available at
`gravity.self-hosting.sh01-host-resource-broker`. The incremental-check wiring
uses the canonical Git-common root described above. Other callers pass the
same trusted, existing, absolute host-local `:coordination-root` from every
participating worktree:

```clojure
(broker/with-lease
 {:coordination-root "/private/tmp/gravity-sh01-resource-broker-v1"
  :timeout-ms 3600000}
 :memory-heavy
 run-one-development-unit)
```

The coordination directory must be a non-symlink directory owned by the
current user with mode `0700`. The broker creates only direct child policy,
ticket, admission-lock, and slot-lock files. Reviewed host-wide capacities are
fixed at two `:normal` leases, one `:memory-heavy` lease, and one
`:exclusive` lease. An exclusive lease holds every class slot, so it cannot
overlap normal or memory-heavy work. Tickets provide deterministic FIFO
admission within each class; an earlier exclusive ticket blocks later class
admission. Independent classes before that barrier may proceed within their
own capacity.

Waiting is bounded by `:timeout-ms`. OS file locks release on process death,
and a queued ticket is reclaimed only after its lock proves that no process
still owns it. Malformed state and policy mismatches fail closed. Queue,
admission, timeout, stale-recovery, and release events use
`:gravity/sh01-host-resource-telemetry-v1` with `:authority
:non-authoritative`; they are operational observations, not test, proof,
benchmark, integration, release, self-hosting, or seed-retirement evidence.
An injected `:on-event` telemetry callback runs synchronously and must be
nonblocking; callback work is outside the broker's bounded lock-wait claim.
Each acquisition outcome also has a deterministic semantic receipt with schema
`:gravity/sh01-host-resource-non-authoritative-receipt-v1`. The closed receipt
fields are only `:schema`, `:resource-class`, `:capacity`, `:outcome`, and
`:diagnostic-id`; successful release returns the same receipt shape. Absolute
coordination roots, ticket names and sequences, queue positions and lengths,
wait times, and concurrency-dependent stale counts remain observational
telemetry and never enter the receipt. Neither form has evidence authority.
Lease release is exactly-once and thread-affine. Callers must not nest
acquisition; nested behavior is unspecified. Admission is interruptible and
restores the interrupted flag with
`SH01-BROKER-INTERRUPTED`; locks carry no poisoned state, so every acquisition
revalidates the fixed policy and direct-child shapes before admission.

The broker does not launch or execute tests, persist test results, or run a
daemon. The incremental runner holds its lease only across one child process
launch and completion; direct callers hold it around their thunk. A parent
process that dies after launching an uncontained descendant can release its OS
lock while that descendant survives; strict cross-session descendant
containment remains
an OS job/container boundary and is not claimed. The coordination root is a
trusted cooperative boundary: same-user deletion or
replacement of broker state files while leases are active is out of scope and
can bypass inode-based file locks.

Stable fail-closed diagnostics include `SH01-BROKER-ROOT-REQUIRED`,
`SH01-BROKER-ROOT-ABSOLUTE`, `SH01-BROKER-ROOT-INVALID`,
`SH01-BROKER-ROOT-OWNER`, `SH01-BROKER-ROOT-PERMISSIONS`,
`SH01-BROKER-POLICY-MISMATCH`, `SH01-BROKER-RESOURCE-CLASS`,
`SH01-BROKER-STATE-CORRUPT`, `SH01-BROKER-LOCK`,
`SH01-BROKER-TIMEOUT-OPTION`, `SH01-BROKER-TIMEOUT`,
`SH01-BROKER-INTERRUPTED`, `SH01-BROKER-STALE`, `SH01-BROKER-IO`, and
`SH01-BROKER-RELEASE`.

The namespace-lazy development runner can write an opt-in EDN timing receipt:

```bash
clojure -M:dev-test \
  --exact hosted-hello-runs \
  --timing-receipt target/validation/development-test-timing.edn
```

The `gravity/development-verification-timing-v1` receipt separates each
selected namespace's observable require interval from its fixture-wrapped test
execution, and records per-test-var elapsed time, outcome, assertion counts,
selection, and bounded JVM/OS metadata. The JVM-start-to-runner-load interval
is explicitly combined because the bootstrap and runner namespace portions are
not separately observable from inside the runner.

The receipt is non-authoritative scheduling input. It has no benchmark,
performance-regression, proof, conformance, release, self-hosting, or
seed-retirement authority, and it does not make results comparable across
different source, JVM, host, profile, target, or runtime conditions. Omitting
`--timing-receipt` preserves the existing command output and behavior.
Receipts are published only after a selected test run returns normally;
namespace-load or fixture exceptions retain the runner's existing error path
and do not publish a partial receipt.

### Exact-tree warm development worker

Repeated exact development selections can reuse one already-started JVM:

```bash
clojure -M:warm-dev-worker \
  --expected-commit CANDIDATE_COMMIT \
  --expected-tree CANDIDATE_TREE \
  --max-requests 64
```

The worker accepts one closed EDN request per input line. Requests must repeat
the exact startup commit and tree and may use only explicit `--namespace` and
`--exact` development-test selections, with optional `--fail-fast`. It checks
the live commit, tree, and clean worktree before and after every request. Any
failure, dirty checkout, identity drift, malformed request, or request bound
terminates reuse. Output is returned in bounded `WARM_DEV` receipts.
The protocol does not impose an in-JVM test deadline; its supervisor must set
a wall-clock timeout and terminate the worker if a selected var does not
return. A timed-out worker must not be reused.

This worker is non-authoritative development feedback. It never supplies fresh
integration or SH-07 evidence, and it does not alter the cold exported-tree,
authoritative SH-07, release, self-hosting, or seed-retirement lanes.

Stage3 fixed batches run directly through the Clojure runner:

```bash
clojure -J-Xmx2g -M:stage3-verification --batch source-control-form-arity
```

Batch-specific heap requirements and accepted names are enforced by that
runner. Its receipts remain non-authoritative unless a governing contract
explicitly admits the exact result.

## SH-07 And Integration

For bounded cached development feedback, invoke the Clojure iteration runner
directly:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-iteration-cache-runner --help
```

Only the exact four-variable C10 source-coverage batch enables path-neutral
SH-07 carrier reuse. The selection must contain all four variables exactly
once; order is irrelevant. Namespace, subset, superset, duplicate, mixed, and
unknown selections bypass semantic reuse and keep the ordinary path-bound
lane. The runner reads each regular non-symlink source again, binds live
C2/C3/SH-05/SH-06/SH-07 and module-policy pins, admits a carrier only after one
ordinary successful build plus fresh verification, and reconstructs separate
per-path artifacts. Same-key work is single-flight, different keys may build
concurrently, and the process-local FIFO is bounded by four entries and 512
MiB of canonical carrier bytes. Failed, interrupted, stale, mutated, malformed,
or oversized values are not published and may be retried.

Carrier hits still execute the two exact bounded Gravity digest-resolver leaves
needed to rebind artifact and provenance identities. They do not construct a
fresh SH-07 template or cache a final artifact, diagnostic, or verification
report. Result fields `:semantic-cache-enabled?` and `:cache` are operational
telemetry only. Every result remains non-authoritative; a fresh authoritative
run is required only for an actual admission claim. Reuse grants no timing,
benchmark, proof, roadmap, release,
self-hosting, or seed-retirement authority.

The authentic accepted/rejected B8 carrier oracle is intentionally outside the
fast development lane because it performs ordinary full SH-07 construction.
Run that exact evidence only when admission review requires it:

```bash
GRAVITY_SH07_SEMANTIC_CACHE_AUTHENTIC=1 \
  clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -e "(require 'clojure.test 'gravity.self-hosting.sh07-try-catch-test) (binding [clojure.test/*report-counters* (ref clojure.test/*initial-report-counters*)] (clojure.test/test-vars [#'gravity.self-hosting.sh07-try-catch-test/sh07-b8-semantic-carrier-authentic-acceptance-evidence]) (let [r @clojure.test/*report-counters*] (prn r) (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1))))"
```

Without the environment variable, the added carrier evidence variable skips
the expensive construction while the ordinary B8 namespace behavior is
unchanged. The carrier and iteration-cache unit namespaces remain the bounded
default checks.

For a bounded exact SH-07 development selection, use the dedicated process
runner:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-bounded-development-runner \
  --route c6-contract --timeout-ms 900000
```

The route catalog is closed.  `c6-coverage` selects the single authentic C6
coverage test and additionally requires the existing coordination-root option.
The terminal non-authoritative receipt is printed as EDN on stdout; pass
`--progress-file PATH` to retain bounded phase/heartbeat progress while the
child runs.  Timeout, malformed output, failed tests, or missing admission are
always non-passing, and no result is cached.  This helper does not replace the
authoritative SH-07 command or grant proof, performance, integration, release,
self-hosting, or seed-retirement authority.

For a fresh reviewed SH-07 transaction:

```bash
clojure -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' \
  -M -m gravity.self-hosting.sh07-authoritative-runner --fresh all
```

Workstream lifecycle and integration eligibility are governed by:

```bash
clojure -M tools/validate_workstream_governance.clj
clojure -M tools/check_worktree_preflight.clj --mode inspect --base-ref main
```

Inspection is candidate-focused by default. It reports the current candidate
worktree without enumerating registered peers; use `--worktree-scan all` only
for an explicit, bounded repository-wide inventory.

The preflight is read-only. Integration mode requires the exact recorded base,
candidate commit, candidate tree, named branch, and clean worktree. A passing
development command never substitutes for independent acceptance or grants
authority beyond the workstream ledger.
