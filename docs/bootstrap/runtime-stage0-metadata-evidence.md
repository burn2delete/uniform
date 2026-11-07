# Native Stage0 runtime metadata checks

## Scope and governing contracts

Move the seven complete Runtime metadata bodies from foundation component
00535 to `gravity.runtime`: suite selection, suite presence, runtime-family
selection, managed manifests/null flow, capability-decision metadata,
observability-sink metadata, and aggregate validation. Retain all seven
Bootstrap signatures as thin late-Var adapters. Leave the managed required-field
catalog and the following domain catalog/suite helpers with their existing
owners. This increment does not move runtime proof/report orchestration.

D0, D1, D2, D3, D6, D8, D9, C1, C15, P4, R1, R4, R11, R12, BOOT8
and TEST2 govern the boundary. Root read the Runtime phase README and all four
owning Runtime contracts. The metadata checks run during existing Stage0 plan
assembly, before execution. They do not defer compiler legality to execution
and do not implement canonical runtime authority enforcement.

The frozen source parent is `e44d46682af7e5077b1c4e739b0a74d88d2655fe`,
tree `6144a5a89a54eb81ea6b29459c75efe99c06141a`. Published Main
`31fd889199d60d56f6dd26ff4e078b5bcf09b571` is already an ancestor.
The parent's normal eight-path writer release is recorded by source freeze
SHA256 `3763c31104f0d9ee28b73e2f417aa8a59c5e4fff492d8b3b37eb79151a112957`.
Its own CI/provider audit remains pending; no acceptance transfers. A new
six-path claim covers only Runtime, component 00535, its two existing test
files and the two evidence/map documents. No CI or workflow edit is planned.

## Public API and artifacts

The intended Runtime public additions are:

```clojure
(native-stage0-runtime-suite module)
(native-stage0-runtime-suite-present? module)
(validate-native-stage0-runtime-selection! operations module selection)
(validate-native-stage0-managed-runtime! operations module managed-runtime)
(validate-native-stage0-runtime-capability! fail-var module decision)
(validate-native-stage0-runtime-observability! fail-var module sink)
(validate-native-stage0-runtime! operations module)
```

Inputs are caller-owned native module/runtime metadata plus explicit late
owner Vars for existing failure constructors, set intersection, missing-field
calculation, required-field catalog and aggregate component validators. Outputs
are the original suite/presence values, nil, or unchanged owner returns and
exceptions. The enclosing genuine source route must emit real plans/reports,
execute accepted programs and refuse invalid runtime metadata before emission.
The checks create no new canonical runtime artifact or authority token.

## Ownership, diagnostics and dependencies

Runtime owns the seven existing algorithms and decisions/facts for
R1-SELECTION, R1-FORBIDDEN, R4-MANIFEST, R4-NULL, R11-GRANT and R12-SINK.
The existing owners retain generic diagnostic construction, catalog policy,
throw transport, source/module/plan authentication and publication. Effects
and capabilities remain separate; truthy supplied decision fields are native
metadata, not proof that a matching scoped grant exists.

Dependencies point Bootstrap to Runtime to Clojure core. Runtime must not
import Bootstrap, Reader, Diagnostics, another compiler stage or a host broker.
The set operation is an explicit existing owner Var; no new host dependency or
dynamic owner discovery is introduced.

Preserve every module/metadata lookup, false/nil/truthy distinction, empty-suite
behavior, ordered missing-field result, forbidden-service selection/sorting,
aggregate family order, lazy collection traversal, late callback lookup,
returning-failure behavior and host exception identity. Select each supplied
callback before its original argument evaluation. Preserve all legacy
Bootstrap arglists and Runtime's existing 54 APIs.

## Tests and acceptance

Extend the existing whole `gravity.runtime.stage0-execution-test` command with
focused positive and negative native metadata controls. Update the existing
Runtime exact public-API inventory for seven additions; inspect and run that
inventory separately from broad source-factory pipeline tests. Preserve all
existing execution tests and CI command definitions.

Cover all seven bodies, six diagnostic decisions, complete and incomplete
managed manifests, checked and wrapped nulls, denied/false/truthy capability
metadata, granted/denied sinks, suite absence versus explicit nil, aggregate
first refusal, callback interposition/timing, returning failures, original
opaque source/metadata identity and unchanged owner exceptions. Capture full
before/after observations for paired source extensions, real plans/reports,
successful compiled execution and genuine pre-emission refusals with exact
source facts, diagnostics and deterministic identity.

Run affected native regressions, language/ownership boundaries, cold Runtime
loading, changed-file lint, document validators and complete current-Main
comparison. Independent semantic/adversarial/conservation reviews bind the
eventual clean source. Open and maintain an early draft PR, then audit that
source's own hosted CI/provider tuple. A self-audit cannot confer independent
acceptance or integration eligibility.

## Status and unsupported boundaries

This is the initial scope record, before source implementation. Independent
placement review is in progress. Source observations, implementation tests,
clean source reviews, publication and own CI/provider audit remain pending.
There are no callable stubs or new implementation claims in this scope commit.

The slice does not implement scoped capability matching, live host adapters,
runtime grant/deny enforcement, revocation, redaction, event emission, complete
source maps, canonical R1/R4/R11/R12 conformance, source/plan authentication,
profile/effect/ownership/safety admission, backend execution, release,
migration, self-hosting or seed retirement. It does not invoke held
Reader/authentication/private source-factory/pin routes. Earlier broad Runtime
review evidence remains excluded. Route-local source observations cannot prove
global absence of other routes. Clojure remains the temporary seed language.

Exact current-base integration remains with the designated integrator. Manual
Source has no Main/Ready/controller/service/recovery or GraphQL role.
