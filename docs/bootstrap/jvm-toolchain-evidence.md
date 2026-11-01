# JVM toolchain and output extraction

## Contract boundary

D1/D2/D3/D6/D8/D9, C14/C15, B1/B5/B13/B14, P4 and TEST2 govern
this slice. Move the existing eight process, tool-version, file, stored-JAR,
classfile and hash bodies into gravity.backend.jvm. Bootstrap retains input admission, native plan validation,
manifest/provenance construction and publication eligibility. Supplied
operations and data are internal compatibility ports, never authority.

The public operations are run-process!, tool-version!, write-file!,
stored-jar-entry!, write-jar!, classfile-major, jar-record,
validate-content-hashes!. Inputs are caller-owned commands, Java source, paths and
late function/data Vars. Outputs are process bytes, tool records, class/JAR
bytes and records, hash acceptance or original diagnostics. Dependency direction remains clojure.string/JDK to
gravity.backend.jvm to Bootstrap. No dependency back into Bootstrap is allowed.

The stage owns B5-TARGET process/tool refusals and B13-HASH byte-integrity
comparisons. Class/JAR eligibility, staging, compilation sequencing and atomic
publication remain in the existing private Bootstrap closure.
The existing diagnostic publisher and admission owners remain unchanged.

## Evidence obligations

Capture the complete existing products before extraction in this physical
checkout. Compile actual ordinary Gravity source through the existing fixed
seed plan emitter, generate Java, compile with the existing Java 21 flags,
write deterministic JARs and compare execution bytes. Preserve full source,
plan, source-map, tool versions, emitted bytes and diagnostic records, with
original serialization and recomputed hashes. Compare those complete products
after extraction. No opaque host-state serialization or random staging-name
identity is claimed.

Exercise real source/plan rejection, malformed Java, unavailable tools,
classfile inspection, artifact tampering and existing-file refusal. Check
compatibility signatures, exact body conservation, late Vars, operation order, cleanup and exception identity. Run
focused regressions, language/ownership checks, contextual lint, contract
validators, independent acceptance, this PR's CI/artifact receipts and an exact
clean current-main integration preflight.

## Unsupported boundaries

This is an internal temporary Clojure extraction. It does not establish B1
verified MIR admission, full B5/B13/B14 conformance, full source maps,
null/interop/reflection/exception/resource policy, canonical Gravity backend
execution parity, release, migration, self-hosting or seed retirement. Empty
legacy hash manifests remain vacuously accepted by the helper and are not
complete artifact verification. The existing 60-second host process watchdog
is retained. Historical restricted native/Reader/closed-Core routes, pins and
guards are not exercised or changed.

Initial base is c4ae7a51fc815be31cb9636567c6c83a2d051309, tree
ef2dd8a2421439044aff2c1ed7c68a160f9059dd. Scope publication provides no
implementation, acceptance or integration credit.

## Implemented source and artifact evidence

Eight bodies now live in gravity.backend.jvm. Eight compatibility wrappers
retain their original names and arglists and pass eight late function/data Var arguments.
The entire stage and both complete consumer files reconstruct from immutable
base 96f4a3a with only these moves, explicit ports and contract additions.
All other Bootstrap/Gravity source, especially the private closure and input,
manifest and provenance owners, remains unchanged by this slice.

The native-source baseline is 63b62e58c2deeb0559140093462f4a6a651dac99,
tree 67e17a26a17c1c4b3732aad5423f31ecbee55859. The process capturing both
before records terminates before implementation edits. After captures run the working
extraction in the same physical checkout. Full EDN and original serialization
match for both real source spellings, including source/plan/emitter, partial
source map, actual tool versions, Java/class/JAR byte arrays, all five byte
hashes, process output and four complete refusal records. Source and compiler
plan identities and all emitted byte hashes recompute. Both source spellings
produce plan sha256:e84fc441ab8e778d23056b3f42b24e45bacfab2fb10019bed9a56d30e77af1d6.

The installed javac/java are 26.0.1; unchanged --release 21 flags produce major 65
classes. Repeated stored JAR bytes agree and real execution prints the expected
24 bytes, including nil's existing empty output. A real vector source plan
rejects C14-UNSUPPORTED; malformed Java rejects B5-TARGET with real javac stderr;
altered class bytes reject B13-HASH; existing-file creation fails with the
original FileAlreadyExistsException. An altered major is reported as 64 by the
header inspector; that helper does not itself confer class eligibility.
These are internal output controls, not complete backend admission or
canonical execution parity evidence.

Scripts and complete captures are retained under
/tmp/uniform-jvm-toolchain-{capture,verify,extract}.clj and
/tmp/uniform-jvm-toolchain-evidence. Source reconstruction covers all three
production files; repeated source captures after incorporating main 44f0955
still match the original complete records and serialization. That main update
changes release/test infrastructure, not the moved source bodies.

