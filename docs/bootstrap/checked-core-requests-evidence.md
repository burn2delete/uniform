# Checked-core request and reference boundary

This increment moves the existing authenticated C5-to-Core request producer and
semantic/digest-reference helpers into `gravity.checked-core`. D1, D2, D3, D9,
L1, L2, C1-C6, C15, BOOT8 and TEST2 govern the boundary. The input is a C5
artifact plus its lexical verification context and explicit Reader invocation;
the output is the existing Core request data, identities and reference records.

The public gateway must still call the C5 owner's verification through explicit
operations, select the same context/invocation arity, validate Reader invocation
options and reject missing lexical authority. Its request constructor remains
private and retains the existing artifact-kind, passed-report and complete-proof
requirements. Neither a supplied public proof map nor a stage helper grants
Core artifact admission or publication authority.

Digest ordinals remain nonnegative integers with the existing exact reference
schema and authority marker. Resolution must produce an actual SHA-256 identity
or the existing C6-VERIFY rejection. These are format/index legality checks,
not accounting or input quotas. Semantic reference normalization must preserve
the existing source/projection identity distinctions and deterministic order.

Bootstrap keeps Core owner admission, proof transactions, publication and
historical migration checks. Compatibility names/arities/private visibility,
diagnostic data, explicit Var ports and late construction/receipt epochs stay
intact. Acceptance requires real paired requests and rejection, complete output
parity, arity/context/digest adversarial tests, appropriate regressions,
independent acceptance, exact-head CI and clean final integration preflight.
No historical denied Reader, native, migration or external-capture action is
retried. This draft grants no acceptance and no Core artifact, backend, release,
self-hosting or seed-retirement completion.
