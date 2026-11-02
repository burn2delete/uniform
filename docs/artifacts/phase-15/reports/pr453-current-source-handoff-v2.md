# PR453 current-source v2 handoff

## Purpose and dependencies

This is the scoped PR453 repair for a current-main selected-source handoff.
The frozen historical PR453 head is `2ff60a40eab7427f7a6bd9e0099e57654bb45bbc`
(tree `efa051f7997229dd9a394bad3650ce5f867c93a8`, parent
`3e32fa97f58d37763c13ac7cf066e8bdddbc4035`). Its original branch was based on
`942dbdf8fed7b3ee6e7370a0bfb0780a92158ace`. This repair is based on current
main `4ba30149b6a0c3a3c8aa56dd8783a83829e76085` (tree
`2ccf9b46650c581d8b66c99ac0f5813c87fbe5da`); the coordinator must bind the
final candidate commit and tree during integration preflight. The repair branch
then normal-merged current main `88131ab85dadf1a7d81eba46c097af1b117be606`
(tree `8b39b5943cca532022c8f845d6a783180268cf92`) before the C11 consumer
extension below. It subsequently normal-merged `04b03d1d8138c934e4424be7e537764a1175a277`
(tree `0bca2d0c074fae65ab15009b1c75f8f6213665f8`), `5dfe15c9fad14e5abebf23bf1e645bf9154a2e1e`
(tree `643afe5f3b00918fc3de76f2855294481a09b505`), and the final Reader
composition `6218e80e0238cbd071a8837193e7afb5f0b87869` (tree
`51a615a96828ecaff57824625bb062a9dd2c5f20`).

The increment supplies only the explicit public C6/C11 interface version 2
route. It uses the existing source contracts, Reader owner, expression owner,
Core ingress, policy projection, C11 finalization and artifact comparison.
It does not add a general source loader or use a caller-supplied report,
source pin, callback or executable authority.

## Requirements and implementation

The v2 C6 path now performs the following owner-to-consumer handoff:

1. The actual current C6 source binding is created with the version 2
   contract.
2. The existing selected Reader owner creates the current Reader plan binding;
   its source path, source revision, exported shapes, actual C2 envelope and
   provenance are checked before a fixed metadata projection is admitted.
3. The actual current expression emitter creates the version 2 expression
   binding and is joined to the selected Reader plan-emitter rule.
4. Fresh C2/C3 ingress consumes that selected Reader path and binding. The
   v2 Core constructor, verification report and public verifier carry the
   source-policy receipt and current-source scope.
5. The explicit C11 v2 route parses its native header, requires matching
   versioned artifacts and carries the selected source-policy projection
   through finalization.

The legacy arities and v1 source contracts remain unchanged. A v1 call against
the advanced current C6 source therefore fails closed at the historical byte
identity check. Public v3 remains unavailable from this handoff; internal v3
data contracts are not treated as a public current-source implementation.

The observed current source identities used by v2 are:

| Owner | Bytes | SHA-256 content identity |
| --- | ---: | --- |
| C6 checked-core source | 186870 | `0e7e30e15bc68dabdcee3cfea4c91740d82808204c5fe322d67ccd0ffdb76b98` |
| SH03 Reader source | 246369 | `eec041127cb2785f92e7e2c79d14350d869a0ea8a0194ea3da55f75d52099205` |
| Reader plan emitter | 128499 | `57c13e44b46726a136ecd237dde5e85d86cd55b82bd8ac6033afce2fb87bf527` |
| P15/S23 expression emitter | 1529728 | `dad556ad561890e4c107144c1160966fd4440a65293ddbc186d47422ab273564` |
| C11 MIR source | 567399 | `c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e` |

The Reader and expression identities were obtained by invoking their current
owners and comparing the returned bindings, not by accepting declared data.

## Authored-path accounting

