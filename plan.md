# Full Self-Hosting Roadmap

## Objective

Iterate from the current bounded reader slice to a feature-complete, self-hosted Gravity compiler that can compile to every supported target with reproducible proof.

## Current execution

- [completed] Establish baseline: repository state, canon, capability matrix, toolchain, and host-retirement blockers.
- [completed] Implement a source-derived hosted C target for the verified core instruction subset.
- [completed] Run independent adversarial review and required validators.
- [completed] Integrate only reviewed additive commits and refresh the baseline.
- [completed] Route the explicit C target through the public compile boundary.
- [completed] Add an opt-in runtime-derived C lowering subset with fail-closed value semantics.
- [completed] Expose runtime-derived lowering through the public compile boundary with parity and fail-closed option proof.
- [completed] Expand runtime-derived C lowering to scalar locals, conditionals, and lexical lets with fail-closed validation.
- [completed] Correct executable-symbol analysis so quoted data no longer blocks the Gravity-authored P15 compiler source.
- [completed] Add fail-closed runtime-derived `str` concatenation for direct println byte-string values.
- [completed] Bind runtime-derived C lowering to the Gravity-authored P15-S23 stage2 plan-emitter rules.
- [completed] Bind runtime-derived C execution to the Gravity-authored P15-S23 stage2 runtime executor/kernel contracts with fail-closed parity checks.
- [completed] Bind the P15-S23 stage2 source-front-end ingress to authoritative C2/C3 reader products, preserving rich reader identity and diagnostics.
- [completed] Bind the public runtime-derived C route through the Gravity-authored P15-S23 stage2 compiler-driver contract with fail-closed execution-equivalence checks.
- [completed] Bind a Gravity-authored stage2 runtime artifact for value formatting, with pinned function shape, path-neutral identity, and fail-closed tamper handling.
- [completed] Route two-argument `str` through the Gravity-authored runtime artifact, with explicit arity rejection and residual bridge provenance.
- [completed] Route single-argument `println` through the Gravity-authored runtime artifact, preserving the multi-argument host boundary and IO capability contract.
- [completed] Route two-argument `println` through the Gravity-authored runtime artifact, preserving exact space/newline semantics and a host boundary only above two arguments.
- [completed] Add a public Node 20 ES2022 ESM target over the authenticated target-neutral stage2 packet, with byte-exact C/runtime differential execution and transactional artifacts.
- [completed] Move production stage2 collection/let/expression/function lowering behind a pinned Gravity-authored `:meta` emitter artifact, retaining Clojure only as the explicit seed compiler/runner boundary.
- [completed] Add an opt-in Java 21 modular executable JAR target with classfile-65, deterministic packaging, authenticated packet/manifest closure, and real `java -jar` differential execution.
- [completed] Move authoritative stage2 function/binding/summary/entrypoint plan assembly into the pinned Gravity emitter artifact with bounded-depth traversal and cross-target authenticity.
- [completed] Execute the bounded closed stage2 plan through a pinned Gravity-authored runtime function, with trusted-source replay, variadic println, bounded validation, and authenticated C/Node/JVM target records.
- [completed] Build and independently verify an authenticated pure closed checked-core artifact for literal, quote, local, do, if, and let, with canonical C6-C10 pass artifacts and an exact plan-to-C2/C3 origin sidecar.
- [completed] Repair the pinned Gravity runtime module's allocation, failure, effect, capability, and provider contract, then extend checked-core admission to `str` and `println` only after the C8/R11 gates pass.
- [completed] Consume only the resulting verified checked-core artifact in a pinned Gravity-authored C11 MIR builder, preserving real operands/results/CFG/definitions/uses and recomputing verifier facts.
- [completed] Consume only that verified MIR in a bounded internal executable LLVM slice through the pinned ARM64 macOS target and real Clang execution, with standalone accepted/rejected co-canonical fixtures and fail-closed publication.
- [completed] Add and independently review an explicit public native-run admission boundary; keep it fail-closed before source or staging I/O because the current Java host cannot prove descriptor-relative executable selection or OS-level process-tree containment.
- [completed] Add and independently review a bounded Darwin host-launcher primitive that verifies the suspended child's mapped executable vnode and removes live members of its dedicated process group, while explicitly declining descriptor-relative, full-tree, public, self-hosting, and release claims.
- [pending] Build an OS-contained, descriptor-relative Gravity-authored executable driver/runtime that the tracked public `bin/gravity` path invokes, then retire only the public component boundary it actually replaces.

## Baseline snapshot (2026-08-08)

