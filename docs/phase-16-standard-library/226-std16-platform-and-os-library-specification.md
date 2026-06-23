# STD16 - Platform and OS Library Specification

Sequence: 226
Phase: 16 - Standard Library
Status: Normative draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

`gravity.platform` defines process arguments, environment access, clocks, monotonic time, randomness providers, process spawning, signals, target facts, host information, terminal integration, and platform capabilities.
It is the boundary between Gravity programs and operating-system or host-platform services.
Those services are not ambient.

The platform library must keep target facts separate from host facts.
Compilation target, build host, runtime host, container environment, workflow runtime, browser host, embedded board, and kernel environment can differ.
Any code that depends on one of those facts must declare it so builds remain reproducible and profile validation can reject illegal assumptions.

## Requirements

- Platform operations MUST declare effects and capabilities for environment, arguments, time, randomness, process, signal, terminal, and host queries.
- Target facts MUST come from compiler and package artifacts, not from runtime probing unless explicitly requested.
- Environment access MUST be capability-gated and redactable.
- Process spawning MUST require explicit command, arguments, environment policy, IO policy, timeout, working directory, and capability.
- Clock APIs MUST distinguish wall time, monotonic time, CPU time, compile time, and replay-recorded time.
- Random APIs MUST distinguish deterministic PRNGs, cryptographic random providers, test seeds, and host randomness.
- Signal and terminal APIs MUST declare platform support and cleanup behavior.
- Hosted delegation MUST preserve Gravity diagnostics and provider artifacts.
- Distributed workflows MUST not read ambient time, randomness, process, or environment during replay.
- Kernel and firmware platform APIs MUST be target-specific and capability-gated.

## Module Surface

- Target facts: `target`, `target-triple`, `arch`, `os`, `abi`, `features`, and `runtime-profile`.
- Host facts: `host`, `host-os`, `host-arch`, `host-feature`, and `host-delegation`.
- Environment: `env`, `get-env`, `with-env`, `argv`, `program-name`, and `redact-env`.
- Time: `wall-clock`, `monotonic-clock`, `cpu-clock`, `sleep`, `deadline`, and `recorded-time`.
- Randomness: `prng`, `seed`, `random-bytes`, `crypto-random`, and `deterministic-random`.
- Process: `spawn`, `exec`, `wait`, `kill`, `exit-code`, `stdio-policy`, and `process-capability`.
- Signals and terminal: `signal-handler`, `with-signal`, `terminal-size`, `tty?`, and `terminal-mode`.
- Platform policy: `platform-capability`, `host-policy`, `target-manifest`, and `runtime-adapter`.

## Dependencies

- `D6`, `D8`, and `D9` for artifacts, diagnostics, and provenance.
- `L5`, `L6`, and `L11` for effects, capabilities, and resources.
- `SAFE1`, `SAFE5`, `SAFE6`, `SAFE7`, `SAFE10`, and `SAFE15` for resources, unsafe boundaries, FFI, capability security, and proof-carrying adapters.
- `P6`, `P7`, `P5`, `P4`, `P9`, and `P10` for firmware, kernel, native, hosted, distributed, and AI constraints.
- `R1` through `R12` for runtime services across profiles.
- `STD6`, `STD7`, `STD8`, `STD12`, `STD13`, and `STD18` for resources, concurrency, IO, workflows, AI, and crypto randomness.
- `PKG3`, `PKG7`, and `PKG10` for artifact identity, reproducible builds, and provenance.

## Example

```clojure
(ns sample.platform
  (:require [gravity.platform :as platform])
  (:profile :native))

(defn run-tool [cap arg]
  (platform/spawn cap
    {:command "tool"
     :args [arg]
     :env-policy :empty
     :stdio :capture
     :timeout-ms 5000}))
```

The process capability grants authority.
The environment, IO, and timeout policy are explicit.
The command is not assembled through unchecked shell text.

## Profile Availability

- `:core` receives target fact values supplied by compiler artifacts, not live host access.
- `:hardware` receives static target facts only.
- `:firmware` and `:kernel` receive platform APIs through target-specific capability modules.
- `:native` receives environment, clocks, randomness, processes, signals, and terminal APIs under capabilities.
- `:hosted` may delegate to host platform APIs with provider records.
- `:distributed` receives recorded time/randomness and activity-isolated process/environment access.
- `:ai` may use platform operations only through typed tools and `:ai/human-review` policy.
- `:meta` may inspect target and build artifacts during compilation.
- `:formal` requires modeled platform facts or explicit assumptions.

## Outputs and Artifacts

- Platform module manifest with effects, capabilities, providers, and profile matrix.
- Target manifests for compiler, package, runtime, and artifact tooling.
- Host delegation records for environment, process, time, randomness, terminal, and signal APIs.
- Redaction records for environment variables and command arguments.
- Replay records for time, randomness, process, and environment access in distributed workflows.
- Negative fixtures for ambient environment access, missing process policy, target/host confusion, and replay-unsafe reads.
- Security audit records for privileged process, signal, terminal, and platform adapters.

## Diagnostics

- `STD16001` when platform access lacks a capability.
- `STD16002` when code confuses build host, runtime host, and compilation target facts.
- `STD16003` when environment access lacks redaction or allowlist policy.
- `STD16004` when process spawning omits args, environment, stdio, timeout, or working-directory policy.
- `STD16005` when ambient wall time or randomness is used during workflow replay.
- `STD16006` when random provider kind is ambiguous.
- `STD16007` when host delegation lacks provider, version, or error mapping artifacts.
- `STD16008` when platform code is used in a profile that forbids it.

## Conformance Criteria

- Capability fixtures reject ambient environment, process, signal, terminal, and random access.
- Target/host fixtures detect incorrect dependence on the build or runtime host.
- Process fixtures enforce command, args, environment, stdio, timeout, and redaction policy.
- Time and random fixtures distinguish deterministic, recorded, wall-clock, monotonic, and cryptographic providers.
- Distributed fixtures reject replay-unsafe platform reads.
- Host delegation fixtures preserve Gravity diagnostics and provenance.
- Documentation examples compile only under profiles with the required platform capabilities.
- Security review can audit all privileged platform adapters from emitted artifacts.
