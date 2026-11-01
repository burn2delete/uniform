# Preserved List-construction lowering and value controls

Owner: ACC-01. Parent PR623 is exact
`c5e9fccdad293b34c290859cc3e4716870241dd6`. Historical PR502 source is
`6a4f87f013d0076b81aba9e0667650e70d28e635`.

## Scope and dependency

Restore the original complete
`current-gravity-list-construction-lowering-development-kernel` body on the
existing LC width-removal candidate. Current main still has the separate
width predicates; this child does not copy parent production onto main or
inherit parent acceptance. Every parent production and fixture byte survives.

The restored width-1025 case preserves the complete descriptor extent, emitted
instruction, runtime List result shape and identity of the original member
List. The seven invalid-type cases include unknown members at indices 0 and
1024, nested Never at 1024, wrong shape, an extra schema key, a nonvector member
carrier and odd Map member parity. C7 derive, emitter validation and lowering
must reject them. Two additional original wide homogeneous/Map type cases
retain positive C7/emitter validation checks.

## Exact adaptation and conservation

Only two raw substitutions adapt the complete historical body: the positive
and negative `p15-s23-lc-type-work` calls become one-argument
`p15-s23-lc-validate-types` calls. No assertion or case is dropped. All current
assertions, other test bytes, helper definitions and production remain unchanged.
Existing outer compiler budgets and their tests remain separate unresolved
work; no accounting policy is introduced or accepted here.

The census pins the complete before/after test file and original/current/
adapted intervals, all three parent production files, eight selected owner
definitions/argument lists, three upstream fixture/invocation definitions and
the existing runtime-projection helper. Historical definition differences are
explicit. Source identities do not prove transitive helper equivalence.

## Validation and acceptance

Reader checks disable evaluation, keep Bootstrap unloaded and verify the exact
raw substitutions, all other serialized definitions and original/current
assertion multisets. The runtime-projection helper is read, not called. Both
the genuine kernel and target runtime remain UNRUN; no held or denied selector
is retried. Independent source review, current-head CI, parent acceptance,
affected composition and exact integration preflight remain separate gates.

Local checks pass: language boundary 6 tests/11 assertions, canonical docs,
roadmap and staged/full-parent whitespace. Pinned clj-kondo 2025.10.23 with
cache disabled reports zero errors and identical complete 76-warning multisets
before/after, with no new findings or configuration changes. New EDN has zero
findings.

This is a finite successor for the preserved lowering/value test block. It
does not establish current-main LC support, compiler/runtime acceptance,
whole-path incorporation, ACC-01/PR502 closure, release or self-hosting.
