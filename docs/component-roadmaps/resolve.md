# Resolve component roadmap

Working name: **Resolve** (proposed default, not an approved codename).
Semantic key: `resolve`. Status: proposed roadmap; release acceptance open.
Source baseline: `c21068a6d39259b7110845bbbcefaf9bf6901232` (2026-10-25).
Owner role: compiler resolve maintainer.
Current owner: not established by this source inspection; Deus assigns or confirms
the existing owner before implementation. This page does not transfer ownership.

## Purpose

Bind symbols to explicit lexical, module, package and foreign identities without ambient host lookup.

## Release outcome

A stable binding and module-dependency graph covering every required binding position, visibility/import rule and accepted cross-profile boundary, with deterministic unknown/ambiguous/cyclic dependency diagnostics.

This is a finite component release, not perpetual improvement and not a claim
that the implementation already conforms. The [governing compiler contract](../phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md)
defines the required semantic surface; this page schedules it without replacing
the specification. Future language revisions require a new outcome revision.

## Scope and non-goals

Own lexical scopes, namespace/import/export analysis, resolution and invalidation keys. Package retrieval, macro execution and type inference are outside this project. No project-cost budgets, function counts, token/form/depth quotas or
task-count allowances define completion. Existing diagnostic/schema/wire IDs
remain compatible unless an explicit versioned migration is approved; new
implementation names use semantic subsystem names, not planning IDs.

## Inputs, outputs and interfaces

- Inputs: Expanded syntax/core references, lexical scopes, module exports, pinned package identities, profile/target and explicit foreign-import records.
- Outputs: Binding/alias/import/export tables, scope and dependency graphs, cross-profile report, diagnostics and invalidation keys.
- Hard prerequisites: Syntax and Expand artifacts are integrated inputs. Core construction and Resolve may share traversal, but unresolved references cannot enter Types. Export records are contracts, not a prerequisite for completing the package manager.
- Parallel interfaces: Core and module tooling can develop against binding-table fixtures; profile and package owners provide compatibility/version records independently.

## Current evidence and remaining work

Inspected source: [existing implementation](../../bootstrap/clojure/src/gravity/c5_name_resolution/resolution.clj).
The current resolver constructs alias/local/core bindings and includes a dotted fully-qualified-name branch synthesizing a stage0 package identity. This is an inspection finding, not proof of complete module validation. Existing paths containing planning identifiers are historical source
references, not proposed implementation naming. No compiler test was run for
this roadmap and no source inspection or pre-existing report is credited as
release acceptance.

