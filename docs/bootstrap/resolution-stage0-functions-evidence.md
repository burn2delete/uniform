# Native Stage0 function declarations and table

## Scope and governing contracts

Extract all seven function-definition/header/table bodies from foundation
component 00546 into `gravity.resolution.stage0-functions`:
`function-definition-form`, `seed-declared-header?`,
`normalize-seed-function-header`, `validate-function-definition!`,
`validate-seed-function-names!`, `stage0-function-table` and
`validate-seed-source-headers!`. Retain the three public Bootstrap signatures
and four private Bootstrap helpers as thin adapters. Preserve all other
component bodies and the existing Runtime, Resolution root, Syntax, Checked
Core and Types APIs.

D0, D1, D2, D3, D6, D8, D9, L1, L2, L3, C1, C5, C6, C15, BOOT8
and TEST2 govern this increment. L3/C5 owns declaration classification,
duplicate names and table assembly; L2/C6 constrains header desugaring,
fixed-arity/name/body shape checks and declaration preservation. These native
compatibility checks do not resolve type names or emit typed Core. L17/C7
provider or type-checking authority is not claimed.

Independent static placement review SHA256
`4443b1801b38cfd16bd7f1407b51e38b81e7024b88d3d3d2297efe8328f3b569`
identifies the complete group and its direct resolution/B51, compiler/plan
and runtime consumers. That discovery review is not source acceptance.

The frozen source parent is `dc100cee7388672e270f9ccacb7d5be7e0c552b8`,
tree `d56a1c4bc92f7ff230fb44e5b2fe0d94e755fbcd`. Published Main
`37208958765decff246e58d62cd33fc1a7913ee2` is already an ancestor.
The parent completed independent source reviews and normal writer release;
its own hosted CI/provider audit remains pending at this scope capture. No
acceptance transfers. The new eight-path writer claim includes only the
child, Bootstrap wiring/component, focused tests/CI registration and evidence.

## Public API, internal wiring and artifacts

The planned child public API is:

```clojure
(function-definition-form defn-form?-var def-function-form?-var source-path form)
(validate-function-definition! fail-var source-path definition)
(stage0-function-table operations module)
```

The four seed helpers remain private in both the child and Bootstrap. Their
explicit internal bridge signatures are:

```clojure
(seed-declared-header? definition)
(normalize-seed-function-header fail-var declared-header?-var source-path definition)
(validate-seed-function-names! fail-var source-path definitions)
(validate-seed-source-headers! operations source-path forms)
```

Bootstrap's private adapters call the child's explicit private Vars; this is
temporary Clojure wiring, not a Gravity public/private import facility or a
stable public API expansion. Every supplied semantic operation is a late
owner Var. The table operations name classification, duplicate checking,
normalization and definition validation. The source-header operations also
name header recognition. There is no dynamic owner discovery.

Inputs are caller-owned ordered native expanded forms, source-path values,
function definitions and module data. Outputs are the original raw definition,
normalized table with arity/body-count and exact `:source-declarations`, nil,
or unchanged owner return/failure. The child creates no canonical C5/Core/type
artifact and grants no source/file/plan authority. The enclosing permitted
public source route must emit real plans/reports and execute genuine modules,
retain complete tables/declarations, and reject genuine malformed sources.

## Ownership, diagnostics and dependency direction

The child owns all seven algorithms, local L2-FUNCTION-PARAMS and
L2-DUPLICATE-FUNCTION decisions/facts, declaration retention and native table
assembly. Diagnostics retains generic diagnostic construction; Bootstrap
retains adapters and `fail!` throw transport. Existing source intake, effects,
profiles, capabilities, ownership, safety, authentication and artifact
publication remain with their existing owners.

Dependencies point Bootstrap adapters to this Resolution child to Clojure
core. The child must not import Bootstrap, the Resolution root, Reader,
Diagnostics, Types, Checked Core, package/build/CLI or release namespaces.
It reads no source and executes no macros.

Preserve classifier order, lazy definition scanning, duplicate selection,
`into {}` behavior, exact parameter/body/return declaration data and every
lookup/exception boundary. Table assembly normalizes before selecting the
definition validator. Source-header validation selects its validator before
evaluating normalization as a call argument. Its `some` header predicate is
selected before lazy definition realization, while per-definition predicate
calls remain late. Do not introduce eager ports, sorting, shape gates,
quotas, fallback behavior or type resolution.

## Tests and acceptance

Add a dedicated `gravity.resolution.stage0-functions-test` command to the
existing contracts cohort and extend its existing exact inventory/whole-
namespace CI regression checks. Preserve all previous commands; this adds one
contract command, without a workflow or runtime-policy change.

Focused tests must cover all seven bodies, the exact public/private surfaces,
valid defn/def-fn and declared headers, malformed headers/names/params/body,
duplicate functions, retained source declarations, first-refusal order, late
Vars, opaque source identity, returning failures, unchanged owner exceptions
and adapter interposition. Source evidence must pair both extensions using
genuine public macro-source modules, real tables/plans/reports and interpreted/
compiled results. Preserve complete before/after observations and deterministic
source/plan/report identities. Supplied paired text is not authenticated file
input. Run affected existing declaration/table tests only after inspecting their
routes; held Reader/private builders and broad local pipeline tests remain
outside the permitted route.

Run the language/ownership boundaries, cold child loading, focused CI checks,
lint, document validators and complete current-Main comparison. Independent
semantic/adversarial/conservation review and own hosted CI/provider audit bind
the eventual clean source. The prior broader Runtime review incident remains
excluded and must not be rerun or credited.

## Status and unsupported boundaries

This scope is documentation only; no body or CI command has moved yet and no
new passing result is claimed. Open and maintain the draft PR before source
implementation. Exact current-base integration remains with the designated
integrator. This manual Source has no Main/Ready/controller/service/recovery
or GraphQL role.

This bounded native table is not canonical C5 resolution, C6 verified Core,
C7/L17 typing, source/plan authentication, a complete source-map/origin graph,
profile/effect/capability/ownership/safety admission, backend execution,
release, migration, self-hosting or seed retirement. Gravity/Uniform remains
the destination implementation language and Clojure remains temporary seed.
