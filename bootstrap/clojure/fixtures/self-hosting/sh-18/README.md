# SH-18 Provisional Native C Emitter and Toolchain Harness

This directory contains two deliberately separate SH-18 leaf boundaries.

The pre-existing C fixtures test the external process boundary for a native
compiler target. They exercise C compilation, executable launch, bounded output
capture, timeout cleanup, and negative compiler/runtime outcomes. They are not
Gravity language fixtures.

`native_c_emitter.gravity` and the paired `.gravity`/`.qst` request fixtures are
a provisional Gravity-authored emitter for one bounded `:native` program shape.
They follow the SH-18 backlog gate and the B1, B2, P5, and R3 contracts. The
emitter validates an explicitly verifier-shaped input descriptor, constructs a
strict hosted-C11 translation unit, records its generated helpers, runtime and
failure policy, source mappings, and path-neutral semantic identity, and passes
the generated source to the independent toolchain harness.

This bounded leaf accepts only the semantic target triple `:host-native`.
During external validation that token is resolved to the compiler discovered on
the current host; the harness records the concrete compiler target and
toolchain identity in the compilation result, and the test binds those records
to the emitted `:host-native` request. Foreign target triples are rejected
rather than being silently compiled by the local toolchain.

The request fixtures carry synthetic checkout paths to prove that physical
paths are excluded from semantic identity while remaining distinct in
provenance. They do not prove that coordinator ingress independently captured
the actual fixture path. That binding remains coordinator-owned.

Neither boundary proves authenticated SH-17 input, complete MIR consumption,
collections or layouts, runtime completeness, release-grade artifact
provenance, public routing, seed retirement, or SH-18 completion. The SH-18
integration owner must later replace the provisional verifier-shaped descriptor
with authenticated SH-17 output and satisfy the complete executable slice gate.

The harness freezes bounded source bytes inside its temporary compilation
directory, captures stdout and stderr through live byte limits, and requires
observed subprocess descendants to terminate within a finite cleanup deadline.
The dedicated tests generate output-flood and descendant-process programs
inside temporary directories; those programs are not persistent fixtures.

The emitter test distinguishes an unavailable external compiler from a passed
execution. Set `GRAVITY_SH18_REQUIRE_NATIVE_TOOLCHAIN=1` for the authoritative
gate; in that mode an unavailable compiler is a test failure rather than a
skipped external execution.
