# C17 - Compiler Plugin and Pass API Specification

Sequence: 96
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Compiler plugins extend the Gravity compiler with passes, facets, domain IRs,
analysis providers, diagnostics, code generators, proof providers, and target
helpers. They are `:meta` programs with explicit authority, pass contracts,
versioned APIs, verifier obligations, and artifacts.

Plugins cannot mutate hidden compiler internals. They consume declared compiler
artifacts and return transformed artifacts, diagnostics, proof requests,
certificates, or registration records.

## Requirements

- Every plugin must declare manifest, API version, package identity, trust level,
  supported compiler versions, build effects, compiler capabilities, provided
  passes, emitted artifacts, and conformance fixtures.
- Every plugin pass must declare the same pass contract fields as built-in
  passes.
- Untrusted plugins must run with sandboxed compiler capabilities and build
  effects.
- Plugins must not introduce unsafe behavior, capabilities, effects, domain IR,
  target metadata, or generated code without ordinary Gravity artifacts and
  checks.
- Plugin output must pass the verifier for its declared output artifact.
- Plugin cache keys must include plugin package, version, manifest, grants,
  dependencies, and replay records.
- API compatibility must be checked before loading plugin code.

## Dependencies

- `L14` defines language facets.
- `L15` defines capability providers and compiler capability grants.
- `L12` defines build effects and replay.
- `C1`, `C13`, and `C16` define pass contracts, optimization records, and cache
  keys.
- `C12` defines domain IR registration.
- `C15` defines diagnostics.
- Package and governance documents define package trust and plugin signing.

## Outputs and Artifacts

- Plugin manifest.
- API compatibility report.
- Sandbox or trust grant.
- Pass registration records.
- Domain IR and facet registration records when provided.
- Plugin execution trace.
- Plugin output artifacts.
- Plugin diagnostics.
- Plugin conformance results.

## Plugin Manifest

```clojure
{:artifact :gravity/compiler-plugin
 :plugin 'gravity.plugins.loop-fuser
 :package {:name 'gravity/loop-fuser :version "0.1.0"}
 :api-version "1"
 :compiler-compatibility {:min "0.1.0" :max-exclusive "0.2.0"}
 :trust :sandboxed
 :profile :meta
 :build-effects #{}
 :capabilities #{:compiler/ir-transform}
 :capability-scopes {:compiler/ir-transform #{:read-mir :write-mir}}
 :passes [:fuse-adjacent-loops]
 :domains []
 :facets []
 :emits #{:optimization-decision-log}
 :conformance [:loop-fuser-fixtures]}
```

The manifest is loaded before plugin code. Loading is rejected when policy,
version, trust, or capability requirements fail.

## Compiler Capabilities

The `:compiler/ir-transform` capability is scoped. Standard scopes include:

- read syntax,
- write syntax,
- read typed core,
- write typed core,
- read MIR,
- write MIR,
- read domain IR,
- write domain IR,
- register pass,
- register facet,
- register domain IR,
- request proof,
- provide proof,
- emit diagnostics,
- emit artifacts,
- query compiler configuration.

Capabilities are scoped to artifact kinds, namespaces, pass phases, and package
policy. A plugin granted only the `:read-mir` scope cannot rewrite MIR unless it
also has the `:write-mir` scope under `:compiler/ir-transform`.

## Pass API

```clojure
(defpass fuse-adjacent-loops
  {:input :gravity/mir
   :output :gravity/mir
   :requires #{:dominators :effect-graph}
   :preserves #{:types :source-origins}
   :invalidates #{:dominators :loop-analysis}
   :regenerates #{:effect-ordering}
   :proof-obligations #{:effect-order-preserved}
   :emits #{:optimization-decision :verifier-report}}
  [context module]
  ...)
```

The context exposes only granted capabilities and read-only compiler facts unless
write authority is explicit.

## Sandbox and Trust

Trust levels:

- `:built-in`: shipped with the compiler distribution.
- `:trusted-package`: signed package accepted by policy.
- `:sandboxed`: restricted capabilities and effects.
- `:audit-required`: allowed only with human or governance approval.
- `:rejected`: policy denies loading.

Sandboxing restricts filesystem, network, process, environment, compiler IR,
model/tool calls, and artifact writes. A sandboxed plugin can still be useful
when it transforms provided artifacts deterministically.

## Domain and Facet Extensions

Plugins may register facets and domain IRs only through `L14` and `C12`
registration artifacts. The plugin must provide:

- schema,
- verifier,
- supported profiles,
- effects and capabilities,
- lowering paths,
- diagnostics,
- conformance fixtures.

Opaque payloads without verifiers are rejected.

## Execution Trace

```clojure
{:artifact :gravity/plugin-execution
 :plugin 'gravity.plugins.loop-fuser
 :pass :fuse-adjacent-loops
 :input input-artifact-id
 :output output-artifact-id
 :grants grant-hash
 :build-effects []
 :decisions [decision-id]
 :diagnostics []
 :verifier-result :passed}
```

The execution trace is part of incremental cache keys and self-hosting
comparison.

## Diagnostics

Plugin diagnostics use `C17` identifiers:

- `C17-MANIFEST` for malformed plugin manifests.
- `C17-API` for incompatible compiler API versions.
- `C17-CAPABILITY` for missing or excessive compiler capabilities.
- `C17-BUILD-EFFECT` for ungranted build effects.
- `C17-SANDBOX` for sandbox violations.
- `C17-PASS-CONTRACT` for invalid pass contracts.
- `C17-OUTPUT` for plugin output failing verification.
- `C17-DOMAIN` for invalid domain IR registration.
- `C17-FACET` for invalid facet registration.
- `C17-TRUST` for package trust or signature rejection.

Diagnostics must include plugin id, package id, version, pass id, manifest span
or entry, requested capability, trust level, compiler API version, source or
artifact id, and remediation.

## Rejected Designs

Gravity rejects plugins that mutate hidden compiler state.

Gravity rejects unversioned compiler plugin APIs.

Gravity rejects plugin authority inferred from package identity alone.

Gravity rejects plugin output that bypasses normal verifiers.

Gravity rejects opaque domain IR or facet payloads.

## Conformance Criteria

A conforming plugin system must demonstrate:

- manifest loading and rejection cases,
- API compatibility checks,
- sandboxed and trusted plugin execution,
- compiler capability scoping,
- pass contract validation,
- plugin output verifier failure,
- domain IR and facet registration acceptance and rejection,
- build-effect denial in hermetic mode,
- plugin execution trace and cache-key integration,
- diagnostics for trust, capability, API, and output violations.
