# Uniform Seedless

A standalone Clojure controller for replacing the Clojure bootstrap with
Uniform. It plans concrete seed dependencies, implements independent compiler,
runtime and build slices, independently reviews immutable candidates, and
integrates their PRs through one coordinator. GitHub Projects is the visual
tracker. This is temporary repository tooling, not the Uniform AI runtime or
evidence that the compiler is already self-hosted.

## Commands

Requires Clojure CLI, Git, GitHub CLI with repository/Project access, and the
existing signed-in Codex CLI. It reuses `tools/simplify` through a local Clojure
dependency; no API key is requested or provisioned. The shared app-server
protocol baseline is documented in that tool's README. The installed Codex
model default is used unless `:model` or role-specific `:models` entries are
configured (`:plan`, `:implement`, `:review`). Actual model, thread, prompt,
schema and tool events are retained in the local trace.

From `tools/seedless`:

```text
clojure -M:test
clojure -M:run doctor config.edn
clojure -M:run setup config.edn
clojure -M:run run config.edn
clojure -M:run status config.edn
clojure -M:run status-summary config.edn
clojure -M:run pause config.edn
clojure -M:run resume config.edn
```

`doctor` checks existing connectivity without starting workers or writing to
GitHub. `setup` creates/reuses only the identified Seedless Project, its views,
labels and campaign issue; it starts no workers. `run` starts autonomous source
implementation and GitHub writes, including normal merge commits. Installation
and tests never launch the campaign, create schedules or install services.

`status-summary` reads an existing native `:seedless/v1` state snapshot and local
pause control before startup context is constructed. It performs no Git or
provider probes, lock acquisition, controller registration, coordination import,
event/state writes or service actions. A missing/malformed state, incompatible
schema, mismatched configuration identity or malformed existing control fails
closed. Raw `status` remains available with its existing startup behavior.

The summary preserves the recorded identity, every task key/status and every
Attention item, linked effect references, recorded model-job identities,
restrictions and local control. State-file pointers and a digest identify the
exact snapshot read; local control is a separate observation. Configuration
identity is checked against the recorded fields without revalidating Git or
remote identity. The output distinguishes an effect's current status from its
retained refusal and settlement history. A completed effect does not erase an
active restriction or grant permission to replay its original action.

Recorded model jobs do not cover all native checks or other processes. These
records establish neither service health, worker drain, idleness nor dispatch
authority. State and control may change after they are read. The command takes
no arguments after the config path and creates no state for an absent campaign.

Paths resolve from the config file. State, prompt artifacts and redacted JSONL
logs live under ignored `target/validation/seedless`; worker worktrees are
outside the source checkout. Use a distinct state directory and campaign for a
different objective. State cannot be reused with a different objective or
completion policy. `:parallelism` limits concurrent host processes, not useful
work, findings, attempts or semantic input sizes. Check watchdogs are actual
host restrictions and are separate from compiler admission.

## Handover and main ownership

Before `run`, explicitly hand over the main-write window from any desktop
coordinator or other automation. Existing chats and open PRs are not silently
adopted. Reconcile their actual state and avoid duplicate work before handing
over. The planner receives this campaign's retained candidates and a read-only
open-PR snapshot; it must not duplicate in-flight work or treat an unmerged
dependency as current main.

Each campaign has its own controller/state lock. Seedless and Simplify can
plan, implement disjoint paths, test and review concurrently. Their shared
coordination records protect actual active writers; immutable candidates
awaiting review or CI do not reserve paths. Git snapshots use separate refs
without changing shared `FETCH_HEAD` or remote-tracking refs.

A short main-integration lock covers the fresh base, exact acceptance/check
validation and preflight, matched-head merge and actual parent/tree verification.
Composition, model turns, checks and review run outside this lock. An uncertain
main write leaves a durable shared fence requiring inspection and reconciliation;
another campaign cannot treat lock release as proof that the mutation ended.
These locks cover worktrees sharing one Git common directory, not other clones
or desktop applications. Operator coordination is still required for external
main writers. Pause takes effect at scheduler boundaries, lets active operations
finish, stops dispatch/merging, and releases controller ownership when `run`
returns. Resume preserves candidates and PRs.

