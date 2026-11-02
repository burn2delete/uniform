# Standard Library

Working name: **Standard Library** (proposed). Semantic key: `stdlib`.
Owner role: library maintainer; current owner unassigned by this proposal.
Baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232`.

Implementation task authority: [Spec Kit ledger](../../specs/stdlib-workflow/tasks.md#implementation-tasks).

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

Task criteria, statuses, history and closing evidence live in the [sole editable task ledger](../../specs/stdlib-workflow/tasks.md#implementation-tasks).

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
