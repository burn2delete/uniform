# Resolution product projection and identity algorithms

This increment moves the existing C5 product projections, identity chunking,
artifact identity inputs and envelope summaries into gravity.resolution.
D1, D2, D3, D9, C3, C4, C5, C15 and TEST2 govern the boundary. The algorithms
project already resolved analysis; they do not confer resolution or upstream
authentication authority.

Public APIs are product-projections, identity-chunks,
product-identity-chunks, artifact-identity-input-with-chunks,
artifact-identity-input, artifact-id, envelope-summary-with-chunks and
envelope-summary. Inputs are resolved analysis or artifacts, optional existing
identity chunks and explicit seed operations. Outputs are C5 product records,
identity preimages/digests and envelope summary records. Bootstrap retains
compatibility entry points, authentication, descriptor assembly and catalog
constants. The stage must not require Bootstrap.

Explicit operations retain existing resolution-order compatibility and canonical
hash behavior. Receipt qualification and construction drift checks must include
each new stage and callback dependency. Existing identity chunk size is part of
the unchanged hash representation, not an admission allowance.

Acceptance requires real accepted source products, real unresolved-reference
rejection, fresh verification and complete paired-spelling before/after parity.
Tests cover product fields, provenance, deterministic identity, semantic change
invalidation and source-span exclusion only at the existing identity boundary.
Existing C5 diagnostics retain rejection ownership. Independent adversarial
review, clean commits, current CI and final integration preflight remain
required. This slice claims no complete resolution, authoritative migration,
current Reader admission, release, performance, self-hosting or seed retirement.

The stage cold-loads 38 public APIs without Bootstrap. Focused contract,
product, chunk and semantic-identity checks passed 4 tests / 111 assertions.
Direct stage/test lint and changed-source contextual lint report zero blockers;
language boundary passed 6 / 11, documentation passed 240 documents / 19 phase
indexes, and roadmap validation passed. Initial cold-load failures exposed
missing forward Var declarations for callbacks defined later in the seed;
explicit declarations corrected them. An initially redundant nested let was
flattened without changing behavior. The failed initial runs remain nonpasses.

Actual nested-binding source emits 276 bindings / 21 resolutions in both
spellings, with identity
sha256:0fdf519ad7657aa8b291b7aa46df085324bb1fb6bff3cb290b5ecbaf5c8152f2.
Fresh public verification passes. The actual lexical leak rejects right with
C5-UNRESOLVED. All four complete accepted artifacts and rejected diagnostic
payloads compare equal before and after extraction, including each spelling's
provenance. Canonical fixture and Reader paths stayed fixed in the reused
worktree. This comparison binds the original extraction before later upstream
composition; it does not promote supplied chunks into authentication evidence.

Implementation 5641d73a retains the unchanged surrounding descriptor code and
catalog constants. Formerly private identity helpers gain explicit stage APIs
because construction and verification still consume them through private seed
compatibility entry points. A caller-supplied chunk record is data, not a proof;
fresh public verification retains its existing independently derived identity
checks. Final regression, independent acceptance and integration are separate
gates.
