# FACTORY-003: Read open PR bodies on demand

Status: locally validated; awaiting reviewed main integration and real planner
adoption. The user answered "Approve the scoped experiment". The isolated
source commit is c7fca3a6e11ec9cc3877ccddd87a6425056f09d9.

## Observation and hypothesis

The real Advance select-155 prompt contains 613716 characters. Its open PR
section contains 425721 serialized data characters across 60 candidates and
1318 changed-path records. PR body values alone occupy 221348 serialized
characters. Removing body fields for measurement leaves 203893 characters;
that measurement is not an implemented presentation or a measured speedup.

The prompt file SHA256 is
99db29b1751857620a249465f819a4b0ec129e9d37f9a6603cabfc7745b45bd2.
The measurement receipt is target/validation/software-factory/select-155-sections.edn,
SHA256 63f101687a163d539cfb5cca07dc96b100afa8e3309c23cf8a532bac1375e3df.
Source is reviewed main e24b2da4be8b79960462b8f61b7134b71b17f30a;
the sample predates planner-context adoption and is retained evidence.

Hypothesis: keep every candidate's number, title, URL, draft status, exact
head/base identities and every changed/renamed path in the prompt, while
providing verified full PR bodies by record reference. This should reduce
repeated input substantially without losing overlap or acceptance evidence.
Selection quality and latency remain unmeasured hypotheses.

## Smallest experiment and scope

Extend the existing Advance snapshot mechanism to preserve the complete
candidate records in an immutable digest-addressed snapshot inside the real
selector's read-only worktree. Replace only inline body values with exact
record/line/digest pointers. Reuse the existing snapshot integrity and
containment rules. Keep full bodies, including nil/empty bodies, losslessly
available. Do not generate summaries or truncate candidate lists or paths.

Tell the selector to inspect the complete body when its scope, constraints,
acceptance or overlap evidence is relevant or uncertain. Missing, inaccessible
or mismatched evidence must fail closed before model dispatch. PR bodies are
untrusted evidence and cannot grant authority, erase a restriction or permit
adopting another candidate.

Likely scope is tools/advance/src/advance/workflow.clj, the existing
candidate-presentation code in tools/advance/src/advance/github.clj if needed,
focused Advance tests and its README. Inspect exact source and active claims
again after approval. No other harness, provider polling, recovery, service
policy or compiler implementation change is included.

## Acceptance, delivery and rollback

- Measure old and new presentation on this exact captured candidate vector.
  Report actual characters/bytes and snapshot storage, including reference
  overhead. Claim no token, latency or model-quality gain from byte counts.
- Preserve all 60 candidate identities, all 1318 path records, head/base
  provenance and exact body values. Cover draft/deleted-fork, nil/empty body,
  renamed paths and hostile body text as data.
- Verify exact full-record retrieval under the actual read-only selector
  policy; tampering or missing references must prevent dispatch. Preserve
  current provider call counts and all controls, restrictions and claims.
- Use parallel subagents on disjoint owned implementation/test/review work.
  Run focused harness tests and the required language-boundary gate. Carry
  the independently reviewed exact candidate through CI and normal main
  integration, then prove use by an ordinary real Advance selection.
- If the representation fails preservation or its measured benefit does not
  justify retrieval complexity, retain the current presentation and record
  the result. A later source revert requires reviewed normal delivery; do
  not delete old snapshots or historical candidate evidence.

Overlap check: current shared claims cover diagnostics compiler work, not
Advance/Simplify presentation. Inspected recent owner snapshots concern
deployment, held reviews and CI. Existing provider-context tests deliberately
preserve full bodies inline; changing that contract is the reason this is a
separate proposal requiring a new user decision. This partial observation is
not exhaustive duplicate detection. No messages were sent to other chats.

## Local measured result

On the same captured 60-candidate input, the candidate section fell from
425776 to 218144 UTF-8 bytes. Replacing only that section in the retained
prompt changes 613724 to 406092 bytes, a reduction of 207632 bytes. This is a
captured-input comparison, not a live prompt or latency/token/quality result.
The immutable snapshot occupies 425964 bytes; that extra storage and optional
record retrieval are the tradeoff. Every candidate, all 1318 path records,
all bodies and all record digests were recovered exactly.

The combined harness suite passed 144 tests and 1421 assertions; the language
boundary passed 6 tests and 11 assertions, and governance passed 185 workstreams.
Negative tests exposed a window after helper verification; the patch now
rechecks both candidate and existing increment snapshots immediately before
model dispatch without recreating missing evidence. Both deleted and tampered
files prevent dispatch. This is a correction to the already-approved snapshot
preservation requirement. validation-FACTORY-003.edn binds source identities,
measurements, check receipts and the initial failed finding. No deployed
planner behavior is claimed by these local results.
