# C3 - Syntax Object Model

Sequence: 82
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Syntax objects are Gravity's compiler values for source and generated syntax.
They combine a form with identity, source location, namespace context, hygiene
context, compile phase, profile context, metadata, facts, and provenance.

Macros, analyzers, tools, diagnostics, incremental compilation, and
self-hosting all operate on syntax objects rather than raw lists.

## Requirements

- Every syntax object must have a stable syntax id, form kind, source or
  generated span, origin chain, namespace context, compile phase, profile
  context, metadata, and hygiene context.
- Syntax transformations must preserve origin chains and source maps.
- Generated syntax must record producer identity, input syntax ids, expansion
  step, and generated span.
- Hygiene marks and explicit capture records must be inspectable.
- Syntax objects may accumulate facts, but facts produced by semantic phases
  must be versioned and invalidated when transformations affect them.
- Syntax objects must be immutable values. Transformations produce new syntax
  objects with links to prior syntax ids.
- Syntax object serialization must be stable enough for incremental builds and
  self-hosting comparison.

## Dependencies

- `C2` provides tokens, form trees, syntax seeds, and source maps.
- `L1` defines surface syntax.
- `L3` defines namespace context.
- `L4` defines macro input and output requirements.
- `L5`, `L6`, and safety documents define semantic facts that later attach to
  syntax-derived nodes.
- `C4` consumes syntax objects for expansion.
- `C5` consumes syntax objects for name resolution.

## Outputs and Artifacts

- Syntax object schema.
- Syntax object stream.
- Hygiene context map.
- Origin-chain graph.
- Metadata ledger.
- Generated syntax report.
- Syntax verification report.
- Syntax serialization fixture.

## Syntax Object Shape

```clojure
{:artifact :gravity/syntax-object
 :syntax/id syntax-hash
 :form {:kind :list
        :value [child-id-1 child-id-2]}
 :span {:primary source-span
        :all [source-span generated-span]}
 :source {:source-id source-hash
          :form-id :form-17
          :token-range [:tok-40 :tok-58]}
 :namespace {:current 'app.main
             :aliases {'http 'gravity.net.http}
             :imports []}
 :phase :read
 :profile :native
 :metadata {:doc "example"}
 :hygiene {:marks [mark-1]
           :renames {}
           :captures []}
 :origin [{:kind :source
           :span source-span}]
 :facts {}
 :version 1}
```

The syntax id is content-derived over semantic syntax fields and origin links.
Tooling-only trivia is referenced, not embedded in the identity unless the
artifact kind requires it.

## Form Kinds

Required form kinds include:

- list,
- vector,
- map,
- set,
- symbol,
- keyword,
- string,
- character,
- numeric literal,
- boolean,
- nil,
- tagged literal,
- metadata wrapper,
- abbreviation expansion,
- generated form.

Each form kind stores enough raw spelling and decoded value to support
diagnostics and later semantic phases.

## Origin Chain

An origin entry records:

- source span or generated span,
- producer kind such as reader, macro, compiler pass, provider, or tool,
- producer identity and version,
- input syntax ids,
- reason for generation,
- build effects used by the producer when relevant.

Diagnostics for generated syntax must be able to show both the generated form
and the source or macro call that produced it.

## Hygiene Context

The hygiene context contains:

- marks introduced by macro expansion,
- lexical scopes,
- rename mappings,
- introduced identifiers,
- explicit capture declarations,
- macro definition namespace,
- macro call-site namespace.

Accidental capture is a verifier error. Intentional capture must name the
captured identifier and macro API that requested capture.

## Metadata and Facts

Metadata comes from source and macro output. It is preserved unless a
transformation records an explicit metadata change.

Facts are compiler-produced annotations such as:

- tentative namespace,
- declared profile,
- declared effects,
- declared capabilities,
- safety mode,
- resolved binding,
- type facts,
- effect facts,
- ownership facts,
- proof obligations.

Facts include producer stage and invalidation conditions. A syntax object cannot
pretend stale facts are current after a transformation.

## Generated Syntax

Generated syntax records:

```clojure
{:origin [{:kind :generated
           :producer {:kind :macro
                      :name 'gravity.core/when
                      :version macro-version}
           :inputs [call-site-syntax-id]
           :generated-span "generated:gravity.core/when:1"}]
 :hygiene {:marks [macro-mark]
           :captures []}}
```

Generated syntax is checked under the caller's profile unless the producing
document explicitly defines a different profile transition.

## Syntax Verification

The syntax verifier checks:

- required fields are present,
- source spans resolve to source units,
- generated origins have valid producers,
- hygiene marks are well scoped,
- metadata has valid syntax shape,
- namespace context is structurally valid,
- phase transitions are allowed,
- serialized form round-trips.

Verifier failures stop macro expansion and analysis.

## Diagnostics

Syntax object diagnostics use `C3` identifiers:

- `C3-SHAPE` for malformed syntax object fields.
- `C3-ID` for unstable or inconsistent syntax ids.
- `C3-SPAN` for invalid source or generated spans.
- `C3-ORIGIN` for missing or broken origin chains.
- `C3-HYGIENE` for malformed hygiene context.
- `C3-CAPTURE` for accidental or undeclared capture.
- `C3-METADATA` for invalid metadata representation.
- `C3-FACT-STALE` for stale facts used after transformation.
- `C3-SERIALIZE` for non-round-tripping syntax artifacts.

Diagnostics must include syntax id, form kind, phase, producer, source span,
origin chain, hygiene context summary, and remediation.

## Rejected Designs

Gravity rejects raw-list macro APIs.

Gravity rejects syntax values without source or generated origin.

Gravity rejects mutable syntax objects.

Gravity rejects hidden hygiene state unavailable to tools and diagnostics.

Gravity rejects generated syntax that bypasses caller profile checks.

## Conformance Criteria

A conforming syntax object implementation must demonstrate:

- construction from reader syntax seeds,
- stable syntax ids,
- source and generated origin chains,
- hygiene mark propagation,
- intentional capture and accidental capture rejection,
- metadata preservation and explicit metadata changes,
- fact attachment and invalidation,
- serialization round-trip,
- diagnostics pointing to both generated and source origins.
