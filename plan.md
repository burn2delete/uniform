# Full Self-Hosting Roadmap

## Objective

Iterate from the current bounded reader slice to a feature-complete, self-hosted Gravity compiler that can compile to every supported target with reproducible proof.

## Current execution

- [completed] Establish baseline: repository state, canon, capability matrix, toolchain, and host-retirement blockers.
- [pending] Select and implement the smallest executable vertical slice with accepted/rejected behavior.
- [pending] Run independent adversarial review and required validators.
- [pending] Integrate only reviewed additive commits and refresh the baseline.

## Baseline snapshot (2026-07-10)

- Main: `c69c60aba2aa84debce411a8f822e1ef1c91c22a`.
- Current code proves a genuine lexical/C2/C3/P15 reader slice; C2/C3/P15 remain partial and FL-P01-T01 remains unchecked.
- `GRAVITY_BOOTSTRAP_ONLY=1` checks and runs `examples/core-app.gravity` and `.qst` with equivalent output.
- `bootstrap/gravity/p15_s23/compiler.gravity` is not yet accepted by the current public check (`L3-UNKNOWN-ALIAS`).
- The public wrapper and packaged/release artifacts remain bootstrap-hosted; final seed proof is incomplete and seed boundary remains true.
- Roadmap audit records 74/181 accepted fixtures passing public `check`, 107 failing, and 1,054 rejected fixtures collapsing to generic `P18T06004`.
- Host Java is OpenJDK 26.0.1; system `clojure` is absent, so baseline probes use the bundled temporary launcher at `/tmp/gravity-clojure-runtime/bin`.
- Working tree changes currently consist only of coordinator `plan.md` and `heartbeat.md`.

## Completion gates

- Genuine self-hosted bootstrap/compiler path with no hidden host-only dependency.
- Feature-complete language/compiler pipeline across documented targets.
- Real rejection behavior and stable diagnostics for unsupported/invalid inputs.
- Reproducible cross-CWD/cross-root artifacts, target outputs, and proof evidence.
- No overstated roadmap checkboxes, packaged parity, or release claims.
