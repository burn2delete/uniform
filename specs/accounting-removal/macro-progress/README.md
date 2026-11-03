# Opt-in development-runner progress diagnostics

## Purpose

This current-main port adds an opt-in `--progress` stream to the existing
Clojure development runner. It writes closed, non-authoritative EDN breadcrumbs
to the existing stderr writer for namespace loading, once fixtures, each
fixtures, and test-Var calls. Default invocations remain unchanged.

## Requirements and dependencies

The runner keeps the existing selection, fail-fast, timing, completion schema,
machine-result sink, fixture, and cleanup paths. Progress state is created only
when `--progress` is selected. A diagnostic write, flush, or causal-inspection
failure is retained and blocks final completion after the selected body/finally
has unwound; the original body error remains primary. Interrupted causes are
restored only when directly observed or safely discovered. Frames contain phase,
edge, namespace, optional test identity, error class, schema, and the explicit
`:non-authoritative` marker; they do not contain argv, environment, messages,
stacks, results, or policy decisions.

The existing macro and Resolution commands opt in without changing selectors,
900-second aggregate timeout, 600-second serialized Resolution child timeout,
heap, capture, cleanup, or process behavior. The three Resolution children keep
their existing split and names. Progress logging is additional synchronous IO;
no resource-equivalence or termination claim is made.

## Outputs and artifacts

The implementation is in
`bootstrap/clojure/test/gravity/development_test_runner.clj` and the CI command
selection is in `tools/ci/run.clj`. Synthetic controls are in
`tools/ci/progress_test.clj` and `tools/ci/progress_isolation_test.clj`.
The exact source checkpoint, Git blob identities, focused commands, logs, and
nonclaims are recorded in `../pr697-current-port.edn`.

## Conformance and acceptance limits

Focused runner controls passed 12 tests/165 assertions, including the timing
and single-result-delivery composition control and the restored diagnostic
failure controls; isolation controls passed 3 tests/16 assertions. These
controls use synthetic Vars and writers; they do not run
Bootstrap, native code, a selected macro workload, or a hosted CI job. The
historical PR697 macro/static nonpasses remain historical evidence and are not
reclassified by this source port. Independent review of the exact checkpoint
and normal current-head CI are still required.
