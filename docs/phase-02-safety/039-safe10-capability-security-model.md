# SAFE10 - Capability Security Model

Sequence: 39
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Capability security prevents Gravity programs from relying on ambient authority.
Code can read files, open sockets, call models, invoke tools, execute processes,
read secrets, touch raw memory, query databases, access hardware, or mutate
compiler IR only when it holds an explicit capability or operates in a declared
ambient context accepted by policy.

This document defines the safety model built on `L15`: least authority, grant
intersection, provider selection, scope checks, attenuation, revocation, secret
handling, compile-time authority, runtime authority, and diagnostics.

## Requirements

- Authority-bearing operations must require a capability, grant, and provider.
- Effective authority is the intersection of source declaration, package
  manifest, workspace policy, profile policy, deployment policy, and runtime
  provider scope.
- Capability use must be visible in types, effects, manifests, or artifacts.
- Ambient authority must be declared and rejected when profile or policy forbids
  it.
- Capabilities must support scoped attenuation where the provider category
  supports it.
- Secrets must not leak into public diagnostics, generated code, cache keys, or
  artifact records.
- Runtime capability failures must have declared error or panic behavior.

## Dependencies

- `L6` defines effects that require authority.
- `L12` defines compile-time build capabilities.
- `L13` defines standard APIs that consume capabilities.
- `L15` defines provider and grant mechanics.
- `L19` defines interop boundaries requiring capabilities.
- `SAFE1` defines safety outcomes.
- `SAFE6` defines unsafe authority escapes.
- `SAFE11`, `SAFE13`, and `SAFE14` refine input, AI tool, and supply-chain
  authority.

## Outputs and Artifacts

- Capability requirement records.
- Grant intersection records.
- Provider selection records.
- Scope check records.
- Attenuation and revocation records.
- Secret redaction records.
- Runtime capability check records.
- Capability denial diagnostics.
- Capability usage summary for packages and deployments.

## Capability Families

Core capability families include:

- Filesystem: `:fs/read`, `:fs/write`, `:fs/stat`, `:fs/watch`.
- Environment and secrets: `:env/read`, `:secret/read`.
- Network: `:net/client`, `:net/server`, `:http/client`, `:http/server`.
- Process and shell: `:process/spawn`, `:shell/exec`.
- Database and query: `:db/query`, `:db/migrate`, `:query/remote`.
- Model and AI: `:model/call`, `:embedding/call`, `:tool/invoke`.
- Memory and unsafe: `:memory/raw`, `:memory/arena`, `:ffi/c`.
- Hardware: `:hardware/mmio`, `:interrupt/register`,
  `:hardware/device-control`.
- Compiler and build: `:compiler/ir-transform`, `:build/read-file`,
  `:build/write-artifact`, `:build/env`, `:build/network`, `:build/exec`,
  `:build/time`, `:build/random`, `:build/model-call`, `:build/tool-call`,
  `:build/target-probe`, `:build/package-index`.
- Deployment: `:deploy/publish`, `:config/write`, `:credential/mint`.

Packages may define narrower capabilities, but they must map to provider
contracts and policy scopes.

## Grant Intersection

The effective grant for an operation is computed from:

1. Source or namespace requirement.
2. Function type and effect declaration.
3. Package manifest.
4. Dependency policy.
5. Workspace policy.
6. Profile policy.
7. Build or deployment invocation.
8. Runtime provider scope.

If any layer denies or narrows authority, the operation is denied or narrowed.
No layer can expand authority beyond its parent policy.

## Explicit Capability Values

APIs should accept capability values when authority is part of their contract:

```clojure
(defn read-config
  [fs :- (Capability :fs/read)
   path :- Path]
  :- (Result Config FsError)
  (:effects #{:filesystem/read})
  (:capabilities #{:fs/read})
  (parse-config (fs/read-text fs path)))
```

The capability type records provider, scope, phase, lifetime, thread-safety, and
revocation behavior. Passing the value documents authority transfer through the
program.

## Ambient Contexts

Ambient providers are allowed only when declared:

