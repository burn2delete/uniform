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

The implemented child public API is:

```clojure
(function-definition-form defn-form?-var def-function-form?-var source-path form)
(validate-function-definition! fail-var source-path definition)
(stage0-function-table operations module)
```

The four seed helpers remain private in both the child and Bootstrap. Their
explicit internal bridge signatures are:

```clojure
(seed-declared-header? definition)
(normalize-seed-function-header fail-var declared-header?-var source-path fields definition)
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

The scope was published as draft PR #1348 before source implementation at
commit `b4009385cf225368a7fdffa60d7352f02da12424`, tree
`f3625adeec4150fe59e66f5adb2826b6707dcb7f`. All seven bodies now live in the
child, with three public APIs and four private bridges. Original/body/adapter
and complete source-byte receipt SHA256:
`0f146ed537c7bb866e4d6c33f733e5a47fcd830b6cdaa38dda08413eb1c82b86`.
The original receipt remains immutable. A separate reader-discarded lint
annotation receipt has SHA256
`b9cfd0ea09ca84bff39347a745de30d893a69f7a10ec4619078b75448789701e`.
It records only the intentionally unused classifier source-path parameter
and private Vars called through Bootstrap bridges; algorithms, signatures and
contract data are unchanged. The validator/predicate adapters project their
already-destructured fields to preserve the exact legacy argument lists;
their old algorithms use only those fields. The table and normalizer preserve
all additional definition data and source declarations.

Twenty-two complete before/after source observations are equal. Ten genuine
plans/reports are emitted and ten interpreted/compiled executions agree;
twelve genuine malformed-source refusals retain exact source facts and
diagnostics. Inputs cover both source extensions, defn/def-fn, plain and
declared parameters/returns, explicit return wrappers, duplicate functions,
duplicate/variadic parameters, empty bodies and malformed headers. Raw
declarations, normalized tables, macro/module/plan/report data, source paths/
hashes, deterministic IDs, helper calls/values/classes and failures survive.
Baseline SHA256:
`5c16ef7dc33626f3a41bae65b85f9942ae40f70a06462dcd00d6d75a573a15e2`;
implementation SHA256:
`0fefebe0d07963836b7f5b475693ff64cbdf8b8836574363fd8b51708107660f`.
Both record the actual scope HEAD above and separate producer hashes, not a
future clean commit.

The source route directly exercises six table/classifier/header bodies.
An additional direct pure native header-validator control consumes the genuine
original module forms and records its own result. Together these cover all
seven bodies. That private helper control is not Stage2 source admission or
emission-context execution; no private source factory, authenticated builder,
Reader replacement or pin change is invoked. Its distinct control kind is
retained in each observation. Route-local nonclaims do not establish a global
absence of other source routes.

Focused tests pass 9 tests and 174 assertions. Earlier Runtime Stage0 and
namespace-parser regressions pass 44 tests and 760 assertions. Language/
ownership checks pass 48 tests and 1202 assertions. Cold loading verifies the
three public/four private API declarations and arglists, with no aliases or
Bootstrap, Resolution root, Diagnostics, Reader, Types or Checked Core loaded.
Focused CI controls pass 2 tests and 169 assertions and prove the new command
selects the whole new test namespace, including omission/wrong-namespace
negative controls. Child, test and changed CI files lint with zero errors/
warnings; diff checking passes. Documentation/roadmap validation, complete
current-Main lint, final source publication/reviews and own CI/provider audit
remain pending. No parent acceptance transfers.

### Normalizer transport correction

Initial implementation commit `260d3824028cd9531438be2678558f25afa54d79`
received bounded static reviews, then failed the first complete Main comparison
with three unused-field warnings in its Bootstrap normalizer adapter. An
additional adversarial lookup control also exposed a compatibility defect:
the adapter and child both destructured the same caller-owned definition.
The control observed six field reads before the header callback where the
original algorithm performs three. The failing 1-test/3-assertion capture and
initial review/comparison artifacts remain history and confer no successor
acceptance. That implementation was not published beyond the scope draft.

The private normalizer bridge now takes the already-destructured field map
and original definition separately. This preserves the original Bootstrap
signature, reads caller fields once, passes the original carrier to the header
callback, and keeps it for normalization/metadata retention. The normalization
algorithm is unchanged; the private child bridge changes from four arguments
to five. Transport correction receipt SHA256:
`3001049d144b1e59e630dd43cee303c93ffd756cb645fa79e5250c5166a48e08`.
Its initial arity-label typo is corrected by a separate verification of actual
before/after contract arglists, SHA256
`8ebfccb4a9c1ad5c715ad1c9f4af98febd2ac54ff46d3e59d4bc3779f42b9e11`;
the original receipt is preserved.

The updated focused suite passes 10 tests and 177 assertions, including the
lookup-order regression. Fresh Runtime/parser regressions pass 44/760,
language/ownership 48/1202, CI selection controls 2/169 and cold declarations
3 public/4 private. All 22 complete source observations still equal baseline:
10 plans, 10 interpreted/compiled successes and 12 genuine refusals. The new
capture records the actual dirty producer context at HEAD `260d382...`, with
separate updated producer hashes, SHA256
`7894020469e8773145fb29bb43f20fff8098cadb45825b429c0a44f55cdb6e28`.
It does not claim a future clean successor was observed. The clean corrective
source is `7d5df1d01ee5a6b5432f0288904328cf7a32f311`, tree
`3ac22e85c921c34c21d622419c9af0b389036d64`; independent corrective source
reviews bind that exact tuple without transferring the predecessor's verdict.

### Composition with current Main

The next comparison preparation stopped at its stale-base assertion before
running lint: Main had advanced to
`31fd889199d60d56f6dd26ff4e078b5bcf09b571`. Its two changes are the retained-
candidate seedless workflow and its tests. They are disjoint from the eight
owned paths and do not change the compiler contracts or shared nine-file SDK.
Root read the full diff and imported both exact mode/blob records through
ordinary Source merge `a3176328f3ff1bb771cefb5393a69fa449365eb6`, canonical
tree `46f6e160fe20959331c5feb99a99b5a0600a6c8c`, with parents
`7d5df1d01ee5a6b5432f0288904328cf7a32f311` and the new Main commit.
All eight owned records and all other feature records are conserved.
Composition receipt SHA256:
`e9004b061587f6efad3aac8b33e6dea45c6706f87cbef8fa0972838cc571edac`.
This invokes no seedless/controller/service/recovery role and transfers no
source, CI, integration or release acceptance.

Fresh source observations on that actual merge HEAD still equal all 22
baseline observations: 10 plans, 10 interpreted/compiled successes and 12
genuine refusals, SHA256
`879e6b1d70b9981433293c61098a4b5f126686a0dd1aa8927405186dc77c7226`.
The combined new/Runtime/parser suites pass 54 tests and 937 assertions;
language/ownership passes 48 tests and 1202 assertions. Complete current-Main
lint passes all 132 changed paths and 81 Clojure/EDN paths, with 3452 unchanged
baseline warnings and zero blockers, report SHA256
`ac81e44a1a7182f81a03923661f1a0cbbb3c8c7991c7c4f2d99a8318c61a8f1e`.
The original three warnings are fixed by using the captured fields in the
correct transport, without weakening lint policy. Own source publication,
hosted CI/provider audit and current-base integration acceptance remain pending.

Exact current-base integration remains with the designated integrator. This
manual Source has no Main/Ready/controller/service/recovery or GraphQL role.

This bounded native table is not canonical C5 resolution, C6 verified Core,
C7/L17 typing, source/plan authentication, a complete source-map/origin graph,
profile/effect/capability/ownership/safety admission, backend execution,
release, migration, self-hosting or seed retirement. Gravity/Uniform remains
the destination implementation language and Clojure remains temporary seed.
