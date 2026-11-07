# Native backend manifest and structural checks

## Scope and governing contracts

This slice extracts three native backend data algorithms from the monolithic
Bootstrap into `gravity.lowering`. D1, D2,
D3, D6, D8, D9, C14, B1, B2, B3, B7, B13 and B14 govern its boundary.
The starting source is `d1f75b39c212bf80bc8c301c2bf8fdcda041a4c0`, tree
`15977c966be8acf0a49b1333365ac70e950fb0b5`; published main
`7204d9b0b9c80011e637f8456deec558301127c1` is already its ancestor.
Earlier slice results confer no acceptance on this successor.
The scope was committed before implementation as
`91a43690efad24d68531391778856f4ac61e158e`, followed by early draft PR #1275.

## Ownership and public API

Three complete existing bodies move from
`00209-native-lowering-source-overrides.clj`: content-hashed artifact manifest
construction, thirteen ordered native-backend structural checks, and coverage
projection. Their stage APIs are:

- `native-backend-artifact-manifest [operations backend kind content input-id evidence-id]`.
- `validate-native-backends! [operations source-path artifact]`.
- `native-backends-coverage-report [operations artifact]`.

Explicit late hash/failure operations and required-field/diagnostic value ports
preserve lookup, traversal, callback and exception behavior. Inputs are
unverified document data; outputs are the legacy manifest, structural completion
or refusal, and coverage record. The stage owns these construction/validation
decisions. Bootstrap retains diagnostic payload assembly and throw/publication
transport, outer source/file assembly, source admission and final artifact hash.
The previous fourteen Lowering APIs and all other stage bodies remain intact.
The stage requires Clojure core and `clojure.set`; Bootstrap consumes it through
the unchanged compatibility names/arities. `:must-not-require` records the
stage-to-Bootstrap dependency prohibition, not an admission mechanism.

The manifest constructor uses `:artifact-id` once at the original hash site.
Validation uses `:fail!`, `:manifest-required-fields` and `:diagnostic-ids`;
coverage uses the latter two. Each is a value or Var dereferenced at the
original use. The fields and diagnostic IDs are data, not supplier functions.
The earlier C14 `native-validate!`/`native-capability-proof` APIs retain their
different artifact shape and are neither replaced nor overloaded.

## Executed evidence

The new cold namespace passes 12 tests and 105 assertions. The five existing
data regressions pass 122 assertions; their original baseline was 5/113. Their
only edits reflect the three additional public APIs and declared `clojure.set`
dependency. The sixth existing current-Gravity-source compilation test is not
run for this slice; its source-emitter path is outside the reviewed direct-data
route. This selection confers no source compilation or admission evidence.

All three original Bootstrap roots are rebound together in one live comparison,
using the actual C4 identity operation. Complete raw and parsed records match
for all 58 outcomes: 35 returned and 23 threw the original structured/native
exceptions. The capture covers every guard, empty/nil/duplicate/wrong-count
manifests, wrong hash prefix and hash types, diagnostic and field drift, callback
return/throw order, late hash/failure/field/ID values and callee-before-subject
evaluation. Its receipt SHA256 is
`8db6f496349c0a1a4fcc6c6430037d48f9eca36f946461491a71ac96cec1400f`.

The comparison emits `manifest.edn`, 710 bytes, SHA256
`8212da488c1364d4d866e63116dcf12ca4769e7bd8481f36320fc196103bbfe6`.
Its content identity is
`sha256:d334210e2da8808e2cb995e8a59ce9d1271a99c3244fcd9c95d20211b2515601`.
That identity hashes the original C4 `pr-str` representation, not raw target
bytes. Input identity is borrowed at source/MIR/interface fields, evidence
identity remains in proofs, and pass/compiler/reproducibility fields survive.
This is an emitted document manifest, not a native target binary.

Language/ownership checks pass 48 tests and 1202 assertions, with zero skipped
namespaces. The CI harness passes 54 tests and 636 assertions. The nineteenth
contract command runs the whole cold namespace at the existing 2g heap and
180-second host watchdog and passes. Cold loading observes all seventeen public
APIs with Bootstrap unloaded. The conservation check confirms exactly ten owned
paths, unchanged previous fourteen API bodies and adapter prefix, twenty-two
protected paths and all eighteen prior command vectors. Its receipt SHA256 is
`837db0af4af46478d6aed0a8bdcaba29eb6bfdcf863ca9daed024862605ea247`.
The thirteen generated harness files were archived with hashes after terminal
completion. Documents (240 documents/19 indexes), roadmap, tools/ci formatting
and diff checks pass. Complete current-main context, independent final reviews,
provider composition and hosted CI are recorded in the maintained PR after
their execution; no pending result is inferred from prior slices.

The first native run had one test-fixture error (11 tests, 98 assertions,
zero failures and one error): a lazy required-fields fixture referred to its
redefined Var rather than capturing the original value. The corrected fixture
captures that original data and observes re-reading per manifest. The callback
selection probe additionally observes mutation during final subject lookup.
Production body transformations did not change to make these tests pass.
The initial failed log remains historical evidence.

## Preserved limitations

Validation does not require exactly three manifests; coverage does. Empty
manifest collections pass validation's vacuous predicates but report incomplete
emission. Three duplicate manifests can report complete emission, and a string
such as `sha256:not-a-digest` satisfies the prefix check. A missing MLIR conversion
status affects coverage but not the separate verifier-status guard. Nil and
non-string hashes retain their native host exceptions. Returning refusal
callbacks continue and return `:complete`; throwing callbacks retain identity.
These are native compatibility semantics, not authentic schema/proof validation.

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
