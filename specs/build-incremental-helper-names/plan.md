# Incremental build helper plan

Spec: [spec.md](spec.md). Base: `8b0b2ed4fb20511b46478189a8c8f554d47d5983`.
Base tree: `b56e5a0d2a2d88f889850f07d6bd4460c371a1cd`.

Move `bootstrap/clojure/src/gravity/c16_incremental/{artifact,policy,proof,validation}.clj`
to `bootstrap/clojure/src/gravity/build/incremental/` with the same basenames;
declare `gravity.build.incremental.{artifact,policy,proof,validation}`.
The four files total 434 lines. Preserve every byte after each namespace line.
Update only the four corresponding requires in `gravity.c16-incremental`.
Existing aliases, calls and public names remain unchanged.

Exact-reference discovery found only the four declarations and facade requires;
there are no other callers, canonical links, load-order or manifest entries.
The remaining facade and test names are a public compatibility boundary, not
permission to extend this rename. Preserve serialized IDs and historical pins.

Add validation-rejection and proof-projection observations to the existing
`gravity.c16-incremental-test`, first execute them before the move, then run the
renamed helper group through the real leaf runner and adjacent pass-cache tests.
Exercise actual planner classification/planning for each old/new path, old/new
groups, rename group, facade, test and complete transaction, both with and
without dependant expansion. Preserve existing nested-path refusals rather than
broadening ownership. Run fresh namespace, language, source-size and doc checks.
