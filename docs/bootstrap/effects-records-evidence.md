# Effects: hosted seed record builders

D1, D2, D3, D8, D9, L6, L15, C1, C7, C8, C15, BOOT8 and TEST2 govern
this bounded extraction. The original control is main
cd73d9a60f8f5f47a3161783eebd3b54e4f2d256, tree
8f42da2301565979dd6b6289e4a566e96ecf3027, in the Runtime checkout.

Six unchanged record bodies move from c8-effect-checker.evidence into
gravity.effects: legality-records, capability-proof-records, build-effect-log,
replay-requirements, ordering-constraints and residual-effect-report. Their
existing compatibility entry points retain argument lists and interposition.
Inputs are native module/effect graph data and an explicit effect/capability
catalog. Outputs are the same native records, labels and ordering. The stage
depends only on clojure.set; native C8 consumers depend on the stage. Existing
C8 diagnostics, source intake, artifact assembly and identity remain unchanged.

The builders do not authenticate source, grants, providers or performed work.
Legacy accepted and complete labels remain data. The original grant projection
may exist for an absent capability; build grants do not prove performed effects;
replay record ids use the original leaf effect name and can collide across
namespaces. None of these existing limitations is repaired or promoted into
authority by moving a body. Canonical L6/L15/C8 legality, latent/call inference,
handlers, continuation safety and full source admission remain unsupported.

The original capture emits actual effect graphs and reports from retained
paired source/Core/type data. It preserves the pure graph's C8-ORDER refusal
and rejects an actual changed effect with C8-UNDECLARED. Historical producer
identities remain historical, with source/import hashes and Core/provenance
joins checked; no new C7 or C8 admission is inferred. A separate existing
unverified native C7 fixture emits a complete hosted C8 artifact with its real
owner digest; removing the namespace allowance produces C8-UNDECLARED.

Before and after products must compare as complete data and original serialized
text. Required checks include all six body/wrapper conservation, positive and
negative native tests, source/identity and argument forwarding, graph ordering,
effect/capability distinction, cold load, language/ownership, lint, contract
validators, independent review, own applicable CI/artifacts and exact clean
current-main integration preflight. This initial draft snapshot records scope
and the completed original control; later execution receipts belong in the PR.

No native/private authority, frozen Reader, package/archive/release builder,
Attempt30, migration, self-hosting, seed retirement, performance or full Effects
completion is claimed or probed.
