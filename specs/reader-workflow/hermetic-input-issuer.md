# Hermetic Reader input issuer and original-result boundary

This is the bounded Auth/Build source response to
[the effectful provider handoff](effectful-provider-boundary.md), at Reader
handoff `f07efdf4ea7060f1cfd6ef8e4295b675c07b84b2`, tree
`5c09fd19f4689048715e3cd3917c401f9494d5f6`. Governing contracts are C2, L12,
L15, SAFE10, PKG2, PKG6 and D9. It does not change Reader production, the pure
Reader route, Stage2 builtins, task completion or integration status.

## Entry decision and actual missing producer

Choose the fixed hermetic declared-input provider. Auth owns issuance and
grant validation; Build owns immutable declared text inputs and result capture.
The provider id is `gravity.build/hermetic-inputs`, version `1`, build-only,
explicitly supporting `:meta`. `:filesystem/read` describes the Reader service;
L12 records that service during compilation as `:build/read-file`; both require
`:fs/read`. This relation grants no runtime effects or filesystem access.

The traced existing interfaces do not supply the required trust root:

- `gravity.capability-validation.contract` explicitly denies grant and provider
  trust authority. Its projection cannot be used as an issuer.
- `gravity.build.incremental.artifact/source-artifact` builds records from
  supplied stage/source inputs. Its replay metadata is not a provider result.
- `gravity.cli` is presentation only. The actual bootstrap `-main` in
  `components/foundation/00942-print-diagnostic.clj` dispatches commands through
  `cli-entrypoint/run!`; its inspected special Reader selection handles only
  the existing development-current Reader choice. It does not produce this
  local build authorization or declared-input snapshot.
- The Reader dispatch leaf's separately supplied `:invoke` and seven-field
  grant are trusted-host inputs, not an implementation-owned issuance route.

Two smallest concrete entry alternatives remain:

1. **Recommended: explicit local host build invocation.** Add an owning
   `gravity.build.invocation/run-declared-input-build!` entry, reached from the
   actual bootstrap command dispatcher after explicit local build permission
   is parsed independently of target source. Auth intersects that permission
   with package/workspace/profile/source requirements; Build captures only its
   declared input set. This host producer calls the private issuer and keeps
   original construction and fresh verification inside its lexical lifetime.
   Required inputs are principal, selected provider/version, `:meta` profile,
   target, build phase, invocation lifetime, logical input scope, read effect
   and capability, required audit/recorded replay, policy id, fixed handler and
   its current implementation identity, plus immutable resource/content rows.
   A target-source map, projection, digest, or serialized receipt is never this
   permission. The command/authorization producer is absent and is NOT added
   or simulated as production authority by this cut.
2. **Trusted embedding host entry.** An existing embedding application can
   supply an Auth-owned opaque permission from its own explicit local user
   authorization and invoke the same lexical Build producer directly, without
   a CLI addition. This requires identifying that actual host owner/entry and
   its permission issuer; no such embedding entry was found in the traced cut.
   A public factory accepting a supplied `:trusted?`/`:allowed?` map would not
   implement this alternative.

Neither choice requires completing the whole Build system. The next missing
production edge is this explicit host permission/invocation producer, not a
generic permission question. This source cut does not expose a source-callable
factory or claim that a private function by itself establishes a trust root.

## Minimum implemented source boundary

`gravity.build.declared-inputs` implements the separable private
`call-with-authorized-inputs` seam, reserved for that trusted host producer.
Only model tests call it by private Var access. Trusted Clojure host code and
private-Var/reflection access are outside the Gravity source sandbox; Clojure
privacy is not claimed as isolation against a malicious host.

It validates and copies native inputs, creates an opaque Object capability,
retains immutable declared text rows, and owns an identity-keyed original result
store. The callback is lexical trusted host control flow, never a source-picked
provider. Source/data-facing operations cannot issue a capability:

| Operation | Input | Output / failure |
| --- | --- | --- |
| `preflight!` | Live capability, complete native vector of requests | Nil after all request grants validate; no input lookup or result capture. |
| `read-input!` | Same live capability, one exact request | Opaque receipt and original DATA record, with actual immutable returned text and computed content hashes. Fixed in-memory lookup only. |
| `verify-original-result!` | Same capability, freshly derived request, original opaque receipt, submitted record | Original result on exact request/record equality; never accesses inputs or invokes a provider. |

