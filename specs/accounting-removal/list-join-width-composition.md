# List join width and consumer composition

Owner: ACC-01. This finite child of PR637 at
`c7a4d959d319e0e1bbaef059e41fc5c059497af8` composes exactly the two
production edits from PR611 at
`edef2c00c103df4b5222cf44124c6e1d707820e4`. Both source branches and their
original evidence remain preserved.

PR637 restores original width-1025 List join consumer controls, but its PR595
parent still refuses positional List element types above 1024 in Effects and
lowering. Remove that width-only condition from `body-ij-element` and
`p15-s23-ij-element`. Both complete definitions then match historical PR502.
Keep vector representation, finite/homogeneous shape, atomic element legality,
all-member equality, exact checked-member counts, diagnostics and native
validation unchanged. C7, C8, C14, L2 and D9 govern these obligations.

`list-join-width-composition.edn` binds the parent and donor identities,
complete file replacement equations, historical definition identities and
unchanged direct callers. Every PR637 test and evidence byte is preserved.
Twelve inert source mutations detect restoration of the ceiling, weakened
representation checks, wrong operands and wrong shape predicates. They are
source-checker controls, not compiler execution.

Local source checks pass: 1,538 comparisons and twelve inert refusals.
The language boundary passes six tests and eleven assertions; documentation
validation passes 240 documents and nineteen phase indexes; roadmap and
whitespace checks pass. Pinned clj-kondo 2025.10.23 with cache disabled reports
the exact parent finding multiset for each complete Gravity file: Effects has
76 errors and 44 warnings; lowering has 85 errors and 34 warnings. These are
standalone unresolved/context findings, not a clean lint result. The new EDN
record has zero findings. No finding is suppressed or changed by this child.

Independent source review, parent acceptance and current-main composition
remain pending. Genuine compiler, Effects, lowering and runtime execution is
UNRUN. Work parameters, charges, refusal branches and historical outer-work
assertions still need their own dispositions. These edits do not establish
whole-context wide-input support or accept the original accounting assertions.
No PR502 closure, milestone, release or self-hosting credit follows.
