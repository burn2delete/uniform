# PR512 current C3 source port

## Purpose and immutable scope

This port removes the C3 graph and container accounting ceilings while
preserving shape, arity, reference order, uniqueness, Unicode, metadata,
provenance, source identity, native-class, callback, cycle, and tamper checks.
The two Gravity source files are byte-identical to original PR512.

| Item | Commit | Tree |
| --- | --- | --- |
| Original PR512 | `5b5f0a05efb1453b479d6ab967e42fd30b0663a6` | `0c9b0fa57d98d0d7f9554e5a6cfd06bb91ee1f82` |
| Original patch parent | `d20c9adb648bf26e3d648e296683fe0203271486` | `ff33b028389fa8f7ab9c066dff73c04dfaff4a3c` |
| Explicit current-owner handoff | `45bcf5d3e24e5e6e7cb2254e2be8504883ef7891` | `3013d6cd6e7964c37b718c0ef138ea5e483b9ee9` |
| Gravity source port | `ddd19ef21a7e63d0ddb06206c84aa021149901dd` | `45347de18794dd6da3b961e53f1436f7bcc8848e` |
| Core/C11 affected-consumer repair | `9a04a7afb3fee39a716e3dc5ea375b6a00a4a556` | `34802c4eff7c01dd09fc3ea3f6f81f09011331e1` |
| Actual current main | `43dbdd419c643df42454db42c2cf1b75dea0e292` | `d48baf6d2dfc99ae94f5438a7e6f729d8b8db7b8` |
| Composed source candidate | `b37bcc6262852ef1ebf797fa6cd13136dcfd9c06` | `9d481cddad407a5e3c0526eeb42c5ef67523767f` |

Original ancestry is preserved through the ancestry-only merge
`fe685be1ccadd23b5426969ee8fcdd6c594ca2bf`. The clean named checkout is
`/Users/matt/.codex/worktrees/review-pr-535-port/uniform`, branch
`codex/review-513-current`.

## Explicit development handoff

`gravity.syntax` accepts an explicit current-source selection. It validates
the owned path and strict source snapshot, builds current program identities,
and rechecks the source before and after execution. SH03/C3 and resolution
construction carry the selected binding and authenticity joins. The route is
marked `:development-unadmitted`. Existing calls without this selection retain
the frozen-source admission checks and their stale-source refusals.

An affected integration check with PR453 initially stopped with `C3-ID`:
expected Syntax source bytes `81241`, observed `78739`. The v2 Core route had
omitted its validated Syntax selection. The four-file repair carries the
owner-produced invocation through current Reader selection, fresh C6
construction, private Core ingress, and C2/C3 front-end products. Legacy
arities are preserved. The genuine Core-to-C11 handoff then passed.

The merge of actual main preserved all thirteen PR512 source/test blobs from
that repaired candidate, including the current handoff. It also retained
main's Core/C11 alias cleanups and CI changes. The source evidence is reused
with this explicit conservation; no repeated execution is implied.

## Source identities

| Source | Bytes | SHA-256 |
| --- | --- | --- |
| `bootstrap/gravity/src/gravity/bootstrap/syntax.gravity` | 78739 | `327763641df1c00ceb3d2838e6419a7af77b8bae5c85f5ccdd02200583874184` |
| `bootstrap/gravity/src/gravity/compiler/c3_syntax_object_model.gravity` | 4062 | `31241b6fafa5ff7ab7eefad272397b6141d9c1166cb12c3529bf6803147cb585` |

These are development source observations. No canonical admission hash was
renewed.

## Validation

- Syntax pipeline: 29 tests / 332 assertions passed.
- SH04 adapter: 22 tests / 1014 assertions passed.
- Latest Reader composition, fresh current C3: 1 test / 13 assertions passed.
- Latest Reader composition, stale pinned/default source refusal: 1 test /
  7 assertions passed.
- Genuine combined PR453 Core/C11 handoff after the consumer repair: 10 tests /
  52 assertions, zero failures and errors. The failed pre-repair attempt is
  retained in `/tmp/uniform-pr512-pr453-current-source-handoff.log`.
- Pinned clj-kondo 2025.10.23 against actual main43: zero inherited warnings
  and zero blocking diagnostics.
- Language boundary: 6 tests / 11 assertions passed.
- Docs: 240 documents / 19 phase indexes passed. Roadmap and governance
  validators passed. `git diff --check` passed.

