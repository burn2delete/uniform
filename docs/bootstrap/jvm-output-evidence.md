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
