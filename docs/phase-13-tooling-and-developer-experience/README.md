# Phase 13 - Tooling and Developer Experience

Defines the command line, REPL, editor, debugger, registry, inspector, profiler, and AI-assisted tools.

## Documents

- [177 T1 - CLI Specification](177-t1-cli-specification.md)
- [178 T2 - REPL UX Specification](178-t2-repl-ux-specification.md)
- [179 T3 - Formatter Specification](179-t3-formatter-specification.md)
- [180 T4 - Linter Specification](180-t4-linter-specification.md)
- [181 T5 - Language Server Protocol Design](181-t5-language-server-protocol-design.md)
- [182 T6 - Debugger Design](182-t6-debugger-design.md)
- [183 T7 - Documentation Generator Design](183-t7-documentation-generator-design.md)
- [184 T8 - Dev Server Design](184-t8-dev-server-design.md)
- [185 T9 - Package Registry UX Specification](185-t9-package-registry-ux-specification.md)
- [186 T10 - Compiler Explorer and IR Inspector Design](186-t10-compiler-explorer-and-ir-inspector-design.md)
- [187 T11 - Profiler and Performance Inspector Design](187-t11-profiler-and-performance-inspector-design.md)
- [188 T12 - Safety Audit Explorer Design](188-t12-safety-audit-explorer-design.md)
- [189 T13 - AI-Assisted Development Tooling Specification](189-t13-ai-assisted-development-tooling-specification.md)

## Phase Authoring Contract

- Phase 13 documents must cite upstream language, safety, profile, compiler, package, and conformance contracts instead of redefining them.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
