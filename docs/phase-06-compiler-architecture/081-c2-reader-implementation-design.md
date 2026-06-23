# C2 - Reader Implementation Design

Sequence: 81
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The reader converts source bytes into token streams and syntax-object seeds. It
owns lexical decoding, delimiter structure, literal surface shape, reader
abbreviations, source maps, trivia retention, reader extension dispatch, and
incremental reader hashes.

The reader does not execute macros, resolve names, type check, effect check,
profile validate, or call host services beyond declared source input.

## Requirements

- Source decoding must be deterministic for a declared encoding and project
  policy.
- Every token and form must carry file identity, byte offsets, line and column
  spans, source hash, and reader-origin metadata.
- Reader abbreviations such as quote, syntax quote, unquote, metadata, and deref
  shorthand must expand to explicit syntax shapes with generated-origin links.
- Literal decoding must preserve radix, suffix, escape, exactness, and raw text
  facts needed by numeric and diagnostic phases.
- Comments and whitespace trivia must be retained when tooling or formatter
  modes request them.
- Reader extensions must be registered by build policy and may use only declared
  build effects.
- Reader output must be content-addressable for incremental compilation.
- Syntax-valid but semantically illegal forms must continue to later phases so
  the owning semantic diagnostic can be produced.

## Dependencies

- `L1` defines accepted surface syntax and reader-visible forms.
- `L4` defines macro expansion after reading.
- `L12` defines compile-time evaluation and build effects.
- `L15` defines capability providers for reader extensions.
- `C1` defines pipeline artifacts and pass contracts.
- `C3` consumes reader output as syntax objects.
- Package and build documents define source roots and reader extension policy.

## Outputs and Artifacts

- Source unit record.
- Token stream.
- Form tree.
- Syntax seed stream.
- Reader source map.
- Literal decoding records.
- Reader extension invocation records.
- Reader diagnostics.
- Incremental reader hash.

## Source Unit Record

```clojure
{:artifact :gravity/source-unit
 :source-id source-hash
 :path "src/app.gravity"
 :encoding :utf-8
 :bytes-hash bytes-hash
 :project-root project-hash
 :reader-options {:retain-comments true
                  :enabled-features #{:standard-reader}
                  :extension-policy policy-hash}
 :source-kind :gravity}
```

Source identity is based on source bytes, declared encoding, source path under
the project root, reader options, and feature decisions. It excludes downstream
semantic facts.

## Token Stream

Tokens record raw spelling and decoded kind:

```clojure
{:token-id :tok-42
 :kind :symbol
 :raw "defn"
 :decoded 'defn
 :span {:file source-id
        :byte-start 120
        :byte-end 124
        :line-start 8
        :column-start 2
        :line-end 8
        :column-end 6}
 :trivia-before [:newline :spaces]
 :reader-origin :source}
```

Token ids are stable within a source unit. Tooling may preserve trivia; compiler
semantic stages may ignore trivia after source maps are built.

## Form Tree

The reader builds delimiter and literal structure:

```clojure
{:form-id :form-17
 :kind :list
 :open-token :tok-40
 :close-token :tok-58
 :children [:form-18 :form-19 :form-20]
 :span span
 :metadata []
 :origin :source}
```

Malformed delimiter, map, set, metadata, string, or extension forms produce
reader diagnostics and no checked syntax seed for that form.

## Reader Abbreviations

Reader abbreviations are represented as explicit forms plus origin links:

```clojure
{:form-id :form-quote-1
 :kind :abbreviation
 :abbrev :quote
 :surface-span quote-span
 :expanded-form '(quote x)
 :generated-origin {:from :form-x
                    :reason :reader-quote}}
```

The macro expander receives explicit syntax, not a bare textual abbreviation.

## Literal Decoding

Literal records preserve raw and decoded views:

- integer radix and sign,
- integer width suffix where present,
- decimal exponent spelling,
- ratio numerator and denominator spelling,
- string escape sequence locations,
- character escape identity,
- keyword or symbol namespace spelling,
- tagged literal tag and payload form.

The reader validates lexical shape. Numeric family availability, overflow,
rounding, allocation, and profile legality are checked later.

## Reader Extensions

Reader extensions are declared in project or package policy:

```clojure
{:tag 'gravity/schema
 :handler gravity.reader.schema/read
 :build-effects #{}
 :capabilities #{}
 :profiles #{:meta}
 :output :syntax-seed}
```

An extension handler receives syntax seeds and returns syntax seeds. It may not
perform ambient IO, network, shell, environment, model, tool, reflection, or
host dynamic loading. Granted build effects are recorded in the invocation
artifact.

## Incremental Hashing

The reader emits hashes for:

- source unit,
- token stream,
- form tree,
- syntax seed stream,
- extension invocation set,
- reader diagnostics.

Whitespace and comments affect token and formatter hashes. They affect semantic
reader hashes only when retained trivia is part of the downstream artifact under
the selected mode.

## Diagnostics

Reader diagnostics use `C2` identifiers:

- `C2-ENCODING` for source decoding failure.
- `C2-DELIMITER` for unmatched or mismatched delimiters.
- `C2-STRING` for malformed string or character literals.
- `C2-MAP` for odd map literal arity.
- `C2-SET` for duplicate literal set entries decidable at read time.
- `C2-METADATA` for metadata without a following form or invalid metadata shape.
- `C2-ABBREV` for invalid reader abbreviation placement.
- `C2-EXTENSION` for unknown, disallowed, or effect-violating reader extension.
- `C2-HASH` for unstable reader artifact identity.

Diagnostics must include source id, exact span, token or form id when available,
raw spelling, reader option set, extension tag when relevant, and remediation.

## Rejected Designs

Gravity rejects readers that execute macros or load code.

Gravity rejects source forms without stable source spans.

Gravity rejects reader extensions with ambient authority.

Gravity rejects literal decoding that loses raw spelling needed for diagnostics
or numeric modes.

Gravity rejects treating syntax-valid semantic errors as reader errors.

## Conformance Criteria

A conforming reader must demonstrate:

- source decoding and stable source-unit hashes,
- token and form spans for representative source files,
- reader abbreviation expansion with generated origins,
- literal decoding records for numeric, string, symbol, keyword, and tagged
  literal cases,
- comment/trivia retention for tooling mode,
- deterministic incremental hashing,
- reader extension acceptance and rejection cases,
- diagnostics for malformed delimiters, strings, maps, sets, metadata, and
  extensions.
