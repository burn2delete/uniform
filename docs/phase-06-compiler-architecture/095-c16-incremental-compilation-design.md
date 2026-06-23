# C16 - Incremental Compilation Design

Sequence: 95
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Incremental compilation reuses compiler artifacts without weakening semantic,
safety, profile, proof, or reproducibility guarantees. Each reusable artifact is
keyed by the source, compiler, profile, target, dependency, build-effect,
capability, pass, and policy facts that affect its meaning.

Reuse is an optimization. It never bypasses validation for stale or incomplete
artifacts.

## Requirements

- Incremental keys must include every fact that can affect the artifact's
  semantic meaning, legality, proof validity, diagnostics, or target output.
- Cache reuse must be stage-specific and validated against the producing pass
  contract.
- Source, macro, namespace, type, effect, profile, capability, safety, proof,
  provider, target, backend, package, and policy changes must invalidate
  dependent artifacts.
- Cached artifacts must retain diagnostics, provenance, and invalidation
  conditions.
- Stale proofs or certificates must be rejected or regenerated, never silently
  reused.
- Hermetic and release builds must be reproducible from recorded inputs and
  replay records.
- Interactive builds may use speculative caches, but emitted artifacts must
  record speculation and revalidation status.

## Dependencies

- `C1` defines the pipeline manifest.
- `C2` through `C15` define stage artifacts and diagnostics.
- `L12` defines compile-time replay and build effects.
- `SAFE15` defines proof and certificate invalidation.
- `PERF3` defines specialization cache invalidation.
- Phase 12 build and reproducibility documents define project, lockfile,
  artifact graph, and environment inputs.
- Phase 15 self-hosting documents consume stage comparison artifacts.

## Outputs and Artifacts

- Incremental dependency graph.
- Stage cache key schema.
- Cache entry manifest.
- Invalidation trace.
- Artifact reuse report.
- Revalidation report.
- Stale-proof rejection report.
- Incremental diagnostics.

## Dependency Graph

The incremental graph has nodes for:

- source units,
- syntax object streams,
- macro expansion traces,
- namespace analysis,
- typed core,
- effect graph,
- ownership graph,
- safety outcomes,
- MIR modules,
- optimization decisions,
- domain IR artifacts,
- target artifacts,
- diagnostics,
- proofs and certificates,
- package and provider manifests.

Edges record the exact field-level dependencies when available. Coarse edges are
allowed early in implementation but reduce cache reuse.

## Cache Key

```clojure
{:artifact :gravity/cache-key
 :stage :type-check
 :source source-hash
 :reader reader-options-hash
 :syntax syntax-stream-hash
 :macro-expansion expansion-trace-hash
 :namespace namespace-analysis-hash
 :profile profile-manifest-hash
 :target target-request-hash
 :compiler compiler-version-hash
 :pass-contract pass-contract-hash
 :dependencies dependency-graph-hash
 :build-effects replay-record-hash
 :capabilities capability-policy-hash
 :language-facets facet-set-hash}
```

Each stage owns the subset of fields required for its artifact. Missing required
fields make the key invalid.

## Cache Entry Manifest

```clojure
{:artifact :gravity/cache-entry
 :cache-key cache-key-hash
 :artifact-id artifact-hash
 :producer {:stage :type-check
            :pass-version pass-version}
 :inputs [input-hash-1 input-hash-2]
 :preserved-facts #{:source-spans :resolved-bindings}
 :invalidated-by #{:source-change :macro-change :type-rule-change
                   :profile-change :dependency-change}
 :diagnostics diagnostic-stream-hash
 :trust :local-build
 :revalidation :required-before-release}
```

Cache entries are artifacts. They can be inspected and invalidated like any
other compiler output.

## Invalidation Classes

Invalidation causes include:

- source bytes changed,
- reader options changed,
- reader extension changed,
- macro definition or macro dependency changed,
- build grant or replay input changed,
- namespace import/export/alias changed,
- package dependency version changed,
- type rule or type provider changed,
- effect registry or capability policy changed,
- profile manifest changed,
- safety rule, proof provider, or certificate trust policy changed,
- optimization pass contract changed,
- target feature or backend changed,
- runtime/provider manifest changed,
- language facet set changed,
- diagnostic schema changed.

The invalidation trace names affected nodes and downstream revalidation stages.

## Revalidation

Revalidation checks:

- cache key equality,
- artifact schema version,
- producer pass version,
- preserved fact set,
- proof/certificate freshness,
- profile and target compatibility,
- diagnostic schema compatibility,
- dependency graph compatibility.

If revalidation is partial, the artifact must carry `:reuse :speculative` and
cannot be published as a release artifact until full revalidation passes.

## Proof and Certificate Reuse

Proofs and certificates are reusable only when their claim, inputs,
assumptions, profile, target, compiler, provider, package, and invalidation
conditions still match. If a pass changes the IR node identity but preserves the
semantic operation, it must emit a proof mapping record before reuse.

## Diagnostics

Incremental compilation diagnostics use `C16` identifiers:

- `C16-KEY` for malformed or incomplete cache keys.
- `C16-ENTRY` for malformed cache entries.
- `C16-STALE` for artifact reuse after invalidating input changes.
- `C16-PROOF` for stale proof or certificate reuse.
- `C16-SPECULATIVE` for speculative reuse reaching a publishable boundary.
- `C16-REPLAY` for missing build-effect replay records.
- `C16-POLICY` for cache reuse across incompatible profile, capability, or
  safety policy.
- `C16-DIAGNOSTIC` for stale diagnostic streams.
- `C16-GRAPH` for dependency graph inconsistencies.

Diagnostics must include cache key, artifact id, stage, invalidating input,
source span or manifest entry when available, profile, target, and remediation.

## Rejected Designs

Gravity rejects timestamp-only incremental compilation.

Gravity rejects cache reuse that skips changed type, effect, profile, safety, or
proof validation.

Gravity rejects reusing diagnostics whose origin spans or facts changed.

Gravity rejects release artifacts built from speculative cache entries.

Gravity rejects proof reuse without checking invalidation conditions.

## Conformance Criteria

A conforming incremental compiler must demonstrate:

- stage-specific cache keys,
- dependency graph construction,
- reuse for unchanged source and unchanged policy,
- invalidation for source, macro, namespace, type, effect, profile, safety,
  proof, dependency, target, and backend changes,
- stale proof and stale diagnostic rejection,
- build-effect replay in cache keys,
- speculative interactive reuse that cannot publish,
- reproducible release rebuild from recorded inputs.
