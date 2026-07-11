# Coordinator Heartbeat

- Goal: full self-hosting and compilation to every documented target.
- Phase: iterative target/self-hosting expansion.
- Main branch baseline: `abbf743` (`Route single-argument println through Gravity runtime`).
- Current public proof: core app check/run pass for `.gravity` and `.qst` through the bootstrap-only wrapper; full self-hosting is not proven.
- Known blocker: runtime formatting, two-argument `str`, and single-argument `println` are Gravity-authored, but the generic bridge, multi-argument effects, and broader driver/verifier/runtime implementations remain Clojure-hosted, so seed retirement and full target coverage remain incomplete.
- Active audits: canon/roadmap, toolchain/host-retirement, target/compiler coverage.
- Completed slice: source-derived hosted C target for the verified core instruction subset, with NUL-safe emission and output-path containment.
- Public route completed and reviewed; runtime-derived scalar control flow and direct string concatenation are publicly selectable and fail-closed; runtime-derived plans bind to the Gravity-authored P15 stage2 front-end, emitter, runtime executor/kernel, compiler-driver, and runtime-artifact contracts with explicit hashes, parity checks, and fail-closed gap diagnostics; stage2 front-end ingress uses authoritative C2/C3 reader products. Formatting, two-argument `str`, and single-argument `println` are Gravity-authored through an explicitly residual bridge; multi-argument effects and the wider runtime remain Clojure-hosted, and the next focus is removing that bridge for another capability.
- Rule: keep each implementation slice narrow, independently reviewed, and honestly scoped.
