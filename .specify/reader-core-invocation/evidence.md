# Reader-to-Core invocation pilot evidence

Status: finite pilot checks passed; independent aggregate acceptance remains open.
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
The draft subsequently reconciled accepted main `16ff1f59` normally at
`3326a974a3b45e683391bfc7a99e0cc2965e8899`, tree
`1c50652f57588d5bb715a0b7497845f24fa456cc`, without a new authored compiler
delta. Execution receipts below remain bound to the earlier frozen tree;
reconciled/cohort verification must be recorded separately.
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
- The retained real Resolver product rejects binding-table mutation, absent or
  historical invocation, foreign Reader selection, and invalid envelope options.
  Mixed AE source hashes fail the fresh-envelope check. The omitted-options AE
  verifier retains `B1-INPUT` / pinned-source rejection. Each rejection phase
  exited successfully under the same 120-second host watchdog.
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

## Actual paired public Core results

Both actual public CLI commands used the unchanged
`sh-07/compiler-exact-keys.gravity` / `.qst` fixtures and the explicit
`--development-current-reader` option. They exited 0 in 181.014 and 146.878
seconds respectively and returned accepted Core products with complete proofs.
Their shared artifact identity is
`sha256:16ec6d50a8514188f3010880f3022e88f7f1054fe4058a5caa6b74367ee98f0e`.
These fixture files are separate subjects, not full compiler-owner coverage.

- Both genuine CLI products passed the unchanged fresh generic Core verifier.
  Both coherently rehashed Bool substitutions were rejected while their
  declared semantic identities still recomputed. The existing
  `development-public-product-passes-fresh-verification` and
  `development-public-product-rejects-rehashed-core` tests produced 4 tests /
  26 assertions, no failures or errors, across the two products.
- Seven existing Reader/public ingress tests passed 90 assertions. They cover
  exact caller-owned options, historical routing, strict source decoding,
  real Reader source-byte drift and restoration, and actual Core construction
  after deleting the target file once Resolver returned. The checkout and
  Reader bytes were clean afterward.
- Direct retained-product inspection passed 166 checks: both 25-node products
  conserve original forms, source-map lineage and spans, source bytes and path
  provenance. Semantic preimages/IDs agree while file provenance differs.
  This inspection supplements, never replaces, the fresh verifier results.
- All three malformed public-source cases rejected at the owning Core stage
  for both extensions: the existing paired test passed 1 test / 34 assertions
  in 661.181 seconds. Actual default generic Core verification also rejected
  the retained development product rather than inferring current authority.

## Scoped Core quota-removal regression checks

The independently identified structural coverage gap was checked without a
full-owner construction replay: eight B16 shape/digest/forest/native/decimal
tests, seven carrier-DAG and receipt tests, three actual Native report-join
tests, and the changed keyword-map development kernel. Result: 19 tests /
1,951 assertions, no failures or errors, 22.784 seconds wall time.
These are real current source-function and native carrier checks on structural
inputs, not authentication of an entire compiler-owner module.

Public integration uses the existing documented 1,200-second host watchdog and
2 GiB heap. It does not add a compiler quota or increase the separate Resolver
phase watchdog. Raw CLI output is retained; a JVM performance-data warning on
stdout was kept separately from the unique Core EDN record, without rerunning
construction or changing any artifact value.

The finite affected-consumer closure and independent review are recorded below.
The full authenticated-envelope-owner C01 construction remains unestablished;
the unchanged historical full-owner timeout was not replayed.

## Minimal affected-consumer closure and final verification

The final candidate tree `c3fe709394b414652dc8e19d8170af5eca312dac` extends
Diagnostics draft #385 (`298c7bc7`) with exactly six files: the four-file explicit
Native invocation delta published in #387 (`d1c56a4f`) and the newest two-file
current-Core/C11 metadata correction published in #378 (`9b7e6094`). Their
unfinished parent stacks are not imported. Historical arities remain fail-closed;
the public Core API does not depend on the legacy Native ingress repair.

- Actual original Core construction, fresh replay and three literal mutations:
  1 test / 11 assertions passed, with the MIR plan never executed.
- Actual original Core to Gravity C11 construction/fresh verification and eight
  malformed metadata, lineage and type variants: 1 test / 31 assertions passed.
  These two receipts precede addition of the Native test file; production code
  is byte-identical to the final candidate.
- Final-candidate real C2/C3 invocation, default and tamper regression: 4 tests /
  16 assertions passed. Language boundary: 6 tests / 11 assertions passed. LOC
  validation passed across 2,026 source files, zero exceptions, maximum 250
  physical lines; whitespace validation passed.
- In the original configured source root, 12 direct checks reconfirmed retained
  product paths and bytes. Both public Core formats then passed fresh generic
  verification and both coherently rehashed substitutions rejected: 4 tests /
  26 assertions, zero failures/errors, natural exit 0, with matching clean
  candidate-tree bookends. Constructors were not rerun.

Independent read-only review found no blocking issue in the six-file closure,
its explicit authority routing, retained C11 semantic checks, or exact final
verification. Separate receipt readback accepted the final 4/26 result. This
establishes the selected development-current invocation outcome, not Native C11
verification, C12 specialization, full-owner census or whole-component closure.
Integration of the exact reviewed tree is recorded separately below.

## Verified integration receipt

[PR #388](https://github.com/burn2delete/uniform/pull/388) merged to main on
2026-10-25 at 23:07:25 UTC. The merge commit is
`001ab9edbe84ca0132f3ebc7e6117792ec8d3e13`, with tree
`b8104cef6aa946d309a0e43c338835f5b9a222ad` and parents
`837ed8bc56c230574245988904f32773677584b1` and
`ed1d8b5616a3c532d4f32a3e712f418f112c7c20`. Independent remote readback and
Git object inspection confirmed the merged state, exact parents and expected
conflict-free tree. The named integration checkout was clean afterward.

The public PR head is byte-identical to tested tree `c3fe7093`. The merge also
retains the separately reviewed semantic naming change from #386, which had
zero changed-path overlap. Existing test receipts retain their original tree
attribution; no unexecuted post-merge suite is claimed. No GitHub workflow or
status checks were present, and no CI pass is inferred from their absence.

The integrated finite outcome is current development source-to-Core invocation
plus minimal existing-consumer compatibility. This is not full SH07/C01,
whole-MIR, compiler-owner census, release, migration or self-hosting acceptance.

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

## Pilot method and finite completion

The merged `speckit-specify`, `speckit-plan`, `speckit-tasks` and
`speckit-analyze` instructions were read and followed manually for this selected
feature. No external Specify CLI, shell helper, automation, literal chat-command
parsing or skill autodiscovery is claimed. Existing scaffold/adoption checks
remain historical and separate.

The spec -> plan -> tasks artifacts track a real focused implementation,
independent review, final joined-tree verification and verified integration.
The finite end-to-end pilot is complete at the recorded scope. This does not
turn the earlier scaffold/adoption checks into implementation evidence.
Migration, component release, self-hosting, seed retirement and performance are
not claimed.

At this update's preparation time, planning [draft #381](https://github.com/burn2delete/uniform/pull/381)
is published and implementation #388 is verified merged. This documentation
receipt is prepared for the same planning draft; it does not broaden the
recorded implementation acceptance.
