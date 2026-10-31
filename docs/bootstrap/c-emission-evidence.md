# C17 emission data extraction

## Contract and scope

D1, D2, D3, D6, D8, D9, C1, C15, B1, B2, B13, B14 and TEST2 govern
this partial seed extraction into `gravity.backend.c`. The existing Gravity
owner is `bootstrap/gravity/src/gravity/backend/b2_c_backend_design.gravity`.
The implementation moves C17 expression, source, header and supporting
artifact-record construction out of Bootstrap. It preserves compatibility
arities, call ordering, source/proof associations and deterministic text.

The input is explicit operation and manifest data already checked by callers.
The output is C text and data records, not authenticated backend admission.
The existing B2-MANIFEST exact-comparison refusal remains a real diagnostic;
the existing owner retains diagnostic publication and genuine B1/C11 input
verification. No native toolchain, Gate A/Gate B, migration or release probe
is authorized by this data extraction. The lexical authority holder and full
guarded assembler remain in Bootstrap.

## Implementation and evidence

Seventeen functions and the header constant move from Bootstrap components
022/023/024 into the stage. Ten explicit Var ports preserve the existing
interposition and evaluation order. The lexical authority holder, source pins,
full guarded assembler and native toolchain paths remain unchanged. The stage
imports no compiler or orchestration namespace; Bootstrap consumes it. Every
public Var carries its exact contract, API, artifact and diagnostic metadata.

The focused fixture contains 22 explicit scalar operations: signed endpoints,
booleans, nil, all five forwarded forms, three truthiness cases, all integer
comparisons and a conditional join. These are data fixtures, not authenticated
MIR or B1 packets. Emission writes actual program.c and program.h files in the
local evidence directory. The current Gravity B2 implementation is compiled
and its operation, source/header, build, ABI, runtime, debug and proof record
kernels execute on the same data and compare equal. Its bounded builder
receives nil and genuinely rejects B1-INPUT/:exact-sealed-b1-c-packet. No
accepted packet, authority token or preflight result is fabricated.

Original and extracted captures match as full EDN data and exact serialized
text in four paired files: all native output/diagnostic records, current
Gravity source/plan, C source and C header. Both source hashes and declared
plan identities recompute through the unchanged canonical identity protocol.
Read-only reconstruction from main 41ab1c357e4d96a510fae4cf5a46e903fe340510
matches the entire stage, all three consumer files and the Bootstrap require
delta. This checks every unmoved form, including the guarded assembler and
lexical holder. Before capture used the original implementation on scope
commit 909d7f032 with the new test untracked; after capture used the working
extraction over that commit. These are ordinary development captures, not
committed-head authority or migration bookends.

Focused regressions pass 5 tests/155 assertions. Ownership passes 25/888 and the
language gate passes 6/11. Documentation validates 240 documents and 19 phase
indexes; roadmap and workstream governance validators pass. Independent exact
candidate acceptance and successful final candidate CI remain required.

The initial focused command used the self-hosting runner, which correctly
rejected this new nested standalone namespace; the existing development
runner executes it without changing shared routing. The first ownership
check exposed a direct-child count that included a nested helper. It now
counts direct helper paths for the direct-child projection while retaining
the complete helper/module ownership checks. No catalog counts changed.
The first lint observation ran before the implementation commit, so its
base-to-HEAD source mapping did not cover the working require-line insertion;
two inherited Bootstrap warnings did not map. A third warning was a new unused
test import, which was removed. Committed-range lint against main 41ab passes
all eight changed Clojure/EDN paths: 3491 unchanged baseline warnings and zero
blocking findings. The nonpassing working-tree observation remains at
`/tmp/uniform-c-emission-working-lint-nonpass`; the passing committed receipt
is `/tmp/uniform-c-emission-context-lint-56177c12`.

## Main composition

Source commit 56177c12a707945a86c097334391ece6c4cbdbe8 has tree
2fae0b148ddc89773709ead6d8192ebbc17fe89c. Main advanced to
aa503b03136033a3cc932f555dc1692978d3720e by removing an unused UEP test helper.
A normal merge e23ccd37e0ba9c57e8df4625041bde6ebd893530 incorporates that
single unrelated deletion without conflicts. All C emission source, tests and
evidence remain byte-identical to 56177c12a. This composition does not claim to
remedy any earlier CI failure. Final candidate review, checks and exact clean
integration preflight must bind the final commit and current main separately.

Reproduction:

```bash
clojure -M:dev-test --namespace gravity.backend.c.pipeline-test
clojure -M:test --namespace gravity.self-hosting.sh01-ownership-test
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Local producer, verifier and extraction scripts are
`/tmp/uniform-c-emission-{evidence,verify,extract}.clj`; captures are in
`/tmp/uniform-c-emission-evidence/`. The verifier captures reconstructed writes
in memory and compares complete source text; it does not rewrite the checkout.

## Unsupported boundaries

Canonical public backend `emit`/`verify` remain proposed. This slice does not
establish full B2 conformance, authenticate supplied facts or callbacks, issue
proof certificates, execute C, verify the configured ABI/toolchain, implement
general MIR or domain IR lowering, or complete self-hosting or seed retirement.
Historical data labels such as proven-safe and proof-authorized are copied
records whose authority still depends on the unchanged upstream gates.
The legacy unknown-op expression fallback still returns zero; it is not a
validator. The native exit-code helper still returns nil for nil, whereas the
Gravity helper returns zero. That pre-existing difference is outside valid
admitted process results and is preserved explicitly, not claimed as parity.
