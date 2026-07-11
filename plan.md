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
- [pending] Add the next closed semantic/runtime subset, then begin replacing the Clojure seed oracle with Gravity-authored compiler stages.
- [pending] Replace the stage0 plan-emitter boundary with a Gravity-authored stage2 compiler step after the newly accepted P15 source can pass its front-end gates.

## Baseline snapshot (2026-07-10)

- Main: `6537643` (`Add runtime-derived string concatenation`).
- Current code proves a genuine lexical/C2/C3/P15 reader slice; C2/C3/P15 remain partial and FL-P01-T01 remains unchecked.
- `GRAVITY_BOOTSTRAP_ONLY=1` checks and runs `examples/core-app.gravity` and `.qst` with equivalent output.
- `bootstrap/gravity/p15_s23/compiler.gravity` is not yet accepted by the current public check (`L3-UNKNOWN-ALIAS`).
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

## Active slice

- Owner: next runtime-derived semantic expansion worker.
- Scope: widen the reviewed runtime-derived mode to the next documented instruction/builtin subset, preserving fail-closed unsupported values and keeping the Clojure evaluator non-authoritative.
- Completed proof: public accepted/rejected behavior, `.qst`/`.gravity` parity, deterministic hashes, actual `/usr/bin/cc` execution, stable unsupported diagnostics, NUL-safe output, output-path containment, unrelated-CWD routing, option validation, and explicit seed-boundary honesty.
- Next self-hosting gate: use the now-accepted P15 compiler source to replace one stage0 plan-emitter boundary with a Gravity-authored stage2 artifact, with explicit equivalence and rejection proof before any seed claim changes.

## Completion gates

- Genuine self-hosted bootstrap/compiler path with no hidden host-only dependency.
- Feature-complete language/compiler pipeline across documented targets.
- Real rejection behavior and stable diagnostics for unsupported/invalid inputs.
- Reproducible cross-CWD/cross-root artifacts, target outputs, and proof evidence.
- No overstated roadmap checkboxes, packaged parity, or release claims.
