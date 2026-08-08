# SH-09 Effect Legality Fixtures

These paired `.gravity` and `.qst` modules construct normalized typed-operation
requests for the bounded SH-09 effect, capability, profile, provider, build,
replay, and ordering legality kernel.

The accepted fixture covers a pure operation, an explicitly authorized compiler
IR read, and a replay-recorded hermetic build input. The rejected fixture
constructs one request for each diagnostic family exercised by this slice.

The stage-owned adapter now accepts two narrow authenticated SH-08 inputs: the
legacy primitive typed-core shape with pure type facts, and the current C7
capture-free one-hop function-typed core when every declared call and latent
effect set is empty. The latter preserves `[:pending-sh09]` thrown effects
rather than treating them as discharged. Both paths recompute normalized pure
C8 legality products and keep physical source paths outside semantic identity.

The fixtures do not claim effectful adaptation, general effect inference,
completed thrown effects, transitive effect inference, handler checking,
function or module summaries, runtime enforcement, MIR preservation, or an
authenticated C8-to-C9 adapter.

The legality request and recomputed candidate are bounded before identity
comparison: 8,192 nodes, depth 32, width 256, 32,768 scalar serialization
units, 7,000 scalars, and 2,048 collections. This leaf accepts only the `:meta` profile,
`:build` phase, and `:jvm` target. Effectful requests require explicit authority,
an exactly matching grant/resource subject, a hermetic build policy, and a
typed content-addressed replay record when the effect is replay-sensitive.
