# Compiler implementation relocation

## Purpose

This increment makes the remaining pure compiler and backend source layout
explicit. Uniform-authored language behavior now lives under `language/` even
while the Clojure stage0 seed remains the execution host. The move changes
repository ownership paths only; namespaces, exports, source bytes, and
semantic behavior remain unchanged.

## Moved source

The following current source groups moved byte-for-byte:

- the pure stage1 compiler modules in
  `language/compiler/src/gravity/compiler/` (authenticated envelope, L2,
  B48 call arity, C7-C14, and C16-C18);
- backend contracts B1-B12 in
  `language/compiler/src/gravity/backend/`;
- `language/compiler/src/gravity/effects.gravity`; and
- `language/runtime/src/gravity/runtime.gravity`.

The SH-01 ownership inventory scans `bootstrap/gravity/src`,
`language/compiler/src`, and `language/runtime/src`, and checks ownership,
profile, and integration metadata across all three roots. The stage1 reader
source list is intentionally narrower: it keeps `bootstrap/gravity/src` and
the explicit checked-core file, then names each of the 28 moved files. The
accepted stage1 fixture carries the same 30 locations. The singular
`stage1-reader-bootstrap-source-root` and accepted fixture `:fixture` remain
the compatibility value `bootstrap/gravity/src`.

Current compiler/runtime adapters, source contracts, effects acceptance
tooling, and CI path checks use the new paths. The stage1 source-file adapter
accepts the explicit file entries; it does not infer a future whole-language
root.

## Deliberate exclusions

The reader/syntax split remains pending for C3 and L1/C2. C4 macro expansion,
C5 name resolution, C6 core lowering/C6-C10 checked-core support, and C15
diagnostics remain at their existing paths with their owning slices. W5 compiler verification leaves,
B13/B14 evidence leaves, bootstrap readers/diagnostics, self-hosting modules,
P15-S23 sources, SH-07-B51 sources, and transitional standard-library modules
remain bootstrap-owned. Historical reports, source pins, ledgers, and archived
attempts are not rewritten.

## Historical path labels

Twenty-four moved Gravity files retain an old bootstrap path in their
`:metadata :bootstrap :conformance :accepted-fixture` label (C7's declaration
is the representative case). Those strings are serialized historical
conformance declarations preserved for byte identity; they are not source
resolution or file I/O. The stage1 validator reads actual files from its
explicit source list, while current adapters and live tests provide the
relocated source paths. No conformance renewal, source-hash refresh, or pin
refresh is implied by this layout change.

The remaining old-path mentions found in `docs/bootstrap`, frozen P15/source
inventory evidence, and the pinned `tools/sh_crosswalk.clj` view are historical
selected inputs or declared evidence. They remain unchanged for provenance.
There are no old moved-source paths in current `bootstrap/clojure/src` or
`bootstrap/clojure/test` I/O.

## Focused audit

Verify exact source conservation and the current path graph with:

```bash
git diff --find-renames --summary
clojure -M:dev-test --namespace gravity.self-hosting.sh01-ownership-test
clojure -J-Xmx2g -M:dev-test --namespace gravity.self-hosting.sh25-component-build-test --exact sh25-catalog-covers-the-current-authoritative-inventory --fail-fast
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

The relocation does not claim a self-hosted compiler, Clojure seed retirement,
new compiler authority, complete backend execution, or a seedless release.
