# Coordinator Heartbeat

- Goal: full self-hosting and compilation to every documented target.
- Phase: iterative target/self-hosting expansion.
- Main branch baseline: `f6988f0` (`Expose runtime-derived C lowering publicly`).
- Current public proof: core app check/run pass for `.gravity` and `.qst` through the bootstrap-only wrapper; full self-hosting is not proven.
- Known blocker: `bootstrap/gravity/p15_s23/compiler.gravity` currently rejects with `L3-UNKNOWN-ALIAS`; final seed-retirement proof remains incomplete.
- Active audits: canon/roadmap, toolchain/host-retirement, target/compiler coverage.
- Completed slice: source-derived hosted C target for the verified core instruction subset, with NUL-safe emission and output-path containment.
- Public route completed and reviewed; runtime-derived scalar lowering is now publicly selectable and fail-closed; next focus is semantic expansion followed by Gravity-authored compiler-stage replacement.
- Rule: keep each implementation slice narrow, independently reviewed, and honestly scoped.
