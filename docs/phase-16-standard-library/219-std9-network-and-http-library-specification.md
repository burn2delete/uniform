# STD9 - Network and HTTP Library Specification

Sequence: 219
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.net` and `gravity.net.http` define network sockets, clients, servers, URLs, requests, responses, headers, TLS configuration, streaming bodies, retries, timeouts, and routing.
They expose networking as effectful and capability-gated behavior.
No profile receives ambient outbound or inbound network authority.

HTTP APIs are also schema and policy boundaries.
Request and response bodies may carry tainted data, secrets, credentials, model outputs, workflow events, or user-controlled text.
The network library must preserve type, schema, timeout, retry, authentication, TLS, replay, and audit evidence in artifacts.

## Requirements

- Network operations MUST declare outbound, inbound, DNS, TLS, websocket, stream, or listen effects.
- Every outbound or inbound operation MUST require an in-scope network capability.
- Client calls MUST declare timeout, retry, redirect, body size, streaming, and cancellation policy.
- Server routes MUST declare request schema, response schema, authentication policy, and effect boundary.
- TLS configuration MUST declare trust roots, certificate validation policy, and insecure exceptions as unsafe or test-only.
- URL parsing MUST be typed and must not treat unchecked strings as safe endpoints.
- Request and response bodies MUST use STD10 schemas or explicit byte/text policies.
- Secrets in headers, URLs, and bodies MUST be marked and redacted in diagnostics and artifacts.
- Distributed workflow network calls MUST be recorded, idempotent, or isolated as workflow activities.
- AI tool network calls MUST pass through Phase 11 tool, policy, and `:ai/human-review` checks.

## Module Surface

- URL and authority: `url`, `parse-url`, `origin`, `host`, `port`, `scheme`, `path`, `query`, and `endpoint-policy`.
- Client APIs: `request`, `get`, `post`, `put`, `patch`, `delete`, `stream-request`, and `with-client`.
- Server APIs: `serve`, `route`, `router`, `middleware`, `handler`, `response`, `status`, and `headers`.
- Body APIs: `body-bytes`, `body-text`, `body-json`, `body-schema`, `stream-body`, and `body-limit`.
- Network primitives: `socket`, `connect`, `listen`, `accept`, `send`, `recv`, and `close`.
- Security: `tls-config`, `client-cert`, `redact-header`, `auth-scheme`, and `credential-source`.
- Policy: `timeout-policy`, `retry-policy`, `redirect-policy`, `rate-limit-policy`, and `network-capability`.

## Dependencies

- `L5`, `L6`, and `L11` for effects, capabilities, and resource ownership.
- `SAFE1`, `SAFE5`, `SAFE6`, `SAFE7`, `SAFE10`, and `SAFE15` for resources, unsafe boundaries, FFI boundaries, capability security, and proof-carrying wrappers.
- `P5`, `P4`, `P9`, and `P10` for native, hosted, distributed, and AI network legality.
- `R3`, `R4`, `R7`, and `R8` for native, hosted, distributed, and AI runtime integration.
- `STD4` for URL/text encoding and taint.
- `STD6` for sockets and streams as resources.
- `STD7` for cancellation, structured concurrency, and backpressure.
- `STD10` for schemas and canonical request/response data.
- `STD12` and `STD13` for workflow and AI integration.

## Example

```clojure
(ns sample.client
  (:require [gravity.net.http :as http])
  (:profile :distributed))

(defn fetch-user [cap id]
  (http/get cap
    (http/url "https://api.example.test/users/{id}" {:id id})
    {:timeout-ms 1000
     :retry {:max 2 :idempotent true}
     :response-schema :user/v1}))
```

The network capability supplies authority.
The request declares timeout, retry, and response schema.
In `:distributed`, the call must run as a recorded or activity-isolated step.

## Profile Availability

- `:core`, `:hardware`, and `:formal` receive URL/data helpers only, not live network IO.
- `:firmware` and `:kernel` receive network device adapters only through specialized capabilities and unsafe audits.
- `:native` receives sockets, clients, servers, TLS, and streaming under capabilities.
- `:hosted` may delegate to host fetch, HTTP, TLS, or socket libraries with provider records.
- `:distributed` receives network APIs only through workflow-safe activity and replay contracts.
- `:ai` receives network behavior only through tool definitions, model policy, and `:ai/human-review` gates.
- `:meta` may access network services only when compiler tooling is granted an explicit capability.

## Outputs and Artifacts

- Network module manifest with effects, capabilities, TLS policy, and profile matrix.
- Request and response schema artifacts.
- Timeout, retry, redirect, rate limit, and cancellation policy artifacts.
- Redaction records for headers, credentials, URLs, and bodies.
- Negative fixtures for ambient access, missing timeout, unbounded retry, insecure TLS, and replay-unsafe calls.
- Host delegation records for HTTP clients, servers, TLS providers, DNS, and sockets.
- Security audit records for privileged listeners, raw sockets, and unsafe TLS overrides.

## Diagnostics

- `STD9001` when network access lacks a capability.
- `STD9002` when a client call omits timeout or cancellation policy.
- `STD9003` when retry policy is unbounded or not tied to idempotency.
- `STD9004` when TLS validation is disabled outside an allowed unsafe or test context.
- `STD9005` when request or response body lacks schema, encoding, or byte policy.
- `STD9006` when secret data would appear unredacted in diagnostics or artifacts.
- `STD9007` when workflow replay observes unrecorded network IO.
- `STD9008` when AI network access bypasses tool authorization or `:ai/human-review` policy.

## Conformance Criteria

- Capability fixtures deny all undeclared hosts, ports, protocols, and listen operations.
- Client fixtures enforce timeout, retry, redirect, body, streaming, and cancellation policy.
- Server fixtures validate request and response schemas.
- TLS fixtures distinguish valid, invalid, pinned, expired, and intentionally unsafe configurations.
- Distributed fixtures record or reject network nondeterminism.
- AI fixtures prove network calls occur through typed tools with policy records.
- Host-backed implementations preserve Gravity error codes and redaction behavior.
- Documentation examples compile only with declared network capabilities.
