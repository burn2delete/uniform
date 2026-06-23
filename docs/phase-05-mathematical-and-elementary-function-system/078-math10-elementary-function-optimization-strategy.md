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
- Correctly rounded candidates must declare input representation, output
  representation, rounding mode, tie policy, branch policy, and exceptional
  value behavior.
- Correctly rounded generation must construct accepted-result intervals from
  exact EFIR values rounded to the declared representation and rounding mode.
- Polynomial, rational, table, and fused kernels that claim correct rounding
  must prove that coefficient representation, evaluation order, range
  reduction, reconstruction, and backend lowering keep every result inside the
  accepted-result interval.
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
- Correct-rounding target manifest.
- Correctly rounded interval-generation ledger.
- Synthesis constraint transcript.
- Provider comparison matrix.
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
5. `generate-candidates`: produce provider, fused, correctly rounded,
   approximate, SIMD, GPU, or generated-code candidates.
6. `prove-or-certify`: validate equality, bounded error, accepted-result
   intervals, branch behavior, roundoff, and target assumptions.
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
- correctly rounded generated polynomial, rational, table, or fused kernel,
- piecewise approximation,
- SIMD vector kernel,
- GPU or accelerator kernel,
- generated verified code,
- residual checked implementation.

Each candidate records which EFIR graph it implements and which proof artifacts
would be required for acceptance.

## Correctly Rounded Generation

Gravity supports RLibm-style generation where an approximation kernel is
synthesized to return the exact EFIR result rounded to a declared representation
and rounding mode. Such kernels are approximation strategies, but their
observable contract is `:correctly-rounded` rather than an absolute-error-only
`:certified-approx` contract.

A correct-rounding target records the representation and rounding obligations:

```clojure
{:artifact :gravity/correct-rounding-target
 :target-id target-hash
 :efir graph-hash
 :function :exp
 :input-representation {:type :F32
                        :domain :all-finite}
 :output-representation {:type :F32}
 :rounding-modes [:nearest-even]
 :tie-policy :nearest-even
 :branch-policy {:complex-intermediates :forbidden}
 :exceptional-values {:nan :propagate
                      :inf :domain-error
                      :signed-zero :preserve}
 :target-assumptions {:evaluation-format :F64
                      :contract-fma false
                      :denormals :preserved}}
```

For each representable input or proof cell, interval generation computes the
exact EFIR result, rounds it according to the target record, and derives the
open or closed interval of real approximation values that would round to the
same output. The ledger records tie cases, subnormal boundaries, signed-zero
rules, exceptional paths, and any cell splitting used to avoid unresolved
rounding boundaries.

When a kernel supports multiple output representations or rounding modes, the
candidate must either prove the intersection of all accepted-result intervals
or emit separate kernels with separate certificates. A provider that is
correctly rounded only for `:nearest-even` is not eligible for
`:toward-positive`, `:toward-negative`, or `:toward-zero` contracts.

Polynomial, rational, table, and fused synthesis constraints must name:

- basis, degree, partition, and range-reduction form,
- coefficient representation and coefficient rounding,
- evaluation order, FMA policy, reassociation policy, and intermediate format,
- reconstruction steps and table lookup or interpolation rules,
- backend lowering assumptions that can change rounding behavior.

The solver may be heuristic, but the accepted artifact must be checkable. If
the constraint system is infeasible, the optimizer rejects the candidate or
chooses a legal fallback; it must not weaken `:correctly-rounded` into
`:faithful`, `:libm`, or `:certified-approx` without a `MATH7` downgrade record.

Correctly rounded certificates must include the target manifest,
interval-generation ledger, synthesis constraint transcript, coefficient and
table digests, evaluation-roundoff proof, exceptional-path proof, checker
identity, and invalidation conditions. The independent checker verifies that
the generated implementation stays inside every accepted-result interval under
the declared target assumptions.

Provider comparisons are semantic artifacts, not only benchmark tables. The
optimizer compares generated correctly rounded kernels, libm providers,
hardware instructions, vendor intrinsics, and RLibm-style providers by
representation coverage, rounding-mode coverage, domain coverage, branch
policy, exceptional-value behavior, target and version assumptions,
certificate status, fallback behavior, and cost. A faster provider can outrank
a generated kernel only after this comparison proves that it satisfies the same
or a stronger contract.

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
- `MATH10-ROUNDING-TARGET` for incomplete representation or rounding targets.
- `MATH10-ROUNDING-INTERVAL` for missing or unresolved accepted-result
  intervals.
- `MATH10-SYNTHESIS` for unchecked or infeasible correctly rounded synthesis
  constraints.
- `MATH10-FUSION` for whole-expression proof gaps.
- `MATH10-PROVIDER` for provider eligibility failures.
- `MATH10-PROVIDER-COMPARE` for provider rankings that ignore semantic
  comparison fields.
- `MATH10-SIMD` for lane or target certificate gaps.
- `MATH10-GPU` for device lowering gaps.
- `MATH10-AUTOTUNE` for unreproducible selection.
- `MATH10-FALLBACK` for missing legal fallback when required.

Diagnostics must include EFIR graph id, candidate id, selected provider, source
span, profile, target fingerprint, numeric mode, precision contract, rounding
target, interval-generation ledger, synthesis transcript, missing proof or
certificate, provider comparison result, and fallback status.

## Rejected Designs

Gravity rejects elementary optimization from raw syntax without EFIR.

Gravity rejects benchmark-only correctness decisions.

Gravity rejects fused approximations without whole-expression certificates.

Gravity rejects target-native elementary calls that bypass provider eligibility.

Gravity rejects autotuning that cannot replay its candidate set and selection.

Gravity rejects SIMD or GPU fast math without explicit numeric contracts.

Gravity rejects correctly rounded generation that omits representation,
rounding mode, or tie-policy targets.

Gravity rejects treating ulp-bounded or average-error approximations as
correctly rounded without accepted-result interval proof.

Gravity rejects provider comparisons that ignore representation, rounding mode,
branch policy, or exceptional-value behavior.

## Conformance Criteria

A conforming elementary optimizer must demonstrate:

- elementary subgraph detection and EFIR preservation,
- legal and rejected provider candidates,
- EML and symbolic candidate generation without equality claims,
- accepted fused approximation with whole-expression certificate,
- rejected fused approximation with only per-call certificates,
- accepted correctly rounded generated kernel with target manifest,
  interval-generation ledger, synthesis transcript, and checker certificate,
- rejected correctly rounded candidate with unresolved rounding intervals or
  unsupported rounding mode,
- semantic comparison between generated, libm, hardware, and RLibm-style
  providers,
- SIMD and GPU lowering with target guards,
- autotune selection replay,
- fallback dispatch behavior,
- diagnostics for missing proof, illegal provider, unresolved rounding
  interval, unchecked synthesis constraints, and unreproducible ranking.
