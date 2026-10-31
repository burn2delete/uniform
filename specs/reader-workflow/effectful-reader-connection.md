# Effectful Reader connection to the lexical build scope

Interface input: `aa03d20d938926a98b591e2b26c3b00e510e8bf9`,
tree `827d44da0348744ac09bc7e46088371bdef9c0ce`. Its independent source/model
PASS receipt has SHA256
`57bb900a4c46d1c941072eda4e0616ce214742c5e7682f635267085013525c07`
(211 checks, 57 negative controls). That acceptance does not establish a
normal producer or runtime capability. The separate module/callee amendment
remains conditional on its affected review. The frozen pure Reader, CI oracle
correction and effectful issuer branches are not edited.

## First actual source action

`bootstrap/gravity/src/gravity/reader.gravity` defines the fixed destination
handler `gravity.reader/read-input`. It declares `:meta`, `:jvm`,
`:build/read-file` and `:fs/read`. Its one-argument function invokes the fixed
`build-read-declared-input` operation exactly once and places that actual return
in the input syntax seed's `:datum`, preserving the remaining seed fields.
There is no host callback/provider parameter, grant flag, hash admission or
copied result in this implementation. It is not yet connected to a normal
producer or compiled/executed as Gravity.

Use a separate handler plan. The existing pure Reader source declares empty
effects/capabilities, its builder selects `sh03-*` names, and
`sh03-reader-validate-plan!` requires empty module/function effects. Widening
that parser plan or pretending the effectful operation is pure would violate
its existing contract. This cut preserves that source and its defaults.

The new handler's public identity is the qualified module/function pair
`gravity.reader/read-input`; its actual function definition name is the
unqualified `read-input`. `stage0-function-table` keys definitions by their
actual normalized `:name`. The Stage2 evaluator also looks up the supplied
callee directly in `plan.functions`. A scope must bind those actual compiled
objects, not manufacture an alias entry that was never compiled from source.

## Existing grant seam and one exact private bridge amendment

The existing two-argument private Reader link receives the capability and
request, not the outer principal. It need not change arity: the trusted host
producer can read `(:grant (#'gravity.build.declared-inputs/live! capability))`.
That existing private function checks exact capability membership, lexical
liveness and the owner thread before returning its original context. Only the
validated grant descriptor is used; no store access or new permission issuance
is needed. Private host access remains outside the source sandbox, as the
issuer's own contract states. A target map or guessed principal cannot replace
this live-capability check. Keep this private coupling bound to the exact issuer
interface and its eventual accepted review; do not expose it as a source API.
Reader will recompute implementation/policy/source identities and preflight
them against the issued grant before effects. Request-supplied implementation
and policy fields remain constraints, not source authentication.

In the private `gravity.build.reader-bridge/call-with-scope`, keep the fixed
public request handler `gravity.reader/read-input`, but select instructions
at `plan.functions['read-input']` and require
`plan.module.module == gravity.reader`. Retain complete source/plan
authentication as Reader's prerequisite and all existing identity,
instruction-membership, native, grant, mode and lifetime guards. Current
code instead looks under `plan.functions['gravity.reader/read-input']`,
which is neither the new definition's actual key nor a function in the
existing pure parser plan. No caller-selected callee/module field, alias
insertion, callback, nil scope or B51 routing change is needed.

This one amendment belongs to OWN's current issuer/bridge writer. This Reader cut
does not edit `invocation.clj`, `reader_bridge.clj`, `declared_inputs.clj`,
`00516`, `00576`, `00588`, `00590`, `00593` or the owning Gravity emitter.
No public command, permission schema or ordinary runtime entry is broadened.
The private unavailable link must remain unavailable until the actual Reader
producer below exists; do not replace it with a direct host-store read.

## Concrete normal-producer sequence after the bridge amendment

1. Build the existing pure parser plan and the separate fixed effectful handler
   plan from their actual sources through the existing authenticated compiler
   seams. Bind their real source/plan identities to the selected policy and
   implementation identity, independently of target-supplied hashes.
2. Add an explicit pure Gravity preparation producer/verifier to the Reader
   source: parse original bytes and derive every original tagged form,
   payload, span and decoded logical input, without executing a handler.
   Its product is pending effectful preparation, not a completed C2 artifact
   or an already-invoked extension. Verify that preparation afresh against the
   original source and selected pure plan; a caller's parsed map is not input
   authority. Reject the whole population on malformed/unknown/disallowed tags,
   source/policy mismatch or an invalid grant before any input read.
3. Derive every closed issuer request from those verified original forms and
   the descriptor obtained through the original live capability. Locate exactly the fixed builtin call in
   the actual handler plan, retain that original instruction object, and create
   one row per original request. Use the original runtime and handler plan
   objects throughout `call-with-scope`; do not copy them into an equivalent
   scope-looking map.
