# Syntax plan runtime extraction

This increment moves the pinned SH-04 syntax plan source checks, plan identity
checks, lazy cache construction and freshness checks, bounded runtime
execution, result admission and declared-slot digest resolution into
`gravity.syntax`. The existing bootstrap functions retain their arities and
supply explicit, trusted seed dependencies as callable Vars. They preserve
late interposition. The source and function pins remain unchanged.

D1, D2, D3, D9, L1, C1, C2, C3, C15 and TEST2 govern this boundary. The public
runtime API and each function's operation inputs are recorded in the namespace
contract attached to the public Vars. The bootstrap retains the cache holder
for its existing process lifetime; the stage constructs the delay and checks
fresh source and plan identities on admission. Operations are trusted host
composition, not caller proof authority. The dependency points from bootstrap
to syntax and from syntax to explicit operations and the existing verifier.
The stage does not import or resolve bootstrap.

Inputs are the pinned Gravity source, plan and function identities, generic
compiler and runtime operations, declared digest requests and reader binding.
Outputs are the unchanged authenticated plan binding, bounded Gravity results,
resolved syntax templates, digest identities and verification reports.
The stage owns C3-ID source/plan/digest refusals and C3-SHAPE runtime and result
containment through the existing diagnostic catalog and source-span producer.
Digest placeholders are interpreted only in declared identity slots; ordinary
source maps remain semantic data. Interruptions propagate with the thread
interrupt flag restored. Fatal host failures outside the existing contained
classes retain their previous behavior.

Development verification and independent acceptance are pending in this draft.
The recorded complete C3 artifact and pinned binding will be compared across
the extraction, along with genuine positive execution and negative fixtures.

This increment does not complete the entire syntax migration. SH-04 reader
binding, registered-literal projection, envelope binding, rich-object/stream
adaptation, graph adaptation and earlier helper boundaries remain open. Source
path and fixed pin configuration remain bootstrap composition. It grants no
language feature, migration, self-hosting, seed-retirement or release authority.
