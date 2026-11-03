# PR621 imported-producer controls current port

PR621's original head is
`6c8d70290a52789ba57d5f7c95e5c700cc7ce89c`, tree
`f2b5fbcc0a7fa8b2d4ac70cccad72fe874f62f2d`, on parent
`3a1a553dbb1b3eb0ae472f5bf806171c02a71109`. Its authored delta was test and
report only: the complete imported-producer validation form, its 117-line
census, and its 61-line description. The original test exercised request
equality, active-cycle and producer-membership checks, malformed input shape,
4,097 request rows, and a 16,777,217-character finite DATA carrier.

## Current-main adaptation

This port starts at current main `7201ddcc82ab3bf778f65f9f1f6e6ac821632128`,
tree `b1703faa759a7bf0daa79b03258737d1dab80012`. Production remains byte
unchanged: the current C7 source is
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`,
SHA-256
`492a6076887c6c5be2bbd13520b065c1b9af8cf88c7ffa0f773039d4c64ca74f`.

The controls live in the new focused namespace
`gravity.self-hosting.sh08-imported-producer-controls-test`, so no active
writer is added to the shared SH08 test file. The current main kernel exposes
the four-argument `sh08-im-producers` path and no longer exposes the retired
`sh08-carrier-preflight` wrapper. The port therefore uses the current input
schema check for the finite DATA acceptance and the current producer path for
the authentication refusal; it does not restore an old wrapper or allowance
API.

The two tests retain all independent semantic cases:

- memoized request equality, substituted request and active-cycle refusal;
- retained producer membership, forged membership and duplicate producer
  refusal;
- schema/version/vector/extra-key rejection;
- acceptance of 4,097 request rows without a project-work quota; and
- acceptance of the finite 16,777,217-character DATA carrier by the direct
  original-data validator and input-shape check, followed by producer
  rejection, proving DATA is not producer or source authentication authority.

The focused port is 83 lines; its updated SHA-256 is recorded in the paired
EDN receipt. It passed 2 tests / 13 assertions with zero failures and errors. These are
normalized C7 kernel controls, not genuine authenticated source or public C7
acceptance. No production change, allowance replacement, source pin refresh,
native-C route, or main integration is claimed.

## Evidence boundary

The original PR621 source/report remain historical evidence. This current port
has a fresh source checkpoint and does not self-hash its report metadata.
Independent review, applicable composition, CI and integration preflight remain
separate. This port confers no whole imported-accounting, authenticated
producer, runtime, release, self-hosting or PR502-closure authority.
