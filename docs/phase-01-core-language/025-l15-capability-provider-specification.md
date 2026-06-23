# L15 - Capability Provider Specification

Sequence: 25
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity makes authority explicit. Code that reads files, opens sockets, calls
models, invokes tools, allocates from special memory, touches hardware, queries a
package registry, performs compiler IR rewrites, or delegates elementary math to
an implementation does so through a capability provider.

A capability provider is a named implementation of an authority-bearing service
with a typed contract, profile support, effect declarations, safety obligations,
artifact schema, conformance suite, and selection record. Providers let Gravity
replace built-ins without weakening the language's safety and audit model.

## Requirements

- Authority-bearing operations must require explicit capabilities or declared
  ambient providers.
- Providers must declare implemented capabilities, effects, profiles, targets,
  scopes, contracts, failures, trust level, and conformance suites.
- Grants must identify principal, capability, provider, scope, phase, lifetime,
  and audit policy.
- Build-time and runtime authority must remain separate.
- Provider selection must be deterministic and recorded in artifacts.

## Dependencies

- `L5` defines capability value types and provider contract interfaces.
- `L6` defines effects that capabilities authorize.
- `L9` defines capability-denial error behavior.
- `L10` and `L11` define memory and concurrency services that providers may implement.
- `L12` defines build effects and compile-time provider use.
- `L13` defines standard library APIs that consume providers.
- `L14` defines facet services that may require providers.
- Phase 12 build and package documents define lockfile and policy integration.
- Runtime and platform phases define provider initialization per target.

## Outputs and Artifacts

- Provider declaration records.
- Grant records.
- Provider selection records.
- Capability scope audit logs.
- Compile-time replay records for build providers.
- Runtime provider manifests.
- Provider conformance results.

## Authority Model

Gravity separates three things:

- An effect describes what kind of action occurs.
- A capability describes what authority is needed to perform that action.
- A provider implements the authority under a declared contract.

For example, reading a file has effect `:filesystem/read`, requires capability
`:fs/read`, and may be implemented by a POSIX provider, an in-memory test
provider, a browser sandbox provider, a package-archive provider, or a hermetic
build-input provider.

Effects are checked by the language. Capabilities are checked by the compiler,
runtime, build system, package policy, and profile. Providers are selected and
recorded as artifact inputs.

## Terms

- A capability is a typed authority such as `:fs/read`, `:http/client`,
  `:time/read`, `:model/call`, `:tool/invoke`, `:memory/arena`, or
  `:compiler/ir-transform`.
- A provider is a concrete implementation of one or more capabilities.
- A grant is an authorization that lets a package, namespace, function, build
  step, or runtime principal use a capability through a provider.
- A capability value is a first-class value passed to code when the authority is
  explicit in the API.
- An ambient provider is a provider installed in an execution context. Ambient
  providers are allowed only when the profile and package policy make the
  ambient authority explicit.
- Attenuation is deriving a narrower capability from a broader one.
- Revocation is invalidating a capability value or grant.
- A provider record is the artifact entry that identifies selected providers,
  versions, policies, and conformance evidence.

## Provider Declaration

Providers are declared as data:

```clojure
(defprovider gravity.fs/posix
  {:kind :filesystem
   :implements #{:fs/read :fs/write :fs/stat}
   :profiles #{:hosted :native}
   :runtime-effects #{:filesystem/read :filesystem/write}
   :build-effects #{}
   :contracts [gravity.contracts/FileSystem]
   :path-policy :grant-scoped
   :artifact-schema :gravity.provider/fs-v1
   :conformance :gravity.conformance/fs})
```

A declaration must state:

- Provider id and version.
- Implemented capabilities.
- Supported profiles and targets.
- Runtime effects and build effects.
- Required lower-level capabilities.
- Contract interfaces.
- Failure types.
- Blocking and concurrency behavior.
- Resource and memory behavior.
- Determinism and replay behavior.
- Artifact schema.
- Conformance suite.
- Trust level and signature policy when relevant.

Provider declarations are part of package metadata. A compiler may reject a
provider before typechecking code that depends on it if its declaration is
incompatible with the active profile or policy.

## Grant Declaration

Grants authorize use:

