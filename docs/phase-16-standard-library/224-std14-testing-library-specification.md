# STD14 - Testing Library Specification

Sequence: 224
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.test` defines unit tests, conformance tests, property tests, fuzz tests, fixtures, mocks, golden artifacts, differential tests, benchmark assertions, and test reports.
It is the standard library interface to the testing strategy defined in Phase 14.
Tests must be first-class artifacts that preserve profile, target, seed, capability, fixture, expected diagnostic, and provenance metadata.

The testing library cannot hide nondeterminism or live service access.
Tests that use time, randomness, filesystem, network, database, AI providers, hardware, or host services must declare effects and capabilities.
Conformance tests must be able to run against seed compilers, self-hosted compilers, runtime adapters, and package artifacts.

## Requirements

- Test definitions MUST declare profile, target constraints, effects, capabilities, fixtures, and expected artifacts.
- Assertions MUST produce structured failures with source spans and expected/actual values.
- Property and fuzz tests MUST record seeds, generators, shrinking strategy, and minimized counterexamples.
- Golden tests MUST hash and version their golden artifacts.
- Tests expecting diagnostics MUST match diagnostic id, span class, profile, and remediation category.
- Mocks and fakes MUST declare which effects or capabilities they replace.
- Live service tests MUST be opt-in through capabilities and must redact secrets.
- AI eval tests MUST record model identity, prompt version, dataset, metric, and threshold.
- Performance tests MUST record target, profile, compiler, runtime, and benchmark settings.
- Test reports MUST be packageable artifacts usable by conformance and governance tooling.

## Module Surface

- Test forms: `deftest`, `testing`, `is`, `are`, `throws?`, `diagnostic?`, and `test-var`.
- Fixtures: `fixture`, `with-fixture`, `temp-resource`, `golden`, `golden-update`, and `artifact-fixture`.
- Property testing: `defproperty`, `for-all`, `gen`, `shrink`, `seed`, and `counterexample`.
- Fuzzing: `deffuzz`, `fuzz-input`, `corpus`, `minimize`, and `coverage-guided`.
- Mocks: `fake-capability`, `mock-effect`, `record-calls`, and `expect-call`.
- Conformance: `conformance-suite`, `profile-suite`, `backend-suite`, `stdlib-suite`, and `report`.
- AI eval: `eval-case`, `eval-dataset`, `metric`, `threshold`, and `probe`.
- Reporting: `test-report`, `junit`, `json-report`, `coverage-report`, and `evidence-pack`.

## Dependencies

- `TEST1` through `TEST13` for testing strategy, suite categories, fuzzing, differential testing, formal validation, performance regression, and self-hosting.
- `D8` and `D9` for safety outcomes, proof obligations, replay records, audit records, and conformance evidence.
- `L5`, `L6`, and `L14` for effects, capabilities, and compile-time validation.
- `SAFE10`, `SAFE11`, and `SAFE15` for capability security, taint, and proof-carrying libraries.
- `P1` through `P13` for profile-specific test matrices.
- `STD8`, `STD9`, `STD10`, `STD12`, and `STD13` for IO, network, schemas, workflows, and AI testing.
- `PKG7`, `PKG10`, and `PKG12` for reproducible test builds, provenance, signing, and SBOMs.

## Example

```clojure
(ns sample.test
  (:require [gravity.test :as t]
            [gravity.collections :as c])
  (:profile :core))

(t/defproperty assoc-then-get
  {:profiles #{:core :hosted :native}}
  [m k v]
  (t/is (= v (c/get (c/assoc m k v) k))))
```

The property records generator identities, seed, profile, and any minimized counterexample.
It can run as part of standard-library conformance.

## Profile Availability

- `:core` receives pure tests, properties, fixtures, diagnostics tests, and artifact tests.
- `:hardware`, `:firmware`, and `:kernel` receive harness adapters that respect no-host or restricted-host execution.
- `:native` receives full test, fuzz, benchmark, and sanitizer integration where configured.
- `:hosted` receives host test runners, coverage, and integration test adapters.
- `:distributed` receives workflow replay tests, activity fakes, and event-log fixtures.
- `:ai` receives eval, prompt-injection, tool-policy, model-output, and workflow-agent tests.
- `:meta` receives compiler and macro test helpers.
- `:formal` receives proof, model-checking, and certificate validation hooks.

## Outputs and Artifacts

- Testing module manifest with suite categories and profile matrix.
- Structured test reports with source spans, profile, target, compiler, runtime, seed, and artifact hashes.
- Property/fuzz counterexample artifacts.
- Golden artifact hashes and update records.
- Expected diagnostic fixtures.
- Capability and effect mock manifests.
- AI eval reports and prompt-injection probe results.
- Coverage, benchmark, sanitizer, formal, and differential evidence packs.

## Diagnostics

- `STD14001` when a test uses an undeclared effect or capability.
- `STD14002` when a property or fuzz test fails without recording seed and minimized input.
- `STD14003` when a golden artifact changes without an update record.
- `STD14004` when an expected diagnostic is matched only by free-form text.
- `STD14005` when a live service test runs without an explicit capability.
- `STD14006` when a mock replaces an effect without declaring the replaced capability.
- `STD14007` when an AI eval lacks model, dataset, metric, or threshold metadata.
- `STD14008` when a test report cannot be tied to source, package, compiler, target, and runtime provenance.

## Conformance Criteria

- Test examples compile and emit structured test artifacts.
- Failing assertions include stable source spans and expected/actual values.
- Property and fuzz runs are reproducible from recorded seeds and generators.
- Golden changes are explicit, hashed, and reviewable.
- Diagnostic tests match ids and structured fields, not only prose.
- Live service and AI tests are denied without capabilities.
- Conformance suites can select tests by profile, target, module, diagnostic id, and artifact type.
- Reports can be consumed by package, governance, bootstrap, and release tooling.
