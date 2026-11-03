# GPU native diagnostic policy data

## Contract boundary

D1, D2, D3, D8, D9, C1, C15, B1, B8, B13, B14, BOOT8 and TEST2 govern
this extraction. Three existing native functions move to gravity.backend.gpu:
source-overrides, missing-fact and kernel-id. Inputs are trusted native module
metadata and diagnostic ids. Outputs are borrowed overrides and existing
diagnostic context labels. Bootstrap consumes the stage; the stage requires
only clojure.core and imports no orchestrator or consumer.

The stage owns native override precedence and rule-context defaults. Existing
B8 wrappers retain arities and late interposition. Refusal decisions, failure
construction, catalog streams, source admission and publication remain with
their existing owners. Whole-file conservation checks preserve each lookup
body and all surrounding component and Bootstrap code except delegation.

## Artifacts and checks

Focused tests emit and read back an actual B8-MANIFEST diagnostic from an empty
malformed native artifact, preserving its supplied source context. Native
catalog records preserve supplied input identity, order and serialization.
Catalog rows are compatibility examples, not ten observed semantic failures.
Frozen-parent and candidate captures compare complete original serialized
source records, policies, overrides, catalog streams, malformed-input
observations and ten actual override refusals. Source and malformed-input
digests are recomputed. Tests cover borrowed identity, false and empty
overrides, unknown defaults and late stage/wrapper interposition.

The first local baseline launcher emitted its observations but then failed on
an extra command-line delimiter. It receives no validation credit. Its output
is retained separately; a correctly launched frozen-parent capture completed
before source editing.

## Unsupported boundaries

This is native diagnostic data extraction. Full Gravity B8 source compilation,
verified MIR/domain ingress, GPU code generation or execution, target feature
and launch validation, kernel legality, device memory lifetimes, transfers,
synchronization, atomics, numeric certificates, downstream handoff, source
authentication and public diagnostic redaction remain unsupported. Existing
kernel labels and complete/proof fields are data, with no certificate or
admission authority. No native broker, restricted factory, frozen Reader,
package/archive/release builder or Attempt30 route executes. Migration,
release, self-hosting and seed retirement are not claimed. Independent review,
own CI/artifacts and exact clean current-main integration preflight remain
required.
