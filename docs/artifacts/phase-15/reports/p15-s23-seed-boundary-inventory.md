# P15-S23 Seed-Boundary Inventory

Status: partial boundary accounting after the bounded public native-run admission, internal Darwin launcher-prerequisite, bounded internal native runtime-provider, and authenticated packet-binding adapter slices.

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

## Completed internal packet-binding slice

The bounded internal slice is a Clojure-hosted adapter, not a new public
runtime or compiler boundary. It consumes the existing
target-neutral stage2 compiler packet and perform contextual authentication
before producing the native-provider wire:

- binds the packet to strictly decoded `.gravity` or `.qst` source text, its
  re-encoded UTF-8 hash, and the
  pinned Gravity rule identities;
- lowers only the supported scalar `str` and `println` operations into the
  existing provider wire;
- executes accepted co-canonical source extensions through that wire; and
- rejects packet tamper, source/context mismatch, pinned-rule tamper, and
  unsupported forms before a child is launched.

The supervised focused namespace passed 13 tests and 303 assertions. This
slice removes only hand-authored-payload evidence and establishes actual
packet consumption. It does not reduce the public or global Clojure boundary.
The provider remains host-authored C; compiler, verifier, adapter, artifact
construction, process/file I/O, and release-wrapper boundaries remain trusted.
The only selected-child reduction remains
`:selected-runtime-clojure-seed-boundary? false`; the public route remains
blocked by `P18T04002` / `:missing-fact :contained-public-native-run`, and
`:full-language-compiler-self-hosted?`, `:clojure-seed-retired?`,
`:clojure-seed-boundary?`, `:self-hosted?`, `:release-credit?`, and formal
language completion remain unchanged; no backend-boundary or backend-
conformance claim is added.
The proof record preserves the supervised run, its first bounded timeout, the
successful retry receipt, adapter/test hashes, and exact residual boundaries.

## Completed internal Gravity C-emitter semantic-owner sub-gate

The next internal slice consumes a real target-neutral stage2 packet and its
trusted context through
`gravity.p15-native-plan-specialization/specialize-native-runtime-plan`. It
authenticates the packet/context before plan traversal and invokes the existing
bounded C-plan validator. It then compiles and executes the pinned Gravity
helper `bootstrap/gravity/p15_s23/native_plan_c_emitter.gravity`; that helper
owns the printable-ASCII `println`/`str` plan walk and deterministic C-source
construction. The generic host-C packet interpreter is unused on this selected
evidence path. The production runner remains `:not-exposed`; compilation and
execution are test-owned in a private root and do not establish a public
process or file-I/O boundary.

Accepted evidence compiles and runs real ARM64 macOS C for
`accepted-print.gravity`, `accepted-print.qst`, and `accepted-str.gravity`,
with exact `Hello Gravity\n` and `name42\n` output. Packet/context tamper and
authenticated unsupported plans reject before the validator or emitter, with
`P15NS001` and `P15NS002`; the overbound tamper case is authenticated as
`P15NS001` and does not claim `P15NS003`. Validator-accepted boolean,
non-ASCII, control-string, and C11-trigraph inputs reach the Gravity helper and
reject with `P15GCE002`; helper-source substitution rejects with `P15GCE001`.
The final stable-input fail-fast run passed 6 exact vars and 89 assertions in
1594.489 seconds, peaked at 1,397,178,368 bytes RSS and three processes, matched all
tracked before/after input hashes, and had 0 failures/errors.

This advances only bounded C-source construction semantic ownership. The
generated child and selected runtime record no Clojure/JVM availability, but
authentication, plan validation, helper compilation/execution via stage0,
artifact construction, process/file I/O, compiler, public wrapper, and global
boundaries remain Clojure-seed-bound. The helper's `pr-str` primitive remains
an explicit host boundary. The provider and compiler are not
authored in Gravity, the production runner is not exposed, and the public
`P18T04002` / `:missing-fact :contained-public-native-run` admission block is
unchanged. Self-hosting, release, backend-complete, full-language, and 0/240
claims remain unchanged.

The proof is
`docs/artifacts/phase-15/native-runtime/p15-s23-native-plan-specialization.edn`,
with report
`docs/artifacts/phase-15/reports/p15-s23-native-plan-specialization-report.md`.
Its boundary fields retain `:selected-generated-child-clojure-seed-boundary?
false`, `:selected-generated-child-jvm-available? false`,
`:selected-runtime-clojure-seed-boundary? false`,
`:selected-child-clojure-seed-boundary? false`, while
`:compiler-clojure-seed-boundary?`, `:authentication-clojure-seed-boundary?`,
`:validator-clojure-seed-boundary?`, `:c-emitter-clojure-seed-boundary?`,
`:artifact-clojure-seed-boundary?`, `:artifact-construction-clojure-seed-boundary?`,
`:process-clojure-seed-boundary?`, `:file-io-clojure-seed-boundary?`,
`:public-clojure-seed-boundary?`, and `:global-clojure-seed-boundary?` remain
true. `:generic-host-c-packet-interpreter-used?` is false for this selected
path, not a public boundary-reduction claim.

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
