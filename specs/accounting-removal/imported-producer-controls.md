# Original imported-producer validation control

Owner: ACC-01. This test-only successor restores the complete original
`sh08-imported-producer-validation-development-kernel` from PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635` on PR618 base
`3a1a553dbb1b3eb0ae472f5bf806171c02a71109`, tree
`c8600e2c52f4ba47f1366fae4035248d23365623`.

## Scope and requirements

The governing accounting-removal spec requires preservation of independently
meaningful input validation and source authentication. The C7 type contract and
D9 evidence contract distinguish finite DATA controls from authenticated source
and compiler acceptance. This increment restores the original test body and
comments byte for byte, changing no production code and preserving all prior
test bytes. It depends on the shared native carrier repair in PR613 and retains
all later stack changes without claiming their acceptance.

The control calls the actual C7 entrypoints through `invoke-c7`. It preserves
memoized request equality, active-cycle rejection, retained-producer membership,
duplicate producer rejection, acceptance of 4097 request rows, closed input
schema and vector validation, and a 16777217-character DATA carrier. The final
negative requires producer rejection despite carrier/input acceptance. Its
comments explicitly deny authentication authority to invocation-local state or
finite DATA. This is no public producer/source acceptance claim.

## Dependencies and artifacts

`imported-producer-controls-census.edn` pins historical and parent identities,
raw copied bytes, complete test form, full test-file hashes and the unchanged
C7 file. The four actual entrypoint forms match history exactly. Nine forms
cover those entrypoints and their immediate source call targets; this does not
prove the transitive call graph or runtime behavior.

One immediate target differs: `sh08-im-derive-with-allowance` uses the current
producer core-validation failure wrapper with reason
`:module-type-producer-core-invalid`; history returned the carrier failure
itself. Both complete form hashes remain in the census. That production form is
unchanged by this draft, and no helper-equivalence claim conceals the difference.
Other imported inference allowances remain outside this test-only restoration.

The local Clojure integrity checker uses reader evaluation disabled, verifies
the exact appended span and unchanged production, and never loads Bootstrap.
No helper is reimplemented in Clojure and no test assertion is adapted.

## Validation and acceptance

- Source integrity: PASS, one complete raw historical copy, four exact actual
  entrypoints, nine immediate source pins, prior tests/production unchanged.
- Language boundary: `clojure -M:test --namespace
  gravity.self-hosting.sh01-language-boundary-test`, PASS: 6 tests, 11 assertions.
- clj-kondo 2025.10.23 with repository config and cache disabled: zero errors,
  71 warnings exactly matching the parent finding multiset.
- Structural documentation and full-language roadmap validators: PASS.
- Whitespace check: PASS.

Genuine execution is UNRUN. Independent acceptance, applicable producer/context
composition and fresh integration remain pending; the coordinator alone routes
those steps. No parent review is inherited. This draft confers no whole imported
accounting, authenticated producer, main integration, release, self-hosting or
PR502 closure authority.
