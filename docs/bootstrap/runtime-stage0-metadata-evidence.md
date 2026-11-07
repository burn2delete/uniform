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

### Implementation and development evidence

Draft PR #1356 was opened at scope commit
`a6a62e05d96f40537b306f6e4dd0050c384b6a37`, tree
`4059d220b520161e60703e533932a24df5d2c7bf`, before moving source bodies.
Independent placement review SHA256
`4bbe4af9124c7d5623c8a4028a35c6ae318a2e894193077447182dac6144ceaa`
recommends the complete seven-body family in existing Runtime. It identifies
the absence of a nonempty accepted compiled-gate fixture as an evidence gap;
the new durable inline source fixture exercises every family, rather than
claiming a suite-absent plan proves those checks ran.

All seven implementations now live in Runtime behind seven original Bootstrap
adapters. Runtime has 61 public APIs, with all 54 prior bodies retained. There
are no new CI commands, workflow/catalog/load-order edits or host imports.
The complete original/moved-body, adapter and before/after source receipt has
SHA256 `112d736262ee17eab8eac041f8973cb8841851125ca6ec45f5f3d18503edda34`.
It precedes the added adversarial tests; its test snapshots record the initial
inventory-only edit and are not claimed as the final test file bytes.

Sixteen complete public-source observations are equal before and after the
move. Four genuine plans and compiler/runtime reports are emitted and four
interpreted/compiled executions agree. Twelve genuine source refusals preserve
all six diagnostic IDs and exact source/failure facts. Both extensions cover
the absent suite, the nonempty accepted suite and each existing rejected
fixture. The capture records complete macro/module/plan/report products,
helper arguments/results/classes and source/deterministic identities. Baseline
SHA256 `3a463fa39a8422d38a5ad4a7abf60f5b35e5ff72d148f444c49e683e3689748f`;
implementation SHA256
`0c8ed4b9f5a5db4b678221e7d42a360f7a6b37e9b991e1e046bcdbe98d165241`.
Both record actual scope HEAD above; the latter additionally records the dirty
producer context and updated source hashes, not a future clean commit. Fixture
bytes are supplied as text under paired paths, with no file-authentication or
runtime-authority claim.

The focused suite passes 41 tests and 691 assertions. It includes the prior
33 execution tests plus eight metadata tests covering all seven bodies,
nonempty real source emission, real refusals, opaque lookup and exception
identity, failure/phase ordering, catalog/helper rebinding, callback selection
before argument lookup, truthiness, returning failures and all seven legacy
adapter signatures. The initial new exception control supplied an ILookup-only
carrier to a helper requiring `contains?`; its three failed assertions remain
history. The control now supplies an explicit missing-field operation so it
reaches the intended owner-exception boundary; implementation bytes did not
change for that test correction.

The single existing native public-inventory test passes 1 test/435 assertions
without forcing any delayed source factory. Language and ownership gates pass
separately at 6/11 and 42/1191. Cold loading verifies all 61 APIs/arglists and
the absence of Bootstrap/Diagnostics imports or aliases. Changed files lint
with zero errors/warnings. Canonical document and roadmap structural checks
pass; the roadmap's dated coverage counts are explicitly non-authoritative.
An initial regression command used an unknown namespace and stopped before
tests; its log is preserved. The corrected declaration and namespace-parser
regressions pass 21 tests/418 assertions. Full current-Main lint, clean source
review/publication and own CI/provider audit remain pending.

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
