# OWN-02 direct-owner population selectors

Base: `149503152d7edab3f7e638c000a553f62c54e679`.
Contracts: `docs/end-to-end-contracts.md` OWN-02, C2-C6, C16 and C18.
Status: source increment only; all ten new genuine selectors are UNRUN.

## Existing coverage and precise addition

The existing `stage-parent-chain-uses-bootstrap-identity-domain` test replaces
delegated compiler calls. The separate compiler-free parent-cache controls
from PR576 evaluate real facade functions with explicit stand-ins. Neither
authenticates the required source population. The existing public development
ingress tests already cover genuine paired Core products and coherently
rehashed Core substitutions. Those fixtures and controls are unchanged.

The existing `gravity.self-hosting.authenticated-artifact-pipeline-test`
namespace now has separate exact selectors for each original
`sh-07/compiler-exact-keys.gravity` and `.qst` subject:

| Subject | Exact test vars |
| --- | --- |
| `.gravity` | `real-owner-gravity-prefix-passes-fresh-replay`, `real-owner-gravity-missing-parent-is-rejected`, `real-owner-gravity-wrong-stage-parent-is-rejected`, `real-owner-gravity-facade-domain-parent-is-rejected`, `real-owner-gravity-foreign-parent-cache-is-rebuilt` |
| `.qst` | `real-owner-qst-prefix-passes-fresh-replay`, `real-owner-qst-missing-parent-is-rejected`, `real-owner-qst-wrong-stage-parent-is-rejected`, `real-owner-qst-facade-domain-parent-is-rejected`, `real-owner-qst-foreign-parent-cache-is-rebuilt` |

Each selector constructs its own real prefix. No saved report, artifact, prior
test PASS or cross-selector product supplies acceptance. The positive retains
the actual source identity, path, project provenance, Reader revision, the owning
C3 Reader view and Macro/Resolve/Core parents, and the explicitly empty schema-1 module
input. It executes the existing fresh public checks after construction. This
is the finite paired pilot population, not every compiler-owner source module.
The C3 parent is compared with the actual invocation-aware
`c3-reader-artifact-view`, not the full C2 record. The retained current Reader
descriptor revision is compared with that view and the actual owner's current
invocation binding using its source/plan/function identity projection.

The three mutation selectors remove the immediate parent, supply the actual
Core product as a wrong-stage parent, or substitute the real parent facade ID.
They call the actual `authenticate-embedded-parent!` and require
`ARTIFACT-PARENT` with stage and expected/observed identities. These derived
mutations are not claimed to be authentic owner products or coherent forgeries.

The foreign-cache selector constructs a second genuine prefix from the existing
`compiler-reader-quote` fixture of the same extension. It inserts the actual
foreign child into an actual facade envelope, requires envelope admission to
pass, and exercises the real fast cache gate. The foreign parent must be
different, the entry must be evicted before rebuild, owner replay must not run
before that miss, and the actual rebuilt child must retain the selected parent.
Cache retention is not required when the existing cache policy declines it.

Its narrow harness adapter replaces `build-payload` only to forward the
explicit invocation to actual owner constructors. It never supplies a verifier
report or modeled compiler product. The replay wrapper delegates to its saved
original if called. Cache, builder and replay Var roots restore in `finally`;
an isolated cache atom keeps preexisting global entries untouched. No source
fixture or Reader file is modified.

## Actual handoff and dependency closure

All paths below are under `bootstrap/clojure/src/gravity/` unless stated:

