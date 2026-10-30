# Preserved dispatch and direct-call source correspondence

Planning: ACC-01/04/08/12. Accountable disposition owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. DSE implementation ownership
remains solely with PR568's existing owner. This is a source-grounded mapping
proposal, not an independent incorporation/rejection decision or new source port.

## Inputs and governing boundary

Historical PR502 is pinned to `6a4f87f013d0076b81aba9e0667650e70d28e635`.
Assessed main is `98c391a71c69b8086f15053319de123ee8cbf7fc`. Existing DSE draft
PR568 is pinned to `f4c9fdcd5a3ca24be5b8e24a72ad9d908800796a`, tree
`71b343d4cc131ef3be1601c8f8ba12509744b31e`. Its affected source-only review
covers `ec085f5feb9f0f9719c6393e8b3f7d9b62d89eec`, tree
`66304198a8c44f59adc4349030541fd26d674e29`, receipt SHA256
`8b2994e26a18580bdaac238cd5652118a74f0737bfc1d095b4f9a8edb9573328`.
All twelve selected candidate definitions and the consumed DSE census remain
identical to that reviewed source. This does not expand the review to the new
mapping or later selector preparation.

[L5](../../docs/phase-01-core-language/015-l5-type-system-specification.md),
[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[C8](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md)
and [C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
require independently established type/source facts and preserved consumer
legality. [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
requires evidence matching the claim. A renamed call, identical body or source
call path does not establish execution or complete producer/consumer equivalence.

## Twelve exact original dispatch units

[The machine record](dispatch-source-correspondence.edn) accounts for these
original changed units, all present in the original 29-path inventory:

| Source relationship | Original definitions | Exact remaining qualification |
| --- | --- | --- |
| Five complete historical forms match PR568 after only listed helper-symbol correspondences | C7 `sh08-kse-call-fact`, `sh08-kse-constraint`, `sh08-tc-constraint`, `sh08-ft-call-facts-with-context`; Effects `body-tc-constraint` | The complete forms and alias map are pinned; helper semantic equivalence and this mapping's independent acceptance remain unestablished. |
| Two complete forms differ only at the listed DSE kind guard after helper correspondence | Effects `body-kse-call-fact`, `body-kse-constraint` | Historical `(if (keyword? kind) (= tag kind) false)` becomes `(= tag kind)`. Full actual expressions are recorded. The guard is not normalized away or independently declared equivalent. |
| Three complete historical else branches equal both assessed main and the reviewed DSE candidate | Effects `body-kse-call-valid?`; emitter `p15-s23-typed-compile-keyword-set-equality`, `p15-s23-typed-compile-equality` | Historical DSE dispatch moved through existing BKA entrypoints; preserving the fallback alone is not the whole DSE route. |
| One fallback has an additional explicit selector difference | Effects `body-ae-call-valid?` | Besides the moved outer DSE dispatch, one inner kind-selector Set omits the DSE kind. The record pins both Sets and verifies the complete form after only those listed transformations. It is not exact fallback equality. |
| One complete caller decomposes into a moved dispatch plus two exact inline bodies | Emitter `p15-s23-typed-compile-call` | The record pins old/new route conditions and branches, and both helper substitutions. Applying only those transformations yields the complete assessed-main caller. This is source decomposition, not semantic acceptance of moved dispatch. |

Eight structural source call paths pin the existing PR568 forwarding routes
through `body-bka-entry?`, `body-bka-call-valid?`, `p15-s23-bka-entry?` and
`p15-s23-typed-compile-bare-keyword-anchor-set-equality` to the actual DSE entry,
validation and compile functions. Every function on those paths is pinned to
the same reviewed source. Branch conditions, native DATA validation, complete
row/fact reconstruction and actual runtime effects still need their genuine
owner acceptance. Source edges alone do not prove semantic reachability.

The two explicit kind-guard differences remain distinct from those dispatch
paths. Do not treat a same-named family or a function touched by PR568 as proof
that an unrelated unique historical delta is incorporated.

## Direct-call helper incorporation proposals

The complete body of historical
`p15-s23-typed-direct-call-compatibility` occurs exactly once inline in the
unchanged assessed-main `p15-s23-typed-compile-call`. The complete body of
`p15-s23-typed-direct-call-arguments` also occurs exactly once there. The
historical caller invokes each helper once with exactly its declared parameter
symbols. Full helper, body and current caller hashes and exact call expressions
are recorded. The candidate's caller equals assessed main.

No implementation extraction or duplicated helper is necessary to preserve
those bodies. Both proposals remain pending independent disposition. This
observation preserves their existing work accounting as an open removal
obligation; it does not endorse the accounting, accept direct calls generally
or cover every source/type/arity/argument-order/Never obligation of the caller.

## Positive, negative and historical evidence

The reader-only source checker verifies complete forms and exact listed
transformations without evaluating Gravity or loading Bootstrap. It neither
changes production/tests nor adapts DSE's model. The initial fallback comparison
failed because it exposed the additional `body-ae-call-valid?` selector
change. That difference is now explicitly recorded. A later caller-decomposition
check failed because a combined bottom-up substitution changed a parent key
before matching it; applying the two recorded transformations in sequence
passed. Neither source-check failure is relabeled semantic acceptance.

The final record and source checks pass twelve dispatch units, two exact inline
helper bodies, twelve source-file/ref hashes and seven negative integrity
controls. Removing a dispatch or guard record, changing a digest or route,
losing an inline occurrence, or forging accepted/executed status fails closed.
The integrity check also binds all twelve historical units to the original
29-path census. Exact invocations, generator sources and raw outputs remain
local; generator digests are in the record. The published record integrity
reader requires one complete EDN value, ASCII and Bootstrap absence. Docs/roadmap, existing source
mapping and 29-path inventory checks pass; whitespace passes.

PR568's initial CHANGES REQUIRED result, corrected source-only acceptance,
adapted model 7 tests/249 assertions and later preparation 2/38 keep their
original scope. The latest supported owner readback confirms managed recovery
at the same preserved head/tree and an unchanged preparation receipt. Its
recovery receipt is SHA256
`7070b90845d6a0975d2f51e0cdef25f5d5284b2ed1d5fe3497389bbf3d286594`.
The genuine DSE launch denial remains STOPPED; no owner is activated, messaged,
replaced or duplicated here. No selector, compiler, runtime, broker or probe ran.

Routine CI at PR611 `edef2c00` and prior PR569 `9f64bb34` completed with seven
successes and two configured skips each. Those jobs do not execute this mapping
or the held genuine acceptance. The earlier PR569 Maven HTTP429/bootstrap
failure remains preserved with its original head/run/log digest.

## Remaining acceptance and nonclaims

Keep the mapping draft pending coordinator-routed independent disposition of
these exact source relationships, including both guard differences and the
moved-dispatch selector. The denied successor review remains stopped and is not
replayed or routed elsewhere. Existing DSE implementation acceptance and genuine
positive/negative row/fact/source/type/runtime controls remain with PR568.

Compose only accepted dependencies on fresh main, independently review changed
composition, consume applicable results and run integration preflight before
any integration. The original inventory snapshot is unchanged. All other
unmapped units and original positive/negative obligations remain open. Nothing
here closes PR502, changes merged PR532, closes an ACC ID, authenticates source,
or establishes release/self-hosting support.
