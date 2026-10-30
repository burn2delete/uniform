# Private Map construction-report accounting removal

This finite ACC-04 supplement starts from `5815e47be5b1e94ca8b88f3e1cb59ba200e1a9b3`,
tree `9c3557467f7abb24bdad6f43f29b30813ec8ccd1`. The earlier source-census
review explicitly retained construction accounting. This change removes that
remaining fixed report/debit; it does not repeat PR534, fact-census or
source-census removals.

## Classification and transaction

The private-Map source builders published a fixed `:construction-work 1`.
It neither counted Map members nor validated the output List. Headers charged
that one unit and attached a `:construction-work-basis` label. The three private
constraint constructors echoed the field, and Effects allocation reporting
echoed both fields. Proof/evidence/lowering consume the aggregate header report;
they contain no other direct debit of this construction field.

Remove the local binding, descriptor field, accounting invalidation, header
debit/basis and constraint/allocation report fields. No zero/nil sentinel,
replacement bound or raised constant is introduced. Headers retain their
existing three-unit result charge and proof report; all other pools stay open.
Their aggregate evidence/lowering callers therefore receive exactly one unit
less charged work through the existing interface. The production transaction
is ten functions: three each of `pmk-source`, `pmk-header`, `pmk-constraint`,
plus `body-pmk-allocation`.

## Semantic conservation

[STD3](../../docs/phase-16-standard-library/213-std3-collections-library-specification.md)
requires complete entry-specific whole-key-domain proof, all-value validation,
ordinary argument evaluation and an unrefined homogeneous Keyword List result.
C7/C8/C14 require source, binding, profile, target, effect and reconstructed
descriptor/fact identity. Current AGENTS.md removes artificial accounting.

All member/domain/normal-witness checks, call/edge/owner/binding validation,
result type, each-key selection, effect/capability facts, allocation/storage
policy and semantic coverage extents remain. The allocation report retains its
unknown extent, normal-completion condition and complete key-domain proof.
Deleting a cost field does not infer an extent, cardinality or allocation bound.
Public Map `keys`, ANE, BKA, DSE, runtime and held operations are outside scope.

[Conservation evidence](private-map-construction-conservation.edn) compares
all three production files to the exact parent. Bytes outside the ten selected
functions are identical; each selected body equals the exact cost-field/debit
projection. Proof, evidence and lowering bodies are unchanged. Old immutable
selector/source packets remain historical and stale; no pin is refreshed.

## Controls and remaining gates

Compiler-free source/cycle/fact controls pass 11 tests / 4,907 assertions with
Bootstrap unloaded. The source model now has five tests / 185 assertions.
They check the real header allowance/report handoff, three-owner descriptor
parity, stale nil/zero/one constraint rows, zero/one/basis and opaque descriptor
cost-field rejection with zero callbacks,
and exact semantic List/allocation facts. Earlier wide-input, late-invalid and
source-corruption controls remain. Native type and complete entry proof are
explicit seams; these are not authentic compiler requests. One test reader
error was corrected before the passing result; no failed result is promoted.

The genuine SH08 controls receive absence assertions and coherent stale-field
negative definitions only; they remain UNRUN. Independent affected review
returned finite source-only PASS for `31183a4814dc991e33d095874edd5d3645838450`,
tree `daed76363cd9bfc87c063d32bf20461287905a12`, with no actionable defect.
The reviewer reproduced all 11 tests / 4,907 assertions, verified the conservation
digest and source/consumer scope, and passed whitespace checks. Boundary/lint
results remain implementation-owner observations.
[Review consumption](private-map-construction-review-consumption.edn) records
the exact input digests and completed independent reviewer turn; no duplicate
review or new assessment is routed.
Result/proof/physical/parameter/map-source/entry/runtime pools,
actual compiler/native/runtime composition and main integration remain open.
The original genuine-selector goal stays BLOCKED. NEW ACC09 v4 delivery, PR531
merge and all denied/unknown operations remain held and are not retried or
routed through another mode.

The later [physical-input supplement](private-map-physical.md) owns physical
allowance/report removal and its required first-consumer interface changes.
It does not expand this historical construction review's authority.
