# ACC-07 lexical reservation removal on current main

## Scope and requirement

Composition base: `06b5c4e4d48d3fd41d7621097e474033dbfca0b6`.
The selected implementation is the finite lexical reservation cut reviewed at
`398adad1103c5fab6fe86497ed50f458a51831d4`, not that branch's complete history.
ACC-07 still requires coherent inference allowance/debit/reservation removal
with fixed-point, cycle/conflict and genuine fresh verification preserved.

L2 immutable initializer semantics and L5/C7 type, source, proof and constraint
requirements remain. Current main's governing documents, sole task ledgers,
macro/loader behavior, other compiler changes and CI configuration are retained.
This includes the freshly integrated C6 assembly-evidence reuse. The earlier
provisional composition on `d0c793c1` is preserved as `6d104cdb`; it is not an
independently accepted increment or a second implementation to integrate.

## Selected source and test equations

The only production change is in `sh08-ft-infer-pass-products-prepared`:
remove `2 * count(lexical-bindings)`, its limit gate and debit, and
`:bounded-unit-access-lexical-work-limit`. The accepted get/nth v2 proof, known
non-Never type, immutable binding and exact initializer selection are unchanged.
No policy option, larger bound, substitute proof or unbounded iteration is added.

The actual existing
`sh08-bounded-unit-access-current-lexical-development-kernel` definition retains
all semantic controls. Its three budget-only refusal assertions and their two
local values are removed. Three assertions cover unrelated lexical records,
preserved selected type and unchanged remaining work count. All other current
main tests and production functions remain unchanged.

The standalone `sh08_lexical_reservation_model_test.clj` contains only the
reviewed lexical DATA contract and its controls. Their bodies are byte-identical
to the selected legacy increment; the namespace is new because main lacks the
older alias model file. It imports no prior alias or constructor models.

## Retained lineage and evidence scope

The older alias/constructor implementation, source-conjunction plumbing and
four alias-contract functions remain preserved in the PR536 lineage. They are
not dependencies of this lexical cut and are not imported here. Their reviewed
source/model evidence does not establish this current-main composition.
No old full C7 file, unaccepted policy family or cost requirement is restored.

The actual legacy kernel run at `398adad1` passed one test/30 assertions in a
fresh JVM. That result establishes its original source/configuration only.
The prior source/DATA review and terminal CI at merge `c4bd57cc` over `71a63ce`
are reused with their exact limits; neither is relabeled as current-main
semantic, source-composition or integration acceptance.

## Minimal fresh validation and current boundary

This source-only continuation permits inert reader/DATA, conservation, boundary
and diff checks. No actual target, Bootstrap, compiler, runtime, native, broker
or probe execution is performed. No old 700-graph audit or 1/30 kernel is rerun.

After one Accounting-routed affected source review, fresh semantic validation
should first use the existing exact lexical kernel on this new tuple. It covers
get/nth, v1/v2, padding, immutable/mutable, wrong initializer, pending/Never and
unvisited bindings. Further source validation is limited to authentic frontend
input reaching cold inference and the affected stored-value transfer. The
existing cold-source selector is a diagnostic prefix with an injected diagnostic
branch, not complete C7-product verification; do not claim broader authority
from it or impose unrelated C2/C3/native selectors or all ACC-07 closure.

Freeze the full mode/blob manifest, source/test equations, dependency/input pins
and limits for direct Accounting intake. No publication or merge is part of
this continuation. Current-main semantic/source acceptance, general convergence,
remaining global accounting, integration and full ACC-07 remain Open.
