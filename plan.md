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
- [pending] Add the next closed semantic/runtime subset, then begin replacing the Clojure seed oracle with Gravity-authored compiler stages.
- [pending] Add the next real executable target through the stage2 driver while continuing to retire the generic Clojure runtime bridge.

## Baseline snapshot (2026-07-10)

- Main: `89d5e4b` (`Route two-argument println through Gravity runtime`).
- Current code proves a genuine lexical/C2/C3/P15 reader slice; C2/C3/P15 remain partial and FL-P01-T01 remains unchecked.
- `GRAVITY_BOOTSTRAP_ONLY=1` checks and runs `examples/core-app.gravity` and `.qst` with equivalent output.
- `bootstrap/gravity/p15_s23/compiler.gravity` passes the bootstrap-only public check after executable-symbol analysis was corrected; whole-language self-hosting remains partial.
- The public wrapper and packaged/release artifacts remain bootstrap-hosted; final seed proof is incomplete and seed boundary remains true.
- Roadmap audit records 74/181 accepted fixtures passing public `check`, 107 failing, and 1,054 rejected fixtures collapsing to generic `P18T06004`.
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
- Single-argument `println` now routes through a Gravity-authored effectful runtime function with exact `:io/write`/`:io/stdout` validation and pinned shape; multi-argument printing remains explicitly host-bound until a variadic effect contract is authored.
- Two-argument `println` now routes through a Gravity-authored effectful runtime function and preserves `left right\n` in both the stage2 oracle and compiled C; only arities above two remain host-compatible.

## Active slice

- Owner: next runtime-derived semantic expansion worker.
- Scope: select and implement the next executable backend target through the stage2 driver for the already-proven runtime subset, preserving accepted/rejected behavior, deterministic provenance, and explicit seed boundaries.
- Completed proof: public accepted/rejected behavior, `.qst`/`.gravity` parity, deterministic hashes, actual `/usr/bin/cc` execution, stable unsupported diagnostics, NUL-safe output, output-path containment, unrelated-CWD routing, option validation, and explicit seed-boundary honesty.
- Next gate: add a second real public executable backend (prefer the documented JS/TS hosted target if the contract inventory confirms it) and prove parity with the C route before expanding further runtime semantics.

## Completion gates

- Genuine self-hosted bootstrap/compiler path with no hidden host-only dependency.
- Feature-complete language/compiler pipeline across documented targets.
- Real rejection behavior and stable diagnostics for unsupported/invalid inputs.
- Reproducible cross-CWD/cross-root artifacts, target outputs, and proof evidence.
- No overstated roadmap checkboxes, packaged parity, or release claims.
