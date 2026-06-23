# S8 - Typed Configuration and Environment Specification

Sequence: 152
Phase: 10 - Schema, Data and Interop
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Typed configuration turns environment variables, config files, secrets,
feature flags, deployment settings, build inputs, and runtime provider settings
into validated schemas with explicit read effects and redaction rules.

Configuration is not ambient global state. Reading it is an effect governed by
profile, package, build, runtime, and deployment policy.

## Requirements

- Config declarations must define schema, sources, precedence, defaults,
  required fields, secret fields, validation rules, environment identity,
  artifact inclusion, and redaction policy.
- Reading env, files, network config, secrets, or deployment providers requires
  effects and capabilities.
- Secrets must not appear in logs, artifact manifests, source maps, prompts,
  traces, or diagnostics except as redacted markers allowed by policy.
- Build-time config reads must be captured as reproducibility inputs or rejected
  in hermetic builds.
- Config values decoded from external sources are tainted until validated.
- Runtime config reload must define compatibility, lifecycle, and observability
  behavior.

## Dependencies

- `S1`, `S2`, `SAFE10`, `SAFE11`, `R11`, and `R12` define schemas,
  serialization, capabilities, taint, runtime enforcement, and redaction.
- Package/build/deployment phases define hermeticity and release policy.

## Outputs and Artifacts

- Typed config manifest.
- Config schema.
- Source precedence record.
- Config loader artifact.
- Secret policy record.
- Redaction report.
- Build reproducibility record.
- Runtime reload policy.
- Configuration diagnostics.

## Config Manifest

```clojure
{:artifact :gravity/config-schema
 :schema AppConfig/v1
 :sources [:env :secrets :file]
 :effects #{:build/env :secrets/read :filesystem/read}
 :capabilities #{:env/read :secret/read :fs/read}
 :secret-fields #{:database-url}
 :artifact-policy :redacted}
```

## Source Policy

Sources include environment, config files, secret stores, deployment provider
metadata, command-line flags, build inputs, remote config providers, and test
fixtures. Source precedence must be deterministic and recorded. Secret sources
must define access grant, redaction, rotation, and audit behavior.

## Diagnostics

Configuration diagnostics use `S8` identifiers:

- `S8-SCHEMA` for missing or invalid config schema.
- `S8-SOURCE` for missing source, ambiguous precedence, or unsupported provider.
- `S8-CAPABILITY` for config/environment/secret/file reads without grants.
- `S8-SECRET` for secret leakage or unredacted output.
- `S8-VALIDATION` for malformed or missing required config values.
- `S8-HERMETICITY` for build-time config reads not captured as inputs.
- `S8-RELOAD` for unsafe runtime config reload behavior.
- `S8-MANIFEST` for incomplete config artifacts.

Diagnostics must include config id, field path, source, source span or artifact
edge, effect, capability, secret/redaction state, and remediation.

## Rejected Designs

Gravity rejects ambient config and environment access.

Gravity rejects secret values in artifacts and diagnostics.

Gravity rejects untracked build-time environment reads in hermetic builds.

Gravity rejects config reload without compatibility and lifecycle policy.

Gravity rejects unvalidated external config values.

## Conformance Criteria

A conforming typed config system must demonstrate:

- config schemas and loaders,
- source precedence fixtures,
- capability checks for env/file/secret/provider reads,
- redaction tests,
- hermetic build input records,
- runtime reload policy fixtures,
- rejection of missing validation, secret leakage, and ambient config reads.
