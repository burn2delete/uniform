# Tasks: Resolve implementation workflow

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner role: compiler resolve maintainer; existing implementation reservations are unchanged.
Authority transfer baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Implementation tasks

This is the sole editable Resolve implementation task ledger. The roadmap's
[current evidence and remaining work](../../docs/component-roadmaps/resolve.md#current-evidence-and-remaining-work)
is generated from these records. IDs preserve the established phase/title slug
convention; the original `resolve/1` through `resolve/5` labels remain display
text. Order is display order, not dependency evidence. An unchecked box means
not accepted complete. Check a box only with explicit `Accepted;` status and an
immutable accepted-outcome receipt independently reviewed by the responsible
owner. Structural validation cannot grant acceptance. No task closes here.

The frozen [owner source-to-Core verification](../resolve-owner-source-core-verification/spec.md)
package is a separate finite evidence task. It remains unchanged; its historical
cohort and accepted scope do not establish completion of these five outcomes.

<!-- resolve-implementation-tasks:start -->

- [ ] `resolve/current-evidence-and-remaining-work/resolve1-resolve-lexical-positions` resolve/1: Resolve lexical positions

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Cover locals, loop/recur, vars, macros, types, protocols, constructors, capabilities and special positions with stable identity and legal shadowing. C5/L3 binding-position criterion language/module/resolve-binding (Open): Consume actual expanded syntax and its namespace/hygiene/macro context after expansion and before type/effect checking; every executable or declarative symbol resolves to an explicit identity or diagnostic. Use actual lexical stack, recur/function bindings, namespace, aliases, required/foreign tables, profile-allowed core imports, active profile/target, package graph and language facets. Preserve L3 unqualified expression order: lexical, recur/parameter, current namespace, profile-permitted core; macro/special forms use their syntactic positions. Alias-qualified and fully qualified names bypass that unqualified search and resolve through the actual alias/namespace/foreign records. C5's local/recur/namespace/alias-qualified/fully-qualified/core/foreign/intrinsic cases remain distinct eligible contexts; intrinsics require explicit syntax, macro positions prefer macro bindings over expression vars, and type/protocol positions use the type namespace. Required outputs: syntax-to-binding table and scope graph with positive local/namespace/alias/fully-qualified/macro/type/foreign/intrinsic and legal-shadowing fixtures. Reject unresolved/ambiguous names, illegal shadowing, missing/duplicate aliases and invented host bindings with C5-UNRESOLVED/AMBIGUOUS/SHADOW/ALIAS and applicable L3 diagnostics, retaining the actual resolved/candidate identities.

- [ ] `resolve/current-evidence-and-remaining-work/resolve2-validate-module-boundaries` resolve/2: Validate module boundaries

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Resolve aliases/imports/exports against actual module artifacts; reject absent, private or ambiguous names rather than synthesizing unsupported external bindings. Criterion behavior/pkg1/project-entrypoint-resolution: accept Resolve each artifact-kind entrypoint to its actual source var or artifact declaration after namespace analysis under the owning visibility rules. Reject An entrypoint is absent, ambiguous, violates its owning visibility rules or resolves to the wrong declaration kind. Emit Entrypoint-to-binding/artifact table consumed by the emission plan; PKG1003 names manifest key, package, profile and target. Dependencies: L3, PKG1, B13. C5/L3 import/visibility criterion compiler/c5/explicit-imports language/module/enforce-export-visibility (Open): Resolve Gravity requires and foreign imports only from actual module artifacts, package manifests or explicit typed foreign/interop records; never inspect ambient host namespaces/globals or fabricate a dependency identity. Preserve profile, target, effects, capability requirements, safety, package/version and build-effect metadata on imports. Public API is the exported set plus package-public declarations; unexported definitions remain private, and debug/provenance visibility or an inspection grant does not authorize stable public import. Required outputs: actual alias/import/export tables, public API manifest, checked foreign records and positive/refused visibility/import fixtures. Reject absent/private/ambiguous names, stable-module wildcard imports, malformed foreign records, unsupported targets, unavailable required capabilities and missing metadata with C5-PRIVATE/ALIAS/FOREIGN/TARGET/CAPABILITY and L3-UNKNOWN-ALIAS/PRIVATE-IMPORT/AMBIGUOUS-NAME. The existing PKG1 entrypoint predicate remains unchanged; package retrieval and interop implementation are separate producers, not prerequisites for declared artifact fixtures or authority inferred from lookup.

- [ ] `resolve/current-evidence-and-remaining-work/resolve3-check-graph-legality` resolve/3: Check graph legality

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Record dependency versions and initialization edges; reject unapproved cycles and illegal cross-profile imports, preserving effect/capability metadata. P1/P2/P4/P13: validate direct/facade-required/artifact-only/rejected edges after expansion/resolution; artifact consumption never implies source import. Check facade symbols/effects/grants/memory/runtime/error/safety/provider/provenance evidence against the actual producer/consumer matrix. Record all dependency edges and missing evidence; reject macro-hidden edges and package widening. Core imports require core support or already-present authority-free artifact data; kernel native facades must establish no-GC/no-hidden-allocation/no-throw and supported atomics. C5/L3 namespace/initialization criterion language/module/check-initialization-graph (Open): Record the namespace contract for each normal source file's single ns, actual name/package/profile/target/source-path identity, one active implementation profile or declared reusable-library profile set with per-profile exclusions/variants, requested targets, requires/imports/exports, effect allowance, required capabilities, default safety and selected providers. Library support sets cannot union profile powers; target selection cannot legalize a forbidden profile behavior or claim core portability for a target-specific import. Emit versioned dependency and initialization edges. Reject cycles unless a declared checked recursive/module-initialization protocol explicitly admits them. Same-profile edges still require legal effects/authority; cross-profile edges require an actual pure-core API, profile-safe facade, typed schema/artifact, FFI ownership/effect/capability/safety boundary or generated artifact consumed through package metadata. Record boundary kind and missing evidence; runtime availability alone is insufficient and artifact consumption is not a source-import grant. Required outputs: namespace/dependency/initialization graphs, cross-profile edge and declaration/authority reports, accepted core/firmware and refused hosted-UI/kernel fixtures, cycle/protocol and profile cases. Preserve declared effect allowances and capability requests for actual Effects/Auth/profile/safety consumers; actual inferred summaries must fit declarations and package/deployment/build grants, with widening or absent authority rejected rather than treating resolution success as semantic approval. Emit C5-CYCLE/CROSS-PROFILE/TARGET/CAPABILITY and L3-NS-MISSING/PROFILE-MULTIPLE/CROSS-PROFILE/EFFECT-WIDEN/CAPABILITY-MISSING at the owning stage.

- [ ] `resolve/current-evidence-and-remaining-work/resolve4-publish-stable-indexes` resolve/4: Publish stable indexes

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Serialize scope/binding graphs and invalidate dependent references when export, profile, package or semantic identity changes. C5/L3 definition/interface criterion language/module/publish-definition-entries language/module/emit-reproducible-interface (Open): Publish actual expanded/analyzed def/defn/defmacro/defschema/defprotocol/facet entries with name, distinct kind, visibility, source span, type references, latent effects, required capabilities, profile restrictions, safety mode, artifact links and present deprecation/stability metadata. Binding records retain namespace/package/version/relevant profile-target/definition-kind/visibility/source and definition-artifact identity; binding IDs depend on semantic definition fields, excluding documentation/nonsemantic formatting while retaining separate exact provenance. Namespace identity cannot collapse to its name alone. Emit a reproducible/content-addressable module interface before target lowering with namespace/package/profile/target, aliases/exports/local-scope hash/syntax-binding map, requires including boundary/effect/authority facts, explicit foreign imports/rejected edges, diagnostics, source hash and definition identity. Actual backend/MIR/package consumers derive dependency/capability graphs, docs and conformance targets without rereading source. Required outputs: serialized namespace/binding/scope/module/public-API artifacts and stable index/invalidation records. Invalidate on namespace source, imports/exports, aliases, dependency versions, visibility, kind, profile/target set, language facets or macro-altered symbol positions, explicitly naming affected downstream artifacts/rechecks. Positive fixtures retain semantic binding identity across formatting while updating accurate source/artifact provenance; reject stale exports/keys, missing metadata, incompatible reused identities and unrecorded downstream invalidation.

- [ ] `resolve/current-evidence-and-remaining-work/resolve5-integrate-binding-consumers` resolve/5: Integrate binding consumers

Current evidence/status: Open; partial source evidence above is not closure

Done when (yes/no): Connect Core/Types and diagnostic/LSP consumers; demonstrate accepted shadowing and imports plus rejected ambiguity, visibility, cycle and stale-export cases. C5/L3 consumer/conformance criterion (Open): Connect actual C3/C4 expanded-context and L1/L2 namespace-form inputs, explicit package/profile/foreign artifacts and resulting bindings to Core/Types/Effects/Auth/profile checks, package graphs, documentation/LSP and incremental consumers. Namespace tables precede expansion where required by L3; final symbol binding follows expanded positions under C5, and unresolved references cannot enter Types. Declared type/effect/authority records remain distinct from actual later inference/legality results; all required module summaries and facts must be current before target consumption. Required outputs: real producer/consumer and serialization/invalidation results covering every C5/L3 resolution position, stable binding identity, single-profile/multi-profile library exclusions, public/private and foreign imports, legal/refused shadowing, graph/cross-profile/target/capability cases and source-free package graph derivation. Preserve all ten C5 diagnostic meanings and all eight L3 meanings, with symbol/syntax ID/span/namespace/profile/target/candidate-bindings-when-applicable/dependency-edge/remediation context. Namespace-rule changes require conformance-fixture updates and package compatibility review under their owning policies; this source reconciliation changes no language rule. Preserve the separate frozen owner source-to-Core verification package and its finite scope. These are future implementation/evidence requirements, not current resolver/module/profile/package/runtime/probe execution, whole-component acceptance or independent source approval.

<!-- resolve-implementation-tasks:end -->

## Historical migration record

The original scaffold below is frozen history at the transfer baseline, not
implementation work or a completion denominator. Its pending review/integration
text records what that scaffold stated, not a fresh claim about its merged PR.
The implementation authority above supersedes its navigation-only rule; no
historical checkbox is reclassified.

# Tasks: Resolve workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md)
Owner: Frontend Spec Kit migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare `specs/resolve-workflow/spec.md` and `plan.md` with retained contract and canonical task/acceptance links.
- [x] T002 [US2] Prepare `specs/resolve-workflow/tasks.md` and `evidence.md` with migration-only checks and explicit implementation nonclaims.
- [x] T003 [US1] Validate all four artifacts, local links/anchors, ASCII and exact owned diff; record positive and negative checks in `evidence.md`.
- [ ] T004 [US2] Independently review the immutable package candidate and record scope and findings in `evidence.md`.
- [ ] T005 [US2] Integrate the reviewed documentation increment and record the verified PR/merge receipt in `evidence.md`.

## Dependencies and outcomes

T001/T002 prepare the package; T003 validates it; T004 reviews that candidate;
T005 integrates only that reviewed scope. This five-task denominator is workflow
migration, not a percentage of Resolve implementation or language completion.
Implementation task status remains solely in [the canonical table](../../docs/component-roadmaps/resolve.md#current-evidence-and-remaining-work).
No existing open component task becomes complete because this package exists.
Partial work remains visible in a draft PR.

## New-name review

Only semantic `resolve-workflow` documentation paths are added. No code names
or compatibility IDs change; [semantic naming guidance](../../.specify/semantic-names.md)
continues to govern later implementation.
