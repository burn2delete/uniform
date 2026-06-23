# PKG1 - Project File Specification

Sequence: 165
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines the Gravity project file. The project file is a
source artifact consumed before package resolution, source analysis, target
selection, and build execution. It is the authoritative declaration of package
identity, profiles, targets, entrypoints, dependencies, capabilities,
permissions, artifact kinds, safety policy, and reproducibility requirements.

The file prevents build tools from discovering policy implicitly. A Gravity
project must say what it is allowed to build and which authorities it may need
before the compiler or package manager performs work.

## Required Shape

A project file contains:

- package name and semantic version;
- language edition and minimum compiler version;
- source roots and generated-source roots;
- profiles and target matrix references;
- entrypoints by artifact kind;
- dependencies and registry scopes;
- capability requests and explicit denials;
- build, test, doc, and package tasks;
- safety and unsafe-code policy;
- artifact list and signing policy;
- reproducibility and provenance policy.

The file is EDN-like Gravity data, parseable by the reader without running
project code.

## Requirements

- The project file MUST be readable without macro expansion or arbitrary code execution.
- Package name, version, edition, source roots, profiles, targets, dependencies, capabilities, artifacts, and policy MUST be explicit.
- Profiles MUST be validated against Phase 3 profile definitions.
- Targets MUST be valid for the declared profiles.
- Entry points MUST resolve to source vars or artifact declarations.
- Dependencies MUST resolve through declared registries or local paths.
- Capability requests MUST name effects or runtime services and MUST NOT grant themselves.
- Unsafe-code policy MUST state deny, allow-with-audit, or internal-only.
- Release builds MUST require a lockfile.
- Project metadata MUST be included in artifact manifests.

## Semantic Dependencies

- `D1` defines system architecture.
- `P1` through `P13` define profile legality.
- `L6` and `L15` define effects and capabilities.
- `SAFE6` and `SAFE14` define unsafe and supply-chain safety.
- `C1` defines compiler pipeline entry.
- `S9` defines artifact schemas.
- `PKG5`, `PKG6`, and `PKG7` define dependency resolution, capability manifests, and reproducibility.

## Outputs and Artifacts

The package tool emits:

- normalized project manifest;
- project-file canonical hash;
- dependency root set;
- declared profile and target set;
- capability request table;
- unsafe policy summary;
- task graph seeds;
- artifact emission plan;
- release policy inputs.

The compiler includes the normalized manifest hash in every emitted artifact
that depends on project configuration.

## Example

```clojure
(project
  {:name acme/support-agent
   :version "0.3.0"
   :edition "2026.1"
   :source-roots ["src" "workflows"]
   :profiles [:hosted :ai]
   :targets [:jvm-21 :workflow-graph]
   :entrypoints {:service support.main/serve
                 :workflow support.workflow/triage}
   :dependencies {gravity/core "1.0.0"
                  gravity/ai "1.2.0"}
   :capabilities {:request [:network/http :database/read :ai/model-call]
                  :deny [:shell/exec :secrets/read]}
   :artifacts [:library :agent-manifest :workflow-graph :docs]
   :policy {:unsafe :deny
            :release {:lockfile true :sign true :sbom true}}})
```

## Rejection Rules

- Reject project files requiring code execution to read configuration.
- Reject unknown profiles or profile/target pairs.
- Reject entrypoints that cannot resolve after namespace analysis.
- Reject release builds without a complete lockfile.
- Reject dependencies outside declared registries or local path grants.
- Reject capability expansion introduced only by dependencies.
- Reject generated-source roots that are not marked generated and traceable.
- Reject unsafe policy omission in packages containing unsafe forms.

## Diagnostics

- `PKG1001` reports unreadable or effectful project configuration.
- `PKG1002` reports unknown profile or target.
- `PKG1003` reports unresolved entrypoint.
- `PKG1004` reports dependency source outside registry policy.
- `PKG1005` reports undeclared capability request.
- `PKG1006` reports missing release lockfile.
- `PKG1007` reports unsafe policy mismatch.
- `PKG1008` reports artifact plan missing a required kind.

Diagnostics include manifest path, key path, package id, profile, target,
dependency id, and remediation.

## Conformance Criteria

- A minimal legal project file normalizes to a deterministic manifest hash.
- A project file with an unknown profile is rejected.
- A release build with no lockfile is rejected.
- A dependency adding a new capability triggers a capability diff.
- A generated-source root without provenance is rejected.
- Artifact manifests include the project manifest hash.
- The project file can be parsed in offline mode without executing project code.
