# STD8 - IO and Filesystem Library Specification

Sequence: 218
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.io` and `gravity.io.fs` define byte streams, text streams, handles, filesystem paths, directory operations, metadata, atomic file updates, and resource-safe IO.
They expose IO as effectful and capability-gated behavior.
No file, handle, standard stream, or host path is available by ambient authority.

Filesystem behavior varies sharply across profiles and hosts.
The library therefore describes capabilities, path roots, symlink policy, encoding policy, resource lifetime, atomicity, durability, and error mapping.
Hosted implementations may delegate to a platform, but they must preserve Gravity diagnostics and manifest evidence.

## Requirements

- Filesystem and stream operations MUST declare read, write, seek, metadata, watch, delete, or execute-like effects.
- Every filesystem operation MUST require an in-scope capability naming allowed roots and operations.
- Paths MUST be typed values, not unchecked strings.
- Path normalization MUST declare platform and symlink policy.
- File handles MUST be linear resources or scoped resources with deterministic close semantics.
- Text IO MUST state encoding and decoding error policy from STD4.
- Atomic writes MUST state temp path, rename, flush, and durability guarantees.
- Directory traversal MUST state recursion, symlink, permission, and cycle policy.
- `:distributed` workflow IO MUST be recorded, idempotent, or isolated in activity steps.
- Shell execution and process spawning are not part of filesystem IO; those belong to STD16.

## Module Surface

- Streams: `input-stream`, `output-stream`, `read`, `read-bytes`, `read-text`, `write`, `write-bytes`, `write-text`, `flush`, and `close`.
- Handles: `open`, `open-read`, `open-write`, `with-open`, `seek`, `position`, `truncate`, and `sync`.
- Paths: `path`, `join`, `normalize`, `parent`, `filename`, `extension`, `relative?`, `absolute?`, and `within-root?`.
- Files: `exists?`, `metadata`, `copy`, `move`, `delete`, `atomic-write`, `create-dir`, and `list`.
- Directory traversal: `walk`, `watch`, `glob`, and `with-symlink-policy`.
- Capabilities: `filesystem-capability`, `read-root`, `write-root`, `temporary-root`, and `path-policy`.
- Error values: permission denied, not found, already exists, interrupted, invalid path, encoding failure, and durability failure.

## Dependencies

- `L5`, `L6`, and `L11` for effects, capabilities, and resource ownership.
- `SAFE1`, `SAFE5`, `SAFE6`, `SAFE7`, `SAFE10`, and `SAFE15` for resources, unsafe boundaries, FFI, capability security, and proof-carrying wrappers.
- `P7`, `P5`, `P4`, `P9`, and `P10` for kernel, native, hosted, distributed, and AI profile constraints.
- `R3`, `R4`, and `R7` for native, hosted, and distributed runtime services.
- `STD4` for encoding and text policy.
- `STD6` for file handles as linear resources.
- `STD12` and `STD13` for workflow and AI restrictions on IO side effects.
- `PKG6`, `PKG8`, and `PKG10` for capability manifests, safety metadata, and provenance.

## Example

```clojure
(ns sample.files
  (:require [gravity.io.fs :as fs])
  (:profile :hosted))

(defn load-config [cap name]
  (fs/read-text cap (fs/join (fs/read-root cap) name) {:encoding :utf-8}))
```

The caller supplies a filesystem capability.
The path is resolved inside the capability root.
The encoding policy is explicit.

## Profile Availability

- `:core`, `:hardware`, and `:formal` do not receive ambient filesystem APIs.
- `:firmware` may receive device-specific streams only through firmware capability modules.
- `:kernel` may receive VFS or device IO only through kernel capabilities and unsafe audits.
- `:native` receives stream, file, path, directory, and watcher APIs under explicit capabilities.
- `:hosted` may delegate to host filesystem APIs with path and error mapping records.
- `:distributed` receives workflow-safe IO wrappers that record, isolate, or reject side effects during replay.
- `:ai` may use filesystem tools only through Phase 11 tool and policy gates.
- `:meta` may read source and artifact files only through compiler capabilities.

## Outputs and Artifacts

- IO and filesystem module manifests with effect and capability metadata.
- Path policy artifacts describing roots, symlink handling, normalization, and platform rules.
- Resource lifetime records for opened handles.
- Atomic write and durability fixtures.
- Negative fixtures for ambient access, path traversal, missing close, missing encoding policy, and replay-unsafe IO.
- Host delegation records for path, permission, metadata, and error mapping.
- Security audit artifacts for privileged filesystem adapters.

## Diagnostics

- `STD8001` when filesystem access lacks a capability.
- `STD8002` when a path escapes its declared root.
- `STD8003` when text IO omits encoding or decode policy.
- `STD8004` when a file handle leaks or is used after close.
- `STD8005` when directory traversal lacks symlink or recursion policy.
- `STD8006` when atomic write guarantees are claimed without required flush or rename evidence.
- `STD8007` when workflow replay observes unrecorded IO.
- `STD8008` when host filesystem delegation lacks path or error mapping artifacts.

## Conformance Criteria

- Capability fixtures allow only declared roots and operations.
- Path traversal fixtures reject escape, symlink loops, and platform normalization gaps.
- Resource fixtures prove handles close exactly once or stay inside `with-open`.
- Text IO fixtures preserve STD4 encoding and error behavior.
- Atomic write fixtures record durability guarantees and failure modes.
- Distributed workflow fixtures reject replay-unsafe IO outside recorded activities.
- Host-backed implementations emit Gravity diagnostics for common filesystem errors.
- Documentation examples compile only in profiles with explicit filesystem capabilities.
