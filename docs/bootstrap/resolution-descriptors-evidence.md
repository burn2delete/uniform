# Resolution envelope descriptor and boundary keys

This increment moves the existing resolution envelope descriptor constructor
and boundary-key selection into gravity.resolution. D1, D2, D3, D9, C5, C15,
BOOT8 and TEST2 govern the boundary. Descriptor data is a construction input;
the existing authenticated-envelope owner retains admission authority.

Public APIs are envelope-descriptor and boundary-keys-for. Inputs are the
actual source path, current resolution plan binding, product summary or boundary,
and explicit digest, configuration and working-directory operations. Outputs
are the unchanged private descriptor and allowed boundary-key set. Digest
domains, source/plan/function identities, projections, transitions, lineage,
reference closure and actual-path provenance must remain equal.

Bootstrap retains compatibility entry points, immutable catalog/configuration
roots, host property access and authenticated-envelope construction/verification.
The stage must not depend on Bootstrap. Its receipt and construction drift
qualification must cover each added stage API, callback and configuration root.
The existing C5 catalog and envelope owner retain rejection diagnostics.

Acceptance requires actual paired source emission, actual lexical-scope
rejection, fresh public verification and complete before/after artifact and
diagnostic equality. Focused tests must verify digest inputs, source identity,
lineage, reference closure, property access, boundary-key selection and drift.
Independent review, clean commits, current CI and exact integration preflight
remain required. This increment grants no descriptor authentication, complete
resolution, current Reader migration, release, performance, self-hosting or
seed-retirement authority.

Implementation 2eb0426a is based on e53bea52. The stage cold-loads 40 public
APIs without Bootstrap. Focused checks passed 6 tests / 1010 assertions,
including descriptor fields, ordered digest/property callbacks, input access,
callback failure, boundary selection and stale receipt/construction roots.
The final resolution regression passed 40 / 1460, including actual emission,
real rejection, fresh verification and tampered artifact/envelope refusal.
The initial focused run assumed map configuration-read order; that test was
corrected to require each configuration input once. Digest and property order
assertions remain exact. The initial failed run remains a nonpass.

The actual paired nested-binding fixtures emit 276 bindings / 21 resolutions
with identity
sha256:0fdf519ad7657aa8b291b7aa46df085324bb1fb6bff3cb290b5ecbaf5c8152f2.
Each passes fresh public verification; the escaped right binding rejects with
C5-UNRESOLVED. All four complete before/after accepted artifacts and rejection
payloads compare equal, including each spelling's provenance. Canonical fixture
and Reader paths remain fixed in this worktree. This comparison binds the
extraction before any later upstream composition.

Direct stage/test lint and repository contextual lint passed with zero blockers.
Language boundary passed 6 / 11; documentation passed 240 documents / 19 phase
indexes; roadmap validation passed. Independent review found no source blocker
for the frozen implementation. Final CI, prerequisite integration, independent
acceptance of any later composition and exact clean integration preflight
remain required.
