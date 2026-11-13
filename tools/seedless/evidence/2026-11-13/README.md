# Foreground compiler and runtime evidence

This is a scoped development execution record, not migration, release, or
compiler-repair acceptance. Source changes are the generic Seedless controller
implementation; no Map-key classifier or transition rule was added to it.

## Archived evidence and verification

The following files preserve the original bytes of the indicated captures and
executed scratch scripts. Absolute paths in the scripts identify this audit
environment; the scripts are archived experiments, not installed controller
commands. Other artifact paths named below refer to the retained scratch audit.

| File | Scope |
| --- | --- |
| [foreground-observation.edn](foreground-observation.edn) | Fresh uninstrumented compiler stdout; SHA256 `f31f709b8bb6cf724918dd5c3fc31dad0a7fd960e5715754c1c04a901c4f1486`. |
| [receiver-summary.edn](receiver-summary.edn) | Supplemental replay receiver facts and provenance. |
| [runtime-receipt.edn](runtime-receipt.edn) | Real production diagnosis eligibility and blocked runtime initialization; SHA256 `5cdacea5fb9783432e2ed9a05fc8116a2d5366dc2d4d612c4daf2ae4d12633a3`. |
| [receiver-observer.clj](receiver-observer.clj) | Exact executed observer, delegating actual C7 arguments/results unchanged; SHA256 `60a1708b65aa3dc61e3d6969675ba917cca8925403c4b424003f4f75d36dfc53`. |
| [diagnosis-driver.clj](diagnosis-driver.clj) | Exact executed foreground receipt-ingestion and production diagnosis driver. |

The clean implementation revision exercised by the driver was
`236cf3151913a9187add611f7028be5c4720eeba`, tree
`87c2a372215f20b7d7718bd193dc4fc0c2e2d738`. The delivery commit adds this evidence
archive. It remains a local review candidate: external publication was blocked
as described below, and no PR was created.

| Verification on the frozen implementation | Result |
| --- | --- |
| Complete Seedless suite | 149 tests, 1,615 assertions; zero failures/errors. |
| Language boundary | 6 tests, 11 assertions; zero failures/errors. |
| Canonical documentation validator | 240 documents and 19 phase indexes passed. |
| Full-language roadmap validator | Structural claims passed; its historical coverage snapshot is not current compiler progress. |
| Workstream governance validator | 185 workstreams passed. |
| Independent code review | Recovery, source/progress authority, duplicate-candidate handling, and scheduler ownership reviewed; identified defects fixed and covered before freezing. |
| `git diff --check` | Passed. |

Tests used the existing installed Clojure tools JAR through `java ...
clojure.main`, because the normal CLI could not resolve Maven dependencies.
The complete Simplify suite was also attempted after its shared schema change:
103 tests and 1,189 assertions, zero assertion failures and one environment
error. Its CLI subprocess regression could not find `clojure` on PATH. That
suite is not reported as fully passing; the new integer/boolean schema cases
passed in the Seedless suite. No shim or dependency installation was used to
hide that limitation.

The regression suite includes two distinct synthetic unfamiliar failures,
an unchanged genuine historical capture with labelled provenance, ordinary
planning/admission and eligibility, unfamiliar after-results, pending
continuation, source-specific progress evidence followed by independent review,
stale/missing evidence, invalid ownership, execution restrictions, duplicate
candidates, interrupted admission, changed PR heads, inspected recovery, and
same-task scheduler ownership with disjoint-task concurrency. Recorded model
proposals in these tests do not establish live model operation.

Status: the fresh full compiler run, supplemental receiver replay and real production diagnosis entrypoint invocation are complete. The model runtime could not initialize its required state database; no diagnosis, planner or author model turn completed and no compiler repair was authorized.

## Source and publication identities

