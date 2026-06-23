# L12 - Compile-Time Evaluation Specification

Sequence: 22
Phase: 1 - Core Language
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Gravity treats compile-time computation as part of the language, not as an
untracked compiler convenience. Macro expansion, constant evaluation, schema
loading, target specialization, generated bindings, and compiler extension code
all run before the final program artifact exists. This specification defines
how those computations are authorized, evaluated, recorded, replayed, cached,
and rejected.

The central rule is: compile-time execution may change artifacts, but it may not
hide authority. Every read, generated form, nondeterministic choice, external
tool call, model call, or target probe must be represented in the build effect
system and in emitted provenance.

## Requirements

- Compile-time evaluation must obey the same type, effect, capability, memory,
  profile, and safety checks as runtime code.
- Pure constant evaluation must reject undeclared IO, host access,
  nondeterminism, and runtime-only values.
- Build effects must be declared, granted, traced, and replayable according to
  build policy.
- Generated code must retain provenance and pass normal Gravity validation.
- Hermetic builds must be reproducible from declared inputs, lockfiles, target
  manifests, grants, compiler version, and replay records.

## Dependencies

- `L1` defines syntax objects, source spans, reader forms, and metadata.
- `L2` defines core evaluation and value identity.
- `L4` defines macro expansion and generated-code provenance.
- `L5` defines type legality for compile-time values and generated forms.
- `L6` defines effect legality, including build effects.
- Phase 12 build and package documents define artifact graph storage, lockfiles,
  and cache persistence.

`L15` is a later refinement that defines capability providers and grant records
for compile-time services.

## Outputs and Artifacts

- Compile-time evaluation trace.
- Constant value table.
- Generated-form provenance records.
- Build effect log.
- Hermetic replay record.
- Cache key and cache reuse decision.
- Diagnostics for rejected compile-time behavior.

## Position in the Language

Compile-time evaluation depends on:

- `L1` for syntax objects, metadata, reader forms, and source spans.
- `L2` for core evaluation rules and value identity.
- `L4` for macro expansion and generated-code provenance.
- `L5` for the type legality of compile-time values and generated forms.
- `L6` for build effects and capability requirements.
- Phase 2 safety documents for rejecting unsafe generated code.
- Phase 12 build and package documents for artifact graph storage, lockfiles, and cache keys.

Provider details for filesystem, network, host, model, and tool access during
compilation are refined later by `L15`.

Compile-time evaluation is earlier than runtime evaluation but is not more
privileged. The compiler may provide evaluator services, but those services must
obey the active package manifest, profile, target, lockfile, build grants, and
hermeticity mode.

## Compile-Time Entities

Gravity recognizes these compile-time entities:

- A syntax object is source data plus lexical context, metadata, source spans,
  expansion phase, and provenance.
- A compile-time value is a value produced by the compile-time evaluator and
  serializable into a build artifact, generated form, constant table, or
  diagnostic.
- A generated form is syntax emitted by a macro, compiler plugin, schema loader,
  derivation pass, or code generator.
- A build effect is an effect that occurs while producing artifacts rather than
  while the final program is running.
- A build grant is a manifest or command-line authorization that allows a build
  effect for a package, namespace, tool, or extension.
- A compile-time trace is the ordered record of evaluated forms, accessed inputs,
  granted capabilities, emitted generated forms, and replay keys.
- A hermetic build is a build in which all compile-time inputs are declared,
  content-addressed when possible, and replayable without ambient host state.

## Evaluation Phases

Gravity has ordered compile-time phases:

1. Source reading converts bytes into syntax objects.
2. Namespace loading resolves imports, aliases, profile declarations, and
   package manifests.
3. Macro expansion rewrites syntax into ordinary Gravity forms.
4. Name, type, and effect analysis establish the facts required to authorize
   compile-time work.
5. Profile and capability validation reject compile-time forms, providers, or
   generated outputs that are illegal for the selected profile and grants.
