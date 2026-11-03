# Uniform Simplify

A standalone Clojure application for one loop: audit current main, identify
evidenced unnecessary complexity, implement independent improvements in
parallel, independently review exact candidates, open PRs, wait for CI, and
merge through one coordinator. GitHub Projects is the visual tracker.

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
   hold overlapping writers until the earlier item merges. Authors get
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
7. Recheck main after preflight and match the remote PR head when merging. The
   coordinator must own the main-write window; its lock does not control other
   applications. Existing GitHub branch protection and reviews still apply.
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
`state.edn` is replaced atomically. A repository-wide file lock prevents two
harness coordinators from running.
The check watchdog does not impose a deadline on model turns or controller
Git/GitHub calls. A stalled call requires host/operator interruption; pause
takes effect at scheduler boundaries, after active operations finish.

```text
clojure -M:run status config.edn
clojure -M:run pause config.edn
clojure -M:run resume config.edn
clojure -M:run retry config.edn FINDING-KEY
```

Pause stops dispatch and merges at scheduler boundaries; active workers finish.
Resume preserves existing PRs, commits, threads and dispositions. Interrupted
authors/reviewers require inspection rather than automatic redispatch. `retry`
explicitly queues an inspected Attention item for repair; restricted actions
cannot use this route. Attention is not completion.

Non-idempotent mutations have durable intents. A connection loss may leave an
operation applied without its result saved. Such effects are uncertain and are
never automatically replayed. Inspect GitHub/Git, then record the actual result:

```text
clojure -M:run reconcile config.edn EFFECT-ID inspected-result.edn
```

The supplied EDN is `{:evidence "What was inspected and where" :result ...}` with
the actual original operation's result. This records an observation; it does not
retry an effect or override denial. Preserve uncertain effects and worktrees.

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
