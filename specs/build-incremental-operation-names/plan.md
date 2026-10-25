# Incremental operation rename plan

Spec: [spec.md](spec.md). Base: `16ff1f59471a7fedd7d2a4552c4bde1edf0b3d9e`.
Base tree: `8cce676948422e2a07d947357aec9974fd1be519`.

## Complete transaction

- Move `bootstrap/clojure/src/gravity/c16_incremental/operations.clj` to
  `bootstrap/clojure/src/gravity/build/incremental/operations.clj` and declare
  `gravity.build.incremental.operations`. Existing function names are semantic.
- Update the require and two qualified calls in
  `bootstrap/clojure/src/gravity/c16_incremental.clj`.
- Add binding-scope regressions to the existing
  `bootstrap/clojure/test/gravity/c16_incremental_test.clj`.

Repository-wide exact path/namespace searches found only the helper declaration
and those three facade references. There are no explicit helper-path manifest
or source-pin entries to rewrite. The facade and test stems remain unchanged,
preserving the leaf catalog, ownership projection and documented selectors.
No old-namespace alias is needed for this internal helper. Public compatibility
APIs and historical identities are deliberately not part of this rename.

## Validation and delivery

Run the incremental and adjacent pass-cache leaf tests, bootstrap compatibility,
leaf catalog/ownership checks, language boundary and source-size validator.
Verify the new namespace resolves and the old resource is absent. Review the
complete candidate diff for semantic names and unchanged compatibility fields.
Keep the implementation in a draft PR; record exact results in [evidence](evidence.md).
No authority proof is regenerated or silently reused for this source revision.
