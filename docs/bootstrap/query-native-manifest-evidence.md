# Native query document data ownership

## Scope and governing contracts

The intended slice moves the remaining B11 document manifest constructor,
ordered validation checks and coverage projection into `gravity.backend.query`.
D1, D2, D3, D8, D9, B1, B11, B13 and B14 govern its architecture and evidence
boundary. This scope declaration precedes implementation and its early draft PR.

The starting source is `6ef91b069e870762c2ce22b00e32400528d16f92`, tree
`87847a27e56d357bc541ef2dd212023df5ea7d19`. Published main
`7204d9b0b9c80011e637f8456deec558301127c1` is already its ancestor.
Prior slice CI results confer no acceptance on this successor.

## Proposed ownership boundary

The stage already owns eleven native diagnostic context, catalog, failure,
override, fixed SQL/migration/adapter text and structural predicate APIs.
Three additional APIs will own manifest data construction, the existing 25
ordered shape refusals and coverage projection. Bootstrap names and arities
will remain adapters. Explicit late operations must preserve hash/content and
callback order, input identity, parameter spans, source/debug maps and
generated origins. The stage owns diagnostic payload shape; the caller owns
throw transport/publication, source admission and final artifact assembly/hash.
The stage must load without Bootstrap.

## Required evidence

The slice must emit the fixed SQL, migration and result-adapter files, reject
malformed document data with existing B11 identifiers, compare complete old/new
records, test late callback/value interposition, conserve unmoved source and
prior CI commands, pass focused/cold/boundary/ownership checks and receive
independent semantic, adversarial and provider review. Results receive credit
only after execution. No implementation or acceptance is claimed here.

## Unsupported boundaries

Legacy status, proof, dialect, schema, provider, transaction, capability and
taint labels are document data. Substring predicates do not parse SQL or verify
parameterization, result decoding or database behavior. No external connection,
query, migration or result adapter is executed by this increment.

The retained C2-HASH source-artifact refusal remains held. This slice will not
invoke source/file-artifact builders, retry or rebind the reader, repin source,
substitute an upstream result, or reconstruct an admitted artifact. No verified
relational/schema IR, source-to-backend compilation, semantic SQL generation,
database execution, transaction/capability/taint validation, dialect/type/null
equivalence, result decoding, migration safety, B1/B11/B13/B14 conformance,
release, self-hosting or seed-retirement support is claimed.
