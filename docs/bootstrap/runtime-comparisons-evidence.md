# Hosted seed comparison extraction

## Contract boundary

Move only the five existing equality/ordering helper bodies from Bootstrap
component00587-02 into gravity.runtime. D1/D2/D3/D8/D9, L2, C1/C15, SAFE9,
R1/R4/R11/R12, BOOT8 and TEST2 govern this temporary hosted seed boundary.
R4 preserves the distinction between host delegation and portable Gravity
semantics. The existing compiler/runtime remains the semantic owner.

The proposed public APIs are hosted-equal, hosted-less-than, hosted-greater-than,
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
accepted combined candidate in PR707. That PR must integrate before this slice.
This early draft scope has no implementation, acceptance or integration credit.

## Unsupported boundaries

No new equality, coercion, ordering, numeric-mode, collection or hashing rule;
full L2/SAFE9/R4/C15 conformance; untrusted host-carrier admission; provider or
filesystem authority; native/backend execution; migration; release; self-hosting
or seed retirement is claimed. Existing builtin diagnostics retain their legacy
nil source, with actual provenance in enclosing plan/control records. Historical
restricted Reader/H6/native/default-pinned routes and all pins remain unchanged.
