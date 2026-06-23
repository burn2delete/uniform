# DOM7 - Mobile Application Domain Specification

Sequence: 130
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover mobile application slices normally
written in Swift, Kotlin, Java, Objective-C, TypeScript/React Native, Dart,
Flutter, or platform-specific UI DSLs.

The replacement scope is typed screens, platform API adapters, permissions,
local storage, offline sync, push notifications, camera/sensor access,
background tasks, mobile API clients, and device/simulator tests.

## Requirements

- Mobile code must use hosted/native profile boundaries accepted by the mobile
  backend.
- Platform APIs require permission manifests, capabilities, lifecycle state,
  thread/actor rules, nullability, error mapping, and taint policy.
- UI updates must obey platform main-thread or actor requirements.
- Background work must declare lifecycle, timeout, retry, network, storage, and
  cancellation behavior.
- Local storage and offline sync require schemas, migration policy, encryption
  or storage provider, retention, and conflict handling.
- Secrets must use keychain/keystore or declared providers with redaction policy.
- Device tests must cover permissions, lifecycle, offline behavior, and crash
  diagnostics.

## Dependencies

- `P4`, `P5`, and `P13` define hosted/native profile boundaries.
- `B12` defines mobile backend artifacts.
- `R4`, `R5`, `R11`, and `R12` define managed/native runtime, memory,
  capability, and observability behavior.
- Phase 10 schema docs define local data and sync schemas.
- `SAFE10` and `SAFE11` define capabilities and taint.

## Outputs and Artifacts

- Mobile domain manifest.
- App bundle.
- Platform binding manifest.
- Permission manifest.
- Lifecycle and threading map.
- Offline schema and migration bundle.
- Resource manifest.
- Store-audit metadata.
- Simulator/device conformance report.
- Mobile diagnostics.

## Domain Manifest

```clojure
{:domain :mobile
 :profiles #{:hosted :native}
 :backends #{:mobile :javascript-typescript :llvm}
 :artifacts #{:app-bundle :permission-manifest :platform-bindings
              :offline-schema :device-tests}
 :examples #{:screen :camera-capture :offline-sync :push-handler}
 :rejects #{:platform-api-without-permission :hidden-background-work
            :unchecked-platform-null :lifecycle-unsafe-ui}}
```

## Replacement Scope

Gravity should replace mobile slices for:

- screens and components,
- API clients and offline caches,
- camera, sensor, notification, storage, and keychain adapters,
- push and background task handlers,
- local persistence and sync,
- mobile workflow clients,
- simulator/device test fixtures.

Platform UI runtimes and store signing remain backend/package boundaries.

## Minimum End-to-End Slice

The first complete slice is an offline ticket screen:

- Gravity source declares ticket schema, screen, local store, network sync, and
  notification permission.
- Mobile checks validate lifecycle, main-thread UI updates, storage migration,
  and network capability.
- Backend emits app bundle, platform bindings, permission manifest, and resource
  manifest.
- Simulator test covers permission denial, offline edit, sync retry, and crash
  source maps.

## Diagnostics

Mobile domain diagnostics use `DOM7` identifiers:

- `DOM7-PERMISSION` for platform API use without permission and capability.
- `DOM7-LIFECYCLE` for invalid lifecycle or background assumptions.
- `DOM7-THREAD` for UI or callback thread violations.
- `DOM7-NULL` for unchecked platform null or optional values.
- `DOM7-STORAGE` for missing local schema, migration, encryption, retention, or
  conflict policy.
- `DOM7-SECRET` for unsafe keychain/keystore or secret handling.
- `DOM7-CONFORMANCE` for missing simulator/device evidence.

Diagnostics must include screen or platform API id, source span, platform,
permission, capability, lifecycle state, schema id, and remediation.

## Rejected Designs

Gravity rejects platform API calls without permission manifests.

Gravity rejects hidden background work.

Gravity rejects UI updates outside platform lifecycle/thread rules.

Gravity rejects offline storage without schemas and migrations.

Gravity rejects unchecked platform null/error interop.

## Conformance Criteria

A conforming mobile slice must demonstrate:

- app bundle and platform binding artifacts,
- screen, permission, storage, background, and notification examples,
- simulator/device tests for permissions, lifecycle, offline sync, and crashes,
- source-map diagnostics,
- rejection of missing permissions, hidden background work, lifecycle misuse,
  unchecked nulls, and unsafe storage.
