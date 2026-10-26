# Assurance boundaries

This is the detailed assurance-separation cleanup backlog and acceptance
contract. [Cleanup navigation](component-roadmaps/README.md) indexes the other
outcomes. This contract applies D1, D8, D9 and
[C18 pass correctness](phase-06-compiler-architecture/097-c18-compiler-verification-and-pass-correctness-strategy.md).
It does not replace the component implementation task lists or introduce a
routine approval gate for development.

## Responsibilities and actual consumers

The source audit used main `176c4ee65bb3ceb545f51edfaa599902030a0cb7`.
Paths below are repository-relative; the compiler component directory is
`bootstrap/clojure/src/gravity/bootstrap_impl/components/bootstrap-p15/`.

| Responsibility | Owner and exact consumer | Evidence required; insufficient substitutes |
| --- | --- | --- |
| Construction | MIR producer: `00736-p15-s23-c11-mir-block-keys.clj` and `00741-p15-s23-c11-mir-map-blocks.clj` | Raw products and construction provenance. Pending modules have `:constructed-unverified` status. Returning a value, a construction token or a successful constructor is not independent acceptance. |
| Independent semantic validation | Native verifier owner: `00740-p15-s23-c11-mir-validate-effect-edge-placement.clj`, `00743-p15-s23-c11-mir-validate-final-artifact.clj`, `00744-opaque-c11-construction-token-definitions.clj` | `validate-constructed!` creates its report. Final-artifact validation recomputes the verifier record from the pending view and authentic checked Core, then checks both equality and strict typed structure against the supplied report. Saved report equality is not itself fresh source replay. Test each actual entry point separately. |
| Publication readiness | Publisher and independent reviewer: `tools/check_worktree_preflight.clj`, `tools/validate_workstream_governance.clj`, [governance](workstream-governance.md) | Exact base/head/tree, clean named branch, affected scope, observed validation and independent review. Preflight checks Git identity; the ledger validator checks structured declarations. Neither authenticates a reviewer merely because its string differs from the author. Mergeability, test totals and a saved ledger are insufficient semantic evidence. |
| Runtime safety | Owning compiler/runtime checks under D8, C7-C11 and target contracts | Real type/index/arity/effect/capability, source/authentication, native class/callback, metadata, memory and runtime checks remain enforced. Construction, publication and project accounting removal cannot legalize unsafe execution. |

Host process observation is a fifth, operational responsibility. The bounded
development runner at
`bootstrap/clojure/test/gravity/self_hosting/sh07_bounded_development_runner.clj`
owns deadline checks, process exit observation and bounded capture. Its child
owns test execution. Both receipts explicitly remain non-authoritative.
An observed exit must agree with the child's declared exit, captured terminal
output must be complete, and closed routes with executable assertions cannot
pass with no tests or no passing assertions. Nonzero counters still do not
prove source authenticity, independent validation or publication readiness.

The runner's `:child-command-fn` is a trusted test injection seam, not a sandbox
for an adversarial same-user executable. Closed result keys reject supplied
authority, mergeability and saved-report additions. This protects the receipt
contract; it does not authenticate the truth of a producer's valid-looking data.

## Finite backlog and acceptance

| ID | Exact work and dependencies | Acceptance evidence and remaining boundary |
| --- | --- | --- |
| AS1 | Audit the four responsibility boundaries above, including actual constructor/report consumers, governance declarations and preflight. Depends on D1/D8/D9/C18 and current owner confirmation. | Source-grounded inventory above; independent review must confirm no stronger authority is inferred. Runtime safety guards remain owned by their implementation components. |
| AS2 | Repair the existing bounded development runner and its adjacent `_test.clj`: no-execution false passes and truncated passing prefixes. No compiler or serializer edits. | Cheap direct parser negatives plus a real saturated shell-output control; failed stream reads remain incomplete. Existing successful-result, timeout, late-result and descendant cleanup controls must be reported separately. No resource-heavy compiler selection is needed. |
| AS3 | Shared DATA transport, watchdog/capture and fresh replay mechanics in the existing Native parameter and MIR paired-process harnesses. Native owns the codec; MIR owns its consumer adaptation. | Exact-class preflight before foreign callbacks; reviewed collection-class policy; scalar/metadata/class roundtrip and negative controls; observed exit/deadline/digest gates; then actual original construction and independent fresh replay on the exact candidate. Controlled shapes and saved reports cannot close unknown actual artifact domains or public admission. |
| AS4 | Publish the completed declared increment with exact immutable evidence, independent review and ordinary integration. Depends on AS1/AS2; whole-outcome closure also depends on AS3. | Draft while incomplete. Record tested head/tree, command, passed/failed/unrun scope, original failed attempts, reviewer and disposition. Independent review and publisher readback own acceptance; this document cannot accept itself. |

100% means every responsibility inventoried here has independently testable
positive and negative boundaries, exact immutable evidence and an ordinary
publication disposition. It requires AS1-AS4; finishing the small runner fix
does not close the shared transport/fresh replay dependency or complete the
language. New findings must be added to the owning row with exact affected
contracts and acceptance evidence, rather than silently excluded.

## Existing dependency evidence

