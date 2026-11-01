# Hosted seed value adapters

## Contract boundary

Move only the existing string, host-error fact and lookup bodies from Bootstrap
component00587-02 into gravity.runtime. D1/D2/D3/D8/D9, L2, C1/C15,
R1/R4/R11/R12, BOOT8 and TEST2 govern this temporary hosted seed boundary.
R4 distinguishes managed host delegation from portable Gravity semantics.

The intended APIs are hosted-str [args], fail-builtin-error! with an explicit
late failure Var plus plan/callee/exception, and hosted-get with an explicit
late host-error Var plus plan/collection/key and optional default. Results are
the existing string, lookup value, unchanged ExceptionInfo or original legacy
L2 host-error facts. Bootstrap retains private compatibility Vars and argument
lists, compiler authorization, source/admission guards and instruction dispatch.
Existing callers decide when a failure or arity refusal occurs; the stage owns
only the moved value operations and existing error-fact construction.

Dependency direction remains clojure.core to gravity.runtime to Bootstrap.
Supplied Vars are trusted internal operations, not authentication or authority.
Preserve failure-operator lookup order, ExceptionInfo identity, lookup/default
identity, late error selection after lookup failure and all other Runtime bodies.

## Evidence obligations

Compile and execute real paired .gravity/.qst string and lookup programs.
Reject real host-operation failures through the moved diagnostic constructor.
Capture complete before/after source, plan, value and refusal records with
original serialization and recomputed identities. Compile complete public
development-current Core subjects in both spellings before/after in the same
physical worktree. Direct adversarial host callbacks remain separate from
genuine source evidence. Run focused/coupled, ownership/language, docs/roadmap,
governance, contextual lint, independent acceptance, own CI and exact clean
current-main integration preflight. Maintain this early draft as work proceeds.

Initial source base is main27881ab209d69681d29ddd1ecf9ec59aa87da7f1/tree
cff208cc705bbf6c9452dd50b74588a27d0394d3. Scope publication carries no
implementation, acceptance or integration credit.

## Unsupported boundaries

No canonical Gravity formatting, collection, error, numeric or runtime-family
semantics; full L2/R4/R12/C15 conformance; untrusted host-carrier admission;
capability grant; native/backend execution; migration; release; self-hosting or
seed retirement is claimed. Existing legacy builtin source gaps remain honest;
actual provenance stays in enclosing source/plan/control records. Historical
restricted Reader/H6/native/default-pinned routes and source pins remain intact.
