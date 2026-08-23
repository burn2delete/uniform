# Development Verification Workflow

The live workflow is defined in `docs/development-verification.md` and uses the
repository's Clojure runners directly.

The former Python scheduling workflow was non-authoritative migration debt. It
is retained only in Git history. Do not reconstruct its manifest, cache,
heartbeat, admission, or receipt protocols as a new host-language layer.

Current coordination is owned by the Clojure workstream-governance and
worktree-preflight gates. Compiler and self-hosting behavior remains owned by
the applicable Clojure and Gravity tests and by the evidence requirements in
the normative contracts.
