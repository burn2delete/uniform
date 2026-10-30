# Normal Reader extension binding boundary

## Purpose and disposition

Source-only supplement for `FL-P01-T01`, canonical task
`reader/finite-remaining-work/extension-policy-closure`, inspected at
`ae0cd80165b2085e7ce362d0c7bfc5eeec79b2de`. The first missing interface is
an authenticated normal Reader registration input that binds the selected
policy to an implementation and its execution. This supplement defines test
inputs; it does not implement that interface or establish task completion.

## Exact current call boundary

In `bootstrap/clojure/src/gravity/bootstrap_impl/components/self-hosting-sh03-sh07/`:

- `00812-sh03-reader-input-source-unit.clj`, `sh03-reader-input-policy
  [reader-options]`, produces exactly `:artifact`, `:schema-version`,
  `:retain-trivia` and `:enabled-reader-tags`. The tags come from
  `standard-reader-policy`; no registry, implementation or grant enters it.
- `00813-sh03-reader-canonical-identity.clj`, `sh03-reader-resolved-result!`,
  constructs `arguments [source-unit source-byte-vector reader-policy]` for
  `sh03-reader-entrypoint`. It calls `sh03-reader-verifier` with
  `[source-unit source-byte-vector reader-policy raw-result]`. These existing
  producer/verifier calls do not carry a custom registration binding.
- `00815-sh03-reader-normalization-deferred.clj`, `sh03-reader-host-values!`,
  preflights every tag using `validate-standard-tags!`, then calls
  `dispatch-standard-tag source-unit standard-reader-policy tag form payload`.

In `bootstrap/clojure/src/gravity/reader/dispatch.clj`, `standard-policy!`
requires the exact standard policy, with `inst` and `uuid` and ambient
authority denied. `dispatch-standard-tag` calls `(dispatch policy tag seed nil)`.
The generic `dispatch [policy tag seed provider]` is a separate leaf: it
validates native carriers and implementation-owned effects/capabilities, but
does not authenticate Reader products. Its provider is a trusted host binding,
not a source-deserializable function or an authenticated registration product.

## First product needed

The owning normal producer/verifier contract must specify where an authenticated
registration input enters the calls above, and how verification binds:

1. Policy identity, tag membership, enabled tags and applicable profile.
2. Selected implementation identity and its declared effects/capabilities.
3. For effectful handlers, grant principal, provider identity, build phase,
   invocation lifetime, capability and exact input, joined to a trusted binding.
4. Original source identity/content, form/span, input and returned syntax seed,
   joined to the actual invocation and selected implementation.

No such custom-registration parameter is present in the inspected normal
calls. The concrete next interface decision is the registration artifact and
its producer/verifier field and call joins, including how implementation and
provider identity are authenticated. This is a bounded contract gap; full
Build completion and final self-hosting are not prerequisites. A caller boolean,
caller purity assertion, arbitrary provider callback or relaxed standard-policy
check would not fill it. No production API is added by this supplement.

## Existing equations and required joins

Let `H` be the existing hosted C2 canonical hash and `P` the exact standard
policy. The current standard path requires:

```text
H(P) = source-unit.extension-policy
     = source-unit.reader-options.extension-policy
     = source-unit.identity-inputs.extension-policy
     = source-unit.identity-inputs.reader-options.extension-policy
seed = {source-id, form-id, span, datum}
input-hash = H({:domain :gravity/reader-extension-input-v1,
                :policy-id H(P), :tag tag, :seed seed})
result-hash = H({:domain :gravity/reader-extension-result-v1,
                 :input-hash input-hash, :value returned-seed})
implementation.effects <= entry.build-effects <= policy.build-effects
implementation.capabilities <= entry.capabilities <= policy.capabilities
```

The last two relations are set inclusion in the existing generic leaf. Its
implementation comes from the private handler table, never caller code.
The standard invocation records these hashes as
`:authority :unsealed-bootstrap-observation`; hashing is not authentication.
An authenticated normal registration must join actual policy selection,
implementation and grant to these source/input/result identities through its
owning verifier. The equations above describe existing code, not a proposed
wire format. PR549's standard handlers and observation authority remain intact.

## Fixture definitions and acceptance criteria

[extension-binding-fixtures.json](extension-binding-fixtures.json) defines five
paired `.qst`/`.gravity` source inputs and declarative policy variations using
the existing `read-text` and `read-input` handler identities. The JSON is test
planning data, not a new runtime policy/provider API. The granted case refers
to the existing input file and exact grant fields; no callable is supplied.

Future normal integration must accept pure/granted cases, reject unknown and
disallowed tags and undeclared effects before any handler/provider execution,
and identify the actual policy/handler with source-bound invocation evidence.
Currently the custom tags cannot be admitted through the fixed standard seam.
These are unrun definitions, not accepted normal Reader fixtures. No parser,
handler, Bootstrap, compiler, runtime, helper, lease or probe is executed here.
Source/data checks cannot establish semantic acceptance. The original PR571
hold and blocked goal are unchanged. No lifecycle ledger or task checkbox is
modified; independent review, publication and integration remain unperformed.

## Governing inputs

- [Reader spec](spec.md), [plan](plan.md), [canonical tasks](tasks.md).
- [C2 Reader design](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md),
  extension policy, syntax seeds and ambient authority requirements.
- [Existing normal dispatch evidence](normal-dispatch-evidence.md), including
  its explicit unsealed observations and missing authenticated binding scope.

## Observed source/data checks

On 2026-10-30, `jq -e` parsed the definition packet and checked its unrun
status, five unique cases, two source paths per case and the two existing
handler selections. `cmp` passed for all five source pairs; `find` confirmed
exactly ten source files. `test -f` confirmed the declared input exists.
`LC_ALL=C rg -n '[^\x00-\x7F]'` found no non-ASCII bytes in the new
artifacts. `git diff --check` passed after staging the complete supplement.
These are data/whitespace checks only; no Gravity source was parsed or run.
The executable language-boundary gate and other Clojure validators were not
run under the current source/data-only instruction.

Inspected source SHA256 values, in the call order above:

| Existing source | SHA256 |
| --- | --- |
| `00812-sh03-reader-input-source-unit.clj` | `00e3c2306e3acb646d9d066f5bc91256c6097dbd941ed9910680a2ecd00e9380` |
| `00813-sh03-reader-canonical-identity.clj` | `3736db819b4bb80c1ca6cfb423495d72a2623e268e469d137730a5c43b62c63f` |
| `00815-sh03-reader-normalization-deferred.clj` | `21b226b2714ebaa39b1743c248f87a8423f69ad212726d2964a61bad8038ddbc` |
| `gravity/reader/dispatch.clj` | `1397962426fbb1f323f58b4a3d7262b05a5f0c1303772da6c8c8fe62afe059b6` |

Definition packet SHA256:
`499cbf2d2683481055fd0dbd18f83daa1f06e87fccf196dd2d6374c8cd7c4b3e`.
The local Git commit freezes all supplement bytes; no review or publication
receipt is asserted.
