# A10 - Human-in-the-Loop and Human-Review Workflow Specification

Sequence: 163
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines `:ai/human-review` as a typed workflow effect.
Human-review is not an informal UI checkbox and not a prompt instruction. It is a
recorded decision over an action payload, actor identity, role, policy reason,
expiry, and replay rule.

The human-review system lets Gravity agents participate in high-impact workflows
while keeping final authority with an authorized human or governance-defined
emergency process.

## Human-Review Declaration

A human-review declaration contains:

- human-review id and version;
- action schema;
- required role or principal policy;
- evidence shown to the reviewer;
- payload hash rule;
- expiry and revocation behavior;
- denial and timeout behavior;
- replay behavior;
- audit storage policy;
- emergency override policy when allowed.

Human-review declarations are referenced by policies, tools, workflows, and agent manifests.

## Requirements

- Human-review-required actions MUST stop until a matching human-review record exists.
- Human-review records MUST bind actor identity, role, action schema, payload hash, timestamp, expiry, and policy reason.
- The reviewed payload MUST match the executed payload.
- Human-review records MUST expire according to the declaration.
- Revoked human-review records MUST not authorize later execution.
- Denial MUST be a first-class workflow outcome.
- Human-review UIs or APIs MUST show enough typed evidence for the reviewer to understand the action.
- Replay MUST not ask for new human-review unless replay policy declares live human-review.
- Emergency bypass MUST be explicit, scoped, logged, and reviewable.
- Human-review records MUST respect redaction policy while preserving audit hashes.

## Human-Review States

A human-review instance moves through:

- `:requested`;
- `:granted`;
- `:denied`;
- `:expired`;
- `:revoked`;
- `:bypassed-by-emergency-policy`;
- `:replayed`;
- `:invalidated-by-payload-change`.

Only `:granted`, `:replayed`, or an allowed emergency state can authorize the
action, and only when payload hash, role, expiry, and policy still match.

## Semantic Dependencies

- `L6` defines human-review as an effect.
- `SAFE10` defines authority and capabilities.
- `R7` defines workflow replay.
- `R12` defines diagnostic and audit records.
- `A4` defines privileged tools.
- `A6` defines workflow human-review nodes.
- `A8` defines policy rules that require human-review.
- `S3` defines canonical payload hashes.
- `S9` defines human-review record artifacts.

## Outputs and Artifacts

The compiler emits:

- human-review manifest;
- action schema link;
- required role or principal policy;
- evidence schema;
- expiry rule;
- denial and timeout branch rules;
- replay rule;
- conformance fixture list.

The runtime emits:

- human-review request record;
- rendered evidence record or hash;
- grant, denial, expiry, revocation, replay, or bypass record;
- actor identity reference;
- payload hash;
- policy reason;
- audit redaction report.

## Example

```clojure
(defhumanreview submit-review
  {:action ReviewSubmission
   :requires-role :repo-maintainer
   :evidence ReviewEvidence
   :expires-in "15m"
   :on-deny :finish-without-write
   :replay :reuse-recorded-decision-if-payload-matches})

(workflow/human-review submit-review findings)
```

The human-review decision authorizes only the exact `findings` payload hash shown to the
reviewer. If the agent changes the findings after human-review, the review is
invalidated.

## Rejection Rules

- Reject execution of a human-review-required action with no human-review record.
- Reject human-review reuse when payload hash changes.
- Reject human-review by a principal lacking the required role.
- Reject expired or revoked human-review records.
- Reject human-review records missing evidence hash.
- Reject replay that asks for live human-review when replay policy forbids it.
- Reject emergency bypass without declared emergency policy.
- Reject human-review ledgers that expose protected data beyond redaction policy.

## Diagnostics

- `A10001` reports missing human-review.
- `A10002` reports wrong reviewer role.
- `A10003` reports expired human-review.
- `A10004` reports revoked human-review.
- `A10005` reports payload mismatch.
- `A10006` reports denial branch selected.
- `A10007` reports replay human-review violation.
- `A10008` reports invalid emergency bypass.

Diagnostics include human-review id, workflow node, action schema, payload hash,
actor reference, policy reason, expiry, and remediation.

## Conformance Criteria

- A legal human-review declaration emits a human-review manifest and action schema link.
- A write-tool workflow blocks until human-review is granted.
- A denied human-review request follows the declared denial branch.
- A changed payload invalidates a previous human-review decision.
- An expired human-review record cannot authorize execution.
- Replay reuses human-review decisions only when policy and payload match.
- An emergency fixture records bypass scope, reason, actor, and review requirement.
