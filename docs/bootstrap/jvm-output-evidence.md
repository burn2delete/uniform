# JVM output preflight extraction

## Contract boundary

D1/D2/D3/D6/D8/D9, C14/C15, B1/B5/B13/B14, P4 and TEST2 govern
this slice. Move the existing jvm-backend-preflight-output! body into
gravity.backend.jvm/preflight-output!. Explicit late path-policy and diagnostic
Vars preserve containment-before-IO, fresh-directory refusal, parent checks
and creation, original File values and exception behavior. Bootstrap retains
the shared path policy and diagnostic publication. Host path preflight is not
input, proof, capability or release admission.

Inputs are caller-owned output/source paths and the existing owner operations.
Output is the original output/parent File map or existing C14-INPUT diagnostic.
Dependency direction is JDK to gravity.backend.jvm to Bootstrap; the stage
cannot import Bootstrap, CLI or release. The existing JVM pipeline test owns
positive and adversarial regression coverage.

## Initial evidence

Initial base a958fd45c5bc2672fc0848be251722ec30e1a8a5 has tree
c9e23fe670827632abb4cf943a02063a3d3ad2fd. Before any tracked edits, a
fresh process ran on this exact clean main in the physical Runtime checkout.
The process finished before this scope document was written.

The original preflight created real parent directories below the actual host
temporary root and retained the expected java.io.File paths. Both existing
.gravity and .qst source fixtures compiled through the permitted ordinary
fixed seed to Java 21 classes and deterministic stored JARs, then executed.
The complete source, plan, source map, command, output, byte/hash and refusal
records are retained at /tmp/uniform-jvm-output-evidence/before*.edn.
The actual output root is recorded separately; it is not replaced by /tmp.
Existing artifact, outside-root and file-parent paths refused with their
original fresh-output-path, output-path-containment and
output-parent-directory facts.

## Acceptance obligations

Compare all complete serializable products and their original serialization
after extraction. Recompute source, plan and all five artifact byte hashes.
Observe File class/path separately; do not claim deterministic opaque object
identity. Reconstruct the full source files from immutable base. Check late
Var lookup, opaque result forwarding, failure identity, path-policy order,
real source/toolchain composition, language boundary and affected regressions.
Obtain independent exact-tuple acceptance, candidate-owned CI/artifacts and
immediate clean current-main preflight before normal PR integration.

## Unsupported boundaries

No manifest authentication, B1 verified-MIR admission, private compilation or
publication closure, broker, authority token, Gate A/B, restricted Reader or
release path is exercised or changed. The path check does not reserve a name,
prevent filesystem races, prove caller ownership or add symlink protection.
No native C execution, complete B5/B13/B14, full source mapping, performance,
migration, release, self-hosting or seed retirement is claimed.

The first temporary capture command selected the test runner alias instead of
script execution and rejected its arguments. No capture ran in that attempt;
the Clojure report remains at
/var/folders/3y/hpny2h8j4jn2qwyxxrzm3hb00000gn/T/clojure-17511274505986293471.edn.
The corrected script command explicitly supplies source/test paths without
runner main options. This is a harness correction, not a compiler change.

## Implemented composition and verification

Source composition 7474996862229389e629bb84ee873a750b1996cc has tree
e906d6f41c9cc2fcd6f3bdaa003eac6d2a50792d. One unchanged body and both
complete production files reconstruct from the immutable initial base, with
only the new name, two explicit operation parameters, compatibility forwarding
and namespace contract additions. The Bootstrap manifest body is untouched.
The original private output-tools caller still calls the compatibility API.

The existing paired source/toolchain regression now invokes output preflight
before writing Java files. Both fresh after products match the complete before
products and original serialization. Source and plan identities recompute,
as do all five actual artifact byte hashes. The source plan remains
sha256:e84fc441ab8e778d23056b3f42b24e45bacfab2fb10019bed9a56d30e77af1d6.
The .gravity product digest is
752f3c72b9317a0120c1acccf6e418b4d8efdbb9bf8343a6037f23e7c02f4262;
the .qst product digest is
0ca0270b8093fcae5220b0b13c357a3a27854f92cfa6c00c2c49762f674fc600.
No source field, actual root, tool version, mode or pin is normalized.

Focused JVM checks pass 11 tests / 207 assertions, including real parent
creation, all three path refusals, wrapper argument/opaque return forwarding,
policy-before-host-path use, late diagnostic replacement and original failure
identity. Language/ownership checks pass 47 / 1154. Contextual lint against
exact a958fd45 covers three source/test paths with zero warnings and blockers;
receipts are at /tmp/uniform-jvm-output-lint-maina958. A fresh JVM loads all
20 public APIs without Bootstrap. Document validation passes 240 documents / 19
indexes; roadmap and 185-workstream governance validation pass.

The first focused namespace command selected the self-hosting runner, which
does not own this backend namespace, and ran no test. Its report remains at
/var/folders/3y/hpny2h8j4jn2qwyxxrzm3hb00000gn/T/clojure-15164431913801616330.edn.
The documented development runner executes the backend's actual test namespace;
the incorrect command receives no test credit. No admission guard was changed.

