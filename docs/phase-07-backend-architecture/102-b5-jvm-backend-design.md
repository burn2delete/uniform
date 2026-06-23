# B5 - JVM Backend Design

Sequence: 102
Phase: 7 - Backend Architecture
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The JVM backend emits class files, JARs, module descriptors, interop adapters,
debug metadata, and optional native-image configuration for hosted Gravity
programs, bootstrap compilers, server applications, libraries, and Clojure/Java
interop.

The JVM is a hosted runtime for Gravity. Java null, reflection, exceptions,
class loading, synchronization, GC, finalization, threads, and library APIs must
be represented through Gravity types, effects, capabilities, profiles, and
runtime manifests.

## Requirements

- Input must be verified MIR or verified domain IR accepted by `B1` and `C14`.
- The backend must declare classfile version, JVM version range, module system
  policy, classloader policy, packaging mode, reflection policy, and native-image
  constraints when applicable.
- Java and JVM values crossing boundaries must have nullability, type, lifetime,
  ownership/rooting, exception, and effect metadata.
- Java `null` must not enter safe Gravity except through `Option`, `Result`,
  checked wrappers, or opaque foreign values whose operations validate null.
- Java exceptions must be translated into Gravity error, panic, or effect
  channels.
- Reflection, dynamic proxies, method handles, invokedynamic, class loading,
  finalization, shutdown hooks, thread creation, and native access require
  explicit effects and capabilities.
- JVM GC may manage memory, but linear resources still require deterministic
  cleanup.
- JVM-only semantics must not leak into modules claiming `:core`, `:native`,
  `:firmware`, `:kernel`, `:hardware`, or other lower-profile contracts.

## Dependencies

- `B1` defines common backend input and emission rules.
- `C11` and `C14` define MIR/domain input and lowering manifests.
- `P4` defines hosted-profile legality; `P13` defines cross-profile boundaries.
- `L9`, `L15`, `L19`, `SAFE5`, `SAFE7`, `SAFE10`, and `SAFE11` define errors,
  capabilities, interop, resource cleanup, FFI/host boundaries, secrets, and
  taint.
- Runtime documents define managed runtime providers, schedulers, and debug
  hooks.
- Package documents define JAR, module, dependency, and native-image artifacts.

## Outputs and Artifacts

- JVM backend manifest.
- Classfile and JVM target record.
- Class files.
- JAR or module artifact.
- Java interop descriptor.
- Nullability and exception translation map.
- Reflection and dynamic-use manifest.
- Native-image configuration when selected.
- Runtime helper manifest.
- Source/debug map.
- Bootstrap/self-hosting record when applicable.
- JVM backend diagnostics.

## Backend Manifest

```clojure
{:artifact :gravity/jvm-backend-manifest
 :backend :gravity.backend/jvm
 :target {:classfile 65
          :runtime :jvm-21
          :module-system :named}
 :emits #{:class-files :jar :interop-descriptors :debug-map}
 :requires #{:hosted-profile :exception-map :nullability-map
             :runtime-provider-manifest}
 :rejects #{:unchecked-null-flow :undeclared-reflection
            :untranslated-host-exception :hidden-classloading}}
```

The manifest records enough target information for repeatable builds,
debuggers, package tools, native-image tooling, and conformance tests.

## Class and Module Model

The backend records:

- package and module names,
- generated class names,
- symbol-to-method mapping,
- static initializer behavior,
- field layout and mutability,
- interface and protocol mapping,
- closure representation,
- generic signature strategy,
- annotations emitted or suppressed,
- visibility and access flags,
- classloader assumptions.

Generated class names must be stable under incremental compilation unless a
manifest explicitly records the compatibility break.

## Value Representation

Gravity values may lower to primitives, boxed values, records/classes, arrays,
interfaces, invokedynamic call sites, opaque host objects, or runtime-managed
handles. Representation records include:

- Gravity type,
- JVM type descriptor,
- nullability,
- boxing/unboxing behavior,
- equality and hashing semantics,
- mutability,
- serialization boundary if exported,
- ownership/rooting behavior for host references.

