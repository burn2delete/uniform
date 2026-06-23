# L14 - Language Facet System Specification

Sequence: 24
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity uses language facets to support specialized domains without creating
separate languages. A facet is a declared surface, set of macros, checker hooks,
domain IR, lowering rules, diagnostics, artifacts, and conformance tests that
live inside the normal Gravity compilation pipeline.

Facets let Gravity express schemas, elementary mathematics, circuits, workflows,
AI agents, prompt/tool systems, UI trees, relational queries, compiler passes,
and other domain-specific forms while preserving one source model, one macro
model, one type and effect discipline, and one profile system.

## Requirements

- A facet must declare its surface forms, profiles, effects, capabilities,
  lowering targets, artifacts, and conformance tests.
- Facet syntax must lower to checked Gravity forms, declared domain IR, or both.
- Facet processing must preserve source spans, hygiene context, type and effect
  annotations, generated-origin chains, and profile assumptions.
- Facet activation must be explicit and namespace-scoped unless generated with
  provenance by a macro.
- Facet IR must be versioned, serializable, and diagnosable against source.

## Dependencies

- `L1` defines the syntax object substrate used by facet forms.
- `L3` defines namespace-scoped activation and name resolution.
- `L4` defines macro interaction with facet syntax.
- `L5` and `L6` define type and effect legality.
- `L12` defines build effects for facet expansion and artifact generation.
- Later domain phases define the concrete IR and runtime behavior for each
  standard facet.

`L15` is a later refinement for capability providers used by facet services.

## Outputs and Artifacts

- Facet manifest.
- Facet activation records.
- Generated Gravity forms with provenance.
- Domain IR with source maps and artifact schema version.
- Facet diagnostics.
- Compatibility and migration records.

## Facet Principle

A facet may extend syntax and artifacts, but it may not bypass the language. The
output of a facet is checked Gravity core, a declared domain IR, or both. The
facet must preserve source spans, hygiene, type information, effects,
capabilities, profile constraints, safety obligations, and build provenance.

Gravity rejects "stringly embedded" sublanguages as the primary facet model.
Facets may parse external syntax when interop requires it, but the parsed result
must become structured syntax or domain IR with diagnostics that point back to
the original source.

## Terms

- A facet is a named language extension with manifest metadata.
- A facet form is source syntax owned by a facet.
- A facet expander rewrites facet forms into Gravity forms or facet IR.
- A facet checker validates domain rules not expressible in the base type system.
- A facet IR is a typed domain representation such as EFIR, workflow IR, circuit
  IR, query IR, UI IR, or prompt/tool IR.
- A facet artifact is an emitted file, record, proof, schema, generated source,
  backend input, or runtime manifest.
- A facet boundary is the point where facet syntax enters or leaves ordinary
  Gravity code.
- A facet grant is permission to use facet-specific build effects, compiler IR
  access, external tools, or runtime capabilities.

## Facet Manifest

Every facet has a manifest:

```clojure
{:facet/id :gravity.facet.workflow
 :version "0.1.0"
 :surface #{'workflow 'step 'activity 'signal}
 :profiles #{:hosted :native :distributed}
 :requires-facets #{:gravity.facet.schema}
 :runtime-effects #{:io/read :io/write :network/http}
 :build-effects #{:build/write-artifact :compiler/write-ir}
 :capabilities #{:workflow/scheduler :workflow/store :compiler/ir-transform}
 :lowers-to #{:gravity.core :gravity.ir.workflow}
 :artifacts #{:workflow/schema :workflow/replay-plan}
 :stability :experimental}
```

The compiler must load facet manifests before expanding namespaces that depend
on them. A namespace can activate facets through namespace metadata:

```clojure
(ns app.orders.workflow
  (:profile :distributed)
  (:facets #{:gravity.facet.workflow
             :gravity.facet.schema}))
```

Facet activation is lexical to the namespace unless a macro explicitly emits a
nested activation with provenance. Importing a namespace does not implicitly
enable all of that namespace's facets in the importer.

## Initial Facets

Gravity's initial facet set is:

- `:gravity.facet.schema` for schemas, validation, codecs, and contract data.
- `:gravity.facet.efir` for elementary functions, differentiable expressions,
  numeric simplification, and proof-oriented math IR.
- `:gravity.facet.zk` for zero-knowledge and privacy-preserving relation
  declarations, witness boundaries, public/private input manifests, proof
  system selection, verifier artifacts, and disclosure policy records.
- `:gravity.facet.hardware` for circuits, modules, registers, clocks, buses,
  memory maps, and hardware synthesis artifacts.
- `:gravity.facet.workflow` for durable workflows, steps, retries, timers,
  signals, compensation, and replay constraints.
- `:gravity.facet.agent` for agents, prompts, tool calls, model calls, memory,
  guardrails, and evaluation traces.
- `:gravity.facet.ui` for UI trees, attributes, component composition, layout,
  platform lowering, and event handlers.