- Preserved full local candidate: `49eb3c5805a61bdf5ec88553cf93ab9d418af272`, tree `c69c1ecb76f7f554ca8bfef97e214b6407c9627f`, checkout `/workspace/scratch/0ca27623ede8/uniform-c7-routing`.
- Local C7 optimization parent: `d5775869ceb05bb28d0fc247ee42c00fa0179bd0`. Its later Map-key selector/controller additions are part of local 49eb, not the new generic controller implementation.
- PR #1423 was refreshed as OPEN/DRAFT with published head `0de2f6a2087e27c5dec5e4af7ab71c2a2ae24cd5`; its green CI covered that published head. Complete d577 and 49eb patches survive in PR comments 6833318009 and 6833662307. They are not published branch commits.
- Earlier attempted publication of the 1,937,695-byte C7 blob hung and was interrupted; no automatic approval denial was found for that historical operation. The expected blob `bf0e4665a1b4b7cdf099ffbdd5456d01b0920e66` remained unavailable on refresh. An earlier code-only tree object for the new controller was created successfully, but no commit, branch or PR was published from it. The later combined source/evidence publication was separately rejected by automatic approval review.

No preserved compiler worktree or branch was edited during this audit or foreground validation.

### Current external-publication blocker

Normal Git push could not authenticate through the command-line environment.
The subsequent GitHub tree-creation request for the complete 18-file candidate
was rejected by automatic approval review. The stated reason was disclosure of
repository source, compiler captures, runtime receipts and environment metadata
to `burn2delete/uniform`, without recognized trusted end-user authorization for
that specific disclosure and destination. This rejection is separate from both
the historical C7 blob interruption and the model-runtime initialization failure.

No replacement upload, alternate transport, branch creation or PR creation was
attempted after that rejection. The complete local candidate, exact execution
receipts and review package remain available for inspection. Publication of this
package requires explicit user confirmation in response to the approval block.
The archived evidence includes workspace paths and runtime diagnostics; the
runtime receipt excludes credentials, full prompts and configuration contents.

## Fresh full compiler route

Working directory: `/workspace/scratch/0ca27623ede8/uniform-c7-routing`.

The configured CLI attempt used the retained installed Clojure CLI with `-J-Xmx2g -M:compiler-probe`. It exited 1 before compilation: Maven could not resolve `org.clojure:clojure:1.12.0` because `repo1.maven.org` DNS resolution was unavailable. Its exact stdout, stderr and exit are retained at the report directory's top level. This is an execution prerequisite failure, not a compiler rejection.

The fresh full-route subprocess instead used the already installed Clojure tools JAR, without installing dependencies, changing configuration or relaxing permissions:

```sh
timeout --signal=TERM 7200 java -Xmx2g \
  -cp /workspace/scratch/0ca27623ede8/toolchains/clojure-1.12.0.1530/runtime/libexec/clojure-tools-1.12.0.1530.jar:bootstrap/clojure/src:tools:bootstrap/clojure/test \
  clojure.main -m gravity.compiler-probe
```

This runs the original complete `gravity.compiler-probe` entrypoint on the full local 49eb compiler source. It uses a fresh JVM, Clojure 1.12.0, Java 21.0.13, 2 GiB heap and 7200-second watchdog. The Java launcher differs from the configured CLI command. It is a foreground source execution, not a normal Seedless campaign/check adoption.

The subprocess completed naturally with exit 1, from 2026-11-13T21:16:31Z through 2026-11-13T21:46:45Z (1814 seconds), with a clean checkout before and after. All 14 declared input identities remain current. It used the unchanged six-owner closure and declared LLVM target/triple; the earliest rejection remains component 0, `:authenticated-envelope`, stage `:function-types`, `C7-VERIFY / :map-keys-finite-keyword-map-type-required`.

The complete envelope retained all 70 functions, no recursion components, successful fresh Core verification, source SHA `7ef0386733632569a81a3e709551e4c4d300ec43ff0f7f13b5fe7399df29c3b7` and Core identity `sha256:28ba1a5232045882c90b746e5b0db4d6cb04310f71aa23c3be2d9fb571004c12`. These source inputs, frontier, diagnostic reason and Core agree with the retained d577 run. The new input identity is `3669dc5cf007d28fd00362ae36ca309e616d674bcb87024e9b85dfbdbacb153b`, bound to the actual 49eb execution revision.