6. Constant evaluation computes values required by type checking, layout
   selection, specialization, precomputed tables, or target configuration.
7. Compiler extension execution runs authorized plugins and derivation hooks.
8. Typed core lowering produces checked core artifacts after generated forms are
   revalidated.
9. Backend preparation may run target-specific specialization guarded by build
   effects and target capabilities.

A later phase may inspect artifacts from an earlier phase, but it must not
mutate earlier artifacts in place. A compiler that normalizes or memoizes
compile-time data must keep the original source span and generated-origin chain
available for diagnostics.

## Compile-Time Evaluation Forms

Gravity provides these source-level entry points:

```clojure
(defconst page-size
  (compile-time (* 4 1024)))

(def generated-table
  (compile-time
    (pure/generate-table 256)))

(derive-code :json/User UserSchema)
```

`compile-time` evaluates an expression during compilation and stores the result
as a compile-time value. The expression is legal only when its type, effects,
capabilities, allocation behavior, and profile assumptions are legal in the
compile-time environment.

`defconst` binds a compile-time value that may be embedded into generated code
or runtime constants. A `defconst` value must have a stable representation for
the target profile. Hosted objects, open handles, mutable cells, thread objects,
closures over ambient host state, and unrecorded foreign references are not
valid constants.

Derivation forms such as `derive-code` are ordinary macros or compiler
extensions with declared build effects. They may emit declarations, protocol
implementations, serialization functions, layout metadata, tests, or proof
obligations, but their output must pass the same type, effect, capability, and
safety checks as handwritten source.

## Pure Constant Evaluation

Pure constant evaluation is the default compile-time mode. It may evaluate:

- Literal values, quoted data, and syntax objects.
- Pure arithmetic with declared overflow behavior.
- Pure collection construction and access.
- Pure functions explicitly marked as compile-time callable.
- Total pattern matching over known finite values.
- Type-level predicates and layout queries whose inputs are fixed by the
  current profile and target.

Pure constant evaluation must not perform file IO, network IO, environment reads,
clock reads, randomness, process execution, model calls, host reflection,
mutable global access, allocator-dependent pointer inspection, or target probing.

If pure evaluation cannot prove termination within the compiler's configured
fuel limit, the compiler must emit a deterministic diagnostic rather than
silently falling back to runtime evaluation. Runtime fallback is allowed only
when the source form explicitly permits it and the resulting runtime effects are
legal for the profile.

## Build Effects

Build effects are effect names in the compile-time effect domain. The base set
is:

- `:build/read-file` for reading declared files.
- `:build/write-artifact` for writing generated artifacts inside the build
  output graph.
- `:build/env` for reading declared environment variables.
- `:build/network` for network access.
- `:build/exec` for running external commands.
- `:build/time` for reading a clock.
- `:build/random` for nondeterministic bytes.
- `:build/model-call` for AI model calls.
- `:build/tool-call` for tool invocations mediated by a provider.
- `:build/target-probe` for querying target capabilities.
- `:compiler/read-ir` for inspecting compiler IR during a declared compile-time
  service.
- `:compiler/write-ir` for transforming compiler IR during a declared
  compile-time service.
- `:build/package-index` for dependency resolution and registry queries.

Each build effect requires:

- A source span or manifest entry that requested the effect.
- A package identity and namespace identity.
- A capability provider identity.
- A grant with scope, lifetime, and profile constraints.
- A trace record containing inputs, outputs, tool identity, result digest, and
  replay policy.

Build effects compose with ordinary effects. A compile-time function that reads
a schema file and emits parser code has a build effect; the generated parser may
also have runtime effects. The two sets must remain distinct in artifacts.

## Capability and Grant Rules

Compile-time authority is never ambient. The compiler must reject a build effect
unless a grant authorizes it. Grants may come from:

- The package manifest.
- The workspace policy.
- The command line or build invocation.
- A signed lockfile.
- A trusted local profile for interactive development.
- A capability provider configured by the build system.

