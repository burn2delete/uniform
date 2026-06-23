# A10 - Human-in-the-Loop and Approval Workflow Specification

Sequence: 163
Phase: 11 - AI and Agentic Programming
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This specification defines `:ai/human-approval` as a typed workflow effect.
Approval is not an informal UI checkbox and not a prompt instruction. It is a
recorded decision over an action payload, actor identity, role, policy reason,
expiry, and replay rule.

The approval system lets Gravity agents participate in high-impact workflows
while keeping final authority with an authorized human or governance-defined
emergency process.

## Approval Declaration

An approval declaration contains:

- approval id and version;
- action schema;
- required role or principal policy;
- evidence shown to the approver;
- payload hash rule;
- expiry and revocation behavior;
- denial and timeout behavior;
- replay behavior;
- audit storage policy;
- emergency override policy when allowed.

Approvals are referenced by policies, tools, workflows, and agent manifests.

## Requirements

- Approval-required actions MUST stop until a matching approval record exists.
- Approval records MUST bind actor identity, role, action schema, payload hash, timestamp, expiry, and policy reason.
- The approved payload MUST match the executed payload.
- Approvals MUST expire according to the declaration.
- Revoked approvals MUST not authorize later execution.
- Denial MUST be a first-class workflow outcome.
- Approval UIs or APIs MUST show enough typed evidence for the approver to understand the action.
- Replay MUST not ask for new approval unless replay policy declares live re-approval.
- Emergency bypass MUST be explicit, scoped, logged, and reviewable.
- Approval records MUST respect redaction policy while preserving audit hashes.

## Approval States

An approval instance moves through:

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

- `L6` defines approval as an effect.
- `SAFE10` defines authority and capabilities.
- `R7` defines workflow replay.
- `R12` defines diagnostic and audit records.
- `A4` defines privileged tools.
- `A6` defines workflow approval nodes.
- `A8` defines policy rules that require approval.
- `S3` defines canonical payload hashes.
- `S9` defines approval record artifacts.

## Outputs and Artifacts

The compiler emits:

- approval manifest;
- action schema link;
- required role or principal policy;
- evidence schema;
- expiry rule;
- denial and timeout branch rules;
- replay rule;
- conformance fixture list.

The runtime emits:

- approval request record;
- rendered evidence record or hash;
- grant, denial, expiry, revocation, replay, or bypass record;
- actor identity reference;
- payload hash;
- policy reason;
- audit redaction report.

## Example

```clojure
(defapproval submit-review
  {:action ReviewSubmission
   :requires-role :repo-maintainer
   :evidence ReviewEvidence
   :expires-in "15m"
   :on-deny :finish-without-write
   :replay :reuse-recorded-decision-if-payload-matches})

(workflow/approval submit-review findings)
```

The approval authorizes only the exact `findings` payload hash shown to the
approver. If the agent changes the findings after approval, the approval is
invalidated.

## Rejection Rules

- Reject execution of an approval-required action with no approval record.
- Reject approval reuse when payload hash changes.
- Reject approval by a principal lacking the required role.
- Reject expired or revoked approvals.
- Reject approval records missing evidence hash.
- Reject replay that asks for live approval when replay policy forbids it.
- Reject emergency bypass without declared emergency policy.
- Reject approval ledgers that expose protected data beyond redaction policy.

## Diagnostics

- `A10001` reports missing approval.
- `A10002` reports wrong approver role.
- `A10003` reports expired approval.
- `A10004` reports revoked approval.
- `A10005` reports payload mismatch.
- `A10006` reports denial branch selected.
- `A10007` reports replay approval violation.
- `A10008` reports invalid emergency bypass.

Diagnostics include approval id, workflow node, action schema, payload hash,
actor reference, policy reason, expiry, and remediation.

## Conformance Criteria

- A legal approval declaration emits an approval manifest and action schema link.
- A write-tool workflow blocks until approval is granted.
- A denied approval follows the declared denial branch.
- A changed payload invalidates a previous approval.
- An expired approval cannot authorize execution.
- Replay reuses approval only when policy and payload match.
- An emergency fixture records bypass scope, reason, actor, and review requirement.
