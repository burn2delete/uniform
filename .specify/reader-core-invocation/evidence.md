# Reader-to-Core invocation pilot evidence

Status: planning complete; joined implementation and independent acceptance open.
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
Native and current main still require proportionate composition validation.

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

The spec -> plan -> tasks artifacts name a real pending integration slice.
The next acceptance step is an exact frozen frontend join, actual paired public
accepted/rejected checks, and independent review. The end-to-end pilot remains
incomplete until that evidence exists. Migration, component release, self-hosting,
seed retirement and performance are not claimed.

At preparation time, publication is partial: spec.md, plan.md and tasks.md are on the feature branch.
This evidence file and a draft PR have not yet been published. No publication or
review completion is inferred from the local artifact set.