Raw observation SHA256: `f31f709b8bb6cf724918dd5c3fc31dad0a7fd960e5715754c1c04a901c4f1486`. Artifacts: `offline-full-route/observation.edn`, `probe.stderr`, `execution-revision`, `execution-tree`, `before-status`, `started-at`, `exit-code`, `finished-at`, `after-status` and `summary.edn`.

## Actual receiver: supplemental retained-request replay

The separate diagnostic replay completed naturally with process exit 0 because its script successfully recorded C7's rejected result. The compiler result itself was `:rejected` with `C7-VERIFY / :map-keys-finite-keyword-map-type-required`. Runtime 301.1 seconds; clean 49eb before and after.

It replayed the exact complete original C7 request from `/workspace/scratch/0ca27623ede8/next-c7-diagnostic-20261112/producer-arguments.edn`:

| Identity | Value |
| --- | --- |
| Request bytes | 759,087,985 |
| Request SHA256 |`1d311b33c3cdd8024c3ad992617ae54a6b28299cb78130d467884ac880e3eda3` |
| Original capture source revision |`0de2f6a2087e27c5dec5e4af7ab71c2a2ae24cd5` |
| Complete canonical Core |`sha256:28ba1a5232045882c90b746e5b0db4d6cb04310f71aa23c3be2d9fb571004c12` |
| Function population | 70; no recursion components |
| Retained original Core verification |`:passed` |
| Envelope source SHA256 |`7ef0386733632569a81a3e709551e4c4d300ec43ff0f7f13b5fe7399df29c3b7` |
| Executed current C7 source SHA256 |`b76234be9c0496d92a5c6650c41393df240828d1e1c6861ea3d8bbdd7267e4d8` |
| Executed current C7 plan |`sha256:c5c2acd85c05c311f52c30850e1309b9ba170b17a455cc98b9208b07d908628b` |

The complete retained request parsed at the original 2 GiB heap. No request/context fields, type facts, callback results or delegated call arguments were changed. Its upstream source is unchanged between 0de and 49eb; the compiler-source difference is the C7 optimization. This is an actual current-C7 execution on authentic retained data. It does not rerun the frontend, establish new full-route acceptance, or establish compiler progress.

A scratch observer delegated `gravity.runtime/execute-function-scoped` unchanged. Its exact C7 source-hash/module filter was verified first on a cheap delegated call. It recorded 42 Map-key header rejections. All 12 matching the final failing call had receiver type **`:gravity.type/unknown`**:

| Actual failing source fact | Value |
| --- | --- |
| Function |`ae-exact-keys?`, authenticated_envelope.gravity lines 116–119 |
| Expression |`(keys value)` inside the selected `map?` then-branch |
| Operator |Resolved public `gravity.core/keys` binding |
| Call Core node |`sha256:22967db63913c2943a663442470599e52424c0bef0c67a8999eef75830b7e094` |
| Receiver Core node |`sha256:44ff718dac106c90eae75bc22d797cceafd2b18bd3b2a419c96a023378d8f729` |
| Receiver lexical binding |`sha256:0df083c43e79228fb2f9e73eb75cd8b6c615ede29aa52c8774f75027cb351d4f` |
| Inference type-table entries | 4574 |

Source-grounded implication: `sh08-mkeys-header` reads this receiver from the inferred type table and first passes it to `sh08-lc-validate-types`. That validator does not accept Unknown; the header maps rejection to the observed finite-keyword-map diagnostic. The separate private Map-key entry route requires exactly `:gravity.type/map`, so Unknown does not select it. The accepted ordinary shape requires a finite literal Map descriptor with alternating Keyword key types. The source's `map?` guard has not supplied that evidence at this occurrence. This does not justify weakening the Map-key rule or treating arbitrary Maps as finite Keyword-keyed Maps.

The owning current source declaration is SH-08 / SH-08.2; source-level applicability and the precise repair/prerequisite still need the ordinary source investigation and independent review. No compiler repair was made or authorized by this supplemental observation.

Executable script: `inspect-retained-request-receiver.clj`. It ran with the same installed JAR/classpath and 2 GiB heap under an 1800-second diagnostic watchdog. Evidence: `receiver-retained-request/events.edn`, `type-result.edn`, `summary.edn`, shell stdout/stderr/exit and source metadata.

