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
