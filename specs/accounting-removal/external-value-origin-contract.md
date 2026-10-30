# ACC-07 external value-origin contract cut

## Question and resolution

Can an independent origin exist outside the unique reference-initializer
census? Yes, at its actual parameter, handler, callee, import or proof-specific
consumer. That fact does not supply a second initializer for a binding already
inside a closed pure-reference component. A source dependency must reach the
distinct origin. In a closed component all such dependencies stay internal.

This is a source-contract clarification under L2, L5 and C7, not a new rule
granting normalization. L2 specifies left-to-right immutable let initialization;
L5 distinguishes inferred facts from checked declarations; C7 requires producer,
dependency and invalidation evidence. No contract establishes an arbitrary
observation or declared restriction as an independently initialized value.

The exact original ACC-07 criterion, base/source pins, producer interfaces and
their identity coordinates are in `external-value-origin-contract.edn`. The
reviewed 700-graph/2112-assertion result is reused without rerunning that audit.
This cut does not remove the original inference accounting or complete ACC-07.

## Smallest implementation cut

`alias-closed-reference-source?` checks the existing retained member order,
single cycle, reference/initializer/binding joins, visibility and any matched
definition node. It adds no input or artifact field. A successful check makes
the existing `:normalization-reason` say
`:closed-reference-initializers-have-no-independent-value-origin`.

The factory still reconstructs the component from the original source census.
The new predicate is a local structural check over that fresh evidence, not an
independent admission or authentication service. An opaque, stale or unmatched
record cannot obtain this more precise reason. It retains the generic reason
and remains unresolved under its original source/compatibility checks.

Both paths retain `:grounding :not-established` and `:normalization :unresolved`.
No type is chosen, no initializer is replaced, and no seed or obligation is
deleted. C7-VERIFY source failures still precede scalar conflicts, which still
precede the existing annotation; resource, return and existing source failures
retain priority. Constructor growth and the inference guards remain unchanged.

## Eligible external origins and boundaries

| Origin | Required original identity and consumer | Closed-cycle consequence |
| --- | --- | --- |
| Declaration | Parameter position, function record, signature and resolved atomic type through declaration seeds/source check | A restriction; no value anchor |
| Call-entry parameter | Actual argument nodes, call, resolved callee, exact parameter binding/position, arity and all compatibility premises through call constraints | Parameter is excluded from the alias producer index |
| Catch binding | Source-checked owner, catch/handler nodes, catch syntax, error-type binding and type through handler products/seeds | Conditionally supplied catch value; no second local initializer |
| Imported function | Existing origin/export/profile admission, producer SH06/SH07 identities and complete function signature | Only the already supported import surface; no new function-value import use |
| Callee result | Actual call node/edge, resolved function and independently established return, with arguments and all other obligations checked | The initializer is a call, outside pure-reference edges |
| Recursive/HO context | Actual admitted proof, definition/function/parameter identities and its own relation | No general promotion to a cyclic-alias value origin |

Changes to bindings, definitions, source nodes, call arguments, callee/producer
artifacts, declarations, signatures, visibility or proof inputs invalidate the
corresponding origin through its existing consumer. A previous observation or
same-spelled binding cannot replace those coordinates. The new predicate does
not bypass or duplicate those consumer checks.

## Product policy alternatives

Recommend A: preserve current identities and the rule above. External origins
remain usable by their existing consumers and acyclic source dependencies;
closed reference components remain unresolved or retain a prior failure. This
requires no product semantic decision and is the only implemented local cut.

B is a separate possible product policy for profile-permitted redefinition:
an explicitly specified reference could read a distinct, already initialized
definition version. L2 permits profile-dependent runtime redefinition but does
not specify this version-read rule. Adoption would need an evaluation contract,
exact original definition/reference provenance, version identities, prior
initialization evidence, and fresh versioned dependency/compatibility checks.
There would still be one producer per binding/version. A genuinely closed
versioned cycle would remain ungrounded. This is not permission to reinterpret
the current unversioned cycle, substitute a literal, or reuse a stale value.
No B implementation or origin-input API is introduced here.

## Acceptance and limitations

Compiler-free DATA controls cover exact lexical/definition cycles, a
literal-rooted acyclic chain outside this cut, external origin coordinates,
conflicting retained seeds and provisional observations, duplicate/rebound/
stale records, visibility failures, opaque calls and forged authority flags.
They classify the source contract without evaluating the Gravity function.
The held Gravity controls cover the actual new predicate/reason and unchanged
diagnostic priorities; they are source only and remain unrun.

Future B acceptance would additionally require a legal prior-version read with
its exact initialization and reference-source witness; stale/wrong versions,
uninitialized reads and a closed versioned cycle must fail or remain unresolved.
That is a product decision and separate implementation, not a required step
before continuing other sound ACC-07 work.

Freeze this local cut for one independent affected review routed by Accounting/
Reconciliation. Do not dispatch that review or publish from this chat. Genuine
compiler/authentication/composition/runtime acceptance remains unrun/held; no
normalization, finite-growth, integration, self-hosting or release claim follows.