```clojure
{:grant/id :app.read-config
 :principal :package/app
 :capability :fs/read
 :provider gravity.fs/posix
 :scope {:paths ["config/*.edn"]}
 :phase :build
 :expires :build-end
 :audit :required}
```

The grant must identify principal, capability, provider, scope, phase, lifetime,
and audit policy. Runtime grants and build grants are distinct. A build grant
that reads a schema file does not authorize the final program to read that file
at runtime.

Grant scopes are capability-specific:

- Filesystem scopes are paths, content ids, package resources, or virtual roots.
- Network scopes are hosts, ports, protocols, methods, and replay policies.
- Environment scopes are key sets and redaction policies.
- Process scopes are command ids, arguments, working directories, and output
  policies.
- Model scopes are provider, model id, input class, output class, cost limit,
  retention policy, and evaluation policy.
- Tool scopes are tool id, schema, allowed operations, and side-effect policy.
- Memory scopes are region, arena, lifetime, alignment, and aliasing policy.
- Hardware scopes are address ranges, registers, devices, interrupts, and memory
  ordering policy.
- Compiler scopes are IR level, pass id, namespace set, and allowed transforms.

## First-Class Capabilities

Gravity prefers explicit capability values for APIs that need authority:

```clojure
(defn load-config
  [fs :- (Capability :fs/read)
   path :- Path]
  :- (Result Config ConfigError)
  (:effects #{:filesystem/read})
  (:capabilities #{:fs/read})
  (parse-config (fs/read-text fs path)))
```

Capability values may be passed, stored, attenuated, and revoked according to
their type. A capability type includes its capability id, provider contract,
scope, phase, lifetime, and thread-safety marker.

Implicit ambient use is limited to declarations that make the ambient provider
visible:

```clojure
(ns app.main
  (:profile :hosted)
  (:effects #{:filesystem/read})
  (:capabilities #{:fs/read}))
```

The compiler must reject ambient access when the namespace, package, or profile
does not declare it.

## Provider Categories

The standard provider categories are:

- Filesystem providers.
- Environment providers.
- Clock and randomness providers.
- Network and HTTP providers.
- Process and shell providers.
- Database and query providers.
- Workflow runtime providers.
- Model and inference providers.
- Tool invocation providers.
- Prompt and memory providers.
- Package registry providers.
- Build cache providers.
- Compiler IR providers.
- Memory allocation providers.
- Garbage collector providers.
- Region and arena providers.
- FFI and ABI providers.
- Hardware device providers.
- GPU and accelerator providers.
- Elementary math providers.
- Cryptography providers.
- Logging and telemetry providers.

Each category has a contract document in later phases or standard library
specifications. L15 defines the common provider machinery.

## Replaceable Built-Ins

Gravity allows provider replacement for services that many languages hard-code.
Examples:

```clojure
(defprovider gravity.math.elementary/default
  {:kind :elementary-function-system
   :implements #{:math/sin :math/cos :math/exp :math/log}
   :profiles #{:core :hosted :native :gpu}
   :semantic-ir :efir
   :normal-forms #{:standard :eml}
   :proof-engine :interval
   :approx-engine :remez
   :backends #{:llvm :c :wasm :jvm :js :gpu}})

(defprovider custom.memory/arena
  {:kind :memory-system
   :implements #{:memory/arena :memory/region}
   :profiles #{:native :firmware}
   :contracts [gravity.contracts/MemorySafety
               gravity.contracts/RegionSafety]
   :proof-artifacts ["arena-safety.gproof"]})
```

A replacement provider may be faster, narrower, more specialized, or more
auditable. It may not weaken the safe-code guarantees promised by the active
profile. If it cannot satisfy the profile, selection fails.

## Selection

Provider selection occurs in this order:

1. Explicit source annotation.
2. Package manifest selection.
3. Workspace policy selection.
4. Profile default.
5. Compiler default for the active target.

Selection must be deterministic. If multiple providers satisfy the same
requirement and no ordering rule chooses one, the compiler must emit an
ambiguity diagnostic.

The selected provider becomes part of:

- Typed core metadata.
- Compile-time evaluation records.
- Runtime manifests.
- Package lockfiles.
- Safety audit artifacts.
- Reproducibility cache keys.

## Compile-Time Providers

Compile-time providers implement build effects from `L12`. They must support
replay or explicitly mark themselves non-replayable. Release, conformance, and
safety-critical builds reject non-replayable providers unless a policy exception
is recorded.

