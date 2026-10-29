# List construction admission and derive interface

Owner: ACC-01. Base main `e047157d711652a5624012f0da7d6911ce086718`.
Historical source: PR502 `6a4f87f013d0076b81aba9e0667650e70d28e635`.
This finite increment follows the accounting-removal rule, C7, C8, C14, L5 and
D9. It removes the List-construction operand ceiling and its C7 allowance
interface while preserving actual semantic/source validation.

## Production changes

Delete only `(<= (count args) 1023)` from the complete C7, Effects and lowering
`lc-source` headers. Their closed call/edge, binding, source, evaluation-order,
validation and descriptor checks remain unchanged. In particular, current
Effects/lowering native-map and exact accepted-validation guards survive; the
historical headers lack those newer guards and are not copied wholesale.

Restore complete historical `sh08-lc-derive` and `sh08-lc-step` forms. Derive no
longer accepts a budget, calculates a base/work charge, rejects underpayment,
or emits work counters. Step no longer accepts or forwards an allowance. The
sole C7 step caller in `sh08-ft-infer-pass-products-prepared` drops that one
argument. Every other expression in that complete inference function remains
unchanged. No zero telemetry or sentinel allowance replaces the deleted API.

The existing generic inference accumulator already treats an absent work field
with its existing default. Its other accounting remains open and unchanged;
this increment claims only the LC producer/caller removal. Effects context,
typed lowering and runtime accounting remain separate obligations.

Source/operand mismatch diagnostics retain semantic rule/reason distinctions.
The artificial arity-refusal branch becomes the ordinary operand-type rejection;
expected data is `:established-first-order-values`. Source mismatch remains
`C7-VERIFY` / `:list-construction-source-mismatch`. Descriptor values, effects,
allocation, constraints, Never handling and every source-form comparison survive.

## Test and source evidence

Migrate both existing direct Clojure derive callers to eight arguments, including
the literal List value used by the separate List-to-Set control. Remove only
exact-payment/underpayment and positive-counter assertions from the LC kernel;
change the old width-1024 refusal to acceptance. Preserve existing inference,
constraint, descriptor-forgery, source/binding, allocation/effect, Never and
malformed-type controls, plus the later width/depth/late-invalid helper checks.
All other SH08 units remain byte-identical; prior SH09/runtime test bytes remain
unchanged.

Three new actual-owner kernel definitions cover widths 0, 1023, 1024, 1025 and
2049. C7 checks full result type, the new step interface, actual operand extent,
absent counters, late invalid types and late source-parent tampering. Effects
and lowering check their real header functions against C7's complete header,
plus altered calls/edges, rejected/extra-field validation and late invalid types.
These are normalized source/Core DATA controls. All genuine kernel execution,
authenticated source, Effects context and typed/runtime composition are UNRUN.

`list-construction-admission-census.edn` pins six changed production definitions,
complete files/forms and direct source dependencies. It verifies byte conservation
outside the three header predicates, the two historical definitions and one
inference call. C7 has one derive caller and one step caller, both migrated.
The test census pins three existing changed SH08 units and three appended
actual-owner controls. Source comparisons use inert readers; they do not load
Bootstrap or evaluate Gravity.

## Validation and remaining acceptance

- Source conservation and caller/arity checks: PASS, six production definitions,
  two exact historical forms, three width deletions and one inference call.
- Unchanged original reader-only List-construction contract: PASS, 4 tests and
  167 assertions against the edited source. This is source-shape evidence only.
- Language boundary: PASS, 6 tests / 11 assertions.
- clj-kondo 2025.10.23, repository config/cache disabled: zero errors, 223 warnings
  and one info, with identical type/level/message finding multiplicities to the
  three base files. Source locations move with the removed lines.
- Canonical docs, full-language roadmap and whitespace: PASS.

Initial source comparison caught newer validation guards missing from the
historical Effects/lowering headers; the final predicate-only edit conserves
them. Local checker authoring also encountered reader errors and generated
syntax-quote symbol differences; corrected checks use complete raw-byte
conservation for untouched test text. These are source-check preparation, not
compiler/runtime outcomes or waived failures.

Keep draft pending independent source/result disposition and genuine permitted
acceptance. Coordinator alone routes integration and fresh preflight. PR622's
separate exact recursive-type controls are not imported or accepted here.
ACC06's retained finite List-return capture preparation is untouched. No whole
LC context/lowering/runtime, inference accounting, authenticated source,
main integration, ACC closure, release, self-hosting or PR502 closure is claimed.
