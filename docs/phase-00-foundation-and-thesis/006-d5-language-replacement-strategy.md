# D5 - Language Replacement Strategy

Sequence: 6
Phase: 0 - Foundation and Thesis
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

D5 defines how Gravity replaces existing language and platform slices without making broad, unearned claims. Replacement is measured by executable parity, artifact parity, safety parity, tooling parity, and migration feasibility.

Gravity does not replace an incumbent by mimicking its surface syntax. It replaces an incumbent slice when Gravity can express the same domain concepts, enforce stronger or comparable safety, emit the needed artifact family, interoperate during migration, and pass conformance checks against realistic examples.

## Replacement Strategy

Replacement proceeds in five stages.

1. Interop: Gravity calls, generates, or wraps incumbent artifacts while preserving types, effects, capabilities, and provenance.
2. Facade: Gravity exposes a stable API over incumbent behavior and records the boundary as an artifact.
3. Native slice: Gravity implements a narrow real slice in its own semantics and emits incumbent-compatible artifacts when needed.
4. Parity: Gravity matches required behavior, performance, diagnostics, packaging, safety, and tooling for a larger slice.
5. Supersession: Gravity becomes the source of truth for the slice, with the incumbent reduced to a target, backend, runtime, or compatibility layer.

No stage may erase D0 commitments. Interop is acceptable; hidden semantics are not.

## Incumbent Mapping

| Incumbent family | Gravity replacement basis | Required evidence |
| --- | --- | --- |
| C | `:native`, `:kernel`, layout control, FFI, explicit memory, unsafe audit | ABI fixtures, native artifacts, memory-safety diagnostics, performance baseline |
| Rust/Zig | ownership, borrowing, regions, linear resources, no hidden allocation | borrow fixtures, region escape checks, unsafe island audits |
| Clojure/Lisp | homoiconicity, macros, persistent data, namespaces, protocols, REPL | macro expansion traces, hosted artifacts, idiom-parity examples without Clojure source compatibility claims |
| Java/Kotlin/C# | hosted runtime integration, typed APIs, packages, tooling | JVM/CLR-style artifact mapping, exception/null normalization, package metadata |
| JavaScript/TypeScript | hosted/browser target, UI facets, schemas, package interop | JS/Wasm artifacts, source maps, DOM capability policy, type/schema generation |
| Python | scripting, data workflows, AI/provider ecosystem, REPL | package interop, typed data schemas, AI/tool effects, notebook/REPL fixtures |
| SQL | schema IR, query IR, migrations, typed rows | generated SQL, migration proof, query plan, database capability manifest |
| Verilog/VHDL | `:hardware`, HDL/state-machine IR, bounded logic | HDL artifact, clock/reset records, synthesis-compatible fixtures |
| Workflow engines | `:distributed`, workflow graph IR, replay, compensation | workflow graph, event log schema, retry/compensation fixtures |
| AI frameworks | `:ai`, agents, prompts, tools, memory, evals, policy | agent manifest, prompt artifact, structured output schema, eval report |
| Shell/automation | hosted scripting with process/file effects | process capability policy, hermetic build rejection fixtures |

The table is not a compatibility promise. It identifies what evidence must exist before replacement can be claimed.

## Replacement Slice Record

Each replacement effort uses a record:

```clojure
{:replacement-id "native-cli-c-subset"
 :incumbent [:c :posix-cli]
 :gravity-profiles [:native]
 :targets [:llvm-x86_64-linux :c99]
 :supported-slice [:args :stdout :filesystem-read :owned-buffers]
 :interop-boundaries [:libc-stdio]
 :effects [:io/write :filesystem/read :ffi/call]
 :capabilities [:io/write :filesystem/read :ffi/call]
 :accepted-fixtures ["cli-copy.gravity" "owned-buffer.gravity"]
 :rejected-fixtures ["unchecked-ptr.gravity" "undeclared-file-read.gravity"]
 :artifacts [:binary :c-header :abi-manifest :safety-report]
 :exit-gates [:behavior-parity :diagnostics :profile-legality :performance-baseline]}
```

The record is stored in the artifact graph when the replacement slice is tested or released.

