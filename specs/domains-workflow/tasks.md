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
