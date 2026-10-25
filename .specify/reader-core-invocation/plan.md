# Implementation plan: Reader-to-Core current-source invocation

Spec: [spec.md](spec.md)
Input Reader revision/tree: recorded in spec.md.

## Contract alignment

Use existing source constructors and explicit invocation context; do not add an
authority wrapper or new acceptance mechanism. Preserve original-byte checking,
strict native classes/metadata, Unicode and source provenance. Remove only
obsolete project quotas, not intrinsic checks or host safeguards. Follow the
existing [verification workflow](../../docs/development-verification.md).

## Approach and exact files

1. Verify frozen input commits. Compose accepted main
   and the five published frontend candidates in an isolated worktree, preserving
   each owner's changes and recording the resulting exact tree.
2. Trace the public options through current Reader, Syntax/Macro, Resolver and
   Core. Reader adapters include `00816-sh03-reader-raise-rejection.clj`,
   `00850-sh03-c3-precomputed-c2-verify.clj` and
   `semantic_late/146156-sh03-reader-adapt-boundary.clj` under the bootstrap
   implementation tree. Resolver's `00877-sh06-resolution-product-projections.clj`
   and Core's public ingress adapters participate in the same invocation route.
3. Correct only demonstrated invocation/descriptor consumer mismatches. AE
   producer and Native source stay with their owners. Do not revive an obsolete
   bounds field merely to satisfy an old consumer.
4. Reuse existing public development ingress and unmetered Reader tests. Add a
   semantic-name integration test only for a genuine uncovered boundary.

## Validation and evidence

Run focused context/options tests before actual composition. Then run real
paired public CLI/API construction, fresh generic Core verification, malformed
source and tamper/default/context rejection checks on the exact joined sources.
Record namespaces/selectors, command, exit, assertion counts, original-source
conservation and exact input tree. Run language-boundary and affected lightweight
structural checks after source edits. Do not rerun unrelated compiler suites.

Do not call an aggregate pass when a constituent test failed, threw, skipped or
never ran. A real constructor success is not full-chain verification. No private
filesystem path is published in draft evidence. No forced push or main merge.

## Delivery and remaining work

The four files in this feature directory form the finite Spec Kit pilot.
Planning/scaffold completion is distinct from implemented, tested, independently
reviewed and integrated behavior. Update the existing Reader draft only for a
completed validated increment. Obtain independent acceptance of the immutable
candidate; record the integration decision separately.

## Names, callers and compatibility

New test namespace, if required: `gravity.reader-core-invocation-test`.
New helper names describe invocation, source selection or envelope validation.
No API or schema rename is planned. Inspect all added names in the candidate
diff, including additions inside legacy files. Global legacy renaming is not a
prerequisite and foreign active files are excluded.
