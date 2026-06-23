# T11 - Profiler and Performance Inspector Design

Sequence: 187
Phase: 13 - Tooling and Developer Experience
Status: Manual Draft
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

This design defines profiling and performance inspection tools. The profiler
connects runtime measurements, compiler optimizations, source spans, MIR, target
artifacts, profile constraints, benchmark metadata, and safety proofs. It helps
developers understand performance without treating unsafe behavior or removed
checks as invisible.

Performance reports are artifacts that can be compared in CI.

## Measured Data

Reports may contain:

- wall time and CPU time;
- allocation counts and bytes;
- cache and branch counters where available;
- latency percentiles;
- throughput;
- energy or device counters where supported;
- model/tool cost for AI workflows;
- GC or runtime service costs;
- check elision decisions;
- specialization and layout choices.

Targets declare which measurements are valid.

## Requirements

- Profiling runs MUST identify profile, target, artifact, benchmark, environment, and compiler version.
- Performance reports MUST link samples to source or artifact positions where possible.
- Comparisons MUST use declared thresholds and benchmark policy.
- Safety check elision views MUST show proof or analysis evidence.
- Runtime overhead from profiling MUST be measured or declared unknown.
- AI and distributed performance reports MUST separate model/tool latency, retry, replay, and human-review time.
- Hardware, firmware, and GPU reports MUST record target device identity.
- Performance artifacts MUST be structured and reproducible enough for trend tracking.
- Profiling MUST not require granting unrelated runtime authority.

## Semantic Dependencies

- `PERF1` through `PERF10` define performance rules.
- `C13` defines MIR optimization passes.
- `R12` defines observability events.
- `B3`, `B8`, and other backends define target data.
- `TEST12` defines regression testing.
- `A9` defines AI eval cost and latency reporting.

## Outputs and Artifacts

The profiler emits:

- performance report;
- benchmark environment manifest;
- sample or trace records;
- comparison report;
- regression decision;
- check elision report;
- layout and specialization summaries.

## Example

```bash
gravity profile run bench/parse.grav --profile native --target x86_64-linux --format json
gravity profile compare baseline.perf current.perf --threshold perf-policy.edn
gravity profile inspect build/app --show-layout --show-check-elision
```

## Rejection Rules

- Reject performance comparisons with different benchmark identities unless policy allows normalization.
- Reject reports missing profile, target, artifact, or environment identity.
- Reject check-elision claims with no evidence.
- Reject target counters used on unsupported targets.
- Reject CI regression passes when thresholds are exceeded.
- Reject profiling sessions requiring unrelated capabilities.

## Diagnostics

- `T11001` reports missing performance identity field.
- `T11002` reports incompatible comparison inputs.
- `T11003` reports missing check-elision evidence.
- `T11004` reports unsupported target counter.
- `T11005` reports performance regression.
- `T11006` reports profiler capability overreach.

## Conformance Criteria

- A benchmark run emits structured performance artifacts.
- Comparison detects threshold regressions.
- Reports link measurements to source, MIR, or artifact positions where available.
- Check elision output cites proof or analysis evidence.
- AI workflow reports separate provider, tool, replay, and human-review costs.
- Unsupported counters are rejected for targets that cannot provide them.
- Profiling does not grant authority outside declared runtime needs.