Before either compatible controller starts, register each preserved stopped
campaign with `coordinate CONFIG` in its own tool directory. This acquires its
state lock, reloads identity, and imports original refusals, uncertain merges
and explicitly recorded unknown writers without starting jobs or writing to
GitHub. An unknown writer keeps only its actual owned paths held for inspection.

Commands that can change campaign state acquire its lock and reload/validate
state after acquiring it. A lock-busy contender does not change the live state.
`doctor` therefore also requires the campaign lock; `status` and atomic pause
control do not mutate the execution state. Startup Git probes cannot persist a
stale snapshot. State identity binds the checkout, Git common directory, remote,
base branch, worktree root, Project policy, checks and campaign objective.
Fetch and push URLs must match the configured GitHub repository.

Workers have isolated worktrees. Authors get workspace-write with network
disabled; planners/reviewers get read-only. These are app-server filesystem
policies, not VMs. Workers do not spawn agents, mutate Git/GitHub, commit, push
or change permissions. The controller executes proposed Clojure checks through
sandboxed `command/exec`, not through its unsandboxed process runner.

## The replacement loop

1. Read existing contracts, the SH completion plan and actual source closure.
   Identify the Clojure owner, Uniform successor, actual consumer, preserved
   facts, accepted/rejected cases, dependencies and residual host boundary.
2. Start disjoint writers in parallel. Publish a draft PR at the first coherent
   implementation checkpoint, before full validation. Each slice keeps one
   issue, branch, worktree and PR across repairs.
3. Execute the claimed behavior. Tests must distinguish real caller adoption
   from injected leaf execution or a host fallback. Quoted templates, catalogs,
   stdout replay, file moves and compatibility dispatch confer no retirement
   credit by themselves.
4. Independently review the exact base/commit/tree and evidence scope. An
   implementation slice does not need a new authoritative migration proof.
   Historical pins, denied actions and old failed runs keep their status.
5. Wait for actual CI. Missing or all-skipped checks are not successful evidence.
   Failed checks return to source repair; required human GitHub reviews remain
   required. Unresolved review threads prevent integration.
6. Compose current main, refresh focused checks/review, publish normally and
   await that head's CI. Ordinary owned merge conflicts return to the author;
   an ownership conflict requires attention. No force push or protection bypass.
7. Run exact integration preflight, recheck main and merge with a matched remote
   head. Verify the actual two parents, tree and reachability before closing
   the slice. An unexpected external merge race stops for inspection; an
   already-applied merge is not undone.
8. Continue discovery while immutable candidates await CI. The planner runs
   again when main, candidate dispositions or completion evidence changes;
   it does not spin on the same unchanged audit inputs.

An explicit `stack-on` dependency permits a child to begin from a frozen parent
candidate before the parent merges. Active conflicting writers remain serialized.
Children retarget to main and normally compose the verified parent merge before
their own integration. Normal merge commits preserve stack ancestry. Branches
and worktrees remain available for recovery.

## GitHub tracking and completion

The Project is `Uniform Seedless`. Views are All work, Workflow, SH milestones
and Attention. Native milestones group SH stages; labels show
compiler/runtime/build and implementation/adoption/seed-removal/rebuild/
equivalence/release scope. Issue bodies retain the source/consumer/evidence
detail. Native sub-issues and dependency links connect deliverables. Group the
SH milestones view by Milestone using GitHub's view menu.

Projects require verified remote owner, Seedless marker and repository link,
including recorded IDs. A durably recorded new creation may initialize only its
identity marker/link before verification; unrelated remote drift is rejected.
Issue/milestone identities also bind the campaign objective. Existing unrelated
Status fields are not overwritten. No issues are created for individual agent
turns or test commands. No sprint dates are invented.

Done means a verified slice merge. The harness never automatically closes an
SH milestone or rewrites its acceptance ledger. Existing roadmaps, BOOT1-8,
TEST13 and release evidence own those decisions.

