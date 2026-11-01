# Evidence: Reader workflow migration

## Token spelling reuse

The focused Reader change reuses the raw spelling already constructed for an
atom or string token and obtains atom codepoints from that same spelling.
The existing token constructor signature and numeric classifier are retained.
Current-source regressions cover raw tokens including trivia, exact bytes,
Unicode spans, descriptors, malformed inputs, fresh replay and tampering.

Focused validation of `c4a56b81778eb29b29e11a16af8642d68fb00d35`, tree
`1bf281a9bfdf573d5a918e459f05ec89b7b6730e`, passed nine current-source tests
with 306 assertions and the language boundary's six tests with 11 assertions.
The process exited naturally zero under the existing serialized 2 GiB /
120-second profile; clean commit/tree bookends matched. These are semantic
regressions, not performance measurements or authenticated handoff acceptance.

Independent review requested exact malformed-atom spelling/span assertions.
Their first run failed one assertion (two tests / 32 assertions) because the
test expected an engine identifier where the unchanged malformed-keyword
diagnostic returns nil. Correcting only that expectation produced the passing
receipt above; Reader production stayed unchanged. Independent source/test
review accepted the corrected candidate without a production finding.
Reader projection checks and four projection tests / 29 assertions passed,
as did the canonical document and full-language roadmap validators.

This increment does not close the lexical surface, authenticated
Reader-to-Syntax handoff, full C2/C5 construction, digest-resolution cost, C14
acceptance or release. It changes no historical source pin and claims no
measured speedup.

## Staged closure compatibility correction

PR414's full-module semantic tests did not exercise the staged Reader builder's
existing `sh03-` function selection. Its new `reader-token-with-raw` helper was
therefore omitted, and genuine selected-source construction failed during seed
compilation with `L2-UNKNOWN-CORE-FORM`, operator `reader-token-with-raw`.
The original passing leaf evidence above remains limited to its stated route.

The correction renames only the private helper definition and its three calls
to `sh03-token-with-raw`. This compatibility name follows the established
Reader closure contract; selection, structural validation, historical pins,
the token constructor signature and spelling reuse are unchanged.

The actual staged regression uses the existing explicit-current-source Reader
binding and current-emitter lexical binding. It builds and structurally verifies
the selected closure, checks the eight-argument helper, executes original source
and fresh Reader verification, and rejects missing-helper, wrong-arity and four
raw/span/descriptor/digest substitutions. Retained leaf regressions check Unicode
spans, exact bytes, numeric classification and malformed inputs.

Baseline `de605c89cb1b7e375e96eaaba6c64ef8db0dc505`, tree
`5807f121437d9eadf7cbbf4fba92d4be2adc9bb9`, failed naturally in 10.434 seconds
with the unknown-helper diagnostic before any C3/C6 consumer. After the rename,
an initial test run failed two newly written malformed-result expectations:
successful verification of a rejected source returns an accepted report, and
the engine diagnostic is stored in `:reader-engine-diagnostic`. Correcting those
test expectations preserved production and the original failed receipt.

Validated `edf7da074f1120c1dc743c74d9d1b673d3189f48`, tree
`ef0b6583ce1dddd218e5d2c445b9db9b912ab185`, passed 10 tests / 345 assertions
and language boundary 6 / 11, with zero failures/errors. The process exited
naturally zero in 12.998 seconds under serialized 2 GiB / 120-second execution;
clean before/after commit and tree identities matched. This establishes the
focused staged Reader route, not whole C2/C5/C6 composition, C14 acceptance,
migration, release or a performance improvement. Independent final review and
integration remain separate requirements.

## Native Keyword canonical identity path

The hosted canonical form builder now handles an exact native Keyword root
without allocating the generic traversal's entered map and stack. It retains
the existing shared ancestor binding and calls the existing carrier validator,
then scalar/component/Unicode validation, freshly on every invocation. All
other values retain the generic traversal. Both canonical and host-equality
uniqueness checks, source pins and admission policy are unchanged.

The contract tests enter through the production Reader canonical identity
entrypoint. They compare complete root/collection-contained diagnostics for
invalid components and Unicode; observe exact validation order and ancestor
context; reject a second carrier or scalar validation with the original
exception; preserve ancestor markers on success/failure; and retain both
collision controls. Existing scalar and actual-cycle regressions also ran.

Corrected-main baseline `5579e0f14fb303299b7028a062815790b7fde162`, tree
`110093df7e02dcf23914e636a6cb5a1d2c7c8024`, and candidate
`68685af9327631dd37ba8ca4f05b93f4712c1d1a`, tree
`b13ffb098b6f60c07ee0ccc67770c9b9c196c227`, used the identical test blob.
Each passed 19 tests / 582 assertions plus language boundary 6 / 11, with zero
failures/errors, natural exit zero and matching clean commit/tree bookends.
Execution used serialized 512 MiB / 60-second profiles; elapsed times were
7.806 and 7.981 seconds respectively. An intervening capacity wait expired
before candidate tests started; that separate failure was retained, and the
unchanged candidate ran only after confirmed release of the competing process.

These finite checks support the scoped validation contract, not a speedup or
full-source performance claim. Full original C11 C2 construction, its later
digest-resolution behavior, and the C14 contextual consumer remain separate
validation milestones. No C14 acceptance, authoritative migration or release
credit is established here.

## Earlier workflow evidence

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
