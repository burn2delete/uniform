# Reader-to-Core invocation pilot evidence

Status: planning and focused routing correction complete; public acceptance open.
Date: 2026-10-25.

## Public input revisions

- Planning baseline: main `77d6e29ee780c6329acf80ae6d6563c05be21cc1`.
- Reader #369: `10a1abc59a33021572e220fee4bb5f74371155f6`.
- Resolver #360: `8820cfdc676a374007cb0efde04bf618feff263e`.
- Core #362: `a70c14258090aa8309cac84fa53cf2e3ef47b4cf`.
- Native #372: `385e82fb989f707ec1779e5da965c629031e43ef`.
- Authenticated envelope #366: `d1b90eb0c5725cecbf2bc817a4a939f037a83d5b`.

Core already includes the published Reader, authenticated-envelope and Resolver
histories. Those ancestry relationships do not establish joined acceptance.
The joined tree includes Native and the recorded accepted main baseline.

## Newly observed focused implementation and Resolver results

The composed implementation tree is `fde2db9adc09877610910d8a5b5296e2fc106a17`.
It is published as [draft #384](https://github.com/burn2delete/uniform/pull/384),
head `a3305b00a4584ff83bb5a9a55a08684108993543`; fetch, remote head and exact
tree comparison were independently checked.
The new correction authenticates Reader invocation source bytes first, then
compares canonical paths so equivalent relative/absolute paths do not reject.
Missing, foreign and invalid paths still fail closed. The routing regression
test explicitly uses a mocked product boundary and claims no artifact acceptance.

- Focused invocation routing plus language boundary: 7 tests, 30 assertions,
  no failures or errors. LOC validation passed: 2,026 source files, maximum 250
  production Clojure lines, no exceptions. `git diff --check` passed.
- Independent code review accepted the narrow two-file correction. This is not
  independent acceptance of the remaining public pipeline.
- Actual Resolver construction accepted the unchanged
  `sh-06/current-cohort-unmetered.gravity` fixture, with complete embedded proof,
  schema-2 descriptor and development-unadmitted AE source. Artifact identity:
  `sha256:291746d8304a9e21a92e3fbd16fd269dae0bc607a11570e8feb0edece7837791`.
- Fresh Resolver verification passed with no failed checks. Serialization read
  back exactly, and fresh deserialization returned the identical artifact.
  Serialized size was 2,471,634 characters.
- These were separate actual phases under the unchanged 120-second host
  watchdog and 2 GiB heap: construction 88.60 s, verification 23.24 s,
  serialization 25.80 s, deserialization 22.31 s, measured after namespace load.
  The earlier combined test timed out; it is not reported as an aggregate pass.

The constructor used `compiler-c5-resolution-source-artifact` with actual source
text, empty schema-1 module inputs, the canonical current Reader path, and the
host-owned context below. Verification used the same context with the legacy
boundary mode. Serialize/deserialize also received this context explicitly.

```clojure
{:reader-source-path b/sh03-reader-source-relative-path
 :authenticated-envelope-options {:source :development-current}}
```

No stored report or artifact field supplied authority to a later phase.
Resolver rejection checks and paired public Core acceptance are still open.

## Attributed prior results

The Reader draft reports 2 public context/source-lifetime tests with 25
assertions, a broader 56 tests with 2,007 assertions, and separate LOC/docs/
roadmap checks. These do not establish full public Core/Resolver composition.
An earlier real constructor succeeded, but its aggregate had 11 passes and
1 harness-envelope lookup failure. It is not a full pass. None of these prior
results is relabeled as validation of the current joined tree.

## Planning checks observed for this increment

- All four feature files are ASCII.
- All 10 relative Markdown links resolve.
- `git diff --check` passed.
- No compiler implementation or semantic test changed in this docs-only slice.

## Pilot method and unfinished acceptance

The merged `speckit-specify`, `speckit-plan`, `speckit-tasks` and
`speckit-analyze` instructions were read and followed manually for this selected
feature. No external Specify CLI, shell helper, automation, literal chat-command
parsing or skill autodiscovery is claimed. Existing scaffold/adoption checks
remain historical and separate.

The spec -> plan -> tasks artifacts now track a real focused implementation.
The next acceptance step is actual paired public accepted/rejected checks and
independent review of their complete evidence. The end-to-end pilot remains
incomplete until that evidence exists. Migration, component release, self-hosting,
seed retirement and performance are not claimed.

At this update's preparation time, the initial four planning files and the
implementation draft are published. This evidence update is prepared locally.
The separate planning draft PR creation has no confirmed result. No full
acceptance is inferred from publication or locally prepared artifacts.
