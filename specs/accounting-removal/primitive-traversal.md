# Primitive C7 traversal successor

Planning: ACC-01/06/07/12. Accountable implementation owner: ACC-01,
thread `01a183dd-7208-7d33-a336-7483fd90e358`. Parent: PR613 at
`5ebff5cae276ddd93b002a28806a1c363687f7c9`, tree
`aa6a3d0d21905452338262c48dce94e2b9b8dce3`. This finite PR502 successor
preserves original primitive traversal and structural-vector work. Parent and
candidate acceptance remain distinct and pending.

## Source and contracts

[L5](../../docs/phase-01-core-language/015-l5-type-system-specification.md)
and [C7](../../docs/phase-06-compiler-architecture/086-c7-type-checker-design.md)
require independently established types, ordered facts, accurate diagnostics
and source relationships. The current
[accounting removal rule](../../docs/development-verification.md#remove-project-cost-and-budget-mechanisms)
removes width-only refusal of otherwise valid product containers.
[D9](../../docs/phase-00-foundation-and-thesis/010-d9-verifiability-and-mathematical-correctness-charter.md)
requires evidence matching the actual claimed compiler scope.

Restore exactly three complete definitions from historical PR502 head
`6a4f87f013d0076b81aba9e0667650e70d28e635`:

- `sh08-all-unique?` uses an explicit loop while retaining the supplied observed
  prefix and the existing equality/membership helper. Existing entries in the
  observed prefix are not independently revalidated as a new uniqueness premise.
- `sh08-type-nodes` uses an explicit loop while retaining ordered node typing,
  initial type/fact state, incremental facts, and the exact accumulated prefix
  plus first diagnostic on rejection. It does not process the remaining tail
  after a rejected node.
- `sh08-ft-product-vector?` still requires an existing key and a Vector value;
  its unrelated 65536-element ceiling is removed. No element-type, metadata,
  source or artifact-validity fact is inferred from this shape predicate.

All three argument lists are unchanged. Every other production definition is
unchanged. [The census](primitive-traversal-census.edn) pins all external C7
source call sites and their arities: six callers of uniqueness, one primitive
artifact caller of node typing, and no C7 source caller of the product-vector
predicate. Only the two obsolete self-recursive edges disappear. The shape
predicate is separately exercised by the original test; it does not establish
wider public artifact admission.

Twelve transitive C7 source dependencies are unchanged from both parent and
historical source, including primitive type catalogs, character validation,
child-type lookup, collection parity, fact/diagnostic construction and existing
membership. Source edges are recorded as structural observations, not semantic
reachability or evidence of a compiled execution. No backend/runtime provider,
loop implementation, authentication or language type rule is changed here.

## Preserved test and new state controls

Restore the complete original
`sh08-structural-vectors-unmetered-development-kernel`, including its original
body, comments and bytes. Its positive obligations retain 1025 independently
identified Core nodes/roots, full primitive typing, wide module declarations,
source/metadata fields, binding fields and the 70000-entry product-vector shape.
Its negative obligations retain a malformed permission at index 128, malformed
origin at 256, forged binding identity, rejected final primitive node with 1024
retained prefix facts/types, malformed last source identity, duplicate root and
missing root. B47/authenticated source remains explicitly separate.

One new `primitive-traversal-state-development-kernel` exercises retained
observed-prefix semantics, 1025 distinct values and a final duplicate, seeded
empty/accepted/rejected type/fact results, exact primitive fact fields, the first
failure diagnostic and exclusion of the unvisited tail. Missing/nonvector
product fields reject, an empty Vector accepts and a nonvector Counted object
must not have its count callback invoked. These are actual C7 kernel definitions
on normalized helper data, not a host implementation model.

Every existing parent test byte remains unchanged. No original assertion,
fixture, type, arity, diagnostic or numeric case is normalized away. The restored
numbered test/helper identities are preserved compatibility names; the new test
and artifact filenames use semantic names. Neither kernel has executed here.

## Observed checks

The pinned local Clojure checker passed all three complete historical forms,
unchanged argument lists, exact external call-site conservation, removal of only
the two self-recursive edges, unchanged production definitions, twelve unchanged
transitive C7 dependencies, complete historical test form/byte span and existing
parent test-byte conservation. Reader evaluation is disabled and Bootstrap
remains unloaded. Generator digest and complete source/test pins are in the
census; exact scripts, invocations and raw outputs remain local.

The language boundary gate passes 6 tests/11 assertions. Contextual clj-kondo
under `tools/ci/kondo`, with cache disabled, has zero errors and the same 71
warnings as the exact parent for the changed test file. Document/roadmap and
whitespace checks pass. The initial append produced an extra blank line at EOF;
adding the planned state kernel resolved that whitespace failure without
altering the original kernel. No semantic selector was run as a repair.

Routine CI on parent PR613 `5ebff5ca` and prior evidence PR569 `d565aeef`
completed seven successes/two configured skips each. Those jobs do not establish
these genuine C7 kernels. The earlier rejected cache cleanup remains stopped;
the existing generated cache is local and untracked, without a clean-worktree or
integration claim.

## Remaining acceptance and nonclaims

Keep draft pending coordinator-routed independent source/result acceptance.
No denied successor review is replayed or rerouted. Actual compiler, runtime,
authenticated/B47, Effects/emitter, broker and held selectors remain UNRUN.
Required finite evidence remains:

1. Execute the complete original structural-vector kernel and new state kernel
   using the real current C7 source and provider, retaining every wide positive,
   late invalid, exact fact, observed-prefix, callback and partial-result control.
2. Revalidate actual primitive source/Core construction and the unchanged
   primitive artifact caller, including malformed source/graph inputs and
   ordered diagnostics. Direct helper execution is insufficient for this step.
3. Preserve parent native-carrier and existing shape/graph/typing controls;
   genuine parent and affected consumer acceptance is not inherited from source
   conservation. Other core/structural/inference accounting remains open.
4. Consume applicable CI, independently review the exact finite result and fresh
   main composition, then integrate only accepted identities through preflight.

No performance bound, whole-input admission, public source authentication,
whole accounting completion, independent acceptance, integration, release,
self-hosting, PR502 closure or expanded PR532 acceptance is claimed.
