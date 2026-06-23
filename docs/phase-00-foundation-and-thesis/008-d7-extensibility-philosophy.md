# D7 - Extensibility Philosophy

Sequence: 8
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

D7 defines how Gravity can be extended without splitting into incompatible languages or bypassing safety. Gravity is extensible because code, syntax, IR, schemas, passes, providers, and artifacts are data with contracts.

Extensibility is allowed at declared seams. It is not permission to mutate compiler internals, add hidden runtime services, bypass profile validation, erase proof metadata, or grant ambient authority.

## Extension Model

Every extension has:

- kind,
- owner,
- input contract,
- output contract,
- supported profiles,
- effects,
- required capabilities,
- safety mode,
- artifact schema,
- conformance fixtures,
- diagnostic behavior,
- version and compatibility policy.

```clojure
{:extension-id "custom-arena"
 :kind :memory-provider
 :owner "systems-working-group"
 :profiles [:native :firmware]
 :input-contract :region-requests
 :output-contract :allocated-regions
 :effects [:memory/allocate :memory/free]
 :capabilities [:allocator/custom-arena]
 :safety :audited-unsafe
 :api-boundary :safe-wrapper
 :artifacts [:provider-manifest :unsafe-audit :region-proof]
 :fixtures [:positive-region-use :escape-rejected]
 :diagnostics [:region-escape :allocator-capability-missing]
 :compatibility {:gravity ">=0.4 <0.5" :stability :experimental}}
```

## Extension Kinds

Macros transform syntax objects into syntax objects. They must preserve source spans, hygiene or declared unhygiene, metadata, profile context, phase separation, and generated-origin chains.

Reader extensions are narrowly controlled. They may introduce literal or tagged data forms only when the result is an ordinary syntax object with stable source spans and no hidden IO, network, host reflection, or execution.

Compiler passes transform one declared IR into another. They must declare preserved facts, invalidated facts, regenerated facts, diagnostics, proof obligations, and artifacts.

Domain IR extensions introduce specialized representations such as EFIR, schema IR, query IR, workflow IR, AI IR, HDL IR, UI IR, or GPU IR. They must keep a semantic anchor to typed core or MIR.

Type-system extensions add type forms or inference rules only when they lower into the core type contract and state soundness obligations by profile.

Effect extensions add effect kinds only when they define ordering, capability requirements, profile legality, diagnostics, and runtime/build enforcement.

Capability providers implement authority for effects. They must state grant source, scope, revocation behavior, audit metadata, and profile constraints.

Memory providers implement GC, ownership, region, arena, stack, static, device, or linear-resource behavior. They must state allocation, deallocation, escape, aliasing, initialization, and unsafe-boundary rules.

Math providers implement numeric and elementary functions. They must state domains, branch policy, numeric mode, precision contract, EFIR/EML participation, certificate strategy, and target assumptions.

AI providers implement model, embedding, tool, memory, evaluation, or policy services. They must expose effects, capabilities, structured-output schemas, prompt artifacts, audit logs, budgets, and replay records.

Backends and runtimes are extensions only through declared backend/runtime contracts. They may lower or execute legal IR; they may not define new source semantics.

Tooling extensions consume compiler artifacts and diagnostics. They may not rewrite program legality or hide diagnostics.

## Extension Boundaries

Extensions cannot cross these boundaries:

- A macro cannot introduce illegal effects into the caller profile.
- A compiler pass cannot consume unchecked source forms.
- A backend cannot legalize rejected MIR.
- A runtime cannot grant undeclared capabilities.
- A package cannot widen deployment authority.
- A tool cannot suppress safety diagnostics as a successful build.
- A provider cannot claim profile support without conformance fixtures.
- An unsafe extension cannot expose safe APIs without safe-wrapper tests and audit records.

## Extension Manifest

Each distributable extension includes a manifest:

```clojure
{:name gravity.ext.fast-math
 :version "0.2.0"
 :kind :math-provider
 :profiles [:native :gpu]
 :targets [:llvm :cuda]
 :effects []
 :capabilities []
 :contracts [:efir-provider :approximation-certificate]
 :unsafe-islands []
 :artifacts [:provider-manifest :certificate-checker :test-vectors]
 :fixtures [:sin-domain-fixture :implicit-fast-math-rejected]
 :diagnostics [:missing-certificate :unsupported-target-feature]
 :compatibility {:gravity ">=0.4 <0.5"}
 :signing {:required true}}
```

Extensions loaded during builds also declare build effects. A compiler plugin that reads files, calls a solver, queries a model, or executes a process must expose those effects and be rejected in hermetic mode unless granted.

## Requirements

- All extension points must be data-oriented and artifact-producing.
- Extensions must declare profiles, effects, capabilities, safety mode, and artifact schemas.
- Extensions must have positive and negative conformance fixtures.
- Extensions must preserve diagnostics and source provenance.
- Extensions that participate in safety, math, memory, capabilities, packages, AI, or compiler passes must emit audit or proof artifacts.
- Extension compatibility must be versioned and governed like standard-library compatibility.

## Dependencies

D7 depends on `D0`, `D1`, `D3`, `D6`, and `D8`.

Detailed extension contracts are owned by `L4`, `L16`, `L17`, `L18`, `L15`, `SAFE6`, `P1`, `MATH2`, `C17`, backend/runtime documents, package documents, AI provider documents, and governance documents.

## Outputs and Artifacts

D7 requires:

- extension manifests,
- pass contracts,
- provider contracts,
- capability provider manifests,
- unsafe audit records,
- proof/certificate artifacts,
- compatibility records,
- extension conformance reports,
- signing and provenance metadata.

## Rejected Extensions

D7 rejects extensions that:

- mutate compiler internals outside pass contracts,
- transform text instead of syntax objects while claiming macro status,
- introduce effects without effect declarations,
- require capabilities not present in package/deployment/build grants,
- hide unsafe operations behind safe APIs without audit records,
- drop source spans or generated-origin metadata,
- define target-specific semantics as language semantics,
- depend on network/shell/model access during hermetic builds,
- claim standard-library compatibility without conformance fixtures.

## Diagnostics

- `D7-EXTENSION-MANIFEST`: extension lacks required profile, effect, capability, safety, or artifact fields.
- `D7-MACRO-PROVENANCE`: macro output drops source span, hygiene, or origin chain.
- `D7-PASS-CONTRACT`: compiler pass consumed or produced undeclared IR facts.
- `D7-CAPABILITY-BYPASS`: extension attempted authority outside declared grants.
- `D7-UNSAFE-LEAK`: unsafe extension exposed safe API without wrapper proof and audit.
- `D7-HERMETIC-BUILD`: build extension used file/env/network/shell/model effects not granted by build policy.

## Conformance Criteria

- Each extension kind has a manifest schema.
- Macro extension fixtures prove hygiene, provenance, and profile legality.
- Compiler-pass fixtures prove preserved and invalidated facts.
- Provider fixtures prove profile support and rejected unsupported profiles.
- Unsafe extension fixtures prove safe-wrapper containment.
- Hermetic build fixtures reject unauthorized build effects.
- Extension artifacts include source, package, version, signature, and compatibility metadata.

## Change Control

Adding a new extension kind requires updates to manifests, package policy, conformance tests, governance, and any affected phase contract. Extensions that can affect safe-code guarantees, profile legality, compiler correctness, or capability authority require safety and compatibility review.
