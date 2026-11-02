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

<!-- domains-schema-parent:start -->
Current proposed refinement of the existing `schema` task (Open). The four
criteria below resume the preserved parent criteria from
[the existing reconciliation](https://github.com/burn2delete/uniform/blob/55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6/specs/domains-workflow/tasks.md#implementation-tasks),
without changing their text. They are future implementation obligations, not
accepted behavior or a new task authority. Owner role: schema semantics
maintainer; no individual assignment is inferred.

This parent owns source schema normalization, validators, declared codecs and
canonical value/encoding semantics. Evolution and migration decisions belong
to `schema/compatibility-and-migration`; API/database/ABI/config/artifact
projection and actual consumer conjunction belong to
`schema/checked-consumer-projection`, both in the future-record region below.
The following original projected paragraph is retained as history after this
refinement; its broad generation wording does not create duplicate ownership.

For the S7 criterion, Schema supplies the declared ABI/binary schema and
schema-bound codec/binding input and checks byte vectors. Concrete target
layout derivation stays with
`lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout` in
[Lower's authority](../lower-workflow/tasks.md#implementation-tasks). Consume
its actual layout/ownership manifest rather than create a second layout
producer. STD10 callable codec use and Runtime's actual FFI call/callback
validation remain separate consumers. The preserved
`interface/schema/emit-explicit-binary-layout` decision allocates a producer
role to this parent; that exact cross-component allocation remains pending
current repartition review against Lower, not silently adopted or erased.
The four frozen criterion bodies below are preserved source context for that
review, not proof that the old producer allocation is current.

The existing decision-role references are `interface/schema/` followed by:
`normalized-source-schema`, `expand-authoritative-schema`,
`generate-source-validator`, `serialization-codecs`, `encode-declared-format`,
`decode-declared-format`, `canonical-bytes`, `encode-canonical-value`,
`decode-canonical-value`, `check-canonical-identity-input`,
`emit-explicit-binary-layout`, `emit-binary-codec-binding-input` and
`check-binary-reference-layout`. They remain Open in the preserved
source dispositions; these are role references, not aliases for whole tasks.

Source-specific acceptance for S1-S9 remains open. S1: expanded manifests and
static types preserve every declared schema family, recursive strategy,
refinement classification, version, hash, source span, compatibility mode,
validation boundary and derivation target. Shared validators cover API,
database, message, workflow, AI, config, decoded-data, FFI and artifact sinks;
schema validation alone cannot clear stronger sink taint. Semantic diffs cover
field removal/rename, nullability, enums/variants, refinements and binary layout;
missing compatibility or migration policy rejects.

S2: codecs declare representability, unknown/duplicate/missing-field,
numeric/string/byte/variant and conversion policies, retain schema hashes and
pass round trips. Reject schemaless or unbounded polymorphic decoding, implicit
lossy conversion and decoded values used before intended-boundary validation.
Recursion/length checks may enforce concrete encoding or target representation
legality; arbitrary input/work quotas confer no acceptance authority.

S3: byte-for-byte vectors cover every supported canonical type family and
schema/version identity, encoded-byte map/set ordering, absent versus null,
numeric special values, UTF-8/normalization, timestamps and metadata policy.
Host order, identity, timezone and layout cannot affect bytes. Reject values
outside the declared domain and noncanonical hash/signature inputs.

Criterion `schema/source-manifest-and-validator/decision-interface` (Open): Input: S1 source schema and resolved type/effect/capability/taint facts, declared refinement class, boundaries and derivation policy. Outputs: Expanded versioned schema manifest; Generated runtime validator; Recursive/refined schema fixtures and source/generated parity report. Accept: Expand records/maps/arrays/sets/tuples/enums/tagged unions/optional/refined/alias/reference/recursive/opaque schemas into an authoritative typed manifest, preserving id/version/hash/span/compatibility/boundary/derivation target. Generate the validator from that manifest; preserve compile-time/runtime/boundary/proof/advisory refinement classes and intended-schema versus stronger-sink trust distinctions. Run accepted and invalid payloads through API/database/message/workflow/AI/config/decoded/FFI/artifact boundary fixtures; documented narrowing projections remain explicit. Refuse: Malformed shape, unsupported/unclassified refinement or recursion without strategy; Missing identity/boundary/manifest metadata or generated constraints weaker than source; External values trusted before intended validator/sink validation or AI output bypass. Dependencies consume actual artifacts: Types resolved schema identities and constraints; Effects/Auth/Legality authority and taint facts; S2-S9 consumers receive the expanded schema and validator; no whole-component completion gate. Diagnostics: Applicable S1-SHAPE/REFINEMENT/BOUNDARY/PROJECTION/TAINT/RECURSION/MANIFEST with schema/version/span/boundary/target/field and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/declared-format-codecs/decision-interface` (Open): Input: S2 schema/version/format representability, field/unknown-field/numeric/string-byte/variant/conversion/trust/canonicalization policy and actual typed values or external bytes. Outputs: Serializer manifest and encoder/decoder products; Validation/trust/compatibility records; Round-trip and invalid-byte/value fixtures tied to schema hashes. Accept: Encode typed values under the declared policies and decode external bytes while checking required/unknown/duplicate fields, variants/refinements, precision, UTF-8/normalization and binary representation legality. Preserve numeric/string/timestamp/decimal/UUID/binary/variant conversions only under explicit compatible loss policy. Round-trip all claimed formats and retain untrusted/authenticated-unvalidated/schema-validated/sink-validated distinctions. Refuse: Schemaless or unsafe polymorphic decode without a finite declared type set; Implicit lossy conversion, duplicate/missing/incompatible fields, invalid variants or encoding; Trusting decoded data before intended validation, missing round-trip evidence, or noncanonical hash/signature use. Dependencies consume actual artifacts: S1 schema/validator and SAFE11 sink-taint facts; S3 canonical rules when canonical identity is claimed; S7 target representation when binary/ABI format is claimed. Diagnostics: Applicable S2 format/field/variant/numeric/string/polymorphic/taint/roundtrip/manifest diagnostics bind schema/format/path/span/policy/trust state. Removed work/input quotas are not reinstated by mixed recursion/length text. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/canonical-codecs-and-identity/decision-interface` (Open): Input: S1 schema hash, format version, canonical domain/policy, typed semantic value or encoded bytes; hash/sign/replay consumers supply their actual identity input. Outputs: Canonical encoder/decoder and format manifest; Byte-exact per-type reference vectors; Hash/sign/replay input admission and rejection records. Accept: Encode equal values identically under the same schema/version/policy, using encoded-byte map/set ordering, type/version tags and explicit absent/null, integer/decimal width, NaN/infinity/signed-zero, timestamp/timezone, UUID/symbol/keyword, union, UTF-8/normalization, byte and metadata rules. Decode against that same version/domain and reference vectors. Actual hash/sign/replay identity consumers require those canonical bytes and reject mismatched or unsupported encodings before use. Refuse: Host iteration/object identity/target layout/timezone/encoding changing canonical bytes; Ambiguous values, missing canonical rules, invalid UTF-8/metadata or vector mismatch; Hashes/signatures over noncanonical or wrong-schema/version bytes; a canonical flag alone is insufficient. Dependencies consume actual artifacts: S1 expanded schema and S2 representation policy; B13/package/workflow/AI/proof/release consume checked canonical bytes and input records. Diagnostics: S3-NONCANONICAL/ORDER/NUMERIC/STRING/METADATA/HASH/VECTOR/MANIFEST bind schema/value-path/rule/version/consumer context and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/binary-layout-codecs-and-vectors/decision-interface` (Open): Input: S1/S2 schema and declared target triple/calling convention, explicit field representation, compatibility, pointer ownership/lifetime and target ABI facts. Outputs: Binary/ABI and layout manifests; Schema-hash-bound codecs and typed FFI binding input; Ownership/lifetime map, cross-target report and byte reference fixtures. Accept: Produce field-order/width/alignment/padding/endian/signedness/optional/tag/pointer layouts including struct/union/enum/string/buffer/error/ownership-transfer ABI policy. Generate codecs/binding input from the accepted layout and its schema hash. Check each supported target against byte-exact reference vectors or a declared target-independent encoding, including buffers/handles/callback/bitfield/resource forms where claimed. Refuse: Implicit host layout/packing/pointer size or ABI/calling-convention mismatch; Uninitialized/nondeterministic stable bytes or pointers/handles missing ownership/lifetime policy; Wrong schema hash, unsupported representation, missing vectors or undeclared binary-breaking changes. Dependencies consume actual artifacts: Types/ownership facts and S1/S2 schema contracts; B2/B3/B4/B9/B12 and SAFE2/SAFE7/SAFE15 layout/proof contracts; R10 consumes binding input, while actual foreign invocation remains a separate runtime consumer. Diagnostics: Applicable S7 layout/endian/pointer/ABI/variant/fixture/compatibility/manifest diagnostics bind schema/field/target/rule/pointer-or-variant/fixture/proof and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

Historical projected paragraph (unchanged):
<!-- domains-schema-parent:end -->

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

<!-- domains-math-schema-proposals:start -->
## Math and Schema producer task proposals

These seven existing producer/checker records are explicit future tasks in
this same authority, resumed from reconciliation commit
`55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6`. IDs, Open states, owner
roles, semantic/error/target constraints and required evidence remain. The
current aggregate `math` and `schema` keys and seven-key SH region are
unchanged. Producer/checker refinement is not a second task denominator,
whole-parent completion prerequisite or accepted implementation.

Four exact inherited passages receive the current accounting-removal
disposition: `math/eml-candidate-search` records heuristic/tie inputs without
an allowance gate; `math/eml-search-manifest` retains semantic reproducibility
without fuel/depth/time admission; `math/interval-partition-production` retains
complete coverage and unresolved cells without stopping/fuel accounting;
`math/egraph-extraction` retains legal runtime ranking without project
optimization fuel/stopping allowances. All other criterion text is preserved,
except the explicit full-context reference rebinding used for the wider
Domains proposals. Numeric error bounds, exact domain/branch/rounding/target
conditions, runtime objectives and independent proof checks remain required.

Math criterion references below are existing source-family criterion IDs,
not extra checkbox tasks. Criteria still carried only by the preserved
aggregate Math/Schema blocks retain their exact original owner and pending
current composition. Declared typed/source/provider inputs permit isolated
producer or checker work; actual composition needs the corresponding real
artifacts and consumers, never completion of unrelated whole components.

The existing [Math20 applicability](https://github.com/burn2delete/uniform/blob/55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6/specs/requirement-reconciliation/math20-qualified-current-applicability.edn)
and [Schema25 applicability](https://github.com/burn2delete/uniform/blob/55fed8d91eb6c9b3fb5c50c8ca5dbdf7d67545f6/specs/requirement-reconciliation/schema25-qualified-current-applicability.edn)
retain their exact earlier source predicates, subjects and limits. They do
not independently accept these corrections, every attached source facet,
new current projection composition or actual execution. No held C12/provider
operation or new policy approval is enabled by these future records.

Schema criterion labels are not whole-operation aliases. The existing current
decision allocation retains these distinct finite roles (all Open), within the
same two future task owners:

| Producer/checker context | Existing decision IDs under `interface/schema/` | Existing future task |
| --- | --- | --- |
| Semantic compatibility diff | `compatibility-diff` | `schema/compatibility-and-migration` |
| Migration plan, authority and validation | `emit-typed-migration-plan`, `authorize-runtime-migration`, `validate-migration-change` | `schema/compatibility-and-migration` |
| Configuration reload | `configuration-reload` | `schema/compatibility-and-migration` |
| GraphQL generation | `emit-graphql-sdl`, `emit-graphql-resolver-adapter` | `schema/checked-consumer-projection` |
| OpenAPI generation | `emit-openapi-document`, `emit-http-boundary-validators` | `schema/checked-consumer-projection` |
| Executed HTTP contract tests | `run-http-contract-tests` | `schema/checked-consumer-projection` |
| Typed client generation and checking | `emit-typed-api-client`, `check-client-operation` | `schema/checked-consumer-projection` |
| Database structure and row adapter | `emit-database-structure`, `emit-typed-row-adapter` | `schema/checked-consumer-projection` |
| Typed configuration loading | `configuration-loading` | `schema/checked-consumer-projection` |
| Artifact validation and evidence gate | `artifact-validation`, `release-evidence-gating` | `schema/checked-consumer-projection` |
| Retained migration and binary projection contexts | `database-migration`, `binary-layout-and-bindings` | `schema/checked-consumer-projection` |

These references preserve the existing allocations; they do not discharge a
whole historical owner, source facet or task. The actual C6 projected-product
consumer conjunction still requires its full GraphQL/HTTP/migration/ABI/config/
artifact consumers. An HTTP test role alone cannot satisfy that conjunction.
Parent Schema normalization/codecs/layout criterion proposals, Lower's layout
production, STD10's callable codec consumption, Runtime FFI/reload/execution and
accountable Release decisions retain their separate required interfaces.

- [ ] `math/eml-normalization-and-search` EML normalization and search

Status: Open

Projected task block:
Outcome: EML normalization/search candidates and source/domain-bound transformation traces. Remaining tasks, all open: Normalize/search under explicit rules, conditions and deterministic recorded inputs; emit replayable traces and candidates.

Yes/no acceptance: Normalize/search under explicit rules, conditions and deterministic recorded inputs; emit replayable traces and candidates. Reject unproved equality, dropped side conditions and a searched candidate presented as a checked runtime implementation. Required outputs: EML normalization/search candidates and source/domain-bound transformation traces. Integration: consume actual interface artifacts from math, pass-evidence; these are not whole-component completion prerequisites. Decision owner: existing EML/search maintainer. Search emits candidates and traces, not accepted runtime implementations. Promotion consumes independent proof or certificate verdicts while preserving source/domain/branch/mode facts. No project fuel, node, depth, time or candidate-count allowance is an admission requirement. Implementation acceptance remains unassessed; sibling identity grants no completion credit.

Criterion `math/eml-candidate-promotion` (MATH4; Open).
Implement: Prevent lowering from using a candidate before accepted exact proof or accepted bounded-approximation evidence.
Boundary: Generated/typed/domain/branch/proof-requested/proved/bounded/rejected states; bounded candidates require the applicable MATH5 certificate, not just a state label.
Done when: Retain generated/typed/domain-checked/branch-checked/proof-requested/proved/bounded/rejected states; allow lowering only after the applicable independent proof or MATH5 certificate is accepted for the exact candidate.
Negative fixtures: Reject generated or merely labeled-proved candidates, stale evidence, missing bounded certificates and tree-identity equality.
Outputs: Candidate lifecycle decision with accepted exact-proof or bounded-certificate reference.
Dependencies: math/semantic-equality-proof-gate, math/efir-verification. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-CANDIDATE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-candidate-search` (MATH4; Open).
Implement: Return ranked search/normalization candidates with proof obligations and rejection reasons without making search output an accepted compiler rewrite.
Boundary: Same declared source/profile/target/provider/search inputs select the same accepted candidate or report a deterministic tie; recorded heuristic strategy and tie policy remain visible; no project allowance gates candidate admission.
Done when: Given identical source/profile/target/provider/manifest inputs emit replayable candidate ordering and explicit obligations; discovery grants no rewrite authority.
Negative fixtures: Reject hidden search choices, domain/branch-invalid candidates promoted as accepted and nondeterministic selection without an explicit tie.
Outputs: Ranked candidates, outstanding proof obligations, deterministic ties and rejection reasons.
Dependencies: math/eml-construction, math/eml-search-manifest. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-SEARCH is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-construction` (MATH3; Open).
Implement: Lower verified EFIR into the optional EML representation while preserving source/node correspondence and semantic facts.
Boundary: Exp-minus-log basis with arithmetic/variables/constants/domain guards/branch facts; EML is optional proof/search input and never a mandatory runtime representation.
Done when: Lower verified EFIR through exp-minus-log basis with constants/variables/arithmetic/guards, preserving EFIR identity, source map, domain/codomain, mode, precision, branch choices and explicit complex intermediates.
Negative fixtures: Reject unverified input, lost source/node correspondence, missing branch assumptions or making EML the required runtime representation.
Outputs: Optional EML expression/DAG, EFIR node correspondence and semantic context.
Dependencies: math/efir-verification. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-EFIR is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-guarded-normalization` (MATH4; Open).
Implement: Normalize EML through guarded registered transformations while preserving the final domain, branch, numeric mode and pending proof obligations.
Boundary: Basis introduction, domain/branch transport and complex simplification; complex intermediates require permission, recorded choices, valid final domain and applicable proof/error evidence.
Done when: Introduce the basis, transport domains/branches and simplify complex intermediates only with permission, recorded choices and applicable final-domain/equality/error evidence.
Negative fixtures: Reject log-exp identities outside their domain/periodic guards, forbidden complex intermediates or dropped numeric/precision/proof facts.
Outputs: Guarded normalized candidate with transported domain/branch facts and pending obligations.
Dependencies: math/eml-construction, math/symbolic-rule-promotion. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-BRANCH is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-search-manifest` (MATH4; Open).
Implement: Emit the declared search-space/objective/ranking/pruning inputs into the build artifact when search affects generated code.
Boundary: Grammar/domain/provider/target/source/profile inputs and tie policy; project fuel/depth/time allowances are superseded under current AGENTS.md; semantic input capture and deterministic replay remain required.
Done when: Record every semantic and heuristic input affecting generated code, including source/profile/target/provider identities and deterministic ranking/ties, so accepted candidate choice can be reproduced.
Negative fixtures: Reject omitted objective/pruning/ranking inputs, hidden provider/target choice or a changed manifest reused for the same recorded decision.
Outputs: Build-bound search grammar/domain/objective/ranking/pruning/provider/target manifest.
Dependencies: math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-SEARCH is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/eml-trace-production` (MATH4; Open).
Implement: Emit a normalization trace that identifies every applied rule, before/after form, premises, assumptions, origins, invalidations and proof obligation.
Boundary: Trace artifact-production contract; target/provider/heuristic choices that affect the run are recorded, not hidden in a process.
Done when: Emit each applied step and relevant heuristic/target/provider choice with exact EFIR/EML identity and generated origins, retaining pending obligations and branch/complex facts.
Negative fixtures: Reject missing rule versions, lost assumptions/origins, absent invalidations or a trace concealing an unproved replacement.
Outputs: Normalization trace with rule/premise/before-after/assumption/source/invalidation/proof entries.
Dependencies: math/eml-construction. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

- [ ] `math/certified-implementation-synthesis` Certified implementation synthesis

Status: Open

Projected task block:
Outcome: Generated approximation/implementation candidates with exact expression/domain/error/rounding/target certificate inputs. Remaining tasks, all open: Produce a candidate and complete certificate inputs, including required domain partition and backend assumptions.

Yes/no acceptance: Produce a candidate and complete certificate inputs, including required domain partition and backend assumptions. Reject candidate promotion from benchmark agreement, missing certificate subjects or unsupported target/numeric assumptions. Required outputs: Generated approximation/implementation candidates with exact expression/domain/error/rounding/target certificate inputs. Integration: consume actual interface artifacts from math, opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors; these are not whole-component completion prerequisites. Decision owner: existing approximation/synthesis maintainer. This task produces candidates, certificates and complete checker inputs. Independent checker verdicts are consumed from math/independent-proof-and-certificate-checking; producer success cannot certify itself. Implementation acceptance remains unassessed; sibling identity grants no completion credit.

Foundation requirement gravity/approximation-certificate-checkability: Produce and independently check an approximation certificate bound to the exact EFIR expression, domain, error measure/bound, rounding, branch policy, target and checker assumptions; preserve its checked result through the selected consumer. Refusals: Reject a changed domain, error bound, rounding mode, branch, target, checker or expression; a named checker and benchmark agreement alone cannot establish checkability. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts; lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs - Verified MIR/domain-operation disposition matrix and target/provider/preservation inputs.

Criterion `math/approximation-certificate-production` (MATH5; Open).
Implement: Produce a content-identified approximation certificate and independent-checker transcript/proof references bound to its exact EFIR implementation contract.
Boundary: Each supported approximation family/domain/mode/precision/target; approximation, coefficient, roundoff, reconstruction and target error ledgers remain acceptance facets.
Done when: For each declared polynomial, rational, piecewise, table, SIMD/GPU/hardware or fused family, parse the emitted certificate, recompute semantic identity, check domain coverage, separate approximation/coefficient/reduction/evaluation/reconstruction/roundoff/target error and combine within the precision contract.
Negative fixtures: Reject incomplete domain coverage, poles, branch/exception drift, relative-error-only claims near zero, unsupported target assumptions or a synthesizer attestation substituted for independent checking.
Outputs: Certificate, target-assumption manifest, exceptional-path coverage and independent checker transcript references.
Dependencies: math/efir-verification, math/interval-bound-ledger. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH5-CHECKER is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/correctly-rounded-synthesis` (MATH10; Open).
Implement: Synthesize and check an implementation meeting exact accepted-output intervals for the requested representation and rounding modes, or reject infeasibility without silent downgrade.
Boundary: Tie/subnormal/zero/exception cells, coefficient/evaluation/FMA/intermediate/reduction/reconstruction/lowering constraints, and multi-format/multi-rounding intersection or separate certified kernels.
Done when: For each declared input/output representation and rounding mode prove every generated result lies in the exact accepted-result interval, including ties/subnormals/signed zero/exceptions and coefficient, evaluation, FMA, intermediate, reduction, reconstruction and backend assumptions; use a proved intersection or separately certified kernels.
Negative fixtures: Reject unresolved rounding cells, unsupported modes, unchecked/infeasible constraints and silent downgrade to faithful/libm/approximate behavior.
Outputs: Rounding-target manifest, accepted-result interval ledger, synthesis transcript, kernel digests and independent certificate.
Dependencies: math/efir-verification, math/numeric-mode-resolution, math/interval-bound-ledger. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH10-SYNTHESIS is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/fused-kernel-generation` (MATH10; Open).
Implement: Fuse elementary operations only with whole-expression domain/branch/numeric and combined-error evidence.
Boundary: Independent per-call certificates do not prove fused intermediate/roundoff behavior.
Done when: Generate a kernel for the verified combined graph, preserving intermediate exceptional behavior, precision, source diagnostics and combined roundoff/reconstruction obligations.
Negative fixtures: Reject fusion justified only by separate per-call certificates, incompatible branches or unapproved FMA/reassociation.
Outputs: Fused implementation candidate and whole-expression error/branch/domain evidence.
Dependencies: math/efir-verification, math/floating-transform-legality. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH10-FUSION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/implementation-candidate-generation` (MATH10; Open).
Implement: Generate executable elementary implementation candidates tied to verified EFIR and the requested semantic/target contract.
Boundary: Declared libm/intrinsic/symbolic/polynomial/rational/table/fused/vector/GPU/generated families; generation alone grants no acceptance.
Done when: Generate declared library, intrinsic, symbolic, polynomial, rational, table, piecewise, fused, vector/GPU, generated or residual-checked candidates while keeping each unaccepted until its obligations pass.
Negative fixtures: Reject missing semantic anchors, target/mode mismatch, hidden allocation/effects or generation mislabeled as proof/selection.
Outputs: Executable candidate set with EFIR/source/target/semantic contract and required proof references.
Dependencies: math/efir-verification, math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH10-CANDIDATE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/legal-implementation-ranking` (MATH10; Open).
Implement: Rank only semantically eligible implementations under the declared cost/benchmark/autotuning policy and record choice, rejected candidates and fallback.
Boundary: Provider representation/mode/domain/branch/exception/target/version/certificate eligibility precedes performance ranking; benchmarks never prove semantics.
Done when: Filter by representation/mode/domain/branch/exceptions/target/provider/version/certificate/effect/allocation legality before benchmarking or autotuning; record rejected alternatives, deterministic ties and legal target-mismatch fallback.
Negative fixtures: Reject a faster but illegal provider, benchmark-only correctness, changed target fingerprints, unreplayable tuning or weaker fallback semantics.
Outputs: Semantic eligibility comparison, versioned runtime objective, replayable ranking/selection and fallback.
Dependencies: math/implementation-candidate-generation, math/floating-backend-eligibility. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH10-PROVIDER-COMPARE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

- [ ] `math/interval-and-real-proof-production` Interval and real proof production

Status: Open

Projected task block:
Outcome: Outward enclosures, domain partitions and proof/counterexample traces tied to exact inputs and assumptions. Remaining tasks, all open: Execute declared interval/real-proof operations and produce their enclosure/partition/trace artifacts.

Yes/no acceptance: Execute declared interval/real-proof operations and produce their enclosure/partition/trace artifacts. Reject inward or unsound rounding, uncovered domains, hidden assumptions and a claimed proof without its subject-bound trace. Required outputs: Outward enclosures, domain partitions and proof/counterexample traces tied to exact inputs and assumptions. Integration: consume actual interface artifacts from math; these are not whole-component completion prerequisites. Decision owner: existing interval/real-proof maintainer. Produce actual enclosure, partition and unresolved-cell/residual-check artifacts. Independent replay and proof import are supplied by math/independent-proof-and-certificate-checking. Implementation acceptance remains unassessed; sibling identity grants no completion credit.

Criterion `math/interval-bound-ledger` (MATH6; Open).
Implement: Produce separate real and roundoff bound ledgers whose entries expose their method, assumptions and invalidation conditions.
Boundary: Exact arithmetic/Taylor/minimax/Bernstein/monotonicity/Lipschitz/symbolic/external-prover/roundoff methods; changed consumed assumptions invalidate the entries.
Done when: Record exact interval, Taylor/minimax/Bernstein/monotonicity/Lipschitz/symbolic/external-proof or roundoff derivations and combine only under current consumed assumptions.
Negative fixtures: Reject conflated real/roundoff error, missing method/premises, insufficient bounds or reuse after branch/target/mode/coefficient/layout changes.
Outputs: Separate exact-real and floating-roundoff bound ledgers with method and invalidation conditions.
Dependencies: math/interval-claim-production, math/interval-domain-check. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-BOUND is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-claim-production` (MATH6; Open).
Implement: Produce a proof claim bound to exact source EFIR/EML, domain, branch, numeric mode, precision and requested property.
Boundary: Exact equivalence, bounded error, coverage, denominator, range-reduction or monotonic claims; a pending claim is not a proved result.
Done when: Emit equality, bounded-error, coverage, denominator, range-reduction or monotonicity claims with the actual requested property and source origins.
Negative fixtures: Reject omitted or mismatched input identities, a pending claim presented as proof, or a widened domain/property without corresponding evidence.
Outputs: Pending claim with exact EFIR/EML/input/domain/branch/mode/precision/property identity.
Dependencies: math/efir-verification, math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-CLAIM is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-domain-check` (MATH6; Open).
Implement: Parse and validate an interval domain with exact endpoints, boundary openness, exclusions and bound provenance.
Boundary: Closed-by-default versus explicit open/half-open intervals; reject empty domains except an explicit no-usable-domain claim.
Done when: Parse rational endpoints or equivalently exact checked enclosures with closed default/open/half-open policy, exclusions, symbolic predicates and complex projection provenance.
Negative fixtures: Reject reversed/empty domains except explicit no-usable-domain claims, inexact unproved endpoints, hidden exclusions or missing bound provenance.
Outputs: Validated exact interval-domain map and bound provenance.
Dependencies: declared typed inputs, fixture/oracle policy or exact source contract; no preceding Math producer required. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-DOMAIN is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-enclosure` (MATH1; Open).
Implement: Execute the required interval arithmetic operations with an inclusion-preserving result and applicable domain/branch/nonzero-denominator evidence.
Boundary: Required arithmetic, powers, root/exp/log, trig/hyperbolic, comparisons, polynomial/rational and piecewise operations; floating evaluation needs certified outward rounding.
Done when: Execute supported add/subtract/multiply/divide, integer/rational powers, sqrt/exp/log, trig/hyperbolic, abs/comparison, polynomial/rational and min/max/piecewise cases against exact or independent enclosure references.
Negative fixtures: Reject denominator cells containing zero without proof, invalid roots/branches or target-floating enclosure without certified outward rounding.
Outputs: Inclusion-preserving operation results and outward-rounding/nonzero/branch evidence.
Dependencies: math/interval-domain-check, math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-ROUNDING is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-partition-production` (MATH6; Open).
Implement: Produce a complete domain partition with cell order, split decisions, local assumptions, statuses and unresolved cells.
Boundary: Heuristic construction is permitted; project stopping/fuel allowances are superseded under current AGENTS.md. Preserve complete domain coverage and truthful unresolved-cell status.
Done when: Produce cells whose union covers the claimed domain with explicit boundary/exclusion policy, recorded heuristic split inputs and truthful unresolved regions.
Negative fixtures: Reject omitted cells, coverage gaps, incompatible boundary overlap or unresolved cells hidden as proved.
Outputs: Domain partition tree, ordered split decisions/local assumptions/statuses and unresolved-cell report.
Dependencies: math/interval-domain-check, math/interval-claim-production. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-PARTITION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-residual-check-emission` (MATH6; Open).
Implement: Emit the exact preserved runtime predicate for unresolved cells only when the numeric mode permits it, otherwise reject total-proof acceptance.
Boundary: Cell domains, placement, failure behavior, cost classification and SAFE15 preservation record; predicate execution belongs to the applicable runtime/check consumer.
Done when: Emit the exact residual predicate only in a mode permitting it, retaining domain, placement, observable failure and performance classification; runtime execution remains a consumer obligation.
Negative fixtures: Reject unresolved total-proof acceptance, omitted checks, predicate/domain drift or residual checks in a total compile-time-proof mode.
Outputs: Unresolved-cell runtime predicates, check placement/failure policy and SAFE15 preservation record.
Dependencies: math/interval-partition-replay, math/numeric-mode-resolution. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-UNRESOLVED is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

- [ ] `math/symbolic-rule-and-trace-production` Symbolic rule and trace production

Status: Open

Projected task block:
Outcome: Registered symbolic rules and domain-guarded rewrite traces/counterexamples. Remaining tasks, all open: Apply registered rules only under preserved domain/branch/assumption conditions and emit exact rewrite evidence.

Yes/no acceptance: Apply registered rules only under preserved domain/branch/assumption conditions and emit exact rewrite evidence. Reject unproved rules, wrong-domain rewrites and identity/tree-shape equality claims. Required outputs: Registered symbolic rules and domain-guarded rewrite traces/counterexamples. Integration: consume actual interface artifacts from math; these are not whole-component completion prerequisites. Decision owner: existing symbolic math maintainer. Produce registered guarded rules, e-graph explanations, rewrite traces and counterexamples; accepted rule use consumes independent replay or proof verdicts. Runtime ranking objectives are separate from removed project work accounting. Implementation acceptance remains unassessed; sibling identity grants no completion credit.

Criterion `math/egraph-equality-saturation` (MATH9; Open).
Implement: Build and merge e-graph equality classes only under checked equality premises and conservative compatible analysis joins.
Boundary: Unproved edges cannot justify code, proofs, side conditions or further equality; conflict requires rejection/restriction/candidate status.
Done when: Merge only checked compatible equalities; join facts valid for selected members across domain/codomain/branch/mode/floating/exception/provider effects/checks/proof/origins, keeping discovery-only edges distinct.
Negative fixtures: Reject or restrict conflicting analysis joins; reject an unproved edge used for code, proof, a side condition or further accepted equality.
Outputs: Guarded equality edges, conservative e-class analyses and explanation report.
Dependencies: math/symbolic-rule-promotion, math/symbolic-guarded-rewrite. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-PROOF is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/egraph-extraction` (MATH9; Open).
Implement: Extract a legal representative using the declared versioned runtime cost model, deterministic ties and replayable selection explanation.
Boundary: Hidden semantic constraints or unproved edges cannot enter extraction; project optimization fuel/stopping allowances are superseded under current AGENTS.md; actual runtime ranking and recorded choices remain explicit.
Done when: Select a legal representative with explicit runtime/proof/stability/allocation/check/provider/code-size objective and deterministic ties; reconstruct its equality path and relevant rejected choices without rerunning saturation.
Negative fixtures: Reject extraction through unproved edges, hidden semantic constraints in heuristic weights, changed cost-model identity, nondeterministic ties or an unreplayable explanation.
Outputs: Extracted representative, versioned runtime-cost model, tie policy and independent explanation replay.
Dependencies: math/egraph-equality-saturation, math/symbolic-independent-trace-replay. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-counterexample-production` (MATH9; Open).
Implement: Preserve a rejected rule and reproducible counterexample with the original failed assumptions.
Boundary: Later accepted narrower rules retain distinct provenance rather than overwriting the broad rejection.
Done when: Reproduce a failing broad rule such as sqrt(x*x)=x at negative x, bind inputs/rule version/domain/reason and preserve it when a separately justified narrower rule is introduced.
Negative fixtures: Reject overwritten rejection history, missing failed assumptions or a narrowed rule used to retroactively validate the original broad claim.
Outputs: Rejected rule/application and reproducible counterexample with original assumptions.
Dependencies: math/symbolic-rule-registration. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-COUNTEREXAMPLE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-guarded-rewrite` (MATH9; Open).
Implement: Match and apply a registered symbolic rewrite only when its typed semantic side conditions hold and output obligations remain explicit.
Boundary: Guarded pattern matching and independent domain/branch/mode premises; no tree-shape equality shortcut.
Done when: Match registered typed patterns, discharge domain/branch/nonzero/periodic/exception/mode/floating/provider side conditions and preserve effects/checks/source/generated origins.
Negative fixtures: Reject an undischarged condition promoted to accepted rewrite, tree-shape equality shortcut or erased runtime check/provider effect.
Outputs: Typed guarded rewrite result with explicit pending/discharged obligations and anchors.
Dependencies: math/symbolic-rule-promotion, math/semantic-equality-proof-gate. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-PROOF is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-rule-promotion` (MATH9; Open).
Implement: Keep candidate, proved, bounded, rejected and deprecated rules distinct and select only a rule whose required proof or approximation certificate is accepted.
Boundary: Deprecated rules remain readable but unselected; a narrowed later rule does not validate a rejected broad rule.
Done when: Distinguish declared/candidate/guarded/proved/bounded/rejected/deprecated, permitting code changes only for current proved or bounded evidence; retain deprecated readability and broad-rule rejection history.
Negative fixtures: Reject selection of a candidate, deprecated/rejected rule, missing MATH5 bounded certificate or stale evidence under changed domain/branch/mode.
Outputs: Rule-state transition with exact proof or bounded-approximation certificate.
Dependencies: math/symbolic-rule-registration, math/semantic-equality-proof-gate. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-PROOF is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-rule-registration` (MATH9; Open).
Implement: Validate and register a versioned semantic rewrite-rule artifact with pattern/replacement, domain/branch/mode guards, proof obligations and provenance.
Boundary: Standard/package/provider/generated rules; package import obeys trust policy and semantic content identity.
Done when: Validate pattern/replacement, typed variables, domain/codomain, branch/modes, side conditions, proof status/refs, invalidation rules and source provenance before registry insertion.
Negative fixtures: Reject incomplete rule shape, missing branch/mode guards, semantic content changes under an unchanged version identity or untrusted package import.
Outputs: Versioned content-addressed rule artifact and package trust/source provenance.
Dependencies: math/efir-content-identity. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-RULE-SHAPE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-trace-production` (MATH9; Open).
Implement: Emit a symbolic rewrite trace retaining before/after identities, rule/version, side conditions, evidence, source origins and invalidations.
Boundary: Proof/diagnostic back-edges survive rewrite execution and extraction.
Done when: Emit each actual rule application and relevant heuristic/target/provider/runtime-cost choice with exact graph/node/trace identities and proof or candidate status.
Negative fixtures: Reject lost diagnostic/source back-edges, hidden choices, missing proof/side-condition results or accepted status assigned to a discovery-only step.
Outputs: Rewrite trace with before/after/rule/version/premises/evidence/origins/preserved-invalidated facts.
Dependencies: math/symbolic-rule-registration. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

- [ ] `math/independent-proof-and-certificate-checking` Independent math proof and certificate checking

Status: Open

Projected task block:
Outcome: Independent subject-bound certificate, EML/symbolic/interval replay and proof-import verdicts. Remaining tasks, all open: Check the actual producer artifact, source/expression/domain/rounding/target/assumption/trust identity and emit a verdict consumed before promotion/elision.

Yes/no acceptance: Check the actual producer artifact, source/expression/domain/rounding/target/assumption/trust identity and emit a verdict consumed before promotion/elision. Reject changed subjects, missing domain coverage, stale or revoked proof/trust, forged traces and self-certified producer success. Required outputs: Independent subject-bound certificate, EML/symbolic/interval replay and proof-import verdicts. Integration: consume actual interface artifacts from pass-evidence, formal, opt/current-evidence-and-remaining-work/opt3-protect-checks-and-anchors; these are not whole-component completion prerequisites. Decision owner: existing independent math checker maintainer. Independently check MATH5 approximation certificates and MATH10 correctly-rounded inputs against exact EFIR/expression/domain/branch/exceptional/rounding/target subjects. Validate all applicable polynomial/rational/piecewise/table/fused/vector/device assumptions, separate error ledgers, denominator/index bounds, domain coverage, accepted-result intervals and invalidation/trust. Reject unsupported methods, changed subjects, stale or revoked evidence, missing exceptional paths, unsatisfied target assumptions and self-certified synthesis. Emit a deterministic verdict and replay transcript before selection or check erasure; the synthesizer remains a separate producer. Implementation acceptance remains unassessed; sibling identity grants no completion credit.

Criterion `math/eml-independent-trace-replay` (MATH4; Open).
Implement: Independently and deterministically check a recorded EML normalization trace.
Boundary: Replay of recorded rules/premises/choices; rerunning the heuristic search is not the checker.
Done when: Replay each recorded rule, premise, assumption, choice and before/after identity using the independent checker; reproduce preserved and invalidated facts without rerunning heuristic search.
Negative fixtures: Reject missing/reordered/substituted steps, changed rule/provider/target facts, unsupported assumptions or optimizer self-attestation.
Outputs: Independent deterministic trace-check report bound to exact input and rule versions.
Dependencies: math/eml-trace-production. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH4-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/interval-partition-replay` (MATH6; Open).
Implement: Replay the emitted partition deterministically and reject incomplete or unreplayable partitions.
Boundary: Partition proof artifact, not a second run of the adaptive search.
Done when: Replay emitted cell order, split points, local assumptions and statuses from the original claim, verifying complete coverage without rerunning adaptive search.
Negative fixtures: Reject a missing/substituted cell, changed split input/domain/claim or unreplayable ordering.
Outputs: Independent deterministic partition/coverage replay report.
Dependencies: math/interval-partition-production. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-PARTITION is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/real-proof-provider-import` (MATH6; Open).
Implement: Import a real-proof provider result only through an accepted checker validating claim/input identities, method, domain, branch, trust, replay and current assumptions.
Boundary: Opaque provider output may guide search but cannot certify transformations or erase checks.
Done when: Check exact claim/input hashes, domain/branch/method/trust/replay/proof-object/current assumptions and invalidation conditions independently of the provider or optimizer.
Negative fixtures: Reject opaque attestations used to certify, unsupported trust/method, stale inputs, branch mismatch or invalidated assumptions.
Outputs: Accepted-checker import report and SAFE15 proof reference.
Dependencies: math/interval-claim-production, math/interval-partition-replay. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH6-PROVIDER is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.

Criterion `math/symbolic-independent-trace-replay` (MATH9; Open).
Implement: Independently check the symbolic rewrite trace without trusting or rerunning the heuristic optimizer.
Boundary: Exact registered rules, premises, semantic identities and proof artifacts.
Done when: Reconstruct the transformation from exact input, registered rule versions, side-condition results and proof artifacts without trusting or rerunning the optimizer/saturation scheduler.
Negative fixtures: Reject missing/substituted/reordered steps, changed guards or versions, unproved edges and optimizer-generated pass status without checker replay.
Outputs: Independent rule/edge/premise/identity replay and proof-status report.
Dependencies: math/symbolic-trace-production. Types/Legality supplies checked numeric/profile/effect facts; Compiler supplies typed-core/MIR semantic/source anchors; Lower/Runtime/Stdlib supply declared target/provider interfaces only where consumed. Inputs are interface facts, not whole-component completion prerequisites.
Diagnostics: MATH9-TRACE is an example; require the applicable owning code and its context for each negative case.
Required future evidence: exact source/compiler/pass/profile/target/provider and input/output identities; parsed emitted artifacts; independent checker/oracle results where applicable; positive and negative fixture inputs with expected and actual owning diagnostics, failing node/source span and remediation; invalidation on changed consumed assumptions. Status Open; no execution or implementation acceptance is established by this criterion.


- [ ] `schema/compatibility-and-migration` Schema compatibility and migration

Status: Open

Projected task block:
Outcome: Old/new schema compatibility classification, migration plan/result and rejected incompatible or lossy transitions. Remaining tasks, all open: Compare exact schema versions and execute or reject the declared migration while preserving required data/taint and replay/consumer compatibility.

Yes/no acceptance: Compare exact schema versions and execute or reject the declared migration while preserving required data/taint and replay/consumer compatibility. Reject unsupported version drift, omitted migration, hidden data loss, stale consumers and unauthorized secret changes. Required outputs: Old/new schema compatibility classification, migration plan/result and rejected incompatible or lossy transitions. Integration: consume actual interface artifacts from schema, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution; these are not whole-component completion prerequisites. Decision owner: existing schema evolution maintainer. Own exact old/new compatibility and checked migration or reload transitions, including data-loss, authority, lifecycle, replay and consumer-version decisions. Projection and runtime services supply their actual interface artifacts. Implementation acceptance remains unassessed; sibling identity grants no completion credit.

Criterion `schema/configuration-reload/full-source-interface` (Open).
Implement a reload boundary for a declared typed-config schema and environment
using recorded deterministic source precedence, defaults/required/secret fields,
validation and redaction policy. Require an explicit compatibility, lifecycle
and observability policy for each reload. Exercise a compatible reload and
policy-rejected incompatible or missing-policy reloads, preserving config ID,
schema/source versions, lifecycle result and redacted observation records.
External replacement values stay tainted until validated. Reject malformed or
missing required values and env/file/secret/provider reads without declared
effects/grants. Reject ambient config reads and secret leakage in logs,
manifests, source maps, prompts, traces or diagnostics; only policy-allowed
redacted markers may appear. Capture build-time reads as reproducibility inputs
or reject them in hermetic builds. Closing evidence is the versioned reload
policy, typed manifest/source-precedence/secret records, accepted/rejected
reload fixtures and redaction report. Emit applicable S8 diagnostics including
S8-RELOAD with config/field/source/span-or-edge, effect/grant, secret/redaction
state and remediation. Dependencies consume S1/S2 schema/decoding and Runtime
lifecycle, SAFE10/SAFE11/R11 grants/taint and R12 observations; loader generation
alone does not prove an actual reload.

Criterion `schema/semantic-compatibility-diff/decision-interface` (Open): Input: Exact old/new schema identities, expanded source and generated projection contracts, and additive/migration-required/binary-stable/reviewed-breaking/append-only/exact compatibility policy. Outputs: Semantic diff and compatibility report; Required migration/review decisions; Accepted and rejected evolution fixtures. Accept: Classify added/removed/renamed fields, changed nullability/enums/variants/refinements/binary layouts and semantic hazards. Include GraphQL resolver policy, HTTP route/error/auth, database dialect/migration, ABI and artifact-schema changes; preserve public schema-library consumer context when those adapters are used. Emit the required compatibility/migration decision for each changed boundary instead of accepting a textual-only diff. Refuse: Changed schema/projection without declared policy or required migration/review evidence; Hidden API/client/AI or ABI compatibility breaks and weakened constraints; Wrong from/to identity, dropped negative condition or schema helper availability substituted for a checked diff. Dependencies consume actual artifacts: S1 old/new schemas and S4/S5/S6/S7/S9 projection/consumer records; STD10 schema APIs and Build/Query/Workflow/AI/Runtime consume the applicable checked diff, not a new whole-component completion condition. Diagnostics: Applicable S1-COMPATIBILITY, S4-DIFF, S5-DIFF, S6-MIGRATION/DATA-LOSS, S7-COMPATIBILITY and S9-COMPATIBILITY retain changed path, boundary/policy and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

Criterion `schema/migration-plan-admission-and-validation/decision-interface` (Open): Input: Checked S1/S6 old/new schema diff, declared dialect, transaction/backfill/rollback-or-forward-only/deployment ordering, data-loss policy and any required review record; runtime supplies actual database write/admin grants. Outputs: Typed SQL/provider migration and plan/identity manifest; Data-loss/review/rollback-or-forward-only and API-client compatibility records; Actual admission/denial and fixture-database or equivalent plan-validation results. Accept: Generate a typed migration with exact from/to identities and all declared policy fields; include shared GraphQL/OpenAPI/client/AI compatibility. At the actual execution boundary admit only with applicable database write/admin grant and required destructive-change policy/review/evidence. Validate generated additive/breaking/destructive changes using fixture databases or equivalent checked plans, binding exact subject, dialect and policy in results. Refuse: String-only migration without schema diff or wrong from/to/dialect; Destructive change lacking explicit data-loss decision or policy-required review/evidence; Missing write/admin grant, missing rollback/forward-only policy, hidden API break or missing/mismatched validation fixture. Dependencies consume actual artifacts: S1 schema/diff and B11 relational artifacts; SAFE10/SAFE11/P9/R7 authority, taint, deployment and replay records; runtime actual execution/admission is not established by plan generation. Diagnostics: Applicable S6-MIGRATION/DATA-LOSS/ROLLBACK/CAPABILITY/FIXTURE/MANIFEST bind schema/path/dialect/migration/effect/grant/data-loss and remediation. These are required future products/results; no execution or implementation acceptance is claimed.

- [ ] `schema/checked-consumer-projection` Checked schema consumer projection

Status: Open

Projected task block:
Outcome: Generated API/client/ABI/config/artifact consumers with source-schema identity and accepted/rejected parity evidence. Remaining tasks, all open: Generate each declared consumer from the verified schema and exercise the same valid/invalid/refinement cases against source and consumer.

Yes/no acceptance: Generate each declared consumer from the verified schema and exercise the same valid/invalid/refinement cases against source and consumer. Reject weakened generated constraints, incompatible ABI/layout, missing source schema/version and generated code that bypasses ordinary checks. Required outputs: Generated API/client/ABI/config/artifact consumers with source-schema identity and accepted/rejected parity evidence. Integration: consume actual interface artifacts from schema, lower/current-evidence-and-remaining-work/lower2-derive-abi-and-layout, emit/current-evidence-and-remaining-work/emit2-emit-real-outputs; these are not whole-component completion prerequisites. Decision owner: existing schema projection/codegen maintainer. Own generated mapping, adapters and source/consumer parity. Migration statements in these contracts consume checked schema/compatibility-and-migration artifacts; declaring a shared source schema does not implement migration admission or execution. Implementation acceptance remains unassessed; sibling identity grants no completion credit.

S4/S5: generated GraphQL SDL/resolvers and OpenAPI routes, errors, validators
and contract tests preserve source constraints, nullability, auth/grants,
effects, provenance and compatibility. Route method/path, parameter/header/body
schemas, response/status/error models, versioning and required idempotency
policy remain explicit. Unsupported mappings require explicit
validated adapters or rejection. Typed clients retain schema/operation hashes
and operation validation. Reject hidden resolver/handler effects, weakened
types, unvalidated HTTP input and undeclared breaking changes.

S6: declared-dialect tables/views, constraints, indexes and typed row adapters
preserve source schemas. Typed migration plans record from/to identities,
transaction/backfill policy, rollback or forward-only behavior and deployment
order. Additive, breaking and destructive fixtures require data-loss decisions,
any policy-required review and evidence; execution requires write/admin grants.
Check shared API/client/AI compatibility and fixture databases or equivalent
plan validation. Reject lossy adapters, hidden dialect dependence in portable
claims, unauthorized migration and string-only migration without schema diffs.

S7: schema-hash-bound binary codecs and FFI inputs declare field widths/order,
endian, alignment/padding, discriminants, pointer ownership/lifetime, target and
calling convention. Cross-target byte vectors reject implicit host layouts,
uninitialized padding and unstable fields in stable formats. Runtime wrapper
execution remains a Runtime consumer, separate from layout production.

S8: config loaders record deterministic source precedence, defaults/required
fields, environment identity, secret access/rotation/audit and redaction policy.
Reads require declared effects and grants; external inputs stay tainted until
validated. Hermetic builds capture config inputs or reject the reads. Reload
fixtures require compatibility, lifecycle and observability policy. Reject
ambient reads, missing validation and secrets in logs, manifests, source maps,
prompts, traces or diagnostics except policy-allowed redacted markers.

S9: validators cover each declared artifact kind and the common versioned
source/compiler/dependency/pass provenance, profile/target/backend/runtime,
effects/grants, content identity, safety/proof/diagnostic/conformance envelope.
Preserve relevant unsafe-island, AI/tool, generated-code and redaction records;
require canonical hash/signing/replay/release inputs, bootstrap provenance for
graph cycles, and compatibility/migration records for version changes. Missing
required release evidence rejects at the schema gate; Release owns the actual
release decision. Each S1-S9 fixture records its stable diagnostic family,
schema/artifact identity, failing path or edge, applicable source/target/boundary
and remediation. These clauses specify future evidence, not passing results.

Foundation requirement gravity/source-schema-projection: Generate a validator, JSON Schema and typed artifact schema from the same source schema/version; run accepted and invalid payloads through every generated consumer and compare field/refinement/compatibility and diagnostic-path behavior. Refusals: Reject a weakened generated constraint, changed schema identity, malformed payload accepted by one consumer or unknown projection version; a generated file name alone is not consumer parity. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pipeline - Versioned stage contracts and actual compiler stage/fact/origin products.

Criterion `schema/graphql-generation/full-source-interface` (Open).
Implement a source-schema-to-GraphQL producer and its resolver boundary using
declared schemas, resolver IDs, query/mutation/subscription effects, grants,
auth/error/naming/nullability/pagination policy and compatibility mode. Generate
SDL, resolver adapters, typed clients, query validation, auth metadata, source
maps, a generation manifest and schema diff from the same source identity.
Cover objects/interfaces, Option/Result, enums/tagged unions, refinements,
lists/IDs/scalars and deprecated fields without weakening constraints;
unsupported mappings require explicit validated adapters or rejection. Compare
generated null/error/constraint behavior with source validators. Execute resolver
fixtures with allowed grants and reject hidden IO/model/tool effects or missing
grants. Reject missing auth/origin metadata, client schema hashes or operation
validation, and undeclared breaking/nullability/enum/field/resolver-policy
changes. Record S4-MAPPING/NULLABILITY/RESOLVER/AUTH/DIFF/CLIENT/SOURCEMAP/MANIFEST
diagnostics with schema, type/field, resolver, span, effect/grant, compatibility
and remediation. Closing evidence is the generated bundle, source/generated
parity and resolver grant results, valid/invalid operation fixtures and diff
report tied to exact source/compiler/policy hashes. Dependencies consume S1
expanded schemas/validators, S2/S3 identities, Types facts and Runtime grant
enforcement outputs; generated adapters alone do not prove resolver invocation.

Criterion `schema/database-mapping/full-source-interface` (Open).
Implement declared-dialect schema/relational-IR mapping into tables, views,
column types, constraints, indexes, primary/foreign/composite keys, nullability,
enum/variant representation, prepared bindings and typed row adapters. Exercise
scalars, decimals/timestamps, optional fields, generated columns, references,
JSON/arrays/views/computed fields with recorded dialect behavior. Compare each
generated row validator to its source schema; external rows remain tainted
until intended validation. Reject lossy mappings/adapters, unsupported dialect
features and hidden dialect dependence when portability is claimed. When this
mapping supplies migration inputs, preserve schema diffs and from/to identity,
transaction/backfill, data-loss/review, rollback-or-forward-only and deployment
ordering; preserve API/client/AI-output compatibility. Require fixture databases
or equivalent plan checks for generated changes, and write/admin grants for
actual migration execution. Reject string-only migrations without schema diffs,
destructive changes lacking required policy/review/evidence and unauthorized
migrations. Closing evidence is the mapping manifest, generated structures,
typed adapters, query-plan metadata, dialect/row-parity fixture results and
applicable migration/compatibility records. Diagnostics use the applicable S6
family with schema/field/table/column, dialect, migration ID, effect/grant,
data-loss state and remediation. Dependencies consume S1 schemas, B11 relational
IR and SAFE10/SAFE11/P9/R7 authority, taint and replay records; consuming a
migration plan is separate from evidence that it executed.

Criterion `schema/artifact-validation/full-source-interface` (Open).
Implement versioned artifact-envelope and graph validators for each declared
Gravity artifact kind, including source-derived schemas, IR/backend outputs,
runtime/package manifests, conformance/proof objects, workflows/agents, APIs,
migrations, HDL/mobile outputs, docs and governance/release records. Preserve
common kind/schema-version, source graph/compiler/dependency/pass provenance,
profile/target/backend/runtime, effects/grants, content/source hashes and
safety/proof/certificate/diagnostic/conformance fields plus kind-specific fields.
Preserve relevant unsafe-island, model/tool policy, generated-code provenance,
runtime service and secret-redaction records. Require canonical encoding for
hash/signing/replay/release uses, bootstrap/self-hosting provenance for graph
cycles, and compatibility/migration records for schema version changes.
Closing evidence is the artifact/graph/content/provenance/evidence schemas,
validator outputs, canonical vectors, compatibility report and per-kind valid
and invalid manifest fixtures. Reject unsupported schemas, missing fields or
evidence, invalid hashes/provenance, noncanonical identity inputs, unexplained
cycles and hidden authority/unsafe decisions with S9 diagnostics naming the
artifact/kind/version, field or graph edge, release-gate context and remediation.
Dependencies consume S1 schemas/validators, S3 canonical bytes and B13 emitted
artifact records; valid schema shape is separate from checked evidence truth.

Criterion `schema/release-evidence-gating/full-source-interface` (Open).
Implement the artifact release-evidence admission interface: derive applicable
requirements from the versioned release policy, bind the artifact graph and
source/compiler/spec/dependency identities, and verify every required provenance,
safety/proof/effect/grant/test/conformance record before emitting a gate result.
Require canonical identity input and explicit bootstrap provenance for cycles;
schema changes retain compatibility/migration records. For formal claims used
by a gate, require machine-checkable evidence naming claim, assumptions/trusted
basis, checker/version and exact input hashes; a proof reference or well-shaped
manifest alone cannot admit a claim. Keep checked proof evidence and executable
test results distinct, retaining checker reports, counterexamples and invalidation
records when applicable. Closing evidence is the release-gate schema, applicable
policy matrix, exact evidence graph, checker/validator results and admitted and
rejected gate fixtures. Reject missing evidence/provenance, noncanonical or
forged hashes, stale source/compiler/spec/dependency bindings, missing assumptions,
unknown checker identity and prose-only/uncheckable proof claims; report the
applicable S9-EVIDENCE/PROVENANCE/HASH/CANONICAL/CYCLE/COMPATIBILITY or TEST11003
condition with artifact/claim, failing edge, policy gate and remediation.
Dependencies consume S9 validation, S3 canonical identity, D9/SAFE15/TEST11
checker results and Release policy inputs. Release owns publication decisions;
its decision consumer must reject absent, failed or identity-mismatched gate
results, with explicit consumer-refusal fixtures in the closing evidence.
This criterion produces and consumes a checked gate result and confers no
publication authority.

Criterion `schema/api-client-generation`: Generate typed API clients tied to schema/operation hashes and validate operations against the declared server contract. Accepted fixture: Generate typed clients bound to operation/schema hashes and validate request/response/error behavior against the declared server contract. Negative fixture: Reject stale schema/version/hash, missing validators, invalid runtime data and silent schema drift. Required evidence: API client/declaration/validator artifacts and client/server parity fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved Domains source context](#wider-integration-task-proposals). Dependencies consume types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit, emit/current-evidence-and-remaining-work/emit2-emit-real-outputs outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `schema/configuration-loading`: Load typed configuration using deterministic recorded precedence, required/default/secret fields and validated provider inputs. Accepted fixture: Load required/default/secret typed fields with deterministic recorded precedence and validated declared provider inputs. Negative fixture: Reject missing required values, invalid source types, undeclared ambient precedence and secret exposure. Required evidence: configuration schema/loader/precedence manifest and redacted load/error fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved Domains source context](#wider-integration-task-proposals). Dependencies consume types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit, emit/current-evidence-and-remaining-work/emit2-emit-real-outputs outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `schema/openapi-generation`: Generate source-faithful OpenAPI route/schema/error/auth documents and request/response validators with handler contract tests. Accepted fixture: Generate source-faithful route/schema/request/response/error/auth OpenAPI artifacts with runtime validators and handler parity tests. Negative fixture: Reject incompatible operation/schema identity, weakened validation, invalid requests/responses and drift from actual handler contracts. Required evidence: OpenAPI/schema/validator artifacts and handler contract fixtures; bind source/compiler/profile/target/provider identities, source-mapped stable diagnostics and every applicable source facet field/exception/negative condition listed for this criterion in [the full preserved Domains source context](#wider-integration-task-proposals). Dependencies consume types/current-evidence-and-remaining-work/types3-make-dynamic-boundaries-explicit, emit/current-evidence-and-remaining-work/emit2-emit-real-outputs outputs under their stated contracts; no whole-component completion prerequisite. Status: Open; implementation acceptance unassessed.

Criterion `schema/executed-http-contract-parity/decision-interface` (Open): Input: Pinned generated OpenAPI/request/response/error schemas, schema/operation/route identities and actual handler/provider/version with declared effects/grants/auth/idempotency policy. Outputs: Executed handler contract-test report; Accepted/invalid request/response/error fixtures and stable source-mapped diagnostics. Accept: Execute generated contract tests against the actual declared handlers and compare request/response/error/status/nullability/constraint/auth behavior to the authoritative route/schema contract; bind handler/provider, schema/operation and source/compiler identities in results. Retain raw external-input taint until intended validation. Refuse: Invalid request/response/error admitted, drifted operation/schema/handler identity or weakened source constraint; Undeclared effect/grant/auth/idempotency requirement and unvalidated HTTP input; Generated test source or OpenAPI document substituted for actual execution results. Dependencies consume actual artifacts: S1-S3 schema/identity and generated S5 document/validators/client contracts; DOM8/B11/R11/R12 actual handler, authority and observation interfaces. Diagnostics: Applicable S5 route/schema/taint/capability/idempotency/diff/client/source-map diagnostics retain route/method/path/span/schema/effect/grant/compatibility and remediation. These are required future products/results; no execution or implementation acceptance is claimed. C6 projected-product consumer criterion (Open): Bind each S4 GraphQL and S5 OpenAPI artifact to an actual typed request/resolver/handler input, schema/version and authority context, and inspect its validated response/error or constraint/nullability/source-schema/authority refusal; generation alone is insufficient. For S6, bind the actual admitted migration plan and prior database/schema state to provider transaction execution, inspected post-state and rollback/data-loss/compatibility refusal under the declared migration policy. For S7, preserve layout production in Lower, actual encoder/decoder consumption in the STD10 public API, and actual ABI/ownership/value checking at a Runtime FFI call/callback as distinct interfaces. For S8, feed actual authorized typed configuration into its lifecycle-compatible reload operation or explicit source-owned refusal. For S9, the actual artifact/trust/release consumer validates the exact versioned subject and required schema/provenance evidence or refuses stale, wrong-subject or untrusted evidence. Pure generation does not grant consumer effects. Preserve every source-declared profile, target, compatibility, nullability, taint and capability condition without treating fields or table headers as operations.

<!-- domains-math-schema-proposals:end -->

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
