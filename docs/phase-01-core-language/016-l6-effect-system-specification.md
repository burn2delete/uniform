# L6 - Effect System Specification

Sequence: 16
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L6 defines effects: the semantic actions a Gravity program may perform. Effects make hidden behavior visible to type checking, profile validation, capability enforcement, package policy, runtime selection, replay, audit, and optimization.

A program is not legal just because it is well typed. Its effects must also be legal for the active profile, namespace declaration, package manifest, deployment/build grant, runtime/provider contract, and safety mode.

## Effect Model

An effect is an action label with ordering, authority, profile, and artifact implications.

```clojure
{:effect :network/http
 :kind :external
 :requires-capability true
 :nondeterministic true
 :replay-record true
 :profiles [:hosted :native :distributed :ai]
 :denied-in [:core :firmware :kernel :hardware]}
```

Effects are inferred from expressions, functions, macros, packages, build steps, runtime services, and tool calls. Declared effects are allowances; inferred effects must fit inside allowances.

## Effect Families

Core effect families include:

- `:pure`,
- `:memory/allocate`,
- `:memory/free`,
- `:memory/raw`,
- `:memory/mmio`,
- `:interrupt/register`,
- `:resource/open`,
- `:resource/close`,
- `:io/read`,
- `:io/write`,
- `:filesystem/read`,
- `:filesystem/write`,
- `:network/http`,
- `:network/listen`,
- `:database/read`,
- `:database/write`,
- `:time/read`,
- `:time/schedule`,
- `:random/read`,
- `:async/suspend`,
- `:async/await`,
- `:generator/yield`,
- `:thread/spawn`,
- `:sync/block`,
- `:error/resume`,
- `:ffi/call`,
- `:reflection/use`,
- `:dynamic/eval`,
- `:compiler/read-ir`,
- `:compiler/write-ir`,
- `:compiler/plugin`,
- `:build/read-file`,
- `:build/write-artifact`,
- `:build/env`,
- `:build/network`,
- `:build/exec`,
- `:build/time`,
- `:build/random`,
- `:build/model-call`,
- `:build/tool-call`,
- `:build/target-probe`,
- `:build/package-index`,
- `:secrets/read`,
- `:shell/exec`,
- `:workflow/event`,
- `:workflow/replay`,
- `:ai/model-call`,
- `:ai/tool-call`,
- `:ai/embedding`,
- `:ai/memory-read`,
- `:ai/memory-write`,
- `:ai/prompt-render`,
- `:ai/output-validate`,
- `:ai/eval-run`,
- `:ai/human-review`.

Unsafe behavior is not represented by a generic `:unsafe` effect label. Unsafe
islands, unsafe wrappers, and unsafe providers must expose the concrete effects
they perform, such as `:memory/raw`, `:memory/mmio`, `:ffi/call`,
`:interrupt/register`, or `:compiler/write-ir`, and must separately carry the
safety outcome or safety mode required by `D8`, `SAFE1`, and `SAFE6`.

Later documents may add effects through the extension/governance process, but new effects must define profile legality, capability requirements, ordering behavior, diagnostics, and artifact representation.

## Effect Algebra

An expression has an effect set. `:pure` is the empty effect set in artifacts unless a document needs an explicit marker.

`do`, `let`, argument evaluation, and sequential constructs combine effects in order.

`if` combines condition effects and branch effect alternatives. The effect checker may represent branch alternatives separately for optimization but must enforce the union against profile and capability constraints.

`fn` records latent effects. Calling a function brings its latent effects into the caller.

`try` includes body and handler effects and may add error effects.

`match` includes scrutinee, guard, and selected branch effects.

Macro execution effects are build effects. Macro output effects are runtime or compile-time effects of the generated code.

## Effect Labels and Handled Effects

L6 distinguishes effect labels from handled effects.

An effect label is the stable registry name for a semantic action. Examples include `:network/http`, `:random/read`, `:generator/yield`, `:workflow/event`, and `:ai/tool-call`. Labels appear in declarations, profiles, capability policy, package manifests, MIR annotations, replay logs, diagnostics, and conformance fixtures.

