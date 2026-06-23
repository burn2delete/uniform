# PKG6 - Capability and Permission Manifest Specification

Sequence: 170
Phase: 12 - Build, Package and Artifact System
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines package-level capability and permission manifests.
The manifest describes what capabilities a package declares, what it denies,
what it delegates to deployment, which dependencies request authority, and what
runtime handles are required. It is evidence for package review, build policy,
runtime enforcement, and supply-chain audit.

Capabilities are requested by packages, granted by deployments or explicit
policy, and enforced at runtime. Dependency resolution never grants them.

## Manifest Fields

The manifest records:

- package id and version;
- declared effects and capabilities;
- denied capabilities;
- per-profile and per-target capability needs;
- dependency capability summaries;
- tool and agent capability needs;
- unsafe or FFI capability use;
- deployment grant requirements;
- `:ai/human-approval` requirements;
- runtime handle types;
- audit event classes.

The manifest is generated from source analysis and project declarations, then
compared against package and deployment policy.

## Requirements

- Every package artifact MUST include a capability summary.
- Capability summaries MUST be derived from effect analysis and explicit declarations.
- Dependencies MUST NOT grant capabilities to dependents.
- Capability expansion across package updates MUST be reported.
- Runtime effects MUST be backed by scoped capability handles.
- Denied capabilities MUST fail closed even if a dependency requests them.
- AI tools, FFI, shell, secrets, filesystem writes, network access, database writes, and production mutation MUST be visible in the manifest.
- Deployment grants MUST be separate from package requests.
- Permission manifests MUST be included in provenance and SBOM data.
- Capability use MUST be auditable at runtime when the runtime participates.

## Permission Flow

The flow is:

1. Source analysis finds effects.
2. Project file requests capabilities.
3. Package manifest summarizes requested and denied capabilities.
4. Dependency resolver compares dependency requests with policy.
5. Deployment grants scoped handles.
6. Runtime records handle use.

Any missing edge rejects the action or build.

## Semantic Dependencies

- `L6` defines effects.
- `L15` defines capability providers.
- `SAFE10` defines capability security.
- `R11` defines runtime enforcement.
- `PKG1` defines project capability declarations.
- `PKG5` defines dependency compatibility.
- `PKG8` defines safety audit metadata.
- `A4` and `A8` define tool and AI policy capability use.

## Outputs and Artifacts

The compiler and package tool emit:

- capability manifest;
- effect-to-capability derivation report;
- denied-capability table;
- dependency capability diff;
- deployment grant requirements;
- runtime audit event schema;
- SBOM capability fields.

## Example

```clojure
(capability-manifest
  {:package acme/support-agent
   :requests [:network/http :database/read :ai/model-call]
   :denies [:shell/exec :secrets/read :filesystem/write]
   :deployment-grants [:network/http :database/read]
   :approval-required [:ai/model-call]
   :runtime-handles {:database/read DatabaseReadCap
                     :network/http HttpClientCap}})
```

## Rejection Rules

- Reject artifacts missing capability summary.
- Reject runtime effects with no capability derivation.
- Reject dependency updates that expand capabilities without policy approval.
- Reject denied capabilities even when requested transitively.
- Reject ambient authority not represented by a runtime handle.
- Reject deployment manifests that grant capabilities not requested by the package.
- Reject SBOM output missing capability fields.
- Reject capability summaries that differ from effect analysis.

## Diagnostics

- `PKG6001` reports missing capability summary.
- `PKG6002` reports effect without capability derivation.
- `PKG6003` reports capability expansion.
- `PKG6004` reports denied capability request.
- `PKG6005` reports ambient authority.
- `PKG6006` reports invalid deployment grant.
- `PKG6007` reports SBOM capability omission.
- `PKG6008` reports source/manifest capability mismatch.

Diagnostics include package id, dependency chain, effect, capability, profile,
target, grant source, and policy rule.

## Conformance Criteria

- Source effects produce a deterministic capability manifest.
- Dependency capability expansion is visible in update diffs.
- Denied capabilities reject builds even when requested transitively.
- Runtime handle use is logged for participating runtimes.
- Deployment cannot grant capabilities absent from the package request set.
- SBOM records package capabilities.
- The manifest matches effect analysis for representative fixtures.
