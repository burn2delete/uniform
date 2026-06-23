# MATH4 - EML Normalization & Search Design

Sequence: 72
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

EML is Gravity's elementary-math normalization and search representation. EFIR
remains the semantic artifact. EML is entered only when the compiler, proof
engine, or math tool wants a compact basis for normalization, equivalence
search, approximation synthesis, or rewrite discovery.

The EML basis is:

```text
eml(x, y) = exp(x) - log(y)
```

with constants, variables, arithmetic combinators, complex intermediates, domain
guards, and branch metadata. EML tree identity is never semantic equality.

## Requirements

- EML lowering must start from verified EFIR.
- EML artifacts must retain the originating EFIR graph id, node map, domain
  environment, numeric mode, precision contract, and branch policy.
- Every normalization step must record its rule id, premises, introduced
  assumptions, source or generated span, and invalidated facts.
- EML search must return candidates with ranking and proof obligations; it must
  not return accepted compiler rewrites by itself.
- Equality requires an accepted symbolic proof, interval proof, SMT/prover
  result, certified approximation bound, or profile-approved proof artifact.
- Complex intermediates must be explicit whenever real-domain expressions are
  transformed through complex-valued identities.
- Branch policy must be checked before a normalized or discovered expression can
  replace source semantics.

## Dependencies

- `MATH3` defines EFIR and the semantic anchors that EML must preserve.
- `MATH5` defines certified approximations produced from EML search.
- `MATH6` defines interval and real-domain proof checking.
- `MATH7` and `MATH8` define numeric modes, precision contracts, and floating
  behavior for accepted rewrites.
- `MATH9` defines symbolic rewrite rule registration and proof obligations.
- `SAFE15` defines machine-checkable proof artifact requirements.

## Outputs and Artifacts

An EML run produces:

- EML expression tree or DAG.
- EFIR-to-EML node correspondence.
- Domain and codomain environment.
- Branch-policy ledger.
- Normalization trace.
- Search-space manifest.
- Candidate list with ranking and rejection reasons.
- Proof requests or accepted proof artifact references.
- Diagnostics for lost facts, branch mismatches, and unproved candidates.

## EML Expression Model

The required abstract model is:

```clojure
{:ir :gravity/eml
 :basis :exp-minus-log
 :source-efir graph-id
 :numeric-mode :certified-approx
 :precision {:type :F64 :max-error 1e-12}
 :domain {x {:real [-1.0 1.0]}}
 :branch-policy {:log :principal
                 :sqrt :principal
                 :complex-intermediates :allowed}
 :expr {:op :eml
        :x {:op :mul :args [0.5 {:var x}]}
        :y {:const 1}}
 :node-map [{:efir :n7 :eml :e12 :span source-span}]
 :proof-state :candidate}
```

Implementations may store EML differently, but they must expose equivalent
semantic fields to the verifier.

## Normalization Pipeline

An EML normalization run has these stages:

1. `efir-read`: import a verified EFIR graph and reject missing domain,
   precision, branch, or source facts.
2. `basis-introduce`: express supported elementary operations in the EML basis
   while recording rule premises.
3. `domain-transport`: map EFIR domains into EML variables and guards.
4. `branch-transport`: attach branch policy to every operation whose meaning
   depends on branch choice.
5. `algebraic-normalize`: apply registered symbolic rewrites under their guards.
6. `complex-simplify`: simplify complex intermediates only when the final domain
   and branch policy remain valid.
7. `candidate-emit`: emit normalized candidates with proof obligations.
8. `trace-check`: replay the trace with an independent checker.

No stage may discard a source span, generated-origin chain, domain guard, branch
condition, numeric mode, precision contract, or pending proof obligation.

## Normalization Trace

A trace entry has this shape:

```clojure
{:step 17
 :rule :log-exp-principal
 :before expr-a
 :after expr-b
 :premises [{:fact :domain :value {:x {:real [0.0 :inf]}}}
            {:fact :branch :value {:log :principal}}]
 :introduced-assumptions []
 :invalidated #{:raw-cost-estimate}
 :proof-obligation :none
 :source {:efir-node :n4 :span source-span}}
```

