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
- `:thread/spawn`,
- `:sync/block`,
- `:ffi/call`,
- `:reflection/use`,
- `:dynamic/eval`,
- `:compiler/read-ir`,
- `:compiler/write-ir`,
- `:compiler/plugin`,
- `:build/read-file`,
- `:build/env`,
- `:build/network`,
- `:build/exec`,
- `:secrets/read`,
- `:shell/exec`,
- `:workflow/event`,
- `:workflow/replay`,
- `:ai/model-call`,
- `:ai/tool-call`,
- `:ai/embedding`,
- `:ai/memory-read`,
- `:ai/memory-write`,
- `:ai/human-review`,
- `:unsafe`.

Later documents may add effects through the extension/governance process, but new effects must define profile legality, capability requirements, ordering behavior, diagnostics, and artifact representation.

## Effect Algebra

An expression has an effect set. `:pure` is the empty effect set in artifacts unless a document needs an explicit marker.

`do`, `let`, argument evaluation, and sequential constructs combine effects in order.

`if` combines condition effects and branch effect alternatives. The effect checker may represent branch alternatives separately for optimization but must enforce the union against profile and capability constraints.

`fn` records latent effects. Calling a function brings its latent effects into the caller.

`try` includes body and handler effects and may add error effects.

`match` includes scrutinee, guard, and selected branch effects.

Macro execution effects are build effects. Macro output effects are runtime or compile-time effects of the generated code.

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

Some effects require no external authority, such as pure arithmetic. Most external, host, unsafe, build, AI, workflow, memory, and platform effects require capabilities.

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

## Dependencies

L6 depends on `D0`, `D1`, `D3`, `D8`, `L2`, `L3`, `L4`, and `L5`.

It is upstream of capability providers, profiles, memory safety, concurrency, compile-time evaluation, package manifests, runtime capability enforcement, AI, workflows, tooling, and tests.

## Outputs and Artifacts

L6 requires:

- effect environment,
- function latent effect table,
- namespace effect summary,
- module effect summary,
- build effect log,
- replay effect log,
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
- treating capability presence as effect inference or effect presence as capability grant.

## Diagnostics

- `L6-EFFECT-UNDECLARED`: inferred effect exceeds declaration.
- `L6-EFFECT-PROFILE`: active profile rejects effect.
- `L6-EFFECT-CAPABILITY`: effect requires missing capability.
- `L6-BUILD-EFFECT`: build-time effect is ungranted.
- `L6-REPLAY-EFFECT`: replay-sensitive effect lacks replay record.
- `L6-EFFECT-ORDER`: optimization or lowering reordered effects illegally.
- `L6-EFFECT-UNKNOWN`: effect kind is unknown or lacks governance registration.

## Conformance Criteria

- Fixtures cover pure, IO, filesystem, network, database, memory, raw memory, FFI, build, workflow, AI, and unsafe effects.
- Function fixtures preserve latent effects through calls.
- Namespace fixtures reject inferred effects outside declarations.
- Profile fixtures reject effects forbidden by constrained profiles.
- Build fixtures reject environment/network/shell/model access in hermetic mode.
- Workflow/AI fixtures record replay-relevant nondeterminism.
- MIR fixtures preserve effect annotations after lowering and optimization.

## Change Control

Adding or changing effects affects profiles, capabilities, package manifests, runtimes, build policy, compiler passes, AI/workflow replay, and conformance fixtures. New effects require governance registration before stable use.
