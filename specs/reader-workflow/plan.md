# Reader implementation planning entry

The 2026-11-01 implementation specification in [spec.md](spec.md) expands this
package beyond its original task-authority transfer. The original plan below
remains historical. Implementation progress stays only in the existing marked
[task ledger](tasks.md#implementation-tasks); this entry is not a second plan
for the whole compiler or a new acceptance authority.

Select the exact unresolved Reader task before refining this plan. First read
its current evidence and source contracts, the
[stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md)
and affected source/fixture files. Resolve current implementation paths from
that table and the actual call graph; historical numbered adapters are not
automatic semantic owners. Preserve accepted row receipts at their exact scope.

The selected plan must state:

- The complete source family, product or extension behavior being changed,
  actual owning implementation paths, and compatibility-bearing identities.
- Explicit source/root/options/policy inputs, actual outputs and the selected
  Syntax/CLI or extension-provider interface; no implicit Build-completion edge.
- Real accepted source fixtures and owning negative diagnostics, including late
  malformed/tampered cases when traversal or identity changes.
- Focused owner tests, affected producer/consumer composition and exact evidence
  capture; a source census or injected helper test does not replace execution.
- Any still-restricted operation, reserved writer or unresolved source decision,
  with the affected acceptance boundary kept open.

Use the current [development verification workflow](../../docs/development-verification.md)
to choose focused selectors after inspecting them. Run the language-boundary
gate after source/tooling changes and proportionate composition tests when
interfaces or identities change. Do not launch heavyweight historical replay
or claim migration/release authority merely to prepare an implementation plan.
Record results and residual host boundaries in the existing evidence record.
Required independent acceptance and coordinator integration stay separate.

## Selected increment: complete contract coverage

Select only `reader/finite-remaining-work/contract-coverage-reconciliation` in
the [existing task authority](tasks.md#implementation-tasks). Owner role: Reader
maintainer; no individual or reserved implementation path is reassigned here.
Planning inspection: `9631bfcd7c2e07251e38210c28979534585bfec9`. Accepted
receipts retain their own immutable revisions. This is the first source/fixture
conformance increment, with real missing cases exposed for implementation; its
completion does not accept the other six Reader outcomes.

Start with captured source bytes, the explicit root id and relative source path,
supplied-path provenance, encoding, reader options, feature decisions and
registered extension policy. Completed Build and self-hosting are not required
to inspect or develop this boundary. Compose only with the actual selected
producer and consumer when that execution is eligible under the existing hold.

### Implementation and fixture boundaries

Follow the [stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md),
then the selected-current call graph. These are existing locations to inspect,
not a transfer of ownership or a claim that a declaration executes:

- [Reader source](../../bootstrap/gravity/src/gravity/bootstrap/reader.gravity)
  and the [surface reader](../../bootstrap/gravity/src/gravity/compiler/l1_c2_surface_syntax_reader.gravity)
  contain Gravity definitions. Trace the invoked construction/verification
  entrypoints; an initial quoted package descriptor is not an executable parse.
- [Native intake and assembly](../../bootstrap/clojure/src/gravity/reader.clj),
  [source identity](../../bootstrap/clojure/src/gravity/c2_source_identity.clj),
  [namespace shape](../../bootstrap/clojure/src/gravity/reader_namespace.clj),
  [lexical validation](../../bootstrap/clojure/src/gravity/c2_lexical_validation.clj),
  [product projection](../../bootstrap/clojure/src/gravity/c2_reader_product_projection.clj)
  and [extension dispatch](../../bootstrap/clojure/src/gravity/reader/dispatch.clj)
  remain seed/adapter boundaries with their declared limits. Projection of an
  injected graph does not establish production or fresh reconstruction.
- Reuse the paired [complete surface corpus](../../bootstrap/clojure/fixtures/self-hosting/sh-03/accepted/complete-reader-surface.gravity),
  its [.qst counterpart](../../bootstrap/clojure/fixtures/self-hosting/sh-03/accepted/complete-reader-surface.qst),
  and [mixed decoding corpus](../../bootstrap/clojure/fixtures/self-hosting/sh-03/accepted/current-decode-mixed.gravity).
  Inspect the actual [lexical/product assertions](../../bootstrap/clojure/test/gravity/self_hosting/sh03_current_lexical_products_test.clj),
  [Reader adapter cases](../../bootstrap/clojure/test/gravity/self_hosting/sh03_reader_adapter_test.clj)
  and [intake/assembly cases](../../bootstrap/clojure/test/gravity/reader/artifact_test.clj)
  before choosing a focused selector. Their historical names do not grant a
  current pass, and importing the namespace may itself start held execution.

The following is the selected task's case design, not a second task/status ledger.
Read the complete [L1](../../docs/phase-01-core-language/011-l1-surface-syntax-specification.md)
and [C2](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md)
sections when binding the cases; no row below substitutes for their clauses.

| Source family / output | Positive case and observable product | Negative case or remaining obligation |
| --- | --- | --- |
| Source units, decoding and identity | Both extensions; zero-form source and normal namespace unit distinguished; mixed CR/LF/CRLF, supplementary scalars, explicit roots/options and relocation preserve exact source bytes, coordinates, hashes and supplied-path provenance. | Malformed UTF-8 and unsupported source extension reject at intake. Changed bytes/root/options/policy invalidate affected identity; ambient working directory cannot supply identity. Preserve the accepted finite decoding receipt. |
| Lexical elements, literals and collections | All enabled nil/boolean/integer/radix/decimal/ratio/string/character/symbol/keyword/list/vector/map/set families, raw facts and semantic-value joins; ordinary `(comment ...)` remains a form. Preserve exact/deferred numeric components and genuine target-format rules. | Malformed identifiers, numeric candidates, escapes/scalars, delimiters, odd maps and decidable duplicate sets reject with owning diagnostics. `1/0` is lexically valid and deferred for semantic judgment. A suffix grammar is covered only when its governing numeric contract enables it. |
| Abbreviations and metadata | Quote, syntax quote, unquote, splice-unquote, metadata and deref retain explicit syntax shapes, wrapper targets and generated-origin links. Unknown metadata is preserved as data. | Reject invalid placement, unattached/invalid metadata and invalid wrapper references. Reading metadata cannot execute it. Namespace qualification and hygiene remain with Syntax/Expand. |
| Namespace and later semantic surfaces | Every L1 namespace clause has its required list/keyword/value shape; type/effect/capability/profile/target/safety declarations and reserved special-form spellings survive as data. | Malformed namespace shape retains `L1-NS-SHAPE` ownership and C2 containment. Name existence, type/effect/profile legality and unsafe legality are later-stage judgments; syntax-valid illegal forms must reach those owners. |
| Tokens, flat form graph, seeds and source map | Retained/filtered trivia modes, ordered roots, all token/form/value/parent/child/payload/metadata references, exact source slices and faithful seeds are observable. Actual fresh Syntax consumes the Reader products. | Missing, duplicated, stale or substituted references and origins reject at the authenticated boundary; a helper-created graph is not producer evidence. Reuse the accepted lexical/origin and fresh-Syntax receipts only at their recorded scope. |
| Extensions and invocation records | Registered pure and explicitly granted handlers consume/return seeds through the ordinary Reader boundary; invocation artifacts bind actual policy, handler and granted effects. | Unknown/disallowed tags, hidden authority and ungranted effects reject. Standard inst/uuid dispatch helpers alone do not establish authenticated normal execution. This route remains with the existing extension task and held integration owner. |
| Incremental hashes and diagnostics | Source/token/form/seed/extension/diagnostic hashes preserve their distinct inputs; trivia changes affect the specified modes. Diagnostics retain source id/span, raw spelling, options, tag and remediation, with token/form id when available. | Tampered joins, changed implementation binding and replay substitution cannot reuse validation. No source-size, spelling-length, depth, width, node or construction-work allowance rejects valid input or defers normalization. Actual host failure is recorded as execution failure. |

### Required result and evidence route

For every independently meaningful L1/C2 clause, bind its exact source location
to the invoked definition, concrete positive/negative fixture and observable
product or diagnostic in the implementation change. If coverage is absent,
include the concrete missing case and route its correction to the existing
Reader outcome above; do not mark it supported from source text or a fixture
filename. Reconcile historical `depth-513`, numeric-equivalence-limit and other
quota-only expectations against current C2 and AGENTS.md; do not restore their
allowances or treat their old rejection as conformance. Numeric equality,
Unicode, arity/index, native metadata/callback and true-cycle rules survive.

Keep the three accepted rows and their exact receipts. Record new observed
commands, exits, source/implementation/fixture identities, actual products,
unrun cases, failures and residual seed/host boundaries in the existing
[evidence record](evidence.md), then seek required independent acceptance through
the existing governance route. The selected task stays Open until its own
yes/no criterion is met; the coverage matrix in this plan is not that evidence.
No new implementation file, namespace or API name is introduced by this plan.

The [native integration hold](pure-native-admission-evidence.md#handoff-and-remaining-gate)
and [normal dispatch limits](normal-dispatch-evidence.md#remaining-gates) remain
binding. Source inspection and case design can proceed; genuine Reader/Gravity/
Bootstrap, paired integration, provider, lease, probe and shared-6343 execution
remain unrun here. Do not dispatch those tests, reassign their owned paths or
retry the held transaction under this selected task. The independent review
and coordinator integration boundaries remain open.

## Historical task-authority-transfer plan

# Implementation plan: Reader task authority

Spec: [spec.md](spec.md) | Owner role: Reader maintainer.
Input revision: `c40d74aded3a019ff2bdeb1f4050cf95dbb11c1e`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification](../../docs/development-verification.md),
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md),
and [owning contract](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md).
Only the editable implementation task location changes, not language semantics
or authoritative compiler migration.

## Canonical work and handoffs

- Implementation work: [the seven authoritative records](tasks.md#implementation-tasks).
- Roadmap navigation: [sole task ledger](tasks.md#implementation-tasks).
- Release criteria: [owning acceptance checklist](../../docs/component-roadmaps/reader.md#acceptance-checklist).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).

Reader owns source bytes and lexical/seed origins; Syntax owns syntax context
and hygiene. Reader can begin with explicit source and policy records; Build
completion and final self-hosting are not start prerequisites. Existing
interface decisions remain with the Reader/Syntax maintainers. Record order is
not dependency evidence. No additional dependency graph is manufactured here.

## Approach and exact files

Edit only the marked implementation region in tasks.md for task progress.
Each record has one stable semantic ID, title, checkbox, exact evidence/status
and exact yes/no acceptance text. The known seven-ID set guards accidental row
loss or substitution; an intentional task-set change requires explicit review
of that set, not positional renumbering. Titles cannot collide.

The Reader roadmap links directly to these records. Historical receipts, release
criteria and anchors remain on the roadmap; the historical scaffold record stays
frozen outside the task region and is not counted as implementation work. No
independent machine-readable status ledger is added.

## Validation and evidence

Run from repository root:

```bash
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -M tools/validate_gravity_docs.clj
clojure -M tools/validate_full_language_roadmap.clj
```

Check `git diff --check`. No compiler behavior tests or full migration acceptance
are inferred from these structural checks.

## Delivery and remaining work

Prepare an immutable candidate for independent review. Record observed checks
separately from review and integration. Future real implementation changes
update the authoritative record; the roadmap continues to link to that ledger.

## Names, callers and compatibility

Preserve existing semantic IDs and roadmap anchors. The dashboard follows the
editable tasks.md source without changing task status or counts. No runtime API,
load order, manifest, hash, schema or protocol identifier changes.
