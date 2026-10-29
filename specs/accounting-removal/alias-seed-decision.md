# Alias seed authority: remaining ACC07 implementation decision

## Purpose and current boundary

Preserve the concrete remaining alias-only counterexample after the matched
definition/constructor increment at `e64260b9a18aa6137c596228be105cf1ea712aad`.
This note specifies the missing implementation decision. It changes no Gravity
typing rule, accepts no alias cycle and removes no round/work safeguard.

The owning source is
`bootstrap/gravity/src/gravity/compiler/c7_type_checker_engine.gravity`, SHA256
`ace7533c50825f102be0f5790720c913c2c6786a6d447ec33a9da64a2baf9548`.
The source is unchanged by this characterization increment.

## Exact counterexample

For the internal equations `X = Y; Y = X`, the observed binding map
`{X Integer, Y String}` becomes `{X String, Y Integer}` after one synchronous
pass and returns to its original state after the next. There is no constructor
edge, so this is not the infinite structural type detected by the existing
constructor-cycle witness.

The alias equations alone have finite solutions, including both
`{X Integer, Y Integer}` and `{X String, Y String}`. If the initial values are
mandatory equality constraints, neither satisfies both seeds. If they are
provisional observations, the differing observations alone do not establish
an inconsistent alias equation. Selecting the first seed, synthesizing a union,
clearing a seed to Unknown or reporting a type contradiction would each need
an independently justified premise. A repeated state does not supply it.

With only `X Integer` mandatory and `Y Unknown`, the same copy iteration also
oscillates, even though `{X Integer, Y Integer}` satisfies the alias equations
and that one mandatory constraint. Thus neither unequal values nor a repeated
state is by itself a contradiction test. The ungrounded all-Unknown case must
continue to supply no invented concrete type.

The new compiler-free finite countermodel retains both observed values and
enumerates those two candidate assignments. It establishes the distinction;
it does not claim to enumerate Gravity's type universe or implement a solver.
All examples are internal DATA. No authenticated source admitting lexical
self/forward references or a cyclic initialization is claimed.

## Actual implementation evidence and dependencies

In the pinned C7 source:

- `sh08-ft-declaration-seeds`, `sh08-im-seeds`,
  `sh08-ft-inference-context` and `sh08-eh-seed-bindings` contribute plain
  binding-to-type values. The artifact entry point composes them before
  calling `sh08-ft-infer-acyclic-with-context`.
- The inference loop also passes its previous `next-bindings` as the next
  round's `current-bindings`. These values represent evolving observations.
- `sh08-ft-bind-lexical-types` replaces each lexical binding with its observed
  initializer type; `sh08-ft-bind-definition-types` similarly copies the value
  type, with a separate function fallback for Unknown. These functions do not
  carry a per-seed authority/origin record alongside the type value.
- Signatures, imports and source proofs still exist elsewhere in the inputs;
  they must be reconstructed where applicable. The type map alone does not
  identify the complete authority of every SCC member, and this note does not
  assert that the full authenticated artifact lacks all such information.
- Current cycle extraction requires matched unique producers, known normal
  observations and exact supported transfers. It excludes parameters,
  intrinsic references, opaque calls and unsupported contexts. It cannot be
  reused as permission to collapse those other producers.

[C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
requires deterministic local inference and constraints with producer,
dependencies and invalidation conditions.
[L5](../../docs/phase-01-core-language/015-l5-type-system-specification.md)
distinguishes inferred and checked declared types and forbids invented dynamic
fallback. Those contracts do not authorize treating every transient observation
as an immutable equality constraint, or choosing one as an SCC representative.

## Precise implementation decision needed

Define and implement a source-bound premise for each alias SCC that separates
mandatory declarations/imported or independently grounded facts from prior
round approximations. Identify every member, alias edge, producer, source and
external incoming constraint, including absence or unresolved evidence. Reuse
existing signatures/import/proof identities where they establish that premise;
do not fabricate an input authority flag.

Then specify how mandatory constraints are checked and retained together, how
unconstrained circular components remain unresolved, and how provisional
observations may be invalidated or recomputed without discarding a mandatory
fact. A contradiction requires independently incompatible obligations; stable
equivalent observations alone are not evidence of independent grounding.
Keep pure alias components distinct from constructor-containing components.
Do not generalize alias equality through opaque or callback transfers.

The implementation must expose a retained component/constraint premise and a
stable unresolved/conflict diagnostic where appropriate. Existing resource,
return-conflict and source/fact/authentication checks retain precedence and
must survive fresh complete-result verification. No such premise or new
production diagnostic is introduced in this increment.

## Required outputs and conformance evidence

The successor needs the actual Gravity implementation and positive/negative
evidence for two-seed oscillation, compatible independently grounded seeds,
contradictory mandatory seeds, ungrounded/unresolved aliases, constructor-cross
cycles, duplicate/competing producers and opaque/callback transfers. Include
declaration-conflict precedence and coherent premise tamper rejection. Verify
the actual solver, then its affected authenticated composition before claiming
semantic convergence acceptance. Models alone do not justify deleting guards.

This increment adds a compiler-free countermodel control and an unrun Gravity
return-conflict precedence control to the existing inference test namespaces.
Prior source/model controls are retained unchanged. Exact checks and review
belong to the immutable publication receipt; this note claims no execution
result, general convergence, accounting-removal completion or release credit.
