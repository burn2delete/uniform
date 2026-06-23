# B6 - JavaScript / TypeScript Backend Design

Sequence: 103
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The JavaScript/TypeScript backend emits JavaScript modules, TypeScript
declarations, source maps, package metadata, UI/runtime bindings, and capability
manifests for browser, Node.js, edge, serverless, desktop-shell, and React
Native-style hosted environments.

JavaScript is a dynamic host target for Gravity. `undefined`, `null`, prototype
mutation, dynamic import, eval, package side effects, event loops, timers,
promises, host globals, and `Number` behavior must be made explicit through
types, effects, capabilities, schemas, runtime manifests, and diagnostics.

## Requirements

- Input must be verified MIR or verified domain IR accepted by `B1` and `C14`.
- The backend must declare runtime kind, ECMAScript target, module format,
  bundler policy, package boundary, tree-shaking assumptions, and source-map
  strategy.
- Generated TypeScript declarations must represent the exported Gravity API and
  identify opaque, unsafe, host, async, and capability-bearing boundaries.
- Host globals such as `window`, `document`, `globalThis`, `process`, storage,
  timers, crypto, network, filesystem, and environment variables require
  effects and capabilities.
- `undefined` and `null` must not enter safe Gravity except through `Option`,
  `Result`, checked adapters, or opaque host values.
- JavaScript exceptions, rejected promises, and event callback failures must be
  translated into Gravity error, panic, or effect channels.
- Numeric lowering must choose `Number`, `BigInt`, typed arrays, checked helper
  calls, or schema validation according to Gravity numeric mode.
- Dynamic `eval`, Function constructors, prototype mutation, untyped package
  imports, hidden global state, and lossy object layout conversions are rejected
  unless isolated behind unsafe or audited host wrappers.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C11` and `C14` define MIR/domain input and target lowering manifests.
- `P4`, `P9`, `P10`, and `P13` define hosted, distributed, AI, and
  compatibility constraints.
- `SAFE9`, `SAFE10`, `SAFE11`, and `SAFE12` define numeric safety,
  capabilities, taint, and generated-code safety.
- Phase 10 schema documents define exported API and host value boundaries.
- Runtime, package, and tooling documents define JS runtime providers, package
  artifacts, source maps, and developer loops.

## Outputs and Artifacts

- JavaScript/TypeScript backend manifest.
- Runtime and module target record.
- JavaScript module or bundle artifacts.
- TypeScript declaration files.
- Source maps and generated-origin maps.
- Capability manifest for host globals and imports.
- Package dependency manifest.
- Async effect boundary map.
- Null/undefined and exception translation map.
- Numeric representation manifest.
- UI or component binding metadata when selected.
- JavaScript/TypeScript backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/js-ts-backend-manifest
 :backend :gravity.backend/js-ts
 :target {:runtime :browser
          :ecmascript :es2022
          :module-format :esm}
 :emits #{:javascript :typescript-declarations :source-map :package-manifest}
 :requires #{:runtime-provider-manifest :capability-manifest
             :async-effect-map :numeric-representation-map}
 :rejects #{:ambient-global-access :dynamic-eval :unchecked-nullish-flow
            :untyped-package-import :lossy-number-lowering}}
```

The manifest records all assumptions needed by bundlers, package managers,
browser tooling, edge deployers, conformance tests, and source-level diagnostics.

## Runtime and Module Target

The target record includes:

- browser, Node.js, edge, serverless, desktop shell, or mobile JS runtime,
- ECMAScript version,
- ESM, CommonJS, UMD, or runtime-specific module format,
- bundler and minifier assumptions,
- side-effect declarations,
- source-map format,
- JSX/UI transform when used,
- worker, thread, and event-loop model,
- package manager and dependency lock metadata,
- supported built-ins and polyfills.

Runtime-specific built-ins are unavailable unless declared by provider and
accepted by profile policy.

## Value and Type Representation

Representation records include:

- Gravity type,
- JavaScript representation,
- TypeScript surface type,
- nullish policy,
- numeric representation,
- object layout and prototype policy,
- mutability and freezing policy,
- equality and hashing semantics,
- serialization schema when exported,
- taint policy for host-provided values.

