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