Grant joins are principal, provider/version, profile/target, phase, lifetime,
scope, effect/capability, audit/replay, handler, implementation and policy.
Request joins add source/content identity, form and payload form, span and
logical input. Reader still owns authentic source/form/span/handler derivation;
this issuer records those validated joins, not a claim that arbitrary supplied
source hashes authenticate source. The stored result binds actual input content
and returned text. Hashes are provenance DATA, never authority.

Map/vector/set native classes, metadata, scalar classes, Unicode, cycles and
callbacks are guarded before collection lookups, hashing or equality. Fresh
containers break caller-owned native backing-array aliases. Receipts are exact
Object identities, not deserializable grant/receipt records. The capability is
bound to the acquiring host thread and active invocation. Wrong invocation,
phase, principal, provider/version, profile, scope, policy or implementation
refuses. A fresh invocation with identical DATA cannot use an old receipt.
Normal return and exception invalidate the capability and clear original
results. A retained/future child thread cannot extend the lifetime.

Inputs are text, including empty text. There is no filesystem, environment,
clock, random or process provider, lazy resource acquisition or arbitrary
callback in a read. Missing/duplicate declared resources and malformed grants
fail before entering the body. A read failure creates no successful result.
Original-result tampering fails with `L15-CONTRACT` and a stable reason; the
Reader owner may contain it as its owning C2 diagnostic without treating it as
an accepted extension value. Original records are private invocation evidence;
publishing content/secrets and persistent cross-process verification are not
implemented.

## Fixed Stage2 bridge handoff; no builtin edit

Accounting's existing Stage2 owner retains the `00588` builtin/native-carrier
cut. Neither `00588` nor `00590` is edited here. Proposed closed bridge contract:

1. Host-owned context retains the live capability and the Reader's fully
   validated complete request population. Preflight all tags, registrations,
   implementation identities, source/form joins and grants before the first
   effect. Preflight success is not a reusable source acceptance flag.
2. One fixed operation, `:build/read-declared-input`, takes one native logical
   resource String from the selected fixed Gravity handler. The host bridge
   derives the exact request from its current authenticated form context; it
   does not accept caller callbacks, provider choices or a grant map as proof.
3. The original bridge invokes `read-input!` once and returns only the text to
   Gravity. It retains the opaque receipt and original DATA record in the host
   invocation sidecar. Opaque host objects are NOT added to ordinary Gravity
   serializable/native carrier values.
4. Fresh Reader verification derives the request anew from original inputs,
   obtains the original sidecar receipt, and calls `verify-original-result!`.
   It supplies the recorded text at the fixed operation instead of redoing the
   provider read. Wrong/stale/tampered joins refuse before consumption.
5. Construction and verification must finish within the same authorized host
   invocation. A new process, expired invocation or serialized sidecar cannot
   recover authority. No callback, source map or content digest reissues it.

The bridge symbol/ABI and original sidecar transport require the actual
Accounting/Reader interface agreement before implementation or composition.
The above is an exact proposed input/output/failure/replay contract, not an
installed builtin or normal Reader integration claim.

## Actual scoped ABI decision after Accounting handoff

Consumed `/tmp/acc09-stage2-ownership-abi-handoff.edn`, SHA256
`47caeed296174de3ee68492bff7bb13b6b4576c6fd25a68ccf78b6a2713c937a`.
Its immutable subject is `9e733a377e67faba184171b817b809ef8087b3ff`, tree
`1425714ec50aa4bb7624e3630e7b9718b3ea2d02`. The handoff does not establish
a currently usable/clean ACC09 checkout or exclusive ownership of `00590`.

Reading those Git objects confirms the following current behavior:

- `00588` has legacy `[plan callee args]` and `[runtime plan callee args]`
  arities. Neither receives instruction identity or invocation scope.
- `00590`'s scoped generic call receives
  `[runtime plan env instruction function public-invocation-scope]`. It
  evaluates arguments left-to-right once, rejects non-tail recur, and passes
  scope to compiler-internal calls. Its ordinary route passes only four
  arguments to `00588`, dropping scope.
- `00593`'s scoped function entry passes every non-module, non-nil scope to
  `p15-s23-sh07-b51-assert-scoped-function-entry!`. In `00945c`, that check
  requires the actual issued B51 context/receipt/scope identities. An arbitrary
  new Reader Object scope will fail it. A module scope is a different contract
  and must not be used to conceal a Reader grant.

