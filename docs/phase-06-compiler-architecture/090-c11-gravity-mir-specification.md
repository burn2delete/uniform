# C11 - Gravity MIR Specification

Sequence: 90
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity MIR is the target-independent, typed, effect-annotated, profile-valid
middle IR used for verification, optimization, domain IR anchoring, and target
lowering. It is small enough to verify and explicit enough to preserve source,
type, effect, ownership, capability, safety, proof, and profile facts.

MIR is not a backend IR. Target-specific instruction selection, ABI layout,
register allocation, device code, circuit synthesis, and host runtime calls are
lowering artifacts that consume MIR.

## Requirements

- Every MIR module must reference checked core, type facts, effect facts,
  ownership facts, safety outcomes, profile, target, and source provenance.
- Every MIR operation must have opcode, operands, result, type, effect set,
  source or generated origin, profile, and verifier status.
- Safety-sensitive MIR operations must reference safety outcome, runtime check,
  proof, or unsafe audit records.
- Effectful operations must carry ordering constraints.
- MIR must represent calls, closures, control flow, loops, matches,
  allocations, regions, resources, errors, FFI, atomics, concurrency, workflow
  suspension, AI/tool calls, and domain anchors.
- Domain-specific behavior may leave MIR through a documented domain IR anchor;
  it must not become opaque target-specific MIR.
- MIR transformations must preserve or invalidate facts explicitly.

## Dependencies

- `C6` provides core AST.
- `C7`, `C8`, `C9`, and `C10` provide type, effect, ownership, and safety facts.
- `C12` defines domain IR anchors.
- `C13` defines MIR optimization passes.
- `C14` lowers MIR to target artifacts.
- `PERF10` defines check-elision proof requirements.
- Backend, runtime, workflow, AI, and hardware documents consume MIR anchors.

## Outputs and Artifacts

- MIR module.
- Control-flow graph.
- Data-flow graph.
- Type, effect, ownership, capability, and safety metadata tables.
- Source and generated-origin map.
- Domain anchor table.
- MIR verifier report.
- MIR diagnostic stream.

## Module Shape

```clojure
{:artifact :gravity/mir-module
 :module module-id
 :source-core checked-core-hash
 :profile :native
 :target-request target-id
 :functions {fn-id function-record}
 :globals {global-id global-record}
 :types type-table-id
 :effects effect-table-id
 :ownership ownership-table-id
 :safety safety-table-id
 :domain-anchors domain-anchor-table-id
 :diagnostics []}
```

The target request can constrain MIR legality, but MIR op semantics remain
target-independent.

## Function and Block Model

```clojure
{:fn-id fn-hash
 :name 'app/sum
 :params [value-id]
 :returns type-id
 :latent-effects #{}
 :blocks {entry-block-id block-record}
 :entry entry-block-id
 :source origin-chain}
```

Blocks contain ordered operations and end in one terminator:

- branch,
- conditional branch,
- switch or match decision,
- return,
- throw,
- panic,
- unreachable with proof,
- suspend or yield for workflow/runtime profiles,
- tail call when legal.

Phi-like block arguments or SSA values are allowed as the representation of
control-flow joins. The public artifact must expose data-flow edges.

## Operation Schema

```clojure
{:op-id op-hash
 :opcode :call
 :operands [callee-id arg-id-1 arg-id-2]
 :result result-id
 :type result-type
 :effects #{:network/http}
 :ordering :sequence
 :source {:core-node core-node-id
          :span source-span
          :origin-chain origin-chain}
 :profile :native
 :facts {:ownership ownership-ref
         :capabilities capability-ref
         :safety safety-outcome-ref
         :proofs [proof-id]}
 :domain-anchor nil}
```

Operations with no result use `:result nil` and type `Unit` or bottom where
appropriate.

## Operation Families

Required MIR families:

