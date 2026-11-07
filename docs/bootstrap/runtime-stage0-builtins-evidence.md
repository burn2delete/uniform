# Native Stage0 ordinary builtin invocation

## Scope and governing contracts

The complete existing `invoke-stage0-builtin` body moves from
`00554-invoke-stage0-builtin.clj` into
`gravity.runtime/invoke-native-stage0-builtin`. The existing three-argument
Bootstrap entry remains an adapter. The other interpreter/compiler definitions in
that component and the separate Stage2 `hosted-builtin` API remain outside
this extraction.

D0, D1, D2, D3, D6, D8, D9, L2, C1, C15, R1, R4, R11, R12, BOOT8 and
TEST2 govern the increment. L2 governs order and failure behavior. The runtime
and diagnostic contracts constrain preservation and nonclaims; this temporary
native interface does not establish their canonical manifests or host adapters.
Compile-time profile, effect, capability and safety legality stays upstream.

The source parent is frozen Parser source
`6385ef78ce88ba60fd5b9308d8aba13d4771a873`, tree
`abe36db99599822795dbf0d1ec0946fb3054878e`. Published Main
`b722e29a0096381fbf476ca04771fe6567657f69` is already an ancestor. At scope
capture the parent's own hosted CI was pending independent audit. Its later
successful independent audit is dependency context, not acceptance here. This source
imports no new Main paths and inherits no source, provider or CI acceptance.
The seven-path claim owns the two implementation files, two existing Runtime
test files, this document and two existing Runtime boundary/evidence documents.

## Public API and artifact inputs and outputs

The new Runtime signature is:

```clojure
(invoke-native-stage0-builtin operations module callee args)
```

Inputs are a trusted native Stage0 module, an existing callee symbol,
already-evaluated arguments and eight explicit late owner Vars:

- `:assert-min-arity!`
- `:assert-exact-arity!`
- `:assert-between-arity!`
- `:assert-even-arity!`
- `:fail-call-arity!`
- `:readable-pr-str`
- `:normalized-rest`
- `:fail!`

Read only the selected operation at its original call position. Select its
function root before evaluating its arguments, including an observable module
`:source-path` lookup. Do not eagerly destructure or dereference every port.
The Bootstrap adapter supplies the existing Vars and preserves its old
`[module callee args]` signature and interposition point. The executor's
separate eight-port operation map continues to invoke that adapter after
evaluating builtin arguments.

Outputs are the existing host values, unchanged owner returns/exceptions or
the existing ordinary-host-error refusal. The body creates no standalone
artifact. The enclosing public source route emits actual compiled plans and
retains module/source facts, source hashes, plan/report identities, stdout and
refusal records. It must not invent source maps, proof facts or manifests.

## Ownership, dependencies and diagnostics

The code dependency is Bootstrap adapter to Runtime to `clojure.core`.
Runtime must not import Bootstrap or discover its Vars dynamically. The eight
owner Vars are explicit supplied operations, not authentication or authority.

Runtime owns ordinary Stage0 arm selection, arity-check placement and the
existing `Exception` to `L2-BUILTIN-ERROR` remap decision and local facts.
Supplied arity helpers retain their existing predicates and
`L2-BUILTIN-ARITY` facts. The subsequent [native arity
extraction](runtime-stage0-arity-evidence.md) moves those helper bodies and
their local facts into Runtime while preserving these five Bootstrap adapter
Vars. The supplied failure operation retains generic
diagnostic construction and transport. Its Bootstrap selection, source/plan
admission, readable printer/normalizer bodies and reserved policy paths remain
with their existing owners. The separate Stage2/P15 outer error translation
does not move as part of this Stage0 body.

Preserve all twenty-one ordinary arms, their guard order and exact value
classes. In particular, preserve `assoc`'s minimum and key/value arity order,
`get`'s two/three argument rule, exact arity before unary indexing, readable
printing's source-path identity and `rest`'s normalizer selection. Rethrow
`ExceptionInfo` unchanged, remap other `Exception` values once through the
selected failure Var, and preserve the existing uncaught fatal/error boundary.
Returning failure callbacks retain compatibility behavior without admission.

## Tests, consumers and verification

Before/after observations use the ordinary public
`macro-source-artifact(path, text)` route, its genuine module/expanded forms,
`stage0-compiled-core-plan` and the public executor. All twelve complete
observations are identical: twelve actual plans, six successful stdout
executions and six runtime refusals after emission. Both source spellings
cover the example and accepted-fixture `core-app`, the builtin minimum-arity
fixture, all twenty-one ordinary arms, odd `assoc` key/value arity and division
by zero. Refusals retain `L2-BUILTIN-ARITY` or `L2-BUILTIN-ERROR` with the
original source facts. Plans, reports, module/macro records, source paths and
hashes, deterministic IDs, builtin calls and value classes are unchanged.

