# GOV2 - Compatibility Policy

Sequence: 232
Phase: 17 - Governance and Evolution
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The compatibility policy defines what Gravity promises not to break once a contract is stable.
Compatibility covers source code, macro expansion, type behavior, effect behavior, capability requirements, diagnostics, artifact schemas, package manifests, profile availability, runtime behavior, and conformance fixtures.
Gravity cannot treat compatibility as only syntax-level source acceptance because the language is artifact-oriented and profile-aware.

Compatibility is evaluated against a declared baseline release and target/profile matrix.
A change can be compatible for one profile and breaking for another.
A change can preserve source syntax while breaking artifact consumers.
Those distinctions must be explicit.

## Requirements

- Stable contracts MUST have a recorded compatibility surface.
- Compatibility checks MUST name baseline release, affected profiles, affected targets, and affected artifacts.
- Source compatibility MUST include reader behavior, macro expansion, namespace resolution, arity, type signatures, and protocol dispatch.
- Semantic compatibility MUST include value results, errors, effects, capabilities, allocation, resource lifetime, replay behavior, and safety guarantees.
- Diagnostic compatibility MUST preserve diagnostic ids and structured fields unless migration is provided.
- Artifact compatibility MUST preserve schema identity or provide migrations.
- Profile compatibility MUST track additions, removals, narrowing, and delegation changes.
- Package compatibility MUST track manifests, dependency resolution, capability metadata, provenance, and signing.
- Security fixes MAY intentionally break compatibility only under recorded security review and migration policy.
- Experimental contracts are not compatibility baselines unless a package explicitly opts into that risk.

## Compatibility Surfaces

- `:source`: reader, forms, macros, namespaces, vars, arity, names, types, protocols, and metadata.
- `:semantic`: value behavior, error behavior, determinism, totality, partiality, safety, and proof obligations.
- `:effect`: effects added, removed, renamed, widened, or narrowed.
- `:capability`: new authority requirements, changed capability shape, or removed capability paths.
- `:profile`: profile availability and profile-specific restrictions.
- `:diagnostic`: ids, severity, primary span, structured fields, and remediation category.
- `:artifact`: schema identity, hashes, generated metadata, provenance, and compatibility migrations.
- `:runtime`: ABI, calling convention, resource lifetime, replay, provider behavior, and host delegation.
- `:package`: manifest fields, dependency resolution, lockfile meaning, signatures, SBOMs, and registry semantics.

## Dependencies

- `D6`, `D8`, and `D9` for artifact, diagnostic, and provenance stability.
- `L1` through `L15` for source and semantic behavior.
- `SAFE1` through `SAFE16` for safety compatibility.
- `P1` through `P13` for profile compatibility.
- `C1` through `C18`, `B1` through `B14`, and `R1` through `R12` for compiler, backend, and runtime compatibility.
- `PKG1` through `PKG12` for package compatibility.
- `TEST1` through `TEST13` for compatibility conformance.
- `STD20` for standard-library stability.
- `GOV1`, `GOV4`, `GOV7`, and `GOV8` for process, security, experimental, and deprecation rules.

## Compatibility Record

```clojure
{:id "COMPAT-0001"
 :change "STD9/request-timeout-required"
 :baseline "gravity-0.8"
 :profiles #{:hosted :native :distributed}
 :targets #{:jvm :wasm-component :native-elf}
 :surfaces #{:source :effect :diagnostic :artifact}
 :classification :compatible-tightening
 :migration ["add-timeout-policy"]
 :fixtures ["http-timeout-required" "http-timeout-migration"]}
```

The record is emitted with release artifacts.
Package tools can use it to warn, reject, or migrate dependencies.

## Compatibility Classes

- `:compatible`: no stable consumer should observe a break.
- `:additive`: new API, profile, diagnostic field, or artifact field that does not alter existing meaning.
- `:compatible-tightening`: rejects behavior that was already invalid under stable safety or security contracts.
- `:profile-specific-break`: breaking only for named profiles or targets.
- `:artifact-break`: source may still compile but emitted artifacts require migration.
- `:diagnostic-break`: ids or structured diagnostic fields changed.
- `:security-break`: compatibility break accepted to remove a security risk.
- `:stable-break`: breaking stable contract requiring governance approval and migration.
- `:experimental-break`: allowed only for explicitly opted-in experimental contracts.

## Outputs and Artifacts

- Compatibility matrix for every release.
- Baseline comparison reports for source, semantics, effects, capabilities, profiles, diagnostics, artifacts, runtimes, and packages.
- Migration records and automated fix metadata where possible.
- Negative fixtures proving old invalid behavior is rejected.
- Golden artifacts for diagnostic and artifact schema stability.
- Security exception records for compatibility-breaking security fixes.
- Package manager metadata for compatibility warnings and rejection.

## Rejection Rules

- Reject stable changes without baseline comparison.
- Reject artifact schema changes without migration or explicit break classification.
- Reject diagnostic id reuse or silent diagnostic field changes.
- Reject profile support removal without deprecation, migration, or security exception.
- Reject new effects or capabilities on stable APIs unless classified and migrated.
- Reject security-break claims without GOV4 evidence.
- Reject compatibility reports that omit target or profile scope.
- Reject release artifacts that cannot identify which compatibility records apply.

## Diagnostics

- `GOV2001` when a stable change lacks baseline compatibility analysis.
- `GOV2002` when a diagnostic id or structured field changes silently.
- `GOV2003` when an artifact schema changes without migration.
- `GOV2004` when profile availability is narrowed without review.
- `GOV2005` when new effects or capabilities are added without compatibility classification.
- `GOV2006` when a security exception lacks GOV4 review.
- `GOV2007` when package compatibility metadata is missing.
- `GOV2008` when release artifacts omit compatibility records.

## Conformance Criteria

- Compatibility reports are generated for every release candidate.
- Stable fixtures from the baseline compile or fail only under approved migration rules.
- Diagnostic golden tests detect id and structured-field drift.
- Artifact golden tests detect schema and provenance drift.
- Profile matrices show each addition, removal, narrowing, and delegation change.
- Package tooling can consume compatibility metadata.
- Security-breaking changes link to security review and migration guidance.
- Experimental breaks are isolated from stable consumers.