A handled effect is a typed occurrence of an effect label that is interpreted by a handler scope. A handled effect records:

- effect label,
- request type,
- response type,
- resume mode,
- replay mode,
- handler identity,
- handler residual effects,
- required handler capabilities.

Handling an effect does not rename or erase the label. A handled `:ai/tool-call` remains an `:ai/tool-call`; artifacts must also record that the occurrence was interpreted by a particular handler. Effect summaries therefore have two projections:

- escaping effects, which may leave the current expression or function;
- handled effects, which were intercepted and interpreted by a typed handler.

A handler may remove a label from the escaping effect set only when the active profile allows that handler form, the handler covers the label, the request and response types match, capability checks pass, and continuation/replay rules are satisfied. The handled-effect record is still preserved for audit, replay, optimization, package policy, and test reporting.

Declarations may separately state which effects a function may perform and which effects a handler may interpret. `:effects` grants no handling authority. `:handles` grants no capability authority.

## Typed Effect Handlers

Typed effect handlers are profile-gated scopes that interpret handled effects. They are ordinary typed constructs with additional effect, capability, continuation, and replay obligations.

A handler declaration must define:

- handled effect labels;
- request and response types for each handled label;
- whether the handler aborts, resumes once, resumes many times, suspends, or delegates;
- residual effects performed by the handler implementation;
- required capabilities for live interpretation, mock interpretation, replay reads, replay writes, scheduler access, tool access, or test fixture access;
- profile availability and target/runtime requirements.

Handler type checking must prove:

- every handled label is registered in the effect registry;
- the active profile permits the handler form and resume mode;
- each handled occurrence has a request and response type accepted by the handler;
- the handler body returns the type expected by the surrounding expression;
- unhandled labels escape unchanged;
- residual handler effects are declared and legal;
- capabilities required by the interpreted effect and by the handler implementation are both present.

Required handler classes include:

- async handlers for `:async/suspend`, `:async/await`, scheduler wakeups, cancellation, and timeout interpretation;
- generator handlers for `:generator/yield`, typed send values, completion values, and close behavior;
- resumable error handlers for `:error/resume`, typed error payloads, typed resume values, abort paths, and cleanup paths;
- workflow replay handlers for `:workflow/event`, `:workflow/replay`, time, random, activity, model-call, tool-call, human-review, retry, and compensation records;
- tool-call interpreters for `:ai/tool-call` with schema validation, tool identity, human-review policy, budget policy, and result typing;
- test interpreters that replace declared effects with fixtures, fakes, mocks, golden records, or deterministic replay data during test and conformance execution.

Test interpreters are not an ambient profile that makes effects legal. They run under the active target profile plus explicit test harness grants. A test fake may satisfy a handled effect only if the replaced label, fake capability, fixture identity, and expected diagnostic behavior are recorded.

## Handler Capability Restrictions

Handlers do not grant authority to the code they wrap. They interpret handled effects at a boundary.

The compiler must enforce:

- a handler cannot interpret an effect label that is forbidden by the active profile;
- a handler cannot perform live external work unless it has the same or narrower capability required by that work;
- a handler cannot use secrets, filesystem, network, database, model, tool, workflow, scheduler, or shell authority through ambient process state;
- a mock or test handler cannot hide a missing capability unless the test explicitly expects the missing-capability diagnostic;
- a handler cannot widen namespace, package, build, deployment, runtime, or provider allowances;
- a handler that delegates an effect must preserve the original label and capability requirements in the escaping effect summary.

For example, a test handler may replace `:network/http` with a fixture response and remove it from escaping effects for that test body, but the artifact must still record the handled `:network/http` label, the fixture capability, and the absence of live network authority. A production handler that performs the HTTP request must still hold the network capability.

## Continuation and Replay Safety

Captured continuations are typed values with profile-specific restrictions. By default, continuations are affine: they may be resumed at most once or cancelled. Multi-shot continuations require an explicit resume mode, a profile that permits cloning, and proof that captured state is replay-safe.

