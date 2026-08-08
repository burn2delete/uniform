# P15-S23 Seed-Boundary Inventory

Status: partial boundary accounting after the bounded public native-run admission, internal Darwin launcher-prerequisite, and bounded internal native runtime-provider slices.

This inventory describes the executable public `bin/gravity` path. It does not
promote source-model, replay, comparison, or attestation records into
self-hosting evidence. The formal-language completion count remains 0/240.

| Boundary | Concrete executable state | Removed from the public trusted boundary | Still trusted | Claim |
| --- | --- | --- | --- | --- |
| Compiler | `bin/gravity` selects the packaged JVM/Clojure entry point or `clojure -M:gravity`; reading, checking, and lowering execute in `gravity.bootstrap`. | Nothing in this slice. The bounded B3 lowering rules are Gravity-authored, but their plan is still executed and reconstructed by Clojure. | Clojure reader, checked-core and MIR orchestration, stage2 plan runner, JVM, Gravity C11/C13/C14/B1/B3 source rules. | Public compiler boundary unchanged and Clojure-seed-bound. |
| Evaluator/runtime | Default public `run` calls the Clojure evaluator. The explicit `run --target c --lowering runtime-derived` admission route still rejects with `P18T04002` / `:contained-public-native-run` before source or staging I/O. An internal ARM64 provider now executes the bounded Gravity-authored native runtime contract over content-bound packets without Clojure/JVM in the selected child. | Nothing on the public path. Only the selected internal runtime process records `:selected-runtime-clojure-seed-boundary? false`. | Clojure packet construction/verification and public evaluator; host-authored C provider; Darwin runtime. | Public runtime boundary unchanged and Clojure-seed-bound; internal runtime-only prerequisite advanced. |
| Verifier | Public `check` and all final P15/P18 claim checks execute in Clojure. Clojure reconstructs/replays C11, C13, C14, B1, B3, B13, B14, and C18 bindings in the LLVM gate before publication. | Nothing on the public path. | Clojure verifier and diagnostic projection, JVM, pinned Gravity rule sources. | Clojure-seed-bound. |
| Artifact construction | Clojure constructs manifests and sidecars and drives the bounded Clang transaction. | Nothing on the public path. | Clojure/JVM file APIs, Apple `xcrun`, Clang, linker, `file`, and `otool`. | Clojure-seed-bound. |
| Process and file I/O | The thin wrapper, Clojure `ProcessBuilder`, Java file APIs, and host shell perform all public and gate I/O. A new unwired host-authored C primitive verifies one suspended Darwin child's mapped vnode and removes live same-process-group members. It explicitly does not provide descriptor-relative execution or full-tree containment. | Nothing from the public path. | Bash, Clojure/JVM, host filesystem, Apple toolchain, Darwin private `libproc` APIs, and the host-authored C launcher. | Clojure-seed-bound; OS-contained descriptor-relative execution remains required. |
| Release wrapper | Tracked `bin/gravity` rejects seedless selection unless the final proof is complete and records `:clojure-seed-boundary? false`. That proof is currently incomplete/true. | Nothing. `bin/gravity-bootstrap` remains the explicit recovery entry point. | Bash selector, packaged Clojure classpath, JVM/Clojure entry point. | `:clojure-seed-boundary? true`, `:self-hosted? false`. |

## Bounded LLVM gate evidence

The integrated internal gate accepts verified C11 MIR, preserves the C13/C14,
B1, pass, fact, proof, target, source-path, and provenance closure, emits LLVM
for the pinned `arm64-apple-macosx14.0.0` target, invokes the real Apple Clang
toolchain, and runs the resulting Mach-O executable. It rejects unsupported MIR,
target leakage, evidence tampering, and unsafe publication before Clang or final
publication. Its records deliberately retain `:public-target? false`,
`:release-credit? false`, `:clojure-seed-boundary? true`, and `:self-hosted? false`.

Focused static verification at this revision:

```text
4 tests, 81 assertions, 0 failures, 0 errors
```

The real accepted Clang transaction and fail-closed pre-Clang rejections are the authoritative focused test,
not a generated `target/` or `validation/` directory:

```text
gravity.bootstrap-test/authenticated-llvm-arm64-macos-emits-runs-and-is-reproducible
gravity.bootstrap-test/authenticated-llvm-rejects-mir-target-and-evidence-tamper-before-clang
gravity.bootstrap-test/authenticated-llvm-output-is-transactional-and-fail-closed
```

## Next boundary-reduction prerequisite

The smallest honest public reduction requires an OS-contained,
descriptor-relative Gravity-authored executable driver/runtime that the tracked
`bin/gravity` path actually invokes. It must consume a source-bound compiler
artifact, execute accepted `.gravity` and `.qst` applications, reject malformed
input with stable diagnostics, and record that the selected public component no
longer invokes Clojure. The current explicit route proves this prerequisite by
rejecting before I/O when descriptor-relative executable selection and complete
process containment are unavailable; that fail-closed admission does not itself
reduce the seed boundary. Replacing a Clojure helper with another Gravity source
model would likewise not reduce the public seed boundary.

The internal Darwin launcher primitive closes only two narrower mechanics: it
binds the suspended child's executable mapping to the vnode opened before
pathname spawn, and it removes live members that remain in the dedicated
process group. Its partial proof is
`docs/artifacts/phase-15/native-launcher/p15-s23-darwin-launcher-primitive.edn`.
The component is host-authored C, unwired from `bin/gravity`, and explicitly
records descriptor-relative execution, same-euid mutation resistance, external
`SIGCONT` resistance, `setsid` containment, whole-tree reaping, code-signature
verification, public boundary reduction, self-hosting, and seedless release as
false.

A second internal prerequisite is
`docs/artifacts/phase-15/native-runtime/p15-s23-bounded-native-runtime-provider.edn`.
Its Gravity source owns the bounded packet/runtime semantics, while a
host-authored C provider executes the packet directly as ARM64 Mach-O with no
Clojure/JVM available to the child. Accepted `.gravity` and `.qst` provenance
and real output execute; malformed, overbound, invalid-UTF-8, noncanonical,
tampered, unsupported, stack, and halt cases reject with stable `P15NR*`
diagnostics. The provider only validates the declared source-hash shape; it
does not independently obtain the source bytes, authenticate the packet
issuer, or replace the Clojure compiler/verifier. It is not routed by
`bin/gravity`, so it reduces no public boundary.

Focused admission and safety evidence:

```text
19 tests, 147 assertions, 0 failures, 0 errors
bin/gravity run examples/hello.gravity --target c --lowering runtime-derived
exit 1; P18T04002; :missing-fact :contained-public-native-run
```

Focused native-runtime-provider evidence:

```text
gravity.p15-native-runtime-driver-test
9 tests, 234 assertions, 0 failures, 0 errors
```

The compiler, evaluator/runtime, verifier, artifact-construction, process/file
I/O, and release-wrapper boundaries all remain trusted Clojure/JVM or host
components. P15 seed retirement, P18 release readiness, and formal-language
completion do not advance.
