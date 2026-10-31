# Resolution dependency and import records

This increment moves the four existing SH-06 dependency/import normalization
algorithms into gravity.resolution. D1, D2, D3, D9, L3, C3, C4, C5, C15 and
TEST2 govern the boundary. Gravity-authored resolution retains semantic policy
and binding-identity authority.

The public APIs are dependency-record, import-records, import-binding-records
and explicit-candidate-records. They consume parsed module declarations,
explicit dependency/candidate records, expanded-parent identity and source
paths. They emit dependency records and import/explicit declaration records,
preserving aliases, visibility, package, profile, target, effects,
capabilities, safety, source spans and provenance digests.

The stage takes canonical digest, span and normalization dependencies through
the existing trusted seed operation map. Bootstrap retains compatibility
entry points and configuration; the stage does not require gravity.bootstrap.
These normalizers confer no authentication or module admission. The actual
Gravity resolver and existing C5 catalog retain diagnostic ownership.

Acceptance requires actual current-development module artifacts, real
rejection, deterministic identity, paired spelling provenance and complete
before/after artifact equality. Focused/adversarial checks cover import kind,
profile/target/capability metadata, candidate precedence, origin fields and
stale dependency interposition. Existing receipt and construction qualification
must include all added stage and seed dependencies.

Pattern/lexical traversal, request and artifact assembly, verification and the
legacy C5 engine remain outside this increment. This work grants no complete
resolution, authoritative migration, Reader admission, release, performance,
self-hosting or seed-retirement support. The stacked prerequisite slices must
clear their own integration gates before this successor can integrate.