Trace replay must be deterministic. If a rule depends on heuristic choice,
target cost, or provider capability, that choice must appear in the trace.

## Search Space Manifest

Search is bounded and auditable. A search manifest includes:

```clojure
{:grammar {:basis :exp-minus-log
           :max-depth 6
           :constants [0 1 2 :pi :e]
           :operators [:add :sub :mul :div :eml :neg]}
 :domain {x {:real [-1.0 1.0]}}
 :objective {:kind :equivalence-or-approximation
             :metric [:proof-cost :runtime-cost :error-bound]}
 :fuel {:candidate-limit 20000
        :time-ms 500}
 :ranking {:primary :proof-simplicity
           :secondary :estimated-runtime}
 :pruning-rules [:domain-empty :branch-mismatch :type-invalid]}
```

The manifest is part of the build artifact when search affects generated code.
Two builds with the same source, profile, target, provider set, and manifest
must either select the same accepted candidate or report a deterministic tie.

## Candidate Lifecycle

Candidates move through explicit states:

- `generated`: produced by grammar search or normalization.
- `typed`: expression has valid numeric family and shape.
- `domain-checked`: domains and codomains are compatible.
- `branch-checked`: branch policy is compatible with EFIR.
- `proof-requested`: proof obligations have been issued.
- `proved`: exact equivalence was accepted.
- `bounded`: bounded approximation was accepted.
- `rejected`: candidate cannot be used; reason is recorded.

Only `proved` and `bounded` candidates may influence lowering. `bounded`
candidates must carry the approximation certificate required by `MATH5`.

## Branch and Complex Handling

EML may introduce complex intermediates for real source expressions. This is
legal only when:

- the EFIR branch policy allows complex intermediates,
- introduced branch choices are recorded,
- the final expression is valid for the declared real or complex domain, and
- a proof artifact shows that the replacement preserves semantics or stays
  within the accepted error bound.

Principal-branch identities such as `log(exp(x)) = x` are guarded identities,
not unconditional rewrites. The guard must mention domain, branch, and periodic
conditions.

## Proof Boundary

EML is a proof input, not a proof result. Accepted evidence can come from:

- symbolic rewrite proof replay,
- interval proof over a declared real domain,
- complex-domain proof with explicit branch conditions,
- SMT or theorem prover artifact accepted by the proof profile,
- certified approximation certificate,
- provider-specific certificate checked by a Gravity verifier.

The optimizer must preserve the EFIR semantic anchor even after proof acceptance
so diagnostics can explain which source expression was transformed.

## Diagnostics

EML diagnostics use `MATH4` identifiers:

- `MATH4-EFIR` for lowering from unverified or incomplete EFIR.
- `MATH4-BASIS` for unsupported basis introduction.
- `MATH4-DOMAIN` for lost or inconsistent domain facts.
- `MATH4-BRANCH` for branch-policy mismatch.
- `MATH4-COMPLEX` for untracked complex intermediates.
- `MATH4-TRACE` for unreplayable normalization traces.
- `MATH4-SEARCH` for nondeterministic or unbounded search.
- `MATH4-CANDIDATE` for candidate use before proof acceptance.
- `MATH4-PROOF` for missing or rejected proof artifacts.

Diagnostics must include EFIR graph id, EML artifact id, rule id when relevant,
source span, domain, branch policy, numeric mode, precision contract, candidate
state, and the proof required to proceed.

## Rejected Designs

Gravity rejects using EML as the mandatory runtime representation.

Gravity rejects treating EML tree identity as equality.

Gravity rejects heuristic search that affects code generation without a manifest.

Gravity rejects branch-sensitive identities without branch guards.

Gravity rejects normalization traces that cannot be replayed.

Gravity rejects accepted math rewrites that lose EFIR source anchors.

## Conformance Criteria

A conforming EML implementation must demonstrate:

- EFIR-to-EML lowering that preserves semantic facts.
- Replayable normalization traces.
- Bounded search with deterministic manifests.
- Candidate rejection for branch and domain mismatches.
- Candidate promotion only after accepted proof or bounded-error evidence.
- Complex-intermediate handling with explicit branch policy.
- Diagnostics for every rejected or unproved optimization path.