Capture, whole-file reconstruction and identity checks are retained at
/tmp/uniform-jvm-output-{capture,extract,verify}.clj. Independent acceptance,
candidate-owned CI/artifacts and immediate exact-current-main clean preflight
remain required. This source record grants no integration or release authority.

## Current integration composition

Main PR 776 removes macro expansion quotas and makes current-source execution
explicit. Current base is e766928c0ef6bb5c5b70b7e8feeae11e7b9814be, tree
54144b7b739ea81f8b029b3aec22f360720b1840. Source composition
67c4d295d6545cd8a4cd8e854b9cd45ad5eeaa0e has tree
0947abcd90852f0b921ad7e4fcba26f4548fa9c3. No JVM production or pipeline
test file changes in this incoming main delta.

A fresh before process executes exact clean main in this same Runtime checkout,
without added baseline fixtures, and exits before restoring this extraction.
Both complete after records and original serialization agree again. Source,
plan and all five byte hashes recompute, with the same product digests recorded
above. Actual paths, modes, versions and source spelling remain unchanged.
Earlier records remain at /tmp/uniform-jvm-output-evidence-basea958-history.

Fresh focused JVM tests pass 11/207; language/ownership 47/1154. Three-path
contextual lint against this base has zero warnings and blockers; receipts
are archived at /tmp/uniform-jvm-output-lint-maine766. Both complete production
files reconstruct from this immutable base. Contract validators, renewed
independent acceptance, candidate-owned CI/artifacts and immediate exact clean
current-main preflight remain gates. Earlier 3e2faf06/basea958 acceptance and
CI are historical and confer no current integration credit. All unsupported
and restricted boundaries above remain in force.

## Combined integration candidate

PR 777 also incorporates the reviewed C compiler-call sequencing source from
PR 775. Source composition 4515de37520d9bb4726f7d820969b256f572b9d2 has
tree 7ec5a468530d12bde1c3c8e53d36df959401963d; base remains exact
main e766928c above. The normal merge retains both immutable source heads,
introduces no production edits and preserves both namespace-map rows. The
combined diff has nine paths, with five owned by each slice sharing the map.

Fresh JVM after products from this composition match both original exact-main
Runtime before products AND their serialization; source, plan and all five
artifact hashes recompute. The C slice separately takes a fresh exact-clean-main
before capture in this same Runtime checkout before composition. Its products
also match without changing actual source paths or normalizing provenance.

Combined affected backend tests pass 45 / 958, language/ownership 47 / 1154,
and six-path exact-base contextual lint has zero warnings and blockers. Both
whole-file reconstruction checks pass. Receipts are retained at
/tmp/uniform-backend-output-composition-lint-e766 and composition-named logs.
All existing unsupported and restricted boundaries remain. Independent review
must accept this exact union; candidate-owned CI/artifacts and immediate clean
current-main preflight remain mandatory. Neither separate source CI substitutes
for this integration composition.

## Current three-slice integration

Current base is 32fe90b1e6a41df5a4083d0abe3917e0cb2643e1, tree
b95a59281e71f741a91e6d93275fd055f8b6c962. Source union
36d24278fc7efae56fd1019673481ea560d9be1c, tree
81fef6b06f309aad1da0cd980c7d8f50a9bd516b, preserves reviewed C/JVM source
and adds the accepted Release file helpers from PR 779. No production edit
is introduced during composition; the shared map preserves all three rows.

Fresh original JVM before products run exact clean main 32fe in this same
Runtime checkout and exit before branch restoration. Complete after records
AND original serialization match again, with source/plan/five byte hashes
recomputed. Actual host root, command, source spelling and File observations
remain unchanged. Prior products are retained at
/tmp/uniform-jvm-output-evidence-e766-history. Backend tests pass 45/958,
selected Release data/file tests 7/83, language/ownership 47/1154 and nine-path
exact-base lint has zero warnings and blockers. All six whole production files
reconstruct under the three slice checks. Shared details and strict unrun
Release boundaries are in release-file-evidence.md. Exact union acceptance,
candidate-owned CI/artifacts and immediate clean current-main preflight remain
required; earlier base/source checks confer no current integration credit.

## Diagnostic-order integration correction

Source f3baca04a23fc0007cabe3c62d66fb0c372c4df9 corrects the C slice's
per-call diagnostic function lookup before cause-message evaluation. JVM and
Release production/test files are unchanged. Fresh JVM complete/raw products
still match the original clean-main 32fe Runtime captures, with all identities
and byte hashes recomputed. Backend regressions now pass 46/961, selected
Release tests 7/83, boundary 47/1154 and nine-path lint has zero blockers.
The C evidence records the retained adversarial nonpass and correction. The
corrected exact union requires renewed review and its own CI/artifact/preflight
gates; prior 90bfa54c checks and acceptance do not transfer.

## Current integration composition

