# C18 - Compiler Verification and Pass-Correctness Strategy

Sequence: 97
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Compiler verification defines how Gravity earns trust in its reader, macro
expander, analyzer, type and effect checkers, safety pipeline, MIR, optimizers,
domain IRs, plugins, and target lowerers. It assigns evidence requirements to
compiler stages based on risk and profile impact.

The compiler does not need every pass mechanized from day one. It does need
explicit evidence, diagnostics, and trust reports that make pass correctness
auditable and improve over time.

## Requirements

- Every compiler pass must have an evidence class appropriate to its risk.
- High-risk passes must use mechanized proof, translation validation, checked
  certificates, differential testing, property testing, or conformance fixtures.
- MIR-to-MIR optimizations that change behavior-relevant structure must emit
  equivalence evidence or validation queries for affected functions.
- Target lowerers must compare source/core/MIR intent against emitted artifact
  behavior for supported profiles.
- Compiler verification must preserve source and generated-origin diagnostics.
- Trust reports must identify which profiles, targets, passes, and artifact
  kinds depend on which evidence.
- Failed verification blocks release artifacts for affected profiles.

## Dependencies

- `D9` defines proof and evidence categories.
- `C1` through `C17` define compiler stages, artifacts, diagnostics, plugins,
  and target lowering.
- `SAFE15` defines proof and certificate trust policy.
- `PERF10` defines proof requirements for check elision.
- Phase 14 testing documents define language, compiler, backend, formal, fuzz,
  differential, performance, and self-hosting tests.
- Phase 15 defines bootstrap comparison and staged trust reduction.

## Outputs and Artifacts

- Compiler verification plan.
- Pass risk classification.
- Pass evidence records.
- Translation validation logs.
- Proof or certificate references.
- Differential and property fixture results.
- Compiler trust report.
- Release gate report.
- Verification diagnostics.

## Pass Risk Classification

```clojure
{:artifact :gravity/pass-risk
 :pass :bounds-check-elide
 :risk :high
 :reason #{:removes-runtime-checks :depends-on-proof}
 :affected-profiles #{:native :kernel :firmware :gpu}
 :minimum-evidence #{:translation-validation :proof-dominance-check}
 :release-gate :required}
```

Risk levels:

- `:low`: formatting, reporting, or metadata-only with verifier checks.
- `:medium`: changes IR shape but not semantics under verifier guarantees.
- `:high`: changes optimization, checks, effects, ownership, target metadata, or
  safety outcomes.
- `:critical`: part of trusted semantic base, safety gate, proof checker, or
  target lowering for safety-critical profiles.

## Verification Matrix

| Pass class | Minimum evidence | Stronger evidence |
| --- | --- | --- |
| Reader and syntax | Golden fixtures, round-trip tests, fuzzing | Parser proof or independent reader differential |
| Macro expansion | Hygiene fixtures, generated-origin tests, build-effect tests | Expansion preservation proof |
| Name resolution | Golden binding graphs, shadowing negatives | Resolver model proof |
| Type/effect checking | Positive and negative fixtures, property tests | Mechanized core typing/effect rules |
| Ownership and safety | Vulnerability fixtures, proof/certificate checks | Proof-carrying analysis |
| MIR construction | MIR verifier and core-to-MIR golden tests | Core-to-MIR translation proof |
| MIR optimization | Translation validation per changed function | Mechanized pass proof |
| Domain IR optimization | Domain verifier plus proof/certificate replay | Mechanized domain semantics |
| Target lowering | Differential execution and backend conformance | Verified backend subset |
| Plugins | Sandbox tests, contract verifier, fixture suite | Signed proof-carrying plugin |

## Translation Validation

A translation-validation artifact contains:

```clojure
{:artifact :gravity/translation-validation
 :pass :loop-invariant-code-motion
 :input input-mir-hash
 :output output-mir-hash
 :changed-functions [fn-id]
 :properties #{:same-observable-result :same-effects :same-safety-outcomes}
 :method :symbolic-plus-fixtures
 :proofs [proof-id]
 :counterexamples []
 :result :accepted}
```

Validation failures reject the transformation and retain the prior artifact.

## Trust Report

```clojure
{:artifact :gravity/compiler-trust-report
 :compiler compiler-id
 :passes [{:pass :reader :risk :critical :evidence [:fuzz :golden]}
          {:pass :bounds-check-elide :risk :high :evidence [:translation-validation]}]
 :profiles {:kernel {:required-evidence :high
                     :blocked-passes []}}
 :known-gaps [{:pass :gpu-lowering
               :profiles #{:gpu}
               :status :experimental}]}
```

The trust report is emitted with release builds and bootstrap stages.

## Release Gates

Release gates require:

- verifier pass for every emitted artifact,
- no active critical verification failures,
- high-risk pass evidence present,
- target-lowering conformance for selected backends,
- stale proof and stale certificate rejection,
- diagnostic golden fixtures for expected failures,
- self-hosting comparison when the compiler is part of the release.

Experimental passes can be shipped only behind explicit feature or profile
gates and must be marked in artifacts.

## Counterexamples

Verification failures should produce counterexample artifacts when possible:

- source fixture,
- input artifact,
- output artifact,
- violated property,
- diagnostic stream,
- minimized reproducer,
- affected pass and version.

Counterexamples become regression fixtures.

## Diagnostics

Compiler verification diagnostics use `C18` identifiers:

- `C18-RISK` for missing or inconsistent pass risk classification.
- `C18-EVIDENCE` for missing required evidence.
- `C18-VALIDATION` for translation validation failure.
- `C18-PROOF` for rejected proof or certificate.
- `C18-TRUST-REPORT` for incomplete compiler trust reports.
- `C18-RELEASE-GATE` for release blocked by verification gaps.
- `C18-COUNTEREXAMPLE` for generated counterexample artifacts.
- `C18-PLUGIN` for plugin evidence below policy.
- `C18-BACKEND` for target-lowering conformance gaps.

Diagnostics must include pass id, version, risk class, required evidence,
available evidence, affected profiles and targets, source or artifact id, and
remediation.

## Rejected Designs

Gravity rejects unverifiable optimization as a release-quality claim.

Gravity rejects check-eliding passes without translation validation or proof.

Gravity rejects target lowerers whose conformance is only documented in prose.

Gravity rejects plugin passes outside the trust report.

Gravity rejects release artifacts with hidden experimental compiler passes.

## Conformance Criteria

A conforming compiler verification strategy must demonstrate:

- pass risk classification for every compiler pass,
- verifier reports for every stage artifact,
- translation validation for MIR optimization examples,
- proof or certificate replay for safety-check elision,
- differential and property tests for representative front-end and backend
  paths,
- trust report emission,
- release gate failures for missing evidence,
- counterexample capture and regression fixture creation,
- explicit experimental gating for incomplete evidence.
