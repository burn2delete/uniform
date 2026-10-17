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

This distinction follows D2's executable slices, TEST2's pass/composition tests,
D9's separate test/proof evidence, and BOOT3's stage comparison after every
**authoritative module migration**. Compile-time legality, profile, safety,
provenance and release obligations are unchanged.

For SH-07, change `bootstrap/gravity/src/gravity/checked_core.gravity` (the
owning Gravity pass), not only the hosted C6 compatibility facade. The thin
adapter lives in
`bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/00885-sh07-core-source-relative-path.clj`.
Tests are in `bootstrap/clojure/test/gravity/self_hosting/sh07_*_test.clj`;
paired `.gravity`/`.qst` fixtures are under
`bootstrap/clojure/fixtures/self-hosting/`.

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

Selectors apply across all selected namespaces. A global regex can therefore
select more than the exact vars added to a mixed batch; use exact selections
when combining different-cost checks. In the SH08 function-test namespace,
`*-c8-composition` tests construct the public C8 module bundle even though their
inputs are normalized DATA. They are integration checks, not fast
`*-development-kernel` tests. Their bodies and coverage remain available;
renaming them does not require replaying an unchanged expensive proof.

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

Edit `bootstrap/gravity/p15_s23/runtime.gravity` for the local closed evaluator.
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

The runtime check freshly compiles the exact six evaluator function forms from
the current source. It distinguishes the projection's hash from its parent
source hash and checks names, arities and dependency closure. This is not
whole-module compilation: older manifest literal declarations in that file are
outside the direct compiler interface's literal subset. No function is spliced
into an old plan, and no admission pin is refreshed.

The affected composition lowers the unchanged accepted paired throw fixture
with the current Gravity emitter and evaluates its returned instruction with
the current Gravity evaluator. The seed's private throw transport must not
implement guest error transfer. The generic seed interpreter and primitive
boundary remain. Old admitted packet validation still excludes throw; these
development checks do not widen it or establish a target exception ABI,
handlers, resource cleanup, complete profile legality or authoritative migration.

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

### Bounded self-recursive body analysis

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

Build `caller-c5` with the ordinary module-input interface above. Build the
callee plan independently with the raw three-argument constructor and the
same emitter. Pass the resulting bundle to the existing
`p15-s23-stage2-runtime-execute-function` interface. Source reconstruction,
actual C5 public exports/origins, separate plans and exact callsite routes are
checked; a matching name or recomputed hash alone does not establish a link.
The public invocation reconstructs the bundle, and linked-call route and arity
checks precede argument evaluation. Local function names remain module-local.

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

The subset is fixed-arity, unannotated `defn` modules in `:meta/:jvm`,
with direct qualified imported calls and source-local literal DATA declarations
of the form `(def name literal-data)`. Scalars, nested vectors/maps/sets and
exact quoted DATA are materialized before body compilation for unqualified
module-local reads; quoted symbols,
lists and call-looking payloads remain inert. Parameters and lexical bindings
take precedence over module DATA. Actual C5 var export descriptors are retained,
but exported DATA is not callable and imported DATA reads remain unsupported.
This is an immutable seed subset, not a language-wide restriction on `def`.
Arbitrary initializer calls, bare-symbol aliases, forward/dependency initializer
references, duplicate definitions, variadics and source/seed operator-shadowing
mismatches reject. Imported calls
under `let`, `loop`, nested functions or collection expressions are not yet
supported; forwarding nested DATA through a parameter is distinct from those
executable forms. Quoted DATA remains inert. General module initialization and
checked runtime linking are separate work, not language-level prohibitions.

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

### Contract requirements versus supplemental process

D2 requires executable increments and honest milestone ordering; D9 requires
the evidence appropriate to a claim, reproducible provenance and invalidation
of stale evidence. TEST2 requires pass-level accepted/rejected cases and
composition regressions. BOOT3 requires fresh equivalence and stage comparison
at authoritative migration. None makes a new admission receipt or refreshed
migration fingerprint a prerequisite for testing an ordinary pass edit.

Workstream reservations, exact candidate tuples, admission reviews, receipt
schemas and terminal publication come from supplemental admission contracts.
Generated test catalogs and historical attempt-specific grants are additional
tooling/coordination policies, not language semantics. They must not govern
explicit local test selection. The repository's language boundary and source
LOC limit remain implementation constraints; host resource limits still apply
to expensive tests. These are separate from proof authority.

## Repository and explicit admission checks

### Product construction versus scheduling overhead

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

### Exact SH-07 B8 regression route

The current B8 expectation changes are owned by one closed route. It runs the
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
owner metadata fails closed. This route remains non-authoritative: use the
complete SH-07 namespace and fresh integration lane for acceptance evidence.

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
