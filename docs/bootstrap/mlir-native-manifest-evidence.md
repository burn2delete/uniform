# Native MLIR document data ownership

## Scope and governing contracts

This slice moves the existing fixed B7 MLIR document text, structural predicate,
manifest constructor, validation checks and coverage projection from the
Clojure bootstrap into `gravity.backend.mlir`. D1, D2, D3, D8, D9, B1, B7, B13
and B14 govern the architecture and evidence boundary. Draft PR #1260 was opened
at scope commit `46fd99f5b710c8836e0fadf6f1dfbc1cb5f4f333` before implementation.

The starting composition is `9becd646b508763878590f7a317b6b541a567fd9`, tree
`9c59d1ed94b1a060ef96ad777cf31411d72d4747`, with parents
`fb212c9cfd0a240d018439fa909751d7429c1071` and published main
`fa14c00518690feb113f9e506d3c8c0b8a96ebea`. The main import changes only the
two published Advance paths; all other source paths are conserved. That
composition has no hosted CI or integration credit yet.

## Public API and dependency direction

The stage owns fixed text emission, the existing substring predicate,
manifest data construction, existing structural refusals and coverage data.
Bootstrap names and arities remain compatibility adapters. Explicit late Var
ports retain the original callback and value lookup order. The stage must load
without `gravity.bootstrap`; Bootstrap consumes the stage, never the reverse.
The original failure publisher retains diagnostic construction and publication.
Caller input identities, source/debug maps, generated origins, hashes and
diagnostic fields must be conserved.

The five new APIs are `native-module-text []`,
`native-module-structurally-valid? [source]`,
`native-manifest [operations input-id]`,
`validate-native-manifest! [operations source-path artifact]` and
`native-coverage-report [operations artifact]`. They extend the four existing
diagnostic context lookups. Operations name the C4 hash callback, module text,
structural predicate, diagnostic IDs and failure publisher. Every new API's
metadata states its inputs, outputs and non-admitting boundary; the stage
requires only Clojure core and `clojure.string`.

## Verification and artifacts

Required evidence includes emitted `.mlir` bytes, deterministic fixed-fixture
identity, rejected malformed data with original B7 identifiers, complete old/new
return and refusal comparisons, callback interposition, cold stage loading,
source conservation, focused regression, language-boundary checks and
independent semantic, adversarial and composition review. No check is credited
before its actual result is recorded.

Development results on the implemented source:

- New fixed-document tests: 10 tests, 176 assertions passed. They emit a
  763-byte `target/validation/mlir-native-manifest/gravity_stage0.mlir` file,
  SHA256 `dc0514afcaa6a44b332111387e77fd36aceced2e71b8187c35b7a097cddb4a59`.
- Existing MLIR compatibility tests: 5 tests, 64 assertions passed both before
  and after extraction. Language-boundary and ownership checks: 48 tests,
  1202 assertions passed.
- Complete old/new raw and parsed data records match for 42 outcomes: 16
  returns and 26 refusals. All five original Bootstrap roots were rebound
  together from the immutable scope source. The comparison covers every
  structural condition, missing text tokens, legacy coverage, callback
  failures, late diagnostic IDs, late structural predicates and hash callback
  rebinding. Receipt `/tmp/uniform-mlir-manifest-parity.edn`, SHA256
  `9a0830ff99f9b16d2b11088544316cd2462c19210b006dfff65bb079a0b83636`.
- Cold stage load exposes nine APIs and loads no Bootstrap. Direct lint passed
  for the stage, new tests and changed CI tooling. The document and roadmap
  validators passed.
- The exact new CI command passed at 2g/180 seconds, with all 10 tests and
  176 assertions selected. The four-file source transformation matches the
  inspected proposal exactly; the original four diagnostic function bodies
  remain byte-identical. All 12 prior contract command vectors are unchanged.
  Conservation receipt `/tmp/uniform-mlir-manifest-conservation.edn`, SHA256
  `b5e5cc5adcbcd19c933e288e5bbf03064acd77f460947e9f6e9c11cf65afe46a`.
- The first harness invocation used an unsupported development-runner namespace
  and executed no tests. The direct harness then reported three missing
  `clj-kondo` executable errors. Both logs remain preserved. With the existing
  verified Kondo directory supplied on PATH, all 54 tests and 594 assertions
  passed; no harness policy or implementation changed to resolve the environment.

The CI catalog appends `mlir-manifest-native-validation`, using the complete
new namespace at the existing 2g/180-second native-check settings. Existing
command vectors, selectors, timeouts, workflow and source admission remain
unchanged. Exact committed-source review, full current-base lint context,
hosted CI and integration evidence remain pending.

The original conservative 13-path source lease also covered two unchanged
published-main Advance imports. After all local checks ended, the original
owner normally released that lease, verified row absence, and acquired a fresh
11-path source-only lease. The old token and evidence remain historical; no
foreign writer was cleared and no token was transferred.

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
