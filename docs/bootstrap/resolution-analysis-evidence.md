# Resolution lexical input normalization

This increment moves the existing lexical-reference/scope collector, its two
sealed traversal helpers, four pattern-binding helpers and dependency-edge
normalizer into gravity.resolution. D1, D2, D3, D9, L2, L3, L7, C3, C4, C5,
C15 and TEST2 govern the boundary. Gravity-authored resolution keeps semantic
binding and graph-legality authority.

Public APIs are analysis-inputs, match-pattern-binding-paths,
fixed-vector-pattern?, unique-match-binding-paths, let-loop-binding-paths and
dependency-edges. Inputs are expanded syntax, explicit overrides, module and
dependency records, and source spans. Outputs are lexical scope/reference
records, pattern binding paths and dependency edges with deterministic
identities and retained source origins. Bootstrap keeps compatibility entry
points and authenticated-parent/request assembly.

The traversal helpers remain lexically sealed; moving them must not introduce
replaceable helper Vars. Explicit seed operations preserve existing callback
behavior. The stage does not require gravity.bootstrap. Receipt qualification
and construction drift checks must include every new stage/seed dependency.

These are normalization algorithms, not complete L7 admission. Pattern typing,
guard Boolean/effect legality, exhaustiveness, ownership moves, decision-tree
lowering and broader pattern-family support remain with their existing owners.
The existing C5 diagnostic catalog owns resolution rejection; this increment
does not reclassify recovery records as accepted language constructs.

Acceptance requires actual nested-binding source products and real lexical
scope rejection, paired spelling provenance, deterministic identity and full
before/after product comparison. Module-import regression, focused adversarial
checks, independent review, clean commits, CI and final integration preflight
remain required. No current Reader admission, authoritative migration, full
resolution, release, performance, self-hosting or seed-retirement claim.
