# Native Tooling and Ownership pipeline CI coverage

## Governing contracts and observed gap

D1/D2/D3/D8/D9, C1/C9/C15, T1-T13, TEST2 and BOOT8 govern this regression
coverage increment. Source parent is
7a50ed6d056a8e47ec35c5ec9f306ebdd5e5ec43, tree
c059db13e592916e0b9943f8ca9b1cc44d123bec. Its MIR source acceptance and
hosted CI remain separate evidence. Run 41952115248 failed before the
Resolution unmetered tests could execute because Maven Central returned HTTP
429 while resolving Clojure 1.12.5. Preserve that infrastructure NONPASS.

The native gravity.tooling.pipeline-test and gravity.ownership.pipeline-test
namespaces are registered as standalone in the component inventory, but are
not selected by tools/ci/run.clj or the Bootstrap-free leaf catalog. Source
registration alone does not execute their tests. Existing source-only slices
include substantial Tooling/Ownership algorithms whose regressions must run
before integration can rely on hosted CI coverage.

## Smallest complete change

Append two complete-namespace commands to the existing contracts group:
tooling-native-validation and ownership-native-validation. Each uses the
existing command constructor, 2g heap, 180-second host child watchdog and
development test runner with progress. Preserve every prior command, workflow,
matrix mode and affected-path decision. Do not change compiler or pipeline tests,
runtime/dependency versions, dependency repositories, authorization or pins.

Selection controls must prove both exact namespace commands select every
current deftest, and that omission or substitution loses that coverage. Keep
existing command ordering and all four prior contract commands unchanged.

## Real execution and evidence

Unmodified source-parent execution of both whole namespaces passes 51 tests /
1495 assertions. Ownership compiles paired normalized SH10 requests and the
current Gravity C9 engine through the ordinary fixed seed; it accepts valid
ownership transitions and rejects actual use-after-move. Tooling compiles
paired W5/source fixtures and exercises native report/artifact construction,
identity, metadata, refusal, substitution and callback-order controls. These
routes do not invoke private Core/C11 admission or renew SourceC9 authority.

Run the real changed contracts command group, CI harness/format/lint controls,
language boundary and document validators. Retain exact source/provenance,
selected commands, positive/negative results and unexpected failures. Require
independent review, the candidate's own applicable hosted CI/artifact audit,
clean commit and exact current-base coordinator integration. An earlier CI
failure or another source's success cannot confer acceptance on this slice.

## Unsupported boundaries

This adds regression execution, not namespace semantic ownership or language
features. Native proof/status labels remain data. No tooling service, private
ownership/Core admission, capability grant, source authentication, package or
release operation, migration, self-hosting or seed retirement is established.
There is no controller/service/Main grant. The scope record itself earns no
implementation or integration credit.

## Implemented selection and initial local checks

The two commands are appended after the four existing contract scripts with
complete namespace selection and no test subset selectors. The new control
checks exact command labels/order, heap/watchdog/runner arguments, every
current source deftest, and omission/substitution negatives. Existing CI
harness controls pass 51 tests / 545 assertions; format and direct CI lint
pass. The language-boundary test passes 6 tests / 11 assertions. The 13 local
harness output files are retained separately from the source checkout.

The real changed contracts group, exact-source contextual lint, independent
review and hosted CI/artifact acceptance remain pending. This candidate does
not retry PR1206's failed dependency request or transfer its other job results.
