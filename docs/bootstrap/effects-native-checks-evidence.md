# Native effect declaration, build and replay checks

## Governing contracts and source boundary

D1/D2/D3/D8/D9, L6/L12/L15, C1/C7/C8/C15, BOOT8 and TEST2 govern
this extraction. The original source parent is
51efe3b4bea78bca8bc2fda2661d14132e13f90b, tree
80f3be8455b1845964244126e6696339e0cbeb0f. It composes own C source
a6a0c081 with verified published Main
41aaea100091dc80067328e9b066f7378845eca3. All 124 prior source paths
and six disjoint incoming Main paths retain exact modes and blobs. No
foreign unmerged source, control state or execution authority is adopted.

Move the four existing effect-only helpers in foundation component 00067 and
the effect-only prefix of check-effects-and-capabilities! into gravity.effects.
Retain handled-capability? and the complete capability/provider loop in
Bootstrap. Effects describe actions; capabilities/providers grant authority.
Preserve declaration/profile checks, build/replay metadata checks, registry
lookups, checker mutation, per-use Var selection, failure data and the order
effect rules -> build -> replay -> capability/provider checks.

## Public boundary, inputs and outputs

The new internal native APIs receive caller-owned checker/context cells,
source nodes, inferred effect labels and options, plus explicit live registry,
classification, catalog, diagnostic and recorder operations. They return the
existing predicates/nil values, append original build/replay records, or
propagate original L6 owner diagnostics. Source node identity, unsafe/check/
human-review metadata, effect labels, record order and owner exceptions remain
intact. No new effect name, budget or quota is introduced.

The stage owns these existing native premise checks. Bootstrap retains input
construction/authentication, type/body/handler inference, capability and
provider policy, actual build authority, source enrichment and diagnostic
publication. The stage imports no Bootstrap namespace. Existing hosted C8
report APIs and their cold/no-Bootstrap controls remain separate from the new
Bootstrap-backed native-check tests.

## Tests and integration

Add focused positive/negative native checks in gravity.effects.pipeline-test,
including actual paired current-source plan compilation/emission and real
L6 refusals. Cover unknown/profile/undeclared/build/replay cases, override ids,
handled effects, preservation of capability refusal after effect checks,
returning failures, checker logs, borrowed node identity, live data/function
ports and callable selection before argument evaluation. Compare immutable
old/native and extracted bodies on identical live inputs and full outcomes.
Preserve cold C8 metadata/arglist tests and every existing CI command. Register
the complete new native namespace explicitly; do not load Bootstrap into the
cold C8 test process. Run boundary, affected regression, source conservation,
complete contextual lint, docs/roadmap and independent semantic/adversarial
review, own CI/artifact audit and exact coordinator integration.

## Unsupported boundaries

These native checks do not implement canonical C8 legality or scoped grants.
Metadata build/replay labels do not authenticate principals, providers,
phases, paths, external inputs, pinned results or performed effects. L6/L12
hermetic input/replay policy still requires those independent premises. Handler
coverage does not itself prove typed requests/responses, continuations or
capability authority. No private C6/C11, default frozen Reader, full H6,
source proof, native toolchain, live provider, broker/controller, release,
migration, self-hosting or seed-retirement authority is exercised or claimed.

## Implemented extraction and evidence

Five new native APIs now own the four original helper bodies and the effect
prefix. Twelve explicit function/catalog ports preserve live per-use Var
selection; one private selector imports no Bootstrap. Public APIs increase
from 25 to 30. Existing data/report APIs retain their metadata. New check APIs
are explicitly native premise checks, not data-only builders or canonical
legality/proof/authority APIs. Bootstrap keeps both combined-checker arities,
handled-capability? and every capability/provider decision byte-for-byte.

Independent reconstruction recovers the four native bodies, complete effect
prefix, entire prior stage bytes and all compatibility call/Var maps. The
original nine CI commands/selectors remain byte-identical; one complete Effects
namespace is appended at the existing 2g/180-second native-test policy.
Coverage controls reject namespace omission/substitution. The workflow is
unchanged. A fresh three-path claim preceded the explicit Bootstrap stage
require and standalone test registration in both existing maps. The existing
effects-graph-data production helper remains unique and cold; no project/cold
scope gains a Bootstrap-backed test. Final scope is eleven paths, covered by
the original eight-path claim plus that registration claim.

The original cold C8 baseline passes 23 tests / 511 assertions. Expanded cold
controls pass 23 / 551 and still assert Bootstrap is absent. Complete native
Effects passes 10 / 124 under the unchanged child policy. It emits actual
Stage0 typed-core, build/replay/handler and MIR-effect records from the accepted
effect-system fixture, and rejects five real typed L6 fixtures. Paired actual
Hello sources retain semantic plan ids and deterministic emitted C text.
Controls also cover ordered logs, node/source metadata, handled-effect/profile/
capability separation, exact owner returns/errors, late function/data Vars and
diagnostic callable selection before options lookup. CI harness passes
54 / 573; boundary/ownership passes 48 / 1202. Direct lint, both document
validators and diff checks pass.

The first native run had two failed assertions because the fixed-seed
executable plan reports an empty inferred effect set for Hello. Its summary
does not establish effect legality. The corrected test uses the actual typed
core's :io/write facts and additionally rejects their use under an undeclared
effect context. It preserves and records the seed plan summary without using
it as an effect premise. No inference implementation or source pin is changed;
the two-failure log remains NONPASS. Full seed-plan effect inference remains
unsupported by this slice.

All six immutable original component functions at
bc6171b99326cff4a30275600624698b8880612b are rebound together for the
old/new comparison. All 25 complete raw and parsed outcomes match: nineteen
native cases and six actual typed-source artifact/refusal cases, eleven
returns and fourteen refusals. Checker logs, source spans, unsafe/check/human-
review metadata, context/node inputs and returning-diagnostic behavior remain
intact. Capture SHA256:
67950bbe66cb4be58cfcb2fe4b711d6498eb8f2f74a8daeb64bf299d68656ef3.
No actual build/provider effect, private source factory or replay is executed.

Complete current-Main contextual lint, independent semantic/adversarial review,
own hosted CI/artifact audit and exact coordinator integration remain separate
requirements. Local tests and legacy complete/granted/recorded labels confer
no admission, release or self-hosting credit.