The grant must name the allowed effect, scope, and principal. For file access,
the scope is a path set or content-addressed input set. For environment access,
the scope is a named key set. For network access, the scope is a host, protocol,
method, and replay policy. For model and tool calls, the scope includes provider,
model or tool id, input class, output class, cost limit, and retention policy.

Secrets must not be embedded in generated source, diagnostics, stable cache
keys, or public provenance. A trace may record that a secret named `API_TOKEN`
was read, but it must not record the secret value unless a private artifact
store explicitly accepts secret material.

## Hermetic Builds

Hermetic mode is the required mode for release builds, conformance suites,
reproducible package publication, and safety-critical profiles. In hermetic
mode:

- Undeclared filesystem reads are rejected.
- Environment reads are rejected unless the key is declared and its value policy
  is explicit.
- Network, shell execution, model calls, and tool calls are rejected unless a
  replayable provider or pinned result is configured.
- Clock and randomness effects must provide recorded values or deterministic
  seeds.
- Target probes must be satisfied from declared target manifests.
- Generated forms must include content hashes of their compile-time inputs.

The same source, compiler version, lockfile, target manifest, build grants, and
declared inputs must produce byte-for-byte identical typed core artifacts. Native
object files may vary by toolchain metadata only when the backend artifact marks
the non-semantic field and the conformance suite accepts the target-specific
exception.

## Replay and Caching

Compile-time evaluation produces a cache key from:

- Source hashes and source spans.
- Macro and compiler extension identities.
- Compile-time function hashes or versioned artifact ids.
- Package lockfile entries.
- Build grants.
- Declared file input hashes.
- Declared environment value digests or redacted presence markers.
- Target and profile manifests.
- Compiler version and enabled language facets.
- Replay records for nondeterministic effects.

A cached compile-time result may be reused only when its key matches and its
trace is still legal under the current policy. A stricter policy may invalidate
otherwise identical cache entries. A looser policy must not bless an entry that
was created by hidden effects.

## Generated Code Contract

Generated code is never exempt from language rules. The compiler must validate:

- The generated syntax is well-formed.
- Hygiene and lexical bindings obey `L4`.
- Public names do not collide except through declared replacement rules.
- The generated forms typecheck under `L5`.
- Runtime effects are legal under `L6`.
- Capability usage is legal under the provider contract refined by `L15`.
- Memory, concurrency, and unsafe operations satisfy the active profile.
- Diagnostics can point to both the generated form and the generating source.

Generated forms must carry a generated-origin chain. The chain identifies the
macro or extension, input syntax, source span, phase, package, version, build
effects, and output digest. When generated code fails validation, diagnostics
must include this chain rather than blaming only the expanded form.

## Compile-Time Function Eligibility

A function may run at compile time only if:

- Its definition is available to the compiler for the current phase.
- Its type is fully known or can be checked in the compile-time environment.
- Its effects are declared and legal.
- Its allocation behavior is legal for the compile-time evaluator.
- It does not capture runtime-only values.
- It does not depend on target-specific behavior unless the target is declared.
- It terminates within configured fuel or has an accepted totality proof.

Native FFI calls are not compile-time callable by default. A package may expose a
compile-time host binding only through a capability provider that records
version, ABI, input digests, output digests, failure mode, and replay policy.

## Profile Behavior

`:core` accepts only pure compile-time evaluation and generated code that lowers
to portable typed core.

`:hosted` may permit host-backed compile-time services, reflection over hosted
libraries, and dynamic code loading when grants and traces make the behavior
auditable.

`:native` may permit target-specific specialization, ABI layout queries, and
linker metadata generation. Such behavior requires target manifests and cannot
leak hosted assumptions into native artifacts.

`:kernel`, `:firmware`, and `:hardware` require hermetic builds and
must reject compile-time outputs that assume GC, reflection, ambient allocation,
host threads, wall clocks, or dynamic loading unless the profile explicitly
models that facility.

