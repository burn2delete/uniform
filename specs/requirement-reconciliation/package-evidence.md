# Package source-family candidate evidence

Date: 2026-10-28. Owner role: bounded source-family reconciliation owner;
Build/package semantic ownership remains the existing maintainer role.

## Scope and immutable inputs

Scope key: `requirement-reconciliation/phase-12-build-package-and-artifact-system`.
Only the 12 original PKG1-PKG12 sources are assigned. Phase 18 and other product
extensions are excluded. Input main is
`6276c94b0b753c888ef8fecc794bcc81245ea3b3`, tree
`9f464ed0f03866bd4978e4213a488f3b1ae48683`.

The source/task input snapshot is PR554 head
`53ae6a403b20d15155bb5b04911fe7ac0092b921`, path
`specs/requirement-reconciliation/source-task-inputs.edn`, SHA256
`423dd6ec37f6d8f461de4a24d325033902ccce3249187073b796065fa3ca5d94`.
The exact proposal format at PR445 head
`e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30` is SHA256
`69ec49fb954ba66ce86621d5dfac3a4cb0b514b84915277b86944ae2ec857adc`.
Occurrence identities are rechecked using only the documented
`source-occurrences` defn from
`c0dc5790197a1bbd3a8aa285e5458c272d877220:tools/requirement_reconciliation.clj`.

The guarded edits use the published PR445 manifest, SHA256
`f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`.
The preserved uncommitted manifest is a distinct input, SHA256
`aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`.
A local read-only comparison verified all 70 PKG requirement records equal
between those inputs. That observation does not establish whole-manifest
equivalence or authorize replay against a later changed record.

## Finite delivery

[The family proposal](package-family.edn), SHA256
`fe5b63e7bbec43f285395ec67513c39ee85bad13b351228939dfb84dc8859bfc`,
preserves 70 original semantic candidate IDs, 1,255 exact facets and all 355
nonlayout source occurrences/dispositions. All 12 sources retain original and
current byte hashes. Normative facets keep their source occurrence IDs, byte
spans, raw hashes and unit associations; nothing is dropped to close a gap.

There are 128 guarded field operations for 64 proposed task bindings. Each
operation names collection, ID, field path, presence and exact before/after
values. They affect only task-reference/task-gap fields, retaining the entire
prior gap. No operation changes atomicity, nonoverlap, historical support,
source facets, acceptance or another family's record. All 70 implementation
assessments remain unassessed. Four equivalence proposals retain target IDs
and source-specific context for central adjudication.

The 65th bound candidate is the conserved PKG5 yank/revocation interface.
[Its already integrated receipt](../build-workflow/yank-interface.edn)
retains its four facets, independently reviewed task amendment and historical
rebuild exception. This candidate does not replay its two-field amendment or
reclassify that review as resolver implementation acceptance.

Five explicit missing interfaces remain:

- `behavior/pkg1/project-entrypoint-resolution`
- `behavior/pkg6/derived-capability-manifest`
- `behavior/pkg6/request-grant-separation`
- `behavior/pkg6/runtime-permission-audit`
- `behavior/pkg6/transitive-deny-precedence`

[The proposed acceptance addendum](package-interfaces.md) supplies concrete
positive/negative conditions, artifacts, diagnostics and producer dependencies
for the five existing Build task IDs. It is prepared for central adoption into
the existing Build plan. Its destination hash is pinned in the proposal.
Canonical task sources, generated roadmaps, SH pins, original sources and
existing evidence files are unchanged. No second task population is created.

## Observed controls

All commands below exited 0 on this candidate's source content:

| Command | Observed result |
| --- | --- |
| `clojure -J-Xmx768m -M tools/package_requirement_patch.clj --prepare` | Prepared only the family proposal |
| `clojure -J-Xmx768m -M tools/package_requirement_patch.clj --check` | 12 documents, 70 candidates, 1,255 facets, 128 operations, five missing interfaces |
| `clojure -J-Xmx768m -M tools/package_requirement_patch_test.clj` | 4 tests, 26 assertions, zero failures/errors |
| `clojure -J-Xmx256m -M tools/build_task_projection.clj --check` | Current Build task projection unchanged and valid |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | 240 documents, 19 phase indexes, ASCII and placeholder controls pass |
| `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj` | Passed |
| `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | 6 tests, 11 assertions, zero failures/errors |

Negative controls reject stale/duplicate patch application, nonexistent IDs,
wrong field presence/value, missing source, wrong source/facet hash, invented
occurrence, lost negative facet, renamed/missing task path, wrong task
hash/marker/ID, context-erasing alias substitution and unsupported acceptance.
The positive control compares exact source occurrences against the pinned API
and validates every facet byte slice and applying task identity.

Preparation initially failed because the family artifact directory was absent;
the directory was created and preparation then succeeded. No source mutation
or denied operation was retried. Only cheap source/fixture tooling ran; no
compiler, resolver, producer, signing, broker or runtime execution is claimed.

## Review and adoption boundary

Independent review is pending coordinator routing to an existing eligible
reviewer. No reviewer was created or independently assigned by this worker.
The canonical reconciliation owner alone composes the family with preserved
PKG5 and delivered Foundation/Math/Compiler decisions, adjudicates cross-family
equivalence and accepts the exact patch. Conflicting before values must refuse
application and return for explicit composition; no overwrite is allowed.
The proposed addendum must be reviewed and adopted with its bindings before
the proposed interfaces can be treated as canonical acceptance scope.

No implementation, whole-family semantic acceptance, global nonoverlap,
aggregate denominator/rate, release, platform choice or seed-retirement claim
is made. The separate PKG5 semantic implementation worker keeps its own source
and evidence authority.

## Test namespace correction after CI

The coordinator reported static failure on original candidate
`bf412f594224a9e9784b554ee0c0eaff8a12c447`: run `40800241567`, job
`123206330204`, one unresolved-namespace warning at test line 4 for
`package-requirement-patch/immutable-inputs`. The reported projection check
passed. That failed candidate and its CI receipt remain historical evidence;
they are not reclassified as passing.

The successor adds the test's proper namespace, loads the tool at top level,
and explicitly requires its namespace through alias `patch`. It retains every
positive and negative assertion. No linter configuration, source/task span,
family artifact, acceptance status or proposed interface changes.

Local successor checks passed: the same CI-pinned clj-kondo version
`2025.10.23`, with `--repro --cache false --config-dir tools/ci/kondo --lint
tools/package_requirement_patch.clj tools/package_requirement_patch_test.clj`,
reported zero errors and zero warnings. Family tests passed 4 tests / 26
assertions; language boundary passed 6 tests / 11 assertions. Whitespace passed.
Fresh successor-head CI and coordinator-routed independent review remain
separate gates. Any review of the original candidate needs qualification for
the changed test namespace/loading and this evidence addendum.
