# Domain implementation task authority

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).

## Implementation tasks

The seven literal project keys below identify grouped outcome records. Each
retains its full remaining-work, acceptance, integration and decision text;
no clause IDs, external dashboard object identities or new project population
are invented. All remain Open. Aggregate domain release and application-sector
claims remain separate; no domain or compiler implementation is accepted here.

<!-- domains-implementation-tasks:start -->
- [ ] `math` Math

Status: Open

Projected task block:
Outcome: [math contracts](../../docs/phase-05-mathematical-and-elementary-function-system/README.md) execute with the declared
numeric semantics and checked justification for transformations. Remaining
finite tasks, all open: implement numeric-mode/domain checking; implement EFIR
lowering and evaluation; connect EML/certificate checking to justified rewrites;
implement certified approximation/interval and symbolic operations; run the
contract conformance matrix with rejected unproved transformations.

Yes/no acceptance: do domain and branch errors reject? Do proof-dependent
rewrites require checked evidence? Do strict-mode results match the selected
semantics? Integration: Types/MIR to EFIR, Opt proof preservation, Lower/Stdlib
numeric APIs. Decision owner: numeric maintainer; recommendation: keep EML as
adjacent proof/search representation, never ordinary runtime equality authority.

- [ ] `schema` Schema

Status: Open

Projected task block:
Outcome: generated consumers enforce the same source schema. Remaining tasks,
all open: implement schema normalization and validators; implement canonical
serialization and version identities; generate GraphQL/OpenAPI and ABI/config
artifacts; implement checked migration mapping; execute source/generated parity
and weakening/taint/invalid-data rejection fixtures.

Yes/no acceptance: is schema identity stable? Do generated consumers reject
invalid values? Are migration loss and secret exposure prevented or explicitly
rejected? Integration: Types to schema, Emit generated artifacts, Build schema
identity, Query/AI/Stdlib consumers. Decision owner: schema maintainer;
recommendation: shared canonical schema IR with explicit target adapters.

- [ ] `query` Query

Status: Open

Projected task block:
Outcome: real parameterized queries return validated rows and respect declared
transaction/provider semantics. Remaining tasks, all open: implement relational
IR and typed parameters; lower to a declared provider; connect row/schema and
transaction checks; execute result-equivalence, injection, missing-grant and
invalid-row fixtures. Yes/no acceptance: are parameters separated from query
syntax? Are results typed and grants enforced? Do transaction failures preserve
semantics? Integration: Schema/MIR to Query, Runtime provider, Stdlib database
API. Decision owner: query maintainer; recommendation: explicit provider dialect
contract, not host SQL strings as semantic authority.

- [ ] `workflow` Workflow

Status: Open

Projected task block:
Outcome: a workflow executes and replays without repeating recorded external
effects. Remaining tasks, all open: implement graph validation/lowering;
implement event-log schema and durable transitions; connect retry/compensation/
timer providers; execute crash/restart/replay and unrecorded-nondeterminism
rejection fixtures. Yes/no acceptance: is replay deterministic? Are external
calls deduplicated? Are invalid transitions rejected? Integration: MIR/Schema,
Runtime durability, AI review steps, Stdlib workflows. Decision owner: workflow
maintainer; recommendation: versioned event log as replay input, explicit
provider boundary for storage rather than a hidden database choice.

- [ ] `ai` AI

Status: Open

Projected task block:
Outcome: the declared AI workflow executes through schema, capability, taint
and human-review boundaries. Remaining tasks, all open: implement declaration
lowering; connect model/tool/memory providers with explicit policies; implement
structured-output and taint validation; connect human review and replay;
execute evaluations plus prompt-injection, missing-review, cross-tenant and
unvalidated-output rejection fixtures. Yes/no acceptance: does untrusted output
remain data? Are actions denied without grants/review? Are evaluations bound to
exact prompt/model/tool identities? Integration: Schema/Auth, Workflow/Runtime,
Stdlib AI. Decision owner: AI maintainer; recommendation: provider-independent
artifacts and explicit model identity, never a vendor SDK side path.

- [ ] `gpu` GPU

Status: Open

