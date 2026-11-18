# Evidence: Syntax workflow migration

## Source and scope

Source revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`. Scope: four documentation files in
`specs/syntax-workflow/`, prepared from repository override templates and the
[component roadmap](../../docs/component-roadmaps/syntax.md). This is manual use of the repository
Spec Kit artifact convention, not observed CLI execution or automatic discovery.

## Artifact preparation

The specification links retained contracts, the plan links the canonical task
table and acceptance heading, and the task ledger tracks only this migration.
No canonical contract, component implementation task or existing feature ledger
was copied or replaced.

## Validation

Observed 2026-10-26 against the prepared five-package documentation slice:

- Local Clojure link/artifact check: 20 ASCII artifacts and 131 relative file/heading links passed. Missing file, missing heading and absent required artifact controls rejected (3 negative controls).
- `java -Xmx512m -cp <existing Clojure runtime and repository classpath> clojure.main tools/validate_gravity_docs.clj`: exit 0; 240 docs and 19 phase indexes passed.
- `java -Xmx512m -cp <existing Clojure runtime and repository classpath> clojure.main tools/validate_full_language_roadmap.clj`: exit 0.
- `git diff --check`: exit 0.

The link check resolves relative targets from each artifact and checks referenced
Markdown headings; it does not fetch external URLs or prove semantic conformance.
The structural validators cover their existing canonical scopes, not new compiler
behavior. Compiler tests were not run. No implementation or normative file changed.

## Independent review

Pending. No self-review is represented as independent acceptance.

## Integration

Pending. No merge, deployment or component-release acceptance is claimed.

## Implementation evidence and limits

The component roadmap records open release acceptance. Source inspection and hosted projections are not accepted full syntax/hygiene conformance.
Read [current canonical component evidence](../../docs/component-roadmaps/syntax.md#current-evidence-and-remaining-work)
for its source-specific status. This package does not repin historical evidence
or claim compiler tests, all-function execution, self-hosting, seed retirement,
performance or release completion. Implementation acceptance is unassessed by
this documentation change.

## Implementation authority transfer candidate

Baseline: `023ec47d86004fc829ae7dc5753b715ca4a653dc`, inspected 2026-10-26.
The five canonical Syntax titles/status/acceptance cells move verbatim into
[tasks.md](tasks.md#implementation-tasks). Their existing table is generated;
release criteria and original anchors remain in the roadmap. All five tasks
remain open. The original observations above retain their scaffold-only scope.

The five full IDs were compared directly with the existing dashboard projection
and its phase/title extraction convention. Slash and colon punctuation in
legacy display labels is removed, not replaced with a new separator: the first
suffix is `syntax1-construct-canonical-syntax`, not `syntax-1-...`.
Those are stable existing derived identities, not original literal Markdown IDs.

The accepted Reader tool pattern initially rejected Syntax's existing literal
`->` interface arrows as unsupported Markdown. The scoped Syntax adaptation
preserves those arrows while still rejecting HTML and context-dependent links.
No canonical acceptance text was rewritten to accommodate a parser.

Validation and independent review are recorded below when actually performed.
No compiler semantic checks, external Specify CLI run, whole-project migration,
implementation acceptance or overall completion percentage is claimed.

### Observed candidate checks

On 2026-10-26, the projection check passed; focused tests passed 4 tests and
28 assertions with zero failures/errors. The accepted Reader projection still
passed. Documentation validation passed 240 docs and 19 phase indexes;
full-language roadmap validation passed. Language-boundary checks passed
6 tests and 11 assertions. Five affected Markdown artifacts passed ASCII and
local file/heading validation. The generated table matched the baseline bytes,
and all five task IDs matched the existing dashboard projection. Whitespace
validation passed. These are workflow/tooling checks, not Syntax semantics.

Independent exact-candidate review and integration remain pending at this
preparation checkpoint. The five implementation checkboxes remain unchecked.

## Hosted generated-origin inputs

Finding `syntax.origins.hosted-generated-input-closure-v1` belongs to
`syntax/current-evidence-and-remaining-work/syntax4-serialize-and-verify`.
[C3](../../docs/phase-06-compiler-architecture/082-c3-syntax-object-model.md#origin-chain)
requires links to prior syntax. The Gravity reference is
`c3-syntax-graph-verify-template` in
[syntax.gravity](../../bootstrap/gravity/src/gravity/bootstrap/syntax.gravity).

Source inspection found that the hosted serialization fixture records origin
kinds but omits input IDs. The [hosted report](../../bootstrap/clojure/src/gravity/c3_syntax_verification.clj)
now resolves both `:input-syntax-ids` and `:inputs` on generated origins
before adding the current object's `:syntax/id` to the set of prior IDs.
If both fields occur, each must contain inputs and every link must resolve.
The capability proof consumes the fresh report's `:generated-origins-valid?`
in its existing `:source-and-generated-origins?` check. The validation order
still gives construction and ID failures precedence over `C3-ORIGIN`.

The [hosted fixtures](../../bootstrap/clojure/test/gravity/c3_syntax_verification_test.clj)
exercise two siblings sharing a source and a successor referencing the second
sibling through a later origin. The validation cases use one generated object
and one source, matching the artifact's existing construction check. Each
case serializes and reads back its full stream, including the altered links.
The fixtures call the report, capability proof and validation code directly.
They supply the reader authentication callback and capture the diagnostic ID,
stage, source and missing field at the hosted failure callback.

| Fixture | Expected report | Expected validation |
| --- | --- | --- |
| Shared source and generated sibling | `:passed`, origins valid | Report entry point only |
| Prior source | `:passed`, serialization current | `:complete` |
| Missing later input, self or forward link | `:failed`, origins invalid, serialization current | `C3-ORIGIN`, `[:source-and-generated-origins?]` |
| Broken second spelling, mixed source/generated chain, empty inputs, missing producer or span | Same failed report checks | Same origin diagnostic |

Controller execution and independent acceptance are pending. The following
argument vectors exercise the fixtures and print the returned reports and
validation payloads for the controller's evidence record.

```clojure
["clojure" "-M:leaf-test" "--namespace" "gravity.c3-syntax-verification-test"]
["clojure" "-Sdeps" "{:paths [\"bootstrap/clojure/src\" \"bootstrap/clojure/test\"]}" "-M" "-e"
 "(require '[gravity.c3-syntax-verification-test :as t]) (prn {:shared (t/shared-origin-report) :validation-cases (t/origin-validation-results)})"]
["clojure" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"]
```
