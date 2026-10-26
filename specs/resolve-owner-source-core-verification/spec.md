# Resolve owner source-to-Core verification

Finite task: `resolve/owner-source-core-verification`.
Owner role: compiler Resolve maintainer. Independent review role: compiler evidence reviewer.

## Outcome

Compile the complete `resolution.gravity` owner source through genuine public
Reader, Syntax, Macro, Resolve and Core APIs, then independently replay the
result and reject genuine binding/Core semantic mutations without accepting a
supplied verification report as authority.

This is one bounded verification task, not a new component or global completion
denominator. It contributes evidence to [Resolve integration](../../docs/component-roadmaps/resolve.md#current-evidence-and-remaining-work)
and [Core preservation and verification](../../docs/component-roadmaps/core.md#current-evidence-and-remaining-work),
and to the semantic portion of [SH07-C16](../../docs/self-hosting-sh07-completion-plan.md).
Their existing acceptance rows remain open; this package does not copy them.

## Acceptance

- Every source form, function, definition and declared export is covered in order.
- Complete captured artifacts round-trip and retain exact source/dependency identities.
- Generic fresh C5 and Core verification pass with original public APIs.
- A genuine binding-name mutation is rejected with current outer identity.
- A genuine Core Bool mutation with coherent recomputed identity is rejected by
  semantic replay while fresh upstream, request, template and stored-proof checks pass.
- An independent reviewer accepts the actual execution and capture evidence.

## Limits

The frozen cohort is `1927aaee826766ea3bf61af609b2333ccd3b5c43`, not current main.
No execution of every compiled function, C7-C10, full SH07, backend, seedless
rebuild, language completion or unrestricted resource-safety claim follows.
The normative [Resolve](../../docs/phase-06-compiler-architecture/084-c5-name-resolution-and-namespace-analyzer-design.md)
and [Core](../../docs/phase-06-compiler-architecture/085-c6-ast-and-core-lowering-design.md)
contracts remain unchanged.
