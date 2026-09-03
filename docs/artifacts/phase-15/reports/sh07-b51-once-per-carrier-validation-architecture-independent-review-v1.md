# SH-07 B51 Once-Per-Carrier Validation Architecture Independent Review V1

## Decision

REJECT the exact frozen architecture candidate. A narrower architecture is
required before implementation may be registered.

The reviewed tuple is:

- base commit: `ba5fbb8add22e9b8e3b21288e5426601841e6839`;
- candidate commit: `704ab6fb69ab9cde05193859e74e90cc19722f7d`;
- candidate tree: `f822b3fd13a4f65123b965bb33c6d79b12fda13f`;
- architecture report:
  `docs/artifacts/phase-15/reports/sh07-b51-once-per-carrier-validation-architecture-v1.md`;
- report SHA-256:
  `6c94064505a7f7397250460a6b6bee80321c46ec830f00e2b0f774b370151537`;
- provisional evidence:
  `docs/artifacts/phase-15/reports/sh07-b51-once-per-carrier-validation-provisional-evidence-v1.md`;
- provisional-evidence SHA-256:
  `8ed961afa35e44b2c8bb6e9c481eec2501c0c88efffb57f899b0974fb64854e1`;
- review-pending lifecycle HEAD:
  `58a35514ca74fcc7a88cd792f4a0786ee6ca61f4`.

The candidate worktree was clean. The three commits after the candidate changed
only its active workstream shard and compact ledger. The candidate itself adds
only the report and provisional evidence. This review does not alter either
frozen file, authorize implementation, accept a carrier or certificate, or
claim SH-07 completion.

Reviewer identity: `sh07-runtime-validation-architecture-reviewer-v1`,
canonical task `/root/runtime_validation_arch_review`.

## Blocking Finding: Root-1 Receipt Launders Unchecked Outer Facts

The report permits Root 1 to replace its complete
`sh07-b51-raw-carrier-shape?` and nested
`sh07-b51-r1-compact-carrier-v4?` gate with the receipt builtin while retaining
only `sh07-b51-root1-invocation-shape?`. It then says the receipt authenticates
closed outer and nested shapes and that both the outer raw request and nested
carrier equal locally constructed authority.

The actual host admission implication is narrower. At implementation evidence
commit `45556a3f447a1262cd5f01afe5630571ee759d18`,
`p15-s23-sh07-b51-v4-raw-joins-valid?` establishes only:

- the exact eleven outer keys;
- the outer artifact, schema version, and scope;
- the predecessor executable-closure descriptor id; and
- that the seven remaining outer products are maps.

The host deeply reconstructs, pins, compares, and validates the nested
`:v18-executable-descriptor`. It does not validate the complete outer
authenticated SH-06 request/report/artifact, fresh SH-06 binding, predecessor
descriptor contract, predecessor contract carrier, raw predecessor observation,
or physical invocation. The outer preflight bounds those values but does not
authenticate their semantics.

Root 1 currently performs those missing checks inside
`sh07-b51-raw-carrier-shape?`:

- `sh07-b51-fresh-sh06-binding?`;
- `sh07-b51-predecessor-descriptor-shape?`;
- `sh07-b51-predecessor-contract-shape?`;
- `sh07-b51-raw-observation-shape?`; and
- `sh07-b51-physical-invocation-shape?`.

The retained shallow `sh07-b51-root1-invocation-shape?` checks only the exact
outer keys, artifact, schema, scope, and that the authenticated request is a
map. Object identity to the admitted raw value cannot promote unchecked fields
of that object into authenticated facts.

This is observable, not merely a documentation mismatch. A read-only probe
against the exact implementation evidence source called the host raw-join
predicate with all eleven outer keys, the correct fixed coordinates and
predecessor id, but `{}` for `:physical-invocation` and the other map-shaped
outer products. It returned:

```clojure
{:host-raw-joins-valid? true, :malformed-physical-invocation {}}
```

The baseline Gravity predicate rejects `{}` because physical invocation has a
closed six-key schema and typed/hash constraints. In the producer source,
`:physical-invocation` is otherwise used only by that complete raw-shape gate
and the shallow exact-key check. Therefore an otherwise authentic raw request
with only this field replaced can pass the proposed host receipt and retained
Root-1 shallow gate while bypassing the baseline rejection. The candidate can
thus change a rejected invocation into semantic execution and violates its own
no-weakening, byte/value-identical result, stable-diagnostic, and mutation
obligations.

