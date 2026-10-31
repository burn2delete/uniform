# Public normal completion sink; selected route lower-port boundary

Historical partial95b2e980 scope. Its source/evidence remains preserved at that
commit. The continuation and implemented route are documented in
[selected-owner-route](../selected-owner-route/README.md).

This partial source candidate starts from exact accepted common252a2e8f, tree
8951512aa5cc84fc3da868d180b8c4d7f923d2ed. RV caller packet71f4065c/tree
d6b36e57cf2029de6b59e749ca04dc13275cdcbc, artifact SHA256
3fe75a0c0261b24c4dfd799c0add84525c53d733cf8b9dbe3c0e5ef86fac8db5,
is the concrete consumer input. RV helper/child/collector files are untouched.

## Implemented port

gravity.development-test-runner/result-delivery-sink[delivery inherited] is a
thin public call to the EXISTING machine-result-file-sink. run-cli! uses that
same export. The entire existing writer/validator body and other source bytes
are unchanged. No copied validator, new schema, extra allowance or fallback.

The original closed delivery/request checks, absolute file, positive
int-representable supplied byte limit, one-shot emission and exact selection
remain. One UTF8 EDN frame plus newline is written with CREATE_NEW/WRITE/
NOFOLLOW_LINKS. Existing output/symlink/IO errors escape; no overwrite/delete/
retry or directory ownership is added. Inherited runs only after a successful
write, receives the original result object, and its original error propagates.
The existing validator's scope is conserved, not expanded into source/config
attestation or full result semantic validation. The normal parent retains that
strict completion validation responsibility.

Six extracted-source DATAIO tests/67 assertions pass. They exercise the actual
existing writer and new export, not project namespace loading or a selected test.
Four readable full-source mutations must fail: bypass factory, allow existing
output, omit inherited sink and omit limit. The ordinary namespace guard and
entire shared parallel/broker sources remain byte-identical to common252.

## Dedicated route NOT implemented: one concrete lower-port conflict

run-resolution-parent-mutation-process is NOT added as a stub or bypass. The
existing broker with-lease exposes only three-argument operation and fourth
AFTER-disposition observer. It computes terminal assessment AFTER thunk returns
or throws, then releases/quarantines C, then invokes the observer. There is no
supported call receiving that assessment before C disposition to finalize L.

This matters for two concrete selected failure paths:

1. Native start throws without a returned Process. Custody is pending/unknown;
   an unconditional thunk finally releasing L would falsely free an unresolved
   resource. An operation error alone cannot distinguish this from an ordinary
   semantic error after fully terminal process/capture facts.
2. L release fails after fully terminal process facts. Throwing that failure from
   the thunk does NOT quarantine C: the existing broker deliberately permits
   release after an ordinary operation error when process facts are complete.
   C must instead be held when L disposition is unresolved. The current fourth
   observer runs too late to prevent that release.

Using :on-event telemetry, a supplied allowed-namespace flag, manual lease without
the dynamic startup custody scope, a fake process-terminal record for L, or private
Var lookup would not close this conflict. Copying the terminal/child validators
or inferring L state from the callback's return is not proposed.

## Exact narrower source interface to coordinate

The needed addition is an existing-broker pre-disposition owned-resource finalizer
step, e.g. a new with-lease arity with this explicit signature:

```clojure
(with-lease options resource-class operation finalize-owned-resources! observe!)
```

It would pass the actual operation result/error, same retained process registry
and computed terminal assessment to finalize-owned-resources! on the acquiring
thread AFTER the operation, BEFORE C release/quarantine. The dedicated route's
LIBRARY-OWNED closure would retain/acquire canonical L after C and before any
preparation, then actually release it only when the joint source contract permits,
otherwise retain it and report quarantine. Hook/release errors must preserve raw
identity/fatal/interrupt/suppression and force C quarantine on unresolved L. C can
release only with its actual terminal facts and actual finalized/not-acquired L,
never merely a caller-supplied guard boolean. The final observer/report receives
both completed disposition observations; general three/four-argument semantics
and ordinary namespace guard remain conserved.

This is a proposed bounded extension of the existing broker interface, not a
third exported route, controller or keepalive. It is NOT implemented in this
candidate. Its extra arity/joint-resource admission must be explicitly coordinated
with Accounting before completing the dedicated public route. The route must
then restrict the exact RV namespace/Var and source-bound owner options; no
allowednamespace flag or private-validator copy is acceptable. Actual selected
root/policy bindings remain RV/Accounting inputs, not inferred from a path string.

## Limits and disposition

Partial task: public sink implemented; dedicated route held at the exact lower
interface above. No two-port completion claim, review dispatch or publication.
Native pre-return creation/hard bounds remain UNKNOWN; no hard-start deadline
is promised. In-JVM custody is not post-parent durable custody. These remain
conditional nonclaims, not universal development gates. No actual process,
broker/lock acquisition, probe, selected namespace, Bootstrap/Gravity/project
model/genuine/runtime/CI/merge/publication ran. Original7434 publication denial
and6343/22232/446/Reader-DSE-R4 action-specific history are untouched.
