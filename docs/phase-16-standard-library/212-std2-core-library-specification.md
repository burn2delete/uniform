# STD2 - Core Library Specification

Sequence: 212
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.core` is the portable library layer available to every Gravity program unless a profile explicitly starts from a smaller kernel.
It exposes pure operations over values, functions, metadata, equality, options, results, errors, symbols, keywords, booleans, predicates, and basic control helpers.
It does not own the primitive special forms; those are defined by the core language documents.
It provides the library names and protocols that ordinary code uses after reader, macro expansion, type checking, effect checking, and profile validation.

The core library must stay small enough for hardware, firmware, kernel, compiler, formal, and self-hosting use.
It may define persistent value helpers and abstractions, but it may not smuggle in host services or runtime assumptions.
All behavior must be deterministic except where the caller passes an explicitly effectful function.

## Requirements

- `gravity.core` MUST be available in `:core`, `:meta`, `:hosted`, `:native`, `:firmware`, `:kernel`, `:hardware`, `:gpu`, and `:formal` unless an export states a narrower profile set.
- Core exports MUST be pure unless their type explicitly accepts an effectful function argument.
- Equality, ordering, hash, truth, nil-like absence, option, result, and error values MUST have stable semantics across profiles.
- Core functions MUST NOT read time, randomness, filesystem, network, process state, thread state, model providers, database connections, or host globals.
- Core helpers MUST NOT require dynamic evaluation, reflection, managed allocation, finalizers, or an ambient runtime.
- Functions over finite values MUST specify total, partial, or checked behavior.
- Any partial operation MUST expose a checked alternative that returns a `Result` or emits a deterministic diagnostic at compile time when statically known invalid.
- Metadata operations MUST preserve source spans and syntax metadata when applied to syntax objects.
- Core protocols MUST be explicit enough for compiler specialization and conformance testing.
- Numeric, collection, text, memory, platform, and AI behavior MUST be delegated to their own modules rather than folded into `gravity.core`.

## Module Surface

- Identity and function helpers: `identity`, `const`, `compose`, `partial`, `apply`, `juxt`, `complement`, `always`, and `never`.
- Predicates: `nil?`, `some?`, `true?`, `false?`, `boolean?`, `symbol?`, `keyword?`, `type?`, `fn?`, `seqable?`, and `record?`.
- Boolean helpers: `not`, `and?`, `or?`, `xor?`, `truthy?`, and `falsy?`.
- Equality and comparison: `=`, `not=`, `identical?`, `compare`, `equiv?`, and `hash`.
- Option values: `some`, `none`, `option?`, `map-option`, `flat-map-option`, `or-else`, and `unwrap-or`.
- Result values: `ok`, `error`, `result?`, `map-result`, `flat-map-result`, `recover`, and `unwrap-or-raise`.
- Error values: `ex-info`, `error-code`, `error-data`, `error-cause`, and `with-error-context`.
- Metadata: `meta`, `with-meta`, `vary-meta`, and `merge-meta`.
- Type and var helpers: `type-of`, `var`, `var?`, `deref-var`, and `resolve-var` where the active profile allows vars.
- Simple reducers: `reduce`, `fold`, and `into` as protocol dispatch points, with concrete collection behavior owned by STD3.

## Dependencies

- `D1` for source forms, syntax objects, namespaces, vars, and metadata.
- `L1` through `L7` for primitive values, functions, type checking, effects, and dispatch.
- `L12` and `L15` for macro interaction and syntax metadata preservation.
- `SAFE1` for absence of undefined behavior in safe code.
- `SAFE3`, `SAFE4`, and `SAFE9` for checked initialization, bounds, and numeric safety when core helpers expose partial operations.
- `P2`, `P8`, `P6`, `P7`, `P5`, `P4`, `P3`, `P12`, and `P13` for core, hardware, firmware, kernel, native, hosted, meta, formal, and compatibility legality.
- `STD1` for module manifest and standard-library architecture.

## Example

```clojure
(ns sample.logic
  (:require [gravity.core :as g])
  (:profile :core))

(defn normalize-result [value]
  (-> value
      (g/map-result g/identity)
      (g/recover (fn [err] (g/error [:normalized err])))))
```

This example is pure.
It is legal in `:core` because the function passed to `recover` constructs data and does not perform effects.
If the function read a file, the effect checker would reject the `:core` compilation.

## Profile Availability

- `:core` receives all pure exports.
- `:hardware` receives only exports that do not require allocation, dynamic vars, exception objects, or runtime dispatch.
- `:firmware` and `:kernel` receive metadata and error helpers only when representation is statically bounded.
- `:native` and `:hosted` may use richer error records and host-efficient implementations while preserving observable Gravity semantics.
- `:distributed` may use core values inside workflow state only when the values are serializable by STD10.
- `:ai` may use core values inside prompt, tool, and agent schemas only when taint and structured-output contracts are preserved.
- `:meta` may use metadata and syntax helpers during macro expansion and compiler passes.
- `:formal` requires total variants or explicit proof obligations for partial helpers.

## Outputs and Artifacts

- `gravity.core` module manifest.
- Exported type and effect signatures for every core var.
- Equality, hash, option, result, metadata, and error conformance fixtures.
- Negative fixtures for host effects, hidden allocation in restricted profiles, partial unwrap misuse, and profile-illegal vars.
- Rewrite and specialization facts used by the optimizer.
- Documentation examples compiled under `:core`, `:meta`, `:native`, `:hosted`, and restricted systems profiles.

## Diagnostics

- `STD2001` when a core export performs an undeclared effect.
- `STD2002` when a core API depends on host state.
- `STD2003` when equality or hashing depends on target object identity instead of Gravity value semantics.
- `STD2004` when a partial unwrap is used without proof or checked handling.
- `STD2005` when metadata manipulation drops source span data.
- `STD2006` when a restricted profile receives an export that requires dynamic allocation or runtime dispatch.
- `STD2007` when a core protocol instance conflicts with collection, text, numeric, or schema contracts.

## Conformance Criteria

- Core examples compile without capabilities in `:core`.
- All exported functions have type, effect, allocation, error, profile, and stability metadata.
- Equality and hash fixtures produce the same Gravity-level result across targets.
- Restricted profiles reject dynamic vars, host-dependent errors, and unbounded allocation.
- Optimizer rewrite rules preserve option, result, metadata, equality, and error semantics.
- Formal profile tests can distinguish total APIs from checked partial APIs.
- Self-hosting stages can use `gravity.core` without depending on a completed hosted runtime.