4. Host iteration binds each original request with private `call-with-form`
   in construction mode, then calls the existing private scoped Stage2 function
   evaluator with callee `read-input`, its original native seed and that exact
   registered scope. The Gravity function calls the registered builtin; its
   actual returned seed supplies the result. The bridge retains the original
   receipt and record privately. The normal final Gravity raw-result builder
   must include original input, actual returned seed and every invocation join
   before the existing raw verification/digests/envelope/adapter steps.
5. Fresh verification prepares and verifies the original forms again, rebuilds
   each request independently, and executes the same Gravity handler in private
   verification mode. The builtin uses `verify-original-result!` and does not
   consult the store or provider. Compare complete freshly reconstructed results
   and bindings; candidate data cannot select the mode, create a receipt, or
   reuse a prior acceptance. Construction and fresh verification finish before
   the lexical issuer and scope close. Expiry refuses; durable replay is not
   supplied by this invocation-only service.

The next Reader implementation files are the existing pure Reader preparation
and verifier source, the normal `sh03-reader-resolved-result!` orchestration,
adapter/envelope projection and a fixed private host constructor reached by
the command's Reader link. No actual authenticated preparation API currently
returns the resource population; the standard/pure policies do not admit it.
That API is Reader-owned work, not authority to consume a rejected raw result
as a completed extension artifact. The one private amendment above is the
specific external interface dependency; whole Auth/Build completion is not.

## Models, paired definitions and remaining evidence

The source model evaluates only the selected new handler definition with an
injected in-memory builtin. It checks the exact module/effect contract, one
builtin occurrence, full seed preservation and actual supplied return for
empty, ordinary and Unicode text. This is Clojure-hosted source modeling,
not a Gravity runtime, provider operation or result receipt.

The existing paired `reader-extension-binding/granted.qst` and `.gravity`
files remain the original-input fixtures. Future normal controls bind that
exact logical resource in the declared store and prove one original read and
zero verification reads. Missing/wrong principal/provider/version/input/phase/
lifetime/source/implementation/policy and a late invalid tag/grant must refuse
before every original read. Returned text/receipt substitution, duplicated
execution and stale invocation must refuse before consumption. These are
integration definitions only and remain unrun. Existing pure, standard and
mixed-population definitions are preserved; no duplicate fixture set is added.

No source above is claimed to be a working normal effectful constructor. No
actual CLI, Bootstrap/Reader load, Gravity execution, provider IO, process,
lease, probe, 6343 operation, publication or review dispatch was performed.
The issuer has only the source/model acceptance stated above; the original
genuine goal remains blocked.

Local validation: handler source models passed 2 tests / 11 assertions;
language boundary passed 6 / 11; direct test-source lint reports no errors or
warnings. Document and full-language roadmap structural validators passed.
These checks do not compile a handler plan or establish the actual normal
producer/form/request-to-scope connection.

## Pure resource preparation source increment

The successor to handler cut `ba461f4125d006380d653647d609b8e6fdfeedb0`
adds actual Gravity definitions `reader-prepare-resources` and
`reader-verify-resource-preparation` to the existing pure Reader source.
Preparation invokes the original decoder, scanner and parser on original
source bytes under a fixed, separate resource-only preparation policy. It
derives every resource form, payload identity, span, raw spelling and decoded
logical input before returning any population. Unknown tags, malformed payloads
and empty logical inputs reject the entire preparation. Ordinary forms remain
in the complete parsed graph. No resource handler is invoked.

The pending product includes the original source unit, bytes, policy, tokens,
parsed graph and complete resource population. It is not a C2 result, an
invocation record, a resolved digest or authority to read. Verification freshly
reconstructs it from the original unit and bytes and compares the complete
product. A rejected preparation cannot verify, including against itself.
No caller-supplied parsed graph, policy or population is a producer input.

The existing plan builder and structural validator include exactly the three
new semantic function names (including `reader-resource-forms`), alongside
their historical `sh03-*` selection. Module and function effects/capabilities
remain empty. Existing standard/pure policy validation does not admit this
preparation policy, and ordinary `sh03-read-source-unit` does not gain resource
execution. Pinned migration identities are unchanged and stale for this new
source; development-current construction and migration acceptance remain
separate under the governing development verification contract.

The source models inject decoder/scanner/parser ports and execute only selected
Gravity definitions as Clojure. They do not execute the actual Reader parser,
compile a plan or run Gravity. With the frozen handler models they passed
8 tests / 81 assertions, including full product mutation, original byte/unit
changes, distinct occurrences, late invalid forms, payload legality, error propagation and the actual
plan-builder selection expression. Language boundary passed 6 / 11; focused
test lint has no errors or warnings; document and roadmap structural validators
passed. This is local evidence, not independent acceptance.

The normal command link is still unavailable. The next source changes must
compile the separate effectful plan from `gravity.reader`, use the original live
grant to preflight the entire freshly verified preparation, execute every
original scoped handler occurrence, and join actual returned seeds into raw
C2 result, digest, adapter and envelope construction. Fresh verification must
repeat the original preparation and use the original issuer receipt lookup in
verification mode. Those operations are not implemented or evidenced by this
preparation cut. No actual provider/store read or held local runtime operation,
publication, CI trigger, review dispatch or new worker occurred.
