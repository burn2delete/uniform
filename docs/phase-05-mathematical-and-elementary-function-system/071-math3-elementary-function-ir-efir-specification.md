# MATH3 - Elementary Function IR - EFIR Specification

Sequence: 71
Phase: 5 - Mathematical and Elementary Function System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

EFIR is Gravity's semantic IR for elementary-function reasoning. It captures
elementary expression graphs, domains, codomains, numeric modes, precision
contracts, branch policies, source spans, generated origins, proof obligations,
and runtime selection anchors.

EFIR is the boundary between source math and replaceable implementations. Libm,
hardware instructions, approximations, lookup tables, SIMD kernels, GPU kernels,
and generated code are selected behind EFIR.

## Requirements

- EFIR nodes must represent constants, variables, arithmetic, elementary calls,
  domain constraints, branch policy, and source spans.
- Every graph must carry numeric mode, precision contract, domain environment,
  source anchors, and proof obligations.
- EFIR must support real, complex, interval, symbolic, fixed-width, and floating
  domains as required by the numeric mode.
- Optional EML lowering must preserve semantic anchors and record introduced
  branch assumptions.
- Backends must not consume bare EFIR as executable code; they consume selected
  implementations tied to EFIR.

## Dependencies

- `MATH1` defines numeric families.
- `MATH2` defines elementary declarations and providers.
- `MATH4` defines EML lowering.
- `MATH5` through `MATH8` define certificates, intervals, numeric modes, and
  floating semantics.
- `SAFE9` and `SAFE15` define numeric safety and proof artifacts.
- Compiler phases define typed core, MIR, pass contracts, and artifact ids.

## Outputs and Artifacts

- EFIR graph.
- Domain environment.
- Branch policy record.
- Codomain facts.
- Numeric mode and precision contract.
- Proof obligation seed list.
- Source anchor map.
- Runtime implementation anchor.
- EFIR verification diagnostics.

## Node Model

EFIR nodes include:

```clojure
(defunion EFNode
  (Const {:value NumericLiteral
          :family NumericFamily
          :exact? Bool})
  (Var {:name Symbol
        :domain Domain
        :source Span})
  (Arith {:op ArithOp
          :args (Vec EFNode)})
  (Call {:op ElementaryOp
         :args (Vec EFNode)
         :branch BranchPolicy})
  (Let {:bindings (Vec Binding)
        :body EFNode})
  (Piecewise {:cases (Vec Case)
              :else EFNode})
  (Constraint {:predicate Predicate
               :body EFNode})
  (EML {:x EFNode
        :y EFNode
        :branch BranchPolicy}))
```

Implementation schemas may differ, but the artifact must expose equivalent
facts.

## Graph Shape

An EFIR graph contains:

```clojure
{:ir :gravity/efir
 :graph-id graph-hash
 :source-anchors [{:span span :origin :source-or-generated}]
 :nodes [{:id :n1
          :op :sin
          :args [:x]
          :domain domain
          :codomain codomain}]
 :numeric-mode :certified-approx
 :branch-policy :principal
 :precision {:type :F64 :max-error 1e-8}
 :proof-obligations #{:domain-coverage :roundoff :branch-consistency}
 :runtime-anchor :unselected}
```

Graph ids are content-derived over semantic fields, not over incidental
serialization order.

## Domain and Branch Policy

Every node has domain and codomain information. Domains may be real intervals,
complex regions, symbolic predicates, bitvector ranges, floating sets, or
provider-defined proof domains. Branch policy records principal branches,
cut behavior, exceptional values, and complex intermediates.

Missing branch policy is invalid for operations where branch behavior affects
semantics.

## EFIR Pass Contract

Standard EFIR passes:

- `elementary-detect` finds elementary subgraphs in typed core.
- `efir-build` creates graph anchors and source maps.
- `domain-infer` infers domains and codomains.
- `precision-infer` attaches numeric mode and precision contracts.
- `efir-normalize` rewrites within EFIR under proof obligations.
- `efir-to-eml` optionally lowers to EML for proof/search.
- `implementation-select` ties EFIR to runtime providers.
- `certificate-check` validates approximation and roundoff evidence.

Passes must state preserved and invalidated facts.

## Verification

The EFIR verifier rejects graphs missing:

- Domain.
- Codomain.
- Numeric mode.
- Precision contract.
- Branch policy where required.
- Source or generated origin.
- Semantic anchor back to typed core or MIR.
- Proof obligations for approximate or transformed nodes.

The verifier also rejects graph rewrites that claim equality without proof.

## Diagnostics

EFIR diagnostics use `MATH3` identifiers:

- `MATH3-NODE` for invalid node shape.
- `MATH3-DOMAIN` for missing or inconsistent domain.
- `MATH3-CODOMAIN` for missing or inconsistent codomain.
- `MATH3-BRANCH` for missing branch policy.
- `MATH3-PRECISION` for missing precision contract.
- `MATH3-SOURCE` for missing source anchor.
- `MATH3-REWRITE` for unproven semantic rewrite.
- `MATH3-EML` for EML lowering that loses branch or source facts.
- `MATH3-RUNTIME` for implementation selection without EFIR anchor.

Diagnostics must include graph id, node id, source span, operation, numeric mode,
domain, branch policy, and required proof.

## Rejected Designs

Gravity rejects math optimization without a semantic IR.

Gravity rejects EFIR graphs without source anchors.

Gravity rejects treating EML tree identity as equality.

Gravity rejects runtime provider selection without EFIR linkage.

Gravity rejects branch behavior hidden in backend libraries.

## Conformance Criteria

A conforming EFIR implementation must demonstrate:

- EFIR construction from source elementary expressions.
- Domain, codomain, numeric mode, and branch policy verification.
- Source mapping through generated code.
- Optional EML lowering with trace records.
- Rejection of unproven rewrites.
- Runtime provider selection tied to EFIR.
- Certificate checks for approximate implementations.

