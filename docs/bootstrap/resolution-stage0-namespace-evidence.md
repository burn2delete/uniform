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

The original scope started at frozen Effects commit
`9a0e5f4cbbc63564abc43ca740c71e709822ee60`, tree
`7635003a1d63d9384dacd032023f0ab93d86d62b`. At that scope capture, published
Main `4d0ab35a4d085eea3de3c8a2af0372578b62fdec` was already an ancestor,
and no additional Main paths were imported. The original nine-path lease
governs authoring. The final candidate must record its own exact corrective
source/Main composition and fresh checks; no predecessor source, provider or
CI acceptance transfers to it.

That Effects candidate subsequently failed CI `42356394110/1`: three stale
C8 contract assertions failed and a separate resolution job encountered
Maven Central HTTP 429 before executing its test. The candidate remains
rejected. Corrective draft PR #1332 retains its producer code and corrects
the C8 contract tests; its frozen source is
`55df8cd899d140ca3dae742ca7aa8eb8b8786d84`, tree
`17b5b37ed9605b1bdbaa4abc647daa502f2bc98a`. Its own CI `42362158063/1`
completed successfully on tested Main
`b722e29a0096381fbf476ca04771fe6567657f69`. The independent audit records
all 68 command receipts passing, 11 intact artifacts and 1014 tests with
32416 assertions. Those exact corrective-source facts do not accept the
rejected predecessor or grant this Parser candidate integration eligibility.
Adopting the verified correction requires an explicit own-branch composition,
fresh Parser checks and commit-bound independent review.

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

## Current local evidence and unsupported boundaries

The eight child operations and eight legacy adapters across three helper files
are implemented. The early draft publication pinned the scope-only commit
`9e016505ef402fa60005cf44fc5db6a5df3e8e45`, not an implementation commit.
The local observations below retain that exact scope head and working-file
hashes; they do not claim a later source commit or hosted run was observed.
CI registration and the namespace boundary map are implemented. The candidate
still requires clean-source reviews, exact-base checks and its own hosted CI.
The early-scope nine-file SDK manifest SHA256
`33cf71a66f4482ed358362aca7438bdcc80a2544c1381cb7131ad1e0a256ca07`
is historical context, not a current-Main or later-base acceptance claim.

The focused namespace passes 11 tests and 241 assertions, including an exact
eight-name public API inventory and explicit operation/input/callback contract
checks. Eight selected existing module-analysis tests pass 101 assertions;
the separate synthetic module-artifact-construction test is outside this
increment's verified route and was not selected. Language/ownership checks
pass 48 tests and 1202 assertions. The focused namespace's initial two
assertion failures incorrectly expected textual `:keys` declaration order;
the corrected expectation records the identical native hash-map lookup order
of the original body, child and adapter. No producer change was needed.

The twenty-nine-command contract list appends only
`resolution-stage0-namespace-validation` at index 28, running the entire
`gravity.resolution.stage0-namespace-test` namespace with the existing 2g heap
and 180-second host watchdog. All earlier twenty-eight command vectors remain
unchanged. Local CI harness controls pass 54 tests and 706 assertions. The
initial harness invocation had three launch errors because its shell omitted
the installed linter from PATH; the corrected tool environment passes without
changing tests or source. The initial error log remains history.

Baseline receipt SHA256
`193f1d41bd3d197f71b760bb811ef242a0b9393d55f32d63c1c07e261a67b3d5`
and current receipt SHA256
`c7c0c0820d69b418431c426a3f27b2fde74e5437d649f6e0f0e3581f984efd59`
record eighteen equal downstream observations: seven macro artifacts, five
plans, five executions and eleven refusals, including one earlier Reader L1
control. They preserve observed source facts, identities, reports, stdout and
parser call arguments. The macro artifact selects only part of the module
map, so these observations alone do not compare every parser return field.

Independent provisional adversarial review SHA256
`73b06bf58dc12fcc88fcd2815ce9f0146fbd83a8493a14224cc45c8c60cb45b0`
and probe receipt SHA256
`14630306af4a3e7108189e8e8f3e038b3ac7bc61684231f417a06ed4fc5e769f`
record nineteen original/current matches, including complete normalized module
maps, rich dependency metadata, callbacks, opaque lookups, exceptions and
legacy adapter behavior. Separate body-conservation receipt SHA256
`50c0ae4cb2031635cb6e4df58a119f5d4f54bd8f59a23ae6dfa9e261f852a184`
compares all eight definitions after normalizing only generated anonymous
function symbols, with no differences. These are dirty-worktree observations;
they do not accept a future commit, integration base or hosted run.

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
