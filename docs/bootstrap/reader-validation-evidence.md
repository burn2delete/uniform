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