```clojure
(ns app.main
  (:profile :hosted)
  (:effects #{:filesystem/read})
  (:capabilities #{:fs/read}))
```

The compiler must reject code that calls ambient authority without namespace,
package, or deployment declaration. Constrained profiles may forbid ambient
authority entirely.

## Scope Checks

Each capability has a scope model:

- Filesystem scopes paths, virtual roots, content ids, and mode.
- Network scopes hosts, ports, protocols, methods, and replay policy.
- Environment scopes key names and redaction policy.
- Secrets scope secret names, read policy, and export policy.
- Models scope provider, model id, input class, output class, cost, and
  retention.
- Tools scope tool id, schema, allowed operation, and side-effect class.
- Hardware scopes address ranges, register widths, and ordering.
- Compiler scopes IR level, pass id, and namespace set.

Operations outside scope are rejected or fail with declared runtime errors.

## Attenuation

Capabilities may be narrowed:

```clojure
(let [public-fs (cap/restrict fs {:paths ["public/**"] :mode :read})]
  (serve-static public-fs))
```

Attenuation cannot add authority. The artifact record must show parent
capability, derived capability, narrowed scope, and lifetime.

## Revocation

Revocation support is provider-specific. If a capability can be revoked, the
type and provider declaration state:

- Who may revoke it.
- What operations fail after revocation.
- Failure type.
- Thread-safety behavior.
- Whether revocation is synchronous.

Profiles without revocation support reject APIs that require revocable
capabilities unless a static lifetime substitute is provided.

## Secrets

Secret-bearing capabilities have additional rules:

- Secret values must not appear in public diagnostics.
- Secret values must not be embedded in generated code.
- Secret values must not be part of stable public cache keys.
- Artifact records may record secret names and redaction policy.
- Secret export requires explicit capability and policy approval.

A compile-time secret read is a build effect and must be separated from runtime
secret authority.

## Runtime Checks

Some capabilities are deployment-specific and cannot be fully checked at compile
time. Runtime checks must:

- Identify the requested capability.
- Check provider presence and scope.
- Fail with declared error or panic behavior.
- Emit telemetry or audit records when policy requires it.
- Preserve secret redaction.

Runtime authority failure is not undefined behavior.

## Diagnostics

SAFE10 diagnostics use these identifiers:

- `SAFE10-MISSING` when no capability is available.
- `SAFE10-DENIED` when policy denies authority.
- `SAFE10-SCOPE` when an operation exceeds grant scope.
- `SAFE10-PROVIDER` when no provider satisfies the capability.
- `SAFE10-AMBIENT` when ambient authority is used without declaration.
- `SAFE10-PHASE` when build authority is used at runtime or runtime authority is
  used during compilation.
- `SAFE10-SECRET-LEAK` when a secret would enter diagnostics, generated code, or
  public artifacts.
- `SAFE10-ATTENUATION` when a derived capability expands authority.
- `SAFE10-REVOCATION` when revocation assumptions are unsupported.
- `SAFE10-RUNTIME` when runtime capability failure lacks declared behavior.

Diagnostics must include requested capability, provider id, grant id, denied
policy layer, source span, active profile, phase, and scope.

## Rejected Designs

Gravity rejects ambient authority by default.

Gravity rejects capability checks that only exist in documentation.

Gravity rejects provider selection that bypasses package or deployment policy.

Gravity rejects secret values in public artifacts.

Gravity rejects build-time authority silently becoming runtime authority.

Gravity rejects attenuation that can expand authority.

## Conformance Criteria

A conforming implementation must demonstrate:

- Capability checks for filesystem, network, environment, secret, process,
  model, tool, memory, FFI, compiler, and hardware families.
- Grant intersection across source, package, workspace, profile, deployment, and
  runtime provider layers.
- Rejection of undeclared ambient authority.
- Scope denial for out-of-scope operations.
- Attenuation records that never expand authority.
- Secret redaction in diagnostics and artifacts.
- Separation of build and runtime capabilities.
- Runtime capability failure with declared behavior.
