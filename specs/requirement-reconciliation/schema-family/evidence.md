# S1-S9 task interface candidate

Scope key: `requirement-reconciliation/phase-10-schema-data-and-interop`.
Source-family owner: this isolated phase-10 implementation worker. Canonical
adoption and review routing remain with the reconciliation owner; integration
remains coordinator-owned. This is a proposed task interface patch, not an
accepted implementation or a second task ledger.

Input task revision: `6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`. Original S1-S9 sources are unchanged.
The existing `schema` literal task remains Open; all seven domain task IDs,
statuses, surrounding task blocks and historical migration records are retained.
Only its source-specific acceptance text and deterministic roadmap projection
change. The plan names producer/consumer dependencies.

## Delivered data and conservation

[proposal.edn](proposal.edn) uses the existing bounded-family proposal format.
[lineage.edn](lineage.edn) preserves the exact delivered family excerpts and
input hashes. The committed PR445 manifest is `f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`;
the preserved applying manifest is `aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`.
These are different inputs. The shared frozen source/task snapshot comes from
PR554 at `53ae6a403b20d15155bb5b04911fe7ac0092b921`, digest
`423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94`.

All nine documents, 511 source/layout occurrences and 538 original facets retain
their semantic/source IDs, UTF-8 byte spans, line ranges and raw hashes.
The 19 original candidate records are archived as inputs. Delivered Language295
data retires nine of those records and supplies successor and peer contexts;
the bounded view contains 34 active inherited IDs touching S1-S9. These are
source incidence counts, not a project denominator or completion measure.
No delivered split, alias or accepted historical evidence is counted again.

The proposal supplies 56 before/after field operations for 28 inherited
`schema/` IDs, appending the exact existing schema task interface and replacing
the stale missing-interface description with a partial-interface qualification.
All other fields, historical support, prior task links and conditional source
roles survive. New task references are pinned to the actual task bytes.

The Language295 application was rooted after a Safety composition. Its expected
occurrence memberships use `safety/safe10/runtime-authority` where the preserved
applying history still uses `runtime/service-policy-enforcement` in 21
occurrences of S4-S6. Both exact predecessor lists and the delivered operations
remain in `:composition-holds`; mismatched records are retained and explicitly
held. This candidate neither overwrites them nor decides that the two contexts
are aliases. The later Foundation applicability sidecar changes no S1-S9 facet;
its identities and conditional-role qualification are retained.

## Remaining interfaces and semantic qualifications

All implementation acceptance is unassessed. The 28 schema IDs gain a grouped
Open interface, not independent semantic acceptance or executable evidence.
The remaining active contexts are:

- `integration/build-input-capture`: exact S8 capture/rejection entailment still
  needs the Build owner; generic migration tasks do not close it.
- `language/types/project-schema-static-type`: actual static type projection
  remains a Types consumer, separate from schema normalization/validation.
- `runtime/foreign-call-adaptation`: actual foreign invocation remains separate
  from S7 layout and binding-input production.
- `runtime/policy-safe-observation` and `runtime/service-policy-enforcement`:
  inherited partial Runtime interfaces remain qualified, not accepted.
- `safety/safe10/runtime-authority`: retain the delivered conditional Safety
  context and central composition hold; no ownership equivalence is inferred.

The S2 decoder clause mentioning recursion limits is retained byte-exact.
Removed work/input quotas cannot be reintroduced as generic bounds; meaningful
representation, UTF-8, shape and validation conditions survive. Exact mixed-clause
adjudication remains explicit. Cross-family equivalence and actual invocation
remain central review questions, not reasons to erase source facets.

## Local validation

Run from the repository root with the immutable Git inputs available:

```bash
clojure -J-Xmx768m -M specs/requirement-reconciliation/schema-family/build-proposal.clj --check
clojure -J-Xmx256m -M tools/domains_task_projection_test.clj
clojure -J-Xmx256m -M tools/domains_task_projection.clj --check
clojure -J-Xmx256m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
clojure -J-Xmx256m -M tools/validate_gravity_docs.clj
clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj
git diff --check
```

The bounded build/control run passes 15 negative controls: missing source,
wrong source hash, lost occurrence/facet/unit, missing/renamed task path,
wrong task hash/marker/ID, lost negative condition, alias with different context,
false acceptance, lost composition hold, and incompatible field predecessors.
The Domains suite passes 6 tests / 103 assertions. The source-only controls do
not execute compiler, producer, runtime, broker, release or migration proofs.
Historical receipts keep their original dates and scope.

Independent review is pending coordinator routing. No new reviewer or duplicate
review is assigned. The task-source change invalidates any claim that earlier
live-source fingerprints describe these bytes; historical SH crosswalk pins
are preserved. No SH mapping acceptance is renewed by this candidate.

## Adoption

Review the exact candidate and source-role lineage. Compose the 21 occurrence
predecessors centrally before applying any delivered membership deltas. Apply
new requirement field operations only when the actual field presence/value
matches `:before`; preserve incompatible later deltas as holds. Bind the final
task source commit after integration, then regenerate central projections as
needed. Never replace the aggregate manifest with this family view.

The initial local proposal digest `481fbe19bd0854f239c0c4db2aa7703c3ed262edfe77a178a23e3c8312a1b7ae`
is superseded working history: it predates the supplied Language295 lineage and
is not an adoption input. The current proposal is the file in this candidate.

New tooling names describe schema-family proposal construction. No source,
protocol or diagnostic identity is renamed; original source documents and
other families' occurrence populations are outside this patch.

## CI successor correction

Candidate `e9c85cca018bedf30cc6c6f090058180de8b9738` remains immutable history.
CI run `40800243836` reported two blocking static findings in job
`123206336962`: namespace/file mismatch at row 1 and unresolved dynamic
`source-occurrences` at row 56. The successor uses the actual `build-proposal`
namespace and binds the preserved pure occurrence API through a normal `def`.
No lint settings, rejection controls or occurrence implementation change.
The bounded controls still pass with the same proposal digest. Local clj-kondo
is unavailable; fresh CI owns static verification of the correction.

Projection job `123206336833` also rejected changed task-source spans. A local
SH crosswalk check independently rejects source/routing fingerprint drift.
Those failures are preserved, not reported as passing aggregate projections.
The reconciliation owner was sent the exact predecessor/successor sources for
composition. Old task digest at the task base is
`ac5436b11b3d5a9918c4471a9237fd9f8358eadd3f242abed78c7a7fe92d7cd4`;
candidate task digest is
`28c21ce9b7d89cf1089a2f53df2d9213c14e2086494a6d41c6d798aad2c8a055`.
The generated Domains roadmap digest is
`9fbc26c26af46074f31ca9753d9160608a46e8dea270bb14cbdecae4ad26328e`.
The existing SH review is not applicable to those new task bytes. Exact
source-version metadata, predecessor history and review must be composed by
the canonical owner before this draft's aggregate projection gate can pass.
