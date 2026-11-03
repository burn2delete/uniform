# Expected Core failure source trace

## Purpose and status

This file documents the EDN beside it as source data for the bounded current
session controls. It is a development fixture and review receipt, not an
implementation contract, authentication artifact, public admission rule or
authority to accept a Core result.

The current source checkpoint is base
`0e2b37cd2eefbab2f02e5898b960f9bbc4100fef`, candidate source/test commit
`82537d2cb6ec3bb0ac51f30e7c53f9ead2b59b96`, tree
`4c7b0bb1422cb4170624249408a93515a64cad92`. The EDN allowlists the five
diagnostic codes and the closed C6-VERIFY reason set. Unknown codes, reasons,
fields and carriers fail closed or are omitted from the optional projection;
messages, paths, raw values and causal objects are never copied.

The source is deliberately data-only. It does not renew a source hash, replace
a historical authentication pin, authorize a retry, or turn an expected
construction failure into a successful product. Historical Exception-only
wording in earlier session receipts remains historical; the current 00899
session catches `Throwable`, performs the identity-safe fatal/interruption
scan, and only then projects the closed diagnostic form for an ordinary failed
session.

## Evidence and nonclaims

The current injected session controls pass 12 tests and 328 assertions. They
exercise forwarding, nil/false handling, failed-construction redaction,
Throwable cause cycles, interruption restoration, strict carrier validation and
closed diagnostic projection. They do not execute Bootstrap, Reader, Core,
native code or a Gravity target.

This record claims no C5/C6 acceptance, C7/C8 composition, ACC09 authority,
native execution, migration acceptance, release readiness or self-hosting. The
unfinished large-result control and prior failed runs remain nonpasses; no
resource workaround or broad runtime claim is implied.
