# Stage0 instruction lowering

## Scope and governing contracts

This slice moves the existing temporary Stage0 expression, collection, let,
function and instruction-summary bodies from Bootstrap into `gravity.lowering`.
It starts at frozen source `6bd0265992ad294d05fbf6611b1cf2315a4e5c14`, tree
`ad45ae152ad95f7fb6e322f1389cac339d207e39`, containing published Main
`9f201518f400b118a7cf60533d6cebb94f5bcd68`.

D0, D1, D2, D3, D6, D8 and D9 govern the extraction. C1 governs explicit pass
boundaries and preservation, L2 and C6 govern evaluation order and lowering,
and C14 identifies the verified input and evidence obligations that this
temporary native instruction format does not satisfy. C15, BOOT1, BOOT8 and
TEST2 govern diagnostic compatibility, seed organization and evidence.

This is the existing Clojure-hosted instruction compiler. It does not turn
macro-expanded native data into authenticated Core, MIR or backend input.
Gravity/Uniform remains the destination implementation language.

## Boundary and dependency direction

The intended new APIs are `compile-native-expression`,
`compile-native-collection`, `compile-native-let`, `compile-native-function`
and `native-instruction-summary`. They consume caller-owned native module,
local scope, form or function data, or an instruction, plus explicit live
operation Vars where the existing implementation reads Bootstrap owners.
They produce the existing instruction maps, function binding/instruction map
or opcode-frequency map. They perform no source IO, authentication, execution,
target selection, provider selection or certificate construction.

Bootstrap keeps its original function names and arities as adapters. It retains
function-header parsing and validation, function-table formation, source and
module intake, all profile/effect/safety/package/tooling preconditions,
whole-plan assembly and plan identity, and instruction execution. The stage
does not import Bootstrap, CLI or release, discover owners with `resolve`, or
interpret supplied callbacks as proof authority.

The stage owns the existing expression unknown-symbol/unknown-form and
host-reflection decisions and let-vector/name checks. Bootstrap retains
diagnostic construction and publication, with the original `L2-UNKNOWN-SYMBOL`,
`L2-UNKNOWN-CORE-FORM`, `P4-HOST-REFLECTION` and `L2-LET-BINDING` identifiers,
payloads, message text, call order and exception behavior. Main-entry and
function-header refusals remain with the caller.

## Preservation and artifact evidence

The extraction must preserve all original branches, map/set sorting, lexical
scope progression, initializer-before-binding semantics, eager and lazy
realization points, callable selection, late recursive/helper/catalog/failure
Vars, source paths, metadata and opaque values. It adds no new lowering rules,
numeric semantics or admission conditions. No unrelated Runtime or plan
assembly body is in scope.

Before moving bodies, record complete actual source observations through
`macro-source-artifact`, its actual module and expanded forms, and
`stage0-compiled-core-plan`. Execute accepted plans with the existing Clojure
interpreter. Compare complete before/after plans, source digests, deterministic
plan IDs, stdout and real source refusals. Exercise both `.gravity` and `.qst`.
Do not replace Reader operations, bind authenticated products, invoke private
source factories or claim a request map alone is compilation.

Focused regression tests must cover the complete five-body group, positive
instruction shapes, genuine malformed/unsupported source, recursive operation
interposition, ordering, exception identity and the unchanged existing native
Lowering API. Add one whole-namespace native CI command and preserve every
existing command. Run the language-boundary gate and proportionate affected
tests, document/roadmap checks and lint. Independent source/adversarial review
and the exact provider/CI audit are required before integration eligibility.

## Current status and unsupported boundaries

The five bodies are now in the stage, with Bootstrap adapters at their original
call sites. Seventeen earlier Lowering bodies are retained and the public API
now contains 22 functions. The exact new arglists are:

| API | Arguments |
| --- | --- |
| `compile-native-expression` | `[operations module locals form]` |
| `compile-native-collection` | `[expression-var module locals form]` |
| `compile-native-let` | `[failure-var expression-var module locals bindings body]` |
| `compile-native-function` | `[expression-var module {:keys [name params body] :as definition}]` |
| `native-instruction-summary` | `[summary-var instruction]` |

The expression operation map contains only `:compile-expression`,
`:compile-collection`, `:compile-let`, `:builtin-functions` and `:fail!`.
Each original callable/catalog reference is read at its original use. Summary
recursion selects one function per `map` invocation and retains it across that
invocation's children; nested invocations select their own current root.

