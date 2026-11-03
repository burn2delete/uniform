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

The stage1 source inventory scans `bootstrap/gravity/src`,
`language/compiler/src`, and `language/runtime/src`, so the moved modules stay
present in the hosted source artifact. Current compiler/runtime tests, source
contracts, effects acceptance tooling, and CI path checks use the new paths.

## Deliberate exclusions

The reader/syntax split remains pending for C3 and L1/C2. C4 macro expansion,
C5 name resolution, C6 core lowering/C6-C10 checked-core support, and C15
diagnostics remain at their existing paths with their owning slices. W5 compiler verification leaves,
B13/B14 evidence leaves, bootstrap readers/diagnostics, self-hosting modules,
P15-S23 sources, SH-07-B51 sources, and transitional standard-library modules
remain bootstrap-owned. Historical reports, source pins, ledgers, and archived
attempts are not rewritten.

## Focused audit

Verify exact source conservation and the current path graph with:

```bash
git diff --check
git diff --find-renames --summary
clojure -M:test --namespace gravity.bootstrap-test
clojure -M:dev-test -e "(require 'gravity.runtime.pipeline-test) (clojure.test/run-tests 'gravity.runtime.pipeline-test)"
clojure -M:dev-test -e "(require 'effects-acceptance-test) (clojure.test/run-tests 'effects-acceptance-test)"
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

The relocation does not claim a self-hosted compiler, Clojure seed retirement,
new compiler authority, complete backend execution, or a seedless release.
