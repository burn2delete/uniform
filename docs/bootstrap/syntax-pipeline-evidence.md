# Syntax pipeline extraction

## Boundary and governing contracts

This increment moves the existing C3 source artifact construction and complete
reader-to-syntax association verification bodies out of `gravity.bootstrap`
into `gravity.syntax`. The public stage API is `build [request operations]`,
`verify [source-path artifact operations]`, and
`reader-products-authentic? [stream reader boundary invocation operations]`.
The old bootstrap entry points retain their arities and supply explicit seed
dependencies. Callable Vars preserve the bootstrap's late interposition.

D1, D2, D3, D9, L1, C1, C2, C3, C15 and TEST2 govern this slice. C3 owns
syntax construction, spans, origin links, hygiene, metadata, fact invalidation,
serialization and verifier ordering. The existing Gravity syntax program and
reader authentication adapters retain their authority. Supplied operations are
trusted seed composition dependencies and cannot confer proof authority.

Inputs are decoded source, the explicit source/project context, reader
selection and invocation, authenticated reader products when internally
provided, and the existing owning operations. Outputs are the unchanged
`:gravity/stage0-c3-syntax-object-artifact`, syntax records, ledgers, verifier
report and serialization records. Stable C2/C3 diagnostic ids and source spans
remain with the existing diagnostic producers. The stage never loads or
resolves bootstrap vars; the bootstrap wrapper supplies every dependency.

## Development evidence

Before extraction, a genuine selected-current reader invocation for source
text `1` produced two syntax records, a passed syntax verifier, a complete
capability report and artifact identity
`sha256:c1e2b235f9efb1087cb1c8ddf2df2f53873853f0cd419b3034d466d11b643859`.
The full product was captured before editing for an independent before/after
comparison. A fresh JVM can load `gravity.syntax` without loading bootstrap.
Focused construction, rejection, tamper, composition and registration checks
will be recorded in this PR as the slice progresses.

## Remaining work and nonclaims

This is an incremental stage extraction, not completion of every syntax
implementation boundary. Existing syntax helper namespaces and SH04 Gravity
plan execution, digest/envelope binding and host adaptation remain. Their
remaining bootstrap-owned implementation must be accounted for before the
namespace migration can be called complete. The reader and diagnostic slices
also remain partial; other destinations in the namespace map remain open.

This slice adds no language features and grants no migration, proof,
self-hosting, seed-retirement, backend or release authority. Existing
authenticated products, caller-supplied input refusals, extension provenance
and deterministic identity must survive. No frozen compiler migration
fingerprint is renewed by this refactor.
