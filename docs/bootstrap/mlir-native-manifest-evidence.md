# Native MLIR document data ownership

## Scope and governing contracts

This slice moves the existing fixed B7 MLIR document text, structural predicate,
manifest constructor, validation checks and coverage projection from the
Clojure bootstrap into `gravity.backend.mlir`. D1, D2, D3, D8, D9, B1, B7, B13
and B14 govern the architecture and evidence boundary. The implementation and
checks are pending in this early draft.

The starting composition is `9becd646b508763878590f7a317b6b541a567fd9`, tree
`9c59d1ed94b1a060ef96ad777cf31411d72d4747`, with parents
`fb212c9cfd0a240d018439fa909751d7429c1071` and published main
`fa14c00518690feb113f9e506d3c8c0b8a96ebea`. The main import changes only the
two published Advance paths; all other source paths are conserved. That
composition has no hosted CI or integration credit yet.

## Intended API and dependency direction

The stage will own fixed text emission, the existing substring predicate,
manifest data construction, existing structural refusals and coverage data.
Bootstrap names and arities remain compatibility adapters. Explicit late Var
ports retain the original callback and value lookup order. The stage must load
without `gravity.bootstrap`; Bootstrap consumes the stage, never the reverse.
The original failure publisher retains diagnostic construction and publication.
Caller input identities, source/debug maps, generated origins, hashes and
diagnostic fields must be conserved.

## Verification and artifacts

Required evidence includes emitted `.mlir` bytes, deterministic fixed-fixture
identity, rejected malformed data with original B7 identifiers, complete old/new
return and refusal comparisons, callback interposition, cold stage loading,
source conservation, focused regression, language-boundary checks and
independent semantic, adversarial and composition review. No check is credited
before its actual result is recorded.

## Unsupported boundaries

These are legacy document-data functions. Their labels such as `:complete`,
`:passed`, `:proven-safe` and capability-proof names do not authenticate inputs,
prove Gravity semantics or establish external MLIR verifier acceptance. The
substring predicate is not an MLIR parser or verifier. Existing gaps and vacuous
collection checks must be recorded honestly rather than treated as admission.

The upstream source-artifact path reaches the retained C2-HASH reader refusal
observed during the LLVM slice (expected source bytes 231576, observed 246369).
This slice does not retry, rebind, repin or route around that refusal, invoke a
source builder, or reconstruct an admitted artifact. Direct unverified data
fixtures cannot confer source-to-backend, verified MIR/domain input, B1/B7/B13/B14
conformance, downstream LLVM/GPU handoff, migration, release, self-hosting or
seed-retirement support. Existing source admission and artifact assembly remain
upstream.
