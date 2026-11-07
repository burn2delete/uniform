# Hosted Stage0 namespace-declaration parser

## Scope and governing contracts

Move the eight complete existing clause, dependency and module parser bodies
from `gravity.module-analysis.clauses`, `.dependencies` and `.module` into
`gravity.resolution.stage0-namespace`. Retain the old names and argument lists
as thin compatibility adapters. This is a parser algorithm extraction, not a
new source intake or a claim of canonical C5 resolution.

D0, D1, D2, D3, D6, D8, D9, L3, C1, C5, C15, P1, B1, BOOT1, BOOT8 and
TEST2 govern this bounded increment. P1/B1 constrain the interpretation of
profile/target data; the existing Stage0 header diagnostics do not implement
their complete manifest or eligibility contracts.

The source parent is frozen Effects commit
`9a0e5f4cbbc63564abc43ca740c71e709822ee60`, tree
`7635003a1d63d9384dacd032023f0ab93d86d62b`. Published Main
`4d0ab35a4d085eea3de3c8a2af0372578b62fdec` is already an ancestor. This
slice imports no additional Main paths and inherits no predecessor source,
provider or CI acceptance. The original nine-path lease governs authoring.

## Public API and artifact boundary

The proposed child APIs retain the existing helper signatures:

```clojure
(require-ns {:keys [ns-form? fail! source-span]} source-path forms)
(parse-clause {:keys [fail! source-span]} source-path clause)
(single-clause-value {:keys [fail! source-span]}
                     source-path clause-map key required?)
(clause-args clause-map key)
(parse-options {:keys [fail! source-span]} source-path entry option-items)
(parse-dependency-entry {:keys [fail! source-span parse-options]}
                        source-path kind entry)
(parse-dependencies {:keys [parse-dependency-entry]}
                    source-path kind entries)
(parse-module {:keys [require-ns parse-clause single-clause-value clause-args
                      parse-dependencies fail! source-span known-source-profiles
                      supported-profiles supported-targets
                      bootstrap-target-supported?]}
              source-path forms)
```

Inputs are existing caller-owned ordered forms, source-path facts, clauses,
dependency entries, explicit callbacks and profile/target catalog values.
These are caller-selected values, not uniformly live Var ports. Preserve
entry-time destructuring, call order and exception identity. Legacy adapters
must not repeat observable lookups on opaque operation maps or add a new
dereference, validation, source-intake or catalog-selection protocol.

Outputs remain the first namespace form, clause/argument vectors, option maps,
ordered dependency records and the existing normalized module declaration, or
the original failure callback return/exception. The module retains namespace,
source path, profile, target, effects, capabilities, requires/imports, exports,
safety, providers, metadata, documentation and forms. It is not a canonical
module artifact and the parser does not compute a hash or artifact identity.

The real downstream public macro route emits the existing macro artifact.
Source hashes, module facts, syntax identities/spans, generated origins,
expansion traces and subsequent plan/report identities must remain identical
for identical source and owner operations. Do not invent missing provenance.

## Dependency direction and diagnostic ownership

Code dependencies are Bootstrap to the module-analysis facade, then its three
compatibility helper namespaces to `gravity.resolution.stage0-namespace`,
then `clojure.string`. The child must not import Bootstrap, module analysis,
the SH06 Resolution root, Diagnostics, Reader authentication, release, package
or a backend. Source reading and macro expansion remain caller-owned.

The separate `gravity.resolution` root retains its existing 55 public APIs,
SH06 operation catalogs, construction roots and receipt invalidation contract.
The child has a different native Stage0 form/callback boundary. Its placement
does not add parser values to authenticated Resolution plans or receipts.

The moved parser owns its existing local decisions:
`L3-NS-MISSING`, `L3-NS-CLAUSE`, `L3-PROFILE-MULTIPLE`,
`L3-UNKNOWN-ALIAS`, `L3-AMBIGUOUS-NAME`, `L3-PRIVATE-IMPORT`,
`P1-PROFILE-UNSUPPORTED` and `B1-TARGET-UNSUPPORTED`. Supplied `fail!` and
`source-span` values retain payload construction, transport and source facts.
These legacy IDs must not be presented as canonical C5/P1/B1 diagnostics.

Earlier Reader/namespace-shape diagnostics retain their owner and first-refusal
order. Later alias/refer ambiguity, qualified-symbol resolution, cross-profile
admission and namespace effect/capability checks remain in the existing
boundary helpers. The Effects-owned inference and direct validator APIs are
unchanged. A successful parser return grants no capability or source authority.

## Evidence and planned verification

Direct discovery receipt SHA256
`8eaa28d78d5d59a65819f334bdf1d6c2fc3fd2598697fd2dd0151c9b68684aa1`
records four real public macro artifacts: hello and namespace-module sources
in both `.gravity` and `.qst` forms. Two real fixtures reject with
`L3-PROFILE-MULTIPLE` and `L3-PRIVATE-IMPORT`; all six invoke the actual
parser. The public route keeps authenticated source records nil and invokes no
held module-artifact builder, private source factory, Reader replacement or pin.

Read-only route supplement SHA256
`51068481b145c600097fa3453d651469a4f488bb67b85c274aa4c6a0e335871d`
and API placement review SHA256
`452bd5970553d7883dd66e2586d0abf8de184e79ed4a663ee9528acac31ac88a`
identify this closed parser family and its distinct input carrier. The earlier
broader module-analysis recommendation remains discovery history; it does not
establish authority or acceptance for module-artifact construction.

Compare complete before/after genuine macro products and executable positive
plans, source facts, IDs, reports, stdout and refusal payloads. Add focused
callback/order/opaque-lookup/identity tests, source-order and default-value
checks, malformed dependency/namespace controls and exact diagnostic ownership.
Preserve the complete eight original bodies and the unchanged outer facade,
Resolution root, Effects APIs and prior CI vectors. Append one whole-namespace
CI command. Run proportionate regressions, language-boundary and ownership
gates, docs/roadmap validation and complete current-Main lint. Independent
semantic, adversarial, conservation, provider and hosted-CI evidence must bind
the exact clean source and integration base before Main eligibility.

## Current status and unsupported boundaries

This is the early draft scope. No child API is implemented or accepted at
this capture. The current published-Main SDK is bound by immutable nine-file
manifest SHA256
`33cf71a66f4482ed358362aca7438bdcc80a2544c1381cb7131ad1e0a256ca07`.

This increment does not implement full L3 or canonical C5 resolution, binding
identities, lexical scopes/shadowing, cross-file imports or cycles, package
versions/trust, module-artifact construction/hash/publication, authenticated
Reader/syntax/macro products, complete P1 profile validation, B1 backend
eligibility, effect/capability/ownership/safety legality, MIR, target lowering,
runtime services, release, migration, self-hosting or seed retirement.
Conservative private-name and dependency-shape rules remain the existing
Stage0 approximations. Returning failure callbacks retain compatibility
continuation, not admission authority.

Module-artifact constructors and their file/from-records routes are outside
this slice's verified evidence; this statement does not reclassify every
legacy public route as denied. Actual held Reader/authenticated/private
builders/factories and source pins remain unchanged and uninvoked. The
designated integrator retains exact current-base preflight and Main
integration. This manual Source has no Main, Ready, controller, service or
recovery role.
