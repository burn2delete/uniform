# DOM21 - Low-Code, Visual Programming and Workflow Domain Specification

Sequence: 144
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover low-code, visual programming, and
workflow slices normally built in BPMN tools, Zapier-like automations, node
editors, ETL canvases, spreadsheet workflows, visual AI chains, or proprietary
workflow DSLs.

The replacement scope is visual graph nodes, typed edges, schema-bound forms,
approval nodes, tool/model nodes, generated Gravity source or MIR, workflow
graphs, replay traces, and visual-to-source diagnostics.

## Requirements

- Visual nodes and edges must have typed input/output schemas, effects,
  capabilities, policy, replay behavior, and generated source/provenance links.
- Graph execution paths must expose all side effects, approvals, retries,
  timeouts, compensation, and nondeterminism.
- Generated code must pass ordinary Gravity compiler checks.
- Visual editor metadata must round-trip diagnostics to graph nodes and edges.
- Tool/model nodes require AI/workflow capability policy.
- Schema changes require graph migration or compatibility records.
- Untyped edges, hidden effects, and visual-only authority are rejected.

## Dependencies

- `B10`, `R7`, `R8`, and Phase 11 AI/workflow docs define workflow, replay, AI,
  tool, policy, and approval behavior.
- Phase 10 schema docs define node/edge schemas and migrations.
- `SAFE10`, `SAFE11`, and `SAFE12` define capabilities, taint, and generated code
  safety.
- Tooling docs define visual editor and diagnostic integration.

## Outputs and Artifacts

- Low-code/visual domain manifest.
- Visual graph artifact.
- Generated Gravity source or MIR map.
- Node and edge schema bundle.
- Workflow graph artifact.
- Policy and approval graph.
- Replay trace.
- Visual layout/provenance metadata.
- Graph migration record.
- Visual workflow diagnostics.

## Domain Manifest

```clojure
{:domain :low-code-visual-workflow
 :profiles #{:distributed :ai :hosted}
 :backends #{:workflow-graph :javascript-typescript}
 :artifacts #{:visual-graph :schema-bindings :generated-source-map
              :approval-policy :replay-trace}
 :examples #{:support-flow :etl-pipeline :approval-flow :ai-tool-chain}
 :rejects #{:untyped-edge :hidden-effect :visual-authority
            :diagnostic-without-node-map}}
```

## Replacement Scope

Gravity should replace:

- visual workflow graphs,
- approval workflows,
- ETL/dataflow canvases,
- support and operations flows,
- AI tool chains,
- low-code form-to-service automations,
- generated workflow source.

Visual editors remain tooling frontends over Gravity artifacts.

## Minimum End-to-End Slice

The first complete slice is a support approval flow:

- Visual editor emits nodes for classify, policy check, approval, and reply.
- Each node has schemas, effects, capability policy, and replay behavior.
- Compiler emits Gravity source or workflow graph with node provenance.
- Runtime records approvals and tool/model outputs.
- Negative fixture rejects an untyped edge and maps diagnostic to the visual
  node.

## Diagnostics

Visual workflow diagnostics use `DOM21` identifiers:

- `DOM21-NODE` for nodes without typed schemas.
- `DOM21-EDGE` for untyped or incompatible graph edges.
- `DOM21-EFFECT` for hidden effects in visual nodes.
- `DOM21-CAPABILITY` for tool/model/service nodes without grants.
- `DOM21-APPROVAL` for missing approval policy.
- `DOM21-REPLAY` for unrecorded nondeterminism.
- `DOM21-GENERATED` for generated code bypassing compiler checks.
- `DOM21-MAPPING` for diagnostics that cannot map back to visual nodes or
  edges.
- `DOM21-MIGRATION` for graph/schema changes without compatibility records.

Diagnostics must include graph id, node/edge id, source/generated artifact,
schema id, effect, capability, policy, replay mode, and remediation.

## Rejected Designs

Gravity rejects visual nodes without schemas.

Gravity rejects visual graph authority not represented in capability artifacts.

Gravity rejects generated workflow code that skips compiler checks.

Gravity rejects hidden side effects in visual nodes.

Gravity rejects diagnostics that cannot map back to visual graph elements.

## Conformance Criteria

A conforming visual workflow slice must demonstrate:

- visual graph, generated source/MIR, and workflow graph artifacts,
- typed node and edge schemas,
- approval and policy nodes,
- model/tool capability checks,
- replay traces,
- graph migration records,
- diagnostics mapped to visual nodes,
- rejection of untyped edges, hidden effects, visual-only authority, and
  unchecked generated code.
