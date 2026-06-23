# DOM8 - Backend Services Domain Specification

Sequence: 131
Phase: 9 - Domain-Specific Computing Coverage
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This domain proves that Gravity can cover backend service slices normally written
in Java, Kotlin, Go, Rust, C#, TypeScript, Python, Ruby, Clojure, or framework
DSLs.

The replacement scope is typed routes, service modules, workers, scheduled jobs,
message consumers, API specs, configuration schemas, database boundaries,
observability, and deployment capability manifests.

## Requirements

- Service routes and handlers must declare request, response, error, auth,
  effect, and capability contracts.
- External input is tainted until validated against schemas.
- Database, filesystem, network, environment, secrets, process, shell, queue,
  clock, random, model, and tool effects require capabilities.
- Configuration and secrets require schemas, policy, redaction, and deployment
  grants.
- Background jobs and workers require retry, timeout, idempotency, cancellation,
  and failure mapping.
- APIs must emit OpenAPI, GraphQL, RPC, or equivalent schema artifacts when
  exposed externally.
- Runtime observability must preserve route/job ids, source spans, effects, and
  capability decisions.

## Dependencies

- `P4`, `P5`, `P9`, and `P13` define hosted/native/distributed boundaries.
- `B3`, `B5`, `B6`, `B10`, `B11`, and `B13` define backend artifacts.
- `R4`, `R6`, `R7`, `R11`, and `R12` define runtime services.
- `SAFE10`, `SAFE11`, and Phase 10 schema docs define capability, taint, and API
  schemas.

## Outputs and Artifacts

- Backend service domain manifest.
- Service binary or package.
- API schema/specification.
- Route and handler manifest.
- Configuration schema.
- Capability manifest.
- Worker/job manifest.
- Observability schema.
- Service conformance report.
- Backend service diagnostics.

## Domain Manifest

```clojure
{:domain :backend-services
 :profiles #{:hosted :native :distributed}
 :backends #{:llvm :jvm :javascript-typescript :workflow-graph}
 :artifacts #{:service-binary :api-spec :config-schema
              :capability-manifest :worker-manifest}
 :examples #{:typed-route :crud-service :background-job :message-consumer}
 :rejects #{:schema-less-route :unauthorized-io :secret-leak
            :untyped-external-input}}
```

## Replacement Scope

Gravity should replace service slices for:

- HTTP/RPC routes,
- API clients and servers,
- background jobs,
- message consumers,
- configuration and secret loading,
- database-backed CRUD,
- workflow-backed endpoints,
- observability integration.

Cloud runtime providers and infrastructure remain deployment boundaries.

## Minimum End-to-End Slice

The first complete slice is a CRUD route:

- Gravity source declares request/response schemas, auth/capability policy,
  database transaction, and error mapping.
- Query backend emits SQL/prepared bindings.
- Service backend emits API spec and service artifact.
- Runtime capability checks deny ungranted filesystem or secret access.
- Integration fixture validates schema, taint, database transaction, and
  observability events.

## Diagnostics

Backend service diagnostics use `DOM8` identifiers:

- `DOM8-ROUTE` for route handlers without typed contracts.
- `DOM8-SCHEMA` for missing or drifted API, config, or database schemas.
- `DOM8-TAINT` for unvalidated external input reaching trusted sinks.
- `DOM8-CAPABILITY` for ungranted IO, secret, database, network, shell, model,
  or tool effects.
- `DOM8-JOB` for missing retry, timeout, idempotency, cancellation, or failure
  policy.
- `DOM8-SECRET` for secret leakage or unredacted diagnostics.
- `DOM8-OBSERVABILITY` for missing source/effect/capability runtime events.
- `DOM8-CONFORMANCE` for missing service integration evidence.

Diagnostics must include route/job id, source span, schema id, effect,
capability, config/secret id when relevant, and remediation.

## Rejected Designs

Gravity rejects schema-less public service boundaries.

Gravity rejects ambient deployment authority.

Gravity rejects config and secret access without schemas and grants.

Gravity rejects background jobs without retry and idempotency policy.

Gravity rejects observability that leaks secrets or drops capability decisions.

## Conformance Criteria

A conforming backend service slice must demonstrate:

- typed route, CRUD, worker, and message-consumer examples,
- emitted API and config schemas,
- database/query and capability integration,
- taint validation for external input,
- secret redaction tests,
- observability records,
- rejection of schema-less routes, unauthorized IO, secret leaks, and missing job
  policy.
