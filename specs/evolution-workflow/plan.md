# Governance implementation interfaces

Spec: [spec.md](spec.md) | Tasks: [tasks.md](tasks.md) | Evidence: [evidence.md](evidence.md).

## Authority and boundaries

Edit only the canonical task records. The [roadmap](../../docs/component-roadmaps/evolution.md)
is generated with `clojure -M tools/evolution_task_projection.clj --write`.
The governance maintainer role owns these producer interfaces; individual
ownership is unassigned. Existing compiler, package, library and release
owners retain their source and execution boundaries.

## Dependencies and outputs

Each task's Dependencies field names the concrete conditional artifact inputs:
compatibility comparisons, security/unsafe review, RFC decisions, profile/target
evidence, module manifests, namespace ownership and retained release history.
Consume the applicable artifacts, not completion of whole components.

Build's project/lock and package-verification tasks consume declared package
policy, namespace, yank and registry decisions. Release consumes the exact
applicable governance evidence; a governance producer is distinct from that
consumer. Language-revision admission and migration interfaces retain their
separate source-family composition and deduplication review.

## Validation and delivery

Validate the ten source-reviewed task bodies, required negative cases, source
facet identities, current Build prefixes and guarded receiver-field amendments.
Register the task authority, schema, generated view and literal routing together.
Preserve all 833 decision IDs/Open states and accepted historical mappings.
Freeze actual source spans for independent affected-composition review before
adopting the twelve historical mappings. No implementation credit follows from
this task authority or its read-only projection.