Projected task block:
Outcome: kernels execute on the adopted target with validated memory/barrier
behavior. Remaining tasks, all open: adopt target and kernel ABI; implement GPU
IR/checking; lower kernel and host dispatch; execute numeric/result and illegal
memory-space/barrier fixtures. Yes/no acceptance: do kernels produce the
specified results? Are invalid synchronization and target features rejected?
Integration: MIR/Legality, Lower/Emit, explicit Runtime dispatch. Decision owner:
accelerator maintainer; recommendation: one named reference target first,
additional targets only by their own conformance evidence. Target choice is
not silently adopted by this roadmap.

- [ ] `ui` UI

Status: Open

Projected task block:
Outcome: an actual UI artifact renders, handles typed events and enforces
capabilities through the adopted adapter. Remaining tasks, all open: lock UI
IR/state/event contract; implement lowering and rendering adapter; connect
runtime events and source maps; execute render/update and invalid-event/unsafe
DOM/authority rejection fixtures. Yes/no acceptance: do state transitions and
rendering follow typed semantics? Are unsafe or unauthorized actions rejected?
Integration: Types/MIR, Lower/Emit, Runtime and Stdlib UI. Decision owner: UI
maintainer; recommendation: explicit adapter contract rather than browser host
semantics defining the language. Backend choice remains an explicit decision.
<!-- domains-implementation-tasks:end -->

<!-- domains-wider-integration-proposals:start -->
## Wider integration task proposals

These nine existing source-family records are explicit future implementation
tasks, resumed from reconciliation commit
`55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6`. Their IDs, Open states,
criteria, owner roles and dependencies are retained below. They remain in this
sole task file, not a second editable ledger. They are outside the current
seven-key SH projection; no SH routing, acceptance or implementation credit is
added. Exact source/facet/owner overlap and projection composition require
affected independent review. Isolated development uses declared interfaces,
not completion of whole components or a new routine permission gate.

