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
