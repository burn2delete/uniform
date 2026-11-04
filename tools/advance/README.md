# Uniform Implementation

A standalone Clojure harness that selects current Spec Kit implementation
increments, publishes early draft PRs, implements and checks them, obtains
independent exact-candidate review, repairs failed checks, waits for CI and
required GitHub reviews, and verifies squash merges before recording progress.
It continues toward full project completion, including self-hosting and the
seedless release. GitHub Projects provides tracking and operator controls.

Matt's 2026-11-03 decision delegates this harness's full integration loop to its
controller. Existing restrictions, external approvals and branch protection
remain effective. This is temporary Clojure repository tooling under AGENTS.md,
D0/D1/D2/D3/D6/D8/D9 and T13, not the Gravity AI runtime or conformance evidence.

## Run

Requires Clojure CLI, Git, authenticated GitHub CLI with repository and Project
access, and the same signed-in Codex CLI used by `../simplify`. Reuses its
app-server client and installed model default; no API key is provisioned.
The inherited protocol baseline is Codex CLI 0.165.0-alpha.1.2; inspect generated
schemas and fixture compatibility before upgrading.

From `tools/advance`:

```text
clojure -M:test
clojure -M:run doctor config.edn
clojure -M:run catalog config.edn
clojure -M:run setup config.edn
clojure -M:run run config.edn
```

`catalog` reads the current checkout without inference or GitHub changes.
`doctor` checks connectivity. `setup` creates/reuses the dedicated Uniform
Implementation Project, views, labels, Priority/Control fields and campaign
issue; no workers start. `run` fetches main and starts the full autonomous loop,
including issue/PR writes and normal squash merges. It does not take over
desktop chats or install a service, schedule or global agent configuration.
Credentials are never changed or escalated by the harness. Denied operations
are recorded and never retried or transferred.

Paths resolve relative to config.edn. Local state, schema outputs, prompt
artifacts and timestamped event logs live under `target/validation/advance`.
Worktrees live outside the checkout. The `parallelism` setting controls host
process concurrency, not work admission or task/attempt allowances.

## Task selection and progress

Every cycle reads `specs/**/tasks.md` from an isolated main snapshot. Marked
`*-implementation-tasks` regions are the editable implementation authority;
their historical migration checklists are excluded. Additional marked Product
regions remain visible even after historical sections. Unmarked feature files
retain current checkboxes up to their historical section, with that ambiguity
explicit for source review. Features explicitly marked as a reference pilot or
historical reference remain catalogued as history; their checklists are not
implementation work or completion prerequisites. Files without classified tasks remain unresolved;
they cannot disappear from the completion decision.

Identity combines the task file with its literal task ID. Unnumbered tasks use
a stable title digest. Source digests, line locations and exact base/tree are
retained. The selector reads actual source, spec, plan, task and evidence owners,
then proposes small independent increments of currently open outcomes. It can
prepare missing planning within an existing task, without inventing new
language requirements or an alternate backlog. It distinguishes development
prerequisites from composition, acceptance and release dependencies, retaining
genuine cycles. Explicit `:priorities` task keys come first, then Project
priority, real prerequisites and work that unblocks completion.

Each outcome has a Project issue; each independently reviewable increment has
a child issue and PR. Source files remain authoritative for requirements and
acceptance. A merged child closes only its own issue. Parent task status changes
only when its source authority changes; partial implementation and independently
accepted completion remain distinct. Removed source outcomes are retained for
attention rather than treated as completed. Source authority changes on main
invalidate stale increment selection before authoring.

Authors own only declared paths, including their source task file for scoped
evidence. The controller commits the first meaningful change and opens a draft
before running local checks and independent review. New checked/reviewed heads
are promoted to ready; repairs preserve the PR and return changed heads to
draft. Exact base/commit/tree and check commands are recorded. CI failures and
actionable GitHub review requests return to repair. Missing/skipped CI provides
no successful-check evidence. No-change repairs, ambiguity, interrupted workers,
restricted operations and uncertain effects retain their evidence for inspection.

## Project controls

The dedicated Project has All work, Workflow, Outcomes and Attention views.
Status records execution separately from operator controls:

| Field | Values | Meaning |
| --- | --- | --- |
| Priority | Highest, High, Normal, Low | Select and dispatch preferred outcomes first |
| Control | Active, Paused, Blocked | Hold dispatch and integration for the issue |

