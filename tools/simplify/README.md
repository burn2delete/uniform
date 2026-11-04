# Uniform Simplify

A standalone Clojure application for one loop: audit current main, identify
evidenced unnecessary complexity, implement independent improvements in
parallel, independently review exact candidates, open PRs, wait for CI, and
serialize main integration. GitHub Projects is the visual tracker.

This is temporary Clojure repository tooling, not a Gravity AI runtime or a
claim of language, release, self-hosting, or seed-retirement conformance. It
follows AGENTS.md, the foundation contracts, T13 and current workstream rules.
AGENTS.md supersedes historical AI cost/accounting requirements.

## Run

Requires Clojure CLI, Git, GitHub CLI and a signed-in Codex CLI. It uses the
existing Codex login and model default; it does not read or provision API keys.
Tested protocol baseline: Codex CLI 0.165.0-alpha.1.2. Pin the CLI and inspect
its generated JSON schemas when upgrading.

From this directory:

```text
clojure -M:test
clojure -M:run doctor config.edn
clojure -M:run setup config.edn
clojure -M:run run config.edn
```

`doctor` checks connectivity without starting workers. `setup` creates or reuses
the Project, views, labels and campaign issue; it starts no agents. `run` starts
autonomous implementation, issue/PR writes and normal squash merging.
Coordinate the main writer with other active development before starting it.
It does not take over existing desktop chats.
Project reuse requires its recorded identity in this repository's state, or
the harness repository marker and matching repository link. A matching title
alone cannot authorize adopting or changing an existing Project.

GitHub needs repository access and `project` scope. If needed, authorize with
`gh auth refresh -h github.com -s project`. The app never changes credentials,
escalates permissions, bypasses branch protection, force-pushes or retries a
restricted action.

Paths resolve relative to the config file. The checked-in config targets this
repository. State/logs are ignored under `target/validation/simplify`; workers
use worktrees outside the source checkout. Use a distinct campaign and state
directory for a different objective. `parallelism` controls simultaneous host
processes, not findings, attempts, input sizes or useful development work.
New improvement issue markers include the campaign identity. Existing state
retains recorded issue IDs, including older markers, when resuming a campaign.

## GitHub organization

| Feature | Role |
| --- | --- |
| Project | Persistent simplification workspace linked to this repository |
| Campaign issue | Standing objective, audit coverage and exact audited commits |
| Milestone | An audit round grouping its improvements |
| Improvement issue | One independently mergeable change, with evidence and acceptance |
| Sub-issue | A real deliverable under the campaign, not an agent turn |
| Dependency | A prerequisite whose verified merge must happen first |
| Checklist | Implementation, checks, review, CI and verified merge steps |
| Labels/tags | `simplify` plus redundant-work, abstraction, duplication, obsolete or campaign |
| Pull request | Implementation and authoritative CI, linked from its issue |
| Status | Proposed, Ready, Implementing, Review, CI, Merge queue, Done, Attention, Not planned |

All work is a table; Workflow is a board; Audit rounds is a table grouped by
milestone in this project's saved view; Attention is a filtered table.
Milestone, Labels, Parent issue, Sub-issue progress and Linked pull requests are
shown when available. The current view API exposes visible columns but not
grouping; group Audit rounds by Milestone in GitHub's view menu. Do not duplicate
stages as labels. Organization-wide issue types are unnecessary for this
user-owned repository. No sprint dates or deadlines are invented.

Milestone percentages count closed items, including non-implemented outcomes;
verified dispositions determine engineering results. Native sub-issues carry
identity, discussion and progress; checklists stay lightweight. No issues are
created for individual commands, test runs or review turns. GitHub's own
platform/sub-issue limits remain external restrictions and are not bypassed.

## Workflow and evidence

1. Fetch main into a dedicated ref and audit an isolated snapshot. Report actual
   coverage, stable keys, concrete evidence, preserved semantics and owned paths.
   No fixed count of findings is requested.
2. Validate structured output; reject unknown/cyclic dependencies, unsafe paths
   and out-of-scope findings. Repeated dispositioned findings do not prove done.
3. Allocate named branches/worktrees. Implement disjoint changes concurrently;
   hold overlapping active writers until their writing/checking phase ends.
   Read-only review and immutable CI candidates do not reserve paths. Authors get
   workspace-write with no network; auditors/reviewers get read-only access.
   Workers do not commit, push, merge or spawn more agents. The controller owns
   those actions. App-server sandboxes are filesystem policies, not full VMs.
   Before authoring, incorporate current main and verify each prerequisite's
   recorded merge is present. Repairs retain their existing candidate changes.