The report's statement that the outer raw request equals local authority is
also false as written: the local authoritative carrier is the nested eleven-key
carrier, not the distinct outer eleven-key raw-adapter request.

## Required Repair

A successor architecture must not deepen the host into a new outer-request
semantic oracle. It should narrow the Root-1 projection to only facts already
proved by host admission:

1. retain the complete Root-1 outer/request/fresh-SH06/predecessor/raw-
   observation/physical checks;
2. replace only the nested
   `sh07-b51-executable-carrier-shape?` / `sh07-b51-r1-compact-carrier-v4?`
   work with the private receipt result;
3. place that replacement in a Root-1-specific gate so the compiler-internal
   builtin does not become reachable through the shared general raw validator;
4. state separately which outer facts come from bounded preflight, which fixed
   coordinates come from host raw joins, and which semantic facts remain
   Gravity-owned; and
5. add mutations for every outer product, especially a map-shaped malformed
   `:physical-invocation`, proving byte/value-identical baseline rejection and
   stable diagnostics.

Root 6 already replaces only
`sh07-b51-r6-c30-carrier-authoritative?` and explicitly retains its separately
authored outer, fresh-SH06, predecessor, same-request, and semantic checks. That
cut point is structurally aligned, but any shared prose claiming deep outer
authentication must still be removed from both role projections.

## Verified Nonblocking Properties

Apart from the Root-1 projection blocker, independent inspection found the
following architecture properties coherent:

- integrated Attempt 32 is the sole lifecycle dependency and is integrated at
  the exact base;
- admission remains preflight-first, locally reconstructed, pin-bound,
  certificate-pair checked, route/owner/arity checked, and fail closed before
  receipt mint;
- the receipt schema is closed, private, nonserialized, role-specific, and
  bound by identity to token, runtime, invocation, raw, nested carrier,
  authority, plan, and argument vector;
- token lookup uses object identity, value-equal clones reject, the first
  authorized lookup consumes the role gate atomically, and synchronous
  `finally` invalidation is mandatory;
- caller-supplied profiles, facts, tokens, receipts, routes, certificates, and
  semantic results grant no authority;
- Root 1 receives certificate A plus common joins, while Root 6 receives
  certificate B plus common joins; neither may borrow the other's witness or
  output;
- the builtin is closed to exactly two freshly censused V2 call occurrences
  with descriptor/source/manifest/plan/function/instruction provenance, and no
  shared, public, dynamic, compatibility, application, FFI, callback, or
  reflective path is authorized;
- the runtime limit remains exactly 8,388,608 transitions, the frame limit
  4,096, the authenticated external-call limit 65,536, and the manifest census
  exactly 1,480 edges; the receipt neither refunds nor resets fuel and its
  machine transitions remain counted;
- the mutation, replay, cleanup, exact-limit, first-plus-one, differential,
  identity-rebuild, and D6 cost/correctness evidence obligations are
  appropriately broad; and
- the dirty full-fuel and 500,000-step observations are honestly classified as
  provisional localization only, with wrapper calls explicitly distinguished
  from machine transitions and clean reproduction required.

These passing properties do not cure an invalid admission-to-fact implication.

## Validation Receipts

The following gates were rerun from the clean review-pending worktree:

```text
clojure -M tools/validate_architecture_authority.clj \
  --report docs/artifacts/phase-15/reports/sh07-b51-once-per-carrier-validation-architecture-v1.md
architecture authority validation passed

clojure -M tools/validate_workstream_governance.clj
validation passed: 149 workstreams

clojure -M tools/validate_gravity_docs.clj
validation passed: 240 docs, 19 phase indexes, ASCII, no placeholders

clojure -M tools/validate_full_language_roadmap.clj
full-language roadmap validation passed

clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
6 tests, 11 assertions, 0 failures, 0 errors

git diff --check \
  ba5fbb8add22e9b8e3b21288e5426601841e6839..704ab6fb69ab9cde05193859e74e90cc19722f7d
passed
```

Exact commit, tree, report digest, evidence digest, base ancestry, sole
dependency state, candidate path set, governance-only post-candidate path set,
and clean status were also independently checked.

## Authority and Next Review

This review rejects only the exact tuple named above. It grants no
implementation registration, integration, publication, carrier, certificate,
performance, safety, proof, equivalence, roadmap, self-hosting, release, or
seed-retirement authority and no aggregate SH-07 completion credit.

A narrowed successor must receive a new exact candidate commit, tree, report
and evidence digests, lifecycle record, validation receipts, and fresh
independent architecture review before implementation starts.
