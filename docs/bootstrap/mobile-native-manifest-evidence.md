# Native mobile document data ownership

## Scope and governing contracts

This slice moves the remaining B12 document manifest constructor,
ordered validation checks and coverage projection into `gravity.backend.mobile`.
D1, D2, D3, D8, D9, B1, B12, B13 and B14 govern its architecture and evidence
boundary. The original scope declaration was committed before implementation
as `50b61df82647edb0eaac9cf9a75c4e27bc4acdb0`; draft PR #1274 opened then.

The starting source is `52f6857e64ee1bc8922bc327108a3e14b30d0056`, tree
`c33c9e6756c397e06babe3cc1a85f117cb332d5b`. Published main
`7204d9b0b9c80011e637f8456deec558301127c1` is already its ancestor.
Prior slice CI results confer no acceptance on this successor.

## Ownership boundary and public API

The stage already owns ten native diagnostic context, catalog, failure,
override, fixed bundle/permission text and structural predicate APIs. Three
additional APIs own manifest data construction, the existing 24 ordered shape
refusals and coverage projection:

- `native-manifest [operations source-path input-id]`: borrowed unverified
  path/input identity and two fixed reference texts to legacy manifest data.
- `validate-native-manifest! [operations source-path artifact]`: unverified
  document data to legacy `:complete`, callback return or caller refusal.
- `native-coverage-report [operations artifact]`: unverified document data to
  the existing coverage record, including its unconditional `:complete` label.

`00264-b12-document-mobile-manifest.clj` and
`00265-b12-document-validate.clj` retain the Bootstrap names/arities as adapters.
The stage requires only Clojure core and string; Bootstrap consumes it. The
`:must-not-require` metadata describes the dependency direction; it is not a
runtime admission mechanism. The cold-load check observes no Bootstrap.

Construction resolves the `:artifact-id` callable twice, bundle then permission,
and rereads text at the original later content sites. Validation/coverage use
`:app-bundle`, `:permission-manifest-fixture`, both structural predicate ports,
and `:diagnostic-ids`; validation additionally uses `:fail!`. Diagnostic IDs are
a value sequence or Var, not a supplier function. Every Var is dereferenced at
its original use. Hash/content mismatch under interposition remains observable.

The stage owns the 24 existing validation decisions and native failure-payload
shape through its existing `fail!`. Bootstrap retains the caller-selected
failure operation, throw transport/publication, source admission, and final
artifact assembly/hash in the unchanged `00266` outer adapter. The unchanged
`00262` and `00263` retain their context, source and fixture compatibility roles.
All ten previous public API bodies remain byte-identical. Earlier native
diagnostic evidence's core-only dependency description is superseded here by
the actual core-and-string dependency; no earlier evidence gains new authority.

## Executed evidence

The cold native namespace has 11 tests and 167 assertions. Combined with the
unchanged compatibility namespace it passes 26 tests and 297 assertions; the
pre-extraction compatibility baseline passed 15 tests and 130 assertions.
Coverage includes all 24 guard positions, all eight required fixture tokens,
diagnostic ID/missing-field/source/subject order, returning and throwing failure
callbacks, exception identity, late operation/value changes, hash/content reads,
borrowed input identity, source locations and generated-origin chains.

The original three Bootstrap roots were rebound together in one comparison,
using the actual C4 artifact-id operation. All 49 outcomes (13 returned and 36
refused) match as complete raw and parsed records. The comparison emits:

| File | Bytes | SHA256 |
| --- | --- | --- |
| `GravityStage0.app/manifest.edn` | 143 | `4124e12cbc98bdbbe7e4ec68e037770a38893fd3a2a9b5597758b260015c2a43` |
| `gravity_stage0_permissions.edn` | 397 | `c6a3b098f102070ac5de06282b58c5cba99e6d991280809f8831cff884df8c35` |

The parity receipt SHA256 is
`aeeb4dac2bfe66e0675b495876567d9cb5177cf007eebb61097297a70cf6b776`.
These are fixed text artifacts, not a compiled or packaged application.

Boundary/ownership checks pass 48 tests and 1202 assertions. The CI harness
passes 54 tests and 629 assertions. The eighteenth contract command runs the
complete native namespace with the existing 2g heap and 180-second host
watchdog; its actual execution passes. All seventeen prior command vectors are
conserved. Documentation validation passes all 240 documents and 19 phase
indexes; roadmap, tools/ci formatting and diff checks pass. Isolated lint of
Bootstrap include files reports their expected unbound outer symbols; complete
current-main context lint is the relevant integration check.

The initial conservation check stopped because it ran before this scope
document had changed, seeing nine rather than ten changed paths. Its log is
retained. After recording this evidence, the final check passed with exactly
the ten owned paths, all fourteen protected paths unchanged, all prior public
API bodies unchanged and all prior command vectors conserved. The thirteen
generated CI-harness files were archived with hashes after the harness ended;
the successor conservation check verified that archive without replaying it.

Independent proposal review required explicit outer-adapter consumer metadata;
that path is now recorded. Final clean-commit semantic/adversarial reviews,
current-main context, provider composition, hosted CI and integration are
recorded in the maintained PR after their execution. This evidence alone does
not confer independent acceptance or integration authority.

## Preserved structural limitations

Validation checks presence and selected status fields, not authentic content.
Forged bundle/permission text in the manifest still passes when the independent
fixed reference roots match. An empty binding descriptor list is rejected by
the validator's `seq`, while empty coverage's `every?` is true. A nonempty list
containing an empty descriptor passes presence validation but has false binding
coverage. Returning failure callbacks continue through every guard and return
`:complete`; a throwing callback preserves the exact exception object. None of
these compatibility behaviors authenticate or admit a mobile program.

## Unsupported boundaries

Legacy iOS/Android, native symbol, binding, permission, lifecycle, thread,
storage, store-audit, proof and conformance labels are document data. Substring
predicates do not compile, package, sign or execute a mobile application, check
native classes/callbacks, grant OS permissions or verify lifecycle legality.

The retained C2-HASH source-artifact refusal remains held. This slice does not
invoke source/file-artifact builders, retry or rebind the reader, repin source,
substitute an upstream result, or reconstruct an admitted artifact. No verified
MIR/UI/platform IR, source-to-backend compilation, native bindings, platform
permission/thread/null/error/callback validation, package/signing/store
authority, device/simulator execution, B1/B12/B13/B14 conformance, release,
self-hosting or seed-retirement support is claimed.
