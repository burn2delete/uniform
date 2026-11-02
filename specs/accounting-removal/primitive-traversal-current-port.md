# PR614 current-main primitive traversal port

This report records the bounded current-main port of PR614. It preserves the
historical PR614 census and report as historical source material; it does not
refresh their old pins or turn them into current admission evidence. The port
removes no semantic validation and makes no migration, release, self-hosting,
native, or runtime-completion claim.

## Immutable identities

| Item | Commit | Tree |
| --- | --- | --- |
| Original PR614 head | `c506514d4b32baf8269a7c3ff49ca86d2dc18c05` | `ef9a09328663a93e213f65754c8caab882362c2d` |
| Original authored base | `5ebff5cae276ddd93b002a28806a1c363687f7c9` | `aa6a3d0d21905452338262c48dce94e2b9b8dce3` |
| Current-main base, first parent | `046ae1a5565c0ca45928d3b3240adf8c643c304c` | `911344d93253d124c6852afe9010f9ee21c64f3b` |
| Ancestry-only merge | `b040fa1b0b980a7033e4cbe5cd44fd3634c3e5b6` | `911344d93253d124c6852afe9010f9ee21c64f3b` |
| Selective source/test port | `99ff2ddf73b3b97bc0603ad843efcb7ff814f7db` | `1fb0d84e7763fa897ef184d95fca01766c628197` |

The ancestry-only merge has current main as its first parent and PR614 as its
second parent. It imports no obsolete PR production tree. The selective port
was committed separately from that merge in the clean checkout
`/Users/matt/.codex/worktrees/review-pr-502/uniform`, branch
`codex/review-614-current`.

## Conserved useful delta

The current C7 source still had two live recursive traversals from PR614:

- `sh08-all-unique?` now uses an explicit loop, retaining the supplied
  observed prefix, existing equality/membership behavior, and early duplicate
  result. Eight invocation expressions remain across seven current contexts;
  only the helper's self-recursive edge was removed.
- `sh08-type-nodes` now uses an explicit loop, retaining initial type/fact
  state, ordered fact production, the accepted result envelope, and the exact
  accumulated prefix plus first diagnostic on rejection. Its one current
  external caller remains `sh08-type-core-artifact`; only the helper's
  self-recursive edge was removed.

The old `sh08-ft-product-vector?` width check was already absent on current
main. Its current shape-only behavior (key present and value is a Vector) was
not replayed from PR614. Existing module, source, node, binding, provenance,
profile, effect, capability, native-class, callback, and cycle controls remain
owned by current main.

The changed test file preserves the existing current shape fixtures and adds
only the missing semantic controls: 1,025 typed nodes, a final invalid node
with the 1,024-node type/fact prefix and first diagnostic preserved, direct
observed-prefix/late-duplicate helper state, seeded type/fact state, an
unvisited tail, a foreign Counted product value whose count callback is not
invoked, and a 70,000-entry Vector positive. No historical PR614 census or
report bytes were copied into the current port.

## Current source identities

These are derived from the exact port source and plan and are development
provenance, not canonical admission pins:

| Identity | Value |
| --- | --- |
| C7 source path | `bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity` |
| C7 source bytes | `1840427` |
| C7 source SHA-256 | `sha256:162c8abf40cd23776c0140e7fc5d1bb9cfbaf2272960062728d6fe7503036695` |
| C7 stage-2 plan semantic ID | `sha256:cf196a1797143505c3b2ed7e9cd79d598f0b9b63bb9f2b20b3c5ba0d0f6f495f` |
| C7 function map ID | `sha256:18d5a579006705d71ce046bf0e684ce15a7fd0828e7a0d0e1cbef927d4d044d5` |
| C7 function-name ID | `sha256:edff501bd7c300c339685889b76cf1875371654df57b81492df252ae7cd7f5d9` |
| C7 function-shape ID | `sha256:bf5e64d261471841c3475378a490e931ef418642d01c0ae1a02c30bccd264994` |
| Test path SHA-256 | `sha256:47d9e56bee4748c172840b7e8ee68fdc36410acefcf9b25de021f47829fb2529` |

The current derived public `sh08-type-core-artifact` function identity is
`sha256:62891d2b72dad3714c6ca9999976e30c7cb2b08dd47f1508d1b497f597baf467`.
The source-coverage fixture's older expected byte count (`284176`) and pins do
not describe this current source and were not rewritten by this port.

## Validation and evidence limits

The following finite checks passed on the selective source commit:

| Check | Result |
| --- | --- |
| `sh08-core-shape-graph-and-typing-development-kernel` | 1 test / 30 assertions, 0 failures, 0 errors |
| `sh08-primitive-traversal-state-development-kernel` | 1 test / 25 assertions, 0 failures, 0 errors |
| `sh08-core-shape-field-extents-development-kernel` | 1 test / 26 assertions, 0 failures, 0 errors |
| Language boundary | 6 tests / 11 assertions, 0 failures, 0 errors |
| Document validator | 240 documents / 19 phase indexes, passed |
| Full-language roadmap validator | passed; dated coverage remains non-authoritative |
| Workstream governance validator | 185 workstreams, passed |
| Git whitespace | `git diff --check`, passed |

The focused primitive-type integration selector was attempted with the normal
development runner and stopped at the pre-existing C2 Reader identity guard:
`C2-HASH`, expected source bytes `231576`, observed `246369`, missing
`:exact-regular-pinned-sh03-reader-source`. It did not reach the changed C7
source. The earlier incorrect `--namespace ... --exact ...` invocation is
recorded as a runner-argument error, not as a safety denial; subsequent runs
used the documented `:dev-test` route.

The pinned changed-file lint command was run against the exact current-main
base and the two changed paths. It failed closed because the pinned executable
`clj-kondo` is unavailable in this checkout (`java.io.IOException`, cannot
run program `clj-kondo`). No warning or error result is claimed. Native,
restricted, C5-heavy, C12, and unsupported host routes were not run.

## Current composition review

The coordinator reran the three focused selectors on the composition with
main `9b6f1169ad5fe1689c722f3975cde339b02a8087`: 3 tests / 81 assertions,
0 failures and 0 errors. Boundary, document, roadmap, governance, and whitespace
checks also passed. The available pinned clj-kondo 2025.10.23 executable was
then used for changed-file lint: 65 baseline warnings, 0 blocking warnings or
errors. Its receipts are retained under
`/tmp/uniform-pr614-current-static-artifacts`.

Independent review accepted the selective implementation and original-path
accounting. Main subsequently advanced to
`04b03d1d8138c934e4424be7e537764a1175a277` through an effect-checker quota
removal in three C8/SH07/SH09 paths. The normal merge preserved all three
PR614 port files exactly. This does not promote the historical C2 guard
nonpass or grant broader compiler authority.

Coordinator integration remains required. This
receipt grants only the bounded C7 development evidence above; it does not
close PR614 or claim the complete C7, source-authentication, or accounting
removal workstream.
