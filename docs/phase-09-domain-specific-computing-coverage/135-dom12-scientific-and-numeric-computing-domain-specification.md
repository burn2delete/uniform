# DOM12 - Scientific and Numeric Computing Domain Specification

Sequence: 135
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover scientific and numeric slices normally
written in Python/NumPy, Julia, MATLAB, R, Fortran, C/C++, CUDA, Mathematica, or
symbolic math systems.

The replacement scope is numeric kernels, elementary functions, EFIR semantic
graphs, EML proof/search artifacts, approximation certificates, symbolic
rewrites, simulations, ODE/iteration steps, vectorized math, GPU kernels, and
conformance against reference numeric implementations.

## Requirements

- Numeric code must declare types, domains, units when used, rounding/precision,
  branch policy, approximation policy, and target backend.
- Elementary function lowering must preserve EFIR semantics, EML proof/search
  artifacts, and certificate evidence.
- Fast math, FMA, reassociation, denormal handling, and approximate functions
  require explicit numeric modes and proof/certificate records.
- Symbolic rewrites require semantic equivalence evidence, not merely syntactic
  equality.
- Interop with BLAS, LAPACK, libm, MPFR, Python, or GPU libraries requires
  provider, FFI, and numeric-mode manifests.
- Benchmarks and accuracy reports must record domain, target, error bounds, and
  reference implementation.

## Dependencies

- Phase 5 math documents define EFIR, EML, approximations, certificates,
  floating modes, and math conformance.
- `P5`, `P11`, and `P12` define native, GPU, and formal profiles.
- `B3`, `B7`, `B8`, `R5`, `R10`, and performance docs define backend/runtime
  support.
- `SAFE9` and `SAFE15` define numeric safety and proof artifacts.

## Outputs and Artifacts

- Scientific/numeric domain manifest.
- EFIR graph.
- EML expression artifact.
- Approximation certificate.
- Interval or error proof.
- Numeric mode manifest.
- Generated native or GPU kernel.
- Benchmark and accuracy report.
- Numeric conformance report.
- Scientific/numeric diagnostics.

## Domain Manifest

```clojure
{:domain :scientific-numeric
 :profiles #{:core :native :gpu :formal}
 :backends #{:llvm :gpu}
 :artifacts #{:efir-graph :eml-expression :math-certificate
              :numeric-conformance :benchmark-report}
 :examples #{:oscillator :simulation-step :activation-function
             :symbolic-rewrite}
 :rejects #{:uncertified-approximation :proofless-fast-math
            :domain-gap :invalid-symbolic-equality}}
```

## Replacement Scope

Gravity should replace:

- elementary function kernels,
- simulation update steps,
- numeric transforms and reductions,
- symbolic simplification slices,
- certified approximation generation,
- native/GPU numeric kernels,
- reproducible benchmark/accuracy reports.

Large external numeric libraries remain provider boundaries until Gravity
implements equivalent certified kernels.

## Minimum End-to-End Slice

The first complete slice is a certified sine approximation:

- Gravity source declares input domain and max error.
- EFIR captures semantic function with branch/domain metadata.
- Approximation system emits certificate.
- LLVM/GPU backend emits kernel without unauthorized fast-math flags.
- Conformance compares reference implementation and checks error bounds.

## Diagnostics

Scientific/numeric diagnostics use `DOM12` identifiers:

- `DOM12-DOMAIN` for missing or invalid numeric domain.
- `DOM12-MODE` for missing precision, rounding, branch, or approximation mode.
- `DOM12-CERTIFICATE` for approximation without certificate or invalid error
  bound.
- `DOM12-REWRITE` for symbolic rewrite without equivalence proof.
- `DOM12-FASTMATH` for backend flags not justified by numeric mode.
- `DOM12-INTEROP` for numeric provider/FFI boundary without mode mapping.
- `DOM12-BENCHMARK` for missing accuracy or performance context.
- `DOM12-CONFORMANCE` for missing reference or error-bound evidence.

Diagnostics must include function/kernel id, source span, domain, numeric mode,
target, certificate/proof id, missing evidence, and remediation.

## Rejected Designs

Gravity rejects fast approximations without declared domain and error.

Gravity rejects backend fast-math as a silent optimization.

Gravity rejects symbolic rewrites without semantic proof.

Gravity rejects numeric provider calls without mode and boundary manifests.

Gravity rejects accuracy claims without reference evidence.

## Conformance Criteria

A conforming scientific/numeric slice must demonstrate:

- EFIR semantic graphs and EML proof/search artifacts,
- certified approximation fixtures,
- strict and relaxed numeric mode tests,
- symbolic rewrite proof fixtures,
- native/GPU kernel emission with proof-gated metadata,
- benchmark and accuracy reports,
- rejection of uncertified approximations and proofless fast math.
