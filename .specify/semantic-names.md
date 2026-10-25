# Semantic implementation names

Adopted user decision, 2026-10-25. This guides Spec Kit work artifacts; it does
not replace [D1 architecture](../docs/phase-00-foundation-and-thesis/002-d1-system-architecture-overview.md)
or [D3 terms](../docs/phase-00-foundation-and-thesis/004-d3-terminology-and-concept-model.md).

New implementation filenames, namespaces, application names and API names must
identify their subsystem and behavior. Planning labels such as SH07, C2 and C16
stay in spec/task metadata and cross-references. Do not replace them with another
numbered code or impose a numeric name-length quota. Prefer familiar short words
and enough behavior detail to distinguish neighboring responsibilities.

## Architecture vocabulary

These are recognizable starting points, not a closed enum or a requirement to
abbreviate every word. Compose them with behavior: `reader.diagnostics`,
`core.source-map`, `build.incremental`, `build.pass-cache`.

| Word | Responsibility |
| --- | --- |
| reader | Source decoding, tokens and forms |
| syntax | Syntax objects, spans, origins and hygiene |
| expand | Macro expansion |
| resolve | Names, bindings, imports and namespaces |
| core | Core AST construction, mapping and verification |
| types | Type checking and inference |
| effects | Effect analysis and declarations |
| mir | Target-independent MIR |
| opt | Proven semantics-preserving optimization |
| lower | Target/domain lowering |
| emit | Artifact emission |
| runtime | Execution and declared runtime services |
| stdlib | Standard-library implementation |
| build | Dependency graph, incremental work and caching |
| cli | Command interface |
| diag | Diagnostics shared across subsystems |
| auth | Actual authentication/identity authority, when that is the responsibility |

Use `reader.diagnostics` rather than `auth` for reader error formatting. A word
must describe the actual responsibility; naming does not grant authority or
alter a security boundary. Follow each language's existing separator/case rules.

## Review the bounded change

At specify: name the behavior and canonical owner; place SH07/C6-style traceability
in metadata. At plan: list proposed semantic paths, namespaces and API names.
At tasks: use those names in implementation tasks and keep planning references
in a separate clause. At analyze/review: inspect new names in the diff and report
legacy dependencies without blocking unrelated development on all old names.

A lightweight review check uses the actual base revision of the candidate:

```bash
git diff --name-status --diff-filter=ACR BASE -- bootstrap tools bin
git diff --unified=0 BASE -- bootstrap tools bin
```

Review new/renamed filenames and introduced namespace/application/API declarations,
including additions within existing files. Use search to locate SH/C-number tokens
in those additions, then classify each occurrence. Existing declarations shown
only because of surrounding edits are legacy, not new introductions. This is a
human diff review, not a complete language parser or automated lint gate. No hook,
CI job or full-tree admission check is added by this increment.

| Proposed introduction | Review outcome |
| --- | --- |
| `core/source_map.clj`, `gravity.core.source-map` | Accept: subsystem and behavior |
| `reader/diagnostics.clj`, `gravity.reader.diagnostics` | Accept: reader diagnostic responsibility |
| `sh07_core_map.clj`, `gravity.c16-incremental` | Revise: planning identity leaked into an implementation name |
| Task metadata `planning-ref: SH07/C6` | Accept: traceability, not an implementation name |
| Existing wire/schema/diagnostic ID | Preserve and inspect its owning compatibility contract |

Real protocol, schema, wire and diagnostic IDs can carry external meaning. Do
not silently rewrite them as a naming cleanup. Record whether a name is internal,
public API, serialized identity or historical evidence. A compatibility alias is
an explicit, temporary migration boundary when needed, not permission to generate
new legacy names throughout the implementation.

## Incremental legacy migration

See the [sample audit and handoff](naming-pilot/plan.md). Its suggestions are not
reserved names or authorization to edit active owners' files. For each selected
rename, coordinate one owner and update its import/require graph, callers, tests
and test selectors, load order, docs, manifests/digests and any compatibility
facade together. Recompute evidence invalidated by source/name changes; do not
rewrite historical receipts as if they described the renamed revision.

This rule does not waive C12, advisory, oracle or other restrictions. Keep those
boundaries visible and do not rename code to evade a classifier. Existing leakage
remains explicit debt; useful new work can proceed with semantic names.
