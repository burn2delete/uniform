# Implementation plan: Standard Library workflow migration

Spec: [spec.md](spec.md) | Migration owner: documentation migration maintainer.
Input revision: `017b541d678c6acb93eb5e843b1eda5a66babc32`.

## Contract alignment

Follow the retained contracts linked in spec.md and [constitution](../../.specify/memory/constitution.md). The current requested migration extends the original pilot scope, without changing language semantics or execution restrictions. Use [development verification](../../docs/development-verification.md) when an actual implementation slice is selected; this documentation change requires structural/link checks, not another compiler proof run.

## Canonical task source

Edit implementation records only in [tasks.md](tasks.md#implementation-tasks). These original outcome sections retain their generated task view and selected-versus-wider acceptance boundaries:

- [remaining tasks and evidence](../../docs/component-roadmaps/stdlib.md#remaining-tasks-and-evidence)
- [compiler support outcome](../../docs/component-roadmaps/stdlib.md#compiler-support-outcome)

## Approach and exact files

1. `spec.md`: scope, retained contract navigation and accepted/rejected workflow behavior.
2. `plan.md`: canonical task anchors, interfaces and dependency boundaries.
3. `tasks.md`: five implementation records and the entire unchanged historical migration file, clearly separated.
4. `evidence.md`: observed checks and implementation nonclaims.
5. `tools/stdlib_task_projection{,_test}.clj`: fixed-path projection and focused controls. Shared helpers remain unchanged; relative file-plus-heading evidence is validated locally without weakening other components.

Central migration inventory/schema is owned separately. This package does not write shared indexes or transfer implementation ownership.

## Interfaces and dependencies

Core, Types, Effects, Legality and Auth govern compiled API acceptance. Runtime providers are needed only for serviced APIs; domain and Platform adapters consume their actual outcomes. Build packages the results later.

## Validation and evidence

Check all local Markdown targets and anchors, four-artifact presence, ASCII and semantic names. Negative controls must reject an absent file/anchor and duplicate implementation task truth. Run existing document and full-language roadmap validators against the candidate. Evidence is documentation-only; no compiler, target execution, external CLI or upstream workflow discovery is run for this slice.

## Delivery and remaining work

Publish the prepared slice as a draft. An independent reviewer checks mapping, anchors, scope and evidence; Deus integrates only the reviewed exact tree. Central inventory links and any remaining per-contract semantic-routing questions stay explicit, not silently marked accepted.

## Names, callers and compatibility

Package key `stdlib-workflow` is semantic. No imports, load order, manifests, source hashes, schemas or public APIs are changed. Historical planning IDs in canonical links remain traceability only.

## Source-specific acceptance and dependencies

This matrix strengthens the five existing Open implementation records in
[tasks.md](tasks.md#implementation-tasks). It creates no new implementation task
or completion credit. Inventory, Pure, Services, Adapters and Release below mean,
respectively, the existing reconcile-export, complete-pure, complete-resource,
complete-schema and integrate-platform task titles. An export name is catalogue
coverage until its actual binding, supported subset and executable evidence are
known. Selected compiler-support evidence cannot close the wider library.

| Original source | Existing task | Required observable evidence and refusal |
| --- | --- | --- |
| [STD1](../../docs/phase-16-standard-library/211-std1-standard-library-architecture.md) | Inventory | Compiled manifests, per-export fixture discovery and package/docs agreement; reject missing metadata, host-dependent core, missing grants, undeclared effects and unsafe audit gaps (STD1001-STD1008). |
| [STD2](../../docs/phase-16-standard-library/212-std2-core-library-specification.md) | Pure | Cross-profile equality/hash, predicates, option/result/error and syntax metadata fixtures; retain ordinary evaluation/transfers and reject host state, unchecked partial unwrap and lost spans (STD2001-STD2007). |
| [STD3](../../docs/phase-16-standard-library/213-std3-collections-library-specification.md) | Pure | Exercise every specified persistent operation, empty/false/nil cases, evaluation/transfers, complete physical member validation and storage/lifetime policy. Prove required presence/range/nonempty premises; reject invalid bounds, escaped builders, invalidated iterators, non-total comparators and unaudited internals (STD3001-STD3008). Project accounting clauses do not authorize quotas; adjacent validation laws survive. |
| [STD4](../../docs/phase-16-standard-library/214-std4-string-and-text-library-specification.md) | Pure | Scalar/grapheme and supplementary Character cases, UTF-8 validation, exact concatenation/conversion, normalization version/form, explicit locale and provider behavior; reject invalid boundaries, silent loss and taint escape (STD4001-STD4008). Separate actual pattern/provider restrictions from removed compiler work accounting. |
| [STD5](../../docs/phase-16-standard-library/215-std5-numeric-and-math-library-specification.md) | Pure | Explicit checked/wrapping/saturating/trapping and floating modes, EFIR, independently checked approximation certificates and target lowering; reject invalid domains, implicit fast math and stale or missing proofs (STD5001-STD5008). |
| [STD6](../../docs/phase-16-standard-library/216-std6-memory-and-resource-library-specification.md) | Services | Borrow/region/linearity and deterministic cleanup fixtures with actual allocators and host wrappers; reject escape, aliasing, double close, use after transfer and hidden allocators (STD6001-STD6008). |
| [STD7](../../docs/phase-16-standard-library/217-std7-concurrency-library-specification.md) | Services | Structured child lifetime/cancellation, race checks, declared atomic memory order and replay choices; reject detached leaks, forbidden blocking and unrecorded nondeterminism (STD7001-STD7008). External provider billing remains distinct from compiler accounting. |
| [STD8](../../docs/phase-16-standard-library/218-std8-io-and-filesystem-library-specification.md) | Services | Root/operation grants, symlink/cycle/platform policy, encoding, linear handles and atomic-write durability/failure records; reject escape, missing close/policy and replay-unsafe IO (STD8001-STD8008). |
| [STD9](../../docs/phase-16-standard-library/219-std9-network-and-http-library-specification.md) | Services | Client policies, typed routes/bodies, TLS validation, redaction and recorded activities; reject undeclared hosts/ports/listeners, unsafe TLS, unauthorized AI tools and replay-unsafe calls (STD9001-STD9008). |
| [STD10](../../docs/phase-16-standard-library/220-std10-serialization-and-schema-library-specification.md) | Adapters | Validate before trust, canonical bytes across targets, schema paths, reflection-free derivation and checked migration; reject weakening, noncanonical bytes and missing boundary schema identity (STD10001-STD10008). |
| [STD11](../../docs/phase-16-standard-library/221-std11-database-and-query-library-specification.md) | Adapters | Separate structure/parameters, validated rows, provider dialect/errors, commit/rollback/retry/isolation and migration evidence; reject interpolation, absent grants, unsafe migration and AI policy bypass (STD11001-STD11008). Transaction lifecycle remains distinct with the qualified Query/provider dependency recorded below. |
| [STD12](../../docs/phase-16-standard-library/222-std12-workflow-library-specification.md) | Adapters | Typed graph/events/state, real replay/activity isolation, compensation success/failure and in-flight migrations; reject repeated external effects, unrecorded nondeterminism, missing compensation and incompatible state (STD12001-STD12008). |
| [STD13](../../docs/phase-16-standard-library/223-std13-ai-and-agent-library-specification.md) | Adapters | Provider/model/schema/policy records, authority-partitioned prompts, granted typed tools, memory isolation, eval and review artifacts; reject unvalidated outputs, injection, unauthorized tools and unchecked generated code (STD13001-STD13008). Human review does not itself confer capability. |
| [STD14](../../docs/phase-16-standard-library/224-std14-testing-library-specification.md) | Pure | Structured failures, seeds/generators/shrinking/counterexamples, hashed golden updates, exact diagnostic fields and packageable reports; reject missing provenance, undeclared mocks and ungranted live-service execution (STD14001-STD14008). |
| [STD15](../../docs/phase-16-standard-library/225-std15-compiler-meta-programming-library-specification.md) | Adapters; Pure syntax helpers | Syntax spans/hygiene/phase/context, ordinary generated-code reentry, stable IR views, pass invariants/invalidations and signed compatible plugins; reject unchecked generation, internal mutation and concealed seed trust (STD15001-STD15008). The syntax API remains distinct from its compiler representation dependency as recorded below. |
| [STD16](../../docs/phase-16-standard-library/226-std16-platform-and-os-library-specification.md) | Release; Adapters random-provider boundary | Target/build/runtime-host separation, wall/monotonic/CPU/compile/replay clocks, explicit random kind and complete process policies including working directory; reject ambient access, omitted policies and replay reads (STD16001-STD16008). The example's omitted working directory is not a default specification. |
| [STD17](../../docs/phase-16-standard-library/227-std17-hardware-and-firmware-library-specification.md) | Release | Memory-map/interrupt-table outputs, volatile/order-preserving lowering, DMA ownership/coherence and synchronization evidence; reject missing widths/alignment, forbidden interrupt effects and unsynchronized clocks (STD17001-STD17008). Software/device adapters are not blanket HDL aliases. |
| [STD18](../../docs/phase-16-standard-library/228-std18-cryptography-library-specification.md) | Adapters | Declared algorithms/parameters/lifecycles, PQ and hybrid success/failure vectors, transcript/combiner/downgrade checks, migration inventory, secrets and side-channel evidence; reject provider-chosen defaults, unavailable entropy, nonce misuse, leaks and invalid lifecycle policy (STD18001-STD18013). |
| [STD19](../../docs/phase-16-standard-library/229-std19-ui-and-application-library-specification.md) | Adapters | Typed component/state/event/render/route/style/accessibility artifacts consumed by snapshots/interactions; reject hidden effects, unsafe markup, missing labels and absent adapters (STD19001-STD19008). Rendering trust overlap stays context-specific. |
| [STD20](../../docs/phase-16-standard-library/230-std20-standard-library-stability-policy.md) | Release | Per-export stability and eight compatibility dimensions, opt-in/internal import rejection, deprecation guidance/schedules, impact/migration/governance records and signed release provenance (STD20001-STD20008). The illustrative policy form does not invent a callable API. |

Inventory precedes fixture selection for each declared module/profile. Pure
semantics depend on Core, Types, Effects and Legality; Services also depend on
Auth and the actual Runtime provider. Adapters consume Schema, Query, Workflow,
AI, compiler-pass and target outcomes, rather than treating a manifest as their
implementation. Release consumes those verified exports, Platform target
fixtures and Build signing/provenance plus the owning governance decisions.
Each dependency needs its actual artifact and invalidation identity. Missing
provider/signature/fallback detail remains an explicit acceptance gap. The
bounded phase-16 proposal delivered to the canonical reconciliation owner
preserves inherited units/facets, historical receipt pointers, five source-adjudicated
cross-family relations with Open execution evidence and selective accounting lineage for central adoption.
It confers no source-scope, implementation or release acceptance.


## Adjudicated library boundaries

All five obligations remain distinct and Open. The exact source adjudication
receipt is `2c1fa6b6ac8f508485a410c90f197017ee7eaaa88b41110402fe83c4e43f94bc`.
Dependencies below are conditional artifact/API dependencies, not component-wide
completion gates. The matching actual task criteria retain positive and refusal
cases; source adoption does not supply execution evidence.

| Obligation | Accountable task ID | Qualified dependency IDs | Required boundary |
| --- | --- | --- | --- |
| stdlib-syntax | `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` | `syntax/current-evidence-and-remaining-work/syntax1-construct-canonical-syntax`, `syntax/current-evidence-and-remaining-work/syntax2-preserve-transformations`, `syntax/current-evidence-and-remaining-work/syntax3-invalidate-facts`, `stdlib/remaining-tasks-and-evidence/complete-pure-core-collections-text-numeric-and-testing-apis` | Actual library calls must preserve syntax context; explicit metadata updates retain origin/hygiene/context and invalidate stale facts. Internal compiler fixtures and export names alone do not close this task. |
| stdlib-release-evidence | `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` | `build/remaining-tasks-and-evidence/produce-and-verify-packageprovenancesigning-records` | Assemble exact release-bound comparison and signed compatibility/provenance reports; consume governance classification/approval records. Missing approval rejects release; signatures or classification alone do not assemble the reports or authorize release. |
| stdlib-transactions | `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` | `query` | Execute commit/rollback/retry/isolation/idempotency through the standard API using the declared provider. Query results or transaction metadata alone do not close lifecycle acceptance. |
| stdlib-clock-domains | `stdlib/remaining-tasks-and-evidence/integrate-platform-modules-and-close-release-manifests` | `backend-hdl` | For physical HDL crossings consume B9 clock/reset/latency/metastability and declared synchronizer/FIFO/handshake/proof or hardware-policy waiver evidence. Software/device checked adapters retain their own capability and synchronization outcomes; no blanket HDL dependency on those targets. |
| stdlib-render-trust | `stdlib/remaining-tasks-and-evidence/complete-schemaqueryworkflowaimetacryptoui-adapters` | `ui` | Declare adapter/target/host services/unsupported features and exercise escaped text, trusted markup, binary media and native views through the actual supported sink policy. Web-only checks do not close native/media/terminal claims. |
