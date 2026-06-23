# L1 - Surface Syntax Specification

Sequence: 11
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

L1 defines the source syntax accepted by the Gravity reader before macro expansion and core lowering. The syntax is Lisp data with enough structure to support macros, profiles, effects, capabilities, types, diagnostics, and target-independent compiler artifacts.

L1 owns surface syntax only. It does not define core semantics for every form; `L2` does that. L1 does define which source shapes the reader accepts, how source is represented as syntax objects, and which syntax annotations must survive into later phases.

## Source Units

A Gravity source file contains zero or more top-level forms. A normal compilation unit begins with an `ns` form.

```clojure
(ns example.http
  (:profile :hosted)
  (:target :jvm)
  (:requires [gravity.net.http :as http])
  (:effects #{:network/http :io/write})
  (:capabilities #{:network/http :io/write})
  (:safety :safe))
```

Top-level forms may include definitions, declarations, macros, schemas, tests, module metadata, and profile-specific forms allowed by later documents.

The reader does not execute top-level forms. It produces syntax objects.

## Lexical Elements

Whitespace separates forms and is otherwise insignificant except inside strings and character literals.

Line comments begin with `;` and continue to the end of the line.

Block comments use `(comment ...)` as an ordinary form that macro/lowering rules may discard in code positions.

Symbols name vars, locals, types, protocols, macros, namespaces, and special forms. A symbol may be unqualified, namespace-qualified, or alias-qualified:

```clojure
value
gravity.core/map
http/get
```

Keywords are self-evaluating identifiers used for maps, options, effects, capabilities, profiles, and artifact tags:

```clojure
:hosted
:network/http
:artifact/schema
```

Qualified keywords use the same namespace rules as symbols.

## Literals

The reader accepts these literal families:

- nil literal: `nil`,
- booleans: `true`, `false`,
- integers: `0`, `42`, `-7`,
- radix integers where later numeric docs allow them: `0xFF`, `0b1010`,
- fixed-width integer annotations in type positions or literal suffix positions specified by numeric documents,
- decimals and floating literals,
- ratios where the active profile and numeric mode allow exact rationals,
- strings,
- characters,
- symbols,
- keywords,
- lists,
- vectors,
- maps,
- sets,
- tagged literals registered by safe reader extension policy.

Strings are Unicode scalar sequences. Escape processing must preserve source spans for diagnostics. Invalid escape sequences are reader errors.

Collection literals are immutable values at the surface level. Mutability, transient behavior, region allocation, and target layout are later semantic decisions.

## Collection Forms

Lists represent calls, special forms, macro invocations, declarations, and data when quoted.

```clojure
(f x y)
(if ready? value fallback)
'(data not call)
```

Vectors represent ordered literal data and binding vectors:

```clojure
[x y z]
[x :- I64 y :- I64]
```

Maps represent key/value literal data and metadata-like records:

```clojure
{:name "Ada" :active true}
```

Sets represent unordered unique literal data:

```clojure
#{:network/http :io/write}
```

The reader rejects malformed maps with an odd number of forms and malformed sets with duplicate literal keys when equality is statically decidable.

## Reader Macros and Abbreviations

L1 reserves these reader abbreviations:

```clojure
'form      ; quote
`form      ; syntax quote
~form      ; unquote inside syntax quote
~@form     ; splice-unquote inside syntax quote
^meta form ; metadata
@form      ; deref shorthand where enabled by core/runtime documents
```

Reader abbreviations lower to ordinary syntax objects with explicit forms and source spans. For example, `'x` is represented as `(quote x)` with provenance for both the abbreviation and expanded form.

Syntax quote is macro-oriented syntax. Its exact namespace qualification and hygiene behavior is owned by `L4`, but the reader must preserve enough structure for it.

## Metadata

Metadata attaches compile-time information to syntax objects and, where semantics allow it, to vars, types, functions, namespaces, schemas, and artifacts.

```clojure
^{:doc "Read one byte" :inline true}
(defn read-byte [port] ...)
```

Metadata is data. It may not execute code during reading. Metadata keys must be keywords or symbols. Metadata used by compiler stages must be declared by the owning document; unknown metadata is preserved but ignored unless a profile or tool marks it illegal.

## Type and Effect Surface

L1 reserves type annotation syntax but leaves type semantics to `L5`.

```clojure
(defn add [x :- I64 y :- I64] :- I64
  (+ x y))
```

Function, namespace, and binding forms may carry effect declarations:

```clojure
(defn fetch
  {:effects #{:network/http}
   :capabilities #{:network/http}}
  [url :- Url]
  :- HttpResponse
  ...)
```

