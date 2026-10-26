# Evidence: Reader workflow migration
Current candidate: Reader implementation task authority transfer. The original
scaffold observations below retain their historical scope and are not fresh
acceptance of this candidate. Current validation is recorded at the end.


## Source and scope

Source revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`. Scope: four documentation files in
`specs/reader-workflow/`, prepared from repository override templates and the
[component roadmap](../../docs/component-roadmaps/reader.md). This is manual use of the repository
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

The accepted Reader-to-Core invocation pilot is partial component evidence, not full Reader surface, extension or release coverage.
Read [current canonical component evidence](../../docs/component-roadmaps/reader.md#accepted-finite-invocation-evidence)
for its source-specific status. This package does not repin historical evidence
or claim compiler tests, all-function execution, self-hosting, seed retirement,
performance or release completion. Implementation acceptance is unassessed by
this documentation change.

## Local implementation task authority candidate

Inspected public main `c40d74aded3a019ff2bdeb1f4050cf95dbb11c1e` on
2026-10-26. The seven original Reader task titles, evidence/status fields and
acceptance fields moved verbatim into the marked implementation region of
[tasks.md](tasks.md#implementation-tasks). Established derived dashboard IDs are preserved; see the provenance correction below.
The roadmap's table is now an explicitly generated compatibility view; its
initial table bytes match that baseline exactly. Original heading anchors,
normative documents and accepted finite invocation receipts are unchanged.

Locally observed validation on 2026-10-26:

- Projection check passed. Focused projection tests: 3 tests, 12 assertions,
  zero failures/errors. Controls reject missing/unknown and duplicate IDs,
  absent records, malformed fields, unsupported completion checkbox, stale
  status projections and missing/duplicate region markers.
- Language boundary: 6 tests, 11 assertions, zero failures/errors.
- Document validator: 240 documents and 19 phase indexes passed.
- Full-language roadmap validator passed.
- Five affected Markdown files passed ASCII and relative target/heading checks.
- Baseline/current projected table comparison and `git diff --check` passed.

The installed Clojure runtime was invoked through `java ... clojure.main` with
the repository source/test classpaths. Initial document-validator invocations
omitted the source classpath and failed to load their namespaces; corrected
invocations above passed. No compiler semantic tests, external Specify CLI,
full compiler migration or release acceptance were run or claimed.

Integration: not performed. None of the seven implementation tasks is accepted complete by
this change, and no global progress percentage is established.

## Local review correction: link context and identity

Independent review found that verbatim relative receipt links could resolve to
one file from tasks.md and a different/nonexistent file from the roadmap. The
projection now rejects context-dependent and unsupported Markdown link syntax;
plain text and inline absolute HTTPS links are the supported record fields.
A positive immutable receipt URL survives in both source and projection; the
relative `evidence.md#source-and-scope` reproduction and other unsupported forms
are negative controls. Link target existence and semantic acceptance remain
review obligations, not inferred from syntactic validation.

The original public c40d Markdown had task titles, not literal semantic IDs.
Canonical IDs derive from the phase ID `reader/finite-remaining-work` plus the
task title slug. This candidate uses those full
`reader/finite-remaining-work/<title-slug>` IDs explicitly, preserving dashboard
identities. These are derived task identities, not IDs that were literally
present in the original Markdown table.

Correction validation on 2026-10-26: focused tests passed 4 tests / 29
assertions, projection `--check` passed, five Markdown link/ASCII checks passed,
and the generated table still matches all seven baseline cells byte-for-byte.
`git diff --check` passed. Independent review of this successor remains pending.
