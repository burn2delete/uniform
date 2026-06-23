# C12 - Domain IR Architecture

Sequence: 91
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Domain IRs let Gravity specialize important domains without fragmenting the
language. EFIR, schema IR, workflow graph IR, HDL/state-machine IR, query IR,
GPU kernel IR, AI agent manifests, and similar representations are compiler
artifacts with semantic anchors, verifiers, pass contracts, and target-lowering
rules.

Domain IRs are not separate languages. They refine typed core or MIR semantics
for a specific domain and must prove or certify any transformation before target
emission.

## Requirements

- Every domain IR must register its schema, owner document, semantic anchor,
  entry passes, exit passes, verifier, supported profiles, target lowerings,
  optimization rules, and diagnostics.
- Domain IR artifacts must reference source syntax and typed-core or MIR nodes.
- Domain-specific optimization must emit proof, certificate, translation
  validation, or typed MIR equivalence evidence before lowering.
- Domain IRs must carry type, effect, ownership, capability, profile, target,
  safety, and provenance facts needed by downstream passes.
- Backends may consume domain IR only through registered lowering paths.
- Domain IRs must provide fallback, rejection, or residual MIR behavior when a
  target lacks support.
- Domain IR registration must be stable enough for plugins and self-hosting.

## Dependencies

- `C11` defines MIR and domain anchors.
- `MATH3` defines EFIR; `MATH4` defines the adjacent EML proof/search
  representation.
- Schema, workflow, AI, hardware, GPU, query, and interop documents define their
  domain-specific IR contracts.
- `SAFE15` defines proof/certificate artifacts.
- `PERF1` and `PERF10` define optimization evidence and check-elision rules.
- Backend documents define target-lowering capabilities.

## Outputs and Artifacts

- Domain IR registry.
- Domain IR artifact schema.
- Semantic anchor map.
- Entry and exit pass records.
- Domain verifier report.
- Proof and certificate references.
- Lowering eligibility matrix.
- Fallback or rejection records.
- Domain diagnostics.

## Registration Record

```clojure
{:artifact :gravity/domain-ir-registration
 :domain :efir
 :owner-doc :MATH3
 :schema schema-hash
 :semantic-anchor #{:typed-core-node :mir-op}
 :entry-passes [:elementary-detect :efir-build]
 :exit-passes [:efir-to-mir :efir-to-target-provider]
 :verifier :gravity.math/verify-efir
 :supported-profiles #{:core :native :gpu :formal}
 :target-lowerings #{:llvm :wasm :gpu}
 :proof-obligations #{:domain :branch-policy :numeric-mode :roundoff}
 :fallback :mir-provider-call}
```

Registrations are package-visible artifacts when plugins or libraries introduce
new domain IRs.

## Domain Artifact Shape

```clojure
{:artifact :gravity/domain-ir
 :domain :workflow
 :artifact-id workflow-ir-hash
 :source {:syntax-id syntax-id
          :span source-span
          :origin-chain origin-chain}
 :semantic-anchor {:mir-ops [op-id]
                   :typed-core [core-node-id]}
 :profile :distributed
 :target-request :workflow-runtime
 :facts {:types type-table-id
         :effects effect-table-id
         :capabilities capability-table-id
         :safety safety-table-id}
 :verifier {:name :gravity.workflow/verify
            :result :accepted}
 :proofs []
 :lowering-status :eligible}
```

The required domain payload lives under a schema owned by the domain document.

## Entry Passes

Entry passes detect and construct domain IR from:

- source forms after macro expansion,
- typed core subgraphs,
- MIR subgraphs,
- provider declarations,
- package manifests,
- schema declarations,
- compiler plugin output.

Entry passes must record what source and IR facts were consumed and what facts
were preserved, invalidated, or translated.

## Exit Passes

Exit passes return from domain IR to:

- MIR subgraphs,
- target provider calls,
- backend-specific IR,
- runtime manifests,
- generated code,
- package artifacts,
- verification artifacts.

An exit pass is legal only after the domain verifier accepts the artifact and
the pass emits equivalence, proof, certificate, or accepted-lowering evidence.

## Domain Verifier

A domain verifier checks:

- schema validity,
- semantic anchor validity,
- source provenance,
- type/effect/capability/profile compatibility,
- domain-specific invariants,
- safety and proof obligations,
- target-lowering preconditions,
- fallback legality,
- diagnostic completeness.

Verifier failures prevent target emission for that domain artifact.

## Optimization Boundary

Domain optimizers may use specialized representations and heuristics. Their
results become compiler-visible only through:

- proof replay,
- certificate validation,
- translation validation against MIR or typed core,
- conformance fixture acceptance for nonsemantic choices,
- explicit rejection with diagnostics.

Search results, benchmarks, or provider claims alone are not correctness
evidence.

## Standard Domain IR Families

Baseline domain IR families include:

- EFIR for elementary math, with EML as its proof/search representation,
- schema IR for data contracts and validators,
- workflow graph IR for durable distributed execution,
- AI agent/tool manifest IR for model and tool orchestration,
- HDL/state-machine IR for hardware,
- GPU kernel IR for accelerator code,
- query IR for database and data access,
- FFI boundary IR for foreign interfaces,
- package/artifact graph IR for build and release metadata.

Each family owns its detailed schema in its phase document.

## Diagnostics

Domain IR diagnostics use `C12` identifiers:

- `C12-REGISTRATION` for malformed domain registration.
- `C12-ANCHOR` for missing or invalid semantic anchors.
- `C12-SCHEMA` for invalid domain payload schema.
- `C12-FACTS` for lost type/effect/capability/safety facts.
- `C12-VERIFY` for domain verifier failure.
- `C12-PROOF` for optimization without accepted evidence.
- `C12-LOWERING` for unsupported target lowering.
- `C12-FALLBACK` for missing or illegal fallback behavior.
- `C12-PLUGIN` for plugin-provided domain IR outside policy.

Diagnostics must include domain, artifact id, source span, semantic anchor, owner
document, profile, target, verifier, missing fact, and remediation.

## Rejected Designs

Gravity rejects domain IRs as separate untyped languages.

Gravity rejects domain artifacts without typed-core or MIR anchors.

Gravity rejects backend consumption of unverified domain IR.

Gravity rejects domain-specific optimization without proof, certificate, or
translation validation.

Gravity rejects plugins that introduce opaque domain payloads with no verifier.

## Conformance Criteria

A conforming domain IR system must demonstrate:

- registration and verification for at least EFIR, schema IR, workflow IR, and
  one target-oriented domain IR,
- semantic anchors from source through MIR,
- domain optimization accepted with proof or certificate,
- domain optimization rejected without evidence,
- target lowering eligibility and fallback records,
- plugin registration policy checks,
- diagnostics for missing anchors, invalid schema, failed verifier, and
  unsupported target.
