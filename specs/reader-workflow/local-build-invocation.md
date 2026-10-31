# Explicit local declared-input build entry

This successor implements the host-entry edge selected in
[the issuer contract](hermetic-input-issuer.md). Parent
`283edcfe195e6bcd2b8131bd2f4c888bcb80a5b6` remains frozen history. The initial
host-entry commit `80ff09ca` changed no Reader/Stage2 production. Its historical
pending-bridge statements below are qualified by the subsequent implementation
section. Reader production remains untouched. Contracts: T1, C2, L12, L15,
SAFE10, PKG2, PKG6 and D9.

## Actual entry and command authority

The actual bootstrap `-main` delegates to `gravity.cli.entrypoint/run!`, then
`gravity.cli.dispatch/dispatch!`. The fixed `build` branch now calls
`gravity.build.invocation/run-declared-input-build!` directly. It does not
resolve a caller-selected operation or use the bootstrap resolver as grant
authority. Help documents the narrow command and its missing Reader bridge.

The command shape is:

```text
gravity build --reader-inputs EDN --principal PACKAGE --profile meta --target jvm
  --allow-read KEY [--allow-read ANOTHER-KEY]
  --package-policy EDN --workspace-policy EDN --format json
```

The trusted local invoker supplies these process arguments. Each `--allow-read`
is explicit permission for one logical resource, not a file path or pattern.
The parser accepts native String arguments only, validates Unicode, rejects
duplicate/unknown options and extra positional arguments, and requires explicit
principal, `meta` profile, `jvm` target and JSON format. It derives permission
only from outer argument pairs. The EDN reader accepts one data value, without
evaluation, arbitrary tagged readers or trailing data. Text inside EDN or target
source cannot introduce an argument, grant or callback.

Package and workspace policies are separately supplied HOST command inputs.
They are not automatically loaded from the target/project file or inferred
from a compatibility projection. Each must contain all fields:

```clojure
{:principals #{"package/example"} :profiles #{:meta} :targets #{:jvm}
 :provider gravity.build/hermetic-inputs :provider-version "1"
 :phase :build :lifetime :invocation :scope #{"schema"}
 :effects #{:build/read-file} :capabilities #{:fs/read}
 :audit :required :replay :recorded}
```

The command's principal/profile/target/provider/phase/lifetime and complete
requested permission scope must be permitted by BOTH policies. Source
requirements are another narrowing layer with the same shape. Missing local
permission or either policy denies before entering the issuer. Denial or
narrowing cannot be expanded by source requirements. This command does not
authenticate arbitrary external package/workspace policy files: the explicit
local host supplies the constraints, as well as the permission. A future route
using an external trusted policy store must establish that origin separately;
it cannot replace an absent policy with a project map.

## Declared data and lexical invocation

The `--reader-inputs` value has exactly these fields:

```clojure
{:source {:path "memory/example.gravity" :text "target source text"}
 :source-requirements policy
 :inputs [{:resource "schema" :content "declared immutable text"}]
 :implementation "sha256:..."
 :policy-id "sha256:..."}
```

The two hash strings require the existing complete SHA-256 shape. They state
requested bindings, not implementation or source authentication. Actual Reader
source/form/implementation authentication remains the fixed bridge's job before
any resource lookup. Logical source path/text are retained command data; the
entry does not open a source path, parse Gravity, evaluate a handler or derive
permission from source contents. Text is Unicode validated before policy/data
lookups. Native copied containers, callback refusal and issuer input rules
remain in force.

After outer permission, both host policies, source constraints and structural
input checks succeed, the entry constructs the fixed build grant and calls
the private issuer. The capability comes from that implementation-owned host
boundary, not a serialized record, digest or `:trusted?` flag. Its body calls
one fixed private Reader link with the capability and declared request. It
accepts no caller callback or alternate provider. The capability is not printed
or returned by the command. Construction and fresh verification must occur in
that same lexical body; return or exception invalidates it and clears original
results. The existing same-thread and original-receipt rules remain unchanged.

Trusted Clojure host/private-Var access remains outside the Gravity source
sandbox. This new host API is not registered as a source builtin or an FFI
factory. Calling it with attacker-controlled arguments from another host entry
would be a separate trust-boundary violation, not authority granted by data.

