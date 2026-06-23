# STD4 - String and Text Library Specification

Sequence: 214
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.text` defines strings, Unicode text processing, encodings, parsing, formatting, and locale-explicit text APIs.
It separates bytes from text and scalar values from grapheme clusters.
It gives macros, schemas, IO, HTTP, AI prompts, UI libraries, diagnostics, and generated artifacts a common text contract.

The text library must be portable where semantics are portable and explicit where they depend on Unicode version, locale, host services, normalization tables, or encoding policy.
Invalid byte sequences, invalid scalar values, lossy conversions, and locale-dependent results are never silent.
Text APIs must expose whether they allocate, borrow, slice, normalize, validate, or stream.

## Requirements

- Strings MUST be immutable Gravity values.
- Bytes and text MUST be distinct types; conversion requires an explicit encoding and error policy.
- UTF-8 validation MUST be available as a checked operation.
- Indexing MUST distinguish byte offset, scalar index, and grapheme cluster index.
- Slicing by byte offset MUST prove boundary validity or return a checked error.
- Normalization MUST declare Unicode version and normalization form.
- Locale-sensitive operations MUST require an explicit locale value.
- Formatting and parsing MUST be deterministic under their declared locale, number mode, and time provider.
- Regular expression or parser APIs MUST declare compilation effects, resource limits, and backtracking policy.
- Host-backed Unicode libraries MAY be used only when their version and behavior are captured in artifacts.

## Module Surface

- Types: `String`, `Text`, `Bytes`, `Utf8`, `Scalar`, `Grapheme`, `Locale`, `Encoding`, and `TextError`.
- Encoding APIs: `utf8/validate`, `utf8/decode`, `utf8/encode`, `decode`, `encode`, `transcode`, and `lossy-decode`.
- Inspection: `empty?`, `count-bytes`, `count-scalars`, `count-graphemes`, `starts-with?`, `ends-with?`, and `contains?`.
- Slicing: `slice-bytes`, `slice-scalars`, `slice-graphemes`, `take`, `drop`, `split`, and `lines`.
- Transformation: `normalize`, `case-fold`, `upper`, `lower`, `trim`, `replace`, `join`, and `interpolate`.
- Parsing and formatting: `parse-int`, `parse-float`, `format`, `format-locale`, `read-token`, and `write-token`.
- Pattern APIs: `pattern`, `match`, `find`, `replace-pattern`, and bounded parser combinators.
- Artifact APIs: `unicode-version`, `normalization-table-id`, and `text-fixture-id`.

## Dependencies

- `D1` and `D8` for string literals, diagnostics, and source spans.
- `L2`, `L5`, `L6`, and `L10` for text types, effects, capabilities, and collection interaction.
- `SAFE1`, `SAFE4`, `SAFE9`, `SAFE11`, and `SAFE15` for checked boundaries, numeric parsing, taint, injection-sensitive strings, and proof-backed checks.
- `P1` through `P13` for profile-specific allocation and host delegation.
- `STD2` for equality and hash semantics.
- `STD3` for sequences and collection views.
- `STD8`, `STD9`, `STD10`, `STD13`, and `STD19` for IO, HTTP, serialization, AI prompts, and UI text.

## Example

```clojure
(ns sample.text
  (:require [gravity.text :as text])
  (:profile :core))

(defn safe-prefix [s n]
  (text/slice-graphemes s 0 n))
```

The example slices by grapheme count, not byte offset.
If `n` is outside the valid range, the operation returns a checked text error or is rejected when statically invalid.

## Profile Availability

- `:core` receives immutable text, UTF-8 validation, scalar operations, grapheme iteration, normalization with declared tables, and deterministic formatting.
- `:hardware` receives only compile-time strings, byte arrays, and statically checked encodings.
- `:firmware` receives bounded text buffers and explicit encoding conversion when allocation is declared.
- `:kernel` receives byte/text conversion only under bounded memory and explicit error policy.
- `:native` receives optimized Unicode tables, SIMD validation, and memory-backed views under safety checks.
- `:hosted` may delegate to host Unicode libraries with version and behavior artifacts.
- `:distributed` may persist text only with canonical encoding and normalization policy.
- `:ai` must track prompt taint, output validation, and schema boundaries for text crossing model calls.
- `:formal` requires total text algorithms or explicit proof obligations for partial operations.

## Outputs and Artifacts

- Text module manifest with Unicode version, normalization table identifiers, and profile matrix.
- Type/effect signatures for encoders, decoders, parsers, formatters, and pattern APIs.
- UTF-8, boundary, normalization, locale, parsing, formatting, and regex fixtures.
- Negative fixtures for invalid byte boundaries, silent lossy decoding, implicit locale, and unbounded pattern evaluation.
- Host delegation records for managed Unicode providers.
- Taint propagation fixtures for AI, HTTP, database, UI, and shell-adjacent text uses.
- Performance evidence for validation, normalization, slicing, and formatting implementations.

## Diagnostics

- `STD4001` when bytes are used as text without validation.
- `STD4002` when slicing uses an invalid byte, scalar, or grapheme boundary.
- `STD4003` when normalization omits Unicode version or form.
- `STD4004` when locale-sensitive behavior omits an explicit locale.
- `STD4005` when lossy decoding is not marked in the type or result.
- `STD4006` when a pattern can exceed the declared resource limit.
- `STD4007` when host text behavior lacks a delegation artifact.
- `STD4008` when tainted text crosses a security boundary without validation.

## Conformance Criteria

- UTF-8 fixtures distinguish valid, invalid, incomplete, overlong, and boundary-sensitive inputs.
- Normalization fixtures record Unicode version and produce stable results across targets.
- Locale-sensitive APIs reject calls without explicit locale data.
- Pattern APIs enforce bounded resource policy in every supported profile.
- Text equality, hash, and ordering agree with STD2 and declared normalization policy.
- Restricted profiles reject unbounded allocation and host-only text services.
- Documentation examples compile and produce the same Gravity-level result across supported targets.
- AI, schema, HTTP, and UI integration fixtures preserve taint and canonical encoding metadata.