The [full preserved Domains context](https://github.com/burn2delete/uniform/blob/55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6/specs/requirement-reconciliation/domain-gap-completion.edn)
contains all seventy historical criterion records; its SHA-256 is
`cbb47d98fc6120ecfc9a9145c8897151046a89feada36c98f5f820842e844d42`.
This checkout's same-named local file retains only the MIR-pass criterion's
captured context. References in the carried records are therefore rebound
explicitly to the full preserved context above; no other record text is
changed. Source requirements remain normative, and that context does not
establish full entailment, provider/target adoption or execution acceptance.

The thirty carried criterion records retain 65 source facets marked
`support-surface` and unresolved across ten documents in that preserved
context. These name wider replacement/support breadth, not newly discovered
product choices. The reference SAXPY, CRUD, simulation, crypto/chain and proof
examples cannot discharge that breadth. Domain architecture and the named
specialist owners retain the full source-surface allocation and required
cross-component refinement; unresolved support stays visible and receives no
completion credit from these finite proposed tasks.

Project accounting remains removed. Concrete chain/provider protocol fees,
cryptographic noise/depth/precision, semantic checks and actual target timing
or platform constraints retain their independent meaning. No live crypto,
chain, process, provider, publication or held C12 operation is executed or
authorized by resuming these future records.

- [ ] `automation` Typed automation and action audit

Status: Open

Projected task block:
Outcome: Typed automation and action audit with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/automation-plan-audit`: Produce declared dry-run/audit records of planned writes/deletes/deployments/commands without hiding the eventual effect contract. Accepted fixture: Produce a dry-run of writes/deletes/deployments/commands and compare it with the authorized actual action audit. Negative fixture: Reject omitted effects, misleading dry-run authority or unredacted secret-bearing audit fields. Required evidence: dry-run plan and source-linked action audit; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/file-transform-integration`: Demonstrate declared typed file transform with root/path/command authority and invalid-input rejection. Accepted fixture: Transform declared input/output files with argument/path schemas, granted roots, source-linked actual audit and a dry-run. Negative fixture: Reject malformed input, root escape, missing write grant and tainted command construction. Required evidence: script package, path/argument schemas, dry-run and file-change audit; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime. Decision owner: Typed automation and action audit maintainer; person unassigned. Provider/target choice remains explicit.

Decision criterion `interface/domain/emit-automation-dry-run` (Open): Emit a source-linked plan of the declared writes, deletes, deployments and commands from typed arguments and granted scope without performing those actions. Refuse: Reject planning that performs an external action, hides a planned effect, expands authority or leaks secrets; a plan is not an actual-change audit. Required outputs: typed dry-run plan and no-action trace with source/authority fields. Dependencies consume build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/execute-typed-process-command` (Open): Invoke the declared process/shell provider using typed argument/environment/command schemas, quoting/escaping, taint validation, timeout, exit/error mapping, grants and policy-required human review. Refuse: Reject tainted string-built commands, missing schemas/grants/review, undeclared environment inputs and untranslated exit/error behavior before an unauthorized invocation. Required outputs: typed command invocation/result/error and review/authority audit fixtures. Dependencies consume build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/record-automation-changes` (Open): Record the actual executed file/automation changes and outcomes with source, command/path, effect, authority and required review context in the declared audit log. Refuse: Reject a dry-run plan presented as executed change, omitted/forged changes, missing authority/source linkage and secret-bearing audit disclosure. Required outputs: actual-change audit record reconciled with executed operation/result identities. Dependencies consume build/remaining-tasks-and-evidence/execute-typed-graph-under-declared-build-effects, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

- [ ] `blockchain` Deterministic chain and programmable accounts

Status: Open

Projected task block:
Outcome: Deterministic chain and programmable accounts with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/account-session-delegation`: Constrain programmable-account session keys and delegated execution by identity, entrypoints, grants, storage/reentrancy, replay domain, nonce and revocation policy. Accepted fixture: Execute a scoped session call and revoke its delegation across the declared account, entrypoint, nonce and replay domain. Negative fixture: Reject revoked, cross-domain, expired or storage/reentrancy-exceeding delegation. Required evidence: session authorization and revocation trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/blockchain-token-transfer-integration`: Demonstrate typed token transfer with selected chain/account/ordering/resource/state/authorization and failure evidence. Accepted fixture: Transfer tokens under pinned chain/account/ABI/state/authorization/ordering rules and prove conservation and insufficient-balance behavior. Negative fixture: Reject unauthorized mutation, unchecked overflow, nondeterminism and incompatible upgrade. Required evidence: contract/ABI artifact, conservation fixture and upgrade report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/chain-state-transition`: Execute typed, versioned deterministic contract state transitions with declared numeric/canonical/storage/protocol-resource and authorization semantics. Accepted fixture: Execute deterministic typed/versioned state transitions with explicit arithmetic, serialization, canonicalization, storage and selected protocol resource semantics. Negative fixture: Reject host nondeterminism, unchecked arithmetic, unknown ABI/state schema or unauthorized mutation. Required evidence: transition trace, state/ABI schema and protocol conformance report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/eip7702-delegation`: Validate and apply selected EIP-7702 authorizations with tuple/signing domain, delegate code/loading, nonce/replay/duplicate/revocation/failure and client accounting semantics. Accepted fixture: Validate the selected EIP-7702 authorization tuple/signing domain, code/delegate loading, nonce/replay/revocation and client gas semantics. Negative fixture: Reject wrong chain/domain/nonce, duplicate or stale authorization and incompatible delegation/failure behavior. Required evidence: pinned EIP-7702 profile and authorization/client fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/erc4337-operation-processing`: Implement the selected ERC-4337 EntryPoint/UserOperation/account validation flow with explicit domains/nonces/factory/aggregation/bundler/simulation and wallet/client contracts. Accepted fixture: Execute selected EntryPoint/UserOperation validation, nonce/domain/factory, aggregation, bundler/simulation and wallet encoding contracts. Negative fixture: Reject stale or missing profile/version/schema, invalid signature/validation time and incompatible chain/client assumptions. Required evidence: pinned ERC-4337 profile, encoding and operation-processing fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/erc7579-module-lifecycle`: Implement selected modular-account interface/type/module install/configure/uninstall/revoke and authorization-hook semantics. Accepted fixture: Install, configure, invoke, uninstall and revoke declared module types and authorization/hooks with account compatibility metadata. Negative fixture: Reject invalid module interfaces/types, unauthorized hooks and invocation after revocation. Required evidence: ERC-7579 profile, module manifest and lifecycle fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/paymaster-enforcement`: Validate sponsor/paymaster operations and post-operation behavior under declared deposit/stake/quota/gas/anti-drain policy. Accepted fixture: Validate sponsorship and post-operation accounting under named payer, selected protocol deposit/stake/quota/gas and anti-drain/failure policy. Negative fixture: Reject unbounded sponsorship, missing postOp accounting, unauthorized payer use or incompatible EntryPoint assumptions. Required evidence: paymaster profile, sponsorship/anti-drain and failure fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/transaction-order-safety`: Model order dependence explicitly and produce evidence that declared visibility/reordering/MEV mitigations preserve invariants under the chosen chain/provider model. Accepted fixture: Model declared mempool visibility, ordering authority, sequencer/builder/private-flow trust and MEV exposure with slippage/deadline/nonce/max-loss or equivalent mitigation evidence. Negative fixture: Reject missing ordering assumptions, unsafe private-flow fallback or invariant-breaking reordering. Required evidence: ordering/MEV report and adversarial ordering/provider-fallback fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries. Decision owner: Deterministic chain and programmable accounts maintainer; person unassigned. Provider/target choice remains explicit.

Decision criterion `interface/domain/execute-erc4337-operation` (Open): Consume the admitted operation/validation result and execute the account call under the selected pinned chain, protocol resource, failure and wallet/client encoding contract; preserve validation/execution phase separation. Refuse: Reject failed, stale or mismatched validation before execution, illegal phase reentry and chain/client/resource-contract mismatches. Required outputs: account execution trace linked to validation identity, protocol result/failure and wallet/client artifact. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/execute-paymaster-post-operation` (Open): Execute the selected paymaster postOp settlement or declared failure path after its corresponding operation, preserving phase order and actual protocol/provider sponsorship accounting. Refuse: Reject duplicate or out-of-order postOp, mismatched operation/payer identity and missing settlement/failure records. Concrete protocol accounting does not reinstate project work allowances. Required outputs: postOp execution/settlement/failure trace bound to operation, payer and selected protocol policy. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/validate-erc4337-operation` (Open): Validate and simulate the selected pinned EntryPoint/UserOperation/account flow under explicit signing, replay-domain/nonce, factory, aggregation, bundler and state-freshness policy. Refuse: Reject invalid signature/nonce/domain, stale simulation, wrong profile/chain/encoding and unsupported factory/aggregation/bundler assumptions before issuing an execution admission. Required outputs: versioned operation validation/simulation result and typed admitted-operation descriptor. Dependencies consume schema, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

- [ ] `crypto` Cryptographic and protected-computation boundaries

Status: Open

Projected task block:
Outcome: Cryptographic and protected-computation boundaries with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/credential-lifecycle`: Apply declared credential rotation/recovery/decommissioning/unknown-or-accepted signaling policy with audit records. Accepted fixture: Rotate, recover, decommission and signal unknown/accepted credentials under the declared account binding, custody and privacy policy. Negative fixture: Reject use of decommissioned credentials, unsupported recovery or privacy-leaking lifecycle records. Required evidence: redacted credential lifecycle and policy audit; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/encrypted-aggregation-integration`: Demonstrate encrypted aggregation using approved arithmetic and valid custody/parameter/noise/depth/precision/transcript/reveal evidence. Accepted fixture: Aggregate protected values through approved arithmetic with explicit custody, participants, scheme parameters, noise/depth/precision/transcript and reveal policy. Negative fixture: Reject implicit decrypt, secret-dependent branching, missing custody or operations outside concrete provider constraints. Required evidence: protected aggregation parameter/boundary manifests and test vectors; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/passkey-assertion`: Verify passkey assertions using origin/RP/account binding, fresh challenge/replay protection, signature and required presence/verification proof. Accepted fixture: Verify origin/RP/account binding, fresh challenge/replay protection, signature, user-presence and policy-required verified user evidence. Negative fixture: Reject challenge reuse, origin/RP mismatch, missing signature/verification proof and unsupported custody assumptions. Required evidence: redacted authentication transcript and credential-policy fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/passkey-registration`: Register credentials through fresh challenges, RP/origin/consent/storage policy and declared attestation/user-verification/trust/custody evidence. Accepted fixture: Register credentials with fresh challenge, RP/origin/consent/storage policy and explicit attestation trust, presence/verification, backup/sync and custody assumptions. Negative fixture: Reject undeclared trust roots, treating synced passkeys as device-bound and attestation tracking beyond privacy policy. Required evidence: registration transcript, WebAuthn/passkey manifest and privacy/replay fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/protected-disclosure-boundary`: Require explicit authorized audited encrypt/decrypt/reveal/reconstruct/attest/compare/branch/serialize/prompt/log/export boundaries with declared leakage/reveal policy. Accepted fixture: Authorize and audit encrypt/decrypt/reveal/reconstruct/attest/compare/branch/serialize/prompt/log/export operations with explicit reveal reason, participants and leakage state. Negative fixture: Reject implicit crossings, unsupported plaintext/ciphertext mixing and disclosures beyond declared output/metadata/timing/transcript policy. Required evidence: protected-boundary manifest and leakage/disclosure fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/protected-provider-execution`: Execute supported FHE/MPC/threshold/enclave/private operations while validating and updating scheme-specific noise/depth/scale/precision/transcript/participant state. Accepted fixture: Execute the declared FHE/MPC/threshold/enclave operation subset, validating and updating actual scheme noise/depth/scale/precision/transcript/participant state. Negative fixture: Reject unsupported operation or parameter/custody/adversary assumptions, stale state and exceeded concrete scheme constraints. Required evidence: provider parameter manifest, updated protected-state records and test vectors; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/protected-value-typing`: Type protected computation values/operations with explicit scheme, parameters, custody, participant/adversary and leakage state. Accepted fixture: Distinguish plaintext, ciphertext, shares, commitments, blinded/enclave and revealed values with scheme, parameters, key custody, participants/adversary and leakage metadata. Negative fixture: Reject unauthorized value-kind coercion, missing custody/leakage state and unsupported protected operations. Required evidence: typed protected-value report and positive/negative type fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/token-signing-integration`: Demonstrate token signing with declared key/randomness/provider policy, test vectors and denied secret disclosure. Accepted fixture: Sign tokens through the declared secret key/provider/algorithm and approved randomness when required, with scoped key access and test vectors. Negative fixture: Reject secret logging/prompting, ungranted key access and unsafe or undeclared provider boundary. Required evidence: signing provider/wrapper manifest, test vectors and redaction trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries. Decision owner: Cryptographic and protected-computation boundaries maintainer; person unassigned. Provider/target choice remains explicit.

Decision criterion `interface/domain/check-protected-operation-state` (Open): Before dispatch, check the selected scheme, parameter set, custody, participants/adversary and current noise/depth/scale/precision/transcript state; emit an admitted operation descriptor only for supported semantics. Refuse: Reject unsupported operations, absent custody/participant evidence, stale state and unsatisfied concrete scheme constraints before the provider operation. Required outputs: source-bound protected-operation admission descriptor and refusal diagnostics. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/execute-protected-operation` (Open): Consume an admitted protected-operation descriptor, run the selected FHE/MPC/threshold/enclave/private provider operation and emit updated scheme state or valid state-neutrality evidence with provider provenance. Refuse: Reject execution without matching admission, unsupported provider behavior, missing updated/neutrality evidence and unauthorized leakage or disclosure. Required outputs: actual provider result, protected-state transition or neutrality proof, parameter/custody/transcript provenance. Dependencies consume auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, legality/remaining-tasks-and-evidence/complete-safe-code-classification-and-taintunsafe-boundaries as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

- [ ] `formal` Formal proof verification and integration

Status: Open

Projected task block:
Outcome: Formal proof verification and integration with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/formal-bounds-integration`: Demonstrate declared checked bounds proof with property/source/counterexample and invalid-proof evidence. Accepted fixture: Emit a source/MIR-bound array bounds obligation, obtain proof or source-mapped counterexample, check the certificate and gate check elision on the valid proof. Negative fixture: Reject stale/invalid proof, hidden solver assumption or check elision before verification. Required evidence: bounds obligation, proof-check report and counterexample/elision fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/proof-checking`: Check proof artifacts against declared property, trust/provider/assumptions and exact source/artifact identity before proof-dependent use. Accepted fixture: Check a proof against the exact property, source/artifact/profile/target, trusted basis, provider and assumptions before dependent use. Negative fixture: Reject unverified or stale certificates, hidden solver assumptions and EML syntax identity used as semantic proof. Required evidence: claim/proof/assumption manifests and verifier acceptance/rejection report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/recursive-proof-chain`: Validate recursive/folding/IVC parent links, root state, step-relation hashes and stale-proof applicability. Accepted fixture: Verify recursive/folding/IVC parent links, accumulator/root state and source-bound step relation hashes before using the root result. Negative fixture: Reject missing parents, stale step proofs, invalid root state or incompatible setup/provider assumptions. Required evidence: proof-chain manifest, root verification and stale-chain fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/zk-relation-integration`: Demonstrate the declared checked zero-knowledge relation with intentional public outputs and rejected invalid/private-boundary cases. Accepted fixture: Emit and verify the selected relation with public/private/witness schemas, custody/disclosure policy, source/MIR hashes, setup/trust/key identity and intentional public outputs. Negative fixture: Reject malformed witness, hidden setup, stale proof/key/provider, invalid chain or contradictory privacy output evidence. Required evidence: relation/witness/setup/verifier artifacts, privacy and verification/rejection reports; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/zk-relation-verification`: Verify declared circuit/constraint/AIR/R1CS/Plonkish/zkVM relations with explicit public/private/witness/setup/trust/key/proof-system/verifier contracts. Accepted fixture: Check the declared circuit/constraint/AIR/R1CS/Plonkish/zkVM relation against public/private/witness, setup/trust, proof-system/provider and verifier-key contracts. Negative fixture: Reject missing/mismatched relation/input/key linkage, hidden setup/provider assumptions or unsupported privacy claims. Required evidence: relation/claim/setup/input manifests and verifier acceptance/rejection fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes. Decision owner: Formal proof verification and integration maintainer; person unassigned. Provider/target choice remains explicit.

Decision criterion `interface/domain/check-zk-proof` (Open): Execute the selected ZK verifier or qualified proof-kernel/provider on the exact proof, relation, public inputs, setup, verifier key and provider assumptions, binding its actual result to those identities. Refuse: Reject invalid, stale or mismatched proof inputs and preserve privacy/custody/disclosure manifests and commitments; never require private witness values to be disclosed to a public verifier. General evidence admission and recursive-chain validation remain separate consumers. Required outputs: actual ZK verification result and identity/privacy-bound accepted or rejected proof record. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/emit-zk-relation-contract` (Open): Emit the selected relation/proof-family, privacy, public/private/witness schema, disclosure/public-output and verifier contract bound to exact source, MIR, schema and relation hashes. Refuse: Reject missing or contradictory relation, schema, disclosure, witness-custody, proof-family or verifier metadata; contract emission is not proof production or verification. Required outputs: source-bound ZK relation/privacy/input/output/verifier manifests and contract diagnostics. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/generate-zk-witness` (Open): Generate the witness for the exact admitted relation using declared input origins, randomness/nondeterminism and secret-custody/transcript rules. Refuse: Reject malformed or wrong-relation witnesses, unrecorded nondeterminism and custody/transcript disclosure violations; keep private witness values out of public verification inputs. Required outputs: relation-bound witness-generation record with permitted origin/custody/transcript metadata. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/prepare-zk-setup` (Open): Produce the selected setup/trust/key provenance record by consuming transparent/trusted setup, ceremony transcript when used, proving/verifier key hashes and provider assumptions. Refuse: Reject missing trust model, stale or mismatched keys/setup/provider assumptions. Require no universal new ceremony or key-provisioning engine; selected external records remain declared inputs. Required outputs: setup/trust/proving-key/verifier-key provenance manifest and compatibility refusals. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/produce-zk-proof` (Open): Run the selected prover on the bound relation, witness and setup to emit the actual proof and observed prover/verifier resource and conformance records. Refuse: Reject wrong relation/witness/setup, missing proof/result provenance and fabricated or missing observed resource records; producing a proof does not verify it. Required outputs: actual prover proof object, relation/witness/setup linkage and observed conformance/resource report. Dependencies consume legality/remaining-tasks-and-evidence/preserve-obligations-into-mir-and-runtime-execution, emit/current-evidence-and-remaining-work/emit3-bind-provenance-to-bytes as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

- [ ] `native` Native performance slice

Status: Open

Projected task block:
Outcome: Native performance slice with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/native-saxpy-integration`: Demonstrate declared scalar/vector SAXPY behavior against a mode-correct reference and reproducible target/input/layout/proof/measurement evidence. Accepted fixture: Emit scalar/vector SAXPY under explicit numeric mode, layout, target features and bounds/alias/alignment justification; compare with the mode-correct reference. Negative fixture: Reject proofless check elision, target UB, undeclared feature assumptions or unqualified fast math/benchmark claims. Required evidence: native artifact/ABI, proof bundle, differential and contextual benchmark report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume backend-llvm, opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: backend-llvm, opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors. Decision owner: Native performance slice maintainer; person unassigned. Provider/target choice remains explicit.

- [ ] `services` Backend service integration

Status: Open

Projected task block:
Outcome: Backend service integration with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/service-crud-integration`: Execute the declared CRUD service using typed API, query/transaction, runtime authority and failure evidence. Accepted fixture: Execute typed CRUD request/response/auth/error routes with schema/taint checks, prepared database transaction, grants and source-linked observability. Negative fixture: Reject schema-less route, ungranted filesystem/secret access, secret logging and broken transaction/failure mapping. Required evidence: service/API/config/transaction artifacts and service integration trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume query, schema outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: query, schema. Decision owner: Backend service integration maintainer; person unassigned. Provider/target choice remains explicit.

- [ ] `simulation` Deterministic simulation slice

Status: Open

Projected task block:
Outcome: Deterministic simulation slice with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/fixed-step-simulation-integration`: Execute declared fixed-step physics/simulation with timestep, numeric/determinism/replay/realtime allocation/blocking and target/frame semantics. Accepted fixture: Execute the fixed timestep physics/entity step with explicit numeric mode, deterministic recorded inputs, allocation/blocking policy and target timing evidence. Negative fixture: Reject hidden frame allocation, unrecorded gameplay randomness and unapproved numeric or plugin authority changes. Required evidence: simulation manifest, replay comparison and target/frame measurement report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume math, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: math, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries. Decision owner: Deterministic simulation slice maintainer; person unassigned. Provider/target choice remains explicit.

- [ ] `storage` Storage durability and recovery

Status: Open

Projected task block:
Outcome: Storage durability and recovery with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/storage-engine-durability`: Implement declared page/binary/checksum/WAL/concurrency/persistence-order and corruption/recovery behavior. Accepted fixture: Implement declared endian/alignment/page/checksum/WAL layout, concurrency, persistence ordering and fsync/provider crash recovery semantics. Negative fixture: Reject schema-less binary layout, unsupported durability assumptions or undetected corruption. Required evidence: storage layout/WAL schema, persistence-order and crash/corruption fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/wal-recovery-integration`: Demonstrate WAL-backed page persistence/recovery including declared crash/corruption behavior. Accepted fixture: Persist a WAL-backed page, restart/replay after declared crash points and detect or recover corruption under the declared provider ordering policy. Negative fixture: Reject false durability success, missing checksum detection or replay that violates the declared state transition. Required evidence: page/WAL artifacts and deterministic crash/replay/corruption report; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries. Decision owner: Storage durability and recovery maintainer; person unassigned. Provider/target choice remains explicit.

Decision criterion `interface/domain/persist-storage-engine-state` (Open): Persist declared page, index and WAL state through the selected storage provider with binary/version/checksum, concurrency and actual ordering/fsync guarantees. Refuse: Reject incompatible layout/version/checksum or concurrency policy, out-of-order durability acknowledgement and unsupported provider fsync/durability claims. Required outputs: actual page/index/WAL write trace and provider-bound persistence-order/checksum fixtures. Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/recover-storage-engine-state` (Open): Recover declared durable page/index state after crash or corruption using the admitted WAL, checksum and schema/version policy and the selected provider ordering contract. Refuse: Report and reject invalid, corrupt, incompatible-version or unrepairable state rather than claiming recovery; replay must preserve the declared durable state transition. Required outputs: crash/corruption/version recovery trace, recovered state and invalid-state diagnostics. Dependencies consume schema, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

- [ ] `visual` Checked visual workflow lowering

Status: Open

Projected task block:
Outcome: Checked visual workflow lowering with source-bound artifacts and rejection behavior. Remaining tasks, all open: execute every explicit criterion below on its named target/provider; retain actual artifacts and failure diagnostics.

Criterion `domain/visual-review-flow-integration`: Execute declared support workflow with typed edges, source-linked diagnostics and explicit policy-required human-review transition. Accepted fixture: Execute classify/policy/human-review/reply nodes with typed edges, source provenance and recorded review/model/tool results. Negative fixture: Reject untyped edges, missing required review or diagnostics without node/edge mapping. Required evidence: visual/source/workflow artifacts, review graph and mapped replay/rejection trace; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume workflow, schema, ui outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `domain/visual-workflow-lowering`: Lower typed visual nodes/edges to ordinary checked workflow source/artifacts while exposing effects/review/retry/timeout/compensation/nondeterminism and round-tripping source diagnostics. Accepted fixture: Lower typed visual node/edge schemas to ordinary checked source/MIR/workflow artifacts exposing effects, grants, review, retry/timeout/compensation, nondeterminism and graph migration. Negative fixture: Reject hidden effects, visual-only authority, incompatible edges, skipped compiler checks and lost node/edge diagnostics. Required evidence: visual graph, generated-source/MIR map, schema/migration and policy/replay artifacts; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved source context](#wider-integration-task-proposals). Dependencies consume workflow, schema, ui outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Yes/no acceptance: do all declared positive and negative fixtures produce the required source-bound artifacts and stable diagnostics? Does evidence bind actual execution to its source/profile/target/provider? All must be yes. Integration: workflow, schema, ui. Decision owner: Checked visual workflow lowering maintainer; person unassigned. Provider/target choice remains explicit.

Decision criterion `interface/domain/check-visual-graph` (Open): Check typed node/edge schemas and expose all effects, grants, review, retry, timeout, compensation and nondeterminism before graph lowering. Refuse: Reject incompatible/untyped edges, hidden effects or review transitions and visual-only authority; no lowering or execution may consume a rejected graph. Required outputs: checked visual graph with policy facts, node/edge source mapping and graph-check diagnostics. Dependencies consume workflow, schema, ui as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

Decision criterion `interface/domain/map-visual-diagnostic` (Open): Map actual compiler/runtime diagnostics through the versioned graph-layout/generated-source provenance to the exact graph node or edge and back to source. Refuse: Reject stale graph/map versions, missing or ambiguous node/edge mappings and loss of source/generated origins; preserve diagnostic identity across the round trip. Required outputs: version-bound visual diagnostic map and compiler/runtime round-trip fixtures. Dependencies consume workflow, schema, ui as typed operation artifacts, not whole-component completion. Preserve every source facet and unresolved metadata ID in the exact phase-09 decision allocation; no implementation acceptance is inferred.

<!-- domains-wider-integration-proposals:end -->

## Historical workflow migration record

The complete original file follows unchanged as historical workflow evidence.
Its checkboxes and earlier source statements are separate from current
implementation authority and do not imply domain completion.

# Tasks: Domains workflow migration

Spec: [spec.md](spec.md) | Plan: [plan.md](plan.md) | Evidence: [evidence.md](evidence.md).
Migration owner role: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Declared increment

- [x] T001 [US1] Prepare the four owned artifacts with retained-contract links and exact canonical task anchors.
- [x] T002 [US1] Validate local targets/anchors, semantic names, ASCII, existing document/roadmap checks and negative controls; record results in evidence.md.
- [ ] T003 [US2] Independent review accepts the exact artifact tree, mapping and nonclaims; record disposition in evidence.md.
- [ ] T004 [US2] Integrate the reviewed change and record its merge receipt without claiming implementation completion.

## Dependencies and outcomes

T002 uses prepared artifacts; T003 uses validated exact content; T004 follows accepted review. These are documentation-migration tasks only. Implementation tasks remain at the links in plan.md, not copied into this ledger. Current implementation acceptance is unassessed by this package. Existing accepted finite increments retain their original scope and evidence.

## New-name review

All new paths use `domains-workflow` and ordinary artifact names. No planning-ID implementation name or compatibility identifier is changed.
