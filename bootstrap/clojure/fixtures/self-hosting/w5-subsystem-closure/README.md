# W5 Wave4 runtime, standard-library, and package-build subsystem closure

This fixture pair drives the Gravity-authored static verifier at
`bootstrap/gravity/src/gravity/self_hosting/w5_subsystem_closure_verifier.gravity`.
It consumes an exact request for the runtime, standard-library, and
package-build subsystems.  Each subsystem carries source, executable,
conformance, compiler, and provenance identities.  The request also carries
cross-subsystem linkage, accepted/rejected transcript descriptors, replay
metadata, build recipes, a hermetic environment descriptor, an offline lock,
toolchain identity, residual trusted components, source spans, and
path-bearing provenance.

Runtime, standard-library, and package-build identities are frozen per
subsystem. Replay transcript IDs, recipes, the deterministic environment,
offline lock, LLVM toolchain, and provenance records are bound back to those
owning subsystem records; locally well-formed substitutions therefore fail
closed. Source spans use nonnegative ordered bytes and positive line/column
coordinates. Every span uses the exact fixture source identity, binds its
actual path to the owning source path, and the top request declares whether
the fixture is `.gravity` or `.qst`; mismatched legal suffixes fail closed.

The linkage record has one exact verification identity,
`:subsystem-linkage-verification`. The singleton environment, lock, and
toolchain records have exact IDs `:hermetic-environment`, `:dependency-lock`,
and `:llvm-toolchain`; every build recipe and provenance record must carry
those same supplied singleton IDs. Coherent substitutions that change a
singleton and all dependent references remain rejected by the request-level
crosslink predicate.

The accepted pair is structurally valid but deliberately incomplete.  It is
accepted only when source, executable, and provenance roles are exactly
`:descriptor-only`, conformance is exactly `:pending`, and execution remains
`:pending`; cross-role, missing, extra, and unknown status values fail closed.
The compiler descriptor is exactly `:descriptor-only` and linkage is exactly
`:pending`. Replay is descriptor evidence only: both `:native-host?` and
`:emulator-authority?` are false, so no native or development-emulator
authority is claimed and no emulator is omitted from the exact residual TCB.
The result is always
`:closure-status :incomplete`, `:completion-status :blocked`, and
`:authority :non-authority`; it does not claim self-hosting, public authority,
release readiness, final subsystem hashes, or seed retirement.  Upstream W1-W4
identities and a target matrix are explicitly not provided, so no values are
inferred from them.

The candidate tuple is exact `:llvm-x86_64-linux` on Linux/x86_64 using LLVM,
ELF, and `:sysv-amd64`.  `(:target :jvm)` is only the stage2 seed-plan
harness declaration.  Darwin and Windows are unsupported in the ordered
vector `[:darwin :darwin-arm64 :darwin-x86_64 :windows]`; every unsupported
entry explicitly sets `:invokes-clojure? false`, `:links-jvm? false`, and
`:fallback? false`.  The candidate has no fallback and no cross-target
inference.

The rejected pair exports an executable mutator for each stable diagnostic
family: schema, target, runtime, standard-library, package-build, linkage,
compiler identity, transcript, replay, build recipe, environment, lock,
toolchain, exact role statuses, conformance, provenance, replay/emulator
authority, residual TCB, authority, upstream inference, source-span, and result
substitution.  Diagnostics carry a stable
`W5-SC-*` id, field, source span, remediation, target/profile facts, and
path-bearing provenance.

Semantic identity is path-neutral.  Checkout roots and actual artifact paths
appear only in `:provenance`; `:identity-input` contains the canonical
identity fields.  The `.gravity` and `.qst` files are co-canonical and must be
byte identical. Their constructor records the actual fixture extension;
source spans are cross-bound to source paths, compiler/subsystem sources end
in `.gravity`, provenance records cross-bind their owning paths, descriptor
evidence ends in `.edn`, and forged suffixes fail closed before an incomplete
record is accepted.

This slice is static-only.  No JVM, Clojure, native, container, toolchain,
network, signing, or release command is run while authoring it.  The later
focused command is:

```text
clojure -M:test --namespace gravity.self-hosting.w5-subsystem-closure-verifier-test
```
