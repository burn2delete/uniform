# Normal runner progress diagnostics

## Purpose and interface

The opt-in --progress flag writes closed non-authoritative EDN breadcrumbs to
the existing stderr stream. Source call boundaries cover namespace load,
once fixture, each fixture and test-Var invocation: enter/return/throw. Return
means the call returned; failed assertions may return normally. No messages,
stack traces, raw errors, argv or environment values enter these frames.
The macro CI selection enables the flag; its namespaces, 2 GiB heap and
600-second watchdog are unchanged. Other command selections remain unchanged.

## Failure and completion

Format/write/flush errors are retained before fallible causal inspection and
deferred while selected execution/finally unwinds. Sticky failures block timing
and final completion publication. Escaping original errors remain primary;
distinct diagnostic/accessor errors remain suppressed by identity. Direct root
interruption is recorded before inspection and restored after driver unwind.
Unreadable graph edges do not establish an interrupted cause. PrintWriter's
error flag also fails the driver; an IOException hidden by that API has no
available original identity, which is explicitly reported rather than invented.

## Outputs and validation

No extra output file, controller, thread or clock is added. Disabled mode has no
progress state or writes. Reporting adds synchronous formatting/IO and log bytes;
there is no cost-equivalence, timeout-cause, cleanup or authenticity claim.
Existing selection, fixtures, fail-fast counters, count equality, completion
schema, System.exit and process/capture cleanup remain. Cheap injected controls
in tools/ci/progress_test.clj exercise the actual runner and Writer call sites
with synthetic Vars and no Bootstrap or test-family workload. They do not
measure real test overhead or establish descendant termination. Independent
implementation review and normal changed-head CI remain required before use.
