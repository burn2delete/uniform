# Phase 14 - Testing, Verification and Conformance

Defines how the language, compiler, runtime, profiles, backends, and libraries are mechanically tested.

## Documents

- [190 TEST1 - Language Conformance Test Plan](190-test1-language-conformance-test-plan.md)
- [191 TEST2 - Compiler Test Strategy](191-test2-compiler-test-strategy.md)
- [192 TEST3 - Runtime Test Strategy](192-test3-runtime-test-strategy.md)
- [193 TEST4 - Profile Compliance Test Plan](193-test4-profile-compliance-test-plan.md)
- [194 TEST5 - Safety Conformance Test Plan](194-test5-safety-conformance-test-plan.md)
- [195 TEST6 - Backend Conformance Test Plan](195-test6-backend-conformance-test-plan.md)
- [196 TEST7 - Standard Library Test Strategy](196-test7-standard-library-test-strategy.md)
- [197 TEST8 - AI and Workflow Evaluation Strategy](197-test8-ai-and-workflow-evaluation-strategy.md)
- [198 TEST9 - Fuzzing and Property Testing Plan](198-test9-fuzzing-and-property-testing-plan.md)
- [199 TEST10 - Differential Testing Strategy](199-test10-differential-testing-strategy.md)
- [200 TEST11 - Formal Semantics and Verification Plan](200-test11-formal-semantics-and-verification-plan.md)
- [201 TEST12 - Performance Regression Test Plan](201-test12-performance-regression-test-plan.md)
- [202 TEST13 - Self-Hosting Validation Plan](202-test13-self-hosting-validation-plan.md)

## Phase Authoring Contract

- Phase 14 documents must cite upstream language, safety, profile, compiler, package, and conformance contracts instead of redefining them.
- Every document in this phase must name concrete artifacts, rejection rules, and evidence that make its contract testable.
- Profile-specific behavior must be explicit: allowed, checked, delegated, or rejected.
- Unsafe behavior must be isolated behind audit records and safe API boundaries.
- Tooling, package, test, bootstrap, standard-library, and governance documents must preserve the same effects/capabilities/artifact discipline as source code.
