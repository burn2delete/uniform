# JVM toolchain and output extraction

## Contract boundary

D1/D2/D3/D6/D8/D9, C14/C15, B1/B5/B13/B14, P4 and TEST2 govern
this slice. Move the existing twelve process, tool-version, file, stored-JAR,
classfile, hash, staging, compilation and atomic-commit bodies into
gravity.backend.jvm. Bootstrap retains input admission, native plan validation,
manifest/provenance construction and publication eligibility. Supplied
operations and data are internal compatibility ports, never authority.

The proposed public operations are run-process!, tool-version!, write-file!,
stored-jar-entry!, write-jar!, classfile-major, jar-record,
validate-content-hashes!, stage-output, compile-source!, class-jar-record and
commit-output!. Inputs are caller-owned commands, Java source, paths, existing
state and late function/data Vars. Outputs are process bytes, tool records,
class/JAR bytes and records, retained state, hash acceptance or original
diagnostics. Dependency direction remains clojure.string/JDK to
gravity.backend.jvm to Bootstrap. No dependency back into Bootstrap is allowed.

The stage owns B5-TARGET process/tool refusals, B5-MANIFEST class/JAR/commit
refusals, C14-INPUT staging failure and B13-HASH byte-integrity comparisons.
The existing diagnostic publisher and admission owners remain unchanged.

## Evidence obligations

Capture the complete existing products before extraction in this physical
checkout. Compile actual ordinary Gravity source through the existing fixed
seed plan emitter, generate Java, compile with the existing Java 21 flags,
write deterministic JARs and compare execution bytes. Preserve full source,
plan, source-map, tool versions, emitted bytes and diagnostic records, with
original serialization and recomputed hashes. Compare those complete products
after extraction. Opaque host state is tested for identity and preservation;
random staging names are host-local and receive no deterministic identity claim.

Exercise real source/plan rejection, malformed Java, unavailable tools,
classfile and artifact tampering, existing-file refusal and failed staging or
commit. Check compatibility signatures, exact body conservation, late Vars,
dynamic javac binding, operation order, cleanup and exception identity. Run
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
