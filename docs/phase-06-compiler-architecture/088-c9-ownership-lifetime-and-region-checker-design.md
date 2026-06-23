# C9 - Ownership, Lifetime and Region Checker Design

Sequence: 88
Phase: 6 - Compiler Architecture
Status: Manual Draft 2
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

The ownership checker implements the compiler analysis for ownership, borrowing,
lifetimes, regions, arenas, and linear resources. It consumes typed and effected
core and emits facts that safety analysis, MIR construction, optimizers,
backends, runtimes, and diagnostics must preserve.

The checker supports Lisp-style persistent immutable sharing and systems-style
explicit ownership without treating either as a hidden universal memory model.

## Requirements

- Mutable owned data must satisfy "many immutable borrows or one mutable borrow"
  over each proven alias range.
- Moved or consumed values must be unavailable afterward unless the type is
  persistent and copyable.
- Borrows must not outlive owner, region, arena generation, provider scope,
  callback, or structured task scope.
- Region and arena values must not escape their valid lifetime or generation.
- Linear resources must reach exactly one valid terminal state on every control
  path.
- Ownership transfer across tasks, actors, workflows, FFI, callbacks, tools, and
  model requests must be explicit and artifacted.
- Runtime borrow or generation checks are legal only in profiles that support
  the metadata and failure behavior.
- Unsafe lifetime extension, alias recovery, or manual resource flow must point
  to unsafe audit records.

## Dependencies

- `L10` defines memory regimes and safe memory contract.
- `SAFE3` defines ownership, borrowing, and lifetimes.
- `SAFE4` defines regions and arenas.
- `SAFE5` defines linear resource safety.
- `SAFE8` defines concurrency and data-race transfer requirements.
- `C7` provides ownership and resource types.
- `C8` provides effects and capability evidence.
- `C10` consumes checker output for safety outcomes.
- `C11` carries ownership facts into MIR.

## Outputs and Artifacts

- Ownership graph.
- Borrow graph.
- Lifetime interval map.
- Move and consume records.
- Escape-analysis report.
- Region lifetime graph.
- Arena generation graph.
- Linear resource flow graph.
- Transfer records.
- Runtime check records.
- Unsafe audit references.
- Ownership diagnostics.

## Ownership Artifact

```clojure
{:artifact :gravity/ownership-analysis
 :module module-id
 :owners {value-id owner-id}
 :moves [{:from local-a
          :to local-b
          :value value-id
          :span source-span}]
 :borrows [{:borrow-id borrow-id
            :value value-id
            :kind :immutable
            :range :whole
            :lifetime lifetime-id}]
 :regions {region-id {:scope scope-id
                      :allocations [value-id]
                      :escapes []}}
 :arenas {arena-id {:generation gen-id
                   :resets [core-node-id]}}
 :linear {resource-id {:provider provider-id
                      :state :owned
                      :terminal-paths terminal-report-id}}
 :transfers []
 :diagnostics []}
```

MIR construction must attach relevant ownership, lifetime, region, and linear
facts to allocation, load, store, call, spawn, send, receive, release, and
cleanup operations.

## Borrow Graph

Borrow graph nodes are owners, borrows, references, fields, ranges, and provider
scopes. Edges represent:

- immutable borrow,
- mutable borrow,
- field projection,
- slice or range borrow,
- move,
- consume,
- transfer,
- alias through interior mutability provider.

The checker may split fields or ranges when it proves non-overlap. If it cannot
prove non-overlap, it must treat borrows as potentially aliasing.

## Lifetime Intervals

Lifetime intervals are inferred for:

- lexical scopes,
- closures,
- stack slots,
- owned heap values,
- regions,
- arena generations,
- foreign calls and callbacks,
- provider scopes,
- structured tasks,
- device buffers,
- generated artifacts.

Each interval records start, end, owner, allowed escape destinations, and
invalidation conditions.

## Regions and Arenas

Region checks reject:

- returning region references,
- storing region references globally,
- capturing region values in longer-lived closures,
- sending region values to detached tasks,
- storing inner-region references in outer storage,
- FFI retention of region memory.

Arena checks additionally track generations. Arena reset invalidates prior
generation values. Dynamic generation checks are emitted only when profile and
provider allow them.

## Linear Resource Flow

The linear flow graph covers acquire, borrow, transfer, close, commit,
rollback, unlock, cancel, release, poison, and forget operations. The checker
computes all normal, error, panic, cancellation, and early-return paths.

A resource is accepted only when every path reaches exactly one valid terminal
state or transfers ownership to a destination with cleanup obligations.

## Escape Analysis

Escape destinations include:

- function return,
- global or static storage,
- closure capture,
- task or actor transfer,
- workflow state,
- FFI retention,
- callback retention,
- AI/tool/model request,
- generated artifact,
- region or arena storage.

Illegal escapes produce diagnostics. Legal alternatives include copy,
serialization, ownership transfer, promotion to longer-lived owner, or unsafe
boundary with audit metadata.

## Runtime Checks

Runtime ownership checks can include:

- dynamic borrow state,
- region generation,
- arena generation,
- provider-scope validity,
- resource terminal-state checks.

The checker emits explicit runtime check records with failure behavior. Profiles
that forbid metadata, allocation, or runtime failure behavior reject programs
that require these checks.

## Diagnostics

Ownership checker diagnostics use `C9` identifiers:

- `C9-USE-AFTER-MOVE` for use after move.
- `C9-USE-AFTER-CONSUME` for use after terminal consumption.
- `C9-BORROW-ESCAPE` for borrows outliving valid scope.
- `C9-MUT-ALIAS` for mutable access while aliases exist.
- `C9-MOVE-WHILE-BORROWED` for moving an owner during active borrow.
- `C9-REGION-ESCAPE` for region values escaping lifetime.
- `C9-ARENA-GENERATION` for use after arena reset.
- `C9-LINEAR-LEAK` for missing terminal operation.
- `C9-LINEAR-DOUBLE` for duplicate terminal operation.
- `C9-TRANSFER` for invalid ownership transfer.
- `C9-RUNTIME-CHECK` for required dynamic checks unavailable in profile.
- `C9-UNSAFE` for lifetime, alias, or resource behavior needing audit.

Diagnostics must include value id, owner id, borrow id, region id, arena
generation, resource id, control path, source span, generated-origin chain,
profile, target, and remediation.

## Rejected Designs

Gravity rejects implicit copying of owned mutable values to avoid move errors.

Gravity rejects region and arena safety backed by hidden GC fallback.

Gravity rejects finalizers as the only safe cleanup for linear resources.

Gravity rejects detached tasks capturing local borrows without structured
lifetime proof.

Gravity rejects generated code that duplicates linear resources or drops
ownership facts.

## Conformance Criteria

A conforming ownership checker must demonstrate:

- accepted immutable and mutable borrow patterns,
- rejected mutable aliasing, use after move, and use after consume,
- borrow escape diagnostics through return, closure, task, global, and FFI paths,
- valid and invalid region and arena examples,
- arena reset generation invalidation,
- linear resource normal, error, panic, and cancellation paths,
- ownership transfer across function, actor, task, and FFI boundaries,
- runtime check artifacts where supported,
- unsafe audit references for manual lifetime and resource handling.
