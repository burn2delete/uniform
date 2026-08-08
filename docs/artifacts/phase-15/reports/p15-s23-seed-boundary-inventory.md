# P15-S23 Seed-Boundary Inventory

Status: partial boundary accounting at `920723b26242eec749171c0216b96874df0502e2`.

This inventory describes the executable public `bin/gravity` path. It does not
promote source-model, replay, comparison, or attestation records into
self-hosting evidence. The formal-language completion count remains 0/240.

| Boundary | Concrete executable state | Removed from the public trusted boundary | Still trusted | Claim |
| --- | --- | --- | --- | --- |
| Compiler | `bin/gravity` selects the packaged JVM/Clojure entry point or `clojure -M:gravity`; reading, checking, and lowering execute in `gravity.bootstrap`. | Nothing in this slice. The bounded B3 lowering rules are Gravity-authored, but their plan is still executed and reconstructed by Clojure. | Clojure reader, checked-core and MIR orchestration, stage2 plan runner, JVM, Gravity C11/C13/C14/B1/B3 source rules. | Public compiler boundary unchanged and Clojure-seed-bound. |
| Evaluator/runtime | Public `run` calls the Clojure evaluator. The bounded LLVM gate runs one closed scalar Gravity program as a real ARM64 macOS executable. | Nothing on the public path. The internal gate proves native execution only for its explicitly bounded source surface. | Clojure public evaluator and stage2 runtime; Darwin process/runtime for the internal executable. | Internal execution capability advanced; public runtime boundary unchanged. |
| Verifier | Public `check` and all final P15/P18 claim checks execute in Clojure. Clojure reconstructs/replays C11, C13, C14, B1, B3, B13, B14, and C18 bindings in the LLVM gate before publication. | Nothing on the public path. | Clojure verifier and diagnostic projection, JVM, pinned Gravity rule sources. | Clojure-seed-bound. |
| Artifact construction | Clojure constructs manifests and sidecars and drives the bounded Clang transaction. | Nothing on the public path. | Clojure/JVM file APIs, Apple `xcrun`, Clang, linker, `file`, and `otool`. | Clojure-seed-bound. |
| Process and file I/O | The thin wrapper, Clojure `ProcessBuilder`, Java file APIs, and host shell perform all public and gate I/O. | Nothing. | Bash, Clojure/JVM, host filesystem, Apple toolchain. | Clojure-seed-bound. |
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

The smallest honest public reduction requires a Gravity-authored executable
driver/runtime that the tracked `bin/gravity` path actually invokes. It must
consume a source-bound compiler artifact, execute an accepted application, reject
malformed input with stable diagnostics, and record that the selected public
component no longer invokes Clojure. Until that component exists, replacing a
Clojure helper with another Gravity source model would not reduce the public
seed boundary.
