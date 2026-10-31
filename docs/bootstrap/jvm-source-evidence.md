# JVM source emission extraction

## Governing contracts and scope

D1/D2/D3/D6/D8/D9, C1/C14/C15, B1/B5/B13/B14, P4 and TEST2 govern
this bounded extraction. B5 requires verified input for backend admission and
declared JVM/profile/interop policy. This increment moves existing internal
Java source construction and plan-level rejection behind gravity.backend.jvm;
it does not implement canonical public emit/verify or authenticate input.

The planned API owns byte-array/value/write/test source, recursive value and
instruction source, Program.java and module-info.java text, output-path and
source-map data, and the existing plan validator adapter. Inputs are explicit
caller-owned scalar/control-flow plans, counters/environments, source text and
compatibility operations. Outputs are source strings, descriptors, paths,
partial source-map records or the existing C14-UNSUPPORTED diagnostic.

The stage may require clojure.string and explicit operation ports. Bootstrap
consumes it; it must not require Bootstrap, CLI or release. Bootstrap keeps the
shared iterative scalar validator, diagnostic construction, source/packet
authentication, process invocation, class/JAR validation and publication.
Target constants and dynamic Java commands retain their existing owners.

## Planned evidence and tests

Before/after captures will compare full Java/module text, complete source-map
and path data, stable source hashes and plan identity. Focused fixtures will
exercise scalar literals, UTF8/NUL, quotes, locals, let, do, branch and println,
plus real unsupported-op and malformed-plan rejection. Tests must cover public
arity metadata, late Var interposition and operation order. Current Gravity B5
source-model execution will be reported at its actual authority.

All moved bodies and complete consumer files must reconstruct from the exact
immutable source parent. Focused regression, source-language boundary,
ownership, document/roadmap/governance and committed contextual lint checks,
independent acceptance, exact CI and clean integration preflight remain pending.

## Unsupported boundaries

This slice emits Java target source; it does not run javac/java, emit or execute
class files, build JARs, authenticate stage2 packets or admit untrusted plans.
The source map is source-unit-only and explicitly partial, with no per-form
origin claim. Existing host scalar formatting and emitter fallbacks must remain
honest compatibility boundaries. General MIR/domain lowering, nullability,
exception/reflection/classloading/resource policy, native-image support, full
B5 conformance, migration, release, self-hosting and seed retirement are not
established by this extraction.