## Precise current production result

The fixed `reader-build!` link currently refuses with `T1004`, reason
`:reader-effectful-bridge-unavailable`, exit code 4. It does not call the host
input provider and label the result as a Gravity handler result. This is an
implemented host-entry/issuer lifetime with an explicit missing downstream
link, not a successful effectful Reader command. There is no production success
report yet. Command usage failures use T1006/exit 2; missing or denied authority
uses T1003/exit 6. JSON failure output is a closed implementation-owned schema
and excludes source/input contents, supplied paths, policies and capability
objects. Existing non-build command diagnostics and exit behavior are retained.

The pending bridge is exactly the agreed scope/occurrence sidecar proposal:
ordinary `00590` forwarding must retain original instruction and scope,
`00588` needs its context arity, `00593` needs a distinct authenticated Reader
entry preserving B51, and source registration must carry real effect/capability
legality. Accounting/Reader writer and composition agreement remain required.
No empty-effect compiler-internal IO route or ambient grant fallback is used.

## Model evidence and limits

### Subsequent fixed bridge source under closed agreement

Accounting agreement SHA256
`316a24d91e9566a5c8af0b9cc41aee74ba89a31d8e18411389c79d50aebcb4c0`
designated this engineer as single bridge writer. Preservation commit
`f4ab55a9c511237e23dd5c53e00c23e15feecb85` first composed six differing
Native/Set paths from ACC09 `9e733a37`; all 14 agreement pins then matched
exact modes/blobs. This is source conservation, not fresh execution or
acceptance. Other incoming source and the new host entry were preserved.

The new `gravity.build.reader-bridge` implements private lexical identity scope,
whole request/grant preflight, original runtime/plan identity and fresh native
snapshot checks, approved handler instruction membership, host-selected current
form and construction/verification mode, original-result capture and private
receipt lookup. Source fields cannot select mode or supply authority. The
issuer's default native input schema stays strict; the private plan snapshot
schema additionally admits native symbol-keyed tables, immutable numeric and
character literals and native PersistentList source forms. Foreign callbacks,
metadata and cycles remain refused.

`00590` forwards original instruction and scope without reevaluating arguments.
`00588` retains the agreed three/four/six-argument compatibility and one fixed
case. `00593` checks native scope shape before module lookup, then authenticates
privately registered Reader scopes separately from B51, module and nil routes.
These changes do not treat Native representation validation as grant authority.

The fixed name and String/effect/capability contract are in `00516`.
Compile-time legality is checked in the Clojure seed `00576` and owning Gravity
emitter: `:meta`, `:jvm`, declared `:build/read-file` and `:fs/read` are required.
The provider also records the service effect `:filesystem/read`. No
empty-effect compiler-internal route is used, and existing provenance and
Native/Set code remain preserved outside the named bridge delta.

The private scope creator requires an already-authenticated original Reader
plan and form requests from trusted host integration. It does not authenticate
an arbitrary plan because its hash or metadata looks correct. That actual
normal Reader producer/handler/form-to-scope connection is still missing.
Accordingly the outer command's fixed link still reports
`:reader-effectful-bridge-unavailable`; no host read is relabeled a Gravity
handler result. Compatibility with a genuine complete Reader plan and normal
integration remains unverified. The injected bridge models are not that result.

### Evidence boundary

Tests inject the fixed private Reader link or observe the issuer; no callback
is accepted by the production command. These command/entry models cover native
argv and EDN parsing, explicit permission and policy denial before issuance,
source-looking permission rejection, provider/profile/phase/scope intersections,
duplicate/malformed input refusal, original-result lookup without provider
replay, capability lifetime on return/throw, fixed dispatch routing, and redacted
JSON/exit behavior. They load neither Bootstrap nor Reader and execute no
Gravity, actual command process, filesystem/provider IO, lease or probe.

This source implementation does not constitute independent acceptance,
integration, authentic effectful Reader execution, release or self-hosting.
The original native goal remains blocked and its history is unchanged. No
review dispatch or publication is performed.