## Retained local nonpasses and review gates

The initial twelve-body scope included four private helpers. Ordinary Bootstrap
loading captures them in a closure and removes their Vars, so the first test
harness could not reference the compilation helper. That harness is discarded;
no private loader, admission route or source projection is used to obtain
access. The slice is narrowed to eight public compatibility bodies. Early
harness loading/read failures and a verifier comparison of distinct regex
objects are retained as local nonpasses; the corrected verifier compares complete source
reconstruction and printed forms.

Initial contextual lint found the test's missing explicit clojure.java.io
require. Adding that dependency yields zero warnings and zero blockers across
four changed Clojure paths. A later concurrent capture exposed an invalid test
assumption about all global temp files. The corrected cleanup test records only
the stderr File actually passed to its own process read, then checks deletion
on success and failure. Whole-body conservation separately preserves both
original cleanup calls and the host watchdog. No production behavior changes
are made for these test or evidence repairs.

Focused and affected backend tests initially pass 31/627; language/ownership
pass 47/1154. Cold loading the 19-operation stage does not load Bootstrap.
Canonical docs 240/19, roadmap and governance 185 validations pass. Final focused
checks after the cleanup-test correction, independent acceptance, this PR's
own CI/artifacts and an immediate exact clean current-main preflight remain
required before integration. No historical CI or acceptance is transferred.

## Earlier local composition

The integration base is 44f095500595320552db1f1b632faac39906c17c,
tree 5c7165578211835948084c04ff13e7b682a027be. Corrected source checkpoint
e0b095cab229bfd0890bc5a0f550636b8c7d66c0 has tree
8cc945d7d342cc37ed3b118edc07bde492780172. Fresh affected backend tests pass
31 tests / 630 assertions. Contextual lint at that committed checkpoint against
this exact base covers four paths with zero warnings and zero blockers; receipts
are archived at /tmp/uniform-jvm-toolchain-lint-final-main44. Source
conservation and document validation pass again. Product equality and original
serialization still match both original captures with all five byte hashes
recomputed. Independent corrected-candidate acceptance, own CI artifact
verification and immediate clean current-main preflight remain integration
gates, recorded on the PR rather than inferred from these local results.

A final source audit found that the extraction printer had omitted the two
original Java type hints (ProcessBuilder's List argument and the JAR entry
receiver). Both hints are restored without changing expressions. The immutable
source verifier now independently compares all type tags as well as complete
reconstructed text and printed forms. Earlier candidate acceptance and CI are
not transferred to this source correction; fresh products, focused checks,
independent exact acceptance and candidate-owned CI are required.

## Packaged-CLI reconciliation

Main advanced through PR 768 while the corrected candidate CI was running.
The new integration base is 19d9426133c041088e2a11e2cf7c85d359a09630,
tree 92d4442da6895feeb1aa065d4a26cbec1311af2b. Source composition
18f90ec16f5dda2adbabc1c45cc64dff6db0d14a has tree
c0f477a43b6c408fc08465aacfbe2f9c54b8b175. The main delta changes packaged CLI
help/version and their tests; all three JVM production files are unchanged in
that delta. Fresh complete products, affected backend and packaged-launcher
checks, language/ownership, lint, independent acceptance and own CI bind this
new composition. The earlier base/candidate acceptance and CI remain historical
and confer no integration credit on the reconciled candidate.

## Current integration composition and fresh native baseline

Main PR 772 removes cache and pass-identity work quotas. The current integration
base is 2c8d01e9ef3ebb27497f50708fee44ab0f8a23fe, tree
9b9437d6e643faef4393cf191bd4a9ee4a94549d. Source composition
d3f891436134c444aceb3e2221af1604855de9aa has tree
88acd47f4eaa2c15a9022c18814a729d9b69752b.

A fresh native baseline, 1186d30831987f8cf480967b51426e0786d0061e
(tree 22bb18b977d9feda23acf317e74eb5c9e26e9860), adds only the four
fixtures and original capture-test file to this exact main base. It runs the
original eight JVM bodies. Its capture process terminates before switching
this same physical checkout back to the extracted composition. Both full
before/after records and original serialization agree again; source/plan
identities and all five emitted byte hashes recompute. No field, root, pin,
mode or authority callback is substituted. Earlier records are preserved at
/tmp/uniform-jvm-toolchain-evidence-base19-history; current records retain
/tmp/uniform-jvm-toolchain-evidence and main2c script/log names.

Fresh affected backend tests pass 31/630, isolated packaged-launcher tests
2/17 and language/ownership 47/1154. Contextual lint against this base covers
four paths with zero warnings and zero blockers; its receipts are archived at
/tmp/uniform-jvm-toolchain-lint-main2c. Independent acceptance, own CI/artifact
receipts and immediate exact clean current-main preflight must bind this
composition. Earlier base/candidate acceptance and CI remain historical.
