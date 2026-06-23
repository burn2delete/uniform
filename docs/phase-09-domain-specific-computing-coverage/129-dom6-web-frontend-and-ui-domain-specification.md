# DOM6 - Web Frontend and UI Domain Specification

Sequence: 129
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover web frontend and UI slices normally
written in JavaScript, TypeScript, JSX/TSX, Elm, ClojureScript, Svelte, Vue, or
framework-specific DSLs.

The replacement scope is typed components, event handlers, route views, forms,
client API calls, browser storage, offline cache, generated API clients, Wasm
modules, and UI conformance tests under the `:hosted` profile.

## Requirements

- Web UI code must target JS/TS or Wasm through a hosted profile.
- DOM, storage, network, timers, clipboard, notifications, workers, and browser
  APIs require effects and capabilities.
- Component props, route params, API inputs/outputs, form data, and browser
  storage values must have schemas.
- User and network input is tainted until validated for a UI, HTML, URL, CSS,
  script, storage, or network sink.
- Generated API clients must track schema version and drift from server
  artifacts.
- Package imports require typed wrappers or opaque foreign boundaries.
- Source maps must connect browser diagnostics to Gravity source and generated
  UI artifacts.

## Dependencies

- `P4` defines hosted behavior.
- `B4` and `B6` define Wasm and JS/TS backends.
- `R4`, `R9`, `R11`, and `R12` define managed, REPL, capability, and
  observability runtime behavior.
- `SAFE10`, `SAFE11`, and Phase 10 schema docs define capability, taint, and
  schema validation.

## Outputs and Artifacts

- Web UI domain manifest.
- JavaScript bundle.
- TypeScript declaration artifact.
- Wasm module when selected.
- Component schema bundle.
- API client artifact.
- Browser capability manifest.
- Source map.
- UI conformance report.
- Web UI diagnostics.

## Domain Manifest

```clojure
{:domain :web-ui
 :profiles #{:hosted}
 :backends #{:javascript-typescript :wasm}
 :artifacts #{:js-bundle :typescript-declarations :typed-components
              :api-client :browser-capability-manifest}
 :examples #{:component :form :typed-fetch :offline-cache}
 :rejects #{:ambient-dom-access :unsafe-html-sink
            :schema-drift :untyped-package-import}}
```

## Replacement Scope

Gravity should replace frontend slices for:

- components and route views,
- state and event handlers,
- forms with validation,
- typed fetch/API clients,
- browser storage wrappers,
- offline cache and service-worker-style boundaries,
- generated Wasm compute modules,
- UI test fixtures.

Framework runtimes and browser engines remain host boundaries with typed
adapters.

## Minimum End-to-End Slice

The first complete slice is a ticket-list page:

- Gravity source declares `Ticket` schema, route, component props, and network
  effect.
- Compiler validates fetch capability, taint, and rendering sinks.
- JS/TS backend emits bundle and declarations.
- Schema tool emits typed API client.
- Browser fixture tests loading, validation failure, user input, and source-map
  diagnostics.

## Diagnostics

Web UI diagnostics use `DOM6` identifiers:

- `DOM6-DOM` for browser API use without effects and capabilities.
- `DOM6-TAINT` for unvalidated input reaching UI, HTML, URL, CSS, script, or
  storage sinks.
- `DOM6-SCHEMA` for missing or drifted component, route, or API schemas.
- `DOM6-PACKAGE` for untyped package imports.
- `DOM6-NUMERIC` for lossy JS numeric boundaries.
- `DOM6-SOURCEMAP` for browser diagnostics without source mapping.
- `DOM6-CONFORMANCE` for missing browser or component test evidence.

Diagnostics must include component or route id, source span, browser capability,
schema id, taint category, package id when relevant, and remediation.

## Rejected Designs

Gravity rejects ambient browser globals.

Gravity rejects unsafe HTML or script sinks for tainted values.

Gravity rejects API clients without schema drift detection.

Gravity rejects TypeScript-only safety for untrusted runtime data.

Gravity rejects package imports as safe without typed boundaries.

## Conformance Criteria

A conforming web UI slice must demonstrate:

- component, form, event, and route examples,
- typed fetch and API client artifacts,
- browser capability checks,
- taint validation for UI sinks,
- source-map diagnostics,
- rejection of ambient globals, unsafe HTML, schema drift, and untyped imports,
- browser or DOM-level conformance fixtures.
