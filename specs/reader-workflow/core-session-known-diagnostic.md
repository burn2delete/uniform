# Failed current Core session diagnostics

## Purpose and scope

This bounded source increment preserves failed-session control flow while
making expected-construction outcomes distinguishable. It is based on current
main `0e2b37cd2eefbab2f02e5898b960f9bbc4100fef`, and the candidate source/test
freeze is commit `82537d2cb6ec3bb0ac51f30e7c53f9ead2b59b96`, tree
`4c7b0bb1422cb4170624249408a93515a64cad92`.

The session records an optional `:expected-build-failure` only when expected
construction returns nil or an ordinary caught Exception. Its closed forms are
`{:origin :expected-core-construction :kind :nil-return}` and an exception
form containing only the binary exception class name. Failed sessions remain
invalid and are not products or authority.

The complete causal chain is scanned by identity before deciding control flow.
Any non-Exception Throwable or InterruptedException causes the original outer
object to be rethrown; an interruption restores the thread flag. Cycles are
handled without a depth or work quota. Existing upstream containment and
direct-interruption behavior is unchanged.

## Requirements and dependencies

- Preserve schema version, ready-session fields, replay binding and legacy
  omitted/nil invocation behavior.
- Inspect diagnostic data only after strict native carrier validation.
- Project only the closed code and reason allowlists in
  `expected-core-failure-source-trace.edn`; never copy messages, paths, raw
  values, causes or arbitrary ex-data.
- Keep diagnostic projection failed-only and conservative: malformed,
  conflicting, unknown or foreign data omits the extension.
- Preserve fatal, interrupt, callback and source-provenance semantics.

The implementation uses the existing 00899 session, native carrier validator,
construction ports and upstream verifier. The focused test injects only those
ports in a disposable namespace. It does not execute Bootstrap, Reader, Core,
native code or a Gravity target.

## Outputs and evidence

`gravity.reader.core-session-invocation-test` passes 12 tests and 328
assertions. It covers exact invocation forwarding, nil/legacy behavior,
malformed and substituted contexts, failed construction redaction, causal
fatal/interruption propagation including cycles, strict diagnostic projection,
foreign callback refusal, projection-failure propagation, session integrity and
upstream containment.

The source contract EDN declares the five literal diagnostic codes, the
top-level C6-VERIFY reason allowlist, fail-closed unknown handling and the
development-only nonclaims. Pinned clj-kondo reports zero errors; the large
SH01 file has 52 existing warnings in unchanged portions and no finding on the
added ranges, while the smaller changed tests have no warnings. The
language-boundary gate passes 6 tests and 11 assertions.

## Conformance, acceptance and limits

The frozen source/test tuple is base `0e2b37cd2eefbab2f02e5898b960f9bbc4100fef`,
commit `82537d2cb6ec3bb0ac51f30e7c53f9ead2b59b96`, tree
`4c7b0bb1422cb4170624249408a93515a64cad92`. The worktree was clean before
this documentation metadata commit. Independent affected review and
current-main composition remain required.

This increment does not claim an actual C5/C6/Core result, ACC09 authority,
native execution, migration acceptance, release readiness or self-hosting. It
does not recover the cause of any historical failed run and does not authorize
a retry, source-pin renewal or consumer adoption of the optional diagnostic.
