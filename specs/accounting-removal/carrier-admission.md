# Native carrier admission successor

Planning: ACC-01/06/07/08/12/13. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. Parent: PR599 at
`b786b464a67e33af2b75bfcf90a87619b71774c3`, tree
`b2fd8edc5444f46c588a5db66e6881b72b3f1e33`. This stacked source increment
reuses the native-DATA dependencies introduced in PR591 and retained by its
successors. Parent and candidate acceptance remain separate and pending.

## Contract and production changes

The current [accounting removal rule](../../docs/development-verification.md#remove-project-cost-and-budget-mechanisms)
removes node, depth, width and scalar quotas whose purpose is refusing otherwise
valid DATA. [C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md),
[D8](../../docs/phase-00-foundation-and-thesis/009-d8-safety-philosophy-and-charter.md)
and [D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
retain native class/metadata/callback rules, authentic inputs, complete type/source
validation and evidence appropriate to each claim.

Restore the complete historical `sh08-carrier-preflight` definition from PR502
head `6a4f87f013d0076b81aba9e0667650e70d28e635`. It delegates to the existing
`sh08-vf-original-data` traversal, which validates native headers before
collection operations, rejects unsupported carriers, metadata, invalid symbol
names, foreign comparators, malformed List tails, zero-denominator ratios and
cycles, and preserves shared acyclic subgraphs as separate occurrences.
The five native dependency forms remain pinned and unchanged from both
historical source and parent. Their runtime/provider acceptance is still pending.

The obsolete preflight's node/depth/width/scalar accounting, scalar rendering
and aggregate flattening are removed. Four helpers used only by that preflight
are deleted: `sh08-aggregate?`, `sh08-aggregate-width`,
`sh08-aggregate-components` and `sh08-max`. Each is also absent in historical
PR502. Their four entries in the current source-coverage expectation are removed;
all other bytes of that file are unchanged.

Three orphan policy fields are removed from `sh08-local-bounds-value`:
`:maximum-carrier-nodes`, `:maximum-carrier-width` and `:maximum-scalar-units`.
The retained `:maximum-core-nodes`, `:maximum-node-children` and
`:maximum-carrier-depth` still have other callers. They remain open accounting
obligations, not endorsed semantic bounds. In particular, structural-shape depth
refusals remain outside this finite global carrier increment. No constant is
raised and no unbounded sentinel or zero-work telemetry replaces a quota.

The imported-result wrapper now uses the complete historical definition,
including `:imported-type-result-carrier-invalid` instead of the obsolete
carrier-limit reason. The imported-producer wrapper preserves its current
C7-VERIFY diagnostic structure and original failure data but reports
`:module-type-producer-core-invalid` instead of core-bound. That wrapper is not
claimed identical to historical PR502; the diagnostic change is explicit.
All other production forms remain unchanged.

[The source census](carrier-admission-census.edn) pins all eleven direct callers:
the two imported wrappers plus primitive core-shape admission, Map association,
present Map lookup, Map merge, parameter-entry evidence, structural shape/check,
Vector append and Vector projection closure. Their set and call arities are
unchanged. Only the two listed caller diagnostics change. The census does not
claim that these whole callers, their remaining work/shape accounting or their
transitive public/authenticated paths are accepted.

## Positive and negative obligations

Two new actual-C7 kernel definitions call the global carrier preflight:

- `carrier-native-safety-development-kernel` preserves the complete existing
  private-verifier cycle/callback safety body under the one named entrypoint
  substitution. Opaque objects, foreign map implementations, nested invalid
  carriers, lazy tails, metadata, invalid symbols, malformed native maps and
  cyclic backing arrays retain rejection and zero-callback obligations.
- `carrier-native-extents-development-kernel` requires accepted DATA at depth
  192, width 65537, 1049601 traversal occurrences and scalar length 16777217,
  plus repeated shared acyclic data. Those numbers are historical regression
  inputs, not a new policy. Native unsupported leaves occur after equally wide,
  deep and repeated valid siblings. Indexed traversal pushes children in order
  and visits index zero last; the late-invalid fixtures follow that actual order.
  Default sorted Maps remain accepted; zero-denominator ratios, subvectors and
  foreign-comparator sorted Maps reject without invoking their callbacks.

Existing imported-module validation now accepts oversized native DATA at the
carrier boundary while retaining the independent malformed-producer rejection.
Its historical scalar test size is explicit, rather than read from a retired
policy field. Existing normalized import routing replaces a quota-only deep
value with actual forbidden metadata, retaining diagnostic/memo behavior and
ensuring producer/inference calls do not occur after native-carrier rejection.
The authentication-failure branch and all other routing assertions remain.
The existing policy-key assertion removes exactly the three retired fields;
all other shape/extent controls remain.

The census lists each existing-test byte substitution and verifies that every
other prior test byte is preserved. No expectation, number, argument list,
schema field or diagnostic is silently normalized into historical equality.
The new native tests are present but UNRUN; constructing test definitions is not
execution, callback evidence or source authentication.

## Observed checks and remaining acceptance

The pinned local Clojure source checker passed complete historical preflight and
imported-result wrapper equality, four historical helper absences, three policy
field deletions, five unchanged native dependencies, all eleven direct caller
pins, exact diagnostic-only caller edits, existing-test byte conservation and
the two appended native kernels. Bootstrap remains unloaded and reader evaluation
is disabled. Its first attempt failed on a local checker syntax error before
producing the census; the corrected checker passed. Exact invocations, scripts
and raw outputs remain local; the generator digest is in the census.

Contextual clj-kondo with `tools/ci/kondo`, cache disabled, reports zero errors
and the same 72 warnings as the exact parent across both changed Clojure files,
including the per-file finding multisets. The language boundary gate passes
6 tests/11 assertions. Canonical docs, full-language roadmap and whitespace
checks pass. The earlier rejected cache cleanup is not retried or routed through
another tool; the previously generated cache remains local and untracked.
No clean-worktree or integration claim follows from these checks.

Keep draft pending coordinator-routed independent source/result acceptance.
The denied successor review remains stopped, unreplayed and unrouted. Genuine
compiler, public/authenticated C7, Effects/emitter, runtime, broker and held
selectors remain UNRUN. Finite remaining evidence includes:

1. Execute the two native carrier kernels and affected imported/shape controls
   with the actual Native provider, preserving late-invalid, callback, cycle,
   metadata, class and positive extent results under the applicable assessment.
2. Revalidate affected real producer/consumer paths through the eleven callers,
   preserving source provenance, authentic inputs, full type/constraint/fact
   reconstruction, Unicode, independent semantic diagnostics and tamper controls.
   Carrier DATA admission alone accepts no source/Core/type artifact.
3. Exercise the updated source-coverage expectation and genuine affected
   source/Core/constructor/verifier composition. PR599's existing fixtures and
   fresh-verifier controls remain required; this source change invalidates any
   assumption that the earlier carrier-limit behavior is still current here.
4. Consume applicable CI, independently review the exact composed candidate
   against fresh main, and integrate only accepted identities through the
   governing preflight. Parent drafts and standalone PR610/611 acceptance are
   not implied or composed by this increment.

Remaining primitive/core/structural/inference accounting and all other original
source/test deltas remain open. No whole accounting completion, public compiler
or authentication support, release, self-hosting, PR502 closure or expanded
PR532 acceptance is claimed.
