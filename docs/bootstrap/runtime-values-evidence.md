# Hosted seed value adapters

## Contract boundary

Move only the existing string, host-error fact and lookup bodies from Bootstrap
component00587-02 into gravity.runtime. D1/D2/D3/D8/D9, L2, C1/C15,
R1/R4/R11/R12, BOOT8 and TEST2 govern this temporary hosted seed boundary.
R4 distinguishes managed host delegation from portable Gravity semantics.

The stage APIs are hosted-str [args], fail-builtin-error! with an explicit
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

## Implemented value boundary

The three bodies now live in gravity.runtime. Only owner names/privacy and
explicit late operation Var parameters change. Bootstrap keeps its original
private argument lists and compatibility Vars. The complete component is
otherwise exact, including compiler-only context/authentication decisions.
The previous13 Runtime bodies, Bootstrap loader and complete builtin,
unary/generic and instruction execution callers are unchanged. The stage
constructs existing L2-BUILTIN-ERROR facts; callers select failures and the
supplied failure operation retains transport/publication.

Actual paired source plans execute17 string/lookup cases and reject3 real
host failures per spelling. The lookup default is evaluated eagerly by the
unchanged caller, including its real division-by-zero refusal. Source SHA
d4220e2f894c30d49768e18b4f9831dc12d47a80fd6e9e0b049a6deb5ba55762
has plan sha256:b79d20fc32f5579794bc4a4a073143d83f0bb9adf1166382b3557cafebc959fb.
Rejected source SHA
e9f4aba864177a2b6321a96f9a84991fa3b9e33067c5e579c3f1ce2c1ace8117
has plan sha256:d45b44e2a55a812efca08711662e4c33f3b363031a79954c9e3f9348886cea9d.

All seven complete data and original serialized pairs pass: four source/
plan/result/refusal records, arguments and both complete current Core
artifacts. Before06de5ac1 and after controls run in the same physical Records
worktree, with25 Core nodes, one target read, source8e63 and recomputed
Core16ec in both spellings. No root, source pin, admission mode or returned
field is substituted. Scripts/products use uniform-runtime-values-* under
/tmp; the generic Core control uses runtime-values-before/after labels.

Focused tests pass18/778; coupled Reader/CLI/diagnostics/Release/Runtime/
Safety/literal checks71/1596; ownership/language47/1153. Direct adversarial
tests preserve lookup/default reference identity, nil/false values, original
ExceptionInfo identity, error-operation lookup only after host lookup fails,
failure-operator selection before message construction, all three private
signatures and late stage interposition. Anonymous unit callback Vars and
host objects are separate from genuine source evidence; no guard or pin
is altered. The original diagnostic source gaps remain unchanged.

Current-base contextual lint passes3 paths/0 warnings/0 blockers. Docs240/19
indexes, roadmap and governance185 pass. Terminal lint receipts are archived
outside the checkout. Independent exact acceptance, this implementation's own
CI and clean exact integration preflight still govern integration. Scope checks
confer no implementation acceptance or complete namespace/runtime credit.