TypeScript declarations are API evidence, not the compiler's only safety
mechanism. Runtime validation is required for untrusted host, package, network,
storage, DOM, tool, or model inputs.

## Async, Events, and Effects

Async effect records include:

- promise creation and awaiting,
- timers,
- DOM or UI events,
- network and fetch,
- filesystem and process APIs,
- storage APIs,
- worker messages,
- model/tool calls,
- cancellation behavior,
- scheduler or event-loop assumptions,
- replay and nondeterminism policy.

Callbacks generated from Gravity code must preserve source maps and error
translation. Rejected promises cannot disappear into host logging; they must map
to Gravity error, panic, or effect handling.

## Host Globals and Package Imports

The capability manifest records every host global and package import:

- symbol or package name,
- version or integrity constraint,
- side-effect policy,
- effects and capabilities,
- typed wrapper or schema,
- trust and taint level,
- browser/Node/edge availability,
- tree-shaking and bundler assumptions.

Untyped imports are allowed only as opaque foreign values behind audited
wrappers. Dynamic import is effectful and may be denied by package or deployment
policy.

## Numeric Lowering

Numeric lowering selects:

- `Number` for modes that fit JavaScript double behavior,
- `BigInt` for exact integer modes requiring it,
- typed arrays for packed numeric data,
- checked helper calls for overflow, narrowing, shifts, or bounds,
- string or structured representations for schema-stable large values,
- target intrinsics only with feature/provider records.

`NaN`, signed zero, integer precision loss, BigInt/Number mixing, and
JSON-serialization loss must be represented in the numeric manifest and checked
at boundaries.

## UI and Component Output

When the backend emits UI artifacts, component metadata records:

- component name and props schema,
- state and effect hooks or runtime equivalents,
- DOM/event capabilities,
- style or asset dependencies,
- hydration or server/client boundary,
- source map and hot-reload identity,
- taint policy for user input.

UI lowering must not hide DOM, storage, network, timer, or browser capability
use behind ordinary pure functions.

## Diagnostics

JavaScript/TypeScript backend diagnostics use `B6` identifiers:

- `B6-TARGET` for unsupported runtime, module format, ECMAScript feature, or
  bundler assumption.
- `B6-GLOBAL` for host global access without effects and capabilities.
- `B6-IMPORT` for untyped, side-effectful, denied, or version-unstable package
  imports.
- `B6-NULLISH` for unchecked `null` or `undefined` flow.
- `B6-EXCEPTION` for untranslated exceptions or rejected promises.
- `B6-NUMERIC` for precision loss, invalid BigInt/Number mixing, unchecked
  overflow, or invalid JSON numeric boundaries.
- `B6-EVAL` for dynamic eval, Function construction, or dynamic code loading
  without policy.
- `B6-PROTOTYPE` for prototype mutation or object-layout assumptions that
  violate safe Gravity.
- `B6-ASYNC` for missing async effect, cancellation, or event-loop metadata.
- `B6-UI` for UI/component output without schema, source map, or capability
  records.
- `B6-MANIFEST` for incomplete JS/TS artifacts.

Diagnostics must include source span, MIR operation or domain anchor, runtime,
module format, host symbol or package id, missing effect/capability/schema fact,
selected adapter or rejection, and remediation.

## Rejected Designs

Gravity rejects treating TypeScript annotations as a substitute for Gravity type
and safety artifacts.

Gravity rejects ambient access to browser, Node.js, edge, or package globals.

Gravity rejects unchecked `null` and `undefined` in safe Gravity values.

Gravity rejects dynamic eval and prototype mutation as ordinary hosted behavior.

Gravity rejects lossy `Number` lowering when exact numeric semantics are
required.

## Conformance Criteria

A conforming JavaScript/TypeScript backend must demonstrate:

- ESM and selected runtime target emission,
- TypeScript declaration generation for exported Gravity APIs,
- source-map and generated-origin preservation,
- host global capability manifests,
- accepted and rejected package import fixtures,
- checked `null`/`undefined`, exception, and rejected-promise translation,
- numeric tests for `Number`, `BigInt`, typed arrays, and boundary validation,
- async effect and event callback records,
- UI/component metadata when UI lowering is enabled,
- rejection of eval, prototype mutation, ambient globals, and lossy numeric
  conversions,
- differential execution against MIR reference fixtures.
