# List join element width disposition

Planning: ACC-01/06/08/12. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. Standalone assessed base:
`e9ec1c599614a99e3779b5e3a329e8d9a4b8fa80`. Historical PR502 head:
`6a4f87f013d0076b81aba9e0667650e70d28e635`. This draft does not change
historical PR502 or merged PR532.

## Source and contracts

[L5](../../docs/phase-01-core-language/015-l5-type-system-specification.md)
requires independently established positional members and a common atomic
List element type. Empty positional Lists supply no element type. Actual branch
types and source-bound compatibility remain necessary; an expected declaration
cannot supply missing evidence. [C8](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md)
and [C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md)
preserve those source/type relationships at their consuming stages. The current
[accounting removal rule](../../docs/development-verification.md#remove-project-cost-and-budget-mechanisms)
removes input-width quotas without independent semantic justification.

Remove only `(<= width 1024)` and its enclosing conditional from
`body-ij-element` and `p15-s23-ij-element`. Both complete resulting definitions
match the preserved historical definitions exactly. Each still requires the
exact three-key literal List schema, a Vector of member types, an established
atomic first member and agreement of every remaining member. Empty and
homogeneous List behavior and `:checked-member-count` are unchanged.

All other production definitions, including the two direct `*-ij-type-work`
callers, are unchanged. Their work parameters, cumulative charges, refusals and
work-count results remain open accounting obligations. This patch neither
increases a constant nor replaces the removed width with another bound. It
cannot establish whole-path wide-input support while those mechanisms remain.

The complete two-owner function bodies correspond under the explicit atomic
and homogeneous-type helper names in [the census](list-join-width-census.edn).
That observation does not infer helper semantic equivalence. C7's separate
unmetered branch-join successor is PR595; this standalone candidate does not
compose, accept or replace its source. No API, schema or diagnostic identifier
is renamed here. Existing numbered names remain compatibility-bearing legacy
names, not a new naming scheme.

## Positive and negative obligations

Two appended test definitions call the actual Gravity functions through their
existing owner harnesses:

- `list-join-element-width-development-kernel` in SH09 calls `body-ij-element`.
- `lowering-list-join-element-width-development-kernel` in the stage2 runtime
  test file calls `p15-s23-ij-element` without executing a generated application.

Both require accepted positional Lists of widths 0, 1, 1024, 1025 and 2049,
with the exact relation, established element type and checked-member count.
For widths 1025 and 2049, a differing atomic type, unknown type, nil or nested
unestablished List in the last position must remain not applicable with
`:incompatible-list-elements`. Additional controls retain exact schema keys,
Vector member representation, literal shape, independently valid atomic type,
empty and homogeneous identity semantics. The existing harness allowance is
unchanged; tests add no work-count or exact-payment assertion.

Every prior byte of both test files remains unchanged. The new controls are
actual-owner definitions, not an adapted host model or proof that a selector
ran. Existing positive, negative, source, Never, ledger and runtime controls
remain available with their original scope and pending status.

## Observed validation

The pinned local Clojure source checker (`-J-Xmx512m -M`) passed complete
historical-form comparison, exact expression substitution, preservation of all
other production forms, unchanged direct caller hashes, exact existing-test
prefix bytes, one appended test per file and positive/negative control census.
Reader evaluation is disabled and Bootstrap is unloaded. The source script's
SHA256 and complete source/test pins are in the census; its implementation,
exact invocation and raw output remain local.

The first comparison attempt failed on normalization of reader-generated
symbols in an unchanged pre-existing test. The corrected checker uses exact
prefix-byte conservation plus complete form counts, and passed. This source
checker failure is not a failed or passing semantic selector. An initial lint
command could not find its executable on PATH; no lint process ran. The located
existing executable subsequently completed contextual lint with `tools/ci/kondo`:
zero errors, 152 warnings and one information finding, identical to the exact
base's finding multiset across both files.

`clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`
passed 6 tests and 11 assertions. Canonical document and full-language roadmap
validators and whitespace checks pass. None of these checks evaluates Gravity
functions or establishes type/effect/runtime acceptance.

## Remaining finite acceptance

Keep draft pending coordinator-routed independent source/result acceptance.
The denied successor review remains stopped and is not replayed or rerouted.
Genuine compiler, Effects/lowering, runtime, authenticated pipeline, broker and
held selectors remain UNRUN. Required remaining evidence includes:

1. Execute both actual-owner element controls, preserving the late invalid
   cases and exact checked extents, under the applicable execution assessment.
2. Compose with the accepted C7 join source and run actual context, complete
   row/fact reconstruction, source/type/binding tamper and typed lowering
   controls. Direct element checks cannot establish that composition.
3. Preserve actual application/runtime values, exact/Never scheduling and
   nil/List semantics in their existing owner acceptance; no runtime source is
   changed here and no held operation is launched.
4. Consume applicable CI; independently disposition the exact candidate and
   fresh main composition before any integration. Remaining join accounting,
   unrelated original source units and PR502 closure stay open.

No independent acceptance, integration, whole accounting completion, source
input authentication, C12, release or self-hosting authority is claimed.
