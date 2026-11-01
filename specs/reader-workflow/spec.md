# Reader implementation specification

Implementation scope added under Matt's 2026-11-01 project-wide reconciliation
request. Reader maintainers retain implementation ownership. The original
workflow-transfer specification follows unchanged as historical scope; its
delivery criteria do not define Reader completion.

## Implementation outcome and interfaces

Implement [reader/source-to-syntax-seeds](../../docs/component-roadmaps/reader.md#release-outcome):
declared source bytes become faithful lexical products and syntax seeds,
without evaluating source. [L1](../../docs/phase-01-core-language/011-l1-surface-syntax-specification.md)
and [C2](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md)
own the complete behavior; this section selects implementation responsibilities
and does not replace their rules.

Inputs are bytes, encoding, explicit project identity and relative path, actual
supplied path/extension, reader options/features and registered extension policy.
Outputs are the source unit, tokens, flat form graph, exact/deferred literals,
syntax seeds, origins, invocation records, diagnostics and deterministic hashes.
Syntax owns completed syntax objects and hygiene context. Numeric, name, type,
effect and profile legality remain with their later owners; syntax-valid forms
must survive until those owners can diagnose them.

## Source-to-task implementation routes

The [marked task region](tasks.md#implementation-tasks) is the only editable
implementation ledger. References below use its existing task IDs; they add no
new tasks or completion states. The aggregate requirement reconciliation retains
responsibility for finer source-facet entailment and cross-component overlap.

| Canonical behavior | Existing Reader task suffix | Required implementation and acceptance boundary |
| --- | --- | --- |
| Complete L1 surface and C2 products | `contract-coverage-reconciliation` | Join every required family/output to the actual owner implementation and accepted/rejected fixture, including namespace-clause shape, identifiers and numeric candidates. An unsupported family remains an explicit failing case; links or source declarations alone cannot establish support. |
| L1 Source Units; C2 Source Unit Record | `decode-and-source-identity-closure` | Both extensions retain actual-path provenance, explicit-root identity, strict UTF-8 and exact coordinates. Malformed encoding and unsupported extensions reject at the original source boundary; relocation cannot substitute ambient working-directory state for identity. |
| L1 Lexical Elements through Namespace Form; C2 Token Stream through Literal Decoding | `lexical-products-and-origins-closure` | Exercise complete literals/collections, abbreviations, metadata, trivia and exact/deferred values. Reject malformed identifiers, numeric candidates, delimiters, strings, map arity, decidable duplicate sets and malformed/unattached wrappers with source-located owning diagnostics. |
| L1 reader-extension rules; C2 Reader Extensions; L12/L15 | `extension-policy-closure` | Execute registered pure handlers and explicitly granted handlers through the ordinary Reader boundary. Unknown/disallowed tags, hidden effects and ungranted calls reject; invocation products preserve the actual policy, handler and granted effects. A registry or injected dispatch fixture alone is insufficient. |
| C2 authenticated Form Tree, Literal Decoding and hashing requirements | `authentication-and-quota-removal-closure` | Independently reconstruct complete products from retained original bytes. Reject altered, missing, duplicate or stale graph/hash joins; accept well-formed inputs without project capacity/work refusals. Preserve intrinsic native, Unicode, index, arity, metadata and target-format rules. |
| C2/C3 producer-consumer boundary | `fresh-syntax-handoff` | Actual fresh Syntax consumes actual Reader output, preserves spans/metadata/origins and rejects malformed or substituted producer products at the owning stage. A cached historical pass cannot authorize a changed invocation. |
| L1/C2 conformance and component release acceptance | `component-release-evidence` | Bind the complete claimed population, actual source/implementation/fixtures/runtime identities, diagnostic goldens, current handoff and residual host boundary to independently accepted evidence. Finite accepted rows do not close the component. |

Every suffix above belongs to the unchanged
`reader/finite-remaining-work/` task namespace. Current status and evidence must
be read from the ledger rather than inferred from this specification.

## Workflow entry and completion

For `speckit-plan`, select one existing unresolved task and its actual input and
consumer closure; preserve applicable accepted evidence instead of redoing it.
The plan must identify the owning source paths and real accepted/rejected
programs. For `speckit-tasks`, refine that same authoritative task as needed;
do not generate a second checklist that competes with the marked ledger.
`speckit-implement` must change or verify actual Reader behavior and emit the
required products, then exercise affected Syntax/CLI composition when relevant.

Declared source/policy inputs and the existing seed are sufficient to start
ordinary Reader development. Completed Build, all compiler stages and a
self-hosted binary are not start prerequisites. Actual composition requires the
real selected producer/consumer; component acceptance requires the full
[acceptance checklist](../../docs/component-roadmaps/reader.md#acceptance-checklist).
Preserve existing writer reservations and restricted operations; a blocked
extension or replay path stays visible rather than being reassigned here.

The L1 literal paragraph's normalization-work-bound wording is superseded by
AGENTS.md and current C2: exact/deferred numeric treatment is semantic, not a
spelling-length or estimated-work escape. Canonical accounting-text cleanup
remains with the existing [accounting-removal authority](../accounting-removal/spec.md).
This section records the applicable decision without claiming that cleanup,
source-facet reconciliation, implementation, independent review or integration
is complete.

## Historical workflow-transfer specification

# Feature specification: Reader implementation workflow

Status: authority-transfer candidate; integration pending.
Owner role: Reader maintainer. Existing implementation reservations are unchanged.
Input revision: `c40d74aded3a019ff2bdeb1f4050cf95dbb11c1e`.

## Purpose and canonical contracts

Use Spec Kit tasks for the seven real Reader implementation outcomes, not the
five historical documentation-migration checkboxes. This changes task editing
location only; it does not close an implementation task or alter its evidence.

- [Owning compiler contract](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md) remains normative.
- [Upstream language contract](../../docs/phase-01-core-language/011-l1-surface-syntax-specification.md) remains normative.
- [Implementation tasks](tasks.md#implementation-tasks) is the sole editable task ledger.
- [Component roadmap](../../docs/component-roadmaps/reader.md) retains release acceptance, interfaces and historical receipts, with a direct link to the task ledger.
- [Canonical inventory](../../docs/document-inventory.json) owns contract identity.

## User scenarios and acceptance

### US1 - Find the next real component task

A maintainer selects an existing semantic task ID in tasks.md and changes its
actual evidence/status only when supported by the implementation outcome. The
task title, current evidence/status and yes/no completion criteria remain in
that record, and the roadmap links directly to it. No second editable
implementation ledger is introduced.

### US2 - Distinguish migration from implementation completion

The frozen migration record is historical, outside the implementation region.
An unchecked implementation task may be open, partial or unassessed; exact
status text is retained. A checked task requires explicit `Accepted;` status
and an immutable accepted-outcome receipt, independently reviewed by its owner.
The ledger's status and checkbox remain explicit, but structural navigation
cannot grant semantic acceptance.
The accepted Reader-to-Core invocation pilot remains partial component evidence,
not full Reader surface, extension or release coverage.

## Scope and compatibility

Own the four Reader workflow artifacts and the Reader roadmap. No other component, normative contract, compiler
behavior, source schema, diagnostic, wire ID or historical pilot is changed.
Existing roadmap headings remain compatible with the dashboard extractor. No external Specify CLI execution or automatic workflow
discovery is claimed. There is no new overall percentage or dependency edge.

## Evidence and outcome ownership

The Reader maintainer owns implementation evidence under existing reservations.
The task migration does not assign a person or transfer compiler ownership.
Independent review and integration remain separate from local validation.
Exact observations and limits belong in [evidence](evidence.md).

## Unresolved items

This authority transfer does not close any implementation outcome. Existing
component decisions remain in the roadmap. Other components are outside this
candidate.

## Implementation naming review

`reader-workflow` and its component roadmap are semantic names. Existing
`reader/*` task IDs are preserved; no compatibility identifiers change.
Follow [semantic naming guidance](../../.specify/semantic-names.md).
