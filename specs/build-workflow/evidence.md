# Evidence: Build workflow migration

Inspected source: `017b541d678c6acb93eb5e843b1eda5a66babc32` (2026-10-26).

## Status dimensions

- Artifact preparation: complete; four migration documents are present.
- Local validation: passed on 2026-10-26 for the eight-package documentation candidate.
- Independent package review: pending.
- Main integration: pending; no merge receipt claimed.
- Component implementation acceptance: unassessed by this package; canonical
  [release outcome](../../docs/component-roadmaps/build.md#release-outcome) remains open.

## Source evidence and preservation

The [Build canonical remaining work](../../docs/component-roadmaps/build.md#remaining-tasks-and-evidence) and its contracts were inspected as work inputs, not
executed implementation evidence. Existing phase completion retains its exact
historical surface. Existing finite feature receipts are neither discarded
nor promoted to whole-component completion.

The four documents link the original task and acceptance locations instead of
creating another implementation ledger. No compiler source, runtime action,
target selection, contract semantics or historical identity was changed.

## Validation receipts

The same documentation candidate was checked as one eight-package transaction:

| Check | Result |
| --- | --- |
| Focused ASCII, file-set, Markdown link/anchor and migration-ledger checks | Passed: 8 packages, 32 files; all local links resolve; exactly four migration tasks per package |
| `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Exit 0: 240 documents, 19 phase indexes, ASCII and placeholder checks |
| `clojure -J-Xmx256m -M tools/validate_full_language_roadmap.clj` | Exit 0 |
| `git diff --check` | Passed |

The focused check enumerated only these eight package directories, resolved
relative targets from each document, compared anchor slugs with actual headings,
and asserted that tasks are migration-only rather than implementation copies.
The canonical validators establish structural compatibility, not semantic
implementation acceptance. No source or tooling was added to the repository.
Compiler execution, external Specify CLI invocation, command parsing and fresh
skill discovery are unrun. Independent review and integration remain pending.

## Review and integration receipts

Pending independent review and coordinator integration. Their absence is not
a request for another general project approval. Only an exact accepted package
plus integration receipt closes this migration increment.

## Implementation authority transfer

Source: `dcc9849df582ebb177560b41d702071e3481669a`.
The five original implementation rows are edited only in
[tasks.md](tasks.md#implementation-tasks), with the roadmap table generated.
IDs derive from the existing component/section/title-slug convention rather
than literal IDs in the original table. All rows remain Open. Actual evidence
targets, the entire historical task file, prior navigation and any integrated
contract-interface appendix remain unchanged. Historical workflow statements
above describe the earlier preparation slice, not current task authority.

Exact checks and independent review belong to the public change receipt.
Structural projection does not execute the product, authenticate semantic
evidence, grant authority or accept implementation/release outcomes.

### Observed transfer checks

Focused projector tests passed 4 tests / 66 assertions, zero failures/errors;
projection check passed. Baseline comparisons passed all five original table
rows/cells, derived IDs/Open statuses, five links/five actual targets, entire
historical suffix, original full roadmap reconstruction, prior navigation,
exact L12/PERF3/PERF6/GOV2/GOV10/SAFE14 appendix, five Markdown ASCII/file/heading
checks and missing-file/missing-heading controls.
Table SHA256:
`3ca5f3bf94744a582694ea129aeef7382af38deb20383979006695fc60d0eb07`.
Appendix SHA256:
`db0dd0083300d6cbd75daed56debcc075478b28745c79be8e7ff3dbffe846b67`.
Language boundary passed 6 tests / 11 assertions; dependency tests passed
10 tests / 88 assertions. Documentation passed 240 documents / 19 phase indexes;
full-language roadmap and whitespace passed. All commands exited naturally with
0. Exact committed-tree preflight and independent disposition belong to the
public PR receipt. None of these checks executes a build or verifies a package.

## PKG5 yank/revocation interface correction

Input main: `68a127520c2465a90a4a9b06742cb58ebaffca77`.
Task-source commit: `3bd7e21145dfe5d71f45375f6e787b384d361e2e`.
The existing first Build record now explicitly requires policy decisions,
signature/revocation status, retained locked history, the explicit historical
rebuild exception and `PKG5006`. The five IDs and Open statuses, other four
records and entire historical suffix are unchanged. The roadmap is generated.

[The amendment receipt](yank-interface.edn) binds the existing requirement
`behavior/pkg5/yank-revocation-admission` and its four exact PKG5 facets. The
original preserved manifest remains SHA256
`aa7ee8dfa7c7b0b7a9cb0e609944d7dcbcd81791f080708364a0faf8cc2e7980`.
Only task-reference and task-gap were changed in an isolated copy; its SHA256 is
`38545162bbcff6c38052e58a296b26aa43e4ac8ec7a4641531d6e7cec5905158`.
A streaming comparison checked all 325,588 lines and found one changed map
line; outside those two fields the requirement, prefix and suffix are exact.
The snapshot contains 1,225 requirement records and 29,182 facets; those are
preserved snapshot counts, not a replacement for PR445's separately published
1,231-candidate view or its 577 missing-interface census. No census is updated.
The preserved uncommitted snapshot is distinct from the committed manifest at
PR445 head `e9fa4d0d8e7c942e9ac1cc535c13f6d7840dae30`, whose SHA256 is
`f4c171820dfdadaf7e7b5a3f04a701a124adfffd6cbaf6b598c1b82098aae690`.
Main receives the replayable amendment receipt, not the full draft manifest.
Canonical manifest adoption remains a separate reconciliation gate.

The preparation process completed both file writes and then exhausted its
512 MiB JVM heap while reporting their identities. That exit-1 observation is
preserved. A separate 128 MiB streaming verifier consumed the already-written
artifacts, verified both full-file hashes, checked all lines and exited 0;
the preparation mutation was not repeated. The original was never overwritten.

The existing SH source-version bridge pins the Build task and generated roadmap
to the exact task-source commit. Task-authority lineage retains the predecessor
and records the single closing-evidence refinement. The previous mapping review
is retained in full as history; changed-source review is pending. All 180
whole-criterion implementation assessments remain unassessed. The GOV10 binding
defect is excluded, and accepted isolated 586 versus proposed-unverified 470
remains unchanged.

Final focused controls passed: Build 5 tests/78 assertions, dependency 14/402,
SH projection 10/110 and boundary 6/11. Duplicate facet rejection and explicit
Build pin/lineage conservation passed; a separate comparison conserved all180
requirement predicates,18 sequence rows, every other component task authority,
the complete predecessor bridge/lineage and prior review. Both projection
checks and whitespace passed. Independent exact-candidate receipts belong to
the PR record. No resolver,
compiler, producer, authentication, runtime or broker execution occurred.

Independent source/interface review accepted exact candidate
`023a28488f8f161c68cbe57aa29fa48247197d62`, tree
`e8b660791bdaf3e865528f943725b5d1dd307b41`. Receipt SHA256:
`c0714e09a510f173d0e69380478beb1a5790b07635283009749612aff9754b57`.
The reviewer reproduced the focused controls, streaming manifest comparison,
full SH conservation and byte-identical pin-validating regeneration. Acceptance
is limited to the task-interface amendment; manifest adoption and implementation
remain open.

Main then advanced through Reader540 and RV543 to
`114281c40e1ada82bc7fd3faac834c9f9805d8bb`. The normal merge conflicted in the
shared SH generator and projection. Resolution retains the incoming Reader
dispatch pins, ownership and complete review history, adds the Build pins and
retains the accepted PKG5 source-review receipt and its predecessor mapping.
The composed source fingerprint is new; mapping promotion remains pending.
Changed-composition review and exact-head CI are separate remaining gates.

## B1-B3 Build consumer source repair

The independent bounded PR #767 review identified two source gaps in the existing Open Build records: exact target lock configuration before compilation and the selected repeat-build inputs. The same two tasks now carry those precise B1-B3 consumer clauses, linked to the existing Backend criteria. No task or producer is added. C dialect/compiler/flags/data-model/ABI/layout and LLVM version/triple/layout/CPU/features/relocation/code-model/ABI/sanitizer/unwind/pass settings remain selected declared inputs; missing, conflicting, host-inferred and changed-input cases must fail or invalidate affected products. Repetition consumes actual canonicalization, manifests/content, source/debug/proof maps, runtime/helper/provider/dependency identities and selected comparison/refusal products. Strict typed-Core equality and separately accepted nonsemantic native-metadata exceptions retain their distinct scopes.

The original task bodies remain unique exact prefixes, with identical scalar records, IDs, Open status and unchecked state. The complete prior task file is byte-recoverable by removing the two explicit additions. The original source contracts, histories, execution restrictions and actual independent implementation requirements remain. Final signing, whole Backends/C/LLVM/Runtime completion and unavailable live operation are not early interface gates.

Source proposal: `/tmp/uniform-pr767-eight-partial-current-owner-proposal.edn` (SHA-256 `cb80bbbe8e5d0a6a041b93a97ef8cf752b02b2e266af81f4158a7c9e3bbaa2f1`). Independent bounded semantic review: `/tmp/uniform-pr767-eight-partial-current-owner-independent-review.edn` (SHA-256 `56431c30ac5aa268d017a690b45bf07f765d68e91371db3ed95931cba40082c3`). The repeat-build criterion also spells out the review's existing manifest/map/helper/dependency and selected-result requirements; the exact edited criteria still require their own candidate review. The missing original route-mapping sidecar bytes remain unrecovered; reproduced discovery payloads are not substituted as old-byte authentication.

This first content commit is an immutable source subject for a separate authenticated changed-file/current-task successor. Until that successor and checks are finished, existing current-source references must not be relabeled as current or passed. No compiler/backend/provider/native/signing/publication operation, Main merge, PR #767 closure or full-240 acceptance is claimed.
