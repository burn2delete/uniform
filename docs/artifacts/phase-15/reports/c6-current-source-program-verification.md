# Selected current C6 and Reader program verification

This increment verifies compiler programs needed by a current-source Core
successor. It does not yet add a public Core constructor or verifier for that
successor. Existing public arities and historical source contracts remain.

## Compiler program

The explicit C6 program contract selects the current 186870-byte source, SHA256
`0e7e30e15bc68dabdcee3cfea4c91740d82808204c5fe322d67ccd0ffdb76b98`.
The existing source owner freshly checks stable source bytes and UTF8, compiles
those bytes, and compares the complete function manifest and exported function
identities with the reviewed contract. The legacy 141562-byte contract remains
the default. A current program cannot pass that legacy rule.

The selected contract and private compiled-program cache key are versioned.
Both cold and cached lookups authenticate source and plan-emitter inputs before
reuse. Neither Core products nor verification reports are cached by this change.
An Integer binding tag is not interchangeable with the required Long tag.

## Reader program prerequisite

The existing two-argument selected-source Reader owner builds and structurally
validates the actual Reader plan. The C6 wrapper checks its complete reviewed
identity tuple, source path, exported signatures, and actual plan-emitter join.
It checks C6 and compiler source inputs before and after that operation.

The selected Reader has 231666 source bytes, SHA256
`a74e3d1d3bc3d7627493586c78367844b6ba41980cb65b2bee1638fe3273928a`,
and 221 functions. Its plan and complete-function hashes are
`71f5ab0216a0030fa3d78eef5efdb14db58628636f3c430f4eb4e0d7b6b3576a`
and `f2ff5ef5c42e208e519bc2e1df85635982a2710f8626073a748089651d76bacb`.
The actual plan emitter is `compiler.gravity`, source SHA256
`be4ba46088d413322ba5cf86156bb22bb79aeb7bec1fcf3899d8b081019ef854`,
with rule hash
`40c39d9c65d9d23d442eb87f1e0610a87c24daf145e87ec6695f3c4023b3d42c`.
The older expression-emitter labels embedded in Reader plan metadata remain
declared labels; they do not prove the current expression-emitter dependency.

The wrapper returns the existing explicit `:development-current` invocation
and a selected-source-plan scope. It reports neither Reader-result execution
nor an observed envelope state. A later fresh C2 run must obtain the actual
`:development-unadmitted` state and preserve it through downstream contracts.
No frozen Reader, SH02, C3 or release authority follows from a plan match.

## Actual focused evidence

On source commit `4cfa9f04cffa2c164071da53bda52e23484eb25e`, tree
`8cac3793b432f00f1cb7d0eaf5af38c2215b4719`, the two selected owner tests
passed 55 assertions, and language-boundary/LOC checks passed 11 tests and
23 assertions. The process naturally exited zero with actual heap 2147483648
bytes under the unchanged 120-second execution watchdog. Its recorded phase
was 12357.962542 ms, before cleanup and process exit; this is not total wall time.
Stderr was empty. Complete source, harness and runtime bookends were identical.

The actual cache metrics recorded one cold compilation and one hit, with two
source authentications and two emitter authentications. Reader checks executed
the actual selected-source plan owner. Negative controls reject altered program
identities, omitted/mixed version tags, numeric-class aliases, legacy-source
substitution, Reader identities, paths, exports, emitter joins and stale input
bookends at their named checks. The stale-bookend control is an injected negative
caller fixture; it is not a genuine source-authentication positive.

Compiler-free registry and full-form controls separately passed 3 tests and
19 assertions at 256 MiB, with Bootstrap unloaded and empty stderr. Earlier
2/13 and 3/18 preparations retain their own identities and are not added to this
count. Independent source and exact-harness review preceded execution; retained
result review is a separate requirement before integration.

## Remaining boundary

The current expression-lowering emitter requires its own actual observed
dependency tuple and validation through its existing owner. Fresh public Core
construction and verification must then use the selected contract, execute the
real Reader and fresh verifier, observe envelope state, and compare complete
typed Core and physical provenance. New hosted seed-projection changes must be
bound to that execution, not credited through older Core receipts.

Public Core/C11 final admission, C12, emitted-code execution, frozen migration,
release and self-hosting completion are not established by this increment.
