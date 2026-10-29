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

Criterion profile-gap/formal/numeric-semantics (Open): Input: Formal numeric expressions with exact integer/rational/real, bitvector, symbolic, interval, correctly rounded or certified mode and explicit conversion/assumption/provider data. Accept: Admit only declared formal math modes; verify explicit conversions and assumptions and preserve rounding/precision/certificate facts through symbolic or target lowering. Policy cases do not imply that all arithmetic providers are implemented. Reject: Relaxed or target-default math, hidden conversion assumptions, missing proof-mode or certificate, and incompatible backend numeric assumptions. Required outputs: Formal numeric/mode/rounding records, conversion and assumption checks, accepted/rejected P12-MATH-MODE/P12-ASSUMPTION/P12-BACKEND fixtures with theorem/proof/provider/kernel/span/target context. Dependencies: types/current-evidence-and-remaining-work/types2-check-declared-abstractions, legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping formal/numeric-semantics; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/formal/profile-proof-contract (Open): Input: Formal source and theorem/refinement/pre-postcondition claims with quantified inputs or explicit assumptions, trust roots, proof provider/kernel and selected profile/target. Accept: Check deterministic source admission; lower eligible source to symbolic IR; produce proof objects/theorem summaries and explicit assumptions/trusted-kernel/input hashes/certificate chain/conclusion; validate imported source digest/schema/trust/assumptions/theorem/profile/target/hash-chain before policy-permitted use. Reject: Runtime effects or unmodeled nondeterminism/failure, unsafe/raw FFI, hidden target assumptions, untrusted provider, theorem mismatch, failed proof or incompatible imported certificate. Unverified imports follow the declared ignore-or-reject policy, never silent proof acceptance. Required outputs: Symbolic IR, proof object, assumption and trusted-kernel manifests, checked theorem/certificate chain/import verification, positive/negative source/proof/import cases and origin-rich formal diagnostics. Dependencies: legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping formal/profile-proof-contract; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/math/numeric-profile-eligibility (Open): Input: Numeric family/mode/conversion/provider request with active profile/target, allocation support, precision/rounding/NaN/branch/unit and proof facts. Accept: Emit and check the concrete profile support matrix for fixed/arbitrary integers, rationals, proof reals, floats, complex, intervals, symbolic and enabled units. Keep numeric families explicit in types/EFIR/runtime artifacts; admit explicit exact/checked/rounded/saturating/wrapping/proof/unsafe conversion classes only under their actual declared legality; preserve domain/codomain/precision/branch/source annotations. Symbolic equality requires proof rather than tree shape. Reject: Unavailable family/mode/provider, hidden allocation in constrained/core contexts, implicit narrowing/truncation/overflow/precision or rounding/NaN-policy loss, ambiguous branch/unit conversion and symbolic equality without valid evidence. Keep independent source conflicts on particular conversion modes open rather than choosing an undocumented policy. Required outputs: Numeric-kind lattice, conversion-rule table, machine-readable support matrix/mode/provider records, EFIR annotations and conformance cases for every supported conversion/family; MATH1-FAMILY/CONVERSION/NARROW/PRECISION/ROUNDING/BRANCH/ALLOCATION/EQUALITY/PROFILE diagnostics with span/family/mode/profile/target/provider/proof. Dependencies: types/current-evidence-and-remaining-work/types2-check-declared-abstractions, legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping math/numeric-profile-eligibility; all attached contextual and negative predicates remain required. No implementation acceptance is inferred.

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

Criterion profile-gap/distributed/durable-step-contract (Open): Input: Workflow graph and step input/output schemas, declared effects/grants, idempotency key, retry/timeout/cancellation/compensation and failure policy. Accept: Construct typed step records and durable transitions; execute admitted external steps with declared idempotency; execute failure/retry/timeout/cancel/compensation transitions separately; replay recorded results/decisions without repeating the recorded external action. Retain human-review state where applicable. Reject: Untyped external failure, missing required idempotency/compensation or timeout/failure contract, ambient grants, raw device operations and unrecorded nondeterminism in replay-sensitive steps. Required outputs: Workflow graph, typed step table, event-log/persistence boundary records, retry/timeout/compensation table, replay policy/schema and accepted/rejected recovery fixtures. Dependencies: runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution, schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping distributed/durable-step-contract; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/distributed/message-state-boundary (Open): Input: Message, persisted state and service endpoint with schema/version, compatibility/migration, taint/retention, delivery/order and relevant idempotency policies. Accept: Validate transport input/output against the named schemas, apply or reject state/schema migrations under replay compatibility, enforce data-sink taint/retention, and admit services only with declared effect/grant/timeout/retry/error and required backpressure/circuit-breaker policy. Reject: Schemaless boundary, version drift without migration, untyped service failure, ambient service authority and missing delivery/order or required side-effect policy. Required outputs: Message schema bundle, migration result, service capability/error boundary manifest, state persistence and delivery/ordering conformance records. Dependencies: schema, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping distributed/message-state-boundary; all attached contextual and negative predicates remain required. No implementation acceptance is inferred.

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

