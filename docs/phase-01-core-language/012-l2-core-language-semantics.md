# L2 - Core Language Semantics

Sequence: 12
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L2 defines Gravity's core semantic language after reading and macro expansion. Surface conveniences lower into this core unless another document explicitly defines a primitive or domain IR boundary.

The core is intentionally small so the type checker, effect checker, profile validator, safety analyzers, MIR builder, optimizer, and backends all preserve the same meaning.

## Core Forms

The initial core forms are:

```clojure
quote
if
do
let
fn
loop
recur
def
var
set!
try
throw
match
```

Later documents may add core-adjacent forms only by updating this document and the conformance suite. Forms such as `defn`, `when`, `cond`, `case`, `with-open`, `with-region`, `defschema`, `defworkflow`, `defagent`, UI forms, query forms, and AI forms are surface or facet forms until they lower into core forms, MIR, or a documented domain IR.

## Evaluation Model

Gravity evaluation is expression-oriented. Every expression either produces a value, transfers control through `throw`, performs a declared effect, reaches a profile-defined panic path, or is rejected before execution.

Evaluation order is left-to-right for function arguments, binding initializers, `do` forms, and effectful expressions unless a later optimization proves reordering preserves effect semantics. Pure expressions may be reordered only after type/effect checking identifies them as pure and optimization preserves diagnostics.

Evaluation never relies on target-language undefined behavior. If a target has undefined behavior for a construct, Gravity must prove the construct cannot reach that behavior, emit a runtime check, reject the program, or isolate the behavior in an unsafe island.

## Value Model

Core values include:

- nil,
- booleans,
- integers,
- floating values,
- exact numeric values where mode/profile allow them,
- characters,
- strings,
- symbols,
- keywords,
- persistent lists, vectors, maps, and sets,
- tuples,
- records and structs,
- tagged unions,
- functions and closures,
- vars,
- mutable cells where profile allows them,
- linear resources,
- region and ownership values,
- syntax objects in meta contexts,
- artifact references where package/compiler contexts allow them.

Value equality, identity, layout, mutation, and allocation behavior are refined by type, memory, profile, standard-library, and backend documents.

## Form Semantics

`quote` returns data without evaluating it. Quoted forms preserve enough information for macro and tooling contexts, but quoted runtime values are data, not executable authority.

`if` evaluates its condition, then exactly one branch. Truthiness is defined by core semantics: `false` and `nil` are false; all other values are true unless a profile explicitly narrows accepted condition types.

`do` evaluates expressions left-to-right and returns the last value.

`let` creates lexical bindings. Initializers evaluate left-to-right. Bindings are immutable unless a binding form explicitly creates a mutable cell accepted by the active profile.

`fn` creates a function value with parameters, optional type annotations, effect declaration, closure captures, and return type where declared or inferred. Captures obey ownership, lifetime, and effect rules.

`loop` establishes a recur target. `recur` transfers control to the nearest compatible `loop` or function recur point. `recur` arity and types must match the target.

`def` installs a top-level var in the current namespace. It is a compile-time and artifact-visible definition, not a hidden global mutation. The profile decides whether runtime redefinition is legal.

`var` references a top-level var object when the profile allows var identity. Constrained profiles may reject runtime var indirection.

`set!` updates an explicit mutable location: mutable local cell, var where allowed, reference cell, register, field, or compiler-generated slot. It is never ambient mutation. The updated location must have a mutation capability or profile permission.

`try` evaluates protected code and handles `throw` according to typed error rules and profile legality. Systems profiles may restrict or reject stackful exception behavior while allowing explicit `Result` values or panic paths.

`throw` transfers control with a typed error value. It has an error effect. Profile documents decide whether it lowers to host exceptions, result rewriting, panic, or rejection.

`match` destructures tagged unions, records, tuples, literals, collection shapes, and guards according to `L7`. It is core because result/error flow and algebraic data are central across profiles.

## Calls and Dispatch

A list whose operator is not a core special form is a function, macro-expanded call, protocol call, intrinsic call, or profile/facet form after analysis.

Function calls evaluate operator and arguments according to call semantics, then apply the function if types, effects, ownership, capabilities, and profile rules allow it.