**Select one dedicated Reader build scope, one fixed operation.** The source
builtin name is `build-read-declared-input`; its operation identity is
`:build/read-declared-input`, source arity exactly one native String, result
exactly String. The future trusted host producer registers a fresh opaque scope
in a private identity registry for the existing lexical issuer invocation.
Its retained association binds the live capability, original runtime and plan
identities, source/handler implementation identities, approved handler's actual
builtin instruction identity, original request/form and result sidecar. A
scope-looking map, `:compiler-internal-caller`, metadata or a copied grant has
no registry membership and cannot authorize it.

Minimum proposed owner delta, NOT edits in this candidate:

1. `00590`: preserve argument evaluation and both existing special routes.
   On the ordinary route, forward the original instruction and exact original
   scope with the already evaluated arguments:
   `(p15-s23-stage2-runtime-invoke-builtin runtime plan function args
   instruction public-invocation-scope)`. Do not copy/reconstruct scope or
   reevaluate arguments.
2. `00588`: add this six-argument host arity. The three-argument compatibility
   wrapper keeps forwarding to four; four forwards to six with nil instruction
   and nil scope. Existing cases and exception containment remain unchanged.
   Only the new fixed case consumes instruction/scope. It checks exact source
   arity with existing `L2-BUILTIN-ARITY` diagnostics, native String/carriers,
   then private registry membership, liveness/thread, original runtime/plan,
   actual approved handler occurrence, grant and exact current form/input joins.
   Only then may it call the issuer's fixed read or original-result lookup.
   Nil legacy scope refuses this operation; it must not fall back to ambient
   issuer context. Carrying instruction as well as scope is necessary because
   scope alone cannot authenticate the handler's original builtin occurrence.
3. `00593` scoped entry also needs an owning agreement: privately recognized
   Reader scope must go through its own authenticated plan/function-entry
   check. All other non-module scopes continue through the unchanged B51
   check. Recognition is private identity membership after native guards, not
   a caller field or boolean. No new Reader scope is sent through the B51
   receipt issuer or given its authority. Public nil-scope entry behavior and
   module handling stay unchanged. This means a two-file forwarding patch
   alone is NOT a complete scoped entry implementation.

The trusted invocation producer enters the existing scoped function evaluator
with that original registered scope, keeps original construction and fresh
verification inside the issuer lifetime, and unregisters scope on return or
throw. It must not use the ordinary public function wrapper that supplies nil.
For construction, the fixed bridge captures the original record/opaque receipt
in its private sidecar and returns text. For verification, the host-owned
verification context selects original lookup for that same operation and exact
original request; source code cannot select mode or supply a receipt as proof.
There is no provider IO on verification. Wrong mode/join, missing or stale
receipt, changed implementation/result and expired scope refuse, with original
`ExceptionInfo` preserved. Other host exceptions retain existing builtin error
containment; no successful event is fabricated after a failure.

The source/compiler registration also remains an explicit owning dependency:
the current builtin set is in `foundation/00516-defn-form.clj`. Merely adding a
runtime case does not admit a source builtin or establish its type/effect
contract. The approved fixed handler occurrence must carry the real
`:filesystem/read` / `:build/read-file` and `:fs/read` legality through compilation.
It cannot enter the compiler-internal empty-effect/empty-capability route or
be labeled pure to pass it. No existing internal provenance/version/caller or
native representation check is weakened; Native preflight is not authority.

Before implementation, Accounting must coordinate `00588` with its frozen
Native/Set candidate and establish the `00590`/`00593` writer agreement with
Reader. The proposed scope-entry and fixed compiler registration must be
agreed together, rather than pretending the original ABI already transports
authority. This document supplies the closed proposal for that agreement; no
scope registry, new runtime arity, function-entry routing or builtin admission
was implemented, evaluated or accepted here.

## Validation and limits

Model tests inject the private trusted-host entry with immutable in-memory
inputs. They check original lookup without provider replay, wrong identity and
expired lifetime, source/form/implementation/result substitution, full request
preflight refusal before input access, native callback/carrier/cycle guards,
backing-array mutation, unwinding and legal empty content. They load neither
Bootstrap nor Reader, invoke no Gravity runtime and perform no resource or
provider IO. This is issuer/store model evidence, not genuine Reader evidence.

The private issuer and its tests are source implementation only. Production
host permission issuance, source/bridge agreement, normal effectful Reader
integration, durable result custody, independent acceptance and genuine
execution remain open. The original blocked native goal and historical
attempts remain unchanged. No review dispatch, publication or runtime resource
operation is authorized or performed by this increment.
