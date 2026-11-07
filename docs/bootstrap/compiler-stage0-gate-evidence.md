# Native Stage0 legacy compiler gate

## Scope and governing contracts

Move five complete legacy compiler-metadata bodies into the existing
`gravity.compiler-pass-manifest` owner: manifest projection, suite lookup,
suite presence, C1 pass-contract validation and C18 risk/evidence validation.
The first four bodies are in foundation component 00529; the fifth is in
00531. Preserve all five Bootstrap signatures as thin adapters. Leave the
mixed C1/C11/C14/C18 aggregate, its loop order and invocation in Bootstrap.

D0, D1, D2, D3, D6, D8, D9, C1, C15, C18, BOOT8 and TEST2 govern the
increment. Root read the compiler phase and owning contracts, including C1
directly for this family. Independent placement review SHA256 is
`b487c25d048170c685b1e19e503ba77d67f069f6e33d548b4298984f58c835ab`.
It recommends the existing C1/C18 manifest owner, rather than putting pass
metadata in MIR, Lowering, Build or Tooling, or inventing a new namespace.

The frozen parent is `f85d92ff1860c7a020c13f1939333045499ef6ed`, tree
`a3f6dd8d85d2915ea9de36204e3b0bac246b18df`, source freeze SHA256
`84154146078d0211d7ca97b135aeaf8ceacd2f8c1976f07cb8a3cb762a0f56a3`.
Main `21f42b7d92d163be9ede20376c32808ec3b90efd` is already an ancestor;
the seven-path worktree claim imports no additional Main files. Parent CI is
under independent audit and transfers no acceptance. The nine publication SDK
records match current Main and source. No CI/workflow change is planned.

## Public API, inputs and outputs

Add only these native legacy-gate APIs to the existing manifest owner:

```clojure
(native-stage0-compiler-manifest module)
(native-stage0-compiler-suite module)
(native-stage0-compiler-suite-present? module)
(validate-native-stage0-pass-contract! operations module manifest contract)
(validate-native-stage0-verification-risk! operations module risk-record)
```

Inputs are caller-owned module, manifest, pass and risk metadata. Explicit late
C1 operations supply the missing-field helper, required-field and durable-fact
catalog Vars, set intersection/difference and the five-argument C1 failure Var.
C18 operations separately supply set subset/difference and its four-argument
failure Var. Callbacks and catalogs remain live at their original use sites.

Outputs are the original projected manifest, suite/default value, presence
boolean, nil, owner return or exact owner exception. These functions issue no
artifact identity or proof. The enclosing ordinary public macro/module/Stage0
plan route must emit and execute a real instruction plan/report for a nonempty
accepted gate and refuse genuine C1/C18 source metadata before lowering.

The legacy key is `:metadata/:compiler/:compiled-gate`. The owner's existing
modern `:passes` suite and richer C18 artifact validators are different
representations and must not replace this legacy behavior. Present nil/empty
gates remain distinct from absence. The ordinary report must keep its existing
lack of verified MIR and target-artifact emission.

## Ownership, diagnostics and dependencies

The manifest stage owns these five algorithms, their borrowed projection
fields, C1-PASS-CONTRACT/C1-EVIDENCE-DROP selection and C18-EVIDENCE subset
decision/payload. C1 and C18 keep separate operation ports and diagnostic
families. Existing owners retain generic diagnostic construction, source-span
fallback, enrichment, rendering/publication and exception transport. Bootstrap
retains cross-stage invocation/order and source/plan/report identity.

Dependencies point Bootstrap adapters to the existing manifest namespace and
its current leaves/Diagnostics/Digest dependencies, with no reverse Bootstrap
import or new host dependency. Do not remove or reinterpret existing APIs.
Preserve lookup counts/order, late catalog/helper selection, short circuits,
set behavior, replacement-evidence union, original record identity, exact
callback argument shapes, default version, sorted C18 missing-evidence facts,
returning failures and original exceptions. C1's existing set-derived missing
fact vector is compatibility data, not canonical C15 ordering evidence.

Supplied fields and matching evidence labels are native metadata. They do not
authenticate an IR, prove a transformation, validate a certificate, establish
golden/differential evidence or satisfy a release gate.

## Tests and acceptance

Extend the existing `gravity.compiler-pass-manifest-test` native leaf. Its
Bootstrap-free contract, catalog registration and fresh-JVM execution remain
intact. The existing reviewed leaf catalog and component inventory include
this namespace, so its new native tests need no new CI command.

Place genuine source controls in existing
`gravity.lowering.plan-assembly-test`, whose whole-namespace CI command already
tests cross-stage invocation and emission/refusal. Do not import Bootstrap into
the manifest leaf tests. Cover all five APIs and adapters, both C1 refusals,
C18 low/medium/high/critical behavior, subset success/refusal, sorted facts,
opaque lookup and field identity, callback/catalog timing and exception
identity, and absent versus present empty/nil suites.

Add durable paired source text with a complete legacy pass contract, surviving
or replaced durable facts and a risk record with matching evidence labels.
Keep that positive distinct from an absent-suite plan. Retain the three real
C1/C18 refusal fixtures and adjacent C11/C14 aggregate regressions. Compare
complete before/after source/module/plan/report products, helper arguments and
results, diagnostic payloads, provenance and deterministic identities.

Run appropriate native/source regressions, language/ownership gates, cold
loading and exact API declarations, changed-file lint, document validators and
full current-Main comparison. Open an early draft PR, maintain it and bind
independent semantic/adversarial acceptance to the clean commit. Audit that
source's own hosted provider/run before exact current-base integration.

## Status and unsupported boundaries

This is the initial scope record before implementation. Source captures,
focused tests, source acceptance, final publication and own CI audit remain
pending. The seven owned paths are the manifest namespace, components 00529
and 00531, the two existing test files, this record and the namespace map.

No canonical C1/C18 authority, full compiler verification, authenticated
source/IR/evidence, certificate or trust-provider checking, canonical C15
diagnostic ordering/publication, complete source maps, backend/native artifact
emission, package/build/release authority, self-hosting or seed retirement is
claimed. Held Reader/authentication/private source-factory/pin routes remain
excluded. Clojure remains the temporary seed. Manual Source has no
Main/Ready/controller/service/recovery or GraphQL role; exact current-base
integration remains with the designated integrator.
