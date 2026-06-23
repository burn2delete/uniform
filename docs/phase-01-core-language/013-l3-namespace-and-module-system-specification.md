# L3 - Namespace & Module System Specification

Sequence: 13
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L3 defines how Gravity groups definitions, resolves names, declares profile and target constraints, controls imports/exports, and records module-level effects, capabilities, safety mode, and artifact identity.

Namespaces are compilation contracts. They are not merely naming conveniences.

## Namespace Form

Every normal source file belongs to exactly one namespace declared with `ns`.

```clojure
(ns app.server
  (:profile :native)
  (:target :llvm)
  (:requires [gravity.net.http :as http]
             [app.schema :as schema])
  (:imports [c.libc :as libc])
  (:exports [main start stop])
  (:effects #{:network/listen :io/write})
  (:capabilities #{:network/listener :io/stdout})
  (:safety :safe-optimized))
```

A namespace identity is the tuple:

```clojure
{:name app.server
 :package acme/app
 :profile :native
 :target :llvm
 :source-path "src/app/server.gravity"}
```

The namespace name alone is not enough for artifact identity because the same namespace may be built for different targets or package versions.

## Namespace Clauses

`(:profile p)` declares one active profile for implementation namespaces.

`(:profiles #{p ...})` declares a reusable library namespace with a bounded set of supported profiles. Such namespaces must state per-profile exclusions and may not use the union of all profile powers.

`(:target t)` and `(:targets #{t ...})` declare requested artifact families. A target never legalizes behavior rejected by profile validation.

`(:requires [...])` imports Gravity namespaces.

`(:imports [...])` imports foreign or host symbols through an interop boundary.

`(:exports [...])` names public definitions. Unexported definitions are private to the module unless tooling or governance policy grants inspection.

`(:effects #{...})` declares namespace-level effect allowance. Inferred effects must be a subset or produce a diagnostic.

`(:capabilities #{...})` declares required authority. Package and deployment grants must satisfy this set.

`(:safety mode)` declares default safety mode for definitions in the namespace.

`(:providers [...])` declares provider implementations or selected providers for effects, memory, math, AI, runtime, or capability services.

## Definitions and Visibility

Top-level `def`, `defn`, `defmacro`, `defschema`, `defprotocol`, and facet-specific declarations create namespace entries after macro expansion and analysis.

Each entry records:

- name,
- kind,
- visibility,
- source span,
- type,
- latent effects,
- required capabilities,
- profile restrictions,
- safety mode,
- artifact links,
- deprecation/stability metadata when present.

Public API is the exported set plus any declarations a package marks as public. Private definitions may still appear in debug/provenance artifacts, but cannot be imported as stable APIs.

## Name Resolution

Resolution order for an unqualified symbol in expression position is:

1. local lexical binding,
2. local recur/function parameter binding,
3. current namespace private or public var,
4. core namespace auto-imports allowed by profile,
5. macro or special-form resolution in syntactic positions.

Ambiguous names are rejected. Shadowing is allowed only where the language permits it and diagnostics can still identify the resolved binding.

Alias-qualified and fully qualified symbols bypass the unqualified search and must resolve through namespace aliases, fully qualified namespace names, or imported foreign module aliases. Wildcard imports are rejected for stable modules because they make artifact dependencies unstable.

## Cross-Profile Imports

A namespace may depend on another namespace with the same profile when effects and capabilities remain legal.

Cross-profile imports require one of:

- a pure `:core` API,
- a profile-safe facade,
- a typed schema/artifact boundary,
- an FFI-like boundary with effects, capabilities, ownership, and safety metadata,
- a generated target artifact consumed through package metadata.

Examples:

```clojure
(ns firmware.main
  (:profile :firmware)
  (:requires [shared.checksum :as checksum])) ; allowed if shared.checksum is :core
```

```clojure
(ns kernel.driver
  (:profile :kernel)
  (:requires [web.ui :as ui])) ; rejected: hosted UI import has no artifact boundary
```

## Module Artifacts

Each namespace emits a module artifact before target lowering:

```clojure
{:module app.server
 :package acme/app
 :profile :native
 :target :llvm
 :exports [main start stop]
 :requires [{:module gravity.net.http :profile :native :effects [:network/http]}]
 :effects [:network/listen :io/write]
 :capabilities [:network/listener :io/stdout]
 :safety :safe-optimized
 :source-hash "sha256:..."
 :definitions definitions-hash}
```

Backends consume module artifacts or MIR derived from them. Package tools use module artifacts to compute dependency graphs, capability graphs, documentation, and conformance targets.

## Requirements

- Every implementation namespace must declare exactly one active profile.
- Library namespaces that declare multiple profiles must state profile-specific exclusions or implementation variants.
- Namespace effect inference must not exceed declared effect allowance.
- Namespace capability requirements must be satisfied by package and deployment/build grants.
- Cross-profile imports must use explicit allowed boundaries.
- Import graphs must be acyclic unless a later document defines a checked initialization and recursive module protocol.
- Module artifacts must be content-addressable or otherwise reproducible under package policy.

## Dependencies

L3 depends on `D0`, `D1`, `D3`, `L1`, and `L2`.

It is upstream of macro expansion, type checking, effect checking, profile validation, capability checking, package dependency resolution, compiler module analysis, documentation generation, and language server indexing.

## Outputs and Artifacts

L3 requires:

- namespace table,
- alias table,
- import/export table,
- module dependency graph,
- namespace effect summary,
- namespace capability summary,
- profile boundary records,
- module artifact,
- public API manifest,
- diagnostics for resolution and boundary failures.

## Rejected Behavior

L3 rejects:

- namespace without profile,
- two active profiles in one implementation namespace,
- cross-profile import without boundary,
- undeclared effect widening,
- capability use absent from namespace/package/deployment grants,
- wildcard stable imports,
- private definition imported as public API,
- circular module initialization without explicit protocol,
- target-specific import that claims core portability.

## Diagnostics

- `L3-NS-MISSING`: file has forms that require a namespace but no `ns` declaration.
- `L3-PROFILE-MULTIPLE`: implementation namespace declares more than one active profile.
- `L3-UNKNOWN-ALIAS`: qualified symbol uses unknown alias.
- `L3-AMBIGUOUS-NAME`: unqualified symbol resolves to multiple candidates.
- `L3-PRIVATE-IMPORT`: code imports a private definition.
- `L3-CROSS-PROFILE`: import crosses profiles without accepted boundary.
- `L3-EFFECT-WIDEN`: inferred namespace effects exceed declaration.
- `L3-CAPABILITY-MISSING`: namespace requires an ungranted capability.

## Conformance Criteria

- Namespace fixtures cover single-profile modules, multi-profile libraries, imports, exports, aliases, private definitions, and foreign imports.
- Name resolution fixtures prove lexical bindings shadow namespace vars only where legal.
- Cross-profile fixtures show pure core import accepted and hosted-to-kernel import rejected.
- Module artifact fixtures include profile, target, effects, capabilities, source hash, exports, and dependency graph.
- Package fixtures can build a dependency graph from module artifacts without reading source text.

## Change Control

Changing namespace or module rules affects macro expansion, type checking, effect checking, package resolution, artifact identity, LSP indexing, documentation generation, and bootstrap ordering. Changes require conformance fixture updates and package compatibility review.
