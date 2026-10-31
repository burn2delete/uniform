# JVM source emission extraction

## Governing contracts and scope

D1/D2/D3/D6/D8/D9, C1/C14/C15, B1/B5/B13/B14, P4 and TEST2 govern
this bounded extraction. B5 requires verified input for backend admission and
declared JVM/profile/interop policy. This increment moves existing internal
Java source construction and plan-level rejection behind gravity.backend.jvm;
it does not implement canonical public emit/verify or authenticate input.

The API owns byte-array/value/write/test source, recursive value and
instruction source, Program.java and module-info.java text, output-path and
source-map data, and the existing plan validator adapter. Inputs are explicit
caller-owned scalar/control-flow plans, counters/environments, source text and
compatibility operations. Outputs are source strings, descriptors, paths,
partial source-map records or the existing C14-UNSUPPORTED diagnostic.

The stage requires clojure.string and explicit operation ports. Bootstrap
consumes it; it must not require Bootstrap, CLI or release. Bootstrap keeps the
shared iterative scalar validator, diagnostic construction, source/packet
authentication, process invocation, class/JAR validation and publication.
Target constants and dynamic Java commands retain their existing owners.

## Implementation and evidence

Ten original functions and one module-source initializer move from backend
components 00772/00773. The initializer becomes a stage function called once by
the original Bootstrap def; its value and initialization order remain intact.
Sixteen explicit Var ports and one target-value port preserve late resolution,
recursive forwarding and callback order. Bootstrap public arities are unchanged.
The new stage publishes its contract on every public operation. No enforcing
accounting, input quota or host-process policy changes are introduced.

Source parent is f2bfea387b60077a64e0dcbf2573c6afad0126e9. Before captures were
written before extraction. Scalar and control fixtures produce real Program.java
and module-info.java files: 2177/51 and 2014/51 bytes respectively. They exercise
UTF8/NUL, integer extrema, nil/booleans, quotes, locals, let, do, branches,
println and direct string operands. Five actual plans reject C14-UNSUPPORTED:
unsupported operation, malformed println, unbound local, unsupported string
arity and malformed let. Complete diagnostics are paired.

Six complete before/after files match, before and after the unused-port lint
fix. Byte digests match; fixture canonical data identities, B5 source hash and
partial source-map content hashes recompute. Fixture identities are data
digests, not authenticated compiler plan IDs. Caller-supplied source-map text
and historical origin-chain labels are copied data; these fixtures do not
establish that illustrative source-map text compiled into their explicit plans.
No authenticated compilation provenance is claimed.

The read-only verifier reconstructs the whole stage, Bootstrap require and both
complete consumer files from immutable Git input. All four source files match;
unmoved packet authority, shared validator, diagnostic construction, processes
and publication remain exact. Temporary tools are Clojure:
`/tmp/uniform-jvm-source-{evidence,extract,verify,register}.clj`; captures are in
`/tmp/uniform-jvm-source-evidence/`. Do not extract from rewritten wrappers.

Focused JVM/LLVM regressions pass 10 tests / 119 assertions; JVM alone covers
four tests / 53 assertions. Arity/API, real refusal, callback order, late
interposition, unrelated exception identity and preserved fallback tests pass.
Ownership passes 26/905 and language 6/11; docs pass 240/19, roadmap/governance
pass. Leaf lint has zero warnings/errors. Registration uses the existing
sh-target-jvm owner with SH-00 as a non-migration association; no SH milestone
completion follows. Final committed contextual lint, independent acceptance,
exact CI and clean current-main integration preflight remain required.

## Retained local nonpasses

The initial test attempted to compile the complete current Gravity B5 module.
The frontend rejected its keyword call, operator :required-fields, with
L2-UNKNOWN-CORE-FORM before runtime execution. No fallback, source rewrite or
model execution claim replaces this result; the unsupported execution test was
removed from the positive set. Its log is
`/tmp/uniform-jvm-source-baseline-tests.log`. Original Java/plan tests passed
1/28, then extraction passed the same tests. A missing closing parenthesis in
the added API test caused a read error before tests; the first verifier launch
also preceded completed after-capture files. Their logs are retained. The
corrected ordered sequence passes. Leaf lint found five unused ports in the
short forwarding arity; underscore prefixes preserve their positions and all
expressions, callbacks and wrapper arguments. Fresh captures and whole-source
reconstruction pass after that explicit fix.

## Unsupported boundaries

This slice emits Java target source; it does not run javac/java, emit or execute
class files, build JARs, authenticate stage2 packets or admit untrusted plans.
The source map is source-unit-only and explicitly partial, with no per-form
origin claim. Existing host scalar formatting is preserved; unsupported emitter
operations still return empty text and test-source defaults false, so emission
is not a validator. General MIR/domain lowering, Gravity B5 execution parity, nullability,
exception/reflection/classloading/resource policy, native-image support, full
B5 conformance, migration, release, self-hosting and seed retirement are not
established by this extraction.