4. Run configured checks plus author-selected Clojure command vectors through
   app-server's sandboxed `command/exec` without inference. Model-selected argv
   is never executed on the unsandboxed controller. The check-process watchdog
   is configurable separately from semantic admission and accounting. Commit
   locally and obtain independent review of the exact base/commit/tree in a
   fresh thread. Requested changes return to implementation; no-change repairs
   stop for attention. The reviewer returns the exact base/commit/tree, and
   worktree identity is checked across workers, checks, review and publication.
5. Push without force; create/reuse the PR. Failed CI returns to repair. Skipped
   checks remain skipped, not executed-test evidence. Required GitHub human
   reviews remain required. A rollup containing only skipped/neutral checks
   does not provide successful-check evidence.
6. Serialize merges. When main moves, compose it into the candidate, rerun checks,
   obtain fresh review and await the new head's CI. Run the repository's exact
   integration preflight immediately before merging.
7. Recheck main and PR head/base, CI, review and mergeability after preflight.
   Review threads are read with complete cursor pagination; unresolved threads
   wait before preflight and are rechecked before mutation. Match the remote
   PR head when merging. The
   coordinator must own the shared main-integration lock. Other desktop writers
   must use the same integration protocol; it cannot control uncooperative
   external applications. Existing GitHub branch protection and reviews still apply.
   The app does not change those settings. An external write during the final
   request race is detected by actual merge-parent/tree verification and stops
   the campaign for attention; the app does not undo an already applied merge.
8. Verify the actual squash commit's parent, tree and inclusion on main before
   marking Done and closing its issue. Close completed rounds. Finish only after
   a fresh empty audit on still-current main.

An agent's final message is not a passed check or successful merge. Prompt
artifacts, schema/model responses, thread/turn IDs, tool events, check commands,
review tuples and mutation intents are retained locally. Execution is live-only;
reading its trace does not replay models or tool effects.

## Console and recovery

Verbose timestamped JSON lines go to stderr and `events.jsonl`: commands and
output, elapsed time, app-server requests/events, transitions, GitHub results,
waiting reasons and failures. Common credential fields and known token patterns
are redacted; logs still contain repository content. Keep logs local.
`state.edn` is replaced atomically. A controller lock derived from the canonical
state directory prevents two owners of that state. State is reloaded and its
campaign identity revalidated after successful lock acquisition. A failed
contender, read-only status command or shutdown hook cannot save a stale
snapshot over the owner.
The check watchdog does not impose a deadline on model turns or controller
Git/GitHub calls. A stalled call requires host/operator interruption; pause
takes effect at scheduler boundaries, after active operations finish.

The implementation harness in `../advance` shares persistent path claims,
denied-operation records and a main-write lock through the Git common directory.
Disjoint authors can run concurrently; overlapping active writers wait. Claims
release before independent review, so immutable candidates awaiting review or
CI do not prevent isolated successors. Failed/interrupted writing claims persist.
Restart older Simplify processes with this version before running both tools.
These cooperative locks cover harnesses in the same Git repository; coordinate
other main writers and separate clones explicitly.

```text
clojure -M:run status config.edn
clojure -M:run pause config.edn
clojure -M:run resume config.edn
clojure -M:run retry config.edn FINDING-KEY
```

Pause stops dispatch and merges at scheduler boundaries; active workers finish.
Resume preserves existing PRs, commits, threads and dispositions. Interrupted
authors/reviewers require inspection rather than automatic redispatch. `retry`
queues an inspected Attention item at its retained recovery phase: verified
merges finish tracking, existing CI candidates return to CI, and unfinished
implementation returns to repair. Restricted actions and unresolved current
external effects cannot use this route. Attention is not completion.

Non-idempotent mutations have durable intents. A connection loss may leave an
operation applied without its result saved. Such effects are uncertain and are
never automatically replayed. Inspect GitHub/Git, then record the actual result:

```text
clojure -M:run reconcile config.edn EFFECT-ID inspected-result.edn
```

The supplied EDN is `{:evidence "What was inspected and where" :result ...}` with
the actual original operation's result. This records an observation; it does not
retry an effect or override denial. Preserve uncertain effects and worktrees.

## Parallel campaigns and deployment

Simplify, Seedless and Advance can plan, implement, check and review concurrently with
separate Projects, campaign identities, state directories and worker worktrees.
Do not reuse one campaign's state for a different objective. Deploy compatible
versions of all consumers; old versions use an incompatible lifetime repository
lock and must be stopped before this protocol is installed.

The shared runtime's `coordination` function supplies the per-state controller
lock, `uniform-main.lock`, `uniform-harnesses.edn` and its short metadata lock
under the common Git directory. Each controller has its own snapshot ref and
serializes its own fetches without writing shared `FETCH_HEAD` or tracking refs.