An earlier supplemental full-route observer was intentionally interrupted with session exit 130 before reaching its target: its filter incorrectly expected `:source :path`, which compiled C7 plans omit. Its separate `receiver-diagnostic/termination-note.txt` records the limitation. It supplies no receiver evidence and is not the authoritative full-route run.

## Normal model runtime blocker

Normal installed availability checks reported `codex-cli 0.167.0-alpha.4` and an existing ChatGPT login. `gh auth status` reported no authenticated GitHub hosts; connector read access worked.

The normal `codex app-server --listen stdio://` initialize handshake exited 1 before responding or creating a model turn:

```text
failed to initialize sqlite state runtime under /home/agent/.codex
failed to open state DB at /home/agent/.codex/state_5.sqlite
(code: 14) unable to open database file
```

Read-only storage inspection established the cause: `/home/agent/.codex` is an environment-provided tmpfs mounted `ro,nosuid,nodev,relatime,mode=755,uid=1000,gid=1000`. The state database does not exist, and the current process cannot write the directory. This is not merely a missing ordinary file that normal startup can initialize. No credential contents were read, and no environment variables, database paths, permissions, mounts, sandbox policies or authentication methods were changed.

Exact handshake transcript: `app-server-handshake.txt`. Storage evidence: `app-server-storage-inspection.txt`.

## Production diagnosis entrypoint

Script `run-production-diagnosis.clj` loaded the new production controller namespaces, created a scratch state context using native configuration, ingested the original fresh full-route subprocess receipt through `compiler/observe!`, and called real `workflow/diagnose!` / configured `job!`. It retained the actual Java launcher and did not execute the compiler twice. No model/provider substitutes, campaign registration, schedules or GitHub writes were used.

The fresh candidate inventory was refreshed at 2026-11-13T21:47:30.911Z through the connected GitHub API: all 87 open PRs, with all 3,979 changed-path entries. Every retained file inventory was reused only after its exact head and base matched. The complete candidate input is `current-candidates.edn`; metadata and file-inventory times remain distinct. Newly published controller work must be included if it appears before invocation.

The driver ran on clean implementation revision `236cf3151913a9187add611f7028be5c4720eeba`, tree `87c2a372215f20b7d7718bd193dc4fc0c2e2d738`, with the separate clean compiler checkout at 49eb. The fresh raw capture was classified as blocked/unclassified and **investigation-eligible true**. The production workflow therefore attempted real job `diagnose-1`, requested role `:diagnose` / mode `:read`, and sent the actual app-server `initialize` request at 2026-11-13T21:52:16.937592268Z.

The server emitted the exact SQLite state initialization failure at 2026-11-13T21:52:26.982352896Z and closed the connection before returning any RPC response. The workflow retained:

- Diagnosis status `:blocked`, message `App-server request failed`, no model proposal.
- Job `diagnose-1` status `:attention`, no thread and no turn.
- Original effect `compiler-diagnosis/a0e689dec345bd7575af6171ea5b586fc76223818b17526a4841b95d3db081e1` status `:uncertain`.
- Failure method `initialize`, error `App-server connection closed`, `writer-ended? false`, `restricted false`.
- `model-turn-completed false`, `campaign-started false`, `provider-writes false`.

The foreground driver completed naturally with exit 0 at 2026-11-13T21:52:27Z because it successfully retained that blocked outcome. Both checkouts remained clean. This verifies the actual production investigation entrypoint and its retained runtime blocker; it does not establish normal campaign adoption or completed model investigation, planning, implementation, review or acceptance. No repeated model attempt or runtime-path/permission workaround was used.

Safe durable receipt: `production-runtime-receipt.edn`. It contains the controller/compile identities, exact original subprocess launcher/provenance, raw observation hash, eligibility, job/effect state, full initialization error, and the read-only storage finding. It excludes full prompts, unrelated candidate contents, credentials and runtime configuration contents. The original uncertain effect remains retained for explicit inspection after the environment owner restores normal runtime initialization.
