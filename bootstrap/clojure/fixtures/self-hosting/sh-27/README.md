# SH-27 bounded stage-equivalence leaf

This fixture root contains an early Gravity-owned comparator for the seven
stage-comparison modes required by `docs/self-hosting-slice-backlog.md`.

The comparator validates compiler lineage and controlled rebuild environments,
compares canonical artifacts, manifests, diagnostics, conformance results,
runtime output, and normalized MIR, and permits a difference only when an exact
approved delta binds both compared values and its policy review.

The bounded schemas require a canonical `sha256:` toolchain identity and
artifact hashes rather than paths, exact mode-to-canonical-form mapping,
canonical runtime output, exact diagnostic source spans, and an explicitly
bounded target-independent opcode sequence whose identifier renaming is unique,
bijective, and consistent. Product identities are unique and ordered exactly
like the declared comparison modes. Accepted-delta identities are unique, and
every accepted delta must bind its one reviewed product exactly.

This is preparatory leaf evidence only. The fixtures contain bounded stage
descriptors, not authentic SH-26 rebuild products. They do not establish a
fixed point, complete SH-27, retire the Clojure seed, or authorize release.