- constants and literals,
- locals and block arguments,
- function calls and tail calls,
- closure creation and application,
- direct, dictionary, vtable, and dynamic dispatch,
- records, structs, tuples, enums, and tagged unions,
- field, index, slice, and buffer operations,
- arithmetic and numeric conversions,
- memory allocation, initialization, load, store, move, and drop,
- region and arena enter, allocation, reset, and exit,
- linear acquire, transfer, and terminal operations,
- control flow, loops, and match decision trees,
- error operations: throw, try edges, panic, result construction,
- FFI and host interop boundary operations,
- atomics, locks, channels, task spawn, and synchronization,
- workflow suspend/resume/event operations,
- AI model/tool/memory/policy operations,
- domain anchor operations,
- runtime check operations,
- proof assertion and certificate reference operations.

Backends may lower a family only when profile and target support the family or a
documented runtime/provider implements it.

## Metadata Tables

MIR stores repeated facts in tables:

- type table,
- effect table,
- ownership and lifetime table,
- capability proof table,
- safety outcome table,
- runtime check table,
- proof and certificate table,
- source-origin table,
- profile and target table,
- domain anchor table.

Operations reference these tables by id. A pass may rewrite operations but must
preserve references or emit invalidation records.

## Domain Anchors

A domain anchor references an external semantic IR such as EFIR, schema IR,
workflow graph IR, HDL state-machine IR, query IR, or GPU kernel IR:

```clojure
{:domain :efir
 :anchor-id anchor-hash
 :mir-ops [op-id]
 :semantic-artifact efir-graph-id
 :equivalence-proof proof-id
 :fallback mir-subgraph-id}
```

The MIR verifier rejects unanchored domain artifacts.

## Runtime Checks

Runtime checks are MIR operations with safety outcome references. They cannot be
removed by generic dead-code elimination. Check elision requires `PERF10`
records and proof dominance.

## MIR Verification

The verifier checks:

- every block terminates,
- operand definitions dominate uses,
- types match operation schemas,
- effect ordering constraints are present,
- safety-sensitive operations reference safety outcomes,
- source origins are valid,
- profile constraints are attached,
- ownership and resource operations match C9 facts,
- runtime checks guard the intended operation,
- domain anchors are valid,
- no target-specific opcodes appear outside target-lowering metadata.

Verifier failure prevents optimization and target lowering.

## Diagnostics

MIR diagnostics use `C11` identifiers:

- `C11-MODULE` for malformed module records.
- `C11-BLOCK` for invalid control-flow blocks.
- `C11-DOMINANCE` for use before definition.
- `C11-TYPE` for type mismatch.
- `C11-EFFECT` for missing effect or ordering facts.
- `C11-SAFETY` for missing safety outcome or check reference.
- `C11-ORIGIN` for missing source or generated origin.
- `C11-DOMAIN` for invalid domain anchors.
- `C11-TARGET-LEAK` for target-specific operations in generic MIR.
- `C11-VERIFY` for verifier failures not covered above.

Diagnostics must include MIR module, function, block, operation id, source span,
origin chain, profile, target request, missing fact, and remediation.

## Rejected Designs

Gravity rejects MIR without type and effect annotations.

Gravity rejects target-specific instruction semantics in generic MIR.

Gravity rejects optimization over MIR that lacks safety outcome references.

Gravity rejects domain IRs with no MIR or typed-core anchor.

Gravity rejects source maps that stop at macro expansion and do not reach MIR.

## Conformance Criteria

A conforming MIR implementation must demonstrate:

- module, function, block, and operation serialization,
- verifier acceptance for valid typed/effected/safe MIR,
- verifier rejection for malformed control flow, missing types, missing effects,
  missing safety outcomes, and invalid origins,
- operation coverage for calls, closures, allocation, regions, resources,
  errors, concurrency, FFI, workflow, AI, and domain anchors,
- runtime check preservation,
- domain anchor round-trip,
- optimization invalidation hooks,
- target-lowering input validation.
