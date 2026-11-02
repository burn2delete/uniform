# Release file bytes and digest extraction

## Contract boundary

D1/D2/D3/D8/D9, B13, C15, PKG3/PKG7/PKG10/PKG12, BOOT8 and
TEST2 govern this slice. Move the existing p18-file-bytes, p18-bytes-sha256
and p18-file-sha256 bodies into gravity.release. The API consumes a caller-owned
path or byte array and two explicit late owner Vars, returning observed file
bytes, a hexadecimal digest, the existing prefixed digest or original host
failure. Directory creation remains existing seed orchestration.

The stage propagates host file errors unchanged and retains the historical
sha256:missing sentinel. That sentinel is not artifact verification. The caller
retains input admission, provenance, diagnostics, release policy, proof,
packaging, signing and publication. Dependency direction is JDK/clojure.string
to gravity.release to Bootstrap. No stage imports Bootstrap or discovers Vars.

## Baseline and acceptance obligations

Initial base is e766928c0ef6bb5c5b70b7e8feeae11e7b9814be, tree
54144b7b739ea81f8b029b3aec22f360720b1840. Before tracked edits a fresh
process executes exact clean main in the physical Analysis checkout and exits
before this document is written. Existing paired .gravity/.qst source compiles
through the permitted ordinary fixed seed to actual executable plan data.
The plans are written as real EDN files, read back and hashed by original
helpers. Both source spellings produce plan
sha256:e84fc441ab8e778d23056b3f42b24e45bacfab2fb10019bed9a56d30e77af1d6.
A missing file rejects with the real NoSuchFileException; the digest helper
independently retains sha256:missing. Empty/high-bit bytes and unchanged seed
directory results are captured as surrounding behavior.

Complete records and original serialization are retained at
/tmp/uniform-release-files-evidence. Compare full products after extraction;
recompute source/plan and actual emitted-file hashes. Preserve full production
files, wrapper arities, late owner lookup before argument evaluation, original
byte-array identity and dependency exceptions. Run focused allowed file/data
tests, language/ownership, contextual lint, validators, independent exact review,
own CI/artifact checks and immediate clean current-main preflight. Maintain an
early draft PR. Scope publication supplies no integration credit.

## Unsupported and unrun boundaries

These helpers do not authenticate paths, prove file ownership, reserve names,
prevent races, canonicalize manifests, validate archives, establish provenance,
verify signatures or issue certificates. Host paths remain actual development
inputs; byte hashes alone do not prove PKG7 reproducibility or PKG12 consumer
verification. No archived/release builder, native C driver/broker/Gate, default
pinned Reader, private authority or release verification/export route is run.
Existing release pipeline tests that execute package/archive builders remain
unrun; selected data/file tests must be identified exactly. No full artifact,
release, migration, self-hosting or seed-retirement claim is made.

## Implemented source and current-main evidence

Current base is 32fe90b1e6a41df5a4083d0abe3917e0cb2643e1, tree
b95a59281e71f741a91e6d93275fd055f8b6c962, including PR 778's shared macro
digests. Source composition c305998fcc94150303b5cca943d0505c0c9c3fb1 has
tree ce64d2967f4ab22a2a334139624af341d57bc0da. Incoming main changes no
Release production or pipeline-test file. Three bodies move; both complete
production files reconstruct from this immutable base. The legacy inventory
boundary identifier remains unchanged, with a separate additional file boundary.

Fresh before products execute exact clean main in the same Analysis checkout
and finish before restoring the source composition. Both complete after records
AND their original serialization match. Actual source and plan identities and
emitted file hashes recompute; plan files round-trip to the actual compiler
plan. Both file hashes remain
sha256:f795cce4d434fb126aae28ed403b71edcb42331fb89a5ea92db3d6a681c25252.
Full product digests are
6d59a40f2e2d0ded4cf7e1b72e246711db5a405ca1306e0ce7d1bfba8e0aed22
and ea43aa04cd7bd3219cc32dbed543b6134348e632f57ccaacc07959cb81619e6e.
Previous e766 products remain at
/tmp/uniform-release-files-evidence-e766-products-history.

A separate fresh original-helper probe runs on exact clean main 32fe in this
same checkout before restoring the extraction. The file-read callback replaces
the digest Var root while reading actual bytes. Original and extracted code
both invoke the digest root selected before argument evaluation. Complete probe
records and original serialization match. Explicit dereferences in the new API
preserve this ordering; callbacks are internal dependencies, never authority.

Seven selected tests pass 83 assertions: the existing namespace-boundary,
canonical-source-order, whole-file-inventory and expected-inventory-var data
tests, plus real-file-byte-and-digest-operations,
file-helper-wrapper-ports-and-byte-identity and
file-digest-preserves-operation-root-lookup-and-failure. The two package/archive
builder tests are unrun. Language/ownership passes 47 / 1154. Three-path
contextual lint against exact main has zero warnings and blockers, archived at
/tmp/uniform-release-files-lint-main32fe. Fresh load exposes five public APIs
without Bootstrap. Scripts are /tmp/uniform-release-files-{capture,extract,
verify,lookup}.clj; main32fe and boundary-preserved logs retain exact checks.

## Retained nonpasses

The first byte fixture used an incorrect expected digest literal; both original
baseline and implementations returned
89273d2f70b93285bb7ddb4bcee86a5347ca7159352e3cbdd20c23e9d1e507d3.
Only the fixture literal is corrected; the failed 7/82 run with two failures is
retained at /tmp/uniform-release-files-focused.log. An ownership check then
caught an unnecessary change to the existing inventory boundary identifier.
The original identifier is restored and the additional file boundary is explicit;
no ownership gate is changed. The failed boundary log remains at
/tmp/uniform-release-files-boundary-main32fe.log. The corrected focused and
boundary runs above pass.

Independent exact acceptance, candidate-owned CI/artifacts and immediate clean
current-main preflight remain required. These data/file checks do not authorize
any restricted package/archive/release or native route.