`:distributed` must record workflow schema generation, retry policies,
external-service bindings, and replay semantics. Workflow-specific surface forms
are facets and targets inside the distributed profile, not a separate profile
dimension.

`:ai` may use model calls for code generation, schema adaptation, test synthesis,
or evaluation only when model identity, prompt class, input digest, output digest,
cost limit, retention policy, and `:ai/human-review` policy are recorded.

`:meta` may inspect and transform compiler IR, but it must not bypass profile,
type, effect, safety, or artifact validation.

## Artifact Record

The compiler emits a compile-time evaluation record for every namespace that
uses compile-time execution. The record contains:

- Namespace, package, profile, target, compiler version, and language facets.
- Ordered compile-time evaluation events.
- Macro expansion events and generated-origin chains.
- Constant table entries with stable representations.
- Build effect events with grants and providers.
- File input digests and output artifact digests.
- Environment key reads with redaction policy.
- External command, tool, model, and network result digests.
- Nondeterminism replay records.
- Cache keys and cache reuse decisions.
- Diagnostics emitted during compile-time execution.

The record is part of the build artifact graph. Release builds must be able to
ship or archive it according to package policy. Safety-critical profiles may
require the record to be signed.

## Diagnostics

Compile-time evaluation diagnostics use `L12` identifiers:

- `L12-PURE-EFFECT` when pure constant evaluation attempts a build effect.
- `L12-BUILD-GRANT` when no grant authorizes a requested build effect.
- `L12-HERMETIC-INPUT` when hermetic mode observes undeclared input.
- `L12-NONDETERMINISM` when time, randomness, network, model, or tool output is
  used without replay policy.
- `L12-CONST-REPRESENTATION` when a compile-time value cannot be represented in
  the target artifact.
- `L12-GENERATED-ILLEGAL` when generated code fails syntax, type, effect,
  capability, memory, or safety validation.
- `L12-PHASE-CAPTURE` when compile-time code captures a runtime-only value.
- `L12-CACHE-UNSAFE` when a cached result was created under incompatible policy.
- `L12-SECRET-LEAK` when a secret value would be embedded in generated output,
  diagnostics, or public provenance.
- `L12-FUEL` when compile-time evaluation exceeds deterministic fuel.

Each diagnostic must name the active phase, profile, target, source span,
generated-origin chain when present, requested effect, relevant grant, and
remediation path.

## Rejected Designs

Gravity rejects implicit host scripting during compilation. A macro that reads
the user's home directory, calls a shell command, or queries a service without a
grant is invalid even when the host language would permit it.

Gravity rejects untracked code generation. Generated source without provenance
cannot participate in conformance, audit, self-hosting, or safety review.

Gravity rejects nondeterministic release builds. Time, randomness, model output,
network responses, package registries, and tool calls must be pinned or replayed.

Gravity rejects compile-time escape hatches that bypass type, effect, capability,
memory, or safety checks. Extensions may create syntax and artifacts; they may
not declare themselves correct by fiat.

Gravity rejects profile leakage. Hosted compile-time conveniences must not cause
native, embedded, hardware, or safety-critical artifacts to depend on hosted
runtime behavior unless that behavior is modeled by the target profile.

## Conformance Criteria

A conforming compiler must demonstrate:

- Pure compile-time evaluation of arithmetic, data construction, pattern
  matching, and layout queries.
- Rejection of undeclared file, environment, network, shell, clock, random,
  model, tool, and target-probe effects.
- Hermetic replay with stable typed core artifacts for identical declared inputs.
- Generated-code validation with diagnostics that include generation provenance.
- Cache invalidation when build grants, target manifests, source hashes, or
  replay records change.
- Secret redaction in diagnostics and public artifact records.
- Profile-specific rejection for hosted-only compile-time behavior in constrained
  profiles.
- Acceptance of authorized compile-time schema loading and generated protocol
  implementations when all effects and artifacts are declared.
