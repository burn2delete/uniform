# Failed current Core session diagnostics

## Purpose and scope

This bounded source increment preserves failed-session control flow while
making expected-construction outcomes distinguishable. It is based on current
main `a5b6427cab15f40a8e1d61a6562a04b4a0f63efb`, and the frozen implementation
is commit `c0bb8f0a8518e1ab535078e6830c87c566782039`, tree
`2a1ca714446f56c04fd1a9e4483872f024bf2ab2`.

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
development-only nonclaims. Pinned clj-kondo reports zero errors and warnings
for the changed tests, and the language-boundary gate passes 6 tests and 11
assertions.

## Conformance, acceptance and limits

The frozen source tuple is base `a5b6427cab15f40a8e1d61a6562a04b4a0f63efb`,
commit `c0bb8f0a8518e1ab535078e6830c87c566782039`, tree
`2a1ca714446f56c04fd1a9e4483872f024bf2ab2`. The worktree was clean before
this documentation commit. Independent affected review and current-main
composition remain required.

This increment does not claim an actual C5/C6/Core result, ACC09 authority,
native execution, migration acceptance, release readiness or self-hosting. It
does not recover the cause of any historical failed run and does not authorize
a retry, source-pin renewal or consumer adoption of the optional diagnostic.