| Boundary | Existing entrypoint and actual selected arguments |
| --- | --- |
| Source | `authenticated_artifact_pipeline/05-run-pipeline.clj`: fresh source-only pipeline request and its actual snapshot/context |
| Reader | `bootstrap_impl/components/compiler/00829-compiler-c2-reader-source-artifact.clj`: seven-argument constructor, selected Reader path and invocation; no precomputed Reader products |
| Syntax | `components/compiler/00856-compiler-c3-syntax-source-artifact.clj` under `bootstrap_impl/`: actual C2 parent, existing private product authority, nil direct Reader path and the same invocation |
| Expand | `bootstrap_impl/components/self-hosting-sh03-sh07/00866-sh05-macro-artifact-verification-02.clj`: actual C3 parent and invocation |
| Resolve | `bootstrap_impl/components/self-hosting-sh03-sh07/00881-sh06-resolution-finalize-candidate.clj`: actual Macro parent, schema-1 module inputs, selected Reader path and invocation |
| Core | `bootstrap_impl/components/self-hosting-sh03-sh07/00898-sh07-core-from-authenticated-request.clj`: actual Resolve parent and invocation |
| Replay/cache | C2 validator, C3 invocation-aware validator, Macro public verifier, Resolve public verifier with its existing legacy boundary plus invocation, Core public verifier, and actual facade envelope/parent/key/cache functions |

The invocation uses the existing
`bootstrap/gravity/src/gravity/bootstrap/reader.gravity` and exactly
`{:authenticated-envelope-options {:source :development-current}}` alongside
its `:reader-source-path`. It does not select a new language/profile/fact.
Dynamic owner plans, their lexically captured helpers, AE source/emitter and
transitive runtime dispatch remain real dependencies. Before future execution,
freeze the complete tracked candidate tree, bootstrap load manifest, all
Clojure/Gravity source/resources, contracts and the four existing subject files;
record the Clojure/JDK and actual host execution boundary separately. A short
constructor-root list is not a complete runtime dependency proof. The private
source receipt conservatively inventories the entire tracked tree; no sampled
hash list is substituted for that closure. External host/broker state is not
authenticated by Git.

## Validation and exact next admission

Only compiler-free source parsing, declared arity/selector inspection, lint,
document validation and the language boundary are eligible in this increment.
Observed checks: source parsing and eight declared owner arities passed, all
ten exact vars were found, and Bootstrap remained unloaded. Lint had zero
errors and the same preexisting unused `LinkOption` import warning. The language
boundary passed 6 tests/11 assertions; both document validators and whitespace
checks passed. These results do not execute the genuine selectors.
Requiring this genuine test namespace loads Bootstrap and is not a source-only
check. No genuine selector, owner constructor/verifier, compiler/runtime/broker
or host confinement probe is launched by this increment. A public draft, if
created, must retain this unrun disposition; routine CI cannot establish these
new selectors unless they are explicitly selected and actually executed.

The next admission operation is independent source review of the frozen patch
and complete input closure, followed by separately authorized execution of
`real-owner-gravity-prefix-passes-fresh-replay` alone. Its future command shape
is the existing `clojure -J-Xmx2g -M:dev-test --namespace
gravity.self-hosting.authenticated-artifact-pipeline-test --exact
real-owner-gravity-prefix-passes-fresh-replay --fail-fast`, under the unchanged
1,200-second host watchdog used by the paired pilot. This command is NOT RUN.
The companion positive and each negative require distinct exact selections,
separate receipts and restoration evidence. No aggregate namespace discovery,
new allowance, watchdog increase or held-operation replay is authorized.
Any source/host restriction or timeout remains a nonpass, not permission to
substitute a report or retry another route.

## Separate public facade interface dependency

The public facade still accepts only source path, target stage and lane.
Its builder and cache replay use historical arities. Direct-owner tests with
explicit invocation therefore do not establish public current-Reader support.
A separately assigned repair must conserve these exact producer/consumer joins:

- Explicit caller selection matching the existing public Core development
  option; historical/default calls must retain their existing behavior.
- Host-owned invocation forwarding through actual C2/C3, Macro, module-aware
  Resolve and Core construction, and every corresponding cache replay verifier.
- Cache identity bound to actual selected Reader/source/plan and relevant
  module/context inputs; an option label alone cannot authenticate a plan.
- Negatives for missing/foreign invocation, selected Reader byte/plan drift,
  wrong source/stage/parent, context/module substitution, facade-domain IDs and
  fresh/fast separation, preserving diagnostics, eviction and nonpublication.

This patch changes no production interface, selected-source implementation,
ACC10 producer/harness or ACC11 versioned consumer. OWN-04 remains accepted at
its existing Bool scope. No full OWN-02, v3, release or self-hosting claim follows.
