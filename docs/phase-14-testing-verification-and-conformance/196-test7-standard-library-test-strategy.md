# TEST7 - Standard Library Test Strategy

Sequence: 196
Phase: 14 - Testing, Verification and Conformance
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This strategy defines tests for the Gravity standard library. The standard
library must work across profiles without smuggling hosted assumptions into
core, firmware, kernel, hardware, native, distributed, AI, GPU, or formal
contexts. Tests cover API semantics, profile availability, effects,
capabilities, safety, performance-sensitive paths, and documentation examples.

Standard library tests are the contract between language semantics and everyday
programming APIs.

## Module Areas

The strategy covers:

- core library;
- collections;
- strings and text;
- numeric and math;
- memory and resources;
- concurrency;
- IO and filesystem;
- network and HTTP;
- serialization and schemas;
- database and query;
- workflow;
- AI and agents;
- testing;
- compiler metaprogramming;
- platform and OS;
- hardware and firmware;
- cryptography;
- UI and application support.

## Requirements

- Each public API MUST have behavior fixtures or proof/evidence where executable tests are not possible.
- Tests MUST declare profile and target availability.
- Effectful APIs MUST test required effects and missing-capability failures.
- Safe APIs wrapping unsafe internals MUST test boundary behavior and audit evidence.
- Collection, string, numeric, and schema APIs MUST have property tests.
- IO, network, database, and platform APIs MUST have mock and capability-denial fixtures.
- Documentation examples marked runnable MUST be checked.
- Stability policy tests MUST flag breaking API changes.

## Semantic Dependencies

- `STD1` through `STD20` define library contracts.
- `P1` through `P13` define profile legality.
- `SAFE1` through `SAFE16` define safety behavior.
- `S1` through `S9` define schema and interop behavior.
- `TEST9` defines property testing.
- `T7` defines documentation example checks.

## Outputs and Artifacts

Standard library tests emit:

- module test reports;
- profile availability matrix;
- capability denial reports;
- property test reports;
- documentation example reports;
- stability compatibility report.

## Example

```clojure
(deftest vector-map-preserves-count
  {:suite :stdlib
   :module :collections
   :property true
   :profile :core}
  (= (count xs) (count (map inc xs))))
```

## Rejection Rules

- Reject public APIs without tests or explicit evidence.
- Reject effectful APIs that succeed without required capabilities.
- Reject profile availability claims not backed by fixtures.
- Reject unsafe wrapper APIs without safety tests.
- Reject documentation examples that fail.
- Reject breaking public API changes without stability policy handling.

## Diagnostics

- `TEST7001` reports untested public API.
- `TEST7002` reports missing capability denial fixture.
- `TEST7003` reports profile availability mismatch.
- `TEST7004` reports unsafe wrapper test gap.
- `TEST7005` reports documentation example failure.
- `TEST7006` reports stability break.

## Conformance Criteria

- Public APIs have behavior tests, properties, or proof evidence.
- Profile matrix reports where each module is legal.
- Missing capabilities deny effectful APIs.
- Unsafe wrappers preserve safe API contracts.
- Property tests cover collections, strings, numerics, and schemas.
- Runnable documentation examples pass.
- Stability checks identify breaking API changes.