The MIR owner reports PR421 head
`8b9dd46ea38eafba1d3d7a49d8c6ca6b6488563f`, tree
`ed9d118e18f5f1a3f04138603e282ade9fb70701`, C11 source SHA256
`c41d9e0317b398a53e4b28d7bba90be7bebda23c51fa9c74a6a8c91be38ce79e`.
Its genuine selector timed out at 120 seconds, exit 124, without completed
counts. Helper 36/327, harness 10/52 and codec shape 10/92 results are reported
separate scopes, not acceptance of that source pair. Newer main receipts do not
accept this older tuple.

The shared codec version SHA256
`df10864240951a7f84c4b22bcdc67ace7d0076144110207ea1c0b1d9c6b7af31`
preserves Integer and metadata but its controlled probe showed HashMap becoming
ArrayMap, SubVector becoming Vector, and foreign metadata invoked before class
rejection. MIR requires exact concrete classes; its actual raw/Core/MIR runtime
domain is unproven. Native remains the sole codec writer. Shared policy and
preflight are open dependencies, not permission to weaken class/callback guards
or create a second serializer. Heavy A/B replay and public admission remain
unrun for this adaptation. Preserve historical failures and existing specific
C12 execution and original C7 publication restrictions.

## Runner increment evidence

Before the fix, the two new controls produced 2 tests / 17 assertions:
9 passed, 8 failed, exit 1. The real 256 KiB saturated-output control showed a
passing retained prefix hiding a second terminal result. No compiler source
construction, broker acquisition or restricted operation occurred.

The first affected-namespace run after the fix produced 10 tests / 64 assertions:
63 passed, 1 failed, exit 1. The unchanged descendant-cleanup assertion expected
a positive captured descendant count but observed zero; root cleanup completed.
That failure is retained and is not a passing watchdog receipt. A subsequent
minimal process-observation diagnostic failed with `Operation not permitted
(sysctl failed)`. The sampler previously swallowed that exception and could
claim complete cleanup without observing descendants. Sampling failure now
explicitly prevents a complete-cleanup claim. Failed-stream-read and
failed-process-observation controls were added. Final immutable validation and independent
review must record their own exact source tuple and current results.

Run this namespace with a 256 MiB JVM and the existing test classpath; the tests
launch only synthetic shell children. They do not launch a compiler route.
Run the language boundary and structural docs gate under the existing host
resource policy before integration. Preserve fresh independent compiler replay
for the component acceptance claims that require it.

## Immutable runner validation receipt

Tested head `3d98800f906e7a66fdbda26cc6b1be474278811f`, tree
`6ba5a222d2267690de797dd4c5f02b5960f7797e`, base
`176c4ee65bb3ceb545f51edfaa599902030a0cb7`. Git head/tree and empty
tracked/untracked status matched before and after each command. Both existing
normal resource brokers admitted the run. Supported escalation enabled actual
process inspection; the earlier sandbox failure remains above. Every child
exited naturally within its 60-second watchdog.

| Stage | Exact selection | Result |
| --- | --- | --- |
| Runner | `clojure -J-Xmx256m -Sdeps '{:paths ["bootstrap/clojure/src" "bootstrap/clojure/test"]}' -M -e "(require 'clojure.test 'gravity.self-hosting.sh07-bounded-development-runner-test) (let [r (clojure.test/run-tests 'gravity.self-hosting.sh07-bounded-development-runner-test)] (System/exit (if (zero? (+ (:fail r) (:error r))) 0 1)))"` | Natural exit 0, 3778 ms; 12 tests / 71 assertions, zero failures/errors |
| Docs | `clojure -J-Xmx256m -M tools/validate_gravity_docs.clj` | Natural exit 0, 1391 ms; 240 docs / 19 phase indexes |
| Language | `clojure -J-Xmx512m -M:test --namespace gravity.self-hosting.sh01-language-boundary-test` | Natural exit 0, 1710 ms; 6 tests / 11 assertions, zero failures/errors; one namespace attempted, none skipped |

The exact runner SHA256 is
`17721a14e5cca596df91185b49b384df5793fef1175fc9b07abc5c162e2716a7`;
test SHA256 is
`b4d0f4805112e72ca82b7813230658be64c29e52c511113de5aac9d4faabe0f4`.
Retained process receipt SHA256 values are runner
`ee264dc27ba1e5ae12e135bc46c767341a91ff7833a15e720407c720f5dc91f1`,
docs `cd16b7050458fb35f58fa182423b7acc604b5d37063ddf399d00ea08b20bd0c5`,
language `c0454599a3dc6daa4ad2e846fa412896781f2e17507ca0678897122592de894c`.
These are operational receipts, not new acceptance authority.

Independent static review of this exact source/tests/document tuple found no
blocking defect for the finite AS1/AS2 runner increment. The reviewer checked
EOF-only capture, saturation and read failure, declared/observed exit agreement,
empty execution, counter representability and sampling-failure cleanup. The
review did not independently execute the tests. A later documentation-only
receipt commit does not change tested runner/test bytes or imply another run.

AS1 source inventory and AS2 runner controls are evidenced for this increment.
AS3 shared codec policy, actual artifact domain, genuine fresh replay and
public admission remain open with their existing owners. AS4 publication and
integration must name the final exact head. No compiler construction,
authoritative migration, runtime safety proof, C12 execution or original C7
publication was performed by these checks. Whole-outcome completion is open.
