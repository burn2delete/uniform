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

## Remaining work and nonclaims

The Reader revision byte ceiling `1048576` remains in this source and is the
separate PR566 delta. The structural projection control does not prove
acceptance above that ceiling. PR544's additional selected-interface controls
and PR580's selected-revision/process evidence remain separately reviewable.

This receipt grants the finite C3 development evidence above. It grants no
canonical admission, C12, large-Wasm, native-C, migration, release,
self-hosting, or seed-retirement authority. Integration requires independent
acceptance of the exact candidate and an immediate clean integration preflight.
