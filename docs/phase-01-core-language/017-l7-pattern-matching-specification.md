# L7 - Pattern Matching Specification

Sequence: 17
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L7 defines `match`, the core destructuring and branch selection form. Pattern matching is primitive because Gravity uses records, tagged unions, result values, schemas, workflow states, AI outputs, hardware states, and protocol data across profiles.

Pattern matching must be analyzable. The compiler uses match structure for exhaustiveness, type narrowing, effect analysis, safety diagnostics, optimization, and target lowering.

## Match Form

Canonical shape:

```clojure
(match value
  pattern1 expr1
  pattern2 expr2
  ...
  _ default-expr)
```

Each pattern is checked against the scrutinee type. Each branch expression is type checked in an environment extended by the pattern bindings and narrowed type facts.

## Pattern Families

Gravity supports these pattern families:

- wildcard: `_`,
- binding: `x`,
- literal: `nil`, `true`, `false`, numbers, strings, keywords, symbols where quoted,
- tuple/vector: `[x y z]`,
- list shape where profile/runtime supports it,
- map shape: `{:id id :name name}`,
- record/struct shape,
- tagged union constructor: `(Ok value)`, `(Err error)`,
- enum case,
- type pattern,
- schema-derived shape,
- resource/linear pattern where ownership rules allow destructuring,
- guard pattern using `:when`,
- explicit default.

Examples:

```clojure
(match result
  (Ok value) value
  (Err error) (throw error))
```

```clojure
(match request
  {:method :GET :path path} (handle-get path)
  {:method :POST :body body :when (valid? body)} (handle-post body)
  _ (not-found))
```

## Binding Rules

A binding introduced by a pattern is lexical and visible only in the branch body and guard position after the binding is introduced.

Duplicate binding names in the same pattern are rejected unless a later document defines equality-pattern syntax explicitly.

Bindings inherit narrowed types from the matched pattern. Matching `(Ok value)` against `Result[T, E]` binds `value` as `T`.

Linear or owned values may be moved by a pattern only if the branch consumes them according to ownership and linear-resource rules.

## Exhaustiveness

Exhaustiveness is required for:

- closed enums,
- tagged unions,
- formal verification profile,
- safety-critical namespaces that opt into total matching,
- compiler-internal analysis where missing cases would be unsound.

Exhaustiveness may be relaxed with an explicit default branch, but a default branch over a closed type can still produce a warning or policy diagnostic when it hides future compatibility issues.

Open maps, host objects, dynamic values, and untrusted external data require runtime checks or schema validation before closed-shape exhaustiveness may be claimed.

## Guards and Effects

Guards are expressions and carry effects.

```clojure
(match packet
  {:kind :data :payload p :when (checksum/valid? p)} (accept p)
  _ (reject packet))
```

Guard effects are part of match effects. A guard with filesystem, network, model, tool, or random effects must be legal in the active profile and declared effect set.

Guards must not mutate values in a way that changes later pattern behavior unless effect ordering and profile rules allow it.

## Schema and Untrusted Data

Pattern matching over untrusted data does not by itself validate the data.

Network, filesystem, database, AI, and tool outputs must pass schema validation or typed boundary checks before the compiler treats them as typed closed shapes.

```clojure
(match (schema/validate User input)
  (Ok user) (:email user)
  (Err e) (reject e))
```

AI structured outputs must be validated against their declared schema before matching on variant constructors or required keys.

## Lowering

The analyzer lowers `match` into a decision tree with:

- tested fields/constructors,
- binding moves/borrows,
- guard calls,
- branch effect summaries,
- exhaustiveness facts,
- unreachable branch diagnostics,
- source spans for each pattern and branch.

Backends may lower decision trees into jumps, if chains, tables, vtables, hardware state transitions, workflow branch nodes, or query predicates, but must preserve match semantics and diagnostics.

## Profile Behavior

`:core` supports pure structural matching over portable values.

`:hosted` may match host objects only through typed wrappers or checked interop patterns.

`:native` supports layout-aware matching when record/union layout is known.

`:kernel`, `:firmware`, and `:hardware` may restrict collection patterns, allocation-heavy destructuring, and dynamic patterns.

`:distributed` uses match for workflow state and event handling; replay-sensitive guards must be recorded.

`:ai` uses match for structured outputs and tool results after schema validation.

`:formal` requires totality or explicit partiality proof for closed matches.

## Requirements

- Pattern matching must be type checked after macro expansion.
- Branch bindings must have precise narrowed types.
- Closed tagged unions and enums must support exhaustiveness checking.
- Guard effects must be included in match effect summaries.
- Pattern matching must preserve source spans for every pattern and branch.
- Pattern lowering must be deterministic and artifacted enough for compiler diagnostics and optimization.
- Matching untrusted data as a closed shape requires validation evidence.

## Dependencies

L7 depends on `L1`, `L2`, `L3`, `L4`, `L5`, and `L6`.

It feeds error handling, protocols/dispatch, schema validation, workflow branching, AI structured outputs, compiler lowering, formal verification, and conformance testing.

## Outputs and Artifacts

L7 requires:

- match decision tree artifact,
- exhaustiveness report,
- branch type-narrowing table,
- branch effect summary,
- unreachable branch diagnostics,
- schema validation links when applicable,
- ownership move/borrow facts for pattern bindings.

## Rejected Behavior

L7 rejects:

- non-exhaustive closed matches where totality is required,
- duplicate binding names in one pattern,
- guard effects not legal for the active context,
- matching unvalidated external data as a trusted closed shape,
- pattern moves that violate ownership or linear-resource rules,
- backend lowering that changes branch order where guards/effects make order observable,
- default branch used to suppress required safety proof in formal/safety-critical contexts.

## Diagnostics

- `L7-NONEXHAUSTIVE`: closed match lacks required case.
- `L7-UNREACHABLE`: branch can never match.
- `L7-DUP-BINDING`: pattern binds same name illegally.
- `L7-PATTERN-TYPE`: pattern incompatible with scrutinee type.
- `L7-GUARD-EFFECT`: guard uses undeclared or forbidden effect.
- `L7-UNVALIDATED-SHAPE`: untrusted value matched as validated shape.
- `L7-LINEAR-MOVE`: pattern violates ownership or linear-resource rules.

## Conformance Criteria

- Fixtures cover literals, records, maps, vectors, tagged unions, enums, guards, defaults, schema-derived shapes, and linear resources.
- Exhaustiveness fixtures include accepted total matches and rejected missing cases.
- Guard fixtures prove effects are tracked and ordered.
- Schema fixtures reject unvalidated AI/network/tool outputs before closed-shape matching.
- Lowering fixtures preserve branch source spans and generated decision trees.
- Backend fixtures produce equivalent branch behavior for representative targets.

## Change Control

Changing pattern semantics affects core lowering, type narrowing, effect checking, ownership, schema validation, workflow/AI branching, formal verification, and backend lowering. Changes require conformance updates for each affected pattern family.
