# Hosted seed comparison extraction

## Contract boundary

Move only the five existing equality/ordering helper bodies from Bootstrap
component00587-02 into gravity.runtime. D1/D2/D3/D8/D9, L2, C1/C15, SAFE9,
R1/R4/R11/R12, BOOT8 and TEST2 govern this temporary hosted seed boundary.
R4 preserves the distinction between host delegation and portable Gravity
semantics. The existing compiler/runtime remains the semantic owner.

The public APIs are hosted-equal, hosted-less-than, hosted-greater-than,
hosted-less-than-or-equal and hosted-greater-than-or-equal, each taking the
existing evaluated argument sequence. They return the existing boolean or
propagate the original host failure. Bootstrap retains private compatibility
Vars, minimum-arity checks, error translation, context/authentication, effects,
capabilities and source provenance. Dependency direction remains clojure.core
to gravity.runtime to its Bootstrap consumer; no reverse import is permitted.

## Evidence obligations

Compile real paired .gravity/.qst sources into actual seed plans. Execute legal
unary equality, binary, ternary and variadic comparisons, true/false results and
existing literal values. Reject real illegal arities and numeric operands.
Compare complete source/plan/result/diagnostic records and their original
serialization, with recomputed source and plan identities. Capture complete
public development-current Core controls before and after in the same physical
worktree. Preserve the entire component except five wrappers, all other Runtime
bodies, the dispatcher and loader. Verify late Var interposition and refusal
before helper invocation. Run focused/coupled, ownership/language, documentation,
contextual lint, independent review, own CI and exact clean-main preflight.

The source parent is1821efd7542676f63c68fbb48d13429b69a8c786, the independently
accepted combined candidate in PR707, now integrated as main79165f458/
tree7f79de6071a6b849ee1319fd303f2b86709fe4fc. Its prerequisite is fulfilled.
The original early draft scope carried no implementation or acceptance credit.

## Unsupported boundaries

No new equality, coercion, ordering, numeric-mode, collection or hashing rule;
full L2/SAFE9/R4/C15 conformance; untrusted host-carrier admission; provider or
filesystem authority; native/backend execution; migration; release; self-hosting
or seed retirement is claimed. Existing builtin diagnostics retain their legacy
nil source, with actual provenance in enclosing plan/control records. Historical
restricted Reader/H6/native/default-pinned routes and all pins remain unchanged.

## Implemented extraction

All five bodies moved with only name/privacy changes. Bootstrap retains
private [args] compatibility wrappers; its entire context/authentication
prefix, later string/error helpers and builtin dispatcher remain exact.
The eight preceding Runtime invocation/arithmetic bodies and Bootstrap
loader remain exact. No root inventory captures these five helpers; their
only existing callers are the unchanged builtin dispatcher.

Real plans execute26 positive functions and reject9 actual arity/operand
cases per spelling. Accepted source SHA is
ea0ca92620e88be18874a7a659221d2eb82dfb8cc7b71033332b5f10a5e30244,
with plan sha256:ec61df02adcbc7a6baf045cff4006f614c7a4397c9b6c0ce5b7e05c26643a8a7.
Rejected source SHA is
bb5f7bcd3dfdf8e83415911b41caceeffe394faeba49fd0b10329bf64c634b53,
with plan sha256:4cdf55d75ad0249ff722989d9526095fed76e36c0eb456bc8236a88ef0fdb90a.
All seven full data and original serialized pairs pass: four source/plan/
result/refusal records, arguments and two complete Core artifacts. Actual
before61ed039d and after controls execute in the same Runtime worktree.
Core retains25 nodes, one target read, source8e63 and recomputed Core16ec
in both spellings. No root, pin, mode or returned-field substitution occurs.

Focused tests pass13/618; coupled Reader/CLI/diagnostics/Release/Runtime/
Safety/literal checks66/1436; ownership/language47/1153. Tests cover all
five helper ports, original result classes, host failure kinds, original
exception identity, public metadata and caller arity refusal before helper
execution. Source controls use uniform-runtime-comparisons-control.clj;
full conservation and product checks use the similarly named scripts under
/tmp. Original diagnostic source gaps and unsupported boundaries remain.

Current-main contextual lint passes3 paths/0 warnings/0 blockers. Docs240/19
indexes, roadmap and governance185 pass; terminal lint receipts are archived
outside the checkout. Final exact-source independent acceptance, this candidate's
own CI and clean exact integration preflight remain required. Earlier scope/
parent CI gives no acceptance for this implementation or namespace completion.
