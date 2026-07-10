# Full Self-Hosting Roadmap

## Objective

Iterate from the current bounded reader slice to a feature-complete, self-hosted Gravity compiler that can compile to every supported target with reproducible proof.

## Current execution

- [completed] Establish baseline: repository state, canon, capability matrix, toolchain, and host-retirement blockers.
- [completed] Implement a source-derived hosted C target for the verified core instruction subset.
- [completed] Run independent adversarial review and required validators.
- [completed] Integrate only reviewed additive commits and refresh the baseline.
- [completed] Route the explicit C target through the public compile boundary.
- [pending] Replace constant-output/Clojure-seed evaluation with a self-hosted/runtime-derived target path.

## Baseline snapshot (2026-07-10)

- Main: `b5ddd0886a26073c437b73cb9fd8769b54c6764a`.
- Current code proves a genuine lexical/C2/C3/P15 reader slice; C2/C3/P15 remain partial and FL-P01-T01 remains unchecked.
- `GRAVITY_BOOTSTRAP_ONLY=1` checks and runs `examples/core-app.gravity` and `.qst` with equivalent output.
- `bootstrap/gravity/p15_s23/compiler.gravity` is not yet accepted by the current public check (`L3-UNKNOWN-ALIAS`).
- The public wrapper and packaged/release artifacts remain bootstrap-hosted; final seed proof is incomplete and seed boundary remains true.
- Roadmap audit records 74/181 accepted fixtures passing public `check`, 107 failing, and 1,054 rejected fixtures collapsing to generic `P18T06004`.
- Host Java is OpenJDK 26.0.1; system `clojure` is absent, so baseline probes use the bundled temporary launcher at `/tmp/gravity-clojure-runtime/bin`.
- The new C target is real and source-derived from the verified stage0 plan, but remains Clojure-seed-bound (`:clojure-seed-boundary? true`) and internal; it does not close public seedless release.
- Explicit `gravity compile --target c -o ...` now routes to that backend; the default packaged/JVM compile path remains unchanged.
- Working tree changes currently consist only of coordinator `plan.md` and `heartbeat.md` pending this status refresh.

## Active slice

- Owner: next self-hosting/runtime-derived lowering worker.
- Scope: remove constant-output/Clojure-seed evaluation from the explicit C target while preserving the reviewed public route.
- Completed proof: accepted and rejected behavior, `.qst`/`.gravity` parity, deterministic hashes, actual `/usr/bin/cc` execution, stable unsupported diagnostics, NUL-safe output, output-path containment, and explicit seed-boundary honesty.

## Completion gates

- Genuine self-hosted bootstrap/compiler path with no hidden host-only dependency.
- Feature-complete language/compiler pipeline across documented targets.
- Real rejection behavior and stable diagnostics for unsupported/invalid inputs.
- Reproducible cross-CWD/cross-root artifacts, target outputs, and proof evidence.
- No overstated roadmap checkboxes, packaged parity, or release claims.
