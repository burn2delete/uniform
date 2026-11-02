# Expand implementation planning entry

Select one existing unresolved task from [tasks.md](tasks.md#implementation-tasks)
and reconcile this plan against the implementation behavior in [spec.md](spec.md).
The original task-transfer plan below remains preserved. Determine the owning
Gravity algorithm, temporary Clojure transport and current test selectors from
the [stage ownership table](../../docs/bootstrap/stage-owned-namespaces.md) and
actual callers; do not grant semantic authority to a host evaluator or a
manifest-only compatibility projection.

For the selected task, identify the complete macro/form population, real syntax
inputs, macro environment/dependencies, source-selected profile/target and any
build-provider/grant/replay contract. Name the actual expanded products and
their downstream consumer. Add concrete pure/nested/generated positive cases
and applicable non-syntax, phase, capture, origin, hidden-effect, taint, unsafe,
cache-drift and generated-program negative cases with stable diagnostics.
Preserve existing source provenance and compatibility IDs.

Use [development verification](../../docs/development-verification.md) for
focused owner checks and affected Syntax/Resolve/Core/legality composition.
Actual source execution and real verifier/replay behavior are required for the
claimed outcome; fixture translation, supplied reports and metadata cannot
replace them. Any restricted provider/replay path or reserved writer remains
an explicit unfinished acceptance boundary. Record evidence in the existing
package and update only its authoritative task; do not create a parallel
implementation checklist. Independent review and integration remain separate.

## Provider and capability dependency interface

Use the [provider and capability producer and consumer order](../auth-workflow/plan.md#provider-and-capability-producer-and-consumer-order) and existing selected-result table. Consume actual compatible provider/trust/build-grant/gate and exact L6/L12 hermetic/replay premises before macro P action; later generated Q and replay/denial results remain separate. A replay exception is no grant or hermetic waiver.

Task authorities and frozen review subjects stay unchanged. Selected interfaces remain proposed; current independent acceptance and implementation are unassessed. Preserve all actual restrictions.

## Provider and capability source interface

Use the [complete provider and capability source criteria](../auth-workflow/plan.md#complete-provider-and-capability-source-criteria). The existing compile-time gate checks actual build-phase provider authority, replay/isolation and exact hermetic premises before invocation. Program P's admission remains distinct from checking generated Q afterward.

Historical finite reviews remain scoped; current whole-source/interface review and implementation acceptance remain pending. Preserve all actual holds and existing task authority.

## Source effect dependency interface

Use the [source effect and handler dependency order](../effects-workflow/plan.md#source-effect-and-handler-dependency-order) and the existing selected-result table. Keep checked macro program P distinct from generated caller Q. Expand3's early enforcement/replay gate precedes Expand1 execution; later actual action logs feed recording. An output cannot supply its own prior admission; exact hermetic premises remain.

Task bodies/statuses and previous qualified evidence stay unchanged. Current source/dependency entailment and implementation acceptance remain pending; preserve all actual operation holds.

## Source effects and typed-handler interface

Use the [complete source effect and handler criteria](../effects-workflow/plan.md#complete-source-effect-and-handler-criteria) with the existing task authority. The existing expand3 task owns actual compile-time boundary enforcement and input/output/phase/replay records. Auth issues grants, Effects infers behavior and Build packages the logs; listed grants do not prove performed work.

The frozen finite review is qualified to its exact source predicates; complete L6/current source entailment, independent review and implementation acceptance remain pending. Preserve every actual held operation.

## Historical task-authority-transfer plan

# Implementation plan: Expand task authority

Spec: [spec.md](spec.md) | Owner role: compiler expand maintainer.
Input revision: `023ec47d86004fc829ae7dc5753b715ca4a653dc`.

## Contract alignment

Follow the [constitution](../../.specify/memory/constitution.md),
[development verification](../../docs/development-verification.md),
[compiler architecture phase](../../docs/phase-06-compiler-architecture/README.md)
and [owning contract](../../docs/phase-06-compiler-architecture/083-c4-macro-expansion-engine-design.md).
Only task editing authority changes; compiler and language semantics do not.

## Canonical work and handoffs

- Implementation work: [five authoritative records](tasks.md#implementation-tasks).
- Roadmap navigation: [sole task ledger](tasks.md#implementation-tasks).
- Release criteria: [owning acceptance checklist](../../docs/component-roadmaps/expand.md#acceptance-criteria-yesno).
- Overall composition: [integration outcomes](../../docs/component-roadmaps/README.md#integration-outcomes).
- Input: Verified syntax, macro versions, namespace context and explicit build grants/replay inputs.
- Output: Expanded syntax, trace, generated origins, capture/safety declarations and cache identity.

Syntax schema and macro binding records are required inputs. Declared module
artifacts support lookup before full Resolve or Build release. Expansion
preserves hygiene, declared effects and provenance; generated programs require
the same downstream checks as handwritten programs. Record order adds no
dependency graph or start prerequisite.

## Approach and exact files

Edit the marked implementation region in `specs/expand-workflow/tasks.md`.
Preserve all five derived semantic IDs, titles, status and exit evidence.
Each record has a checkbox; the known ID set prevents row loss/substitution,
and its ID must agree with the retained phase/title slug. An intentional task-set
change needs review of that set, not positional renumbering.

## Validation and evidence

Run from the repository root:

```bash
clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx512m -M tools/validate_gravity_docs.clj
clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj
```

Check the five task IDs, statuses, preserved criteria, retained historical
checkboxes and links. Check the affected Markdown files for ASCII and local
file/heading links, `git diff --check`, then run integration preflight with the
exact final base/commit/tree. Record commands and results in [evidence](evidence.md).
Structural checks are not compiler semantic validation or migration admission.

## Delivery and remaining work

Publish a draft for independent current-head review. Record review separately
from implementation and integration. Merge only after the maintainer confirms
independent current-head review. Future implementation updates change the ledger;
the roadmap remains a direct navigation link. No implementation task closes here.

## Names, callers and compatibility

Preserve existing anchors, links, explicit IDs and task text; consumers continue
following the same direct task-ledger link. New semantic tool filenames/namespaces
follow existing Reader conventions. No caller, load order, runtime API, schema,
diagnostic, manifest or wire ID changes. No global catalog or dashboard is edited.

## Additional contract interfaces

The [central contract mapping](../../.specify/contract-mapping.md) records
these scoped dependencies. They add navigation and evidence obligations, not
implementation tasks, accepted outcomes or new semantic authority. Existing
task IDs, statuses, acceptance text and historical receipts retain their scope.

- [L16](../../docs/phase-01-core-language/026-l16-alternative-macro-system-contract.md#requirements): Reference-equivalence conformance preserves L4 expansion semantics, hygiene, phase separation, declared/granted/replayable build effects and generated-origin chains. Compiler API retains provider registration; generated forms still undergo ordinary downstream validation.
- [SAFE12](../../docs/phase-02-safety/041-safe12-macro-safety.md#requirements): Produce hygiene/capture, macro safety declarations, build-effect traces and generated-origin/unsafe metadata; reject declaration/expansion disagreement. Legality retains downstream safety classification and generated code must pass normal checks.
