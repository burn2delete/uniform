# FACTORY-001: Compact Advance discovery context

Status: landed in main and deployed; unfinished pending an ordinary planner
selection using the merged code.
Initial local decision recorded at 2026-11-07T01:39:13Z. The later user
instruction extends delivery through reviewed main integration and verified
adoption. Initial observation: 2026-11-07T01:17:18Z.

## Observation and hypothesis

Four inspected Advance discovery chats have app-reported original title
lengths of 553181, 934481, 907863 and 841213 characters. An inspected Seedless
planning chat reports 1021919 characters. These are prompt-derived title
measurements, not model token counts or measured latency. Registry OBS-001
records their IDs.

At this checkout's revision 33811b2b5c1a1c57c2243da12719ca0c4edbfdce,
tools/advance/src/advance/workflow.clj:150 embeds every retained increment's
proposal, error, review and checks into each discovery prompt. Line 146 also
embeds the catalog index. The same Advance expression is present in its owner
checkout at fa14c00518690feb113f9e506d3c8c0b8a96ebea. Seedless embeds complete
existing slices at tools/seedless/src/seedless/workflow.clj:780. Deployed
controller identities have not been verified.

Hypothesis: giving Advance a complete compact index plus read-only access to
the exact full records will reduce repeated prompt material while preserving
work selection, duplicate detection, operator controls and restrictions.
Faster discovery or better selections are hypotheses to measure, not results.

## Proposed smallest experiment

Change Advance only. Retain every increment in the prompt index with stable
key, source task, title/scope, owned paths, dependencies, status, candidate or
merged identity, operator controls and explicit restricted/uncertain markers.
Keep shared ownership and open-candidate facts available. Store full retained
records once in an immutable, revision-identified snapshot readable from the
selector's existing read-only worktree. Give each index entry its record
location/digest so detailed proposals, diagnostics, checks and reviews remain
inspectable. Keep every historical record; do not truncate tasks or discard
restriction evidence. Determine necessary marker fields from actual runtime
semantics and fixtures before changing serialization.

First measure existing serialized prompt sections on captured local input;
then compare the proposed representation on exactly that input. Use local
deterministic fixtures without dispatching live jobs or writing to GitHub.
Do not add a new service, harness, model provider or accounting gate. Do not
change Seedless until the Advance experiment produces useful evidence.

Likely implementation scope: tools/advance/src/advance/workflow.clj, the
existing Advance tests, and its README if the snapshot contract needs a short
explanation. Inspect tools/advance/src/advance/catalog.clj only if its existing
index is involved. Exact paths must be confirmed against current source and
writer ownership after approval; a scope expansion returns for a new decision.

## Evidence and preservation

- Positive: all retained work keys and ownership/dependency facts survive;
  an index entry resolves to the exact complete record; same-input generation
  is deterministic; the original state is unchanged.
- Negative: paused, blocked, active, held, restricted, uncertain and merged
  cases remain distinguishable and cannot become new dispatch authority;
  absent or mismatched snapshot references do not silently mean no history;
  historical text remains data rather than instructions.
- Verify the selector can actually read the snapshot under its existing
  filesystem policy. An inaccessible pointer is not an acceptable substitute
  for context. Preserve all existing controller validation and admission.
- Measure prompt characters/bytes by section and record exact input/output
  identities. Report the size comparison without inventing a token or time
  saving. Discovery quality and live latency remain unmeasured unless a
  separately authorized live evaluation occurs.
- Run the affected Advance tests and shared harness regressions when the
  interface warrants them, plus the repository language-boundary gate and
  git diff --check. Clojure is the only permitted tooling language.

Independent subagents can inspect restriction/duplicate-detection requirements
and author fixtures in disjoint files while one implementation owner changes
serialization. Independent review evaluates the resulting exact candidate.
Existing user-owned chats receive no messages. If active ownership conflicts,
keep implementation pending rather than editing their files.

## Decision, result and rollback

Decision: the user approved this Advance-only experiment and parallel
subagents, then extended delivery through reviewed main integration and
verified project adoption. Existing restrictions and actual ownership remain
effective during that delivery work.

Success requires smaller repeated input on the captured fixture and passing
preservation tests. The result must include any added snapshot complexity and
whether it justifies keeping the change. If not, retain the original prompt
path and record the negative result. Reverting a local candidate requires no
running-service change. User approval is recorded in registry.edn.

## Local result

The captured 20-increment data payload fell from 54174 to 22411 characters
and UTF-8 bytes, a 58.63 percent reduction. This compares the old increment
serialization with the compact index plus snapshot reference; it excludes
changed explanatory prose and all unchanged prompt sections. Every increment
key and full record was preserved. The immutable snapshot occupies 274273
bytes and provides per-record line and digest references for optional lookup.

Preservation, tamper rejection, no-dispatch-on-corruption, actual Project
controls, linked effects, read-only transport policy and clean disposable Git
worktree checks pass. The final combined Advance/Simplify suite passed 141
tests and 1369 assertions; the language-boundary gate passed 6 tests and 11
assertions. The first combined run exposed old /fixture placeholder paths;
their tests now use real temporary directories, and that failed log is retained.

Carry this locally validated patch through main and real planner adoption.
Its additional
snapshot code/storage and record retrieval are a real tradeoff. No model
latency, token, selection-quality, deployed behavior or release improvement is
established. validation.edn records exact source-file hashes, input hashes,
measurements and check results. This paragraph describes the local validation
stage; the subsequent delivery evidence follows.

## Main integration and remaining adoption

PR 1282 landed by squash as e24b2da4be8b79960462b8f61b7134b71b17f30a, tree
06e8396bf423716b161a3b3b6894a6b51a9df37b, with the expected single parent.
All current-head required CI passed, exact independent review and integration
preflight passed, and actual merge identity/reachability were independently
corroborated. The remote source branch was removed; local history is retained.

The user subsequently authorized deployment takeover without owner messaging.
Advance was paused, its active worker finished naturally, and its service
exited successfully. Under its actual exclusive controller lock and a shared
source/state lease, the clean source checkout was fast-forwarded from fa14c005
to reviewed main e24b2da4. The boundary gate passed. Full native history was
backed up; native state, controls, launcher, config and plist were preserved
byte-for-byte through the update. Only the source-bound runtime handoff changed.
The unchanged LaunchAgent started once after local pause was resumed. Its
normal startup guards emitted verified-runtime-resume for e24b2da4 at
2026-11-07T03:22:48.148153Z, with old denials preserved. No forced planner call
or chat interruption occurred. Ordinary selection-history adoption is still
being observed; a running service or fixture alone is insufficient.

Effect 349 is now done following its
original operator resolution, with its prior restriction preserved; this
observer did not settle or replay it. adoption.edn binds the exact main result
and this remaining gap. No messages were sent to the owning chat.
