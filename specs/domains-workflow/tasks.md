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

SAFE9 acceptance: execute checked/panic/wrapping/saturating/arbitrary-precision/proof-required integer modes, exceptional divisor/shift/minimum-negation cases and explicit float conversion behavior; reject implicit narrowing, hidden target defaults and unapproved relaxed math. Inspect strict/relaxed floating contracts including NaN, infinity, signed zero, rounding and fusion; elementary providers expose domain/range/accuracy/target evidence. Invalidated numeric proofs restore checks or reprove; target instructions preserve mode or are guarded/rejected. Explicit wrapping/saturating narrowing retains the source conflict with checked/proven narrowing until adjudicated. P2/P12 profile interface: check portable/core and exact/symbolic/interval/bitvector/rounded/certified mode eligibility and explicit conversions/assumptions; reject hidden target-default or provider-dependent core math. Formal symbolic lowering emits symbolic IR, theorem/assumption/trusted-kernel records and checked conclusions; imported proof validation and actual arithmetic remain distinct interfaces. Dependencies consume Types/Legality facts and declared proof-provider outputs, not completion of another component.

Foundation requirement gravity/approximation-certificate-checkability: Produce and independently check an approximation certificate bound to the exact EFIR expression, domain, error measure/bound, rounding, branch policy, target and checker assumptions; preserve its checked result through the selected consumer. Refusals: Reject a changed domain, error bound, rounding mode, branch, target, checker or expression; a named checker and benchmark agreement alone cannot establish checkability. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts; lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs - Verified MIR/domain-operation disposition matrix and target/provider/preservation inputs.

Foundation requirement gravity/elementary-math-semantic-carrier: Lower analyzable elementary expressions into EFIR with domain/numeric-mode/source anchors, use EML only for declared proof/normalization/search work, and retain checked evidence for any semantic rewrite. Refusals: Reject declaring expressions equal solely because their EML trees or spellings match, dropping EFIR analysis facts or replacing strict semantics with an unproved approximate provider. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): pass-evidence - Actual subject-bound checker/proof/certificate/audit/replay/test evidence and verdicts; pipeline - Versioned stage contracts and actual compiler stage/fact/origin products.

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

DOM9 acceptance also requires declared CRDT identity/order/merge/update and
serialization laws, causal history, offline authorization, conflict visibility,
tombstone/compaction and migration policy. Classify coordination as free,
required or deferred; reject nonmonotonic rules without an explicit mechanism.
The checkout fixture must not double-charge on replay. The local-first cart
fixture must converge after partition/heal, duplicate/reordered delivery,
restart and migration, and reject invalid merge laws or hidden intent loss.
Dependencies: Schema state/event versions, Runtime durable storage/replay and
Auth grants. These criteria add no acceptance credit or provider selection. P9 profile interface: emit typed step input/output/effect/grant/retry/timeout/cancellation/idempotency/compensation and event/replay records; validate versioned messages and persisted state with migration/taint/retention/order/delivery policy. Execute recovery separately from admitted step construction; replay must consume recorded nondeterminism without repeating external actions. Reject schema drift without migration, untyped external failure, unrecorded replay inputs and missing required compensation. Service-boundary admission consumes Schema/Auth and Runtime facts, not their whole-task completion.

- [ ] `ai` AI

Status: Open

Projected task block:
Outcome: the declared AI workflow executes through schema, capability, taint
and human-review boundaries. Remaining tasks, all open: implement declaration
lowering; connect model/tool/memory providers with explicit policies; implement
structured-output and taint validation; connect human review and replay;
execute evaluations plus prompt-injection, missing-review, cross-tenant and
unvalidated-output rejection fixtures.

Source acceptance for A1-A11 remains open. A1: lower all declared AI forms
through syntax, typed core and legal profiles into linked manifests, preserving
source/macro origins, every AI effect, explicit replay modes and runtime audit
records. Reject ambient credentials, hidden adapter effects, missing model or
schema identities, generated source bypassing the normal compiler pipeline,
and privileged deployments without review policy or required evaluation evidence.

