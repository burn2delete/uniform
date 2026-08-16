# SH-07 B48 call arity leaf

This fixture pair exercises the bounded direct-call arity boundary over the
authenticated B47 function, call, and call-edge products.  Zero-, one-, and
fixed multi-argument calls are accepted when the actual argument vector exactly
matches the callee's `:fixed-arity`.  A lexical or nonlocal operator is carried
as `:pending-nonlocal`; it is not treated as a proof of callable or type
legality.

The companion rejected pair contains too-few and too-many direct calls.  The
fixtures are co-canonical `.gravity` and `.qst` source units and must remain
byte-identical.  This leaf does not claim variadic calls, multi-arity dispatch,
closures, call execution, type/effect legality, public routing, or seedless
execution.