Continuation safety checks must reject:

- resuming a continuation after completion, cancellation, panic finalization, or generator close;
- resuming on a thread, scheduler, actor, workflow step, or target that the profile forbids;
- capturing linear resources, borrowed references, raw pointers, interrupt state, locks, or FFI frames across suspension unless the profile and safety rules define a valid transfer;
- replaying a continuation in a way that repeats an external side effect;
- serializing a continuation into an artifact without an approved profile representation.

Replay-sensitive handlers must distinguish record mode from replay mode. In record mode, a handler may perform an allowed live effect and append a typed replay record. In replay mode, the handler must read the existing record and must not re-execute time, random, network, database, model-call, tool-call, `:ai/human-review`, workflow-event, or scheduler effects unless the profile explicitly defines a deterministic replay operation.

Workflow replay handlers must use stable event ids and typed payloads. Missing, mismatched, reordered, or extra replay records are L6 errors, not host runtime warnings.

## Legality Rule

An effect is legal only if it is accepted by all applicable authorities:

```text
inferred effects
  subset namespace declared effects
  subset active profile allowed effects
  subset package/build/runtime declared effect allowances
  allowed by safety mode

required capabilities for those effects
  subset namespace declared capabilities
  subset package capability manifest
  subset build/deployment grants
  subset runtime/provider contract
```

Effect sets are not compared directly with capability sets. The narrowest set wins. Package resolution cannot widen effects or authority. Runtime selection cannot widen effects or authority. A macro cannot widen effects secretly.

## Effects and Capabilities

Effects describe what code does. Capabilities grant authority to do it.

Some effects require no external authority, such as pure arithmetic. Most external, host, build, AI, workflow, memory, platform, and unsafe-island concrete effects require capabilities.

Examples:

- `:network/http` requires a network capability and often timeout/retry policy.
- `:filesystem/read` requires path-scoped filesystem capability.
- `:io/write` requires output authority when it writes outside pure in-memory data.
- `:ai/tool-call` requires a tool capability and schema.
- `:build/exec` requires build policy grant and is denied in hermetic mode by default.
- `:memory/mmio` requires hardware/MMIO capability and constrained profile support.

Capability semantics are refined by `L15`.

## Build Effects

Compile-time work is not free of authority. Reader extensions, macros, compiler plugins, code generators, package resolution, and build scripts may request build effects.

Hermetic builds deny:

- environment reads,
- undeclared file reads,
- network access,
- shell/process execution,
- model calls,
- tool calls,
- non-lockfile package fetching.

Granted build effects are recorded in build artifacts.

## Replay and Nondeterminism

Nondeterministic effects include time, random, network, filesystem where contents are external, database reads, model calls, tool calls, `:ai/human-review`, scheduler decisions, and workflow events.

Distributed and AI profiles must record replay-relevant effects. A workflow cannot silently re-read current time during replay. An agent cannot silently re-call a model when replay requires the original output.

## Requirements

- Every expression, function, namespace, module, package, macro, and artifact must expose effect facts where relevant.
- Effect inference must run after macro expansion and type checking has enough facts to identify calls.
- Inferred effects must not exceed declared namespace/function/package allowances.
- Effect legality must be checked before MIR construction and preserved into MIR.
- Optimizers must not reorder effectful operations unless effect semantics permit it.
- Build effects must be enforced by build policy and recorded as artifacts.
- Replay-relevant effects must produce replay records in distributed and AI contexts.
- Effect summaries must distinguish escaping labels from handled-effect records.
- Typed handlers must be checked for profile availability, handled-label coverage, residual effects, capabilities, continuation safety, and replay safety.
- Async, generator, resumable-error, workflow replay, tool-call, and test interpreter handlers must produce typed artifacts that preserve original labels and handler identity.

## Dependencies

L6 depends on `D0`, `D1`, `D3`, `D8`, `L2`, `L3`, `L4`, and `L5`.

