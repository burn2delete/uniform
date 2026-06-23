# D3 - Terminology & Concept Model

Sequence: 4
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

D3 is the canonical glossary and concept graph for Gravity. Later documents use these terms with the meanings defined here. When a later document needs a narrower meaning, it must name the narrower context explicitly instead of silently redefining a core term.

The model exists to keep the project from collapsing distinct concepts such as profile and target, effect and capability, runtime and backend, syntax object and source form, safe code and verified code, or artifact and file.

## Concept Graph

Gravity concepts are related as follows:

```text
source bytes
  -> source forms
  -> syntax objects
  -> expanded syntax
  -> core AST
  -> typed/effected/profile-valid core
  -> safety-checked core
  -> Gravity MIR
  -> domain IRs
  -> target artifacts
  -> runtime execution or static consumption
  -> provenance, diagnostics, tests, and governance records
```

The graph is directed. A backend may not reinterpret source syntax. A runtime may not change profile legality. A package may not add authority. A domain IR may not become a separate language without an explicit semantic anchor back to typed core or MIR.

## Canonical Terms

| Term | Meaning |
| --- | --- |
| Gravity program | One or more namespaces plus package metadata, profile declarations, effects, capabilities, dependencies, and build policy. |
| Source bytes | The bytes read from source files before tokenization or decoding. |
| Source form | Reader-visible Lisp data before macro expansion. |
| Syntax object | Source form plus source location, namespace context, metadata, hygiene context, compile phase, profile context, and generated-origin chain. |
| Namespace | Named compilation unit that declares profile, imports, exported vars/types/macros, effects, capabilities, safety mode, target constraints, and metadata. |
| Macro | Compile-time transformer from syntax objects to syntax objects. |
| Expansion | The result of macro execution, preserving provenance from generated syntax to original syntax. |
| Core AST | Small semantic language after surface syntax and macros have been lowered. |
| Typed core | Core AST annotated with resolved names, types, effects, profile facts, and capability requirements. |
| Profile | Compile-time contract for legal forms, effects, capabilities, memory regime, runtime assumptions, unsafe rules, and target lowerings. |
| Target | Concrete emitted artifact family or backend environment such as C, LLVM, Wasm, JVM, JavaScript, HDL, workflow graph, query plan, schema bundle, or AI manifest. |
| Backend | Compiler component that lowers MIR or a domain IR into target artifacts. |
| Runtime | Execution support linked, delegated, or generated for a profile/target pair. |
| Effect | Semantic action visible to compiler and policy. Examples: IO, allocation, raw memory, FFI, network, time, random, shell, database, model call, tool call, and build effect. |
| Capability | Granted authority to perform an effect in a package, deployment, build, runtime, or tool context. |
| Grant | Concrete capability authorization from package manifest, deployment policy, build policy, or operator authorization. |
| Safety mode | Namespace or package safety policy such as `:safe`, `:safe-optimized`, `:audited-unsafe`, `:systems`, or `:trusted-runtime`. |
| Unsafe island | Explicit region of code whose preconditions, postconditions, effects, capabilities, invariants, owner, review status, and safe boundary are recorded in an audit artifact. |
| Gravity MIR | Target-independent, typed, effect-annotated, profile-valid IR used for optimization, verification, and lowering. |
| Domain IR | Specialized IR with a semantic anchor to typed core or MIR, such as EFIR, schema IR, workflow graph IR, AI agent IR, query IR, HDL IR, UI IR, or GPU IR. |
| Artifact | Typed output with provenance. Examples: binary, library, object file, schema, workflow graph, AI manifest, proof certificate, diagnostic bundle, benchmark report, SBOM, or generated documentation. |
| Provenance | Links that explain which source, compiler, package graph, pass contracts, profile, target, effects, capabilities, and policies produced an artifact. |
| Diagnostic | Stable explanation of a rejected or checked condition, including rule ID, source span or manifest entry, active profile, target, relevant facts, and remediation. |
| Schema | Source-level data contract that can generate validators, static types, canonical data encoders, GraphQL, OpenAPI, migrations, ABI layouts, config loaders, artifact manifests, and AI structured outputs. |
| EFIR | Elementary Function IR, the semantic carrier for analyzable math expressions. |
| EML | Elementary math language used for normalization, proof, synthesis, and search, not as the universal runtime representation. |
| Replay record | Durable record of nondeterministic workflow or AI decisions needed for replay and audit. |
| Human-review effect | Canonical `:ai/human-review` effect for human decision points in AI, workflow, tooling, shell, package, deployment, and other privileged actions. Approval, denial, repair request, escalation, timeout, and revocation are possible human-review outcomes, not independent capability grants. |
| Proof certificate | Machine-checkable evidence for safety, approximation, rewrite, optimization, pass correctness, or artifact provenance. |
| Seed compiler | Initial implementation used to bootstrap Gravity before components become self-hosted. |
| Self-hosted component | Compiler, build, package, standard-library, or tool component implemented in Gravity and validated through bootstrap equivalence. |

## Term Distinctions

Profile is not target. `:native` is a profile. `llvm-x86_64-linux` is a target. A profile states what the program may assume; a target states where a legal program is emitted.