<!-- resolve-task-projection:start -->
| Task | Status | Concrete exit evidence |
| --- | --- | --- |
| resolve/1: Resolve lexical positions | Open; partial source evidence above is not closure | Cover locals, loop/recur, vars, macros, types, protocols, constructors, capabilities and special positions with stable identity and legal shadowing. C5/L3 binding-position criterion language/module/resolve-binding (Open): Consume actual expanded syntax and its namespace/hygiene/macro context after expansion and before type/effect checking; every executable or declarative symbol resolves to an explicit identity or diagnostic. Use actual lexical stack, recur/function bindings, namespace, aliases, required/foreign tables, profile-allowed core imports, active profile/target, package graph and language facets. Preserve L3 unqualified expression order: lexical, recur/parameter, current namespace, profile-permitted core; macro/special forms use their syntactic positions. Alias-qualified and fully qualified names bypass that unqualified search and resolve through the actual alias/namespace/foreign records. C5's local/recur/namespace/alias-qualified/fully-qualified/core/foreign/intrinsic cases remain distinct eligible contexts; intrinsics require explicit syntax, macro positions prefer macro bindings over expression vars, and type/protocol positions use the type namespace. Required outputs: syntax-to-binding table and scope graph with positive local/namespace/alias/fully-qualified/macro/type/foreign/intrinsic and legal-shadowing fixtures. Reject unresolved/ambiguous names, illegal shadowing, missing/duplicate aliases and invented host bindings with C5-UNRESOLVED/AMBIGUOUS/SHADOW/ALIAS and applicable L3 diagnostics, retaining the actual resolved/candidate identities. |
| resolve/2: Validate module boundaries | Open; partial source evidence above is not closure | Resolve aliases/imports/exports against actual module artifacts; reject absent, private or ambiguous names rather than synthesizing unsupported external bindings. Criterion behavior/pkg1/project-entrypoint-resolution: accept Resolve each artifact-kind entrypoint to its actual source var or artifact declaration after namespace analysis under the owning visibility rules. Reject An entrypoint is absent, ambiguous, violates its owning visibility rules or resolves to the wrong declaration kind. Emit Entrypoint-to-binding/artifact table consumed by the emission plan; PKG1003 names manifest key, package, profile and target. Dependencies: L3, PKG1, B13. C5/L3 import/visibility criterion compiler/c5/explicit-imports language/module/enforce-export-visibility (Open): Resolve Gravity requires and foreign imports only from actual module artifacts, package manifests or explicit typed foreign/interop records; never inspect ambient host namespaces/globals or fabricate a dependency identity. Preserve profile, target, effects, capability requirements, safety, package/version and build-effect metadata on imports. Public API is the exported set plus package-public declarations; unexported definitions remain private, and debug/provenance visibility or an inspection grant does not authorize stable public import. Required outputs: actual alias/import/export tables, public API manifest, checked foreign records and positive/refused visibility/import fixtures. Reject absent/private/ambiguous names, stable-module wildcard imports, malformed foreign records, unsupported targets, unavailable required capabilities and missing metadata with C5-PRIVATE/ALIAS/FOREIGN/TARGET/CAPABILITY and L3-UNKNOWN-ALIAS/PRIVATE-IMPORT/AMBIGUOUS-NAME. The existing PKG1 entrypoint predicate remains unchanged; package retrieval and interop implementation are separate producers, not prerequisites for declared artifact fixtures or authority inferred from lookup. |
| resolve/3: Check graph legality | Open; partial source evidence above is not closure | Record dependency versions and initialization edges; reject unapproved cycles and illegal cross-profile imports, preserving effect/capability metadata. P1/P2/P4/P13: validate direct/facade-required/artifact-only/rejected edges after expansion/resolution; artifact consumption never implies source import. Check facade symbols/effects/grants/memory/runtime/error/safety/provider/provenance evidence against the actual producer/consumer matrix. Record all dependency edges and missing evidence; reject macro-hidden edges and package widening. Core imports require core support or already-present authority-free artifact data; kernel native facades must establish no-GC/no-hidden-allocation/no-throw and supported atomics. C5/L3 namespace/initialization criterion language/module/check-initialization-graph (Open): Record the namespace contract for each normal source file's single ns, actual name/package/profile/target/source-path identity, one active implementation profile or declared reusable-library profile set with per-profile exclusions/variants, requested targets, requires/imports/exports, effect allowance, required capabilities, default safety and selected providers. Library support sets cannot union profile powers; target selection cannot legalize a forbidden profile behavior or claim core portability for a target-specific import. Emit versioned dependency and initialization edges. Reject cycles unless a declared checked recursive/module-initialization protocol explicitly admits them. Same-profile edges still require legal effects/authority; cross-profile edges require an actual pure-core API, profile-safe facade, typed schema/artifact, FFI ownership/effect/capability/safety boundary or generated artifact consumed through package metadata. Record boundary kind and missing evidence; runtime availability alone is insufficient and artifact consumption is not a source-import grant. Required outputs: namespace/dependency/initialization graphs, cross-profile edge and declaration/authority reports, accepted core/firmware and refused hosted-UI/kernel fixtures, cycle/protocol and profile cases. Preserve declared effect allowances and capability requests for actual Effects/Auth/profile/safety consumers; actual inferred summaries must fit declarations and package/deployment/build grants, with widening or absent authority rejected rather than treating resolution success as semantic approval. Emit C5-CYCLE/CROSS-PROFILE/TARGET/CAPABILITY and L3-NS-MISSING/PROFILE-MULTIPLE/CROSS-PROFILE/EFFECT-WIDEN/CAPABILITY-MISSING at the owning stage. |
| resolve/4: Publish stable indexes | Open; partial source evidence above is not closure | Serialize scope/binding graphs and invalidate dependent references when export, profile, package or semantic identity changes. C5/L3 definition/interface criterion language/module/publish-definition-entries language/module/emit-reproducible-interface (Open): Publish actual expanded/analyzed def/defn/defmacro/defschema/defprotocol/facet entries with name, distinct kind, visibility, source span, type references, latent effects, required capabilities, profile restrictions, safety mode, artifact links and present deprecation/stability metadata. Binding records retain namespace/package/version/relevant profile-target/definition-kind/visibility/source and definition-artifact identity; binding IDs depend on semantic definition fields, excluding documentation/nonsemantic formatting while retaining separate exact provenance. Namespace identity cannot collapse to its name alone. Emit a reproducible/content-addressable module interface before target lowering with namespace/package/profile/target, aliases/exports/local-scope hash/syntax-binding map, requires including boundary/effect/authority facts, explicit foreign imports/rejected edges, diagnostics, source hash and definition identity. Actual backend/MIR/package consumers derive dependency/capability graphs, docs and conformance targets without rereading source. Required outputs: serialized namespace/binding/scope/module/public-API artifacts and stable index/invalidation records. Invalidate on namespace source, imports/exports, aliases, dependency versions, visibility, kind, profile/target set, language facets or macro-altered symbol positions, explicitly naming affected downstream artifacts/rechecks. Positive fixtures retain semantic binding identity across formatting while updating accurate source/artifact provenance; reject stale exports/keys, missing metadata, incompatible reused identities and unrecorded downstream invalidation. |
| resolve/5: Integrate binding consumers | Open; partial source evidence above is not closure | Connect Core/Types and diagnostic/LSP consumers; demonstrate accepted shadowing and imports plus rejected ambiguity, visibility, cycle and stale-export cases. C5/L3 consumer/conformance criterion (Open): Connect actual C3/C4 expanded-context and L1/L2 namespace-form inputs, explicit package/profile/foreign artifacts and resulting bindings to Core/Types/Effects/Auth/profile checks, package graphs, documentation/LSP and incremental consumers. Namespace tables precede expansion where required by L3; final symbol binding follows expanded positions under C5, and unresolved references cannot enter Types. Declared type/effect/authority records remain distinct from actual later inference/legality results; all required module summaries and facts must be current before target consumption. Required outputs: real producer/consumer and serialization/invalidation results covering every C5/L3 resolution position, stable binding identity, single-profile/multi-profile library exclusions, public/private and foreign imports, legal/refused shadowing, graph/cross-profile/target/capability cases and source-free package graph derivation. Preserve all ten C5 diagnostic meanings and all eight L3 meanings, with symbol/syntax ID/span/namespace/profile/target/candidate-bindings-when-applicable/dependency-edge/remediation context. Namespace-rule changes require conformance-fixture updates and package compatibility review under their owning policies; this source reconciliation changes no language rule. Preserve the separate frozen owner source-to-Core verification package and its finite scope. These are future implementation/evidence requirements, not current resolver/module/profile/package/runtime/probe execution, whole-component acceptance or independent source approval. |
<!-- resolve-task-projection:end -->

