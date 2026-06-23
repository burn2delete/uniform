# C5 - Name Resolution & Namespace Analyzer Design

Sequence: 84
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Name resolution binds syntax-level symbols to locals, vars, macros, types,
protocols, constructors, capabilities, package entries, foreign imports, or
target intrinsics. Namespace analysis builds the module dependency graph and
records profile, target, effects, capabilities, visibility, and initialization
facts for downstream checking.

Resolution occurs after macro expansion and before type/effect checking.

## Requirements

- Every symbol in executable or declarative position must resolve to a binding
  identity or produce a diagnostic.
- Binding identity must include namespace, package, profile, target when
  relevant, definition kind, visibility, version, and source span.
- Lexical bindings must shadow namespace vars only where the language permits.
- Macro, type, protocol, var, local, and special-form namespaces must be
  distinguishable.
- Imports must record profile, target, effect, capability, safety, package, and
  build-effect metadata.
- Cross-profile imports must use an accepted boundary such as pure `:core`,
  facade, schema/artifact boundary, FFI-like boundary, or generated artifact.
- Cycles must be rejected unless a later module-initialization protocol accepts
  the cycle explicitly.
- Resolution artifacts must be stable for incremental invalidation and LSP
  indexing.

## Dependencies

- `L3` defines namespace forms, visibility, import/export, and cross-profile
  rules.
- `C3` defines syntax-object namespace context.
- `C4` produces expanded syntax.
- `L5`, `L6`, and `L15` consume resolved identities for type, effect, and
  capability checking.
- Profile documents define profile compatibility.
- Package documents define package identity, dependency versions, and trust
  policy.

## Outputs and Artifacts

- Namespace analysis artifact.
- Binding table.
- Alias table.
- Import and export table.
- Lexical scope graph.
- Dependency graph.
- Cross-profile edge report.
- Resolution diagnostics.
- Incremental invalidation keys.

## Binding Identity

```clojure
{:binding-id binding-hash
 :name 'http/get
 :kind :var
 :namespace 'gravity.http
 :package {:name 'gravity/http :version "1.2.0"}
 :visibility :public
 :profile-set #{:hosted :native}
 :target-set #{:jvm :llvm :wasm}
 :type-ref type-id
 :effects #{:network/http}
 :capabilities #{:http/client}
 :safety :safe
 :source-span definition-span
 :artifact definition-artifact-id}
```

The binding id is stable over semantic definition fields. Documentation text and
nonsemantic formatting do not affect binding identity.

## Resolution Context

Resolution uses:

- current namespace,
- lexical scope stack,
- macro expansion context,
- alias table,
- required namespace table,
- imported foreign table,
- core auto-import table allowed by profile,
- active profile and target,
- package dependency graph,
- language facets.

The resolver never reaches out to ambient host namespaces. All imports come from
module artifacts, package manifests, or explicit foreign import records.

## Resolution Order

For expression symbols:

1. local lexical binding,
2. loop or function recur binding,
3. current namespace private or public binding,
4. alias-qualified required namespace binding,
5. fully qualified namespace binding,
6. profile-allowed core binding,
7. imported foreign binding through an interop record,
8. target intrinsic only when syntax explicitly requests one.

For macro positions, macro bindings are resolved before expression vars. For
type positions, type and protocol bindings are resolved in the type namespace.

Ambiguous resolution is rejected.

## Namespace Artifact

```clojure
{:artifact :gravity/namespace-analysis
 :namespace 'app.server
 :package 'acme/app
 :profile :native
 :target :llvm
 :aliases {'http 'gravity.http
           'json 'gravity.json}
 :exports ['main 'start]
 :locals scope-graph-hash
 :bindings {syntax-id binding-id}
 :requires [{:namespace 'gravity.http
             :package 'gravity/http
             :edge :direct
             :profile-boundary :compatible
             :effects #{:network/http}
             :capabilities #{:http/client}}]
 :foreign-imports []
 :rejected-edges []
 :diagnostics []}
```

This artifact feeds type checking, effect checking, profile validation, package
graph construction, documentation, LSP indexing, and incremental compilation.

## Cross-Profile Edges

A dependency from one profile to another is accepted only when the callee exposes
one of:

- pure `:core` API,
- profile-specific facade,
- typed schema or artifact boundary,
- FFI-like boundary with ownership, effect, capability, and safety records,
- generated target artifact consumed through package metadata.

The resolver records the boundary type and rejects imports that rely on runtime
availability alone.

## Incremental Invalidation

Resolution invalidates when:

- namespace source changes,
- import/export list changes,
- alias table changes,
- dependency package version changes,
- visibility changes,
- binding kind changes,
- profile or target set changes,
- language facet set changes,
- macro expansion changes symbol positions.

The invalidation record must identify which downstream artifacts require
rechecking.

## Diagnostics

Resolution diagnostics use `C5` identifiers:

- `C5-UNRESOLVED` for symbols with no binding.
- `C5-AMBIGUOUS` for multiple legal bindings.
- `C5-PRIVATE` for private binding access.
- `C5-ALIAS` for unknown or duplicate aliases.
- `C5-SHADOW` for illegal shadowing.
- `C5-CYCLE` for illegal namespace dependency cycles.
- `C5-CROSS-PROFILE` for rejected profile edges.
- `C5-CAPABILITY` for imported binding requiring unavailable capability.
- `C5-TARGET` for target-incompatible imports.
- `C5-FOREIGN` for malformed foreign import records.

Diagnostics must include symbol, syntax id, source span, namespace, profile,
target, candidate bindings when applicable, dependency edge, and remediation.

## Rejected Designs

Gravity rejects name lookup through ambient host globals.

Gravity rejects wildcard imports in stable modules.

Gravity rejects cross-profile imports without explicit boundaries.

Gravity rejects ambiguous unqualified symbols.

Gravity rejects resolver artifacts that omit effect, capability, profile, or
target metadata.

## Conformance Criteria

A conforming resolver must demonstrate:

- local, namespace, alias-qualified, and fully qualified resolution,
- macro-position and type-position resolution,
- binding identity stability,
- private/public visibility diagnostics,
- legal and illegal shadowing,
- dependency graph emission,
- cross-profile boundary acceptance and rejection,
- target and capability compatibility diagnostics,
- incremental invalidation for changed aliases, exports, packages, and profiles.
