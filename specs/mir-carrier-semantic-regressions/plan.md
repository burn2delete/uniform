# MIR carrier regression plan

Spec: [spec.md](spec.md). Canonical task record: [tasks.md](tasks.md).

1. Reproduce the two obsolete scalar-carrier assertions on the exact #378 head.
2. Replace only accounting-contract expectations; add positive width boundaries
   and late invalid scalar/native and zero-realization sequence controls.
3. Execute the scoped scalar namespace and existing no-project-quotas preflight
   selector, plus applicable repository boundary checks. Do not run restricted
   consumers or repin authenticated producer identities.
4. Obtain independent review of the immutable candidate; publish a draft until
   review and integration requirements are met. Record merge separately.

Dependency: current #378 carrier contract, initially inspected at
`f487d977fd86f7b35bc60a5d76c9240cd9cbc3ba`. The whole producer/consumer chain stays
held independently of this regression task. Preserve earlier failing receipts
as historical evidence, not current accepted results.
