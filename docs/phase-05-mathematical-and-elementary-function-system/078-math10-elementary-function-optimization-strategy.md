# MATH10 - Elementary Function Optimization Strategy

Sequence: 78
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Elementary-function optimization turns ordinary source math into profile-legal
runtime implementations while preserving EFIR semantics. The optimizer may
select libm calls, hardware instructions, symbolic rewrites, fused kernels,
polynomial or rational approximations, lookup tables, SIMD kernels, GPU kernels,
or generated verified code.

Correctness is anchored in EFIR, branch policy, numeric mode, precision
contract, proof artifacts, and certificates. Benchmarks and autotuning rank only
the candidates that have already passed semantic legality.

## Requirements

- Optimization must start from verified EFIR graphs.
- Detection of elementary subgraphs must preserve source spans, generated-origin
  chains, domains, branch policies, and numeric modes.
- EML normalization and symbolic rewrites may propose candidates but cannot
  prove correctness by tree identity.
- Whole-expression fusion is legal only when the proof or approximation
  certificate covers the fused EFIR graph.
- Provider selection must consider profile, target, target features, vector
  width, memory layout, cache behavior, effects, allocation, branch policy,
  numeric mode, certificates, and benchmark evidence.
- SIMD and GPU lowerings must reference lane, target, and numeric certificates.
- Autotuning may choose among legal variants but must emit a reproducible
  selection record and deterministic fallback policy.
- Runtime checks may be removed only under `PERF10` and `SAFE15` evidence.

## Dependencies

- `MATH2` defines elementary functions and provider eligibility.
- `MATH3` defines EFIR.
- `MATH4` defines EML normalization and search.
- `MATH5` defines certified approximations.
- `MATH6` defines interval and real proof checking.
- `MATH7` and `MATH8` define modes and floating semantics.
- `MATH9` defines symbolic rewrites.
- `PERF5`, `PERF7`, `PERF8`, and `PERF10` define benchmark, multiversioning,
  SIMD/cache, and check-elision evidence.
- Backend and profile documents define target lowering constraints.

## Outputs and Artifacts

- Elementary subgraph detection report.
- Fused EFIR graph.
- Candidate implementation set.
- EML and rewrite candidate references.
- Provider eligibility report.
- Certificate and proof references.
- Autotune or benchmark evidence.
- Selected lowering decision record.
- Rejected candidate report.
- Backend lowering map.

## Optimization Pipeline

The optimizer runs this sequence:

1. `detect-elementary`: find elementary subgraphs in typed core or MIR.
2. `build-efir`: construct EFIR with source and generated-origin anchors.
3. `infer-context`: infer domains, codomains, branch policies, numeric modes,
   precision contracts, floating manifests, and profile constraints.
4. `normalize-or-search`: optionally run EML and symbolic rewrite search.
5. `generate-candidates`: produce provider, fused, approximate, SIMD, GPU, or
   generated-code candidates.
6. `prove-or-certify`: validate equality, bounded error, branch behavior,
   roundoff, and target assumptions.
7. `rank`: rank only legal candidates by objective.
8. `select`: emit selected-lowering record and fallback.
9. `lower`: lower through MIR, domain IR, or backend IR while preserving
   semantic anchors.
10. `verify-lowering`: recheck certificate assumptions and backend feature
    guards.

Each stage declares preserved and invalidated facts.

## Candidate Families

Candidate implementation families include:

- direct library call,
- target hardware instruction,
- provider intrinsic,
- symbolic rewrite to simpler elementary form,
- fused elementary graph,
- polynomial approximation,
- rational approximation,
- table or table-plus-polynomial approximation,
- piecewise approximation,
- SIMD vector kernel,
- GPU or accelerator kernel,
- generated verified code,
- residual checked implementation.

Each candidate records which EFIR graph it implements and which proof artifacts
would be required for acceptance.

## Decision Record

