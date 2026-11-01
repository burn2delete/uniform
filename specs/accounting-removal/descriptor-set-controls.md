# Preserved bare-Set descriptor controls

This ACC-01 test restoration addresses the missing sixth case in PR502's shared
descriptor table. It depends on PR568 at
`f4c9fdcd5a3ca24be5b8e24a72ad9d908800796a`, tree
`71b343d4cc131ef3be1601c8f8ba12509744b31e`. It changes no PR568 implementation,
existing test, source model, fixture or denied selector.

The owning contracts are [C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[C8](../../docs/phase-06-compiler-architecture/087-c8-effect-checker-design.md),
[C14](../../docs/phase-06-compiler-architecture/093-c14-target-lowering-architecture.md),
and [development verification](../../docs/development-verification.md). Complete
source-bound descriptors, constraint/fact reconstruction, evaluation order and
tamper refusal must survive accounting removal.

## Restored definitions

The original source is PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635`, tree
`f8440ffcf2d2fa7e2f898178ee299ac64ef59d32`. Its
`descriptor_reconstruction_test.clj` contains six cases. The relocated table in
PR568 contains the five ANE/BKA cases, omitting
`[:dse [:gravity.type/set :gravity.type/set]]`. PR568's separate Clojure model
and unrun source selector do not establish execution of this original case.

The restored canonical test file selects only that missing DSE case. Its first
test retains the entire original body: accepted C7 derivation, complete
three-owner descriptor equality, absence of the retired field, complete typed
lowering, and Bool return/result types. The second retains every original
assertion and mutant. Exactly three raw substitutions remove the unused family
stem binding and name the current DSE fact/constraint constructors. Coherent
forgeries rebuild both facts and ledger rows before asking lowering to reject.
The eleven negative mutations cover obsolete/unknown fields, forged source,
wrong profile, missing/duplicate ledger, missing facts, evaluation order,
binding class, stale form parent and unknown operand type.

Test helpers use the existing normalized atomic-equality input and genuine
owner invocation helpers. C7 derivation and both consumer headers use their
current eight-argument APIs; no retired DSE allowance is reintroduced. The
existing outer typed-lowering argument remains unchanged. The unused finite-Set
helper is omitted. The other five cases and private/public Map tests retain
their existing separate dispositions; this is not restoration of the whole
historical file.

[The census](descriptor-set-controls-census.edn) pins the original and restored
files, the three raw test substitutions, five direct dependency files and nine
actual function definitions/argument lists. These are source correspondences,
not transitive helper equivalence or semantic acceptance.

## Validation and remaining work

- Reader-only source checks: original positive body identical; negative body
  identical after the three recorded substitutions; all nine dependencies exist
  at the pinned parent; Bootstrap absent and Gravity never evaluated.
- `clojure -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`:
  passed, 6 tests / 11 assertions.
- Pinned clj-kondo 2025.10.23 with repository configuration, reproducible mode
  and cache disabled: zero errors, warnings or infos on the two Clojure/EDN inputs.
- Canonical document, roadmap and whitespace checks: passed.

Initial candidate `508e9360` static CI failed on a new blank line at EOF,
line 78 (run `40966217048`, job `123771287698`). Changed-file lint had zero
findings. The successor removes exactly one final newline, leaving every reader
form and assertion identical, and refreshes its census hash. Staged and complete
parent-relative whitespace checks now pass. The initial local whitespace command
ran before staging the new file and therefore missed it; that earlier claim is
superseded. No CI policy or lint configuration was changed.

Both restored genuine tests are **UNRUN**. They are definitions for review,
not replacement model evidence or a selector launch. PR568's denied genuine
operation remains stopped. Independent source review, genuine acceptance where
permitted, dependency reconciliation, applicable CI and coordinator integration
remain separate. No whole ACC-01, PR502 closure, main, release or self-hosting
claim follows from this draft.

The new file/namespace use the preserved semantic descriptor-reconstruction
name. Existing SH08/SH09 helpers are legacy dependencies, not newly introduced
implementation names. The untracked lint cache is retained; no cleanup,
exclusion, alternate-checkout preflight or clean-worktree claim is made.