- Parent implementation baseline for the authenticated C11 candidate: `6410068` (`Authenticate checked-core reference runtime replay`).
- Verified integration baseline for this slice: `920723b26242eec749171c0216b96874df0502e2`; generated `target/` and `validation/` directories are in-flight evidence only and are not completion authority.
- Current code proves a genuine lexical/C2/C3/P15 reader slice; C2/C3/P15 remain partial and FL-P01-T01 remains unchecked.
- `GRAVITY_BOOTSTRAP_ONLY=1` checks and runs `examples/core-app.gravity` and `.qst` with equivalent output.
- `bootstrap/gravity/p15_s23/compiler.gravity` passes the bootstrap-only public check after executable-symbol analysis was corrected; whole-language self-hosting remains partial.
- The public wrapper and packaged/release artifacts remain bootstrap-hosted; final seed proof is incomplete and seed boundary remains true.
- Current roadmap audit records 0/240 full-language documents complete, 7 without an executable owner, public `check` accepting 74/196 accepted fixtures, and 664/1,720 rejected fixtures with specific public diagnostics.
- Host Java is OpenJDK 26.0.1; system `clojure` is absent, so baseline probes use the bundled temporary launcher at `/tmp/gravity-clojure-runtime/bin`.
- The new C target is real and source-derived from the verified stage0 plan, but remains Clojure-seed-bound (`:clojure-seed-boundary? true`) and internal; it does not close public seedless release.
- Explicit `gravity compile --target c -o ...` now routes to that backend; the default packaged/JVM compile path remains unchanged.
- Runtime-derived C lowering is now an explicit public option (`--target c --lowering runtime-derived`); scalar literal/quote/println/do semantics execute in generated C, while unsupported value positions fail closed. The Clojure evaluator remains a non-authoritative parity oracle.
- The public runtime-derived slice proves both co-canonical extensions, deterministic identity, UTF-8/NUL output, unrelated-CWD routing, option diagnostics, and preserved default/JVM behavior. The seed boundary remains explicit.
- Runtime-derived control flow now executes scalar locals, `if`, and `let` in generated C, including zero-length values; malformed, deep, collection, call, and unsupported value forms reject with structured B2 diagnostics.
- Qualified-symbol analysis now skips quoted payloads while preserving executable alias rejection; `GRAVITY_BOOTSTRAP_ONLY=1 bin/gravity check bootstrap/gravity/p15_s23/compiler.gravity` passes, but the whole-language self-hosting gate remains partial.
- Runtime-derived C now supports direct `str` concatenation over byte-string literals, quotes, and proven string locals, preserving empty/NUL/UTF-8 bytes; numeric, collection, nested, and general-value uses reject closed with B2 diagnostics.
- Runtime-derived C plans now come from the Gravity-authored `p15-s23-stage2-plan-emitter` rule set, with path-neutral rule hashes and compiler-stage/engine provenance; the Clojure instruction runner remains an explicit oracle and seed boundary.
- Runtime-derived C execution now binds to the Gravity-authored `p15-s23-stage2-runtime-executor` and `p15-s23-stage2-runtime-kernel` contracts, compares stage2 output with the Clojure stage0 runner and generated C executable, and fails closed on missing, malformed, or mismatched runtime bindings. The executor implementation is still the Clojure seed and is explicitly marked comparison-only; no self-hosting claim is made.
- Stage2 source-front-end ingress now consumes the authoritative C3-to-C2 reader products rather than the legacy simplified P15 parser, preserving token/form IDs, parent graphs, Unicode/line-ending spans, metadata/abbreviations/deref/tagged literals, deferred ratios, and structured reader diagnostics for both co-canonical extensions. The Clojure verifier/compiler seed remains explicit.
- The public runtime-derived C route now loads and validates the Gravity-authored stage2 compiler-driver contract, runs the declared driver pipeline, compares driver-emitted plans and runtime records against the bound products, emits deterministic driver hashes/provenance, and fails closed on missing, malformed, mismatched, or incomplete driver results. The stage2 driver still executes through Clojure-hosted implementations and is not self-hosted.
- A Gravity-authored `bootstrap/gravity/p15_s23/runtime.gravity` artifact now supplies the runtime value-formatting function used by runtime-derived `println`; it is compiled through the stage2 plan emitter, invoked through a generic host bridge whose residual boundary is explicit, and protected by pinned function-shape, source/hash, cross-root, and semantic-tamper checks. This is one runtime capability, not a self-hosted runtime.
- Two-argument `str` now routes through a second Gravity-authored runtime function with pinned shape and explicit arity rejection for unsupported forms; the generic bridge remains recorded as residual and the broader Clojure runtime executor is still trusted.
- Single- and two-argument named `println` helpers remain pinned Gravity-authored functions with exact `:io/write`/`:io/stdout` validation; the newer authenticated closed-plan executor owns bounded variadic printing without changing the legacy helper-boundary record.
- Variadic `println` now executes through the Gravity closed-plan output accumulator and preserves exact ordered spaces/newlines in C, Node, and JVM products under the shared 128-node/depth bound.
- Public `--target js` and `--target js-ts` now compile the bounded stage2 subset to executable Node 20 ES2022 ESM. The JS and runtime-derived C routes share an authenticated target-neutral stage2 packet; Node execution is byte-compared with the authoritative stage2 runtime and executed C behavior. Emission is an atomic artifact directory containing `program.mjs`, declarations, source map, package metadata, manifest, and provenance with explicit UTF-8 digest proof.
- The JS slice remains non-release and non-conforming for full B6: its input is the bounded stage2 packet rather than verified MIR/domain IR, source maps are source-unit-only, the TypeScript API is empty and unverified by `tsc`, and the Clojure seed boundary remains true.
- Production stage2 collection, let, expression, and function lowering now invoke `bootstrap/gravity/p15_s23/emitter.gravity`. Exact source, public function shape, and canonical semantic-plan hashes are pinned; missing or tampered artifacts fail before C/JS output. The prior Clojure lowering functions remain only as seed helpers used to compile and verify the artifact, and the generic Clojure runner remains an explicit residual.
- Opt-in `--target jvm --lowering runtime-derived` now emits Java 21 sources, classfile-major-65 named-module classes, and a deterministic modular executable JAR, then proves byte-exact output with `java -jar` before atomic publication. The authenticated JVM manifest binds source, plan, emitter, compiler, driver, runtime, effects, and capabilities; the legacy/default JVM route remains unchanged.
- The Gravity emitter now owns authoritative function compilation orchestration, ordered binding products, instruction/effect summaries, main validation, and plan products. Traversal is divide-and-conquer for large functions, bodies, maps, and lets; 2,049 functions and 4,096 flat forms pass without host-stack failure. C, Node, and JVM authenticate the derived compiler-artifact identity against pinned source and semantic hashes before lowering.
- `bootstrap/gravity/p15_s23/runtime.gravity` now owns the bounded closed-plan interpreter for literal/quote/local/`str`/`println`/`do`/`if`/`let`. Consumers independently recompile trusted source, reload pinned emitter/driver/runtime rules, replay the Gravity executor, and bind finalized target manifests to plan/entrypoint/stdout context. Validation is iterative and capped at 128 nodes/depth; authoritative execution and consumer verification replay are recorded separately. The Clojure seed still compiles and generically runs the Gravity functions, MIR remains absent, and self-host/release claims remain false.
- An authenticated pure checked-core boundary now consumes genuine C2/C3 products for literal, quote, local, `do`, `if`, and `let`; emits canonical C6-C10 envelopes, fact tables, proofs, ownership, and exact origin sidecars; separates path-neutral semantic identity from actual-path provenance; and fails closed on malformed modules, quoted-value/type gaps, effects, capabilities, hostile bounds, opaque authority, and coherent tampering before packet execution. Independent final-hash reviews and the focused, historical, hostile, routing, compatibility, and repository-validator gates pass. This is pre-MIR and does not credit effectful core, target lowering, whole-language support, self-hosting, or release readiness.

