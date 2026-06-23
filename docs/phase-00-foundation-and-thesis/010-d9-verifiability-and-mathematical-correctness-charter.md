# D9 - Verifiability & Mathematical Correctness Charter

Sequence: 10
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

D9 defines when Gravity must produce proof, certificate, replay, audit, or conformance evidence. Verification is a normal compiler and package product, not an optional research appendix.

Gravity does not require every program to be fully formally verified. It does require that safety-critical, math-critical, optimization-critical, replay-critical, unsafe, package, and bootstrap claims produce explicit evidence that tools can inspect.

## Verification Layers

Gravity uses several evidence layers.

- Static proof: compiler or prover establishes a property before runtime.
- Runtime check: specified check protects a property when static proof is absent.
- Certificate: compact machine-checkable evidence attached to an artifact.
- Conformance fixture: executable positive or negative test that defines accepted behavior.
- Replay record: durable trace of nondeterministic decisions for distributed or AI behavior.
- Audit record: human and machine-readable evidence for unsafe, supply-chain, or governance-sensitive behavior.
- Differential evidence: comparison against another implementation, backend, stage, or oracle.
- Benchmark evidence: measurement with source, target, compiler, layout, and harness metadata.

Evidence must be stored as artifacts with provenance links.

## Mathematical Correctness Model

Core semantics must remain small enough to specify and test. Primitive forms such as `quote`, `if`, `do`, `let`, `fn`, `loop`, `recur`, `def`, `var`, `set!`, `try`, `throw`, and `match` form the semantic base refined by phase 1.

MIR must be typed, effect-annotated, profile-valid, and explicit about control flow, data flow, calls, allocation, ownership, error paths, and effect operations so pass correctness can be checked locally.

EFIR is the semantic carrier for elementary functions. It records expression graph, domains, codomains, branch policy, numeric mode, precision contract, and source spans.

EML is a proof/search/normalization substrate. It is not the required runtime representation and not an equality oracle. Two expressions are equivalent only when a symbolic proof, interval proof, SMT/prover result, rewrite certificate, or approximation certificate establishes the relation under declared domains and modes.

Numeric modes and math-analysis modes include:

- `:symbolic`,
- `:exact-real`,
- `:integer-exact`,
- `:rational-exact`,
- `:interval`,
- `:correctly-rounded`,
- `:faithful`,
- `:certified-approx`,
- `:fast-approx`,
- `:hardware-native`,
- `:libm`,
- `:eml-normalized`.

`:eml-normalized` is a proof/search normalization mode, not a runtime implementation mode. Mode changes must be explicit and artifacted.

## Proof Artifact Shape

```clojure
{:proof-id "sin-poly-[-pi,pi]-f32"
 :kind :approximation-certificate
 :source-expression '(sin x)
 :semantic-ir :efir
 :domain {:x [:closed -3.1415927 3.1415927]}
 :numeric-mode :certified-approx
 :target :llvm-f32
 :claim {:max-absolute-error "2^-22"}
 :assumptions [:round-to-nearest-even :no-flush-to-zero :finite-input]
 :checker :gravity.math.interval/check
 :inputs [:efir-graph :polynomial :rounding-model]
 :result :accepted}
```

The certificate checker is part of the trusted surface. A certificate that cannot be checked by the declared checker is not evidence.

## Verification Obligations

Safety verification:

- check elision requires proof preservation or regeneration,
- unsafe islands require audit records and safe-wrapper evidence,
- capability use requires manifest and grant evidence,
- FFI requires ABI, ownership, nullability, lifetime, and error evidence,
- taint boundaries require sanitizer or validator evidence.

Compiler verification:

- passes declare input IR, output IR, preserved facts, invalidated facts, and regenerated facts,
- MIR verifier runs after construction and after transformations that may invalidate invariants,
- target lowering preserves or explains type, effect, profile, safety, and source metadata,
- self-hosting stages compare compiler output through equivalence artifacts.

Math verification:

- elementary functions use EFIR when optimization or proof requires analysis,
- EML normalization never by itself proves equality,
- approximations require domain, error, roundoff, branch, target, and checker evidence,
- numeric mode changes require diagnostics or explicit source annotations.

Workflow and AI verification:

- replay-sensitive nondeterminism is recorded,
- model calls record provider, model, prompt artifact, schema, policy, and output validation,
- tool calls record capability grant, arguments, result schema, and approval state,
- evaluation artifacts record prompts, fixtures, model identity, scoring, and regressions.

Package and supply-chain verification:

- reproducible builds record source, lockfile, compiler identity, target matrix, build effects, and artifact hashes,
- signed packages record SBOM, provenance, capabilities, safety summary, and dependency graph,
- bootstrap artifacts record stage identity and equivalence evidence.

## Requirements

- Every proof or certificate must name its claim, domain, assumptions, checker, inputs, and result.
- Runtime checks must be stable enough to test and diagnose.
- Optimization cannot erase checks or proof obligations without replacement evidence.
- Mathematical equivalence must not be inferred from syntax similarity or EML tree identity alone.
- Replay evidence must be sufficient to audit distributed and AI behavior without silently redoing nondeterministic choices.
- Package and bootstrap verification must be reproducible from recorded inputs.

## Dependencies

D9 depends on `D0`, `D1`, `D3`, `D6`, and `D8`.

It is refined by phase 5 math documents, phase 6 compiler verification, phase 14 testing and formal verification, package provenance, safety proof documents, AI evaluation documents, and bootstrap equivalence documents.

## Outputs and Artifacts

D9 requires:

- proof certificates,
- runtime check manifests,
- approximation certificates,
- EFIR graphs,
- EML normalization traces,
- MIR verifier reports,
- pass correctness records,
- replay records,
- audit records,
- conformance fixture results,
- reproducible build evidence,
- bootstrap equivalence reports.

## Rejected Verification Claims

D9 rejects:

- prose-only proof claims for compiler, safety, math, package, or bootstrap guarantees,
- EML normalization presented as equality without a proof,
- benchmark results presented as correctness evidence,
- test snapshots presented as proof certificates,
- model output presented as verified structured output without schema validation,
- replay by re-executing nondeterministic operations,
- package trust based only on registry identity,
- self-hosting trust without reproducible stage comparison.

## Diagnostics

- `D9-PROOF-MISSING`: a claim requires proof or certificate but none is attached.
- `D9-CERT-UNCHECKABLE`: certificate cannot be checked by declared checker.
- `D9-EML-EQUALITY`: EML form identity was used as equivalence evidence.
- `D9-CHECK-ELISION-NO-PROOF`: optimizer erased a check without proof evidence.
- `D9-REPLAY-GAP`: workflow or AI artifact lacks a replay-relevant event.
- `D9-BOOTSTRAP-EQUIV`: self-hosting stage lacks equivalence evidence.

## Conformance Criteria

- Approximation fixtures include accepted and rejected certificates.
- MIR verifier fixtures catch malformed control flow, missing type facts, missing effect facts, and invalid profile facts.
- Optimization fixtures show preserved or regenerated proof evidence.
- Workflow and AI fixtures replay from recorded event logs.
- Package fixtures reproduce artifact hashes from source, lockfile, compiler identity, and target matrix.
- Bootstrap fixtures compare stage outputs and explain permitted differences.
- Documentation and tools distinguish proof, test, benchmark, audit, and replay evidence.

## Change Control

Weakening D9 evidence requirements affects safety, performance, math, compiler correctness, AI auditability, package trust, and self-hosting. Such changes require updates to phase 5, phase 6, phase 12, phase 14, phase 15, phase 17, and any artifact schemas that encode proof or provenance.