- `:gravity.facet.query` for relational, graph, document, and streaming queries.
- `:gravity.facet.compiler` for compiler passes, IR transforms, optimization
  hooks, and self-hosting tools.
- `:gravity.facet.ffi` for foreign declarations, ABI contracts, marshalling,
  ownership transfer, and exception boundaries.

Additional facets may be added by packages, but package facets are subject to
the same manifest, compatibility, effect, profile, and conformance rules as
standard facets.

## Expansion and Lowering

Facet processing happens in ordered steps:

1. Read source into syntax objects.
2. Resolve namespace facet activations.
3. Expand base macros that introduce facet forms.
4. Dispatch facet forms to the owning facet expander.
5. Typecheck facet-local bindings and domain constraints.
6. Emit Gravity forms, facet IR, or both.
7. Validate generated Gravity code under the active profile.
8. Validate facet IR under the facet checker.
9. Emit artifacts with source and generated-origin provenance.

A facet may lower directly to Gravity core when the semantics are ordinary
language semantics. A facet may lower to domain IR when the target needs domain
structure that would be lost by immediate core lowering. A facet may emit both,
for example a workflow facet may emit typed wrapper functions and workflow IR.

## Example: Schema Facet

```clojure
(ns app.user
  (:profile :native)
  (:facets #{:gravity.facet.schema}))

(schema User
  {:id    Uuid
   :name  String
   :email Email
   :roles (Vector Keyword)})

(defn parse-user [bytes :- Bytes]
  :- (Result User DecodeError)
  (decode/json User bytes))
```

The schema facet emits:

- A `User` type descriptor.
- Validation code.
- JSON codec metadata when requested.
- A schema artifact for tools.
- Diagnostics that point to individual fields.

The emitted code still typechecks as ordinary Gravity. The JSON decoder still
declares allocation and decode effects according to the active profile.

## Example: Hardware Facet

```clojure
(ns chip.counter
  (:profile :hardware)
  (:facets #{:gravity.facet.hardware}))

(module Counter
  (:clock clk)
  (:reset rst)
  (:input enable Bool)
  (:output value (UInt 8))

  (reg count (UInt 8) 0)

  (on-rising clk
    (when enable
      (set! count (+ count 1)))))
```

The hardware facet emits circuit IR, timing constraints, register metadata, and
backend artifacts. The ordinary Gravity `+` must resolve to a width-aware,
overflow-defined operation. Runtime allocation, GC, reflection, threads, and
host IO are illegal in this facet unless represented as hardware constructs.

## Example: Agent Facet

```clojure
(ns support.agent
  (:profile :ai)
  (:facets #{:gravity.facet.agent
             :gravity.facet.schema}))

(agent SupportAgent
  (:model :support-small)
  (:tools [lookup-order create-ticket])
  (:memory {:kind :bounded :max-items 32})

  (prompt
    "Resolve customer support requests using only granted tools."))
```

The agent facet emits prompt artifacts, tool schemas, model-call declarations,
evaluation hooks, trace requirements, and guardrail checks. Model and tool
access are effects and capabilities; they are not hidden runtime privileges.

## Type, Effect, and Capability Rules

Facet-local rules may refine Gravity's type system, but they may not replace it
without an alternative-system contract. A facet checker may prove additional
facts such as:

- A schema field is present.
- A circuit signal has a fixed width.
- A workflow step is replay-safe.
- A query only returns rows matching a declared shape.
- A zero-knowledge proof exposes only declared public inputs and public outputs.
- A prompt tool call matches a schema.
- A UI event handler runs in an allowed effect context.
- A numeric expression satisfies an EFIR proof obligation.

Those facts become typed artifacts or proof records consumed by later passes.
Runtime effects, build effects, and compiler effects remain visible at the facet
boundary. A facet that reads or writes compiler IR must declare
`:compiler/read-ir` or `:compiler/write-ir` plus the `:compiler/ir-transform`
capability; a facet that generates files must declare `:build/write-artifact`;
a facet that calls an external solver, synthesizer, model, or package tool must
declare the matching build effect and capability.

## Profile Rules

A facet manifest declares supported profiles. The compiler must reject facet use
outside that set before backend lowering.

Profile-specific behavior:

- `:core` accepts only facets that lower fully to portable typed core.
- `:hosted` may permit facets backed by hosted runtimes, reflection, or dynamic
  loading when effects and capabilities are declared.
- `:native` may permit facets that emit target-specific ABI, layout, vector, FFI,
  or linker artifacts.
- `:kernel`, `:firmware`, and `:hardware` require facets to avoid managed-runtime
  assumptions unless those assumptions are modeled by the target.
- `:distributed` requires replay-safe effects and deterministic workflow artifacts.
- `:ai` requires model, prompt, tool, memory, and evaluation provenance.
- `:meta` may inspect compiler internals only through declared compiler APIs.

Facet rejection must happen as a source diagnostic, not as a backend crash or an
unexplained missing symbol.

## Domain IR Requirements

Facet IR must include:

