# SH-18 Native Toolchain Harness Fixtures

These fixtures test the process boundary for a future SH-18 native compiler
target. They exercise external C compilation, executable launch, bounded output
capture, timeout cleanup, and negative compiler/runtime outcomes.

They are deliberately C fixtures rather than Gravity language fixtures. They do
not prove Gravity-to-C or Gravity-to-LLVM lowering, verified MIR consumption,
runtime completeness, artifact provenance completeness, or SH-18 completion.
The SH-18 integration owner must later feed verified Gravity-derived output
through this harness and satisfy the executable slice gate.

The harness freezes bounded source bytes inside its temporary compilation
directory, captures stdout and stderr through live byte limits, and requires
observed subprocess descendants to terminate within a finite cleanup deadline.
The dedicated tests generate output-flood and descendant-process programs
inside temporary directories; those programs are not persistent fixtures.
