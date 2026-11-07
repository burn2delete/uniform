# Native backend manifest and structural checks

## Scope and governing contracts

This scope declares a next extraction from the monolithic Bootstrap into
`gravity.lowering`, before implementation and its early draft PR. D1, D2,
D3, D6, D8, D9, C14, B1, B2, B3, B7, B13 and B14 govern its boundary.
The starting source is `d1f75b39c212bf80bc8c301c2bf8fdcda041a4c0`, tree
`15977c966be8acf0a49b1333365ac70e950fb0b5`; published main
`7204d9b0b9c80011e637f8456deec558301127c1` is already its ancestor.
Earlier slice results confer no acceptance on this successor.

## Proposed ownership and API

Move three complete existing bodies from
`00209-native-lowering-source-overrides.clj`: content-hashed artifact manifest
construction, thirteen ordered native-backend structural checks, and coverage
projection. Their proposed stage APIs are:

- `native-backend-artifact-manifest [operations backend kind content input-id evidence-id]`.
- `validate-native-backends! [operations source-path artifact]`.
- `native-backends-coverage-report [operations artifact]`.

Explicit late hash/failure operations and required-field/diagnostic value ports
must preserve lookup, traversal, callback and exception behavior. Inputs are
unverified document data; outputs are the legacy manifest, structural completion
or refusal, and coverage record. The stage owns these construction/validation
decisions. Bootstrap retains diagnostic payload assembly and throw/publication
transport, outer source/file assembly, source admission and final artifact hash.
The previous fourteen Lowering APIs and all other stage bodies remain intact.

## Required evidence

Emit an actual serialized manifest with the existing C4 content identity, retain
borrowed input/evidence identity and provenance, reject real malformed records
through the original diagnostics, and compare complete original/current raw and
parsed outcomes with all three original roots rebound together. Test every guard,
late operations/value reads, callback order and exact thrown objects, native
host exceptions, deterministic identity, collection vacuity and count differences.
The stage and new native test must load without Bootstrap. Preserve all prior
CI command vectors and use proportionate regressions, boundary/ownership checks,
current-main context and independent semantic/adversarial/provider review.
Actual results will be recorded after execution.

## Unsupported boundaries

This slice does not admit verified MIR, authenticate backend-interface records,
implement canonical target/profile/ABI/provider/proof legality, compile native
code, run a target toolchain, or establish B1/B13/B14 conformance. Native complete,
proof, preservation and conformance labels remain unverified data. The existing
hash check recognizes only the `sha256:` prefix; it does not authenticate content.
Empty collections and returning failure callbacks retain their legacy behavior.

B1 and native/hosted/specialized source/file builders and their upstream held
C2-HASH route remain uninvoked. No Reader rebind/repin, substituted input, private
factory, native execution, source admission, release, migration, self-hosting or
seed-retirement authority is claimed. Hosted and specialized peers remain outside
this source slice.
