# Original actual-owner predicate family control

Owner: ACC-01. Base main `e047157d711652a5624012f0da7d6911ce086718`.
Historical source: PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635`.
This test-only draft restores the original predicate-family test file, including
its family table and complete multi-owner control, with three exact raw edits.
No production or preexisting test byte changes.

## Preserved obligations and adaptations

The original test covers Map, Set, List, Symbol, Bool, Character, nil, Keyword,
String, integer and Vector predicates. It retains actual C7 derivation, complete
C7/Effects/lowering descriptor equality, Effects context and call-join validation,
typed lowering, runtime evaluation, coherent obsolete/unknown-field forgeries,
late-invalid positional type data and opaque zero-callback checks. These are
normalized actual-owner controls under C7/C8/C14 and D9, not authenticated source
or release admission.

The original Effects and lowering `lc-type-work` calls use retired APIs.
Replace those two calls with their existing `lc-validate-types` interfaces and
remove the allowance argument. No validator is reimplemented or stubbed.
The obsolete `:operand-type-work` forgery formerly read a work counter from that
validation result; retain the forgery with explicit values 0, 1 and nil, alongside
the original unknown-field mutation. The closed-descriptor rejection obligation
survives even though validation no longer emits a work counter. Every other raw
original byte and every family entry remains unchanged.

## Artifacts and dependencies

`predicate-family-controls-census.edn` records the exact three old/new text
replacements, historical and candidate file/form hashes, unchanged family table,
all three production file hashes, 114 selected actual-owner source definitions
and 22 upstream input/derive helper forms. These pins establish exact source
inputs, not a transitive call graph, semantic equivalence or a passing kernel.
The previous source-only predicate records retain their original inputs and
explicit helper differences; this draft does not silently turn those records
into semantic acceptance.

The local Clojure checker verifies the complete original-to-candidate raw
transformation, unique forms, all eleven families, every selected definition
and unchanged production. It reads with evaluation disabled and leaves Bootstrap
unloaded. The entire restored test remains unexecuted.

## Validation and acceptance

- Source integrity: PASS, one restored test file, three raw replacements,
  eleven unchanged families, 114 owner definitions and 22 upstream helpers.
- Language boundary: PASS, 6 tests / 11 assertions.
- clj-kondo 2025.10.23, repository config and cache disabled: zero errors,
  warnings or infos for both the original test and the adapted candidate.
- Canonical docs, full-language roadmap and whitespace: PASS.

All genuine C7, Effects, lowering and runtime execution is UNRUN. Independent
source/result acceptance, permitted actual-owner composition and integration
remain pending; coordinator alone routes integration. The restored file supplies
a concrete test successor for the original authored path, not proof that it
passes. No source authentication, whole predicate accounting removal, helper
semantic equivalence, ACC/PR502 closure, release or self-hosting is claimed.
