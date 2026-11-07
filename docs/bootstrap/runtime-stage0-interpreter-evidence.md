# Native Stage0 interpreter

## Scope and governing contracts

Move the complete existing `eval-do`, `bind-let`, `invoke-stage0-function`,
`eval-collection`, `eval-expr` and `run-main` bodies from foundation components
00546 and 00554 into `gravity.runtime`. Retain all six Bootstrap signatures
as explicit adapters. Preserve the function-table/header producer, compiled
instruction executor, ordinary builtin implementation and every unrelated
component body.

D0, D1, D2, D3, D6, D8, D9, L2, C1, C6, C15, R1, R4, R11, R12,
BOOT8, TEST2 and TEST3 govern this increment. L2 requires stable left-to-right
evaluation, lexical initialization, selected-branch execution and failure
preservation. Runtime checks enforce existing authority; moving the interpreter
does not move compile-time profile/effect/capability/ownership/safety legality
to execution. Clojure remains temporary bootstrap context, not the source of
canonical Gravity semantics.

The source parent is `5f7df9d75339ef6b14cd405379a617f4241e93e1`, tree
`e062bcacbac7404c972f4e80ea60b18b3a566c32`. Published Main
`37208958765decff246e58d62cd33fc1a7913ee2` is already an ancestor.
The preceding arity source completed independent bounded source review and
normal writer release; its own hosted CI/provider audit remains pending.
No source or CI acceptance transfers. The new seven-path writer claim covers
this complete interpreter extraction without CI/workflow/catalog edits.

## Public API and artifacts

Planned Runtime APIs are:

```clojure
(eval-native-stage0-do eval-expr-var module env forms)
(bind-native-stage0-let fail-var eval-expr-var module env bindings)
(invoke-native-stage0-function fail-call-arity-var eval-do-var module callee args)
(eval-native-stage0-collection eval-expr-var module env form)
(eval-native-stage0-expr operations module env form)
(run-native-stage0-main function-table-var fail-var eval-do-var module)
```

Inputs are the existing native module, expression/environment/function data
and explicit late owner Vars. Outputs remain native values, environments,
captured stdout or the original owner/host exception. These functions do not
authenticate input or construct a standalone compiler artifact. Public source
observations must use genuine public macro-source modules, compile real
instruction plans and reports, and also run this interpreted route. Emission
alone does not prove the interpreter ran. Preserve full source/module/macro/
plan/report identity and actual interpreted results before and after moving
the bodies. Pair both source extensions without pretending supplied text is
authenticated file input.

## Ownership and dependency direction

Runtime owns expression/collection traversal, binding initialization, ordinary
interpreted call sequencing, entry validation, stdout capture and local
diagnostic facts/decisions. Bootstrap retains explicit late adapter wiring,
function-table/header production, builtin catalog/dispatch, effect validation
and failure throw transport. The existing Diagnostics constructor retains
generic diagnostic construction. Dependencies point Bootstrap to Runtime to
Clojure core; operations supplied by the caller are trusted seed ports.
Runtime must not import Bootstrap or dynamically discover owner Vars.

Keep each recursive/helper Var selection at its original call point, including
selection before argument evaluation. Read the builtin catalog when the
original dispatch reads it. Preserve lazy map traversal, native collection
behavior, nil/false symbol values, sequential lexical bindings, shadowing,
selected branches, returning callbacks and exact exception identity. Do not
add new shape gates, error remapping, fallback semantics or input quotas.

Diagnostic decisions include existing L2-LET-BINDING, L2-UNKNOWN-SYMBOL,
L2-UNKNOWN-CORE-FORM, L2-FUNCTION-ARITY, L2-MAIN-ARITY,
L3-UNKNOWN-ALIAS and P4-HOST-REFLECTION paths. Existing effect/failure helpers
retain their own diagnostic construction and transport behavior.

## Tests and acceptance

Extend the existing Stage0 execution tests and exact Runtime public inventory.
Cover all six APIs, successful source execution and actual refusals, sequential
bindings and branch/argument order, collection traversal, source/opaque
identity, late recursive/catalog/failure roots, adapters and unchanged
ExceptionInfo/Exception/Error propagation. Preserve all earlier Runtime bodies
and tests except the explicit API-count inventory update. Run focused prior
native regressions, language/ownership boundaries, cold API loading, changed
file lint, document validators and exact current-Main comparison lint.

Open and maintain an early draft PR. Final clean source, independent semantic,
adversarial and conservation reviews, own hosted CI/provider audit and exact
current-base integration acceptance remain required. The prior broader local
Runtime pipeline incident remains excluded and must not be rerun or credited.

## Status and unsupported boundaries

This initial scope contains documentation only; the six bodies have not moved
and no new test result is claimed. Scope publication precedes implementation.
Held Reader, authenticated/private builders, source pins and release/migration
routes remain outside the permitted source observation route.

This slice does not establish full L2 semantics, canonical Core/MIR admission,
typed host delegation, managed runtime manifests, runtime/provider selection,
source/plan authentication, build-read/file authority, portable errors,
complete source maps/generated origins, redaction/replay, backend execution,
release, self-hosting, migration or seed retirement. The native interpreter's
existing host behavior is a bounded compatibility boundary.

Exact current-base integration remains with the designated integrator. This
manual Source has no Main, Ready, controller, service, recovery or GraphQL role.