The reader preserves these declarations as data. It does not decide effect legality; `L6`, later `L15`, and profile documents do.

## Namespace Form

The `ns` form is the only standard way to start a namespace.

Allowed clauses include:

- `(:profile p)` for a single active profile,
- `(:profiles #{...})` for reusable library declarations,
- `(:target t)` or `(:targets #{...})`,
- `(:requires [...])`,
- `(:imports [...])`,
- `(:exports [...])`,
- `(:effects #{...})`,
- `(:capabilities #{...})`,
- `(:safety mode)`,
- `(:providers [...])`,
- `(:doc "...")`,
- `(:metadata {...})`.

The reader validates only syntax shape. Name existence, profile legality, effect legality, and capability grants are later phases.

## Special Form Surface

L1 reserves source spellings for core special forms:

```clojure
quote if do let fn loop recur def var set! try throw match
```

These names are reserved in operator position unless a later document explicitly defines a qualified escape. Surface macros may expand into these forms. User code may quote them as data.

## Profile-Visible Syntax

Surface syntax can be accepted by the reader and still be illegal for a profile after macro expansion.

Examples:

- `eval`-like forms may be syntax-valid but rejected in `:core`, `:firmware`, `:kernel`, and `:hardware`.
- Host interop forms may be syntax-valid but rejected outside allowed hosted/native interop contexts.
- Raw-memory forms may be syntax-valid but rejected outside unsafe islands or safe wrappers.
- AI forms may be syntax-valid but rejected without `:ai` profile or model/tool capabilities.
- Build-effect forms may be syntax-valid but rejected by hermetic build policy.

The reader must not erase these forms before profile validation can report the correct diagnostic.

## Requirements

- The reader must produce syntax objects, not direct AST nodes.
- Every syntax object must preserve source file, byte/line/column span, raw form kind, metadata, namespace context when known, and reader-origin data.
- Reader abbreviations must expand into explicit syntax objects with generated-origin links.
- Metadata, type annotations, effect declarations, profile clauses, capability clauses, safety clauses, and target clauses must survive reading and macro expansion.
- Reader extensions must be registered through extension policy and cannot perform hidden IO, network, shell, model, or host reflection.
- Malformed source is rejected before macro expansion with stable diagnostics.

## Dependencies

L1 depends on `D0`, `D1`, and `D3`.

It is upstream of `L2`, `L3`, `L4`, `L5`, `L6`, `L10`, `SAFE1`, `P1`, `C2`, and `C3`. L1 does not depend on `L2`; instead, L2 assigns meaning to forms whose surface is reserved here.

## Outputs and Artifacts

L1 requires:

- syntax object stream,
- reader diagnostics,
- reader extension registry entries,
- abbreviation expansion provenance,
- source map data,
- literal decoding records for target-sensitive literals,
- namespace clause syntax records.

## Rejected Syntax

The reader rejects:

- unbalanced delimiters,
- malformed strings or invalid escapes,
- malformed map literals,
- duplicate set literals when decidable at read time,
- metadata not followed by a form,
- namespace clauses with invalid list/vector/map/set shape,
- reader extension tags not registered in the active build policy,
- reader extensions that require ungranted build effects.

Syntax-valid but semantically illegal forms are not reader errors. They continue to later phases so diagnostics can name the semantic rule violated.

## Diagnostics

- `L1-DELIMITER`: unbalanced or mismatched delimiter.
- `L1-STRING`: invalid string or character literal.
- `L1-MAP-ARITY`: map literal contains an odd number of forms.
- `L1-METADATA`: metadata form is malformed or unattached.
- `L1-NS-SHAPE`: namespace clause has invalid syntax shape.
- `L1-READER-EXTENSION`: reader extension is unknown, disallowed, or requires ungranted build effect.
- `L1-SOURCE-ENCODING`: source bytes cannot be decoded according to project policy.

Each diagnostic includes source span, raw text excerpt when safe, reader state, and remediation.

## Conformance Criteria

- Reader fixtures cover every literal family and collection form.
- Reader abbreviation fixtures preserve both original and expanded spans.
- Namespace fixtures preserve profile, target, effect, capability, safety, provider, and import clauses.
- Malformed source fixtures produce stable diagnostics.
- Macro expansion fixtures can trace generated forms back to L1 syntax objects.
- Profile rejection fixtures confirm the reader accepts syntax that later phases reject for semantic reasons.

## Change Control

Adding or changing surface syntax affects reader, syntax objects, macros, formatter, linter, LSP, diagnostics, tests, and bootstrapping. Syntax changes require fixtures for read, print, format, macro expansion, and diagnostics before adoption.
