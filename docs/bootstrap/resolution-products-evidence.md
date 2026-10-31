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
