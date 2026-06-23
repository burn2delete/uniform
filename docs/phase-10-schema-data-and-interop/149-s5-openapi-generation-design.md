# S5 - OpenAPI Generation Design

Sequence: 149
Phase: 10 - Schema, Data and Interop
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

OpenAPI generation turns Gravity service routes, schemas, error models, auth
policy, idempotency, effects, capabilities, examples, and compatibility records
into OpenAPI documents, typed clients, server validators, request validators,
response validators, and contract tests.

The OpenAPI artifact is generated evidence. Gravity route and schema source
remain authoritative.

## Requirements

- Every exposed route must declare method, path, path/query/header/body schemas,
  response schemas, error envelopes, status codes, auth/capability policy,
  effects, idempotency when relevant, and versioning metadata.
- HTTP input is tainted until validated.
- Generated OpenAPI must preserve source spans, schema hashes, route ids,
  compatibility policy, auth metadata, and examples.
- Handlers must not perform filesystem, network, database, secret, model, tool,
  process, or shell effects outside declared grants.
- Client generation must track schema/operation hashes.
- Breaking changes must emit compatibility diagnostics.

## Dependencies

- `S1`, `S2`, and `S3` define schemas, serialization, and canonical identity.
- `DOM8`, `B11`, `R11`, and `R12` define backend-service, query, capability, and
  observability integration.
- Package and testing phases consume OpenAPI artifacts and contract tests.

## Outputs and Artifacts

- OpenAPI generation manifest.
- OpenAPI document.
- Request validator.
- Response validator.
- Typed client artifact.
- Error model artifact.
- Auth/capability metadata.
- Breaking-change report.
- Contract test suite.
- OpenAPI diagnostics.

## Generation Manifest

```clojure
{:artifact :gravity/openapi-generation
 :service TicketService
 :routes #{:get-ticket :create-ticket}
 :schemas #{Ticket TicketCreateRequest ErrorEnvelope}
 :taint-boundary :http-input
 :artifacts #{:openapi-spec :typed-client :request-validator
              :response-validator}}
```

## Route Mapping

Route mapping covers method, path, path/query/header/body parameters,
serialization format, status codes, error envelopes, auth requirements,
idempotency keys, pagination, rate-limit metadata, examples, and deprecation.
Unsupported schema features require adapters or rejection.

## Diagnostics

OpenAPI diagnostics use `S5` identifiers:

- `S5-ROUTE` for missing method/path/handler route metadata.
- `S5-SCHEMA` for missing request, response, parameter, or error schemas.
- `S5-TAINT` for unvalidated HTTP input reaching trusted sinks.
- `S5-CAPABILITY` for route effects outside grants.
- `S5-IDEMPOTENCY` for mutating routes without idempotency policy where
  required.
- `S5-DIFF` for breaking changes without compatibility policy.
- `S5-CLIENT` for generated clients without operation/schema hash validation.
- `S5-SOURCEMAP` for OpenAPI operations without source provenance.
- `S5-MANIFEST` for incomplete OpenAPI artifacts.

Diagnostics must include route id, source span, method/path, schema id, effect,
capability, compatibility status, and remediation.

## Rejected Designs

Gravity rejects routes without typed request and response contracts.

Gravity rejects treating OpenAPI documents as authoritative source schemas.

Gravity rejects undocumented handler effects.

Gravity rejects generated clients without schema hash checks.

Gravity rejects compatibility-breaking changes without policy records.

## Conformance Criteria

A conforming OpenAPI generator must demonstrate:

- route-to-OpenAPI generation,
- request/response/error validators,
- auth and capability metadata,
- taint validation for HTTP input,
- typed clients with operation hashes,
- breaking-change detection,
- contract tests comparing handlers to generated schemas,
- rejection of missing schemas and hidden route effects.
