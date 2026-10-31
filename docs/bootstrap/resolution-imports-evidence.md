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

The stage cold-loads 24 public APIs without Bootstrap. Direct lint reports
zero errors/warnings. Four focused tests passed 4 / 133, covering API metadata,
dependency/import fields, candidate precedence, source origins and construction
dependency drift. An initial test-reader delimiter error was corrected before
that pass. The final resolution regression passed 30 tests / 991 assertions,
including actual Gravity emission and unresolved-reference rejection.
Repository lint passed with zero blockers; language boundary 6 / 11,
documentation 240 / 19 and roadmap validation passed.

A fresh replay asserts the 24-API stage and the import-binding-records arity,
then compiles and verifies the actual values and consumer modules in both
spellings. All four complete products compare equal to the verified 20-API
predecessor products. The library retains 267 bindings / 10 resolutions and
identity sha256:7cee4a4156c5eb1ddd4ed19e94a6ee6938bd19bb594cdc495863eb648f8cf0a4;
the consumer retains 267 / 6 and identity
sha256:4b7cf3c7880ab84ad20efb41b158d70f43392107a8a107724473d7fdb935903d.
The consumer uses the actual verified library artifact. Each spelling's
provenance remains equal; canonical source/Reader paths stay fixed while an
explicit classpath selects the new stage implementation.

Implementation commit 3e1f15bf is based on 74412834. This slice changes no
ownership input, migration record or historical mapping fingerprint. The
existing historical crosswalk refusal after main's navigation changes remains
a nonclaim; current task-body validation and integration disposition are
separate. Independent final acceptance, exact-head CI and clean integration
preflight remain required.
