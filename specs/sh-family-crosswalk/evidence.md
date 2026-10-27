# SH crosswalk evidence and finite gates

## Observed source population

The source extractor found 180 unique criteria, SH-00.1 through SH-29.6, and
30 milestone sections; each milestone has six criteria. SH07 has 18 original
sequence rows. All 24 component task authorities are read inside explicit
implementation markers; grouped literal IDs remain unchanged. No source,
compiler implementation, existing task checkbox, global manifest or dashboard
status was modified. The owner-approved new SH01 task is unchecked/Open.

The current milestone-plan states are source records, not a new acceptance:
seven historical complete, eleven partial, eight queued, four blocked. The
first 42 criteria retain the existing audit's original commits and per-row
scope; source-reported S/P classifications do not imply current whole-closure
acceptance. SH-05.1, SH-05.6 and SH-06.6 retain their current-cohort gaps.
The accepted current Reader fresh-Syntax task remains scoped to its actual
record and receipt, with other Reader/Syntax/Core outcomes open.

## Concrete gaps requiring owner integration or review

1. SH-01 ownership, dedicated test routing, delegated compatibility and strict
   selection have historical accepted implementation. They currently lack an
   appropriate bounded marked Spec Kit task in the original baseline. The
   canonical owner confirmed this task-authority gap. The component owner then
   explicitly handed over exact paths. Merged PR489 adds one named
   development-boundaries record with six acceptance facets, preserving E01
   historical completion. The original three API records and historical suffix
   remain unchanged. No missing implementation or new acceptance is inferred.
2. SH07-C01 must establish the actual required source/import/generated-form and
   primitive population now. Existing Core tasks own semantics; C17/C18 retain
   distinct migration/completion acceptance facets. Six selected sources do not
   replace the population, and later SH23-SH25 completion is not a prerequisite.
   C12's restricted operation stays with its original owner and restrictions.
3. SH-26.2-.4 need a real process-invoking prior-stage to full-next-stage build,
   link/package and compiler execution. The existing Release actual-execution
   task is accountable; merged PR489 clarifies this executor in its existing
   acceptance criterion, preserving ID/status/public obligations and the four
   other Release records. Supplied-record validators do not satisfy the actual
   execution requirement. This is task-boundary integration, not implementation.
4. Native/provider and SH21-SH25 source-closure/build/CLI composition require
   actual execution evidence. SH26-SH29 require real rebuild, independent
   equivalence, trust/TCB/provenance and delivered public seedless behavior.
   Historical P15/P18 and native ABI subfacts survive at their original narrow
   scope; they are not all-current acceptance, full self-hosting or seed retirement.
5. D1 versus C1 Core/Resolve ordering remains qualified at the exact documented
   boundary. The operational C6 consumer requires C5 bindings; no completed Core
   prerequisite is imposed on resolution. Architecture-owner confirmation is
   still required for any source-level clarification of the two pipeline labels.

## Actual checks

- `clojure -J-Xmx256m -M tools/sh_inventory.clj --check`: source population
  extraction/check is included in the crosswalk verification.
- `clojure -J-Xmx256m -M tools/sh_crosswalk.clj --check`: passed for the initial
  generated projection. All 180 source routes and 18 sequence rows are present.
- `clojure -J-Xmx256m -M tools/sh_crosswalk_test.clj`: 3 tests / 22 assertions,
  zero failures/errors. Missing/malformed/duplicate source populations reject;
  UTF-8 offsets and real deterministic rollup/source/gate boundaries are checked.
- `clojure -J-Xmx256m -M tools/acceptance_dependencies_test.clj`: focused
  extension passed 13 tests / 392 assertions, zero failures/errors. The first
  run caught an incorrect false-condition predicate (two errors); it was fixed
  to explicit membership and the final run passed. This is source/graph checking,
  not compiler, migration or release acceptance.
- API task projection checks passed 5 tests / 74 assertions after correcting
  stale three-record introductions found by independent review. Release task
  projection checks passed 5 tests / 78 assertions. Both generated projections
  validate. The task amendment `796c0e3c` passed independent source review and
  current-main composition `96816fba` passed separate review. PR489 merged at
  `254a5b50`; fresh remote-main and all ten reviewed file blobs were verified.
- Document and full-language roadmap validators passed; language-boundary check
  passed 6 tests / 11 assertions, zero skipped namespaces. No compiler proof,
  native execution, authoritative migration or release run was performed.
- All 24 component projection `--check` commands passed. The new crosswalk
  Markdown and dependency documentation passed 319 local link/anchor checks.
- The P15 final-seed-retirement proof has status incomplete, full-selfhost false,
  seed-retired false and seed-boundary true. Its distinct retained whole-language
  gate artifact has contradictory complete/true/true/false flags. Both exact
  artifact hashes and parsed top-level values are retained in crosswalk.edn;
  neither the inconsistent whole-gate flags nor older completion prose grants
  current SH26-SH29 acceptance. No artifact was regenerated.

## Remaining finite publication gates

1. Full 180-route semantic/nonoverlap review plus all 18 SH07 sequence joins,
   historical/current evidence separation, dependency conditions and frontier.
2. Exact graph/source/links/projection checks on the final reviewed candidate,
   followed by normal publication/integration. Draft work earns no closure.
3. Accepted graph-input handoff to the existing dashboard collector after
   integration; no direct store/Site write or duplicate graph is introduced.

Exact final checks, source hashes, immutable review and publication receipts
will be added only when those operations have actually completed. Owner and
independent semantic reviews are pending; all mappings remain marked pending.
No accepted dashboard input has been handed off yet.
