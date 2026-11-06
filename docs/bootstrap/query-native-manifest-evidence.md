# Native query document data ownership

## Scope and governing contracts

This slice moves the remaining B11 document manifest constructor,
ordered validation checks and coverage projection into `gravity.backend.query`.
D1, D2, D3, D8, D9, B1, B11, B13 and B14 govern its architecture and evidence
boundary. Draft PR #1273 was opened at scope commit
`699073e4c056fa02a65763a8f35ff33013265603` before implementation.

The starting source is `6ef91b069e870762c2ce22b00e32400528d16f92`, tree
`87847a27e56d357bc541ef2dd212023df5ea7d19`. Published main
`7204d9b0b9c80011e637f8456deec558301127c1` is already its ancestor.
Prior slice CI results confer no acceptance on this successor.

## Public API and ownership boundary

The stage already owns eleven native diagnostic context, catalog, failure,
override, fixed SQL/migration/adapter text and structural predicate APIs.
Three additional APIs own manifest data construction, the existing 25
ordered shape refusals and coverage projection: `native-manifest [operations
source-path input-id]`, `validate-native-manifest! [operations source-path
artifact]` and `native-coverage-report [operations artifact]`. Bootstrap names
and arities remain adapters. Explicit late operations preserve hash/content and
callback order, input identity, parameter spans, source/debug maps and
generated origins. The stage owns diagnostic payload shape; the caller owns
throw transport/publication, source admission and final artifact assembly/hash.
The stage requires Clojure core and `clojure.string` and must load without
Bootstrap. The descriptive `:must-not-require` contract records forbidden
require edges while Bootstrap remains the consumer. This current boundary
supersedes the earlier context-only bookend's core-only dependency wording.

The constructor takes one `:artifact-id` callable and three fixture-value ports:
`:sql-statement`, `:migration-sql` and `:result-adapter-fixture`. Validation uses
`:fail!`, both structural predicate callables, SQL/adapter values and the
`:diagnostic-ids` sequence; coverage uses the same non-failure ports. Each port
may supply a Var, resolved at every original use. Hash callbacks run SQL,
migration, adapter, then content reads observe the current roots again. Both
validation and reachable coverage predicates run SQL then adapter. Adapter
coverage short-circuits when its record status is incomplete.

## Verification and artifacts

The cold tests emit fixed SQL, migration and result-adapter files under
`target/validation/query-native-manifest/`, reject malformed document data with
existing B11 identifiers, preserve borrowed identities and source/debug maps,
and exercise all 25 refusal steps, token policies, callback return/throw
identity, late Var interposition and callee-before-subject lookup. The supplied
SHA256 callback hashes only explicit unverified fixture data.

Development results on the implemented source:

- The combined cold and compatibility run passed 27 tests and 315 assertions.
  The existing compatibility baseline passed 16 tests and 143 assertions
  before extraction. The exact appended native command passed 11 tests and
  172 assertions at the existing 2g/180-second settings.
- Language-boundary and ownership checks passed 48 tests and 1202 assertions.
  The CI harness passed 54 tests and 622 assertions. CI formatting passed.
- A separate comparison using the original C4 operation and all three original
  Bootstrap roots rebound together matched complete raw and parsed records
  for 52 outcomes: 13 returns and 39 refusals. It covers all record guards,
  required/forbidden text tokens, callback return/throw, late diagnostic IDs,
  hash/value mutation, predicate interposition, conditional coverage and
  callee-before-subject lookup. Receipt `/tmp/uniform-query-manifest-parity.edn`,
  SHA256 `698c008bcbadde38589289c9ab4b246dfe78d1064949d5aad17fe39fe34fb25c`.
- The SQL is 35 bytes, SHA256
  `488f53e5a89c58015718421c74b313b367ccf31c2fb9c44819b9e63f1559213f`;
  migration SQL is 140 bytes, SHA256
  `1c8df61ae19adbcb9f36f2ec2754fc9f3f9806f738c866ad940259932ff76daa`;
  adapter text is 250 bytes, SHA256
  `2cab27ad7bbc56c6d3d0204f5cfc7e90456a98bbd8f94f353a076415fd509334`.
- A fresh JVM loaded the stage and native test namespace with fourteen public
  APIs and without Bootstrap. Three production transforms match the inspected
  proposal; the eleven prior API bodies remain byte-identical. Fourteen
  protected paths, the workflow and all sixteen prior contract command vectors
  are unchanged. Conservation receipt `/tmp/uniform-query-manifest-conservation.edn`,
  SHA256 `2dbee22f3d6051c03146b52efb1130f92f6d1ba230f14d8224c0af4ece1b843f`.

The CI catalog appends `query-manifest-native-validation` for the complete cold
namespace. Committed-source acceptance, current-base context, hosted CI and
integration remain separate evidence obligations. Results receive credit only
after actual execution.

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
