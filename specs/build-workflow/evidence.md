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

## Authenticated Build source companion

The exact content subject `9f6f43f93725e65b93464f62b922bcd16f2f17d9` (tree `cc0fdb6f7ed6014b6a449e092a1461cf738c5bc6`, parent `0293c86cba25ecaf0e96cfcf827c029042c4ef2c`) received qualified source-content acceptance in `/tmp/uniform-build-b1-b3-consumers-independent-content-review.edn`, SHA-256 `d38d33499e03cf21cc279266e6c4414b13e0daa5eb2057b6c14a8c9d81dac35b`, with no findings. The review's evidence reminder remains: eventual actual comparison/refusal records bind the consumed runtime/helper/provider/dependency identities alongside source/compiler/toolchain/backend/profile/target identity. A list of labels or filenames does not satisfy those joins. No actual comparison or implementation result is accepted here.

The companion advances only Build's current task file and its existing model bridge, retaining the complete prior bridge table, requirement record and source-view history. The previously bound L12 criterion and scalar task record are byte-identical; its new file identity and relocated byte span are explicit. All five task IDs, Open states, checkboxes and scalar records remain exact. Complete prior canonical task bodies remain unique prefixes. The source-frame companion preserves all 48 earlier file transitions and adds the first separate Build task-file transition from exact prior bytes. It preserves all 576 prior raw payload subjects; the two amended parser captures are separate subjects.

Preflight source checks pass: current-task history guards 10 tests/100 assertions and source-frame guards 6 tests/96 assertions. The complete old frame and model have exact value and byte inverses. The derived source review remains pending under fingerprint `8c458da0bf1453a8091a0ba1337dc6eae98d85f2f5c3f3e92466b863098b4f30`; its complete previous review remains preserved under the original subject. Final committed checks and independent exact-companion review still require their own receipts. This does not close PR #767, any task, implementation, release, Main integration or full-240 verification.

## Explicit provider configuration: local implementation, 2026-11-04