The table above is generated from the sole editable
[implementation task ledger](../../specs/resolve-workflow/tasks.md#implementation-tasks).
Edit that ledger, then run `clojure -M tools/resolve_task_projection.clj --write`;
verify with `--check`. This transfer changes no task status or release criteria.
Close a task only with the exact source revision, actual invocation/result and relevant
accepted/rejected artifacts, reviewed by the responsible owner. Do not infer
completion from assertion counts, generated status fields or filenames.

## Acceptance criteria (yes/no)

- [ ] Every executable/declarative reference resolves to a verified binding or a stable diagnostic.
- [ ] No ambient host namespace or fabricated dependency grants authority.
- [ ] Scope and dependency records survive serialization and correctly invalidate on changed exports.
- [ ] Each task has source-pinned execution evidence for the claimed surface,
  with residual host boundaries explicit and independent acceptance recorded.
- [ ] The owner confirms the release outcome without claiming whole-language,
  whole-front-end migration or seed retirement from this component alone.

## Integration milestones

1. Agree the named input/output schema and positive/negative contract fixtures
   with immediate producers and consumers; implementation may proceed in parallel.
2. Resolved-core handoff unlocks Types and Effects; module indexing can ship in parallel without granting compiler release status.
3. Record accepted component outcome here and let the [overall registry](README.md)
   reference it; the overall roadmap must not copy this task list.

## Traceability

[governing compiler contract](../phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md) is normative. The foundation pipeline and milestone ordering remain
owned by [D1](../phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
and [D2](../phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md).
Existing self-hosting milestones are evidence/traceability references, not this
project's identity and not authority to run restricted migration actions.
