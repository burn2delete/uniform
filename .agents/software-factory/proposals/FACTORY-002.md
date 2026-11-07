# FACTORY-002: Read-only Advance status summary

Status: implemented and locally validated; unfinished pending main integration
and verified project adoption under the user's updated completion rule.
Observation: 2026-11-07T01:41:16Z.

## Observation and hypothesis

tools/advance/src/advance/main.clj:42 delegates status to Simplify.
tools/simplify/src/simplify/main.clj:153 prints the entire state with pr-str.
The observed Advance state file was 5597126 bytes, with 20 increment records.
A read-only capture for FACTORY-001 showed 18 Attention, one ready and one done.
The original campaign state was not modified. Those counts describe a captured
local state, not verified live worker or service health.

Hypothesis: an additional compact, explicitly observational status command
would make routine monitoring easier and avoid repeatedly transferring full
retained histories. This could reduce interpretation errors, but no monitoring
latency or error reduction has been measured. Existing raw status remains
available. No status-summary equivalent was found in inspected local Advance
or Simplify source, including their original owner checkouts; this is not an
exhaustive inventory of in-flight work.

## Smallest proposed experiment

Add an Advance-only status-summary command. Read a single retained local state
snapshot and optional local control file. Report campaign identity, snapshot
digest, recorded catalog base/tree, counts by state, every Attention key with
its actual error/effect identity and restriction/uncertainty markers, recorded
job state, and pointers to complete records. Separate recorded execution state,
operator controls and unverified process/service liveness. Do not label a
recorded running job as a proven live process. Do not invent recovery advice.

Use existing state readers and redaction helpers. No network calls, process
probing, worker dispatch, coordination import, lock acquisition, native state
writes, effect replay or service changes. Missing/malformed state and control
data must be explicit rather than interpreted as a healthy empty campaign.
Keep all records; this is a presentation projection, not an admission bound.

Likely scope: tools/advance/src/advance/main.clj, existing Advance tests, and a
short README command entry. Confirm actual ownership and current source after
approval. Do not modify shared Simplify or Seedless for this experiment.

## Evidence and decision

Test mixed recorded states, explicit pause controls, nested restricted and
uncertain effects, missing control information, malformed/missing state,
credential redaction, deterministic output and unchanged source files. Stub
provider, dispatch and mutation functions to fail if called. Verify all
Attention keys survive and each displayed detail links back to the original
state. Compare output size against raw status on the same captured input;
make no claim about live health or recovered work. Run focused tests and the
language-boundary gate for source changes.

The user approved the local experiment through the change-decision question,
then extended delivery through reviewed main integration and verified project
adoption. The local result below is evidence history, not completion.

## Local result

The captured native state was 5597126 bytes; the new summary is 29648 UTF-8
bytes, a 99.47 percent reduction. All 18 Attention items survive, along with
counts for all 20 tasks and the recorded states of 47 jobs. The captured
control file was unavailable, and the summary says so. It reports process and
service liveness as unverified.

`clojure -M:run status-summary config.edn` is now available from tools/advance.
It parses and digests one captured state read, rejects malformed carriers and
record values, reports malformed local controls, and takes its CLI path before
controller context creation, startup logging and shutdown-hook installation.
Raw status delegation is unchanged.

Tests cover source consistency across concurrent file changes, explicit
missing/malformed inputs, pending/missing/restricted/uncertain effects,
credential redaction, and the real CLI path with mutable/provider paths
guarded. The final combined suite passed 141 tests and 1369 assertions; the
language-boundary gate passed 6 tests and 11 assertions. validation.edn binds
the exact source hashes and measurements. This is a local presentation
improvement; interpretation quality and live health remain unmeasured.
