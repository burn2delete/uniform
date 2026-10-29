# Runtime source-family task interface proposal

Status: proposed, independent source review and central adoption pending.
Owner role: runtime source-family implementation owner.
Scope key: `requirement-reconciliation/phase-08-runtime-architecture`.

## Declared change

The 12 original R1-R12 documents retain their bytes. The five existing Runtime
implementation tasks retain IDs, titles, evidence baselines and Open status;
their closing evidence now names the concrete source predicates, negative
cases, artifacts and input dependencies omitted by the earlier broad clauses.
The Runtime roadmap is generated from those same records.

[runtime-family.edn](runtime-family.edn) is the complete bounded proposal, not a
new task ledger or adopted reconciliation manifest. It conserves 876 occurrences,
1,021 facets and 43 existing source-backed semantic units. Of these units, 28
already had partial task references; 15 lacked a primary stable task reference.
All 43 receive proposed source-qualified bindings to the amended five tasks.
No requirement obtains implementation acceptance from this change.

## Exact inputs and lineage

- Current source/task baseline: `6276c94b0b753c888ef8fecc794bcc81245ea3b3`,
  tree `9f464ed0f03866bd4978e4213a488f3b1ae48683`.
- Original source baseline: `a1b991be499d50579a8b4366976c519ae8949224`.
  The proposal carries each assigned original/current document hash, actual
  occurrence ID, byte/line span and raw slice hash.
- Frozen inputs: PR554 head `53ae6a403b20d15155bb5b04911fe7ac0092b921`,
  `source-task-inputs.edn` SHA256
  `423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94`.
- Proposal format: PR445 head `e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30`,
  SHA256 `69ec49fb954ba66ce86621d5dfac3a4cb0b514b84915277b86944ae2ec857adc`.
- Applying manifest: preserved overlay SHA256
  `aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`.
  This differs from the committed PR445 manifest SHA256
  `f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`.
- Exact pure occurrence API: `c0dc5790197a1bbd3a8aa285e5458c272d877220`,
  `tools/requirement_reconciliation.clj`, imported without executing its CLI.
- Delivered Language295 normalized application and context routing are consumed
  by hash and all 43 inherited routing records in `:preserved-lineage`.
  Conditional record/replay, host loading, FFI and no-runtime alternatives
  retain their existing boundaries. Reviewed historical aliases remain intact.
  Foundation82's two task-gap wording refreshes remain central composition
  inputs; this proposal does not overwrite their fields.

## Remaining source-qualified decisions

The 44 inherited R8 mixed accounting facets remain individually unresolved,
with their exact stable IDs in `:scope-decisions` and each affected binding's
`:unresolved-facet-ids`. The known unit owners are
`runtime/ai-memory-policy`, `runtime/durable-effect-replay`,
`runtime/model-provider-execution`, `runtime/nondeterminism-recording`,
`runtime/service-policy-enforcement`, `runtime/tool-provider-execution` and
`schema/boundary-validation`. Removed project accounting cannot become an
admission requirement; external provider billing/account restrictions remain
distinct. Surrounding taint, validation, grants, review and replay predicates
remain required. No facet is bulk reclassified or discarded.

The target/runtime-family matrix decision remains unresolved: each claimed
family must provide its own profile/target/ABI/provider and conformance evidence.
Selected compiler services do not establish all-platform or product completion.
Existing atomicity/nonoverlap states and source dispositions remain unchanged.

## Composition and rejection controls

The 43 edits carry collection, ID, field path, exact predecessor record hash,
before-presence, before and after values. They add only a scope-qualified field
to the preserved records. Five task amendments also carry exact before/after
acceptance and task-source hashes. Stale context, duplicate application, missing
or renamed semantic/task IDs, wrong source/hash/marker, changed alias context,
lost negative cases and hidden unresolved facets reject.

If central Language295 composition has retired or split an original unit, its
record guard must fail. The central owner then reconciles only the exact R1-R12
facets with the already nominated target IDs. This patch cannot resurrect a
retired unit, apply an alias, or transfer another family's source context.

## Observed validation

Observed on 2026-10-28, with the preserved applying input above:

- `clojure -J-Xmx512m -M tools/runtime_requirement_family_test.clj APPLYING_MANIFEST`:
  2 tests, 20 assertions, zero failures/errors, including positive conservation
  and mutation rejection controls.
- `clojure -J-Xmx256m -M tools/runtime_task_projection_test.clj`:
  4 tests, 67 assertions, zero failures/errors.
- `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj`:
  240 documents, 19 phase indexes, ASCII and placeholder checks passed.
- `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj`: passed.
- `clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`:
  6 tests, 11 assertions, zero failures/errors.
- Runtime projection and `git diff --check`: passed.

One scratch generation and one large historical-input read exhausted the 256 MiB
heap. Streaming input and a 512 MiB heap completed the source-only checks; those
failed attempts confer no evidence. No compiler/runtime/provider execution,
historical heavy test, grant change, migration or release acceptance is claimed.

## Delivery and adoption

Keep the candidate draft. The reconciliation owner receives its exact commit,
tree and artifact hash and routes independent review through the existing queue.
No new reviewer or ledger is created here. Central acceptance must reconcile the
new task-source hashes, delivered normalization deltas and source-qualified
bindings before canonical adoption. All implementation stays Open/unassessed;
aggregate adoption, nonoverlap and counts remain with the canonical owner.