It is upstream of capability providers, profiles, memory safety, error handling, concurrency, compile-time evaluation, package manifests, runtime capability enforcement, AI, workflows, tooling, and tests.

## Outputs and Artifacts

L6 requires:

- effect environment,
- function latent effect table,
- namespace effect summary,
- module effect summary,
- build effect log,
- replay effect log,
- handled effect table,
- handler capability and profile report,
- continuation and replay safety report,
- effect legality report,
- MIR effect annotations,
- effect diagnostics,
- effect conformance fixtures.

## Rejected Behavior

L6 rejects:

- undeclared effects,
- effect inference widening namespace declarations silently,
- macros hiding runtime effects,
- build tools using ambient environment, network, shell, model, or tool access,
- runtime providers performing effects not present in artifacts,
- optimizers reordering effects illegally,
- replay-sensitive effects without replay records,
- treating capability presence as effect inference or effect presence as capability grant,
- treating a handled effect as if its original label disappeared,
- handlers that widen effects, capabilities, profile permissions, package permissions, runtime permissions, or provider permissions,
- handlers that resume continuations unsafely or replay external side effects,
- test interpreters that hide live effects, missing capabilities, or profile violations without structured test artifacts.

## Diagnostics

- `L6-EFFECT-UNDECLARED`: inferred effect exceeds declaration.
- `L6-EFFECT-PROFILE`: active profile rejects effect.
- `L6-EFFECT-CAPABILITY`: effect requires missing capability.
- `L6-BUILD-EFFECT`: build-time effect is ungranted.
- `L6-REPLAY-EFFECT`: replay-sensitive effect lacks replay record.
- `L6-EFFECT-ORDER`: optimization or lowering reordered effects illegally.
- `L6-EFFECT-UNKNOWN`: effect kind is unknown or lacks governance registration.
- `L6-HANDLER-TYPE`: handled effect request, response, or result type does not match the handler.
- `L6-HANDLER-PROFILE`: active profile rejects handler form, handled label, or resume mode.
- `L6-HANDLER-CAPABILITY`: handler lacks capability for live, mock, replay, scheduler, tool, or fixture interpretation.
- `L6-HANDLER-CONTINUATION`: handler captures, resumes, clones, serializes, or transfers a continuation unsafely.
- `L6-HANDLER-REPLAY`: handler would replay a side effect, read a mismatched record, or omit a required replay record.
- `L6-HANDLER-COVERAGE`: handler claims to cover an effect label but leaves an occurrence unhandled or delegates it without declaring the escape.

## Conformance Criteria

- Fixtures cover pure, IO, filesystem, network, database, memory allocation,
  raw memory, MMIO, FFI, build, workflow, AI, and unsafe-island concrete
  effects.
- Function fixtures preserve latent effects through calls.
- Namespace fixtures reject inferred effects outside declarations.
- Profile fixtures reject effects forbidden by constrained profiles.
- Build fixtures reject environment/network/shell/model access in hermetic mode.
- Workflow/AI fixtures record replay-relevant nondeterminism.
- MIR fixtures preserve effect annotations after lowering and optimization.
- Handler fixtures distinguish effect labels from handled-effect records in source, MIR, and artifacts.
- Async and generator fixtures prove typed yield, suspend, await, resume, cancel, close, and completion behavior.
- Resumable-error fixtures prove typed resume values, abort paths, cleanup ordering, and rejected unsafe resumes.
- Workflow replay fixtures prove record-mode writes, replay-mode reads, stable event ids, and rejection of missing or mismatched records.
- Tool-call interpreter fixtures prove schema validation, capability checks, human-review policy, budget policy, typed results, and no ambient tool authority.
- Test interpreter fixtures prove fakes and mocks are profile-gated, artifact-recorded, capability-checked, and unable to hide unexpected live effects.

## Change Control

Adding or changing effects or handler semantics affects profiles, capabilities, package manifests, runtimes, build policy, compiler passes, AI/workflow replay, testing, and conformance fixtures. New effect labels, handler forms, resume modes, or replay modes require governance registration before stable use.