Finding: `build.provider-configuration.explicit-layers-v1`.
Owning task:
`build/remaining-tasks-and-evidence/implement-projectlock-normalization-and-constraint-resolution`.
Local carrier/planning:
[tasks.md](tasks.md#explicit-provider-configuration-increment), after the
implementation-region end marker. Applied
[speckit-implement](../../.agents/skills/speckit-implement/SKILL.md).
Only the four admitted paths were edited. Parent status remains Open;
independent acceptance and integration are pending.

Worktree: `/Users/matt/.codex/worktrees/9da8/uniform-advance-worktrees/increment-1167`.
Revision/tree and Git status were not inspected: the worker's governing
instructions prohibit all Git commands, including Git hidden in Clojure.
No commit, ref change, network action, cache write or whitespace action was
attempted. Existing select23 Git/cache-write and the907 whitespace restrictions
remain effective. No independent reviewer was invoked by this worker.

### Observed behavior and semantic limits

The new pure `gravity.build.provider-configuration/normalize` preserves
source/package/workspace/profile-default/compiler-default layers in L15 order,
exact provider/version references, context-bound defaults, public locations,
effect/capability request/deny records and build/runtime separation. Reordered
maps and unordered declaration vectors yield equal products and printed data.
Conflicting requests and denials remain visible for later enforcement.
Malformed references, duplicate per-layer selection keys, missing context,
wrong default bindings, malformed summaries/locations, callbacks, lazy data and
metadata reject with fixed, payload-safe reason/path diagnostics.

The contracts do not fix this decoded transport shape or a universal provider
version grammar. The local carrier is explicitly documented rather than
claimed as canonical project syntax. It requires already-public decoded inputs
for one context. It neither authorizes private metadata disclosure nor
authenticates locations, validates semantic profile/target legality, computes
source-derived/transitive summaries, resolves providers, verifies trust,
issues grants or executes effects. Mapping future real producers to this seam
and their independent acceptance remain separate composition work.
Existing package policy/selection, private-registry authorization, invocation,
pinned plan and source-version records are untouched. No source-conformance,
release, self-hosting or seed-retirement authority is claimed.

### Exact identities

SHA-256 over UTF-8 file content, obtained with the Clojure identity check below:

| Subject | SHA-256 |
| --- | --- |
| New normalizer | `63fc3c4a318f3e5cd3f1e142108f348693a29fecb48868c432f98f9faec6ebe4` |
| New focused fixtures/tests | `51484e5a44f2f8559c5d3498c249a891e6c8a6145686929246493af4b30ac296` |
| tasks.md with the local appendix | `77f1eb34ed0726d374c4f6e2c80241c62a1d1f40db1885ca81abb3b7931d41cf` |
| Original tasks.md, reconstructed by removing only the local appendix | `5bf868e7d95cdbbf4e50978a0fa7e6dd552e16841a6b20cde56025de479709e8` |
| Unchanged plan.md | `40a69b4ef88e185ccfd1c0977fc51a99e9a5bab929dca95400d84a10df938da7` |
| Evidence before this appended record | `0e44467378b1a9662df36fb43148890bb9714172890ab7a562c4dfc7b6f62759` |
| tools/acceptance_dependencies.clj | `c1eff9a0e3aef0de97429698b097c88aefa1d6571761862cd08cf781e738d087` |
| tools/acceptance_dependencies_test.clj | `f644ce6b58c2653b2d4e8bf69cec9c33847f5d00f99c4df5e91208ba3e0410ec` |
| tools/requirement_sources.clj | `93dab0852fd34fbd9c12cb5f6847eff66c5fc7bfb4c31945a51d58f2bb496bd0` |
| L15 | `81a875ffbbc43bf05f95aafd60774aaf1b1fa2e54c0eb9331b03dd28597f6ae5` |
| PKG1 | `b88e8311b34da26a127bf3beb601f15c6137386818a31d6c01d74325a09faedd` |
| PKG6 | `64d7893fa2d5a7c5789703b156a5d95af1c21f103fc83d8bc713fb64dbc943e3` |

The original-task reconstruction equals the recorded before-edit digest,
conserving the entire canonical region, all five IDs/Open statuses, source
records and historical suffix. This is content conservation, not Git lineage
authentication or source-conformance acceptance.

### Actual Clojure checks

Clojure CLI help and `-Sdescribe` identified installed version
`1.12.5.1654`. Its documented `-Scp` option uses the installed local tools JAR
and explicit repository paths without computing/caching a classpath or
resolving dependencies. No cache was redirected elsewhere.
All check vectors below ran from the worktree root and exited 0.

Focused tests, final source: 10 tests / 147 assertions, zero failures/errors.
The earlier run, before the precise missing-field diagnostic refinement, also
passed 10 tests / 144 assertions; that earlier result is not substituted for
the final run.

```clojure
["clojure" "-Scp" "/opt/homebrew/Cellar/clojure/1.12.5.1654/libexec/clojure-tools-1.12.5.1654.jar:bootstrap/clojure/src:bootstrap/clojure/test:tools" "-M:dev-test" "--namespace" "gravity.build.provider-configuration-test"]
```

Language-boundary gate: 6 tests / 11 assertions, zero failures/errors.
Document validation: 240 documents / 19 phase indexes, ASCII, no filler.
Full-language roadmap validator passed its structural checks; its dated
coverage snapshot is not a current execution or completion result.

```clojure
["clojure" "-Scp" "/opt/homebrew/Cellar/clojure/1.12.5.1654/libexec/clojure-tools-1.12.5.1654.jar:bootstrap/clojure/src:bootstrap/clojure/test:tools" "-M:test" "--namespace" "gravity.self-hosting.sh01-language-boundary-test"]
["clojure" "-Scp" "/opt/homebrew/Cellar/clojure/1.12.5.1654/libexec/clojure-tools-1.12.5.1654.jar:bootstrap/clojure/src:bootstrap/clojure/test:tools" "-M" "tools/validate_gravity_docs.clj"]
["clojure" "-Scp" "/opt/homebrew/Cellar/clojure/1.12.5.1654/libexec/clojure-tools-1.12.5.1654.jar:bootstrap/clojure/src:bootstrap/clojure/test:tools" "-M" "tools/validate_full_language_roadmap.clj"]
```

The unchanged original
`unchanged-captured-spans-follow-layout-only-ledger-edits` deftest was evaluated
in isolation from the file's automatic full-suite runner: 1 test / 7 assertions,
zero failures/errors. This command loads the original namespace/setup and only
that test definition; it does not replace functions, mock Git or claim the
whole file passed.

```clojure
["clojure" "-Scp" "/opt/homebrew/Cellar/clojure/1.12.5.1654/libexec/clojure-tools-1.12.5.1654.jar:bootstrap/clojure/src:bootstrap/clojure/test:tools" "-M" "-e" "(require '[clojure.java.io :as io] '[clojure.test :as test]) (with-open [r (java.io.PushbackReader. (io/reader \"tools/acceptance_dependencies_test.clj\"))] (binding [*read-eval* false *ns* *ns*] (loop [n 0] (let [f (read {:eof ::eof} r)] (when-not (= ::eof f) (when (or (< n 3) (and (= 'deftest (first f)) (= 'unchanged-captured-spans-follow-layout-only-ledger-edits (second f)))) (eval f)) (recur (inc n))))))) (let [v (ns-resolve 'acceptance-dependencies-test 'unchanged-captured-spans-follow-layout-only-ledger-edits)] (binding [test/*report-counters* (ref test/*initial-report-counters*)] (test/test-vars [v]) (prn @test/*report-counters*) (when (pos? (+ (:fail @test/*report-counters*) (:error @test/*report-counters*))) (System/exit 1))))"]
```

Preservation and ASCII check: passed. Exact full original task bytes are
reconstructed and checked against the before-edit hash, the pinned plan hash
is checked, all five Open status lines remain, and all four owned files are
ASCII. This is not the denied whitespace action.

```clojure
["clojure" "-Scp" "/opt/homebrew/Cellar/clojure/1.12.5.1654/libexec/clojure-tools-1.12.5.1654.jar:bootstrap/clojure/src:bootstrap/clojure/test:tools" "-M" "-e" "(require '[clojure.string :as str] '[gravity.digest :as digest])\n(let [task (slurp \"specs/build-workflow/tasks.md\")\n      start (.indexOf task \"## Explicit provider configuration increment\\n\")\n      historical (.indexOf task \"## Historical workflow migration record\\n\")\n      original (str (subs task 0 start) (subs task historical))]\n  (assert (<= 0 start historical))\n  (assert (= \"5bf868e7d95cdbbf4e50978a0fa7e6dd552e16841a6b20cde56025de479709e8\" (digest/sha256-hex original)))\n  (assert (= \"40a69b4ef88e185ccfd1c0977fc51a99e9a5bab929dca95400d84a10df938da7\" (digest/sha256-hex (slurp \"specs/build-workflow/plan.md\"))))\n  (assert (= 5 (count (re-seq #\"(?m)^Status: Open$\" task))))\n  (doseq [p [\"bootstrap/clojure/src/gravity/build/provider_configuration.clj\"\n             \"bootstrap/clojure/test/gravity/build/provider_configuration_test.clj\"\n             \"specs/build-workflow/tasks.md\" \"specs/build-workflow/evidence.md\"]]\n    (assert (every? #(<= (int %) 127) (slurp p))))\n  (println \"Preserved original task bytes, all five Open statuses and pinned plan; four owned files ASCII.\"))"]
```

Identity check producing the table above:

```clojure
["clojure" "-Scp" "/opt/homebrew/Cellar/clojure/1.12.5.1654/libexec/clojure-tools-1.12.5.1654.jar:bootstrap/clojure/src:bootstrap/clojure/test:tools" "-M" "-e" "(require '[gravity.digest :as digest])\n(doseq [p [\"bootstrap/clojure/src/gravity/build/provider_configuration.clj\"\n           \"bootstrap/clojure/test/gravity/build/provider_configuration_test.clj\"\n           \"specs/build-workflow/tasks.md\" \"specs/build-workflow/plan.md\"\n           \"tools/acceptance_dependencies.clj\" \"tools/acceptance_dependencies_test.clj\"\n           \"tools/requirement_sources.clj\"\n           \"docs/phase-01-core-language/025-l15-capability-provider-specification.md\"\n           \"docs/phase-12-build-package-and-artifact-system/165-pkg1-project-file-specification.md\"\n           \"docs/phase-12-build-package-and-artifact-system/170-pkg6-capability-and-permission-manifest-specification.md\"]]\n  (println p (digest/sha256-hex (slurp p))))"]
```

### Restricted and unrun checks; failures

The required full `tools/acceptance_dependencies_test.clj` run and its
`normalized-source-snapshot-tampering-rejects` test remain **restricted/unrun**.
Inspection shows `task-sh-view` invokes
`platform-task-transition-view` and `stdlib-task-transition-view`, which call
`requirement-sources/git-source` and `git-commit-tree` before the first
requirement's tampered acceptance snapshot is checked. Those functions execute
Git through ProcessBuilder. The worker's explicit prohibition on all Git
commands therefore prevents these checks. They were not executed, mocked,
reframed as passing rejection evidence, or routed through another executable.
The harness controller must obtain these required results within its own
permitted boundary before declaring the increment accepted.

A separate read-only Clojure trace confirmed that the snapshot mutated by that
test differs from the shared-rule snapshot checked before those transitions:
first acceptance reference
`901ed5800ad757e9360ce90b596286fcec6744b7516062ea0ac0e49b3bf52153`;
shared-rule reference
`e72027e8febb7e7f5c9014835f2717d006ce664ce1fb091cc54b47f2efe3800a`.

```clojure
["clojure" "-Scp" "/opt/homebrew/Cellar/clojure/1.12.5.1654/libexec/clojure-tools-1.12.5.1654.jar:bootstrap/clojure/src:bootstrap/clojure/test:tools" "-M" "-e" "(require '[clojure.edn :as edn]) (binding [*command-line-args* nil] (load-file \"tools/acceptance_dependencies.clj\")) (let [s (edn/read-string (slurp acceptance-dependencies/sh-crosswalk-path))] (prn {:first-acceptance-ref (get-in s [:requirements 0 :acceptance-source :source-snapshot-ref]) :shared-rule-ref (get-in s [:shared-completion-rule :source :source-snapshot-ref]) :same-reference? (= (get-in s [:requirements 0 :acceptance-source :source-snapshot-ref]) (get-in s [:shared-completion-rule :source :source-snapshot-ref]))}))"]
```

No executed Clojure check failed. One initial task-append patch failed its
context match and wrote nothing; the corrected marker-only insertion
succeeded. Read-only path probes also reported absent optional paths and one
incorrect runner path before the actual test-runner path was located; these
are not test passes or semantic failures. No denied action was retried.
Git status/whitespace/preflight, full compiler/package/invocation suites,
real producer composition, independent acceptance and integration were unrun.
The parent task and all broader acceptance criteria remain open.

Final appended-evidence preservation check also exited 0: original evidence
prefix matches its before-edit hash and all four final owned files are ASCII.

```clojure
["clojure" "-Scp" "/opt/homebrew/Cellar/clojure/1.12.5.1654/libexec/clojure-tools-1.12.5.1654.jar:bootstrap/clojure/src:bootstrap/clojure/test:tools" "-M" "-e" "(require '[gravity.digest :as digest])\n(let [s (slurp \"specs/build-workflow/evidence.md\")\n      marker \"\\n## Explicit provider configuration: local implementation, 2026-11-04\"\n      i (.indexOf s marker)]\n  (assert (pos? i))\n  (assert (= \"0e44467378b1a9662df36fb43148890bb9714172890ab7a562c4dfc7b6f62759\"\n             (digest/sha256-hex (subs s 0 i))))\n  (doseq [p [\"bootstrap/clojure/src/gravity/build/provider_configuration.clj\"\n             \"bootstrap/clojure/test/gravity/build/provider_configuration_test.clj\"\n             \"specs/build-workflow/tasks.md\" \"specs/build-workflow/evidence.md\"]]\n    (assert (every? #(<= (int %) 127) (slurp p))))\n  (println \"Original evidence prefix conserved; all four final owned files ASCII.\"))"]
```


## Controller continuation after instruction-scope inspection

The preceding author phase stopped under an operator-added prohibition on all Git commands. Inspection found no actual sandbox/provider denial and no privileged request; the full dependency suite was never attempted. The controller corrected only that instruction scope: required existing repository Clojure suites and their ordinary source/fixture Git operations remain authorized in the same workspace-write, network-disabled check sandbox. The original author output, error, command log and four file hashes remain retained in the local controller's build-1167-author-snapshot.edn receipt. The907 whitespace action, select23 read-only Git/cache-write actions and every actual shared restriction remain denied; none is retried or wrapped.

The authored implementation is reused without another author run. This note does not report unrun checks as passing. Exact committed-candidate checks and outputs are recorded by the controller and in target/validation/advance/check-receipts/build-provider-1167.edn; independent review/CI/integration remain separate. The local bootstrap carrier and all broader Open parent criteria retain their original scope.

## Oldest-first PR1173 composition, 2026-11-09

The two implementation/test files are byte-identical to PR1173 source commit
`5f5b3b2052cb5cb0616025df630d7cca4f861829`. The provider task section is
inserted outside the implementation region; removing only that section
restores the complete Main f138 task file. Main's complete evidence prefix is
preserved before the historical provider records and this continuation.

Actual checks on this composition: the documented `:dev-test` focused runner
passed 10 tests/147 assertions; the language-boundary gate passed 6 tests/11
assertions; document validation passed 240 documents/19 indexes; the roadmap
structural validator passed. The initial `:test` invocation selected the
self-hosting runner, which does not own this namespace, and executed no tests.

The required acceptance-dependency suite ran 32 tests/7484 assertions with
zero failures and seven errors: each reported that a current source transition
was not bound to the checked-out source. These are retained failures pending
precise lineage diagnosis, not passing integration evidence. Logs are preserved
at `/tmp/uniform-pr1173-main-f138-*-root-v1.log` and the focused development
result at `/tmp/uniform-pr1173-main-f138-focused-dev-root-v1.log`.
No independent acceptance, CI success, Main integration, parent completion,
release or self-hosting authority is claimed by this continuation.