Primitive specialization may be selected for performance, but it must preserve
generic Gravity semantics and emit bridge methods or adapters when required.

## Java Interop

Interop descriptors include:

- class, method, field, constructor, or module id,
- JVM descriptor and generic signature,
- Gravity type,
- nullability,
- exception mapping,
- thread or event-loop affinity,
- reflection requirement,
- effects and capabilities,
- taint policy for host-provided values.

Direct calls to host APIs are safe only through descriptors whose effects,
capabilities, and exception behavior are accepted by the profile and package
policy.

## Exceptions, Errors, and Panics

The backend maps:

- Gravity `Result` and checked error channels to ordinary values or declared
  exception adapters,
- Gravity panic to configured runtime helper or JVM exception type,
- Java checked and unchecked exceptions to Gravity error or panic policy,
- fatal JVM failures to diagnostic/runtime records when catchable.

Untyped Java exceptions must not cross into safe Gravity as ordinary values.
Catch-all wrappers must preserve source span and host exception identity.

## Reflection, Dynamic Loading, and Invokedynamic

Reflection, dynamic loading, method handles, invokedynamic, service loaders,
dynamic proxies, serialization frameworks, and native-image reflection metadata
must be declared. GraalVM native-image builds require a generated configuration
that corresponds exactly to the reflection and resource use accepted by the
profile.

If a package denies dynamic behavior, the backend must emit direct calls or
reject the feature.

## Runtime, Threads, and Resources

The JVM runtime manifest records:

- GC assumptions,
- scheduler and thread provider,
- synchronized/monitor use,
- atomics and `VarHandle` use,
- executor integration,
- classloader and lifecycle hooks,
- shutdown behavior,
- deterministic cleanup helpers,
- logging and tracing hooks.

Linear resources such as files, sockets, locks, transactions, and native handles
must use `SAFE5` cleanup paths. JVM finalizers or cleaners cannot be the only
release mechanism for safe Gravity resources.

## Diagnostics

JVM backend diagnostics use `B5` identifiers:

- `B5-TARGET` for unsupported classfile, JVM version, module, or packaging
  target.
- `B5-NULL` for unchecked Java null flow.
- `B5-EXCEPTION` for untranslated or undeclared host exceptions.
- `B5-REFLECTION` for reflection, proxies, method handles, or invokedynamic
  without policy and capability records.
- `B5-CLASSLOADING` for hidden or denied dynamic loading.
- `B5-INTEROP` for incomplete Java boundary descriptors.
- `B5-RESOURCE` for GC-only cleanup of linear resources.
- `B5-THREAD` for undeclared thread, monitor, scheduler, or blocking behavior.
- `B5-NATIVE-IMAGE` for configuration inconsistent with accepted dynamic use.
- `B5-PROFILE` for JVM-only behavior exported through lower-profile modules.
- `B5-MANIFEST` for incomplete JVM artifacts.

Diagnostics must include source span, MIR operation or domain anchor, JVM
symbol, classfile target, profile, missing type/effect/capability/cleanup fact,
selected adapter or rejection, and remediation.

## Rejected Designs

Gravity rejects treating Java null as a valid inhabitant of every safe type.

Gravity rejects host exceptions leaking through safe Gravity APIs.

Gravity rejects reflection and class loading hidden behind ordinary calls.

Gravity rejects relying on GC finalization for deterministic resource release.

Gravity rejects exposing JVM-only semantics through lower-profile contracts.

## Conformance Criteria

A conforming JVM backend must demonstrate:

- classfile, JAR, module, and source-map emission,
- hosted pure code and Java interop lowering,
- nullability wrapper acceptance and unchecked-null rejection,
- Java exception translation fixtures,
- reflection and dynamic-loading policy acceptance and rejection,
- native-image configuration generation when selected,
- linear resource cleanup despite JVM GC,
- thread, monitor, executor, and atomic effect records,
- profile-boundary rejection for JVM-only behavior,
- differential execution against MIR reference fixtures.