## Interop Rules

Interop is a migration tool, not a loophole.

FFI interop must declare ABI, ownership transfer, lifetime, nullability, thread behavior, panic/exception behavior, allocation responsibility, and unsafe audit status.

Hosted interop must normalize host nulls, exceptions, reflection, dynamic loading, promises, callbacks, and object identity into Gravity types, effects, and errors.

Package interop must record provenance, licenses, signatures, SBOM data, capabilities, target compatibility, and reproducibility limits.

AI/provider interop must record model identity, tool schemas, memory policy, structured outputs, prompt artifacts, nondeterminism, and evaluation evidence.

Database interop must record schema identity, migration version, query parameterization, transaction mode, row types, and capability requirements.

## Migration Path

A migration from an incumbent into Gravity should follow this order:

1. inventory incumbent effects, capabilities, data schemas, unsafe operations, runtime services, and package dependencies,
2. define the Gravity profile and target set,
3. write source schemas for data boundaries,
4. wrap incumbent APIs with typed facades,
5. replace pure logic first,
6. replace effectful logic behind explicit capabilities,
7. replace unsafe or low-level logic only after audit records and negative fixtures exist,
8. retire interop boundaries only when conformance and performance evidence justify it.

Migration tools must preserve source provenance. Generated Gravity code is not accepted as safe simply because it was generated.

## Requirements

- A replacement claim must specify incumbent family, Gravity profiles, targets, supported slice, unsupported slice, interop boundaries, artifacts, tests, and performance expectations.
- Replacement cannot depend on hidden host services or ambient authority.
- Replacement cannot bypass type, effect, profile, capability, memory, safety, package, or artifact checks.
- Replacement must include negative fixtures for the incumbent behaviors Gravity intentionally rejects.
- Replacement must preserve or improve safety evidence relative to the incumbent slice.
- Replacement must name where the incumbent remains better or remains delegated.

## Dependencies

D5 depends on `D0`, `D1`, `D3`, and `D4`.

Detailed replacement evidence is provided by later domain, backend, runtime, package, testing, standard-library, and governance documents. D5 defines claim discipline; it does not itself prove every domain.

## Outputs and Artifacts

D5 requires:

- replacement slice records,
- incumbent capability inventories,
- interop boundary manifests,
- accepted and rejected fixture suites,
- generated target artifacts,
- behavior parity reports,
- performance baselines,
- safety and unsafe audit evidence,
- migration notes.

## Rejected Claims

The following claims are rejected:

- "Gravity replaces C" without naming a C slice, ABI boundary, memory model, and native artifact evidence.
- "Gravity replaces JavaScript" without browser/host artifact evidence, source maps, and DOM capability policy.
- "Gravity replaces Python for AI" without provider, tool, memory, schema, eval, and policy artifacts.
- "Gravity replaces SQL" without schema migration, parameterized queries, typed rows, and database capability records.
- "Gravity replaces HDL" without bounded state-machine/clock/reset artifacts.
- "Gravity replaces workflow engines" without replay, retry, compensation, and event log artifacts.

## Diagnostics

- `D5-OVERBROAD-CLAIM`: replacement claim names an incumbent family but no supported slice.
- `D5-INTEROP-HIDDEN`: interop boundary lacks type, effect, capability, ownership, or provenance metadata.
- `D5-PARITY-MISSING`: replacement record lacks behavior, safety, performance, or tooling evidence.
- `D5-UNSUPPORTED-UNSTATED`: migration docs fail to name unsupported incumbent behavior.
- `D5-UNSAFE-MIGRATION`: generated or migrated code contains unsafe behavior without an unsafe island.

## Conformance Criteria

- Every replacement claim has a replacement slice record.
- Every replacement slice has accepted and rejected fixtures.
- Every interop boundary is represented as a typed artifact.
- Every emitted incumbent-compatible artifact includes provenance.
- Every unsafe migration point has an audit record.
- Replacement docs distinguish implemented, delegated, experimental, and unsupported behavior.

## Change Control

Expanding replacement scope requires new fixtures, artifacts, and parity evidence. Governance may reject marketing language or documentation that claims replacement beyond the recorded slice evidence.
