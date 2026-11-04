# Reader native validation ownership

## Purpose and governing contracts

This slice moves the existing ten-field native Reader report validation body
from component00827 into gravity.reader. D1/D2/D3/D8/D9, L1, C1/C2/C3/C15,
BOOT8 and TEST2 govern the pipeline boundary, ordered refusals, provenance and
development evidence. The source-artifact constructor and cache revalidation
retain their existing calls and authentication prerequisites.

## API, inputs, outputs and diagnostic ownership

The native API takes explicit late proof/failure/span owner Vars, source path
and the original artifact view. It obtains the same proof once, tests the ten
fields in their original order using native truthiness, and returns the original
complete marker when the failure owner returns normally. Missing-field ids,
source id/options, first-token-span preference, fallback span timing, repeated
failure behavior and exception identity remain unchanged. The complete marker
and proof labels are compatibility data, not authentication or authority.

The stage owns ordered rule selection and native diagnostic arguments; the
supplied proof and failure operations retain projection and diagnostic policy.
It imports no Bootstrap or downstream stage. The bootstrap wrapper keeps its
two-argument public arity, and existing proof/override/extension wrappers remain
exact. No source admission, SH03 resolution, product authentication, cache
reuse, identity algorithm, pin or canonical C2 construction moves.

## Verification obligations

Capture original behavior over genuine native compatibility products and their
native lexical/proof projections without entering canonical source admission.
Preserve full paths/text/hashes, identities and exact native refusal data.
Exercise each missing-field rule, simultaneous failures, truthiness, returning
and throwing failure owners, span fallback and late Var interposition. Run the
owning Reader and affected compatibility tests, cold stage construction,
language/ownership checks, lint, validators and independent adversarial review.
Maintain an early draft PR and finish with clean verified source. Fresh own CI
and exact current-main composition/preflight remain integration gates.

## Unsupported boundaries

This initial scope record has no implementation or executed-test credit.
Native compatibility views and passing markers cannot enter or replace C2/SH03
authentication, source grants, cache authority, canonical proof, migration,
release, self-hosting or seed retirement. Previously refused SourceC9/default
Reader operations and every held/restricted route remain untouched.

## Implemented native boundary and original control

gravity.reader/validate-native-reader-report! now owns the original rule loop
through three explicit late Vars. The wrapper keeps [source-path artifact].
Proof construction occurs once before the rule loop; failure lookup remains
before its diagnostic arguments and fallback span lookup. The surrounding
proof, override and extension wrappers and all source/cache admission paths
remain unchanged. No new callback or input-carrier admission check is added.

Original controls derive two native compatibility views from actual paired
surface-syntax files using real token/form records, syntax seeds, literal/trivia
records, lexical validation, source identities and incremental hashes. Both
return the original complete marker while their actual proof remains partial;
the view itself records admission:none and authenticates-inputs?:false. Five
altered views per source exercise source-id, literal, extension-policy, hash and
lexical refusal. Complete records retain actual paths, text and source hashes
under /tmp/uniform-reader-validation-before.edn, SHA-256
6aae3d0590d1e2b6f2cffebc93e0f4196ae1026530f56475d703cfff4a9bb149.
This is native compatibility evidence only, never a C2 admission artifact.

The first new field-rule test expected the raw token span, but the existing C2
diagnostic owner adds its source-id as the file field. Its failing source/log
remain retained; the test expectation now follows that unchanged owner output.
No production refusal, source pin or diagnostic construction was modified.
Post-extraction equality, final regressions, independent review and integration
checks remain outstanding at this source commit.