Compile-time provider examples:

- A hermetic file input provider.
- A package registry provider backed by a lockfile.
- A solver provider for proofs.
- A code generator provider.
- A model provider for generated test cases.
- A compiler IR transform provider.

Compile-time provider outputs must be content-addressed or recorded with enough
metadata to replay or audit the result. Secrets must be redacted from public
artifacts.

## Runtime Providers

Runtime providers are initialized by the runtime, host, operating system,
deployment platform, workflow engine, kernel, firmware image, or hardware
integration layer. Runtime initialization must check that:

- Required providers exist.
- Provider versions satisfy the artifact record.
- Provider scopes satisfy grants.
- Provider contracts match the compiled program.
- Profiles and targets are compatible.
- Safety and audit requirements are enabled.

If a required provider is missing, the runtime must fail before executing user
code unless the program has a declared fallback provider.

## Attenuation and Revocation

Capability values may be attenuated:

```clojure
(def readonly-public
  (fs/restrict root-fs {:paths ["public/**"] :mode :read}))
```

An attenuated capability cannot exceed the authority of its parent. The compiler
and runtime must preserve the narrower scope in type metadata and audit logs.

Revocation is profile-specific. Hosted and distributed profiles may support
revocable capability handles. Firmware, hardware, or kernel profiles may require
static lifetimes instead. A function that depends on revocation must declare
that requirement.

## Safety and Trust

Provider trust is explicit:

- `:trusted-core` for providers distributed with the verified compiler/runtime.
- `:trusted-package` for signed providers accepted by package policy.
- `:local-dev` for interactive development providers.
- `:sandboxed` for providers mediated by a sandbox.
- `:untrusted` for providers that may be called only through explicit isolation.

Trust does not eliminate checks. A trusted provider still declares effects,
capabilities, failure modes, and artifacts. An untrusted provider may be used
only when the profile has an isolation mechanism and the grant allows it.

## Diagnostics

Capability diagnostics use `L15` identifiers:

- `L15-CAPABILITY-MISSING` when code requires authority that is not granted.
- `L15-PROVIDER-MISSING` when no provider implements a required capability.
- `L15-PROVIDER-AMBIGUOUS` when selection has multiple valid providers.
- `L15-PROFILE` when a provider is unsupported by the active profile or target.
- `L15-SCOPE` when a requested operation exceeds the grant scope.
- `L15-PHASE` when build authority is used at runtime or runtime authority is
  used during compilation.
- `L15-TRUST` when provider trust level violates policy.
- `L15-REPLAY` when a compile-time provider cannot satisfy replay requirements.
- `L15-SECRET` when provider output would leak secret material.
- `L15-CONTRACT` when a provider declaration fails its contract suite.
- `L15-REVOCATION` when code assumes revocation in a profile that cannot provide
  it.

Diagnostics must include requested capability, selected or missing provider,
grant id, scope, phase, active profile, target, source span or manifest entry,
and the nearest valid provider or grant when one exists.

## Rejected Designs

Gravity rejects ambient global authority as the default. Convenience contexts may
exist, but they must be declared and reflected in artifacts.

Gravity rejects provider replacement that changes safe semantics while claiming
the same contract.

Gravity rejects build-time and runtime authority collapse. A package that may
read a schema during compilation is not automatically allowed to read files when
the program runs.

Gravity rejects provider selection that is not recorded. Reproducible builds and
audits require provider identity and version.

Gravity rejects trust as a substitute for specification. Even trusted providers
must declare contracts, effects, scopes, failures, and conformance evidence.

## Conformance Criteria

A conforming implementation must demonstrate:

- Explicit capability values passed to pure APIs.
- Ambient provider rejection without namespace or package declaration.
- Provider selection by source annotation, manifest, policy, profile default,
  and compiler default.
- Ambiguity rejection when multiple providers match.
- Scope checks for filesystem, network, environment, model, tool, memory, and
  compiler providers.
- Separate build and runtime grants.
- Replay checks for compile-time providers.
- Artifact records containing provider ids, versions, scopes, trust levels, and
  conformance results.
- Safe provider replacement for at least one math provider and one memory or IO
  provider.
- Negative tests for secret leaks, unsupported profiles, untrusted providers,
  and contract failures.
