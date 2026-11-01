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

The stage cold-loads 30 public APIs without Bootstrap. The three retired
late-helper resources are removed from both load orders; their compatibility
installer remains in its original position. The two traversal helpers remain
lexical locals, absent from both namespaces' Var inventories. Receipt and
construction qualification include all six added stage APIs and four pattern
callback ports.

The actual lexical-normalization fixtures emit 276 bindings and 21 resolutions
in each spelling, with identity
sha256:0fdf519ad7657aa8b291b7aa46df085324bb1fb6bff3cb290b5ecbaf5c8152f2.
Fresh public verification passes. The lexical-leak fixtures reject the escaped
right binding with C5-UNRESOLVED. All four complete before/after values compare
equal, including each accepted artifact's source provenance and each rejected
diagnostic's full data. Canonical source and Reader paths were held constant.
This evidence uses the explicit current-development invocation and grants no
historical Reader migration authority.

Language boundary passed 6 tests / 11 assertions; ownership and catalog checks
passed 45 / 1036. Documentation validation passed 240 documents / 19 phase
indexes, and roadmap and workstream-governance validation passed. Direct
stage/test lint and changed-file repository lint have zero blockers. An initial
new-test delimiter error and an incorrect expected vector index were corrected;
those initial runs remain nonpasses. Final regression and independent review
are recorded separately before integration.

The final resolution regression passed 34 tests / 1156 assertions, and
independent focused lexical, pattern and dependency checks passed 3 / 40.
Complete CI lint selection exposed an unrecognized companion EDN manifest and
incomplete removal of its three retired records. Both defects were corrected.
The full contextual/EDN lint comparison then passed with zero blockers and
3502 unchanged baseline warnings. CI harness regression passed 11 / 106,
including companion selection and rejection of unknown EDN paths. Earlier
companion-manifest and test-reader failures remain nonpasses. The companion
manifest's preexisting Macro navigation entries were not changed; this slice
does not claim that it mirrors every active loader resource.
