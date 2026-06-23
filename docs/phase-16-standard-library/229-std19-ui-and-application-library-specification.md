# STD19 - UI and Application Library Specification

Sequence: 229
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.ui` defines standard application and UI abstractions for components, state, events, commands, routing, rendering, styling, accessibility, platform adapters, and application lifecycle.
It gives hosted, native, web, mobile, desktop, and tool-facing applications a typed interface without pretending every UI target shares one rendering runtime.
The library expresses UI behavior as data and effects that can be inspected, tested, compiled, and adapted to targets.

UI code often crosses security and platform boundaries.
Events are untrusted input, rendered text must preserve escaping policy, platform adapters may access filesystem or network capabilities, and AI-generated UI must still pass type, effect, schema, and safety checks.
The standard UI library must therefore preserve event contracts, state schemas, accessibility metadata, effect handlers, and target adapter artifacts.

## Requirements

- Components MUST declare props, state, emitted events, handled commands, effects, and target availability.
- UI state crossing persistence, workflow, AI, or network boundaries MUST use STD10 schemas.
- Event handlers MUST declare effects and capabilities.
- Rendering APIs MUST distinguish escaped text, trusted markup, binary media, and target-native views.
- Accessibility metadata MUST be representable in component artifacts for supported targets.
- Routing APIs MUST declare parameter schemas and navigation effects.
- Platform adapters MUST declare target, host services, lifecycle hooks, and unsupported features.
- Styling APIs MUST preserve deterministic tokens or target-specific delegation records.
- AI-generated UI code MUST pass compiler, profile, safety, schema, and policy checks.
- UI tests MUST be able to consume component schemas, event contracts, and rendered artifact snapshots.

## Module Surface

- Components: `defcomponent`, `component`, `props`, `state`, `view`, `slot`, and `fragment`.
- Events and commands: `event`, `on`, `emit`, `command`, `dispatch`, `effect-handler`, and `event-contract`.
- State: `state`, `signal`, `atom-state`, `derived`, `reducer`, `store`, and `state-schema`.
- Routing: `route`, `router`, `navigate`, `link`, `route-param`, and `route-schema`.
- Rendering: `render`, `hydrate`, `mount`, `unmount`, `portal`, `view-tree`, and `render-artifact`.
- Styling: `style`, `class`, `theme-token`, `layout`, `media-query`, and `target-style`.
- Accessibility: `label`, `role`, `focus`, `keyboard`, `announce`, and `accessibility-tree`.
- Adapters: `web-adapter`, `desktop-adapter`, `mobile-adapter`, `terminal-adapter`, and `native-view`.

## Dependencies

- `L2`, `L5`, `L6`, `L10`, `L12`, and `L14` for types, effects, capabilities, values, macros, and compile-time checks.
- `SAFE1`, `SAFE10`, `SAFE11`, and `SAFE15` for safe semantics, capability security, taint, and proof-carrying wrappers.
- `P5`, `P4`, `P9`, and `P10` for native, hosted, distributed, and AI integration.
- `STD4`, `STD9`, `STD10`, `STD13`, and `STD14` for text, network, schemas, AI, and tests.
- `T5`, `T7`, and `T8` for LSP, documentation, and development server integration.
- `PKG3`, `PKG7`, and `PKG10` for artifact identity, reproducible builds, and provenance.

## Example

```clojure
(ns sample.ui
  (:require [gravity.ui :as ui])
  (:profile :hosted))

(ui/defcomponent Counter
  {:props {:initial :i64}
   :state {:count :i64}
   :events {:increment {:by :i64}}}
  [props state]
  (ui/view [:button {:on-click [:increment {:by 1}]}
            (str (:count state))]))
```

The component emits props, state, event, and rendered view artifacts.
The click handler declares an event, not an unchecked host callback.

## Profile Availability

- `:hosted` receives web, managed host, and development UI adapters.
- `:native` receives desktop, mobile, terminal, and native view adapters where implemented.
- `:distributed` may render workflow dashboards only when state and events are schema-backed.
- `:ai` may generate or operate UI through policy-checked tools and schema-bound actions.
- `:core` receives component data definitions and pure view trees.
- `:firmware` and `:kernel` may receive constrained terminal or device UI adapters under capabilities.
- `:hardware` receives static display descriptions only when a backend supports them.
- `:meta` may generate components and inspect UI artifacts.
- `:formal` may verify state machines, event contracts, and accessibility invariants.

## Outputs and Artifacts

- UI module manifest with component, target, effect, capability, and adapter metadata.
- Component artifacts with props, state, events, commands, accessibility, and render shape.
- Route and navigation artifacts with parameter schemas.
- Style artifacts with token and target-delegation metadata.
- Snapshot and interaction fixtures for STD14 tests.
- Negative fixtures for untyped state, hidden platform effects, unsafe markup, missing accessibility labels, and AI-generated bypasses.
- Adapter records for web, desktop, mobile, terminal, and native targets.

## Diagnostics

- `STD19001` when a component lacks props, state, event, or effect metadata required by its profile.
- `STD19002` when event handlers perform undeclared effects or use missing capabilities.
- `STD19003` when rendered text or markup violates escaping or trust policy.
- `STD19004` when route parameters lack schema metadata.
- `STD19005` when accessibility metadata is missing for an interactive element.
- `STD19006` when a platform adapter lacks target or unsupported-feature artifacts.
- `STD19007` when AI-generated UI bypasses compiler, schema, safety, or policy checks.
- `STD19008` when UI state crosses a boundary without serialization or taint metadata.

## Conformance Criteria

- Component fixtures emit stable props, state, event, accessibility, and render artifacts.
- Event fixtures enforce effects and capabilities.
- Rendering fixtures distinguish escaped text, trusted markup, and target-native views.
- Routing fixtures validate parameter schemas and navigation effects.
- Adapter fixtures document unsupported features and preserve Gravity diagnostics.
- AI UI fixtures prove generated code passes ordinary checks.
- Snapshot and interaction tests consume component artifacts rather than raw host internals.
- Documentation examples compile under declared UI targets and fail when required adapters are absent.