Current base is 068321a889f4b4278c234a53d3f7a92d1300d22b, tree
744a2318c4627c4fc7fd29754a2e1e7244f3b896. Main PR 535 adds only 26
SH-08 regression-test lines. Source composition
56178be8b88f61408e4ccc0379862df6a1c305af has tree
6ee0db00ba5d9a33a358526235c972d5098e08d5 and retains the C diagnostic-order
correction. No incoming C/JVM/Release implementation changes occur.

Fresh original before processes for all three slices execute exact clean main
068 in this same Runtime checkout, without added fixtures, and exit before
source restoration. All complete after records AND original serialization agree;
source/plan/file hashes and the Release lookup probe recompute. Earlier c8
records remain in each slice's c8-history directory. No actual path, mode, pin
or artifact field is projected or normalized.

Fresh backend tests pass 46/961, the same seven allowed Release selectors
7/83 and language/ownership 47/1154. Six whole production files reconstruct,
including equivalent explicit diagnostic function retrieval and C type tags.
Nine-path contextual lint has zero warnings and blockers, archived at
/tmp/uniform-backend-release-068-lint. All existing native, authority, Reader,
package/archive and release restrictions remain. Renewed exact independent
acceptance, new candidate-owned CI/artifacts and immediate clean current-main
preflight remain required. Earlier source/base acceptance and CI do not transfer.

## Exception-data callee-order correction

Independent review found a second concrete lookup-order regression in the
already extracted validate-plan! adapter. ExceptionInfo.getData is called for
classification and again while constructing diagnostic arguments. An override
that changes the failure Var on the second read caused the stage to select the
replacement function, unlike the original named-call evaluation order.

The Bootstrap-free probe at /tmp/uniform-jvm-diagnostic-data-probe.clj requires
only gravity.backend.jvm and anonymous rejecting/failure callbacks. Before the
fix it selects :replacement and fails; afterward it selects :original. No
Bootstrap, file, process, factory or authority operation runs. The pre-extraction
source at f2bfea387b60077a64e0dcbf2573c6afad0126e9, component00772 lines
48-63, is inspected as source. An inline reference reproduces only its named
callee evaluation shape; the original Bootstrap function is not executed.
The coordinator's probe independently exercises the actual stage. Two initial
probe reader errors are retained in the reader-nonpass logs, followed by the
real semantic nonpass in /tmp/uniform-jvm-diagnostic-data-before.log.

Source 8ebf43808972c2994b91321d2f09a754e6a745cd, tree
de9d790ef133574f8f61eea75d2f97d2acb3b53f, selects the Var/function at the
one C14 diagnostic call site before argument evaluation. A new regression checks
replacement during the first classification read remains visible, while
replacement during the second argument-data read cannot change the selected
callee. JVM tests pass 12/213 and complete product/hash equality remains.

## Printer and package main composition

The exact-222 source composition, checks and unchanged restrictions are recorded
in [the C composition evidence](c-compile-sequencing-evidence.md#printer-and-package-main-composition).
Fresh paired before/after captures retain their actual host temporary directory
/var/folders/3y/hpny2h8j4jn2qwyxxrzm3hb00000gn/T/gravity-jvm-output-evidence-1246136878570217781.
Complete/raw record hashes are
798b46541e014e196e91c26bda470f0074bd2e546e50e1a6fc22eb4f37e291a3
for .gravity and
c0141e35374c76c54b3cd1510bde5cdee730dc6f9f9a78f30d69c1bfb17af5e0
for .qst. Source and plan identity and all five emitted-file hashes recompute;
real javac/JAR/java products and the existing source, path, integrity and
CREATE_NEW refusals remain. No private manifest admission or publication runs.

## Four-slice macro-main composition

The fresh exact-b30 composition is recorded in
[the shared composition evidence](c-compile-sequencing-evidence.md#four-slice-macro-main-composition).
Paired JVM records preserve actual host directory
/var/folders/3y/hpny2h8j4jn2qwyxxrzm3hb00000gn/T/gravity-jvm-output-evidence-5803085413034590558.
Complete/raw hashes are
8ff74603e299689142b7e0e48b57a8df5f39a03d8fc8dbd4a205d9daf3837a76
and 2e7b661cf982262b0e2321595d13bf7b9757bea5dd581df43192cf98438f7ac1.
Source/plan identity and five emitted hashes per spelling recompute; no actual
path, mode, pin, native authority record or file field is normalized.

## Five-slice metadata-main composition

The fresh exact-58dec composition is recorded in
[the shared evidence](c-compile-sequencing-evidence.md#five-slice-metadata-main-composition).
Actual paired host directory is
/var/folders/3y/hpny2h8j4jn2qwyxxrzm3hb00000gn/T/gravity-jvm-output-evidence-18139367169518934292.
Complete/raw hashes are
58d34b2af35d1beef96f2767dc4bc265113073c2cbc7e00095273274955a1145
and 6a441c5b30b02c2871355c9ec19bb5f72ea66ff279a1be546681aad48a63dd18.
Real Java/class/JAR execution, five byte hashes per spelling and existing
source/path/integrity/CREATE_NEW refusals remain. No private JVM admission runs.
