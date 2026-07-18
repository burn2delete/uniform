# SH-03 Reader Fixtures

Every source case is a byte-identical co-canonical `.gravity` / `.qst` pair.
The source extension and actual path remain provenance; neither changes the
paired source bytes.

## Accepted pairs

- `complete-reader-surface`: namespace clauses, comments, comma whitespace,
  nil, booleans, signed and arbitrary-size decimal integers, binary and
  hexadecimal integers, decimals and exponents, ordinary and deferred ratios,
  host-independent huge-decimal deferment,
  strings and basic/Unicode escapes, named/Unicode/octal/delimiter characters,
  Unicode scalar text, symbols, keywords,
  lists, vectors, maps, sets, quote, syntax quote, unquote, splice-unquote,
  deref, map/keyword/symbol metadata, and registered `inst` / `uuid` tags.
- `newline-lf`: physical LF line and comment termination.
- `newline-crlf`: physical CRLF line and comment termination.
- `newline-cr`: physical CR line and comment termination.
- `namespace-clause-shapes`: every L1 namespace clause container shape plus a
  later ordinary form that resembles a clause but is outside the `ns` form.
- `depth-512`: a complete namespace source followed by the accepted exact
  delimiter-depth boundary.
- `numeric-work-boundary`: accepted 256-scalar decimal/radix and bounded-ratio
  canonicalization cases at the declared numeric work edge.
- `numeric-semantic-work-deferred`: a read-valid 257-scalar integer remains
  accepted with lossless spelling and deferred cross-radix normalization.
- `numeric-set-distinct-deferred`: opposite-sign nonzero numeric members remain
  provably distinct when full normalization is deferred.
- `unicode-supplementary`: a direct supplementary-plane Unicode scalar
  character is retained as codepoint `128512`.

## Rejected pairs

| Base name | Expected diagnostic | Rejection |
| --- | --- | --- |
| `encoding-invalid-utf8` | `C2-ENCODING` | raw bytes `C0 AF`, an invalid overlong UTF-8 sequence |
| `delimiter-mismatched` | `C2-DELIMITER` | mismatched collection closer |
| `string-invalid-escape` | `C2-STRING` | unsupported string escape |
| `character-invalid` | `C2-STRING` | invalid named character |
| `character-surrogate-unicode` | `C2-STRING` | surrogate Unicode character escape |
| `map-odd-arity` | `C2-MAP` | map with an unmatched key |
| `set-duplicate` | `C2-SET` | duplicate literal set member |
| `set-equivalent-ratio` | `C2-SET` | arbitrary-size ratio equal to `1/2` |
| `set-equivalent-decimal` | `C2-SET` | equivalent decimal spellings |
| `set-equivalent-decimal-exponent` | `C2-SET` | equivalent arbitrary-size decimal exponents |
| `set-equivalent-integer` | `C2-SET` | equivalent arbitrary-size hex and decimal integers |
| `set-numeric-equivalence-limit` | `C2-HASH` | deferred numeric spellings whose equality cannot be decided within reader work bounds |
| `metadata-invalid-shape` | `C2-METADATA` | numeric metadata shape |
| `metadata-invalid-map-key` | `C2-METADATA` | metadata map with a non-symbolic key |
| `metadata-invalid-target` | `C2-METADATA` | metadata attached to a non-attachable literal |
| `abbreviation-unattached` | `C2-ABBREV` | quote without a following form |
| `abbreviation-unquote-outside` | `C2-ABBREV` | unquote outside syntax quote |
| `abbreviation-splice-outside` | `C2-ABBREV` | splice-unquote outside a syntax-quoted collection |
| `extension-unsupported` | `C2-EXTENSION` | unregistered reader tag |
| `extension-invalid-inst-calendar` | `C2-EXTENSION` | impossible `inst` calendar date |
| `extension-invalid-uuid-payload` | `C2-EXTENSION` | non-string `uuid` payload |
| `malformed-numeric` | `C2-NUMERIC` | repeated exponent marker (`1e2e3`) |
| `numeric-invalid-radix` | `C2-NUMERIC` | non-binary digit in a binary literal |
| `numeric-incomplete-exponent` | `C2-NUMERIC` | exponent sign without digits |
| `malformed-identifier` | `C2-IDENTIFIER` | lone keyword prefix |
| `character-octal-out-of-range` | `C2-STRING` | octal character above `0377` |
| `string-invalid-unicode-escape` | `C2-STRING` | surrogate Unicode string escape |
| `depth-513` | `C2-HASH` | reader form depth exceeds the bound of 512 |
| `namespace-missing-name` | `L1-NS-SHAPE` | `ns` form has no namespace name |
| `namespace-name-nonsymbol` | `L1-NS-SHAPE` | namespace name is not a symbol |
| `namespace-clause-not-list` | `L1-NS-SHAPE` | namespace clause is not a list |
| `namespace-clause-key-nonkeyword` | `L1-NS-SHAPE` | clause does not begin with a keyword |
| `namespace-clause-unknown` | `L1-NS-SHAPE` | clause key is not in the L1 catalog |
| `namespace-clause-single-arity` | `L1-NS-SHAPE` | single-value clause has extra values |
| `namespace-clause-set-shape` | `L1-NS-SHAPE` | set-valued clause uses a vector |
| `namespace-clause-vector-shape` | `L1-NS-SHAPE` | vector-valued clause uses a set |
| `namespace-clause-dependency-shape` | `L1-NS-SHAPE` | dependency clause mixes vector and non-vector arguments |
| `namespace-clause-doc-shape` | `L1-NS-SHAPE` | doc clause value is not a string |
| `namespace-clause-metadata-shape` | `L1-NS-SHAPE` | metadata clause value is not a map |

Large token-count and throughput sources are generated by dedicated tests
instead of being checked in. The semantic-work boundary is also generated: a
minimal namespace contributes 10 work units and each independent depth-512
form contributes 263682, so 32 forms are accepted at the exact bound of
8437834 and the 33rd form is rejected with `C2-HASH` / `semantic-work-limit`.

Deferred numeric literals remain valid outside sets. Set insertion uses exact
canonical equality within the numeric work cap, accepts bounded discriminators
that prove inequality, and rejects only undecidable comparisons with `C2-HASH`
/ `numeric-set-equivalence-limit`. Invalid input carriers are summarized rather
than echoed. Logical source ids and actual provenance paths are limited to 1024
UTF-16 code units, a conservative bound below the declared 4096-byte UTF-8
identity ceiling.