A2: normalize provider/model declarations, including adapter, endpoint, modes,
version/tokenizer, schema enforcement, retention and credential requirements.
Require a provider handle in addition to the effect; network access alone is
insufficient. Check embedding dimensions, CI live-call allowance, fallback
mode/schema/retention/evaluation compatibility and floating-identity upgrades.
Record reproducibility parameters, response/refusal/validation state, typed
failure classes, drift and retry/fallback decisions. Pinned audit replay rejects
identity mismatch. Secret/no-store bodies stay out of prompts, memory, datasets
and ledgers; retain permitted hashes, validation facts and observed cost/latency.
Preserve real external spending/account preflight before issuing paid calls.

A3: emit typed prompt schemas, tool visibility, authority/taint maps, render
hashes and compatibility policy. Validate format/schema, canonicalization,
per-field taint and intended use before typed output admission. Reject arbitrary
model-selected schemas, illegal partial outputs, undeclared tools and breaking
output changes mislabeled compatible. Refusals follow a typed branch; repair
policy and attempts are recorded. Secret-to-provider flow requires explicit
policy; no-store responses retain only permitted hashes and facts.
The inherited prompt-authority bridge retains approved sanitizer OR approved
delimiter policy without promoting data to instruction authority or clearing
source/taint, grant, secret or review obligations.

A4-A5: emit tool and agent manifests with complete dependencies and separate
memory read/write modes. Tool declaration, agent toolset, package/deployment
grant, scoped runtime handle and policy must all agree. Plans and model text
grant no authority. Validate tool and agent results, expose implementation
effects, normalize foreign errors, redact logs and model-facing descriptions,
and record agent identity on every invocation event. Privileged tools require
review policy; non-idempotent retries/replay require declared idempotency or
compensation. Generated agent source re-enters compiler validation.

A6: consume the Workflow task's typed graph, state/event-log schemas, durable
transitions and replay records. Preserve node effects, capabilities, idempotency,
review payload binding, retry/compensation and lowering metadata. Validate
branch inputs and typed/effect-checked compensation. Record or forbid model,
tool, memory, random, clock and external IO nondeterminism; reject unsafe repeated
side effects, changed review payloads and incompatible graph/state/log evolution
without migration. Replay fixtures reuse the actual recorded decisions.

A7: emit memory item/metadata schemas, provenance, partitions, retention,
redaction, indexing, embedding and replay policies. Enforce separate scoped
read/write and embedding capabilities. Reject invalid writes, unauthorized
protected-data embedding/persistence, implicit cross-tenant sharing, retrieved
instruction authority, incompatible index reuse and fresh retrieval where a
recorded result is required. Record query/result ids, scores, embedding identity
and old/new embedding lineage. Deletion/redaction must remove stale retrievable
entries and update indexes and ledgers.

A8: compile explicit production policy and deterministic layered decisions with
deny precedence. Later policies cannot silently broaden authority. Record action
identity, effects/capabilities, data/taint, review, eval and replay facts and the
resulting rule/decision. Reject cyclic policy, unsafe fallback, unvalidated output
or generated code and protected-data logging. Authority-broadening changes need
compatibility/security review; emergency overrides are explicit, scoped,
time-bound and auditable, with no new capability implied.

A9: produce versioned dataset/schema/provenance/split artifacts and eval reports
bound to every behavior-changing subject/model/prompt/tool/memory/policy/runtime
identity. Record changed artifacts, metric direction/threshold/aggregation and
confidence/sample or variance rules, seeds/traces, redacted samples or hashes,
probe results and release decisions. Required safety probes cover injection,
tool misuse, denial, schema failure, refusal and protected-data leakage. Reject
absent/stale/incompatible release evidence, name-similarity fallback, missing
probes or thresholds and unauthorized live-provider evaluation; preserve real
credentials, provider spend/account policy and report-signing requirements.

A10: compile typed review declarations with action, actor/role, displayed evidence
and evidence hash, payload hash, timestamp, expiry, revocation, denial/timeout,
replay and emergency rules. Block required actions until matching valid review;
reject absent records, wrong roles, changed payloads, expiry or revocation.
Display enough typed evidence for the decision, follow denial as a workflow
outcome, and reuse recorded decisions only under the declared replay policy.
Audit requests and lifecycle transitions with redaction and permitted hashes.

A11: keep user/web/file/package/retrieved/tool/model content in its declared
data authority, preserving taint back into model context. Data cannot grant
tools or change policy. Reject instruction promotion, unauthorized secret flow,
unvalidated tool output and generated source, and privileged releases without
defense probes. Monitor escalation, override and exposure attempts; incident
bundles reconstruct prompt, taint, tool, memory, policy and workflow decisions
without exposing protected raw content. Each source's diagnostic identifiers
and contextual fields and its positive/negative conformance fixtures are part
of acceptance, not separate implementation tasks.

