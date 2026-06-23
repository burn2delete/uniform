# DOM11 - Data, Query and Analytics Domain Specification

Sequence: 134
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover data, query, and analytics slices
normally written in SQL, Python, R, Spark, Pandas, Polars, Scala, Java, dbt, or
workflow DSLs.

The replacement scope is typed datasets, relational and dataframe queries,
streaming windows, ETL pipelines, lineage, schema validation, query plans,
analytics kernels, privacy/taint labels, and batch/distributed execution plans.

## Requirements

- Inputs, intermediate datasets, outputs, and sinks must have schemas and taint
  or privacy labels when policy requires them.
- Data sources such as files, databases, streams, object stores, APIs, and model
  outputs require effects and capabilities.
- Query lowering must record whether execution occurs in SQL, native kernels,
  GPU kernels, distributed workflow steps, or hosted runtimes.
- Aggregations and windowing must declare ordering, determinism, numeric mode,
  null behavior, and approximate policy.
- Unbounded materialization is rejected for constrained profiles or must use a
  declared streaming/memory policy.
- Lineage and schema evidence must be preserved through transformations when
  policy requires it.

## Dependencies

- `B8`, `B10`, `B11`, and `B13` define GPU, workflow, query, and artifact
  output.
- `P4`, `P5`, and `P9` define hosted, native, and distributed execution.
- `SAFE10`, `SAFE11`, Phase 10 schema docs, and `R7` define capabilities,
  taint, schemas, and distributed runtime behavior.
- Math and performance phases define numeric and optimization evidence.

## Outputs and Artifacts

- Analytics domain manifest.
- Dataset schema bundle.
- Query or dataflow plan.
- Generated SQL or native/GPU kernel.
- Lineage record.
- Privacy/taint report.
- Numeric mode report.
- Execution and memory plan.
- Analytics conformance report.
- Analytics diagnostics.

## Domain Manifest

```clojure
{:domain :data-analytics
 :profiles #{:hosted :native :distributed}
 :backends #{:query-relational :llvm :gpu :workflow-graph}
 :artifacts #{:query-plan :lineage :schema-report :analytics-kernel}
 :examples #{:typed-dataframe :etl-pipeline :stream-window :gpu-aggregate}
 :rejects #{:schema-drift :unbounded-materialization
            :lineage-loss :unauthorized-data-source}}
```

## Replacement Scope

Gravity should replace:

- typed dataframe and table transformations,
- SQL and query-builder slices,
- batch ETL jobs,
- streaming windows,
- analytics APIs,
- GPU/native aggregate kernels,
- lineage and validation pipelines.

External warehouses, lakehouses, and streaming brokers remain providers.

## Minimum End-to-End Slice

The first complete slice is a revenue aggregate:

- Gravity source declares input schema, database read capability, decimal
  numeric mode, grouping semantics, and output schema.
- Query backend emits SQL or native plan.
- Runtime validates taint and capability.
- Artifact pack records lineage from source tables to aggregate output.
- Negative fixture rejects schema drift and unauthorized dataset access.

## Diagnostics

Analytics diagnostics use `DOM11` identifiers:

- `DOM11-SCHEMA` for missing or drifted input/output schemas.
- `DOM11-CAPABILITY` for unauthorized file, database, network, stream, object
  store, model, or tool data access.
- `DOM11-TAINT` for unsafe flow into sinks.
- `DOM11-LINEAGE` for lost lineage where policy requires it.
- `DOM11-MEMORY` for unbounded materialization without policy.
- `DOM11-NUMERIC` for aggregation or approximation without numeric mode.
- `DOM11-DETERMINISM` for nondeterministic aggregates without policy.
- `DOM11-CONFORMANCE` for missing query, kernel, or dataflow evidence.

Diagnostics must include dataset/query id, source span, schema id, execution
target, effect, capability, taint/privacy label, missing artifact, and
remediation.

## Rejected Designs

Gravity rejects schema-less analytics boundaries.

Gravity rejects data access through ambient provider credentials.

Gravity rejects lineage loss where downstream policy needs provenance.

Gravity rejects nondeterministic or approximate aggregates without policy.

Gravity rejects unbounded materialization in constrained runtimes.

## Conformance Criteria

A conforming analytics slice must demonstrate:

- typed dataset and aggregate examples,
- SQL/native/GPU or workflow execution artifacts,
- lineage and schema reports,
- capability and taint enforcement,
- streaming or bounded memory policies,
- numeric/determinism fixtures,
- rejection of schema drift, unauthorized data access, and lineage loss.
