# B12 - Mobile Backend Design

Sequence: 109
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The mobile backend emits mobile application bundles, platform bindings,
permission manifests, resource manifests, lifecycle adapters, store-audit
metadata, source maps, and simulator/device conformance records for iOS,
Android, and mobile-adjacent runtimes.

Mobile targets combine hosted and native behavior with platform-specific
permission, lifecycle, threading, sandbox, UI, background-task, sensor, network,
storage, notification, and store-policy constraints. Gravity must expose those
constraints through effects, capabilities, manifests, and diagnostics.

## Requirements

- Input must be verified MIR, UI/domain IR, or platform domain IR accepted by
  `B1`, `C12`, and `C14`.
- The backend must declare platform, OS version range, architecture, bundle id,
  packaging mode, runtime provider, UI bridge, lifecycle model, threading model,
  permission model, and app-store policy target.
- Platform API calls require effects, capabilities, permission manifest entries,
  typed adapters, nullability records, error mapping, and lifecycle constraints.
- UI access must respect main-thread or platform actor requirements.
- Background tasks, notifications, location, sensors, camera, microphone,
  contacts, files, keychain/keystore, network, Bluetooth, and local database
  access require explicit capability and deployment policy records.
- Platform nulls, exceptions, callbacks, intents, activities, scenes, deep links,
  and delegate methods must be translated into Gravity types and effects.
- Offline sync, local persistence, and replayable workflows must use schema and
  migration artifacts.
- Hidden background work, unmodeled lifecycle assumptions, and permission-less
  platform API calls must be rejected.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C12` and `C14` define domain IR anchoring and target lowering.
- `P4`, `P5`, and `P13` define hosted, native, and compatibility constraints.
- `SAFE5`, `SAFE10`, `SAFE11`, and `SAFE13` define resource cleanup,
  capabilities, taint, and AI/tool safety for mobile agents.
- Phase 10 schema documents define local data, sync, and platform boundary
  schemas.
- Runtime, package, signing, and tooling documents define mobile runtime,
  packaging, device testing, and store release workflows.

## Outputs and Artifacts

- Mobile backend manifest.
- Platform target record.
- Application bundle artifact.
- Platform binding descriptors.
- Permission manifest.
- Resource and asset manifest.
- Lifecycle and threading map.
- UI bridge metadata.
- Local storage and sync schema bundle.
- Store-audit metadata.
- Source/debug map.
- Device/simulator conformance report.
- Mobile backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/mobile-backend-manifest
 :backend :gravity.backend/mobile
 :target {:platform :ios
          :os-range "declared-range"
          :architecture :arm64}
 :emits #{:app-bundle :platform-bindings :permission-manifest
          :resource-manifest :store-audit}
 :requires #{:lifecycle-map :threading-map :capability-manifest
             :platform-adapter-map}
 :rejects #{:platform-api-without-permission :hidden-background-effect
            :unmodeled-lifecycle-assumption :unchecked-platform-null}}
```

The manifest is consumed by package tools, signing/release tools, store-audit
checks, device test runners, and diagnostics.

## Platform Target and Packaging

The platform record includes:

- platform and OS version range,
- architecture and ABI,
- bundle/application id,
- entitlements and permissions,
- resource and asset catalogs,
- native libraries,
- UI framework or bridge,
- lifecycle entry points,
- background mode declarations,
- deployment environment,
- signing and release-policy references.

Signing and store submission are controlled by later package/release documents,
but the mobile backend must emit the metadata they require.

## Platform API Bindings

Binding descriptors include:

- platform symbol, framework, class, method, intent, callback, or delegate,
- Gravity type and platform type,
- nullability,
- exception/error mapping,
- thread or actor affinity,
- lifecycle state required,
- permission and capability,
- taint policy for platform input,
- resource cleanup behavior,
- source and generated-origin links.

Bindings that cannot provide enough metadata are opaque foreign values and may
only be used through audited wrappers.

## Lifecycle, Threads, and UI

Lifecycle records include app start, foreground/background transitions,
termination, scene/activity creation, view lifecycle, configuration changes,
deep links, push notifications, background fetch, and restore paths.

Threading records include main/UI thread requirements, background workers,
structured concurrency, cancellation, platform schedulers, and callback
affinity. UI updates outside the required thread or lifecycle state are rejected
or routed through explicit runtime adapters.

## Permissions and Capabilities

The permission manifest records:

- camera,
- microphone,
- photos/media,
- location,
- contacts,
- calendar,
- notifications,
- Bluetooth,
- sensors,
- keychain or keystore,
- local files,
- network,
- background execution,
- inter-app communication,
- model/tool providers when mobile agents are used.

Permission text, deployment grant, runtime request behavior, denial behavior,
and source locations are recorded for each capability.

## Storage, Sync, and Offline Behavior

Local data records include schema id, migration policy, encryption/storage
provider, taint category, retention policy, backup policy, sync behavior, and
conflict handling. Offline queues and mobile workflow steps must connect to
distributed replay and idempotency records when they cross service boundaries.

## Diagnostics

Mobile backend diagnostics use `B12` identifiers:

- `B12-TARGET` for unsupported platform, OS range, architecture, bundle mode, or
  UI bridge.
- `B12-PERMISSION` for platform API use without permission, capability, runtime
  request, or denial policy.
- `B12-LIFECYCLE` for unmodeled or impossible lifecycle assumptions.
- `B12-THREAD` for main-thread, actor, callback, or scheduler violations.
- `B12-NULL` for unchecked platform null or optional values.
- `B12-ERROR` for untranslated platform exceptions, callbacks, or error codes.
- `B12-BACKGROUND` for hidden background execution, network, sync, or sensor
  use.
- `B12-STORAGE` for missing local schema, migration, encryption, retention, or
  sync policy.
- `B12-RESOURCE` for missing asset/resource/bundle metadata.
- `B12-MANIFEST` for incomplete mobile artifacts.

Diagnostics must include source span, MIR operation or domain anchor, platform,
API symbol, lifecycle state, thread/actor, permission/capability, missing
adapter or schema, and remediation.

## Rejected Designs

Gravity rejects platform API calls without permission and capability records.

Gravity rejects hidden background execution and network work.

Gravity rejects unchecked platform nulls, exceptions, callbacks, and lifecycle
state.

Gravity rejects UI updates that ignore platform thread or actor requirements.

Gravity rejects local storage and sync without schemas and migration policy.

## Conformance Criteria

A conforming mobile backend must demonstrate:

- iOS and Android-style platform target manifests,
- app bundle, resource, permission, and binding artifact emission,
- platform API capability acceptance and rejection,
- lifecycle and threading diagnostics,
- null/error/callback adapter fixtures,
- camera, network, storage, notification, and background-task permission tests,
- local schema, migration, and offline sync artifacts,
- source/provenance/effect/capability metadata preservation,
- simulator or device smoke records for startup, permission prompts, lifecycle,
  and crash diagnostics.
