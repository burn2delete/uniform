# Original recursive type controls

Owner: ACC-01. This test-only draft restores unique historical validation
obligations from PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635` on main
`e047157d711652a5624012f0da7d6911ce086718`, tree
`2b2168fe83fc08a7b4a882d75650c5b6e6eac31d`.

## Requirements and changes

The accounting-removal contract requires removal of artificial admission quotas
while preserving complete nested type validation, native class/callback/cycle
checks and late-invalid inputs. The C7 type contract and D9 require accurate
source and execution evidence. These controls preserve those requirements at
the actual C7 helper boundary; no Clojure substitute implements the validator.

The complete original `sh08-list-construction-unmetered-types-development-kernel`
body and comments are copied byte for byte. Its actual `sh08-lc-validate-types`
calls retain positive empty/scalar/width-4097/depth-192/operand-Never inputs;
late invalid member at index 4096; nested Never, odd Map member parity, extra
fields, cyclic maps and opaque receiver rejection; and zero callback assertions.
Genuine execution remains UNRUN.

The current Symbol-carrier kernel already covers depths 127 and 129, width
1025 and a late invalid member. The historical depth-192 Symbol-carrier case
was absent from that rewritten control. Add only `(nest 192 carrier)` to its
existing positive sequence, conserving all existing positive/negative cases,
API adaptations and callback checks. No production or other test bytes change.

## Dependencies and artifacts

`recursive-type-controls-census.edn` pins historical/base identities, raw copied
span, full test forms and files, the one literal byte replacement, and the
unchanged production C7 file. Nine source pins cover the actual validator and
its immediate source call targets. Five forms differ from history, including
four native/ancestor helpers and the iterative validator itself; the census
retains both identities or an explicit historical absence. No whole-helper
or transitive call-graph equivalence is claimed.

The local Clojure integrity checker reads with evaluation disabled, verifies
one exact original control plus the one Symbol-carrier insertion, compares every
other complete test form and production bytes, and leaves Bootstrap unloaded.
The original bodies and main implementations are independent source inputs;
matching pins do not prove their runtime behavior.

## Validation and acceptance

- Source integrity: PASS, raw historical control and literal Symbol-carrier
  insertion, all other test bytes and all production conserved.
- Language boundary: `clojure -M:test --namespace
  gravity.self-hosting.sh01-language-boundary-test`, PASS: 6 tests, 11 assertions.
- clj-kondo 2025.10.23 with repository config and cache disabled: zero errors,
  71 warnings identical to the complete base finding multiset.
- Structural documentation and full-language roadmap validators: PASS.
- Whitespace: PASS.

Independent acceptance, genuine helper execution, authenticated source and
Effects/emitter/runtime composition remain pending. The coordinator alone routes
acceptance and integration. This test-only restoration confers no whole ACC-06,
recursive accounting, main integration, release, self-hosting or PR502 closure
credit. Existing source owners and restricted execution paths are unchanged.