The baseline receipt SHA256 is
`a2ab302d47a12181f12c5e9b0e8d0c98922dab2d05ac5f23213ea520d7758ed3`;
the implementation observation receipt SHA256 is
`b338f7327c555764735fb369fdfb131b64098f0f85086928910b0f6115a4218b`.
Both record the actual scope HEAD
`2fb7ea6e73d4ba856055349d7a64121b3a167911`, with separate implementation
file hashes; they do not claim a later clean commit was observed. Example
inputs use actual file bytes. Where the fixture has no `.qst` file, its paired
text is explicitly caller-supplied under that path. Inline cases are likewise
caller-supplied text. No file authentication or source admission is inferred.

The existing whole `gravity.runtime.stage0-execution-test` command owns new
Stage0 cases. Separate eight-port API contract checks cover all ordinary arm
results, malformed arities, host exceptions, exact exception/source identity,
late selection, unselected-port avoidance and operator selection before
argument lookup. The four executor API contract checks remain separate and
the Runtime public count changes from 42 to 43. Only the new public name is
added to the exact inventory in `gravity.runtime.pipeline-test`; every old
entry and generic non-authority assertion remains. Stage2/P15 behavior tests
are unchanged. The whole Stage0 namespace passes 19 tests and 222 assertions;
the selected generic public-inventory test passes 1 test and 309 assertions.
Language-boundary and namespace-ownership checks pass 48 tests and 1202
assertions. Runtime and both changed test files pass clj-kondo with zero errors
and warnings; `git diff --check` passes. Eleven selected earlier native Runtime
tests pass 419 assertions without invoking their source-compiler fixtures.
Cold loading exposes all 43 public APIs with matching declarations and argument
lists, no namespace aliases, and no Bootstrap or Diagnostics namespace loaded.
The structural document validator passes 240 documents and 19 phase indexes;
the full-language roadmap structural validator also passes. Neither validator
establishes semantic completeness or current roadmap coverage.
Existing whole-namespace CI mappings already select both test files, so no CI
command, workflow, catalog, ownership or load-order edit is required.

The existing Stage0 execution evidence and namespace map distinguish
the new ordinary Stage0 API from the existing Stage2 builtin API and narrow
the old Bootstrap-dispatch ownership statement. Historical observations remain
history. Complete current-Main comparison lint remains required. Independent
semantic, adversarial, conservation, provider and own-CI review must bind the
clean final source and its exact base before integration eligibility.

Independent read-only boundary review SHA256
`cf0699b338d980dc98bfdffadaafa9c0dcfd4899a3ea06b839e54b673599d035`
and consumer review SHA256
`a2361647b0bd0564649f0ae4fc1dddfdbd388f0fb641c2e616d58c9977efe34c`
identify the real executor route and actual public-inventory consumers. They
are discovery evidence, not implementation acceptance.

## Current status and unsupported boundaries

The new Runtime API and compatibility adapter are implemented in the current
working tree. The original complete-body receipt has SHA256
`27c48a26aaea4798c989d64ac507b2ae92eab347c459a8f14bb9c34a8041a7a9`.
It retains the original and transformed bodies, the exact eight operator
substitutions, new argument list and adapter. Final clean-commit acceptance,
own hosted CI, provider review and Main integration remain pending; provisional
review of dirty source does not confer acceptance. The original module carrier
differs from the Stage2 plan, so
`hosted-builtin` and its seventeen-key map are not substitutes for this body.

These counts and observations describe the ordinary-builtin extraction at its
recorded capture. The subsequent native arity extraction adds five distinct
APIs and takes the current public inventory to 48. The builtin's eight ports,
guard placement and outer host-error remap stay unchanged; Bootstrap now
retains arity adapters and generic failure transport rather than the five
helper algorithm bodies.

`build-read-declared-input` remains an existing catalog/lowering vocabulary
gap without a usable service in this dispatcher. This increment adds no
Reader, file, build-read or capability authority to close it. It does not move
`println`, argument evaluation, reserved/compiler-native dispatch, formatter
implementations or source/plan admission. It does not implement canonical L2
numeric/collection/error semantics, typed host delegation, runtime-family
selection, complete R1/R4/R11/R12 or SAFE9 conformance, public source/plan
authentication, MIR/backend execution, portable errors, source maps, redaction,
replay, release, migration, self-hosting or seed retirement. Held Reader,
authenticated/private builders, factories and source pins stay uninvoked.
Gravity/Uniform remains the destination implementation language.

The designated integrator retains exact current-base preflight and Main
integration. This manual Source has no Main, Ready, controller, service or
recovery role.