- Facet id and version.
- Source span map.
- Generated-origin map.
- Type and effect annotations.
- Profile and target assumptions.
- Capability requirements.
- Build inputs and generated outputs.
- Validation results and proof obligations.
- Stable serialization format.
- Compatibility version.

Domain IR may be consumed by optimizers, verifiers, code generators, package
tools, visualizers, or runtime systems. Any consumer must preserve enough source
mapping to report diagnostics against original Gravity source.

## Composition

Facets may compose only through declared boundaries. For example:

- A workflow step may use a schema facet for payload validation.
- An agent tool may use a query facet for database access.
- A UI component may use a schema facet for form validation.
- A hardware memory map may use a schema facet for register layout.
- A compiler facet may read EFIR proof artifacts for numeric optimization.
- A zero-knowledge facet may use the schema facet for public input formats,
  the security/crypto domain for witness custody, and the formal-verification
  domain for relation and proof-check records.

When two facets claim the same surface form, the namespace must disambiguate
through aliases or explicit activation. The compiler must reject ambiguous facet
dispatch.

Facet composition must not create hidden effects. If an agent facet invokes a
query facet through a tool, the resulting artifacts must show model effects,
tool effects, query effects, and capability requirements.

Privacy-preserving facets must also preserve disclosure boundaries across
composition. A facet that converts a private witness, encrypted value,
commitment, credential assertion, or proof artifact into another representation
must carry the original privacy label, witness-generation provenance, reveal
reason, and public-output schema unless a checked boundary operation explicitly
declassifies it.

## Versioning and Stability

Facet versions are part of compilation. A package lockfile records facet
identity, version, compatibility range, source hash, and artifact schema version.

Facet stability levels are:

- `:experimental` for source forms that may change.
- `:draft` for forms under active conformance development.
- `:stable` for forms with compatibility guarantees.
- `:deprecated` for forms that remain accepted with migration diagnostics.
- `:removed` for forms that are rejected by new compilers.

Changing source syntax, domain IR shape, lowering semantics, profile support,
or artifact schemas is a compatibility event. A stable facet must provide
migration diagnostics and, where possible, automatic rewrites.

## Diagnostics

Facet diagnostics use `L14` identifiers:

- `L14-FACET-NOT-ACTIVE` for using a facet form without activation.
- `L14-FACET-AMBIGUOUS` for multiple active facets claiming the same form.
- `L14-PROFILE` for facet use outside supported profiles.
- `L14-BUILD-EFFECT` for missing build effect declaration or grant.
- `L14-CAPABILITY` for missing runtime or compile-time capability.
- `L14-LOWERING` for facet output that cannot lower to declared core or IR.
- `L14-DOMAIN-CHECK` for failed facet-local validation.
- `L14-GENERATED-CODE` for generated Gravity code that fails normal checking.
- `L14-IR-SCHEMA` for invalid, stale, or incompatible facet IR.
- `L14-COMPOSITION` for illegal interaction between facets.
- `L14-PRIVACY-BOUNDARY` for facet lowering or composition that drops a
  private-input, witness, encrypted-value, credential, or disclosure-policy
  boundary.

Diagnostics must include facet id, facet version, active profile, source span,
generated-origin chain when present, requested effects and capabilities, and the
domain rule that failed. Privacy-boundary diagnostics must also include the
private value or witness id, public-output schema id, reveal reason when present,
and the facet edge that attempted to erase the boundary.

## Rejected Designs

Gravity rejects facets as separate languages with independent semantics. A facet
must enter the same compiler pipeline and artifact graph as ordinary Gravity.

Gravity rejects opaque string sublanguages as the main integration pattern.
External syntax may be imported, but it must become structured syntax or IR.

Gravity rejects facet-specific privilege. A compiler facet, hardware facet, or
agent facet cannot bypass the base type, effect, capability, memory, and safety
checks.

Gravity rejects backend-only validation. Illegal facet use must be diagnosed
before backend lowering whenever the source and manifest contain enough
information to make the decision.

Gravity rejects unversioned domain IR. Artifacts emitted by facets must be
stable enough for tools, audits, replay, and self-hosting.

Gravity rejects privacy-preserving facets that treat proof generation,
credential assertion, or private computation as opaque backend calls. The facet
IR must expose the declared relation, public/private input split, witness or
credential custody, disclosure policy, provider trust basis, and verification
artifact shape.

## Conformance Criteria

A conforming facet implementation must demonstrate:

- Manifest loading and namespace-scoped activation.
- Ambiguity rejection when forms are claimed by multiple active facets.
- Profile rejection before backend lowering.
- Build effect and capability checks for facet expansion and artifact emission.
- Generated Gravity code validated by the normal compiler.
- Domain IR with source maps, profile assumptions, type/effect annotations, and
  artifact schema version.
- Composition tests for at least two facets.
- Privacy-boundary preservation tests for `:gravity.facet.zk` composed with
  schema, security/crypto, and formal-verification artifacts.
- Version compatibility diagnostics and migration records.
- Negative tests for opaque, untracked, or privilege-bypassing facet behavior.
