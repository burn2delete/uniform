# Phase 16 - Standard Library

Defines the stable libraries exposed across profiles and runtimes.

## Documents

- [211 STD1 - Standard Library Architecture](211-std1-standard-library-architecture.md)
- [212 STD2 - Core Library Specification](212-std2-core-library-specification.md)
- [213 STD3 - Collections Library Specification](213-std3-collections-library-specification.md)
- [214 STD4 - String and Text Library Specification](214-std4-string-and-text-library-specification.md)
- [215 STD5 - Numeric and Math Library Specification](215-std5-numeric-and-math-library-specification.md)
- [216 STD6 - Memory and Resource Library Specification](216-std6-memory-and-resource-library-specification.md)
- [217 STD7 - Concurrency Library Specification](217-std7-concurrency-library-specification.md)
- [218 STD8 - IO and Filesystem Library Specification](218-std8-io-and-filesystem-library-specification.md)
- [219 STD9 - Network and HTTP Library Specification](219-std9-network-and-http-library-specification.md)
- [220 STD10 - Serialization and Schema Library Specification](220-std10-serialization-and-schema-library-specification.md)
- [221 STD11 - Database and Query Library Specification](221-std11-database-and-query-library-specification.md)
- [222 STD12 - Workflow Library Specification](222-std12-workflow-library-specification.md)
- [223 STD13 - AI and Agent Library Specification](223-std13-ai-and-agent-library-specification.md)
- [224 STD14 - Testing Library Specification](224-std14-testing-library-specification.md)
- [225 STD15 - Compiler Meta-Programming Library Specification](225-std15-compiler-meta-programming-library-specification.md)
- [226 STD16 - Platform and OS Library Specification](226-std16-platform-and-os-library-specification.md)
- [227 STD17 - Hardware and Firmware Library Specification](227-std17-hardware-and-firmware-library-specification.md)
- [228 STD18 - Cryptography Library Specification](228-std18-cryptography-library-specification.md)
- [229 STD19 - UI and Application Library Specification](229-std19-ui-and-application-library-specification.md)
- [230 STD20 - Standard Library Stability Policy](230-std20-standard-library-stability-policy.md)

## Phase Authoring Contract

- Phase 16 documents must cite upstream language, safety, profile, compiler, package, and conformance contracts instead of redefining them.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