## Active slice

- Owner: master coordinator; the current patch is restricted to an internal Darwin host-launcher prerequisite, focused native tests, one partial proof record, and seed-boundary accounting. It does not edit `bin/gravity` or enable the public route.
- Completed proof: the C launcher opens one owner-bound Mach-O target, spawns it suspended in a dedicated process group, enumerates the private Darwin mapped-vnode record before `SIGCONT`, rejects deterministic pathname replacement with `P15NL009` before child code, preserves accepted output/exit behavior, and fails closed on timeout, surviving same-group descendants, and launcher interruption. The supervised focused run passed 8 tests and 60 assertions; independent review approved the narrow contract.
- Honest boundary: the launcher is host-authored C and is not descriptor-relative execution. It does not resist same-euid in-place mutation or external `SIGCONT`, contain `setsid`/double-fork escapees, verify code signatures or dyld closure, or prove whole-process-tree reaping. It is not wired into the public command, so compiler, evaluator/runtime, verifier, artifact construction, process/file I/O, and release-wrapper Clojure boundaries are unchanged. Formal-language completion remains 0/240.
- Next gate: provide descriptor-relative execution plus OS-level complete-tree containment for a Gravity-authored driver/runtime, then prove accepted `.gravity` and `.qst` execution and stable rejection through tracked `bin/gravity` without a Clojure evaluator in the selected runtime component. The new launcher primitive closes only the mapped-vnode and same-process-group subproblem; it cannot satisfy `:contained-public-native-run` by itself.

## Completion gates

- Genuine self-hosted bootstrap/compiler path with no hidden host-only dependency.
- Feature-complete language/compiler pipeline across documented targets.
- Real rejection behavior and stable diagnostics for unsupported/invalid inputs.
- Reproducible cross-CWD/cross-root artifacts, target outputs, and proof evidence.
- No overstated roadmap checkboxes, packaged parity, or release claims.
