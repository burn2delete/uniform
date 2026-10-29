# Requirement reconciliation plan

Spec: [spec.md](spec.md). Canonical writes and aggregate deduplication have one
reconciliation owner. The coordinator routes disjoint source-family proposals
and independent review.

1. Freeze the current 240 source identities, extension identities, existing
   task authorities and preserved proposal format.
2. Consume delivered Foundation82, Math/Backends134 and Compiler/Testing65
   decisions through exact source and predecessor guards. Preserve later
   independently reviewed composition lineage.
3. Compose the remaining phase-family task-interface corrections. Resolve
   cross-family aliases centrally and amend the existing task authorities;
   do not create a competing implementation ledger.
4. Verify source/facet conservation, literal task entailment, rejected stale
   or corrupt joins and the accepted/open qualifications. Obtain independent
   review of the actual composed candidate before computing its final scope.
5. Publish the exact count/ratio, validate the candidate and integrate through
   the normal coordinator-routed process.

Only source and inexpensive data controls are in scope. This reconciliation
does not replay compiler, producer, broker, provider or held operations. New
tooling, if required, is Clojure. Existing implementation and release evidence
retains its original scope.
