# Feature specification: Reader-to-Core current-source invocation

Status: implementation pending | Owner: cloud frontend delegate
Input: Reader `10a1abc59a33021572e220fee4bb5f74371155f6`, tree
`830589cb02a1850bea6edf5c21870ee9ed150c83`.
Existing draft: [Reader #369](https://github.com/burn2delete/uniform/pull/369).
Planning references: SH-03, SH-04, SH-05, SH-06, SH-07; C2-C6.

## Purpose and canonical contracts

Complete one actual public current-source Reader-to-Core invocation across the
published Reader, native-canonical, authenticated-envelope, Resolver and Core
changes. This is one integration slice of the canonical
[Reader roadmap](https://github.com/burn2delete/uniform/blob/77d6e29ee780c6329acf80ae6d6563c05be21cc1/docs/component-roadmaps/reader.md),
not another roadmap or a claim of Reader component completion.

Owning contracts are [Reader design](../../docs/phase-06-compiler-architecture/081-c2-reader-implementation-design.md),
[resolution](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md),
[Core lowering](../../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md),
and the [development verification workflow](../../docs/development-verification.md).
D1 pass boundaries and D9 evidence obligations remain unchanged.

## User scenarios and acceptance

### US1 - Explicit development compilation accepts real paired source

The public Core API accepts exactly `{}` or `{:reader :development-current}`.
The explicit development selection follows host-owned invocation context through
Reader, Syntax, Macro, Resolver and Core. Real `.gravity` and `.qst` sources
produce Core artifacts accepted by the unchanged fresh verifier. Accepted
products retain actual reader revision, source identity and provenance; current
source remains development-unadmitted. Both public CLI products are checked.

### US2 - Authentication and historical defaults still fail closed

Unknown/extra API options and CLI arguments reject before target reading.
Historical default calls never infer development authority from an artifact.
Missing/wrong invocation context, changed source bytes, malformed source and
coherently rehashed Core substitutions reject at their owning boundaries.
No artifact field, process-global acceptance cache or earlier test result grants
authority to a later invocation. Actual source bytes are retained once, not
silently reread after the authenticated upstream lifetime ends.

## Scope and compatibility

Reader owns its existing descriptor and invocation forwarding adapters and
tests. Core/Resolver ownership transfers only after their explicit local pause
receipts. Native and AE producer commits are immutable dependencies, with their
owners retaining producer implementation. Shared test runners/catalogs require
parent coordination and are not edited by this slice.

No C12, 00581, Effects, denied Reader-oracle Git, advisory reviewer wake,
descendant-process inspection, migration admission, seed retirement, performance
or whole-compiler completion is included. Existing protocol/schema/diagnostic
identifiers retain their compatibility meaning.

## Evidence and outcome ownership

This delegate owns implementation and locally observed raw evidence. Reported
local-thread tests are attributed, never relabeled as cloud observations.
Deus routes independent review and owns final integration. See [evidence](evidence.md)
for exact inputs and pending results.

## Unresolved items

Core and Resolver ownership is transferred with explicit local pause receipts.
Public full-chain compatibility is not yet established for the joined tree.
The full authenticated-envelope owner source previously reached a Resolver
watchdog timeout; diagnose repeated Macro verification before any replay.
These are real integration checks, not routine permission gates.

## Implementation naming review

New tests, if needed, use semantic names such as `reader_core_invocation_test.clj`
and `gravity.reader-core-invocation-test`. Existing numbered adapter paths and
compatibility identifiers remain legacy locators, not templates for new names.
Follow the merged [semantic naming guidance](https://github.com/burn2delete/uniform/blob/77d6e29ee780c6329acf80ae6d6563c05be21cc1/.specify/semantic-names.md).