An empty audit cannot finish the campaign. On an exact clean revision, the
controller runs the existing `clojure -M:gravity self-host verify` route and
any additional configured completion checks, then obtains independent review
for that exact revision and rechecks main. The canonical verifier cannot be
removed from config. Its current `P18T04007` failure is a real remaining gap.
A failed completion check informs another gap analysis; unchanged failures
are not repeatedly rebuilt. Final completion is bound to its recorded source,
target/subset and evidence, not to issue counts or Clojure line counts.

## Interrupted and uncertain work

Atomic state and durable effect intents come from the existing Simplify runtime.
An interrupted author/reviewer needs inspection. Resume does not silently rerun
it. Interrupted controller cleanup signals only process objects owned by that
invocation; it does not perform global or descendant process discovery. It
records unknown descendant coverage and requires inspected recovery, without
claiming that all children stopped. Restricted cleanup remains restricted.
For a non-restricted Attention slice, record what was inspected in EDN:

```text
{:evidence "Inspected the retained worktree and original job trace; ..."}
clojure -M:run retry config.edn SLICE-KEY inspection.edn
```

Use `audit` as the key for inspected non-restricted planning/completion or
controller-interruption failures. The original unknown-coverage observation
remains in inspection history.
This preserves their history and queues new work; it does not turn the failure
into success. Restricted actions cannot be retried or routed through this path.

An uncertain GitHub effect is not automatically replayed. Inspect the actual
external result, then record it using the inherited reconciliation format:

```text
{:evidence "Inspected GitHub/Git at ..." :result ORIGINAL-OPERATION-RESULT}
clojure -M:run reconcile config.edn EFFECT-ID inspected-result.edn
```

If the shared main journal also retains that original merge, its owner must
inspect and reconcile it separately using `reconcile-main CONFIG EFFECT FILE`.
The EDN needs `:evidence`, `:outcome :merged` with verified `:actual` commit,
tree, parents and observed `:main` commit, or `:outcome :not-applied`. Verify
that the merge is reachable from the observed main. Reconciliation never retries the
mutation or removes its denial. A retained unknown writer can be released only
by its original state owner with `release-writer CONFIG LEASE.edn INSPECTION.edn`;
the exact lease token and inspected evidence are required. Never clear either
record merely because its process is absent.

An unresolved original main intent blocks run, setup, retry and integration
before provider queries, even when the local merge command exited successfully.
For an inspected `:not-applied` outcome, explicitly reconcile the original local
effect against that same shared inspection, then retry the slice. This also
applies to a previously successful command receipt. Only a matching accepted
base/candidate/tree and non-restricted settlement permit a new inspected intent;
the original receipt and inspection remain in history. No uncertain or denied
intent is automatically replayed.

An already merged slice remains in finalization until its tracking writes are
complete. Tracking failure never queues another author for the merged code.
Pending/uncertain task effects must be reconciled before retry; restricted
effects cannot be retried. Restricted/uncertain failures are recorded locally
without additional GitHub reporting writes.

Logs redact known credential fields/patterns but contain repository content.
Keep them local. Traces are live-only; reading a trace does not replay models
or tool effects. Controller Git/GitHub/model calls have no hidden time/attempt
budget; host interruption and the configured check watchdog remain distinct.

## Validation

Tests use deterministic model/GitHub doubles and real disposable Git
repositories. They never launch real workers or mutate GitHub. They exercise
early draft publication, stacked ancestry and retargeting, focused checks,
independent review, changing main, conflict repair, merge races, uncertain
effects and completion refusal while the seed is still required. The scoped
`seedless-harness.yml` workflow runs these tests without starting the tool.
The existing Simplify tests cover the reused transport and recovery primitives.

After source changes also run the root language-boundary gate. See
`docs/development-verification.md`, `bootstrap/README.md`, the SH completion plan,
and BOOT1/3/5/7/8 plus TEST13 for the implementation/integration/admission
distinction. Harness code, test fixtures and deterministic doubles were authored
for this request; their check results are tool-development evidence only.