Before extraction, six actual plans compiled and executed: hello and core-app
in both extensions, plus a collection/let/local-call/conditional source in both
extensions. Five invalid sources reached expression lowering and refused for
unknown symbol, unknown form, odd let bindings, non-symbol let name and host
reflection. All eleven complete observations, including plans, IDs, paths,
source hashes, stdout, failures and call counts, matched after extraction.
The baseline receipt SHA256 is
`c6d375968822a8a4387810abded26b406b1a9e81e30ce36dcf65a81fc493e151`.
The first extracted receipt SHA256 is
`bebabe75133e6a8614aa351c8cfe7b130c3b870886f988c357b9129b07f379e9`.
The original-body receipt SHA256 is
`cac10d5d9ef77aa1d7e979c700afaef83ff26d15d9730b3e9b445f9d4883c387`.
These are development observations, not migration evidence.

The final source-observation receipt, after contract metadata correction,
also matches all eleven baseline observations; SHA256
`2658340f8c1b9181ef9eaa24f1287d466bac70ed187daf8ca91cec250b9ccbe4`.
The conservation receipt SHA256
`b0ab8f02ec6f21e95faf322e54eb1ce1609d36482f732877ebb793cd245ef573`
binds 17 byte-identical earlier bodies, five complete port-substituted bodies,
unchanged unrelated component text and 24 identical earlier CI vectors.
One new whole-namespace command is contract 25, with the existing 2g heap and
180-second process timeout. No workflow or runner configuration changes.

Focused validation passes:

- new instruction tests: 13 tests, 142 assertions;
- earlier native backend tests: 12 tests, 105 assertions;
- five native Lowering contract/reconstruction tests: 5 tests, 137 assertions;
- affected package/build/Safety source regressions: 24 tests, 614 assertions;
- language/ownership boundary: 48 tests, 1,202 assertions;
- CI harness: 54 tests, 678 assertions;
- cold Lowering load: 22 exact APIs and arglists, without Bootstrap/Diagnostics;
- structural docs and full-language roadmap validation.

The held sixth Lowering pipeline test was not invoked. Initial development
runs exposed a contract arglist missing its parameter destructuring and test
callback-capture mistakes. These were corrected without changing lowering
behavior; the earlier logs remain historical evidence. Twenty generated check
files were moved to a byte-preserving archive, recorded by SHA256
`9003e88cd67548b16d001c86aacf4dc53dee461de4ba33e4f9d17b85f813d187`.

This authored capture does not establish independent acceptance, current-Main
composition, complete current-context lint, provider evidence or hosted CI.
Those later observations belong to their exact commit/base/provider/run tuple
and are maintained with the PR; acceptance does not transfer across tuples.
The complete Stage0 pipeline is still not extracted: header admission,
orchestration and execution retain the explicit owners listed above and receive
no extraction credit from this change.

Independent semantic review of authored `490ea964f58d82220ffefd6fc47f4c769b3af2a9`
found a P2 metadata omission: this document named D0 while the machine contract
omitted it. The correction adds D0 and a regression assertion, without changing
any implementation body or instruction/plan semantics. The preceding local
counts describe the authored capture; checks and independent review for the
corrected successor are recorded separately with their exact identity.
The original ten-path writer lease governs implementation. This manually
authored external source has no Main, Ready, controller, service or recovery
role; final current-base preflight and integration remain with the designated
integrator.

Native instruction maps do not contain canonical node identities, complete
per-instruction spans, type/effect/ownership/safety proofs or verifier results.
Existing plan-level paths, source hashes and plan IDs are the preservation
boundary, not proof of full C1/C6/C14 conformance. This slice claims no canonical
Core or MIR admission, target emission, native execution, new unsafe support,
performance, migration, release, self-hosting or seed retirement. Held Reader,
authenticated source/Core builders, private factories and source pins remain
unchanged and uninvoked.

Existing Stage0 limitations are retained explicitly: `if` does not enforce
exact source arity, `quote` uses its second item without a shape guard, and
map/set traversal uses native `pr-str` sorting rather than source ordering.
The summary can count loop/recur/throw/compiler-module opcodes that this
expression compiler does not emit. The builtin vocabulary includes
`build-read-declared-input`, but this route supplies no corresponding usable
declared-input service. Compatibility probes record these gaps without
executing that operation. They do not grant L2/C6, effect-sensitive collection
ordering, filesystem or runtime support. Function arity and builtin execution
errors remain caller/executor-owned.
