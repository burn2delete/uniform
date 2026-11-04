# Evidence: Auth workflow migration

## Scope and identity

Observed source: `017b541d678c6acb93eb5e843b1eda5a66babc32`.
Source tree: `a9e593299ab0963578da242403572eb3941281ce`.
Date: 2026-10-26. Scope: four Markdown workflow artifacts only.

## Current disposition

- Artifact preparation: complete.
- Documentation validation: passed; structural checks only.
- Independent review: pending.
- Integration: pending; no merge receipt claimed.
- Component implementation: unassessed by this change; canonical outcome remains open.

## Retained evidence and limits

The [component roadmap](../../docs/component-roadmaps/auth.md) is the
source for existing partial implementation observations and remaining work.
Hosted capability projections explicitly lack grant/provider-trust, proof and release authority. Workflow artifacts do not change credentials, tool permissions or trust roots.
No source compilation, runtime action, authoritative migration or release test
was executed for this documentation slice. No new CLI execution or full automatic
workflow discovery is claimed. Contract routing is navigation, not a replacement
for retained specifications or an accepted semantic ownership transfer.

## Validation record

Locally observed 2026-10-26 on the 16-file checked-IR workflow candidate:

- Focused Clojure document checks: 16 artifacts, 141 valid relative links and
  heading fragments, ASCII/private-path checks, four migration-only checklists
  and four explicit unassessed implementation states passed.
- Negative link controls: missing file and missing heading rejected.
- `clojure -M tools/validate_gravity_docs.clj`: passed (240 contracts,
  19 phase indexes, ASCII and placeholder checks).
- `clojure -M tools/validate_full_language_roadmap.clj`: passed.
- `git diff --check`: passed.

These checks validate document structure and navigation only. No compiler test
ran and no implementation task received acceptance from them.

## Review and integration record

Await independent review of the exact candidate and a verified merge receipt.
Do not mark integration complete merely because a branch or draft PR exists.

## Implementation authority transfer candidate

