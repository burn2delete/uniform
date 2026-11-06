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
   require attention. The reviewer returns the exact base/commit/tree, and
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
   request race is detected by actual merge-parent/tree verification and records
   Attention while preserving the fence; the app does not undo an applied merge.
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

An ordinary `needs_input` report may retain an owned dirty patch while waiting
for an external source dependency. After that dependency has a verified merge
on current main, the original operator can inspect the retained work without
pretending it was a verification-only restriction:

```text
clojure -M:run recover-dependency config.edn FINDING-KEY inspected-source.edn
```

This command uses the original controller lock; it is not a second live
controller or a reason to pause a running service. When deploying this recovery
support, queue an eligible inspection at the necessary stopped deployment
boundary and promptly return the same service. The command never starts it.

The inspection file is retained under the original state directory. It binds
the original job/error/prompt/base/candidate/tree, exact changed-file bytes and
executable modes, and a nonblank `:source-provenance` basis and SHA for every
file, including separately authored supplements. Its operator classification
is `:verified-external-source-dependency`, `:inspector :controller-operator`,
`:phase :source-development-current`, the exact original-output SHA and explicit
no-unresolved-authority/no-unresolved-semantics findings. A model report alone
does not provide this classification.

Each `:verified-source-dependencies` entry contains `:owner-state-file`,
`:intent-id`, `:commit`, `:candidate`, `:tree`, `:history-sha256` and changed
`:source-paths`. It must match an actual shared `:main-verified` history entry;
the merge must advance the retained base and be an ancestor of fresh main, with
matching Git tree and at least one changed dependency path outside the author's
scope. This records an observed dependency, not new ownership of its files.
Actual Git parents must match the recorded merge or squash intent, and the
candidate tree must match the value integrated on Main. Each entry also binds
`:source-phase`: the original Source `:owner-state-file`, `:lease-id`,
`:lease-token`, `:paths`, current accepted `:candidate` and `:tree`, explicit
`:writer-ended?` and `:no-unresolved-authority` findings, and immutable
`:release-evidence` and `:acceptance-evidence` references (`:path`, `:sha256`).
The original release record must retain the same lease identity and paths.
Current relevant writer rows and refusals for that exact Source owner still
block recovery. A Main publisher is not substituted for the Source author;
unrelated historical owner identities remain separate held evidence. A later
unchanged published-Main import may use an authenticated evidence join without
inventing a new lease or rewriting the original release or acceptance records.

For an admitted immutable lineage, tag the phase `:accepted-immutable-lineage`
instead of inventing a creation lease. Its `:acceptance-evidence` is parsed as a
`:source-only-admission` record with `:disposition :accepted-source-only`, a
`:subject` binding the candidate, tree, exact paths, per-path blob/mode entries,
known current `:owner-state-files`, and the admitted source parent/tree. Every
source entry must equal Git at both that parent and the current candidate.
Immutable references to the genuine independent reviews remain in `:evidence`.
The admission's `:nonclaims` must retain `:no-retroactive-source-ownership`,
`:no-standalone-held-source-admission`, `:no-release` and
`:no-migration-authority`. This is source evidence, not integration authority.
The strict original-lease branch remains available for an owned dirty source
phase and uses the same parsed source-value admission.

Both variants require `:status-evidence`: an immutable
`:dependency-source-status` record binding that entire subject, `:known-end true`
and `:current-status {:writers {} :refusals {}}`. The controller compares it to
the current shared records for those exact Source identities and paths. An old
Main publisher or unrelated historical hold is not relabeled as the Source
creator; relevant active or unknown writers and refusals still block. Protected
Main history and actual merge parentage are separately required in both cases.

`:preserved-failed-actions` is the exact ordered vector of completed exit-one
semantic checks, each with `:id`, `:command`, `:exit` and `:output-sha256`.
`:preserved-nonclaims` explicitly retains historical nonpasses and evidence
boundaries. Those old checks are never relabeled as passed. Native terminal
events must prove an empty active-action set even when older job metadata lacks
that field. Actual refusal text, privileged requests, unknown/unfinished actions,
other failure exits, stale source or a retained restriction still reject this
route. In particular, it cannot recover HTTP permission or process-inspection
refusals, renew reader pins, or confer migration authority.

An accepted inspection queues native checking only. The controller commits the
retained owned patch, normally composes fresh main, runs the configured and
originally proposed checks, and obtains independent exact review and new-head
CI. The old job, output, error and nonpasses remain unchanged. A changed queued
inspection or later refusal stops the continuation before authoring or checks.

### Fresh validation after a retained status or check failure

`recover-authored` and `recover-dependency` do not reclassify a genuine HTTP
refusal, failed check or unknown cleanup. When the human specifically authorizes
fresh validation of the committed source, a distinct route preserves those
records and uses a separate, clean, independently accepted normal composition:

```text
clojure -M:run recover-fresh-source config.edn FINDING-KEY fresh-source.edn
```

The evidence is retained inside this controller's state directory. It binds the
entire original task SHA, PR, error SHA, explicit operator classification and
preserved nonclaims; immutable human authorization and independent Source and
composition evidence; the exact fresh base/commit/tree/normal parents, named
branch and worktree; and specific configured-toolset checks. The authorization
scope `:fresh-prNNN-status-validation` or `:fresh-prNNN-whitespace-validation`
must name this original PR and cover the actual validation step. A retained
status-read refusal additionally needs the newly authorized identical argv,
successful original-provider response, matching original head and open/main
identity bound to that authorization. An old approval or cached old status
result is insufficient. An unresolved shared API refusal still blocks.

The accepted Source bookend binds `:source-review` (source-only disposition,
zero findings, original committed source and owned paths) and `:composition`
(fresh base/candidate/tree and ordered source/Main parents). The controller
checks the real Git merge tree and rejects foreign paths, a dirty/detached or
reused original worktree, stale input, or any refused proposed check. It never
reads or edits the abandoned old worktree or retries its failed commands.

Queueing archives the complete original task in
`:fresh-source-validation-history`, retains its original error and all global
guards, and records a distinct fresh phase. Native preparation imports current
Main, performs fresh checks and passive independent review, then publishes a
native-owned follow-up PR with a new durable creation-effect identity. It does
not replay the original PR creation or close the original PR. No new author turn
is needed for already committed source. New failures replace the current phase's
error normally while the complete old records remain immutable in history.
Old Macro/process-cleanup results stay nonpasses with their original unknown
boundaries; only fresh exact-head CI can satisfy the new candidate's gates.

Known-ended, nonrestricted semantic check failures return to repair with the
failed source tuple and check result retained. The repair must preserve meaningful
checks, including authenticity and provenance. Restricted or uncertain effects
and unknown writer termination remain Attention. The controller waits for an
explicit pause while blocked; it does not silently exit or redispatch those
tasks. Temporary writer contention and pending CI also wait. CI/review repair
and waiting for missing CI evidence are enabled by default.

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
