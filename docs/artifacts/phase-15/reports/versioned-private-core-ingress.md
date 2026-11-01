# Versioned private Core ingress prerequisite

## Purpose and ownership

OWN-06 requires the actual selected Reader program, Reader result, authenticated
envelope and expression binding to survive into fresh Core construction. The
preserved constructor needs versioned private ingress arities absent from the
PR601 baseline. This finite Native source increment restores only that ingress
dependency. It does not expose a new public constructor or report overload.

The baseline is PR601 head 5efe6d8928679bd4551980de80329879d934aaa7.
The ingress function is restored byte-identically from preserved Native
f66d9ff4554815b89ee7d51ee73b7961f0c33a44 in existing component 00716, with
its forward declarations. Surrounding functions, including the existing private
zero-arity stage2-plan helper, remain unchanged. No component registration or
source pins change. No active workstream path reservation names 00716/00723.

## Behavior and artifacts

Legacy two/three-argument ingress retains its existing route. Four/five-argument
ingress carries the explicit source policy version and selected Reader binding.
The strict source-contract selector rejects invalid versions before front-end
work. Versions 2/3 require the selected current Reader invocation path.

The actual project-context C2/C3 producer is still called once. Existing literal,
front-end, module and plan projections, parameter handling and carrier validation
retain their order. Only after those steps does versioned ingress invoke the
existing Reader-envelope policy owner with the unprojected producer products,
selected Reader path/binding and version. Its returned receipt is retained.
Upstream or envelope refusal propagates without a successful receipt.

Invocation path selection compares canonical paths. The downstream envelope
owner still requires its exact canonical selected-program path; a path alias
that passes invocation selection does not acquire envelope authority.

The now-used private envelope helper in 00723 no longer needs its targeted
unused-private annotation. The expression metadata projection annotation remains
until fresh construction actually references that separate helper.

## Conformance evidence and limits

`tools/c6_private_ingress_test.clj` runs 5 tests / 67 assertions after the
existing relative-policy 4/54 and Reader-policy 6/64 controls. Ingress routing,
source-version selection, path checks and the full Reader policy helper are
actual code. The project context, C2/C3 producer, parsing, compiler, projections,
emitter, carrier-validation result and external envelope verifier are declared
oracles; products and diagnostic transport are synthetic. No Reader, expression
program, Core producer or authentication operation executes. Bootstrap remains
unloaded.

Controls cover exactly one front-end call with original arguments, zero-arity
and parameter plan routing, receipt placement after carrier validation, native
version/path rejection before work, upstream refusal, forged Reader commitments,
path alias refusal at the envelope owner, and unchanged legacy receipt absence.
These tests establish source mechanism behavior, not successful actual product
production, authentication or admission.

## Remaining acceptance

Versioned fresh C6 construction/receipt completion and public report overloads
remain, followed by genuine lexical C11 authentication/builder/replay. Exact
source review, applicable CI, current-main composition and coordinator
integration remain separate requirements. Prior source acceptance cannot accept
this new delta; identical or tree-equivalent prerequisites must not be replayed.

PR453's original-v2 evidence, PR574's unresolved final independent disposition,
PR446's unknown registration and operation-specific holds are unchanged. No
held runtime/controller operation or denied message is retried. This increment
claims no PR552 completion, OWN-06 closure, v3 product acceptance, backend,
release or self-hosting authority.