The exact mixed A1/A3/A5/A6 call-count, repair-attempt and retry-accounting
clauses remain unresolved in the bounded family proposal; they cannot receive
acceptance by linking this task. Remove synthetic project/compiler work quotas
under current accounting policy while preserving real external billing/account
limits, semantic representability and actual host restrictions. Do not silently
supersede a mixed source clause or invent a replacement work allowance.

Yes/no acceptance: does untrusted output
remain data? Are actions denied without grants/review? Are evaluations bound to
exact prompt/model/tool identities? Integration: Schema/Auth, Workflow/Runtime,
Stdlib AI. Decision owner: AI maintainer; recommendation: provider-independent
artifacts and explicit model identity, never a vendor SDK side path.

SAFE13 acceptance: inspect model/provider/version, input/output schemas, nondeterminism, real external cost, retention, safety and replay records. Preserve structured prompt roles and provenance; reject untrusted grant/policy changes, privileged concatenation without approved sanitization/delimiting, and unvalidated tool arguments. Distinguish read-only/write/destructive/external/privileged tools; explicit grants and required human/policy decisions bind action/diff, reviewer, time and result before invocation. Generated code carries model/prompt origins and passes ordinary compiler/package checks before execution. Memory remains tainted absent trust metadata and enforces retention/redaction/privacy/deletion and secret policy. Check exact/audit replay and idempotent side effects through Runtime; prompt comments are not review decisions.

P10 profile interface: keep model admission/execution, tool schema/grant/review admission, generated-program compiler validation, prompt-role/taint checks, memory retention/deletion/privacy and secret-sink gates distinct. Emit prompt/model/provider/tool/policy/review/input/output/error/retry/generated-code digests and replay records. An evaluation hook emits separately identified results and grants no action authority. Reject prompt policy escalation, unvalidated outputs, missing applicable review, ungranted destructive tools and unauthorized secret disclosure. Provider billing limits remain external; no compiler work quota is introduced. Runtime model/tool/memory and Schema/Auth/generated-code APIs supply interface facts.

Foundation requirement gravity/ai-tool-schema-policy-record: Execute a declared model/tool/memory interaction with typed request and response schemas, separately checked effects and grants, output validation, tool/review policy and a redacted attributable decision record naming provider/model/tool/memory identities. Refusals: Reject schema-invalid output, an ungranted tool, crossed memory authority and model text attempting to supply review authority before any protected effect. Evidence: retain the exact source/compiler/profile/target and applicable provider/checker identities, actual positive products and negative diagnostics for this criterion. Consume these actual dependency artifacts (not whole-component completion): schema - Versioned source schema, generated validators/codecs and parity artifacts; legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks - Expanded source profile/effect/grant facts and checked/delegated/rejected legality products.

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

P11 profile interface: emit host/device argument/address-space/layout/alignment/alias/lifetime, transfer/synchronization and feature/workgroup/launch/occupancy records. Reject host services in kernels, use-after-free/double release/unsynchronized host reads, unsupported launch/features and invalid barriers; safe sharing needs provider evidence. Preserve precision/rounding/fusion/denormal/approximation/provider/certificate facts through lowering or diagnose. ABI production, kernel/launch checks, buffer validity and actual Runtime transfers remain separate interfaces; a graph is not executed transfer evidence.

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

DOM6 browser acceptance requires the ticket-list slice, typed API/schema drift
checks and source-mapped loading, validation and unsafe-sink failures. DOM7
mobile acceptance separately requires an adopted platform adapter with permission,
lifecycle, main-thread/actor, null/error and taint contracts; background timeout,
retry and cancellation; local schema/migration/encryption/retention/conflict
policy; and keychain/keystore grants and redaction. The offline ticket fixture
must exercise permission denial, offline edit, sync retry and crash source maps
on a named simulator/device. Dependencies: Schema generated clients and local
migrations, Auth grants, Runtime lifecycle and Backend mobile artifacts. Browser
evidence cannot establish mobile conformance; both remain Open.
<!-- domains-implementation-tasks:end -->

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