```clojure
{:artifact :gravity/elementary-optimization-decision
 :decision-id decision-hash
 :efir graph-hash
 :source-spans [source-span]
 :profile :native
 :target {:triple :x86_64-linux
          :features #{:avx2 :fma}}
 :numeric-mode :certified-approx
 :precision {:absolute-error-max 1.0e-8}
 :domain {x {:real [0.0 0.1]}}
 :branch-policy {:log :principal}
 :objective {:primary :latency
             :secondary :code-size}
 :candidates [{:id :libm-pair
               :status :legal
               :cost {:latency-ns 37}}
              {:id :fused-poly-7
               :status :legal
               :certificate cert-hash
               :cost {:latency-ns 6}}
              {:id :hardware-native
               :status :rejected
               :reason :missing-error-bound}]
 :selected :fused-poly-7
 :proofs [cert-hash interval-proof-hash]
 :benchmarks [bench-hash]
 :fallback :libm-pair}
```

The decision id is content-derived from semantic inputs, candidate set,
objective, target fingerprint, and selected candidate.

## Fusion Rules

Fusion may combine multiple elementary calls when:

- the combined EFIR graph is verified,
- source domains and branch policies are compatible,
- intermediate exceptional behavior is either unobservable or preserved,
- the certificate covers the whole fused graph,
- floating manifests allow any reassociation or FMA contraction used,
- diagnostics can still point back to the source operations.

Independent per-call certificates do not prove a fused implementation unless a
combination proof accounts for intermediate and roundoff behavior.

## Ranking and Autotuning

Ranking considers:

- declared objective,
- profile constraints,
- target fingerprint,
- benchmark evidence,
- code size,
- latency and throughput,
- memory and table footprint,
- cache behavior,
- vector width,
- startup cost,
- deterministic latency requirements.

Autotuning can run at build time, install time, or profile-guided rebuild time.
It must never select a semantically illegal candidate. If the target fingerprint
does not match the selected variant's assumptions, dispatch uses a legal
fallback.

## SIMD and GPU Lowering

SIMD and GPU elementary lowerings require:

- lane independence and bounds evidence,
- target feature guards,
- vector or device floating manifest,
- certificate covering lane behavior and divergence behavior,
- memory layout and transfer assumptions,
- fallback for unsupported devices unless the profile forbids fallback.

Device-native elementary functions are providers. They are not automatically
eligible merely because they exist on the target.

## Runtime Checks and Fallbacks

Optimized implementations may include residual checks for domains, target
features, table bounds, or fallback dispatch. Residual checks are part of the
selected implementation. Erasing them requires proof or certificate evidence.

Fallbacks must satisfy the same source-level semantic contract, though they may
have a different performance class.

## Diagnostics

Elementary optimization diagnostics use `MATH10` identifiers:

- `MATH10-DETECT` for lost source or generated-origin anchors.
- `MATH10-EFIR` for optimization from unverified EFIR.
- `MATH10-CANDIDATE` for malformed candidate records.
- `MATH10-PROOF` for candidates selected without required proof.
- `MATH10-CERTIFICATE` for missing or mismatched approximation certificates.
- `MATH10-FUSION` for whole-expression proof gaps.
- `MATH10-PROVIDER` for provider eligibility failures.
- `MATH10-SIMD` for lane or target certificate gaps.
- `MATH10-GPU` for device lowering gaps.
- `MATH10-AUTOTUNE` for unreproducible selection.
- `MATH10-FALLBACK` for missing legal fallback when required.

Diagnostics must include EFIR graph id, candidate id, selected provider, source
span, profile, target fingerprint, numeric mode, precision contract, missing
proof or certificate, and fallback status.

## Rejected Designs

Gravity rejects elementary optimization from raw syntax without EFIR.

Gravity rejects benchmark-only correctness decisions.

Gravity rejects fused approximations without whole-expression certificates.

Gravity rejects target-native elementary calls that bypass provider eligibility.

Gravity rejects autotuning that cannot replay its candidate set and selection.

Gravity rejects SIMD or GPU fast math without explicit numeric contracts.

## Conformance Criteria

A conforming elementary optimizer must demonstrate:

- elementary subgraph detection and EFIR preservation,
- legal and rejected provider candidates,
- EML and symbolic candidate generation without equality claims,
- accepted fused approximation with whole-expression certificate,
- rejected fused approximation with only per-call certificates,
- SIMD and GPU lowering with target guards,
- autotune selection replay,
- fallback dispatch behavior,
- diagnostics for missing proof, illegal provider, and unreproducible ranking.