Dynamic dispatch is not universal. Hosted code may use richer dynamic behavior; constrained profiles require statically resolved, dictionary-passed, vtable, direct, or rejected dispatch as specified by `L8` and profile documents.

## Effects and Capabilities

L2 establishes that effects are part of expression semantics. An expression is not legal merely because it is well typed; its effects must be allowed by active profile, package manifest, capability grants, and deployment/build policy.

Core forms propagate effects:

- `if` has condition effects plus effects of the selected branch in the effect model,
- `do` combines effects in order,
- `let` combines initializer and body effects,
- `fn` records latent effects of the body,
- `try` records body and handler effects,
- `throw` records error effect,
- `set!` records mutation effect,
- calls record callee effects.

Detailed effect algebra is owned by `L6`.

## Profiles and Core Semantics

The meaning of core forms is shared, but profile legality varies.

`:core` accepts pure portable forms and rejects host services, hidden allocation, dynamic eval, reflection, raw memory, and ambient mutation.

`:kernel`, `:firmware`, and `:hardware` may reject `try` lowering strategies, var indirection, heap closures, dynamic dispatch, and unbounded collection literals unless a profile-specific lowering exists.

`:hosted` may delegate some semantics to a host runtime, but host nulls, exceptions, reflection, and dynamic behavior must be normalized into Gravity semantics before crossing the boundary.

`:meta` allows syntax and IR values as ordinary data for compiler work, but generated code is checked under the caller profile.

## Requirements

- Every surface form must lower to L2 core, a declared primitive, or a declared domain IR boundary.
- Core evaluation order must be stable enough for diagnostics and effect checking.
- Core semantics must not depend on backend undefined behavior.
- Core artifacts must preserve source spans, generated-origin chains, types, effects, capabilities, profile facts, and safety facts.
- Macro-generated core and handwritten core must pass the same legality checks.
- Target lowering must preserve L2 meaning or reject the construct.

## Dependencies

L2 depends on `D0`, `D1`, `D3`, `L1`, `D8`, and `D9`.

It is upstream of namespaces, macros, types, effects, pattern matching, dispatch, errors, memory, concurrency, compile-time evaluation, profiles, safety, compiler MIR, backends, runtimes, and tests.

## Outputs and Artifacts

L2 requires:

- expanded core AST,
- core node source map,
- core form kind records,
- evaluation-order metadata,
- latent function effect records,
- call records,
- core diagnostics,
- core conformance fixtures.

These artifacts feed the type checker, effect checker, profile validator, safety pipeline, and MIR builder.

## Rejected Semantics

L2 rejects:

- target-specific behavior as core semantics,
- undefined behavior as optimization fuel,
- implicit host nulls,
- hidden global mutation,
- implicit dynamic eval,
- exceptions that bypass typed error/effect rules,
- macro-expanded code that skips core checks,
- backend-specific loops or calls that cannot be represented in core or a documented domain IR.

## Diagnostics

- `L2-UNKNOWN-CORE-FORM`: analyzer found an unrecognized reserved core form.
- `L2-EVAL-ORDER`: transformation changed required evaluation order for effectful expressions.
- `L2-RECUR-TARGET`: `recur` has no compatible target or wrong arity/type.
- `L2-SET-ILLEGAL`: `set!` targets an immutable or profile-forbidden location.
- `L2-THROW-ILLEGAL`: `throw` is used where profile or effect rules reject it.
- `L2-HOST-SEMANTICS`: code depends on host behavior not represented in Gravity semantics.
- `L2-LOWERING-GAP`: surface form failed to lower to core or declared domain IR.

## Conformance Criteria

- Core fixtures cover every primitive form.
- Macro-expanded and handwritten equivalents produce the same core AST legality.
- Evaluation-order tests distinguish pure and effectful expressions.
- Profile fixtures show the same core form accepted in one profile and rejected in another where appropriate.
- Error fixtures cover `try`, `throw`, result-style lowering, and profile restrictions.
- MIR construction fixtures preserve core source spans and semantic facts.

## Change Control

Changing L2 changes the semantic center of Gravity. New core forms, changed evaluation order, changed truthiness, changed mutation rules, or changed error semantics require updates to syntax, type, effect, safety, compiler, backend, runtime, test, and documentation artifacts.