Unset controls mean Active/Normal. Source-task controls apply to its increments;
campaign controls apply to the whole harness. Active workers finish before a
pause takes effect. Status changes never erase operator Control values. Views
can be customized in GitHub to display Priority and Control. Existing Projects
are reused only with recorded identity or the harness marker and repository
link, never by title alone.

## Coordination with Simplify

Both harnesses use the updated code under `../simplify`. They keep separate
campaign state, Projects, issues, branches and coordinator locks. Under the
Git common directory, `uniform-harness/claims.edn` records persistent path
claims; directory/descendant writers cannot dispatch concurrently, including
within one harness. Claims cover authoring, checks and composition, and release
when the candidate is immutable before independent review. A PR awaiting review
or CI does not prevent an isolated successor; each changed candidate must still
compose with current main and receive fresh checks and review. Interrupted or
failed writing retains its claims for inspection. `merge.lock` serializes the
main-write window. Shared denial records
prevent transferring a forbidden controller operation between campaigns.

Restart an older running Simplify process with the updated code before running
both harnesses. An active legacy process without a live protocol registration
blocks startup. Locks coordinate harnesses sharing one Git common directory;
other clones, desktop workers and external mergers still need actual ownership
coordination. The controller rechecks main and remote head, reruns composition
checks/review after main moves, runs exact integration preflight, and verifies
the actual squash parent/tree and main inclusion. External merge races remain
detectable failures, not permission to undo an applied merge.

## Waiting, completion and recovery

Blocked work does not stop independent task discovery. When there is no useful
action, the harness remains waiting and polls actual main, controls and ownership
changes. It does not repeatedly invoke the selector on unchanged inputs. It
keeps existing denied/uncertain work; a differently named increment cannot
bypass a retained writer claim. No fixed task or failed-attempt count applies.

An empty queue never establishes project completion. Completion requires every
current catalog outcome checked, no unclassified or missing outcomes, every
admitted increment verified merged, configured release checks actually passed,
and an independent assessment of canonical coverage, full-language, self-hosting
and seed-retirement evidence on still-current main. Default completion checks
include the document/roadmap validators and `gravity self-host verify`, whose
current fail-closed result is not a completion pass.

The inherited recovery commands preserve state and mutation intents:

```text
clojure -M:run status config.edn
clojure -M:run pause config.edn
clojure -M:run resume config.edn
clojure -M:run retry config.edn INCREMENT-KEY
clojure -M:run reconcile config.edn EFFECT-ID inspected-result.edn
```

`pause` takes effect at scheduler boundaries. Inspect interrupted files, jobs,
threads, PRs and actual GitHub effects before explicit retry or reconciliation.
Status, catalog and pause do not write campaign state. Mutable commands reload
state after acquiring the coordinator lock; rejected second coordinators cannot
overwrite active mutation intents. Ordinary check failures during composition
return to repair with the composed candidate retained. Actual sandbox permission
denials remain restricted, including when reported through check exit/stderr.
Externally merged drafts still require exact passed checks, independent review
and successful head-bound CI before their outcomes can be accepted.

Restricted actions cannot be retried. An uncertain external effect is never
automatically replayed. Reconciliation uses the original operation's observed
result and inspection evidence; see `../simplify/README.md`. Retained path claims
and historical restrictions must not be deleted to bypass unresolved ownership.

## Validation and limits

The test command runs both harness suites: deterministic protocol/model/GitHub
doubles, real temporary Git repositories, task authority parsing, shared writer
and merge conflicts, shared denials, early draft publication, exact review,
failed-check repair and verified squash results. Tests start no real agents and
make no GitHub writes. The scoped harness CI runs this combined suite.

After source/tooling changes also run the repository language-boundary gate:

```text
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Model output is a proposal, not proof. Independent review and source-bound
evidence remain required. Logs are local and credential-redacted but contain
repository content. Traces are live-only; reading them does not replay effects.
The inherited check-process watchdog is a host safeguard; model turns and
controller Git/GitHub calls require operator interruption if stalled.

Official app-server interface: https://learn.chatgpt.com/docs/app-server
GitHub Project interface: https://docs.github.com/en/issues/planning-and-tracking-with-projects/automating-your-project/using-the-api-to-manage-projects