## Post-composition current-source caller repair

The first hosted validation of the composed candidate was red and is retained
as a defect receipt, not treated as a stale check. Run `41498207973` reported
the following affected jobs:

- Resolution pipeline job `125458841658`: 42 tests / 2144 assertions, 2
  failures and 4 errors. The current-cohort fixture reached C3 with expected
  Syntax bytes `81241` and observed current bytes `78739` because its
  current Reader/Macro invocation omitted the validated Syntax selection.
- SH04 fresh-C2 job `125458841740`: 63 tests / 609 assertions, 2 failures and
  1 error. Its current construction and standalone frozen-entry fixtures had
  the same missing handoff; the standalone entry also exposed the correct
  earlier pinned C3 refusal for a current C2.
- Macro pipeline job `125458841884`: 27 tests / 148 assertions, 12 errors in
  paired expansion, source-mode, and current receipt fixtures for the same
  omitted Syntax selection.
- SH06 unmetered Resolution job `125458841639`: 9 tests / 58 assertions,
  1 error in the actual current-source artifact fixture.

The scoped repair keeps Syntax selection explicit and independent. Resolution,
SH06, SH08's C8 fixture helper, Macro paired fixtures, and SH04 fresh-C2
construction now carry the validated
`:gravity/sh04-development-syntax-selection-v1` binding for their intended
current-source routes. The Resolution Macro-isolation case selects Reader and
Syntax but omits Macro, so it still fails closed at C4 with
`:exact-pinned-macro-source`; a separate Reader-only case omits both selections
and fails closed at C3 with `:exact-pinned-syntax-source`. The SH04 standalone
two-argument public entry likewise records the early frozen C3 refusal when
given an explicitly current C2. No default/pinned call was converted to
current mode and no admission hash was refreshed.

On the repaired local candidate, the affected JVM receipts are:

- `gravity.resolution.pipeline-test`: 43 tests / 2203 assertions passed.
- `gravity.self-hosting.sh06-unmetered-resolution-test`: 9 tests / 78
  assertions passed.
- `gravity.self-hosting.sh04-fresh-c2-construction-test`: 5 tests / 30
  assertions passed.
- `gravity.macro.pipeline-test`: 22 tests / 307 assertions passed.

These are focused development checks, not hosted or canonical admission
authority. The original hosted red result, the exact current-source byte
comparison, and the pinned/default refusal cases remain part of the record.

## Remaining work and nonclaims

The Reader revision byte ceiling `1048576` remains in this source and is the
separate PR566 delta. The structural projection control does not prove
acceptance above that ceiling. PR544's additional selected-interface controls
and PR580's selected-revision/process evidence remain separately reviewable.

This receipt grants the finite C3 development evidence above. It grants no
canonical admission, C12, large-Wasm, native-C, migration, release,
self-hosting, or seed-retirement authority. Integration requires independent
acceptance of the exact candidate and an immediate clean integration preflight.

## SH07 construction handoff for explicit current Syntax

The post-composition caller audit found one remaining owner-boundary omission.
SH09's current Reader/Macro fixture and the SH07 current Map-guard fixture now
carry the validated `:gravity/sh04-development-syntax-selection-v1` selection.
The SH07 construction guard correspondingly reads the selected current Syntax
bytes and binding when that selection is present, and keeps the historical
pinned Syntax read and binding when it is absent. The Macro mode remains a
separate authenticated assertion; the current Map-guard fixture explicitly
selects current Reader, Syntax, and Macro because its source route is current.
No default call was silently converted, and no admission pin or hash was
refreshed.

The first attempt with current Syntax and the old pinned Macro default failed
closed at C4 with `:exact-pinned-macro-source`; this was retained as the
incompatible mixed-source observation. After the owner fix and explicit full
current selection, the real Map-guard-to-Core branch replay passed 1 test / 131
assertions, including hostile branch replay rejection. The independent SH07
Macro-mode/source controls passed 2 tests / 9 assertions. The SH09 private
Map-key paired current-source test was started once, but reached a host JVM
resource boundary at about 10 minutes and 3.66 GB RSS before a terminal result;
that run is not claimed as a pass and was stopped without retry.

The source owner change is intentionally narrow: no native-C, C11, PMK
admission, or migration evidence is inferred from these JVM observations.
