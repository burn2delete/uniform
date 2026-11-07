# Native Stage0 effect-policy kernel

## Scope and governing contracts

Move the four complete existing form classifier, effect inference, capability
projection and module-effect guard bodies from
`gravity.module-analysis.effects` into `gravity.effects`. Keep the intermediate
module-analysis helper names, argument lists and Bootstrap interposition APIs
as compatibility adapters. This moves the algorithms, not only their names.

D0, D1, D2, D3, D6, D8, D9, L3, L6, L15, P4, C1, C8, C15, BOOT1, BOOT8
and TEST2 govern this bounded extraction. Existing C7/L12 and native C8
contracts remain applicable to the earlier Effects APIs. The Stage0 family
does not establish canonical C8 legality or capability authority.

The frozen predecessor is Runtime source
`310702b7aedf7a00dcabf88cf217cc212a86f998`, tree
`3a4f51efd5bc50952b1bfcd787ca53f0488843a6`. The isolated branch begins at
normal own-branch composition `db33272ed73b4251422fa9de1f9f158010d4be26`,
tree `044f889e2bd99062196600709d9b4f2234b48ef3`, with ordered parents
`[310702b7aedf7a00dcabf88cf217cc212a86f998,
4d0ab35a4d085eea3de3c8a2af0372578b62fdec]`. Exactly four published-Main
Simplify paths were imported; all other predecessor modes/blobs are retained.
The new recovery implementation belongs to original controllers and grants
this manual Source no recovery, service, controller or Main role.

## Public API and artifact boundary

The intended public APIs are:

```clojure
(stage0-uses-println? recur-check form)
(infer-stage0-effects uses-println? forms)
(required-stage0-capabilities effect-capability effects)
(validate-stage0-module-effects! {:keys [uses-println? fail!]} module)
```

Inputs are existing caller-owned native forms, expanded module/declaration
data, source path, classifier/failure callbacks and effect-capability catalog.
Preserve the existing caller-selected callback values and capture timing;
these are not uniformly live-Var ports. Do not add a new dereference protocol,
callback validation, source intake or provider selection.

Outputs are the original truthy/nil/false classifier observation, effect set,
capability set, nil/failure callback return or unchanged exception. The public
consumer continues to emit the existing hosted instruction plan and stdout.
Plan bytes, source path/hash, plan IDs and report identities are preserved.
The existing thirty public Effects Vars comprise twenty-three functions and
seven data Vars; preserve all of them, adding only four public functions.

Dependency direction is module-analysis compatibility adapters to Effects,
then Bootstrap callers. Effects retains only its existing `clojure.set`
dependency and must not import Bootstrap, module analysis, CLI, release or a
backend. Bootstrap/module analysis retain interposition, source/namespace
admission, policy catalogs and diagnostic construction/publication.

## Diagnostic ownership and two evidence routes

The first assembler profile port is
`b/validate-stage0-compiled-profile!`. It remains ahead of the effect port and
owns `P4-HOST-EFFECT` and `P4-HOST-CAPABILITY`. The full source-to-plan route
for missing declarations must continue to fail there, before plan emission,
with zero calls to the later effect validator. Do not override that profile
gate or claim its failures as Effect-stage L6 evidence.

The effect kernel owns its existing `L6-EFFECT-UNDECLARED` decision and the
effect-gate variant of `L3-CAPABILITY-MISSING`. Test those decisions directly
through public `b/validate-module-effects!` using the genuine unmodified
module and expanded forms from public `b/macro-source-artifact`. This checks
the pass's actual upstream product; it does not compile an invalid plan,
execute a denied program, mutate declarations or grant source admission.

The separate namespace-admission algorithm retains `L3-EFFECT-WIDEN` and its
own `L3-CAPABILITY-MISSING` variant. Supplied failure callbacks retain payload
construction, transport and publication. All IDs, messages, source-path
identity, declared/required fields and remediation text remain unchanged.

## Preservation and tests

Preserve recursive traversal, short-circuit/truthy behavior, lazy callback
realization, native collection order, catalog lookup and entry-time callback
capture. Preserve quoted-form scanning, top-level-only `network-listen`
inference, omitted nil catalog values, retained false values and the existing
two-rule guard. A returning failure callback retains its original
continuation and supplies no source, capability or proof authority.

Positive evidence uses real `.gravity` and `.qst` source, public macro
expansion, the actual Lowering assembler and public executor. Compare complete
before/after plans, reports, source hashes/IDs, stdout and failure payloads.
Record P4 full-route negatives as owner-order controls and the separate direct
Effect-stage L6/L3 negatives as this slice's refusal evidence.

Read-only discovery and ordering correction are recorded by supplement SHA256
`c14b8f2cbe2405a193ec800b2c54a352b20dd162d73bf3f91b605789763e094c`.
Actual four-source discovery receipt SHA256
`aaa3744177acf0721f45c4a2976f1c069947e890e41c4a5111b27a3c38335748`
proves both routes without replacing Reader, profile policy or module facts.
Earlier failed attribution probes remain history; they are not candidate
failures or acceptance evidence.

Add focused callback/order/identity/refusal tests, preserve all earlier CI
vectors and append one whole-namespace command. Run affected integration
tests, source-language boundary checks, structural docs/roadmap validation,
complete current-Main lint and the relevant imported Simplify regressions.
Independent semantic/adversarial, composition/provider and CI reviews must
bind the exact clean authored tuple before integration eligibility.

## Current status and unsupported boundaries

This is the early draft scope; no new Effects API is implemented or accepted
at this capture. The original eight-path lease governs authoring. The
current published-Main SDK is recorded by manifest SHA256
`33cf71a66f4482ed358362aca7438bdcc80a2544c1381cb7131ad1e0a256ca07`.
Its nine exact published code files include the new recovery/workflow code;
older source/CI acceptance remains bound to its recorded older tuple.

This kernel does not provide canonical typed-core/C8 checking, latent or
transitive function effects, branch/handler/continuation facts, complete
profile/package/deployment/provider legality, build/replay/order/residual
effects, capability proofs, source authentication, C15 schema/publication
authority, MIR admission or backend/runtime authorization. `network-listen`
is an existing inference fact; no executable network service is added.
Host/native conservative scanning is not full Gravity effect semantics.
No performance, release, migration, self-hosting or seed-retirement claim is
made. Held Reader/authenticated/private builders/factories and source pins
remain unchanged and uninvoked. Gravity/Uniform remains the destination
implementation language. The designated integrator retains exact current-base
preflight and Main integration; this manual external Source has no Main,
Ready, controller, service or recovery role.