Effect is not capability. An effect is what code does. A capability is authority to do it. Code may have a statically known `:network/http` effect and still be rejected if no package or deployment grant authorizes that effect.

Runtime is not backend. A backend emits target artifacts. A runtime provides execution support. The LLVM backend may emit a native artifact that links no runtime, minimal runtime, or a capability-enforcement runtime depending on profile and target.

Safe is not proved-correct. Safe Gravity means no undefined behavior in the specified safety model. It does not mean the program implements the user's intended business logic.

Artifact is not file. A file may store an artifact, but artifact identity includes kind, schema, source hash, compiler identity, profile, target, effects, capabilities, safety status, and provenance.

Macro is not text substitution. A macro transforms syntax objects and must preserve hygiene, source spans, generated-origin metadata, and profile legality.

Portable is not universal. A portable construct is portable across the profiles and targets it declares, not across all possible Gravity outputs.

Hosted is not default. Hosted features such as reflection, dynamic loading, host exceptions, and managed memory do not define core Gravity behavior.

Unsafe is not untracked. Unsafe code is explicitly tracked and audited; untracked unsoundness is rejected.

Human review is not ambient approval. Earlier approval-oriented wording maps to the repository contract term `:ai/human-review` and the A10 human-review document. A human-review record may approve, deny, escalate, expire, or revoke a request; only a matching capability grant and policy decision can authorize the effect.

## Concept Records

Compiler and tools should use concept records when exchanging metadata:

```clojure
{:concept :namespace
 :name my.app.main
 :profile :hosted
 :target :jvm
 :declared-effects [:io/write :network/http]
 :required-capabilities [:io/stdout :http/client]
 :safety :safe
 :source-span "src/main.gravity:1:1"}
```

```clojure
{:concept :artifact
 :kind :workflow-graph
 :profile :distributed
 :target :workflow-graph
 :source-hash "sha256:..."
 :compiler "gravity-0.4"
 :effects [:workflow/event :time/read :network/http]
 :capabilities [:http/client]
 :provenance [:macro-expansion :typed-core :workflow-ir :artifact-emission]}
```

These records are illustrative shapes; detailed schemas are owned by later schema, compiler, package, and artifact documents.

## Requirements

- Later documents must use D3 terms with D3 meanings unless they explicitly define a narrower local meaning.
- Normative text must qualify `runtime`, `dynamic`, `portable`, `safe`, `artifact`, `capability`, and `target` when ambiguity would affect implementation.
- Compiler, runtime, package, tooling, and governance artifacts must use the same vocabulary for profile, target, effect, capability, safety mode, and provenance.
- Diagnostics must report concepts using stable D3 names, not backend-specific approximations.
- Domain-specific documents must name how their domain concepts map to source forms, typed core, MIR, domain IRs, artifacts, effects, and capabilities.

## Dependencies

D3 depends on `D0` for project thesis and `D1` for architecture. It is upstream of every document that defines forms, profiles, effects, capabilities, memory, MIR, runtimes, artifacts, packages, tests, governance, AI, or standard-library behavior.

If a later document changes a concept name or introduces a new concept that crosses phase boundaries, D3 must be updated before downstream documents rely on that term.

## Outputs and Artifacts

D3 produces:

- canonical glossary,
- concept graph,
- ambiguity rules,
- concept record shapes,
- diagnostic vocabulary,
- term-governance policy.

Downstream artifacts should reference D3 terms by stable names so conformance tools can compare compiler output, runtime manifests, package manifests, diagnostics, and governance records without per-phase translation.

## Rejected Terminology

The following wording is rejected in normative sections unless narrowed:

- "runtime" without naming the runtime family,
- "target" when the text means profile,
- "capability" when the text means effect,
- "effect" when the text means authority,
- "artifact" when the text means arbitrary file,
- "safe" when the text means secure, correct, or verified,
- "dynamic" without profile and capability constraints,
- "portable" without declared profile and target set,
- "backend" when the text means runtime service,
- "macro expansion" when the text means string preprocessing.

## Diagnostics

Terminology diagnostics are emitted by doc tooling, compiler manifest checks, and governance review.

- `D3-AMBIGUOUS-RUNTIME`: normative text uses runtime without family.
- `D3-PROFILE-TARGET-CONFLATION`: a manifest uses a target where a profile is required or vice versa.
- `D3-EFFECT-CAPABILITY-CONFLATION`: a document or manifest treats effect presence as authority.
- `D3-ARTIFACT-UNSTRUCTURED`: an emitted file claims artifact status without required identity and provenance fields.
- `D3-UNSAFE-UNTRACKED`: text describes unsafe behavior without an unsafe island or audit artifact.

## Conformance Criteria

- The glossary includes every cross-phase concept used by the roadmap's critical pre-implementation documents.
- Every phase README and document uses profile, target, effect, capability, runtime, artifact, and unsafe with D3 meanings.
- Compiler diagnostics and artifact manifests use D3 concept names.
- Documentation checks reject ambiguous normative use of listed terms.
- Schema and package records can express the concept records in this document.

## Change Control

Changing a D3 term is a compatibility event. The change must identify all affected documents, artifact schemas, diagnostics, tools, package manifests, and governance records. Deprecated terms require a migration period and diagnostics that name the replacement.
