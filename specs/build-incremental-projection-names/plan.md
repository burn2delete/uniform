# Incremental projection rename plan

Spec: [spec.md](spec.md). Base: `7d008ac94ddc7d0bad78354317035e87ff4889cb`.
Base tree: `e18546b094b24790b1ff343a59019e12e8b685a5`.

- Move `bootstrap/clojure/src/gravity/c16_incremental/projection.clj` to
  `bootstrap/clojure/src/gravity/build/incremental/projection.clj`, declaring
  `gravity.build.incremental.projection`; preserve every other byte.
- Update its single require in `gravity.c16-incremental`; the existing alias
  keeps all three facade calls unchanged. Update the one Compiler API source link.
- Add metadata-precedence and exact stage-key regressions through the unchanged
  facade in `bootstrap/clojure/test/gravity/c16_incremental_test.clj`.

Repository-wide references identify only that require, declaration and live doc
link. No historical pin or explicit nested-helper manifest needs rewriting.
Public names and test paths/selectors remain unchanged. Compare actual planner
behavior before/after, not just literal references; run focused development
tests and language/document checks. Retain baseline failures separately.
