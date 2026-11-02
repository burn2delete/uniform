# Diagnostic implementation planning entry

Select one unresolved [existing task](tasks.md#implementation-tasks), preserving
the transfer plan below. Read [spec.md](spec.md), C15 and the actual producer
rule and consumer/redaction contracts. Resolve current stage/carrier/renderer
ownership through [development verification](../../docs/development-verification.md)
and the [stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md).

Name actual producing input/failure, source/generated/artifact locations,
available and nil context, semantic identity/facts, policy and consumer.
Construct a real failure plus relevant malformed-record, origin/ordering,
wording-change, redaction or view-parity cases. Assert semantic fields and
remediation categories; do not invent facts merely to satisfy the common shape.

Use focused owner checks and affected real producer/CLI/Build/editor/runtime
composition. Preserve exact diagnostic and compatibility IDs, private-data
boundaries and failed/unrun evidence. Refine only the selected task; a successful
rendering fixture does not establish all-stage integration or release.

## Foundation source criteria

The existing runtime/build/editor integration task now carries the preserved
`gravity/runtime-check-diagnostic-stability` clause from the
[foundation context](https://github.com/burn2delete/uniform/blob/df11890b21f71d3f246cc47e9f63d866239a0d68/specs/requirement-reconciliation/foundation-residual-interfaces.edn).
[D9 requires testable diagnosed runtime checks](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md#requirements);
[C15](../../docs/phase-06-compiler-architecture/094-c15-compiler-diagnostics-specification.md#golden-fixtures)
and [R12](../../docs/phase-08-runtime-architecture/123-r12-runtime-observability-and-diagnostics-design.md#requirements)
govern the actual structured result, stable semantic identity, source/generated
or manifest attribution, failed facts, remediation and redaction. Unavailable
early-stage context stays explicit; no invented profile/target/span is required.

Consume Legality's actual operation/check/proof/audit facts and Runtime's
selected service and execution products. Trigger the actual failure before and
after a relevant optimization and across each claimed supported provider;
compare rule, failed predicate, locations, profile/target and remediation.
Reject raw host-failure leakage, changed/missing attribution and erased checks
without surviving evidence. Assert structured fields rather than only display
text, and preserve actual check/event/artifact identities and permitted
redaction. Runtime owns check execution, semantic owners own the rule and Diag
owns common records/consumer consistency. The existing task remains Open.

## Historical task-authority-transfer plan

# Implementation plan: Diagnostics task authority

Spec: [spec.md](spec.md) | Evidence: [evidence.md](evidence.md)
Owner role: diagnostic infrastructure maintainer.
Input revision: `063989e153b88c00f3ac3f69c2f7a04b5981ab2d`.

## Contract alignment

Follow the retained L9/C15/R12 contracts in
[spec.md](spec.md#purpose-and-canonical-contracts), the
[constitution](../../.specify/memory/constitution.md) and
[development verification](../../docs/development-verification.md).
Stage owners keep rule semantics. Structured-record infrastructure, consumers
and public rendering retain their existing responsibilities; contract routing
is not exclusive ownership or implementation acceptance.

## Canonical task source

- Editable implementation authority: [four records](tasks.md#implementation-tasks).
- Compatibility view: [remaining tasks and evidence](../../docs/component-roadmaps/diag.md#remaining-tasks-and-evidence).
- Semantic finish line: [acceptance and integration](../../docs/component-roadmaps/diag.md#acceptance-and-integration).

## Approach and exact files

Move only the four real implementation rows to the marked ledger region.
Preserve IDs derived from component/heading/title slugs, exact titles, Open
statuses, evidence baselines and closing criteria. Preserve the complete original
tasks file as a frozen suffix, with its historical checkboxes unchanged.

The original first row contains two evidence links; validate and preserve both.
Four bootstrap links keep the same spelling in ledger and roadmap. The tooling
README uses ../../docs/phase-13-tooling-and-developer-experience/README.md in the
ledger and projects to ../phase-13-tooling-and-developer-experience/README.md,
keeping the same target. No duplicate link catalog is introduced.

Own the roadmap and four workflow artifacts. Shared mapping artifacts and other
package files are unchanged. Preserve stage ownership, base-carrier limits,
redaction, compatibility and release acceptance wording.

## Interfaces and dependencies

Reader/Syntax provide origins; semantic stages provide rule facts; Runtime,
Build, CLI and editor consumers share record identity. Each stage can integrate
without waiting for the full release. No source rule completion transfers to
this workflow. The base carrier's construction/throwing surface does not imply
schema, catalog, identity, ordering or rendering acceptance.

## Validation and evidence

Run from repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Compare the preserved IDs, task criteria, evidence targets, historical suffix and
retained contract/package navigation. Check the Markdown documents for ASCII and
file/heading links, then run whitespace and clean exact-base/head/tree
integration preflight. These checks establish structure, not compiler or
release acceptance.

## Delivery and remaining work

Independent review inspects all seven public files and the proposed PR body.
Publish a validated draft with exact identities for maintainer current-head
review. Normal integration follows that disposition and actual repository gates.
All four implementation outcomes remain open; no compiler semantic suite is
required for this task-authority transfer.

## Names, callers and compatibility

No compiler/runtime imports, schemas, diagnostic IDs, wire identities, manifest
hashes, redaction rules or public APIs change. Planning IDs in canonical links
remain traceability only.
