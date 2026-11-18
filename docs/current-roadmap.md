# Gravity implementation roadmap

The Clojure/JVM bootstrap runs hosted source and emits checked scalar
JavaScript that runs independently in Node 20. Building the full compiler
from its own source on the staged Linux LLVM path remains open. Each
milestone exercises a real accepted program and a real rejection through the
path it claims.
[D2 milestone contract](phase-00-foundation-and-thesis/003-d2-implementation-roadmap-and-milestones.md)

## What runs today

Run these commands from the repository root. The hosted runs, C and JVM
artifact runs, checked JavaScript export, and `self-host verify` were
exercised with compiler source at `1c2ec05e5`.

| Capability | Command and observable result |
| --- | --- |
| Run a hosted hello program | `clojure -M:gravity run examples/hello.gravity` and the same command with `examples/hello.qst` each print `Hello Gravity`. |
| Run a hosted app with local calls, collections, and a branch | `clojure -M:gravity run examples/core-app.gravity` prints `core-app`, `gravity:19:2`, and `(:ok 19)` on separate lines. `clojure -M:gravity run-compiled examples/core-app.qst` prints the same lines through the bootstrap instruction-plan executor. |
| Reject an invalid hosted program | `clojure -M:gravity run bootstrap/clojure/fixtures/rejected/core-app-function-arity.gravity` exits 1 with `L2-FUNCTION-ARITY` for a call with too few arguments. |
| Emit and run a C executable | `clojure -M:gravity compile examples/hello.gravity --target c --lowering runtime-derived -o target/hello-c` uses the host C toolchain. `./target/hello-c` prints `Hello Gravity`. Compiling `examples/core-app.gravity` through the same route with a fresh output path exits 1 with `B2-UNSUPPORTED`. |
| Emit and run a JVM JAR | `clojure -M:gravity compile examples/hello.qst --target jvm --lowering runtime-derived -o target/hello-jvm` emits `program.jar` with class files, a manifest, a source map, and provenance. `java -jar target/hello-jvm/program.jar` prints `Hello Gravity`. |
| Compile and call a checked JavaScript export | `bin/gravity compile examples/checked-js/acceptance.gravity --target js -o target/acceptance-js` emits `program.mjs` and five companion files in a fresh directory. The [Node calls in the checked JavaScript guide](compiler-checked-js-modules.md#execute-the-exported-functions) print `10`, `7`, and `18014398509481987`. The [public CLI acceptance record](compiler-public-js-cli-receipt.edn) captured `C7-TYPE-MISMATCH` for a Boolean passed to an I64 parameter and `C8-UNDECLARED` when the source omitted `:error/raise`. |

The explicit runtime-derived C and JVM routes use the Stage2 plan emitter and
report `:development-unadmitted` revisions, unchecked runtime effects, and
`:clojure-seed-boundary? true`. The checked JavaScript route covers hosted/safe
I64 and Bool, checked addition, eager bindings, branches, and fixed-arity
nonrecursive calls. The emitted Node module accepts its own runtime inputs.
[Checked scalar JavaScript modules](compiler-checked-js-modules.md)

## What the compiler still needs to build itself

1. **Type-check the compiler's own frontend modules.** The
   `clojure -J-Xmx2g -M:compiler-probe` command feeds the six complete frontend
   source modules and their declared inputs through resolution, checked Core,
   Core verification, and function typing. The [archived November 13 run](../tools/seedless/evidence/2026-11-13/README.md#fresh-full-compiler-route)
   on `49eb3c580` reached verified Core for `:authenticated-envelope` and
   stopped at function typing with `C7-VERIFY` and
   `:map-keys-finite-keyword-map-type-required`. This result is bound to that
   revision. The [development workflow](development-verification.md#compiler-feedback-used-by-seedless)
   describes how to measure the frontier on current source.
2. **Build and run the compiler on the staged Linux LLVM path.** Carry the
   required source through checked effects, ownership, safety, MIR, and native
   emission. Include the runtime, standard library, and build inputs needed to
   rebuild the compiler. Then use the emitted compiler to rebuild the next
   stage and compare artifacts, program results, and feature diagnostics with
   reproducible provenance. [Self-hosting completion plan](self-hosting-milestone-completion-plan.md)
3. **Verify the seedless public release.** `clojure -M:gravity self-host verify`
   exits 1 with `P18T04007` and reports `:clojure-seed-boundary? true`. The
   [P15 seed-retirement proof](artifacts/phase-15/bootstrap/p15-s23-final-seed-retirement-proof.edn)
   remains incomplete. The [Phase 18 release requirements](phase-18-binary-distribution-and-seedless-release/README.md#phase-completion-standard)
   call for the rebuilt `gravity` executable to check, run, and compile the
   core app in both source extensions, reject invalid source with stable
   diagnostics, and carry reproducibility, provenance, SBOM, and signing
   records.

The [full-language gap map](full-language-implementation-gap-map.md) tracks
the wider conformance work. The [phase roadmap and evidence ledger](implementation-roadmap.md)
and [2026-08-08 capability audit](roadmap-capability-audit.md) retain their
original stage claims and fixture reachability results.
