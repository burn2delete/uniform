# Incremental build helper names

Move the remaining private incremental-build helpers under
`gravity.build.incremental`, completing the internal group started by
[operations](../build-incremental-operation-names/spec.md) and
[projection](../build-incremental-projection-names/spec.md).

The bounded group is artifact assembly, operation policy, proof projection and
validation. Governing inputs are the [semantic-name rule](../../.specify/semantic-names.md)
and [incremental compilation contract](../../docs/phase-06-compiler-architecture/095-c16-incremental-compilation-design.md).

Acceptance requires complete private caller closure, unchanged helper behavior,
stable public facade/schema/diagnostic identities, actual focused leaf execution,
before/after routing checks and independent review. These compatibility helpers
do not implement a real cache or confer proof/release authority. Stop this
private-group migration at the existing public facade and test-selector boundary.
