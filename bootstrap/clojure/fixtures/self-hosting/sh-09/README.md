# SH-09 Effect Legality Fixtures

These paired `.gravity` and `.qst` modules construct normalized typed-operation
requests for the bounded SH-09 effect, capability, profile, provider, build,
replay, and ordering legality kernel.

The accepted fixture covers a pure operation, an explicitly authorized compiler
IR read, and a replay-recorded hermetic build input. The rejected fixture
constructs one request for each diagnostic family exercised by this slice.

The fixtures do not claim transitive effect inference, handler checking,
function summaries, runtime enforcement, an authenticated SH-08 adapter, or MIR
preservation.

The legality request and recomputed candidate are bounded before identity
comparison: 8,192 nodes, depth 32, width 256, 32,768 scalar serialization
units, 7,000 scalars, and 2,048 collections. This leaf accepts only the `:meta` profile,
`:build` phase, and `:jvm` target. Effectful requests require explicit authority,
an exactly matching grant/resource subject, a hermetic build policy, and a
typed content-addressed replay record when the effect is replay-sensitive.
