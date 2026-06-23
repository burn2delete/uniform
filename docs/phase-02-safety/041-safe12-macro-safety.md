# SAFE12 - Macro Safety

Sequence: 41
Phase: 2 - Safety
Status: Manual Draft 3
Source basis: PDF pages 1-33 define the language/platform thesis, pages 73-89 define EFIR/EML and certified elementary math, pages 89-113 define safety, and pages 114-130 define the final document sequence.

## Purpose

Macros are powerful because Gravity code is data. That power must not bypass
safety. Macro expansion may generate code, introduce bindings, call compile-time
services, activate facets, or emit unsafe islands, but all generated output must
preserve source provenance and pass the same safety checks as handwritten code.

This document defines macro-specific safety obligations: hygiene, phase
separation, build effects, generated unsafe code, capability preservation, taint
preservation, and diagnostics that point to both macro definition and call site.

## Requirements

- Macro expansion must preserve source spans, generated-origin chains, hygiene
  context, metadata, profile assumptions, and safety metadata.
- A macro must not introduce undeclared unsafe operations, capabilities, build
  effects, host access, taint suppression, or profile-specific behavior.
- Generated code must pass normal type, effect, capability, memory, profile, and
  safety checks.
- Macros that intentionally generate unsafe islands must advertise that behavior
  and emit complete `SAFE6` metadata.
- Macro compile-time effects must be declared and checked under `L12`.
- Diagnostics for generated unsafe or rejected code must identify macro
  definition, macro call site, generated form, and missing safety fact.

## Dependencies

- `L1` defines syntax objects and source spans.
- `L4` defines the reference macro system.
- `L12` defines compile-time evaluation and build effects.
- `L14` defines facet interaction.
- `L16` defines alternative macro engine requirements.
- `SAFE1` defines safety outcomes.
- `SAFE6` defines unsafe islands.
- `SAFE10` defines capability authority.
- `SAFE11` defines taint propagation through generated code.

## Outputs and Artifacts

- Macro expansion trace.
- Generated-origin chain.
- Macro build effect records.
- Macro safety declaration records.
- Generated unsafe island records.
- Hygiene and capture records.
- Taint and capability propagation records.
- Macro safety diagnostics.

## Macro Safety Declaration

A macro can declare safety behavior:

```clojure
(defmacro with-buffer
  {:safety {:generates-unsafe false
            :build-effects #{}
            :capabilities #{}
            :preserves-taint true}}
  [n & body]
  `(let [buf# (buffer/new ~n)]
     ~@body))
```

When declaration and expansion disagree, expansion is rejected. If a macro can
generate unsafe code depending on input, the declaration states the conditions
and emitted unsafe metadata schema.

## Hygiene and Privileged Names

Hygiene prevents accidental or malicious capture of privileged bindings. Macro
generated identifiers must not capture:

- Unsafe helper functions.
- Capability values.
- Provider globals.
- Secret-bearing bindings.
- Raw memory operations.
- Compiler internal bindings.
- User bindings unexpectedly.

Explicit capture is allowed only through marked operations and must appear in the
expansion trace. Capture of authority-bearing bindings requires capability and
policy checks.

## Phase Separation

Macro code executes at compile time. It cannot use runtime authority unless that
authority is passed through declared compile-time providers. Runtime values,
runtime capabilities, open handles, and deployment-only secrets cannot be
captured into macro execution.

Macro build effects include file reads, environment reads, network calls, model
calls, tool calls, compiler IR access, and generated artifact writes. These
effects follow `L12` hermetic and replay rules.

## Generated Unsafe Code

Macros may generate unsafe islands only when:

- The macro declaration advertises unsafe generation.
- The call site's safety mode permits reviewed unsafe code.
- The generated unsafe island has operation, reason, owner, invariant, effects,
  capabilities, safe boundary, and review policy.
- The generated-origin chain points to the macro and call site.
- The unsafe island passes `SAFE6`.

Generated raw unsafe code without an unsafe island is rejected.

## Taint and Input Safety

Macros that transform tainted data must preserve taint facts. A macro that
builds SQL, shell commands, HTML, prompts, generated code, or tool calls must
either preserve structured parameterization or emit validator and sink metadata.
Dropping taint facts during expansion is a safety error.

## Facets and Generated IR

Facet macros may emit domain IR. Macro safety applies to:

- Generated Gravity forms.
- Facet IR.
- Generated schema validators.
- Generated FFI bindings.
- Generated workflow or agent artifacts.
- Generated hardware or compiler IR.

Facet IR must retain source mapping and safety metadata needed by downstream
checks.

## Alternative Macro Engines

Alternative macro engines must preserve these safety rules. A faster or typed
macro engine may reject unsafe expansion earlier, but it may not skip the safety
checks or emit weaker provenance than the reference macro system.

## Diagnostics

SAFE12 diagnostics use these identifiers:

- `SAFE12-GENERATED-UNSAFE` for unsafe operations generated without valid
  `SAFE6` metadata.
- `SAFE12-BUILD-EFFECT` for undeclared compile-time effects.
- `SAFE12-CAPABILITY` for generated or compile-time capability use without
  authority.
- `SAFE12-HYGIENE` for capture that violates macro hygiene or policy.
- `SAFE12-PHASE` for runtime value or runtime authority captured at compile time.
- `SAFE12-TAINT` for generated code that drops taint facts.
- `SAFE12-PROFILE` for generated code illegal in the active profile.
- `SAFE12-ORIGIN` for generated code missing source or macro provenance.
- `SAFE12-FACET` for facet macro output that bypasses facet safety checks.
- `SAFE12-ENGINE` for alternative macro engines that fail safety preservation.

Diagnostics must include macro symbol, macro definition span, call site span,
generated form span, expansion phase, active profile, build effect set, requested
capabilities, and missing safety fact.

## Rejected Designs

Gravity rejects macros as safety escape hatches.

Gravity rejects generated unsafe code without unsafe island metadata.

Gravity rejects hidden compile-time authority in macros.

Gravity rejects hygiene failures that capture privileged bindings.

Gravity rejects macro expansion that discards taint, capability, profile, or
source provenance.

## Conformance Criteria

A conforming macro safety implementation must demonstrate:

- Generated code checked by ordinary safety passes.
- Rejection of undeclared macro build effects.
- Rejection of generated unsafe operations without `SAFE6` records.
- Hygiene tests for privileged binding capture.
- Phase-separation tests for runtime value capture.
- Taint propagation through generated code.
- Facet macro source mapping and safety metadata.
- Alternative macro engine safety-equivalence tests.
