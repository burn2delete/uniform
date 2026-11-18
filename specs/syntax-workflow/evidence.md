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
resolves C3 links under both `:input-syntax-ids` and `:inputs` on generated origins
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

### Controller observations for the reviewed candidate

The controller receipt in review 353 identifies base
`378b98048d93d2eccef432ec57cc68f8644ea98e`, candidate
`ed1eb059e8fc4b29d6567fc9c960a11ab362bdae` and tree
`eebe2228dd190bc453db4a0ae37a5c020bff1095`. All three commands below
passed on that tree. In the controller's `target/validation/advance/events.jsonl`
on 2026-11-18 UTC, `check-350` reported 6 language-boundary tests and 11
assertions. `check-351` reported 6 hosted verifier tests and 141 assertions.
Both reported zero failures and errors.

The stdout records for `check-352` contain the returned
`:gravity/syntax-verification-report` and validation results. Every report
had `:serialization-current? true` and `:serialization-round-trips? true`.

| Fixture key | Report `:status` | `:generated-origins-valid?` | Validation result |
| --- | --- | --- | --- |
| `:shared` | `:passed` | `true` | Report entry point only |
| `:prior` | `:passed` | `true` | `:complete` |
| `:missing`, `:self`, `:forward`, `:second-spelling`, `:mixed-chain`, `:empty-inputs`, `:missing-producer`, `:missing-span` | `:failed` | `false` | Each returned the diagnostic below |

```clojure
{:id "C3-ORIGIN" :source "source.gravity" :stage :syntax-object-model
 :missing-fields [:source-and-generated-origins?]}
```

The controller ran these argument vectors on the reviewed tree.

```clojure
["clojure" "-M:leaf-test" "--namespace" "gravity.c3-syntax-verification-test"]
["clojure" "-Sdeps" "{:paths [\"bootstrap/clojure/src\" \"bootstrap/clojure/test\"]}" "-M" "-e"
 "(require '[gravity.c3-syntax-verification-test :as t]) (prn {:shared (t/shared-origin-report) :validation-cases (t/origin-validation-results)})"]
["clojure" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"]
```

### Hosted Reader abbreviation repair

On the hosted path, the Reader abbreviation constructor puts the originating
C2 seed's `:syntax-id` in `:inputs` on a source object's generated origin.
The verifier delegates the identity of that link to the existing Reader
authentication callback, which rebuilds the stream from the supplied C2
products. An accompanying `:input-syntax-ids` still resolves against prior
C3 objects. [Hosted origin constructor](../../bootstrap/clojure/src/gravity/syntax_origin.clj)
and [Reader authentication](../../bootstrap/clojure/src/gravity/syntax.clj).

The repair fixture constructs a first source object from `'x` and a generated
successor. It exercises the hosted Reader reconstruction with fixture C2
integrity, revision and top-level product inputs. Its negative cases omit
the Reader artifact, substitute the seed link, and add an unresolved C3 link
to the Reader origin. Each altered stream has fresh serialization.

The supplied controller receipt for base
`378b98048d93d2eccef432ec57cc68f8644ea98e`, candidate
`9938a8f8f0fae2108d32f74124b9136b199705ac`, tree
`35326512b6b17c666b3721bf600b253dbfa55619` reports that all three
controller checks passed. Review requested changes after observing
`:generated-origins-valid? true` on a generated form whose first origin was
`{:kind :source}` and whose second origin was valid.

### Leading source origin on a generated form

The verifier now requires a producer and span on the leading origin of a
generated form, including when that origin describes the source. The
`:source-first` fixture supplies both fields and an empty input vector,
followed by a generated origin that links to the preceding syntax object.
It expects a passed report and `:complete` validation. Three variants use
a bare leading source, omit just the producer, or omit just the span. Each
regenerates serialization and expects a failed origin check and `C3-ORIGIN`
with `:missing-fields [:source-and-generated-origins?]`.

The supplied controller receipt for base
`378b98048d93d2eccef432ec57cc68f8644ea98e`, candidate
`072dec774bd3040f342a41fb3111eeedbd168ba3`, tree
`b5db7af7f4616c6b367fcbd08314c1af33478a8f` reports that all three checks
passed, including the leading-source fixtures. Review then changed only the
positive fixture's `:input-syntax-ids` to `["sha256:missing"]` and observed
`:generated-origins-valid? true` on that candidate.

### Source inputs in a generated form's origin chain

The hosted verifier checks both input spellings on each source entry in a
generated form's chain. A source entry has zero inputs, as in the
[hosted constructor](../../bootstrap/clojure/src/gravity/syntax_origin.clj)
and the [Gravity origin predicate](../../bootstrap/gravity/src/gravity/bootstrap/syntax.gravity).

`:leading-source-input-syntax-ids` reproduces the review case.
`:leading-source-inputs` adds the same missing ID under `:inputs` alongside
the empty `:input-syntax-ids`. `:later-source-with-prior-input` puts a source
entry after a generated entry and gives it an otherwise resolvable ID.
The supplied controller receipt for base
`378b98048d93d2eccef432ec57cc68f8644ea98e`, candidate
`ca827e6af247e26dc11a35835d1a1bfa3dcb16e3`, tree
`69762319781fa35c33c7e437b29ae7fa15e58ef1` reports that all three checks
passed. The fixture assertions establish that serialization was current and
round-tripped for all three source-input variants. Each report returned
`:status :failed` and `:generated-origins-valid? false`. Validation returned
`C3-ORIGIN` with `:missing-fields [:source-and-generated-origins?]`. With an
empty input vector, `:source-first` passed the origin check and report, and
validation returned `:complete`.

### Non-sequential origin chains

Review requested changes on that tree. Replacing the source object's origin
vector in `:mixed-chain` with a set kept both entries, including the unresolved
generated input. The hosted report treated the set as an empty sequence and
returned `:generated-origins-valid? true`.

The repair requires a sequential origin chain before validating its entries.
`:mixed-chain-set` changes only the carrier of the reviewed mixed chain.
`:source-only-set` makes the same change to the valid `:prior` stream to
isolate the carrier failure. Both fixtures regenerate serialization and assert
a current round-trip, a failed report, a false origin check and `C3-ORIGIN`
with `:missing-fields [:source-and-generated-origins?]`.

Controller checks on the set fixtures and independent acceptance are pending.
The proposed checks are the two namespace checks above and the report command
with `:abbreviations (t/hosted-abbreviation-reports)` included in its map.
