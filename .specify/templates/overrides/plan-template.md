# Implementation plan: [change]

Spec: [spec.md] | Owner: [exact owner] | Input revision: [commit/tree]

## Contract alignment

Link canonical architecture/semantics and existing verification workflow.
Explain how the constitution's actual decisions apply; no generic approval gate.

## Approach and exact files

Reuse the current architecture. Name file ownership, dependencies and boundaries.
Omit inapplicable data models, service designs or research deliverables.

## Validation and evidence

Name positive/negative checks, existing commands and required evidence artifacts.
Distinguish executed, reported and unrun checks. No performance or authority
claim without the owning contract's evidence.

## Delivery and remaining work

Keep a current draft PR; separate declared implementation, review and integration.
Deus owns integration. Record blocked actions and independent next steps.

## Names, callers and compatibility

List semantic names for new implementation files, namespaces and application/API
surfaces, for example `reader/diagnostics.clj` and `gravity.reader.diagnostics`.
Map planning IDs only in traceability metadata. For an owned legacy rename, map
imports/requires, callers, tests/selectors, load order, docs, manifests/hashes and
compatibility before editing. Preserve meaningful wire/schema/diagnostic IDs.
Coordinate active owners; do not make all legacy renames a prerequisite.
