# R11 - Runtime Capability Enforcement Design

Sequence: 122
Phase: 8 - Runtime Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Runtime capability enforcement is the last authority gate after compile-time
effect checking, package policy, deployment policy, and backend/runtime
selection. It prevents modules, plugins, tools, workflows, agents, and FFI
adapters from acquiring authority through ambient runtime channels.

Runtime checks do not grant new authority. They enforce the authority already
declared in manifests and provide auditable grant, deny, delegate, revoke, and
redaction decisions.

## Requirements

- Every authority-bearing runtime action must carry effect, capability,
  principal, package, deployment, artifact, and source/provenance identity.
- Capability checks must be deny-by-default when no matching grant exists.
- Runtime enforcement must cover filesystem, network, database, environment,
  process, shell, secrets, FFI, raw memory, device access, model calls, tool
  calls, memory writes, `:ai/human-approval`, deployment, package mutation, and
  observability sinks.
- Delegated handles must be scoped, typed, revocable when supported, and tied to
  provider policy.
- Capability decisions must be recorded according to audit policy without
  leaking secrets.
- Tools and plugins are checked against their own declared contract as well as
  the caller's grant.
- Runtime enforcement must reject broader profile assumptions that are denied by
  package or deployment policy.

## Dependencies

- `L15`, `SAFE10`, `SAFE11`, `SAFE13`, and `P13` define capabilities, taint,
  AI/tool safety, and profile boundaries.
- `B10`, `B11`, `B12`, `R1`, `R7`, `R8`, `R9`, `R10`, and `R12` define runtime
  actions requiring enforcement.
- Package and deployment phases define install-time and release-time policy
  inputs.

## Outputs and Artifacts

- Runtime capability manifest.
- Capability table.
- Principal and identity record.
- Runtime decision log.
- Delegated handle record.
- Revocation record when supported.
- Denial diagnostic.
- Redaction and secret-handling record.
- Capability conformance evidence.
- Runtime capability diagnostics.

## Runtime Manifest

```clojure
{:artifact :gravity/runtime-capabilities
 :inputs #{:compiled-effects :package-policy :deployment-policy
           :principal :artifact-manifest}
 :handles #{:filesystem :network :database :secrets :process :shell
            :ffi :raw-memory :model :tool :approval}
 :decisions #{:grant :deny :delegate :revoke}
 :records #{:capability-table :decision-log :denial-diagnostic}
 :rejects #{:ambient-authority :effect-outside-grant
            :tool-contract-violation}}
```

The manifest is runtime-specific but all runtimes use the same decision shape.

## Decision Model

A runtime decision contains:

- action id,
- principal or agent id,
- namespace/module/package id,
- artifact id,
- source span or generated-origin edge,
- requested effect,
- requested capability,
- provider,
- policy inputs,
- grant or denial,
- delegated handle id when granted,
- redaction policy,
- audit status.

Denials are deterministic when policy inputs are deterministic.

## Handles and Delegation

Capability handles are typed values that narrow authority. They may represent a
file root, network host, database connection, secret scope, process provider,
tool contract, model provider, memory provider, approval token, or observability
sink. Delegation records name the grantor, recipient, allowed effects, lifetime,
revocation behavior, and audit requirements.

Ambient globals are not handles.

## Tools, Plugins, and Agents

Runtime enforcement checks both caller and callee:

- a caller must have authority to invoke a tool or plugin,
- the tool or plugin may perform only effects in its own contract,
- AI agents must satisfy model/tool/memory/approval policy,
- workflow steps must satisfy replay, idempotency, and capability policy,
- FFI adapters must satisfy foreign effect policy.

## Diagnostics

Runtime capability diagnostics use `R11` identifiers:

- `R11-GRANT` for missing or denied capability grants.
- `R11-AMBIENT` for attempts to use ambient filesystem, network, environment,
  secret, process, shell, model, tool, or provider authority.
- `R11-PRINCIPAL` for missing or invalid runtime identity.
- `R11-DELEGATE` for invalid delegation or handle scope.
- `R11-REVOKE` for use after revocation or unsupported revocation assumptions.
- `R11-TOOL` for tool or plugin effects outside contract.
- `R11-SECRET` for secret access or logging outside policy.
- `R11-OBSERVABILITY` for logs, traces, or metrics sinks without authority.
- `R11-AUDIT` for missing decision logs where policy requires them.
- `R11-MANIFEST` for incomplete capability artifacts.

Diagnostics must include action id, source span or artifact edge, principal,
effect, capability, provider, policy source, decision, redaction status, and
remediation.

## Rejected Designs

Gravity rejects ambient authority at runtime.

Gravity rejects treating compile-time profile allowance as deployment authority.

Gravity rejects unscoped capability handles.

Gravity rejects tools and plugins performing effects outside their contracts.

Gravity rejects decision logs that expose secrets or omit required audit data.

## Conformance Criteria

A conforming runtime capability system must demonstrate:

- deny-by-default behavior,
- grant, deny, delegate, and revoke records,
- enforcement across filesystem, network, database, secrets, process, shell,
  FFI, raw memory, model, tool, memory, `:ai/human-approval`, and observability
  actions,
- caller/tool/plugin dual-contract checks,
- secret redaction and audit records,
- deployment policy narrowing of broader compile-time permissions,
- source/provenance links in denial diagnostics.