Historical-gap criterion interfaces (all Open). The stable labels below
reuse inherited source behavior IDs; they add no implementation task IDs.
Each requires its complete original source-context facets, including the
retained negative conditions, diagnostics and conditional consumer roles.

Criterion `behavior/a1/credential-source` (Open; implementation unassessed).
Required behavior: Reject ambient provider credentials embedded in AI source.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a provider credential literal embedded in source before declaration admission.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/declaration-lowering` (Open; implementation unassessed).
Required behavior: Compile declared AI forms from syntax objects to typed declarations and an AI program manifest preserving source/macro origins and declared cross-artifact edges.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject an AI declaration whose emitted manifest loses source/macro origin or a required dependency edge.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/deployment-eval-gate` (Open; implementation unassessed).
Required behavior: Require production agent deployment to name its required evaluation evidence and carry accepted report references.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject production promotion when a required evaluation report is absent or has a different subject identity.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/effect-exposure` (Open; implementation unassessed).
Required behavior: Expose every AI operation as its declared nonpure effect in typed/effect output and emitted manifests, including hidden provider tool effects.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a provider adapter whose implementation performs an undeclared tool effect.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/external-call-policy` (Open; implementation unassessed).
Required behavior: Resolve and enforce the source-specific model-call budget/timeout policy, preserving actual external provider spend and controls while excluding removed synthetic compiler accounting.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Deny an actual paid call before issuance when its external spending policy is exceeded; do not refuse a legal mock call merely for synthetic call-count exhaustion.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Semantic work still Open: produce an exact mixed-clause disposition distinguishing removed synthetic call/repair/retry accounting from actual external spend/account policy, semantic representability and real host restrictions. Preserve unaffected schema, taint, authority, refusal and replay conditions; reject blanket paragraph retirement and renamed work allowances.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/memory-write-policy` (Open; implementation unassessed).
Required behavior: Require AI memory writes to carry retention, redaction, tenant partition and source provenance policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a write missing retention, redaction, tenant or source-provenance policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/model-call-contract` (Open; implementation unassessed).
Required behavior: Require a model call's model/provider identity, input/output schema and replay policy before admitting it; retain the source-specific budget requirement for interpretation.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a call missing provider/model identity, input/output schema or explicit replay mode.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/output-admission` (Open; implementation unassessed).
Required behavior: Keep model output tainted until declared schema validation succeeds; explicitly unstructured text cannot drive a trusted tool action.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a raw or explicitly unstructured response used to drive a trusted tool action.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/prompt-authority` (Open; implementation unassessed).
Required behavior: Prevent untrusted retrieved, user or tool data from acquiring system/developer instruction authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject retrieved/user/tool data promoted to developer or system instruction authority.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/replay-execution` (Open; implementation unassessed).
Required behavior: Require explicit replay modes in distributed/workflow AI contexts and emit/reuse nondeterministic step records instead of issuing undeclared live effects during replay.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject a fresh provider or tool side effect substituted for a required recorded replay result.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a1/runtime-audit-ledger` (Open; implementation unassessed).
Required behavior: Emit model/tool/memory/review/refusal/repair/denial/replay execution ledgers, retaining canonical hashes where raw data cannot be stored.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing linked AI program manifests, source/macro origins, typed/effect output and replay/audit records.
Negative control: Reject an invocation audit bundle missing a model/tool/memory/review/denial/replay event or a required permitted hash.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/emergency-review-bypass` (Open; implementation unassessed).
Required behavior: Permit review bypass only under the declared emergency policy and retain scoped actor/reason/action records; bypass does not create capabilities.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject bypass without declared scoped emergency policy, actor/reason/action audit or the independent capability grant.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/payload-bound-review-gate` (Open; implementation unassessed).
Required behavior: Execute a human-review-required action only under the matching actor/role/action/payload/policy review record with its evidence hash, while it is unexpired and unrevoked; reject absent records, changed payloads, insufficient roles and missing evidence hashes. Denial remains a typed workflow outcome.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject absent, expired, revoked, wrong-role or changed-payload review, and follow the typed denial branch.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/review-audit-ledger` (Open; implementation unassessed).
Required behavior: Record review requests, decisions and lifecycle transitions with protected payload redaction and canonical hashes.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject a review lifecycle record with missing actor/payload/evidence hash or protected raw data.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/review-declaration-artifact` (Open; implementation unassessed).
Required behavior: Compile the typed human-review declaration with action schema, role/principal, displayed evidence, payload hash, lifecycle, replay and emergency policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject a review declaration missing action/evidence schema, role, lifecycle, replay or emergency binding.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/review-replay-policy` (Open; implementation unassessed).
Required behavior: During replay reuse a valid recorded review decision unless the declared replay mode requires a live human decision.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject live review in a mode requiring recorded review, or recorded reuse with changed payload/policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a10/typed-reviewer-evidence` (Open; implementation unassessed).
Required behavior: Display enough typed action evidence through the UI/API for the reviewer to understand what is being authorized.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed review/action/evidence artifacts and payload/actor/role/policy-bound lifecycle and replay records.
Negative control: Reject an action API/UI that conceals the typed payload, evidence hash or action consequences from the reviewer.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/generated-source-trust` (Open; implementation unassessed).
Required behavior: Treat model-generated source as untrusted until the ordinary compiler validation and required review complete.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject execution of generated source before ordinary reader, macro, type, effect, profile and safety validation.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/incident-reconstruction` (Open; implementation unassessed).
Required behavior: Emit sufficient prompt/model/tool/policy/retrieval/review/result lineage to reconstruct a malicious or denied action without leaking protected raw data.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject an incident bundle with an unjoinable prompt/tool/memory/policy/workflow decision or leaked protected body.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/privileged-defense-gate` (Open; implementation unassessed).
Required behavior: Require privileged AI releases to pass applicable prompt-injection, unauthorized-tool, secret and policy-bypass defense probes.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject privileged promotion with missing, stale or failing injection/tool-escalation/exfiltration/policy-bypass probes.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/runtime-escalation-monitor` (Open; implementation unassessed).
Required behavior: Record denied tool escalation, policy override attempts and protected-data exposure attempts in runtime monitoring records.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject an audit result that omits denied escalation, policy override or protected-data exposure attempts.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/secret-access-policy` (Open; implementation unassessed).
Required behavior: Permit secret access only under explicit scoped policy and reject protected-data exfiltration through prompts, tools, memory or logs.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject secret exfiltration via prompt, tool, memory or log without explicit scoped policy and grant.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/tool-authority-agreement` (Open; implementation unassessed).
Required behavior: Deny a model-requested tool action unless tool declaration, agent toolset, deployment grants and runtime capability handle all agree.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject a requested tool if declaration, agent toolset, package/deployment grant, scoped runtime handle or policy disagrees.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a11/untrusted-instruction-boundary` (Open; implementation unassessed).
Required behavior: Keep repository/web/retrieved/tool/model text in its declared untrusted data role and prevent instruction or policy override.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing authority/taint/tool-authorization artifacts, defense probe results and reconstructable redacted incident records.
Negative control: Reject repository/web/file/package/retrieved/tool/model data that changes instructions, grants or policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/credential-binding` (Open; implementation unassessed).
Required behavior: Load provider credentials from a declared secret source and reject missing or unsafe credential bindings.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject missing or ambient credential binding and any credential value copied into a forbidden artifact.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/embedding-shape-admission` (Open; implementation unassessed).
Required behavior: Reject an embedding provider whose vector dimension differs from the consuming memory manifest.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject embedding output whose vector dimension differs from the consuming memory manifest.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/external-spend-preflight` (Open; implementation unassessed).
Required behavior: Deny an actual live provider call before issuance when its known external spending limit is already exceeded; retain max-cost-usd and observed cost metadata.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject known-over-limit external spend before a live request, retaining max-cost policy and observed cost facts.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/failure-classification` (Open; implementation unassessed).
Required behavior: Classify provider failures as retryable, permanent, spending/policy denied or schema invalid so callers observe the declared failure contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject provider failures collapsed into untyped exceptions instead of retryable/permanent/spend-denied/policy-denied/schema-invalid results.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/fallback-admission` (Open; implementation unassessed).
Required behavior: Admit a fallback only if requested mode, schema guarantee, retention/data policy and applicable evaluation gate remain satisfied; preserve external spending policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject fallback that changes mode/schema/retention/data/spend policy or lacks the applicable comparison evaluation.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/floating-identity-upgrade` (Open; implementation unassessed).
Required behavior: Accept a floating provider/model identity upgrade only under an agent/workflow evaluation gate covering that new identity.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject floating model upgrades not covered by the agent/workflow evaluation gate for the new identity.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/live-ci-policy` (Open; implementation unassessed).
Required behavior: Reject live provider calls in CI unless package/build policy explicitly permits them.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject a live CI provider request without explicit package/build allowance.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-call-ledger` (Open; implementation unassessed).
Required behavior: Emit request/response, retry/fallback, drift, redaction and validation records with the required source/agent/prompt/schema/tools/handle/parameter/result identities and observed usage/cost/latency.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject call records missing source/agent/prompt/schema/tool/handle/parameter/response/refusal/validation/usage/redaction identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-effect` (Open; implementation unassessed).
Required behavior: Require :ai/model-call or :ai/embedding for an actual provider operation.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject inference or embedding without its declared AI effect.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-handle` (Open; implementation unassessed).
Required behavior: Reject a provider call without the provider capability handle even when network access is granted.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject a model call with network access but no scoped provider capability handle.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-manifest` (Open; implementation unassessed).
Required behavior: Normalize provider/model declarations from package, deployment or local-profile inputs into one compiler-visible package artifact with the declared adapter, endpoint, modes, representability, schema, safety and retention contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject a normalized provider/model artifact missing adapter, endpoint, mode, version, schema, safety or retention requirements.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/provider-retention-admission` (Open; implementation unassessed).
Required behavior: Reject provider use when its data retention behavior conflicts with the active no-retention policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject a retaining provider under a no-retention policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/replay-model-identity` (Open; implementation unassessed).
Required behavior: Record the complete provider/model/version/endpoint/adapter/tokenizer/output/safety-retention identity; audit-sensitive pinned replay rejects any mismatch or undeclared reproducibility-affecting request parameter.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject pinned replay after model/version/endpoint/adapter/tokenizer/output/safety-retention drift or undeclared reproducibility parameters.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/secret-and-no-store-isolation` (Open; implementation unassessed).
Required behavior: Prevent provider credentials and no-store data from leaking into prompt/log/memory/eval/replay records and omit forbidden raw request/response bodies while preserving canonical hashes and validation facts.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject credentials/no-store bodies entering prompt/log/memory/eval/replay records; retain only policy-permitted hashes and validation facts.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/structured-mode-contract` (Open; implementation unassessed).
Required behavior: Declare and honor whether structured-output guarantees come from provider enforcement, runtime validation or both; reject unsupported modes and report validation failures against that mode.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject unsupported output mode or a response admitted without the declared provider/runtime validation guarantees.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a2/tool-mediation-contract` (Open; implementation unassessed).
Required behavior: Declare provider-native, Gravity-mediated or runtime-adapted tool execution and expose its effects instead of hiding tool authority in the adapter.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing provider/model artifacts and typed request/response, validation, failure, fallback, replay, spend and redaction records.
Negative control: Reject provider-native or adapted tool execution whose mediation mode, effects or authority are hidden.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/input-schema-admission` (Open; implementation unassessed).
Required behavior: Reject an agent/workflow prompt without its declared input schema.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject an agent/workflow prompt without its declared input schema.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/output-schema-admission` (Open; implementation unassessed).
Required behavior: Reject a response consumed by trusted code without its declared output schema, and prevent the model from choosing an arbitrary output schema identity.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject trusted code consuming a response with no declared output schema or a model-selected arbitrary schema.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/partial-output-admission` (Open; implementation unassessed).
Required behavior: Reject partial output unless the chosen output schema explicitly defines a legal partial state.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject partial output unless the declared schema admits that partial state.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/prompt-artifact` (Open; implementation unassessed).
Required behavior: Compile a syntax-derived typed prompt artifact with declared input/output-or-unstructured schema, authority/taint/tool/provider/validation/compatibility references rather than treating it as an arbitrary string.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject a prompt treated as an arbitrary string without source/schema/authority/taint/tool/provider/validation/compatibility identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/prompt-evolution` (Open; implementation unassessed).
Required behavior: Classify prompt evolution as additive, breaking or eval-required, rejecting a compatibility claim that changes output meaning.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject a compatibility claim that changes output meaning without the required breaking/evaluation disposition.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/prompt-no-store` (Open; implementation unassessed).
Required behavior: Reject storing response bodies in an active no-store context while preserving permitted validation/hash records.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject raw response-body storage in a no-store context.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/refusal-routing` (Open; implementation unassessed).
Required behavior: Represent provider refusal in the output type or declared workflow branch and follow that branch.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject provider refusal treated as success instead of its declared output type or workflow branch.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/render-identity` (Open; implementation unassessed).
Required behavior: Record the prompt artifact hash and actual rendered-input hash for each rendered prompt.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject rendering records whose prompt artifact hash or actual rendered-input hash is missing or substituted.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/repair-recording` (Open; implementation unassessed).
Required behavior: Permit repair only under declared repair policy and record each attempt and final validation outcome; validation failure can branch only to declared repair/retry/review/failure handling.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject repair outside declared policy, an unrecorded attempt or validation failure escaping its typed retry/review/failure branch.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/repair-termination-policy` (Open; implementation unassessed).
Required behavior: Resolve the source-specific maximum repair attempt rule and reject illegal repair loops without inventing a replacement synthetic compiler work quota.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject a mixed-clause disposition that removes validation/refusal/data policy with synthetic attempt accounting, or substitutes a renamed compiler quota.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Semantic work still Open: produce an exact mixed-clause disposition distinguishing removed synthetic call/repair/retry accounting from actual external spend/account policy, semantic representability and real host restrictions. Preserve unaffected schema, taint, authority, refusal and replay conditions; reject blanket paragraph retirement and renamed work allowances.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/secret-to-provider-policy` (Open; implementation unassessed).
Required behavior: Reject secrets in rendered prompts except when the policy explicitly permits that secret-to-provider flow.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject rendered secrets without an explicit secret-to-provider policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/taint-propagation` (Open; implementation unassessed).
Required behavior: Carry interpolated-value taint through rendering and per-field output validation until the policy for intended use is satisfied.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject interpolated-value or output-field taint disappearing before intended-use policy validation.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/tool-visibility` (Open; implementation unassessed).
Required behavior: Expose only prompt-declared tool schemas under the declared tool-call policy; reject undeclared tool exposure.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject exposing tools not declared by the prompt toolset/policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a3/typed-output-admission` (Open; implementation unassessed).
Required behavior: Validate tainted provider output by declared format/schema, required canonicalization, per-field taint and intended-use data policy before converting it to a typed core value or allowing tools/workflow/generated-source use.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed prompt/schema/authority/taint artifacts, actual render hashes and validation/refusal/repair results.
Negative control: Reject conversion to typed core, workflow branch, tool argument or generated-source input before format/schema/canonicalization/taint/use-policy validation.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/foreign-error-normalization` (Open; implementation unassessed).
Required behavior: Convert foreign implementation failures to the declared typed tool error contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject foreign exceptions escaping the declared typed tool result contract.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/nonidempotent-retry` (Open; implementation unassessed).
Required behavior: Do not retry or replay a non-idempotent effect without its declared idempotency/compensation contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject a non-idempotent retry/replay without its declared idempotency or compensation contract.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/privileged-tool-review` (Open; implementation unassessed).
Required behavior: Stop privileged tool actions until the declared human-review policy is satisfied without treating review as capability creation.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject privileged execution before required review, or treating review as a new capability grant.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-authority-agreement` (Open; implementation unassessed).
Required behavior: Authorize a tool call only when declaration, package/deployment grant, agent toolset and scoped runtime implementation handle agree.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject a requested tool if declaration, agent toolset, package/deployment grant, scoped runtime handle or policy disagrees.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-effects` (Open; implementation unassessed).
Required behavior: Expose every implementation effect at the tool boundary; hidden effectful adapter behavior is rejected.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject any effect performed by a tool implementation but absent from its declaration.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-manifest` (Open; implementation unassessed).
Required behavior: Compile typed tool declarations with declared input/output schemas and public model-facing descriptions that expose neither secret values nor implementation handles.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject a tool with missing input/output schemas or a model-visible description exposing credentials, internal policy or implementation handles.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-redaction` (Open; implementation unassessed).
Required behavior: Emit tool call and failure records with declared redaction, preserving auditable identities without leaking secrets.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject tool success/failure ledgers leaking secret/private/no-store fields or omitting required audit identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a4/tool-result-admission` (Open; implementation unassessed).
Required behavior: Validate tool results before they become trusted program values or downstream action inputs.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing tool artifacts and schema/effect/handle/policy/review/typed-error/redaction invocation records.
Negative control: Reject an unvalidated tool result entering an agent/workflow or trusted downstream sink.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-eval-admission` (Open; implementation unassessed).
Required behavior: Require production agent deployment to bind the applicable passing evaluation evidence.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject production deployment without passing evidence for its release class and exact agent dependencies.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-external-call-policy` (Open; implementation unassessed).
Required behavior: Interpret and enforce the specific agent call/retry/wall-time/token/spend policy without substituting removed synthetic compiler accounting for actual external controls.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject a source disposition that keeps synthetic agent call/retry work allowances or removes actual external spend/account, effect or authority policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Semantic work still Open: produce an exact mixed-clause disposition distinguishing removed synthetic call/repair/retry accounting from actual external spend/account policy, semantic representability and real host restrictions. Preserve unaffected schema, taint, authority, refusal and replay conditions; reject blanket paragraph retirement and renamed work allowances.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-ledger-identity` (Open; implementation unassessed).
Required behavior: Bind every agent execution ledger to the exact agent artifact and dependency identities for reconstruction and replay.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject any model/tool/memory/review/workflow ledger event not bound to its agent artifact and dependency identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-manifest` (Open; implementation unassessed).
Required behavior: Compile the agent's complete model/prompt/tool/memory/policy/eval/workflow dependencies into its normalized artifact and reject incomplete bindings.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject an agent missing a declared model, prompt, tool, memory, policy, evaluation or workflow dependency.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-memory-access` (Open; implementation unassessed).
Required behavior: Require distinct declared read/write memory access modes and matching runtime authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject a memory write through an agent's read-only binding or a missing separate write capability.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-output-admission` (Open; implementation unassessed).
Required behavior: Validate agent output before a typed consumer or tool action may use it.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject an unvalidated agent result used as typed data or tool input.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/agent-tool-authority` (Open; implementation unassessed).
Required behavior: Constrain tool use to the intersection of the agent-declared toolset and actual grants; neither a model plan nor an agent declaration enlarges authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject a plan or declaration used to enlarge the intersection of declared toolset and actual grants.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a5/generated-code-reentry` (Open; implementation unassessed).
Required behavior: Send agent-generated source through the ordinary compiler checks before use.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing agent dependency manifests and identity-linked model/tool/memory/review/output-validation events.
Negative control: Reject agent-generated source used without normal compiler validation and preserved generated origin.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a6/validated-branch-input` (Open; implementation unassessed).
Required behavior: Allow workflow branches only on validated typed values, never raw model output.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed graph/state/event-log artifacts with branch, compensation, review-payload, replay and retry transition evidence.
Negative control: Reject a workflow branch selected from raw, unvalidated model output.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a6/workflow-retry-policy` (Open; implementation unassessed).
Required behavior: Preserve the declared retry policy and records while resolving any source-specific accounting clause separately.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed graph/state/event-log artifacts with branch, compensation, review-payload, replay and retry transition evidence.
Negative control: Reject a retry that loses its declared replay/idempotency/compensation record or turns removed accounting into a new compiler allowance.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Semantic work still Open: produce an exact mixed-clause disposition distinguishing removed synthetic call/repair/retry accounting from actual external spend/account policy, semantic representability and real host restrictions. Preserve unaffected schema, taint, authority, refusal and replay conditions; reject blanket paragraph retirement and renamed work allowances.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a6/workflow-review-binding` (Open; implementation unassessed).
Required behavior: Bind workflow human-review decisions to the exact executed payload and declared review/replay policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing typed graph/state/event-log artifacts with branch, compensation, review-payload, replay and retry transition evidence.
Negative control: Reject workflow review reuse after payload change or outside the declared review/replay policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/deletion-index-coherence` (Open; implementation unassessed).
Required behavior: Apply memory deletion to stored items, index state and the required audit ledger consistently.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject deletion/redaction that leaves a retrievable stale item or inconsistent index/ledger.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/embedding-authority` (Open; implementation unassessed).
Required behavior: Require actual embedding-provider authority and a dimension-compatible embedding contract.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject embedding without the actual provider grant or with an incompatible dimension contract.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/memory-access-handles` (Open; implementation unassessed).
Required behavior: Require the distinct read and write handles for the requested memory operation; declarations do not supply ambient access.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject a read/write using a missing or wrong-mode scoped memory handle.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/memory-manifest` (Open; implementation unassessed).
Required behavior: Compile a typed memory store/item schema with provenance, partition, retention, index and embedding identity.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject a memory artifact missing item/metadata schema, provenance, partition, retention, indexing, embedding or replay policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/persistence-data-policy` (Open; implementation unassessed).
Required behavior: Reject protected-data persistence that violates retention, redaction, deletion or permitted data-class policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject protected-data embedding/persistence contrary to data-class, retention, redaction or deletion policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a7/reembedding-lineage` (Open; implementation unassessed).
Required behavior: Record old/new embedding and source identities when re-embedding; incompatible dimensions cannot reuse an index silently.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing memory/schema/embedding/partition/retention artifacts and access, deletion, index and re-embedding lineage evidence.
Negative control: Reject re-embedding without source-item hash and old/new embedding identities, or silent incompatible-index reuse.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/ai-policy-artifact` (Open; implementation unassessed).
Required behavior: Compile an explicit production AI policy binding and preserve its input/output domains and decision rules.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject production AI policy without its explicit normalized input/output domains and rules.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/authority-change-compatibility` (Open; implementation unassessed).
Required behavior: Treat policy changes that broaden authority as security/compatibility changes requiring the declared review, rather than harmless metadata edits.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject authority-broadening policy changes released without compatibility and security review.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/emergency-override-policy` (Open; implementation unassessed).
Required behavior: Allow only explicitly scoped/time-bound emergency policy overrides and retain the actor, reason and action audit trail.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject unscoped, expired, unlogged or unauditable emergency policy override.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/fallback-evidence-policy` (Open; implementation unassessed).
Required behavior: Deny model/tool fallback when its policy or applicable evidence requirements are not met.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject a fallback missing required safety/retention/schema/spend policy or applicable evaluation evidence.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/generated-code-admission` (Open; implementation unassessed).
Required behavior: Require ordinary compiler validation of AI-generated source before policy permits its use.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject generated code executed, published or used as a dependency before ordinary compiler validation and required tests.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/output-and-instruction-boundary` (Open; implementation unassessed).
Required behavior: Keep model/tool/retrieved output tainted data until permitted validation; data cannot change instructions or grant authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject tainted model/tool/retrieved data becoming trusted instructions or authority without its permitted validation boundary.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/policy-composition` (Open; implementation unassessed).
Required behavior: Compose policy layers deterministically with deny precedence; a later layer may narrow but cannot silently broaden authority.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject cyclic/nondeterministic policy, an allow overriding an equal/narrower deny, or silent authority broadening by a later layer.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/policy-decision-ledger` (Open; implementation unassessed).
Required behavior: Record each AI action's policy inputs, decision, rule and artifact identity so approval or denial can be reconstructed.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject an action record missing principal/agent/effect/grant/data/taint/review/eval/replay facts, rule or decision.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a8/review-rule-enforcement` (Open; implementation unassessed).
Required behavior: Apply typed human-review role/payload/expiry/revocation/replay rules to the action covered by the policy.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing deterministic policy artifacts and explained allow/deny/review/validation/eval decisions with scoped audit records.
Negative control: Reject a policy decision authorizing the wrong action/role/payload or expired/revoked review.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/changed-artifact-comparison` (Open; implementation unassessed).
Required behavior: Report which model/prompt/tool/policy artifacts changed and the compared evaluation result identities, not an unscoped aggregate score.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject evaluation comparisons that omit a changed model/prompt/tool/memory/policy artifact or result identity.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/eval-dataset-artifact` (Open; implementation unassessed).
Required behavior: Version datasets with schema, provenance and split identities so consumed evaluation input is reproducible and attributable.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject datasets without schema, version, provenance or split identities.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/eval-redaction` (Open; implementation unassessed).
Required behavior: Redact protected data from evaluation datasets/reports while retaining the permitted hashes and provenance needed to reproduce the claim.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject evaluation samples or reports storing protected raw data contrary to policy.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/live-eval-policy` (Open; implementation unassessed).
Required behavior: Permit live provider evaluation only under explicit credentials, real external spending/account and retention/data policy; recorded/mocked evaluation is identified as such.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject live evaluation without explicit credentials and actual external spend/account/retention policy, or a mock result mislabeled live evidence.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/nondeterministic-eval-evidence` (Open; implementation unassessed).
Required behavior: Declare seeds, trace reuse or repeat/variance evidence for nondeterministic evaluation instead of claiming exact replay from uncontrolled live results.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject a nondeterministic evaluation claiming reproducibility without seeds, trace reuse or declared repeat/variance evidence.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion `behavior/a9/safety-probe-coverage` (Open; implementation unassessed).
Required behavior: Run context-relevant safety probes covering prompt injection, tool misuse, policy denial, schema failure, refusal handling and protected-data leakage. Required release evaluation fails closed for missing, stale or incompatible evidence.
Acceptance output: a valid source-bound fixture exercises this behavior
under its declared profile, schema, capability, data and replay policy,
producing subject/dataset/metric/probe/variance-bound evaluation reports and redacted promotion or refusal evidence.
Negative control: Reject a release safety claim without matching injection/tool-misuse/denial/schema/refusal/leakage probes and compatible evidence.
Evidence required: accepted/rejected fixture inputs and exact expected
typed values or refusal, stable diagnostic and contextual fields, artifact
identities and source/policy hashes. Record producer and consumer results
separately; generated metadata alone cannot prove execution.
Dependencies: the existing Schema and Workflow interfaces, typed/effect
and profile legality, scoped Runtime/Auth enforcement, and this source's
declared provider/consumer contracts. All source qualifications in the
A1-A11 acceptance contract above also apply to this criterion.

Criterion profile-gap/ai/profile-tool-contract (Open): Input: AI model/tool declaration, provider policy, typed arguments, caller/deployment grants, side-effect class and applicable review record. Accept: Admit and execute the declared model request through its provider; validate tool input/output schemas and side-effect/review policy before a tool action; re-enter ordinary macro/type/effect/capability/safety/package/profile checking before generated Gravity code can execute. Review approval never manufactures a missing capability. Reject: Missing provider/effect/policy, tool schema or grant; write/destructive/shell/deployment/package/secret action without applicable review; raw memory/FFI in agent logic; generated code executed before compiler validation. Required outputs: Agent/model/tool manifests, schema validation result, effect/grant/review decisions and generated-code validation/provenance record. The mixed original cost/token/call predicates require a source-specific billing-versus-removed-accounting decision; retain that Open policy outcome and all adjacent authority restrictions. Dependencies: schema, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime, expand/current-evidence-and-remaining-work/expand5-integrate-generated-programs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping ai/profile-tool-contract; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/ai/prompt-memory-boundary (Open): Input: Prompt messages with roles/provenance and retrieved/tool/memory/model values carrying trust, taint, retention/deletion/privacy and secret policy. Accept: Preserve each source-visible role and origin; validate model output for the particular sink before trusting it; enforce memory retention/deletion/privacy and trust on reads/writes; permit secret-bearing prompt/tool/provider/log disclosure only through explicit matching policy. Reject: Retrieved or tool content overriding privileged policy, unvalidated model output at a trusted sink, stale/deleted or cross-policy memory access, and unauthorized secret disclosure. Required outputs: Prompt provenance/role record, sink validation and taint result, memory policy/lifecycle evidence, redacted secret-policy denial diagnostics. Dependencies: runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution, auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping ai/prompt-memory-boundary; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/ai/replay-evaluation-artifacts (Open): Input: Agent execution with model/provider/prompt/tool/policy identities, review decisions, retry/error state and selected audit/replay mode. Accept: Emit the execution ledger with prompt/message and generated-code digests, tool inputs/outputs, review/policy/nondeterminism/error/retry records; replay required operations from matching records. Expose an evaluation hook that produces separately identified fixtures/results without granting authority. Reject: Replay/audit-required execution missing required records, digest or identity substitution, or evaluation results used to bypass capability/safety/review checks. Required outputs: Model/tool call trace, replay/audit log schema and records, generated-code digest, independently identified evaluation result. Dependencies: workflow, runtime/remaining-tasks-and-evidence/connect-distributedai-replay-and-interactive-execution interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping ai/replay-evaluation-artifacts; all attached contextual and negative predicates remain required. No implementation acceptance is inferred.

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

Criterion profile-gap/gpu/host-device-boundary (Open): Input: Separate host orchestration and GPU kernel artifacts with argument address-space/layout/alignment/alias/lifetime and declared target feature/workgroup/grid/occupancy/launch configuration. Accept: Validate kernel-only legality, construct the typed host/device ABI boundary and transfer/lifetime records, and check target launch/features independently. Host profile remains explicit; device orchestration cannot import ambient host semantics into a kernel. Reject: Host IO/reflection/eval/allocation/GC/exceptions/ordinary threads in kernels, unsupported recursion/dispatch, missing argument or transfer boundary, invalid target or launch configuration. Required outputs: Host/device and target/launch/occupancy manifests, kernel IR, typed argument ABI and accepted/rejected kernel/launch/feature fixtures with kernel/buffer/address-space/origin context. Dependencies: backend-interface, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs, legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping gpu/host-device-boundary; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/gpu/memory-synchronization (Open): Input: GPU buffers/slices and transfer/queue/kernel/barrier graph with ownership/lifetime/address-space/alignment/layout/alias/visibility and sharing-provider facts. Accept: Treat buffers as linear unless provider-backed sharing is proved; validate use/release and host/device visibility; preserve launch/fence/workgroup barrier/atomic/queue/transfer-completion order and address spaces through backend lowering. Actual transfer execution consumes the validated graph separately. Reject: Use-after-free, double release, unsynchronized host read, invalid device alias, missing barrier/queue dependency/transfer completion or unsupported host allocation. Required outputs: Lifetime report and transfer/synchronization graphs, accepted ownership/transfer/barrier cases and P11-DEVICE-MEMORY/TRANSFER/SYNC/ALIAS/RAW diagnostics with exact buffer/address-space/edge/target/origin. Dependencies: legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks, runtime/remaining-tasks-and-evidence/implement-memory-concurrency-and-typed-ffi-boundaries, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping gpu/memory-synchronization; all attached contextual and negative predicates remain required. No implementation acceptance is inferred. Criterion profile-gap/gpu/numeric-mode-contract (Open): Input: GPU/EFIR numeric operations with declared precision, rounding, fusion, denormal, approximation error, provider, certificate/validation and device feature facts. Accept: Check the selected device implementation and target mapping against every declared numeric/accuracy fact and preserve the certificate contract or emit a diagnostic. Reject: Implicit backend approximate math, missing mode/provider/certificate or accuracy record, undeclared denormal/fusion change and unsupported target features. Required outputs: Math-mode/feature manifest and approximation certificate bundle, accepted/failed numeric-preservation cases and P11-MATH/TARGET-FEATURE context with kernel/target/origin/certificate. Dependencies: math, lower/current-evidence-and-remaining-work/lower1-validate-lowering-inputs, legality/remaining-tasks-and-evidence/complete-profile-restrictions-and-generated-form-checks interface facts, not whole-task completion. Exact source predicates and frozen evidence: specs/requirement-reconciliation/profile-system/gap-completion.edn, mapping gpu/numeric-mode-contract; all attached contextual and negative predicates remain required. No implementation acceptance is inferred.

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