Baseline: `846644db7402c26a421eb7afd52615d52f145590`, inspected on
2026-10-26. Earlier sections retain their original scaffold-only scope. The five
real implementation rows now live in [tasks.md](tasks.md#implementation-tasks)
as the sole editable authority. The roadmap table is generated. All five remain
Open and unchecked. The complete original migration task file remains a
byte-identical suffix, including its checkboxes and historical links. Migration
progress is separate from compiler implementation completion.

### Identity, contracts and relative-link provenance

IDs follow the established component/heading/title-slug convention documented by
[Reader](../reader-workflow/evidence.md#local-review-correction-link-context-and-identity).
They are `auth/remaining-tasks-and-evidence/` plus:

- `complete-provider-declaration-and-deterministic-selection`
- `enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`
- `bind-compiler-decisions-to-runtimebuild-enforcement-hooks`
- `bind-artifact-trust-to-exact-source-compiler-and-policy-identity`
- `publish-conformance-and-remaining-trust-assumptions`

Slash removal in runtime/build yields runtimebuild. No ID was positionally
renumbered. IDs were independently derived from original titles and compared in
display order. All titles, evidence baselines and closing criteria retain their
original meaning and projected bytes. The all-open header carries status;
evidence links are not acceptance. Later status updates render an explicit
Status column and require checkbox/Accepted; consistency, plus actual owner
receipts and semantic review outside the structural tool.

Three bootstrap evidence links retain their spelling in both document contexts.
The runtime-enforcement contract uses
`../../docs/phase-08-runtime-architecture/122-r11-runtime-capability-enforcement-design.md`
in the ledger instead of the roadmap's `../phase-08-runtime-architecture/` path.
The provider contract similarly uses
`../../docs/phase-01-core-language/025-l15-capability-provider-specification.md`
in the ledger instead of `../phase-01-core-language/` in the roadmap. Projection
rebases each target back to the original roadmap spelling. All five files exist;
all 18 contract-navigation lines in the plan retain their original bytes.

### Narrow shared-link extraction

`tools/task_projection_links.clj` extracts the accepted Legality relative-path
syntax, rebasing, file-existence and symlink-containment logic. It has no task
registry, discovery, CLI dispatch or write operation. Target validation inspects
file metadata and canonical paths; it does not read target contents. The fixed
component wrappers still validate before writing only their roadmap. Existing
Legality public arities, paths, record shape, diagnostics and output bytes are
preserved. Its focused test changes only to copy the helper into isolated CLI
fixtures. Legality task data and workflow artifacts are unchanged. The pure
shared parser, six earlier HTTPS-only wrappers and Syntax are unchanged.

### Observed scoped validation

Observed locally on 2026-10-26 from repository root:

- `clojure -J-Xmx512m -M tools/auth_task_projection.clj --check`: passed; original table bytes retained.
- `clojure -J-Xmx512m -M tools/auth_task_projection_test.clj`: 4 tests / 61 assertions, zero failures/errors. Covers deterministic projection, IDs/markers/history/status controls, all five actual evidence targets, unsupported/missing/escaping links, symlink escapes and subprocess fixed-path writes. Loading the helper with write-like arguments performs no CLI action and preserves files.
- `clojure -J-Xmx512m -M tools/legality_task_projection_test.clj`: 4 tests / 59 assertions, zero failures/errors. Existing assertions unchanged; fixture setup includes the extracted helper.
- `clojure -J-Xmx512m -M tools/task_projection_test.clj`: 5 tests / 137 assertions, zero failures/errors.
- An accepted-Legality before/after comparison passed 22 record/projection cases and five instrumented CLI cases, preserving returned records, full table/project bytes, diagnostic messages/data and fixed-path IO effects. It loaded the accepted implementation from Git baseline before the new implementation.
- The retained six-wrapper comparison passed 15 record cases and five CLI cases each, preserving complete output bytes, records, diagnostics, link policies and fixed-path IO effects.
- `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test`: 6 tests / 11 assertions, zero failures/errors.
- `clojure -J-Xmx512m -M tools/validate_gravity_docs.clj`: passed, 240 documents / 19 phase indexes.
- `clojure -J-Xmx512m -M tools/validate_full_language_roadmap.clj`: passed.
- A Clojure comparison checked exact original/current table bytes, five derived IDs/titles/Open statuses/closing criteria and actual link targets, complete historical suffix, 18 contract lines, five Markdown documents' ASCII/file/heading checks, and missing-file/missing-heading negative controls. Table SHA256: `72203b91f3afe43c24ce2f75a8e21fea40f74b43aa9dd4c381eac8f4776e3cfd`.

### Review, integration and limits

Independent review of all ten public files and PR body, clean exact-tree
integration preflight and publication identities are recorded in the handoff
when completed. Keep the candidate draft pending maintainer current-head review
and integration disposition. No external GitHub approval event is claimed.

No compiler source/tests, runtime enforcement, authentication policy, provider
credentials, permissions, signing custody or trust roots changed. Hosted
capability projections retain their explicit lack of grant/provider-trust,
proof, canonical and release authority. No compiler semantic suite, conformance,
release, self-hosting, seed retirement, dependency readiness or overall
completion percentage is established. All five outcomes remain open. Effects,
grants and signatures remain distinct; Build/Release retain signing and custody.

## Explicit provider declaration increment

Finding: `auth.provider-declaration.explicit-fields-v1`.
Owning task:
`auth/remaining-tasks-and-evidence/complete-provider-declaration-and-deterministic-selection`.
Observed locally on 2026-11-03 using the speckit-implement workflow, from clean
base commit `dd929d5584c3aacb1cf8d02039de8487ff6183be`, tree
`4c6bcfd25d665af7f71893590bc40e0194d3935b`. Results describe the uncommitted
owned-path changes, not that immutable base alone. No commit was made.

Source identities (Git blob hashes, obtained with read-only `git hash-object`):

- `bootstrap/clojure/src/gravity/providers/declarations.clj`:
  `5694716165ef1d109d162340a134f3c964842e4f`.
- `bootstrap/clojure/test/gravity/providers/declarations_test.clj`:
  `fc4a5f41cfc9d3435857820028682d0aaa47826d`.

### Observed products and rejection behavior

The [planned local format](tasks.md#explicit-provider-declaration-increment)
is implemented by pure `validate-declaration` and `record-declaration` in
`gravity.providers.declarations`. The producer validates actual input afresh;
it emits an internal declaration record only for explicit valid fields. The
accepted filesystem data fixture preserves every supplied declaration field,
including unsafe/runtime-check/human-review metadata and distinct runtime/build
effect sets, capability implementations and lower-level requirements.

Negative cases call the same producer: every missing required field, including
version, interfaces, resource/memory and replay; malformed effect sets, empty
capabilities/profiles/targets/contracts, invalid failure references and trust
labels; applicable missing signature policy; absent or malformed source context
and missing applicability. Each returns `:invalid` and diagnostics without a
record. Supplied complete/trusted/granted/conformance claims neither supply
missing fields nor become a producer decision. Explicit non-replayability and
all five trust labels remain declarations without granting their use.

Exact diagnostic assertions cover `L15-CONTRACT`, missing/invalid reason,
field path, expected carrier and source/manifest/profile/target context. The
multiple-error fixture observes deterministic order and excludes a secret
marker supplied in a malformed effect set, declaration extensions/scope,
unknown context keys and nested source-span payload. No rejected declaration
payload is returned. Public identities/path components remain the caller's
responsibility, as documented in the plan; this is not arbitrary secret
classification or a public artifact-export implementation.

### Exact checks

Each following argument vector was executed from repository root and exited 0:

```clojure
["clojure" "-J-Xmx512m" "-M:dev-test" "--namespace"
 "gravity.providers.declarations-test"]
;; 8 tests, 387 assertions, 0 failures, 0 errors.

["clojure" "-J-Xmx512m" "-M:dev-test" "--namespace"
 "gravity.capability-validation-test"]
;; 6 tests, 56 assertions, 0 failures, 0 errors.

["clojure" "-J-Xmx512m" "-M:test" "--namespace"
 "gravity.self-hosting.sh01-language-boundary-test"]
;; 6 tests, 11 assertions, 0 failures, 0 errors; no skipped namespaces.

["clojure" "-J-Xmx512m" "-M" "tools/validate_gravity_docs.clj"]
;; Passed: 240 docs, 19 phase indexes, ASCII, no placeholders.

["clojure" "-J-Xmx512m" "-M" "tools/validate_full_language_roadmap.clj"]
;; Passed: structural claims only; dated snapshot is non-authoritative and
;; completion admission remains disabled.
```

`git diff --check` passed. Initial focused-test attempts exited 2 for a test
reader delimiter error and then 1 for five assertions that incorrectly expected
the result's `:status` key to be absent. Those test errors were corrected;
the final focused run above includes additional exact-diagnostic assertions.
These earlier failures are not passing evidence. The historical Auth projection
script named above is absent in this checkout; it was not run or recreated.

### Authority, ambiguity and remaining acceptance

The hosted compatibility API, synthetic `record-provider-call!` records and
historical evidence remain unchanged. Existing memory/concurrency/macro and
whole-source refinements are retained as distinct pending criteria; no PR767/908
acceptance is consumed. Their external current heads were not inspected
(network is disabled).

L15 specifies required meaning but not a complete serialized bootstrap shape,
version syntax or behavior-reference resolution protocol. The plan names the
local carrier choice explicitly. Behavior, schema, conformance and signature
policy references are not resolved or verified by this producer. No current
canonical format acceptance, provider trust decision, grant, selection,
profile/target compatibility, provider service execution or secret export is
conferred. No actual provider action, runtime/build gate, signing, release,
self-hosting, seed retirement or broad integration suite was run or claimed.

Independent acceptance is missing and cannot be supplied by this worker's
self-checks. Integration is controller-owned and unperformed. The parent task
remains Open with its original checkbox, identity and closing criteria, pending
deterministic compatible selection and all other required implementation and
independent acceptance evidence. This increment supplies only the early
explicit declaration product; downstream obligations retain their own gates.

## Contracts CI repair for explicit provider declarations

Observed locally on 2026-11-04 using speckit-implement, starting from clean
commit `f6a851e2f2d9897d2730c78d34b9f5cecf3865b0`, tree
`32225012d6baa120d488a4d0816e9634833e35f5`. These results describe
uncommitted changes to this evidence file, `tasks.md` and `plan.md` in the same
increment. No commit or GitHub action was performed.

The supplied local CI log for run `41839997709`, contracts job `126608972404`,
reports dependency-controls failing with `ACCEPTANCE-DEPENDENCY` and
`Changed or ambiguous current task body: specs/auth-workflow/tasks.md`.
The actual dependency-control script reproduced that failure locally: 24 tests,
5184 assertions, 0 failures, 5 errors, exit 1. Moving the scoped progress
paragraph to the navigation preamble restored the captured task body.

The next actual run exposed a second failure previously hidden by the first:
`Stale SH input: specs/auth-workflow/plan.md` (24 tests, 5184 assertions,
0 failures, 5 errors, exit 1). The plan's only difference from its crosswalk
applying source, `05f11aba381d49124b8c9ddeb509f4d05791dff2`, was the local
explicit-declaration increment section. Moved that section after the task
region's end marker, retaining its input/output shape, required fixtures,
source basis and nonclaims. Rebased its producer-table link and the existing
local-format evidence link to their current locations. The plan now has no
diff from that pinned source; source criteria and source-version records were
not rewritten to accept drift.

All original task criteria/body bytes, task identities and unchecked Open
statuses remain. The historical migration suffix and all producer observations
above retain their scope. Provider source and fixture blob identities still
match `5694716165ef1d109d162340a134f3c964842e4f` and
`fc4a5f41cfc9d3435857820028682d0aaa47826d`, respectively. No implementation,
fixture, hosted compatibility API, synthetic provider record, canonical
contract, crosswalk, source-version/evidence ledger, acceptance validator or
test was changed. Existing positive and negative fixtures exercise the actual
producer and dependency controls; this documentation repair needs no new
provider behavior or fixture.

### Repair checks

Executed from repository root; all following vectors exited 0. The dependency
run passed after both relocations, including its existing navigation-preservation
and changed/ambiguous-body rejection controls. The earlier failed runs used the
same dependency vector and are recorded above, not counted as passing evidence.

```clojure
["clojure" "-J-Xmx512m" "-M" "tools/acceptance_dependencies_test.clj"]
;; 24 tests, 5489 assertions, 0 failures, 0 errors.

["clojure" "-J-Xmx512m" "-M:dev-test" "--namespace"
 "gravity.providers.declarations-test"]
;; 8 tests, 387 assertions, 0 failures, 0 errors.

["clojure" "-J-Xmx512m" "-M:dev-test" "--namespace"
 "gravity.capability-validation-test"]
;; 6 tests, 56 assertions, 0 failures, 0 errors.

["clojure" "-J-Xmx512m" "-M:test" "--namespace"
 "gravity.self-hosting.sh01-language-boundary-test"]
;; 6 tests, 11 assertions, 0 failures, 0 errors; no skipped namespaces.

["clojure" "-J-Xmx512m" "-M" "tools/validate_gravity_docs.clj"]
;; Passed: 240 docs, 19 phase indexes, ASCII, no placeholders.

["clojure" "-J-Xmx512m" "-M" "tools/validate_full_language_roadmap.clj"]
;; Passed: structural claims only; completion admission remains disabled.
```

### Repair authority and remaining work

The prior907 whitespace action remains restricted and was neither proposed nor
run; no substitute executable was used. No denied operation was retried.
Network access, hosted CI reruns, independent acceptance and integration were
not performed. Independent acceptance remains missing; passing local controls
cannot supply it. The local bootstrap-format ambiguity and unresolved downstream
selection, trust, grant, compatibility and execution obligations described above
remain unchanged. The broad parent stays Open and unchecked. This repair grants
no release, self-hosting or seed-retirement authority.

## Environment key policy intersection increment

Finding: `auth.environment-scope.eight-layer-intersection-v1`.
Owning task:
`auth/remaining-tasks-and-evidence/enforce-grant-intersection-attenuation-revocation-phase-and-lifetime`.
Observed locally on 2026-11-04 in the supplied current worktree
`increment-1174`, using the speckit-implement workflow. Results describe only
the uncommitted four owned-path changes. An immutable current candidate commit/
tree was not supplied in this request and was not re-inspected. No commit,
reference change, GitHub action, push, merge or independent acceptance occurred.

### Implemented scope and actual fixtures

The [local carrier](tasks.md#environment-key-policy-intersection-increment)
implements pure `gravity.capabilities.environment-scope/assess-scope`. It
requires the eight named SAFE10 layers, explicit allow/deny key sets, public
policy-source/redaction references, and matching principal, capability,
provider/version, phase, profile and target. The request retains its public
source identity/location. The actual declaration producer is called afresh;
unsupported capability/profile/target and mismatched declaration/context are
refused. Existing provider declarations, provider-choice work, compatibility
APIs, permission-row semantics and pinned source records are untouched.

The actual operation retains exactly the common keys, minus all explicit
denials, and emits sorted deterministic narrowing/denial provenance. Reordered
request/policy/declaration maps give equal results. Broadening later layers
cannot restore an upstream removed key. All redaction references survive in
ordered obligations with `:redaction-enforced? false`. Scope assessments do not
issue grants, select providers or construct handles.

Executable fixtures cover `environment-key-intersection`,
`reordered-maps-produce-equal-results`,
`downstream-broadening-cannot-restore-key`, `missing-policy-layer`,
`explicit-deny-at-each-layer`, `requested-key-outside-intersection`,
`wrong-phase-and-mismatched-identities`,
`mismatched-provider-and-unsupported-applicability`, `forged-scope-satisfied`
and `malformed-inputs-and-secret-marker-controls`. Every layer's missing,
malformed, denied, outside-scope and identity cases call the actual operation;
precise diagnostic ID/path/reason/layer assertions accompany the status checks.
A producer-call observation verifies the declaration/context actually supplied
to the existing producer. A supplied producer record is rejected as a
declaration substitute. Both matching build/runtime phases and empty sets have
explicit fixtures. Key-name legality covers NUL, equals and invalid Unicode,
while accepting a valid Unicode key without input quotas.

Secret-marker fixtures inspect the entire public result, including diagnostics,
for malformed fields and collections, unknown map keys, provider extensions,
producer failures and nested span payloads. Malformed inputs produce no
assessment. Public identities/key names/policy references remain trusted caller
metadata; these controls do not establish general secret classification,
redaction enforcement or public artifact export safety.

### Checks and restrictions

Checks run from repository root with exact argument vectors are recorded below.
The first focused run exited 2 for an extra test reader delimiter at line 172;
two excess closing delimiters were corrected. Subsequent focused runs passed
10 tests / 811 assertions before adding missing-policy-field and malformed
collection controls. Those earlier failures are not passing evidence.

The following final commands exited 0:

```clojure
["clojure" "-J-Xmx512m" "-M:dev-test" "--namespace"
 "gravity.capabilities.environment-scope-test"]
;; 10 tests, 1318 assertions, 0 failures, 0 errors.

["clojure" "-J-Xmx512m" "-M" "tools/acceptance_dependencies_test.clj"]
;; 24 tests, 5489 assertions, 0 failures, 0 errors.

["clojure" "-J-Xmx512m" "-M:dev-test" "--namespace"
 "gravity.providers.declarations-test"]
;; 8 tests, 387 assertions, 0 failures, 0 errors.

["clojure" "-J-Xmx512m" "-M:dev-test" "--namespace"
 "gravity.capability-validation-test"]
;; 6 tests, 56 assertions, 0 failures, 0 errors.

["clojure" "-J-Xmx512m" "-M:test" "--namespace"
 "gravity.self-hosting.sh01-language-boundary-test"]
;; 6 tests, 11 assertions, 0 failures, 0 errors; no skipped namespaces.

["clojure" "-J-Xmx512m" "-M" "tools/validate_gravity_docs.clj"]
;; Passed: 240 docs, 19 phase indexes, ASCII, no placeholders.

["clojure" "-J-Xmx512m" "-M" "tools/validate_full_language_roadmap.clj"]
;; Passed: structural claims only; dated snapshot is non-authoritative,
;; completion admission remains disabled.
```

The focused namespace was expanded after the initial successful run to include
every missing policy field, non-map inputs/layers and metadata-bearing public
symbols. The final 1318 assertions include those controls; secret inspection
enables metadata printing over the complete returned value.
Acceptance dependency controls passed with the unchanged captured bodies and
pinned plan, including existing task-body and source-drift rejection controls.
All seven requested checks completed successfully; none was skipped or denied.

Passive Clojure SHA256 reads identified final source and fixture bytes:

- `bootstrap/clojure/src/gravity/capabilities/environment_scope.clj`:
  `1ae1f9ca4abb08aa36d3c994da981d3203d2d5a4d6396f65a302fa4b86bd9e4f`.
- `bootstrap/clojure/test/gravity/capabilities/environment_scope_test.clj`:
  `e13b3f844d42d78cd9f80f1b96c178f06ed1c5e39779ff18aacedd5cc96fff5a`.

An additional successful Clojure assertion compared passive pre-edit/current
SHA256 snapshots of the pinned plan, complete marked task region and historical
task suffix. They remain respectively
`f1890883dcbdf6eddf407268502f905f1c0b2522b739dcf2c253cd9896f549be`,
`1bc3e9cae5084d86ad81183860fc46e563d61774b95c92520f9b275fb526112c`, and
`c2aa807bd087a8895fff1a424f2c0a53cda96c4edbaa1b4553d229ff0349321d`.
The snapshot check did not inspect Git/cache state or perform the restricted
whitespace action. `git status --short` showed only the two owned Markdown
files and the two new owned namespace directories.

The inherited907 Git whitespace action and select23 Git/cache-write inspection
actions remain restricted and were not attempted, transferred or replaced by
another executable. No actual check in this increment requested privileged
execution. Network access remains disabled. No live environment read, callback
execution, credential access, ambient authority or replacement accounting was
introduced.

### Missing authority and remaining semantics

L15/SAFE10 do not define this decoded serialized carrier, authenticate its
caller-supplied public policy sources, resolve provider scope/redaction
references or define a redaction-policy merging algebra. The local choices
above preserve all obligations without claiming their resolution or enforcement.
Declaration validity/profile/target membership does not prove provider trust,
conformance or canonical compiler profile legality. Real grants and their
authentic policy sources, first-class values, attenuation/lifetime/revocation,
secret/export enforcement and actual Runtime/Build action-boundary composition
remain parent obligations. Source parsing, provider action/conformance suites,
full compiler integration, release, self-hosting and seed retirement checks
were not run or claimed. No provider-choice candidate acceptance is consumed.

Independent acceptance of the exact resulting candidate is missing and cannot
be supplied by these self-checks. Controller integration is unperformed. Parent
task identity, captured criteria, checkbox and Open status remain unchanged;
this finite computation is useful construction evidence, not parent completion
or a release/self-hosting/seed-retirement authority claim.
