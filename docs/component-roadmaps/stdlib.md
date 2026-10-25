# Standard Library

Working name: **Standard Library** (proposed). Semantic key: `stdlib`.
Owner role: library maintainer; current owner unassigned by this proposal.
Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

## Purpose and scope

Provide real profile-aware APIs, not host-language escape hatches. The finite
module inventory is the [standard-library contract index](../phase-16-standard-library/README.md).
Own exports, manifests, safe wrappers and examples; compiler language forms,
runtime providers and platform implementation remain separate components.

## Release outcome

The contract inventory's modules have implemented exports, profile/effect/
capability/allocation/stability metadata, executable API examples and rejected
profile/authority fixtures. Unsupported profile/module pairs are explicit.
Full platform modules consume Platform outcomes rather than implying them.
Outcome open; this does not promote metadata coverage to functioning libraries.

## Interfaces and dependencies

Inputs: typed language operations, runtime/provider APIs and module schemas.
Outputs: compiled modules, export manifests, documentation examples, unsafe
island audits and compatibility evidence. Core/Types/Effects/Legality/Auth are
required for compiled API acceptance; Runtime is required only for serviced
APIs. Pure library algorithms and fixtures can develop earlier with the seed.
Build consumes manifests later, so no circular build prerequisite exists.

## Remaining tasks and evidence

| Task (all open) | Evidence baseline | Closing evidence |
| --- | --- | --- |
| Reconcile export inventory with actual callable implementations | [Library source artifact](../../bootstrap/clojure/src/gravity/bootstrap_impl/components/foundation/00499-standard-library-source-artifact.clj) | Per-export source/implementation/fixture map, with absent exports explicitly open |
| Complete pure core, collections, text, numeric and testing APIs | [Library architecture](../phase-16-standard-library/211-std1-standard-library-architecture.md) | Compiled examples and type/arity/value rejection through real APIs |
| Complete resource/concurrency/IO/network APIs | [Module inventory](../phase-16-standard-library/README.md) | Provider-mediated executions, missing-grant rejection and safe resource teardown |
| Complete schema/query/workflow/AI/meta/crypto/UI adapters | [Module inventory](../phase-16-standard-library/README.md) | APIs consuming corresponding domain/runtime outcomes, with boundary failures exercised |
| Integrate platform modules and close release manifests | [Platform outcome](platform.md#release-outcome) | Actual supported platform API fixtures, unsafe audits and compatibility/stability report |

Architecture gate owner: library maintainer. Recommendation: use contract
modules as export ownership boundaries and separate target adapters from pure
APIs. Record the selected module/profile release matrix before claiming module
completion; do not reduce the normative inventory by silently omitting modules.

## Acceptance and integration

Yes/no: does every claimed export execute? Do manifests and compiled exports
agree? Are effects/grants checked? Are unsafe internals isolated? Do all shipped
examples compile for their stated profiles? All must be yes. Integration:
pure compiler support, serviced Runtime APIs, domain adapters, then Build package
and Release compatibility. A builtin used by a bootstrap fixture is not proof
of the entire standard library.

## Compiler support outcome

`stdlib/compiler-support` is the finite release slice comprising the exact
exports reachable from the audited complete compiler source closure. The
library owner records those exports and profiles, implements their semantics,
executes accepted/rejected API fixtures and verifies no unresolved or hidden
host-only dependency remains in the claimed compiler path. It uses the relevant
inventory, pure/API, resource and manifest tasks above without duplicating them.
The selected compiler release may consume this accepted slice without waiting
for UI, AI, platform libraries or every general-purpose module. Full inventory
completion remains the separate wider-language outcome; the compiler slice is
not permission to redefine that full inventory.
