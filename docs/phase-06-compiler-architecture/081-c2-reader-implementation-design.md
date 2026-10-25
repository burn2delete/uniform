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
 :path actual-supplied-path
 :extension ".gravity"
 :project-relative-path "src/app.gravity"
 :encoding :utf-8
 :bytes-hash bytes-hash
 :project-root project-hash
 :reader-options {:retain-comments true
                  :enabled-features #{:standard-reader}
                  :extension-policy policy-hash}
 :source-kind :gravity}
```

Source identity is based on source bytes, declared encoding, the explicit
project-root id, source path relative to that root, reader options, and feature
decisions. The actual supplied path and extension remain provenance fields
outside the normalized identity tuple. Source identity excludes downstream
semantic facts and ambient process state such as the current working directory.

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

The executable bootstrap representation is a flat, authenticated graph. Token
and form raw spellings may be encoded as source-slice references containing the
source-content identity and exact byte and scalar bounds. Atomic decoded values
are stored once in a semantic-value table and referenced by exact value ids;
collection and wrapper values are reconstructed from child and parent form ids.
Every slice, token, form, semantic value, parent, child, payload, and metadata
reference must resolve exactly once at the authenticated boundary. Recursive
values, repeated descendant raw bytes, and repeated full-product digest
preimages are not required reader products.

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

Numeric-candidate recognition is fail closed. A token whose leading spelling
selects numeric syntax but which fails every enabled numeric grammar produces a
reader diagnostic rather than becoming a symbol or leaking a host numeric
parser failure. Lexically valid values that require later semantic judgment,
including a zero-denominator ratio, retain their raw spelling and decoded
components and continue as deferred literal records.

The reader does not impose project source-byte, character, token, form,
delimiter-depth, container-width, scalar-size, result-node or construction-work
quotas. It does not require a work allowance, prospective reservation, debit
ledger or cost proof before accepting otherwise well-formed input. Numeric
normalization is not deferred because of spelling length or an estimated cost.
Duplicate-set validation and numeric normalization retain their semantic rules.

Intrinsic constraints remain mandatory: strict UTF-8 and Unicode scalar
validity, lexical and delimiter grammar, exact source-byte/hash joins, valid
indexes and arities, registered reader tags, native carrier classes and metadata
rules, and actual target-format representability. Unsupported host callbacks,
foreign comparators and true ancestor cycles remain rejected. A shared acyclic
value is validated independently at every occurrence.

The legacy reader-result `:bounds` field is an empty compatibility record; it
contains no capacity policy and grants no authority. Source records and replay
candidates cannot alter semantic validation. Changing reader code invalidates
the implementation binding and affected verification context. Full result
validation, exact canonical identities and independent fresh reconstruction
remain required, using every original source byte.

Actual heap, broker concurrency, process watchdog and capture controls protect
the execution host. Exhaustion of those controls is an execution resource
failure, not a fabricated reader-semantic rejection or a successful validation.
Historical quota policies, census results and failed attempts remain historical
evidence at their original revisions; they do not govern current admission.
The private decoder stores compact scalar rows and reconstructs character/span
views on demand. It must preserve exact UTF-8 bytes, Unicode-scalar and UTF-16
positions, CRLF handling, diagnostics and complete reader products. This storage
choice reduces retained intermediate structure without changing source identity,
semantic validation or fresh replay obligations.
The private phase loop replaces its interpreter environment between decoding,
scanning, parsing and accepted construction. Character storage is cleared after
scanning, so it is no longer retained by an enclosing decoded-result binding
during parsing or construction. Existing token and parse tables remain
available where required; no aggregate carrier or alias discount is introduced.
Verification still retains the submitted result and reconstructs independently
from every original source byte. This lifetime change does not establish a heap
bound; representative construction, validation and replay must demonstrate fit
under the separately controlled host resource class.

Reader digest requests use a private validating stream for the existing
`gravity-canonical-edn-v1` encoding. Complete validation preserves the canonical
domain, native carrier and metadata rules, Unicode validity and failure order
before opening the digest writer. Emission independently checks the same
semantic rules and preserves exact bytes, type tags, numeric spellings,
escaping, list/vector distinction and hexadecimal UTF-8 canonical key ordering.
Validation reports status only; it carries no work statistics or allowance.

The validation-only common scalar path applies the owning native and scalar
guards without constructing discarded tagged forms or integer decimal strings.
Common keys receive fresh validation before their exact canonical tags are
constructed. Uncommon scalars and compound values use the same canonical
normalizer. True ancestor cycles are rejected through identity-based path
tracking; shared acyclic objects are revisited without a validation cache.
Emission writes common scalar tags directly after their guards, using the
existing numeric spellings and String escape table while writing ordinary
String runs in bulk. Final native Long, Integer, Short and Byte wrappers retain
their direct decimal-text path. Other admitted integer representations retain
the canonical BigInteger conversion after native validation. Named-component
checks preserve code-unit semantics and Unicode refusal order without temporary
character sequences.
Freshly validated keyword
keys use the same fixed encoding and UTF-8 hexadecimal sort order; no key,
validation result or identity is cached across occurrences. Emission uses
one fixed 8,192-character writer buffer to batch UTF-8 encoder calls; closing
the writer flushes its final partial buffer before digest completion. This
bounded temporary buffer changes neither canonical bytes nor input capacity,
and establishes no whole-input time or heap guarantee.
Reader-private typed writer calls and character-array stores avoid reflective
method discovery while retaining the exact existing lowercase hexadecimal
encoding. The generic canonical implementation remains an independent oracle
and the fallback for uncommon keys and values.

The stream retains normalized keys and ordering data for the current map,
rather than normalized values for the entire token vector. Compound keys and
sets may still use the existing normalizer; this is not a universal
constant-memory or heap-fit guarantee. Digest-reference resolution, collision
checks, the generic canonical identity API, raw-byte hashing and complete fresh
reader reconstruction remain separate obligations. No encoding version,
identity domain or validation result is changed or
reused to authorize a later invocation.

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
- `C2-NUMERIC` for a numeric candidate that fails every enabled numeric literal
  grammar.
- `C2-IDENTIFIER` for a malformed symbol or keyword surface spelling.
- `C2-NS-SHAPE` for a namespace clause whose reader-level list, keyword, or
  clause-value shape is invalid. This is the C2 containment mapping of the L1
  owner rule `L1-NS-SHAPE`; diagnostics retain that owner rule as provenance.
- `C2-MAP` for odd map literal arity.
- `C2-SET` for duplicate literal set entries decidable at read time.
- `C2-METADATA` for metadata without a following form or invalid metadata shape.
- `C2-ABBREV` for invalid reader abbreviation placement.
- `C2-EXTENSION` for a source path outside the declared source-extension
  policy, or for an unknown, disallowed, or effect-violating reader tag
  extension.
- `C2-HASH` for unstable reader artifact identity or malformed authenticated
  product graphs.

Diagnostics must include source id, exact span, token or form id when available,
raw spelling, reader option set, extension tag when relevant, and remediation.

## Rejected Designs

Gravity rejects readers that execute macros or load code.

Gravity rejects source forms without stable source spans.

Gravity rejects reader extensions with ambient authority.

Gravity rejects literal decoding that loses raw spelling needed for diagnostics
or numeric modes, silently reclassifies a malformed numeric candidate as a
symbol, or exposes a host numeric parser exception.

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
- diagnostics for malformed delimiters, strings, numeric candidates,
  identifiers, maps, sets, metadata, and extensions,
- lexical acceptance and semantic deferral for valid numeric spellings whose
  value requires later numeric or safety analysis.
