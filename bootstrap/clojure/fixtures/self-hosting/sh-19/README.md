# SH-19 Minimal Runtime Leaf Fixtures

This directory contains an early executable Gravity-owned runtime request
engine plus co-canonical accepted and rejected request fixtures.

The engine validates startup, bounded or no-allocation policy, UTF-8 bytes,
filesystem reads, stdout and stderr writes, panic behavior, and process exit.
Authority-bearing operations are deny-by-default and emit structured provider
actions rather than executing ambient host effects.

Requests are bounded before semantic validation by finite node, depth, width,
scalar, and string limits. Startup runs unique initialization services, then
the entrypoint, then the exact reverse cleanup sequence. Allocation, filesystem,
stdout, stderr, and process-exit providers use exact scopes bound to the caller
principal, grant, resource, and operation-specific limits. Request, action,
caller, provider, and scope identifiers use canonical SHA-256 shapes; the
action identity and decision log bind the complete semantic request lineage.

Actual native provider execution, authenticated SH-17 integration, runtime
linking, cryptographic digest computation from canonical authenticated inputs,
and removal of the Clojure stage0 execution boundary remain pending. This leaf
is early queued evidence, not the authoritative SH-19 runtime implementation or
completion credit.