Claim active paths before dispatch with `claim-paths!`, run the actual writing
or checking phase with `with-claimed-paths`, then release before read-only
review or CI. Busy paths are ordinary waiting. A known-ended failed or blocked
turn releases its lease while its failure/refusal remains recorded. Unknown
termination retains only the affected paths for original-owner inspection;
there is no candidate, task or failed-attempt quota.

Only the final integration transaction uses `try-with-main-lock`: refresh the
immutable base and current evidence, run exact preflight, recheck main, merge
with the matched head, and verify actual parents/tree/reachability. Busy or
fenced main is ordinary waiting. If the base moved, release this lock before
composition, checks and independent review. Lock order is controller, main,
then snapshot/short coordination operations; metadata operations never call
back into Git or acquire the main lock.

Inside the native `effect!` callback, call `begin-main-merge!` before the provider
mutation. After actual Git/provider verification, call `complete-main-merge!`
with the observed commit/tree/parents/main, before post-merge bookkeeping. A
verified merge is saved as `:finalizing`; issue closure and Project updates run
after releasing the main lock and never reauthor the merged candidate. A
crash or uncertain result leaves a durable shared fence even when the file
lock is released. Another campaign cannot merge until the original owner
records the inspected outcome; it can continue independent source work.

For the one-time transition, while old controllers are stopped, register every
preserved campaign and operator state before starting compatible controllers:

```text
clojure -M:run coordinate config.edn
```

This performs no model or GitHub calls. It preserves known refusal guards
across campaigns and imports explicitly recorded legacy unknown-writer holds,
including their exact owned paths, and legacy claims/refusals from
`uniform-harness/claims.edn` and `denials.edn`. Imported claims require inspected
original-owner release; they are never assumed to have ended. A known local
active lease can be reused by scheduler and worker, but a persisted or unknown
lease cannot be reclaimed by starting another controller.
This is deployment recovery, not a prerequisite
for ordinary edits or focused tests. Inspection releases are not expiry timers.

Original-owner recovery commands are explicit:

```text
clojure -M:run reconcile-main config.edn EFFECT-ID inspection.edn
clojure -M:run release-writer config.edn lease.edn inspection.edn
```

A shared main inspection contains `:evidence` and either `:outcome :not-applied`
or `:outcome :merged` with the actually observed `:actual` commit/tree/parents/main.
A legacy incomplete tuple also needs its inspected `:original-intent`.
Writer inspection contains `:evidence`; the lease file is the original shared
lease, including its token. These commands do not rerun a denied operation,
clear its native refusal/effect record or confer acceptance. Native effect
reconciliation is still separate. For an inspected `:not-applied` main result,
run `reconcile` with the observed original result and evidence, then explicitly
run `retry`. The local receipt retains `:prior` and binds the accepted shared
inspection. Only that exact source tuple can receive a new transaction ID;
the old attempt stays recorded. A restricted action cannot take this path.
An own unresolved main effect requires Attention before another provider read;
another campaign's fence remains ordinary integration waiting.

When the response and remote inspection establish that a request did not apply,
use `{:outcome :not-applied :result nil :evidence "Exact inspected response and remote state"}`.
For non-merge effects this records a failed, unapplied attempt and permits its corrected operation;
the old attempt and receipt remain in history. It cannot reclassify a restricted
operation, and unknown outcomes still cannot be automatically replayed.
Main mutations instead require the shared inspection, local binding and explicit
successor intent described above; a legacy `:not-applied` merge still fences main.

CI readiness includes completed workflow runs for the exact candidate, with all
pages observed and the normal CI workflow successful. A successful harness job
does not establish readiness while the normal workflow is queued and absent
from the check rollup. This evidence is checked again around integration preflight.

## Validation and references

Tests use a Clojure JSON-RPC fixture process with no inference, plus real
temporary Git repositories and deterministic GitHub/model doubles. They cover
output validation, asynchronous events, failed turns, approval refusal,
dependencies, ownership, uncertain effects, exact review binding and a complete
audit-to-verified-squash loop. No real agents or GitHub mutations run in tests.

The scoped `.github/workflows/simplify-harness.yml` workflow runs these tests
for harness/toolchain changes, using the existing pinned Clojure setup action.

After source changes, run the root language-boundary gate:

```text
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Official interfaces:

- https://learn.chatgpt.com/docs/app-server
- https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/using-the-api-to-manage-projects
- https://docs.github.com/en/issues/tracking-your-work-with-issues/using-issues/adding-sub-issues
- https://docs.github.com/en/issues/using-labels-and-milestones-to-track-work/about-milestones
- https://docs.github.com/en/rest/issues/issue-dependencies

Source and test fixtures were authored for this user-requested harness change.
The development conversation and local check logs record their development
provenance; they confer no migration authority.
