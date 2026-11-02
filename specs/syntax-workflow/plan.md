# Plan: Syntax task authority and roadmap projection

Baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Authority and compatibility

Move the five existing [remaining-work rows](../../docs/component-roadmaps/syntax.md#current-evidence-and-remaining-work)
verbatim to [implementation tasks](tasks.md#implementation-tasks). Preserve
all original heading anchors and the table's three columns. Generate only the
marked roadmap table from those records; all other source text stays outside
the tool's writable region. Historical scaffold tasks remain frozen separately.

The established IDs use `syntax/current-evidence-and-remaining-work/` followed
by `syntax1-construct-canonical-syntax`, `syntax2-preserve-transformations`,
`syntax3-invalidate-facts`, `syntax4-serialize-and-verify`, and
`syntax5-integrate-consumers`. The original Markdown provided display labels,
not literal full IDs. Preserve this derived identity convention rather than
renumbering or changing IDs when presentation order changes.

## Implementation

Use the accepted Reader projection pattern as a scoped Syntax tool, without
changing Reader. Exact known IDs and unique titles guard missing/substituted
records. Checked boxes require explicit `Accepted;` status. Semantic receipt
acceptance remains independent review, not a string-parser guarantee.

Record fields support plain text, literal `->` arrows and inline absolute HTTPS
links with no embedded credentials. Relative/reference/HTML/image/unsupported
Markdown links reject instead of changing target context between documents.
Literal interface arrows already occur in canonical acceptance and must survive.

## Validation

From repository root:

```bash
clojure -M tools/syntax_task_projection.clj --check
clojure -M tools/syntax_task_projection_test.clj
clojure -M tools/reader_task_projection.clj --check
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

`--write` regenerates only the marked Syntax roadmap region after validating
the complete task source. `--check` is read-only and rejects drift. Compare the
initial table byte-for-byte with baseline and compare the five IDs with the
existing dashboard projection. Check Markdown file/heading links, ASCII and
whitespace. No compiler behavior or whole migration completion follows from
these checks. Obtain independent exact-candidate review before integration.

## Residual work

The five implementation tasks remain open. Their owning source, interface,
acceptance and release decisions remain unchanged. Future evidence-backed task
updates change the ledger and regenerate its table in the same reviewed change.
Other components require their own migration; no aggregate denominator is added.
