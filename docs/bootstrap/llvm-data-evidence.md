# LLVM scalar lowering data extraction

## Contract and scope

D1, D2, D3, D6, D8, D9, C1, C11, C14, C15, B1, B3, B13, B14 and TEST2
govern this incremental extraction into `gravity.backend.llvm`. Gravity's
existing semantic owner remains
`bootstrap/gravity/src/gravity/backend/b3_llvm_backend_design.gravity`.

The bounded change moves scalar operation legality, ordering, LLVM instruction
rendering, evaluation and unverified module reconstruction from the existing
seed into the stage namespace. Explicit operations and policies cross the
boundary. The full B1/C11 admission, source binding, opaque authority holders,
native execution, process and publication owners remain in Bootstrap.

## Implementation and evidence

Twelve functions and four opcode/type constants move from semantic LLVM
components 002/003/004/005 into the stage. Twenty-two explicit Var ports
preserve callback recursion, order and late reads of policy/opcode values.
Bootstrap imports the stage; the stage imports only clojure.string. Every
public Var records its API, artifacts, diagnostic responsibility, dependencies
and non-admission contract. Existing wrappers preserve both operation-rejection
arities. The full preflight, genuine input checks, source binding, native
execution and publication paths remain with their existing owners.

Two explicit data fixtures exercise a 21-operation straight-line module and a
four-block diamond. They contain signed endpoints, booleans, nil, all five
forwarded forms, truthiness, five comparisons, branches and a phi. The fixture
inputs are never represented as authenticated C11 or sealed B1 packets. The
native reconstruction and the current Gravity B3 operation/module kernels emit
identical LLVM text, operation/block records, semantic results, exit codes and
pending closure records. Actual .ll files are written locally. The raw Gravity
kernel returns constructed-unverified data; its record labels do not constitute
admission. No full input builder or preflight is invoked.

Real negative inputs reject missing operand definitions, unsupported effects,
capabilities and opcodes, non-i64 constants, incorrect comparison arity/type,
source/opcode drift, payload drift, future references and cross-arm dominance.
These helpers return legality reasons; the unchanged owner maps those reasons
to B1-UNSUPPORTED and owns diagnostic publication. These tests do not assert
that a supplied proof or manifest label is authentic.

Five original/extracted files match complete EDN data where applicable and
exact text: two complete capture records, two emitted LLVM files and the current
Gravity source/plan. Both source hashes and declared plan identities recompute.
The existing B3 semantic identity operation gives equal identities for each
pair of unverified lowering records. This is data identity, not artifact
admission. Source/core/pass identifiers, source-map/fact counts and operation
associations survive unchanged. The extraction does not create a new debug map.

The original capture used the original implementation over scope commit
66eed0e5e with the new test untracked. The extracted capture used the working
implementation over that commit. These are ordinary development captures, not
committed-head authority or migration bookends. Read-only reconstruction from
immutable main aa503b03136033a3cc932f555dc1692978d3720e matches the complete
stage, four consumers and Bootstrap require delta, including every unmoved
preflight/authority/native form. Only final consumer whitespace is normalized.

Focused tests pass 6/66. Ownership passes 25/888 and the language gate passes
6/11. Documentation validates 240 documents and 19 phase indexes; roadmap and
workstream governance pass. Independent source review accepts the initial
candidate conditionally. Final exact acceptance, corrected committed lint,
successful final CI and a clean current-main integration preflight remain
pending.

Initial source commit 9a68855a78f11581dac5132df0db54d7cd5e6695 has tree
ee3ef04bb9056af65734824a7b00f88d91adfbba. Exact committed lint found nine
unused local bindings: eight ports unused in one operation-rejection arity
and one inherited current-position binding whose expression was already
unused. The correction prefixes only those local names with underscore and
updates their declared arglists. It retains every expression, Var port,
callback, argument position and wrapper. Both original/extracted and
original/corrected five-file pairs match, source/plan/data identities verify,
six-source reconstruction passes, and corrected focused tests pass 6/66.
The working correction was produced over 9a68855a7; it is not an authority
bookend. Initial local lint remains in
`/tmp/uniform-llvm-data-initial-lint-nonpass`. The initial candidate's static
CI is also nonpassing and must remain historical evidence; no configuration,
timeout, executor or warning waiver is introduced.

The first producer and baseline test stopped at an extra test-file delimiter;
no capture completed. A temporary registration-generation command also stopped
at a reader error before writing files. The adapted registration initially
used the C owner's name; the new LLVM entries were corrected to sh-target-llvm.
All existing owner entries remain unchanged. The direct-child inventory now
counts direct helpers separately from the complete module-owner projection,
so this nested helper does not alter primary catalog counts. Initial generated
consumer trailing blank lines were normalized before source conservation.

Reproduction:

```bash
clojure -M:dev-test --namespace gravity.backend.llvm.pipeline-test
clojure -M:test --namespace gravity.self-hosting.sh01-ownership-test
clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test
```

Local scripts are `/tmp/uniform-llvm-data-{evidence,extract,verify}.clj` and
captures are in `/tmp/uniform-llvm-data-evidence/`. The verifier reconstructs
expected writes in memory and compares full source; it never rewrites the
checkout. Legacy bridge/Gravity builder accounting remains outside the moved
data functions and is not adopted as a stage admission requirement.

## Unsupported boundaries

Data helpers do not authenticate MIR, validate callbacks or policies, issue
proof certificates, run LLVM verification or native code, or admit sealed
artifacts. Reconstructed record labels remain data. Canonical public backend
emit/verify, full B3 conformance, generalized MIR/domain lowering, proof-gated
metadata, native toolchain/ABI support, migration, release, self-hosting and
seed retirement remain unsupported by this extraction.
