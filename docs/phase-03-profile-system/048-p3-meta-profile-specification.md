# P3 - :meta Profile Specification

Sequence: 48
Phase: 3 - Profile System
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The `:meta` profile is for code that participates in compilation: macros,
analyzers, compiler passes, code generators, facet expanders, proof tools,
documentation generators, language servers, package checks, and self-hosting
compiler components. `:meta` code may inspect and construct syntax, typed core,
MIR, EFIR, diagnostics, artifacts, and manifests, but it must not bypass the
target profile of the code it generates.

The profile exists to make programmable compilation powerful without making build
authority ambient or unreviewable.

## Requirements

- `:meta` code may inspect and construct compiler artifacts only through
  declared compiler capabilities.
- Build effects must be declared and checked under `L12`.
- Hermetic builds deny network, shell execution, undeclared file access, model
  calls, and tool calls unless replayable grants are present.
- Generated code must be checked against the target namespace profile.
- Compiler passes must declare input IR, output IR, preserved facts, invalidated
  facts, and artifact outputs.
- `:meta` code must preserve source spans, generated-origin chains, type/effect
  facts, profile metadata, and safety evidence.
- Runtime authority must not leak into compile-time execution.

## Dependencies

- `P1` defines common profile validation.
- `L4`, `L12`, and `L16` define macros and compile-time evaluation.
- `L14` defines facet expansion and domain IR.
- `L15` defines compiler and build providers.
- `SAFE12` defines macro safety.
- `SAFE15` defines proof and certificate preservation.
- Phase 6 compiler documents define IR and pass contracts.
- Phase 12 package documents define build and artifact policy.

## Outputs and Artifacts

- `:meta` profile manifest.
- Macro expansion trace.
- Compiler pass manifest.
- Build effect log.
- Generated-code profile report.
- IR access capability records.
- Pass fact preservation and invalidation records.
- Hermetic replay records.
- Meta conformance results.

## Allowed Behavior

`:meta` allows:

- Syntax object construction and inspection.
- Macro definition and expansion.
- Compiler pass implementation.
- Analyzer and lint implementation.
- Facet expansion and domain IR generation.
- Typed core, MIR, EFIR, and diagnostic inspection when granted.
- Artifact manifest construction.
- Pure compile-time evaluation.
- Declared build file reads.
- Declared environment reads.
- Replayable model or tool calls when policy grants them.

The permission to inspect compiler structures is scoped. A pass may read only the
IR levels and namespaces authorized by its provider grant.

## Forbidden or Checked Behavior

`:meta` rejects by default:

- Runtime-only capability use.
- Ambient host authority.
- Undeclared filesystem, environment, network, shell, model, or tool effects.
- Raw memory and FFI unless a compiler implementation profile explicitly allows
  an audited provider boundary.
- Generated unsafe code without `SAFE6` metadata.
- Compiler state mutation outside declared pass outputs.
- Dropping source spans or generated-origin chains.
- Treating generated code as already safe.

Network, shell, model, and tool effects are checked behavior only when granted
and replayable under the build policy.

## Compiler Pass Contract

A pass declares:

```clojure
(defpass inline-small-functions
  {:profile :meta
   :input :gravity/mir
   :output :gravity/mir
   :requires #{:types :effects :call-graph}
   :preserves #{:source-spans :types :effects}
   :invalidates #{:range-proofs}
   :effects #{:compiler/plugin :compiler/read-ir :compiler/write-ir}
   :capabilities #{:compiler/ir-transform}}
  [module]
  ...)
```

The compiler validates the declaration before running the pass. Pass output is
rechecked according to invalidation rules.

## Generated Code

Generated code has two profiles:

- The generator runs in `:meta`.
- The generated namespace is checked in its declared target profile.

The generator cannot legalize behavior forbidden by the target profile. If a
macro in `:meta` emits `:native` FFI into a `:core` namespace, the expansion is
rejected as a `P3-GENERATED-PROFILE` violation.

## Hermeticity

In hermetic mode, `:meta` evaluation may use only declared inputs:

- Source files in the package graph.
- Lockfile entries.
- Declared build files.
- Target manifests.
- Replay records.
- Approved provider outputs.

Unrecorded time, randomness, host filesystem, network, shell, model, and tool
access are rejected.

## Memory and Runtime

The compiler implementation may allocate internally, but `:meta` source does not
gain arbitrary runtime authority. Compiler-owned allocation is not observable in
the generated program. A self-hosted compiler may choose hosted or native
implementation profiles for its executable, but source namespaces that perform
compiler work still obey `:meta` build-effect and artifact rules.

## Diagnostics

Meta diagnostics use `P3` identifiers:

- `P3-BUILD-EFFECT` for undeclared compile-time effects.
- `P3-HERMETIC` for non-replayable build authority in hermetic mode.
- `P3-COMPILER-CAPABILITY` for unauthorized IR or artifact access.
- `P3-PASS-CONTRACT` for missing or invalid pass declarations.
- `P3-FACT-INVALIDATION` for pass output that drops or misreports facts.
- `P3-GENERATED-PROFILE` for generated code illegal in the target profile.
- `P3-GENERATED-SAFETY` for generated code lacking safety provenance.
- `P3-PHASE` for runtime values captured into compile-time execution.
- `P3-SOURCE-MAP` for missing source or generated-origin mapping.

Diagnostics must include generator or pass id, source span, generated-origin
chain, active build policy, target profile, requested build effect, and provider
grant.

## Rejected Designs

Gravity rejects compiler plugins with ambient host authority.

Gravity rejects passes that mutate global compiler state without declared
outputs.

Gravity rejects generated code as a safety bypass.

Gravity rejects build effects hidden behind macro expansion.

Gravity rejects self-hosting as an excuse to skip artifact provenance.

## Conformance Criteria

A conforming `:meta` implementation must demonstrate:

- Macro and pass execution with declared build effects.
- Hermetic rejection of undeclared file, environment, network, shell, model, and
  tool access.
- Compiler IR capability checks.
- Pass preservation and invalidation artifacts.
- Generated code checked in the target profile.
- Source span and generated-origin preservation.
- Replay records for nondeterministic compile-time work.
- Rejection of runtime value capture during compile-time execution.