The historical PR453 path set is retained as accounting input. The useful
implementation paths in this candidate are the stage2 expression binding
(`00580`, `00581`, `00581a`), C6 selected Reader and fresh construction
(`00716`, `00720b`, `00720c`, `00720d`, `00724`, `00724a`, `00726`), the
versioned C6/C11 interface modules, the C11 public definitions and helpers
(`00744`, helpers `02`, `03`, and `05`), the load-order entry, the current
C6 source contract, and the focused PR453 handoff test.

The remaining useful historical implementation paths are already represented
by reconciled current-main owners: `00701a` by the current `00701` source-path
owner, `00716a` by the current `00716` plan owner, `00720a` by the current
`00720` cache owner, `00732a` by current `00732` source binding,
`00735c` by the current `00735` family owners, and `00740a` by current
`00740` policy validation. The existing `00582`, `00702`, `00719`, helper
fixtures and focused data harnesses remain in main unchanged.

The historical PR453 reports and `docs/c11-source-interface.md` contain old
Reader identities and preparation-only nonclaims; they are not copied as
second sources of truth. Their useful dependency and limitation statements
are preserved by the current versioned-source reports and this current-main
report. No authored implementation path is silently repinned or discarded.

## Outputs and evidence

The focused real-source producer/consumer and negative controls pass:

- `gravity.self-hosting.pr453-current-source-handoff-test`: 10 tests,
  52 assertions.
- `gravity.self-hosting.sh01-native-unmetered-helpers-test`: 25 tests,
  304 assertions.
- `gravity.reader.dispatch-test`: 7 tests, 72 assertions.
- `gravity.reader.normal-dispatch-test`: 11 tests, 95 assertions.
- `gravity.self-hosting.sh01-language-boundary-test`: 6 tests, 11 assertions.
- `git diff --check`: passed before this report was added.

The focused handoff test covers actual C6, Reader, expression and C11 source
bytes, a fresh v2 C6 artifact/report, a real v2 C11 construction and
contextual verification report consuming that C6 artifact, wrong
interface/version headers before construction, wrong Reader policy before C2
production, and forged Core/source-context rejection before C11 source
construction. It does not claim that a declared oracle is a compiler result.

The final-main dependency stack was inspected for source coupling. The C8
effect traversal change and C7 iterative traversal change preserve the source
identities above. The final Reader change adds source-byte/product joins and
standard dispatch provenance; the Reader-focused receipts above and the full
handoff receipt pass with those joins active.

The existing legacy v1 C6 binding test fails closed with
`C6-VERIFY :pinned-gravity-c6-c10-source-bytes` because the current file is
186870 bytes while the unchanged v1 pin is 141562 bytes. This is the intended
historical/default boundary, not a v1 repin. No large C11/C5 run, native C
run, fresh authority migration proof or denied operation was attempted.

## Coordinator lint repair and fresh handoff

Pinned clj-kondo 2025.10.23 initially found four new warnings: an unused
`testing` refer and three duplicate requires after dependencies moved earlier
in the Bootstrap facade. The repair removes only those redundant declarations;
the earlier namespace aliases and all function bodies remain intact.

Changed-file lint against main
`6218e80e0238cbd071a8837193e7afb5f0b87869` then passed with 3459 inherited
baseline warnings and zero blocking diagnostics. The complete static receipts
are preserved in `/tmp/uniform-pr453-final-static-artifacts`. After this
cleanup the genuine handoff rerun passed 10 tests / 52 assertions, and the
language boundary passed 6 tests / 11 assertions. The logs are
`/tmp/uniform-pr453-lint-fixed-handoff.log` and
`/tmp/uniform-pr453-lint-fixed-boundary.log`.

## Conformance and acceptance criteria

Integration requires an independent review of the exact candidate commit and
tree, a clean worktree, current-main base identity, the successful focused
receipts above, and the repository preflight in integration mode. The v2
route remains development-unadmitted and retains the generic Clojure bridge;
it provides no release, self-hosting, migration-authority, backend, native-C,
or public-v3 credit.
