# Phase-10 current decision allocation

This is a directly consumable field proposal for the exact 25 phase-10 IDs in
the frozen current-decision partition. It is distinct from the earlier five
historical-gap mappings. It creates no implementation task, changes no decision
status, and does not add 25 to the accepted historical mapping count.

## Pinned inputs and duplicate check

The supplied partition `own-decision530-partition-8b4543c3.edn` has SHA256
`179e01ed0cd699663660404602543cde1c8e4317535eca1edbec90d4fcf7bb74`.
Its source is `8b4543c35a1c267722428691ef7da62c5feb9d87`.
The actual field predecessors, task criteria and plans are read from published
PR554 at `86747916c4c876d707a1548113409fb939d854c9`. Older partition guards are
retained alongside these current guards; they are not substituted for current
record identities.

All 25 assigned IDs exist at that base, remain Open, and have no
`criterion-task-allocation` field. No completed decision allocation is duplicated.
The input's accepted-430 manifest is pinned separately; its 430 historical
mappings remain unchanged. All 833 current decision identities and Open states
are compared before and after simulated field application.

The prior phase-10 source review is reused only for its recorded finite source
conservation and qualified task-interface scope: review digest
`c21b1110b95efcf5e22090572b642f881d997962c805aa0ceaf6d3a180699285`.
The accepted five historical mappings retain source-review digest
`e144d8f4c9823d4f832d4bdd14eb385e34e44581462072469240ac413a02c36b`
and independent composition digest
`217081cbf817433a4602ae1a4fd610b632fc1d112651117f65f2632c4676c59b`.
These reviews do not confer whole-output or whole-attached-facet proof on the
new current-decision allocations.

## Concrete allocation result

[decision-allocation.edn](decision-allocation.edn) contains all 25 exact guarded
before/after operations on `:criterion-task-allocation`, including complete
before records, original dispatch records, required predicates, source facets,
roles, dependencies, outputs, refusals and unresolved metadata. There is no
whole-file replacement or canonical write.

Eight proposed bindings reuse exact current criterion text and byte witnesses:

| Assigned semantic source ID (decision prefix `interface/`) | Exact current criterion | Role |
| --- | --- | --- |
| `schema/emit-graphql-sdl` | `schema/graphql-generation/full-source-interface` | SDL producer |
| `schema/emit-graphql-resolver-adapter` | `schema/graphql-generation/full-source-interface` | Typed adapter producer; actual invocation remains distinct |
| `schema/emit-database-structure` | `schema/database-mapping/full-source-interface` | Declared-dialect structure producer |
| `schema/emit-typed-row-adapter` | `schema/database-mapping/full-source-interface` | Typed adapter producer |
| `schema/emit-typed-api-client` | `schema/api-client-generation`, plus the GraphQL criterion | Client artifact producer |
| `schema/check-client-operation` | The same client/GraphQL criteria | Concrete operation checker |
| `schema/emit-openapi-document` | `schema/openapi-generation` | Document producer |
| `schema/emit-http-boundary-validators` | `schema/openapi-generation` | Boundary validator producer |

Each reuse verifies exact positive and negative substrings inside the actual
current criterion. Supporting links, shared providers and nearby source context
are not substituted for that witness. Artifact dependencies and output ownership
are named explicitly, and the actual current plan is pinned.

The other 17 decisions receive nine minimal Open criterion supplements:

| Proposed criterion group | Decisions covered | Existing task |
| --- | --- | --- |
| Source manifest and validator | `schema/expand-authoritative-schema`, `schema/generate-source-validator` | Schema |
| Static type projection | `language/types/project-schema-static-type` | Types declared-abstractions task |
| Declared-format codecs | `schema/encode-declared-format`, `schema/decode-declared-format` | Schema |
| Canonical codecs and identity | `schema/encode-canonical-value`, `schema/decode-canonical-value`, `schema/check-canonical-identity-input` | Schema |
| Binary layout, codecs and vectors | `schema/emit-explicit-binary-layout`, `schema/emit-binary-codec-binding-input`, `schema/check-binary-reference-layout` | Schema |
| Semantic compatibility diff | `schema/compatibility-diff` | Schema |
| Migration plan, admission and validation | `schema/emit-typed-migration-plan`, `schema/authorize-runtime-migration`, `schema/validate-migration-change` | Schema, with explicit actual runtime admission/checker roles |
| Executed HTTP contract parity | `schema/run-http-contract-tests` | Schema |
| Deployment provider authority | `safety/safe10/runtime-authority` | Runtime enforcement/observability task |

[decision-allocation-plan.edn](decision-allocation-plan.edn) contains the actual
supplement text, inputs, outputs, positives, negatives, dependencies and
diagnostics. The generated proposal aggregates these into three acceptance-field
append operations, each bound to its exact current field and record. Types and
Runtime retain their single-line field format. Other fields, existing criteria,
later unrelated text, task IDs and statuses are preserved; stale or repeated
application refuses rather than overwriting.

Static type generation is not schema normalization. Generating a validator is
not running it at a boundary. Binding-input generation is not foreign invocation.
Migration-plan generation is not runtime authorization or fixture execution.
Compiler/Auth facts are inputs to actual provider presence/scope checks, not
proof that the runtime checked them. These distinctions are recorded per ID.

All 463 distinct attached facet records retain actual document/occurrence IDs,
raw UTF-8 byte spans, hashes and text. Per-decision metadata and unresolved-ID
lists remain verbatim. Cross-family context stays conditional context; no shared
facet is promoted to whole-output proof. Whole-attached-facet entailment remains
unconferred and independent review remains pending for all 25 proposals.

## Controls and handoff

With the supplied frozen partition available as `PARTITION_EDN`:

```bash
clojure -J-Xmx512m -M tools/schema_decision_allocation.clj --check "$PARTITION_EDN"
clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

The bounded checker passes 18 negative controls for missing/duplicate/unassigned
IDs, context promoted to proof, renamed tasks, forged witnesses, lost refusals,
dependencies or facets, false acceptance, changed Open-state accounting,
heading-only substitutes, artificial task expansion, stale decision/task fields
and repeated application. It simulates all 25 field operations while preserving
all 833 ID/status pairs. The language-boundary gate passes 6 tests / 11 assertions.
Pinned clj-kondo 2025.10.23 uses the unchanged CI config and reports no new source
or EDN findings. These checks establish input/patch conservation, not independent
semantic acceptance.

Authored allocations: 25; existing exact-criterion bindings: eight; supplement
decisions: 17; proposed criterion groups: nine; proposed task-field appends:
three; task-count delta: zero. No decision is left without an actionable proposed
interface. All 25 still require independent allocation review/adoption, and all
implementation states remain Open/unassessed. No accepted historical count,
denominator, roadmap credit or whole-project completion rate changes here.

The canonical owner alone applies reviewed field operations to the current
source, resolves any later predecessor mismatch, and composes task/SH source
versions. This work writes only the bounded proposal, its plan, checker and
receipts in this worktree. It creates no ledger, worker or reviewer and runs no
compiler, runtime, broker, release or process-probe operation.
