# Unused loop accounting helper retirement

Planning: ACC-01/07/12. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. Parent: PR615 at
`bb3a9acfbb5dd683d6df0403867a179a5fdc51bc`, tree
`4c78e092a77a7dac53732d5bf0439d5fdf964493`. This finite source increment
retires two unused accounting helpers already absent from historical PR502
`6a4f87f013d0076b81aba9e0667650e70d28e635`.

## Contract and exact source delta

The current [accounting removal rule](../../docs/development-verification.md#remove-project-cost-and-budget-mechanisms)
requires deleting unused work/debit/refusal mechanisms rather than preserving
or renaming their allowances. [C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
and [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
retain active type/fact/ledger validation and evidence appropriate to its scope.

Delete only `sh08-lsi-row-index` and `sh08-lsi-target-rows`, a 29-line change.
Their final absence matches the original historical source. The former is
referenced only by its own definition and the latter helper; the latter is
referenced only by its own definition. Neither is exported or named in current
repository tests. The scanner checks tracked bootstrap, tools, workflow,
agent/Spec Kit and dependency files; it records those exact paths in
[the census](loop-accounting-cleanup-census.edn). After deletion no reference
remains within that source/tooling scope.

The namespace and export form are unchanged. Every other production definition
is unchanged, including all 30 remaining `sh08-lsi-*` functions. The census pins
each complete remaining loop-family definition, both removed definitions and
the historical absences. This deletion does not remove or accept any validation
on the current loop path. Active loop invariants, row reconstruction, kinds,
identity, duplicate/source/type checks and diagnostics remain in their existing
unchanged functions with their existing acceptance obligations.

No test, public API, descriptor, language rule, runtime, Native adapter or
current caller is rewritten. No replacement accounting, zero-cost sentinel or
larger limit is introduced. The removed bodies and their original semantic and
accounting content remain available at the pinned parent and historical source;
no historical evidence or PR502 content is rewritten.

## Observed checks and limits

The pinned local Clojure checker passed exact definition-set subtraction,
unchanged namespace/exports and every remaining definition, historical absence,
zero external source/test references, whole remaining loop-family hashes and
one changed bootstrap path. It reads forms with reader evaluation disabled;
Bootstrap is unloaded. Its generator digest and complete source/form pins are
in the census; exact invocation and raw output remain local. No helper or
Gravity function is evaluated by these checks.

The direct language boundary gate passes 6 tests/11 assertions. Canonical docs,
full-language roadmap and whitespace checks pass. No Clojure test/tooling code
changes, so no changed-Clojure lint or new semantic test is needed for this
unused-helper deletion. The existing generated lint cache remains local and
untracked; its earlier denied cleanup is not retried. No clean-worktree or
integration claim follows from the source checks.

Keep draft pending coordinator-routed independent source disposition and fresh
main composition. No stopped review is replayed or rerouted. Compiler/runtime,
Native, authenticated source, Effects/emitter, broker and all held selectors
remain UNRUN. Remaining finite acceptance is:

1. Independently inspect the exact two historical absences, namespace/export
   conservation, source reference scope and unchanged active loop functions.
2. Consume applicable CI and revalidate references and namespace/source coverage
   against fresh composed main before integration. If composition introduces a
   real caller, reconcile its requirement explicitly instead of inferring that
   this old no-caller observation still holds.
3. Preserve existing positive/negative loop semantics and genuine acceptance
   obligations. No current loop compiler result, live accounting removal,
   performance, public authentication or whole-callgraph proof is claimed.

All other original source/test deltas and live loop/inference accounting remain
open. No independent acceptance, integration, release, self-hosting, PR502 closure
or expanded PR532 acceptance is claimed.
